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
