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
		double[] tempw = new double[options.numModels];
		double min, sumExp, cum, temp1;

		// Calculate BIC and minBIC
		if (options.countBLasParameters)
			min = 2 * models[0].getLnL() + models[0].getK()
					* Math.log(options.sampleSize);
		else
			min = 2 * models[0].getLnL()
					+ (models[0].getK() - options.numBranches)
					* Math.log(options.sampleSize);

		minModel = models[0];
		for (i = 0; i < numModels; i++) {
			if (options.countBLasParameters)
				models[i].setBIC(2 * models[i].getLnL() + models[i].getK()
						* Math.log(options.sampleSize));
			else
				models[i].setBIC(2 * models[i].getLnL()
						+ (models[i].getK() - options.numBranches)
						* Math.log(options.sampleSize));

			if (models[i].getBIC() < min) {
				min = models[i].getBIC();
				minModel = models[i];
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
			for (i = 0; i < (options.numModels - pass); i++)
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
		for (i = 0; i < options.numModels; i++) {
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

	protected void printHeader(TextOutputStream stream) {
		stream.println("\n\n\n---------------------------------------------------------------");
		stream.println("*                                                             *");
		stream.println("*             BAYESIAN INFORMATION CRITERION (BIC)            *");
		stream.println("*                                                             *");
		stream.println("---------------------------------------------------------------");
	}
	
	protected void printFooter(TextOutputStream stream) {
		stream.println("\n------------------------------------------------------------------------");
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
	public double getCumWeight(Model m) {
		return m.getCumBICw();
	}

	@Override
	public int getType() {
		return BIC;
	}
} // class BIC

