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
import es.uvigo.darwin.jmodeltest.selection.DT;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public class Frame_DT extends JModelTestFrame {

	private static final long serialVersionUID = 201104031100L;
	
	private JPanel panelDTSettings = new JPanel();
	private JTextField textTaxaDT = new JTextField();
	private JTextField textSizeDT = new JTextField();
	private JButton runButtonDT = new JButton();
	private JButton cancelButtonDT = new JButton();
	private JButton buttonDefaultDT = new JButton();

	private JCheckBox checkBoxIncludeBL_DT = new JCheckBox();
	private JCheckBox checkBoxPAUPblock = new JCheckBox();
	private JCheckBox checkBoxAveraging = new JCheckBox();
	private JCheckBox checkBoxImportance = new JCheckBox();
	private JSlider sliderInterval = new JSlider();

	private DT myDT;

	public Frame_DT() {
	}

	public void initComponents() throws Exception {
		panelDTSettings.setSize(490, 240);
		panelDTSettings.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "DT Settings", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		panelDTSettings.setLocation(10, 10);
		panelDTSettings.setVisible(true);
		panelDTSettings.setLayout(null);

		runButtonDT.setVisible(true);
		runButtonDT.setSize(190, 40);
		runButtonDT.setText("Do DT calculations");
		runButtonDT.setLocation(280, 190);
		getRootPane().setDefaultButton(runButtonDT);

		buttonDefaultDT.setVisible(true);
		buttonDefaultDT.setSize(141, 40);
		buttonDefaultDT.setText("Default Settings");
		buttonDefaultDT.setLocation(10, 190);

		cancelButtonDT.setVisible(true);
		cancelButtonDT.setSize(110, 40);
		cancelButtonDT.setText("Cancel");
		cancelButtonDT.setLocation(160, 190);

		textSizeDT.setToolTipText("Enter the sample size you want to use for the DT and click RETURN. By default this is the number of sites in the alignment");
		textSizeDT.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "Sample size", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		textSizeDT.setVisible(true);
		textSizeDT.setSize(100, 40);
		textSizeDT.setText(Utilities.format(options.getSampleSize(),10,4,false));
		textSizeDT.setHorizontalAlignment(JTextField.RIGHT);
		textSizeDT.setLocation(30, 20);
		textSizeDT.setEnabled(false);

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
		checkBoxPAUPblock.setToolTipText("Writes a block of PAUP* commands implementing the selected model");

		sliderInterval.setVisible(true);
		sliderInterval.setToolTipText("Set the confidence interval for model averaging and/or parameter importance");
		sliderInterval.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "Confidence interval = 100%", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		sliderInterval.setSize(170, 70);
		sliderInterval.setLocation(300, 70);
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
		setTitle("Decision Theory (DT) Settings");

		panelDTSettings.add(textTaxaDT);
		panelDTSettings.add(textSizeDT);
		panelDTSettings.add(checkBoxIncludeBL_DT);
		panelDTSettings.add(checkBoxAveraging);
		panelDTSettings.add(checkBoxImportance);
		panelDTSettings.add(checkBoxPAUPblock);
		panelDTSettings.add(sliderInterval);
		panelDTSettings.add(buttonDefaultDT);
		panelDTSettings.add(cancelButtonDT);
		panelDTSettings.add(runButtonDT);
		getContentPane().add(panelDTSettings);

		setSize(510, 280);
		setResizable(false);

		checkBoxAveraging.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				jCheckBoxAveragingStateChanged(e);
			}
		});

		checkBoxImportance.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				jCheckBoxImportanceStateChanged(e);
			}
		});

		sliderInterval.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSliderIntervalStateChanged(e);
			}
		});

		buttonDefaultDT.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				JButtonDefaultDTActionPerformed(e);
			}
		});
		cancelButtonDT.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				CancelButtonDTActionPerformed(e);
			}
		});
		runButtonDT.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				RunButtonDTActionPerformed(e);
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
			Component[] components = getLayeredPane().getComponentsInLayer(JLayeredPane.DEFAULT_LAYER.intValue());
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
		//System.exit(0);
	}
	
	public void jCheckBoxImportanceStateChanged(ChangeEvent e) 
		{
		if (checkBoxImportance.isSelected())
			{
			sliderInterval.setEnabled(true);
			}
		else
			{
			if (!checkBoxAveraging.isSelected())
				sliderInterval.setEnabled(false);
			}
		}

	public void jCheckBoxAveragingStateChanged(ChangeEvent e) 
		{
		if (checkBoxAveraging.isSelected())
			{
			sliderInterval.setEnabled(true);
			}
		else
			{
			if (!checkBoxImportance.isSelected())
				sliderInterval.setEnabled(false);
			}
		}
	

	public void JSliderIntervalStateChanged(ChangeEvent e) 
		{
		sliderInterval.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "Confidence interval = " + sliderInterval.getValue() + "%", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		}

				
	public void JButtonDefaultDTActionPerformed(java.awt.event.ActionEvent e) 
		{
		textSizeDT.setEnabled(false);
		ApplicationOptions.getInstance().countBLasParameters = true;
		XManager.getInstance().selectedMenuResultsBLasParameters(true);
		checkBoxPAUPblock.setSelected(false);
		checkBoxAveraging.setSelected(true);
		checkBoxImportance.setSelected(true);
		sliderInterval.setValue(100);
		}
	
	public void CancelButtonDTActionPerformed(java.awt.event.ActionEvent e) 
		{
		try
			{
			setVisible(false);
			dispose();
			}
		catch (Exception f) 
			{
			f.printStackTrace();
			}
		}
	
	public void RunButtonDTActionPerformed(java.awt.event.ActionEvent e) 
		{
		boolean	writePAUPblock, doImportances, doModelAveraging;
		double	credibleInterval;

		try
			{
			setVisible(false);
			dispose();

			writePAUPblock = checkBoxPAUPblock.isSelected();
			options.writePAUPblock |= writePAUPblock;
			doImportances = checkBoxImportance.isSelected();
			doModelAveraging = checkBoxAveraging.isSelected();
			credibleInterval = sliderInterval.getValue()/100.0;
			
			myDT = new DT(writePAUPblock, doImportances, doModelAveraging, credibleInterval);
			myDT.compute();
			myDT.print(ModelTest.getCurrentOutStream());
			ModelTest.setMyDT(myDT);
			options.doDT = myDT != null;
			XManager.getInstance().resultsFrame.enablePane(FrameResults.TAB_DT);
			XManager.getInstance().resultsFrame.populate(FrameResults.TAB_DT);
			XManager.getInstance().enableMenuAveraging(!options.fixedTopology);
			}
		catch (Exception f) 
			{
			f.printStackTrace();
			}
		}
	
}
