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
package es.uvigo.darwin.jmodeltest;

import java.awt.GraphicsEnvironment;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import mpi.MPI;
import mpi.MPIException;
import pal.tree.Tree;
import pal.tree.TreeParseException;
import es.uvigo.darwin.jmodeltest.exe.RunConsense;
import es.uvigo.darwin.jmodeltest.exe.RunPhyml;
import es.uvigo.darwin.jmodeltest.exe.RunPhymlHybrid;
import es.uvigo.darwin.jmodeltest.exe.RunPhymlMPJ;
import es.uvigo.darwin.jmodeltest.exe.RunPhymlThread;
import es.uvigo.darwin.jmodeltest.gui.XManager;
import es.uvigo.darwin.jmodeltest.io.HtmlReporter;
import es.uvigo.darwin.jmodeltest.io.TextInputStream;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ConsoleProgressObserver;
import es.uvigo.darwin.jmodeltest.selection.AIC;
import es.uvigo.darwin.jmodeltest.selection.AICc;
import es.uvigo.darwin.jmodeltest.selection.BIC;
import es.uvigo.darwin.jmodeltest.selection.DT;
import es.uvigo.darwin.jmodeltest.selection.HLRT;
import es.uvigo.darwin.jmodeltest.selection.InformationCriterion;
import es.uvigo.darwin.jmodeltest.tree.TreeSummary;
import es.uvigo.darwin.jmodeltest.tree.TreeUtilities;
import es.uvigo.darwin.jmodeltest.utilities.Simulation;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;
import es.uvigo.darwin.prottest.util.fileio.AlignmentReader;

/**
 * ModelTest.java
 * 
 * Description: Main class for selecting models of nucleotide substitition
 * 
 * @author Diego Darriba, University of Vigo / University of A Coru�a, Spain
 *         ddarriba@udc.es
 * @author David Posada, University of Vigo, Spain dposada@uvigo.es |
 *         darwin.uvigo.es
 * @version 2.1 (May 2012)
 */
public class ModelTest {

	private ApplicationOptions options = ApplicationOptions.getInstance();

	/** The MPJ rank of the process. It is only useful if MPJ is running. */
	public static int MPJ_ME;
	/** The MPJ size of the communicator. It is only useful if MPJ is running. */
	public static int MPJ_SIZE;
	/** The MPJ running state. */
	public static boolean MPJ_RUN;

	// application constant definitions
	public static final int PRECISION = 4;
	public static final double INFINITY = 9999;
	public static final int MAX_NUM_MODELS = 88;
	public static final int MAX_NAME = 60;
	public static final String CURRENT_VERSION = "2.1.6";
	public static final String programName = ("jModeltest");
	public static final String URL = "http://code.google.com/p/jmodeltest2";
	public static final String WIKI = "http://code.google.com/p/jmodeltest2/wiki/GettingStarted";
	public static final String DISCUSSION_GROUP = "http://groups.google.com/group/jmodeltest";
	public static final String CONFIG_FILE = "conf/jmodeltest.conf";
	public static final String UNKNOWN_HOSTNAME = "UNKNOWN";

	private static TextOutputStream MAIN_CONSOLE;
	private static TextOutputStream PHYML_CONSOLE = null;
	private static TextOutputStream CURRENT_OUT_STREAM;

	public static String[] arguments;

	private static boolean AICwasCalculated = false;
	private static boolean AICcwasCalculated = false;
	private static boolean BICwasCalculated = false;
	private static boolean DTwasCalculated = false;

	public static Vector<String> testingOrder; // order of the hLRTs
	public static String averagedTreeString; // model-averaged phylogeny in
												// Newick format
	public static enum ExecMode {
		CONSOLE, GUI
	};
	public static ExecMode execMode;
	
	private static AIC myAIC;
	private static AICc myAICc;
	private static BIC myBIC;
	private static DT myDT;
	private static HLRT myHLRT;

	private static RunConsense consensusAIC;
	private static RunConsense consensusAICc;
	private static RunConsense consensusBIC;
	private static RunConsense consensusDT;

	private static Model[] candidateModels;
	private static Model[] loadedModels;
	private static Model minAIC, minAICc, minBIC, minDT, minHLRT, minDLRT;

	private static String hostname;
	public static Hashtable<String, Integer> HOSTS_TABLE;

	// We can work under a GUI or in the command line
	public static boolean buildGUI = true;

	static {
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			// Get hostname
			hostname = addr.getHostName();
		} catch (UnknownHostException e) {
			hostname = UNKNOWN_HOSTNAME;
			System.err.println("WARNING: This host is unknown");
			// WARN AND DO NOTHING
		}
	}

	// constructor with GUI
	public ModelTest() {
		if (!GraphicsEnvironment.isHeadless()) {
			options.createLogFile();
			execMode = ExecMode.GUI;
			XManager.getInstance();
		} else {
			System.err.println("");
			System.err.println("ERROR: You are trying to run a GUI interface in a headless server.");
			finalize(-1);
		}
	}

	// constructor without GUI
	public ModelTest(String[] arg) {
		try {
			// open mainConsole
			MAIN_CONSOLE = new TextOutputStream(System.out);
			execMode = ExecMode.CONSOLE;
			ParseArguments();
			options.createLogFile();
			options.createCkpFile();
			if (options.doingSimulations) {
				Simulation sim = new Simulation(options);
				sim.run();
			} else
				runCommandLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finalize(0);
	}

	/****************************
	 * main ************************************ * Starts the application * * *
	 ***********************************************************************/

	public static boolean loadCheckpoint (File ckpFile) {
		try {
			InputStream file = new FileInputStream(ckpFile);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			loadedModels = (Model[]) input.readObject();
			int numModels = 0;
			for (Model model : loadedModels) {
				if (model.getLnL() > 0.0) numModels++;
			}
			MAIN_CONSOLE.println(" ok!");
			MAIN_CONSOLE.println("Loaded "+ numModels +" models");
			ApplicationOptions.getInstance().setNumModels(loadedModels.length);
			input.close();
		} catch (ClassNotFoundException ex) {
			MAIN_CONSOLE.println(" cannot perform input.");
			return false;
		} catch (IOException ex) {
			MAIN_CONSOLE.println(" cannot perform input.");
			return false;
		}
		return true;
	}
	
	public static void main(String[] args) {
		// initializing MPJ environment (if available)
		System.err.println("[MPI] Testing MPI environment... (" + hostname
				+ ")");
		try {
			arguments = MPI.Init(args);
			System.err.println("[MPI] ... OK! [" + hostname + " (" + MPJ_ME
					+ ")]");
			MPJ_ME = MPI.COMM_WORLD.Rank();
			MPJ_SIZE = MPI.COMM_WORLD.Size();
			MPJ_RUN = true;
		} catch (MPIException e) {
			System.err.println("[MPI] Proceed without MPI");
			MPJ_ME = 0;
			MPJ_SIZE = 1;
			MPJ_RUN = false;
			arguments = args;
		} catch (Exception e) {
			System.err.println("[MPI] Proceed without MPI");
			MPJ_ME = 0;
			MPJ_SIZE = 1;
			MPJ_RUN = false;
			arguments = args;
		} catch (ExceptionInInitializerError e) {
			System.err.println("[MPI] Initializer error!");
			System.err.println(e.getMessage());
			System.exit(-1);
			MPJ_ME = 0;
			MPJ_SIZE = 1;
			MPJ_RUN = false;
			arguments = args;
		} catch (NoClassDefFoundError e) {
			System.err.println("[MPI] Proceed without MPI");
			MPJ_ME = 0;
			MPJ_SIZE = 1;
			MPJ_RUN = false;
			arguments = args;
		}

		if (arguments.length < 1) {
			buildGUI = true;
			new ModelTest();
		} else {
			buildGUI = false;
			new ModelTest(arguments);
		}

		ApplicationOptions.getInstance().getLogFile().delete();
	}

	/****************************
	 * runCommandLine ************************** * Organizes all the tasks that
	 * the program needs to carry out * * *
	 ***********************************************************************/
	public void runCommandLine() {

		if (MPJ_ME == 0) {
			// print header information
			printHeader(MAIN_CONSOLE);

			// print citation information
			printCitation(MAIN_CONSOLE);

			// print notice information
			printNotice(MAIN_CONSOLE);

			// print the command line
			MAIN_CONSOLE.println(" ");
			MAIN_CONSOLE.print("Arguments =");
			for (int i = 0; i < arguments.length; i++)
				MAIN_CONSOLE.print(" " + arguments[i]);

			try {
				checkInputFiles();
			} catch (InvalidArgumentException.InvalidInputFileException e) {
				MAIN_CONSOLE.println(e.getMessage());
				finalize(-1);
			}

			// calculate number of models
			if (options.getSubstTypeCode() == 0)
				options.setNumModels(3);
			else if (options.getSubstTypeCode() == 1)
				options.setNumModels(5);
			else if (options.getSubstTypeCode() == 2)
				options.setNumModels(7);
			else if (options.getSubstTypeCode() == 3)
				options.setNumModels(11);
			else
				options.setNumModels(203);

			if (options.doF)
				options.setNumModels(options.getNumModels() * 2);

			if (options.doI && options.doG)
				options.setNumModels(options.getNumModels() * 4);
			else if (options.doI || options.doG)
				options.setNumModels(options.getNumModels() * 2);
			options.setCandidateModels();
		}
		// build set of models

		// calculate likelihoods with phyml in the command line
		RunPhyml runPhyml;
		if (MPJ_RUN) {
			if (options.threadScheduling && options.getNumberOfThreads() > 0) {
				runPhyml = new RunPhymlHybrid(MPJ_ME, MPJ_SIZE,
						new ConsoleProgressObserver(options), options,
						getCandidateModels(), options.getNumberOfThreads());
			} else {
				runPhyml = new RunPhymlMPJ(
						new ConsoleProgressObserver(options), options,
						getCandidateModels());
			}
		} else {
			// if (options.getSubstTypeCode() == 4) {
			// runPhyml = new RunPhymlClustering(new
			// ConsoleProgressObserver(options),
			// options, getCandidateModels());
			// } else {
			runPhyml = new RunPhymlThread(new ConsoleProgressObserver(options),
					options, getCandidateModels());
			// }
		}
		runPhyml.execute();

		if (MPJ_ME == 0) {

			List<Model> bestModels = new ArrayList<Model>();
			// do AIC if selected
			if (options.doAIC) {
				myAIC = new AIC(options.writePAUPblock, options.doImportances,
						options.doModelAveraging, options.confidenceInterval);
				myAIC.compute();
				minAIC = myAIC.getMinModel();
				AICwasCalculated = true;
				bestModels.add(minAIC);
			}

			// do AICc if selected
			if (options.doAICc) {
				myAICc = new AICc(options.writePAUPblock,
						options.doImportances, options.doModelAveraging,
						options.confidenceInterval);
				myAICc.compute();
				minAICc = myAICc.getMinModel();
				AICcwasCalculated = true;
				if (!bestModels.contains(minAICc)) {
					bestModels.add(minAICc);
				}
			}

			// do BIC if selected
			if (options.doBIC) {
				myBIC = new BIC(options.writePAUPblock, options.doImportances,
						options.doModelAveraging, options.confidenceInterval);
				myBIC.compute();
				minBIC = myBIC.getMinModel();
				BICwasCalculated = true;
				if (!bestModels.contains(minBIC)) {
					bestModels.add(minBIC);
				}
			}

			// do DT if selected
			if (options.doDT) {
				myDT = new DT(options.writePAUPblock, options.doImportances,
						options.doModelAveraging, options.confidenceInterval);
				myDT.compute();
				minDT = myDT.getMinModel();
				DTwasCalculated = true;
			}

			if (options.isAmbiguous() && options.isForceCheckULnL()) {
				runPhyml.executeIgnoreGaps(bestModels.toArray(new Model[0]));
			}

			if (options.doAIC) {
				myAIC.print(MAIN_CONSOLE);
				if (options.doAveragedPhylogeny) {
					consensusAIC = new RunConsense(myAIC,
							options.consensusType, options.confidenceInterval);
				}
			}
			if (options.doAICc) {
				myAICc.print(MAIN_CONSOLE);
				if (options.doAveragedPhylogeny) {
					consensusAICc = new RunConsense(myAICc,
							options.consensusType, options.confidenceInterval);
				}
			}
			if (options.doBIC) {
				myBIC.print(MAIN_CONSOLE);
				if (options.doAveragedPhylogeny) {
					consensusBIC = new RunConsense(myBIC,
							options.consensusType, options.confidenceInterval);
				}
			}
			if (options.doDT) {
				myDT.print(MAIN_CONSOLE);
				if (options.doAveragedPhylogeny) {
					consensusDT = new RunConsense(myDT, options.consensusType,
							options.confidenceInterval);
				}
			}
			// do hLRT if selected
			if (options.doHLRT) {
				myHLRT = new HLRT(options);
				myHLRT.compute(!options.backwardHLRTSelection,
						options.confidenceLevelHLRT, options.writePAUPblock);
			}

			// do dLRT if selected
			if (options.doDLRT) {
				myHLRT = new HLRT(options);
				myHLRT.computeDynamical(!options.backwardHLRTSelection,
						options.confidenceLevelHLRT, options.writePAUPblock);
			}

			Tree bestAIC = myAIC != null ? myAIC.getMinModel().getTree() : null;
			Tree bestAICc = myAICc != null ? myAICc.getMinModel().getTree()
					: null;
			Tree bestBIC = myBIC != null ? myBIC.getMinModel().getTree() : null;
			Tree bestDT = myDT != null ? myDT.getMinModel().getTree() : null;

			MAIN_CONSOLE.println(" ");
			MAIN_CONSOLE.println(" ");
			MAIN_CONSOLE.println(" ");
			MAIN_CONSOLE
					.println("---------------------------------------------------------------");
			MAIN_CONSOLE
					.println("*                                                             *");
			MAIN_CONSOLE
					.println("*                    SELECTION SUMMARY                        *");
			MAIN_CONSOLE
					.println("*                                                             *");
			MAIN_CONSOLE
					.println("---------------------------------------------------------------");
			MAIN_CONSOLE.println("");

			TreeSummary treeSummary = new TreeSummary(bestAIC, bestAICc,
					bestBIC, bestDT, candidateModels);

			treeSummary.print(MAIN_CONSOLE);

			MAIN_CONSOLE.println(" ");
			MAIN_CONSOLE.println(" ");
			MAIN_CONSOLE.println(" ");
			MAIN_CONSOLE.println("::Best Models::");
			MAIN_CONSOLE.println(" ");
			if (myAIC == null && myAICc == null && myBIC == null
					&& myDT == null) {
				MAIN_CONSOLE.println("No information criterion was selected.");
			}
			MAIN_CONSOLE
					.println("\tModel \t\tf(a) \tf(c) \tf(g) \tf(t) \tkappa \ttitv "
							+ "\tRa\tRb\tRc\tRd\tRe\tRf\tpInv \tgamma");
			MAIN_CONSOLE
					.println("----------------------------------------------------------------------------------------------------------------------------------------");
			if (myAIC != null) {
				Model minModel = myAIC.getMinModel();
				MAIN_CONSOLE.println("AIC \t" + getModelRow(minModel));
				// MAIN_CONSOLE.println("average \t" +
				// getModelAverageRow(myAIC));
			}
			if (myBIC != null) {
				Model minModel = myBIC.getMinModel();
				MAIN_CONSOLE.println("BIC \t" + getModelRow(minModel));
				// MAIN_CONSOLE.println("average \t" +
				// getModelAverageRow(myBIC));
			}
			if (myAICc != null) {
				Model minModel = myAICc.getMinModel();
				MAIN_CONSOLE.println("AICc \t" + getModelRow(minModel));
				// MAIN_CONSOLE.println("average \t" +
				// getModelAverageRow(myAICc));
			}
			if (myDT != null) {
				Model minModel = myDT.getMinModel();
				MAIN_CONSOLE.println("DT \t" + getModelRow(minModel));
				// MAIN_CONSOLE.println("average \t" +
				// getModelAverageRow(myDT));

				MAIN_CONSOLE.println(" ");
				MAIN_CONSOLE.println("Program is done.");

			}

			if (ModelTestConfiguration.isHtmlLogEnabled()) {
				HtmlReporter.buildReport(options,
						ModelTest.getCandidateModels(), null, treeSummary);
			}
		} // end root

	} // end of runCommandLine

	private String getModelRow(Model model) {
		StringBuilder sb = new StringBuilder();
		sb.append(model.getName() + "\t");
		if (model.getName().length() < 8)
			sb.append("\t");
		sb.append(Utilities.format(model.getfA(), 4, 2, false) + "\t"
				+ Utilities.format(model.getfC(), 4, 2, false) + "\t"
				+ Utilities.format(model.getfG(), 4, 2, false) + "\t"
				+ Utilities.format(model.getfT(), 4, 2, false) + "\t"
				+ Utilities.format(model.getKappa(), 4, 2, false) + "\t"
				+ Utilities.format(model.getTitv(), 4, 2, false) + "\t"
				+ Utilities.format(model.getRa(), 7, 3, false) + " "
				+ Utilities.format(model.getRb(), 7, 3, false) + " "
				+ Utilities.format(model.getRc(), 7, 3, false) + " "
				+ Utilities.format(model.getRd(), 7, 3, false) + " "
				+ Utilities.format(model.getRe(), 7, 3, false) + " "
				+ Utilities.format(model.getRf(), 7, 3, false) + " ");
		if (model.ispI()) {
			sb.append(Utilities.format(model.getPinv(), 7, 2, false));
		} else {
			sb.append("N/A");
		}
		sb.append("\t");
		if (model.ispG()) {
			sb.append(Utilities.format(model.getShape(), 7, 2, false));
		} else {
			sb.append("N/A");
		}
		return sb.toString();
	}

	@SuppressWarnings("unused")
	private String getModelAverageRow(InformationCriterion ic) {
		StringBuilder sb = new StringBuilder();
		sb.append(Utilities.format(ic.getAfA(), 4, 2, false) + "\t");
		sb.append(Utilities.format(ic.getAfC(), 4, 2, false) + "\t");
		sb.append(Utilities.format(ic.getAfG(), 4, 2, false) + "\t");
		sb.append(Utilities.format(ic.getAfT(), 4, 2, false) + "\t");
		sb.append(Utilities.format(ic.getAkappa(), 4, 2, false) + "\t");
		sb.append(Utilities.format(ic.getAtitv(), 4, 2, false) + "\t");
		sb.append(Utilities.format(ic.getaRa(), 7, 3, false) + "\t");
		sb.append(Utilities.format(ic.getaRb(), 7, 3, false) + "\t");
		sb.append(Utilities.format(ic.getaRc(), 7, 3, false) + "\t");
		sb.append(Utilities.format(ic.getaRd(), 7, 3, false) + "\t");
		sb.append(Utilities.format(ic.getaRe(), 7, 3, false) + "\t");
		sb.append(Utilities.format(ic.getaRf(), 7, 3, false) + "\t");
		sb.append(Utilities.format(ic.getApinvI(), 7, 4, false) + "\t");
		sb.append(Utilities.format(ic.getApinvIG(), 7, 4, false) + "\t");
		sb.append(Utilities.format(ic.getAshapeG(), 7, 3, false) + "\t");
		sb.append(Utilities.format(ic.getAshapeIG(), 7, 3, false) + "\t");
		return sb.toString();
	}

	/****************************
	 * ParseArguments **************************** * Parses the command line for
	 * jModeltest * *
	 ************************************************************************/

	public void ParseArguments() {
		int i, j;
		String arg = "";
		String error = "\nCOMMAND LINE ERROR: ";
		File ckpFile = null;
		boolean isInputFile = false;
		boolean getPhylip = false;
		try {
			i = 0;
			while (i < arguments.length) {
				if (!arguments[i].startsWith("-")) {
					System.err
							.println(error
									+ "Arguments must start with \"-\". The ofending argument was: "
									+ arguments[i] + ".");
					System.err.print("      Arguments: ");
					for (String argument : arguments) {
						System.err.print(argument + " ");
					}
					System.err.println("");

					CommandLineError();
					System.exit(1);
				}

				arg = arguments[i++];

				if (arg.equals("-d")) {
					if (i < arguments.length) {
						options.setInputFile(new File(arguments[i++]));
						isInputFile = true;
					} else {
						System.err.println(error
								+ "-d option requires an input filename.");
						CommandLineError();
					}
				}

				else if (arg.equals("-o")) {
					String outFile = arguments[i++];
					try {
						MAIN_CONSOLE = new TextOutputStream(new PrintStream(
								outFile));
					} catch (FileNotFoundException e1) {
						System.err
								.println("An error has ocurred while trying to open the output file \""
										+ outFile + "\" for writing");
						e1.printStackTrace();
						System.exit(-1);
					}
				} else if (arg.equals("-s")) {
					if (i < arguments.length) {
						String type = arguments[i++];
						try {
							int number = Integer.parseInt(type);
							switch (number) {
							case 3:
								options.setSubstTypeCode(0);
								break;
							case 5:
								options.setSubstTypeCode(1);
								break;
							case 7:
								options.setSubstTypeCode(2);
								break;
							case 11:
								options.setSubstTypeCode(3);
								break;
							case 203:
								options.setSubstTypeCode(4);
								break;
							default:
								System.err
										.println(error
												+ "-s substitution types have to be 3,5,7,11 only.");
								CommandLineError();
							}
						} catch (NumberFormatException e) {
							System.err
									.println(error
											+ "-s option requires a number for the substitution types: 3,5,7,11.");
							CommandLineError();
						}
					} else {
						System.err
								.println(error
										+ "-s option requires a number for the substitution types: 3,5,7,11.");
						CommandLineError();
					}
				}

				else if (arg.equals("-ckp")) {
					if (i < arguments.length) {
						ckpFile = new File(arguments[i++]);
					} else {
						System.err.println(error
								+ "-ckp option requires a checkpoint filename.");
						CommandLineError();
					}
				}
				else if (arg.equals("-f")) {
					options.doF = true;
				}

				else if (arg.equals("-i")) {
					options.doI = true;
				}

				else if (arg.equals("-g")) {
					if (i < arguments.length) {
						try {
							options.doG = true;
							String type = arguments[i++];
							options.numGammaCat = Integer.parseInt(type);
						} catch (NumberFormatException e) {
							System.err
									.println(error
											+ "-g option requires a number of gamma categories.");
							CommandLineError();
						}
					} else {
						System.err
								.println(error
										+ "-g option requires a number of gamma categories.");
						CommandLineError();
					}
				}

				else if (arg.equals("-G")) {
					if (i < arguments.length) {
						try {
							Double threshold = Double
									.parseDouble(arguments[i++]);
							options.setGuidedSearchThreshold(threshold);
						} catch (NumberFormatException e) {
							System.err.println(error
									+ "-G option requires a threshold.");
							CommandLineError();
						}
					} else {
						System.err.println(error
								+ "-G option requires a threshold.");
						CommandLineError();
					}
				}

				else if (arg.equals("-H")) {
					if (i < arguments.length) {
						String criterion = arguments[i++];
						if (criterion.equals("AIC")) {
							options.setHeuristicInformationCriterion(InformationCriterion.IC_AIC);
						} else if (criterion.equals("BIC")) {
							options.setHeuristicInformationCriterion(InformationCriterion.IC_BIC);
						} else if (criterion.equals("AICc")) {
							options.setHeuristicInformationCriterion(InformationCriterion.IC_AICc);
						} else {
							System.err
									.println(error
											+ "-H argument is invalid (AIC, BIC, AICc).");
							CommandLineError();
						}
					} else {
						System.err
								.println(error
										+ "-H option requires an argument (AIC, BIC, AICc).");
						CommandLineError();
					}
				} else if (arg.equals("-t")) {
					if (i < arguments.length) {
						String type = arguments[i++];

						if (type.equalsIgnoreCase("fixed")) {
							options.fixedTopology = true;
							options.optimizeMLTopology = false;
							options.userTopologyExists = false;
						} else if (type.equalsIgnoreCase("BIONJ")) {
							options.fixedTopology = false;
							options.optimizeMLTopology = false;
							options.userTopologyExists = false;
						} else if (type.equalsIgnoreCase("ML")) {
							options.fixedTopology = false;
							options.optimizeMLTopology = true;
							options.userTopologyExists = false;
						} else {
							System.err
									.println(error
											+ "-t option requires a type of base tree for likelihod calculations: "
											+ "\"fixed\", \"BIONJ\" or \"ML\" only");
							CommandLineError();
						}
					} else {
						System.err
								.println(error
										+ "-t option requires a type of base tree for likelihod calculations: "
										+ "fixed, BIONJ or ML");
						CommandLineError();
					}
				} else if (arg.equals("-getPhylip")) {
					getPhylip = true;
				}

				else if (arg.equals("-u")) {
					if (i < arguments.length) {
						options.setInputTreeFile(new File(arguments[i++]));
						options.fixedTopology = false;
						options.optimizeMLTopology = false;
						options.userTopologyExists = true;
					} else {
						System.err
								.println(error
										+ "-u option requires an file name for the tree file");
						CommandLineError();
					}
				}

				else if (arg.equals("-n")) {
					if (i < arguments.length) {
						options.setExecutionName(arguments[i++]);
					} else {
						System.err
								.println(error
										+ "-n option requires a name for the execution");
						CommandLineError();
					}
				}
				
				else if (arg.equals("-S")) {
					if (i < arguments.length) {
						String type = arguments[i++];

						if (type.equalsIgnoreCase("NNI")) {
							options.treeSearchOperations = ApplicationOptions.TreeSearch.NNI;
						} else if (type.equalsIgnoreCase("SPR")) {
							options.treeSearchOperations = ApplicationOptions.TreeSearch.SPR;
						} else if (type.equalsIgnoreCase("BEST")) {
							options.treeSearchOperations = ApplicationOptions.TreeSearch.BEST;
						} else {
							System.err
									.println(error
											+ "-S option requires a type of tree topology search operation: "
											+ "\"NNI\", \"SPR\" or \"BEST\" only");
							CommandLineError();
						}
					} else {
						System.err
								.println(error
										+ "-S option requires a type of tree topology search operation: "
										+ "\"NNI\", \"SPR\", \"BEST\"");
						CommandLineError();
					}
				}

				else if (arg.equals("-AIC")) {
					options.doAIC = true;
				}

				else if (arg.equals("-AICc")) {
					options.doAICc = true;
				}

				else if (arg.equals("-BIC")) {
					options.doBIC = true;
				}

				else if (arg.equals("-DT")) {
					options.doDT = true;
				}

				else if (arg.equals("-uLnL")) {
					options.setForceCheckULnL(true);
				}

				else if (arg.equals("-p")) {
					options.doImportances = true;
				}

				else if (arg.equals("-v")) {
					options.doImportances = true;
					options.doModelAveraging = true;
				}

				else if (arg.equals("-w")) {
					options.writePAUPblock = true;
				}

				else if (arg.equals("-c")) {
					if (i < arguments.length) {
						try {
							String type = arguments[i++];
							options.confidenceInterval = Double
									.parseDouble(type);
						} catch (NumberFormatException e) {
							System.err
									.println(error
											+ "-c option requires a number (0-1) for the model selection confidence interval.");
							CommandLineError();
						}
					} else {
						System.err
								.println(error
										+ "-c option requires a number (0-1) for the model selection confidence interval.");
						CommandLineError();
					}
				}

				else if (arg.equals("-help")) {
					PrintUsage();
				}

				else if (arg.equals("-hLRT")) {
					options.doHLRT = true;
				}

				else if (arg.equals("-O")) {
					if (i < arguments.length) {
						String type = arguments[i++];
						char[] array = type.toCharArray();
						Arrays.sort(array);

						String validString = "";

						if (type.length() == 5) {
							validString = "ftvgp";
						} else if (type.length() == 6) {
							validString = "ftvwgp";
						} else if (type.length() == 7) {
							validString = "ftvwxgp";
						} else {
							System.err
									.println(error
											+ "-O option requires a 5, 6 or 7 specific letter string with the order of tests (ftvgp/ftvwgp/ftvwxgp)");
							CommandLineError();
						}

						char[] valid = validString.toCharArray();
						if (!Arrays.equals(array, valid)) {
							System.err
									.println(error
											+ "-O option requires a 5, 6 or 7 specific letter string with the order of tests (ftvgp/ftvwgp/ftvwxgp)");
							CommandLineError();
						} else {
							testingOrder = new Vector<String>();
							for (j = 0; j < type.length(); j++) {
								if (type.charAt(j) == 'f')
									testingOrder.addElement("freq");
								else if (type.charAt(j) == 't')
									testingOrder.addElement("titv");
								else if (type.charAt(j) == 'v') {
									if (options.getSubstTypeCode() == 0)
										testingOrder.addElement("2ti4tv");
									else if (options.getSubstTypeCode() >= 1)
										testingOrder.addElement("2ti");
								} else if (type.charAt(j) == 'w') {
									if (options.getSubstTypeCode() >= 1)
										testingOrder.addElement("2tv");
								} else if (type.charAt(j) == 'x') {
									if (options.getSubstTypeCode() > 1)
										testingOrder.addElement("4tv");
								} else if (type.charAt(j) == 'g')
									testingOrder.addElement("gamma");
								else if (type.charAt(j) == 'p')
									testingOrder.addElement("pinv");
							}
						}
					}
				}

				else if (arg.equals("-dLRT")) {
					options.doDLRT = true;
				}

				else if (arg.equals("-r")) {
					options.backwardHLRTSelection = true;
				}

				else if (arg.equals("-h")) {
					if (i < arguments.length) {
						try {
							String type = arguments[i++];
							options.confidenceLevelHLRT = Double
									.parseDouble(type);
						} catch (NumberFormatException e) {
							System.err
									.println(error
											+ "-h option requires a number (0-1) for the hLRT confidence interval.");
							CommandLineError();
						}
					} else {
						System.err
								.println(error
										+ "-h option requires a number (0-1) for the hLRT confidence interval.");
						CommandLineError();
					}
				}

				else if (arg.equals("-a")) {
					options.doAveragedPhylogeny = true;
				}

				else if (arg.equals("-z")) {
					options.consensusType = "strict";
				}

				else if (arg.equals("-sims")) {
					if (i < arguments.length) {
						options.simulationsName = arguments[i++];
						options.doingSimulations = true;
					} else {
						System.err
								.println(error
										+ "-sims option requires a name for the simulations files");
						CommandLineError();
					}

				} else if (arg.equals("-machinesfile")) {
					if (i < arguments.length) {

						File machinesFile = new File(arguments[i++]);
						if (!(machinesFile.exists() && machinesFile.canRead())) {
							if (MPJ_ME == 0) {
								System.err
										.println(error
												+ "Machines file does not exists or it is not readable");
							}
							CommandLineError();
						}

						boolean hostsError = false;
						try {
							TextInputStream machinesInputStream = new TextInputStream(
									machinesFile.getAbsolutePath());
							String line;

							HOSTS_TABLE = new Hashtable<String, Integer>();
							while ((line = machinesInputStream.readLine()) != null) {
								String hostProcs[] = line.split(":");
								if (hostProcs.length == 2) {
									try {

										int numberOfThreads = Integer
												.parseInt(hostProcs[1]);

										HOSTS_TABLE.put(hostProcs[0],
												new Integer(numberOfThreads));

									} catch (NumberFormatException e) {
										hostsError = true;
										break;
										// Warn and continue with 1 processor
									}
								} else {
									hostsError = true;
								}

								if (hostsError) {
									if (MPJ_ME == 0) {
										System.err.println("");
										System.err
												.println("WARNING: Machines File format is wrong.");
										System.err
												.println("         Each line should have the following format:");
										System.err
												.println("         HOSTNAME:NUMBER_OF_PROCESORS");
										System.err
												.println("Using a single thread");
										System.err.println("");
									}
									HOSTS_TABLE = null;
									options.setNumberOfThreads(1);
								}
							}
							options.setMachinesFile(machinesFile);

						} catch (FileNotFoundException e) {
							System.err
									.println(error
											+ "Machines file does not exists or it is not readable");
							CommandLineError();
						}
					} else {
						System.err.println(error
								+ "-machinesfile option requires a filename");
						CommandLineError();
					}
				} else if (arg.equals("-tr")) {
					if (HOSTS_TABLE != null) {
						String type = arguments[i++];
						System.err
								.println("WARNING: Machines File has been specified. -tr "
										+ type + " argument will be ignored.");
					} else {
						if (i < arguments.length) {
							try {
								String type = arguments[i++];
								options.setNumberOfThreads(Integer
										.parseInt(type));
								options.threadScheduling = true;
							} catch (NumberFormatException e) {
								System.err
										.println(error
												+ "-tr option requires the number of processors to compute.");
								CommandLineError();
							}
						} else {
							System.err
									.println(error
											+ "-tr option requires the number of processors to compute.");
							CommandLineError();
						}
					}
				} else {
					System.err.println(error + "the argument \" " + arg
							+ "\" is unknown. Check its syntax.");
					CommandLineError();
				}

			} // while
			if (!isInputFile) {
				System.err.println(error
						+ "Input File is required (-d argument)");
				CommandLineError();
			}
			if (getPhylip) {
				MAIN_CONSOLE.print("\n\nReading data file \"" + options.getInputFile().getName()
						+ "\"...");

				if (options.getInputFile().exists()) {

					try {
						File outputFile = new File(options.getInputFile().getAbsolutePath() + ".phy");
						ModelTestService.readAlignment(options.getInputFile(),
								outputFile, false);
						MAIN_CONSOLE.println(" OK.");
						MAIN_CONSOLE.println("Result written into " + outputFile.getPath());
						MAIN_CONSOLE.println("");
					} catch (Exception e)// file cannot be read correctly
					{
						System.err.println("\nThe specified file \""
								+ options.getInputFile().getAbsolutePath()
								+ "\" cannot be read as an alignment");
						MAIN_CONSOLE.println(" failed.\n" + e.getMessage());
						throw new InvalidArgumentException.InvalidAlignmentFileException(
								options.getInputFile());
					}
				} else // file does not exist
				{
					System.err.println("\nThe specified file \""
							+ options.getInputFile().getAbsolutePath() + "\" cannot be found");
					throw new InvalidArgumentException.UnexistentAlignmentFileException(
							options.getInputFile());
				}
				finalize(0);
			}
			
			if (ckpFile != null) {
				if (!loadCheckpoint(ckpFile)) {
					System.err.println("\nThe specified checkpoint file \""
							+ options.getInputFile().getAbsolutePath()
							+ "\" cannot be read");
					finalize(0);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add default IC
		options.doBIC |= !(options.doAIC || options.doAICc || options.doDT);
	}

	static public void CommandLineError() {
		System.err.println("\njModelTest has finished with an error status. Please run with -help argument for info about the usage\n");
		System.exit(1);
	}
	
	/****************************
	 * PrintUsage **************************** * Prints command line usage * *
	 ************************************************************************/
	static public void PrintUsage() {
		if (MPJ_ME == 0) {
			System.err.println("\njModelTest command usage");
			System.err.println(
			    "java -jar jModelTest.jar -d sequenceFileName"
			+ "\n                        [-getPhylip]"
			+ "\n                        [-ckp checkpointFileName.ckp]"
			+ "\n                        [-n executionName]"
			+ "\n                        [-t fixed|BIONJ|ML] [-u userTreeFileName] [-o outputFileName]"
			+ "\n                        [-S NNI|SPR|BEST]"
			+ "\n                        [-AIC] [-AICc] [-BIC] [-DT] [-c confidenceInterval]"
			+ "\n                        [-s 3|5|7|11|203]"
			+ "\n                        [-f] [-i] [-g numberOfCategories]"
			+ "\n                        [-uLNL]"
			+ "\n                        [-dLRT] [-h confidenceInterval] [-hLRT] [-O {ftvwxgp}]"
			+ "\n                        [-a] [-z] [-p] [-v] [-w]"
			+ "\n                        [-tr numberOfThreads] [-machinesfile machinesFileName]"
			);
			String usage = 
					  "\n     -a"
					+ "\n         estimate model-averaged phylogeny for each active criterion (e.g., -a) (default is false)"
					+ "\n\n     -AIC"
					+ "\n         calculate the Akaike Information Criterion (e.g., -AIC) (default is false)"
					+ "\n\n     -AICc"
					+ "\n         calculate the corrected Akaike Information Criterion (e.g., -AICc) (default is false)"
					+ "\n\n     -BIC"
					+ "\n         calculate the Bayesian Information Criterion (e.g., -BIC) (default is false)"
					+ "\n\n     -DT"
					+ "\n         calculate the decision theory criterion (e.g., -DT) (default is false)"
					+ "\n\n     -c confidenceInterval"
					+ "\n         confidence interval (e.g., -c 90) (default is 100)"
					+ "\n\n     -ckp checkpointFileName"
					+ "\n         Loads a checkpointing file"
					+ "\n\n     -d sequenceFileName"
					+ "\n         input data file (e.g., -d data.phy)"
					+ "\n\n     -dLRT"
					+ "\n         do dynamical likelihood ratio tests (e.g., -dLRT)(default is false)"
					+ "\n\n     -f"
					+ "\n         include models with unequals base frecuencies (e.g., -f) (default is false)"
					+ "\n\n     -g numberOfCategories"
					+ "\n         include models with rate variation among sites and number of categories (e.g., -g 8) (default is false & 4 categories)"
					+ "\n\n     -G threshold"
					+ "\n         heuristic search. Requires a threshold > 0 (e.g., -G 0.1)"
					+ "\n\n     -getPhylip"
					+ "\n         converts the input file into phylip format"
					+ "\n\n     -h confidenceInterval"
					+ "\n         confidence level for the hLRTs (e.g., -a0.002) (default is 0.01)"
					+ "\n\n     -H informationCriterion"
					+ "\n         information criterion for clustering search (AIC, AICc, BIC). (e.g., -H AIC) (default is BIC)"
					+ "\n\n     -help"
					+ "\n         displays this help message"
					+ "\n\n     -hLRT"
					+ "\n         do hierarchical likelihood ratio tests (default is false)"
					+ "\n\n     -i"
					+ "\n         include models with a proportion invariable sites (e.g., -i) (default is false)"
					+ "\n\n     -machinesfile manchinesFileName"
					+ "\n         gets the processors per host from a machines file"
					+ "\n\n     -n executionName"
					+ "\n         execution name for appending to the log filenames (default: current time yyyyMMddhhmmss)"
					+ "\n\n     -o outputFileName"
					+ "\n         set output file (e.g., -o jmodeltest.out)"
					+ "\n\n     -O hypothesisOrder"
					+ "\n         hypothesis order for the hLRTs (e.g., -hLRT -O gpftv) (default is ftvwxgp)"
					+ "\n            f=freq, t=titvi, v=2ti4tv(subst=3)/2ti(subst>3), w=2tv, x=4tv, g=gamma, p=pinv"
					+ "\n\n     -p"
					+ "\n         calculate parameter importances (e.g., -p) (default is false)"
					+ "\n\n     -r"
					+ "\n         backward selection for the hLRT (e.g., -r) (default is forward)"
					+ "\n\n     -s numberOfSubstitutionSchemes"
					+ "\n         number of substitution schemes (e.g., -s 11) (it has to be 3,5,7,11,203; default is 3)"
					+ "\n\n     -S NNI|SPR|BEST"
					+ "\n         tree topology search operation option (NNI (fast), SPR (a bit slower), BEST (best of NNI and SPR)) (default is BEST)"
					+ "\n\n     -t fixed|BIONJ|ML"
					+ "\n             base tree for likelihood calculations (e.g., -t BIONJ)"
					+ "\n             fixed  (common BIONJ-JC topology)"
					+ "\n             BIONJ  (Neighbor-Joining topology)"
					+ "\n             ML     (Maximum Likelihood topology) (default)"
					+ "\n\n     -tr numberOfThreads"
					+ "\n         number of threads to execute (default is "
					+ Runtime.getRuntime().availableProcessors()
					+ ")"
					+ "\n\n     -u treeFileName"
					+ "\n         user tree for likelihood calculations  (e.g., -u data.tre)"
					+ "\n\n     -uLnL"
					+ "\n         calculate delta AIC,AICc,BIC against unconstrained likelihood (e.g., -uLnL)"
					+ "\n\n        (default is false if the input alignment has gaps or ambiguous characters)"
					+ "\n\n     -v"
					+ "\n         do model averaging and parameter importances (e.g., -v) (default is false)"
					+ "\n\n     -w"
					+ "\n         write PAUP block (e.g., -w) (default is false)"
					+ "\n\n     -z"
					+ "\n         strict consensus type for model-averaged phylogeny (e.g., -z) (default is majority rule)"
					+ "\n\n Command line: java -jar jModeltest.jar -d sequenceFileName [arguments]";
			System.err.println(usage);
			System.err.println(" ");
		}
		System.exit(0);
	}

	/****************************
	 * printHeader ****************************** * Prints header information at
	 * start * * *
	 ***********************************************************************/

	static public void printHeader(TextOutputStream stream) {
		// we can set styles using the editor pane
		// I am using doc to stream ....
		stream.print("------------------------------- ");
		stream.print("jModeltest " + CURRENT_VERSION);
		stream.println(" -------------------------------");
		stream.println("(c) 2011-onwards D. Darriba, G.L. Taboada, R. Doallo and D. Posada,");
		stream.println("(1) Department of Biochemistry, Genetics and Immunology");
		stream.println("    University of Vigo, 36310 Vigo, Spain.");
		stream.println("(2) Department of Electronics and Systems");
		stream.println("    University of A Coruna, 15071 A Coruna, Spain.");
		stream.println("e-mail: ddarriba@udc.es, dposada@uvigo.es");
		stream.println("--------------------------------------------------------------------------------");
		stream.println(" ");
		java.util.Date current_time = new java.util.Date();
		stream.println(current_time.toString());
		stream.println(System.getProperty("os.name") + " "
				+ System.getProperty("os.version") + ", arch: "
				+ System.getProperty("os.arch") + ", bits: "
				+ System.getProperty("sun.arch.data.model") + ", numcores: "
				+ Runtime.getRuntime().availableProcessors());
		stream.println(" ");
	}

	/****************************
	 * printNotice ****************************** * Prints notice information at
	 * start up * * *
	 ***********************************************************************/

	static public void printNotice(TextOutputStream stream) {
		// stream.println("\n******************************* NOTICE ************************************");
		stream.println("jModelTest " + CURRENT_VERSION);
		stream.println("Copyright (C) 2011 D. Darriba, G.L. Taboada, R. Doallo and D. Posada");
		stream.println("This program comes with ABSOLUTELY NO WARRANTY");
		stream.println("This is free software, and you are welcome to redistribute it under certain conditions");
		stream.println(" ");
		stream.println("Notice: This program may contain errors. Please inspect results carefully.");
		stream.println(" ");
		// stream.println("***************************************************************************\n");

	}

	/****************************
	 * printCitation ****************************** * Prints citation
	 * information at start up * * *
	 ***********************************************************************/

	static public void printCitation(TextOutputStream stream) {
		// stream.println("\n******************************* CITATION *********************************");
		stream.println("--------------------------------------------------------------------------------");
		stream.println("Citation: Darriba D, Taboada GL, Doallo R and Posada D. 2012.");
		stream.println("          \"jModelTest 2: more models, new heuristics and parallel computing\".");
		stream.println("          Nature Methods 9(8), 772.");
		stream.println("--------------------------------------------------------------------------------");
		stream.println(" ");
		// stream.println("***************************************************************************\n");

	}

	/****************************
	 * writePaupBlockk *************************** * Prints a block of PAUP
	 * commands for the best model * *
	 ************************************************************************/
	static public void WritePaupBlock(TextOutputStream stream,
			String criterion, Model model) {
		try {
			stream.println("\n--\nPAUP* Commands Block:");
			stream.println(" If you want to load the selected model and associated estimates in PAUP*,");
			stream.println(" attach the next block of commands after the data in your PAUP file:");
			stream.print("\n[!\nLikelihood settings from best-fit model (");
			stream.printf("%s", model.getName());
			stream.print(") selected by ");
			stream.printf("%s", criterion);
			stream.print("\nwith ");
			stream.printf("%s", programName);
			stream.print(" ");
			stream.printf("%s", CURRENT_VERSION);
			stream.print(" on ");
			java.util.Date current_time = new java.util.Date();
			stream.print(current_time.toString());
			stream.println("]");

			stream.print("\nBEGIN PAUP;");
			stream.print("\nLset");

			/* base frequencies */
			stream.print(" base=");
			if (model.ispF()) {
				stream.print("(");
				stream.printf("%.4f ", model.getfA());
				stream.printf("%.4f ", model.getfC());
				stream.printf("%.4f ", model.getfG());
				/* stream.printf("%.4f",model.fT); */
				stream.print(")");
			} else
				stream.print("equal");

			/* substitution rates */
			if (!model.ispT() && !model.ispR())
				stream.print(" nst=1");
			else if (model.ispT()) {
				stream.print(" nst=2 tratio=");
				stream.printf("%.4f", model.getTitv());
			} else if (model.ispR()) {
				stream.print(" nst=6  rmat=(");
				stream.printf("%.4f ", model.getRa());
				stream.printf("%.4f ", model.getRb());
				stream.printf("%.4f ", model.getRc());
				stream.printf("%.4f ", model.getRd());
				stream.printf("%.4f)", model.getRe());
				/* stream.print("1.0000)"); */
			}

			/* site rate variation */
			stream.print(" rates=");
			if (model.ispG()) {
				stream.print("gamma shape=");
				stream.printf("%.4f", model.getShape());
				stream.print(" ncat=");
				stream.printf("%d", model.getNumGammaCat());
			} else
				stream.print("equal");

			/* invariable sites */
			stream.print(" pinvar=");
			if (model.ispI())
				stream.printf("%.4f", model.getPinv());
			else
				stream.print("0");

			stream.print(";\nEND;");
			stream.print("\n--\n");
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkInputFiles() {
		// open data file
		File inputFile = options.getInputFile();
		MAIN_CONSOLE.print("\n\nReading data file \"" + inputFile.getName()
				+ "\"...");

		if (inputFile.exists()) {

			try {
				
				ModelTestService.readAlignment(inputFile,
						options.getAlignmentFile());
				
				options.setAlignment(AlignmentReader.readAlignment(
						new PrintWriter(System.err), options.getAlignmentFile()
								.getAbsolutePath(), true)); // file

				MAIN_CONSOLE.println(" OK.");
				MAIN_CONSOLE.println("  number of sequences: "
						+ options.getNumTaxa());
				MAIN_CONSOLE.println("  number of sites: "
						+ options.getNumSites());
			} catch (Exception e)// file cannot be read correctly
			{
				System.err.println("\nThe specified file \""
						+ inputFile.getAbsolutePath()
						+ "\" cannot be read as an alignment");
				MAIN_CONSOLE.println(" failed.\n" + e.getMessage());
				throw new InvalidArgumentException.InvalidAlignmentFileException(
						inputFile);
			}
		} else // file does not exist
		{
			System.err.println("\nThe specified file \""
					+ inputFile.getAbsolutePath() + "\" cannot be found");
			MAIN_CONSOLE.println(" failed.\n");
			throw new InvalidArgumentException.UnexistentAlignmentFileException(
					inputFile);
		}

		// open tree file if necessary
		if (options.userTopologyExists) {
			File treefile = options.getInputTreeFile();
			MAIN_CONSOLE.print("Reading tree file \"" + treefile.getName()
					+ "\"...");

			// read the tree in
			Tree tree = null;
			try {
				tree = TreeUtilities.readTree(treefile.getAbsolutePath());
			} catch (IOException e) {
				System.err.println("\nThe specified tree file \""
						+ treefile.getName() + "\" cannot be found");
				MAIN_CONSOLE.println(" failed.\n");
				throw new InvalidArgumentException.UnexistentTreeFileException(
						treefile.getAbsolutePath());
			} catch (TreeParseException e) {
				System.err.println("\nCannot parse tree file \""
						+ treefile.getName() + "\"");
				MAIN_CONSOLE.println(" failed.\n");
				throw new InvalidArgumentException.InvalidTreeFileException(
						treefile.getAbsolutePath());
			}
			if (tree != null) {
				options.setUserTree(TreeUtilities.toNewick(tree, true, false,
						false));
				TextOutputStream out = new TextOutputStream(options
						.getTreeFile().getAbsolutePath());
				out.print(options.getUserTree());
				out.close();
				MAIN_CONSOLE.println(" OK.");
			} else // tree is not valid
			{
				System.err.println("\nUnexpected error parsing \""
						+ treefile.getName() + "\"");
				MAIN_CONSOLE.println(" failed.\n");
				throw new InvalidArgumentException.InvalidTreeFileException(
						treefile.getAbsolutePath());
			}

		}
	}

	public static TextOutputStream setMainConsole(TextOutputStream mainConsole) {
		ModelTest.MAIN_CONSOLE = mainConsole;
		return mainConsole;
	}

	public static TextOutputStream getMainConsole() {
		return MAIN_CONSOLE;
	}

	public static TextOutputStream setPhymlConsole(TextOutputStream phymlConsole) {
		ModelTest.PHYML_CONSOLE = phymlConsole;
		return phymlConsole;
	}

	public static TextOutputStream getPhymlConsole() {
		return PHYML_CONSOLE;
	}
	
	public static TextOutputStream getCurrentOutStream() {
		return CURRENT_OUT_STREAM;
	}

	public static void setCurrentOutStream(TextOutputStream currentOutStream) {
		CURRENT_OUT_STREAM = currentOutStream;
	}

	public static AIC getMyAIC() {
		if (!AICwasCalculated)
			throw new WeakStateException.UninitializedCriterionException("AIC");
		return myAIC;
	}

	public static void setMyAIC(AIC myAIC) {
		ModelTest.myAIC = myAIC;
		ModelTest.minAIC = myAIC != null ? myAIC.getMinModel() : null;
		AICwasCalculated = (myAIC != null);
	}

	public static boolean testAIC() {
		return AICwasCalculated;
	}

	public static AICc getMyAICc() {
		if (!AICcwasCalculated)
			throw new WeakStateException.UninitializedCriterionException("AICc");
		return myAICc;
	}

	public static void setMyAICc(AICc myAICc) {
		ModelTest.myAICc = myAICc;
		ModelTest.minAICc = myAICc != null ? myAICc.getMinModel() : null;
		AICcwasCalculated = (myAICc != null);
	}

	public static boolean testAICc() {
		return AICcwasCalculated;
	}

	public static BIC getMyBIC() {
		if (!BICwasCalculated)
			throw new WeakStateException.UninitializedCriterionException("BIC");
		return myBIC;
	}

	public static void setMyBIC(BIC myBIC) {
		ModelTest.myBIC = myBIC;
		ModelTest.minBIC = myBIC != null ? myBIC.getMinModel() : null;
		BICwasCalculated = (myBIC != null);
	}

	public static boolean testBIC() {
		return BICwasCalculated;
	}

	public static DT getMyDT() {
		if (!DTwasCalculated)
			throw new WeakStateException.UninitializedCriterionException("DT");
		return myDT;
	}

	public static void setMyDT(DT myDT) {
		ModelTest.myDT = myDT;
		ModelTest.minDT = myDT != null ? myDT.getMinModel() : null;
		DTwasCalculated = (myDT != null);
	}

	public static boolean testDT() {
		return DTwasCalculated;
	}

	public static RunConsense getConsensusAIC() {
		return consensusAIC;
	}

	public static RunConsense getConsensusAICc() {
		return consensusAICc;
	}

	public static RunConsense getConsensusBIC() {
		return consensusBIC;
	}

	public static RunConsense getConsensusDT() {
		return consensusDT;
	}

	public static void setConsensusAIC(RunConsense pConsensusAIC) {
		consensusAIC = pConsensusAIC;
	}

	public static void setConsensusAICc(RunConsense pConsensusAICc) {
		consensusAICc = pConsensusAICc;
	}

	public static void setConsensusBIC(RunConsense pConsensusBIC) {
		consensusBIC = pConsensusBIC;
	}

	public static void setConsensusDT(RunConsense pConsensusDT) {
		consensusDT = pConsensusDT;
	}

	/**
	 * Finalizes the MPJ runtime environment. When an error occurs, it aborts
	 * the execution of every other processes.
	 * 
	 * @param status
	 *            the finalization status
	 */
	public static void finalize(int status) {

		if (status != 0) {
			if (MPJ_RUN) {
				MPI.COMM_WORLD.Abort(status);
			}
		}

		if (MPJ_RUN) {
			MPI.Finalize();
		}

		System.exit(status);

	}

	/**
	 * @param minDLRT
	 *            the minDLRT to set
	 */
	public static void setMinDLRT(Model minDLRT) {
		ModelTest.minDLRT = minDLRT;
	}

	/**
	 * @return the minDLRT
	 */
	public static Model getMinDLRT() {
		return minDLRT;
	}

	/**
	 * @param minHLRT
	 *            the minHLRT to set
	 */
	public static void setMinHLRT(Model minHLRT) {
		ModelTest.minHLRT = minHLRT;
	}

	/**
	 * @return the minHLRT
	 */
	public static Model getMinHLRT() {
		return minHLRT;
	}

	/**
	 * @return the minDT
	 */
	public static Model getMinDT() {
		return minDT;
	}

	/**
	 * @return the minBIC
	 */
	public static Model getMinBIC() {
		return minBIC;
	}

	/**
	 * @return the minAICc
	 */
	public static Model getMinAICc() {
		return minAICc;
	}

	/**
	 * @return the minAIC
	 */
	public static Model getMinAIC() {
		return minAIC;
	}

	/**
	 * @param candidateModels
	 *            the candidateModels to set
	 */
	public static void setCandidateModels(Model[] candidateModels) {
		ApplicationOptions.getInstance().setNumModels(candidateModels.length);
		ModelTest.candidateModels = candidateModels;
	}

	/**
	 * @return the loadedModels
	 */
	public static Model[] getLoadedModels() {
		return loadedModels;
	}
	
	/**
	 * @return the candidateModels
	 */
	public static Model[] getCandidateModels() {
		return candidateModels;
	}

	/**
	 * @return a single candidate model
	 */
	public static Model getCandidateModel(int index) {
		return candidateModels[index];
	}

	public static String getHostname() {
		return hostname;
	}

	public static void purgeModels() {
		List<Model> modelList = new ArrayList<Model>();
		for (Model model : candidateModels) {
			if (model.getLnL() > 0) {
				modelList.add(model);
			}
		}
		candidateModels = modelList.toArray(new Model[0]);
		ApplicationOptions.getInstance().setNumModels(candidateModels.length);
	}

	public class NullPrinter extends OutputStream {

		@Override
		public void write(int arg0) throws IOException {
			// DO NOTHING

		}

	}

} // class ModelTest

