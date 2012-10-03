package es.uvigo.darwin.jmodeltest.exe;

import java.util.ArrayList;
import java.util.List;

import es.udc.gac.nebulator.manager.worker.util.exception.NotAvailableResourceException;
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
	private List<PhymlSingleQueueModel> preEndPsqms	= new ArrayList<PhymlSingleQueueModel>();
	private List<PhymlSingleQueueModel> endPsqms	= new ArrayList<PhymlSingleQueueModel>();
	private boolean cancel = false;
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
		for (PhymlSingleQueueModelIterator i = new PhymlSingleQueueModelIterator(initPsqms); i.hasNext(); )
		{
			if (cancel) break;
			
			PhymlSingleQueueModel psqm = i.next();
			
			if (!psqm.preCompute(jobId))
			{
				throw new ProtTestInternalException("PreComputeError");
			}
			
			i.remove(); /* -> */ runPsqms.add(psqm);
		}
		
		try
		{
			ModelTestQueue.getRunWorkerManager().assignResource(jobId);
		}
		catch (NotAvailableResourceException e)
		{
			throw new ProtTestInternalException("NotAvailableResourceException");
		}
		
		ModelTestQueue.getRunWorkerManager().processJobFiles(jobId, true);
		ModelTestQueue.getRunWorkerManager().processJobCommands(jobId);
		
	}
	
	public synchronized boolean continueExecute(List<Long> identifiers)
	{			
		for (PhymlSingleQueueModelIterator i = new PhymlSingleQueueModelIterator(runPsqms); i.hasNext(); )
		{
			if (cancel) break;
			
			PhymlSingleQueueModel psqm = i.next();
			
			if (identifiers.contains(psqm.getIdentifier()))
			{				
				if (!psqm.preContinueCompute(jobId))
				{
					throw new ProtTestInternalException("PreContinueComputeException");
				}
				
				i.remove(); /* -> */ preEndPsqms.add(psqm);
			}
		}

		ModelTestQueue.getRunWorkerManager().processJobFiles(jobId, false);

		for (PhymlSingleQueueModelIterator i = new PhymlSingleQueueModelIterator(preEndPsqms); i.hasNext(); )
		{
			if (cancel) break;
			
			PhymlSingleQueueModel psqm = i.next();

			if (	identifiers.contains(psqm.getIdentifier()) 
				&&	ModelTestQueue.getRunWorkerManager().filesCopied(psqm.getIdentifier(), false))
			{		
				if (!psqm.continueCompute())
				{
					throw new ProtTestInternalException("Optimization error");
				}
				
				i.remove(); /* -> */ endPsqms.add(psqm);
			}
		}

		return ((runPsqms.size() == 0) && (preEndPsqms.size() == 0));
	}

	public void optimizationCompleted()
	{
		for (PhymlSingleQueueModelIterator i = new PhymlSingleQueueModelIterator(endPsqms); i.hasNext(); )
		{
			PhymlSingleQueueModel psqm = i.next();
			
			psqm.optimizationCompleted();
			
			i.remove();
		}
	}
	
	public void cancelExecute() 
	{
		cancel = true;
		
		List<PhymlSingleQueueModel> psqms = new ArrayList<PhymlSingleQueueModel>();
		
		psqms.addAll(runPsqms);
		psqms.addAll(preEndPsqms);
		
		for (PhymlSingleQueueModelIterator i = new PhymlSingleQueueModelIterator(psqms); i.hasNext(); )
		{
			PhymlSingleQueueModel psqm = i.next();
			
			psqm.cancelCompute();
			
			i.remove();
		}
	}

}
