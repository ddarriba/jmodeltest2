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
package es.uvigo.darwin.jmodeltest.observer;

import java.util.Observable;
import java.util.Observer;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.io.HtmlReporter;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public class ConsoleProgressObserver implements Observer {

	private TextOutputStream stream;
	/** Timer for calculate the elapsed time **/
	private long startTime;
	private ApplicationOptions options;

	public ConsoleProgressObserver(ApplicationOptions options) {
		this.options = options;
		this.startTime = System.currentTimeMillis();
		this.stream = ModelTest.getMainConsole();
	}

	@Override
	public void update(Observable o, Object arg) {

		if (arg != null) {
			ProgressInfo info = (ProgressInfo) arg;

			switch (info.getType()) {

			case ProgressInfo.BASE_TREE_INIT:
				stream.println(" ");
				stream.println("Estimating a BIONJ-JC tree ... ");
				break;

			case ProgressInfo.BASE_TREE_COMPUTED:
				stream.println("OK");
				stream.println(info.getModel().getName() + " tree: "
						+ info.getModel().getTreeString());
				break;

			case ProgressInfo.SINGLE_OPTIMIZATION_INIT:
				stream.println(" ");
				stream.println("Computing " + info.getModel().getName() + "...");
				break;

			case ProgressInfo.SINGLE_OPTIMIZATION_COMPLETED:
				stream.println(" ");
				stream.println("Maximum likelihod estimation for the "
						+ info.getModel().getName() + " model: "
						+ info.getModel().getLnL());
				if (options.userTopologyExists)
					stream.println("  User tree topology");

				if (options.userTopologyExists)
					stream.println("  User tree topology");
				else if (options.fixedTopology)
					stream.println("  BIONJ-JC tree topology");
				else if (options.optimizeMLTopology)
					stream.println("  ML optimized tree topology");
				else
					stream.println("  BIONJ tree topology");
				stream.print("  Computation time = " + info.getMessage());
				stream.println("  ("
						+ Utilities.calculateRuntime(startTime,
								System.currentTimeMillis()) + ")");
				break;

			case ProgressInfo.INTERRUPTED:
				stream.println(" ");
				stream.println("Computation of likelihood scores discontinued ...");
				break;

			case ProgressInfo.ERROR:
				stream.println(info.getMessage());
				break;

			case ProgressInfo.OPTIMIZATION_COMPLETED_OK:

				for (Model model : ModelTest.model) {
					model.print(ModelTest.getMainConsole());
					ModelTest.getMainConsole().println(" ");
				}
				
//				String baseTree = "";
//
//				// update gui status
//				if (!(options.fixedTopology || options.userTopologyExists))
//					baseTree = "(optimized trees)";
//				else
//					baseTree = "(fixed tree)";

				stream.println(" ");
				stream.println("Computation of likelihood scores completed. It took "
						+ Utilities.calculateRuntime(startTime,
								System.currentTimeMillis()) + ".");
				stream.println(" ");
				// continue

			case ProgressInfo.OPTIMIZATION_COMPLETED_INTERRUPTED:
				// dispose
				break;
			}
		}

	}

}
