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
package es.uvigo.darwin.jmodeltest;

import java.awt.Color;
import java.awt.Font;
import java.io.PrintStream;
import java.util.Calendar;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import es.uvigo.darwin.jmodeltest.gui.FrameMain;
import es.uvigo.darwin.jmodeltest.gui.FrameResults;
import es.uvigo.darwin.jmodeltest.io.DocumentOutputStream;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public class XManager {

	private static XManager instance;
	
	public static final Font FONT_CONSOLE, FONT_LABEL, 
		FONT_MENU, FONT_STATUS, FONT_SLIDER, FONT_LABEL_BIG,
		FONT_TABULAR;
	public static final Color LABEL_BLUE_COLOR = new java.awt.Color(102,102,153);
	public static final Color LABEL_FAIL_COLOR = Color.red;
	
	public static SimpleAttributeSet redText;
	public static SimpleAttributeSet blackText;

	public FrameMain frame;
	public FrameResults resultsFrame;
	public static boolean resultsFrameBuilt = false;
	private JTextPane PANE;

	static {

		if (Utilities.isWindows() == false) {
			FONT_CONSOLE = new Font(Font.MONOSPACED, 0, 10);
		} else {
			FONT_CONSOLE = new Font("Lucida Console", 0, 11);
		}
		FONT_LABEL = new Font("Application", 1, 9);
		FONT_LABEL_BIG = new Font("Application", 0, 10);
		FONT_SLIDER = new Font("Application", 1, 8);
		FONT_MENU = new Font("Dialog", 0, 9);
		FONT_STATUS = new Font("Dialog", 0, 9);
		FONT_TABULAR = new java.awt.Font("Verdana", 0, 12);

		redText = new SimpleAttributeSet();
		blackText = new SimpleAttributeSet();

		StyleConstants.setForeground(redText, Color.red);
		StyleConstants.setForeground(blackText, Color.black);
	}

	private XManager() {

		try {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}

			frame = new FrameMain();
			frame.initComponents();
			frame.setVisible(true);
			PANE = frame.getMainEditorPane();

			ModelTest.setMainConsole(new TextOutputStream(new PrintStream(
					new DocumentOutputStream(PANE.getDocument()))));

			ModelTest.setCurrentOutStream(ModelTest.getMainConsole());
			ModelTest.printHeader(ModelTest.getMainConsole());
			ModelTest.printNotice(ModelTest.getMainConsole());
			ModelTest.printCitation(ModelTest.getMainConsole());

			// check expiration date
			// CheckExpiration (frame);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/****************************
	 * CheckExpiration*************************** * Checks for expiration and
	 * quits if needed * * *
	 ***********************************************************************/
	public static void CheckExpiration(JFrame theframe) {
		java.util.Calendar now = java.util.Calendar.getInstance();

		if ((now.get(Calendar.MONTH) != Calendar.JUNE && now.get(Calendar.MONTH) != Calendar.DECEMBER)
				|| now.get(Calendar.YEAR) != 2007) {
			JOptionPane.showMessageDialog(theframe,
					"Program has expired! \n    Bye...", "jModelTest warning",
					JOptionPane.WARNING_MESSAGE);
			theframe.dispose();
			System.exit(0);
		}
	}

	public void setPane(JTextPane pane) {
		PANE = pane;
	}

	public JTextPane getPane() {
		return PANE;
	}

	/****************************
	 * buildFrameResults **************************** * Builds the frame that
	 * displays the model selection results * *
	 ************************************************************************/

	public void buildFrameResults() {
		try {
			if (resultsFrameBuilt) {
				resultsFrame.dispose();
			}
			resultsFrame = new FrameResults();
			resultsFrame.initComponents();
			resultsFrame.setVisible(false);
			resultsFrameBuilt = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static XManager getInstance() {
		if (instance == null) {
			instance = new XManager();
		}
		return instance;
	}
	
	public void setLikeLabelText(String text) {
		frame.setLikeLabelText(text);
	}
	
	public void setDataLabelText(String text) {
		frame.setDataLabelText(text);
	}
	
	public void setLikeLabelColor(Color color) {
		frame.setLikeLabelColor(color);
	}
	
	public void enableMenuAIC(boolean enabled) {
		frame.enableMenuAIC(enabled);
	}
	
	public void enableMenuBIC(boolean enabled) {
		frame.enableMenuBIC(enabled);
	}
	
	public void enableMenuDT(boolean enabled) {
		frame.enableMenuDT(enabled);
	}
	
	public void enableMenuhLRT(boolean enabled) {
		frame.enableMenuhLRT(enabled);
	}
	
	public void enableMenuAveraging(boolean enabled) {
		frame.enableMenuAveraging(enabled);
	}
	
	public void enableMenuShowModelTable(boolean enabled) {
		frame.enableMenuShowModelTable(enabled);
	}
	
	public void selectedMenuResultsBLasParameters(boolean selected) {
		frame.selectedMenuResultsBLasParameters(selected);
	}
	
}
