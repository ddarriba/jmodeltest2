package es.uvigo.darwin.jmodeltest.exe;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;
import es.uvigo.darwin.jmodeltest.selection.AIC;
import es.uvigo.darwin.jmodeltest.selection.AICc;
import es.uvigo.darwin.jmodeltest.selection.BIC;
import es.uvigo.darwin.jmodeltest.selection.InformationCriterion;

/**
 * 
 * @author Jose Manuel Santorum, University of A Coruña
 *         jose.santorum@udc.es
 *
 */

public class RunPhymlQueue extends RunPhyml 
{
	private PhymlQueueModel pqm;
	private boolean 		treeComputed = true;
	private boolean 		gtrComputed = true;
	private boolean			optimizationInit = false;
	private boolean			phymlCompleted = true;
	private boolean 		stop = false;
	
	private PhymlSingleQueueModel jcModelPhyml;
	private PhymlSingleQueueModel gtrPhymlModel;
	private int currentStage;
	private int numModelsInStage;
	
	private Model[] currentModels;
	private Model 	globalBestModel = null;
	private double 	globalBestScore = 0L;
	private Model	bestModel = null;
	private	double 	bestScore = 0L;
	private int 	groups = 6;
	
	public RunPhymlQueue(Observer progress, ModelTest modelTest, Model[] models, Long jobId) 
	{
		super(progress, modelTest, models);
		
		this.pqm = new PhymlQueueModel(jobId);
	}
	
	@Override
	public void execute() 
	{
		preparePhyml();

		checkFixedTopology();
		
		if (treeComputed)
		{
			checkGuidedSearch();
		}
		
		if (treeComputed && gtrComputed)
		{
			doPhyml();
		}
	}
	
	@Override
	public void estimateTree()
	{		
		treeComputed = false;
		notifyObservers(ProgressInfo.BASE_TREE_INIT, 0, jcModel, null);

		jcModelPhyml = new PhymlSingleQueueModel(jcModel, 0, true, false, options);
		jcModelPhyml.addObserver(this);
		pqm.add(jcModelPhyml);
		pqm.execute();
	}
	
	@Override
	protected void computeGTRModel()
	{
		gtrComputed = false;
		// compute GTR model
        notifyObservers(ProgressInfo.GTR_OPTIMIZATION_INIT, models.length, gtrModel, null);
		gtrPhymlModel = new PhymlSingleQueueModel(gtrModel, 0, false, false, options);
		pqm.add(gtrPhymlModel);
		pqm.execute();
	}
	
	@Override
	protected Object doPhyml() 
	{
		if (!optimizationInit)
		{
			optimizationInit = true;
			phymlCompleted = false;
		
			// compute likelihood scores for all models

			notifyObservers(ProgressInfo.OPTIMIZATION_INIT, 0, models[0], null);
			
			if (options.isClusteringSearch()) 
			{
				doClusteringSearch();
			}
			else 
			{
				parallelExecute(models, false);
			}
		}
		
		return null;
	}
	
	private void doClusteringSearch()
	{
		List<Model> evaluatedModels = new ArrayList<Model>();
        
        evaluatedModels.add(gtrModel);
        globalBestModel = gtrModel;
        
        if (gtrModel == null) 
        {
            globalBestModel = models[models.length - 1];
        }
        
        globalBestScore = Double.MAX_VALUE;
        
       	doClusteringSearchStep();
	}
	
	private void doClusteringSearchStep()
	{
		bestScore = Double.MAX_VALUE;
		String partition = globalBestModel==null ? "012345" : globalBestModel.getPartition();
		currentModels = GuidedSearchManager.getModelsSubset(models, partition, groups, modelTest);

		currentStage = 7 - groups;
		numModelsInStage = currentModels.length;
                
		if (currentModels.length > 0) 
		{
			// Optimize the current models
			bestModel = currentModels[0];
			parallelExecute(currentModels, false);
		}
	}
	
	private void continueClusteringSearchStep()
	{
		for (Model model : currentModels) 
		{
			double currentScore = Double.MAX_VALUE - 1.0;
			switch (options.getHeuristicInformationCriterion()) 
			{
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
			
			if (currentScore < bestScore) 
			{
				bestModel = model;
				bestScore = currentScore;
			}
		}
        
		// Check LnL
		if (globalBestModel.getLnL()>0 && bestScore > globalBestScore) 
		{
			// End of algorithm
			groups = 0;
		}
		else 
		{
			globalBestModel = bestModel;
			globalBestScore = bestScore;
		}
	}
	
	private void endClusteringSearch()
	{
		modelTest.purgeModels();
	}
	
	public synchronized void continueExecute(List<Long> identifiers)
	{
		pqm.continueExecute(identifiers);
		
		if (!treeComputed && identifiers.contains(jcModelPhyml.getIdentifier()))
		{
			createTree();
			
			treeComputed = true;
			
			checkGuidedSearch();
		}
		
		if (treeComputed && !gtrComputed && identifiers.contains(gtrPhymlModel.getIdentifier()))
		{
			guidedSearchGTRModel();
			
			gtrComputed = true;
		}
		
		if (treeComputed && gtrComputed && !optimizationInit)
		{
			doPhyml();
		}
		
		if (treeComputed && gtrComputed && optimizationInit && !phymlCompleted && !moreJobs())
		{
			if (options.isClusteringSearch())
			{	
				continueClusteringSearchStep();
				groups--;
				
				if (groups > 0)
				{
					doClusteringSearchStep();
				}
				else
				{
					endClusteringSearch();
				}
			}

			if (!moreJobs())
			{
				pqm.optimizationCompleted();
				
				notifyObservers(ProgressInfo.OPTIMIZATION_COMPLETED_OK,	models.length, null, null);
				
				phymlCompleted = true;
			}
		}
	}
	
	public boolean moreJobs()
	{
		return pqm.moreJobs();
	}
	
	public void cancelExecute()
	{
		stop = true;
		pqm.cancelExecute();
	}
	
	@Override
	protected boolean parallelExecute(Model models[], boolean ignoreGaps) 
	{
		int current = 0;
		for (Model model : models) 
		{
			if (stop) return false;
		
			PhymlSingleQueueModel psqm = new PhymlSingleQueueModel(model, current, false, ignoreGaps, options);
			psqm.addObserver(this);
			pqm.add(psqm);			
			current++;
		}

		pqm.execute();
		
		return false;
	}
	
	@Override
	public void update(Observable o, Object arg) 
	{
		if (arg != null) 
		{
			ProgressInfo info = (ProgressInfo) arg;
			if (info.getType() == ProgressInfo.ERROR) 
			{
				interruptThread();
			}
			else if (options.isClusteringSearch()) 
			{
				info.setHeuristicStage(currentStage);
				info.setNumModelsInStage(numModelsInStage);
			}
		}
		super.update(o, arg);
	}
}
