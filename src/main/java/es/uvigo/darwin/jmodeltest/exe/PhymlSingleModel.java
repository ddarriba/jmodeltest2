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

import pal.tree.TreeParseException;
import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTestConfiguration;
import es.uvigo.darwin.jmodeltest.io.TextInputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public class PhymlSingleModel extends Observable implements Runnable 
{
	protected int verbose = 0;
	
	private static String CURRENT_DIRECTORY = ModelTestConfiguration.PATH;

	public String PHYML_PATH = CURRENT_DIRECTORY + "exe/phyml/";

	protected String phymlStatFileName;
	protected String phymlTreeFileName;

	private boolean PHYML_GLOBAL = false;

	protected Model model;
	protected long startTime;
	protected long endTime;
	protected String commandLine;
	protected int index;
	protected boolean justGetJCTree = false;
	protected boolean ignoreGaps = false;
	protected boolean interrupted = false;
	protected ApplicationOptions options;
	protected int numberOfThreads = -1;

	public Model getModel() 
	{
		return model;
	}

	public PhymlSingleModel(Model model, int index, boolean justGetJCTree, boolean ignoreGaps, ApplicationOptions options) 
	{
		this.options = options;
		this.model = model;
		this.index = index;
		this.justGetJCTree = justGetJCTree;
		this.ignoreGaps = ignoreGaps;
		PHYML_GLOBAL = ModelTestConfiguration.isGlobalPhymlBinary();
		if (PHYML_GLOBAL) 
		{
			PHYML_PATH = "";
		}
		else 
		{
			String path = ModelTestConfiguration.getExeDir();
			if (!path.startsWith(File.separator)) 
			{
				PHYML_PATH = CURRENT_DIRECTORY + File.separator + path;
			}
			else 
			{
				PHYML_PATH = path;
			}
			
			if (!PHYML_PATH.endsWith(File.separator)) 
			{
				PHYML_PATH += File.separator;
			}
		}

		this.phymlStatFileName = options.getAlignmentFile().getAbsolutePath() + RunPhyml.PHYML_STATS_SUFFIX + model.getName() + ".txt";
		this.phymlTreeFileName = options.getAlignmentFile().getAbsolutePath() + RunPhyml.PHYML_TREE_SUFFIX + model.getName() + ".txt";
	}

	public PhymlSingleModel(Model model, int index, boolean justGetJCTree, ApplicationOptions options, int numberOfThreads) 
	{
		this(model, index, justGetJCTree, false, options);
		this.numberOfThreads = numberOfThreads;
	}

	public boolean compute() 
	{
		if (model.getLnL() < 1e-5 || ignoreGaps) 
		{
			// run phyml
			notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_INIT, index, model, null);

			startTime = System.currentTimeMillis();

			writePhyml3CommandLine();
			executeCommandLine();
		
			endCompute();
		}
		
		return !interrupted;
	}
	
	protected void endCompute()
	{
		if (!interrupted) 
		{
			parsePhyml3Files();
			Utilities.deleteFile(phymlStatFileName);
			Utilities.deleteFile(phymlTreeFileName);
		}

		endTime = System.currentTimeMillis();

		model.setComputationTime(endTime - startTime);

		// completed
        if (!interrupted) 
        {
            int value = 0;
            if (ignoreGaps) 
            {
                value = ProgressInfo.VALUE_IGAPS_OPTIMIZATION;
            }
            else 
            {
                value = ProgressInfo.VALUE_REGULAR_OPTIMIZATION;
            }
            
            notifyObservers(ProgressInfo.SINGLE_OPTIMIZATION_COMPLETED, value, model, Utilities.calculateRuntime(startTime, endTime));
        }
	}
	
	@Override
	public void run() 
	{
		compute();
	}

	/************************
	 * writePhym3lCommandLine ********************** * Builds up the command
	 * line for Phyml3 * * *
	 ***********************************************************************/

	protected void writePhyml3CommandLine() 
	{
		StringBuilder sb = new StringBuilder();
		
		// input file
		sb.append(" -i ").append(inputFileNameCommandLine());

		// data type is nucleotide
		sb.append(" -d nt");

		// number of data sets
		sb.append(" -n 1");

		// no bootrstrap or aLRT
		sb.append(" -b 0");

		if (ignoreGaps) 
		{
			sb.append(" --no_gap");
		}
		
		// set execution id
		sb.append(" --run_id ").append(model.getName());

		// set custom model
		sb.append(" -m ").append(model.getPartition());

		// optimize base frequencies if needed
		if (model.ispF())
			sb.append(" -f m"); // changed from -f e DP200509
		else
			sb.append(" -f 0.25,0.25,0.25,0.25");

		// optimize pinvar if needed
		if (model.ispI())
			sb.append(" -v e");

		// optimize rate parameters
		// if (currentModel.pT || currentModel.pR)
		// commandLine += rateParameters;

		// optimize alpha if needed
		if (model.ispG()) 
		{
			sb.append(" -c ").append(options.numGammaCat);
			sb.append(" -a e");
		}
		else
			sb.append(" -c 1");

		// search strategy
		switch (options.treeSearchOperations) 
		{
			case SPR:
				sb.append(" -s SPR");
				break;
			case BEST:
				sb.append(" -s BEST");
				break;
			default:
				sb.append(" -s NNI");
		}

		// threaded version
		if (numberOfThreads > 0) 
		{
			sb.append(" --num_threads ").append(numberOfThreads);
		}

		// avoid memory warning
		sb.append(" --no_memory_check");

		// do optimize topology?
		/*
		 * params=tlr: tree topology (t), branch length (l) and substitution
		 * rate parameters (r) are optimised. params = tlr or tl: optimize tree
		 * topology and branch lengths params = lr or l: tree topology fixed;
		 * optimize branch lengths; params = r or none: both tree topology and
		 * branch lengths are fixed.
		 */
		if (justGetJCTree) 
		{
			sb.append(" -o r"); // both tree topology and branch
								// lengths are fixed.
		}
		/*
		 * else if (ModelTest.userTreeExists) // use user tree for all models {
		 * commandLine += " -u " + userTreeFileName; commandLine += " -o " +
		 * "r"; // both tree topology and branch lengths are fixed. }
		 */
		// use a single tree for all models
		else if (options.userTopologyExists || options.fixedTopology) 
		{
			sb.append(" -u ").append(treeFileNameCommandLine());
			sb.append(" -o lr"); // tree topology fixed; optimize
								 // branch lengths
		}
		else if (!options.optimizeMLTopology) // use BIONJ tree for
											  // each model
		{
			sb.append(" -o lr"); // tree topology fixed; optimize
								 // branch lengths
		}
		else 
		{
			sb.append(" -o tlr"); // optimize tree topology and branch
								  // lengthss
		} // use ML optimized tree for each model
		
		commandLine = sb.toString();
	}

	protected String inputFileNameCommandLine()
	{
		return options.getAlignmentFile().getAbsolutePath();
	}
	
	protected String treeFileNameCommandLine()
	{
		return options.getTreeFile().getAbsolutePath();
	}
	
	/***************************
	 * executeCommandLine ************************ * Executes a set of command
	 * line in the system * * *
	 ***********************************************************************/

	protected void executeCommandLine() 
	{
		String[] executable = new String[1];
		try 
		{
			File dir = new File(PHYML_PATH);

			if (PHYML_GLOBAL) 
			{
				executable[0] = "phyml";
			}
			else 
			{
				File phymlBinary = new File(PHYML_PATH + "phyml");
				if (phymlBinary.exists() && phymlBinary.canExecute()) 
				{
					executable[0] = phymlBinary.getAbsolutePath();
				}
				else 
				{
					executable[0] = PHYML_PATH + Utilities.getBinaryVersion();
				}
			}
			String[] tokenizedCommandLine = commandLine.split(" ");
			String[] cmd = Utilities.specialConcatStringArrays(executable, tokenizedCommandLine);

			// get process and execute command line
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(cmd, null, PHYML_PATH.equals("") ? null : dir);
			ProcessManager.getInstance().registerProcess(proc);

			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR", System.err, options);
			// any output?
			FileOutputStream logFile = new FileOutputStream(options.getLogFile(), true);
			StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT", logFile, options);

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
			printout.println("Command line used for settings above = " + commandLine);
			printout.flush();
			printout.close();

		}
		catch (InterruptedException e) 
		{
			notifyObservers(ProgressInfo.INTERRUPTED, index, model, null);
			interrupted = true;
		}
		catch (Throwable t) 
		{
			notifyObservers(ProgressInfo.ERROR, index, model, "Cannot run the Phyml command line for some reason: " + t.getMessage());
			interrupted = true;
		}
	}

	/***************************
	 * parsePhyml3Files ************************** * Reads contents of Phyml3
	 * output files and loads * models parameter estimates * * *
	 ***********************************************************************/

	protected void parsePhyml3Files() 
	{
		String line;

		boolean showParsing = false;

		// Get model likelihood
		// TextInputStream phymlLkFile = new TextInputStream(phymlLkFileName);
		// currentModel.lnL = (-1.0) * phymlLkFile.readFloat();
		// phymlLkFile.close();

		// Get model likelihood and parameter estimates
		try 
		{
			TextInputStream phymlStatFile = new TextInputStream(phymlStatFileName);
			
			if (ignoreGaps) 
			{
                while ((line = phymlStatFile.readLine()) != null) 
                {
                    if (line.length() > 0) 
                    {
                        if (line.startsWith(". Log-likelihood")) 
                        {
                            model.setLnLIgnoringGaps((-1.0) * Double.parseDouble(Utilities.lastToken(line)));
                        }
                        else if (line.contains("Unconstrained likelihood")) 
                        {
                            double unconstrainedLnL = (-1.0) * Double.parseDouble(Utilities.lastToken(line));
                            model.setUnconstrainedLnL(unconstrainedLnL);
                            if (Math.abs(options.getUnconstrainedLnL() - unconstrainedLnL) > 1e-10) 
                            {
                                // uLK has changed!!!
                                // temporary uLK is updated
                                options.setUnconstrainedLnL(unconstrainedLnL);
                            }
                        } 
					}
				}            
			}
			else 
			{
				while ((line = phymlStatFile.readLine()) != null) 
				{
                    if (line.length() > 0) 
                    {
                        if (line.startsWith(". Log-likelihood")) 
                        {
                            model.setLnL((-1.0) * Double.parseDouble(Utilities.lastToken(line)));
                            if (showParsing)
                                System.err.println("Reading lnL = " + model.getLnL());
                        }
                        else if (line.startsWith(". Discrete gamma model")) 
                        {
                            if (Utilities.lastToken(line).equals("Yes")) 
                            {
                                // currentModel.pG = true;
                                line = phymlStatFile.readLine();
                                model.setNumGammaCat(Integer.parseInt(Utilities.lastToken(line)));
                                if (showParsing)
                                    System.err.println("Reading numGammaCat = " + model.getNumGammaCat());
                                line = phymlStatFile.readLine();
                                model.setShape(Double.parseDouble(Utilities.lastToken(line)));
                                if (showParsing)
                                    System.err.println("Reading shape = " + model.getShape());
                            }
                        }
                        else if (line.startsWith(". Nucleotides frequencies")) 
                        {
                            // currentModel.pF = true; ??
                            line = phymlStatFile.readLine();
                            while (line.trim().length() == 0)
                                // get rid of any number of returns
                                line = phymlStatFile.readLine();
                            model.setfA(Double.parseDouble(Utilities.lastToken(line)));
                            line = phymlStatFile.readLine();
                            model.setfC(Double.parseDouble(Utilities.lastToken(line)));
                            line = phymlStatFile.readLine();
                            model.setfG(Double.parseDouble(Utilities.lastToken(line)));
                            line = phymlStatFile.readLine();
                            model.setfT(Double.parseDouble(Utilities.lastToken(line)));
                            if (showParsing) 
                            {
                                    System.err.println("Reading fA = " + model.getfA());
                                    System.err.println("Reading fC = " + model.getfC());
                                    System.err.println("Reading fG = " + model.getfG());
                                    System.err.println("Reading fT = " + model.getfT());
                            }
                        }
                        else if (line.startsWith(". Proportion of invariant")) 
                        {
                            // model.pI = true;
                            model.setPinv(Double.parseDouble(Utilities.lastToken(line)));
                            if (showParsing)
                                System.err.println("Reading pinv = " + model.getPinv());
                        }
                        else if (line.contains("Unconstrained likelihood")) 
                        {
                            double unconstrainedLnL = (-1.0) * Double.parseDouble(Utilities.lastToken(line));
                            if (!options.isAmbiguous()) 
                            {
                                model.setUnconstrainedLnL(unconstrainedLnL);
                            }
                            else 
                            {
                                model.setUnconstrainedLnL(0.0d);
                            }
                            if (Math.abs(options.getUnconstrainedLnL()) <= 1e-10) 
                            {
                                options.setUnconstrainedLnL(unconstrainedLnL);
                            }
                            else 
                            {
                                if (Math.abs(options.getUnconstrainedLnL() - unconstrainedLnL) > 1e-10) 
                                {
                                    // uLK has changed!!!
                                    // temporary uLK is updated
                                    options.setUnconstrainedLnL(unconstrainedLnL);
                                }
                            }
                            if (showParsing)
                                System.err.println("Reading unconstrained logLK = " + model.getUnconstrainedLnL());
                        }
                        else if (line.startsWith(". GTR relative rate parameters")) 
                        {
                            line = phymlStatFile.readLine();
                            while (line.trim().length() == 0)
                                // get rid of any number of returns
                                line = phymlStatFile.readLine();
                            model.setRa(Double.parseDouble(Utilities.lastToken(line)));
                            line = phymlStatFile.readLine();
                            model.setRb(Double.parseDouble(Utilities.lastToken(line)));
                            line = phymlStatFile.readLine();
                            model.setRc(Double.parseDouble(Utilities.lastToken(line)));
                            line = phymlStatFile.readLine();
                            model.setRd(Double.parseDouble(Utilities.lastToken(line)));
                            line = phymlStatFile.readLine();
                            model.setRe(Double.parseDouble(Utilities.lastToken(line)));
                            line = phymlStatFile.readLine();
                            model.setRf(Double.parseDouble(Utilities.lastToken(line)));
                            if (showParsing) 
                            {
                                System.err.println("Reading Ra = " + model.getRa());
                                System.err.println("Reading Rb = " + model.getRb());
                                System.err.println("Reading Rc = " + model.getRc());
                                System.err.println("Reading Rd = " + model.getRd());
                                System.err.println("Reading Re = " + model.getRe());
                                System.err.println("Reading Rf = " + model.getRf());
                            }
                            // with custom models phyml does not provide a ti/tv, so
                            // we
                            // calculate it from the rate parameters
                            // note this is kappa and we need to transform it to
                            // ti/tv
                            if (model.ispT()) 
                            {
                                model.setKappa(model.getRb());
                                model.setTitv(model.getKappa() * (model.getfA() * model.getfG() + model.getfC() * model.getfT()) / ((model.getfA() + model.getfG()) * (model.getfC() + model.getfT())));
                            }
                        }        
                    }
				}
			}
			phymlStatFile.close();
		}
		catch (FileNotFoundException e) 
		{
			notifyObservers(ProgressInfo.ERROR, index, model, "Optimization results file does not exist: " + phymlStatFileName);
			interrupted=true;

		}
		catch (NullPointerException e) 
		{
			notifyObservers(ProgressInfo.ERROR, index, model, "Error while parsing result data from " + model.getName());
			interrupted=true;
		}

		try 
		{
			// Get ML tree
			TextInputStream phymlTreeFile = new TextInputStream(phymlTreeFileName);
			String treestr = phymlTreeFile.readLine();
			model.setTreeString(treestr);
			phymlTreeFile.close();
		}
		catch (FileNotFoundException e) 
		{
			notifyObservers(ProgressInfo.ERROR, index, model, "Optimized tree file does not exist: " + phymlTreeFileName);
			interrupted=true;
		}
		catch (TreeParseException e) 
		{
			notifyObservers(ProgressInfo.ERROR, index, model, "ML tree for " + model.getName() + " is invalid.");
			interrupted=true;
		}
	}

	protected void notifyObservers(int type, int value, Model model, String message) 
	{
		setChanged();
		notifyObservers(new ProgressInfo(type, value, model, message));
	}
}
