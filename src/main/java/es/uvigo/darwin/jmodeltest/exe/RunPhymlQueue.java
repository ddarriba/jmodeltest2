package es.uvigo.darwin.jmodeltest.exe;

import java.util.List;
import java.util.Observer;

import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;

/**
 * 
 * @author Jose Manuel Santorum, University of A Coruña
 *         jose.santorum@udc.es
 *
 */

public class RunPhymlQueue extends RunPhyml 
{
	private PhymlQueueModel pqm;
	private boolean treeComputed = true;
	private boolean cancel = false;
	
	public RunPhymlQueue(Observer progress, ModelTest modelTest, Model[] models, Long jobId) 
	{
		super(progress, modelTest, models);
		
		pqm = new PhymlQueueModel(jobId);
	}
		
	public void estimateTree()
	{
		treeComputed = false;
		PhymlSingleQueueModel jcModelPhyml = new PhymlSingleQueueModel(jcModel, 0, true, options);
		jcModelPhyml.addObserver(this);
		pqm.add(jcModelPhyml);
		pqm.execute();
	}
		
	@Override
	protected Object doPhyml() 
	{
		if (!treeComputed) return null;
		
		// compute likelihood scores for all models
		// System.out.print("computing likelihood scores for "
		// + models.length + " models with Phyml " + PHYML_VERSION);
		
		notifyObservers(ProgressInfo.OPTIMIZATION_INIT, 0, models[0], null);
		
		int current = 0;
		for (Model model : models) 
		{
			if (cancel) return null;
		
			PhymlSingleQueueModel psqm = new PhymlSingleQueueModel(model, current, false, options);
			psqm.addObserver(this);
			pqm.add(psqm);			
			current++;
		}
		
		pqm.execute();
		
		return null;
	}
	
	public boolean continueExecute(List<Long> identifiers)
	{
		boolean end = pqm.continueExecute(identifiers);
		
		if (!treeComputed)
		{
			createTree();
			treeComputed = true;
			doPhyml();
			
			return false;
		}
		
		if (end)
		{
			pqm.optimizationCompleted();
			notifyObservers(ProgressInfo.OPTIMIZATION_COMPLETED_OK,	models.length, null, null);
		}
		
		return end;
	}
	
	public void cancelExecute()
	{
		cancel = true;
		pqm.cancelExecute();
	}
	
}
