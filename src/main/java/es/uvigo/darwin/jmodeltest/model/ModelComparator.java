package es.uvigo.darwin.jmodeltest.model;

import java.util.Comparator;

public class ModelComparator implements Comparator<Model> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Model model1, Model model2) {
		int value = 0;
		if (model1 != null && model2 != null)
			value = getWeight(model2) - getWeight(model1);
		return value;
	}

	private int getWeight(Model model) {
		int weight = 0;
		if (model.ispG())
			weight += 4;
		if (model.ispI())
			weight += 2;
		if (model.ispF())
			weight += 1;
		return weight;
	}
}
