/** 
 * AIC.java
 *
 * Description:		AIC computation
 * @author			David Posada, University of Vigo, Spain  
 *					dposada@uvigo.es | darwin.uvigo.es
 */

package es.uvigo.darwin.jmodeltest.selection;

import java.util.Random;

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
		if (options.countBLasParameters)
			min = 2 * (models[0].getLnL() + models[0].getK());
		else
			min = 2 * (models[0].getLnL() + models[0].getK() - options.numBranches);

		minModel = models[0];

		for (Model model : models) {
			if (options.countBLasParameters)
				model.setAIC(2 * (model.getLnL() + model.getK()));
			else
				model.setAIC(2 * (model.getLnL() + model.getK() - options.numBranches));
			if (model.getAIC() < min) {
				min = model.getAIC();
				minModel = model;
			}
		}

		// Calculate Akaike differences
		sumExp = 0;
		for (i = 0; i < numModels; i++) {
			models[i].setAICd(models[i].getAIC() - minModel.getAIC());
			sumExp += Math.exp(-0.5 * models[i].getAICd());
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

	protected void printHeader(TextOutputStream stream) {
		stream.println("\n\n\n---------------------------------------------------------------");
		stream.println("*                                                             *");
		stream.println("*             AKAIKE INFORMATION CRITERION (AIC)              *");
		stream.println("*                                                             *");
		stream.println("---------------------------------------------------------------");
	}
	
	protected void printFooter(TextOutputStream stream) {
		stream.println("\n------------------------------------------------------------------------");
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
		if (confidenceInterval == 1.0) {
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
	public int getType() {
		return AIC;
	}

} // class AIC

