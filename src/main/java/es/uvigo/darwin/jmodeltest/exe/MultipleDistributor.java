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
import java.util.Enumeration;
import java.util.List;
import java.util.Observable;

import javax.management.RuntimeErrorException;

import mpi.MPI;
import mpi.Request;
import mpi.Status;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.ModelTestConfiguration;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.model.ModelComparator;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;

public class MultipleDistributor extends Observable implements Runnable {

	public static final int BIG = 999;
	public static final int DEFAULT_PROCESSORS_IG = 4;
	public static final int DEFAULT_PROCESSORS_I = 2;
	public static final int DEFAULT_PROCESSORS_UNIFORM = 1;
	public static int PROCESSORS_IG;
	public static int PROCESSORS_I;
	public static int PROCESSORS_UNIFORM;

	/** MPJ Tag for requesting a new model. */
	public static final int TAG_SEND_REQUEST = 1;

	/** MPJ Tag for sending a new model. */
	public static final int TAG_SEND_MODEL = 2;

	public static final int TAG_EXIST_MORE_MODELS = 3;

	private List<Model> modelsToSend;
	private RunPhymlHybrid caller;

	private static boolean assumeHyperThreading;
	private static boolean homogeneousDistribution;

	/** MPJ Rank of the processor. */
	private int mpjMe;

	/** MPJ Size of the communicator. */
	private int mpjSize;

	private static int numberOfHosts;
	private static int totalNumberOfThreads = 0;
	private static int minProcs = BIG;
	private static int maxProcs = 0;
	private static float avgProcs;

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

	static {
		String gammaThreadsStr = ModelTestConfiguration
				.getProperty(ModelTestConfiguration.G_THREADS);
		String invThreadsStr = ModelTestConfiguration
				.getProperty(ModelTestConfiguration.I_THREADS);
		String uniformThreadsStr = ModelTestConfiguration
				.getProperty(ModelTestConfiguration.U_THREADS);
		try {
			PROCESSORS_IG = Integer
					.parseInt(gammaThreadsStr != null ? gammaThreadsStr
							: String.valueOf(DEFAULT_PROCESSORS_IG));
			PROCESSORS_I = Integer
					.parseInt(invThreadsStr != null ? invThreadsStr : String
							.valueOf(DEFAULT_PROCESSORS_I));
			PROCESSORS_UNIFORM = Integer
					.parseInt(uniformThreadsStr != null ? uniformThreadsStr
							: String.valueOf(DEFAULT_PROCESSORS_UNIFORM));
		} catch (NumberFormatException e) {
			PROCESSORS_IG = DEFAULT_PROCESSORS_IG;
			PROCESSORS_I = DEFAULT_PROCESSORS_I;
			PROCESSORS_UNIFORM = DEFAULT_PROCESSORS_UNIFORM;
		}
	}

	public MultipleDistributor(List<Model> models, RunPhymlHybrid caller,
			int mpjMe, int mpjSize) {
		this.mpjMe = mpjMe;
		this.mpjSize = mpjSize;
		this.modelsToSend = new ArrayList<Model>(models);
		this.caller = caller;
		this.itemsPerProc = new int[mpjSize];
		this.displs = new int[mpjSize];

		if (ModelTest.HOSTS_TABLE != null) {
			numberOfHosts = ModelTest.HOSTS_TABLE.size();
			Enumeration<Integer> procsPerHost = ModelTest.HOSTS_TABLE
					.elements();
			while (procsPerHost.hasMoreElements()) {
				int procs = procsPerHost.nextElement();
				totalNumberOfThreads += procs;
				if (procs < minProcs)
					minProcs = procs;
				if (procs > maxProcs)
					maxProcs = procs;
			}
			homogeneousDistribution = (minProcs == maxProcs);
			assumeHyperThreading = (maxProcs > 8);
			avgProcs = totalNumberOfThreads / numberOfHosts;
		} else {
			homogeneousDistribution = true;
			assumeHyperThreading = false;
			avgProcs = 1;
		}

		Collections.sort(this.modelsToSend, new ModelComparator());
	}

	public void distribute(List<Model> models) throws InterruptedException {
		itemsPerProc = new int[mpjSize];
		Status requestStatus = null;
		displs = new int[mpjSize];
		int[] freePEs = new int[2];
		boolean sended = true;

		Request modelRequest = null;
		while (!modelsToSend.isEmpty()) {
			// check root processor
			//
			// This strategy is an easy way to avoid the problem of
			// thread-safety
			// in MPJ-Express. It works correctly, but it also causes to
			// introduce
			// coupling between this class and
			// ImprovedDynamicDistributionStrategy,
			// having to define two public attributes: rootModelRequest and
			// rootModel.
			//
			if (caller.rootModelRequest && caller.availablePEs > 0) {
				// if (caller.rootModel != null) {
				// caller.setCheckpoint(modelSet);
				// }
				Model rootModel = getNextModel(caller.availablePEs,
						caller.maxPEs);
				if (rootModel != null) {
					caller.rootModel = rootModel;
					caller.rootModelRequest = false;
					itemsPerProc[mpjMe]++;
				}
			} else {
				// getModel request
				if (sended) {
					modelRequest = MPI.COMM_WORLD.Irecv(freePEs, 0, 2, MPI.INT,
							MPI.ANY_SOURCE, TAG_SEND_REQUEST);
					// wait for request
					sended = false;
				}
				requestStatus = modelRequest.Test();
				if (requestStatus != null) {
					Request notifySend = MPI.COMM_WORLD.Isend(
							new boolean[] { true }, 0, 1, MPI.BOOLEAN,
							requestStatus.source, TAG_EXIST_MORE_MODELS);

					// prepare model
					Model[] modelToSend = new Model[1];

					notifySend.Wait();

					modelToSend[0] = getNextModel(freePEs[0], freePEs[1]);
					Request modelSend = MPI.COMM_WORLD.Isend(modelToSend, 0, 1,
							MPI.OBJECT, requestStatus.source, TAG_SEND_MODEL);

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
					// throw new
					// ProtTestInternalException("Thread interrupted");
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
				// throw new ProtTestInternalException("Thread interrupted");
			}
		}
		caller.rootModel = null;
		caller.rootModelRequest = false;

		for (int i = 1; i < mpjSize; i++) {
			// getModel request
			modelRequest = MPI.COMM_WORLD.Irecv(freePEs, 0, 2, MPI.INT,
					MPI.ANY_SOURCE, TAG_SEND_REQUEST);
			// wait for request
			requestStatus = modelRequest.Wait();
			// send null model
			Request notifySend = MPI.COMM_WORLD.Isend(new boolean[] { false },
					0, 1, MPI.BOOLEAN, requestStatus.source,
					TAG_EXIST_MORE_MODELS);
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

	private Model getNextModel(int numPEs, int maxAvailableThreads) {
		Model nextModel = null;
		for (Model model : modelsToSend) {
			if (getPEs(model, maxAvailableThreads) <= numPEs) {
				// try to exclude +G models
				if (homogeneousDistribution || maxAvailableThreads >= avgProcs) {
					nextModel = model;
					break;
				}
			}
		}
		if (nextModel == null && !homogeneousDistribution
				&& maxAvailableThreads < avgProcs && !modelsToSend.isEmpty()) {
			// find another one
			for (Model model : modelsToSend) {
				if (getPEs(model, maxAvailableThreads) <= numPEs) {
					nextModel = model;
					break;
				}
			}
		}
		modelsToSend.remove(nextModel);
		return nextModel;
	}

	public static int getPEs(Model model, int maxAvailableThreads) {
		int numberOfThreads;
		if (model.ispG()) {
			numberOfThreads = PROCESSORS_IG;
		} else if (model.ispI()) {
			numberOfThreads = PROCESSORS_I;
		} else {
			numberOfThreads = PROCESSORS_UNIFORM;
		}
		if (assumeHyperThreading && maxAvailableThreads > 8) {
			numberOfThreads *= 2;
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
