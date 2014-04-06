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
import java.awt.Toolkit;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.BorderUIResource;

import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.selection.HLRT;

public class Frame_hLRT extends JModelTestFrame {

	private static final long serialVersionUID = -3314020071170637296L;
	
	private JPanel PanelSettings = new JPanel();
	private JTextField TextFieldConfidenceLevelhLRT = new JTextField();
	private JList HypothesisList = new JList();
	private DefaultListModel listModel = new DefaultListModel();
	private JButton ButtonListUp = new JButton();
	private JButton ButtonListDown = new JButton();
	private JButton CancelButtonhLRT = new JButton();
	private JButton RunButtonhLRT = new JButton();
	private JButton JButtonDefaulthLRT = new JButton();
	private JPanel PanelForwardBackward = new JPanel();
	private JRadioButton ButtonForward = new JRadioButton();
	private JRadioButton ButtonBackward = new JRadioButton();
	private ButtonGroup ButtonGroupForwardBackward = new ButtonGroup();
	private JCheckBox jCheckBoxPAUPblock = new JCheckBox();
	private JCheckBox jCheckBoxDynamical = new JCheckBox();

	private String currentDirectory = System.getProperty("user.dir");

	public Frame_hLRT() {
	}

	public void initComponents() throws Exception {
		java.awt.Image img0 = Toolkit.getDefaultToolkit().getImage(currentDirectory + "/resources/icons/Up24.gif");
		java.awt.Image img1 = Toolkit.getDefaultToolkit().getImage(currentDirectory + "/resources/icons/Down24.gif");

		PanelSettings.setSize(new java.awt.Dimension(460, 370));
		PanelSettings.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "hLRT Settings", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		PanelSettings.setLocation(new java.awt.Point(10, 10));
		PanelSettings.setVisible(true);
		PanelSettings.setLayout(null);

		TextFieldConfidenceLevelhLRT.setToolTipText("Enter the confidence level for individual LRTs and click ENTER");
		TextFieldConfidenceLevelhLRT.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "Confidence level LRT", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		TextFieldConfidenceLevelhLRT.setVisible(true);
		TextFieldConfidenceLevelhLRT.setSize(new java.awt.Dimension(130, 50));
		TextFieldConfidenceLevelhLRT.setText("0.01");
		TextFieldConfidenceLevelhLRT.setHorizontalAlignment(JTextField.RIGHT);
		TextFieldConfidenceLevelhLRT.setLocation(new java.awt.Point(20, 20));

		HypothesisList.setToolTipText("Use the buttons to move up and down the different hypotheses");
		HypothesisList.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "Hypotheses order", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		HypothesisList.setVisible(true);
		HypothesisList.setSize(new java.awt.Dimension(130, 230));
		HypothesisList.setVisibleRowCount(ModelTest.testingOrder.size());
		HypothesisList.setFont(XManager.FONT_LABEL_BIG);
		HypothesisList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		HypothesisList.setLocation(new java.awt.Point(20, 80));

		ButtonListUp.setIcon(new ImageIcon(img0));
		ButtonListUp.setVisible(true);
		ButtonListUp.setSize(new java.awt.Dimension(35, 40));
		ButtonListUp.setLocation(new java.awt.Point(155, 100));

		ButtonListDown.setIcon(new ImageIcon(img1));
		ButtonListDown.setVisible(true);
		ButtonListDown.setSize(new java.awt.Dimension(35, 40));
		ButtonListDown.setLocation(new java.awt.Point(155, 140));

		PanelForwardBackward.setSize(new java.awt.Dimension(200, 50));
		PanelForwardBackward.setBorder(new BorderUIResource.TitledBorderUIResource(new LineBorder(new java.awt.Color(153, 153, 153), 1, false), "Forward/Backward Selection", 4, 2, new java.awt.Font("Application", 1, 10), new java.awt.Color(102, 102, 153)));
		PanelForwardBackward.setLocation(new java.awt.Point(200, 20));
		PanelForwardBackward.setVisible(true);
		PanelForwardBackward.setLayout(null);

		ButtonForward.setVisible(true);
		ButtonForward.setSize(new java.awt.Dimension(90, 20));
		ButtonForward.setText("Forward");
		ButtonForward.setLocation(new java.awt.Point(10, 20));
		ButtonForward.setToolTipText("Adds parameters starting from a JC model");
		ButtonForward.setSelected(true);

		ButtonBackward.setVisible(true);
		ButtonBackward.setSize(new java.awt.Dimension(90, 20));
		ButtonBackward.setText("Backward");
		ButtonBackward.setLocation(new java.awt.Point(100, 20));
		ButtonBackward.setToolTipText("Removes parameters starting from a GTR+I+G model");

		jCheckBoxDynamical.setVisible(true);
		jCheckBoxDynamical.setSize(new java.awt.Dimension(200, 20));
		jCheckBoxDynamical.setText("Perform dynamical LRTs");
		jCheckBoxDynamical.setLocation(new java.awt.Point(230, 100));
		jCheckBoxDynamical.setSelected(false);
		jCheckBoxDynamical.setToolTipText("The sequences of LRTs will be build according to best lnL moves");

		jCheckBoxPAUPblock.setVisible(true);
		jCheckBoxPAUPblock.setSize(new java.awt.Dimension(160, 20));
		jCheckBoxPAUPblock.setText("Write PAUP* block");
		jCheckBoxPAUPblock.setLocation(new java.awt.Point(230, 160));
		jCheckBoxPAUPblock.setSelected(false);
		jCheckBoxPAUPblock.setToolTipText("Writes a block of PAUP* commands implementing the selected model");

		JButtonDefaulthLRT.setVisible(true);
		JButtonDefaulthLRT.setSize(new java.awt.Dimension(141, 40));
		JButtonDefaulthLRT.setText("Default Settings");
		JButtonDefaulthLRT.setLocation(new java.awt.Point(10, 320));

		CancelButtonhLRT.setVisible(true);
		CancelButtonhLRT.setSize(new java.awt.Dimension(140, 40));
		CancelButtonhLRT.setText("Cancel");
		CancelButtonhLRT.setLocation(new java.awt.Point(160, 320));

		RunButtonhLRT.setVisible(true);
		RunButtonhLRT.setSize(new java.awt.Dimension(140, 40));
		RunButtonhLRT.setText("Run");
		RunButtonhLRT.setLocation(new java.awt.Point(310, 320));
		RunButtonhLRT.setToolTipText("Click here to start the hLRT selection");
		getRootPane().setDefaultButton(RunButtonhLRT);

		ButtonGroupForwardBackward.add(ButtonForward);
		ButtonGroupForwardBackward.add(ButtonBackward);

		setLocation(new java.awt.Point(281, 80));
		getContentPane().setLayout(null);
		setTitle("Hierarchical Likelihood Ratio Tests (hLRT) Settings");

		PanelSettings.add(TextFieldConfidenceLevelhLRT);
		PanelSettings.add(HypothesisList);
		PanelSettings.add(ButtonListUp);
		PanelSettings.add(ButtonListDown);
		PanelSettings.add(PanelForwardBackward);
		PanelSettings.add(jCheckBoxDynamical);
		PanelSettings.add(jCheckBoxPAUPblock);
		PanelSettings.add(CancelButtonhLRT);
		PanelSettings.add(RunButtonhLRT);
		PanelSettings.add(JButtonDefaulthLRT);
	
		PanelForwardBackward.add(ButtonForward);
		PanelForwardBackward.add(ButtonBackward);
	
		getContentPane().add(PanelSettings);

		setSize(new java.awt.Dimension(482, 420));
		setResizable(false);


		// event handling
		HypothesisList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				HypothesisListValueChanged(e);
			}
		});

		ButtonListUp.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				ButtonListUpActionPerformed(e);
			}
		});
		ButtonListDown.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				ButtonListDownActionPerformed(e);
			}
		});
	
		ButtonForward.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				ButtonForwardActionPerformed(e);
			}
		});


		jCheckBoxDynamical.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				jCheckBoxDynamicalActionPerformed(e);
			}
		});

		CancelButtonhLRT.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				CancelButtonhLRTActionPerformed(e);
			}
		});
		RunButtonhLRT.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				RunButtonhLRTActionPerformed(e);
			}
		});
		JButtonDefaulthLRT.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				JButtonDefaulthLRTActionPerformed(e);
			}
		});
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});


//		for (Enumeration e=testOrder.elements(); e.hasMoreElements();) 
//			ModelTest.testingOrder[i++] = (String)e.nextElement();


	if (options.doF)
		listModel.addElement("freq");	

	listModel.addElement("titv");

	if (options.getSubstTypeCode() == 0)
		listModel.addElement("2ti4tv");
	else if (options.getSubstTypeCode() == 1)
		{
		listModel.addElement("2ti");
		listModel.addElement("2tv");
		}
	else
		{
		listModel.addElement("2ti");
		listModel.addElement("2tv");
		listModel.addElement("4tv");
		}

	if (options.doG)
		listModel.addElement("gamma");

	if (options.doI)
		listModel.addElement("pinv");

	
   HypothesisList.setModel(listModel);
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
	
	public void ButtonListUpActionPerformed(java.awt.event.ActionEvent e) 
		{
		int moveMe = HypothesisList.getSelectedIndex();
		if (moveMe != 0)     //not already at top
			{     
            swap (moveMe, moveMe-1);
            HypothesisList.setSelectedIndex(moveMe-1);
            HypothesisList.ensureIndexIsVisible(moveMe-1);
            }
		}
	
	public void ButtonListDownActionPerformed(java.awt.event.ActionEvent e) 
		{
		int moveMe = HypothesisList.getSelectedIndex();
		if (moveMe != listModel.getSize()-1)     //not already at bottom
			{     
       	 	swap (moveMe, moveMe+1);
       	 	HypothesisList.setSelectedIndex(moveMe+1);
        	HypothesisList.ensureIndexIsVisible(moveMe+1);
        	}
		}
	
	public void HypothesisListValueChanged(ListSelectionEvent e) {
	}
	
    //Swap two elements in the list.
    private void swap(int a, int b) 
    	{
    	if (a >= 0 && b >= 0 
    			&& a < listModel.getSize()
    			&& b < listModel.getSize()) {
	        Object aObject = listModel.getElementAt(a);
	        Object bObject = listModel.getElementAt(b);
	        listModel.set(a, bObject);
	        listModel.set(b, aObject);
	    	}
    	}				
	
	
	public void ButtonForwardActionPerformed(ChangeEvent e)
		{
 		listModel.removeAllElements();
		
		if (options.doF)
			listModel.addElement("freq");	

		if (ButtonForward.isSelected())
			{
			listModel.addElement("titv");
			if (options.getSubstTypeCode() == 0)
				listModel.addElement("2ti4tv");
			else if (options.getSubstTypeCode() == 1)
				{
				listModel.addElement("2ti");
				listModel.addElement("2tv");
				}
			else if (options.getSubstTypeCode() > 1)
				{
				listModel.addElement("2ti");
				listModel.addElement("2tv");
				listModel.addElement("4tv");
				}
			}
		else
			{
			if (options.getSubstTypeCode() == 0)
				listModel.addElement("2ti4tv");
			else if (options.getSubstTypeCode() == 1)
				{
				listModel.addElement("2tv");
				listModel.addElement("2ti");
				}
			else if (options.getSubstTypeCode() > 1)
				{
				listModel.addElement("4tv");
				listModel.addElement("2tv");
				listModel.addElement("2ti");
				}
			listModel.addElement("titv");
			}

		if (options.doG)
			listModel.addElement("gamma");

		if (options.doI)
			listModel.addElement("pinv");

		HypothesisList.setModel(listModel);
		}
	

	public void jCheckBoxDynamicalActionPerformed(java.awt.event.ActionEvent e)
		{
		try
			{
			if (jCheckBoxDynamical.isSelected())
				{
				ButtonListUp.setEnabled(false);
				ButtonListDown.setEnabled(false);
				HypothesisList.setEnabled(false);
				}
			else 
				{
				ButtonListUp.setEnabled(true);
				ButtonListDown.setEnabled(true);
				HypothesisList.setEnabled(true);
				}
			}	
		catch (Exception f) 
			{
			f.printStackTrace();
			}
		}


	public void JButtonDefaulthLRTActionPerformed(java.awt.event.ActionEvent e)
		{
   		TextFieldConfidenceLevelhLRT.setText("0.01");
   		
		listModel.removeAllElements();
		
		if (options.doF)
			listModel.addElement("freq");	

		listModel.addElement("titv");

		if (options.getSubstTypeCode() == 0)
			listModel.addElement("2ti4tv");
		else if (options.getSubstTypeCode() == 1)
			{
			listModel.addElement("2ti");
			listModel.addElement("2tv");
			}
		else
			{
			listModel.addElement("2ti");
			listModel.addElement("2tv");
			listModel.addElement("4tv");
			}

		if (options.doG)
			listModel.addElement("gamma");

		if (options.doI)
			listModel.addElement("pinv");

		HypothesisList.setModel(listModel);
		
				
		ButtonForward.setSelected(true);
		jCheckBoxPAUPblock.setSelected(false);
		jCheckBoxDynamical.setSelected(false);
		}

		
	public void CancelButtonhLRTActionPerformed(java.awt.event.ActionEvent e)
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


	public void RunButtonhLRTActionPerformed(java.awt.event.ActionEvent e)
		{
		int i;
		double alpha;
		boolean forward,writePAUPblock;
		
		setVisible(false);
		dispose();
		
		alpha = Double.parseDouble (TextFieldConfidenceLevelhLRT.getText());
		writePAUPblock = jCheckBoxPAUPblock.isSelected();
		
		if (ButtonForward.isSelected())
			forward = true;
		else
			forward = false;
	
		for (i=0; i< listModel.getSize(); i++) {
			ModelTest.testingOrder.setElementAt((String)listModel.getElementAt(i),i);
		}
	
		/* check whether 2tv goes before 4tv */
		if (forward && ModelTest.testingOrder.indexOf("2tv") > ModelTest.testingOrder.indexOf("4tv"))
			{
			JOptionPane.showMessageDialog(this, "for forward hLRTs the hypothesis 2tv needs\nto be tested before the hypothesis 4tv", 
						"jModelTest error", JOptionPane.ERROR_MESSAGE);
			ModelTest.getMainConsole().println ("\nError: for forward hLRTs the hypothesis 2tv needs\nto be tested before the hypothesis 4tv");
			}
		else	if (!forward && ModelTest.testingOrder.indexOf("2tv") < ModelTest.testingOrder.indexOf("4tv"))
			{
			JOptionPane.showMessageDialog(this, "for backward hLRTs the hypothesis 2tv needs\nto be tested after the hypothesis 4tv", 
						"jModelTest error", JOptionPane.ERROR_MESSAGE);
			ModelTest.getMainConsole().println ("\nError: for backward hLRTs the hypothesis 2tv needs\nto be tested after the hypothesis 4tv");
			}
	
		HLRT myHLRT = new HLRT(options);
		if (jCheckBoxDynamical.isSelected())
			myHLRT.computeDynamical(forward, alpha, writePAUPblock);
		else
			myHLRT.compute(forward, alpha, writePAUPblock);
		}
						
}
