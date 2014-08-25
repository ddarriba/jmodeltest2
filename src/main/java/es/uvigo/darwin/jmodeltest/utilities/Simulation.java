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
package es.uvigo.darwin.jmodeltest.utilities;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;

import pal.tree.Tree;
import pal.tree.TreeParseException;
import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.ModelTestService;
import es.uvigo.darwin.jmodeltest.exe.RunConsense;
import es.uvigo.darwin.jmodeltest.exe.RunPhyml;
import es.uvigo.darwin.jmodeltest.exe.RunPhymlThread;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ConsoleProgressObserver;
import es.uvigo.darwin.jmodeltest.selection.AIC;
import es.uvigo.darwin.jmodeltest.selection.AICc;
import es.uvigo.darwin.jmodeltest.selection.BIC;
import es.uvigo.darwin.jmodeltest.selection.DT;
import es.uvigo.darwin.jmodeltest.selection.HLRT;
import es.uvigo.darwin.jmodeltest.selection.InformationCriterion;
import es.uvigo.darwin.jmodeltest.tree.TreeUtilities;
import es.uvigo.darwin.prottest.util.fileio.AlignmentReader;

public class Simulation {
	private ApplicationOptions options;
	
	private static final double nil = -99999;

	private static final String AIC = "AIC";
	private static final String AICc = "AICc";
	private static final String BIC = "BIC";
	private static final String DT = "DT";
	private static final String[] IC_TYPES = { AIC, AICc, BIC, DT };

	public Simulation(ApplicationOptions options) {
		this.options = options;
	}

	/*************************
	 * runSimulations ****************************** * Organizes all the tasks
	 * that the program needs to carry out * * *
	 ***********************************************************************/
	public void run() {
		int i;
		boolean append;
		/*
		 * .trees: base trees for every model in standard orden
		 * 
		 * .AIC_tre: best tree for the AIC model .AIC_mmi_mtre: model averaged
		 * AIC tree (50% majority rule) .AIC_mmi_atre: model averaged AIC tree
		 * (strict) ... same for AICc, BIC and DT
		 * 
		 * .parms: parameter estimates for every model in standard orden
		 * .AIC_parms: parameter estimates for the AIC model .AIC_imp_parms:
		 * parameter importances obtained with the AIC .AIC_mmi_parms: parameter
		 * importances obtained with the AIC ... same for AICc, BIC and DT
		 */

		// make simulation directories
		String simdir = "sims/";
		new File(simdir).mkdir();

		String outdir = simdir + "out/";
		new File(outdir).mkdir();

		String treedir = simdir + "trees/";
		new File(treedir).mkdir();

		String parmdir = simdir + "parameters/";
		new File(parmdir).mkdir();

		String simsName = options.simulationsName;

		if (options.getInputFile().getName().endsWith("001")
				|| options.getInputFile().getName().endsWith("001.phy")
				|| options.getInputFile().getName().endsWith("001.dat"))
			append = false;
		else
			append = true;

		// outfiles
		TextOutputStream outConsole = ModelTest.setMainConsole(new TextOutputStream(
				outdir + simsName + ".out", append));
		TextOutputStream treeConsole = new TextOutputStream(treedir + simsName
				+ ".trees", append);
		TextOutputStream parameterConsole = new TextOutputStream(parmdir
				+ simsName + ".parms", append);

		// print header information
		ModelTest.printHeader(outConsole);

		// print notice information
		// printNotice(outConsole);

		// check expiration date
		// CheckExpiration (outConsole);

		// print the command line
		outConsole.print("\nArguments =");
		for (i = 0; i < ModelTest.arguments.length; i++)
			outConsole.print(" " + ModelTest.arguments[i]);

		// open data file
		File file = options.getInputFile();
		outConsole.print("\n\nReading data file \"" + file.getName()
				+ "\"...");

		if (file.exists()) {
			try {

//				File outputFile = File
//						.createTempFile("jmodeltest", "input.aln");
				ModelTestService.readAlignment(file, options.getAlignmentFile());

				options.setAlignment(AlignmentReader.readAlignment(
						new PrintWriter(System.err),
						options.getAlignmentFile().getAbsolutePath(), true)); // file

				outConsole.println(" OK.");
				outConsole.println("  number of sequences: "
						+ options.getNumTaxa());
				outConsole.println("  number of sites: " + options.getNumSites());
			} catch (Exception e)// file cannot be read correctly
			{
				System.err.println("\nThe specified file \""
						+ file.getName()
						+ "\" cannot be read as an alignment");
				outConsole.println(" failed.\n");
				System.exit(0);
			}
		} else // file does not exist
		{
			System.err.println("\nThe specified file \""
					+ file.getName() + "\" cannot be found");
			outConsole.println(" failed.\n");
			System.exit(0);
		}

		// open tree file if necessary
		if (options.userTopologyExists) {
			File treefile = options.getTreeFile();
			options.setInputTreeFile(file);
			outConsole.print("Reading tree file \"" + treefile.getName()
					+ "\"...");

			// read the tree in
			Tree tree = null;
			try {
				tree = TreeUtilities.readTree(treefile.getAbsolutePath());
			} catch (IOException e) {
				System.err.println("\nThe specified tree file \""
						+ treefile.getName() + "\" cannot be found");
				outConsole.println(" failed.\n");
				System.exit(0);
			} catch (TreeParseException e) {
				System.err.println("\nThe specified file \""
						+ treefile.getName()
						+ "\" cannot be read as valid Newick tree");
				outConsole.println(" failed.\n");
				System.exit(0);
			}
			if (tree != null) {
				options.setUserTree(TreeUtilities.toNewick(tree, true,
						false, false));
				TextOutputStream out = new TextOutputStream(
						options.getTreeFile().getAbsolutePath());
				out.print(TreeUtilities.toNewick(tree, true, false, false));
				out.close();
				outConsole.println(" OK.");
			}

		}

		// print some progress
		System.err.print("Doing \"" + file.getName() + "\" ... ");

		// calculate number of models
		if (options.getSubstTypeCode() == 0)
			options.setNumModels(3);
		else if (options.getSubstTypeCode() == 1)
			options.setNumModels(5);
		else if (options.getSubstTypeCode() == 2)
			options.setNumModels(7);
		else
			options.setNumModels(11);

		if (options.doF)
			options.setNumModels(options.getNumModels() * 2);

		if (options.doI && options.doG)
			options.setNumModels(options.getNumModels() * 4);
		else if (options.doI || options.doG)
			options.setNumModels(options.getNumModels() * 2);

		// build set of models
		options.setCandidateModels();

		// calculate likelihoods with phyml in the command line
		RunPhyml phymlrun = new RunPhymlThread(new ConsoleProgressObserver(options), options, ModelTest.getCandidateModels());
		phymlrun.execute();

		// print all model trees to tree file and parameters to parms file
		if (!append)
			parameterConsole
					.println("data\tname\tln\tK\tfA\tfC\tfG\tfT\tkappa\ttitv\trAC\tAG\trAT\trCG\trCT\trGT\tpinvI\tshapeG\tpinvIG\tshapeIG");
		for (i = 0; i < options.getNumModels(); i++) {
			treeConsole.println(options.getInputFile().getName() + "\t"
					+ ModelTest.getCandidateModels()[i].getName() + "\t"
					+ ModelTest.getCandidateModels()[i].getTreeString());
			printModelLine(
					ModelTest.getCandidateModels()[i],
					parameterConsole,
					options.getInputFile().getName() + "\t"
							+ ModelTest.getCandidateModels()[i].getName(), nil, nil);
		}

		// do AIC if selected
		if (options.doAIC) {
			// TODO: oquhuh
			printSelection("AIC", treedir, simsName, parmdir, outConsole,
					append);
		}

		// do AICc if selected
		if (options.doAICc) {
			printSelection("AICc", treedir, simsName, parmdir, outConsole,
					append);
		}

		// do BIC if selected
		if (options.doBIC) {
			printSelection("BIC", treedir, simsName, parmdir, outConsole,
					append);
		}

		// do DT if selected
		if (options.doDT) {
			printSelection("DT", treedir, simsName, parmdir, outConsole, append);
		}

		// do hLRT if selected
		if (options.doHLRT) {
			HLRT myHLRT = new HLRT(options);
			myHLRT.compute(!options.backwardHLRTSelection,
					options.confidenceLevelHLRT, options.writePAUPblock);
			treeConsole.println(ModelTest.averagedTreeString);
		}

		// do dLRT if selected
		if (options.doDLRT) {
			HLRT myHLRT = new HLRT(options);
			myHLRT.computeDynamical(!options.backwardHLRTSelection,
					options.confidenceLevelHLRT, options.writePAUPblock);
		}

		outConsole.println("\n=> This run has finished.\n");
		System.err.println("OK.");
	}

	/****************************
	 * printModelLine *************************** * Print model components in a
	 * tabulated format * *
	 ************************************************************************/

	public void printModelLine(Model model, TextOutputStream stream,
			String whichName, double score, double weight) {

		if (whichName == null)
			stream.printf("%-10s", model.getName());
		else
			stream.printf("%-10s", whichName);

		if (model.getLnL() == 0) {
			stream.println("\tOPTIMIZATION FAILED!");
			System.exit(0);
		} else {
			stream.printf("\t%.4f", model.getLnL());
			stream.printf("\t%d", model.getK());

			if (score != nil)
				stream.printf("\t%.4f ", score);

			if (weight != nil)
				stream.printf("\t%.4f ", weight);

			if (model.ispF()) {
				stream.printf("\t%.4f ", model.getfA());
				stream.printf("\t%.4f ", model.getfC());
				stream.printf("\t%.4f ", model.getfG());
				stream.printf("\t%.4f ", model.getfT());
			} else
				stream.print("\t-\t-\t-\t-");

			if (model.ispT()) {
				stream.printf("\t%.4f", model.getKappa());
				stream.printf("\t%.4f", model.getTitv());
			} else
				stream.print("\t-\t-");

			if (model.ispR()) {
				stream.printf("\t%.4f", model.getRa());
				stream.printf("\t%.4f", model.getRb());
				stream.printf("\t%.4f", model.getRc());
				stream.printf("\t%.4f", model.getRd());
				stream.printf("\t%.4f", model.getRe());
				stream.printf("\t%.4f", 1.0);
			} else
				stream.print("\t-\t-\t-\t-\t-\t-");

			if (model.ispI() && model.ispG()) {
				stream.printf("\t-\t-\t%.4f", model.getPinv());
				stream.printf("\t%.4f", model.getShape());
			} else if (model.ispI())
				stream.printf("\t%.4f\t-\t-\t-", model.getPinv());
			else if (model.ispG())
				stream.printf("\t-\t%.4f\t-\t-", model.getShape());
			else
				stream.print("\t-\t-\t-\t-");

			stream.println(" ");
		}
	}

	/*************************
	 * CheckNAsim ********************************* * Returns formatted value or
	 * NA symbol depending on variable status * * *
	 ***********************************************************************/

	public static String CheckNAsim(double value) {
		if (value == Utilities.NA)
			return "-";
		else {
			String s = String.format(Locale.ENGLISH, "%.4f", value);
			return s;
		}
	}

	public void printSelection(String icType, String treedir, String simsName,
			String parmdir, TextOutputStream outConsole, boolean append) {

		// Validate
		if (!Arrays.asList(IC_TYPES).contains(icType)) {
			// TODO: EXCEPTION!
		}

		TextOutputStream ICtre = new TextOutputStream(treedir + simsName + "."
				+ icType + "_tre", append);
		TextOutputStream IC_mmi_mtre = new TextOutputStream(treedir + simsName
				+ "." + icType + "_mmi_mtre", append);
		TextOutputStream IC_mmi_atre = new TextOutputStream(treedir + simsName
				+ "." + icType + "_mmi_atre", append);

		TextOutputStream IC_parms = new TextOutputStream(parmdir + simsName
				+ "." + icType + "_parms", append);
		TextOutputStream IC_imp_parms = new TextOutputStream(parmdir + simsName
				+ "." + icType + "_imp_parms", append);
		TextOutputStream IC_mmi_parms = new TextOutputStream(parmdir + simsName
				+ "." + icType + "_mmi_parms", append);

		InformationCriterion ic;
		if (icType.equals(AIC)) {
			ic = new AIC(options.writePAUPblock, options.doImportances,
					options.doModelAveraging, options.confidenceInterval);
		} else if (icType.equals("AICc")) {
			ic = new AICc(options.writePAUPblock, options.doImportances,
					options.doModelAveraging, options.confidenceInterval);
		} else if (icType.equals("BIC")) {
			ic = new BIC(options.writePAUPblock, options.doImportances,
					options.doModelAveraging, options.confidenceInterval);
		} else if (icType.equals("DT")) {
			ic = new DT(options.writePAUPblock, options.doImportances,
					options.doModelAveraging, options.confidenceInterval);
		} else {
			ic = null;
			// TODO: EXCEPTION!!!
		}

		ic.compute();
		ic.print(outConsole);
		if (icType.equals(AIC)) {
			ModelTest.setMyAIC((AIC) ic);
		} else if (icType.equals("AICc")) {
			ModelTest.setMyAICc((AICc) ic);
		} else if (icType.equals("BIC")) {
			ModelTest.setMyBIC((BIC) ic);
		} else if (icType.equals("DT")) {
			ModelTest.setMyDT((DT) ic);
		}

		ICtre.println(ic.getMinModel().getTreeString());
		if (!append)
			IC_parms.print("data\tname\tln\tK\tscore\tweigth\tfA\tfC\tfG\tfT\tkappa\ttitv\trAC\tAG\trAT\trCG\trCT\trGT\tpinvI\tshapeG\tpinvIG\tshapeIG\n");
		printModelLine(ic.getMinModel(), IC_parms, options.getInputFile().getName()
				+ "\t" + ic.getMinModel().getName(), ic.getMinModelValue(),
				ic.getMinModelWeight());

		if (options.doAveragedPhylogeny) {
			new RunConsense(ic,	"50% majority rule", 
					options.confidenceInterval);
			IC_mmi_mtre.println(ModelTest.averagedTreeString);
			new RunConsense(ic, "strict",
					options.confidenceInterval);
			IC_mmi_atre.println(ModelTest.averagedTreeString);
		}
		if (options.doImportances) {
			if (!append) {
				IC_imp_parms
						.print("data\tfA\tfC\tfG\tfT\tkappa\ttitv\trAC\tAG\trAT\trCG\trCT\trGT\tpinvI\tshapeG\tpinvIG\tshapeIG\n");
			}
			IC_imp_parms.print(options.getInputFile().getName());
			IC_imp_parms.printf("\t%.4f", ic.getIfA());
			IC_imp_parms.printf("\t%.4f", ic.getIfC());
			IC_imp_parms.printf("\t%.4f", ic.getIfG());
			IC_imp_parms.printf("\t%.4f", ic.getIfT());
			IC_imp_parms.printf("\t%.4f", ic.getIkappa());
			IC_imp_parms.printf("\t%.4f", ic.getItitv());
			IC_imp_parms.printf("\t%.4f", ic.getiRa());
			IC_imp_parms.printf("\t%.4f", ic.getiRb());
			IC_imp_parms.printf("\t%.4f", ic.getiRc());
			IC_imp_parms.printf("\t%.4f", ic.getiRd());
			IC_imp_parms.printf("\t%.4f", ic.getiRe());
			IC_imp_parms.printf("\t%.4f", ic.getiRf());
			IC_imp_parms.printf("\t%.4f", ic.getIpinvI());
			IC_imp_parms.printf("\t%.4f", ic.getIshapeG());
			IC_imp_parms.printf("\t%.4f", ic.getIpinvIG());
			IC_imp_parms.printf("\t%.4f", ic.getIshapeIG());
			IC_imp_parms.println(" ");
		}
		if (options.doModelAveraging) {
			if (!append) {
				IC_mmi_parms
						.print("data\tfA\tfC\tfG\tfT\tkappa\ttitv\trAC\tAG\trAT\trCG\trCT\trGT\tpinvI\tshapeG\tpinvIG\tshapeIG\n");
			}
			IC_mmi_parms.print(options.getInputFile().getName());
			IC_mmi_parms.printf("\t%s", CheckNAsim(ic.getAfA()));
			IC_mmi_parms.printf("\t%s", CheckNAsim(ic.getAfC()));
			IC_mmi_parms.printf("\t%s", CheckNAsim(ic.getAfG()));
			IC_mmi_parms.printf("\t%s", CheckNAsim(ic.getAfT()));
			IC_mmi_parms.printf("\t%s", CheckNAsim(ic.getAkappa()));
			IC_mmi_parms.printf("\t%s", CheckNAsim(ic.getAtitv()));
			IC_mmi_parms.printf("\t%s", CheckNAsim(ic.getaRa()));
			IC_mmi_parms.printf("\t%s", CheckNAsim(ic.getaRb()));
			IC_mmi_parms.printf("\t%s", CheckNAsim(ic.getaRc()));
			IC_mmi_parms.printf("\t%s", CheckNAsim(ic.getaRd()));
			IC_mmi_parms.printf("\t%s", CheckNAsim(ic.getaRe()));
			IC_mmi_parms.printf("\t%s", CheckNAsim(ic.getaRf()));
			IC_mmi_parms.printf("\t%s", CheckNAsim(ic.getApinvI()));
			IC_mmi_parms.printf("\t%s", CheckNAsim(ic.getAshapeG()));
			IC_mmi_parms.printf("\t%s", CheckNAsim(ic.getApinvIG()));
			IC_mmi_parms.printf("\t%s", CheckNAsim(ic.getAshapeIG()));
			IC_mmi_parms.println(" ");
		}
	}
}
