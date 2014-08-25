package es.uvigo.darwin.jmodeltest.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import pal.tree.Tree;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public class TreeSummary {

	private int AIC_INDEX = 0;
	private int AICC_INDEX = 1;
	private int BIC_INDEX = 2;
	private int DT_INDEX = 3;
	// number of Information Criteria
	private int IC_COUNT = 4;

	private Tree bestTree[] = new Tree[IC_COUNT];
	private TreeDistancesCache rfDistances = TreeRFDistancesCache.getInstance();
	private TreeDistancesCache euclideanDistances = TreeEuclideanDistancesCache
			.getInstance();
	private Hashtable<Tree, SummaryRow> summary;
	// sorted topologies by criterion
	private List<Tree> topologiesAIC = null, topologiesAICc = null,
			topologiesBIC = null, topologiesDT = null, sortedTopologies = null;

	public TreeSummary(Tree bestAIC, Tree bestAICc, Tree bestBIC, Tree bestDT,
			Model[] models) {
		if (bestAIC != null) {
			this.bestTree[AIC_INDEX] = bestAIC;
			this.topologiesAIC = new ArrayList<Tree>();
		}
		if (bestAICc != null) {
			this.bestTree[AICC_INDEX] = bestAICc;
			this.topologiesAICc = new ArrayList<Tree>();
		}
		if (bestBIC != null) {
			this.bestTree[BIC_INDEX] = bestBIC;
			this.topologiesBIC = new ArrayList<Tree>();
		}
		if (bestDT != null) {
			this.bestTree[DT_INDEX] = bestDT;
			this.topologiesDT = new ArrayList<Tree>();
		}
		this.summary = new Hashtable<Tree, SummaryRow>();

		for (Model model : models) {
			boolean done = false;
			for (Tree tree : summary.keySet()) {
				if (sameTopology(tree, model.getTree())) {
					summary.get(tree).addModel(model);
					done = true;
					break;
				}
			}
			if (!done) {
				// new topology
				SummaryRow newRow = new SummaryRow(model.getTree());
				newRow.addModel(model);
				summary.put(model.getTree(), newRow);
				if (topologiesAIC != null)
					topologiesAIC.add(model.getTree());
				if (topologiesAICc != null)
					topologiesAICc.add(model.getTree());
				if (topologiesBIC != null)
					topologiesBIC.add(model.getTree());
				if (topologiesDT != null)
					topologiesDT.add(model.getTree());
			}
		}

		for (Tree tree : summary.keySet()) {
			summary.get(tree).computeEuclideanDistances();
		}
		// sort by criteria
		if (topologiesAIC != null)
			Collections.sort(topologiesAIC, new AicComparator());
		if (topologiesAICc != null)
			Collections.sort(topologiesAICc, new AiccComparator());
		if (topologiesBIC != null)
			Collections.sort(topologiesBIC, new BicComparator());
		if (topologiesDT != null)
			Collections.sort(topologiesDT, new DtComparator());
		
		sortedTopologies = topologiesAIC != null ? topologiesAIC
				: topologiesBIC != null ? topologiesBIC
						: topologiesAICc != null ? topologiesAICc
								: topologiesDT != null ? topologiesDT
									: new ArrayList<Tree>(summary.keySet());
	}

	public int getNumberOfTopologies() {
		return summary.size();
	}

	public Tree getTopology (int index) {
		return sortedTopologies.get(index);
	}
	
	public List<Model> getModelsByTopology (int index) {
		return summary.get(getTopology(index)).models;
	}
	
	public List<Model> getAICModels(int index) {
		Tree key = topologiesAIC.get(index);
		return summary.get(key).models;
	}

	public List<Model> getBICModels(int index) {
		Tree key = topologiesBIC.get(index);
		return summary.get(key).models;
	}

	public List<Model> getAICcModels(int index) {
		Tree key = topologiesAICc.get(index);
		return summary.get(key).models;
	}

	public List<Model> getDTModels(int index) {
		Tree key = topologiesDT.get(index);
		return summary.get(key).models;
	}

	public int aicIndexOf(Tree tree) {
		return topologiesAIC.indexOf(tree);
	}

	public long aicRfOf(Tree tree) {
		return summary.get(tree).rfDistance[AIC_INDEX];
	}
	
	public double aicAvgDistance(Tree tree) {
		return summary.get(tree).avgEuclideanDistance[AIC_INDEX];
	}
	
	public double aicVarDistance(Tree tree) {
		return summary.get(tree).varEuclideanDistance[AIC_INDEX];
	}
	
	public double aiccWeight(Tree tree) {
		return summary.get(tree).support[AICC_INDEX];
	}
	
	public int aiccIndexOf(Tree tree) {
		return topologiesAICc.indexOf(tree);
	}

	public long aiccRfOf(Tree tree) {
		return summary.get(tree).rfDistance[AICC_INDEX];
	}
	
	public double aiccAvgDistance(Tree tree) {
		return summary.get(tree).avgEuclideanDistance[AICC_INDEX];
	}
	
	public double aiccVarDistance(Tree tree) {
		return summary.get(tree).varEuclideanDistance[AICC_INDEX];
	}
	
	public double aicWeight(Tree tree) {
		return summary.get(tree).support[AIC_INDEX];
	}
	
	public int bicIndexOf(Tree tree) {
		return topologiesBIC.indexOf(tree);
	}

	public long bicRfOf(Tree tree) {
		return summary.get(tree).rfDistance[BIC_INDEX];
	}
	
	public double bicAvgDistance(Tree tree) {
		return summary.get(tree).avgEuclideanDistance[BIC_INDEX];
	}
	
	public double bicVarDistance(Tree tree) {
		return summary.get(tree).varEuclideanDistance[BIC_INDEX];
	}
	
	public double bicWeight(Tree tree) {
		return summary.get(tree).support[BIC_INDEX];
	}
	
	public int dtIndexOf(Tree tree) {
		return topologiesDT.indexOf(tree);
	}

	public long dtRfOf(Tree tree) {
		return summary.get(tree).rfDistance[DT_INDEX];
	}
	
	public double dtAvgDistance(Tree tree) {
		return summary.get(tree).avgEuclideanDistance[DT_INDEX];
	}
	
	public double dtVarDistance(Tree tree) {
		return summary.get(tree).varEuclideanDistance[DT_INDEX];
	}
	
	public double dtWeight(Tree tree) {
		return summary.get(tree).support[DT_INDEX];
	}
	
	private boolean sameTopology(Tree t1, Tree t2) {
		return (rfDistances.getDistance(t1, t2) == 0);
	}

	private long rfDistance(Tree t1, Tree t2) {
		if (t1 == null || t2 == null) {
			return -1;
		} else {
			return Math.round(rfDistances.getDistance(t1, t2));
		}
	}

	class SummaryRow {
		List<Model> models;
		Tree commonTopology;
		long rfDistance[] = new long[] { 0l, 0l, 0l, 0l };
		double avgEuclideanDistance[] = new double[] { 0.0d, 0.0d, 0.0d, 0.0d };
		double varEuclideanDistance[] = new double[] { 0.0d, 0.0d, 0.0d, 0.0d };
		double support[] = new double[] { 0.0d, 0.0d, 0.0d, 0.0d };

		SummaryRow(Tree commonTopology) {
			this.commonTopology = commonTopology;
			for (int i = 0; i < IC_COUNT; i++) {
				this.rfDistance[i] = rfDistance(commonTopology, bestTree[i]);
			}
			this.models = new ArrayList<Model>();
		}

		void addModel(Model model) {
			// check same topology
			if (checkTopology(model)) {
				models.add(model);
				support[AIC_INDEX] += model.getAICw();
				support[AICC_INDEX] += model.getAICcw();
				support[BIC_INDEX] += model.getBICw();
				support[DT_INDEX] += model.getDTw();
			}
		}

		void computeEuclideanDistances() {
			// set variables to zero
			for (int i = 0; i < IC_COUNT; i++) {
				if (bestTree[i] != null) {
					avgEuclideanDistance[i] = 0.0d;
					varEuclideanDistance[i] = 0.0d;
					for (Model model : models) {
						double distance = euclideanDistances.getDistance(
								model.getTree(), bestTree[i]);
						avgEuclideanDistance[i] += distance;
						varEuclideanDistance[i] += distance * distance;
					}
					avgEuclideanDistance[i] /= models.size();
					varEuclideanDistance[i] /= models.size();
					varEuclideanDistance[i] -= avgEuclideanDistance[i]
							* avgEuclideanDistance[i];
				}
			}
		}

		boolean checkTopology(Model model) {
			return (rfDistances.getDistance(commonTopology, model.getTree()) == 0);
		}
	}

	class AicComparator implements Comparator<Tree> {

		@Override
		public int compare(Tree o1, Tree o2) {
			long d1 = rfDistance(o1, bestTree[AIC_INDEX]);
			long d2 = rfDistance(o2, bestTree[AIC_INDEX]);
			return (int) (d1 - d2);
		}

	}

	class AiccComparator implements Comparator<Tree> {

		@Override
		public int compare(Tree o1, Tree o2) {
			long d1 = rfDistance(o1, bestTree[AICC_INDEX]);
			long d2 = rfDistance(o2, bestTree[AICC_INDEX]);
			return (int) (d1 - d2);
		}

	}

	class BicComparator implements Comparator<Tree> {

		@Override
		public int compare(Tree o1, Tree o2) {
			long d1 = rfDistance(o1, bestTree[BIC_INDEX]);
			long d2 = rfDistance(o2, bestTree[BIC_INDEX]);
			return (int) (d1 - d2);
		}

	}

	class DtComparator implements Comparator<Tree> {

		@Override
		public int compare(Tree o1, Tree o2) {
			long d1 = rfDistance(o1, bestTree[DT_INDEX]);
			long d2 = rfDistance(o2, bestTree[DT_INDEX]);
			return (int) (d1 - d2);
		}

	}

	public void print(TextOutputStream stream) {
		int MAX_LEN = 80;
		if (topologiesAIC != null || topologiesBIC != null
				|| topologiesAICc != null || topologiesDT != null) {
			List<Tree> sortedTopologies = topologiesAIC != null ? topologiesAIC
					: topologiesBIC != null ? topologiesBIC
							: topologiesAICc != null ? topologiesAICc
									: topologiesDT;
			stream.println("::Optimized Topologies Summary::");
			stream.println("");
			stream.println("There are " + getNumberOfTopologies()
					+ " different topologies.");
			stream.println("");
			for (int i = 0; i < getNumberOfTopologies(); i++) {
				int index = i + 1;
				Tree currentTree = sortedTopologies.get(i);

				SummaryRow summaryRow = summary.get(currentTree);

				int aicIndex = topologiesAIC != null ? aicIndexOf(currentTree) + 1
						: -1;
				int aiccIndex = topologiesAICc != null ? aiccIndexOf(currentTree) + 1
						: -1;
				int bicIndex = topologiesBIC != null ? bicIndexOf(currentTree) + 1
						: -1;
				int dtIndex = topologiesDT != null ? dtIndexOf(currentTree) + 1
						: -1;
				stream.println("Topology Id: " + index);
				stream.println("\tRank\tWeight\t\t RF\tAvgEucl\t\tVarEucl");
				if (topologiesAIC != null) {
						stream.println(getIcRow("AIC", aicIndex, summaryRow, AIC_INDEX));
				}
				if (topologiesBIC != null) {
					stream.println(getIcRow("BIC", bicIndex, summaryRow, BIC_INDEX));
				}
				if (topologiesAICc != null) {
					stream.println(getIcRow("AICc", aiccIndex, summaryRow, AICC_INDEX));
				}
				if (topologiesDT != null) {
					stream.println(getIcRow("DT", dtIndex, summaryRow, DT_INDEX));
				}
				stream.println("Models supporting:   "
						+ summaryRow.models.size());

				stream.print("                     ");
				int chars = 0;
				for (Model model : summaryRow.models) {
					if (chars >= MAX_LEN) {
						stream.println("");
						stream.print("                     ");
						chars = 0;
					}
					stream.print(model.getName() + " ");
					chars += model.getName().length() + 1;
				}
				stream.println("");
				stream.println("");
			}
		}
	}
	
	private String getIcRow(String name, int rankIndex, SummaryRow summaryRow, int criterionIndex) {
		return name + "\t"
				+ Utilities.format(rankIndex,3,0,false)
				+ "\t"
				+ Utilities
						.asPercent(summaryRow.support[criterionIndex] * 100)
				+ "\t\t" + Utilities.format(summaryRow.rfDistance[criterionIndex],3,0,false)
				+ "\t" + Utilities.format(summaryRow.avgEuclideanDistance[criterionIndex],8,2,true)
				+ "\t" + Utilities.format(summaryRow.varEuclideanDistance[criterionIndex],8,2,true);
	}
}
