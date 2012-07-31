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
package es.uvigo.darwin.jmodeltest.selection;

import java.util.Enumeration;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.statistics.Statistics;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public class HLRT {
	private ApplicationOptions options;
	public final double MAX_PROB = 0.999999;
	public final double MIN_PROB = 0.000001;
	public double alphaLRT;
	public TextOutputStream stream = ModelTest.getMainConsole();

	public int TPMnumber;
	public int TIMnumber;

	// constructor
	public HLRT(ApplicationOptions options) {
		this.options = options;
		TPMnumber = 0;
		TIMnumber = 0;
	}

	/****************************
	 * compute ********************************* * Builds and tests the
	 * hierarchy of LRTs accordingly to the starting * model and adding or
	 * removing parameters in a particular order * *
	 ***********************************************************************/

	public void compute(boolean forward, double alpha, boolean writePAUPblock) {

		int i;
		double P;
		Model currentModel, nullModel, altModel, competingModel;
		String[] hypotheses;

		alphaLRT = alpha;
		hypotheses = new String[ModelTest.testingOrder.size()];
		i = 0;

		for (Enumeration<String> e = ModelTest.testingOrder.elements(); e
				.hasMoreElements();)
			hypotheses[i++] = e.nextElement();

		if (ModelTest.buildGUI)
			System.out.print("computing hLRT ... ");

		if (forward)
			currentModel = ModelTest.getCandidateModels()[0];
		else
			currentModel = ModelTest.getCandidateModels()[options
					.getNumModels() - 1];

		stream.println("\n\n\n---------------------------------------------------------------");
		stream.println("*                                                             *");
		stream.println("*          HIERARCHICAL LIKELIHOO RATIO TESTS (hLRT)          *");
		stream.println("*                                                             *");
		stream.println("---------------------------------------------------------------");

		if (options.getSubstTypeCode() >= 4) {
			stream.println("\nhLRT is not available for the 203 substitution scheme.");
			stream.println("Please use one of the other available selection criteria (AIC, BIC, AICc or DT).");
			return;
		}

		if (options.fixedTopology | options.userTopologyExists) {
			stream.println("\nSettings: ");
			if (forward)
				stream.println("  Forward selection (adding parameters)");
			else
				stream.println("  Backward selection (removing parameters)");

			stream.println("    starting model = " + currentModel.getName());
			stream.print("    hypotheses order = ");
			for (i = 0; i < hypotheses.length; i++) {
				stream.print(hypotheses[i]);
				if (i < hypotheses.length - 1)
					stream.print("-");
			}
			stream.print("\n  Confidence alpha level = ");
			stream.printf("%6.4f\n", alphaLRT);

			nullModel = null;
			altModel = null;

			for (i = 0; i < hypotheses.length; i++) {
				if (forward)
					nullModel = currentModel;
				else
					altModel = currentModel;

				stream.println("\n* Tested hypothesis = " + hypotheses[i]);
				competingModel = findCompetingModel(currentModel,
						hypotheses[i], forward);
				if (competingModel == null) {
					stream.println("Cannot be tested. No competing model differs just by "
							+ hypotheses[i]
							+ " from "
							+ currentModel.getName()
							+ ".\nRevise the order of hypotheses selected for the LRTs."
							+ "\n...Skipping to the next hypothesis");
					// System.err.println("hypothesis = " + hypotheses[i] +
					// " -- not found best competing model");
					continue;
				}

				// System.err.println("hypothesis = " + hypotheses[i] +
				// " -- found best competing model = " + competingModel.name);

				if (ApplicationOptions.getInstance().getSubstTypeCode() == 3)
					if (competingModel.getName().startsWith("TIM")
							|| competingModel.getName().startsWith("TPM"))
						competingModel = BestTIMTPM(competingModel);

				if (forward)
					altModel = competingModel;
				else
					nullModel = competingModel;

				stream.printf("Null model        = %-8s", nullModel.getName());
				stream.printf("\t-lnL = %6.4f", nullModel.getLnL());
				stream.printf("\nAlternative model = %-8s", altModel.getName());
				stream.printf("\t-lnL = %6.4f", altModel.getLnL());
				stream.println();

				if (hypotheses[i].equals("gamma")
						|| hypotheses[i].equals("pinv"))
					P = LRTboundary(nullModel, altModel);
				else
					P = LRT(nullModel, altModel);

				if (forward) {
					if (P < alphaLRT)
						currentModel = altModel;
					else {
						stream.println("\nThe current model could not be rejected");
						if (ModelTest.buildGUI)
							Utilities.toConsoleEnd();
						break;
					}
				} else {
					if (P > alphaLRT)
						currentModel = nullModel;
					else {
						stream.println("\nThe current model rejected the null model");
						if (ModelTest.buildGUI)
							Utilities.toConsoleEnd();
						break;
					}
				}

				if (ModelTest.buildGUI)
					Utilities.toConsoleEnd();
			}

			ModelTest.setMinHLRT(currentModel);

			stream.println("\n Model selected: ");
			ModelTest.getMinHLRT().print(stream);

			// print ML tree for best-fit model
			if (ApplicationOptions.getInstance().optimizeMLTopology)
				stream.println("\nML tree (NNI) for the best hLRT model = "
						+ ModelTest.getMinHLRT().getTreeString());

			// print PAUP* block
			if (writePAUPblock)
				ModelTest
						.WritePaupBlock(stream, "hLRT", ModelTest.getMinHLRT());

		} else {
			stream.println("\nhLRT is only available when likelihoods are calculated on the same tree (i.e., models are nested)");
			stream.println("Execute jModelTest using a fixed BIONJ-JC tree or a user-defined topology");
		}

		if (ModelTest.buildGUI) {
			Utilities.toConsoleEnd();
			System.out.println("OK");
		}
	}

	/****************************
	 * computeDynamical ************************ * Builds and tests the
	 * hierarchy of LRTs in a dynamic fashion -- finding the best step to take
	 * (biggest increase in lnL) -- * and adding or removing parameters in a
	 * particular order * *
	 ***********************************************************************/

	public void computeDynamical(boolean forward, double alpha,
			boolean writePAUPblock) {

		int i, j, bestHypothesisIndex;
		double P;
		Model currentModel, nullModel, altModel, competingModel, bestCompetingModel;
		String[] hypotheses, trimmedHypotheses;
		boolean thereAreValidHypotheses;
		double lnDifference, bestLnDifference;

		alphaLRT = alpha;
		hypotheses = new String[ModelTest.testingOrder.size()];
		i = 0;
		for (String s : ModelTest.testingOrder)
			hypotheses[i++] = s;

		if (ModelTest.buildGUI)
			System.out.print("computing dynamical LRTs ... ");

		if (forward)
			currentModel = ModelTest.getCandidateModels()[0];
		else
			currentModel = ModelTest.getCandidateModels()[options
					.getNumModels() - 1];

		stream.println("\n\n\n---------------------------------------------------------------");
		stream.println("*                                                             *");
		stream.println("*          DYNAMICAL LIKELIHOO RATIO TESTS (dLRT)             *");
		stream.println("*                                                             *");
		stream.println("---------------------------------------------------------------");

		stream.println("\nSettings: ");
		if (forward)
			stream.println("  Forward selection (adding parameters)");
		else
			stream.println("  Backward selection (removing parameters)");

		stream.println("    starting model = " + currentModel.getName());
		stream.print("    hypotheses = ");
		for (i = 0; i < hypotheses.length; i++) {
			stream.print(hypotheses[i]);
			if (i < hypotheses.length - 1)
				stream.print(", ");
		}
		stream.print("\n  Confidence alpha level = ");
		stream.printf("%6.4f\n", alphaLRT);

		nullModel = null;
		altModel = null;

		thereAreValidHypotheses = true;
		while (thereAreValidHypotheses) {
			bestCompetingModel = null;
			bestHypothesisIndex = 0;

			if (forward) {
				nullModel = currentModel;
				bestLnDifference = 0;
			} else {
				altModel = currentModel;
				bestLnDifference = 1000000;
			}

			// chose best competing model across hypotheses
			for (i = 0; i < hypotheses.length; i++) {
				lnDifference = 0;
				competingModel = findCompetingModel(currentModel,
						hypotheses[i], forward);
				if (competingModel != null) {
					if (ApplicationOptions.getInstance().getSubstTypeCode() == 3)
						if (competingModel.getName().startsWith("TIM")
								|| competingModel.getName().startsWith("TPM"))
							competingModel = BestTIMTPM(competingModel);
					lnDifference = Math.abs(currentModel.getLnL()
							- competingModel.getLnL());
					if ((forward && lnDifference > bestLnDifference)
							|| (!forward && lnDifference < bestLnDifference)) {
						bestCompetingModel = competingModel;
						bestLnDifference = lnDifference;
						bestHypothesisIndex = i;
					}
				}
			}

			// we should not be here
			if (bestCompetingModel == null) {
				stream.print("\nNo best competing model for any of the "
						+ hypotheses.length + " remaining hypotheses: ");
				for (i = 0; i < hypotheses.length; i++) {
					stream.print(hypotheses[i]);
					if (i < hypotheses.length - 1)
						stream.print(" + ");
				}
				stream.println(", given the current model = "
						+ currentModel.getName());
				// System.err.println("hypothesis = " +
				// hypotheses[bestHypothesisIndex] +
				// " -- not found best competing model");
				thereAreValidHypotheses = false;
				continue;
			}

			/*
			 * stream.println("\nBest hypothesis = " +
			 * hypotheses[bestHypothesisIndex] + "\n current model = " +
			 * currentModel.name + "\n competing model = " +
			 * bestCompetingModel.name + "\n best lnL increase = " +
			 * bestLnDifference);
			 */

			// System.err.println("hypothesis = " +
			// hypotheses[bestHypothesisIndex] +
			// " -- found best competing model = " + bestCompetingModel.name);

			if (forward)
				altModel = bestCompetingModel;
			else
				nullModel = bestCompetingModel;

			stream.println("\nTesting " + hypotheses[bestHypothesisIndex]
					+ " hypothesis");
			stream.printf("Null model        = %-8s", nullModel.getName());
			stream.printf("\t-lnL = %6.4f", nullModel.getLnL());
			stream.printf("\nAlternative model = %-8s", altModel.getName());
			stream.printf("\t-lnL = %6.4f", altModel.getLnL());
			stream.println();

			// test this best hypothesis
			if (hypotheses[bestHypothesisIndex].equals("gamma")
					|| hypotheses[bestHypothesisIndex].equals("pinv"))
				P = LRTboundary(nullModel, altModel);
			else
				P = LRT(nullModel, altModel);

			if (forward) {
				if (P < alphaLRT)
					currentModel = altModel;
				else {
					stream.println("\nThe current model could not be rejected");
					if (ModelTest.buildGUI)
						Utilities.toConsoleEnd();
					break;
				}
			} else {
				if (P > alphaLRT)
					currentModel = nullModel;
				else {
					stream.println("\nThe current model rejected the null model");
					if (ModelTest.buildGUI)
						Utilities.toConsoleEnd();
					break;
				}
			}

			// remove the hypothesis tested
			trimmedHypotheses = new String[hypotheses.length - 1];
			for (i = j = 0; i < hypotheses.length; i++) {
				if (i < bestHypothesisIndex)
					trimmedHypotheses[j++] = hypotheses[i].toString();
				else if (i > bestHypothesisIndex)
					trimmedHypotheses[j++] = hypotheses[i].toString();
			}
			hypotheses = trimmedHypotheses;

			if (hypotheses.length <= 0)
				thereAreValidHypotheses = false;

			if (ModelTest.buildGUI)
				Utilities.toConsoleEnd();
		}

		ModelTest.setMinDLRT(currentModel);

		stream.println("\n Model selected: ");
		ModelTest.getMinDLRT().print(stream);

		// print PAUP* block
		if (writePAUPblock)
			ModelTest.WritePaupBlock(stream, "dLRT", ModelTest.getMinDLRT());

		if (ModelTest.buildGUI) {
			Utilities.toConsoleEnd();
			System.out.println("OK");
		}
	}

	/****************************
	 * findCompetingModel*********************** * Finds the corresponding
	 * competing model given the current * model and hypothesis that is being
	 * tested. The null model * has to be nested within the alternative, and
	 * they can only differ * by one set of parameters * *
	 ***********************************************************************/

	public Model findCompetingModel(Model current, String test, boolean adding) {
		int distance;
		boolean isTest;
		Model competing, found;

		found = null;
		for (Model model : ModelTest.getCandidateModels()) {
			competing = model;
			isTest = false;
			distance = 0;

			// if step forward compare only with more complex models
			if (adding && current.getK() - competing.getK() >= 0)
				continue;
			// if step down compare only with simpler models
			else if (!adding && current.getK() - competing.getK() <= 0)
				continue;

			if (current.ispF() != competing.ispF()) {
				if (test.equals("freq"))
					isTest = true;
				distance++;
			}
			if ((!current.ispT() && competing.ispT())
					|| (!competing.ispT() && current.ispT())) {
				if (test.equals("titv"))
					isTest = true;
				if (!current.ispR() && !competing.ispR()) // because we will
															// increase this
															// distance later
															// for models with
															// pR
					distance++;
			}
			if (current.ispG() != competing.ispG()) {
				if (test.equals("gamma"))
					isTest = true;
				distance++;
			}
			if (current.ispI() != competing.ispI()) {
				if (test.equals("pinv"))
					isTest = true;
				distance++;
			}

			// because JC and GTR have the same pT ...
			if (current.ispT() == competing.ispT()
					&& current.ispR() != competing.ispR())
				isTest = false;

			if (ApplicationOptions.getInstance().getSubstTypeCode() == 0) {
				if ((Math.abs(competing.getNumTi() - current.getNumTi()) == 1)
						&& (Math.abs(competing.getNumTv() - current.getNumTv()) == 3)) {
					if (test.equals("2ti4tv"))
						isTest = true;
					distance++;
				}
			} else if (((adding) && (current.ispT() || current.ispR()))
					|| ((!adding) && (competing.ispT() || competing.ispR()))) {
				if (Math.abs(competing.getNumTi() - current.getNumTi()) == 1) {
					if (test.equals("2ti"))
						isTest = true;
					distance++;
				}
				if (Math.abs(competing.getNumTv() - current.getNumTv()) == 1) {
					if (test.equals("2tv"))
						isTest = true;
					distance++;
				} else if (Math.abs(competing.getNumTv() - current.getNumTv()) == 2) {
					if (test.equals("4tv"))
						isTest = true;
					distance++;
				} else if (Math.abs(competing.getNumTv() - current.getNumTv()) == 3)
					isTest = false;
			}

			if (isTest && distance == 1) {
				found = model;
				break;
			}

			// System.err.println("test = " + test + "   current = " +
			// current.name + "  competing = " + competing.name +
			// "  isTest = " + isTest + "  distance = " + distance);

		}

		return found;
	}

	/****************************
	 * BestTIMTPM ******************************* * Returns the corresponding
	 * individual model within the families * TPM, TPMuf, TIM and TIMef * *
	 *************************************************************************/

	public Model BestTIMTPM(Model whichModel) {
		Model foundModel = null;
		String name, model1, model2, model3;

		name = whichModel.getName();
		model1 = name.replaceFirst("[0-9]", "1");
		model2 = name.replaceFirst("[0-9]", "2");
		model3 = name.replaceFirst("[0-9]", "3");

		// System.err.println ("Find best among " + model1 + " and " + model2 +
		// " and " + model3);
		foundModel = findBest(model1, model2, model3);
		// System.err.println ("Found :" + foundModel.name);

		return foundModel;
	}

	/****************************
	 * findBest ******************************* * Finds the model with the
	 * highest likelihood out of three * TIM or TPM given models * *
	 ************************************************************************/

	public Model findBest(String modelName1, String modelName2,
			String modelName3) {
		Model m1, m2, m3;

		m1 = m2 = m3 = null;

		for (Model model : ModelTest.getCandidateModels()) {
			if (modelName1.equals(model.getName()))
				m1 = model;
			else if (modelName2.equals(model.getName()))
				m2 = model;
			else if (modelName3.equals(model.getName()))
				m3 = model;
		}

		// check if we already know the familiy number
		if (m1.getName().startsWith("TIM") && TIMnumber > 0) {
			if (TIMnumber == 1)
				return m1;
			else if (TIMnumber == 2)
				return m2;
			else
				return m3;
		} else if (m1.getName().startsWith("TPM") && TPMnumber > 0) {
			if (TPMnumber == 1)
				return m1;
			else if (TPMnumber == 2)
				return m2;
			else
				return m3;
		}

		// otherwise we will select it now
		stream.println("Selecting first best representative model among:");
		stream.println("  " + m1.getName() + " (-lnL = " + m1.getLnL() + ")");
		stream.println("  " + m2.getName() + " (-lnL = " + m2.getLnL() + ")");
		stream.println("  " + m3.getName() + " (-lnL = " + m3.getLnL() + ")");

		if (m1.getLnL() <= m2.getLnL() && m1.getLnL() <= m3.getLnL()) {
			if (m1.getName().startsWith("TIM"))
				TIMnumber = 1;
			else
				TPMnumber = 1;
			return m1;
		} else if (m2.getLnL() <= m1.getLnL() && m2.getLnL() <= m3.getLnL()) {
			if (m2.getName().startsWith("TIM"))
				TIMnumber = 2;
			else
				TPMnumber = 2;
			return m2;
		} else {
			if (m3.getName().startsWith("TIM"))
				TIMnumber = 3;
			else
				TPMnumber = 3;
			return m3;
		}
	}

	/****************************
	 * LRT ************************************* * Computes a likelihood ratio
	 * test between two model and returns * a P-value according to a standard
	 * chi2 distribution * *
	 ***********************************************************************/

	private double LRT(Model model0, Model model1) {
		double delta, prob;
		int df;

		delta = 2 * (model0.getLnL() - model1.getLnL());
		df = model1.getK() - model0.getK();

		if (delta == 0)
			prob = 1.0;
		else
			prob = Statistics.chiSquareProbability(delta, df);

		stream.printf("2(lnL1-lnL0)      = %6.4f", delta);
		if (prob == 1.0)
			stream.println("\tP-value > " + MAX_PROB);
		else if (prob < 0.000001)
			stream.println("\tP-value < " + MIN_PROB);
		else
			stream.printf("\tP-value = %6.4f\n", prob);

		return prob;
	}

	/***************************
	 * LRTboundary******************************* * Computes a likelihood ratio
	 * test between two model and returns * a P-value according to a mixed chi2
	 * distribution * *
	 ***********************************************************************/

	private double LRTboundary(Model model0, Model model1) {
		double delta, prob;
		int df;

		delta = 2 * (model0.getLnL() - model1.getLnL());
		df = model1.getK() - model0.getK();

		if (delta == 0)
			prob = 1.0;
		else {
			if (df == 1)
				prob = Statistics.chiSquareProbability(delta, df) / 2;
			else
				prob = (Statistics.chiSquareProbability(delta, df - 1) + Statistics
						.chiSquareProbability(delta, df)) / 2;
		}

		stream.printf("2(lnL1-lnL0)      = %6.4f", delta);
		if (prob == 1.0)
			stream.println("\tP-value > " + MAX_PROB);
		else if (prob < 0.000001)
			stream.println("\tP-value < " + MIN_PROB);
		else
			stream.printf("\tP-value = %6.4f\n", prob);

		return prob;
	}

} // class hLRT