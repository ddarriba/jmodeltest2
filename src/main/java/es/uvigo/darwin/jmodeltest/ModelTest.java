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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Observer;
import java.util.Vector;

import mpi.MPI;
import mpi.MPIException;
import pal.alignment.Alignment;
import pal.tree.Tree;
import pal.tree.TreeParseException;
import es.uvigo.darwin.jmodeltest.exe.RunConsense;
import es.uvigo.darwin.jmodeltest.exe.RunPhyml;
import es.uvigo.darwin.jmodeltest.exe.RunPhymlHybrid;
import es.uvigo.darwin.jmodeltest.exe.RunPhymlMPJ;
import es.uvigo.darwin.jmodeltest.exe.RunPhymlQueue;
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
import es.uvigo.darwin.jmodeltest.tree.TreeUtilities;
import es.uvigo.darwin.jmodeltest.utilities.Simulation;
import es.uvigo.darwin.prottest.util.exception.ProtTestInternalException;
import es.uvigo.darwin.prottest.util.fileio.AlignmentReader;
import es.uvigo.darwin.prottest.util.logging.ProtTestLogger;

/**
 * ModelTest.java
 * 
 * Description: Main class for selecting models of nucleotide substitition
 * 
 * @author Diego Darriba, University of Vigo / University of A Coru�a, Spain
 *         ddarriba@udc.es
 * @author David Posada, University of Vigo, Spain dposada@uvigo.es |
 *         darwin.uvigo.es
 * @version 2.0.2 (Feb 2012)
 */
public class ModelTest {

	protected ApplicationOptions options;

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
	public static final String CURRENT_VERSION = "2.0.2";
	public static final String programName = ("jModeltest");
	public static final String URL = "http://code.google.com/p/jmodeltest2";
	public static final String WIKI = "http://code.google.com/p/jmodeltest2/wiki/GettingStarted";
	public static final String DISCUSSION_GROUP = "http://groups.google.com/group/jmodeltest";
	
	public static final String UNKNOWN_HOSTNAME = "UNKNOWN";

	public static final String DEFAULT_CONFIG_FILE = "conf/jmodeltest.conf";
	public String CONFIG_FILE;
	
	private TextOutputStream MAIN_CONSOLE = new TextOutputStream(System.out);
	private TextOutputStream CURRENT_OUT_STREAM;

	public String[] arguments;

	private boolean AICwasCalculated = false;
	private boolean AICcwasCalculated = false;
	private boolean BICwasCalculated = false;
	private boolean DTwasCalculated = false;

	public Vector<String> testingOrder; // order of the hLRTs
	public String averagedTreeString; // model-averaged phylogeny in
												// Newick format

	private AIC myAIC;
	private AICc myAICc;
	private BIC myBIC;
	private DT myDT;
	private HLRT myHLRT;

	private RunConsense consensusAIC;
	private RunConsense consensusAICc;
	private RunConsense consensusBIC;
	private RunConsense consensusDT;

	private Model[] candidateModels;
	private Model minAIC, minAICc, minBIC, minDT, minHLRT, minDLRT;

	private static String hostname;
	public Hashtable<String, Integer> HOSTS_TABLE;

	// We can work under a GUI or in the command line
	public boolean buildGUI = true;

	protected RunPhyml runPhyml;

	protected boolean runInQueue = false;
	protected File outputFile = null;
	protected Observer progressObserver = null;
	protected Long jobId;
	public ProtTestLogger logger = new ProtTestLogger(String.valueOf(System.currentTimeMillis()));
	
	static 
	{
		InetAddress addr;
		try 
		{
			addr = InetAddress.getLocalHost();
			// Get hostname
			hostname = addr.getHostName();
		}
		catch (UnknownHostException e) 
		{
			hostname = UNKNOWN_HOSTNAME;
			System.err.println("WARNING: This host is unknown");
			// WARN AND DO NOTHING
		}
	}

	public ModelTest(boolean buildGUI, Observer progressObserver, String configFile) 
	{
		this.buildGUI = buildGUI;
		options = new ApplicationOptions(this);
		CONFIG_FILE = configFile;
		
		ModelTestConfiguration.setConfigFile(CONFIG_FILE);

		this.progressObserver = progressObserver;
	}

	public ModelTest(String[] args, boolean buildGUI, String configFile) 
	{
		this(buildGUI, null, configFile);
		
		if (buildGUI)
		{
			XManager.getInstance(this);	
		}
		else
		{
			try 
			{
				arguments = args;
				ParseArguments();
				if (options.doingSimulations) 
				{
					Simulation sim = new Simulation(this);
					sim.run();
				} 
				else
				{
					checkFilesAndBuildSetOfModels();
					runCommandLine();	
					endCommandLine();
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			finalize(0);
		}
	}

	/****************************
	 * main ************************************ * Starts the application * * *
	 ***********************************************************************/

	public static void main(String[] args) 
	{
		// initializing MPJ environment (if available)
		System.err.println("[MPI] Testing MPI environment... (" + hostname + ")");
		try 
		{
			args = MPI.Init(args);
			System.err.println("[MPI] ... OK! ["+hostname+" ("+MPJ_ME+")]");
			MPJ_ME = MPI.COMM_WORLD.Rank();
			MPJ_SIZE = MPI.COMM_WORLD.Size();
			MPJ_RUN = true;
		}
		catch (MPIException e) 
		{
			System.err.println("[MPI] Proceed without MPI");
			MPJ_ME = 0;
			MPJ_SIZE = 1;
			MPJ_RUN = false;
		}
		catch (Exception e) 
		{
			System.err.println("[MPI] Proceed without MPI");
			MPJ_ME = 0;
			MPJ_SIZE = 1;
			MPJ_RUN = false;
		}
		catch (ExceptionInInitializerError e) 
		{
			System.err.println("[MPI] Initializer error!");
			System.err.println(e.getMessage());
			System.exit(-1);
			MPJ_ME = 0;
			MPJ_SIZE = 1;
			MPJ_RUN = false;
		}
		catch (NoClassDefFoundError e) 
		{
			System.err.println("[MPI] Proceed without MPI");
			MPJ_ME = 0;
			MPJ_SIZE = 1;
			MPJ_RUN = false;
		}

		ModelTest modelTest = new ModelTest(args, (args.length < 1), DEFAULT_CONFIG_FILE);
		
		modelTest.getApplicationOptions().getLogFile().delete();
	}

	public ApplicationOptions getApplicationOptions()
	{
		return options;
	}
	
	protected void checkFilesAndBuildSetOfModels()
	{
		if (MPJ_ME == 0) 
		{
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

			try 
			{
				checkInputFiles();
			} 
			catch (InvalidArgumentException.InvalidInputFileException e) 
			{
				MAIN_CONSOLE.println(e.getMessage());
				
				if (runInQueue)
				{
					throw new ProtTestInternalException(e.getMessage());
				}
				
				finalize(-1);
			}

			// calculate number of models
			if (options.getSubstTypeCode() == 0)
				options.numModels = 3;
			else if (options.getSubstTypeCode() == 1)
				options.numModels = 5;
			else if (options.getSubstTypeCode() == 2)
				options.numModels = 7;
			else
				options.numModels = 11;

			if (options.doF)
				options.numModels *= 2;

			if (options.doI && options.doG)
				options.numModels *= 4;
			else if (options.doI || options.doG)
				options.numModels *= 2;
			options.setCandidateModels();
		}
		// build set of models
	}
	
	/****************************
	 * runCommandLine ************************** * Organizes all the tasks that
	 * the program needs to carry out * * *
	 ***********************************************************************/
	public void runCommandLine() 
	{
		// calculate likelihoods with phyml in the command line
		
		if (runInQueue)
		{
			runPhyml = new RunPhymlQueue(progressObserver, this, getCandidateModels(), jobId);
		}
		else
		{
			progressObserver = new ConsoleProgressObserver(this);
			
			if (MPJ_RUN) 
			{		
				if (options.threadScheduling && options.getNumberOfThreads() > 0) 
				{
					runPhyml = new RunPhymlHybrid(MPJ_ME, MPJ_SIZE,	progressObserver, this,	getCandidateModels(), options.getNumberOfThreads());
				}
				else 
				{
					runPhyml = new RunPhymlMPJ(progressObserver, this, getCandidateModels());
				}
			}
			else 
			{
				runPhyml = new RunPhymlThread(progressObserver,	this, getCandidateModels());
			}
		}
		runPhyml.execute();
	}
	
	public void endCommandLine() 
	{
		if (MPJ_ME == 0) 
		{
			// do AIC if selected
			if (options.doAIC) 
			{
				myAIC = new AIC(options.writePAUPblock, options.doImportances, options.doModelAveraging, options.confidenceInterval, this);
				myAIC.compute();
				minAIC = myAIC.getMinModel();
				AICwasCalculated = true;
				myAIC.print(MAIN_CONSOLE);
				if (options.doAveragedPhylogeny) 
				{
					consensusAIC = new RunConsense(myAIC, options.consensusType, options.confidenceInterval, this);
				}
			}

			// do AICc if selected
			if (options.doAICc) 
			{
				myAICc = new AICc(options.writePAUPblock, options.doImportances, options.doModelAveraging, options.confidenceInterval, this);
				myAICc.compute();
				minAICc = myAICc.getMinModel();
				AICcwasCalculated = true;
				myAICc.print(MAIN_CONSOLE);
				if (options.doAveragedPhylogeny) 
				{
					consensusAICc = new RunConsense(myAICc,	options.consensusType, options.confidenceInterval, this);
				}
			}

			// do BIC if selected
			if (options.doBIC) 
			{
				myBIC = new BIC(options.writePAUPblock, options.doImportances, options.doModelAveraging, options.confidenceInterval, this);
				myBIC.compute();
				minBIC = myBIC.getMinModel();
				BICwasCalculated = true;
				myBIC.print(MAIN_CONSOLE);
				if (options.doAveragedPhylogeny) 
				{
					consensusBIC = new RunConsense(myBIC, options.consensusType, options.confidenceInterval, this);
				}
			}

			// do DT if selected
			if (options.doDT) 
			{
				myDT = new DT(options.writePAUPblock, options.doImportances, options.doModelAveraging, options.confidenceInterval, this);
				myDT.compute();
				minDT = myDT.getMinModel();
				DTwasCalculated = true;
				myDT.print(MAIN_CONSOLE);
				if (options.doAveragedPhylogeny) 
				{
					consensusDT = new RunConsense(myDT, options.consensusType, options.confidenceInterval, this);
				}
			}

			// do hLRT if selected
			if (options.doHLRT) 
			{
				myHLRT = new HLRT(this);
				myHLRT.compute(!options.backwardHLRTSelection, options.confidenceLevelHLRT, options.writePAUPblock);
			}

			// do dLRT if selected
			if (options.doDLRT) 
			{
				myHLRT = new HLRT(this);
				myHLRT.computeDynamical(!options.backwardHLRTSelection, options.confidenceLevelHLRT, options.writePAUPblock);
			}

			if (ModelTestConfiguration.isAutoLogEnabled()) 
			{
				HtmlReporter.buildReport(this, getCandidateModels(), outputFile);
			}

			MAIN_CONSOLE.println(" ");
			MAIN_CONSOLE.println("Program is done.");
			MAIN_CONSOLE.println(" ");MAIN_CONSOLE.println(" ");
			MAIN_CONSOLE.println("::Selection Summary::");
			MAIN_CONSOLE.println(" ");
			if (myAIC == null && myAICc == null && myBIC == null && myDT == null) 
			{
				MAIN_CONSOLE.println("No information criterion was selected.");
			}
			
			MAIN_CONSOLE.println("\tModel \t\tf(a) \tf(c) \tf(g) \tf(t) \tkappa \ttitv " +
			"\tRa \tRb \tRc \tRd \tRe \tRf \tpInv \tgamma");
			MAIN_CONSOLE.println("----------------------------------------------------------------------------------------------------------------------------------------");
			if (myAIC != null) 
			{
//				MAIN_CONSOLE.println("BestAICmodelAVG "
//						+ myAIC.getMinModel().getName() + " " + Utilities.RoundDoubleTo(myAIC.getAfA(),4)
//						+ " " + myAIC.getAfC() + " " + myAIC.getAfG() + " "
//						+ myAIC.getAfT() + " " + myAIC.getAkappa() + " "
//						+ myAIC.getAtitv() + " " + myAIC.getaRa() + " "
//						+ myAIC.getaRb() + " " + myAIC.getaRc() + " "
//						+ myAIC.getaRd() + " " + myAIC.getaRe() + " "
//						+ myAIC.getaRf() + " " + myAIC.getApinvI() + " "
//						+ myAIC.getApinvIG() + " " + myAIC.getAshapeG() + " "
//						+ myAIC.getAshapeIG() + " "
//						+ myAIC.getMinModel().getPartition());
				Model minModel = myAIC.getMinModel();
				MAIN_CONSOLE.print("AIC \t" + minModel.getName() + "\t");
				if (minModel.getName().length() < 8)
					System.err.print("\t");
				MAIN_CONSOLE.print(minModel.getfA()
						+ "\t" + minModel.getfC() + "\t" + minModel.getfG() + "\t"
						+ minModel.getfT() + "\t" + minModel.getKappa() + "\t"
						+ minModel.getTitv() + "\t" + minModel.getRa() + "\t"
						+ minModel.getRb() + "\t" + minModel.getRc() + "\t"
						+ minModel.getRd() + "\t" + minModel.getRe() + "\t"
						+ minModel.getRf() + "\t");
				if (minModel.ispI()) 
				{
					MAIN_CONSOLE.print(minModel.getPinv());
				}
				else 
				{
					MAIN_CONSOLE.print("N/A");
				}
				MAIN_CONSOLE.print("\t");
				if (minModel.ispG()) 
				{
					MAIN_CONSOLE.print(minModel.getShape());
				}
				else 
				{
					MAIN_CONSOLE.print("N/A");
				}
				MAIN_CONSOLE.print("\n");
			}
			
			if (myBIC != null) 
			{
//				System.err.println("BestBICmodelAVG "
//						+ myBIC.getMinModel().getName() + " " + myBIC.getAfA()
//						+ " " + myBIC.getAfC() + " " + myBIC.getAfG() + " "
//						+ myBIC.getAfT() + " " + myBIC.getAkappa() + " "
//						+ myBIC.getAtitv() + " " + myBIC.getaRa() + " "
//						+ myBIC.getaRb() + " " + myBIC.getaRc() + " "
//						+ myBIC.getaRd() + " " + myBIC.getaRe() + " "
//						+ myBIC.getaRf() + " " + myBIC.getApinvI() + " "
//						+ myBIC.getApinvIG() + " " + myBIC.getAshapeG() + " "
//						+ myBIC.getAshapeIG() + " "
//						+ myBIC.getMinModel().getPartition());
				Model minModel = myBIC.getMinModel();
				MAIN_CONSOLE.print("BIC \t" +
						  minModel.getName() + "\t");
				if (minModel.getName().length() < 8)
					System.err.print("\t");
				MAIN_CONSOLE.print(minModel.getfA()
						+ "\t" + minModel.getfC() + "\t" + minModel.getfG() + "\t"
						+ minModel.getfT() + "\t" + minModel.getKappa() + "\t"
						+ minModel.getTitv() + "\t" + minModel.getRa() + "\t"
						+ minModel.getRb() + "\t" + minModel.getRc() + "\t"
						+ minModel.getRd() + "\t" + minModel.getRe() + "\t"
						+ minModel.getRf() + "\t");
				if (minModel.ispI()) 
				{
					MAIN_CONSOLE.print(minModel.getPinv());
				}
				else 
				{
					MAIN_CONSOLE.print("N/A");
				}
				MAIN_CONSOLE.print("\t");
				if (minModel.ispG()) 
				{
					MAIN_CONSOLE.print(minModel.getShape());
				}
				else 
				{
					MAIN_CONSOLE.print("N/A");
				}
				MAIN_CONSOLE.print("\n");
			}
			
			if (myAICc != null) 
			{
//				System.err.println("BestAICcmodelAVG "
//						+ myAICc.getMinModel().getName() + " "
//						+ myAICc.getAfA() + " " + myAICc.getAfC() + " "
//						+ myAICc.getAfG() + " " + myAICc.getAfT() + " "
//						+ myAICc.getAkappa() + " " + myAICc.getAtitv() + " "
//						+ myAICc.getaRa() + " " + myAICc.getaRb() + " "
//						+ myAICc.getaRc() + " " + myAICc.getaRd() + " "
//						+ myAICc.getaRe() + " " + myAICc.getaRf() + " "
//						+ myAICc.getApinvI() + " " + myAICc.getApinvIG() + " "
//						+ myAICc.getAshapeG() + " " + myAICc.getAshapeIG()
//						+ " " + myAICc.getMinModel().getPartition());
				Model minModel = myAICc.getMinModel();
				MAIN_CONSOLE.print("AICc \t" +
						  minModel.getName() + "\t");
				if (minModel.getName().length() < 8)
					System.err.print("\t");
				MAIN_CONSOLE.print(minModel.getfA()
						+ "\t" + minModel.getfC() + "\t" + minModel.getfG() + "\t"
						+ minModel.getfT() + "\t" + minModel.getKappa() + "\t"
						+ minModel.getTitv() + "\t" + minModel.getRa() + "\t"
						+ minModel.getRb() + "\t" + minModel.getRc() + "\t"
						+ minModel.getRd() + "\t" + minModel.getRe() + "\t"
						+ minModel.getRf() + "\t");
				if (minModel.ispI()) 
				{
					MAIN_CONSOLE.print(minModel.getPinv());
				}
				else 
				{
					MAIN_CONSOLE.print("N/A");
				}
				MAIN_CONSOLE.print("\t");
				if (minModel.ispG()) 
				{
					MAIN_CONSOLE.print(minModel.getShape());
				}
				else 
				{
					MAIN_CONSOLE.print("N/A");
				}
				MAIN_CONSOLE.print("\n");
			}
			
			if (myDT != null) 
			{
//				System.err.println("BestDTmodelAVG "
//						+ myDT.getMinModel().getName() + " " + myDT.getAfA()
//						+ " " + myDT.getAfC() + " " + myDT.getAfG() + " "
//						+ myDT.getAfT() + " " + myDT.getAkappa() + " "
//						+ myDT.getAtitv() + " " + myDT.getaRa() + " "
//						+ myDT.getaRb() + " " + myDT.getaRc() + " "
//						+ myDT.getaRd() + " " + myDT.getaRe() + " "
//						+ myDT.getaRf() + " " + myDT.getApinvI() + " "
//						+ myDT.getApinvIG() + " " + myDT.getAshapeG() + " "
//						+ myDT.getAshapeIG() + " "
//						+ myDT.getMinModel().getPartition());
				Model minModel = myDT.getMinModel();
				MAIN_CONSOLE.print("DT \t" +
						  minModel.getName() + "\t");
				if (minModel.getName().length() < 8)
					System.err.print("\t");
				MAIN_CONSOLE.print(minModel.getfA()
						+ "\t" + minModel.getfC() + "\t" + minModel.getfG() + "\t"
						+ minModel.getfT() + "\t" + minModel.getKappa() + "\t"
						+ minModel.getTitv() + "\t" + minModel.getRa() + "\t"
						+ minModel.getRb() + "\t" + minModel.getRc() + "\t"
						+ minModel.getRd() + "\t" + minModel.getRe() + "\t"
						+ minModel.getRf() + "\t");
				if (minModel.ispI()) 
				{
					MAIN_CONSOLE.print(minModel.getPinv());
				}
				else 
				{
					MAIN_CONSOLE.print("N/A");
				}
				MAIN_CONSOLE.print("\t");
				if (minModel.ispG()) 
				{
					MAIN_CONSOLE.print(minModel.getShape());
				}
				else 
				{
					MAIN_CONSOLE.print("N/A");
				}
				MAIN_CONSOLE.print("\n");
			}
		} // end root

	} // end of runCommandLine

	/****************************
	 * ParseArguments **************************** * Parses the command line for
	 * jModeltest * *
	 ************************************************************************/

	public void ParseArguments() 
	{
		int i, j;
		String arg = "";
		String error = "\nCOMMAND LINE ERROR: ";
		boolean isInputFile = false;
		try 
		{
			i = 0;
			while (i < arguments.length) 
			{
				if (!arguments[i].startsWith("-")) 
				{
					System.err.println(error + "Arguments must start with \"-\". The ofending argument was: " + arguments[i] + ".");
					System.err.print("      Arguments: ");
					for (String argument : arguments) 
					{
						System.err.print(argument + " ");
					}
					System.err.println("");

					PrintUsage();
					System.exit(1);
				}

				arg = arguments[i++];

				if (arg.equals("-d")) 
				{
					if (i < arguments.length) 
					{
						options.setInputFile(new File(arguments[i++]));
						isInputFile = true;
					}
					else 
					{
						System.err.println(error + "-d option requires an input filename.");
						PrintUsage();
					}
				}
				
				else if (arg.equals("-s")) 
				{
					if (i < arguments.length) 
					{
						String type = arguments[i++];
						try 
						{
							int number = Integer.parseInt(type);
							switch (number) 
							{
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
								default:
									System.err.println(error + "-s substitution types have to be 3,5,7,11 only.");
									PrintUsage();
							}
						} 
						catch (NumberFormatException e) 
						{
							System.err.println(error + "-s option requires a number for the substitution types: 3,5,7,11.");
							PrintUsage();
						}
					} 
					else 
					{
						System.err.println(error + "-s option requires a number for the substitution types: 3,5,7,11.");
						PrintUsage();
					}
				}

				else if (arg.equals("-f")) 
				{
					options.doF = true;
				}

				else if (arg.equals("-i")) 
				{
					options.doI = true;
				}

				else if (arg.equals("-g")) 
				{
					if (i < arguments.length) 
					{
						try 
						{
							options.doG = true;
							String type = arguments[i++];
							options.numGammaCat = Integer.parseInt(type);
						}
						catch (NumberFormatException e) 
						{
							System.err.println(error + "-g option requires a number of gamma categories.");
							PrintUsage();
						}
					}
					else 
					{
						System.err.println(error + "-g option requires a number of gamma categories.");
						PrintUsage();
					}
				}

				else if (arg.equals("-n")) 
				{
					if (i < arguments.length) 
					{
						try 
						{
							String sampleSize = arguments[i++];
							options.sampleSize = Integer.parseInt(sampleSize);
						}
						catch (NumberFormatException e) 
						{
							System.err.println(error + "-n option requires a sample size.");
							PrintUsage();
						}
					} 
					else 
					{
						System.err.println(error + "-n option requires a sample size.");
						PrintUsage();
					}
				} 
				else if (arg.equals("-t")) 
				{
					if (i < arguments.length) 
					{
						String type = arguments[i++];

						if (type.equalsIgnoreCase("fixed")) 
						{
							options.fixedTopology = true;
							options.optimizeMLTopology = false;
							options.userTopologyExists = false;
						}
						else if (type.equalsIgnoreCase("BIONJ")) 
						{
							options.fixedTopology = false;
							options.optimizeMLTopology = false;
							options.userTopologyExists = false;
						}
						else if (type.equalsIgnoreCase("ML")) 
						{
							options.fixedTopology = false;
							options.optimizeMLTopology = true;
							options.userTopologyExists = false;
						}
						else 
						{
							System.err.println(error + "-t option requires a type of base tree for likelihod calculations: " + "\"fixed\", \"BIONJ\" or \"ML\" only");
							PrintUsage();
						}
					}
					else 
					{
						System.err.println(error + "-t option requires a type of base tree for likelihod calculations: " + "fixed, BIONJ or ML");
						PrintUsage();
					}
				}

				else if (arg.equals("-u")) 
				{
					if (i < arguments.length) 
					{
						options.setInputTreeFile(new File(arguments[i++]));
						options.fixedTopology = false;
						options.optimizeMLTopology = false;
						options.userTopologyExists = true;
					}
					else 
					{
						System.err.println(error + "-u option requires an file name for the tree file");
						PrintUsage();
					}
				}

				else if (arg.equals("-S")) 
				{
					if (i < arguments.length) 
					{
						String type = arguments[i++];

						if (type.equalsIgnoreCase("NNI")) 
						{
							options.treeSearchOperations = ApplicationOptions.TreeSearch.NNI;
						}
						else if (type.equalsIgnoreCase("SPR")) 
						{
							options.treeSearchOperations = ApplicationOptions.TreeSearch.SPR;
						}
						else if (type.equalsIgnoreCase("BEST")) 
						{
							options.treeSearchOperations = ApplicationOptions.TreeSearch.BEST;
						}
						else 
						{
							System.err.println(error + "-S option requires a type of tree topology search operation: " + "\"NNI\", \"SPR\" or \"BEST\" only");
							PrintUsage();
						}
					}
					else 
					{
						System.err.println(error + "-S option requires a type of tree topology search operation: " + "\"NNI\", \"SPR\", \"BEST\"");
						PrintUsage();
					}
				}

				else if (arg.equals("-AIC")) 
				{
					options.doAIC = true;
				}

				else if (arg.equals("-AICc")) 
				{
					options.doAICc = true;
				}

				else if (arg.equals("-BIC")) 
				{
					options.doBIC = true;
				}

				else if (arg.equals("-DT")) 
				{
					options.doDT = true;
				}

				else if (arg.equals("-p")) 
				{
					options.doImportances = true;
				}

				else if (arg.equals("-v")) 
				{
					options.doImportances = true;
					options.doModelAveraging = true;
				}

				else if (arg.equals("-w")) 
				{
					options.writePAUPblock = true;
				}

				else if (arg.equals("-c")) 
				{
					if (i < arguments.length) 
					{
						try 
						{
							String type = arguments[i++];
							options.confidenceInterval = Double.parseDouble(type);
						}
						catch (NumberFormatException e) 
						{
							System.err.println(error + "-c option requires a number (0-1) for the model selection confidence interval.");
							PrintUsage();
						}
					}
					else 
					{
						System.err.println(error + "-c option requires a number (0-1) for the model selection confidence interval.");
						PrintUsage();
					}
				}

				else if (arg.equals("-hLRT")) 
				{
					options.doHLRT = true;
				}

				else if (arg.equals("-o")) 
				{
					if (i < arguments.length) 
					{
						String type = arguments[i++];
						char[] array = type.toCharArray();
						Arrays.sort(array);

						String validString = "";

						if (type.length() == 5) 
						{
							validString = "ftvgp";
						}
						else if (type.length() == 6) 
						{
							validString = "ftvwgp";
						}
						else if (type.length() == 7) 
						{
							validString = "ftvwxgp";
						}
						else 
						{
							System.err.println(error + "-o option requires a 5, 6 or 7 specific letter string with the order of tests (ftvgp/ftvwgp/ftvwxgp)");
							PrintUsage();
						}

						char[] valid = validString.toCharArray();
						if (!Arrays.equals(array, valid)) 
						{
							System.err.println(error + "-o option requires a 5, 6 or 7 specific letter string with the order of tests (ftvgp/ftvwgp/ftvwxgp)");
							PrintUsage();
						} 
						else 
						{
							testingOrder = new Vector<String>();
							for (j = 0; j < type.length(); j++) 
							{
								if (type.charAt(j) == 'f')
									testingOrder.addElement("freq");
								else if (type.charAt(j) == 't')
									testingOrder.addElement("titv");
								else if (type.charAt(j) == 'v') 
								{
									if (options.getSubstTypeCode() == 0)
										testingOrder.addElement("2ti4tv");
									else if (options.getSubstTypeCode() >= 1)
										testingOrder.addElement("2ti");
								} 
								else if (type.charAt(j) == 'w') 
								{
									if (options.getSubstTypeCode() >= 1)
										testingOrder.addElement("2tv");
								}
								else if (type.charAt(j) == 'x') 
								{
									if (options.getSubstTypeCode() > 1)
										testingOrder.addElement("4tv");
								}
								else if (type.charAt(j) == 'g')
									testingOrder.addElement("gamma");
								else if (type.charAt(j) == 'p')
									testingOrder.addElement("pinv");
							}
						}
					}
				}

				else if (arg.equals("-dLRT")) 
				{
					options.doDLRT = true;
				}

				else if (arg.equals("-r")) 
				{
					options.backwardHLRTSelection = true;
				}

				else if (arg.equals("-h")) 
				{
					if (i < arguments.length) 
					{
						try 
						{
							String type = arguments[i++];
							options.confidenceLevelHLRT = Double
									.parseDouble(type);
						}
						catch (NumberFormatException e) 
						{
							System.err.println(error + "-h option requires a number (0-1) for the hLRT confidence interval.");
							PrintUsage();
						}
					}
					else 
					{
						System.err.println(error + "-h option requires a number (0-1) for the hLRT confidence interval.");
						PrintUsage();
					}
				}

				else if (arg.equals("-a")) 
				{
					options.doAveragedPhylogeny = true;
				}

				else if (arg.equals("-z")) 
				{
					options.consensusType = "strict";
				}

				else if (arg.equals("-sims")) 
				{
					if (i < arguments.length) 
					{
						options.simulationsName = arguments[i++];
						options.doingSimulations = true;
					}
					else 
					{
						System.err.println(error + "-sims option requires a name for the simulations files");
						PrintUsage();
					}

				}
				else if (arg.equals("-machinesfile")) 
				{
					if (i < arguments.length) 
					{
						File machinesFile = new File(arguments[i++]);
						if (!(machinesFile.exists() && machinesFile.canRead())) 
						{
							if (MPJ_ME == 0) 
							{
								System.err.println(error + "Machines file does not exists or it is not readable");
							}
							PrintUsage();
						}

						boolean hostsError = false;
						try 
						{
							TextInputStream machinesInputStream = new TextInputStream(machinesFile.getAbsolutePath());
							String line;

							HOSTS_TABLE = new Hashtable<String, Integer>();
							while ((line = machinesInputStream.readLine()) != null) 
							{
								String hostProcs[] = line.split(":");
								if (hostProcs.length == 2) 
								{
									try 
									{
										int numberOfThreads = Integer.parseInt(hostProcs[1]);

										HOSTS_TABLE.put(hostProcs[0], new Integer(numberOfThreads));
									}
									catch (NumberFormatException e) 
									{
										hostsError = true;
										break;
										// Warn and continue with 1 processor
									}
								}
								else 
								{
									hostsError = true;
								}

								if (hostsError) 
								{
									if (MPJ_ME == 0) 
									{
										System.err.println("");
										System.err.println("WARNING: Machines File format is wrong.");
										System.err.println("         Each line should have the following format:");
										System.err.println("         HOSTNAME:NUMBER_OF_PROCESORS");
										System.err.println("Using a single thread");
										System.err.println("");
									}
									HOSTS_TABLE = null;
									options.setNumberOfThreads(1);
								}
							}
							options.setMachinesFile(machinesFile);

						}
						catch (FileNotFoundException e) 
						{
							System.err.println(error + "Machines file does not exists or it is not readable");
							PrintUsage();
						}
					} 
					else 
					{
						System.err.println(error + "-machinesfile option requires a filename");
						PrintUsage();
					}
				}
				
				else if (arg.equals("-tr")) 
				{
					if (HOSTS_TABLE != null) 
					{
						String type = arguments[i++];
						System.err.println("WARNING: Machines File has been specified. -tr " + type + " argument will be ignored.");
					}
					else 
					{
						if (i < arguments.length) 
						{
							try 
							{
								String type = arguments[i++];
								options.setNumberOfThreads(Integer.parseInt(type));
								options.threadScheduling = true;
							}
							catch (NumberFormatException e) 
							{
								System.err.println(error + "-tr option requires the number of processors to compute.");
								PrintUsage();
							}
						}
						else 
						{
							System.err.println(error + "-tr option requires the number of processors to compute.");
							PrintUsage();
						}
					}
				} 
				
				else if (arg.equals("-output"))
				{
					if (i < arguments.length) 
					{
						String output = arguments[i++];
						outputFile = new File(output);
					}
					else
					{
						System.err.println(error + "-output option requires output filename.");
						PrintUsage();
					}
				} 
				
				else 
				{
					System.err.println(error + "the argument \" " + arg	+ "\" is unknown. Check its syntax.");
					PrintUsage();
				}

			} // while
			
			if (!isInputFile) 
			{
				System.err.println(error + "Input File is required (-d argument)");
				PrintUsage();
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	/****************************
	 * PrintUsage **************************** * Prints command line usage * *
	 ************************************************************************/
	static public void PrintUsage() 
	{
		if (MPJ_ME == 0) 
		{
			String usage = "\njModelTest command usage"
					+ "\n -d: input data file (e.g., -d data.phy)"
					+ "\n -s: number of substitution schemes (e.g., -s 11) (it has to be 3,5,7,11; default is 3)"
					+ "\n -f: include models with unequals base frecuencies (e.g., -f) (default is false)"
					+ "\n -i: include models with a proportion invariable sites (e.g., -i) (default is false)"
					+ "\n -g: include models with rate variation among sites and number of categories (e.g., -g 8) (default is false & 4 categories)"
					+ "\n -t: base tree for likelihood calculations (fixed (BIONJ-JC), BIONJ, ML) (e.g., -t BIONJ) (default is ML)"
					+ "\n -u: user tree for likelihood calculations  (e.g., -u data.tre)"
					+ "\n -S: tree topology search operation option (NNI (fast), SPR (a bit slower), BEST (best of NNI and SPR)) (default is BEST)"
					+ "\n -n: sample size (-n235) (default is  the number of sites)"
					+ "\n -AIC: calculate the Akaike Information Criterion (e.g., -AIC) (default is false)"
					+ "\n -AICc: calculate the corrected Akaike Information Criterion (e.g., -AICc) (default is false)"
					+ "\n -BIC: calculate the Bayesian Information Criterion (e.g., -BIC) (default is false)"
					+ "\n -DT: calculate the decision theory criterion (e.g., -DT) (default is false)"
					+ "\n -p: calculate parameter importances (e.g., -p) (default is false)"
					+ "\n -v: do model averaging and parameter importances (e.g., -v) (default is false)"
					+ "\n -c: confidence interval (e.g., -c 90) (default is 100)"
					+ "\n -w: write PAUP block (e.g., -w) (default is false)"
					+ "\n -dLRT: do dynamical likelihood ratio tests (e.g., -dLRT)(default is false)"
					+ "\n -hLRT: do hierarchical likelihood ratio tests (default is false)"
					+ "\n -o: hypothesis order for the hLRTs (e.g., -hLRT -o gpftv) (default is ftvgp/ftvwgp/ftvwxgp)"
					+ "\n        f=freq, t=titvi, v=2ti4tv(subst=3)/2ti(subst>3), w=2tv, x=4tv, g=gamma, p=pinv"
					+ "\n -r: backward selection for the hLRT (e.g., -r) (default is forward)"
					+ "\n -h: confidence level for the hLRTs (e.g., -a0.002) (default is 0.01)"
					+ "\n -a: estimate model-averaged phylogeny for each active criterion (e.g., -a) (default is false)"
					+ "\n -z: strict consensus type for model-averaged phylogeny (e.g., -z) (default is majority rule)"
					+ "\n -tr: number of threads to execute (default is "
					+ Runtime.getRuntime().availableProcessors()
					+ ")"
					+ "\n -output: output data file (html) (e.g., -output output.html)"
					+ "\n -machinesfile: gets the processors per host from a machines file"
					+ "\n\n Command line: java -jar jModeltest.jar [arguments]";
			System.err.println(usage);
			System.err.println(" ");
		}
		System.exit(1);
	}

	/****************************
	 * printHeader ****************************** * Prints header information at
	 * start * * *
	 ***********************************************************************/

	static public void printHeader(TextOutputStream stream) 
	{
		// we can set styles using the editor pane
		// I am using doc to stream ....
		stream.print("----------------------------- ");
		stream.print("jModeltest " + CURRENT_VERSION);
		stream.println(" -----------------------------");
		stream.println("(c) 2011-onwards Diego Darriba, David Posada,");
		stream.println("Department of Biochemistry, Genetics and Immunology");
		stream.println("University of Vigo, 36310 Vigo, Spain. e-mail: ddarriba@udc.es, dposada@uvigo.es");
		stream.println("--------------------------------------------------------------------------------");

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

	static public void printNotice(TextOutputStream stream) 
	{
		// stream.println("\n******************************* NOTICE ************************************");
		stream.println("jModelTest " + CURRENT_VERSION
				+ "  Copyright (C) 2011 Diego Darriba, David Posada");
		stream.println("This program comes with ABSOLUTELY NO WARRANTY");
		stream.println("This is free software, and you are welcome to redistribute it");
		stream.println("under certain conditions");
		stream.println(" ");
		stream.println("Notice: This program may contain errors. Please inspect results carefully.");
		stream.println(" ");
		// stream.println("***************************************************************************\n");

	}

	/****************************
	 * printCitation ****************************** * Prints citation
	 * information at start up * * *
	 ***********************************************************************/

	static public void printCitation(TextOutputStream stream) 
	{
		// stream.println("\n******************************* CITATION *********************************");
		stream.println("Citation: Posada D. 2008. jModelTest: Phylogenetic Model Averaging.");
		stream.println("          Molecular Biology and Evolution 25: 1253-1256.");
		stream.println(" ");
		// stream.println("***************************************************************************\n");

	}

	/****************************
	 * writePaupBlockk *************************** * Prints a block of PAUP
	 * commands for the best model * *
	 ************************************************************************/
	static public void WritePaupBlock(TextOutputStream stream, String criterion, Model model) 
	{
		try 
		{
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
			if (model.ispF()) 
			{
				stream.print("(");
				stream.printf("%.4f ", model.getfA());
				stream.printf("%.4f ", model.getfC());
				stream.printf("%.4f ", model.getfG());
				/* stream.printf("%.4f",model.fT); */
				stream.print(")");
			} 
			else
				stream.print("equal");

			/* substitution rates */
			if (!model.ispT() && !model.ispR())
				stream.print(" nst=1");
			else if (model.ispT()) 
			{
				stream.print(" nst=2 tratio=");
				stream.printf("%.4f", model.getTitv());
			}
			else if (model.ispR()) 
			{
				stream.print(" nst=6  rmat=(");
				stream.printf("%.4f ", model.getRa());
				stream.printf("%.4f ", model.getRb());
				stream.printf("%.4f ", model.getRc());
				stream.printf("%.4f ", model.getRd());
				stream.printf("%.4f ", model.getRe());
				/* stream.print("1.0000)"); */
			}

			/* site rate variation */
			stream.print(" rates=");
			if (model.ispG()) 
			{
				stream.print("gamma shape=");
				stream.printf("%.4f", model.getShape());
				stream.print(" ncat=");
				stream.printf("%d", model.getNumGammaCat());
			}
			else
				stream.print("equal");

			/* invariable sites */
			stream.print(" pinvar=");
			if (model.ispI())
				stream.printf("%.4f", model.getPinv());
			else
				stream.print("0");

			stream.print(";\nEND;");
			stream.print("\n--");
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	private void checkInputFiles() 
	{
		// open data file
		File inputFile = options.getInputFile();
		MAIN_CONSOLE.print("\n\nReading data file \"" + inputFile.getName()	+ "\"...");

		if (inputFile.exists()) 
		{
			try 
			{
				ModelTestService.readAlignment(inputFile, options.getAlignmentFile());

				Alignment alignment = AlignmentReader.readAlignment(new PrintWriter(System.err), options.getAlignmentFile().getAbsolutePath(), true, logger); // file
				options.numTaxa = alignment.getSequenceCount();
				options.numSites = alignment.getSiteCount();
				options.numBranches = 2 * options.numTaxa - 3;

				MAIN_CONSOLE.println(" OK.");
				MAIN_CONSOLE.println("  number of sequences: " + options.numTaxa);
				MAIN_CONSOLE.println("  number of sites: " + options.numSites);
				options.sampleSize = options.numSites;
			} 
			catch (Exception e)// file cannot be read correctly
			{
				System.err.println("\nThe specified file \"" + inputFile.getAbsolutePath() + "\" cannot be read as an alignment");
				MAIN_CONSOLE.println(" failed.\n");
				throw new InvalidArgumentException.InvalidAlignmentFileException(inputFile);
			}
		} 
		else // file does not exist
		{
			System.err.println("\nThe specified file \"" + inputFile.getAbsolutePath() + "\" cannot be found");
			MAIN_CONSOLE.println(" failed.\n");
			throw new InvalidArgumentException.UnexistentAlignmentFileException(inputFile);
		}

		// open tree file if necessary
		if (options.userTopologyExists) 
		{
			File treefile = options.getInputTreeFile();
			MAIN_CONSOLE.print("Reading tree file \"" + treefile.getName() + "\"...");

			// read the tree in
			Tree tree = null;
			try 
			{
				tree = TreeUtilities.readTree(treefile.getAbsolutePath());
			}
			catch (IOException e) 
			{
				System.err.println("\nThe specified tree file \"" + treefile.getName() + "\" cannot be found");
				MAIN_CONSOLE.println(" failed.\n");
				throw new InvalidArgumentException.UnexistentTreeFileException(treefile.getAbsolutePath());
			}
			catch (TreeParseException e) 
			{
				System.err.println("\nCannot parse tree file \"" + treefile.getName() + "\"");
				MAIN_CONSOLE.println(" failed.\n");
				throw new InvalidArgumentException.InvalidTreeFileException(treefile.getAbsolutePath());
			}
			
			if (tree != null) 
			{
				options.setUserTree(TreeUtilities.toNewick(tree, true, false, false));
				TextOutputStream out = new TextOutputStream(options.getTreeFile().getAbsolutePath());
				out.print(options.getUserTree());
				out.close();
				MAIN_CONSOLE.println(" OK.");
			}
			else // tree is not valid
			{
				System.err.println("\nUnexpected error parsing \"" + treefile.getName() + "\"");
				MAIN_CONSOLE.println(" failed.\n");
				throw new InvalidArgumentException.InvalidTreeFileException(treefile.getAbsolutePath());
			}
		}
	}

	public TextOutputStream setMainConsole(TextOutputStream mainConsole) 
	{
		this.MAIN_CONSOLE = mainConsole;
		return mainConsole;
	}

	public TextOutputStream getMainConsole() 
	{
		return MAIN_CONSOLE;
	}

	public TextOutputStream getCurrentOutStream() 
	{
		return CURRENT_OUT_STREAM;
	}

	public void setCurrentOutStream(TextOutputStream currentOutStream) 
	{
		CURRENT_OUT_STREAM = currentOutStream;
	}

	public AIC getMyAIC() 
	{
		if (!AICwasCalculated)
			throw new WeakStateException.UninitializedCriterionException("AIC");
		return myAIC;
	}

	public void setMyAIC(AIC myAIC) 
	{
		this.myAIC = myAIC;
		this.minAIC = myAIC != null ? myAIC.getMinModel() : null;
		AICwasCalculated = (myAIC != null);
	}

	public boolean testAIC() 
	{
		return AICwasCalculated;
	}

	public AICc getMyAICc() 
	{
		if (!AICcwasCalculated)
			throw new WeakStateException.UninitializedCriterionException("AICc");
		return myAICc;
	}

	public void setMyAICc(AICc myAICc) 
	{
		this.myAICc = myAICc;
		this.minAICc = myAICc != null ? myAICc.getMinModel() : null;
		AICcwasCalculated = (myAICc != null);
	}

	public boolean testAICc() 
	{
		return AICcwasCalculated;
	}

	public BIC getMyBIC() 
	{
		if (!BICwasCalculated)
			throw new WeakStateException.UninitializedCriterionException("BIC");
		return myBIC;
	}

	public void setMyBIC(BIC myBIC) 
	{
		this.myBIC = myBIC;
		this.minBIC = myBIC != null ? myBIC.getMinModel() : null;
		BICwasCalculated = (myBIC != null);
	}

	public boolean testBIC() 
	{
		return BICwasCalculated;
	}

	public DT getMyDT() 
	{
		if (!DTwasCalculated)
			throw new WeakStateException.UninitializedCriterionException("DT");
		return myDT;
	}

	public void setMyDT(DT myDT) 
	{
		this.myDT = myDT;
		this.minDT = myDT != null ? myDT.getMinModel() : null;
		DTwasCalculated = (myDT != null);
	}

	public boolean testDT() 
	{
		return DTwasCalculated;
	}

	public RunConsense getConsensusAIC() 
	{
		return consensusAIC;
	}

	public RunConsense getConsensusAICc() 
	{
		return consensusAICc;
	}

	public RunConsense getConsensusBIC() 
	{
		return consensusBIC;
	}

	public RunConsense getConsensusDT() 
	{
		return consensusDT;
	}

	public void setConsensusAIC(RunConsense pConsensusAIC) 
	{
		consensusAIC = pConsensusAIC;
	}

	public void setConsensusAICc(RunConsense pConsensusAICc) 
	{
		consensusAICc = pConsensusAICc;
	}

	public void setConsensusBIC(RunConsense pConsensusBIC) 
	{
		consensusBIC = pConsensusBIC;
	}

	public void setConsensusDT(RunConsense pConsensusDT) 
	{
		consensusDT = pConsensusDT;
	}

	/**
	 * Finalizes the MPJ runtime environment. When an error occurs, it aborts
	 * the execution of every other processes.
	 * 
	 * @param status
	 *            the finalization status
	 */
	public static void finalize(int status) 
	{
		if (status != 0) 
		{
			if (MPJ_RUN) 
			{
				MPI.COMM_WORLD.Abort(status);
			}
		}

		if (MPJ_RUN) 
		{
			MPI.Finalize();
		}

		System.exit(status);
	}

	/**
	 * @param minDLRT
	 *            the minDLRT to set
	 */
	public void setMinDLRT(Model minDLRT) 
	{
		this.minDLRT = minDLRT;
	}

	/**
	 * @return the minDLRT
	 */
	public Model getMinDLRT() 
	{
		return minDLRT;
	}

	/**
	 * @param minHLRT
	 *            the minHLRT to set
	 */
	public void setMinHLRT(Model minHLRT) 
	{
		this.minHLRT = minHLRT;
	}

	/**
	 * @return the minHLRT
	 */
	public Model getMinHLRT() 
	{
		return minHLRT;
	}

	/**
	 * @return the minDT
	 */
	public Model getMinDT() 
	{
		return minDT;
	}

	/**
	 * @return the minBIC
	 */
	public Model getMinBIC() 
	{
		return minBIC;
	}

	/**
	 * @return the minAICc
	 */
	public Model getMinAICc() 
	{
		return minAICc;
	}

	/**
	 * @return the minAIC
	 */
	public Model getMinAIC() 
	{
		return minAIC;
	}

	/**
	 * @param candidateModels
	 *            the candidateModels to set
	 */
	public void setCandidateModels(Model[] candidateModels) 
	{
		this.candidateModels = candidateModels;
	}

	/**
	 * @return the candidateModels
	 */
	public Model[] getCandidateModels() 
	{
		return candidateModels;
	}

	/**
	 * @return a single candidate model
	 */
	public Model getCandidateModel(int index)
	{
		return candidateModels[index];
	}

	public static String getHostname()
	{
		return hostname;
	}

	public class NullPrinter extends OutputStream
	{
		@Override
		public void write(int arg0) throws IOException 
		{
			// DO NOTHING
		}

	}
} // class ModelTest

