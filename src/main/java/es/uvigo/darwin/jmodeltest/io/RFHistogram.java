package es.uvigo.darwin.jmodeltest.io;

import java.util.List;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import pal.tree.Tree;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.selection.InformationCriterion;
import es.uvigo.darwin.jmodeltest.tree.TreeDistancesCache;
import es.uvigo.darwin.jmodeltest.tree.TreeEuclideanDistancesCache;
import es.uvigo.darwin.jmodeltest.tree.TreeRFDistancesCache;

public class RFHistogram {

	private static JFreeChart buildHistogram(double[] values, int steps,
			String plotTitle, String xAxis, String yAxis) {
		HistogramDataset hds = new HistogramDataset();
		hds.setType(HistogramType.RELATIVE_FREQUENCY);
		hds.addSeries(1, values, steps);
		PlotOrientation orientation = PlotOrientation.VERTICAL;
		boolean show = false;
		boolean toolTips = false;
		boolean urls = false;
		JFreeChart chart = ChartFactory.createHistogram(plotTitle, xAxis,
				yAxis, hds, orientation, show, toolTips, urls);
		return chart;
	}
	
	
	public static JFreeChart buildDistancesHistogram(InformationCriterion ic, TreeDistancesCache distances, String plotTitle) {
		List<Model> models = ic.getConfidenceModels();
		
		Tree bestTree = ic.getMinModel().getTree();
		double values[] = new double[models.size()-1];
		int i = 0;
		for (Model model : models) {
			if (!model.equals(ic.getMinModel())) {
				double distance = distances.getDistance(bestTree, model.getTree());
				values[i] = distance;
				i++;
			}
		}
		String xaxis = "distance";
		String yaxis = "count";
		int steps = 10;
		JFreeChart chart = buildHistogram(values, steps,
				plotTitle, xaxis, yaxis);
		
		return chart;
	}

	public static JFreeChart buildRFHistogram(InformationCriterion ic) {
		return buildDistancesHistogram(ic, TreeRFDistancesCache.getInstance(),
				ic + " RF distances histogram");
	}
	
	public static JFreeChart buildEuclideanHistogram(InformationCriterion ic) {
		return buildDistancesHistogram(ic, TreeEuclideanDistancesCache.getInstance(),
				ic + " euclidean distances histogram");
	}
	
	public static JFreeChart buildRandomHistogram() {
		Random generator = new Random();
		double[] value = new double[100];
		for (int i = 1; i < 100; i++) {
			value[i] = generator.nextDouble();
		}
		int number = 10;
		String plotTitle = "Histogram";
		String xaxis = "number";
		String yaxis = "value";
		JFreeChart chart = buildHistogram(value, number, plotTitle, xaxis,
				yaxis);
		return chart;
	}
}