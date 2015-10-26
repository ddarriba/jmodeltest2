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
