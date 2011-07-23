package es.uvigo.darwin.jmodeltest.observer;

import java.util.Observable;
import java.util.Observer;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
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
