/** 
 * RunPhyml.java
 *
 * Description:		Makes phyml calculate likelihood scores for competing models
 * @author			David Posada, University of Vigo, Spain  
 *					dposada@uvigo.es | darwin.uvigo.es
 * @version			1.0 (July 2006)
 */

package es.uvigo.darwin.jmodeltest.exe;

import java.util.Observable;
import java.util.Observer;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public abstract class RunPhyml extends Observable implements Observer {

	protected ApplicationOptions options;
	protected Model[] models;
	
	protected static final String PHYML_VERSION = "3.0";

	public static String PHYML_TREE_SUFFIX = "_phyml_tree_";
	public static String PHYML_STATS_SUFFIX;

	protected Observer progress;
	
	static {

		if (Utilities.isWindows())
			PHYML_STATS_SUFFIX = "_phyml_stat_";
		else
			PHYML_STATS_SUFFIX = "_phyml_stats_";
	}

	public RunPhyml(Observer progress, ApplicationOptions options, Model[] models) {
		this.models = models;
		this.options = options;
		this.progress = progress;
		this.addObserver(progress);
	}
	
	public void execute() {
		// remove stuff from exe directories before starting
		deleteFiles();
		printSettings(ModelTest.getMainConsole());
		
		// estimate a NJ-JC tree if needed
		if (options.fixedTopology) {
			notifyObservers(ProgressInfo.BASE_TREE_INIT, 0, models[0],
					null);

			PhymlSingleModel jcModel = new PhymlSingleModel(models[0],
					0, true, options);
			jcModel.run();

			// create JCtree file
			TextOutputStream JCtreeFile = new TextOutputStream(
					options.getTreeFile().getAbsolutePath(), false);
			JCtreeFile.print(models[0].getTreeString() + "\n");
			JCtreeFile.close();

			notifyObservers(ProgressInfo.BASE_TREE_COMPUTED, 0,
					models[0], null);

		}

		// compute likelihood scores for all models
		System.out.print("computing likelihood scores for "
				+ models.length + " models with Phyml " + PHYML_VERSION);
		
		doPhyml();
	}

	/***************************
	 * printSettings ***************************** * Prints the settings for the
	 * likelihood calculation * * *
	 ***********************************************************************/

	protected void printSettings(TextOutputStream stream) {

		stream.println(" ");stream.println(" ");
		stream.println("---------------------------------------------------------------");
		stream.println("*                                                             *");
		stream.println("*        COMPUTATION OF LIKELIHOOD SCORES WITH PHYML          *");
		stream.println("*                                                             *");
		stream.println("---------------------------------------------------------------");
		stream.println(" ");
		stream.println("Settings:");
		stream.println(" Phyml version = " + PHYML_VERSION);
		stream.println(" Phyml binary = " + Utilities.getBinaryVersion());
		stream.println(" Candidate models = " + models.length);
		stream.print("  number of substitution schemes = ");

		if (options.getSubstTypeCode() == 0)
			stream.println("3");
		else if (options.getSubstTypeCode() == 1)
			stream.println("5");
		else if (options.getSubstTypeCode() == 2)
			stream.println("7");
		else
			stream.println("11");

		if (options.doF)
			stream.println("  including models with equal/unequal base frequencies (+F)");
		else
			stream.println("  including only models with equal base frequencies");

		if (options.doI)
			stream.println("  including models with/without a proportion of invariable sites (+I)");
		else
			stream.println("  including only models without a proportion of invariable sites");

		if (options.doG)
			stream.println("  including models with/without rate variation among sites (+G)"
					+ " (nCat = " + options.numGammaCat + ")");
		else
			stream.println("  including only models without rate variation among sites");

		stream.print(" Optimized free parameters (K) =");
		stream.print(" substitution parameters");
		if (options.countBLasParameters)
			stream.print(" + " + options.numBranches + " branch lengths");
		if (options.optimizeMLTopology)
			stream.print(" + topology");
		stream.println(" ");

		stream.print(" Base tree for likelihood calculations = ");
		if (options.userTopologyExists) {
			stream.println("fixed user tree topology.");
			stream.println(" ");
			stream.print("User tree " + "(" + options.getInputTreeFile().getName() + ") = ");
			stream.println(options.getUserTree());
			stream.println(" ");
		}
		/*
		 * else if (ModelTest.userTreeExists)
		 * stream.println("fixed user tree (topology + branch lengths)");
		 */
		else if (options.fixedTopology)
			stream.println("fixed BIONJ-JC tree topology");
		else if (options.optimizeMLTopology)
			stream.println("ML tree");
		else
			stream.println("BIONJ tree");
		
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

