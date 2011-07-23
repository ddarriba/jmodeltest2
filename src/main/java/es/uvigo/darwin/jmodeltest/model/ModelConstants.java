package es.uvigo.darwin.jmodeltest.model;

//TODO: Maybe use a static class instead and include also the static methods
//      from Model ! ! !
public interface ModelConstants {
	// Model names
	public static final String[] modelName = { "JC", "JC+I", "JC+G", "JC+I+G", "F81",
			"F81+I", "F81+G", "F81+I+G", "K80", "K80+I", "K80+G", "K80+I+G",
			"HKY", "HKY+I", "HKY+G", "HKY+I+G", "TrNef", "TrNef+I", "TrNef+G",
			"TrNef+I+G", "TrN", "TrN+I", "TrN+G", "TrN+I+G", "TPM1", "TPM1+I",
			"TPM1+G", "TPM1+I+G", "TPM1uf", "TPM1uf+I", "TPM1uf+G",
			"TPM1uf+I+G", "TPM2", "TPM2+I", "TPM2+G", "TPM2+I+G", "TPM2uf",
			"TPM2uf+I", "TPM2uf+G", "TPM2uf+I+G", "TPM3", "TPM3+I", "TPM3+G",
			"TPM3+I+G", "TPM3uf", "TPM3uf+I", "TPM3uf+G", "TPM3uf+I+G",
			"TIM1ef", "TIM1ef+I", "TIM1ef+G", "TIM1ef+I+G", "TIM1", "TIM1+I",
			"TIM1+G", "TIM1+I+G", "TIM2ef", "TIM2ef+I", "TIM2ef+G",
			"TIM2ef+I+G", "TIM2", "TIM2+I", "TIM2+G", "TIM2+I+G", "TIM3ef",
			"TIM3ef+I", "TIM3ef+G", "TIM3ef+I+G", "TIM3", "TIM3+I", "TIM3+G",
			"TIM3+I+G", "TVMef", "TVMef+I", "TVMef+G", "TVMef+I+G", "TVM",
			"TVM+I", "TVM+G", "TVM+I+G", "SYM", "SYM+I", "SYM+G", "SYM+I+G",
			"GTR", "GTR+I", "GTR+G", "GTR+I+G" };

	// custom String for substitution types
	public static final String[] modelCode = { "000000", "000000", "000000",
			"000000", // JC
			"000000", "000000", "000000", "000000", // F81
			"010010", "010010", "010010", "010010", // K80
			"010010", "010010", "010010", "010010", // HKY
			"010020", "010020", "010020", "010020", // TrNef
			"010020", "010020", "010020", "010020", // TrN
			"012210", "012210", "012210", "012210", // TPM1=K81
			"012210", "012210", "012210", "012210", // TPM1uf=K81uf
			"010212", "010212", "010212", "010212", // TPM2
			"010212", "010212", "010212", "010212", // TPM2uf
			"012012", "012012", "012012", "012012", // TPM3
			"012012", "012012", "012012", "012012", // TPM3uf
			"012230", "012230", "012230", "012230", // TIM1ef
			"012230", "012230", "012230", "012230", // TIM1
			"010232", "010232", "010232", "010232", // TIM2ef
			"010232", "010232", "010232", "010232", // TIM2
			"012032", "012032", "012032", "012032", // TIM3ef
			"012032", "012032", "012032", "012032", // TIM3
			"012314", "012314", "012314", "012314", // TVMef
			"012314", "012314", "012314", "012314", // TVM
			"012345", "012345", "012345", "012345", // SYM
			"012345", "012345", "012345", "012345", // GTR
	};

	// number of free parameters for each model
	public static final int[] freeParameters = { 0, 1, 1, 2, // JC
			3, 4, 4, 5, // F81
			1, 2, 2, 3, // K80
			4, 5, 5, 6, // HKY
			2, 3, 3, 4, // TrNef
			5, 6, 6, 7, // TrN
			2, 3, 3, 4, // TPM1=K81
			5, 6, 6, 7, // TPM1uf=K81uf
			2, 3, 3, 4, // TPM2
			5, 6, 6, 7, // TPM2uf
			2, 3, 3, 4, // TPM3
			5, 6, 6, 7, // TPM3uf
			3, 4, 4, 5, // TIM1ef
			6, 7, 7, 8, // TIM1
			3, 4, 4, 5, // TIM2ef
			6, 7, 7, 8, // TIM2
			3, 4, 4, 5, // TIM3ef
			6, 7, 7, 8, // TIM3
			4, 5, 5, 6, // TVMef
			7, 8, 8, 9, // TVM
			5, 6, 6, 7, // SYM
			8, 9, 9, 10 // GTR
	};

	// different types of transition rates
	public static final int[] numTransitions = { 0, 0, 0, 0, // JC
			0, 0, 0, 0, // F81
			1, 1, 1, 1, // K80
			1, 1, 1, 1, // HKY
			2, 2, 2, 2, // TrNef
			2, 2, 2, 2, // TrN
			1, 1, 1, 1, // TPM1=K81
			1, 1, 1, 1, // TPM1uf=K81uf
			1, 1, 1, 1, // TPM2
			1, 1, 1, 1, // TPM2uf
			1, 1, 1, 1, // TPM3
			1, 1, 1, 1, // TPM3uf
			2, 2, 2, 2, // TIM1ef
			2, 2, 2, 2, // TIM1
			2, 2, 2, 2, // TIM2ef
			2, 2, 2, 2, // TIM2
			2, 2, 2, 2, // TIM3ef
			2, 2, 2, 2, // TIM3
			1, 1, 1, 1, // TVMef
			1, 1, 1, 1, // TVM
			2, 2, 2, 2, // SYM
			2, 2, 2, 2 // GTR
	};

	// different types of transversion rates
	public static final int[] numTransversions = { 0, 0, 0, 0, // JC
			0, 0, 0, 0, // F81
			1, 1, 1, 1, // K80
			1, 1, 1, 1, // HKY
			1, 1, 1, 1, // TrNef
			1, 1, 1, 1, // TrN
			2, 2, 2, 2, // TPM1=K81
			2, 2, 2, 2, // TPM1uf=K81uf
			2, 2, 2, 2, // TPM2
			2, 2, 2, 2, // TPM2uf
			2, 2, 2, 2, // TPM3
			2, 2, 2, 2, // TPM3uf
			2, 2, 2, 2, // TIM1ef
			2, 2, 2, 2, // TIM1
			2, 2, 2, 2, // TIM2ef
			2, 2, 2, 2, // TIM2
			2, 2, 2, 2, // TIM3ef
			2, 2, 2, 2, // TIM3
			4, 4, 4, 4, // TVMef
			4, 4, 4, 4, // TVM
			4, 4, 4, 4, // SYM
			4, 4, 4, 4 // GTR
	};

	// base frequencies restrictions
	public static final boolean[] equalBaseFrequencies = { true, true, true, true, // JC
			false, false, false, false, // F81
			true, true, true, true, // K81
			false, false, false, false, // HKY
			true, true, true, true, // TrNef
			false, false, false, false, // TrN
			true, true, true, true, // TPM1=K81
			false, false, false, false, // TPM1uf=K81uf
			true, true, true, true, // TPM2
			false, false, false, false, // TPM2uf
			true, true, true, true, // TPM3
			false, false, false, false, // TPM3uf
			true, true, true, true, // TIM1ef
			false, false, false, false, // TIM1
			true, true, true, true, // TIM2ef
			false, false, false, false, // TIM2
			true, true, true, true, // TIM3ef
			false, false, false, false, // TIM3
			true, true, true, true, // TVMef
			false, false, false, false, // TVM
			true, true, true, true, // SYM
			false, false, false, false // GTR
	};

	// base frequencies restrictions (0=none 1=+I 2=+G 3=+I+G)
	public static final int[] rateVariation = { 0, 1, 2, 3, // JC
			0, 1, 2, 3, // F81
			0, 1, 2, 3, // K80
			0, 1, 2, 3, // HKY
			0, 1, 2, 3, // TrNef
			0, 1, 2, 3, // TrN
			0, 1, 2, 3, // TPM1=K81
			0, 1, 2, 3, // TPM1uf=K81uf
			0, 1, 2, 3, // TPM2
			0, 1, 2, 3, // TPM2uf
			0, 1, 2, 3, // TPM3
			0, 1, 2, 3, // TPM3uf
			0, 1, 2, 3, // TIM1ef
			0, 1, 2, 3, // TIM1
			0, 1, 2, 3, // TIM2ef
			0, 1, 2, 3, // TIM2
			0, 1, 2, 3, // TIM3ef
			0, 1, 2, 3, // TIM3
			0, 1, 2, 3, // TVMef
			0, 1, 2, 3, // TVM
			0, 1, 2, 3, // SYM
			0, 1, 2, 3 // GTR
	};

	// 0 = part of the 3 substitution schemes = standard 24-model set (ModelTest
	// 1.0)
	// 1 = part of the 5 substitution schemes = standard 40-model set (ModelTest
	// 2.0)
	// 2 = part of the 7 substitution schemes = standard 56-model set (ModelTest
	// 3.0)
	// 3 = part of the 11 substitution schemes = standard 88-model set
	// (jModeltest)
	public static final int[] substType = { 0, 0, 0, 0, // JC
			0, 0, 0, 0, // F81
			0, 0, 0, 0, // K80
			0, 0, 0, 0, // HKY
			1, 1, 1, 1, // TrNef
			1, 1, 1, 1, // TrN
			1, 1, 1, 1, // TPM1=K81
			1, 1, 1, 1, // TPM1uf=K81uf
			3, 3, 3, 3, // TPM2
			3, 3, 3, 3, // TPM2uf
			3, 3, 3, 3, // TPM3
			3, 3, 3, 3, // TPM3uf
			2, 2, 2, 2, // TIM1ef
			2, 2, 2, 2, // TIM1
			3, 3, 3, 3, // TIM2ef
			3, 3, 3, 3, // TIM2
			3, 3, 3, 3, // TIM3ef
			3, 3, 3, 3, // TIM3
			2, 2, 2, 2, // TVMef
			2, 2, 2, 2, // TVM
			0, 0, 0, 0, // SYM
			0, 0, 0, 0 // GTR
	};
}
