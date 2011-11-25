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

import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;

import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.exe.RunConsense;
import es.uvigo.darwin.jmodeltest.selection.InformationCriterion;

public class Frame_Consense extends JModelTestFrame {

	private static final long serialVersionUID = 201104031005L;

	javax.swing.JPanel PanelConsense = new javax.swing.JPanel();
	javax.swing.JButton RunButtonConsense = new javax.swing.JButton();
	javax.swing.JButton CancelButtonConsense = new javax.swing.JButton();
	javax.swing.JButton DefaultButtonConsense = new javax.swing.JButton();

	javax.swing.JPanel PanelCriterion = new javax.swing.JPanel();
	javax.swing.JRadioButton ButtonAIC = new javax.swing.JRadioButton();
	javax.swing.JRadioButton ButtonAICc = new javax.swing.JRadioButton();
	javax.swing.JRadioButton ButtonBIC = new javax.swing.JRadioButton();
	javax.swing.JRadioButton ButtonDT = new javax.swing.JRadioButton();
	javax.swing.ButtonGroup ButtonGroupCriterion = new javax.swing.ButtonGroup();

	javax.swing.JPanel PanelConsensus = new javax.swing.JPanel();
	javax.swing.JRadioButton ButtonMajority = new javax.swing.JRadioButton();
	javax.swing.JRadioButton ButtonStrict = new javax.swing.JRadioButton();
	javax.swing.ButtonGroup ButtonGroupConsensus = new javax.swing.ButtonGroup();

	public static javax.swing.JSlider JSliderInterval = new javax.swing.JSlider();


	public RunConsense runConsense;

	public Frame_Consense() {
	}

	public void initComponents() throws Exception {
		PanelConsense.setSize(new java.awt.Dimension(490, 210));
		PanelConsense.setBorder(new javax.swing.plaf.BorderUIResource.TitledBorderUIResource(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, false), "Phylogenetic averaging", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		PanelConsense.setLocation(new java.awt.Point(10, 10));
		PanelConsense.setVisible(true);
		PanelConsense.setLayout(null);
		RunButtonConsense.setVisible(true);
		RunButtonConsense.setSize(new java.awt.Dimension(190, 40));
		RunButtonConsense.setText("Run");
		RunButtonConsense.setLocation(new java.awt.Point(280, 160));
		getRootPane().setDefaultButton(RunButtonConsense);
		CancelButtonConsense.setVisible(true);
		CancelButtonConsense.setSize(new java.awt.Dimension(110, 40));
		CancelButtonConsense.setText("Cancel");
		CancelButtonConsense.setLocation(new java.awt.Point(160, 160));
		DefaultButtonConsense.setVisible(true);
		DefaultButtonConsense.setSize(new java.awt.Dimension(141, 40));
		DefaultButtonConsense.setText("Default Settings");
		DefaultButtonConsense.setLocation(new java.awt.Point(10, 160));
		
		PanelCriterion.setSize(new java.awt.Dimension(300, 50));
		PanelCriterion.setBorder(new javax.swing.plaf.BorderUIResource.TitledBorderUIResource(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, false), "Criterion for tree weights", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		PanelCriterion.setLocation(new java.awt.Point(20, 20));
		PanelCriterion.setVisible(true);
		PanelCriterion.setLayout(null);
		ButtonAIC.setVisible(true);
		ButtonAIC.setSize(new java.awt.Dimension(60, 20));
		ButtonAIC.setText("AIC");
		ButtonAIC.setLocation(new java.awt.Point(20, 20));
		ButtonAICc.setVisible(true);
		ButtonAICc.setSize(new java.awt.Dimension(60, 20));
		ButtonAICc.setText("AICc");
		ButtonAICc.setLocation(new java.awt.Point(90, 20));
		ButtonBIC.setVisible(true);
		ButtonBIC.setSize(new java.awt.Dimension(60, 20));
		ButtonBIC.setText("BIC");
		ButtonBIC.setLocation(new java.awt.Point(160, 20));
		ButtonDT.setVisible(true);
		ButtonDT.setSize(new java.awt.Dimension(60, 20));
		ButtonDT.setText("DT");
		ButtonDT.setLocation(new java.awt.Point(230, 20));

		PanelConsensus.setSize(new java.awt.Dimension(240, 50));
		PanelConsensus.setBorder(new javax.swing.plaf.BorderUIResource.TitledBorderUIResource(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, false), "Consensus type", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		PanelConsensus.setLocation(new java.awt.Point(20, 80));
		PanelConsensus.setVisible(true);
		PanelConsensus.setLayout(null);
		ButtonMajority.setVisible(true);
		ButtonMajority.setSize(new java.awt.Dimension(120, 20));
		ButtonMajority.setText("Majority rule");
		ButtonMajority.setLocation(new java.awt.Point(20, 20));
		ButtonMajority.setSelected(true);
		ButtonStrict.setVisible(true);
		ButtonStrict.setSize(new java.awt.Dimension(80, 20));
		ButtonStrict.setText("Strict");
		ButtonStrict.setLocation(new java.awt.Point(140, 20));

		ButtonAIC.setEnabled(ModelTest.testAIC());
		ButtonAICc.setEnabled(ModelTest.testAICc());
		ButtonBIC.setEnabled(ModelTest.testBIC());
		ButtonDT.setEnabled(ModelTest.testDT());
		if (ButtonAIC.isEnabled())
			ButtonAIC.setSelected(true);
		else if (ButtonAICc.isEnabled())
			ButtonAICc.setSelected(true);
		else if (ButtonBIC.isEnabled())
			ButtonBIC.setSelected(true);
		else if (ButtonDT.isEnabled())
			ButtonDT.setSelected(true);
	
		ButtonGroupCriterion.add(ButtonAIC);
		ButtonGroupCriterion.add(ButtonAICc);
		ButtonGroupCriterion.add(ButtonBIC);
		ButtonGroupCriterion.add(ButtonDT);

		PanelCriterion.add(ButtonAIC);
		PanelCriterion.add(ButtonAICc);
		PanelCriterion.add(ButtonBIC);
		PanelCriterion.add(ButtonDT);

		ButtonGroupConsensus.add(ButtonMajority);
		ButtonGroupConsensus.add(ButtonStrict);
		PanelConsensus.add(ButtonMajority);
		PanelConsensus.add(ButtonStrict);

		JSliderInterval.setVisible(true);
		JSliderInterval.setToolTipText("Set the confidence interval for model averaging and/or parameter importance");
		JSliderInterval.setBorder(new javax.swing.plaf.BorderUIResource.TitledBorderUIResource(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, false), "Confidence interval = 100%", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		JSliderInterval.setSize(new java.awt.Dimension(170, 70));
		JSliderInterval.setLocation(new java.awt.Point(280, 80));
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
		setTitle("Phylogenetic averaging settings");

		PanelConsense.add(PanelCriterion);
		PanelConsense.add(PanelConsensus);
		PanelConsense.add(JSliderInterval);
		PanelConsense.add(RunButtonConsense);
		PanelConsense.add(CancelButtonConsense);
		PanelConsense.add(DefaultButtonConsense);

		getContentPane().add(PanelConsense);

		setSize(new java.awt.Dimension(510, 250));
		setResizable(false);

		// event handling
		RunButtonConsense.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				RunButtonConsenseActionPerformed(e);
			}
		});

		CancelButtonConsense.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				CancelButtonConsenseActionPerformed(e);
			}
		});

	
		DefaultButtonConsense.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				DefaultButtonConsenseActionPerformed(e);
			}
		});

		JSliderInterval.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent e) {
				JSliderIntervalStateChanged(e);
			}
		});


		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});

// END GENERATED CODE
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
	
	public void JSliderIntervalStateChanged(javax.swing.event.ChangeEvent e) 
		{
		JSliderInterval.setBorder(new javax.swing.plaf.BorderUIResource.TitledBorderUIResource(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, false), "Confidence interval = " + JSliderInterval.getValue() + "%", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		}


	public void RunButtonConsenseActionPerformed(java.awt.event.ActionEvent e)
		{
		InformationCriterion criterion;
		String consensusType;

		double	credibleInterval;
		
		try
			{
			// get criterion
			if (ButtonAIC.isSelected()) {
				criterion = ModelTest.getMyAIC();
			}
			else if (ButtonAICc.isSelected()) {
				criterion = ModelTest.getMyAICc();
			}
			else if (ButtonBIC.isSelected()) {
				criterion = ModelTest.getMyBIC();
			}
			else {
				criterion = ModelTest.getMyDT();
			}

			// get consensus type
			if (ButtonMajority.isSelected())
				consensusType = "50% majority rule";
			else
				consensusType = "strict";

			credibleInterval = JSliderInterval.getValue()/100.0;

			setVisible(false);
			dispose();

			// run consense
			runConsense = new RunConsense(criterion, consensusType, credibleInterval);

			if (ButtonAIC.isSelected()) {
				ModelTest.setConsensusAIC(runConsense);
			}
			else if (ButtonAICc.isSelected()) {
				ModelTest.setConsensusAICc(runConsense);
			}
			else if (ButtonBIC.isSelected()) {
				ModelTest.setConsensusBIC(runConsense);
			}
			else {
				ModelTest.setConsensusDT(runConsense);
			}
			}
		catch (Exception f) 
			{
			f.printStackTrace();
			}
		}

	public void DefaultButtonConsenseActionPerformed (java.awt.event.ActionEvent e)
		{
		try
			{
			if (ModelTest.testAIC())
				ButtonAIC.setSelected(true);
			else if (ModelTest.testAICc())
				ButtonAICc.setSelected(true);
			else if (ModelTest.testBIC())
				ButtonBIC.setSelected(true);
			else if (ModelTest.testDT())
				ButtonDT.setEnabled(true);

			ButtonMajority.setSelected(true);

			JSliderInterval.setValue(100);

			}
		catch (Exception f) 
			{
			f.printStackTrace();
			}
		}



	public void CancelButtonConsenseActionPerformed (java.awt.event.ActionEvent e)
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

	
}
