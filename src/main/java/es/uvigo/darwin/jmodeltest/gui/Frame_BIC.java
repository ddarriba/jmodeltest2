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
import es.uvigo.darwin.jmodeltest.selection.BIC;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public class Frame_BIC extends JModelTestFrame {

	private static final long serialVersionUID = 201104031100L;

	private JPanel PanelBICSettings = new JPanel();
	private JTextField TextFieldTaxaBIC = new JTextField();
	private JTextField TextFieldSizeBIC = new JTextField();
	private JButton RunButtonBIC = new JButton();
	private JButton CancelButtonBIC = new JButton();
	private JButton JButtonDefaultBIC = new JButton();

	private JCheckBox jCheckBoxPAUPblock = new JCheckBox();
	private JCheckBox jCheckBoxAveraging = new JCheckBox();
	private JCheckBox jCheckBoxImportance = new JCheckBox();
	private JSlider JSliderInterval = new JSlider();

	private BIC myBIC;

	public Frame_BIC() {
	}

	public void initComponents() throws Exception {
		PanelBICSettings.setSize(490, 240);
		PanelBICSettings
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(new java.awt.Color(
								153, 153, 153), 1, false), "BIC Settings", 4,
						2, new java.awt.Font("Application", 1, 10),
						new java.awt.Color(102, 102, 153)));
		PanelBICSettings.setLocation(10, 10);
		PanelBICSettings.setVisible(true);
		PanelBICSettings.setLayout(null);

		RunButtonBIC.setVisible(true);
		RunButtonBIC.setSize(190, 40);
		RunButtonBIC.setText("Do BIC calculations");
		RunButtonBIC.setLocation(280, 190);
		getRootPane().setDefaultButton(RunButtonBIC);

		JButtonDefaultBIC.setVisible(true);
		JButtonDefaultBIC.setSize(141, 40);
		JButtonDefaultBIC.setText("Default Settings");
		JButtonDefaultBIC.setLocation(10, 190);

		CancelButtonBIC.setVisible(true);
		CancelButtonBIC.setSize(110, 40);
		CancelButtonBIC.setText("Cancel");
		CancelButtonBIC.setLocation(160, 190);

		TextFieldSizeBIC
				.setToolTipText("Enter the sample size you want to use for the BIC and click RETURN. By default this is the number of sites in the alignment");
		TextFieldSizeBIC
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(new java.awt.Color(
								153, 153, 153), 1, false), "Sample size", 4, 2,
						new java.awt.Font("Application", 1, 10),
						new java.awt.Color(102, 102, 153)));
		TextFieldSizeBIC.setVisible(true);
		TextFieldSizeBIC.setSize(170, 40);
		TextFieldSizeBIC.setText(Utilities.format(options.getSampleSize(),10,4,false));
		TextFieldSizeBIC.setHorizontalAlignment(JTextField.RIGHT);
		TextFieldSizeBIC.setLocation(30, 20);
		TextFieldSizeBIC.setEnabled(false);

		jCheckBoxImportance.setVisible(true);
		jCheckBoxImportance.setSize(260, 20);
		jCheckBoxImportance.setText("Calculate parameter importances");
		jCheckBoxImportance.setLocation(30, 70);
		jCheckBoxImportance.setSelected(true);

		jCheckBoxAveraging.setVisible(true);
		jCheckBoxAveraging.setSize(260, 20);
		jCheckBoxAveraging.setText("Do model averaging");
		jCheckBoxAveraging.setLocation(30, 110);
		jCheckBoxAveraging.setSelected(true);

		jCheckBoxPAUPblock.setVisible(true);
		jCheckBoxPAUPblock.setSize(260, 20);
		jCheckBoxPAUPblock.setText("Write PAUP* block");
		jCheckBoxPAUPblock.setLocation(30, 150);
		jCheckBoxPAUPblock.setSelected(false);
		jCheckBoxPAUPblock
				.setToolTipText("Writes a block of PAUP* commands implementing the selected model");

		JSliderInterval.setVisible(true);
		JSliderInterval
				.setToolTipText("Set the confidence interval for model averaging and/or parameter importance");
		JSliderInterval
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(new java.awt.Color(
								153, 153, 153), 1, false),
						"Confidence interval = 100%", 4, 2, new java.awt.Font(
								"Application", 1, 10), new java.awt.Color(102,
								102, 153)));
		JSliderInterval.setSize(170, 70);
		JSliderInterval.setLocation(300, 70);
		JSliderInterval.setMinimum(0);
		JSliderInterval.setMaximum(100);
		JSliderInterval.setValue(100);
		JSliderInterval.setMajorTickSpacing(20);
		JSliderInterval.setMinorTickSpacing(5);
		JSliderInterval.setPaintTicks(true);
		JSliderInterval.setPaintLabels(true);
		JSliderInterval.setEnabled(true);
		JSliderInterval.setFont(XManager.FONT_SLIDER);

		setLocation(281, 80);
		getContentPane().setLayout(null);
		setTitle("Bayesian Information Criterion (BIC) Settings");

		PanelBICSettings.add(TextFieldTaxaBIC);
		PanelBICSettings.add(TextFieldSizeBIC);
		PanelBICSettings.add(jCheckBoxAveraging);
		PanelBICSettings.add(jCheckBoxImportance);
		PanelBICSettings.add(jCheckBoxPAUPblock);
		PanelBICSettings.add(JSliderInterval);
		PanelBICSettings.add(JButtonDefaultBIC);
		PanelBICSettings.add(CancelButtonBIC);
		PanelBICSettings.add(RunButtonBIC);
		getContentPane().add(PanelBICSettings);

		setSize(510, 280);
		setResizable(false);

		// event handling
		jCheckBoxAveraging
				.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						jCheckBoxAveragingStateChanged(e);
					}
				});

		jCheckBoxImportance
				.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						jCheckBoxImportanceStateChanged(e);
					}
				});

		JSliderInterval
				.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						JSliderIntervalStateChanged(e);
					}
				});

		JButtonDefaultBIC
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						JButtonDefaultBICActionPerformed(e);
					}
				});
		CancelButtonBIC.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				CancelButtonBICActionPerformed(e);
			}
		});
		RunButtonBIC.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				RunButtonBICActionPerformed(e);
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

	public void jCheckBoxImportanceStateChanged(ChangeEvent e) {
		if (jCheckBoxImportance.isSelected()) {
			JSliderInterval.setEnabled(true);
		} else {
			if (!jCheckBoxAveraging.isSelected())
				JSliderInterval.setEnabled(false);
		}
	}

	public void jCheckBoxAveragingStateChanged(ChangeEvent e) {
		if (jCheckBoxAveraging.isSelected()) {
			JSliderInterval.setEnabled(true);
		} else {
			if (!jCheckBoxImportance.isSelected())
				JSliderInterval.setEnabled(false);
		}
	}

	public void JSliderIntervalStateChanged(ChangeEvent e) {
		JSliderInterval
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(new java.awt.Color(
								153, 153, 153), 1, false),
						"Confidence interval = " + JSliderInterval.getValue()
								+ "%", 4, 2, new java.awt.Font("Application",
								1, 10), new java.awt.Color(102, 102, 153)));
	}

	public void JButtonDefaultBICActionPerformed(java.awt.event.ActionEvent e) {
		TextFieldSizeBIC.setEnabled(false);
		TextFieldSizeBIC.setText(Utilities.format(options.getSampleSize(),10,4,false));
		ApplicationOptions.getInstance().countBLasParameters = true;
		XManager.getInstance().selectedMenuResultsBLasParameters(true);
		jCheckBoxPAUPblock.setSelected(false);
		jCheckBoxAveraging.setSelected(true);
		jCheckBoxImportance.setSelected(true);
		JSliderInterval.setValue(100);
	}

	public void CancelButtonBICActionPerformed(java.awt.event.ActionEvent e) {
		try {
			setVisible(false);
			dispose();
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	public void RunButtonBICActionPerformed(java.awt.event.ActionEvent e) {
		boolean writePAUPblock, doImportances, doModelAveraging;
		double credibleInterval;

		try {
			setVisible(false);
			dispose();

			writePAUPblock = jCheckBoxPAUPblock.isSelected();
			options.writePAUPblock |= writePAUPblock;
			doImportances = jCheckBoxImportance.isSelected();
			doModelAveraging = jCheckBoxAveraging.isSelected();

			credibleInterval = JSliderInterval.getValue() / 100.0;

			myBIC = new BIC(writePAUPblock, doImportances, doModelAveraging,
					credibleInterval);
			myBIC.compute();
			myBIC.print(ModelTest.getCurrentOutStream());
			ModelTest.setMyBIC(myBIC);
			options.doBIC = myBIC != null;
			XManager.getInstance().resultsFrame.enablePane(FrameResults.TAB_BIC);
			XManager.getInstance().resultsFrame.populate(FrameResults.TAB_BIC);
			XManager.getInstance().enableMenuAveraging(!options.fixedTopology);
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

}
