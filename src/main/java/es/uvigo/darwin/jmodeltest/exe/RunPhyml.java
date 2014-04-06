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

import java.io.File;
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

	public static final String PHYML_VERSION = "3.0";

	public static String PHYML_TREE_SUFFIX = "_phyml_tree_";
	public static String PHYML_STATS_SUFFIX = "_phyml_stats_";

	public static File phymlBinary;
	public static String phymlBinaryStr;
	private static String CURRENT_DIRECTORY = ModelTestConfiguration.PATH;
	private static boolean PHYML_GLOBAL = false;
	public static String PHYML_PATH = CURRENT_DIRECTORY + "exe/phyml/";

	
	protected Observer progress;

	static {
		if (PHYML_GLOBAL) {
			PHYML_PATH = "";
		} else {
			String path = ModelTestConfiguration.getExeDir();
			if (!path.startsWith(File.separator)) {
				PHYML_PATH = CURRENT_DIRECTORY + File.separator + path;
			} else {
				PHYML_PATH = path;
			}
			if (!PHYML_PATH.endsWith(File.separator)) {
				PHYML_PATH += File.separator;
			}
		}
		if (PHYML_GLOBAL) {
			phymlBinaryStr = "phyml";
		} else {
			phymlBinary = new File(PHYML_PATH + "phyml");
			if (phymlBinary.exists() && phymlBinary.canExecute()) {
				phymlBinaryStr = phymlBinary.getAbsolutePath();
			} else {
				phymlBinaryStr = PHYML_PATH + Utilities.getBinaryVersion();
			}
			/* Check if binary exists */
			phymlBinary = new File(phymlBinaryStr);
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

