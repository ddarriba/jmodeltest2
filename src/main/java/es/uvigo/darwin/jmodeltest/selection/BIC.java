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

import java.util.Random;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;

public class BIC extends InformationCriterion {

	// constructor
	public BIC(boolean mwritePAUPblock, boolean mdoImportances,
			boolean mdoModelAveraging, double minterval) {
		super(mwritePAUPblock, mdoImportances, mdoModelAveraging, minterval);
	}

	/****************************
	 * compute ******************************* * Computes the Bayesian
	 * Information Criterion (BIC) for every model * and finds out the model
	 * with the minimum BIC * *
	 ************************************************************************/

	public void compute() {

		boolean sorted;
		int i, temp2, pass;
		double[] tempw = new double[numModels];
		double min, sumExp, cum, temp1;

		// Calculate BIC and minBIC
		min = computeBic(models[0], options);
		minModel = models[0];
		
		if (doCheckAgainstULK) {
			unconstrainedModel.setBIC(computeSingle(unconstrainedModel));
		}
		for (Model model : models) {
			model.setBIC(computeBic(model, options));

			if (model.getBIC() < min) {
				min = model.getBIC();
				minModel = model;
			}
			
			if (doCheckAgainstULK) {
				model.setUBICd(model.getBIC() - unconstrainedModel.getBIC());
			}
		}

		// Calculate BIC differences
		sumExp = 0;
		for (i = 0; i < numModels; i++) {
			models[i].setBICd(models[i].getBIC() - minModel.getBIC());
			sumExp += Math.exp(-0.5 * models[i].getBICd());
		}

		// Calculate BIC weights
		for (i = 0; i < numModels; i++) {
			if (models[i].getBICd() > 1000)
				models[i].setBICw(0.0);
			else
				models[i].setBICw(Math.exp(-0.5 * models[i].getBICd()) / sumExp);
			tempw[i] = models[i].getBIC();
			order[i] = i;
		}

		// Get order for BIC weights and calculate cumWeigths
		sorted = false;
		pass = 1;
		while (!sorted) {
			sorted = true;
			for (i = 0; i < (numModels - pass); i++)
				if (tempw[i] > tempw[i + 1]) {
					temp1 = tempw[i + 1];
					tempw[i + 1] = tempw[i];
					tempw[i] = temp1;

					temp2 = order[i + 1];
					order[i + 1] = order[i];
					order[i] = temp2;

					sorted = false;
				}
			pass++;
		}

		cum = 0;
		for (i = 0; i < numModels; i++) {
			cum += models[order[i]].getBICw();
			models[order[i]].setCumBICw(cum);
		}

		// confidence interval
		buildConfidenceInterval();

		// parameter importances
		if (doImportances || doModelAveraging)
			parameterImportance();

		// model averaging
		if (doModelAveraging)
			averageModels();

	}

	public double computeSingle(Model model) {
		return computeBic(model, options);
	}
	
	public static double computeBic(Model model, ApplicationOptions options) {
		if (options.countBLasParameters)
			return 2 * model.getLnL()
					+ model.getK()
					* Math.log(options.getSampleSize());
		else
			return 2 * model.getLnL()
					+ (model.getK() - options.getNumBranches())
					* Math.log(options.getSampleSize());
	}
	
	public static double computeBic(double lnL, int k, ApplicationOptions options) {
		if (options.countBLasParameters)
			return 2 * lnL + k * Math.log(options.getSampleSize());
		else
			return 2 * lnL + (k - options.getNumBranches())
					* Math.log(options.getSampleSize());
	}
	
	protected void printHeader(TextOutputStream stream) {
		stream.println("\n\n\n---------------------------------------------------------------");
		stream.println("*                                                             *");
		stream.println("*             BAYESIAN INFORMATION CRITERION (BIC)            *");
		stream.println("*                                                             *");
		stream.println("---------------------------------------------------------------");
		stream.println(" ");
		stream.println(" Sample size: " + options.getSampleSize());
	}
	
	protected void printFooter(TextOutputStream stream) {
		stream.println("-lnL:\tnegative log likelihod");
		stream.println("K:\tnumber of estimated parameters");
		stream.println("BIC:\tBayesian Information Criterion");
		stream.println("delta:\tBIC difference");
		stream.println("weight:\tBIC weight");
		stream.println("cumWeight:\tcumulative BIC weight");
	}
	
	/**************
	 * buildConfidenceInterval ************************
	 * 
	 * Builts the confidence interval of selected models and their cumulative
	 * weight
	 * 
	 * The model that just passed the confidence will be or not in the interval
	 * by chance (see below)
	 ****************************************************************/

	public void buildConfidenceInterval() {
		int i;
		Model tmodel = models[0];
		cumWeight = 0;

		// first construct the confidence interval for models
		if (confidenceInterval >= 1.0d) {
			for (i = 0; i < numModels; i++) {
				tmodel = models[order[i]];
				tmodel.setInBICinterval(true);
				confidenceModels.add(tmodel);
			}
			cumWeight = 1.0;
		} else {
			for (i = 0; i < numModels; i++) {
				tmodel = models[order[i]];
				if (tmodel.getCumBICw() <= confidenceInterval) {
					tmodel.setInBICinterval(true);
					confidenceModels.add(tmodel);
					cumWeight += tmodel.getBICw();
				} else
					break;
			}

			// lets decide whether the model that just passed the confidence
			// interval should be included (suggested by John Huelsenbeck)
			double probOut = (tmodel.getCumBICw() - confidenceInterval)
					/ tmodel.getBICw();
			double probIn = 1.0 - probOut;
			Random generator = new Random();
			double randomNumber = generator.nextDouble();
			if (randomNumber <= probIn) {
				tmodel.setInBICinterval(true);
				confidenceModels.add(tmodel);
				cumWeight += tmodel.getBICw();
			} else
				tmodel.setInBICinterval(false);

			/*
			 * System.out.print("name=" + tmodel.name + " w=" + tmodel.BICw +
			 * " cumw=" + tmodel.cumBICw); System.out.print(" in=" + probIn +
			 * " out=" + probOut); System.out.print(" r=" + randomNumber +
			 * " isIn=" + tmodel.isInBICinterval);
			 */

		}

		/*
		 * System.out.print(confidenceInterval + " confidence interval (" +
		 * numModels + " models) = ["); for (Enumeration
		 * e=confidenceModels.elements(); e.hasMoreElements();) { Model m =
		 * (Model)e.nextElement(); System.out.print(m.name + " "); }
		 * System.out.print("]");
		 */
	}

	public double getMinModelValue() {
		return minModel.getBIC();
	}

	public double getMinModelWeight() {
		return minModel.getBICw();
	}
	
	@Override
	public double getValue(Model m) {
		return m.getBIC();
	}
	
	@Override
	public double getWeight(Model m) {
		return m.getBICw();
	}
	
	@Override
	public double getDelta(Model m) {
		return m.getBICd();
	}
	
	@Override
	public double getUDelta(Model m) {
		return m.getUBICd();
	}
	
	@Override
	public double setUDelta(Model m) {
		m.setUBICd(computeBic(m.getLnLIgnoringGaps(), m.getK(), options)
				- computeBic(unconstrainedModel.getLnL(), 
						unconstrainedModel.getK(), options));
		return m.getUBICd();
	}
	
	@Override
	public double getCumWeight(Model m) {
		return m.getCumBICw();
	}

	@Override
	public int getType() {
		return IC_BIC;
	}
} // class BIC

