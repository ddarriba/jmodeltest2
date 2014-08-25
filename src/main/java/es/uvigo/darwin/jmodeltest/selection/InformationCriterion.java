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

import java.util.ArrayList;
import java.util.List;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.exe.PhymlSingleModel;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public abstract class InformationCriterion {

	protected ApplicationOptions options = ApplicationOptions.getInstance();

	public static final int IC_AIC = 1;
	public static final int IC_AICc = 2;
	public static final int IC_BIC = 3;
	public static final int IC_DT = 4;

	private static final String[] names = { "", "AIC", "AICc", "BIC", "DT" };

	public int[] order;
	protected int numModels;
	protected Model[] models;
	protected boolean writePAUPblock;
	protected boolean doImportances;
	protected boolean doModelAveraging;
	protected double confidenceInterval;
	protected List<Model> confidenceModels;
	protected double cumWeight;
	protected Model minModel;

	protected boolean doCheckAgainstULK;
	protected Model unconstrainedModel;

	// importances
	protected double ifA, ifG, ifC, ifT;
	protected double ititv, ikappa, ipinvI, ishapeG, ipinvIG, ishapeIG;
	protected double iRa, iRb, iRc, iRd, iRe, iRf;
	// averaged estimates
	protected double afA, afG, afC, afT;
	protected double atitv, akappa, apinvI, ashapeG, apinvIG, ashapeIG;
	protected double aRa, aRb, aRc, aRd, aRe, aRf;

	public List<Model> getConfidenceModels() {
		return confidenceModels;
	}

	public InformationCriterion(boolean mwritePAUPblock,
			boolean mdoImportances, boolean mdoModelAveraging, double minterval) {
		List<Model> modelList = new ArrayList<Model>();
		for (Model model : ModelTest.getCandidateModels()) {
			if (model.getLnL() > 0) {
				modelList.add(model);
			}
		}
		models = modelList.toArray(new Model[0]);
		numModels = models.length;
		order = new int[numModels];
		writePAUPblock = mwritePAUPblock;
		doImportances = mdoImportances;
		doModelAveraging = mdoModelAveraging;
		confidenceInterval = minterval;
		confidenceModels = new ArrayList<Model>();

		doCheckAgainstULK = (getType() != IC_DT
				&& (options.getNumPatterns() > 0) && (options
				.getUnconstrainedLnL() > 1e-10));

		if (doCheckAgainstULK) {
			unconstrainedModel = new Model(numModels, "UModel", "-",
					options.getNumPatterns() - 1);
			unconstrainedModel.setLnL(options.getUnconstrainedLnL());
		}
	}

	/**************************************************************
	 * parameterImportance
	 * 
	 * Calculates the importance for each parameter
	 * 
	 * Assumes model estimates rate parametes Ra-Re when they are different to
	 * Rf
	 * 
	 * Note: Modeltest assumed TrN y TIM estimate only Rb, Re K81 estimates no R
	 * parameter TVM estimates only Ra, Rc, Rd GTR y SIM estimate Ra, Rb, Rc,
	 * Rd, Re
	 * 
	 * Importance is rescaled by the total weight of the models included in the
	 * confidence interval
	 **************************************************************/
	public void parameterImportance() {
		double weight;
		String partition;
		ifA = ifG = ifC = ifT = 0;
		ititv = iRa = iRb = iRc = iRd = iRe = iRf = 0;
		ipinvI = ishapeG = ipinvIG = ishapeIG = 0;

		for (Model tmodel : confidenceModels) {

			weight = getWeight(tmodel) / cumWeight;
			partition = tmodel.getPartition();

			/* base frequencies */
			if (tmodel.ispF()) {
				ifA += weight;
				ifC += weight;
				ifG += weight;
				ifT += weight;
			}

			/* substitution rates */
			if (tmodel.ispT()) {
				ikappa += weight;
				ititv += weight;
			} else if (tmodel.ispR()) {
				if (partition.charAt(0) != partition.charAt(5))
					iRa += weight;
				if (partition.charAt(1) != partition.charAt(5))
					iRb += weight;
				if (partition.charAt(2) != partition.charAt(5))
					iRc += weight;
				if (partition.charAt(3) != partition.charAt(5))
					iRd += weight;
				if (partition.charAt(4) != partition.charAt(5))
					iRe += weight;
				iRf += weight;
			}

			/* rate variation */
			if (tmodel.ispI() && !tmodel.ispG()) {
				ipinvI += weight;
			} else if (!tmodel.ispI() && tmodel.ispG()) {
				ishapeG += weight;
			} else if (tmodel.ispI() && tmodel.ispG()) {
				ipinvIG += weight;
				ishapeIG += weight;
			}
		}
	}

	/************************************************************
	 * averageModels
	 * 
	 * Calculates the model averaged estimates
	 * 
	 * Assumes K81,TrN,TIM,TVMSIM, GRT estimate all rate parametes Ra-Rf
	 * 
	 * for a given parameter, weight is rescaled by parameter importance
	 ***********************************************************/

	public void averageModels() {

		double weight, minWeight, NA;
		String partition;

		afA = afG = afC = afT = 0;
		atitv = akappa = aRa = aRb = aRc = aRd = aRe = aRf = 0;
		apinvI = ashapeG = apinvIG = ashapeIG = 0;

		NA = Utilities.NA;
		minWeight = getWeight(models[order[numModels - 1]]);

		for (Model tmodel : confidenceModels) {

			weight = getWeight(tmodel) / cumWeight; // because importance is
													// already reescaled by
													// cumWeight
			partition = tmodel.getPartition();

			/* base frequencies */
			if (tmodel.ispF()) {
				if (ifA >= minWeight)
					afA += tmodel.getfA() * weight / ifA;
				if (ifC >= minWeight)
					afC += tmodel.getfC() * weight / ifC;
				if (ifG >= minWeight)
					afG += tmodel.getfG() * weight / ifG;
				if (ifT >= minWeight)
					afT += tmodel.getfT() * weight / ifT;
			}

			/* substitution rates */
			if (tmodel.ispT()) {
				if (ikappa >= minWeight)
					akappa += tmodel.getKappa() * weight / ikappa;
				if (ititv >= minWeight)
					atitv += tmodel.getTitv() * weight / ititv;
			} else if (tmodel.ispR()) {
				if (partition.charAt(0) != partition.charAt(5)
						&& iRa >= minWeight)
					aRa += tmodel.getRa() * weight / iRa;
				if (partition.charAt(1) != partition.charAt(5)
						&& iRb >= minWeight)
					aRb += tmodel.getRb() * weight / iRb;
				if (partition.charAt(2) != partition.charAt(5)
						&& iRc >= minWeight)
					aRc += tmodel.getRc() * weight / iRc;
				if (partition.charAt(3) != partition.charAt(5)
						&& iRd >= minWeight)
					aRd += tmodel.getRd() * weight / iRd;
				if (partition.charAt(4) != partition.charAt(5)
						&& iRe >= minWeight)
					aRe += tmodel.getRe() * weight / iRe;
				if (iRf >= minWeight)
					aRf += tmodel.getRf() * weight / iRf;

			}

			/* rate variation */
			if (!tmodel.ispI() && tmodel.ispG()) {
				if (ishapeG > minWeight)
					ashapeG += tmodel.getShape() * weight / ishapeG;
			} else if (tmodel.ispI() && !tmodel.ispG()) {
				if (ipinvI > minWeight)
					apinvI += tmodel.getPinv() * weight / ipinvI;
			} else if (tmodel.ispI() && tmodel.ispG()) {
				if (ishapeIG > minWeight)
					ashapeIG += tmodel.getShape() * weight / ishapeIG;
				if (ipinvIG > minWeight)
					apinvIG += tmodel.getPinv() * weight / ipinvIG;
			}
		}

		// make NA estimates when importance is zero (almost)
		if (ifA < minWeight)
			afA = NA;
		if (ifC < minWeight)
			afC = NA;
		if (ifG < minWeight)
			afG = NA;
		if (ifT < minWeight)
			afT = NA;
		if (ikappa < minWeight)
			akappa = NA;
		if (ititv < minWeight)
			atitv = NA;
		if (iRa < minWeight)
			aRa = NA;
		if (iRb < minWeight)
			aRb = NA;
		if (iRc < minWeight)
			aRc = NA;
		if (iRd < minWeight)
			aRd = NA;
		if (iRe < minWeight)
			aRe = NA;
		if (iRf < minWeight)
			aRf = NA;
		if (ishapeG < minWeight)
			ashapeG = NA;
		if (ipinvI < minWeight)
			apinvI = NA;
		if (ishapeIG < minWeight)
			ashapeIG = NA;
		if (ipinvIG < minWeight)
			apinvIG = NA;
	}

	public double getAfA() {
		return afA;
	}

	public double getIfA() {
		return ifA;
	}

	public double getIfG() {
		return ifG;
	}

	public double getIshapeIG() {
		return ishapeIG;
	}

	public double getIpinvIG() {
		return ipinvIG;
	}

	public double getIshapeG() {
		return ishapeG;
	}

	public double getIpinvI() {
		return ipinvI;
	}

	public double getiRf() {
		return iRf;
	}

	public double getiRe() {
		return iRe;
	}

	public double getiRd() {
		return iRd;
	}

	public double getiRc() {
		return iRc;
	}

	public double getiRb() {
		return iRb;
	}

	public double getiRa() {
		return iRa;
	}

	public double getIkappa() {
		return ikappa;
	}

	public double getItitv() {
		return ititv;
	}

	public double getIfT() {
		return ifT;
	}

	public double getIfC() {
		return ifC;
	}

	public double getAfG() {
		return afG;
	}

	public double getAshapeIG() {
		return ashapeIG;
	}

	public double getApinvIG() {
		return apinvIG;
	}

	public double getAshapeG() {
		return ashapeG;
	}

	public double getApinvI() {
		return apinvI;
	}

	public double getaRf() {
		return aRf;
	}

	public double getaRe() {
		return aRe;
	}

	public double getaRd() {
		return aRd;
	}

	public double getaRc() {
		return aRc;
	}

	public double getaRb() {
		return aRb;
	}

	public double getaRa() {
		return aRa;
	}

	public double getAkappa() {
		return akappa;
	}

	public double getAtitv() {
		return atitv;
	}

	public double getAfT() {
		return afT;
	}

	public double getAfC() {
		return afC;
	}

	public Model getMinModel() {
		return minModel;
	}

	@Override
	public String toString() {
		switch (getType()) {
		case IC_AIC:
			return "AIC";
		case IC_BIC:
			return "BIC";
		case IC_AICc:
			return "AICc";
		case IC_DT:
			return "DT";
		}
		return null;
	}

	public Model getModel(int i) {
		return models[order[i]];
	}

	public void print(TextOutputStream stream) {
		int i, j;
		String criterion = names[getType()];

		printHeader(stream);

		stream.println(" ");
		stream.println(" Model selected: ");
		getMinModel().print(stream);

		// print PAUP* block
		if (writePAUPblock)
			ModelTest.WritePaupBlock(stream, criterion, minModel);

		// print ML tree for best-fit model
		stream.println(" ");
		stream.println("Tree for the best " + criterion + " model = "
				+ minModel.getTreeString());

		// update unconstrained model
		if (unconstrainedModel != null) {
			unconstrainedModel.setLnL(minModel.getUnconstrainedLnL());
		}

		// print weights
		stream.println(" ");
		stream.println(" ");
		stream.println("* " + criterion
				+ " MODEL SELECTION : Selection uncertainty");
		stream.println(" ");
		stream.printf(
				"Model             -lnL    K     %4s       delta       weight   cumWeight",
				criterion);
		if (doCheckAgainstULK && minModel.getUnconstrainedLnL() > 1e-10) {
			stream.println("       uDelta");
		} else {
			stream.println("");
		}
		stream.print("-------------------------------------------------------------------------");
		if (doCheckAgainstULK && minModel.getUnconstrainedLnL() > 1e-10) {
			stream.print("-------------");
		}
		for (i = 0; i < numModels; i++) {
			j = order[i];
			// j = i;
			stream.println(" ");
			stream.printf("%-10s", models[j].getName());
			stream.printf("  %10.5f", models[j].getLnL());
			if (options.countBLasParameters)
				stream.printf("   %2d", models[j].getK());
			else
				stream.printf("  %2d",
						models[j].getK() - options.getNumBranches());
			stream.printf("  %10.6f", getValue(models[j]));
			stream.printf("  %9.6f", getDelta(models[j]));
			if (getWeight(models[j]) > 0.0001)
				stream.printf("   %9.6f", getWeight(models[j]));
			else
				stream.printf("   %4.2e", getWeight(models[j]));
			stream.printf("   %7.6f", getCumWeight(models[j]));
			if (doCheckAgainstULK && minModel.getUnconstrainedLnL() > 1e-10) {
				if (!options.isAmbiguous()) {
					stream.printf("   %10.4f", getUDelta(models[j]));
				} else if (i == 0) {
					stream.printf("   %10.4f", setUDelta(models[j]));
				} else {
					stream.print("            -");
				}
			}
		}
		stream.print("\n-------------------------------------------------------------------------");
		if (doCheckAgainstULK && minModel.getUnconstrainedLnL() > 1e-10) {
			stream.print("-------------");
		}
		stream.println("");
		printFooter(stream);

		Utilities.toConsoleEnd();

		// indicate table availability
		if (ModelTest.buildGUI) {
			stream.println("\nModel selection results also available at the \"Model > Show model table\" menu");
			Utilities.toConsoleEnd();
		}

		// print confidence set
		stream.println(" ");
		stream.println(" ");
		stream.println("* " + criterion
				+ " MODEL SELECTION : Confidence interval");
		stream.println(" ");
		stream.print("There are " + confidenceModels.size() + " models in the ");
		stream.printf("%.0f% ", confidenceInterval * 100);
		stream.print("confidence interval: [ ");
		for (Model m : confidenceModels) {
			stream.print(m.getName() + " ");
		}
		stream.println("] ");

		// print parameter importances
		if (doImportances) {
			stream.println(" ");
			stream.println(" ");
			stream.println("* " + criterion
					+ " MODEL SELECTION : Parameter importance");
			stream.println(" ");
			stream.println("Parameter   Importance");
			stream.print("----------------------");
			if (options.doF) {
				stream.printf("\nfA\t%10.4f", ifA);
				stream.printf("\nfC\t%10.4f", ifC);
				stream.printf("\nfG\t%10.4f", ifG);
				stream.printf("\nfT\t%10.4f", ifT);
			}
			stream.printf("\nkappa\t%10.4f", ikappa);
			stream.printf("\ntitv\t%10.4f", ititv);
			stream.printf("\nrAC\t%10.4f", iRa);
			stream.printf("\nrAG\t%10.4f", iRb);
			stream.printf("\nrAT\t%10.4f", iRc);
			stream.printf("\nrCG\t%10.4f", iRd);
			stream.printf("\nrCT\t%10.4f", iRe);
			stream.printf("\nrGT\t%10.4f", iRf);
			if (options.doI)
				stream.printf("\npinv(I)\t%10.4f", ipinvI);
			if (options.doG)
				stream.printf("\nalpha(G)\t%10.4f", ishapeG);
			if (options.doI && options.doG) {
				stream.printf("\npinv(IG)\t%10.4f", ipinvIG);
				stream.printf("\nalpha(IG)\t%10.4f", ishapeIG);
			}
			stream.println("\n----------------------");
			stream.println("Values have been rounded.");
			stream.println(" (I):  considers only +I models.");
			stream.println(" (G):  considers only +G models.");
			stream.println(" (IG): considers only +I+G models.");
		}

		// print model averaging
		if (doModelAveraging) {
			stream.println(" ");
			stream.println(" ");
			stream.println("* " + criterion
					+ " MODEL SELECTION : Model averaged estimates");
			stream.println(" ");
			stream.println("           Model-averaged");
			stream.println("Parameter       estimates");
			stream.print("-------------------------");
			if (options.doF) {
				stream.printf("\nfA\t%13s", Utilities.checkNA(afA));
				stream.printf("\nfC\t%13s", Utilities.checkNA(afC));
				stream.printf("\nfG\t%13s", Utilities.checkNA(afG));
				stream.printf("\nfT\t%13s", Utilities.checkNA(afT));
			}
			stream.printf("\nkappa\t%13s", Utilities.checkNA(akappa));
			stream.printf("\ntitv\t%13s", Utilities.checkNA(atitv));
			stream.printf("\nrAC\t%13s", Utilities.checkNA(aRa));
			stream.printf("\nrAG\t%13s", Utilities.checkNA(aRb));
			stream.printf("\nrAT\t%13s", Utilities.checkNA(aRc));
			stream.printf("\nrCG\t%13s", Utilities.checkNA(aRd));
			stream.printf("\nrCT\t%13s", Utilities.checkNA(aRe));
			stream.printf("\nrGT\t%13s", Utilities.checkNA(aRf));
			if (options.doI)
				stream.printf("\npinv(I)\t%13s", Utilities.checkNA(apinvI));
			if (options.doG)
				stream.printf("\nalpha(G)\t%13s", Utilities.checkNA(ashapeG));
			if (options.doI && options.doG) {
				stream.printf("\npinv(IG)\t%13s", Utilities.checkNA(apinvIG));
				stream.printf("\nalpha(IG)\t%13s", Utilities.checkNA(ashapeIG));
			}
			stream.println("\n-------------------------");
			stream.println("Numbers have been rounded.");
			stream.println(" (I):  considers only +I models.");
			stream.println(" (G):  considers only +G models.");
			stream.println(" (IG): considers only +I+G models.");
		}

		stream.println(" ");
		stream.println(" ");
		stream.println("* " + criterion
				+ " MODEL SELECTION : Best Model's command line");
		stream.println(" ");
		stream.println("phyml "
				+ PhymlSingleModel.writePhyml3CommandLine(getMinModel(), false,
						options, false, -1));
		if (getValue(minModel) < 0.0) {
			stream.println(" ");
			stream.println("WARNING! Criterion has negative values. Please check whether your sample size is big enough compared to the number of parameters (K)");
		}
	}

	public int getNumModels() {
		return numModels;
	}

	public abstract void compute();

	public abstract double computeSingle(Model model);

	// public abstract void print (TextOutputStream stream);
	public abstract void buildConfidenceInterval();

	public abstract double getMinModelValue();

	public abstract double getMinModelWeight();

	public abstract double getValue(Model m);

	public abstract double getDelta(Model m);

	public abstract double getUDelta(Model m);

	public abstract double setUDelta(Model m);

	public abstract double getWeight(Model m);

	public abstract double getCumWeight(Model m);

	protected abstract void printHeader(TextOutputStream stream);

	protected abstract void printFooter(TextOutputStream stream);

	public abstract int getType();

}
