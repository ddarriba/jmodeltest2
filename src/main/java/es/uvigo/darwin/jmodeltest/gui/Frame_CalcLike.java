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


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.BorderUIResource;

import pal.tree.Tree;
import pal.tree.TreeParseException;
import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.exe.RunPhyml;
import es.uvigo.darwin.jmodeltest.exe.RunPhymlThread;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.threads.SwingWorker;
import es.uvigo.darwin.jmodeltest.tree.TreeUtilities;

public class Frame_CalcLike extends JModelTestFrame {

	private static final long serialVersionUID = 201103091058L;
	private static final int TOTAL_NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();
	private static final int DEFAULT_NUMBER_OF_THREADS = TOTAL_NUMBER_OF_THREADS; 
	
	private JPanel PanelProcessors = new JPanel();
	private JSlider SliderProcessors = new JSlider(1,TOTAL_NUMBER_OF_THREADS,DEFAULT_NUMBER_OF_THREADS);
	private JLabel jLabelNumProcessors = new JLabel();
	
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
	
	public Frame_CalcLike(ModelTest modelTest) {
		super(modelTest);
	}

	public void initComponents() throws Exception {
		PanelProcessors.setLocation(10, 10);
		PanelProcessors.setSize(460, 80);
		PanelProcessors
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(XManager.PANEL_BORDER_COLOR, 1, false),
						"Number of processors requested", 4, 2, XManager.FONT_LABEL,
						XManager.LABEL_BLUE_COLOR));
		PanelProcessors.setVisible(true);
		PanelProcessors.setLayout(null);
		
		SliderProcessors.setValue(SliderProcessors.getMaximum());
		SliderProcessors.setLocation(10, 20);
		SliderProcessors.setSize(400, 45);
		SliderProcessors.setPaintLabels(false);
		
		jLabelNumProcessors.setText(String.valueOf(SliderProcessors.getValue()));
		jLabelNumProcessors.setLocation(420, 25);
		jLabelNumProcessors.setSize(20,40);
		
		PanelProcessors.add(SliderProcessors);
		PanelProcessors.add(jLabelNumProcessors);
		
		PanelCalcLike.setLocation(10, 100);
		PanelCalcLike.setSize(460, 360);
		PanelCalcLike
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(XManager.PANEL_BORDER_COLOR, 1, false),
						"Likelihood settings", 4, 2, XManager.FONT_LABEL,
						XManager.LABEL_BLUE_COLOR));
		PanelCalcLike.setVisible(true);
		PanelCalcLike.setLayout(null);

		JButtonDefaultCalcLike.setText("Default Settings");
		JButtonDefaultCalcLike.setLocation(10, 310);
		JButtonDefaultCalcLike.setSize(141, 40);
		JButtonDefaultCalcLike.setVisible(true);

		CancelButtonCalcLike.setText("Cancel");
		CancelButtonCalcLike.setLocation(160, 310);
		CancelButtonCalcLike.setSize(110, 40);
		CancelButtonCalcLike.setVisible(true);

		RunButtonCalcLike.setText("Compute Likelihods");
		RunButtonCalcLike.setLocation(280, 310);
		RunButtonCalcLike.setSize(170, 40);
		RunButtonCalcLike.setVisible(true);
		getRootPane().setDefaultButton(RunButtonCalcLike);

		PanelNumberModelsCalcLike.setLocation(20, 20);
		PanelNumberModelsCalcLike.setSize(420, 50);
		PanelNumberModelsCalcLike
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(XManager.PANEL_BORDER_COLOR, 1, false),
						"Number of substitution schemes", 4, 2,
						XManager.FONT_LABEL,
						XManager.LABEL_BLUE_COLOR));
		PanelNumberModelsCalcLike.setVisible(true);
		PanelNumberModelsCalcLike.setLayout(null);
		Button3SubsTypeCalcLike.setVisible(true);
		Button3SubsTypeCalcLike.setSize(50, 20);
		Button3SubsTypeCalcLike.setText("3");
		Button3SubsTypeCalcLike.setLocation(20, 20);
		Button5SubsTypeCalcLike.setVisible(true);
		Button5SubsTypeCalcLike.setSize(50, 20);
		Button5SubsTypeCalcLike.setText("5");
		Button5SubsTypeCalcLike.setLocation(70, 20);
		Button7SubsTypeCalcLike.setVisible(true);
		Button7SubsTypeCalcLike.setSize(50, 20);
		Button7SubsTypeCalcLike.setText("7");
		Button7SubsTypeCalcLike.setLocation(120, 20);
		Button11SubsTypeCalcLike.setVisible(true);
		Button11SubsTypeCalcLike.setSize(50, 20);
		Button11SubsTypeCalcLike.setText("11");
		Button11SubsTypeCalcLike.setLocation(170, 20);
		Button11SubsTypeCalcLike.setSelected(true);
		ButtonGroupNumberModelsCalcLike.add(Button3SubsTypeCalcLike);
		ButtonGroupNumberModelsCalcLike.add(Button5SubsTypeCalcLike);
		ButtonGroupNumberModelsCalcLike.add(Button7SubsTypeCalcLike);
		ButtonGroupNumberModelsCalcLike.add(Button11SubsTypeCalcLike);
		jLabelNumModels.setSize(160, 20);
		jLabelNumModels.setLocation(240, 20);
		jLabelNumModels.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		jLabelNumModels.setAlignmentY(JLabel.CENTER_ALIGNMENT);
		jLabelNumModels.setVisible(true);
		jLabelNumModels.setForeground(Color.gray);
		jLabelNumModels.setFont(XManager.FONT_LABEL_BIG);
		
		PanelFrequenciesCalcLike.setSize(130, 50);
		PanelFrequenciesCalcLike
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(XManager.PANEL_BORDER_COLOR, 1, false), "Base frequencies",
						4, 2, XManager.FONT_LABEL,
						XManager.LABEL_BLUE_COLOR));
		PanelFrequenciesCalcLike.setLocation(20, 80);
		PanelFrequenciesCalcLike.setVisible(true);
		PanelFrequenciesCalcLike.setLayout(null);
		jCheckBoxFrequencies.setVisible(true);
		jCheckBoxFrequencies.setSize(50, 20);
		jCheckBoxFrequencies.setText("+F");
		jCheckBoxFrequencies.setLocation(20, 20);
		jCheckBoxFrequencies.setSelected(true);

		PanelRateVariationCalcLike.setLocation(170, 80);
		PanelRateVariationCalcLike.setSize(270, 50);
		PanelRateVariationCalcLike
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(XManager.PANEL_BORDER_COLOR, 1, false), "Rate variation", 4,
						2, XManager.FONT_LABEL,
						XManager.LABEL_BLUE_COLOR));
		PanelRateVariationCalcLike.setVisible(true);
		PanelRateVariationCalcLike.setLayout(null);
		jCheckBoxPinv.setLocation(20, 20);
		jCheckBoxPinv.setSize(50, 20);
		jCheckBoxPinv.setText("+I");
		jCheckBoxPinv.setVisible(true);
		jCheckBoxPinv.setSelected(true);
		jCheckBoxGamma.setVisible(true);
		jCheckBoxGamma.setSize(55, 20);
		jCheckBoxGamma.setText("+G");
		jCheckBoxGamma.setLocation(70, 20);
		jCheckBoxGamma.setSelected(true);
		TextFieldNcat.setEnabled(true);
		TextFieldNcat
				.setToolTipText("Number of rate categories for the discrete gamma distribution");
		TextFieldNcat
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(XManager.PANEL_BORDER_COLOR, 1, false), "nCat", 4, 2,
						XManager.FONT_LABEL,
						XManager.LABEL_BLUE_COLOR));
		TextFieldNcat.setVisible(true);
		TextFieldNcat.setSize(60, 35);
		TextFieldNcat.setText("4");
		TextFieldNcat.setHorizontalAlignment(JTextField.RIGHT);
		TextFieldNcat.setLocation(122, 10);

		PanelTreeOptimizationCalcLike.setLocation(20, 140);
		PanelTreeOptimizationCalcLike.setSize(420, 90);
		PanelTreeOptimizationCalcLike
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(XManager.PANEL_BORDER_COLOR, 1, false),
						"Base tree for likelihood calculations", 4, 2,
						XManager.FONT_LABEL,
						XManager.LABEL_BLUE_COLOR));
		PanelTreeOptimizationCalcLike.setVisible(true);
		PanelTreeOptimizationCalcLike.setLayout(null);

		ButtonFixedCalcLike.setText("Fixed BIONJ-JC");
		ButtonFixedCalcLike.setSize(160, 20);
		ButtonFixedCalcLike.setLocation(20, 20);
		ButtonFixedCalcLike.setVisible(true);

		ButtonFixedUserTopologyCalcLike.setText("Fixed user topology");
		ButtonFixedUserTopologyCalcLike
				.setLocation(200, 20);
		ButtonFixedUserTopologyCalcLike
				.setSize(200, 20);
		ButtonFixedUserTopologyCalcLike.setVisible(true);

		ButtonBIONJCalcLike.setText("BIONJ");
		ButtonBIONJCalcLike.setLocation(20, 57);
		ButtonBIONJCalcLike.setSize(160, 20);
		ButtonBIONJCalcLike.setVisible(true);

		ButtonMLCalcLike.setText("ML optimized");
		ButtonMLCalcLike.setLocation(200, 57);
		ButtonMLCalcLike.setSize(200, 20);
		ButtonMLCalcLike.setVisible(true);
		ButtonMLCalcLike.setSelected(true);
		
		enableTreeSearching(ButtonMLCalcLike.isSelected());

		jLabelUserTopology.setLocation(194, 22);
		jLabelUserTopology.setSize(300, 50);
		jLabelUserTopology.setForeground(Color.gray);
		jLabelUserTopology.setFont(XManager.FONT_LABEL_BIG);
		jLabelUserTopology.setVisible(true);

		ButtonGroupTreeOptimizationCalcLike.add(ButtonFixedCalcLike);
		ButtonGroupTreeOptimizationCalcLike.add(ButtonBIONJCalcLike);
		ButtonGroupTreeOptimizationCalcLike.add(ButtonMLCalcLike);
		ButtonGroupTreeOptimizationCalcLike
				.add(ButtonFixedUserTopologyCalcLike);

		setLocation(XManager.MAIN_LOCATION);
		getContentPane().setLayout(null);
		setTitle("Likelihood settings");

		PanelTreeOptimizationMethod.setLocation(95, 240);
		PanelTreeOptimizationMethod.setSize(250, 60);
		PanelTreeOptimizationMethod
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(XManager.PANEL_BORDER_COLOR, 1, false),
						"Base tree search", 4, 2,
						XManager.FONT_LABEL,
						XManager.LABEL_BLUE_COLOR));
		PanelTreeOptimizationMethod.setVisible(true);
		PanelTreeOptimizationMethod.setLayout(null);
		
		ButtonNNICalcLike.setText("NNI");
		ButtonNNICalcLike.setLocation(20, 20);
		ButtonNNICalcLike.setSize(70, 20);
		ButtonNNICalcLike.setVisible(true);
		
		ButtonSPRCalcLike.setText("SPR");
		ButtonSPRCalcLike.setLocation(90, 20);
		ButtonSPRCalcLike.setSize(70, 20);
		ButtonSPRCalcLike.setVisible(true);
		
		ButtonBestCalcLike.setText("Best");
		ButtonBestCalcLike.setLocation(160, 20);
		ButtonBestCalcLike.setSize(70, 20);
		ButtonBestCalcLike.setVisible(true);
		ButtonBestCalcLike.setSelected(true);
		ButtonBestCalcLike.setToolTipText("Best of NNI and SPR algorithms. Tests both of them (Slowest).");
		
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

		PanelNumberModelsCalcLike.add(Button3SubsTypeCalcLike);
		PanelNumberModelsCalcLike.add(Button5SubsTypeCalcLike);
		PanelNumberModelsCalcLike.add(Button7SubsTypeCalcLike);
		PanelNumberModelsCalcLike.add(Button11SubsTypeCalcLike);
		PanelNumberModelsCalcLike.add(jLabelNumModels);
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

		getContentPane().add(PanelProcessors);
		getContentPane().add(PanelCalcLike);

		setSize(480, 500);
		setResizable(false);

		// Update number of models
		CalculateNumberOfModels(null);
		
		// event handling
		RunButtonCalcLike
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						RunButtonCalcLikeActionPerformed(e);
					}
				});

		CancelButtonCalcLike
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						CancelButtonCalcLikeActionPerformed(e);
					}
				});

		JButtonDefaultCalcLike
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JButtonDefaultCalcLikeActionPerformed(e);
					}
				});
		
		SliderProcessors
		.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				SliderChangeListener(e);
			}
		});

		// common event handling for set of models
		Button3SubsTypeCalcLike
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						CalculateNumberOfModels(e);
					}
				});
		Button5SubsTypeCalcLike
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						CalculateNumberOfModels(e);
					}
				});
		Button7SubsTypeCalcLike
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						CalculateNumberOfModels(e);
					}
				});
		Button11SubsTypeCalcLike
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						CalculateNumberOfModels(e);
					}
				});
		jCheckBoxPinv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CalculateNumberOfModels(e);
			}
		});
		jCheckBoxGamma.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (jCheckBoxGamma.isSelected())
					TextFieldNcat.setEnabled(true);
				else
					TextFieldNcat.setEnabled(false);

				CalculateNumberOfModels(e);
			}
		});

		jCheckBoxFrequencies
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						CalculateNumberOfModels(e);
					}
				});

		ButtonFixedUserTopologyCalcLike
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ReadUserTopologyCalcLike(e);
					}
				});
		
		ButtonBIONJCalcLike
		.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableTreeSearching(false);
			}
		});
		
		ButtonFixedCalcLike
		.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableTreeSearching(false);
			}
		});
		
		ButtonFixedUserTopologyCalcLike
		.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableTreeSearching(false);
			}
		});
		
		ButtonMLCalcLike
		.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableTreeSearching(true);
			}
		});
		
		/*
		 * ButtonFixedUserTreeCalcLike.addActionListener(new
		 * ActionListener() { public void
		 * actionPerformed(ActionEvent e) {
		 * ReadUserTreeCalcLike(e); } });
		 * 
		 * 
		 * CheckBoxFixedTopology.addActionListener(new
		 * ActionListener() { public void
		 * actionPerformed(ActionEvent e) {
		 * CheckBoxFixedTopologyActionPerformed(e); } });
		 * 
		 * 
		 * CheckBoxOptimizeTopology.addActionListener(new
		 * ActionListener() { public void
		 * actionPerformed(ActionEvent e) {
		 * CheckBoxOptimizeTopologyActionPerformed(e); } });
		 */
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				thisWindowClosing(e);
			}
		});

		// END GENERATED CODE
	}
	
	private void enableTreeSearching(boolean enabled) {
		ButtonNNICalcLike.setEnabled(enabled);
		ButtonSPRCalcLike.setEnabled(enabled);
		ButtonBestCalcLike.setEnabled(enabled);
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
	void thisWindowClosing(WindowEvent e) {
		setVisible(false);
		dispose();
		// System.exit(0);
	}

	public void RunButtonCalcLikeActionPerformed(ActionEvent e) {
		try {
			// set number of processors
			options.setNumberOfThreads(SliderProcessors.getValue());
			
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
			progressFrame = new Frame_Progress(options.numModels, this, modelTest);

			setVisible(false);

			// scroll to the bottom
			XManager.getInstance(modelTest)
					.getPane()
					.setCaretPosition(
							XManager.getInstance(modelTest).getPane().getDocument()
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
			ActionEvent e) {
		try {
			SliderProcessors.setValue(DEFAULT_NUMBER_OF_THREADS);
			Button11SubsTypeCalcLike.setSelected(true);
			ButtonMLCalcLike.setSelected(true);
			enableTreeSearching(ButtonMLCalcLike.isSelected());
			ButtonBestCalcLike.setSelected(true);
			
			jCheckBoxFrequencies.setSelected(true);
			jCheckBoxPinv.setSelected(true);
			jCheckBoxGamma.setSelected(true);
			TextFieldNcat.setEnabled(true);
			TextFieldNcat.setText("4");

			CalculateNumberOfModels(null);
			jLabelUserTopology.setText("");
			jLabelUserTree.setText("");

		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	public void CancelButtonCalcLikeActionPerformed(ActionEvent e) {
		try {
			setVisible(false);
			dispose();
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	public void CalculateNumberOfModels(ActionEvent e) {
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

	private void SliderChangeListener(ChangeEvent e) {
		jLabelNumProcessors.setText(String.valueOf(SliderProcessors.getValue()));
	}
	
	public void ReadUserTopologyCalcLike(ActionEvent e) {
		FileDialog fc = new FileDialog(this,
				"Load Newick tree file with branch lengths", FileDialog.LOAD);
		fc.setDirectory(System.getProperty("user.dir"));
		fc.setVisible(true);

		String treefilename = fc.getFile();

		if (treefilename != null) // menu not canceled
		{
			String treefilenameComplete = fc.getDirectory() + treefilename;
			modelTest.getMainConsole().print(
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
				modelTest.getMainConsole().println(" failed.\n");
				ButtonMLCalcLike.setSelected(true);
				;
			} catch (TreeParseException e1) {
				modelTest.getMainConsole().println(" failed.\n");
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
				modelTest.getMainConsole().println(" OK.");
				System.err.println("OK.");
			}
		} else // file does not exists
		{
			ButtonMLCalcLike.setSelected(true);
			enableTreeSearching(true);
			// ModelTest.mainConsole.println ("\nOpen file canceled\n");
		}
	}

	private class ComputeLikelihoodTask extends SwingWorker {

		private RunPhyml runPhyml;
		
		public ComputeLikelihoodTask() {
			this.runPhyml = new RunPhymlThread(progressFrame, modelTest, modelTest.getCandidateModels());
		}
		
		public Object construct() {
			this.runPhyml.execute();
			return runPhyml;
		}

		public void interrupt() {
			runPhyml.interruptThread();
			super.interrupt();
		}
				
		public RunPhyml getValue() {
			return runPhyml;
		}

	}

}
