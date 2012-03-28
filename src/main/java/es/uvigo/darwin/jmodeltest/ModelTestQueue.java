package es.uvigo.darwin.jmodeltest;

import java.util.List;
import java.util.Observer;

import es.udc.gac.hpcmanager.webapp.worker.RunWorkerManager;
import es.udc.gac.hpcmanager.webapp.worker.util.exception.NotAvailableResourceException;
import es.uvigo.darwin.jmodeltest.exe.RunPhymlQueue;
import es.uvigo.darwin.jmodeltest.utilities.Simulation;

/**
 * 
 * @author Jose Manuel Santorum, University of A Coruña
 *         jose.santorum@udc.es
 *
 */

public class ModelTestQueue extends ModelTest
{

	private static RunWorkerManager runWorkerManager;
	
	public static RunWorkerManager getRunWorkerManager()
	{
		return runWorkerManager;
	}
	
	public static void setRunWorkerManager(RunWorkerManager runWorkerManager)
	{
		ModelTestQueue.runWorkerManager = runWorkerManager;
	}
	
	public ModelTestQueue(String[] args, Observer progressObserver)
	{
		super(false, progressObserver);
		runInQueue = true;
		arguments = args;
		ParseArguments();
		if (options.doingSimulations) 
		{
			Simulation sim = new Simulation(this);
			sim.run();
		} 
		else
		{
			runCommandLine();	
		}
	}
	
	public boolean continueAnalysis(int jobId)
    {
   		try 
   		{
   			if (((RunPhymlQueue) runPhyml).continueExecute(jobId))
   			{
   				endCommandLine();
   				return true;
   			}
		}
   		catch (NotAvailableResourceException e) 
   		{
			e.printStackTrace();
		}
   		
   		return false;
    }

	public List<Integer> pendingJobs() 
	{
		return ((RunPhymlQueue) runPhyml).pendingJobs();
	}
	
}
