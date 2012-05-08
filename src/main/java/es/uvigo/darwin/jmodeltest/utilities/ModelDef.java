package es.uvigo.darwin.jmodeltest.utilities;

import es.uvigo.darwin.jmodeltest.model.Model;

public class ModelDef {

	final String name;
	final String partition;
	final boolean equalFreqs;
	final boolean equalRates[];
	
	ModelDef(Model model) {
		this.name = model.getName();
		this.equalFreqs = !model.ispF();
		this.partition = model.getPartition();
		this.equalRates = new boolean[] { 
				checkRates(0,1), checkRates(0,2), checkRates(0,3),
				checkRates(0,4), checkRates(0,5), checkRates(1,2),
				checkRates(1,3), checkRates(1,4), checkRates(1,5),
				checkRates(2,3), checkRates(2,4), checkRates(2,5),
				checkRates(3,4), checkRates(3,5), checkRates(4,5)};
	}

	private boolean checkRates(int p0, int p1) {
		return partition.charAt(p0) == partition.charAt(p1);
	}
	public String getName() {
		return name;
	}

	public boolean isEqualFreqs() {
		return equalFreqs;
	}

	public boolean isEqualRate(int aaPair) {
		return equalRates[aaPair];
	}

	public boolean isName(String name) {
		//!name.contains("\\+") || 
		return name.split("\\+")[0].equals(this.name);
	}
}
