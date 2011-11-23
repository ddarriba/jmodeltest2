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
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;

public class PhymlParallelModel extends Observable implements Observer {

	    /** The runtime **/
	    private Runtime runtime = Runtime.getRuntime();
	    /** The size of parallel tasks **/
	    private int maxNumberOfTasks;
	    /** The list of model estimators **/
	    private List<PhymlSingleModel> estimatorList;
	    /** The pool of threads **/
	    private ExecutorService threadPool;
	    /** Collection of callable objects (tasks) **/
	    private Collection<Callable<Object>> c = new ArrayList<Callable<Object>>();

	    /**
	     * Instantiates a new PhymlParallelModel with no models to optimize. 
	     * The size of the thread pool is equal to the number of available cores in the machine.
	     * 
	     * @param alignment the common alignment of the models
	     */
	    public PhymlParallelModel() {

	        this(-1);

	    }

	    /**
	     * Instantiates a new PhymlParallelModel with no models to optimize and
	     * a fixed size of the thread pool
	     *
	     * @param availableThreads the size of the thread pool 
	     * @param alignment the common alignment of the models
	     */
	    public PhymlParallelModel(int availableThreads) {

	        if (availableThreads < 0) {
	            availableThreads = runtime.availableProcessors();
	        }
	        this.maxNumberOfTasks = availableThreads;
	        this.estimatorList = new ArrayList<PhymlSingleModel>(maxNumberOfTasks);
	        this.threadPool = Executors.newFixedThreadPool(maxNumberOfTasks);
	    }


	    /**
	     * Executes the model optimization
	     * 
	     * @param estimator the model estimator to execute
	     * 
	     * @return if succesfully added the task
	     */
	    public boolean execute(PhymlSingleModel estimator) {
	        estimator.addObserver(this);

	        boolean added = estimatorList.add(estimator);
	        c.add(Executors.callable(estimator));

			threadPool.execute(estimator);

	        return added;
	    }

	    protected void notifyObservers(int type, int value, Model model,
				String message) {
			setChanged();
			notifyObservers(new ProgressInfo(type, value, model, message));
		}

		@Override
		public void update(Observable o, Object arg) {
			setChanged();
			notifyObservers(arg);
		}

	    /**
	     * Checks if exist more tasks in the task queue
	     * 
	     * @return true, if exist more tasks to execute
	     */
	    public boolean hasMoreTasks() {
	        for (PhymlSingleModel estimator : estimatorList) {
	            if (estimator.getModel().getTree() == null) {
	                return true;
	            }
	        }
	        return false;
	    }
}
