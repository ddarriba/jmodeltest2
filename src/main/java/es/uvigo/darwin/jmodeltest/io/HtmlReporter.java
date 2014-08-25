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

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jfree.chart.ChartUtilities;

import pal.tree.Tree;
import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.ModelTestConfiguration;
import es.uvigo.darwin.jmodeltest.exe.RunPhyml;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.selection.InformationCriterion;
import es.uvigo.darwin.jmodeltest.tree.TreeSummary;
import es.uvigo.darwin.jmodeltest.tree.TreeUtilities;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;
import es.uvigo.darwin.prottest.facade.TreeFacade;
import es.uvigo.darwin.prottest.facade.TreeFacadeImpl;
import freemarker.template.Configuration;
import freemarker.template.Template;

public abstract class HtmlReporter {

	private static String[] TEMPLATE_DIRS = { "resources" };
	private static String[] TEMPLATE_FILES = {
			"resources" + File.separator + "style.css",
			"resources" + File.separator + "homeIcon.gif",
			"resources" + File.separator + "topIcon.gif",
			"resources" + File.separator + "logo0.png" };
	private static TreeFacade treeFacade = new TreeFacadeImpl();
	private static Map<String, Object> datamodel;
	private static File LOG_DIR;
	private static File IMAGES_DIR;
	private static String RESOURCES_DIR;
	
	static {
		String strLogDir = ModelTestConfiguration.getLogDir();
		if (!strLogDir.startsWith(File.separator)) {
			strLogDir = ModelTestConfiguration.PATH + strLogDir;
		}
		LOG_DIR = new File(strLogDir);
		IMAGES_DIR = new File(strLogDir + File.separator + "images");
		RESOURCES_DIR = ModelTestConfiguration.PATH + "resources" + File.separator + "template";
	}
	
	public static void buildReport(ApplicationOptions options, Model models[],
			File mOutputFile) {
		buildReport(options, models, mOutputFile, null);
	}
	
	public static void buildReport(ApplicationOptions options, Model models[],
			File mOutputFile, TreeSummary summary) {

		File outputFile;
		if (mOutputFile != null) {
			if (!(mOutputFile.getName().endsWith(".htm") || mOutputFile.getName()
					.endsWith(".html"))) {
				outputFile = new File(mOutputFile.getAbsolutePath() + ".html");
			} else {
				outputFile = mOutputFile;
			}
		} else {
			outputFile = new File(LOG_DIR.getPath() + File.separator
					+ options.getInputFile().getName()
					+ ".jmodeltest." + options.getExecutionName()
					+ ".html");
		}
		
		// Add the values in the datamodel
		datamodel = new HashMap<String, Object>();
		java.util.Date current_time = new java.util.Date();
		datamodel.put("date", current_time.toString());
		datamodel.put("system", System.getProperty("os.name") + " "
				+ System.getProperty("os.version") + ", arch: "
				+ System.getProperty("os.arch") + ", bits: "
				+ System.getProperty("sun.arch.data.model") + ", numcores: "
				+ Runtime.getRuntime().availableProcessors());
		
		fillInWithOptions(options);
		fillInWithSortedModels(models);
		datamodel.put("isTopologiesSummary", summary!=null ? new Integer(1) : new Integer(0));
		if (summary != null) {
			fillInWithTopologies(summary, options);
		}

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
			if (options.writePAUPblock) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				TextOutputStream strOutput = new TextOutputStream(ps);
			    ModelTest.WritePaupBlock(strOutput, "AIC", ModelTest.getMyAIC().getMinModel());
			    try {
			    	String pblock = baos.toString("UTF8");
			    	pblock = pblock.replaceAll("\n", "<br/>");
					datamodel.put("aicPaup", pblock);
				} catch (UnsupportedEncodingException e) {
				}
				
			}
			buildChart(outputFile, ModelTest.getMyAIC());
			datamodel.put("aicEuImagePath",IMAGES_DIR.getName() + File.separator + outputFile.getName() + "_eu_AIC.png");
			datamodel.put("aicRfImagePath",IMAGES_DIR.getName() + File.separator + outputFile.getName() + "_rf_AIC.png");
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
			if (options.writePAUPblock) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				TextOutputStream strOutput = new TextOutputStream(ps);
			    ModelTest.WritePaupBlock(strOutput, "AICc", ModelTest.getMyAICc().getMinModel());
			    try {
			    	String pblock = baos.toString("UTF8");
			    	pblock = pblock.replaceAll("\n", "<br/>");
					datamodel.put("aiccPaup", pblock);
				} catch (UnsupportedEncodingException e) {
				}
				
			}
			buildChart(outputFile, ModelTest.getMyAICc());
			datamodel.put("aiccEuImagePath",IMAGES_DIR.getName() + File.separator + outputFile.getName() + "_eu_AICc.png");
			datamodel.put("aiccRfImagePath",IMAGES_DIR.getName() + File.separator + outputFile.getName() + "_rf_AICc.png");
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
			if (options.writePAUPblock) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				TextOutputStream strOutput = new TextOutputStream(ps);
			    ModelTest.WritePaupBlock(strOutput, "BIC", ModelTest.getMyBIC().getMinModel());
			    try {
			    	String pblock = baos.toString("UTF8");
			    	pblock = pblock.replaceAll("\n", "<br/>");
					datamodel.put("bicPaup", pblock);
				} catch (UnsupportedEncodingException e) {
				}
				
			}
			buildChart(outputFile, ModelTest.getMyBIC());
			datamodel.put("bicEuImagePath",IMAGES_DIR.getName() + File.separator + outputFile.getName() + "_eu_BIC.png");
			datamodel.put("bicRfImagePath",IMAGES_DIR.getName() + File.separator + outputFile.getName() + "_rf_BIC.png");
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
			if (options.writePAUPblock) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				TextOutputStream strOutput = new TextOutputStream(ps);
			    ModelTest.WritePaupBlock(strOutput, "DT", ModelTest.getMyDT().getMinModel());
			    try {
			    	String pblock = baos.toString("UTF8");
			    	pblock = pblock.replaceAll("\n", "<br/>");
					datamodel.put("dtPaup", pblock);
				} catch (UnsupportedEncodingException e) {
				}
				
			}
			buildChart(outputFile, ModelTest.getMyDT());
			datamodel.put("dtEuImagePath",IMAGES_DIR.getName() + File.separator + outputFile.getName() + "_eu_DT.png");
			datamodel.put("dtRfImagePath",IMAGES_DIR.getName() + File.separator + outputFile.getName() + "_rf_DT.png");
		}

		datamodel.put("doAICAveragedPhylogeny",
				ModelTest.getConsensusAIC() != null ? new Integer(1)
						: new Integer(0));
		if (ModelTest.getConsensusAIC() != null) {
			datamodel.put("aicConsensusTree", treeFacade.toNewick(ModelTest
					.getConsensusAIC().getConsensus(), true, true, true));
			datamodel.put("consensusType", ModelTest.getConsensusAIC()
					.getConsensusType());
		}
		datamodel.put("doAICcAveragedPhylogeny",
				ModelTest.getConsensusAICc() != null ? new Integer(1)
						: new Integer(0));
		if (ModelTest.getConsensusAICc() != null) {
			datamodel.put("aiccConsensusTree", treeFacade.toNewick(ModelTest
					.getConsensusAICc().getConsensus(), true, true, true));
			datamodel.put("consensusType", ModelTest.getConsensusAICc()
					.getConsensusType());
		}
		datamodel.put("doBICAveragedPhylogeny",
				ModelTest.getConsensusBIC() != null ? new Integer(1)
						: new Integer(0));
		if (ModelTest.getConsensusBIC() != null) {
			datamodel.put("bicConsensusTree", treeFacade.toNewick(ModelTest
					.getConsensusBIC().getConsensus(), true, true, true));
			datamodel.put("consensusType", ModelTest.getConsensusBIC()
					.getConsensusType());
		}
		datamodel.put("doDTAveragedPhylogeny",
				ModelTest.getConsensusDT() != null ? new Integer(1)
						: new Integer(0));
		if (ModelTest.getConsensusDT() != null) {
			datamodel.put("dtConsensusTree", treeFacade.toNewick(ModelTest
					.getConsensusDT().getConsensus(), true, true, true));
			datamodel.put("consensusType", ModelTest.getConsensusDT()
					.getConsensusType());
		}

		// Process the template using FreeMarker
		try {
			freemarkerDo(datamodel, "index.html", outputFile);
		} catch (Exception e) {
			System.out
					.println("There was a problem building the html log files: "
							+ e.getLocalizedMessage());
		}
		
	}

	// Process a template using FreeMarker and print the results
	static void freemarkerDo(Map<String, Object> datamodel, String template,
			File outputFile) throws Exception {
		if (!LOG_DIR.exists() || !LOG_DIR.isDirectory()) {
			LOG_DIR.delete();
			LOG_DIR.mkdir();
		}
		if (!IMAGES_DIR.exists() || !IMAGES_DIR.isDirectory()) {
			IMAGES_DIR.delete();
			IMAGES_DIR.mkdir();
		}
		
		// Check auxiliary files
		for (String file : TEMPLATE_DIRS) {
			File auxDir = new File(LOG_DIR.getPath() + File.separator + file);
			if (!auxDir.exists()) {
				auxDir.mkdirs();
			}
		}

		for (String file : TEMPLATE_FILES) {
			File auxFile = new File(LOG_DIR.getPath() + File.separator + file);
			if (!auxFile.exists()) {
				File inFile = new File(RESOURCES_DIR + File.separator
						+ file);
				if (inFile.exists()) {
					copyFile(inFile, auxFile);
				}
			}
		}
		
		Configuration cfg = new Configuration();

		cfg.setDirectoryForTemplateLoading(new File(RESOURCES_DIR));

		Template tpl = cfg.getTemplate(template);
		OutputStreamWriter output = new FileWriter(outputFile);

		tpl.process(datamodel, output);
	}

	private static void fillInWithOptions(ApplicationOptions options) {

		StringBuffer arguments = new StringBuffer();
		for (String argument : ModelTest.arguments)
			arguments.append(argument + " ");
		datamodel.put("arguments", arguments);
		datamodel.put("alignName", options.getInputFile());
		datamodel.put("numTaxa", options.getNumTaxa());
		datamodel.put("seqLength", options.getNumSites());
		datamodel.put("phymlVersion", RunPhyml.PHYML_VERSION);
		datamodel.put("phymlBinary", Utilities.getBinaryVersion());
		datamodel.put("candidateModels", ModelTest.getCandidateModels().length);
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
		datamodel.put("isPAUP", options.writePAUPblock ? new Integer(1) : new Integer(0));

		StringBuffer optimizedParameters = new StringBuffer(
				"Substitution parameters ");
		if (options.countBLasParameters)
			optimizedParameters.append("+ " + options.getNumBranches()
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

		datamodel.put("confidenceInterval", String.format(Locale.ENGLISH,
				"%5.2f", options.confidenceInterval * 100));
	}

	private static void fillInWithSortedModels(Model[] models) {

		Collection<Map<String, String>> sortedModels = new ArrayList<Map<String, String>>();
		int index = 1;
		for (Model model : models) {
			Map<String, String> modelMap = new HashMap<String, String>();
			modelMap.put("index", String.valueOf(index++));
			modelMap.put("name", model.getName());
			modelMap.put("partition", model.getPartition());
			modelMap.put("lnl",
					String.format(Locale.ENGLISH, "%5.4f", model.getLnL()));
			modelMap.put("k", String.valueOf(model.getK()));
			modelMap.put(
					"fA",
					model.ispF() ? String.format(Locale.ENGLISH, "%5.4f",
							model.getfA()) : "-");
			modelMap.put(
					"fC",
					model.ispF() ? String.format(Locale.ENGLISH, "%5.4f",
							model.getfC()) : "-");
			modelMap.put(
					"fG",
					model.ispF() ? String.format(Locale.ENGLISH, "%5.4f",
							model.getfG()) : "-");
			modelMap.put(
					"fT",
					model.ispF() ? String.format(Locale.ENGLISH, "%5.4f",
							model.getfT()) : "-");
			modelMap.put(
					"titv",
					model.ispT() ? String.format(Locale.ENGLISH, "%5.4f",
							model.getTitv()) : "-");
			modelMap.put(
					"rA",
					model.ispR() ? String.format(Locale.ENGLISH, "%5.4f",
							model.getRa()) : "-");
			modelMap.put(
					"rB",
					model.ispR() ? String.format(Locale.ENGLISH, "%5.4f",
							model.getRb()) : "-");
			modelMap.put(
					"rC",
					model.ispR() ? String.format(Locale.ENGLISH, "%5.4f",
							model.getRc()) : "-");
			modelMap.put(
					"rD",
					model.ispR() ? String.format(Locale.ENGLISH, "%5.4f",
							model.getRd()) : "-");
			modelMap.put(
					"rE",
					model.ispR() ? String.format(Locale.ENGLISH, "%5.4f",
							model.getRe()) : "-");
			modelMap.put(
					"rF",
					model.ispR() ? String.format(Locale.ENGLISH, "%5.4f",
							model.getRf()) : "-");
			modelMap.put(
					"pInv",
					model.ispI() ? String.format(Locale.ENGLISH, "%5.4f",
							model.getPinv()) : "-");
			modelMap.put(
					"shape",
					model.ispG() ? String.format(Locale.ENGLISH, "%6.4f",
							model.getShape()) : "-");
			modelMap.put("tree", model.getTreeString());
			sortedModels.add(modelMap);
		}

		datamodel.put("sortedModels", sortedModels);
	}

	private static void fillInWithTopologies(TreeSummary summary, ApplicationOptions options) {
		datamodel.put("numberOfTopologies", summary.getNumberOfTopologies());
		Collection<Map<String, String>> sortedTopologies = new ArrayList<Map<String, String>>();
		for (int index=0; index<summary.getNumberOfTopologies(); index++) {
			Map<String, String> topologyMap = new HashMap<String, String>();
			topologyMap.put("index", String.valueOf(index));
			Tree topology = summary.getTopology(index);
			topologyMap.put("tree", TreeUtilities.toNewick(topology, false, false, false));
			List<Model> models = summary.getModelsByTopology(index);
			StringBuilder sb = new StringBuilder();
			for (Model model : models) {
				sb.append(model.getName() + " ");
			}
			topologyMap.put("models", sb.toString());
			topologyMap.put("models", sb.toString());
			
			if (options.doAIC) {
				topologyMap.put("aicRank", String.valueOf(summary.aicIndexOf(topology)));
				topologyMap.put("aicRF", String.valueOf(summary.aicRfOf(topology)));
				topologyMap.put("aicAvgDistance", Utilities.format(summary.aicAvgDistance(topology), 6, 4, true));
				topologyMap.put("aicVarDistance", Utilities.format(summary.aicVarDistance(topology), 6, 4, true));
				topologyMap.put("aicWeight", Utilities.format(summary.aicWeight(topology), 6, 4, false));
			}
			
			if (options.doAICc) {
				topologyMap.put("aiccRank", String.valueOf(summary.aiccIndexOf(topology)));
				topologyMap.put("aiccRF", String.valueOf(summary.aiccRfOf(topology)));
				topologyMap.put("aiccAvgDistance", Utilities.format(summary.aiccAvgDistance(topology), 6, 4, true));
				topologyMap.put("aiccVarDistance", Utilities.format(summary.aiccVarDistance(topology), 6, 4, true));
				topologyMap.put("aiccWeight", Utilities.format(summary.aiccWeight(topology), 6, 4, false));
			}
			
			if (options.doBIC) {
				topologyMap.put("bicRank", String.valueOf(summary.bicIndexOf(topology)));
				topologyMap.put("bicRF", String.valueOf(summary.bicRfOf(topology)));
				topologyMap.put("bicAvgDistance", Utilities.format(summary.bicAvgDistance(topology), 6, 4, true));
				topologyMap.put("bicVarDistance", Utilities.format(summary.bicVarDistance(topology), 6, 4, true));
				topologyMap.put("bicWeight", Utilities.format(summary.bicWeight(topology), 6, 4, false));
			}
			
			if (options.doDT) {
				topologyMap.put("dtRank", String.valueOf(summary.dtIndexOf(topology)));
				topologyMap.put("dtRF", String.valueOf(summary.dtRfOf(topology)));
				topologyMap.put("dtAvgDistance", Utilities.format(summary.dtAvgDistance(topology), 6, 4, true));
				topologyMap.put("dtVarDistance", Utilities.format(summary.dtVarDistance(topology), 6, 4, true));
				topologyMap.put("dtWeight", Utilities.format(summary.dtWeight(topology), 6, 4, false));
			}
			
			sortedTopologies.add(topologyMap);
		}
		
		datamodel.put("sortedTopologies", sortedTopologies);
	}
	
	private static void fillInWIthInformationCriterion(InformationCriterion ic,
			Collection<Map<String, String>> sortedModels,
			Map<String, String> bestModel) {

		Model model = ic.getModel(0);
		bestModel.put("index", String.valueOf(1));
		bestModel.put("name", model.getName());
		bestModel.put("partition", model.getPartition());
		bestModel.put("lnl",
				String.format(Locale.ENGLISH, "%5.4f", model.getLnL()));
		bestModel.put("k", String.valueOf(model.getK()));
		bestModel.put(
				"fA",
				model.ispF() ? String.format(Locale.ENGLISH, "%5.4f",
						model.getfA()) : "-");
		bestModel.put(
				"fC",
				model.ispF() ? String.format(Locale.ENGLISH, "%5.4f",
						model.getfC()) : "-");
		bestModel.put(
				"fG",
				model.ispF() ? String.format(Locale.ENGLISH, "%5.4f",
						model.getfG()) : "-");
		bestModel.put(
				"fT",
				model.ispF() ? String.format(Locale.ENGLISH, "%5.4f",
						model.getfT()) : "-");
		bestModel.put(
				"titv",
				model.ispT() ? String.format(Locale.ENGLISH, "%5.4f",
						model.getTitv()) : "-");
		bestModel.put(
				"rA",
				model.ispR() ? String.format(Locale.ENGLISH, "%5.4f",
						model.getRa()) : "-");
		bestModel.put(
				"rB",
				model.ispR() ? String.format(Locale.ENGLISH, "%5.4f",
						model.getRb()) : "-");
		bestModel.put(
				"rC",
				model.ispR() ? String.format(Locale.ENGLISH, "%5.4f",
						model.getRc()) : "-");
		bestModel.put(
				"rD",
				model.ispR() ? String.format(Locale.ENGLISH, "%5.4f",
						model.getRd()) : "-");
		bestModel.put(
				"rE",
				model.ispR() ? String.format(Locale.ENGLISH, "%5.4f",
						model.getRe()) : "-");
		bestModel.put(
				"rF",
				model.ispR() ? String.format(Locale.ENGLISH, "%5.4f",
						model.getRf()) : "-");
		bestModel.put(
				"pInv",
				model.ispI() ? String.format(Locale.ENGLISH, "%5.4f",
						model.getPinv()) : "-");
		bestModel.put(
				"shape",
				model.ispG() ? String.format(Locale.ENGLISH, "%6.4f",
						model.getShape()) : "-");
		bestModel.put("value",
				String.format(Locale.ENGLISH, "%5.4f", ic.getValue(model)));
		bestModel.put("delta",
				String.format(Locale.ENGLISH, "%5.4f", ic.getDelta(model)));
		bestModel.put("weight",
				String.format(Locale.ENGLISH, "%5.4f", ic.getWeight(model)));
		bestModel.put("tree", model.getTreeString());
		bestModel.put("cumWeight",
				String.format(Locale.ENGLISH, "%5.4f", ic.getCumWeight(model)));
		sortedModels.add(bestModel);
		for (int i = 1; i < ic.getNumModels(); i++) {
			model = ic.getModel(i);
			Map<String, String> modelMap = new HashMap<String, String>();
			modelMap.put("index", String.valueOf(i + 1));
			modelMap.put("name", model.getName());
			modelMap.put("partition", model.getPartition());
			modelMap.put("lnl",
					String.format(Locale.ENGLISH, "%5.4f", model.getLnL()));
			modelMap.put("k", String.valueOf(model.getK()));
			modelMap.put("value",
					String.format(Locale.ENGLISH, "%5.4f", ic.getValue(model)));
			modelMap.put("delta",
					String.format(Locale.ENGLISH, "%5.4f", ic.getDelta(model)));
			modelMap.put("weight",
					String.format(Locale.ENGLISH, "%5.4f", ic.getWeight(model)));
			modelMap.put(
					"cumWeight",
					String.format(Locale.ENGLISH, "%5.4f",
							ic.getCumWeight(model)));
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

	private static void buildChart(File mOutputFile, InformationCriterion ic) {
		
		/* This line prevents Swing exceptions on headless environments */
		if (GraphicsEnvironment.isHeadless()) { return; }
		
		int width = 500;
		int height = 300;
		try {
			if (!IMAGES_DIR.exists()) {
				IMAGES_DIR.mkdir();
			}
			ChartUtilities.saveChartAsPNG(new File(IMAGES_DIR.getPath() + File.separator + mOutputFile.getName() + "_rf_" + ic + ".png"), 
					RFHistogram.buildRFHistogram(ic),
					width, height);
			ChartUtilities.saveChartAsPNG(new File(IMAGES_DIR.getPath() + File.separator + mOutputFile.getName() + "_eu_" + ic + ".png"), 
					RFHistogram.buildEuclideanHistogram(ic),
					width, height);
		} catch (IOException e) {
//			e.printStackTrace();
		}
	}
}
