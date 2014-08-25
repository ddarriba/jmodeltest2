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
package es.uvigo.darwin.jmodeltest.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.BorderUIResource;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.selection.AIC;
import es.uvigo.darwin.jmodeltest.selection.AICc;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public class Frame_AIC extends JModelTestFrame {

	private static final long serialVersionUID = 201104031100L;

	private JPanel panelAICSettings = new JPanel();
	private JTextField textTaxaAIC = new JTextField();
	private JTextField textSizeAICc = new JTextField();
	private JButton runButtonAIC = new JButton();
	private JButton cancelButtonAIC = new JButton();
	private JButton buttonDefaultAIC = new JButton();
	private JCheckBox checkBoxAICc = new JCheckBox();

	private JCheckBox checkBoxPAUPblock = new JCheckBox();
	private JCheckBox checkBoxAveraging = new JCheckBox();
	private JCheckBox checkBoxImportance = new JCheckBox();
	private JSlider sliderInterval = new JSlider();

	private AIC myAIC;
	private AICc myAICc;

	public Frame_AIC() {
	}

	public void initComponents() throws Exception {
		panelAICSettings.setSize(490, 240);
		panelAICSettings
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(new java.awt.Color(
								153, 153, 153), 1, false), "AIC Settings", 4,
						2, new java.awt.Font("Application", 1, 10),
						new java.awt.Color(102, 102, 153)));
		panelAICSettings.setLocation(10, 10);
		panelAICSettings.setVisible(true);
		panelAICSettings.setLayout(null);

		runButtonAIC.setVisible(true);
		runButtonAIC.setSize(190, 40);
		runButtonAIC.setText("Do AIC calculations");
		runButtonAIC.setLocation(280, 190);
		getRootPane().setDefaultButton(runButtonAIC);

		buttonDefaultAIC.setVisible(true);
		buttonDefaultAIC.setSize(141, 40);
		buttonDefaultAIC.setText("Default Settings");
		buttonDefaultAIC.setLocation(10, 190);

		cancelButtonAIC.setVisible(true);
		cancelButtonAIC.setSize(110, 40);
		cancelButtonAIC.setText("Cancel");
		cancelButtonAIC.setLocation(160, 190);

		checkBoxAICc.setVisible(true);
		checkBoxAICc.setSize(170, 20);
		checkBoxAICc.setText("Use AICc correction");
		checkBoxAICc.setLocation(30, 30);
		checkBoxAICc.setSelected(false);

		textSizeAICc.setEnabled(false);
		textSizeAICc
				.setToolTipText("Enter the sample size you want to use for the AICc correction and click RETURN. By default this is the number of sites in the alignment");
		textSizeAICc
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(new java.awt.Color(
								153, 153, 153), 1, false), "Sample size", 4, 2,
						new java.awt.Font("Application", 1, 10),
						new java.awt.Color(102, 102, 153)));
		textSizeAICc.setVisible(true);
		textSizeAICc.setSize(170, 40);
		textSizeAICc.setText(Utilities.format(options.getSampleSize(),10,4,false));
		textSizeAICc.setHorizontalAlignment(JTextField.RIGHT);
		textSizeAICc.setLocation(300, 20);

		checkBoxImportance.setVisible(true);
		checkBoxImportance.setSize(260, 20);
		checkBoxImportance.setText("Calculate parameter importances");
		checkBoxImportance.setLocation(30, 70);
		checkBoxImportance.setSelected(true);

		checkBoxAveraging.setVisible(true);
		checkBoxAveraging.setSize(260, 20);
		checkBoxAveraging.setText("Do model averaging");
		checkBoxAveraging.setLocation(30, 110);
		checkBoxAveraging.setSelected(true);

		checkBoxPAUPblock.setVisible(true);
		checkBoxPAUPblock.setSize(260, 20);
		checkBoxPAUPblock.setText("Write PAUP* block");
		checkBoxPAUPblock.setLocation(30, 150);
		checkBoxPAUPblock.setSelected(false);
		checkBoxPAUPblock
				.setToolTipText("Writes a block of PAUP* commands implementing the selected model");

		sliderInterval.setVisible(true);
		sliderInterval
				.setToolTipText("Set the confidence interval for model averaging and/or parameter importance");
		sliderInterval
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(new java.awt.Color(
								153, 153, 153), 1, false),
						"Confidence interval = 100%", 4, 2, new java.awt.Font(
								"Application", 1, 10), new java.awt.Color(102,
								102, 153)));
		sliderInterval.setSize(170, 70);
		sliderInterval.setLocation(300, 105);
		sliderInterval.setMinimum(0);
		sliderInterval.setMaximum(100);
		sliderInterval.setValue(100);
		sliderInterval.setMajorTickSpacing(20);
		sliderInterval.setMinorTickSpacing(5);
		sliderInterval.setPaintTicks(true);
		sliderInterval.setPaintLabels(true);
		sliderInterval.setEnabled(true);
		sliderInterval.setFont(XManager.FONT_SLIDER);

		setLocation(281, 80);
		getContentPane().setLayout(null);
		setTitle("Akaike Information Criterion (AIC) Settings");

		panelAICSettings.add(runButtonAIC);
		panelAICSettings.add(buttonDefaultAIC);
		panelAICSettings.add(cancelButtonAIC);
		panelAICSettings.add(checkBoxAICc);
		panelAICSettings.add(textTaxaAIC);
		panelAICSettings.add(textSizeAICc);
		// PanelAICSettings.add(jCheckBoxIncludeBL_AIC);
		panelAICSettings.add(checkBoxAveraging);
		panelAICSettings.add(checkBoxImportance);
		panelAICSettings.add(checkBoxPAUPblock);
		panelAICSettings.add(sliderInterval);
		getContentPane().add(panelAICSettings);

		setSize(510, 280);
		setResizable(false);

		// event handling

		checkBoxAICc.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				jCheckBoxAICcStateChanged(e);
			}
		});

		checkBoxAveraging
				.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						jCheckBoxAveragingStateChanged(e);
					}
				});

		checkBoxImportance
				.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						jCheckBoxImportanceStateChanged(e);
					}
				});

		sliderInterval
				.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						JSliderIntervalStateChanged(e);
					}
				});
		
		buttonDefaultAIC
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						JButtonDefaultAICActionPerformed(e);
					}
				});
		cancelButtonAIC.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				CancelButtonAICActionPerformed(e);
			}
		});
		runButtonAIC.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				RunButtonAICActionPerformed(e);
			}
		});

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});

	}

	private boolean mShown = false;

	public void addNotify() {
		super.addNotify();

		if (mShown)
			return;

		// resize frame to account for menubar
		JMenuBar jMenuBar = getJMenuBar();
		if (jMenuBar != null) {
			int jMenuBarHeight = jMenuBar.getPreferredSize().height;
			Dimension dimension = getSize();
			dimension.height += jMenuBarHeight;
			setSize(dimension);

			// move down components in layered pane
			Component[] components = getLayeredPane().getComponentsInLayer(
					JLayeredPane.DEFAULT_LAYER.intValue());
			for (int i = 0; i < components.length; i++) {
				Point location = components[i].getLocation();
				location.move(location.x, location.y + jMenuBarHeight);
				components[i].setLocation(location);
			}
		}

		mShown = true;
	}

	// Close the window when the close box is clicked
	void thisWindowClosing(java.awt.event.WindowEvent e) {
		setVisible(false);
		dispose();
		// System.exit(0);
	}

	public void jCheckBoxAICcStateChanged(ChangeEvent e) {
		if (checkBoxAICc.isSelected()) {
			textSizeAICc.setEnabled(true);
			runButtonAIC.setText("Do AICc calculations");
		} else {
			textSizeAICc.setEnabled(false);
			runButtonAIC.setText("Do AIC calculations");
		}
	}

	public void jCheckBoxImportanceStateChanged(ChangeEvent e) {
		if (checkBoxImportance.isSelected()) {
			sliderInterval.setEnabled(true);
		} else {
			if (!checkBoxAveraging.isSelected())
				sliderInterval.setEnabled(false);
		}
	}

	public void jCheckBoxAveragingStateChanged(ChangeEvent e) {
		if (checkBoxAveraging.isSelected()) {
			sliderInterval.setEnabled(true);
		} else {
			if (!checkBoxImportance.isSelected())
				sliderInterval.setEnabled(false);
		}
	}

	public void JSliderIntervalStateChanged(ChangeEvent e) {
		sliderInterval
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(new java.awt.Color(
								153, 153, 153), 1, false),
						"Confidence interval = " + sliderInterval.getValue()
								+ "%", 4, 2, new java.awt.Font("Application",
								1, 10), new java.awt.Color(102, 102, 153)));
	}

	public void JButtonDefaultAICActionPerformed(java.awt.event.ActionEvent e) {
		checkBoxAICc.setSelected(false);
		textSizeAICc.setEnabled(false);
		runButtonAIC.setText("Do AIC calculations");
		ApplicationOptions.getInstance().countBLasParameters = true;
		XManager.getInstance().selectedMenuResultsBLasParameters(true);
		checkBoxPAUPblock.setSelected(false);
		checkBoxAveraging.setSelected(true);
		checkBoxImportance.setSelected(true);
		sliderInterval.setValue(100);
	}

	public void CancelButtonAICActionPerformed(java.awt.event.ActionEvent e) {
		try {
			setVisible(false);
			dispose();
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	public void RunButtonAICActionPerformed(java.awt.event.ActionEvent e) {
		boolean writePAUPblock, doImportances, doModelAveraging;
		double credibleInterval;

		try {
			setVisible(false);
			dispose();

			writePAUPblock = checkBoxPAUPblock.isSelected();
			options.writePAUPblock |= writePAUPblock;
			doImportances = checkBoxImportance.isSelected();
			doModelAveraging = checkBoxAveraging.isSelected();

			credibleInterval = sliderInterval.getValue() / 100.0;

			if (checkBoxAICc.isSelected()) {
				myAICc = new AICc(writePAUPblock, doImportances,
						doModelAveraging, credibleInterval);
				myAICc.compute();
				myAICc.print(ModelTest.getCurrentOutStream());
				ModelTest.setMyAICc(myAICc);
				options.doAICc = myAICc != null;
				XManager.getInstance().resultsFrame.enablePane(FrameResults.TAB_AICc);
				XManager.getInstance().resultsFrame.populate(FrameResults.TAB_AICc);
			} else {
				myAIC = new AIC(writePAUPblock, doImportances,
						doModelAveraging, credibleInterval);
				myAIC.compute();
				myAIC.print(ModelTest.getCurrentOutStream());
				ModelTest.setMyAIC(myAIC);
				options.doAIC = myAIC != null;
				XManager.getInstance().resultsFrame.enablePane(FrameResults.TAB_AIC);
				XManager.getInstance().resultsFrame.populate(FrameResults.TAB_AIC);
			}
			XManager.getInstance().enableMenuAveraging(!options.fixedTopology);
			
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

}
