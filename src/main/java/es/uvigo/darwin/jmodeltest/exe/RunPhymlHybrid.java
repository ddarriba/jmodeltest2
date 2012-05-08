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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import mpi.MPI;
import mpi.Request;
import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;

public class RunPhymlHybrid extends RunPhyml {

	private List<Model> myModels;

	// Synchronization package variables.
	// Thread safe under current operation. Keep in mind.
	volatile Model rootModel = null;
	volatile boolean rootModelRequest = false;

	int mpjMe, mpjSize;
	int maxPEs;
	/** The number of available PEs. */
	int availablePEs;
	private PhymlParallelModel pme;
	private Model[] computedModels;
	private MultipleDistributor distributor;
	int[] itemsPerProc;
	int[] displs;

	public RunPhymlHybrid(Observer progress, ApplicationOptions options,
			Model[] models) {
		super(progress, options, models);
		// this.deleteObserver(progress);
		myModels = new ArrayList<Model>();

	}

	public RunPhymlHybrid(int mpjMe, int mpjSize, Observer progress,
			ApplicationOptions options, Model[] models, int numberOfThreads) {
		super(progress, options, models);
		// this.deleteObserver(progress);
		myModels = new ArrayList<Model>();

		this.mpjMe = mpjMe;
		this.mpjSize = mpjSize;

		itemsPerProc = new int[mpjSize];
		displs = new int[mpjSize];

		maxPEs = numberOfThreads;
		availablePEs = maxPEs;
		pme = new PhymlParallelModel(maxPEs);
		pme.addObserver(this);

	}

	public void distribute() {

		List<Model> modelList = Arrays.asList(models);
		distributor = new MultipleDistributor(modelList, this,
				ModelTest.MPJ_ME, ModelTest.MPJ_SIZE);
		distributor.addObserver(progress);
		
		notifyObservers(ProgressInfo.OPTIMIZATION_INIT, 0,
				models[0], null);
		
		Thread distributorThread = new Thread(distributor);
		distributorThread.start();
		request();
		
		modelList = Arrays.asList(computedModels);
		for (Model model : models) {
			model.update(modelList.get(modelList.indexOf(model)));
		}

		notifyObservers(ProgressInfo.OPTIMIZATION_COMPLETED_OK, models.length,
				null, null);

	}

	public void request() {

//		List<PhymlSingleModel> phymlEstimatorList = new ArrayList<PhymlSingleModel>();

		Model[] lastComputedModel = new Model[1];
		while (true) {
			// send request to root
			Model[] modelToReceive = null;
			Model model = null;
			if (ModelTest.MPJ_ME > 0) {
				int[] sendMessage = { availablePEs, maxPEs };
				Request modelRequest = MPI.COMM_WORLD.Isend(sendMessage, 0, 2,
						MPI.INT, 0, MultipleDistributor.TAG_SEND_REQUEST);
				// prepare reception
				modelToReceive = new Model[1];
				boolean[] notification = new boolean[1];
				// wait for request
				modelRequest.Wait();

				Request notifyRecv = MPI.COMM_WORLD.Irecv(notification, 0, 1,
						MPI.BOOLEAN, 0,
						MultipleDistributor.TAG_EXIST_MORE_MODELS);
				notifyRecv.Wait();

				if (notification[0]) {
					// receive model
					Request modelReceive = MPI.COMM_WORLD.Irecv(modelToReceive,
							0, 1, MPI.OBJECT, 0,
							MultipleDistributor.TAG_SEND_MODEL);
					modelReceive.Wait();
					model = modelToReceive[0];
				} else {
					break;
				}
			} else {
				// This strategy is an easy way to avoid the problem of
				// thread-safety in MPJ-Express. It works correctly, but
				// it also causes to introduce coupling between this class
				// and Distributor having to define two volatile attributes:
				// rootModelRequest and rootModel.
				rootModelRequest = true;
				while (rootModelRequest) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						throw new RuntimeException("Thread interrupted");
					}
				}
				model = rootModel;
				if (model == null)
					break;
			}
			if (model != null) {
				// compute
				myModels.add(model);
				availablePEs -= MultipleDistributor.getPEs(model, maxPEs);
				PhymlSingleModel runenv = new PhymlSingleModel(model, 0, false,
						options, MultipleDistributor.getPEs(model, maxPEs));
				pme.execute(runenv);
				while (availablePEs <= 0) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						throw new RuntimeException("Thread interrupted");
					}
				}

				/*
				// runenv.addObserver(this);
				if (!runenv.compute())
					throw new RuntimeException("Optimization error");

				phymlEstimatorList.add(runenv);
				*/
				lastComputedModel[0] = runenv.getModel();
			}
		}

		// endTime = System.currentTimeMillis();

		while (pme.hasMoreTasks()) {
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				throw new RuntimeException("Thread interrupted");
			}
		}

		if (mpjMe > 0) {
			gather();
		} else {
			computedModels = gather();
		}
		
	}

	protected Object doPhyml() {
		return null;
	} // doPhyml

	/**
	 * Gathers the models of all processors into the root one. This method
	 * should be called by every processor after computing likelihood value of
	 * whole model set.
	 * 
	 * This method will return an empty array of models for every non-root
	 * processor
	 * 
	 * @return the array of gathered models
	 */
	private Model[] gather() {

		int numberOfModels = models != null?models.length:1;
		Model[] allModels = new Model[numberOfModels];
		
		if (distributor != null) {
			itemsPerProc = distributor.getItemsPerProc();
			displs = distributor.getDispls();
		}

		MPI.COMM_WORLD.Bcast(itemsPerProc, 0, mpjSize, MPI.INT, 0);
		
		// gathering optimized models
		MPI.COMM_WORLD.Gatherv(myModels.toArray(new Model[0]), 0,
				myModels.size(), MPI.OBJECT, allModels, 0, itemsPerProc,
				displs, MPI.OBJECT, 0);

		return allModels;
	}

	public void execute() {

		if (ModelTest.MPJ_ME == 0) {
			printSettings(ModelTest.getMainConsole());

			// TODO: Send topology to each processor
			// estimate a NJ-JC tree if needed
			if (options.fixedTopology) {
				notifyObservers(ProgressInfo.BASE_TREE_INIT, 0, models[0], null);

				PhymlSingleModel jcModel = new PhymlSingleModel(models[0], 0,
						true, false, options);
				jcModel.run();

				// create JCtree file
				TextOutputStream JCtreeFile = new TextOutputStream(options
						.getTreeFile().getAbsolutePath(), false);
				JCtreeFile.print(models[0].getTreeString() + "\n");
				JCtreeFile.close();

				options.setUserTree(models[0].getTreeString());

				notifyObservers(ProgressInfo.BASE_TREE_COMPUTED, 0, models[0],
						null);

			}

			// compute likelihood scores for all models
			System.out.println("computing likelihood scores for "
					+ models.length + " models with Phyml " + PHYML_VERSION);
		}

		// sincronize ApplicationOptions from root
		ApplicationOptions[] optionsBCast = new ApplicationOptions[1];
		optionsBCast[0] = options;
		MPI.COMM_WORLD.Bcast(optionsBCast, 0, 1, MPI.OBJECT, 0);
		this.options = optionsBCast[0];
		ApplicationOptions.setInstance(this.options);

		if (ModelTest.MPJ_ME == 0) {
			distribute();
		} else {
			try {
				this.options.buildWorkFiles();
			} catch (IOException e) {
				e.printStackTrace();
			}

			request();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg != null) {
			ProgressInfo info = (ProgressInfo) arg;
			if (info.getType() == ProgressInfo.SINGLE_OPTIMIZATION_COMPLETED) {
				availablePEs += MultipleDistributor.getPEs(info.getModel(),
						maxPEs);
			}
		}
		// Ignore runtime messages
		setChanged();
		notifyObservers(arg);
	}
} // class RunPhyml

