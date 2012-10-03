package es.uvigo.darwin.jmodeltest.exe;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTestQueue;
import es.uvigo.darwin.jmodeltest.io.TextInputStream;
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
	private Long identifier;
	private String queueFileName;
	private long startComputeTime;
	private long endComputeTime;
	private boolean preContinue = false;
	
	public PhymlSingleQueueModel(Model model, int index, boolean justGetJCTree, ApplicationOptions options) 
	{
		super(model, index, justGetJCTree, options);		
	}
	
	public Long getIdentifier()
	{
		return identifier;
	}
	
	public boolean preCompute(Long jobId)
	{
		// run phyml
		notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_INIT, index, model, null);

		startTime = System.currentTimeMillis();

		writePhyml3CommandLine(model, justGetJCTree);
		
		preExecuteCommandLine(jobId);
		
		return !interrupted;
	}
		
	private void preExecuteCommandLine(Long jobId)
	{
		String[] executable = new String[1];
		
		executable[0] = "phyml";
			
		String[] tokenizedCommandLine = commandLine.split(" ");
		String[] cmd = Utilities.specialConcatStringArrays(executable, tokenizedCommandLine);
		
		//Same cmd => same job
		this.identifier = Long.valueOf(commandLine.hashCode());
		this.queueFileName = getDirName(this.phymlStatFileName) +  File.separator + String.format("jobDetails%s.txt", this.identifier);
		
		ArrayList<String> files = new ArrayList<String>();
		
		files.add(options.getAlignmentFile().getAbsolutePath());

		if (options.userTopologyExists || options.fixedTopology)
		{
			files.add(options.getTreeFile().getAbsolutePath());
		}
		
		String[] clines = new String[5];

		clines[0] = "echo \"#\" `date` > " + getFileName(queueFileName);
		clines[1] = "echo \"TIME INIT:\" `date +%s` >> " + getFileName(queueFileName);
		
		clines[2] = "";
		for (int i=0; i<cmd.length; i++)
		{
			clines[2] += cmd[i] + " ";
		}
		
		clines[3] = "echo \"#\" `date` >> " + getFileName(queueFileName);
		clines[4] = "echo \"TIME END:\" `date +%s` >> " + getFileName(queueFileName);
		
		try
		{
			ModelTestQueue.getRunWorkerManager().initJob(jobId, identifier, clines);
			ModelTestQueue.getRunWorkerManager().setJobFiles(identifier, files, true);
		}
		catch (Exception e)
		{
			notifyObservers(ProgressInfo.ERROR, index, model, "Cannot init remote Phyml command line for some reason: " + e.getMessage());
			interrupted = true;
		}
	}
		
	public boolean preContinueCompute(Long jobId)
	{
		if (!preContinue)
		{
			preContinue = true;
			
			try 
			{
				if (!interrupted) 
				{			
					ArrayList<String> files = new ArrayList<String>();
					
					files.add(phymlStatFileName);
					files.add(phymlTreeFileName);
					files.add(queueFileName);
					
					ModelTestQueue.getRunWorkerManager().setJobFiles(identifier, files, false);
				}			
			}
			catch (Exception e) 
			{
				interrupted = true;
				notifyObservers(ProgressInfo.ERROR, index, model, "Cannot continue remote Phyml command line for some reason:" + e.getMessage());
			}
		}
		
		return !interrupted;
	}
	
	public boolean continueCompute()
	{
		try 
		{
			if (!interrupted) 
			{			
				parsePhyml3Files(model);
				parseQueueFiles();
			}
			
			endTime = System.currentTimeMillis();
			
			model.setComputationTime(endTime - startTime);
				
			// completed
			if (!interrupted) 
			{			
//				notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_COMPLETED, index, model, 
//						Utilities.calculateRuntime(startTime, endTime) + "::" + String.valueOf(endTime - startTime) + "::" + commandLine);
//				notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_COMPLETED, index, model, Utilities.calculateRuntime(startTime, endTime));
				notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_COMPLETED, index, model, String.valueOf(endComputeTime - startComputeTime));
				
				ModelTestQueue.getRunWorkerManager().endJob(identifier);
			}
		}
		catch (Exception e) 
		{
			notifyObservers(ProgressInfo.ERROR, index, model, "Cannot end remote Phyml command line for some reason; " + e.getMessage());
		}
		
		return !interrupted;
	}
	
	@Override 
	protected String inputFileNameCommandLine()
	{
		return getFileName(options.getAlignmentFile().getAbsolutePath());
	}
	
	@Override 
	protected String treeFileNameCommandLine()
	{
		return getFileName(options.getTreeFile().getAbsolutePath());
	}
			
	@Override
	public void run() 
	{
		//Do nothing	
	}
	
    private String getFileName(String file)
    {
    	return file.substring(file.lastIndexOf(File.separator) + 1);
    }
    
    private String getDirName(String file)
    {
    	return file.substring(0, file.lastIndexOf(File.separator));
    }
    
	public void cancelCompute() 
	{
		try 
		{
			interrupted = true;
			
			ModelTestQueue.getRunWorkerManager().cancelJob(identifier);
			
			optimizationCompleted();
		}
		catch (Exception e) 
		{
			
		}
	}
    
	public void optimizationCompleted()
	{
		ModelTestQueue.getRunWorkerManager().unassignResource(identifier);
		
		Utilities.deleteFile(phymlStatFileName);
		Utilities.deleteFile(phymlTreeFileName);
		Utilities.deleteFile(queueFileName);
	}
	
	private void parseQueueFiles()
	{
		String line;
		
		try 
		{
			TextInputStream phymlStatFile = new TextInputStream(queueFileName);
			
			while ((line = phymlStatFile.readLine()) != null) 
			{
				if (line.length() > 0 && line.startsWith("TIME INIT:")) 
				{
					startComputeTime = Long.parseLong(Utilities.lastToken(line));
				} 
				else if (line.length() > 0 && line.startsWith("TIME END:")) 
				{
					endComputeTime = Long.parseLong(Utilities.lastToken(line));
				}
			}
			
			phymlStatFile.close();
		}
		catch (FileNotFoundException e) 
		{
			notifyObservers(ProgressInfo.ERROR, index, model, "Queue results file does not exist: " + queueFileName);
			interrupted=true;
		}
	}
}
