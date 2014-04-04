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
package es.uvigo.darwin.jmodeltest.observer;

import es.uvigo.darwin.jmodeltest.model.Model;

public class ProgressInfo {

	public static final int NA = 0;
	public static final int BASE_TREE_INIT = 1;
	public static final int BASE_TREE_COMPUTED = 2;
	public static final int OPTIMIZATION_INIT = 3;
	public static final int SINGLE_OPTIMIZATION_INIT = 4;
	public static final int SINGLE_OPTIMIZATION_COMPLETED = 5;
	public static final int OPTIMIZATION_COMPLETED_OK = 6;
	public static final int OPTIMIZATION_COMPLETED_INTERRUPTED = 7;
	public static final int REOPTIMIZATION_INIT = 8;
	public static final int REOPTIMIZATION_COMPLETED = 9;
	public static final int GTR_OPTIMIZATION_INIT = 10;
	public static final int GTR_OPTIMIZATION_COMPLETED = 11;
	public static final int GTR_NOT_FOUND = 12;
	public static final int INTERRUPTED = 20;
	public static final int ERROR = 21;
	public static final int ERROR_BINARY_NOEXISTS = 22;
	public static final int ERROR_BINARY_NOEXECUTE = 23;
	
	public static final int VALUE_REGULAR_OPTIMIZATION = 1;
	public static final int VALUE_IGAPS_OPTIMIZATION = 2;
	
	
	private int type;
	private Model model;
	private int value;
	private String message;
	private boolean doHeuristicSearch = false;
	private int heuristicStage;
	private int numModelsInStage;
	
	public ProgressInfo(int type, int value, Model model, String message) {
		this.type = type;
		this.model = model;
		this.value = value;
		this.message = message;
	}

	public int getType() {
		return type;
	}
	
	public Model getModel() {
		return model;
	}

	public int getValue() {
		return value;
	}
	
	public String getMessage() {
		return message;
	}
	
	public boolean isHeuristicSearch() {
		return doHeuristicSearch;
	}
	
	public int getNumModelsInStage() {
		return numModelsInStage;
	}
	
	public void setNumModelsInStage(int numModelsInStage) {
		this.numModelsInStage = numModelsInStage;
	}
	
	public int getHeuristicStage() {
		return heuristicStage;
	}
	
	public void setHeuristicStage(int heuristicStage) {
		this.heuristicStage = heuristicStage;
		this.doHeuristicSearch = (heuristicStage > 0);
	}
}
