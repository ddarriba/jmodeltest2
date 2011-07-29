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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;

import javax.management.RuntimeErrorException;

import mpi.MPI;
import mpi.Request;
import mpi.Status;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;

public class MultipleDistributor extends Observable implements Runnable {

	/** MPJ Tag for requesting a new model. */
	public static final int TAG_SEND_REQUEST = 1;

	/** MPJ Tag for sending a new model. */
	public static final int TAG_SEND_MODEL = 2;
	
	public static final int TAG_EXIST_MORE_MODELS = 3;
	
	private List<Model> modelsToSend;
	private RunPhymlHibrid caller;
	/** MPJ Rank of the processor. */
	private int mpjMe;

	/** MPJ Size of the communicator. */
	private int mpjSize;

	/**
	 * The number of models per processor. It will be necessary by the root
	 * process to do the non-uniform gathering
	 */
	private int[] itemsPerProc;

	/**
	 * The array of displacements after the distribution. It will be necessary
	 * by the root process to do the non-uniform gathering
	 */
	private int[] displs;

	public MultipleDistributor(List<Model> models, RunPhymlHibrid caller, int mpjMe,
			int mpjSize) {
		this.mpjMe = mpjMe;
		this.mpjSize = mpjSize;
		this.modelsToSend = new ArrayList<Model>(models);
		this.caller = caller;
		this.itemsPerProc = new int[mpjSize];
		this.displs = new int[mpjSize];
		
		Collections.sort(this.modelsToSend, new ModelComparator());
	}

	public void distribute(List<Model> models) throws InterruptedException {
		 itemsPerProc = new int[mpjSize];
	        Status requestStatus = null;
	        displs = new int[mpjSize];
	        int[] freePEs = new int[1];
	        boolean sended = true;

	        Request modelRequest = null;
	        while (!modelsToSend.isEmpty()) {
	            // check root processor
	            //
	            // This strategy is an easy way to avoid the problem of thread-safety
	            // in MPJ-Express. It works correctly, but it also causes to introduce
	            // coupling between this class and ImprovedDynamicDistributionStrategy,
	            // having to define two public attributes: rootModelRequest and rootModel.
	            //
	            if (caller.rootModelRequest && caller.availablePEs > 0) {
//					if (caller.rootModel != null) {
//						caller.setCheckpoint(modelSet);
//					}
	                Model rootModel = getNextModel(caller.availablePEs, caller.maxPEs);
	                if (rootModel != null) {
	                    caller.rootModel = rootModel;
	                    caller.rootModelRequest = false;
	                    itemsPerProc[mpjMe]++;
	                }
	            } else {
	                // getModel request
	                if (sended) {
	                    modelRequest = MPI.COMM_WORLD.Irecv(freePEs, 0, 1, MPI.INT, MPI.ANY_SOURCE, TAG_SEND_REQUEST);
	                    // wait for request
	                    sended = false;
	                }
	                requestStatus = modelRequest.Test();
	                if (requestStatus != null) {
	                    Request notifySend = MPI.COMM_WORLD.Isend(new boolean[]{true}, 0, 1, MPI.BOOLEAN, requestStatus.source, TAG_EXIST_MORE_MODELS);

	                    // prepare model
	                    Model[] modelToSend = new Model[1];

	                    notifySend.Wait();

	                    modelToSend[0] = getNextModel(freePEs[0], caller.maxPEs);
	                    Request modelSend = MPI.COMM_WORLD.Isend(modelToSend, 0, 1, MPI.OBJECT, requestStatus.source, TAG_SEND_MODEL);

	                    if (modelToSend[0] != null) {
	                        // update structures
	                        itemsPerProc[requestStatus.source]++;
	                    }

	                    // wait for send
	                    modelSend.Wait();
	                    sended = true;

	                    requestStatus = null;

	                }
	                try {
	                    Thread.sleep(200);
	                } catch (InterruptedException e) {
	                	e.printStackTrace();
	                    //throw new ProtTestInternalException("Thread interrupted");
	                }

	            }
	        }
	        
	        displs[0] = 0;
	        for (int i = 1; i < mpjSize; i++) {
	            displs[i] = displs[i - 1] + itemsPerProc[i - 1];
	        }

	        // finalize
	        // check root
	        while (!caller.rootModelRequest) {
	            try {
	                Thread.sleep(200);
	            } catch (InterruptedException e) {
	            	e.printStackTrace();
	                //throw new ProtTestInternalException("Thread interrupted");
	            }
	        }
	        caller.rootModel = null;
	        caller.rootModelRequest = false;
	        
	        for (int i = 1; i < mpjSize; i++) {
	            // getModel request
	            modelRequest = MPI.COMM_WORLD.Irecv(freePEs, 0, 1, MPI.INT, MPI.ANY_SOURCE, TAG_SEND_REQUEST);
	            // wait for request
	            requestStatus = modelRequest.Wait();
	            // send null model
	            Request notifySend = MPI.COMM_WORLD.Isend(new boolean[]{false}, 0, 1, MPI.BOOLEAN, requestStatus.source, TAG_EXIST_MORE_MODELS);
	            notifySend.Wait();

	        }
	}

	public int[] getItemsPerProc() {
		return itemsPerProc;
	}

	public int[] getDispls() {
		return displs;
	}

	@Override
	public void run() {
		try {
			distribute(modelsToSend);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeErrorException(new Error(e));
		}
	}

	private class ModelComparator implements Comparator<Model> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Model model1, Model model2) {
			int value = 0;
			if (model1 != null && model2 != null)
				value = getWeight(model2) - getWeight(model1);
			return value;
		}

		private int getWeight(Model model) {
			int weight = 0;
			if (model.ispG())
				weight += 4;
			if (model.ispI())
				weight += 2;
			if (model.ispF())
				weight += 1;
			return weight;
		}
	}

	private Model getNextModel(int numPEs, int maxAvailableThreads) {
        Model nextModel = null;
        for (Model model : modelsToSend) {
            if (getPEs(model, maxAvailableThreads) <= numPEs) {
                nextModel = model;
                break;
            }
        }
        modelsToSend.remove(nextModel);
        return nextModel;
    }
	
	public static int getPEs(Model model, int maxAvailableThreads) {
        int numberOfThreads;
        if (model.ispG()) {
            numberOfThreads = 4;
        } else if (model.ispI()) {
            numberOfThreads = 2;
        } else {
            numberOfThreads = 1;
        }
        return Math.min(maxAvailableThreads, numberOfThreads);
    }
	
	@SuppressWarnings("unused")
	private void notifyObservers(int type, int value, Model model,
			String message) {
		setChanged();
		notifyObservers(new ProgressInfo(type, value, model, message));
	}
}
