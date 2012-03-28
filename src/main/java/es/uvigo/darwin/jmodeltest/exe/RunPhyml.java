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

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
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
 * @version 2.0.2 (Feb 2012)
 */
public abstract class RunPhyml extends Observable implements Observer {

	protected ApplicationOptions options;
	protected Model[] models;

	public static final String PHYML_VERSION = "3.0";

	public static String PHYML_TREE_SUFFIX = "_phyml_tree_";
	public static String PHYML_STATS_SUFFIX = "_phyml_stats_";

	protected Observer progress;
	protected ModelTest modelTest;

	public RunPhyml(Observer progress, ModelTest modelTest,
			Model[] models) {
		if (models != null)
			this.models = new Model[models.length];
		else
			this.models = new Model[0];
		for (int i = 0; i < this.models.length; i++)
			this.models[i] = models[i];
		this.modelTest = modelTest;
		this.options = modelTest.getApplicationOptions();
		this.progress = progress;
		this.addObserver(progress);
		Arrays.sort(this.models, new ModelComparator());
	}

	public void execute() {
		// remove stuff from exe directories before starting
		deleteFiles();
		printSettings(modelTest.getMainConsole());

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
						0, true, options);
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

		// compute likelihood scores for all models
		// System.out.print("computing likelihood scores for "
		// + models.length + " models with Phyml " + PHYML_VERSION);

		notifyObservers(ProgressInfo.OPTIMIZATION_INIT, 0, models[0], null);

		doPhyml();
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
			stream.print("User tree " + "("
					+ options.getInputTreeFile().getName() + ") = ");
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

	protected void deleteFiles() {
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

