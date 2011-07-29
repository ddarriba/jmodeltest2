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
import java.awt.FileDialog;
import java.awt.Point;
import java.io.File;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;

import pal.tree.Tree;
import pal.tree.TreeParseException;
import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.XManager;
import es.uvigo.darwin.jmodeltest.exe.RunPhyml;
import es.uvigo.darwin.jmodeltest.exe.RunPhymlThread;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.threads.SwingWorker;
import es.uvigo.darwin.jmodeltest.tree.TreeUtilities;

public class Frame_CalcLike extends JModelTestFrame {

	private static final long serialVersionUID = 201103091058L;

	private ApplicationOptions options = ApplicationOptions.getInstance();
	
	private JPanel PanelCalcLike = new JPanel();
	JButton RunButtonCalcLike = new JButton();
	private JButton CancelButtonCalcLike = new JButton();
	private JButton JButtonDefaultCalcLike = new JButton();
	private JPanel PanelNumberModelsCalcLike = new JPanel();
	private JRadioButton Button3SubsTypeCalcLike = new JRadioButton();
	private JRadioButton Button5SubsTypeCalcLike = new JRadioButton();
	private JRadioButton Button7SubsTypeCalcLike = new JRadioButton();
	private JRadioButton Button11SubsTypeCalcLike = new JRadioButton();

	private JPanel PanelFrequenciesCalcLike = new JPanel();
	private JCheckBox jCheckBoxFrequencies = new JCheckBox();

	private JPanel PanelRateVariationCalcLike = new JPanel();
	private JCheckBox jCheckBoxPinv = new JCheckBox();
	private JCheckBox jCheckBoxGamma = new JCheckBox();
	private JTextField TextFieldNcat = new JTextField();

	private JPanel PanelTreeOptimizationCalcLike = new JPanel();
	private JPanel PanelTreeOptimizationMethod = new JPanel();
	private JRadioButton ButtonFixedCalcLike = new JRadioButton();
	private JRadioButton ButtonBIONJCalcLike = new JRadioButton();
	private JRadioButton ButtonMLCalcLike = new JRadioButton();
	private JRadioButton ButtonFixedUserTopologyCalcLike = new JRadioButton();
	private JRadioButton ButtonNNICalcLike = new JRadioButton();
	private JRadioButton ButtonSPRCalcLike = new JRadioButton();
	private JRadioButton ButtonBestCalcLike = new JRadioButton();

	private ButtonGroup ButtonGroupTreeOptimizationCalcLike = new ButtonGroup();
	private ButtonGroup ButtonGroupNumberModelsCalcLike = new ButtonGroup();
	private ButtonGroup ButtonGroupTreeSearchCalcLike = new ButtonGroup();

	private JLabel jLabelNumModels = new JLabel();
	private JLabel jLabelUserTree = new JLabel();
	private JLabel jLabelUserTopology = new JLabel();

	private Frame_Progress progressFrame;

	private RunPhyml runPhyml;
	private ComputeLikelihoodTask task;

	public void cancelTask() {
		task.interrupt();
	}
	
	public RunPhyml getRunPhyml() {
		return runPhyml;
	}
	
	public Frame_CalcLike() {
	}

	public void initComponents() throws Exception {
		PanelCalcLike.setLocation(new java.awt.Point(10, 10));
		PanelCalcLike.setSize(new java.awt.Dimension(460, 360));
		PanelCalcLike
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(new java.awt.Color(
								153, 153, 153), 1, false),
						"Likelihood settings", 4, 2, new java.awt.Font(
								"Application", 1, 10), new java.awt.Color(102,
								102, 153)));
		PanelCalcLike.setVisible(true);
		PanelCalcLike.setLayout(null);

		JButtonDefaultCalcLike.setText("Default Settings");
		JButtonDefaultCalcLike.setLocation(new java.awt.Point(10, 310));
		JButtonDefaultCalcLike.setSize(new java.awt.Dimension(141, 40));
		JButtonDefaultCalcLike.setVisible(true);

		CancelButtonCalcLike.setText("Cancel");
		CancelButtonCalcLike.setLocation(new java.awt.Point(160, 310));
		CancelButtonCalcLike.setSize(new java.awt.Dimension(110, 40));
		CancelButtonCalcLike.setVisible(true);

		RunButtonCalcLike.setText("Compute Likelihods");
		RunButtonCalcLike.setLocation(new java.awt.Point(280, 310));
		RunButtonCalcLike.setSize(new java.awt.Dimension(170, 40));
		RunButtonCalcLike.setVisible(true);
		getRootPane().setDefaultButton(RunButtonCalcLike);

		/*
		 * CheckBoxBLengthsAsParameters.setVisible(true);
		 * CheckBoxBLengthsAsParameters.setSize(new java.awt.Dimension(280,
		 * 20)); CheckBoxBLengthsAsParameters.setText(
		 * "Consider branch lengths as parameters");
		 * CheckBoxBLengthsAsParameters.setSelected(true);
		 * CheckBoxBLengthsAsParameters.setLocation(new java.awt.Point(30, 90));
		 * CheckBoxFixedTopology.setVisible(true);
		 * CheckBoxFixedTopology.setSize(new java.awt.Dimension(340, 20));
		 * CheckBoxFixedTopology
		 * .setText("Use a BIONJ-JC tree to optimize all models");
		 * CheckBoxFixedTopology.setSelected(false);
		 * CheckBoxFixedTopology.setLocation(new java.awt.Point(30, 120));
		 * CheckBoxOptimizeTopology.setVisible(true);
		 * CheckBoxOptimizeTopology.setSize(new java.awt.Dimension(340, 20));
		 * CheckBoxOptimizeTopology
		 * .setText("Optimize by ML the topology for each model");
		 * CheckBoxOptimizeTopology.setSelected(true);
		 * CheckBoxOptimizeTopology.setLocation(new java.awt.Point(30, 150));
		 */
		PanelNumberModelsCalcLike.setLocation(new java.awt.Point(20, 20));
		PanelNumberModelsCalcLike.setSize(new java.awt.Dimension(270, 50));
		PanelNumberModelsCalcLike
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(new java.awt.Color(
								153, 153, 153), 1, false),
						"Number of substitution schemes", 4, 2,
						new java.awt.Font("Application", 1, 10),
						new java.awt.Color(102, 102, 153)));
		PanelNumberModelsCalcLike.setVisible(true);
		PanelNumberModelsCalcLike.setLayout(null);
		Button3SubsTypeCalcLike.setVisible(true);
		Button3SubsTypeCalcLike.setSize(new java.awt.Dimension(50, 20));
		Button3SubsTypeCalcLike.setText("3");
		Button3SubsTypeCalcLike.setLocation(new java.awt.Point(20, 20));
		Button5SubsTypeCalcLike.setVisible(true);
		Button5SubsTypeCalcLike.setSize(new java.awt.Dimension(50, 20));
		Button5SubsTypeCalcLike.setText("5");
		Button5SubsTypeCalcLike.setLocation(new java.awt.Point(80, 20));
		Button7SubsTypeCalcLike.setVisible(true);
		Button7SubsTypeCalcLike.setSize(new java.awt.Dimension(50, 20));
		Button7SubsTypeCalcLike.setText("7");
		Button7SubsTypeCalcLike.setLocation(new java.awt.Point(140, 20));
		Button11SubsTypeCalcLike.setVisible(true);
		Button11SubsTypeCalcLike.setSize(new java.awt.Dimension(50, 20));
		Button11SubsTypeCalcLike.setText("11");
		Button11SubsTypeCalcLike.setLocation(new java.awt.Point(200, 20));
		Button11SubsTypeCalcLike.setSelected(true);
		ButtonGroupNumberModelsCalcLike.add(Button3SubsTypeCalcLike);
		ButtonGroupNumberModelsCalcLike.add(Button5SubsTypeCalcLike);
		ButtonGroupNumberModelsCalcLike.add(Button7SubsTypeCalcLike);
		ButtonGroupNumberModelsCalcLike.add(Button11SubsTypeCalcLike);

		PanelFrequenciesCalcLike.setSize(new java.awt.Dimension(110, 50));
		PanelFrequenciesCalcLike
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(new java.awt.Color(
								153, 153, 153), 1, false), "Base frequencies",
						4, 2, new java.awt.Font("Application", 1, 10),
						new java.awt.Color(102, 102, 153)));
		PanelFrequenciesCalcLike.setLocation(new java.awt.Point(20, 80));
		PanelFrequenciesCalcLike.setVisible(true);
		PanelFrequenciesCalcLike.setLayout(null);
		jCheckBoxFrequencies.setVisible(true);
		jCheckBoxFrequencies.setSize(new java.awt.Dimension(50, 20));
		jCheckBoxFrequencies.setText("+F");
		jCheckBoxFrequencies.setLocation(new java.awt.Point(20, 20));
		jCheckBoxFrequencies.setSelected(true);

		PanelRateVariationCalcLike.setLocation(new java.awt.Point(150, 80));
		PanelRateVariationCalcLike.setSize(new java.awt.Dimension(180, 50));
		PanelRateVariationCalcLike
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(new java.awt.Color(
								153, 153, 153), 1, false), "Rate variation", 4,
						2, new java.awt.Font("Application", 1, 10),
						new java.awt.Color(102, 102, 153)));
		PanelRateVariationCalcLike.setVisible(true);
		PanelRateVariationCalcLike.setLayout(null);
		jCheckBoxPinv.setLocation(new java.awt.Point(20, 20));
		jCheckBoxPinv.setSize(new java.awt.Dimension(50, 20));
		jCheckBoxPinv.setText("+I");
		jCheckBoxPinv.setVisible(true);
		jCheckBoxPinv.setSelected(true);
		jCheckBoxGamma.setVisible(true);
		jCheckBoxGamma.setSize(new java.awt.Dimension(55, 20));
		jCheckBoxGamma.setText("+G");
		jCheckBoxGamma.setLocation(new java.awt.Point(70, 20));
		jCheckBoxGamma.setSelected(true);
		TextFieldNcat.setEnabled(true);
		TextFieldNcat
				.setToolTipText("Number of rate categories for the discrete gamma distribution");
		TextFieldNcat
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(new java.awt.Color(
								153, 153, 153), 1, false), "nCat", 4, 2,
						new java.awt.Font("Application", 1, 10),
						new java.awt.Color(102, 102, 153)));
		TextFieldNcat.setVisible(true);
		TextFieldNcat.setSize(new java.awt.Dimension(40, 35));
		TextFieldNcat.setText("4");
		TextFieldNcat.setHorizontalAlignment(JTextField.RIGHT);
		TextFieldNcat.setLocation(new java.awt.Point(122, 10));

		PanelTreeOptimizationCalcLike.setLocation(new java.awt.Point(20, 140));
		PanelTreeOptimizationCalcLike.setSize(new java.awt.Dimension(350, 90));
		PanelTreeOptimizationCalcLike
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(new java.awt.Color(
								153, 153, 153), 1, false),
						"Base tree for likelihood calculations", 4, 2,
						new java.awt.Font("Application", 1, 10),
						new java.awt.Color(102, 102, 153)));
		PanelTreeOptimizationCalcLike.setVisible(true);
		PanelTreeOptimizationCalcLike.setLayout(null);

		ButtonFixedCalcLike.setText("Fixed BIONJ-JC");
		ButtonFixedCalcLike.setSize(new java.awt.Dimension(130, 20));
		ButtonFixedCalcLike.setLocation(new java.awt.Point(20, 20));
		ButtonFixedCalcLike.setVisible(true);

		ButtonFixedUserTopologyCalcLike.setText("Fixed user topology");
		ButtonFixedUserTopologyCalcLike
				.setLocation(new java.awt.Point(170, 20));
		ButtonFixedUserTopologyCalcLike
				.setSize(new java.awt.Dimension(160, 20));
		ButtonFixedUserTopologyCalcLike.setVisible(true);

		ButtonBIONJCalcLike.setText("BIONJ");
		ButtonBIONJCalcLike.setLocation(new java.awt.Point(20, 57));
		ButtonBIONJCalcLike.setSize(new java.awt.Dimension(90, 20));
		ButtonBIONJCalcLike.setVisible(true);

		ButtonMLCalcLike.setText("ML optimized");
		ButtonMLCalcLike.setLocation(new java.awt.Point(170, 57));
		ButtonMLCalcLike.setSize(new java.awt.Dimension(130, 20));
		ButtonMLCalcLike.setVisible(true);
		ButtonMLCalcLike.setSelected(true);

		jLabelUserTopology.setLocation(new java.awt.Point(194, 22));
		jLabelUserTopology.setSize(new java.awt.Dimension(300, 50));
		jLabelUserTopology.setForeground(java.awt.Color.gray);
		jLabelUserTopology.setFont(XManager.FONT_LABEL_BIG);
		jLabelUserTopology.setVisible(true);

		ButtonGroupTreeOptimizationCalcLike.add(ButtonFixedCalcLike);
		ButtonGroupTreeOptimizationCalcLike.add(ButtonBIONJCalcLike);
		ButtonGroupTreeOptimizationCalcLike.add(ButtonMLCalcLike);
		ButtonGroupTreeOptimizationCalcLike
				.add(ButtonFixedUserTopologyCalcLike);

		jLabelNumModels.setSize(new java.awt.Dimension(400, 50));
		jLabelNumModels.setLocation(new java.awt.Point(310, 25));
		jLabelNumModels.setVisible(true);
		jLabelNumModels.setForeground(java.awt.Color.gray);
		jLabelNumModels.setFont(XManager.FONT_LABEL_BIG);
		jLabelNumModels.setText("Number of models = 88");

		setLocation(new java.awt.Point(281, 80));
		getContentPane().setLayout(null);
		setTitle("Likelihood settings");

		PanelTreeOptimizationMethod.setLocation(new java.awt.Point(20, 240));
		PanelTreeOptimizationMethod.setSize(new java.awt.Dimension(250, 60));
		PanelTreeOptimizationMethod
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(new java.awt.Color(
								153, 153, 153), 1, false),
						"Tree topology search operations", 4, 2,
						new java.awt.Font("Application", 1, 10),
						new java.awt.Color(102, 102, 153)));
		PanelTreeOptimizationMethod.setVisible(true);
		PanelTreeOptimizationMethod.setLayout(null);
		
		ButtonNNICalcLike.setText("NNI");
		ButtonNNICalcLike.setLocation(new java.awt.Point(20, 20));
		ButtonNNICalcLike.setSize(new java.awt.Dimension(70, 20));
		ButtonNNICalcLike.setVisible(true);
		ButtonNNICalcLike.setSelected(true);
		
		ButtonSPRCalcLike.setText("SPR");
		ButtonSPRCalcLike.setLocation(new java.awt.Point(90, 20));
		ButtonSPRCalcLike.setSize(new java.awt.Dimension(70, 20));
		ButtonSPRCalcLike.setVisible(true);
		
		ButtonBestCalcLike.setText("Best");
		ButtonBestCalcLike.setLocation(new java.awt.Point(160, 20));
		ButtonBestCalcLike.setSize(new java.awt.Dimension(70, 20));
		ButtonBestCalcLike.setVisible(true);
		
		ButtonGroupTreeSearchCalcLike.add(ButtonNNICalcLike);
		ButtonGroupTreeSearchCalcLike.add(ButtonSPRCalcLike);
		ButtonGroupTreeSearchCalcLike.add(ButtonBestCalcLike);
		
		PanelCalcLike.add(RunButtonCalcLike);
		PanelCalcLike.add(CancelButtonCalcLike);
		PanelCalcLike.add(JButtonDefaultCalcLike);
		PanelCalcLike.add(PanelNumberModelsCalcLike);
		PanelCalcLike.add(PanelTreeOptimizationCalcLike);
		PanelCalcLike.add(PanelTreeOptimizationMethod);
		PanelCalcLike.add(PanelRateVariationCalcLike);
		PanelCalcLike.add(PanelFrequenciesCalcLike);
		PanelCalcLike.add(jLabelNumModels);

		PanelNumberModelsCalcLike.add(Button3SubsTypeCalcLike);
		PanelNumberModelsCalcLike.add(Button5SubsTypeCalcLike);
		PanelNumberModelsCalcLike.add(Button7SubsTypeCalcLike);
		PanelNumberModelsCalcLike.add(Button11SubsTypeCalcLike);
		PanelRateVariationCalcLike.add(jCheckBoxPinv);
		PanelRateVariationCalcLike.add(jCheckBoxGamma);
		PanelRateVariationCalcLike.add(TextFieldNcat);
		PanelFrequenciesCalcLike.add(jCheckBoxFrequencies);
		PanelTreeOptimizationCalcLike.add(ButtonFixedCalcLike);
		PanelTreeOptimizationCalcLike.add(ButtonBIONJCalcLike);
		PanelTreeOptimizationCalcLike.add(ButtonMLCalcLike);
		PanelTreeOptimizationCalcLike.add(ButtonFixedUserTopologyCalcLike);
		PanelTreeOptimizationMethod.add(ButtonNNICalcLike);
		PanelTreeOptimizationMethod.add(ButtonSPRCalcLike);
		PanelTreeOptimizationMethod.add(ButtonBestCalcLike);
		// PanelTreeOptimizationCalcLike.add(ButtonFixedUserTreeCalcLike);
		PanelTreeOptimizationCalcLike.add(jLabelUserTopology);
		PanelTreeOptimizationCalcLike.add(jLabelUserTree);

		getContentPane().add(PanelCalcLike);

		setSize(new java.awt.Dimension(480, 410));
		setResizable(false);

		// event handling
		RunButtonCalcLike
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						RunButtonCalcLikeActionPerformed(e);
					}
				});

		CancelButtonCalcLike
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						CancelButtonCalcLikeActionPerformed(e);
					}
				});

		JButtonDefaultCalcLike
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						JButtonDefaultCalcLikeActionPerformed(e);
					}
				});

		// common event handling for set of models
		Button3SubsTypeCalcLike
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						CalculateNumberOfModels(e);
					}
				});
		Button5SubsTypeCalcLike
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						CalculateNumberOfModels(e);
					}
				});
		Button7SubsTypeCalcLike
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						CalculateNumberOfModels(e);
					}
				});
		Button11SubsTypeCalcLike
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						CalculateNumberOfModels(e);
					}
				});
		jCheckBoxPinv.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				CalculateNumberOfModels(e);
			}
		});
		jCheckBoxGamma.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {

				if (jCheckBoxGamma.isSelected())
					TextFieldNcat.setEnabled(true);
				else
					TextFieldNcat.setEnabled(false);

				CalculateNumberOfModels(e);
			}
		});

		jCheckBoxFrequencies
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						CalculateNumberOfModels(e);
					}
				});

		ButtonFixedUserTopologyCalcLike
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						ReadUserTopologyCalcLike(e);
					}
				});
		/*
		 * ButtonFixedUserTreeCalcLike.addActionListener(new
		 * java.awt.event.ActionListener() { public void
		 * actionPerformed(java.awt.event.ActionEvent e) {
		 * ReadUserTreeCalcLike(e); } });
		 * 
		 * 
		 * CheckBoxFixedTopology.addActionListener(new
		 * java.awt.event.ActionListener() { public void
		 * actionPerformed(java.awt.event.ActionEvent e) {
		 * CheckBoxFixedTopologyActionPerformed(e); } });
		 * 
		 * 
		 * CheckBoxOptimizeTopology.addActionListener(new
		 * java.awt.event.ActionListener() { public void
		 * actionPerformed(java.awt.event.ActionEvent e) {
		 * CheckBoxOptimizeTopologyActionPerformed(e); } });
		 */
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

	public void RunButtonCalcLikeActionPerformed(java.awt.event.ActionEvent e) {
		try {
			// get parameters
			if (Button3SubsTypeCalcLike.isSelected())
				options.setSubstTypeCode(0);
			else if (Button5SubsTypeCalcLike.isSelected())
				options.setSubstTypeCode(1);
			else if (Button7SubsTypeCalcLike.isSelected())
				options.setSubstTypeCode(2);
			else
				options.setSubstTypeCode(3);

			if (jCheckBoxFrequencies.isSelected())
				options.doF = true;
			else
				options.doF = false;

			if (jCheckBoxPinv.isSelected())
				options.doI = true;
			else
				options.doI = false;

			if (jCheckBoxGamma.isSelected()) {
				options.doG = true;
				options.numGammaCat = Integer.parseInt(TextFieldNcat
						.getText());
			} else
				options.doG = false;

			CalculateNumberOfModels(e);

			// base tree for ML calculations
			if (ButtonFixedCalcLike.isSelected()) {
				options.fixedTopology = true;
				options.optimizeMLTopology = false;
				options.userTopologyExists = false;
				// ModelTest.userTreeExists = false;
			} else if (ButtonBIONJCalcLike.isSelected()) {
				options.fixedTopology = false;
				options.optimizeMLTopology = false;
				options.userTopologyExists = false;
				// ModelTest.userTreeExists = false;
			} else if (ButtonMLCalcLike.isSelected()) {
				options.fixedTopology = false;
				options.optimizeMLTopology = true;
				options.userTopologyExists = false;
				// ModelTest.userTreeExists = false;
			} else if (ButtonFixedUserTopologyCalcLike.isSelected()) {
				options.fixedTopology = false;
				options.optimizeMLTopology = false;
				options.userTopologyExists = true;
				// ModelTest.userTreeExists = false;
			}
			
			if (ButtonNNICalcLike.isSelected()) {
				options.treeSearchOperations = ApplicationOptions.TreeSearch.NNI;
			} else if (ButtonSPRCalcLike.isSelected()) {
				options.treeSearchOperations = ApplicationOptions.TreeSearch.SPR;
			} else if (ButtonBestCalcLike.isSelected()) {
				options.treeSearchOperations = ApplicationOptions.TreeSearch.BEST;
			}
			/*
			 * else if (ButtonFixedUserTreeCalcLike.isSelected()) {
			 * ModelTest.fixedTopology = false; ModelTest.optimizeMLTopology =
			 * false; ModelTest.userTopologyExists = false;
			 * ModelTest.userTreeExists = true; }
			 */else { /* should not be here */
			}

			// build set of models
			options.setCandidateModels();

			// build progress frame
			progressFrame = new Frame_Progress(options.numModels, this, options);

			setVisible(false);
			dispose();

			// scroll to the bottom
			XManager.getInstance()
					.getPane()
					.setCaretPosition(
							XManager.getInstance().getPane().getDocument()
									.getLength());
			// run phyml
			this.task = new ComputeLikelihoodTask();
			this.runPhyml = task.getValue();
			
			task.start();

		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	public void JButtonDefaultCalcLikeActionPerformed(
			java.awt.event.ActionEvent e) {
		try {
			Button11SubsTypeCalcLike.setSelected(true);
			ButtonMLCalcLike.setSelected(true);

			jCheckBoxFrequencies.setSelected(true);
			jCheckBoxPinv.setSelected(true);
			jCheckBoxGamma.setSelected(true);
			TextFieldNcat.setEnabled(true);
			TextFieldNcat.setText("4");

			jLabelNumModels.setText("Number of models = " + 88);
			jLabelUserTopology.setText("");
			jLabelUserTree.setText("");

		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	public void CancelButtonCalcLikeActionPerformed(java.awt.event.ActionEvent e) {
		try {
			setVisible(false);
			dispose();
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	public void CalculateNumberOfModels(java.awt.event.ActionEvent e) {
		int numberOfModels;
		try {
			if (Button3SubsTypeCalcLike.isSelected())
				numberOfModels = 3;
			else if (Button5SubsTypeCalcLike.isSelected())
				numberOfModels = 5;
			else if (Button7SubsTypeCalcLike.isSelected())
				numberOfModels = 7;
			else
				numberOfModels = 11;

			if (jCheckBoxFrequencies.isSelected())
				numberOfModels *= 2;

			if (jCheckBoxPinv.isSelected()
					&& jCheckBoxGamma.isSelected())
				numberOfModels *= 4;
			else if (jCheckBoxPinv.isSelected()
					|| jCheckBoxGamma.isSelected())
				numberOfModels *= 2;

			options.numModels = numberOfModels;
			jLabelNumModels.setText("Number of models = " + numberOfModels);
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	public void ReadUserTopologyCalcLike(java.awt.event.ActionEvent e) {
		FileDialog fc = new FileDialog(this,
				"Load Newick tree file with branch lengths", FileDialog.LOAD);
		fc.setDirectory(System.getProperty("user.dir"));
		fc.setVisible(true);

		String treefilename = fc.getFile();

		if (treefilename != null) // menu not canceled
		{
			String treefilenameComplete = fc.getDirectory() + treefilename;
			ModelTest.getMainConsole().print(
					"Reading tree file \"" + treefilename + "\"...");
			System.err.print("\nreading tree file \"" + treefilename + "\"...");

			options.setInputTreeFile(new File(treefilename));

			// read the tree in
			Tree tree = null;
			try {
				tree = TreeUtilities.readTree(treefilenameComplete);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this, "The specified file \""
						+ treefilename + "\" cannot be found",
						"jModelTest error", JOptionPane.ERROR_MESSAGE);
				ModelTest.getMainConsole().println(" failed.\n");
				ButtonMLCalcLike.setSelected(true);
				;
			} catch (TreeParseException e1) {
				ModelTest.getMainConsole().println(" failed.\n");
				System.err.println(" failed.\n");
				ButtonMLCalcLike.setSelected(true);
				;
			}
			if (tree != null) {
				// delete previous usertreefile from phyml directory if needed
				// Utilities.deleteFile(RunPhyml.userTreePhymlFileName);
				options.setUserTree(TreeUtilities.toNewick(tree, true,
						false, false));
				TextOutputStream out = new TextOutputStream(
						options.getTreeFile().getAbsolutePath());
				System.out.println("INPUT TREE: "
						+ options.getUserTree());
				out.print(options.getUserTree());
				out.close();
				jLabelUserTopology.setText(treefilename);
				ModelTest.getMainConsole().println(" OK.");
				System.err.println("OK.");
			}
		} else // file does not exists
		{
			ButtonMLCalcLike.setSelected(true);
			// ModelTest.mainConsole.println ("\nOpen file canceled\n");
		}
	}

	private class ComputeLikelihoodTask extends SwingWorker {

		private RunPhyml runPhyml;
		
		public ComputeLikelihoodTask() {
			this.runPhyml = new RunPhymlThread(progressFrame, options, ModelTest.model);
		}
		
		public Object construct() {
			this.runPhyml.execute();
			return runPhyml;
		}

		public void finished() {

		}
		
		public RunPhyml getValue() {
			return runPhyml;
		}

	}

}
