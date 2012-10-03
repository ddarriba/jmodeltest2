/*
Copyright (C) 2011  Diego Darriba, David Posada

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package es.uvigo.darwin.jmodeltest.exe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;

public class RunPhymlThread extends RunPhyml 
{

	private ExecutorService threadPool;

	public RunPhymlThread(Observer progress, ModelTest modelTest, Model[] models) 
	{
		super(progress, modelTest, models);

		this.threadPool = Executors.newFixedThreadPool(options.getNumberOfThreads());
	}

	/*******************************
	 * doPhyml ****************************** * run the phyml calculations on a
	 * separate thread * * *
	 ***********************************************************************/

	protected Object doPhyml() 
	{
		// compute likelihood scores for all models
		// System.out.print("computing likelihood scores for "
		// + models.length + " models with Phyml " + PHYML_VERSION);

		notifyObservers(ProgressInfo.OPTIMIZATION_INIT, 0, models[0], null);
		
		Collection<Callable<Object>> c = new ArrayList<Callable<Object>>();

		int current = 0;
		for (Model model : models) 
		{
			PhymlSingleModel psm = new PhymlSingleModel(model, current, false, options);
			psm.addObserver(this);
			c.add(Executors.callable(psm));

			current++;
		}

		boolean errorsFound = false;

		Collection<Future<Object>> futures = null;
		try 
		{
			futures = threadPool.invokeAll(c);
		}
		catch (InterruptedException e) 
		{
			notifyObservers(ProgressInfo.INTERRUPTED, 0, null, null);
		}

		if (futures != null) 
		{
			for (Future<Object> f : futures) 
			{
				try 
				{
					f.get();
				}
				catch (InterruptedException ex) 
				{
					errorsFound = true;
					notifyObservers(ProgressInfo.INTERRUPTED, 0, null, null);
					ex.printStackTrace();
					return "Interrupted";
				}
				catch (ExecutionException ex) 
				{
					// Internal exception while computing model.
					// Let's continue with errors
					errorsFound = true;
					ex.printStackTrace();
					return "Interrupted";
				}
			}
		}

		if (!errorsFound) 
		{
			notifyObservers(ProgressInfo.OPTIMIZATION_COMPLETED_OK, models.length, null, null);
		}
		else 
		{
			notifyObservers(ProgressInfo.OPTIMIZATION_COMPLETED_INTERRUPTED, models.length,	null, null);
		}

		return "All Done";
	} // doPhyml

	public void interruptThread() 
	{
		super.interruptThread();
		ProcessManager.getInstance().killAll();
		threadPool.shutdownNow();// shutdown();
	}

	@Override
	public void update(Observable o, Object arg) 
	{
		if (arg != null) 
		{
			ProgressInfo info = (ProgressInfo) arg;
			if (info.getType() == ProgressInfo.ERROR) 
			{
				interruptThread();
			}
		}
		super.update(o, arg);
	}
}
