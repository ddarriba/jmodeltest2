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

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;

import es.uvigo.darwin.jmodeltest.statistics.Statistics;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public class Frame_LRTcalculator extends JModelTestFrame {

	private static final long serialVersionUID = 3100651418525568495L;
	
	private JPanel jPanelLRTsettings = new JPanel();
	private JButton jButtonRunLRT = new JButton();
	private JButton jButtonCancelLRT = new JButton();
	private JButton jButtonDefaultLRT = new JButton();
	private JTextField jTextFieldLRT0 = new JTextField();
	private JTextField jTextFieldLRT1 = new JTextField();
	private JLabel jLabelLRTpvalue = new JLabel();
	private JLabel jLabelLRT = new JLabel();
	private JTextField jTextFieldLRTdf = new JTextField();
	private JPanel PanelChi2Type = new JPanel();
	private JRadioButton ButtonStandardChi = new JRadioButton();
	private JRadioButton ButtonMixedChi = new JRadioButton();
	private ButtonGroup ButtonGroupChi = new ButtonGroup();

	public Frame_LRTcalculator() {
	}

	public void initComponents() throws Exception {
		// Dimensions(width, height));
		jPanelLRTsettings.setSize(new java.awt.Dimension(480, 270));
		jPanelLRTsettings.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "Likelihood Ratio Test for Nested Models", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		jPanelLRTsettings.setLocation(new java.awt.Point(10, 10));
		jPanelLRTsettings.setVisible(true);
		jPanelLRTsettings.setLayout(null);

		jTextFieldLRT0.setToolTipText("Enter here the absolute log likelihood of the simple model");
		jTextFieldLRT0.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "-lnL null model", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		jTextFieldLRT0.setVisible(true);
		jTextFieldLRT0.setSize(new java.awt.Dimension(150, 50));
		jTextFieldLRT0.setLocation(new java.awt.Point(30, 30));

		jTextFieldLRT1.setToolTipText("Enter here the absolute log likelihood of the complex model");
		jTextFieldLRT1.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "-ln alternative model", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		jTextFieldLRT1.setVisible(true);
		jTextFieldLRT1.setSize(new java.awt.Dimension(150, 50));
		jTextFieldLRT1.setLocation(new java.awt.Point(220, 30));

		jTextFieldLRTdf.setToolTipText("Enter here the difference in free parameters between the two models");
		jTextFieldLRTdf.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "degrees of freedom", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		jTextFieldLRTdf.setVisible(true);
		jTextFieldLRTdf.setSize(new java.awt.Dimension(150, 50));
		jTextFieldLRTdf.setLocation(new java.awt.Point(30, 90));

		jLabelLRT.setSize(new java.awt.Dimension(330, 40));
		jLabelLRT.setLocation(new java.awt.Point(30, 145));
		jLabelLRT.setVisible(true);
		jLabelLRT.setText("LRT = ");

		jLabelLRTpvalue.setSize(new java.awt.Dimension(330, 40));
		jLabelLRTpvalue.setLocation(new java.awt.Point(30, 175));
		jLabelLRTpvalue.setVisible(true);
		jLabelLRTpvalue.setText("P-value =");

		PanelChi2Type.setSize(new java.awt.Dimension(210, 50));
		PanelChi2Type.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "Chi-square distribution", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		PanelChi2Type.setLocation(new java.awt.Point(220, 90));
		PanelChi2Type.setVisible(true);
		PanelChi2Type.setLayout(null);
		ButtonStandardChi.setVisible(true);
		ButtonStandardChi.setSize(new java.awt.Dimension(100, 20));
		ButtonStandardChi.setText("Standard");
		ButtonStandardChi.setLocation(new java.awt.Point(20, 20));
		ButtonStandardChi.setToolTipText("Select this if the value of the restricted parameter in the null model is not at the boundary of its range");
		ButtonStandardChi.setSelected(true);
		ButtonMixedChi.setVisible(true);
		ButtonMixedChi.setSize(new java.awt.Dimension(70, 20));
		ButtonMixedChi.setText("Mixed");
		ButtonMixedChi.setToolTipText("Select this if the value of the restricted parameter in the null model is at the boundary of its range");
		ButtonMixedChi.setLocation(new java.awt.Point(125, 20));
		ButtonGroupChi.add(ButtonStandardChi);
		ButtonGroupChi.add(ButtonMixedChi);
		PanelChi2Type.add(ButtonStandardChi);
		PanelChi2Type.add(ButtonMixedChi);

		jButtonDefaultLRT.setVisible(true);
		jButtonDefaultLRT.setSize(new java.awt.Dimension(141, 40));
		jButtonDefaultLRT.setText("Default Settings");
		jButtonDefaultLRT.setLocation(new java.awt.Point(30, 220));

		jButtonCancelLRT.setVisible(true);
		jButtonCancelLRT.setSize(new java.awt.Dimension(110, 40));
		jButtonCancelLRT.setText("Cancel");
		jButtonCancelLRT.setLocation(new java.awt.Point(190, 220));

		jButtonRunLRT.setVisible(true);
		jButtonRunLRT.setSize(new java.awt.Dimension(110, 40));
		jButtonRunLRT.setText("Run");
		jButtonRunLRT.setLocation(new java.awt.Point(320, 220));
		jButtonRunLRT.setToolTipText("Remember that models have to be nested!");


		setLocation(new java.awt.Point(281, 80));
		getContentPane().setLayout(null);
		setTitle("LRT calculator");

		jPanelLRTsettings.add(jTextFieldLRT0);
		jPanelLRTsettings.add(jTextFieldLRT1);
		jPanelLRTsettings.add(jTextFieldLRTdf);
		jPanelLRTsettings.add(jButtonDefaultLRT);
		jPanelLRTsettings.add(jButtonCancelLRT);
		jPanelLRTsettings.add(jButtonRunLRT);
		jPanelLRTsettings.add(jLabelLRT);
		jPanelLRTsettings.add(jLabelLRTpvalue);
		jPanelLRTsettings.add(PanelChi2Type);
		getContentPane().add(jPanelLRTsettings);

		setSize(new java.awt.Dimension(500, 310));
		setResizable(false);
		
		
		
		// event handling
		jButtonDefaultLRT.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				jButtonDefaultLRTActionPerformed(e);
			}
		});
		jButtonCancelLRT.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				jButtonCancelLRTActionPerformed(e);
			}
		});
		jButtonRunLRT.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				jButtonRunLRTActionPerformed(e);
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



  
/**************************** LRT **************************************
 *																		*
 *	Computes a likelihood ratio test given a LRT and df					*					
 *	Returns  P-value according to a standard chi2 distribution			*	
 *																		*
 ***********************************************************************/

	public double LRT (double delta, int df)
		{
		double prob;
		
		if (delta == 0)
			prob = 1.0;
		else
			prob = Statistics.chiSquareProbability (delta, df);
	
		return prob;
		}

 /**************************** LRTboundary *****************************
 *																		*
 *	Computes a likelihood ratio test given a LRT and df					*					
 *	Returns  P-value according to a mixed chi2 distribution				*	
 *																		*
 ***********************************************************************/

	static private double LRTboundary(double delta, int df)
		{
		double prob;

		if (delta == 0)
			prob = 1.0;
		else
			{
			if (df == 1)
				prob = Statistics.chiSquareProbability(delta,df)/2;
			else	
				prob= (Statistics.chiSquareProbability(delta,df-1) + 
					Statistics.chiSquareProbability(delta,df)) / 2;
			}

		return prob;
		}

  
  
	public void jButtonRunLRTActionPerformed(java.awt.event.ActionEvent e) 
		{
		double	L0, L1, delta, pvalue;
		int		df;
		String sL0, sL1, sdf;

		try
			{
			// get the strings from the text fields
			sL0 = jTextFieldLRT0.getText();
			sL1 = jTextFieldLRT1.getText();
			sdf = jTextFieldLRTdf.getText();

			// check for empty fields and numbers
			if (sL0.length() == 0 || !Utilities.isNumber(sL0))
					JOptionPane.showMessageDialog (new JFrame(), "Enter a positive likelihood for the null model (" + sL0 + ")",
				"jModeltest error", JOptionPane.ERROR_MESSAGE); 
			else if (sL1.length() == 0 || !Utilities.isNumber(sL1))
					JOptionPane.showMessageDialog (new JFrame(), "Enter a positive likelihood for the alternative model",
				"jModeltest error", JOptionPane.ERROR_MESSAGE);
			else if (sdf.length() == 0 || !Utilities.isNumber(sdf))
					JOptionPane.showMessageDialog (new JFrame(), "Enter a positive number of degrees of freedom",
				"jModeltest error", JOptionPane.ERROR_MESSAGE); 
			else
				{
				// get values
				L0 = Double.parseDouble (sL0);
				L1 = Double.parseDouble (sL1);
				df = Integer.parseInt (sdf);
				delta = 2 * (L0 - L1);
								
				// check for valid values
				if (L0 <= 0 || Double.isNaN(L0))
					JOptionPane.showMessageDialog (new JFrame(), "Enter a positive likelihood for the null model*",
					"jModeltest error", JOptionPane.ERROR_MESSAGE); 
				else if (L1 <= 0 || Double.isNaN(L1))
					JOptionPane.showMessageDialog (new JFrame(), "Enter a positive likelihood for the alternative model*",
					"jModeltest error", JOptionPane.ERROR_MESSAGE); 
				else if (df <= 0 || Double.isNaN(df))
					JOptionPane.showMessageDialog (new JFrame(), "Enter a positive number of degrees of freedom",
					"jModeltest error", JOptionPane.ERROR_MESSAGE); 
				else if (delta < 0)
					JOptionPane.showMessageDialog (new JFrame(), "The likelihood of the null model cannot be bigger than the likelihood of the alternative model!",
					"jModeltest error", JOptionPane.ERROR_MESSAGE); 
				else
					{
					if (ButtonMixedChi.isSelected())
						pvalue = LRTboundary(delta, df);
					else
						pvalue = LRT(delta, df);
	
					jLabelLRT.setText("LRT = " + Utilities.roundDoubleTo(delta,6));
					jLabelLRTpvalue.setText("P-value = " + Utilities.roundDoubleTo(pvalue,6));
					}
				}			
			}
		catch (Exception f) 
			{
			f.printStackTrace();
			}
		}


	public void jButtonDefaultLRTActionPerformed(java.awt.event.ActionEvent e) 
		{
		try
			{
			ButtonStandardChi.setSelected(true);
			jTextFieldLRT0.setText("");
			jTextFieldLRT1.setText("");
			jTextFieldLRTdf.setText("");
			jLabelLRT.setText("LRT = ");
			jLabelLRTpvalue.setText("P-value =");
			}
		catch (Exception f) 
			{
			f.printStackTrace();
			}
		}


	public void jButtonCancelLRTActionPerformed(java.awt.event.ActionEvent e) 
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
