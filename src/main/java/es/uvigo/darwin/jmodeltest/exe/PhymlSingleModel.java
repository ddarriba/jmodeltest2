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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Observable;

import javax.sql.rowset.spi.SyncResolver;

import pal.tree.TreeParseException;
import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.ModelTestConfiguration;
import es.uvigo.darwin.jmodeltest.io.TextInputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public class PhymlSingleModel extends Observable implements Runnable {

	private int verbose = 0;

	private String phymlStatFileName;
	private String phymlTreeFileName;

	private Model model;
	private long startTime, endTime;
	private String commandLine;
	private int index;
	private boolean justGetJCTree = false;
	private boolean ignoreGaps = false;
	private boolean interrupted = false;
	private ApplicationOptions options;
	private int numberOfThreads = -1;
	
	public Model getModel() {
		return model;
	}

	public PhymlSingleModel(Model model, int index, boolean justGetJCTree,
			boolean ignoreGaps, ApplicationOptions options) {
		this.options = options;
		this.model = model;
		this.index = index;
		this.justGetJCTree = justGetJCTree;
		this.ignoreGaps = ignoreGaps;
		
		this.phymlStatFileName = options.getAlignmentFile().getAbsolutePath()
				+ RunPhyml.PHYML_STATS_SUFFIX + model.getName() + ".txt";
		this.phymlTreeFileName = options.getAlignmentFile().getAbsolutePath()
				+ RunPhyml.PHYML_TREE_SUFFIX + model.getName() + ".txt";
	}

	public PhymlSingleModel(Model model, int index, boolean justGetJCTree,
			ApplicationOptions options, int numberOfThreads) {
		this(model, index, justGetJCTree, false, options);
		this.numberOfThreads = numberOfThreads;
	}

	public boolean compute() {
		notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_INIT, index,
				model, null);
		if (model.getLnL() < 1e-5 || ignoreGaps) {
			// run phyml
			startTime = System.currentTimeMillis();

			commandLine = writePhyml3CommandLine(model, justGetJCTree, options,
					ignoreGaps, numberOfThreads);
			executeCommandLine();
			
			if (!interrupted) {
				parsePhyml3Files(model);
			}

			endTime = System.currentTimeMillis();

			model.setComputationTime(endTime - startTime);
		}
		// completed
		if (!interrupted) {
			int value = 0;
			if (ignoreGaps) {
				value = ProgressInfo.VALUE_IGAPS_OPTIMIZATION;
			} else {
				value = ProgressInfo.VALUE_REGULAR_OPTIMIZATION;
			}
			notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_COMPLETED,
					value, model,
					Utilities.calculateRuntime(startTime, endTime));

		}
		return !interrupted;
	}

	@Override
	public void run() {
		compute();
	}

	/************************
	 * writePhym3lCommandLine 
	 ************************
	 * Builds up the command
	 * line for Phyml3
	 ************************/
	public static String writePhyml3CommandLine(Model currentModel,
			boolean justGetJCtree, ApplicationOptions options,
			boolean ignoreGaps, int numberOfThreads) {

		StringBuilder sb = new StringBuilder();

		// input file
		sb.append(" -i ").append(options.getAlignmentFile().getAbsolutePath());

		// data type is nucleotide
		sb.append(" -d nt");

		// number of data sets
		sb.append(" -n 1");

		// no bootrstrap or aLRT
		sb.append(" -b 0");

		if (ignoreGaps) {
			sb.append(" --no_gap");
		}
		// set execution id
		sb.append(" --run_id ").append(currentModel.getName());

		// set custom model
		sb.append(" -m ").append(currentModel.getPartition());

		// optimize base frequencies if needed
		if (currentModel.ispF())
			sb.append(" -f m"); // changed from -f e DP200509
		else
			sb.append(" -f 0.25,0.25,0.25,0.25");

		// optimize pinvar if needed
		if (currentModel.ispI())
			sb.append(" -v e");

		// optimize alpha if needed
		if (currentModel.ispG()) {
			sb.append(" -c ").append(options.numGammaCat);
			sb.append(" -a e");
		} else
			sb.append(" -c 1");

		// threaded version
		if (numberOfThreads > 0) {
			sb.append(" --num_threads ").append(numberOfThreads);
		}

		// avoid memory warning
		sb.append(" --no_memory_check");

		/*
		 * params=tlr: tree topology (t), branch length (l) and substitution
		 * rate parameters (r) are optimised. params = tlr or tl: optimize tree
		 * topology and branch lengths params = lr or l: tree topology fixed;
		 * optimize branch lengths; params = r or none: both tree topology and
		 * branch lengths are fixed.
		 */
		if (justGetJCtree) {
			// tree topology is fixed.
			sb.append(" -o lr");
		} else if (options.userTopologyExists || options.fixedTopology) {
			// use a single tree for all models
			sb.append(" -u ").append(options.getTreeFile().getAbsolutePath());
			sb.append(" -o lr"); // tree topology fixed; optimize branch lengths
		} else if (!options.optimizeMLTopology)
		{
			// use BIONJ tree for each model
			sb.append(" -o lr"); // tree topology fixed; optimize branch lengths
		} else {
			sb.append(" -o tlr"); // optimize tree topology and branch lengthss

			// search strategy
			switch (options.treeSearchOperations) {
			case SPR:
				sb.append(" -s SPR");
				break;
			case BEST:
				sb.append(" -s BEST");
				break;
			default:
				sb.append(" -s NNI");
			}
		}

		return sb.toString();
	}

	/***************************
	 * executeCommandLine ************************ * Executes a set of command
	 * line in the system * * *
	 ***********************************************************************/

	private void executeCommandLine() {
		String[] executable = new String[1];
		try {
			if (!ModelTestConfiguration.isGlobalPhymlBinary()) {
				if (!RunPhyml.phymlBinary.exists()) {
					notifyObservers(
							ProgressInfo.ERROR_BINARY_NOEXISTS, index, model, RunPhyml.phymlBinary.getAbsolutePath());
				} else if (!RunPhyml.phymlBinary.canExecute()) {
					notifyObservers(
							ProgressInfo.ERROR_BINARY_NOEXECUTE, index, model, RunPhyml.phymlBinary.getAbsolutePath());
					
				}
			}
			executable[0] = RunPhyml.phymlBinaryStr;
			
			String[] tokenizedCommandLine = commandLine.split(" ");
			String[] cmd = Utilities.specialConcatStringArrays(executable,
					tokenizedCommandLine);

			// get process and execute command line
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(cmd, null, RunPhyml.PHYML_PATH.equals("") ? null
					: new File(RunPhyml.PHYML_PATH));
			ProcessManager.getInstance().registerProcess(proc);

			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(
					proc.getErrorStream(), "ERROR", System.err, ModelTest.getPhymlConsole());
			// any output?
			FileOutputStream logFile = new FileOutputStream(
					options.getLogFile(), true);
			StreamGobbler outputGobbler = new StreamGobbler(
					proc.getInputStream(), "PHYML", logFile, ModelTest.getPhymlConsole());

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			// any error???
			int exitVal = proc.waitFor();
			ProcessManager.getInstance().removeProcess(proc);

			if (verbose > 1)
				System.out.println("ExitValue: " + exitVal);

			// print command line to phmyl logfile
			PrintWriter printout = new PrintWriter(logFile);
			printout.println(" ");
			printout.println("Command line used for process "+ outputGobbler.getRunId() +":");
			String uCommand = commandLine.replace(options.getAlignmentFile().getAbsolutePath(), 
					options.getInputFile().getAbsolutePath());
			if (options.userTopologyExists) {
				uCommand = uCommand.replace(options.getTreeFile().getAbsolutePath(), 
						options.getInputTreeFile().getAbsolutePath());
			}
			printout.println("    " + RunPhyml.phymlBinary.getAbsolutePath() + " "
					+ uCommand);
			printout.println(" ");
			printout.flush();
			printout.close();
			
			// print to console
			if (ModelTest.getPhymlConsole() != null) {
				synchronized (ModelTest.getPhymlConsole()) {
					ModelTest.getPhymlConsole().println(" ");
					ModelTest.getPhymlConsole().println("Command line used for process "+ outputGobbler.getRunId() +":");
					ModelTest.getPhymlConsole().println("    " + RunPhyml.phymlBinary.getAbsolutePath() + " "
							+ uCommand);
					ModelTest.getPhymlConsole().println(" ");
					ModelTest.getPhymlConsole().flush();
					ModelTest.getPhymlConsole().close();
				}
			}

		} catch (InterruptedException e) {
			notifyObservers(ProgressInfo.INTERRUPTED, index, model, null);
			interrupted = true;
		} catch (Throwable t) {
			notifyObservers(
					ProgressInfo.ERROR,
					index,
					model,
					"Cannot run the Phyml command line for some reason: "
							+ t.getMessage());
			interrupted = true;
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
		try {
			TextInputStream phymlStatFile = new TextInputStream(
					phymlStatFileName);
			if (ignoreGaps) {
				while ((line = phymlStatFile.readLine()) != null) {
					if (line.length() > 0) {
						if (line.startsWith(". Log-likelihood")) {
							currentModel.setLnLIgnoringGaps((-1.0)
									* Double.parseDouble(Utilities
											.lastToken(line)));
						} else if (line.contains("Unconstrained likelihood")) {
							double unconstrainedLnL = (-1.0)
									* Double.parseDouble(Utilities
											.lastToken(line));
							currentModel.setUnconstrainedLnL(unconstrainedLnL);
							if (Math.abs(options.getUnconstrainedLnL()
									- unconstrainedLnL) > 1e-10) {
								// uLK has changed!!!
								// temporary uLK is updated
								options.setUnconstrainedLnL(unconstrainedLnL);
							}
						}
					}
				}
			} else {
				while ((line = phymlStatFile.readLine()) != null) {
					if (line.length() > 0) {
						if (line.startsWith(". Log-likelihood")) {
							currentModel.setLnL((-1.0)
									* Double.parseDouble(Utilities
											.lastToken(line)));
							if (showParsing)
								System.err.println("Reading lnL = "
										+ currentModel.getLnL());
						} else if (line.startsWith(". Discrete gamma model")) {
							if (Utilities.lastToken(line).equals("Yes")) {
								// currentModel.pG = true;
								line = phymlStatFile.readLine();
								currentModel.setNumGammaCat(Integer
										.parseInt(Utilities.lastToken(line)));
								if (showParsing)
									System.err.println("Reading numGammaCat = "
											+ currentModel.getNumGammaCat());
								line = phymlStatFile.readLine();
								currentModel
										.setShape(Double.parseDouble(Utilities
												.lastToken(line)));
								if (showParsing)
									System.err.println("Reading shape = "
											+ currentModel.getShape());
							}
						} else if (line.startsWith(". Nucleotides frequencies")) {
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
						} else if (line.startsWith(". Proportion of invariant")) {
							// currentModel.pI = true;
							currentModel.setPinv(Double.parseDouble(Utilities
									.lastToken(line)));
							if (showParsing)
								System.err.println("Reading pinv = "
										+ currentModel.getPinv());
						} else if (line.contains("Unconstrained likelihood")) {
							double unconstrainedLnL = (-1.0)
									* Double.parseDouble(Utilities
											.lastToken(line));
							if (!options.isAmbiguous()) {
								currentModel
										.setUnconstrainedLnL(unconstrainedLnL);
							} else {
								currentModel.setUnconstrainedLnL(0.0d);
							}
							if (Math.abs(options.getUnconstrainedLnL()) <= 1e-10) {
								options.setUnconstrainedLnL(unconstrainedLnL);
							} else {
								if (Math.abs(options.getUnconstrainedLnL()
										- unconstrainedLnL) > 1e-10) {
									// uLK has changed!!!
									// temporary uLK is updated
									options.setUnconstrainedLnL(unconstrainedLnL);
								}
							}
							if (showParsing)
								System.err
										.println("Reading unconstrained logLK = "
												+ currentModel
														.getUnconstrainedLnL());
						} else if (line
								.startsWith(". GTR relative rate parameters")) {
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
									.lastToken(line)));
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
							// with custom models phyml does not provide a
							// ti/tv, so
							// we
							// calculate it from the rate parameters
							// note this is kappa and we need to transform it to
							// ti/tv
							if (currentModel.ispT()) {
								currentModel.setKappa(currentModel.getRb());
								currentModel
										.setTitv(currentModel.getKappa()
												* (currentModel.getfA()
														* currentModel.getfG() + currentModel
														.getfC()
														* currentModel.getfT())
												/ ((currentModel.getfA() + currentModel
														.getfG()) * (currentModel
														.getfC() + currentModel
														.getfT())));
							}
						}
					}
				}
			}
			phymlStatFile.close();
		} catch (FileNotFoundException e) {
			notifyObservers(ProgressInfo.ERROR, index, model,
					"Optimization results file does not exist: "
							+ phymlStatFileName);

		} catch (NullPointerException e) {
			notifyObservers(
					ProgressInfo.ERROR,
					index,
					model,
					"Error while parsing result data from "
							+ currentModel.getName());
		}

		try {
			// Get ML tree
			TextInputStream phymlTreeFile = new TextInputStream(
					phymlTreeFileName);
			String treestr = phymlTreeFile.readLine();
			currentModel.setTreeString(treestr);
			phymlTreeFile.close();
		} catch (FileNotFoundException e) {
			notifyObservers(ProgressInfo.ERROR, index, model, null);
			System.err.println("Optimized tree file does not exist: "
					+ phymlTreeFileName);

		} catch (TreeParseException e) {
			StringBuffer sb = new StringBuffer();
			sb.append(" Please, check the PhyML log");
			if (ModelTest.execMode == ModelTest.ExecMode.GUI) {
				sb.append(" tab,");
			} else {
				sb.append(" file at " + options.getLogFile());
			}
			sb.append(" or run PhyML alone for getting more information: ");
			notifyObservers(ProgressInfo.ERROR, index, model, "ML tree for "
					+ currentModel.getName() + " is invalid." + sb.toString());
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
