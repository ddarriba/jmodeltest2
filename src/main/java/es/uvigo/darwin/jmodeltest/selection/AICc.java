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

import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;

public class AICc extends InformationCriterion {

	// constructor
	public AICc(boolean mwritePAUPblock, boolean mdoImportances,
			boolean mdoModelAveraging, double minterval, ModelTest modelTest) {
		super(mwritePAUPblock, mdoImportances, mdoModelAveraging, minterval, modelTest);
	}

	/****************************
	 * computeAICc ******************************* * Computes the corrected AIC
	 * (AICc) for small sample sizes (relative * to the number of parameters)
	 * and finds out the model with the * minimum AICc * *
	 ************************************************************************/

	public void compute() {

		boolean sorted;
		int i, temp2, pass, K;
		double[] tempw = new double[options.numModels];
		double min, sumExp, cum, temp1;

		// Calculate AICc and minAICc
		if (options.countBLasParameters)
			K = models[0].getK();
		else
			K = models[0].getK() - options.numBranches;

		min = 2 * (models[0].getLnL() + K)
				+ (2 * K * (K + 1) / (double) (options.sampleSize - K - 1));

		minModel = models[0];
		for (i = 0; i < numModels; i++) {
			if (options.countBLasParameters)
				K = models[i].getK();
			else
				K = models[i].getK() - options.numBranches;

			models[i].setAICc(2
					* (models[i].getLnL() + K)
					+ (2 * K * (K + 1) / (double) (options.sampleSize - K - 1)));

			if (models[i].getAICc() < min) {
				min = models[i].getAICc();
				minModel = models[i];
			}
		}

		// Calculate Akaike differences
		sumExp = 0;
		for (i = 0; i < numModels; i++) {
			models[i].setAICcd(models[i].getAICc() - minModel.getAICc());
			sumExp += Math.exp(-0.5 * models[i].getAICcd());
		}

		// Calculate Akaike weights
		for (i = 0; i < numModels; i++) {
			if (models[i].getAICcd() > 1000)
				models[i].setAICcw(0.0);
			else
				models[i].setAICcw(Math.exp(-0.5 * models[i].getAICcd()) / sumExp);
			tempw[i] = models[i].getAICc();
			order[i] = i;
		}

		// Get order for Akaike weights and calculate cumWeigths
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
			cum += models[order[i]].getAICcw();
			models[order[i]].setCumAICcw(cum);
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

	protected void printHeader(TextOutputStream stream) {
		stream.println("\n\n\n---------------------------------------------------------------");
		stream.println("*                                                             *");
		stream.println("*        CORRECTED AKAIKE INFORMATION CRITERION (AICc)        *");
		stream.println("*                                                             *");
		stream.println("---------------------------------------------------------------");
	}
	
	protected void printFooter(TextOutputStream stream) {
		stream.println("\n------------------------------------------------------------------------");
		stream.println("-lnL:\tnegative log likelihod");
		stream.println(" K:\tnumber of estimated parameters");
		stream.println(" AICc:\tCorrected Akaike Information Criterion");
		stream.println(" delta:\tAICc difference");
		stream.println(" weight:\tAICc weight");
		stream.println(" cumWeight:\tcumulative AICc weight");
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
				tmodel.setInAICcinterval(true);
				confidenceModels.add(tmodel);
			}
			cumWeight = 1.0;
		} else {
			for (i = 0; i < numModels; i++) {
				tmodel = models[order[i]];
				if (tmodel.getCumAICcw() <= confidenceInterval) {
					tmodel.setInAICcinterval(true);
					confidenceModels.add(tmodel);
					cumWeight += tmodel.getAICcw();
				} else
					break;
			}

			// lets decide whether the model that just passed the confidence
			// interval should be included (suggested by John Huelsenbeck)
			double probOut = (tmodel.getCumAICcw() - confidenceInterval)
					/ tmodel.getAICcw();
			double probIn = 1.0 - probOut;
			Random generator = new Random();
			double randomNumber = generator.nextDouble();
			if (randomNumber <= probIn) {
				tmodel.setInAICcinterval(true);
				if (!confidenceModels.contains(tmodel)) confidenceModels.add(tmodel);
				cumWeight += tmodel.getAICcw();
			} else
				tmodel.setInAICcinterval(false);

			/*
			 * System.out.print("name=" + tmodel.name + " w=" + tmodel.AICcw +
			 * " cumw=" + tmodel.cumAICw); System.out.print(" in=" + probIn +
			 * " out=" + probOut); System.out.print(" r=" + randomNumber +
			 * " isIn=" + tmodel.isInAICcinterval + " ");
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
		return minModel.getAICc();
	}

	public double getMinModelWeight() {
		return minModel.getAICcw();
	}
	
	@Override
	public double getValue(Model m) {
		return m.getAICc();
	}
	
	@Override
	public double getWeight(Model m) {
		return m.getAICcw();
	}
	
	@Override
	public double getDelta(Model m) {
		return m.getAICcd();
	}
	
	@Override
	public double getCumWeight(Model m) {
		return m.getCumAICcw();
	}
	
	@Override
	public int getType() {
		return AICc;
	}

} // class AICc
