package es.uvigo.darwin.jmodeltest.exe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Observable;

import pal.tree.TreeParseException;
import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.io.TextInputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;
import es.uvigo.darwin.prottest.exe.ExternalExecutionManager;

public class PhymlSingleModel extends Observable implements Runnable {

	private int verbose = 0;
	private static final String CURRENT_DIRECTORY = System
			.getProperty("user.dir");

	public static final String PHYML_PATH = CURRENT_DIRECTORY + "/exe/phyml/";

	private String phymlStatFileName;
	private String phymlTreeFileName;

	private Model model;
	private long startTime;
	private long endTime;
	private String commandLine;
	private int index;
	private boolean justGetJCTree;
	private boolean interrupted = false;
	private ApplicationOptions options;
	private int numberOfThreads = -1;

	public Model getModel() {
		return model;
	}

	public PhymlSingleModel(Model model, int index, boolean justGetJCTree,
			ApplicationOptions options) {
		this.options = options;
		this.model = model;
		this.index = index;
		this.justGetJCTree = justGetJCTree;

		this.phymlStatFileName = options.getAlignmentFile().getAbsolutePath()
				+ RunPhyml.PHYML_STATS_SUFFIX + model.getName() + ".txt";
		this.phymlTreeFileName = options.getAlignmentFile().getAbsolutePath()
				+ RunPhyml.PHYML_TREE_SUFFIX + model.getName() + ".txt";
	}

	public PhymlSingleModel(Model model, int index, boolean justGetJCTree,
			ApplicationOptions options, int numberOfThreads) {
		this(model, index, justGetJCTree, options);
		this.numberOfThreads = numberOfThreads;
	}

	public boolean compute() {
		// run phyml
		notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_INIT, index, model,
				null);

		startTime = System.currentTimeMillis();

		writePhyml3CommandLine(model, justGetJCTree);
		executeCommandLine();
		if (!interrupted) {
			parsePhyml3Files(model);
		}

		endTime = System.currentTimeMillis();

		model.setComputationTime(endTime - startTime);

		// completed
		if (!interrupted) {
			notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_COMPLETED, index,
					model, Utilities.calculateRuntime(startTime, endTime));

		}
		return !interrupted;
	}

	@Override
	public void run() {
		compute();
	}

	/************************
	 * writePhym3lCommandLine ********************** * Builds up the command
	 * line for Phyml3 * * *
	 ***********************************************************************/

	private void writePhyml3CommandLine(Model currentModel,
			boolean justGetJCtree) {

		// input file
		commandLine = " -i " + options.getAlignmentFile().getAbsolutePath();

		// data type is nucleotide
		commandLine += " -d " + "nt";

		// number of data sets
		commandLine += " -n " + "1";

		// no bootrstrap or aLRT
		commandLine += " -b " + "0";

		// set execution id
		commandLine += " --run_id " + model.getName();

		// set custom model
		commandLine += " -m " + currentModel.getPartition();

		// optimize base frequencies if needed
		if (currentModel.ispF())
			commandLine += " -f m"; // changed from -f e DP200509
		else
			commandLine += " -f 0.25 0.25 0.25 0.25";

		// optimize pinvar if needed
		if (currentModel.ispI())
			commandLine += " -v " + "e";

		// optimize rate parameters
		// if (currentModel.pT || currentModel.pR)
		// commandLine += rateParameters;

		// optimize alpha if needed
		if (currentModel.ispG()) {
			commandLine += " -c " + options.numGammaCat;
			commandLine += " -a e";
		} else
			commandLine += " -c " + 1;

		// search strategy
		switch (options.treeSearchOperations) {
		case SPR:
			commandLine += " -s " + "SPR";
			break;
		case BEST:
			commandLine += " -s " + "BEST";
			break;
		default:
			commandLine += " -s " + "NNI";
		}

		// threaded version
		if (numberOfThreads > 0) {
			commandLine += " --num_threads " + numberOfThreads;
		}

		// do optimize topology?
		/*
		 * params=tlr: tree topology (t), branch length (l) and substitution
		 * rate parameters (r) are optimised. params = tlr or tl: optimize tree
		 * topology and branch lengths params = lr or l: tree topology fixed;
		 * optimize branch lengths; params = r or none: both tree topology and
		 * branch lengths are fixed.
		 */
		if (justGetJCtree) {
			commandLine += " -o " + "r"; // both tree topology and branch
											// lengths are fixed.
		}
		/*
		 * else if (ModelTest.userTreeExists) // use user tree for all models {
		 * commandLine += " -u " + userTreeFileName; commandLine += " -o " +
		 * "r"; // both tree topology and branch lengths are fixed. }
		 */
		// use a single tree for all models
		else if (options.userTopologyExists || options.fixedTopology) {
			commandLine += " -u " + options.getTreeFile().getAbsolutePath();
			commandLine += " -o " + "lr"; // tree topology fixed; optimize
											// branch lengths
		} else if (!options.optimizeMLTopology) // use BIONJ tree for
												// each model
		{
			commandLine += " -o " + "lr"; // tree topology fixed; optimize
											// branch lengths
		} else {
			commandLine += " -o " + "tlr"; // optimize tree topology and branch
											// lengthss
		} // use ML optimized tree for each model

	}

	/***************************
	 * executeCommandLine ************************ * Executes a set of command
	 * line in the system * * *
	 ***********************************************************************/

	private void executeCommandLine() {
		try {
			File dir = new File(PHYML_PATH);

			// to deal with spaces in path we need to fragment commandline (at
			// least it works in MacOS X)
			String[] executable = { PHYML_PATH + Utilities.getBinaryVersion() };
			String[] tokenizedCommandLine = commandLine.split(" ");
			String[] cmd = Utilities.specialConcatStringArrays(executable,
					tokenizedCommandLine);

			// get process and execute command line
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(cmd, null, dir);
			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(
					proc.getErrorStream(), "ERROR", System.err);
			// any output?
			FileOutputStream logFile = new FileOutputStream(
					options.getLogFile(), true);
			StreamGobbler outputGobbler = new StreamGobbler(
					proc.getInputStream(), "OUTPUT", logFile);
			ExternalExecutionManager.getInstance().addProcess(proc);

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			// any error???
			int exitVal = proc.waitFor();
			ExternalExecutionManager.getInstance().removeProcess(proc);

			if (verbose > 1)
				System.out.println("ExitValue: " + exitVal);

			// print command line to phmyl logfile
			PrintWriter printout = new PrintWriter(logFile);
			printout.println("Command line used for settings above = "
					+ commandLine);
			printout.flush();
			printout.close();

		} catch (InterruptedException e) {
			notifyObservers(ProgressInfo.INTERRUPTED, index, model, null);
			interrupted = true;
		} catch (Throwable t) {
			System.err.println(" ");
			System.err
					.println("ERROR: cannot run the Phyml command line for some reason. Check the phmyl log file.");
			System.err.print("Arguments: " + commandLine);
			System.err.println("Current phyml path: " + PHYML_PATH);
			System.err.println("Current phyml binary: "
					+ Utilities.getBinaryVersion());
			System.err.println(" ");
			System.exit(0);
		}

	}

	/***************************
	 * parsePhyml3Files ************************** * Reads contents of Phyml3
	 * output files and loads * models parameter estimates * * *
	 ***********************************************************************/

	private void parsePhyml3Files(Model currentModel) {
		String line;

		boolean showParsing = false;

		// Get model likelihood
		// TextInputStream phymlLkFile = new TextInputStream(phymlLkFileName);
		// currentModel.lnL = (-1.0) * phymlLkFile.readFloat();
		// phymlLkFile.close();

		// Get model likelihood and parameter estimates
		TextInputStream phymlStatFile = new TextInputStream(phymlStatFileName);
		try {
			while ((line = phymlStatFile.readLine()) != null) {
				if (line.length() > 0 && line.startsWith(". Log-likelihood")) {
					currentModel.setLnL((-1.0)
							* Double.parseDouble(Utilities.lastToken(line)));
					if (showParsing)
						System.err.println("Reading lnL = "
								+ currentModel.getLnL());
				} else if (line.length() > 0
						&& line.startsWith(". Discrete gamma model")) {
					if (Utilities.lastToken(line).equals("Yes")) {
						// currentModel.pG = true;
						line = phymlStatFile.readLine();
						currentModel.setNumGammaCat(Integer.parseInt(Utilities
								.lastToken(line)));
						if (showParsing)
							System.err.println("Reading numGammaCat = "
									+ currentModel.getNumGammaCat());
						line = phymlStatFile.readLine();
						currentModel.setShape(Double.parseDouble(Utilities
								.lastToken(line)));
						if (showParsing)
							System.err.println("Reading shape = "
									+ currentModel.getShape());
					}
				} else if (line.length() > 0
						&& line.startsWith(". Nucleotides frequencies")) {
					// currentModel.pF = true; ??
					line = phymlStatFile.readLine();
					while (line.trim().length() == 0)
						// get rid of any number of returns
						line = phymlStatFile.readLine();
					currentModel.setfA(Double.parseDouble(Utilities
							.lastToken(line)));
					line = phymlStatFile.readLine();
					currentModel.setfC(Double.parseDouble(Utilities
							.lastToken(line)));
					line = phymlStatFile.readLine();
					currentModel.setfG(Double.parseDouble(Utilities
							.lastToken(line)));
					line = phymlStatFile.readLine();
					currentModel.setfT(Double.parseDouble(Utilities
							.lastToken(line)));
					if (showParsing) {
						System.err.println("Reading fA = "
								+ currentModel.getfA());
						System.err.println("Reading fC = "
								+ currentModel.getfC());
						System.err.println("Reading fG = "
								+ currentModel.getfG());
						System.err.println("Reading fT = "
								+ currentModel.getfT());
					}
				} else if (line.length() > 0
						&& line.startsWith(". Proportion of invariant")) {
					// currentModel.pI = true;
					currentModel.setPinv(Double.parseDouble(Utilities
							.lastToken(line)));
					if (showParsing)
						System.err.println("Reading pinv = "
								+ currentModel.getPinv());
				}
				// with custom models phyml does not provide a ti/tv. We have to
				// calculate it from the rate parameters

				else if (line.length() > 0
						&& line.startsWith(". GTR relative rate parameters")) {
					line = phymlStatFile.readLine();
					while (line.trim().length() == 0)
						// get rid of any number of returns
						line = phymlStatFile.readLine();
					currentModel.setRa(Double.parseDouble(Utilities
							.lastToken(line)));
					line = phymlStatFile.readLine();
					currentModel.setRb(Double.parseDouble(Utilities
							.lastToken(line)));
					line = phymlStatFile.readLine();
					currentModel.setRc(Double.parseDouble(Utilities
							.lastToken(line)));
					line = phymlStatFile.readLine();
					currentModel.setRd(Double.parseDouble(Utilities
							.lastToken(line)));
					line = phymlStatFile.readLine();
					currentModel.setRe(Double.parseDouble(Utilities
							.lastToken(line)));
					line = phymlStatFile.readLine();
					currentModel.setRf(Double.parseDouble(Utilities
							.lastToken(line))); // for
												// latest
												// phyml3
												// feb08
					if (showParsing) {
						System.err.println("Reading Ra = "
								+ currentModel.getRa());
						System.err.println("Reading Rb = "
								+ currentModel.getRb());
						System.err.println("Reading Rc = "
								+ currentModel.getRc());
						System.err.println("Reading Rd = "
								+ currentModel.getRd());
						System.err.println("Reading Re = "
								+ currentModel.getRe());
						System.err.println("Reading Rf = "
								+ currentModel.getRf());
					}
					// with custom models phyml does not provide a ti/tv, so we
					// calculate it from the rate parameters
					// note this is kappa and we need to transform it to ti/tv
					if (currentModel.ispT()) {
						currentModel.setKappa(currentModel.getRb());
						currentModel
								.setTitv(currentModel.getKappa()
										* (currentModel.getfA()
												* currentModel.getfG() + currentModel
												.getfC() * currentModel.getfT())
										/ ((currentModel.getfA() + currentModel
												.getfG()) * (currentModel
												.getfC() + currentModel.getfT())));
					}
				}
			}
		} catch (NullPointerException e) {
			System.err.println("Error while parsing result data from "
					+ currentModel.getName());
		}
		phymlStatFile.close();

		try {
			// Get ML tree
			TextInputStream phymlTreeFile = new TextInputStream(
					phymlTreeFileName);
			String treestr = phymlTreeFile.readLine();
			currentModel.setTreeString(treestr);
			phymlTreeFile.close();
		} catch (TreeParseException e) {
			System.out.println(" ");
			System.out.println("ERROR: ML tree for " + model.getName()
					+ " is invalid.");
			System.out.println(" ");
			System.exit(-1);
		}

		Utilities.deleteFile(phymlStatFileName);
		Utilities.deleteFile(phymlTreeFileName);

	}

	private void notifyObservers(int type, int value, Model model,
			String message) {
		setChanged();
		notifyObservers(new ProgressInfo(type, value, model, message));
	}
}
