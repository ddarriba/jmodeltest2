/** 
 * DT.java
 *
 * Description:		DT computation (Minin et al (2003). Syst.Biol. 52: 674-683)
 * @author			David Posada, University of Vigo, Spain  
 *					dposada@uvigo.es | darwin.uvigo.es
 */

package es.uvigo.darwin.jmodeltest.selection;

import java.util.Random;

import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.prottest.tree.TreeDistancesCache;
import es.uvigo.darwin.prottest.tree.TreeEuclideanDistancesCache;

//DP check: DT might keep running even with bad likelihoods ?
public class DT extends InformationCriterion {

	private TreeDistancesCache distances = TreeEuclideanDistancesCache.getInstance();
	
	// constructor
	public DT(boolean mwritePAUPblock, boolean mdoImportances,
			boolean mdoModelAveraging, double minterval) {
		super(mwritePAUPblock, mdoImportances, mdoModelAveraging, minterval);
	}

	/****************************
	 * compute ******************************* * Computes the Decision theory
	 * Criterion (DT) for every model * and finds out the model with the minimum
	 * DT * *
	 ************************************************************************/

	public void compute() {

		boolean sorted;
		int i, j, temp2, pass;
		double min, sumExp, sum, sumReciprocal, cum, temp1;
		double[] tempw = new double[numModels];
		double[] BIC = new double[numModels];

		/*
		 * // Calculate BICs and BIC exponentials sumBICexp = 0; for (i=0;
		 * i<numModels; i++) { if (ModelTest.countBLasParameters) BIC[i]
		 * = 2 * model[i].lnL + model[i].K * Math.log(ModelTest.sampleSize);
		 * else BIC[i] = 2 * model[i].lnL + (model[i].K - ModelTest.numBranches)
		 * * Math.log(ModelTest.sampleSize); BICexp[i] = Math.exp(-0.5 *
		 * BIC[i]); // DP: e^1000 gives 0... sumBICexp += BICexp[i]; } //
		 * Calculate DT and minDT min = 999999; for (i=0; i<numModels; i++) {
		 * model[i].DT = 0; for (j=0; j<numModels; j++) model[i].DT +=
		 * euclideanDistances[i][j] * (BICexp[j]/sumBICexp);
		 * 
		 * if (model[i].DT < min) { min = model[i].DT; ModelTest.minDT =
		 * model[i]; } }
		 */

		/* exactly as in DT-ModSel.pl */
		// get BICs and min BIC
		double minBIC = min = 9999;
		for (i = 0; i < numModels; i++) {
			if (options.countBLasParameters)
				BIC[i] = models[i].getLnL() + models[i].getK() / 2.0
						* Math.log(options.sampleSize);
			else
				BIC[i] = models[i].getLnL()
						+ (models[i].getK() - options.numBranches) / 2.0
						* Math.log(options.sampleSize);
			if (BIC[i] < minBIC)
				minBIC = BIC[i];
		}

		for (Model model1 : models) {
			sum = 0;
			j = 0;
			for (Model model2 : models) {
				double distance = distances.getDistance(model1.getTree(), model2.getTree());
				if (distance > 0) {
					double power = Math.log(distance) - BIC[j]
							+ minBIC;
					if (power > -30) {
						sum += Math.exp(power);
					}
				}
				j++;
			}
			model1.setDT(sum);
			if (model1.getDT() < min) {
				min = model1.getDT();
				minModel = model1;
			}
		}

		// Calculate DT differences
		sumReciprocal = sumExp = sum = 0;
		for (i = 0; i < numModels; i++) {
			models[i].setDTd(models[i].getDT() - minModel.getDT());
			sumExp += Math.exp(-0.5 * models[i].getDTd());
			sumReciprocal += 1.0 / models[i].getDT();
		}

		// Calculate DT weights
		// NOTE: It gives very small and similar weigths, as we are taking logs
		// of
		// small and similar numbers
		/*
		 * for (i=0; i<numModels; i++) { model[i].DTw =
		 * Math.exp(-0.5*model[i].DTd) / sumExp; tempw[i] = model[i].DTw;
		 * order[i] = i; }
		 */

		// DP we need to do it in a different way?: i think so...
		for (i = 0; i < numModels; i++) {
			if (models[i].getDTd() > 1000)
				models[i].setDTw(0.0);
			else
				models[i].setDTw((1.0 / models[i].getDT()) / sumReciprocal);
			tempw[i] = models[i].getDT();
			order[i] = i;
		}

		// Get order for DT weights and calculate cumWeigths
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
			cum += models[order[i]].getDTw();
			models[order[i]].setCumDTw(cum);
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
		stream.println("*      DECISION THEORY PERFORMANCE-BASED SELECTION (DT)       *");
		stream.println("*                                                             *");
		stream.println("---------------------------------------------------------------");
	}
	
	protected void printFooter(TextOutputStream stream) {
		stream.println("\n------------------------------------------------------------------------");
		stream.println("-lnL:t\tnegative log likelihod");
		stream.println("K:\tnumber of estimated parameters");
		stream.println("DT:\tdecision theory performance-based score");
		stream.println("delta:\tDT difference");
		stream.println("weight:\tDT weight* (calculated using 1/DT)");
		stream.println("cumWeight:\tcumulative DT weight");
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
				tmodel.setInDTinterval(true);
				confidenceModels.add(tmodel);
			}
			cumWeight = 1.0;
		} else {
			for (i = 0; i < numModels; i++) {
				tmodel = models[order[i]];
				if (tmodel.getCumDTw() <= confidenceInterval) {
					tmodel.setInDTinterval(true);
					confidenceModels.add(tmodel);
					cumWeight += tmodel.getDTw();
				} else
					break;
			}

			// lets decide whether the model that just passed the confidence
			// interval should be included (suggested by John Huelsenbeck)
			double probOut = (tmodel.getCumDTw() - confidenceInterval)
					/ tmodel.getDTw();
			double probIn = 1.0 - probOut;
			Random generator = new Random();
			double randomNumber = generator.nextDouble();
			if (randomNumber <= probIn) {
				tmodel.setInDTinterval(true);
				confidenceModels.add(tmodel);
				cumWeight += tmodel.getDTw();
			} else
				tmodel.setInDTinterval(false);

			/*
			 * System.out.print("name=" + tmodel.name + " w=" + tmodel.DTw +
			 * " cumw=" + tmodel.cumDTw); System.out.print(" in=" + probIn +
			 * " out=" + probOut); System.out.print(" r=" + randomNumber +
			 * " isIn=" + tmodel.isInDTinterval);
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
		return minModel.getDT();
	}

	public double getMinModelWeight() {
		return minModel.getDTw();
	}
	
	@Override
	public double getValue(Model m) {
		return m.getDT();
	}
	
	@Override
	public double getWeight(Model m) {
		return m.getDTw();
	}
	
	@Override
	public double getDelta(Model m) {
		return m.getDTd();
	}
	
	@Override
	public double getCumWeight(Model m) {
		return m.getCumDTw();
	}
	
	@Override
	public int getType() {
		return DT;
	}
} // class DT
