package es.uvigo.darwin.jmodeltest.exe;

import java.util.ArrayList;
import java.util.List;

import es.udc.gac.hpcmanager.webapp.worker.util.exception.NotAvailableResourceException;
import es.uvigo.darwin.prottest.util.exception.ProtTestInternalException;


/**
 * 
 * @author Jose Manuel Santorum, University of A Coruña
 *         jose.santorum@udc.es
 *
 */

public class PhymlQueueModel {

	List<PhymlSingleQueueModel> psqms = new ArrayList<PhymlSingleQueueModel>();
	
	public void add(PhymlSingleQueueModel psqm) 
	{
		psqms.add(psqm);
	}

	public void execute() 
	{
		for (PhymlSingleQueueModel psqm : psqms)
		{
			if (!psqm.compute())
				throw new ProtTestInternalException("Optimization error");	
		}		
	}
	
	public boolean continueExecute(Integer jobId) throws NotAvailableResourceException
	{
		PhymlSingleQueueModel psqmFinished = null;
		for (PhymlSingleQueueModel psqm : psqms)
		{
			if (psqm.getJobId().intValue() == jobId.intValue())
			{
				if (!psqm.continueCompute())
				{
					throw new ProtTestInternalException("Optimization error");
				}
				psqmFinished = psqm;
				break;
			}
		}
		
		if (psqmFinished != null)
		{
			psqms.remove(psqmFinished);
		}
		
		return (psqms.size() == 0);
	}

	public List<Integer> pendingJobs() 
	{
		List<Integer> jobIds = new ArrayList<Integer>();
		System.out.println("jobIds[0]: "+jobIds);
		for (PhymlSingleQueueModel psqm : psqms)
		{
			System.out.println("add: " + psqm.getJobId());
			jobIds.add(psqm.getJobId());
		}

		System.out.println("jobIds[1]: "+jobIds);
		return jobIds;
	}

}
