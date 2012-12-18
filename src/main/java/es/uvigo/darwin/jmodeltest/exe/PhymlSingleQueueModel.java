package es.uvigo.darwin.jmodeltest.exe;

import java.io.File;
import java.util.ArrayList;

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
	private Long 	identifier;
	private long 	computationTime = 0L;	
	
	public PhymlSingleQueueModel(Model model, int index, boolean justGetJCTree, boolean ignoreGaps, ApplicationOptions options) 
	{
		super(model, index, justGetJCTree, ignoreGaps, options);		
		
		writePhyml3CommandLine();
		
		//Same cmd => same job
		this.identifier = Long.valueOf(commandLine.hashCode());
	}
	
	public Long getIdentifier()
	{
		return identifier;
	}
	
	public boolean preCompute(Long jobId)
	{
		if (model.getLnL() < 1e-5 || ignoreGaps) 
		{
			// run phyml
			notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_INIT, index, model, null);

			startTime = System.currentTimeMillis();

			writePhyml3CommandLine();
		
			preExecuteCommandLine(jobId);
		}
		
		return !interrupted;
	}
		
	private void preExecuteCommandLine(Long jobId)
	{
		String[] executable = new String[1];
		
		executable[0] = "phyml";
			
		String[] tokenizedCommandLine = commandLine.split(" ");
		String[] cmd = Utilities.specialConcatStringArrays(executable, tokenizedCommandLine);
				
		ArrayList<String> inputFiles = new ArrayList<String>();
		
		inputFiles.add(options.getAlignmentFile().getAbsolutePath());

		if (options.userTopologyExists || options.fixedTopology)
		{
			inputFiles.add(options.getTreeFile().getAbsolutePath());
		}
		
		ArrayList<String> outputFiles = new ArrayList<String>();
		
		outputFiles.add(phymlStatFileName);
		outputFiles.add(phymlTreeFileName);
				
		String cmdline = "";

		for (int i=0; i<cmd.length; i++)
		{
			cmdline += cmd[i] + " ";
		}
		
		try
		{
			ModelTestQueue.getRunWorkerManager().createJob(jobId, identifier, cmdline, inputFiles, outputFiles);
		}
		catch (Exception e)
		{
			notifyObservers(ProgressInfo.ERROR, index, model, "Cannot init remote Phyml command line for some reason: " + e.getMessage());
			interrupted = true;
		}
	}
			
	public boolean continueCompute()
	{
		if (!interrupted) 
		{			
			parsePhyml3Files();
			
			try 
			{
				computationTime = ModelTestQueue.getRunWorkerManager().getComputationTime(identifier);
			}
			catch (Exception e) 
			{

			}
		}
			
		endTime = System.currentTimeMillis();
			
		model.setComputationTime(endTime - startTime);
			
		// completed
        if (!interrupted) 
        {
            int value = 0;
            if (ignoreGaps) 
            {
                value = ProgressInfo.VALUE_IGAPS_OPTIMIZATION;
            }
            else 
            {
                value = ProgressInfo.VALUE_REGULAR_OPTIMIZATION;
            }
            
            notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_COMPLETED, value, model, String.valueOf(computationTime));
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
			
			optimizationCompleted();
		}
		catch (Exception e) 
		{
			
		}
	}
    
	public void optimizationCompleted()
	{
		Utilities.deleteFile(phymlStatFileName);
		Utilities.deleteFile(phymlTreeFileName);
	}
	
}
