package es.uvigo.darwin.jmodeltest.exe;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.utilities.ModelDef;

public class GuidedSearchManager {

	private static final int RATE_PAIRS = 15;
	private static final int RATES = 6;
	private static final double MIN_GAMMA_FILTER = 50.0;
	private static final double MAX_INV_FILTER = 0.1;
	private static final double MIN_GAMMA_INV_FILTER = 0.5;

	private static double GVALUE = 0;
	private static double Y0 = 4;
	private static double X1 = 150;

	private double guidedSearchThreshold;
	private Model gtrModel;
	private boolean filterFrequencies;
	private boolean filterRateMatrix;
	private boolean filterRateVariation;

	private static ModelDef models[];
	private static ModelDef equalFreqmodels[];
	@SuppressWarnings("unchecked")
	private static ArrayList<ModelDef> rateFilter[] = new ArrayList[15];
	private static ArrayList<ModelDef> freqFilter = new ArrayList<ModelDef>();

	private HashSet<ModelDef> filter;
	private boolean filterInv = false;
	private boolean filterGamma = false;
	private boolean filterGammaInv = false;

	static {
		models = new ModelDef[] { ModelDef.JC, ModelDef.F81, ModelDef.HKY,
				ModelDef.K80, ModelDef.TRN, ModelDef.TRNef, ModelDef.TPM1uf,
				ModelDef.TPM1, ModelDef.TPM2uf, ModelDef.TPM2, ModelDef.TPM3uf,
				ModelDef.TPM3, ModelDef.TIM1ef, ModelDef.TIM1, ModelDef.TIM2ef,
				ModelDef.TIM2, ModelDef.TIM3ef, ModelDef.TIM3, ModelDef.TVM,
				ModelDef.TVMef, ModelDef.SYM, ModelDef.GTR };

		equalFreqmodels = new ModelDef[] { ModelDef.JC, ModelDef.K80,
				ModelDef.TRNef, ModelDef.TPM1, ModelDef.TPM2, ModelDef.TPM3,
				ModelDef.TIM1ef, ModelDef.TIM2ef, ModelDef.TIM3ef,
				ModelDef.TVMef, ModelDef.SYM };

		for (ModelDef model : equalFreqmodels) {
			freqFilter.add(model);
		}

		freqFilter.addAll(Arrays.asList(equalFreqmodels));

		for (int i = 0; i < RATE_PAIRS; i++) {
			rateFilter[i] = new ArrayList<ModelDef>();
			for (ModelDef model : models) {
				if (model.isEqualRate(i)) {
					rateFilter[i].add(model);
				}
			}
		}
	}

	public GuidedSearchManager(double guidedSearchThreshold, Model gtrModel,
			boolean filterFrequencies, boolean filterRateMatrix,
			boolean filterRateVariation) {

		this.guidedSearchThreshold = guidedSearchThreshold;
		this.gtrModel = gtrModel;
		this.filterFrequencies = filterFrequencies;
		this.filterRateMatrix = filterRateMatrix;
		this.filterRateVariation = filterRateVariation;

		System.out.println(" ");
		gtrModel.print(new TextOutputStream(new PrintStream(System.out)));
		System.out.println(" ");
		
		filter = setUpFilter(this.guidedSearchThreshold, this.gtrModel);

	}

	public Model[] filterModels(Model[] models) {

		ArrayList<Model> modelsArray = new ArrayList<Model>();
		for (Model model : models) {
			boolean included = true;

			for (ModelDef mDef : filter) {
				if (mDef.isName(model.getName())) {
					included = false;
					break;
				}
			}

			if (model.ispI()) {
				if (model.ispG()) {
					included &= !filterGammaInv;
				} else {
					included &= !filterInv;
				}
			} else if (model.ispG()) {
				included &= !filterGamma;
			}

			if (included) {
				modelsArray.add(model);
			}
		}

		return modelsArray.toArray(new Model[0]);
	}

	private HashSet<ModelDef> setUpFilter(double guidedSearchThreshold,
			Model gtrModel) {

		HashSet<ModelDef> filter = new HashSet<ModelDef>();
		double threshold = adjustThreshold(guidedSearchThreshold,
				gtrModel.getLnL());

		double mean = (gtrModel.getRa() + gtrModel.getRb() + gtrModel.getRc()
				+ gtrModel.getRd() + gtrModel.getRe() + gtrModel.getRf())
				/ RATES;
		double variance = ((gtrModel.getRa() * gtrModel.getRa()
				+ gtrModel.getRb() * gtrModel.getRb() + gtrModel.getRc()
				* gtrModel.getRc() + gtrModel.getRd() * gtrModel.getRd()
				+ gtrModel.getRe() * gtrModel.getRe() + gtrModel.getRf()
				* gtrModel.getRf()) / RATES)
				- mean * mean;
		boolean hasVar = variance > 1;
		// if (variance > 1) {
		double normalizedRates[] = new double[] {
				(gtrModel.getRa() - mean) / variance,
				(gtrModel.getRb() - mean) / variance,
				(gtrModel.getRc() - mean) / variance,
				(gtrModel.getRd() - mean) / variance,
				(gtrModel.getRe() - mean) / variance,
				(gtrModel.getRf() - mean) / variance };

		double rate[] = new double[RATE_PAIRS];
		int ratePairIndex = 0;
		for (int i = 0; i < (RATES - 1); i++) {
			for (int j = i + 1; j < RATES; j++) {
				rate[ratePairIndex] = computeRates(normalizedRates[i],
						normalizedRates[j]);
				ratePairIndex++;
			}
		}

		if (filterFrequencies
				&& computeFreqs(new double[] { gtrModel.getfA(),
						gtrModel.getfC(), gtrModel.getfG(), gtrModel.getfT() }) < (1 - threshold)) {
			System.out.println("*** FILTER FREQUENCIES");
			filter.addAll(freqFilter);
		}

		if (filterRateMatrix) {
			for (int k = 0; k < RATE_PAIRS; k++) {
				if (rate[k] > threshold && hasVar) {
					filter.addAll(rateFilter[k]);
					System.out.println("*** FILTER RATE " + k);
				}
			}
		}

		filterGamma = filterRateVariation
				&& gtrModel.getShape() > MIN_GAMMA_FILTER;
		filterInv = filterRateVariation
				&& (gtrModel.getPinv() < MAX_INV_FILTER && gtrModel.getShape() > MIN_GAMMA_INV_FILTER);
		filterGammaInv = filterRateVariation
				&& (gtrModel.getShape() > MIN_GAMMA_FILTER && gtrModel
						.getPinv() < MAX_INV_FILTER);
		if (filterGamma | filterInv | filterGammaInv) {
			System.out.println("*** FILTER RATE VARIATION! " + filterGamma + filterInv + filterGammaInv);
		}

		return filter;
	}

	private static double computeRates(double rate1, double rate2) {
		// return Math.abs(Math.min(rate1, rate2) / (rate1+rate2));
		return Math.abs(rate1 - rate2);
	}

	private static double computeFreqs(double[] freqs) {
		Arrays.sort(freqs);
		return Math.abs(freqs[0] / freqs[3]);
	}

	private static double adjustThreshold(double threshold, double lk) {
		if (lk > (X1 * 1000))
			return threshold;
		return adjustThresholdLog(threshold, lk);
	}

	private static double adjustThresholdLog(double threshold, double lk) {
		double value = ((1 - Y0) * Math.log(GVALUE + lk / 1000 + 1) + Y0
				* Math.log(GVALUE + X1 + 1) - Math.log(GVALUE + 1))
				/ (Math.log(GVALUE + X1 + 1) - Math.log(GVALUE + 1));
		return threshold * value;
	}

}
