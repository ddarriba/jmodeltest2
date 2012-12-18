package es.uvigo.darwin.jmodeltest.exe;

import java.util.ArrayList;
import java.util.List;

import es.uvigo.darwin.jmodeltest.ModelTestQueue;
import es.uvigo.darwin.prottest.util.exception.ProtTestInternalException;


/**
 * 
 * @author Jose Manuel Santorum, University of A Coruña
 *         jose.santorum@udc.es
 *
 */

public class PhymlQueueModel 
{
	private List<PhymlSingleQueueModel> initPsqms	= new ArrayList<PhymlSingleQueueModel>();
	private List<PhymlSingleQueueModel> runPsqms	= new ArrayList<PhymlSingleQueueModel>();
	private List<PhymlSingleQueueModel> endPsqms	= new ArrayList<PhymlSingleQueueModel>();
	private boolean stop = false;
	private Long jobId;
	
	public PhymlQueueModel(Long jobId)
	{
		this.jobId = jobId;
	}
	
	public void add(PhymlSingleQueueModel psqm) 
	{
		initPsqms.add(psqm);
	}

	public synchronized void execute() 
	{
		List<Long> identifiers = new ArrayList<Long>();
		
		for (PhymlSingleQueueModelIterator i = new PhymlSingleQueueModelIterator(initPsqms); i.hasNext(); )
		{
			if (stop) return;
			
			PhymlSingleQueueModel psqm = i.next();
			
			if (!psqm.preCompute(jobId))
			{
				throw new ProtTestInternalException("PreComputeError");
			}
		
			identifiers.add(psqm.getIdentifier());
			
			i.remove(); /* -> */ runPsqms.add(psqm);
		}
		
		ModelTestQueue.getRunWorkerManager().processJobs(identifiers);
	}
	
	public synchronized void continueExecute(List<Long> identifiers)
	{			
		for (PhymlSingleQueueModelIterator i = new PhymlSingleQueueModelIterator(runPsqms); i.hasNext(); )
		{
			if (stop) return;
			
			PhymlSingleQueueModel psqm = i.next();

			if (identifiers.contains(psqm.getIdentifier()))
			{		
				if (!psqm.continueCompute())
				{
					throw new ProtTestInternalException("Optimization error");
				}
				
				i.remove(); /* -> */ endPsqms.add(psqm);
			}
		}
	}

	public boolean moreJobs()
	{				
		return ((initPsqms.size()	!= 0) ||
				(runPsqms.size()	!= 0)); 
	}
	
	public void optimizationCompleted()
	{
		List<Long> identifiers = new ArrayList<Long>();
		
		for (PhymlSingleQueueModelIterator i = new PhymlSingleQueueModelIterator(endPsqms); i.hasNext(); )
		{
			PhymlSingleQueueModel psqm = i.next();
			
			psqm.optimizationCompleted();
			
			identifiers.add(psqm.getIdentifier());
			
			i.remove();
		}
		
		ModelTestQueue.getRunWorkerManager().removeJobs(identifiers);
	}
	
	public void cancelExecute() 
	{
		stop = true;
		
		List<Long> identifiers = new ArrayList<Long>();
		
		for (PhymlSingleQueueModelIterator i = new PhymlSingleQueueModelIterator(runPsqms); i.hasNext(); )
		{
			PhymlSingleQueueModel psqm = i.next();
			
			psqm.cancelCompute();
			
			identifiers.add(psqm.getIdentifier());
			
			i.remove();
		}
		
		ModelTestQueue.getRunWorkerManager().cancelJobs(identifiers);
	}
}
