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
import es.uvigo.darwin.jmodeltest.utilities.Utilities;
import es.uvigo.darwin.prottest.util.exception.ProtTestInternalException;

public class Distributor extends Observable implements Runnable {

	/** MPJ Tag for requesting a new model. */
	public static final int TAG_SEND_REQUEST = 1;

	/** MPJ Tag for sending a new model. */
	public static final int TAG_SEND_MODEL = 2;

	private List<Model> models;
	private RunPhymlMPJ caller;
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

	public Distributor(List<Model> models, RunPhymlMPJ caller, int mpjMe,
			int mpjSize) {
		this.mpjMe = mpjMe;
		this.mpjSize = mpjSize;
		this.models = models;
		this.caller = caller;
		this.itemsPerProc = new int[mpjSize];
		this.displs = new int[mpjSize];

		Collections.sort(this.models, new ModelComparator());
	}

	public void distribute(List<Model> models) throws InterruptedException {
		int index = 0;
		for (Model model : models) {
			// check root processor
			//
			// This strategy is an easy way to avoid the problem of
			// thread-safety in MPJ-Express. It works correctly, but
			// it also causes to introduce coupling between this class
			// and RunPhymlMPJ having to define two volatile attributes:
			// rootModelRequest and rootModel.
			if (caller.rootModelRequest || mpjSize == 1) {
				while (!caller.rootModelRequest) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						throw new ProtTestInternalException(
								"Thread interrupted");
					}
				}
				if (caller.rootModel != null) {
					// finalized model optimization
					notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_COMPLETED,
							index, caller.rootModel,
							Utilities.displayRuntime(caller.rootModel
									.getComputationTime()));
				}
				notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_INIT, index,
						model, null);

				caller.rootModel = model;
				caller.rootModelRequest = false;
				itemsPerProc[mpjMe]++;
			} else {
				Model[] computedModel = new Model[1];
				// getModel request
				Request modelRequest = MPI.COMM_WORLD.Irecv(computedModel, 0,
						1, MPI.OBJECT, MPI.ANY_SOURCE, TAG_SEND_REQUEST);
				// prepare model
				Model[] modelToSend = new Model[1];
				modelToSend[0] = model;
				// wait for request
				Status requestStatus = modelRequest.Wait();
				if (computedModel[0] != null) {
					// finalized model optimization
					int recvIndex = models.indexOf(computedModel[0]);
					notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_COMPLETED,
							index, computedModel[0],
							Utilities.displayRuntime(computedModel[0]
									.getComputationTime()));
					models.set(recvIndex, computedModel[0]);
				}
				// send model
				Request modelSend = MPI.COMM_WORLD.Isend(modelToSend, 0, 1,
						MPI.OBJECT, requestStatus.source, TAG_SEND_MODEL);
				notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_INIT, index,
						model, null);
				// update structures
				itemsPerProc[requestStatus.source]++;
				// wait for send
				modelSend.Wait();
			}
			index++;
		}
		displs[0] = 0;
		for (int i = 1; i < mpjSize; i++)
			displs[i] = displs[i - 1] + itemsPerProc[i - 1];

		// finalize
		for (int i = 1; i < mpjSize; i++) {
			Model[] computedModel = new Model[1];
			// getModel request
			Request modelRequest = MPI.COMM_WORLD.Irecv(computedModel, 0, 1,
					MPI.OBJECT, MPI.ANY_SOURCE, TAG_SEND_REQUEST);
			Model[] modelToSend = { null };
			// wait for request
			Status requestStatus = modelRequest.Wait();
			if (computedModel[0] != null) {
				int recvIndex = models.indexOf(computedModel[0]);
				notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_COMPLETED,
						index, computedModel[0],
						Utilities.displayRuntime(computedModel[0]
								.getComputationTime()));
				models.set(recvIndex, computedModel[0]);
			}
			// send null model
			Request modelSend = MPI.COMM_WORLD.Isend(modelToSend, 0, 1,
					MPI.OBJECT, requestStatus.source, TAG_SEND_MODEL);

			modelSend.Wait();
		}
		// check root
		while (!caller.rootModelRequest) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				throw e;
			}
		}
		if (caller.rootModel != null) {
			notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_COMPLETED, index,
					caller.rootModel, Utilities.displayRuntime(caller.rootModel
							.getComputationTime()));
		}
		caller.rootModel = null;
		caller.rootModelRequest = false;
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
			distribute(models);
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

	private void notifyObservers(int type, int value, Model model,
			String message) {
		setChanged();
		notifyObservers(new ProgressInfo(type, value, model, message));
	}
}
