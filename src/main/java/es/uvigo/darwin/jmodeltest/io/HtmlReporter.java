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
package es.uvigo.darwin.jmodeltest.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.exe.RunPhyml;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.selection.InformationCriterion;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;
import es.uvigo.darwin.prottest.facade.TreeFacade;
import es.uvigo.darwin.prottest.facade.TreeFacadeImpl;
import freemarker.template.Configuration;
import freemarker.template.Template;

public abstract class HtmlReporter {

	private static String[] TEMPLATE_DIRS = { "resources" };
	private static String[] TEMPLATE_FILES = {
			"resources" + File.separator + "style.css",
			"resources" + File.separator + "homeicon.gif",
			"resources" + File.separator + "topicon.gif",
			"resources" + File.separator + "logo0.png" };
	private static TreeFacade treeFacade = new TreeFacadeImpl();
	private static Map<String, Object> datamodel;

	public static void buildReport(ApplicationOptions options, Model models[]) {

		// Add the values in the datamodel
		datamodel = new HashMap<String, Object>();

		datamodel.put("doAveragedPhylogeny",
				options.doAveragedPhylogeny ? new Integer(1) : new Integer(0));

		fillInWithOptions(options);
		fillInWithSortedModels(models);

		if (options.doAIC) {
			Collection<Map<String, String>> aicModels = new ArrayList<Map<String, String>>();
			Map<String, String> bestAicModel = new HashMap<String, String>();
			fillInWIthInformationCriterion(ModelTest.getMyAIC(), aicModels,
					bestAicModel);
			datamodel.put("aicModels", aicModels);
			datamodel.put("bestAicModel", bestAicModel);
			datamodel.put("aicConfidenceCount", ModelTest.getMyAIC()
					.getConfidenceModels().size());
			StringBuffer aicConfModels = new StringBuffer();
			for (Model model : ModelTest.getMyAIC().getConfidenceModels())
				aicConfModels.append(model.getName() + " ");
			datamodel.put("aicConfidenceList", aicConfModels.toString());
		}

		if (options.doAICc) {
			Collection<Map<String, String>> aiccModels = new ArrayList<Map<String, String>>();
			Map<String, String> bestAiccModel = new HashMap<String, String>();
			fillInWIthInformationCriterion(ModelTest.getMyAICc(), aiccModels,
					bestAiccModel);
			datamodel.put("aiccModels", aiccModels);
			datamodel.put("bestAiccModel", bestAiccModel);
			datamodel.put("aiccConfidenceCount", ModelTest.getMyAICc()
					.getConfidenceModels().size());
			StringBuffer aiccConfModels = new StringBuffer();
			for (Model model : ModelTest.getMyAICc().getConfidenceModels())
				aiccConfModels.append(model.getName() + " ");
			datamodel.put("aiccConfidenceList", aiccConfModels.toString());
		}

		if (options.doBIC) {
			Collection<Map<String, String>> bicModels = new ArrayList<Map<String, String>>();
			Map<String, String> bestBicModel = new HashMap<String, String>();
			fillInWIthInformationCriterion(ModelTest.getMyBIC(), bicModels,
					bestBicModel);
			datamodel.put("bicModels", bicModels);
			datamodel.put("bestBicModel", bestBicModel);
			datamodel.put("bicConfidenceCount", ModelTest.getMyBIC()
					.getConfidenceModels().size());
			StringBuffer bicConfModels = new StringBuffer();
			for (Model model : ModelTest.getMyBIC().getConfidenceModels())
				bicConfModels.append(model.getName() + " ");
			datamodel.put("bicConfidenceList", bicConfModels.toString());
		}

		if (options.doDT) {
			Collection<Map<String, String>> dtModels = new ArrayList<Map<String, String>>();
			Map<String, String> bestDtModel = new HashMap<String, String>();
			fillInWIthInformationCriterion(ModelTest.getMyDT(), dtModels,
					bestDtModel);
			datamodel.put("dtModels", dtModels);
			datamodel.put("bestDtModel", bestDtModel);
			datamodel.put("dtConfidenceCount", ModelTest.getMyDT()
					.getConfidenceModels().size());
			StringBuffer dtConfModels = new StringBuffer();
			for (Model model : ModelTest.getMyDT().getConfidenceModels())
				dtConfModels.append(model.getName() + " ");
			datamodel.put("dtConfidenceList", dtConfModels.toString());
		}

		if (options.doAveragedPhylogeny) {
			datamodel.put("consensusType", ModelTest.getConsensusAIC()
					.getConsensusType());
			if (ModelTest.getConsensusAIC() != null) {
				datamodel.put("aicConsensusTree", treeFacade.toNewick(ModelTest
						.getConsensusAIC().getConsensus(), true, true, true));
			}
			if (ModelTest.getConsensusAICc() != null) {
				datamodel.put("aiccConsensusTree", treeFacade.toNewick(
						ModelTest.getConsensusAICc().getConsensus(), true,
						true, true));
			}
			if (ModelTest.getConsensusBIC() != null) {
				datamodel.put("bicConsensusTree", treeFacade.toNewick(ModelTest
						.getConsensusBIC().getConsensus(), true, true, true));
			}
			if (ModelTest.getConsensusDT() != null) {
				datamodel.put("dtConsensusTree", treeFacade.toNewick(ModelTest
						.getConsensusDT().getConsensus(), true, true, true));
			}
		}

		// Process the template using FreeMarker
		try {
			freemarkerDo(datamodel, "index.html");
		} catch (Exception e) {
			System.out
					.println("There was a problem building the html log files: "
							+ e.getLocalizedMessage());
		}
	}

	// Process a template using FreeMarker and print the results
	static void freemarkerDo(Map<String, Object> datamodel, String template)
			throws Exception {
		File resourcesDir = new File("resources" + File.separator + "template");
		File logDir = new File("log");
		if (!logDir.exists() || !logDir.isDirectory()) {
			logDir.delete();
			logDir.mkdir();
		}

		// Check auxiliary files
		for (String file : TEMPLATE_DIRS) {
			File auxDir = new File(logDir.getPath() + File.separator + file);
			if (!auxDir.exists()) {
				auxDir.mkdirs();
			}
		}
		for (String file : TEMPLATE_FILES) {
			File auxFile = new File(logDir.getPath() + File.separator + file);
			if (!auxFile.exists()) {
				File inFile = new File(resourcesDir.getPath() + File.separator
						+ file);
				if (inFile.exists()) {
					copyFile(inFile, auxFile);
				}
			}
		}

		File outputFile = new File(logDir.getPath() + File.separator
				+ "jmodeltest-log-" + Calendar.getInstance().getTimeInMillis()
				+ ".html");
		Configuration cfg = new Configuration();

		cfg.setDirectoryForTemplateLoading(resourcesDir);

		Template tpl = cfg.getTemplate(template);
		OutputStreamWriter output = new FileWriter(outputFile);

		tpl.process(datamodel, output);
	}

	private static void fillInWithOptions(ApplicationOptions options) {

		StringBuffer arguments = new StringBuffer();
		for (String argument : ModelTest.arguments)
			arguments.append(argument + " ");
		datamodel.put("arguments", arguments);
		datamodel.put("alignName", "Test Alignment.phy");
		datamodel.put("numTaxa", new Integer(50));
		datamodel.put("seqLength", new Integer(1500));
		datamodel.put("phymlVersion", RunPhyml.PHYML_VERSION);
		datamodel.put("phymlBinary", Utilities.getBinaryVersion());
		datamodel.put("candidateModels", ModelTest.model.length);
		if (options.getSubstTypeCode() == 0)
			datamodel.put("substSchemes", "3");
		else if (options.getSubstTypeCode() == 1)
			datamodel.put("substSchemes", "5");
		else if (options.getSubstTypeCode() == 2)
			datamodel.put("substSchemes", "7");
		else
			datamodel.put("substSchemes", "11");

		datamodel
				.put("includeF", options.doF ? new Integer(1) : new Integer(0));
		datamodel
				.put("includeG", options.doG ? new Integer(1) : new Integer(0));
		datamodel
				.put("includeI", options.doI ? new Integer(1) : new Integer(0));
		datamodel.put("isAIC", options.doAIC ? new Integer(1) : new Integer(0));
		datamodel.put("isAICc", options.doAICc ? new Integer(1)
				: new Integer(0));
		datamodel.put("isBIC", options.doBIC ? new Integer(1) : new Integer(0));
		datamodel.put("isDT", options.doDT ? new Integer(1) : new Integer(0));
		datamodel.put("numCat", options.numGammaCat);

		StringBuffer optimizedParameters = new StringBuffer(
				"Substitution parameters ");
		if (options.countBLasParameters)
			optimizedParameters.append("+ " + options.numBranches
					+ " branch lengths ");
		if (options.optimizeMLTopology)
			optimizedParameters.append("+ topology");
		datamodel.put("freeParameters", optimizedParameters.toString());

		datamodel.put("userTreeDef",
				options.userTopologyExists ? new Integer(1) : new Integer(0));
		if (options.fixedTopology)
			datamodel.put("baseTree", "Fixed BioNJ");
		else if (options.optimizeMLTopology)
			datamodel.put("baseTree", "Maximum Likelihood");
		else if (options.userTopologyExists) {
			datamodel.put("baseTree", "Fixed user tree topology");
			datamodel.put("userTreeFilename", options.getInputTreeFile()
					.getName());
			datamodel.put("userTree", options.getUserTree());
		} else
			datamodel.put("baseTree", "BioNJ");

		switch (options.treeSearchOperations) {
		case NNI:
			datamodel.put("searchAlgorithm", "NNI");
			break;
		case SPR:
			datamodel.put("searchAlgorithm", "SPR");
			break;
		case BEST:
			datamodel.put("searchAlgorithm", "Best of {NNI, SPR}");
			break;
		}

		datamodel.put("confidenceInterval",
				String.format("%5.2f", options.confidenceInterval * 100));
	}

	private static void fillInWithSortedModels(Model[] models) {

		Collection<Map<String, String>> sortedModels = new ArrayList<Map<String, String>>();
		int index = 1;
		for (Model model : models) {
			Map<String, String> modelMap = new HashMap<String, String>();
			modelMap.put("index", String.valueOf(index++));
			modelMap.put("name", model.getName());
			modelMap.put("partition", model.getPartition());
			modelMap.put("lnl", String.format("%5.4f", model.getLnL()));
			modelMap.put("k", String.valueOf(model.getK()));
			modelMap.put("fA",
					model.ispF() ? String.format("%5.4f", model.getfA()) : "-");
			modelMap.put("fC",
					model.ispF() ? String.format("%5.4f", model.getfC()) : "-");
			modelMap.put("fG",
					model.ispF() ? String.format("%5.4f", model.getfG()) : "-");
			modelMap.put("fT",
					model.ispF() ? String.format("%5.4f", model.getfT()) : "-");
			modelMap.put("titv",
					model.ispT() ? String.format("%5.4f", model.getTitv())
							: "-");
			modelMap.put("rA",
					model.ispR() ? String.format("%5.4f", model.getRa()) : "-");
			modelMap.put("rB",
					model.ispR() ? String.format("%5.4f", model.getRb()) : "-");
			modelMap.put("rC",
					model.ispR() ? String.format("%5.4f", model.getRc()) : "-");
			modelMap.put("rD",
					model.ispR() ? String.format("%5.4f", model.getRd()) : "-");
			modelMap.put("rE",
					model.ispR() ? String.format("%5.4f", model.getRe()) : "-");
			modelMap.put("rF",
					model.ispR() ? String.format("%5.4f", model.getRf()) : "-");
			modelMap.put("pInv",
					model.ispI() ? String.format("%5.4f", model.getPinv())
							: "-");
			modelMap.put("shape",
					model.ispG() ? String.format("%6.4f", model.getShape())
							: "-");
			sortedModels.add(modelMap);
		}

		datamodel.put("sortedModels", sortedModels);
	}

	private static void fillInWIthInformationCriterion(InformationCriterion ic,
			Collection<Map<String, String>> sortedModels,
			Map<String, String> bestModel) {

		Model model = ic.getModel(0);
		bestModel.put("index", String.valueOf(1));
		bestModel.put("name", model.getName());
		bestModel.put("partition", model.getPartition());
		bestModel.put("lnl", String.format("%5.4f", model.getLnL()));
		bestModel.put("k", String.valueOf(model.getK()));
		bestModel.put("fA",
				model.ispF() ? String.format("%5.4f", model.getfA()) : "-");
		bestModel.put("fC",
				model.ispF() ? String.format("%5.4f", model.getfC()) : "-");
		bestModel.put("fG",
				model.ispF() ? String.format("%5.4f", model.getfG()) : "-");
		bestModel.put("fT",
				model.ispF() ? String.format("%5.4f", model.getfT()) : "-");
		bestModel.put("titv",
				model.ispT() ? String.format("%5.4f", model.getTitv()) : "-");
		bestModel.put("rA",
				model.ispR() ? String.format("%5.4f", model.getRa()) : "-");
		bestModel.put("rB",
				model.ispR() ? String.format("%5.4f", model.getRb()) : "-");
		bestModel.put("rC",
				model.ispR() ? String.format("%5.4f", model.getRc()) : "-");
		bestModel.put("rD",
				model.ispR() ? String.format("%5.4f", model.getRd()) : "-");
		bestModel.put("rE",
				model.ispR() ? String.format("%5.4f", model.getRe()) : "-");
		bestModel.put("rF",
				model.ispR() ? String.format("%5.4f", model.getRf()) : "-");
		bestModel.put("pInv",
				model.ispI() ? String.format("%5.4f", model.getPinv()) : "-");
		bestModel.put("shape",
				model.ispG() ? String.format("%6.4f", model.getShape()) : "-");
		bestModel.put("value", String.format("%5.4f", ic.getValue(model)));
		bestModel.put("delta", String.format("%5.4f", ic.getDelta(model)));
		bestModel.put("weight", String.format("%5.4f", ic.getWeight(model)));
		bestModel.put("tree", model.getTreeString());
		bestModel.put("cumWeight",
				String.format("%5.4f", ic.getCumWeight(model)));
		sortedModels.add(bestModel);
		for (int i = 1; i < ic.getNumModels(); i++) {
			model = ic.getModel(i);
			Map<String, String> modelMap = new HashMap<String, String>();
			modelMap.put("index", String.valueOf(i + 1));
			modelMap.put("name", model.getName());
			modelMap.put("partition", model.getPartition());
			modelMap.put("lnl", String.format("%5.4f", model.getLnL()));
			modelMap.put("k", String.valueOf(model.getK()));
			modelMap.put("value", String.format("%5.4f", ic.getValue(model)));
			modelMap.put("delta", String.format("%5.4f", ic.getDelta(model)));
			modelMap.put("weight", String.format("%5.4f", ic.getWeight(model)));
			modelMap.put("cumWeight",
					String.format("%5.4f", ic.getCumWeight(model)));
			modelMap.put("tree", model.getTreeString());
			sortedModels.add(modelMap);
		}
	}

	public static void copyFile(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			throw e;
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}
}
