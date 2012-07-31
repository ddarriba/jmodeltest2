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
package es.uvigo.darwin.jmodeltest.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public abstract class ModelConstants {
	// Model names
	public static final String[] modelName = { "JC", "JC+I", "JC+G", "JC+I+G",
			"F81", "F81+I", "F81+G", "F81+I+G", "K80", "K80+I", "K80+G",
			"K80+I+G", "HKY", "HKY+I", "HKY+G", "HKY+I+G", "TrNef", "TrNef+I",
			"TrNef+G", "TrNef+I+G", "TrN", "TrN+I", "TrN+G", "TrN+I+G", "TPM1",
			"TPM1+I", "TPM1+G", "TPM1+I+G", "TPM1uf", "TPM1uf+I", "TPM1uf+G",
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
	public static final List<String> codeList = Arrays
			.asList(ModelConstants.modelCode);

	// custom String for substitution types
	public static final Hashtable<Integer, String[]> fullModelSet;

	static {
		fullModelSet = new Hashtable<Integer, String[]>();
		fullModelSet.put(6, new String[] { "012345" });
		fullModelSet.put(5, new String[] { "001234", "010234", "011234",
				"012034", "012134", "012234", "012304", "012314", "012324",
				"012334", "012340", "012341", "012342", "012343", "012344" });
		fullModelSet.put(4, new String[] { "000123", "001023", "001123",
				"001203", "001213", "001223", "001230", "001231", "001232",
				"001233", "010023", "010123", "010203", "010213", "010223",
				"010230", "010231", "010232", "010233", "011023", "011123",
				"011203", "011213", "011223", "011230", "011231", "011232",
				"011233", "012003", "012013", "012023", "012030", "012031",
				"012032", "012033", "012103", "012113", "012123", "012130",
				"012131", "012132", "012133", "012203", "012213", "012223",
				"012230", "012231", "012232", "012233", "012300", "012301",
				"012302", "012303", "012310", "012311", "012312", "012313",
				"012320", "012321", "012322", "012323", "012330", "012331",
				"012332", "012333" });
		fullModelSet.put(3, new String[] { "000012", "000102", "000112",
				"000120", "000121", "000122", "001002", "001012", "001020",
				"001021", "001022", "001102", "001112", "001120", "001121",
				"001122", "001200", "001201", "001202", "001210", "001211",
				"001212", "001220", "001221", "001222", "010002", "010012",
				"010020", "010021", "010022", "010102", "010112", "010120",
				"010121", "010122", "010200", "010201", "010202", "010210",
				"010211", "010212", "010220", "010221", "010222", "011002",
				"011012", "011020", "011021", "011022", "011102", "011112",
				"011120", "011121", "011122", "011200", "011201", "011202",
				"011210", "011211", "011212", "011220", "011221", "011222",
				"012000", "012001", "012002", "012010", "012011", "012012",
				"012020", "012021", "012022", "012100", "012101", "012102",
				"012110", "012111", "012112", "012120", "012121", "012122",
				"012200", "012201", "012202", "012210", "012211", "012212",
				"012220", "012221", "012222" });
		fullModelSet.put(2, new String[] { "000001", "000010", "000011",
				"000100", "000101", "000110", "000111", "001000", "001001",
				"001010", "001011", "001100", "001101", "001110", "001111",
				"010000", "010001", "010010", "010011", "010100", "010101",
				"010110", "010111", "011000", "011001", "011010", "011011",
				"011100", "011101", "011110", "011111" });
		fullModelSet.put(1, new String[] { "000000" });
	}

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

//	public static final int getNumberOfTransitions(String partition) {
//		System.out.println(" CHECKING " + partition);
//		Integer ti0 = Integer.parseInt(partition.substring(1, 2));
//		Integer ti1 = Integer.parseInt(partition.substring(4, 5));
//		System.out.println(" CHECKING " + ti0 + " and " +  ti1);
//		if (ti0 == ti1) {
//			return 1;
//		} else {
//			return 2;
//		}
//	}
//
//	public static final int getNumberOfTransversions(String partition) {
//		List<Integer> parts = new ArrayList<Integer>();
//		int[] transitions = { 0, 2, 3, 5 };
//		for (int i : transitions) {
//			Integer current_part = Integer.parseInt(partition.substring(i, i+1));
//			if (!parts.contains(current_part)) {
//				parts.add(current_part);
//			}
//		}
//		return parts.size();
//	}

	// base frequencies restrictions
	public static final boolean[] equalBaseFrequencies = { true, true, true,
			true, // JC
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
