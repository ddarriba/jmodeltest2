package es.uvigo.darwin.jmodeltest.exe;

import java.util.List;
import java.util.Observer;

import es.udc.gac.hpcmanager.webapp.worker.util.exception.NotAvailableResourceException;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
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
	private Model jcModel = null;
	private boolean treeComputed = false;
	
	public RunPhymlQueue(Observer progress, ModelTest modelTest, Model[] models) 
	{
		super(progress, modelTest, models);
		pqm = new PhymlQueueModel();
	}

	@Override
	protected Object doPhyml() 
	{
		return null;
	}
	
	@Override
	public void execute() 
	{
		// remove stuff from exe directories before starting
		deleteFiles();
		printSettings(modelTest.getMainConsole());

		// estimate a NJ-JC tree if needed
		if (options.fixedTopology) 
		{
			for (Model model : models) 
			{
				if (model.getName().equals("JC")) 
				{
					jcModel = model;
					break;
				}
			}

			if (jcModel != null) 
			{
				notifyObservers(ProgressInfo.BASE_TREE_INIT, 0, jcModel, null);

				PhymlSingleQueueModel jcModelPhyml = new PhymlSingleQueueModel(jcModel, 0, true, options);
				pqm.add(jcModelPhyml);
				pqm.execute();
			}

		}
		else
		{
			executeModels();
		}


	}
	
	private void createTree()
	{
		// create JCtree file
		TextOutputStream JCtreeFile = new TextOutputStream(options.getTreeFile().getAbsolutePath(), false);
		JCtreeFile.print(jcModel.getTreeString() + "\n");
		JCtreeFile.close();

		notifyObservers(ProgressInfo.BASE_TREE_COMPUTED, 0, jcModel, null);
		
		executeModels();
	}
	
	private void executeModels()
	{		
		// compute likelihood scores for all models
		// System.out.print("computing likelihood scores for "
		// + models.length + " models with Phyml " + PHYML_VERSION);

		notifyObservers(ProgressInfo.OPTIMIZATION_INIT, 0, models[0], null);
		
		int current = 0;
		for (Model model : models) 
		{
			PhymlSingleQueueModel psm = new PhymlSingleQueueModel(model, current, false, options);
			psm.addObserver(this);
			pqm.add(psm);			
			current++;
		}
		
		pqm.execute();
		treeComputed = true;
	}
	
	public boolean continueExecute(Integer jobId) throws NotAvailableResourceException
	{
		boolean end = pqm.continueExecute(jobId);
		
		if (!treeComputed)
		{
			createTree();
			return false;
		}
		
		if (end)
		{
			notifyObservers(ProgressInfo.OPTIMIZATION_COMPLETED_OK,	models.length, null, null);
		}
		
		return end;
	}
	
	public void cancelExecute() throws NotAvailableResourceException
	{
		 pqm.cancelExecute();
	}
	
	public List<Integer> pendingJobs() 
	{
		List<Integer> jobIds;
		
		jobIds = pqm.pendingJobs();
		
		return jobIds;
	}
	
}
