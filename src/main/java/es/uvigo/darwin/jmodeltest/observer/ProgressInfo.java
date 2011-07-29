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
	public static final int SINGLE_OPTIMIZATION_INIT = 3;
	public static final int SINGLE_OPTIMIZATION_COMPLETED = 4;
	public static final int OPTIMIZATION_COMPLETED_OK = 5;
	public static final int OPTIMIZATION_COMPLETED_INTERRUPTED = 6;
	public static final int INTERRUPTED = 7;
	public static final int ERROR = 8;
	
	private int type;
	private Model model;
	private int value;
	private String message;
	
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
}
