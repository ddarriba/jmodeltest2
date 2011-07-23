package es.uvigo.darwin.jmodeltest.gui;

import javax.swing.JFrame;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;

public class JModelTestFrame extends JFrame {
	
	private static final long serialVersionUID = -8636558779921904218L;
	
	protected ApplicationOptions options;
	
	public JModelTestFrame() {
		options = ApplicationOptions.getInstance();
	}

}
