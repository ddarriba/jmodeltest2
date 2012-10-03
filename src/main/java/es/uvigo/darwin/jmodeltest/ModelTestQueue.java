package es.uvigo.darwin.jmodeltest;

import java.util.List;
import java.util.Observer;

import es.udc.gac.nebulator.manager.worker.RunWorkerManager;
import es.uvigo.darwin.jmodeltest.exe.RunPhymlQueue;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
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
	
	public ModelTestQueue(List<String> args, Observer progressObserver, String configFile, TextOutputStream mainConsole, Long jobId)
	{
		super(false, progressObserver, configFile);
		this.runInQueue = true;
		this.jobId = jobId;

		if (mainConsole != null)
			this.setMainConsole(mainConsole);
		
		arguments = new String[args.size()];
		for(int i=0; i < args.size(); i++)
		{
			arguments[i] = args.get(i);
		}

		ParseArguments();
		
		checkFilesAndBuildSetOfModels();
	}
	
	public void startAnalysis()
	{
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
	
	public boolean continueAnalysis(List<Long> identifiers)
    {
		if (((RunPhymlQueue) runPhyml).continueExecute(identifiers))
		{
			endCommandLine();
			return true;
		}
		
   		return false;
    }

	public void cancelAnalysis()
    {
		((RunPhymlQueue) runPhyml).cancelExecute();
    }
	
}
