package es.uvigo.darwin.jmodeltest.exe;

import java.util.ArrayList;
import java.util.Arrays;

import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.model.ModelConstants;

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
	private boolean doFilterFrequencies;
	private boolean doFilterRateMatrix;
	private boolean doFilterRateVariation;
	private boolean invFilter = false;
	private boolean gammaFilter = false;
	private boolean gammaInvFilter = false;
	private boolean freqsFilter = false;	
	private boolean ratesFilter[] = new boolean[RATE_PAIRS];


	static {
		
	}

	public GuidedSearchManager(double guidedSearchThreshold, Model gtrModel,
			boolean filterFrequencies, boolean filterRateMatrix,
			boolean filterRateVariation) {

		this.guidedSearchThreshold = guidedSearchThreshold;
		this.gtrModel = gtrModel;
		this.doFilterFrequencies = filterFrequencies;
		this.doFilterRateMatrix = filterRateMatrix;
		this.doFilterRateVariation = filterRateVariation;

		ModelTest.getMainConsole().println("[Heuristic search] Set up model filter...");
		gtrModel.print(ModelTest.getMainConsole());
		
		setUpFilter(this.guidedSearchThreshold, this.gtrModel);

	}

	public Model[] filterModels(Model[] models) {

		ArrayList<Model> modelsArray = new ArrayList<Model>();
		for (Model model : models) {
			boolean included = true;

			int rateIndex = 0;
			for (int i=0; i<5; i++) {
				for (int j=i+1; j<6; j++) {
					if (checkRates(model.getPartition(), i, j)
							&& ratesFilter[rateIndex]) {
						included = false;
					}
					rateIndex++;
				}
			}

			if (!model.ispF()) {
				included &= !freqsFilter;
			}
			
			if (model.ispI()) {
				if (model.ispG()) {
					included &= !gammaInvFilter;
				} else {
					included &= !invFilter;
				}
			} else if (model.ispG()) {
				included &= !gammaFilter;
			}

			if (included) {
				modelsArray.add(model);
			}
		}

		if (modelsArray.size() < models.length) {
			ModelTest.getMainConsole().println("[Heuristic search] Candidate models set reduced to " + modelsArray.size() + " models");
		} else {
			ModelTest.getMainConsole().println("[Heuristic search] Candidate models set is not reduced (" + modelsArray.size() + " models)");
		}
		
		return modelsArray.toArray(new Model[0]);
	}
	
	public static String[] getPartitions(String partition, int k) {

		ArrayList<String> partitionsArray = new ArrayList<String>();	
		if (k>0 && k<6) {
			boolean equalRates[] = new boolean[] { 
					checkRates(partition,0,1), checkRates(partition,0,2), checkRates(partition,0,3),
					checkRates(partition,0,4), checkRates(partition,0,5), checkRates(partition,1,2),
					checkRates(partition,1,3), checkRates(partition,1,4), checkRates(partition,1,5),
					checkRates(partition,2,3), checkRates(partition,2,4), checkRates(partition,2,5),
					checkRates(partition,3,4), checkRates(partition,3,5), checkRates(partition,4,5)};
			
			for (String curPartition : ModelConstants.fullModelSet.get(k)) {
				boolean included = true;
	
				int rateIndex = 0;
				for (int i=0; i<5; i++) {
					for (int j=i+1; j<6; j++) {
						if (equalRates[rateIndex] && !checkRates(curPartition, i, j)) {
							included = false;
							break;
						}
						rateIndex++;
					}
				}
	
				if (included) {
					partitionsArray.add(curPartition);
				}
			}
		} else {
			return new String[]{"012345"};
		}

		return partitionsArray.toArray(new String[0]);
	}
	
	public static Model[] getModelsSubset(Model[] models, String partition, int k) {

		ArrayList<Model> modelsArray = new ArrayList<Model>();
		for (String curPartition : getPartitions(partition, k)) {
			for (Model model : models) {
				if (model.getPartition().equals(curPartition)) {
					modelsArray.add(model);
				}
			}
		}
		if (k < 6) {
			ModelTest.getMainConsole().println("[Clustering search] Obtain next step models from partition " + partition + "...");
		}
		ModelTest.getMainConsole().println("[Clustering search] Step " + (7-k) + "/6: " + modelsArray.size() + " models.");
		
		return modelsArray.toArray(new Model[0]);
	}

	private static boolean checkRates(String partition, int p0, int p1) {
		return partition.charAt(p0) == partition.charAt(p1);
	}
	
	private void setUpFilter(double guidedSearchThreshold,
			Model gtrModel) {

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

		if (doFilterFrequencies) {
			if (computeFreqs(new double[] { gtrModel.getfA(),
						gtrModel.getfC(), gtrModel.getfG(), gtrModel.getfT() }) < (1 - threshold)) {
				ModelTest.getMainConsole().println("[Heuristic search] Filtering models with equal frequencies");
				freqsFilter = true;
			} else {
				freqsFilter = false;
			}
		} else {
			freqsFilter = false;
		}

		if (doFilterRateMatrix) {
			boolean doRatesFilter = false;
			for (int k = 0; k < RATE_PAIRS; k++) {
				ratesFilter[k] = (rate[k] > threshold && hasVar);
				doRatesFilter |= ratesFilter[k];
			}
			if (doRatesFilter) {
				ModelTest.getMainConsole().println("[Heuristic search] Filtering models with certain equal rates");
			}
		}

		gammaFilter = doFilterRateVariation
				&& gtrModel.getShape() > MIN_GAMMA_FILTER;
		invFilter = doFilterRateVariation
				&& (gtrModel.getPinv() < MAX_INV_FILTER && gtrModel.getShape() > MIN_GAMMA_INV_FILTER);
		gammaInvFilter = doFilterRateVariation
				&& (gtrModel.getShape() > MIN_GAMMA_FILTER && gtrModel
						.getPinv() < MAX_INV_FILTER);
		if (invFilter) {
			ModelTest.getMainConsole().println("[Heuristic search] Filtering +I models");
		}
		if (gammaFilter) {
			ModelTest.getMainConsole().println("[Heuristic search] Filtering +G models");
		}
		if (gammaInvFilter) {
			ModelTest.getMainConsole().println("[Heuristic search] Filtering +I+G models");
		}
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
