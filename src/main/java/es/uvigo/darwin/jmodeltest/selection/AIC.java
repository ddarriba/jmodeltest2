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

public class AIC extends InformationCriterion {

	// constructor
	public AIC(boolean mwritePAUPblock, boolean mdoImportances,
			boolean mdoModelAveraging, double minterval) {
		super(mwritePAUPblock, mdoImportances, mdoModelAveraging, minterval);
	}

	/****************************
	 * compute ********************************** * Computes the Akaike
	 * Information Criterion (AIC) for every model * and finds out the model
	 * with the minimum AIC * *
	 ************************************************************************/

	public void compute() {

		boolean sorted;
		int i, temp2, pass;
		double[] tempw = new double[numModels];
		double min, sumExp, cum, temp1;

		// Calculate AIC and minAIC
		min = computeAic(models[0], options);

		minModel = models[0];

		if (doCheckAgainstULK) {
			unconstrainedModel.setAIC(computeSingle(unconstrainedModel));
		}
		for (Model model : models) {
			model.setAIC(computeAic(model, options));

			if (model.getAIC() < min) {
				min = model.getAIC();
				minModel = model;
			}
			
			if (doCheckAgainstULK) {
				model.setUAICd(model.getAIC() - unconstrainedModel.getAIC());
			}
		}

		// Calculate Akaike differences
		sumExp = 0;
		for (Model model : models) {
			model.setAICd(model.getAIC() - minModel.getAIC());
			sumExp += Math.exp(-0.5 * model.getAICd());
		}

		// Calculate Akaike weights
		for (i = 0; i < numModels; i++) {
			if (models[i].getAICd() > 1000)
				models[i].setAICw(0.0);
			else
				models[i].setAICw(Math.exp(-0.5 * models[i].getAICd()) / sumExp);
			tempw[i] = models[i].getAIC(); // AICw
			order[i] = i;
		}

		// Get order for AIC and calculate cumWeigths
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
			cum += models[order[i]].getAICw();
			models[order[i]].setCumAICw(cum);
		}

		// confidence interval
		buildConfidenceInterval();

		// parameter importances
		if (doImportances || doModelAveraging) {
			parameterImportance();
		}

		// model averaging
		if (doModelAveraging) {
			averageModels();
		}

	}

	public double computeSingle(Model model) {
		return computeAic(model, options);
	}
	
	public static double computeAic(Model model, ApplicationOptions options) {
		if (options.countBLasParameters)
			return 2 * (model.getLnL() + model.getK());
		else
			return 2 * (model.getLnL() + model.getK() - options.getNumBranches());
		
	}
	
	public static double computeAic(double lnL, int k, ApplicationOptions options) {
		if (options.countBLasParameters)
			return 2 * (lnL + k);
		else
			return 2 * (lnL + k - options.getNumBranches());
		
	}
	
	protected void printHeader(TextOutputStream stream) {
		stream.println("\n\n\n---------------------------------------------------------------");
		stream.println("*                                                             *");
		stream.println("*             AKAIKE INFORMATION CRITERION (AIC)              *");
		stream.println("*                                                             *");
		stream.println("---------------------------------------------------------------");
	}
	
	protected void printFooter(TextOutputStream stream) {
		stream.println("-lnL:\tnegative log likelihod");
		stream.println(" K:\tnumber of estimated parameters");
		stream.println(" AIC:\tAkaike Information Criterion");
		stream.println(" delta:\tAIC difference");
		stream.println(" weight:\tAIC weight");
		stream.println(" cumWeight:\tcumulative AIC weight");
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
				tmodel.setInAICinterval(true);
				confidenceModels.add(tmodel);
			}
			cumWeight = 1.0;
		} else {
			for (i = 0; i < numModels; i++) {
				tmodel = models[order[i]];
				if (tmodel.getCumAICw() <= confidenceInterval) {
					tmodel.setInAICinterval(true);
					confidenceModels.add(tmodel);
					cumWeight += tmodel.getAICw();
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
				tmodel.setInAICinterval(true);
				confidenceModels.add(tmodel);
				cumWeight += tmodel.getAICw();
			} else
				tmodel.setInAICinterval(false);

			/*
			 * System.out.print("name=" + tmodel.name + " w=" + tmodel.AICw +
			 * " cumw=" + tmodel.cumAICw); System.out.print(" in=" + probIn +
			 * " out=" + probOut); System.out.print(" r=" + randomNumber +
			 * " isIn=" + tmodel.isInAICinterval);
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
		return minModel.getAIC();
	}

	public double getMinModelWeight() {
		return minModel.getAICw();
	}

	@Override
	public double getValue(Model m) {
		return m.getAIC();
	}

	@Override
	public double getWeight(Model m) {
		return m.getAICw();
	}

	@Override
	public double getCumWeight(Model m) {
		return m.getCumAICw();
	}
	
	@Override
	public double getDelta(Model m) {
		return m.getAICd();
	}

	@Override
	public double getUDelta(Model m) {
		return m.getUAICd();
	}
	
	@Override
	public double setUDelta(Model m) {
		m.setUAICd(computeAic(m.getLnLIgnoringGaps(), m.getK(), options)
				- computeAic(unconstrainedModel.getLnL(), 
						unconstrainedModel.getK(), options));
		return m.getUAICd();
	}
	
	@Override
	public int getType() {
		return IC_AIC;
	}

} // class AIC

