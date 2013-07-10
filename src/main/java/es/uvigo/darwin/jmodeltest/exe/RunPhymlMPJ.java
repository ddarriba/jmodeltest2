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
import es.uvigo.darwin.prottest.util.exception.ProtTestInternalException;

/** 
 * RunPhymlMPJ.java
 *
 * Description:		Makes phyml calculate likelihood scores for competing models
 * @author			Diego Darriba, University of Vigo / University of A Coruna, Spain
 * 					ddarriba@udc.es
 * @author			David Posada, University of Vigo, Spain  
 *					dposada@uvigo.es | darwin.uvigo.es
 * @version			2.0.2 (Feb 2012)
 */
public class RunPhymlMPJ extends RunPhyml {


	private List<Model> myModels;

	// Synchronization package variables.
	// Thread safe under current operation. Keep in mind.
	volatile Model rootModel = null;
	volatile boolean rootModelRequest = false;

	public RunPhymlMPJ(Observer progress, ApplicationOptions options,
			Model[] models) {
		super(progress, options, models);
		// this.deleteObserver(progress);
		myModels = new ArrayList<Model>();

	}

	public void distribute() {

		List<Model> modelList = Arrays.asList(models);
		Distributor distributor = new Distributor(modelList, this,
				ModelTest.MPJ_ME, ModelTest.MPJ_SIZE);
		distributor.addObserver(progress);
		Thread distributorThread = new Thread(distributor);
		distributorThread.start();
		request();

		for (Model model : ModelTest.getCandidateModels()) {
			model.update(modelList.get(modelList.indexOf(model)));
		}

		notifyObservers(ProgressInfo.OPTIMIZATION_COMPLETED_OK, models.length,
				null, null);
	}

	public void request() {

		List<PhymlSingleModel> phymlEstimatorList = new ArrayList<PhymlSingleModel>();

		Model[] lastComputedModel = new Model[1];
		while (true) {
			// send request to root
			Model[] modelToReceive = null;
			Model model = null;
			if (ModelTest.MPJ_ME > 0) {
				Request modelRequest = MPI.COMM_WORLD.Isend(lastComputedModel,
						0, 1, MPI.OBJECT, 0, Distributor.TAG_SEND_REQUEST);
				// prepare reception
				modelToReceive = new Model[1];
				// wait for request
				modelRequest.Wait();
				// receive model
				Request modelReceive = MPI.COMM_WORLD.Irecv(modelToReceive, 0,
						1, MPI.OBJECT, 0, Distributor.TAG_SEND_MODEL);
				modelReceive.Wait();
				model = modelToReceive[0];
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
						throw new ProtTestInternalException(
								"Thread interrupted");
					}
				}
				model = rootModel;
			}
			if (model == null)
				break;
			else {
				// compute
				myModels.add(model);
				PhymlSingleModel runenv = new PhymlSingleModel(model, 0, false,
						false, options);
				runenv.addObserver(this);

				if (!runenv.compute())
					throw new ProtTestInternalException("Optimization error");

				phymlEstimatorList.add(runenv);
				lastComputedModel[0] = runenv.getModel();
			}
		}

		// endTime = System.currentTimeMillis();
	}

	protected Object doPhyml() {
		return null;
	} // doPhyml

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
			System.out.println("computing likelihood scores for " + models.length
					+ " models with Phyml " + PHYML_VERSION);
		}

		// sincronize ApplicationOptions from root
		ApplicationOptions[] optionsBCast = new ApplicationOptions[1];
		optionsBCast[0] = options;
		MPI.COMM_WORLD.Bcast(optionsBCast, 0, 1, MPI.OBJECT, 0);
		this.options = optionsBCast[0];
		ApplicationOptions.setInstance(this.options);

		if (ModelTest.MPJ_ME == 0) {
			notifyObservers(ProgressInfo.OPTIMIZATION_INIT, 0,
					models[0], null);
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
		// Ignore runtime messages
		// setChanged();
		// notifyObservers(arg);
	}
} // class RunPhyml

