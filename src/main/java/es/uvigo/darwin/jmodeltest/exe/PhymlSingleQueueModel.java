package es.uvigo.darwin.jmodeltest.exe;

import es.udc.gac.hpcmanager.webapp.worker.util.exception.NotAvailableResourceException;
import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTestQueue;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;


/**
 * 
 * @author Jose Manuel Santorum, University of A Coruña
 *         jose.santorum@udc.es
 *
 */

public class PhymlSingleQueueModel extends PhymlSingleModel 
{

	private Integer jobId;
	private static final String REMOTE_DIR = "$HPC_INPUT/"; //PFC/input/";

	public PhymlSingleQueueModel(Model model, int index, boolean justGetJCTree, ApplicationOptions options) 
	{
		super(model, index, justGetJCTree, options);		
		
		this.phymlStatFileName = options.getAlignmentFile().getAbsolutePath()
				+ RunPhyml.PHYML_STATS_SUFFIX + model.getName() + ".txt";
		this.phymlTreeFileName = options.getAlignmentFile().getAbsolutePath()
				+ RunPhyml.PHYML_TREE_SUFFIX + model.getName() + ".txt";
	}
	
	public Integer getJobId()
	{
		return jobId;
	}
	
	public boolean compute() 
	{
		// run phyml
		notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_INIT, index, model, null);

		startTime = System.currentTimeMillis();

		writePhyml3CommandLine(model, justGetJCTree);
		executeCommandLine();
		
		return !interrupted;
	}
	
	public boolean continueCompute() throws NotAvailableResourceException
	{
		if (!interrupted) 
		{
			ModelTestQueue.getRunWorkerManager().getResource(this).copyFile(getFileName(phymlStatFileName), REMOTE_DIR, phymlStatFileName, "", false);
			ModelTestQueue.getRunWorkerManager().getResource(this).copyFile(getFileName(phymlTreeFileName), REMOTE_DIR, phymlTreeFileName, "", false);
			
			parsePhyml3Files(model);
		}

		endTime = System.currentTimeMillis();

		model.setComputationTime(endTime - startTime);

		// completed
		if (!interrupted) 
		{
			notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_COMPLETED, index,
				model, Utilities.calculateRuntime(startTime, endTime));

		}
		return !interrupted;
	}
	
	@Override
	protected void writePhyml3CommandLine(Model currentModel, boolean justGetJCtree) 
	{
		try
		{
			// input file
			String inputFile = getFileName(options.getAlignmentFile().getAbsolutePath());
			commandLine = " -i " + inputFile; //options.getAlignmentFile().getAbsolutePath();
			ModelTestQueue.getRunWorkerManager().getResource(this).copyFile(options.getAlignmentFile().getAbsolutePath(), "", inputFile, REMOTE_DIR, true);
	
			commandLine += " -d " + "nt";
	
			// number of data sets
			commandLine += " -n " + "1";
	
			// no bootrstrap or aLRT
			commandLine += " -b " + "0";
	
			// set execution id
			commandLine += " --run_id " + model.getName();
	
			// set custom model
			commandLine += " -m " + currentModel.getPartition();
	
			// optimize base frequencies if needed
			if (currentModel.ispF())
				commandLine += " -f m"; // changed from -f e DP200509
			else
				commandLine += " -f 0.25,0.25,0.25,0.25";
	
			// optimize pinvar if needed
			if (currentModel.ispI())
				commandLine += " -v " + "e";
	
			// optimize rate parameters
			// if (currentModel.pT || currentModel.pR)
			// commandLine += rateParameters;
	
			// optimize alpha if needed
			if (currentModel.ispG()) 
			{
				commandLine += " -c " + options.numGammaCat;
				commandLine += " -a e";
			} else
				commandLine += " -c " + 1;
	
			// search strategy
			switch (options.treeSearchOperations) 
			{
				case SPR:
					commandLine += " -s " + "SPR";
					break;
				case BEST:
					commandLine += " -s " + "BEST";
					break;
				default:
					commandLine += " -s " + "NNI";
			}
	
			// threaded version
			if (numberOfThreads > 0) 
			{
				commandLine += " --num_threads " + numberOfThreads;
			}
	
			// avoid memory warning
			commandLine += " --no_memory_check";
	
			// do optimize topology?
			/*
			 * params=tlr: tree topology (t), branch length (l) and substitution
			 * rate parameters (r) are optimised. params = tlr or tl: optimize tree
			 * topology and branch lengths params = lr or l: tree topology fixed;
			 * optimize branch lengths; params = r or none: both tree topology and
			 * branch lengths are fixed.
			 */
			if (justGetJCtree) 
			{
				commandLine += " -o " + "r"; // both tree topology and branch
											 // lengths are fixed.
			}
			/*
			 * else if (ModelTest.userTreeExists) // use user tree for all models {
			 * commandLine += " -u " + userTreeFileName; commandLine += " -o " +
			 * "r"; // both tree topology and branch lengths are fixed. }
			 */
			// use a single tree for all models
			else if (options.userTopologyExists || options.fixedTopology) 
			{
				String treeFile = getFileName(options.getTreeFile().getAbsolutePath());
				commandLine += " -u " + treeFile; //options.getTreeFile().getAbsolutePath();
				ModelTestQueue.getRunWorkerManager().getResource(this).copyFile(options.getTreeFile().getAbsolutePath(), "", treeFile, REMOTE_DIR, true);
				
				commandLine += " -o " + "lr"; // tree topology fixed; optimize
											  // branch lengths
			} 
			else if (!options.optimizeMLTopology) // use BIONJ tree for
												  // each model
			{
				commandLine += " -o " + "lr"; // tree topology fixed; optimize
											  // branch lengths
			} else 
			{
				commandLine += " -o " + "tlr"; // optimize tree topology and branch
											   // lengthss
			} // use ML optimized tree for each model
		}
		catch (NotAvailableResourceException e)
		{
			notifyObservers(ProgressInfo.ERROR, index, model, "Cannot run the Phyml command line for some reason: " + e.getMessage());
			interrupted = true;
		}

	}
	
	@Override
	protected void executeCommandLine() 
	{
		String[] executable = new String[1];
			
		executable[0] = "phyml";
			
		String[] tokenizedCommandLine = commandLine.split(" ");
		String[] cmd = Utilities.specialConcatStringArrays(executable, tokenizedCommandLine);

		try
		{
			jobId = ModelTestQueue.getRunWorkerManager().getResource(this).initJob(cmd);
		}
		catch (NotAvailableResourceException e)
		{
			notifyObservers(ProgressInfo.ERROR, index, model, "Cannot run the Phyml command line for some reason: " + e.getMessage());
			interrupted = true;
		}
	}
	
	@Override
	public void run() 
	{
	//	compute();
	}
	
    private String getFileName(String filePath)
    {
    	return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

	public void cancelCompute() 
	{
		try
		{
			ModelTestQueue.getRunWorkerManager().getResource(this).cancelJob(jobId);
		}
		catch (NotAvailableResourceException e)
		{
			e.printStackTrace();
		}
	}
    
}
