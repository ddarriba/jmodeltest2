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
import java.util.Calendar;
import java.util.Vector;

import pal.alignment.Alignment;
import pal.datatype.DataType;
import pal.tree.TreeParseException;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.model.ModelConstants;
import es.uvigo.darwin.jmodeltest.selection.InformationCriterion;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;
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
	private static final int AMBIGUOUS_DATATYPE_STATE = 4;

	/** Tree topology search algorithms */
	public static enum TreeSearch {
		NNI, SPR, BEST
	};

	private static ApplicationOptions instance;
	private static Vector<String> testOrder;

	private File inputDataFile, inputTreeFile;
	private File alignmentFile, treeFile;
	private File ckpFile;
	private File logFile;
	private Alignment alignment;
	private boolean isAmbiguous;
	private boolean forceCheckULnL = false;
	private double unconstrainedLnL = 0.0d;
	private int numPatterns = 0;

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

	// Threshold for the guided search mode. A QST == 0.0 means no model
	// but the GTR one is optimized. A high QST means the whole set of
	// set of models will be optimized.
	private double guidedSearchThreshold = 0.0d;
	private boolean doClusteringSearch = false;
	private int heuristicInformationCriterion = InformationCriterion.IC_BIC;

	private int numSites;
	private int numTaxa;
	private int numBranches;
	private int numInvariableSites;
	private int numModels;

	private String executionName;
	
	public String consensusType = "50% majority rule"; // consensus type
														// for model
														// averaged
														// phylogeny
	public boolean countBLasParameters = true; // whether to count branch
	// lengths as parameters

	private int substTypeCode = 0; // number of substitution types to
									// consider
	private ApplicationOptions() { }

	public void createCkpFile() {
		if (ckpFile != null && ckpFile.exists())
			return;
		
		try {
			if (executionName == null) {
				executionName = Utilities.getCurrentTime("yyyyMMddHHmmss");
			}
			if (ModelTestConfiguration.isCkpEnabled() && getInputFile() != null) {
				ckpFile = new File(ModelTestConfiguration.getLogDir() + File.separator + getInputFile().getName() 
						+ "." + executionName + ".ckp");
			} else {
				ckpFile = File.createTempFile("jmodeltest", ".ckp");
				ckpFile.deleteOnExit();
			}
		} catch (IOException e) {
			System.err.println("Error creating checkpointing file");
		}
	}
	
	public void createLogFile() {
		try {
			if (executionName == null) {
				executionName = Utilities.getCurrentTime("yyyyMMddHHmmss");
			}
			if (ModelTestConfiguration.isPhymlLogEnabled() && getInputFile() != null) {
				logFile = new File(ModelTestConfiguration.getLogDir() + File.separator + getInputFile().getName() 
						+ ".phyml." + executionName + ".log");
			} else {
				logFile = File.createTempFile("jmodeltest-phyml", ".log");
				logFile.deleteOnExit();
			}
		} catch (IOException e) {
			System.err.println("Error creating PhyML log file");
		}
	}
	
	public void deleteLogFile() {
		if (!ModelTestConfiguration.isPhymlLogEnabled()) {
			logFile.delete();
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

			setAlignment(AlignmentReader.readAlignment(new PrintWriter(
					System.err), alignmentFile.getAbsolutePath(), true));
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
		boolean includeModel;

		// fill in list of models
		ModelTest.setCandidateModels(new Model[getNumModels()]);

		if (substTypeCode < 4) {
			int j = 0;
			for (int i = 0; i < ModelTest.MAX_NUM_MODELS; i++) {
				includeModel = true;
				if (ModelConstants.substType[i] > substTypeCode)
					includeModel = false;
				if (!doF && !ModelConstants.equalBaseFrequencies[i]) // is F
					includeModel = false;
				if (!doI
						&& (ModelConstants.rateVariation[i] == 1 || ModelConstants.rateVariation[i] == 3))
					includeModel = false;
				if (!doG
						&& (ModelConstants.rateVariation[i] == 2 || ModelConstants.rateVariation[i] == 3))
					includeModel = false;

				if (includeModel) {
					loadModelConstraints(ModelTest.getCandidateModel(j), j, i);
					j++;
				}
			}
		} else {
			int j = 0;
			for (int ratesCount = 1; ratesCount <= 6; ratesCount++) {
				for (String partition : ModelConstants.fullModelSet
						.get(ratesCount)) {
					boolean baseFrequencies;
					int rateVariation;
					for (int k = 0; k < 8; k++) {
						baseFrequencies = (k / 4 == 1);
						rateVariation = (k % 4);
						includeModel = true;
						if (!doF && baseFrequencies)
							includeModel = false;
						if (!doI && (rateVariation == 1 || rateVariation == 3))
							includeModel = false;
						if (!doG && (rateVariation == 2 || rateVariation == 3))
							includeModel = false;
						if (includeModel) {
							loadModelConstraints(
									ModelTest.getCandidateModel(j), j,
									partition, baseFrequencies, rateVariation,
									ratesCount);
							j++;
						}
					}
				}
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
			case 4:
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

		// initialize
		pT = pV = pR = pI = pG = false;

		pF = !ModelConstants.equalBaseFrequencies[modelNo];

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

		switch (ModelConstants.rateVariation[modelNo]) {
		case 1:
			pI = true;
			break;
		case 2:
			pG = true;
			break;
		case 3:
			pI = true;
			pG = true;
			break;
		}

		if (countBLasParameters)
			BL = getNumBranches();
		else
			BL = 0;

		if (optimizeMLTopology)
			modelParameters = ModelConstants.freeParameters[modelNo] + BL + 1;
		else
			modelParameters = ModelConstants.freeParameters[modelNo] + BL;

		ModelTest.getCandidateModels()[order] = new Model(order + 1,
				ModelConstants.modelName[modelNo],
				ModelConstants.modelCode[modelNo], modelParameters, pF, pT, pV,
				pR, pI, pG, ModelConstants.numTransitions[modelNo],
				ModelConstants.numTransversions[modelNo]);
		
		if (ModelTest.getLoadedModels() != null && ModelTest.getLoadedModels().length > 0) {
			for (Model model : ModelTest.getLoadedModels()) {
				if (model.getName().equals(ModelTest.getCandidateModels()[order].getName()) 
						&& model.getLnL() > 0.0) {
					/* load from checkpoint */
					Model otherModel = ModelTest.getCandidateModels()[order];
					otherModel.setLnL(model.getLnL());
					otherModel.setLnLIgnoringGaps(model.getLnLIgnoringGaps());
					otherModel.setShape(model.getShape());
					otherModel.setfA(model.getfA());
					otherModel.setfC(model.getfC());
					otherModel.setfG(model.getfG());
					otherModel.setfT(model.getfT());
					otherModel.setRa(model.getRa());
					otherModel.setRb(model.getRb());
					otherModel.setRc(model.getRc());
					otherModel.setRd(model.getRd());
					otherModel.setRe(model.getRe());
					otherModel.setRf(model.getRf());
					otherModel.setPinv(model.getPinv());
					otherModel.setKappa(model.getKappa());
					otherModel.setTitv(model.getTitv());
					try {
						otherModel.setTreeString(model.getTreeString());
					} catch (TreeParseException e) {
						e.printStackTrace();
					}
					model.setLnL(0.0);
					break;
				}
			}
		}
	}

	public void loadModelConstraints(Model currentModel, int order,
			String partition, boolean baseFrequencies, int rateVariation,
			int ratesCount) {
		int modelParameters, BL, nTi, nTv;
		boolean pF, pT, pV, pR, pI, pG;

		pT = pV = pR = pI = pG = false;

		pF = baseFrequencies;

		switch (rateVariation) {
		case 1:
			pI = true;
			break;
		case 2:
			pG = true;
			break;
		case 3:
			pI = true;
			pG = true;
			break;
		}

		if (countBLasParameters)
			BL = getNumBranches();
		else
			BL = 0;

		modelParameters = (ratesCount - 1) + (pF ? 3 : 0) + (pI ? 1 : 0)
				+ (pG ? 1 : 0) + BL + (optimizeMLTopology ? 1 : 0);

		String modelName;

		if (ModelConstants.codeList.contains(partition)) {
			int index = ModelConstants.codeList.indexOf(partition);
			if (pF)
				index += 4;
			if (pI)
				index += 1;
			if (pG)
				index += 2;
			modelName = ModelConstants.modelName[index];
			nTi = ModelConstants.numTransitions[index];
			nTv = ModelConstants.numTransversions[index];
			if (nTi == 1 && nTv == 1) {
				pT = true;
			} else if (nTi > 1 || nTv > 1) {
				pR = true;
			}
		} else {
			modelName = partition + (pI ? "+I" : "") + (pG ? "+G" : "")
					+ (pF ? "+F" : "");
			nTi = 0;// ModelConstants.getNumberOfTransitions(partition);
			nTv = 0;// ModelConstants.getNumberOfTransversions(partition);
			pT = false;
			pR = true;
		}
		ModelTest.getCandidateModels()[order] = new Model(order + 1, modelName,
				partition, modelParameters, pF, pT, pV, pR, pI, pG, 0, 0);
		
		if (ModelTest.getLoadedModels() != null && ModelTest.getLoadedModels().length > 0) {
			for (Model model : ModelTest.getLoadedModels()) {
				if (model.getName().equals(ModelTest.getCandidateModels()[order].getName()) 
						&& model.getLnL() > 0.0) {
					/* load from checkpoint */
					Model otherModel = ModelTest.getCandidateModels()[order];
					otherModel.setLnL(model.getLnL());
					otherModel.setLnLIgnoringGaps(model.getLnLIgnoringGaps());
					otherModel.setShape(model.getShape());
					otherModel.setfA(model.getfA());
					otherModel.setfC(model.getfC());
					otherModel.setfG(model.getfG());
					otherModel.setfT(model.getfT());
					otherModel.setRa(model.getRa());
					otherModel.setRb(model.getRb());
					otherModel.setRc(model.getRc());
					otherModel.setRd(model.getRd());
					otherModel.setRe(model.getRe());
					otherModel.setRf(model.getRf());
					otherModel.setPinv(model.getPinv());
					otherModel.setKappa(model.getKappa());
					otherModel.setTitv(model.getTitv());
					try {
						otherModel.setTreeString(model.getTreeString());
					} catch (TreeParseException e) {
						e.printStackTrace();
					}
					model.setLnL(0.0);
					break;
				}
			}
		}
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

	public Alignment getAlignment() {
		return alignment;
	}

	public void setAlignment(Alignment alignment) {
		this.alignment = alignment;
		setNumTaxa(alignment.getSequenceCount());
		setNumSites(alignment.getSiteCount());
		setNumBranches(2 * numTaxa - 3);
		setNumInvariableSites(Utilities.calculateInvariableSites(alignment));

		DataType dt = alignment.getDataType();
		// check ambiguity
		isAmbiguous = false;
		if (alignment.getDataType().isAmbiguous()) {
			isAmbiguous = true;
		} else {
			for (int i = 0; i < numTaxa; i++) {
				String seq = alignment.getAlignedSequenceString(i);
				if (seq.indexOf(Alignment.GAP) >= 0) {
					isAmbiguous = true;
					break;
				}
				for (int j = 0; j < seq.length(); j++) {
					if (dt.getState(seq.charAt(j)) == AMBIGUOUS_DATATYPE_STATE) {
						isAmbiguous = true;
						break;
					}
				}
			}
		}
	}

	public boolean isForceCheckULnL() {
		return forceCheckULnL;
	}

	public void setForceCheckULnL(boolean forceCheckULnL) {
		this.forceCheckULnL = forceCheckULnL;
	}

	public double getUnconstrainedLnL() {
		return unconstrainedLnL;
	}

	public void setUnconstrainedLnL(double unconstrainedLikelihood) {
		this.unconstrainedLnL = unconstrainedLikelihood;
	}

	public int getNumPatterns() {
		return numPatterns;
	}

	public void setNumPatterns(int numPatterns) {
		this.numPatterns = numPatterns;
	}

	public boolean isAmbiguous() {
		return isAmbiguous;
	}

	public String getExecutionName() {
		return executionName;
	}

	public void setExecutionName(String executionName) {
		this.executionName = executionName;
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

	public File getCkpFile() {
		return ckpFile;
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
		this.doClusteringSearch = (substTypeCode == 4);
	}

	/**
	 * @return the substTypeCode
	 */
	public int getSubstTypeCode() {
		return substTypeCode;
	}

	public double getSampleSize() {
		return numSites;
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

	public boolean isGuidedSearch() {
		return guidedSearchThreshold > 1e-6;
	}

	public double getGuidedSearchThreshold() {
		return guidedSearchThreshold;
	}

	public void setGuidedSearchThreshold(double guidedSearchThreshold) {
		this.guidedSearchThreshold = guidedSearchThreshold;
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

	public boolean isClusteringSearch() {
		return doClusteringSearch;
	}

	public void setClusteringSearch(boolean doClusteringSearch) {
		this.doClusteringSearch = doClusteringSearch;
	}

	public int getHeuristicInformationCriterion() {
		return heuristicInformationCriterion;
	}

	public void setHeuristicInformationCriterion(
			int heuristicInformationCriterion) {
		this.heuristicInformationCriterion = heuristicInformationCriterion;
	}

	public int getNumSites() {
		return numSites;
	}

	public void setNumSites(int numSites) {
		this.numSites = numSites;
	}

	public int getNumTaxa() {
		return numTaxa;
	}

	public void setNumTaxa(int numTaxa) {
		this.numTaxa = numTaxa;
	}

	public int getNumBranches() {
		return numBranches;
	}

	public void setNumBranches(int numBranches) {
		this.numBranches = numBranches;
	}

	public int getNumInvariableSites() {
		return numInvariableSites;
	}

	public void setNumInvariableSites(int numInvariableSites) {
		this.numInvariableSites = numInvariableSites;
	}

	public int getNumModels() {
		return numModels;
	}

	public void setNumModels(int numModels) {
		this.numModels = numModels;
	}
}
