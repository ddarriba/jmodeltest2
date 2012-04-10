package es.uvigo.darwin.jmodeltest.utilities;

public enum ModelDef {
	JC("JC", true, true, true, true, true, true, true, true, true, true, true,
			true, true, true, true, true), F81("F81", false, true, true, true,
			true, true, true, true, true, true, true, true, true, true, true,
			true), HKY("HKY", true, false, true, true, false, true, false,
			false, true, false, true, false, true, false, true, false), K80(
			"K80", false, false, true, true, false, true, false, false, true,
			false, true, false, true, false, true, false), TRN("TrN", true,
			false, true, true, false, true, false, false, false, false, true,
			false, true, false, true, false), TRNef("TrNef", false, false,
			true, true, false, true, false, false, false, false, true, false,
			true, false, true, false), TPM1uf("TPM1uf", true, false, false,
			false, false, true, false, false, true, false, true, false, false,
			false, false, false), TPM1("TPM1", false, false, false, false,
			false, true, false, false, true, false, true, false, false, false,
			false, false), TPM2uf("TPM2uf", true, false, true, false, false,
			false, false, false, true, false, false, false, false, false, true,
			false), TPM2("TPM2", false, false, true, false, false, false,
			false, false, true, false, false, false, false, false, true, false), TPM3uf(
			"TPM3uf", true, false, false, true, false, false, false, false,
			true, false, false, false, true, false, false, false), TPM3("TPM3",
			false, false, false, true, false, false, false, false, true, false,
			false, false, true, false, false, false), TIM1ef("TIM1ef", true,
			false, false, false, false, true, false, false, false, false, true,
			false, false, false, false, false), TIM1("TIM1", false, false,
			false, false, false, true, false, false, false, false, true, false,
			false, false, false, false), TIM2ef("TIM2ef", true, false, true,
			false, false, false, false, false, false, false, false, false,
			false, false, true, false), TIM2("TIM2", false, false, true, false,
			false, false, false, false, false, false, false, false, false,
			false, true, false), TIM3ef("TIM3ef", true, false, false, true,
			false, false, false, false, false, false, false, false, true,
			false, false, false), TIM3("TIM3", false, false, false, true,
			false, false, false, false, false, false, false, false, true,
			false, false, false), TVM("TVM", true, false, false, false, false,
			false, false, false, true, false, false, false, false, false,
			false, false), TVMef("TVMef", false, false, false, false, false,
			false, false, false, true, false, false, false, false, false,
			false, false), SYM("SYM", true, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false), GTR("GTR", false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false);

	final String name;
	final boolean equalFreqs;
	final boolean equalRates[];

	ModelDef(String name, boolean equalFreqs, boolean rAB, boolean rAC,
			boolean rAD, boolean rAE, boolean rAF, boolean rBC, boolean rBD,
			boolean rBE, boolean rBF, boolean rCD, boolean rCE, boolean rCF,
			boolean rDE, boolean rDF, boolean rEF) {
		this.name = name;
		this.equalFreqs = equalFreqs;
		this.equalRates = new boolean[] { rAB, rAC, rAD, rAE, rAF, rBC, rBD,
				rBE, rBF, rCD, rCE, rCF, rDE, rDF, rEF };
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
