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

public class Frame_DT extends JModelTestFrame {

	private static final long serialVersionUID = 201104031100L;
	
	private JPanel PanelDTSettings = new JPanel();
	private JTextField TextFieldTaxaDT = new JTextField();
	private JTextField TextFieldSizeDT = new JTextField();
	private JButton RunButtonDT = new JButton();
	private JButton CancelButtonDT = new JButton();
	private JButton JButtonDefaultDT = new JButton();

	private JCheckBox jCheckBoxIncludeBL_DT = new JCheckBox();
	private JCheckBox jCheckBoxPAUPblock = new JCheckBox();
	private JCheckBox jCheckBoxAveraging = new JCheckBox();
	private JCheckBox jCheckBoxImportance = new JCheckBox();
	private JSlider JSliderInterval = new JSlider();

	private DT myDT;

	public Frame_DT(ModelTest modelTest) {
		super(modelTest);
	}

	public void initComponents() throws Exception {
		PanelDTSettings.setSize(new java.awt.Dimension(490, 240));
		PanelDTSettings.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "DT Settings", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		PanelDTSettings.setLocation(new java.awt.Point(10, 10));
		PanelDTSettings.setVisible(true);
		PanelDTSettings.setLayout(null);

		RunButtonDT.setVisible(true);
		RunButtonDT.setSize(new java.awt.Dimension(190, 40));
		RunButtonDT.setText("Do DT calculations");
		RunButtonDT.setLocation(new java.awt.Point(280, 190));
		getRootPane().setDefaultButton(RunButtonDT);

		JButtonDefaultDT.setVisible(true);
		JButtonDefaultDT.setSize(new java.awt.Dimension(141, 40));
		JButtonDefaultDT.setText("Default Settings");
		JButtonDefaultDT.setLocation(new java.awt.Point(10, 190));

		CancelButtonDT.setVisible(true);
		CancelButtonDT.setSize(new java.awt.Dimension(110, 40));
		CancelButtonDT.setText("Cancel");
		CancelButtonDT.setLocation(new java.awt.Point(160, 190));

		TextFieldSizeDT.setToolTipText("Enter the sample size you want to use for the DT and click RETURN. By default this is the number of sites in the alignment");
		TextFieldSizeDT.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "Sample size", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		TextFieldSizeDT.setVisible(true);
		TextFieldSizeDT.setSize(new java.awt.Dimension(100, 40));
		TextFieldSizeDT.setText(""+options.sampleSize);
		TextFieldSizeDT.setHorizontalAlignment(JTextField.RIGHT);
		TextFieldSizeDT.setLocation(new java.awt.Point(30, 20));
		TextFieldSizeDT.setEnabled(true);

		jCheckBoxImportance.setVisible(true);
		jCheckBoxImportance.setSize(new java.awt.Dimension(260, 20));
		jCheckBoxImportance.setText("Calculate parameter importances");
		jCheckBoxImportance.setLocation(new java.awt.Point(30, 70));
		jCheckBoxImportance.setSelected(true);

		jCheckBoxAveraging.setVisible(true);
		jCheckBoxAveraging.setSize(new java.awt.Dimension(260, 20));
		jCheckBoxAveraging.setText("Do model averaging");
		jCheckBoxAveraging.setLocation(new java.awt.Point(30, 110));
		jCheckBoxAveraging.setSelected(true);

		jCheckBoxPAUPblock.setVisible(true);
		jCheckBoxPAUPblock.setSize(new java.awt.Dimension(260, 20));
		jCheckBoxPAUPblock.setText("Write PAUP* block");
		jCheckBoxPAUPblock.setLocation(new java.awt.Point(30, 150));
		jCheckBoxPAUPblock.setSelected(false);
		jCheckBoxPAUPblock.setToolTipText("Writes a block of PAUP* commands implementing the selected model");

		JSliderInterval.setVisible(true);
		JSliderInterval.setToolTipText("Set the confidence interval for model averaging and/or parameter importance");
		JSliderInterval.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "Confidence interval = 100%", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		JSliderInterval.setSize(new java.awt.Dimension(170, 70));
		JSliderInterval.setLocation(new java.awt.Point(300, 70));
		JSliderInterval.setMinimum(0);
		JSliderInterval.setMaximum(100);
		JSliderInterval.setValue(100);
		JSliderInterval.setMajorTickSpacing(20);
		JSliderInterval.setMinorTickSpacing(5);
		JSliderInterval.setPaintTicks(true);
		JSliderInterval.setPaintLabels(true);	
		JSliderInterval.setEnabled(true);
		JSliderInterval.setFont(XManager.FONT_SLIDER);
					
		setLocation(new java.awt.Point(281, 80));
		getContentPane().setLayout(null);
		setTitle("Decision Theory (DT) Settings");

		PanelDTSettings.add(TextFieldTaxaDT);
		PanelDTSettings.add(TextFieldSizeDT);
		PanelDTSettings.add(jCheckBoxIncludeBL_DT);
		PanelDTSettings.add(jCheckBoxAveraging);
		PanelDTSettings.add(jCheckBoxImportance);
		PanelDTSettings.add(jCheckBoxPAUPblock);
		PanelDTSettings.add(JSliderInterval);
		PanelDTSettings.add(JButtonDefaultDT);
		PanelDTSettings.add(CancelButtonDT);
		PanelDTSettings.add(RunButtonDT);
		getContentPane().add(PanelDTSettings);

		setSize(new java.awt.Dimension(510, 280));
		setResizable(false);

		jCheckBoxAveraging.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				jCheckBoxAveragingStateChanged(e);
			}
		});

		jCheckBoxImportance.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				jCheckBoxImportanceStateChanged(e);
			}
		});


		JSliderInterval.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSliderIntervalStateChanged(e);
			}
		});

		JButtonDefaultDT.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				JButtonDefaultDTActionPerformed(e);
			}
		});
		CancelButtonDT.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				CancelButtonDTActionPerformed(e);
			}
		});
		RunButtonDT.addActionListener(new java.awt.event.ActionListener() {
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
		if (jCheckBoxImportance.isSelected())
			{
			JSliderInterval.setEnabled(true);
			}
		else
			{
			if (!jCheckBoxAveraging.isSelected())
				JSliderInterval.setEnabled(false);
			}
		}

	public void jCheckBoxAveragingStateChanged(ChangeEvent e) 
		{
		if (jCheckBoxAveraging.isSelected())
			{
			JSliderInterval.setEnabled(true);
			}
		else
			{
			if (!jCheckBoxImportance.isSelected())
				JSliderInterval.setEnabled(false);
			}
		}
	

	public void JSliderIntervalStateChanged(ChangeEvent e) 
		{
		JSliderInterval.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "Confidence interval = " + JSliderInterval.getValue() + "%", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		}

				
	public void JButtonDefaultDTActionPerformed(java.awt.event.ActionEvent e) 
		{
		TextFieldSizeDT.setEnabled(false);
		TextFieldSizeDT.setText("" + options.sampleSize);
		modelTest.getApplicationOptions().countBLasParameters = true;
		XManager.getInstance(modelTest).selectedMenuResultsBLasParameters(true);
		jCheckBoxPAUPblock.setSelected(false);
		jCheckBoxAveraging.setSelected(true);
		jCheckBoxImportance.setSelected(true);
		JSliderInterval.setValue(100);
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

			writePAUPblock = jCheckBoxPAUPblock.isSelected();
			doImportances = jCheckBoxImportance.isSelected();
			doModelAveraging = jCheckBoxAveraging.isSelected();
			credibleInterval = JSliderInterval.getValue()/100.0;
			options.sampleSize = Integer.parseInt(TextFieldSizeDT.getText());
			
			myDT = new DT(writePAUPblock, doImportances, doModelAveraging, credibleInterval, modelTest);
			myDT.compute();
			myDT.print(modelTest.getCurrentOutStream());
			modelTest.setMyDT(myDT);
			options.doDT = myDT != null;
			XManager.getInstance(modelTest).resultsFrame.enablePane(FrameResults.TAB_DT);
			XManager.getInstance(modelTest).resultsFrame.populate(FrameResults.TAB_DT);
			XManager.getInstance(modelTest).enableMenuAveraging(!options.fixedTopology);
			}
		catch (Exception f) 
			{
			f.printStackTrace();
			}
		}
	
}
