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

import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import pal.misc.Identifier;
import pal.tree.ReadTree;
import pal.tree.Tree;
import pal.tree.TreeParseException;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.gui.XManager;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.selection.InformationCriterion;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;
import es.uvigo.darwin.prottest.consensus.Consensus;
import es.uvigo.darwin.prottest.facade.TreeFacade;
import es.uvigo.darwin.prottest.facade.TreeFacadeImpl;
import es.uvigo.darwin.prottest.tree.WeightedTree;
import es.uvigo.darwin.prottest.util.FixedBitSet;

public class RunConsense {
	
	private TextOutputStream stream;
	private Consensus consensus;
	
//	private String criterion;
	private String consensusType;
	private InformationCriterion criterion;
	private int numModels;
	private Model[] model;
	private int[] order;
	private double confidenceInterval;
	private Vector<Model> confidenceModels;

	private double[] w;
	private double[] cumw;
	private boolean[] isInInterval;

	public static es.uvigo.darwin.jmodeltest.threads.SwingWorker workerConsense;

	// constructor
	public RunConsense(InformationCriterion tcriterion, String tconsensusType,
			double minterval) {

		criterion = tcriterion;
		consensusType = tconsensusType;
		numModels = tcriterion.getNumModels();
		model = ModelTest.getCandidateModels();
		order = new int[numModels];
		confidenceInterval = minterval;
		confidenceModels = new Vector<Model>();

		w = new double[numModels];
		cumw = new double[numModels];
		isInInterval = new boolean[numModels];

		stream = ModelTest.getMainConsole();

		/**
		 * This action listener, called by the "Start" button, effectively forks
		 * the thread that does the work.
		 * 
		 * /* Invoking start() on the SwingWorker causes a new Thread to be
		 * creaconsense that will call construct(), and then finished(). Note
		 * that finished() is called even if the worker is interrupconsense
		 * because we catch the InterrupconsenseException in doConsense().
		 */
		if (ModelTest.buildGUI)
			System.out.println("\nComputing model averaged phylogeny ...");

		buildConfidenceInterval();
		consensus = doConsense();
		printConsensus();

		if (ModelTest.buildGUI) {
			XManager.getInstance().getPane().setCaretPosition(
					XManager.getInstance().getPane().getDocument()
					.getLength());
			System.out.println("OK");
		}

	}

	/**
	 * This method represents the application code that we'd like to run on a
	 * separate thread.
	 */
	// Object doConsense()
	private Consensus doConsense() {

		List<WeightedTree> treeList = new ArrayList<WeightedTree>();

		for (Model m : confidenceModels) {

			try {
				// parse tree
				String tree = m.getTreeString();
				StringReader sr = new StringReader(tree);
				Tree t = new ReadTree(new PushbackReader(sr));

				double weight;
				// set criterion
				switch (criterion.getType()) {
				case InformationCriterion.IC_AIC:
					weight = m.getAICw();
					break;
				case InformationCriterion.IC_AICc:
					weight = m.getAICcw();
					break;
				case InformationCriterion.IC_BIC:
					weight = m.getBICw();
					break;
				case InformationCriterion.IC_DT:
					weight = m.getDTw();
					break;
				default:
					weight = 0.0d;
				}

				treeList.add(new WeightedTree(t, weight));

			} catch (TreeParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		double consensusThreshold;
		if (consensusType.equals("strict")) {
			consensusThreshold = 0.99999999d;
		} else {
			consensusThreshold = 0.5d;
		}
		;
		Consensus consensus = new Consensus(treeList, consensusThreshold,
				Consensus.BRANCH_LENGTHS_MEDIAN);

		return consensus;
	} // doConsense

	/**************
	 * buildConfidenceInterval ************************
	 * 
	 * Builds the confidence interval of selected models and their cumulative
	 * weight
	 * 
	 * The model that just passed the confidence will be or not in the interval
	 * by chance (see below)
	 ****************************************************************/

	private void buildConfidenceInterval() {
		int i;
		Model tmodel;
		tmodel = model[0];

		order = criterion.order;

		// set alias
		for (i = 0; i < numModels; i++) {
			tmodel = model[order[i]];

			w[i] = criterion.getWeight(tmodel);
			cumw[i] = criterion.getCumWeight(tmodel);
			
		}

		// construct the confidence interval for models
		if (confidenceInterval == 1.0) {
			for (i = 0; i < numModels; i++) {
				tmodel = model[order[i]];
				isInInterval[i] = true;
				confidenceModels.add(tmodel);
			}
		} else {
			for (i = 0; i < numModels; i++) {
				tmodel = model[order[i]];

				// System.out.print("name=" + tmodel.name + " w=" + w[i] +
				// " cumw=" + cumw[i]);

				if (cumw[i] <= confidenceInterval) {
					isInInterval[i] = true;
					confidenceModels.add(tmodel);
				} else
					break;
			}

			// lets decide whether the model that just passed the confidence
			// interval should be included (suggested by John Huelsenbeck)
			double probOut = (tmodel.getCumAICw() - confidenceInterval)
					/ tmodel.getAICw();
			double probIn = 1.0 - probOut;
			Random generator = new Random();
			double randomNumber = generator.nextDouble();
			if (randomNumber <= probIn) {
				isInInterval[i] = true;
				confidenceModels.add(tmodel);
			} else
				isInInterval[i] = false;
		}
	}

	public double getConfidenceInterval() {
		return confidenceInterval;
	}
	
	public String getConsensusType() {
		return consensusType;
	}
	
	public List<Model> getConfidenceModels() {
		return confidenceModels;
	}
	
	public Tree getConsensus() {
		return consensus.getConsensusTree();
	}
	
	private void printConsensus() {
		
		double consensusThreshold = consensusType.equals("50% majority rule")?0.5:1.0;
		TreeFacade treeFacade = new TreeFacadeImpl();
		
		// print results for best AIC model
		stream.println(" ");stream.println(" ");stream.println(" ");
		stream.println("---------------------------------------------------------------");
		stream.println("*                                                             *");
		stream.println("*                    MODEL AVERAGED PHYLOGENY                 *");
		stream.println("*                                                             *");
		stream.println("---------------------------------------------------------------");

		if (criterion.getType() == InformationCriterion.IC_DT)
			Utilities
					.printRed("\nWarning: The DT weights used for this model averaged phylogeny are very gross"
							+ " and should be used with caution. See the program documentation.\n");
        stream.println(" ");		
        stream.println("Selection criterion: . . . . " + criterion);
        stream.print("Confidence interval: . . . . "); 
        stream.printf("%4.2f\n", confidenceInterval);
        stream.println("Consensus type:. . . . . . . " + consensusType);
        stream.println(" ");

     // print confidence set
		stream.println(" ");
		stream.print("Using " + confidenceModels.size() + " models in the ");
		stream.printf("%4.2f ", confidenceInterval);
		stream.print("confidence interval = ");
		for (Model m : confidenceModels) {
			stream.print(m.getName() + " ");
		}
		stream.println(" ");
		
        Set<FixedBitSet> keySet = consensus.getCladeSupport().keySet();
        List<FixedBitSet> splitsInConsensus = new ArrayList<FixedBitSet>();
        List<FixedBitSet> splitsOutFromConsensus = new ArrayList<FixedBitSet>();

        for (FixedBitSet fbs : keySet) {
            if (fbs.cardinality() > 1) {
                double psupport = (1.0 * consensus.getCladeSupport().get(fbs)) / 1.0;
                if (psupport < consensusThreshold) {
                    splitsOutFromConsensus.add(fbs);
                } else {
                    splitsInConsensus.add(fbs);
                }
            }
        }

        stream.println(" ");
        stream.println("Species in order:");
        stream.println(" ");

        int numTaxa = consensus.getIdGroup().getIdCount();
        for (int i = 0; i < numTaxa; i++) {
            Identifier id = consensus.getIdGroup().getIdentifier(i);
            stream.println("    " + (i + 1) + ". " + id.getName());
        }
 
        stream.println(" ");
        stream.println("Bipartitions included in the consensus tree");
        stream.println(" ");

        stream.println(consensus.getSetsIncluded());
        stream.println(" ");
        
        Tree consensusTree = consensus.getConsensusTree();
        stream.println(treeFacade.toASCII(consensusTree));
        stream.println(" ");
        String newickTree = treeFacade.toNewick(consensusTree, true, true, true);
        stream.println(newickTree);
        stream.println(" ");
        stream.println("Note: this tree is unrooted. Branch lengths are the expected number of "
        		+ "substitutions per site. Labels next to parentheses represent phylogenetic "
        		+ "uncertainty due to model selection (see documentation)");
	}

} // class RunConsense

