/*
Copyright (C) 2011  Diego Darriba, David Posada

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package es.uvigo.darwin.jmodeltest.exe;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.ModelTestConfiguration;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.model.ModelComparator;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

/**
 * RunPhyml.java
 * 
 * Description: Makes phyml calculate likelihood scores for competing models
 * 
 * @author Diego Darriba, University of Vigo / University of A Coruna, Spain
 *         ddarriba@udc.es
 * @author David Posada, University of Vigo, Spain dposada@uvigo.es |
 *         darwin.uvigo.es
 * @version 2.1 (May 2012)
 */
public abstract class RunPhyml extends Observable implements Observer {

	// Set of variables for tuning the guided search algorithm
	private static final boolean filterFrequencies = true;
	private static final boolean filterRateMatrix = true;
	private static final boolean filterRateVariation = true;

	protected ApplicationOptions options;
	protected Model[] models;
	protected Model gtrModel = null;

	public static final String[] COMPATIBLE_VERSIONS = {
		"20130103", "20131022", 
		"20141009", "20141029", 
		"20150501", "20151222"};

	public static String PHYML_VERSION = "3.0";

	public static String PHYML_TREE_SUFFIX = "_phyml_tree_";
	public static String PHYML_STATS_SUFFIX = "_phyml_stats_";

	public static File phymlBinary;
	public static String phymlBinaryStr;
	private static String CURRENT_DIRECTORY = ModelTestConfiguration.PATH;
	private static boolean PHYML_GLOBAL = ModelTestConfiguration.isGlobalPhymlBinary();
	public static String PHYML_PATH = CURRENT_DIRECTORY + "exe/phyml/";
	private static boolean compatiblePhyml = false;
	
	protected Observer progress;

	public static boolean isCompatible() {
		return compatiblePhyml;
	}
	
	public static boolean checkBinary() {
		boolean canExecute = false;
    if (!ModelTestConfiguration.isGlobalPhymlBinary()) {
      if (!RunPhyml.phymlBinary.exists()) {
        if (ModelTest.MPJ_ME == 0)
          Utilities.printRed("ERROR: PhyML binary cannot be found: "
              + RunPhyml.phymlBinary.getAbsolutePath() + "\n");
      } else if (!RunPhyml.phymlBinary.canExecute()) {
        if (ModelTest.MPJ_ME == 0)
          Utilities.printRed(
              "ERROR: PhyML binary exists, but it cannot be executed: "
                  + RunPhyml.phymlBinary.getAbsolutePath() + "\n");
      } else if (!RunPhyml.isCompatible()) {
        if (ModelTest.MPJ_ME == 0) {
          Utilities.printRed(
              "WARNING: PhyML binary is not in the list of compatibility: \n");
          Utilities.printRed(RunPhyml.phymlBinary.getAbsolutePath() + " v"
              + RunPhyml.PHYML_VERSION + "\n");
          Utilities.printRed("Compatible versions: ");
          for (int i = 0; i < RunPhyml.COMPATIBLE_VERSIONS.length; i++)
            Utilities.printBlue(RunPhyml.COMPATIBLE_VERSIONS[i] + " ");
          Utilities.printBlue("\n");
          Utilities.printRed(
              "jModelTest will try to continue execution anyway, but it might fail.\n");
        }
        canExecute = true;
      } else {
        if (ModelTest.MPJ_ME == 0)
          Utilities.printBlue(
              "PhyML binary: " + RunPhyml.phymlBinary.getAbsolutePath() + " v"
                  + RunPhyml.PHYML_VERSION + "\n");
        canExecute = true;
      }
    }
		return canExecute;
	}
	private static boolean checkPhymlCompatibility(String binary) {
		boolean binaryFound = false;
		if (Utilities.findCurrentOS() != Utilities.OS_LINUX)
			return false;
		// get process and execute command line
		String cmd[] = {binary, "--version"};
		Runtime rt = Runtime.getRuntime();
		Process proc;
		try {
			proc = rt.exec(cmd, null, null);
			InputStream stdin = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(stdin);
			BufferedReader br = new BufferedReader(isr);

			String line = null;
			while ( (line = br.readLine()) != null) {
				if (line.toLowerCase().contains("phyml version")) {
					String[] linesplit = line.trim().replace(".", "").split(" ");
					String version = linesplit[linesplit.length - 1];
					PHYML_VERSION = version;
					if (Arrays.asList(COMPATIBLE_VERSIONS).contains(version)) {
						compatiblePhyml = true;
					} else {
						compatiblePhyml = false;
					}
					binaryFound = true;
				}
			}
			br.close();
			isr.close();
			stdin.close();
			proc.destroy();
		} catch (IOException e) {
			return false;
		}
		return binaryFound;
	}
	
	public static boolean isPhymlGlobal() {
		return PHYML_GLOBAL;
	}
	
	static {
		if (PHYML_GLOBAL) {
			PHYML_PATH = "";
			phymlBinaryStr = "phyml";
		} else {
			/* check the local paths */
			String path = ModelTestConfiguration.getExeDir();
			if (!path.startsWith(File.separator)) {
				PHYML_PATH = CURRENT_DIRECTORY + File.separator + path;
			} else {
				PHYML_PATH = path;
			}
			if (!PHYML_PATH.endsWith(File.separator)) {
				PHYML_PATH += File.separator;
			}
			phymlBinary = new File(PHYML_PATH + "phyml");
			if (phymlBinary.exists() && phymlBinary.canExecute()) {
				phymlBinaryStr = phymlBinary.getAbsolutePath();
				compatiblePhyml = true;
			} else {
				phymlBinaryStr = PHYML_PATH + Utilities.getBinaryVersion();
			}
			/* Check if binary exists and is compatible */
			phymlBinary = new File(phymlBinaryStr);
			if (!checkPhymlCompatibility(phymlBinaryStr)) {
				/* Check for system wide PhyML */
				if (!(phymlBinary.exists() && phymlBinary.canExecute()))
				{
					if(checkPhymlCompatibility("/usr/bin/phyml")) {
						PHYML_PATH = "/usr/bin";
						phymlBinary = new File(PHYML_PATH + "/phyml");
						phymlBinaryStr = phymlBinary.getAbsolutePath();						
					}
				}
			}
		}
	}
	public RunPhyml(Observer progress, ApplicationOptions options,
			Model[] models) {
		if (models != null)
			this.models = new Model[models.length];
		else
			this.models = new Model[0];
		for (int i = 0; i < this.models.length; i++)
			this.models[i] = models[i];
		this.options = options;
		this.progress = progress;
		this.addObserver(progress);
		Arrays.sort(this.models, new ModelComparator());
	}

	public void execute() {
		// remove stuff from exe directories before starting
		deleteFiles();
		printSettings(ModelTest.getMainConsole());

		// locate GTR model
		String searchFor;
		int gtrParams;
		if (options.doI && options.doG) {
			searchFor="GTR+I+G";
			gtrParams = 10;
		} else if(options.doI) {
			searchFor="GTR+I";
			gtrParams = 9;
		} else if(options.doG) {
			searchFor="GTR+G";
			gtrParams = 9;
		} else {
			searchFor="GTR";
			gtrParams = 8;
		}
		for (int i = (models.length - 1); i >= 0; i--) {
			if (models[i].getName().startsWith(searchFor)) {
				gtrModel = models[i];
				break;
			}
		}
		if (gtrModel == null) {
			gtrModel = new Model(0, searchFor, "012345", gtrParams, false, false, false, true, options.doI, options.doG, 2, 4);
		}
		// estimate a NJ-JC tree if needed
		if (options.fixedTopology) {
			Model jcModel = null;
			for (Model model : models) {
				if (model.getName().equals("JC")) {
					jcModel = model;
					break;
				}
			}

			if (jcModel != null) {
				notifyObservers(ProgressInfo.BASE_TREE_INIT, 0, jcModel, null);

				PhymlSingleModel jcModelPhyml = new PhymlSingleModel(jcModel,
						0, true, false, options);
				jcModelPhyml.addObserver(this);
				jcModelPhyml.run();

				// create JCtree file
				TextOutputStream JCtreeFile = new TextOutputStream(options
						.getTreeFile().getAbsolutePath(), false);
				JCtreeFile.print(jcModel.getTreeString() + "\n");
				JCtreeFile.close();

				notifyObservers(ProgressInfo.BASE_TREE_COMPUTED, 0, jcModel,
						null);
			}

		}

		if (options.isGuidedSearch()) {

			if (gtrModel != null) {
				// compute GTR model
				notifyObservers(ProgressInfo.GTR_OPTIMIZATION_INIT, models.length, gtrModel, null);
				PhymlSingleModel gtrPhymlModel = new PhymlSingleModel(
						gtrModel, 0, false, false, options);
				gtrPhymlModel.run();
				notifyObservers(ProgressInfo.GTR_OPTIMIZATION_COMPLETED, models.length, gtrModel, null);
				
				GuidedSearchManager gsm = new GuidedSearchManager(
						options.getGuidedSearchThreshold(), gtrModel,
						filterFrequencies, filterRateMatrix,
						filterRateVariation);
	
				models = gsm.filterModels(models);
				ModelTest.setCandidateModels(models);
			} else {
				notifyObservers(ProgressInfo.GTR_NOT_FOUND, models.length, models[0], null);
			}
		}

		// compute likelihood scores for all models

		notifyObservers(ProgressInfo.OPTIMIZATION_INIT, 0, models[0], null);

		doPhyml();

	}

	public void executeIgnoreGaps(Model[] models) {
		notifyObservers(ProgressInfo.REOPTIMIZATION_INIT, models.length, models[0], null);
		parallelExecute(models, true);
		notifyObservers(ProgressInfo.REOPTIMIZATION_COMPLETED, models.length, null, null);
	}
	
	protected boolean parallelExecute(Model models[], boolean ignoreGaps) {
		
		ExecutorService threadPool = Executors.newFixedThreadPool(options
				.getNumberOfThreads());
		Collection<Callable<Object>> c = new ArrayList<Callable<Object>>();
		int current = 0;
		for (Model model : models) {
			if (model != null) {
				PhymlSingleModel psm = new PhymlSingleModel(model, current, false,
						ignoreGaps, options);
				psm.addObserver(this);
				c.add(Executors.callable(psm));
	
				current++;
			}
		}

		Collection<Future<Object>> futures = null;
		try {
			futures = threadPool.invokeAll(c);
		} catch (InterruptedException e) {
			notifyObservers(ProgressInfo.INTERRUPTED, 0, null, null);
		}

		if (futures != null) {
			for (Future<Object> f : futures) {
				try {
					f.get();
				} catch (InterruptedException ex) {
					notifyObservers(ProgressInfo.INTERRUPTED, 0, null, null);
					ex.printStackTrace();
					return false;
				} catch (ExecutionException ex) {
					// Internal exception while computing model.
					// Let's continue with errors
					ex.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}
	
	/***************************
	 * printSettings ***************************** * Prints the settings for the
	 * likelihood calculation * * *
	 ***********************************************************************/

	protected void printSettings(TextOutputStream stream) {

		stream.println(" ");
		stream.println(" ");
		stream.println("---------------------------------------------------------------");
		stream.println("*                                                             *");
		stream.println("*        COMPUTATION OF LIKELIHOOD SCORES WITH PHYML          *");
		stream.println("*                                                             *");
		stream.println("---------------------------------------------------------------");
		stream.println(" ");
		stream.println("::Settings::");
		stream.println(" ");
		stream.println(" Phyml version = " + PHYML_VERSION);
		stream.println(" Phyml binary = " + phymlBinary.getName());
		stream.println(" Phyml path = " + phymlBinary.getAbsolutePath()
				.substring(0, phymlBinary.getAbsolutePath().lastIndexOf(File.separator)) + File.separator);
		stream.println(" Candidate models = " + models.length);
		stream.print("   number of substitution schemes = ");

		if (options.getSubstTypeCode() == 0)
			stream.println("3");
		else if (options.getSubstTypeCode() == 1)
			stream.println("5");
		else if (options.getSubstTypeCode() == 2)
			stream.println("7");
		else
			stream.println("11");

		if (options.doF)
			stream.println("   including models with equal/unequal base frequencies (+F)");
		else
			stream.println("   including only models with equal base frequencies");

		if (options.doI)
			stream.println("   including models with/without a proportion of invariable sites (+I)");
		else
			stream.println("   including only models without a proportion of invariable sites");

		if (options.doG)
			stream.println("   including models with/without rate variation among sites (+G)"
					+ " (nCat = " + options.numGammaCat + ")");
		else
			stream.println("   including only models without rate variation among sites");

		stream.print(" Optimized free parameters (K) =");
		stream.print(" substitution parameters");
		if (options.countBLasParameters)
			stream.print(" + " + options.getNumBranches() + " branch lengths");
		if (options.optimizeMLTopology)
			stream.print(" + topology");
		stream.println(" ");

		stream.print(" Base tree for likelihood calculations = ");
		if (options.userTopologyExists) {
			stream.println("fixed user tree topology.");
			stream.println(" ");
			stream.print("User tree " + "("
					+ options.getInputTreeFile().getName() + ") = ");
			stream.println(options.getUserTree());
			stream.println(" ");
		} else if (options.fixedTopology) {
			stream.println("fixed BIONJ-JC tree topology");
		} else if (options.optimizeMLTopology) {
			stream.println("ML tree");
		} else {
			stream.println("BIONJ tree");
		}

		if (options.optimizeMLTopology) {
			stream.print(" Tree topology search operation = ");
			switch (options.treeSearchOperations) {
			case NNI:
				stream.println("NNI");
				break;
			case SPR:
				stream.println("SPR");
				break;
			case BEST:
				stream.println("BEST");
				break;
			}
		}
		if (options.isClusteringSearch()) {
			stream.println(" Using hill-climbing hierarchical clustering");
		}
		
		if (options.isGuidedSearch()) {
			stream.println(" Using heuristic model filtering ");
		}
		stream.println(" ");
	}

	protected abstract Object doPhyml();

	/***********************************************************************
	 * interruptThread
	 * 
	 * Interrupts the calculation of likelihood scores
	 ***********************************************************************/
	public void interruptThread() {
		notifyObservers(ProgressInfo.OPTIMIZATION_COMPLETED_INTERRUPTED, 0,
				null, null);
		// workerPhyml.interrupt();
		// IS_INTERRUPTED = true;
	}

	private void deleteFiles() {
		/* phymlFolder */
		//if (options.getLogFile() != null)
			options.getLogFile().delete();
	}

	protected void notifyObservers(int type, int value, Model model,
			String message) {
		setChanged();
		notifyObservers(new ProgressInfo(type, value, model, message));
	}

	@Override
	public void update(Observable o, Object arg) {
		setChanged();
		notifyObservers(arg);
	}

} // class RunPhyml

