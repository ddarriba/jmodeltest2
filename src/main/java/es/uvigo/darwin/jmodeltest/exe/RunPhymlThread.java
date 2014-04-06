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
package es.uvigo.darwin.jmodeltest.exe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;
import es.uvigo.darwin.jmodeltest.selection.AIC;
import es.uvigo.darwin.jmodeltest.selection.AICc;
import es.uvigo.darwin.jmodeltest.selection.BIC;
import es.uvigo.darwin.jmodeltest.selection.InformationCriterion;

public class RunPhymlThread extends RunPhyml {

	private ExecutorService threadPool;
	private int currentStage;
	private int numModelsInStage;
	
	public RunPhymlThread(Observer progress, ApplicationOptions options,
			Model[] models) {
		super(progress, options, models);

		this.threadPool = Executors.newFixedThreadPool(options
				.getNumberOfThreads());
	}

	/*******************************
	 * doPhyml ****************************** * run the phyml calculations on a
	 * separate thread * * *
	 ***********************************************************************/

	protected Object doPhyml() {

		boolean errorsFound = false;
		if (options.isClusteringSearch()) {
			List<Model> evaluatedModels = new ArrayList<Model>();
			
			evaluatedModels.add(gtrModel);
			Model globalBestModel = gtrModel;
			if (gtrModel == null) {
				globalBestModel = models[models.length-1];
			}
			double globalBestScore = Double.MAX_VALUE;
			for (int groups=6; groups>0; groups--) {
				double bestScore = Double.MAX_VALUE;
				String partition = globalBestModel==null?"012345":globalBestModel.getPartition();
				Model[] currentModels = GuidedSearchManager.getModelsSubset(models, partition, groups);

				currentStage = 7-groups;
				numModelsInStage = currentModels.length;
				
				if (currentModels.length > 0) {
					// Optimize the current models
					Model bestModel = currentModels[0];
					errorsFound |= !parallelExecute(currentModels, false);
					
					for (Model model : currentModels) {
						double currentScore = Double.MAX_VALUE - 1.0;
						switch (options.getHeuristicInformationCriterion()) {
						case InformationCriterion.IC_AIC:
							currentScore = AIC.computeAic(model, options);
							break;
						case InformationCriterion.IC_BIC:
							currentScore = BIC.computeBic(model, options);
							break;
						case InformationCriterion.IC_AICc:
							currentScore = AICc.computeAicc(model, options);
							break;
						}
						if (currentScore < bestScore) {
							bestModel = model;
							bestScore = currentScore;
						}
					}

					// Check LnL
					if (globalBestModel.getLnL()>0 && bestScore > globalBestScore) {
						// End of algorithm
						break;
					} else {
						globalBestModel = bestModel;
						globalBestScore = bestScore;
					}
				}
			}
			ModelTest.purgeModels();
		} else {
			errorsFound = !parallelExecute(models, false);
		}
		if (errorsFound) {
			notifyObservers(ProgressInfo.OPTIMIZATION_COMPLETED_INTERRUPTED, models.length,
				null, null);
			return "Interrupted";
		} else {
			notifyObservers(ProgressInfo.OPTIMIZATION_COMPLETED_OK, models.length,
					null, null);
		}

		return "All Done";
	} // doPhyml

	protected boolean parallelExecute(Model models[], boolean ignoreGaps) {
		Collection<Callable<Object>> c = new ArrayList<Callable<Object>>();
		int current = 0;
		for (Model model : models) {
			if (model != null) {
				PhymlSingleModel psm = new PhymlSingleModel(model, current, false,
						ignoreGaps, options);
				psm.addObserver(this);
				c.add(Executors.callable(psm));
	
				current++;
			}
		}

		Collection<Future<Object>> futures = null;
		try {
			futures = threadPool.invokeAll(c);
		} catch (InterruptedException e) {
			notifyObservers(ProgressInfo.INTERRUPTED, 0, null, null);
		}

		if (futures != null) {
			for (Future<Object> f : futures) {
				try {
					f.get();
				} catch (InterruptedException ex) {
					notifyObservers(ProgressInfo.INTERRUPTED, 0, null, null);
					ex.printStackTrace();
					return false;
				} catch (ExecutionException ex) {
					// Internal exception while computing model.
					// Let's continue with errors
					ex.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}
	
	public void interruptThread() {
		super.interruptThread();
		ProcessManager.getInstance().killAll();
		threadPool.shutdownNow();// shutdown();
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg != null) {
			ProgressInfo info = (ProgressInfo) arg;
			if (info.getType() == ProgressInfo.ERROR || 
					info.getType() == ProgressInfo.ERROR_BINARY_NOEXECUTE || 
					info.getType() == ProgressInfo.ERROR_BINARY_NOEXISTS) {
				interruptThread();
			} else if (options.isClusteringSearch()) {
				info.setHeuristicStage(currentStage);
				info.setNumModelsInStage(numModelsInStage);
			}
		}
		super.update(o, arg);
	}
}
