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
package es.uvigo.darwin.jmodeltest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Vector;

import pal.alignment.Alignment;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.model.ModelConstants;
import es.uvigo.darwin.prottest.util.exception.AlignmentParseException;
import es.uvigo.darwin.prottest.util.fileio.AlignmentReader;

/**
 * This class gathers the parameters of a single execution of jModelTest 2. It
 * is a singleton class.
 * 
 * @author Diego Darriba
 * 
 */
public class ApplicationOptions implements Serializable {

	private static final long serialVersionUID = -3961572952922591321L;

	/** Tree topology search algorithms */
	public static enum TreeSearch {
		NNI, SPR, BEST
	};

	private static ApplicationOptions instance;
	private static Vector<String> testOrder;

	private File inputDataFile, inputTreeFile;
	private File alignmentFile, treeFile;
	private File logFile;
	// Newick user tree
	private String userTree;

	/*
	 * Number of threads for shared memory execution or static thread scheduling
	 */
	private int numberOfThreads = Runtime.getRuntime().availableProcessors();
	/*
	 * Number of threads for dynamic thread scheduling
	 */
	private File machinesFile;

	public boolean threadScheduling = false;

	public boolean doAIC = false;
	public boolean doAICc = false;
	public boolean doBIC = false;
	public boolean doDT = false;
	public boolean doDLRT = false;
	public boolean doHLRT = false;
	// whether to include the parameter base frequencies
	public boolean doF = false;
	public boolean doI = false; // whether to include the parameter pinv
	public boolean doG = false; // whether to include gamma
	public int numGammaCat = 4;

	public boolean backwardHLRTSelection = false;
	public double confidenceLevelHLRT = 0.01;

	public boolean writePAUPblock = false;
	public boolean doImportances = false;
	public boolean doModelAveraging = false;
	public boolean doAveragedPhylogeny = false;
	public double confidenceInterval = 1.0; // by default include all models

	// for specific simulations
	public boolean doingSimulations = false;
	public String simulationsName = "";

	// whether to use the same user fixed topology for all calculations
	public boolean userTopologyExists = false;
	// whether to use the same BIONJ-JC fixed tree for all calculations
	public boolean fixedTopology = false;
	// whether to optimize the BIONJ-model tree by ML
	public boolean optimizeMLTopology = true;

	public TreeSearch treeSearchOperations = TreeSearch.NNI;

	public int numSites;
	public int numTaxa;
	public int numBranches;
	public int numModels;
	public int sampleSize;

	public String consensusType = "50% majority rule"; // consensus type
														// for model
														// averaged
														// phylogeny
	public boolean countBLasParameters = true; // whether to count branch
	// lengths as parameters

	private int substTypeCode = 0; // number of substitution types to
									// consider

	private ApplicationOptions() {
		try {
			logFile = File.createTempFile("jmodeltest-phyml", ".log");
			logFile.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// This method copies the input files into the scratch.
	// It is important to speed up the I/O in distributed memory.
	public void buildWorkFiles() throws IOException {
		File workDataFile = getAlignmentFile();
		if (!workDataFile.exists()) {
			workDataFile.createNewFile();
			workDataFile.deleteOnExit();
		}
		try {
			ModelTestService.readAlignment(inputDataFile, alignmentFile);

			Alignment alignment = AlignmentReader.readAlignment(
					new PrintWriter(System.err),
					alignmentFile.getAbsolutePath(), true);
			numTaxa = alignment.getSequenceCount();
			numSites = alignment.getSiteCount();
			numBranches = 2 * numTaxa - 3;
		} catch (AlignmentParseException e) {
			e.printStackTrace();
		}

		if (userTree != null) {
			File workTreeFile = getTreeFile();
			if (!workTreeFile.exists()) {
				workTreeFile.createNewFile();
				workTreeFile.deleteOnExit();
			}
			TextOutputStream out = new TextOutputStream(getTreeFile()
					.getAbsolutePath());
			out.print(getUserTree());
			out.close();
		}
	}

	public static ApplicationOptions getInstance() {
		if (instance == null) {
			instance = new ApplicationOptions();
		}
		return instance;
	}

	/****************************
	 * setCandidateModels *********************** * Build the set of candidate
	 * models * *
	 ************************************************************************/

	public void setCandidateModels() {
		int i, j;
		boolean includeModel;

		// fill in list of models
		ModelTest.setCandidateModels(new Model[numModels]);

		for (i = j = 0; i < ModelTest.MAX_NUM_MODELS; i++) {
			includeModel = true;
			if (ModelConstants.substType[i] > substTypeCode)
				includeModel = false;
			if (!doF && !ModelConstants.equalBaseFrequencies[i]) // is F
				includeModel = false;
			if (!doI && ModelConstants.rateVariation[i] == 1 || !doI
					&& ModelConstants.rateVariation[i] == 3)
				includeModel = false;
			if (!doG && ModelConstants.rateVariation[i] == 2 || !doG
					&& ModelConstants.rateVariation[i] == 3)
				includeModel = false;

			if (includeModel) {
				// System.out.println("Including model" + Model.modelName[i] +
				// " out of " + ModelTest.numModels + " models");
				loadModelConstraints(ModelTest.getCandidateModel(j), j, i);
				j++;
			}
		}

		if (ModelTest.buildGUI || ModelTest.testingOrder == null) {
			// set set of hypotheses for hLRTs in default order
			testOrder = new Vector<String>();
			// we need to reinitialize the hypotheses list in case it existed
			// before..
			testOrder.removeAllElements();

			if (doF)
				testOrder.add("freq");

			switch (substTypeCode) {
			case 0:
				testOrder.add("titv");
				testOrder.add("2ti4tv");
				break;
			case 1:
				testOrder.add("titv");
				testOrder.add("2ti");
				testOrder.add("2tv");
				break;
			case 2:
			case 3:
				testOrder.add("titv");
				testOrder.add("2ti");
				testOrder.add("2tv");
				testOrder.add("4tv");
			}

			if (doI)
				testOrder.add("pinv");

			if (doG)
				testOrder.add("gamma");

			// ModelTest.testingOrder.removeAllElements();

			ModelTest.testingOrder = testOrder;
		}
	}

	/************************
	 * loadModelConstraints ************************ * Loads initial definitions
	 * of models * * *
	 ***********************************************************************/

	public void loadModelConstraints(Model currentModel, int order, int modelNo) {
		int modelParameters, BL;
		boolean pF, pT, pV, pR, pI, pG;
		double lk, fA, fC, fG, fT, titv, kappa, Ra, Rb, Rc, Rd, Re, Rf, pinv, shape;

		// initialize
		lk = 0;
		pF = pT = pV = pR = pI = pG = false;
		fA = fC = fG = fT = 0.25;
		titv = 0.5;
		kappa = 1.0;
		Ra = Rb = Rc = Rd = Re = Rf = 1.0;
		pinv = 0;
		shape = ModelTest.INFINITY;

		if (!ModelConstants.equalBaseFrequencies[modelNo]) {
			pF = true;
		}

		if (ModelConstants.numTransitions[modelNo] == 1
				&& ModelConstants.numTransversions[modelNo] == 1) {
			pT = true;
		} else if (ModelConstants.numTransitions[modelNo] > 1
				|| ModelConstants.numTransversions[modelNo] > 1) {
			pR = true;
		}

		/*
		 * find out first wich models in phyml give titv if
		 * (Model.numTransitions[modelNo] == 2 ||
		 * Model.numTransversions[modelNo] == 2) { pV = true; }
		 */

		if (ModelConstants.rateVariation[modelNo] == 1) {
			pI = true;
		} else if (ModelConstants.rateVariation[modelNo] == 2) {
			pG = true;
		} else if (ModelConstants.rateVariation[modelNo] == 3) {
			pI = true;
			pG = true;
		}

		if (countBLasParameters)
			BL = numBranches;
		else
			BL = 0;

		if (optimizeMLTopology)
			modelParameters = ModelConstants.freeParameters[modelNo] + BL + 1;
		else
			modelParameters = ModelConstants.freeParameters[modelNo] + BL;

		ModelTest.getCandidateModels()[order] = new Model(order + 1,
				ModelConstants.modelName[modelNo],
				ModelConstants.modelCode[modelNo], lk, modelParameters, pF, pT,
				pV, pR, pI, pG, ModelConstants.numTransitions[modelNo],
				ModelConstants.numTransversions[modelNo], fA, fC, fG, fT, titv,
				kappa, Ra, Rb, Rc, Rd, Re, Rf, pinv, shape, /* numGammaCat, */
				false);
	}

	public File getInputFile() {
		return inputDataFile;
	}

	public File getInputTreeFile() {
		return inputTreeFile;
	}

	public void setInputFile(File file) {
		this.inputDataFile = file;
	}

	public void setInputTreeFile(File file) {
		this.inputTreeFile = file;
	}

	public File getAlignmentFile() {
		if (alignmentFile == null) {
			try {
				alignmentFile = File.createTempFile("jmodeltest", ".phy");
				alignmentFile.deleteOnExit();
			} catch (IOException e) {
				alignmentFile = inputDataFile;
			}
		}
		return alignmentFile;
	}

	public File getTreeFile() {
		if (treeFile == null) {
			try {
				treeFile = File.createTempFile("jmodeltest", ".tree");
				treeFile.deleteOnExit();
			} catch (IOException e) {
				treeFile = inputTreeFile;
			}

		}
		return treeFile;
	}

	public File getLogFile() {
		return logFile;
	}

	public String getUserTree() {
		return userTree;
	}

	public void setUserTree(String tree) {
		this.userTree = tree;
	}

	/**
	 * @param substTypeCode
	 *            the substTypeCode to set
	 */
	public void setSubstTypeCode(int substTypeCode) {
		this.substTypeCode = substTypeCode;
	}

	/**
	 * @return the substTypeCode
	 */
	public int getSubstTypeCode() {
		return substTypeCode;
	}

	public static void setInstance(ApplicationOptions newInstance) {
		File alignmentFile = instance.alignmentFile;
		File treeFile = instance.treeFile;
		instance = newInstance;
		instance.alignmentFile = alignmentFile;
		instance.treeFile = treeFile;
	}

	public int getNumberOfThreads() {
		return numberOfThreads;
	}

	public void setNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}

	public void setMachinesFile(File machinesFile) throws FileNotFoundException {
		if (ModelTest.HOSTS_TABLE.containsKey(ModelTest.getHostname())) {

			setNumberOfThreads((Integer) ModelTest.HOSTS_TABLE.get(ModelTest
					.getHostname()));

		} else {
			System.err.println("");
			System.err.println("WARNING: Machines File format is wrong.");
			System.err.println("         This host: " + ModelTest.getHostname()
					+ " does not exist");
			System.err.println("         Using a single thread");
			System.err.println("");
			this.numberOfThreads = 1;
		}
	}
	
	public File getMachinesFile() {
		return machinesFile;
	}
}
