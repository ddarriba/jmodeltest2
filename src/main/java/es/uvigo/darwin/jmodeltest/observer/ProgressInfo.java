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
