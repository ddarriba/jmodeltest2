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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.plaf.BorderUIResource;

import pal.alignment.Alignment;

import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.ModelTestService;
import es.uvigo.darwin.jmodeltest.XManager;
import es.uvigo.darwin.jmodeltest.utilities.BrowserLauncher;
import es.uvigo.darwin.jmodeltest.utilities.InitialFocusSetter;
import es.uvigo.darwin.jmodeltest.utilities.PrintUtilities;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;
import es.uvigo.darwin.prottest.util.exception.AlignmentParseException;
import es.uvigo.darwin.prottest.util.fileio.AlignmentReader;

/* This class sets the main GUI */
public class FrameMain extends JModelTestFrame {

	private static final long serialVersionUID = 201103171450L;

	public static JPanel Panel = new JPanel();
	private JScrollPane scrollPane = new JScrollPane();
	private JTextPane mainEditorPane = new JTextPane();
	private JPanel StatusPanel = new JPanel();
	private JLabel LabelStatusLikelihoods = new JLabel();
	private JLabel LabelStatusData = new JLabel();
	private JMenuBar menuBar = new JMenuBar();
	private JMenu menuFile = new JMenu();
	private JMenuItem menuFileOpenDataFile = new JMenuItem();
	private JSeparator menuFileSeparator1 = new JSeparator();
	private JMenuItem menuFileQuit = new JMenuItem();
	private JMenu menuEdit = new JMenu();
	private JMenuItem menuEditCut = new JMenuItem();
	private JMenuItem menuEditCopy = new JMenuItem();
	private JMenuItem menuEditPaste = new JMenuItem();
	private JMenuItem menuEditSelectAll = new JMenuItem();
	private JMenuItem menuEditClear = new JMenuItem();
	private JSeparator menuEditSeparator1 = new JSeparator();
	private JMenuItem menuEditSaveConsole = new JMenuItem();
	private JMenuItem menuEditPrintConsole = new JMenuItem();
	private JMenu menuAnalysis = new JMenu();
	private JMenuItem menuAnalysisCalculateLikelihoods = new JMenuItem();
	private JSeparator menuAnalysisSeparator1 = new JSeparator();
	private JMenuItem menuAnalysisAIC = new JMenuItem();
	private JMenuItem menuAnalysisBIC = new JMenuItem();
	private JMenuItem menuAnalysisDT = new JMenuItem();
	private JMenuItem menuAnalysishLRT = new JMenuItem();
	private JSeparator menuAnalysisSeparator2 = new JSeparator();
	private JMenuItem menuAnalysisAveraging = new JMenuItem();
	private JMenu menuTools = new JMenu();
	private JMenuItem menuToolsLRT = new JMenuItem();
	private JMenu menuHelp = new JMenu();
	private JMenuItem menuHelpOpen = new JMenuItem();
	private JMenu menuAbout = new JMenu();
	private JMenuItem menuAboutModelTest = new JMenuItem();
	private JMenuItem menuAboutWWW = new JMenuItem();
	private JMenuItem menuAboutCredits = new JMenuItem();
	private JMenu menuResults = new JMenu();
	private JMenuItem menuResultsShowModelTable = new JMenuItem();

	private JCheckBoxMenuItem menuResultsBLasParameters = new JCheckBoxMenuItem();

	// variables to be acessible from local classes
	private JMenuItem menuAIC;
	private JMenuItem menuBIC;
	private JMenuItem menuDT;
	private JMenuItem menuhLRT;
	private JMenuItem menuShowModelTable;
	private JMenuItem menuAveraging;

	public FrameMain() {
		// LabelStatusLike = LabelStatusLikelihoods;

		menuAIC = menuAnalysisAIC;
		menuBIC = menuAnalysisBIC;
		menuDT = menuAnalysisDT;
		menuhLRT = menuAnalysishLRT;
		menuShowModelTable = menuResultsShowModelTable;
		menuAveraging = menuAnalysisAveraging;
	}

	public void initComponents() throws Exception {

		menuBar.setVisible(true);
		menuBar.setBackground(new java.awt.Color(199, 199, 220));
		menuBar.setFont(XManager.FONT_MENU);

		// menu File
		menuFile.setVisible(true);
		menuFile.setText("File");
		menuFile.setBackground(new java.awt.Color(199, 199, 220));
		menuFileOpenDataFile
				.setToolTipText("Load a DNA alignment in sequential or interleaved Phylip format");
		menuFileOpenDataFile
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(3, 3, 6, 3)));
		menuFileOpenDataFile.setVisible(true);
		menuFileOpenDataFile.setText("Load DNA alignment");
		menuFileOpenDataFile.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_O, ActionEvent.META_MASK));

		menuFileSeparator1
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(6, 3, 6, 3)));
		menuFileSeparator1.setVisible(true);
		menuFileQuit.setToolTipText("Quit jModelTest");
		menuFileQuit.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(3, 3, 6, 3)));
		menuFileQuit.setVisible(true);
		menuFileQuit.setText("Quit");
		menuFileQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				ActionEvent.META_MASK));

		// menu Edit
		menuEdit.setVisible(true);
		menuEdit.setText("Edit");
		menuEdit.setBackground(new java.awt.Color(199, 199, 220));
		menuEditCut.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(6, 3, 3, 3)));
		menuEditCut.setVisible(true);
		menuEditCut.setText("Cut");
		menuEditCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.META_MASK));
		menuEditCopy.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(3, 3, 3, 3)));
		menuEditCopy.setVisible(true);
		menuEditCopy.setText("Copy");
		menuEditCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.META_MASK));
		menuEditPaste.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(3, 3, 3, 3)));
		menuEditPaste.setVisible(true);
		menuEditPaste.setText("Paste");
		menuEditPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				ActionEvent.META_MASK));
		menuEditSelectAll.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(3, 3, 3, 3)));
		menuEditSelectAll.setVisible(true);
		menuEditSelectAll.setText("Select All");
		menuEditSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				ActionEvent.META_MASK));
		menuEditClear.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(3, 3, 6, 3)));
		menuEditClear.setVisible(true);
		menuEditClear.setText("Clear");
		menuEditClear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				ActionEvent.META_MASK));
		menuEditSeparator1
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(6, 3, 6, 3)));
		menuEditSeparator1.setVisible(true);
		menuEditSaveConsole
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(6, 3, 6, 3)));
		menuEditSaveConsole.setVisible(true);
		menuEditSaveConsole.setText("Save console");
		menuEditSaveConsole.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_S, ActionEvent.META_MASK));
		menuEditPrintConsole
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(6, 3, 6, 3)));
		menuEditPrintConsole.setVisible(true);
		menuEditPrintConsole.setText("Print console");
		menuEditPrintConsole.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_P, ActionEvent.META_MASK));

		// menu Analysis
		menuAnalysis.setVisible(true);
		menuAnalysis.setText("Analysis");
		menuAnalysis.setBackground(new java.awt.Color(199, 199, 220));

		menuAnalysisCalculateLikelihoods
				.setToolTipText("Compute model likelihoods and parameter estimates using Phyml");
		menuAnalysisCalculateLikelihoods
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(5, 5, 5, 5)));
		menuAnalysisCalculateLikelihoods.setVisible(true);
		menuAnalysisCalculateLikelihoods.setText("Compute likelihood scores");
		menuAnalysisCalculateLikelihoods.setEnabled(false);
		menuAnalysisCalculateLikelihoods.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_L, ActionEvent.META_MASK));
		menuAnalysisSeparator1.setVisible(true);
		menuAnalysisAIC.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuAnalysisAIC.setText("Do AIC calculations ...");
		menuAnalysisAIC.setVisible(true);
		menuAnalysisAIC.setEnabled(false);
		menuAnalysisAIC.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
				ActionEvent.META_MASK));
		menuAnalysisBIC.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuAnalysisBIC.setText("Do BIC calculations ...");
		menuAnalysisBIC.setVisible(true);
		menuAnalysisBIC.setEnabled(false);
		menuAnalysisBIC.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
				ActionEvent.META_MASK));
		menuAnalysisDT.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuAnalysisDT.setText("Do DT calculations ...");
		menuAnalysisDT.setVisible(true);
		menuAnalysisDT.setEnabled(false);
		menuAnalysisDT.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				ActionEvent.META_MASK));
		menuAnalysishLRT.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuAnalysishLRT.setText("Do hLRT calculations ...");
		menuAnalysishLRT
				.setToolTipText("Only available when likelihoods are calculated on the same tree (i.e., models are nested)");
		menuAnalysishLRT.setVisible(true);
		menuAnalysishLRT.setEnabled(false);
		menuAnalysishLRT.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				ActionEvent.META_MASK));

		menuAnalysisSeparator2.setVisible(true);
		menuAnalysisAveraging.setVisible(true);
		menuAnalysisAveraging
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(5, 5, 5, 5)));
		menuAnalysisAveraging.setText("Model-averaged phylogeny");
		menuAnalysisAveraging
				.setToolTipText("Compute a model-averaged phylogeny with the candidate models");
		menuAnalysisAveraging.setEnabled(false);
		menuAnalysisAveraging.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Z, ActionEvent.META_MASK));

		// menu Results
		menuResults.setVisible(true);
		menuResults.setText("Results");
		menuResults.setBackground(new java.awt.Color(199, 199, 220));
		menuResultsBLasParameters
				.setToolTipText("Consider branch lengths as parameters");
		menuResultsBLasParameters
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(3, 3, 6, 3)));
		menuResultsBLasParameters.setEnabled(true);
		menuResultsBLasParameters.setVisible(true);
		menuResultsBLasParameters.setSelected(true);
		menuResultsBLasParameters.setText("Branch lenghts are parameters");
		menuResultsShowModelTable
				.setToolTipText("Show table with model likelihoods and parameter estimates obtained with Phyml");
		menuResultsShowModelTable
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(5, 5, 5, 5)));
		menuResultsShowModelTable.setVisible(true);
		menuResultsShowModelTable.setText("Show results table");
		menuResultsShowModelTable.setEnabled(false);
		menuResultsShowModelTable.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_M, ActionEvent.META_MASK));

		// menu Tools
		menuTools.setVisible(true);
		menuTools.setText("Tools");
		menuTools.setBackground(new java.awt.Color(199, 199, 220));
		menuToolsLRT.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuToolsLRT.setVisible(true);
		menuToolsLRT.setText("LRT calculator");
		menuToolsLRT.setEnabled(true);
		menuToolsLRT.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				ActionEvent.META_MASK));

		// menu Help
		menuHelp.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuHelp.setVisible(true);
		menuHelp.setText("Help");
		menuHelp.setBackground(new java.awt.Color(199, 199, 220));
		menuHelpOpen.setVisible(true);
		menuHelpOpen.setText("Open documentation");
		menuHelpOpen.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuHelpOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K,
				ActionEvent.META_MASK));

		// menu About
		menuAbout.setVisible(true);
		menuAbout.setText("About");
		menuAbout.setBackground(new java.awt.Color(199, 199, 220));
		menuAboutModelTest.setVisible(true);
		menuAboutModelTest.setText("About jModelTest");
		menuAboutWWW.setVisible(true);
		menuAboutWWW.setText("WWW home page");
		menuAboutWWW.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuAboutWWW.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				ActionEvent.META_MASK));
		menuAboutCredits.setVisible(true);
		menuAboutCredits.setText("Credits");
		menuAboutCredits.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuAboutCredits.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J,
				ActionEvent.META_MASK));
		menuAboutModelTest.setVisible(true);
		menuAboutModelTest.setText("jModelTest");
		menuAboutModelTest
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(5, 5, 5, 5)));
		menuAboutModelTest.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
				ActionEvent.META_MASK));

		Panel.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(20, 20, 20, 20)));
		Panel.setLocation(new java.awt.Point(10, -10));
		Panel.setVisible(true);
		Panel.setAutoscrolls(true);
		Panel.setLayout(new BorderLayout());
		Panel.setBackground(null);

		scrollPane.setSize(new java.awt.Dimension(590, 600));
		scrollPane.setLocation(new java.awt.Point(20, 20));
		scrollPane.setVisible(true);
		scrollPane.setAutoscrolls(true);

		mainEditorPane.setMargin(new Insets(5, 5, 5, 5));
		mainEditorPane.setFont(XManager.FONT_CONSOLE);

		mainEditorPane.setBackground(Color.white);
		mainEditorPane.setEditable(false);
		mainEditorPane.setSize(new java.awt.Dimension(15, 10));
		mainEditorPane.setAutoscrolls(true);
		mainEditorPane.setVisible(true);

		StatusPanel.setPreferredSize(new java.awt.Dimension(592, 30));
		StatusPanel.setBorder(new BorderUIResource.EtchedBorderUIResource(1,
				new java.awt.Color(182, 182, 182), new java.awt.Color(89, 89,
						89)));
		StatusPanel.setLocation(new java.awt.Point(20, 630));
		StatusPanel.setVisible(true);
		StatusPanel.setLayout(new BorderLayout());
		StatusPanel.setForeground(java.awt.Color.blue);
		StatusPanel.setBackground(new java.awt.Color(220, 220, 220));
		StatusPanel.setFont(XManager.FONT_STATUS);

		LabelStatusLikelihoods.setSize(new java.awt.Dimension(270, 40));
		LabelStatusLikelihoods.setVisible(true);
		LabelStatusLikelihoods.setText("  Likelihood scores not available");
		LabelStatusLikelihoods
				.setToolTipText("Status of likelihood scores calculations");
		LabelStatusLikelihoods.setForeground(new java.awt.Color(153, 0, 0));
		LabelStatusLikelihoods.setFont(XManager.FONT_LABEL);
		LabelStatusData.setSize(new java.awt.Dimension(150, 40));
		LabelStatusData.setVisible(true);
		LabelStatusData.setText("No data file loaded  ");
		LabelStatusData.setToolTipText("Active current data file");
		LabelStatusData.setForeground(new java.awt.Color(153, 0, 0));
		LabelStatusData.setHorizontalAlignment(JLabel.RIGHT);
		LabelStatusData.setFont(XManager.FONT_LABEL);

		menuBar.add(menuFile);
		menuBar.add(menuEdit);
		menuBar.add(menuAnalysis);
		menuBar.add(menuResults);
		menuBar.add(menuTools);
		menuBar.add(menuHelp);
		menuBar.add(menuAbout);

		menuFile.add(menuFileOpenDataFile);
		menuFile.add(menuFileSeparator1);
		menuFile.add(menuFileQuit);

		menuEdit.add(menuEditCut);
		menuEdit.add(menuEditCopy);
		menuEdit.add(menuEditPaste);
		menuEdit.add(menuEditSelectAll);
		menuEdit.add(menuEditClear);
		menuEdit.add(menuEditSeparator1);
		menuEdit.add(menuEditSaveConsole);
		menuEdit.add(menuEditPrintConsole);

		menuAnalysis.add(menuAnalysisCalculateLikelihoods);
		menuAnalysis.add(menuAnalysisSeparator1);
		menuAnalysis.add(menuAnalysisAIC);
		menuAnalysis.add(menuAnalysisBIC);
		menuAnalysis.add(menuAnalysisDT);
		menuAnalysis.add(menuAnalysishLRT);
		menuAnalysis.add(menuAnalysisSeparator2);
		menuAnalysis.add(menuAnalysisAveraging);

		// menuResults.add(menuResultsBLasParameters);
		menuResults.add(menuResultsShowModelTable);

		menuHelp.add(menuHelpOpen);

		menuAbout.add(menuAboutWWW);
		menuAbout.add(menuAboutCredits);
		menuAbout.add(menuAboutModelTest);

		menuTools.add(menuToolsLRT);

		Panel.add(scrollPane, BorderLayout.CENTER);
		Panel.add(StatusPanel, BorderLayout.PAGE_END);
		scrollPane.getViewport().add(mainEditorPane);
		StatusPanel.add(LabelStatusLikelihoods, BorderLayout.LINE_START);
		StatusPanel.add(LabelStatusData, BorderLayout.LINE_END);

		setLayout(new BorderLayout());
		getContentPane().add(Panel);

		setLocation(new java.awt.Point(281, 80));
		setJMenuBar(menuBar);
		// getContentPane().setLayout(null);
		getContentPane().setLayout(new BorderLayout());
		setTitle("jModelTest " + ModelTest.CURRENT_VERSION);
		setSize(new java.awt.Dimension(630, 695));
		setResizable(true);

		// event handling
		menuFileOpenDataFile
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						menuFileOpenDataFileActionPerformed(e);
					}
				});

		menuFileQuit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				menuFileQuitActionPerformed(e);
			}
		});

		menuEditCut.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				menuEditCutActionPerformed(e);
			}
		});
		menuEditCopy.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				menuEditCopyActionPerformed(e);
			}
		});
		menuEditCut.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				menuEditCutActionPerformed(e);
			}
		});
		menuEditPaste.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				menuEditPasteActionPerformed(e);
			}
		});
		menuEditClear.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				menuEditClearActionPerformed(e);
			}
		});
		menuEditSelectAll
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						menuEditSelectAllActionPerformed(e);
					}
				});
		menuEditSaveConsole
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						menuEditSaveConsoleActionPerformed(e);
					}
				});
		menuEditPrintConsole
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						menuEditPrintConsoleActionPerformed(e);
					}
				});

		menuResultsShowModelTable
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						menuResultsShowModelTableActionPerformed(e);
					}
				});

		menuAnalysisCalculateLikelihoods
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						menuAnalysisCalculateLikelihoodsActionPerformed(e);
					}
				});
		menuAnalysisAIC.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				menuAnalysisAICActionPerformed(e);
			}
		});
		menuAnalysisBIC.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				menuAnalysisBICActionPerformed(e);
			}
		});
		menuAnalysisDT.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				menuAnalysisDTActionPerformed(e);
			}
		});
		menuAnalysishLRT.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				menuAnalysishLRTActionPerformed(e);
			}
		});

		menuAnalysisAveraging
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						menuAnalysisAveragingActionPerformed(e);
					}
				});

		menuToolsLRT.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				menuToolsLRTActionPerformed(e);
			}
		});

		menuHelpOpen.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				menuHelpOpenActionPerformed(e);
			}
		});

		menuAboutWWW.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				menuAboutWWWActionPerformed(e);
			}
		});

		menuAboutCredits.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				menuAboutCreditsActionPerformed(e);
			}
		});

		menuAboutModelTest
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						menuAboutModelTestActionPerformed(e);
					}
				});

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});

		// END GENERATED CODE

		// we need this to have the panel resizing with the frame
		setContentPane(Panel);

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
	private void thisWindowClosing(java.awt.event.WindowEvent e) {
		setVisible(false);
		dispose();
		System.exit(0);
	}

	private void menuFileOpenDataFileActionPerformed(
			java.awt.event.ActionEvent e) {
		FileDialog fc = new FileDialog(this, "Load DNA alignment",
				FileDialog.LOAD);
		fc.setDirectory(System.getProperty("user.dir"));
		fc.setVisible(true);

		String dataFileName = fc.getFile();

		if (dataFileName != null) // menu not canceled
		{
			String dataFileNameComplete = fc.getDirectory() + dataFileName;
			ModelTest.getMainConsole().print(
					"Reading data file \"" + dataFileName + "\"...");

			File inputFile = new File(dataFileNameComplete);
			if (inputFile.exists()) // file exists
			{
				options.setInputFile(inputFile);
				try {
					ModelTestService.readAlignment(inputFile,
							options.getAlignmentFile());

					Alignment alignment = AlignmentReader
							.readAlignment(new PrintWriter(System.err), options
									.getAlignmentFile().getAbsolutePath(), true);
					options.numTaxa = alignment.getSequenceCount();
					options.numSites = alignment.getSiteCount();
					options.numBranches = 2 * options.numTaxa - 3;

					LabelStatusData.setText(dataFileName + "  ");
					LabelStatusData.setForeground(new java.awt.Color(102, 102,
							153));
					menuAnalysisCalculateLikelihoods.setEnabled(true);
					ModelTest.getMainConsole().println(" OK.");
					ModelTest.getMainConsole().println(
							"  number of sequences: " + options.numTaxa);
					ModelTest.getMainConsole().println(
							"  number of sites: " + options.numSites);
					options.sampleSize = options.numSites;
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(this, "The specified file \""
							+ dataFileName
							+ "\" cannot be read as an alignment",
							"jModelTest error", JOptionPane.ERROR_MESSAGE);
					ModelTest.getMainConsole().println(" failed.\n");
				}
			} else {
				JOptionPane.showMessageDialog(this, "The specified file \""
						+ dataFileName + "\" cannot be found",
						"jModelTest error", JOptionPane.ERROR_MESSAGE);
				ModelTest.getMainConsole().println(" failed.\n");
			}
		}
	}

	private void menuFileQuitActionPerformed(java.awt.event.ActionEvent e) {
		try {
			dispose();
			System.exit(0);
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuEditCutActionPerformed(java.awt.event.ActionEvent e) {
		try {
			mainEditorPane.cut();
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuEditCopyActionPerformed(java.awt.event.ActionEvent e) {
		try {
			mainEditorPane.copy();
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuEditPasteActionPerformed(java.awt.event.ActionEvent e) {
		try {
			mainEditorPane.paste();
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuEditSelectAllActionPerformed(java.awt.event.ActionEvent e) {
		try {
			mainEditorPane.selectAll();
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	public void menuEditClearActionPerformed(java.awt.event.ActionEvent e) {
		try {
			mainEditorPane.setText("");
			ModelTest.printHeader(ModelTest.getMainConsole());
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuEditSaveConsoleActionPerformed(java.awt.event.ActionEvent e) {
		FileDialog dialog;
		try {
			try {
				dialog = new FileDialog(this,
						"Open file to save console results", FileDialog.SAVE);
				dialog.setFile(options.getInputFile().getName()
						+ ".jmodeltest.console");
				dialog.setVisible(true);
			} catch (Throwable f) {
				JOptionPane.showMessageDialog(this,
						"It appears your VM does not allow file saving",
						"jModelTest error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (dialog.getFile() != null) /* a file was selected */
			{
				String sname = dialog.getFile();
				File sfile = new File(dialog.getDirectory() + sname);
				FileOutputStream fos = new FileOutputStream(sfile);
				PrintWriter pw = new PrintWriter(fos);
				mainEditorPane.write(pw);
				pw.close();
			}
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuEditPrintConsoleActionPerformed(
			java.awt.event.ActionEvent e) {
		try {
			PrintUtilities.printComponent(mainEditorPane);
			// mainEditorPane.print(mainEditorPane.getGraphics());
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuResultsShowModelTableActionPerformed(
			java.awt.event.ActionEvent e) {
		try {
			XManager.getInstance().resultsFrame.setVisible(true);
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuAnalysisCalculateLikelihoodsActionPerformed(
			java.awt.event.ActionEvent e) {
		try {
			Frame_CalcLike Likeframe = new Frame_CalcLike();
			Likeframe.initComponents();
			InitialFocusSetter.setInitialFocus(Likeframe,
					Likeframe.RunButtonCalcLike);
			Likeframe.setVisible(true);
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuAnalysisAICActionPerformed(java.awt.event.ActionEvent e) {
		try {
			Frame_AIC AICframe = new Frame_AIC();
			AICframe.initComponents();
			AICframe.setVisible(true);
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuAnalysisBICActionPerformed(java.awt.event.ActionEvent e) {
		try {
			Frame_BIC BICframe = new Frame_BIC();
			BICframe.initComponents();
			BICframe.setVisible(true);
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuAnalysisDTActionPerformed(java.awt.event.ActionEvent e) {
		try {
			Frame_DT DTframe = new Frame_DT();
			DTframe.initComponents();
			DTframe.setVisible(true);
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuAnalysishLRTActionPerformed(java.awt.event.ActionEvent e) {
		try {
			Frame_hLRT hLRTframe = new Frame_hLRT();
			hLRTframe.initComponents();
			hLRTframe.setVisible(true);
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuAnalysisAveragingActionPerformed(
			java.awt.event.ActionEvent e) {
		try {
			Frame_Consense consenseFrame = new Frame_Consense();
			consenseFrame.initComponents();
			consenseFrame.setVisible(true);
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuToolsLRTActionPerformed(java.awt.event.ActionEvent e) {
		try {
			Frame_LRTcalculator LRTframe = new Frame_LRTcalculator();
			LRTframe.initComponents();
			LRTframe.setVisible(true);
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuHelpOpenActionPerformed(java.awt.event.ActionEvent e) {
		String docfile = null;
		try {
			if (!Utilities.isWindows())
				docfile = "file://"
						+ Utilities.substituteSpaces(System
								.getProperty("user.dir")) + ModelTest.PDF;
			else
				docfile = "file://" + System.getProperty("user.dir")
						+ ModelTest.PDF;

			BrowserLauncher.openURL(docfile);
		} catch (Exception f) {
			System.err.println("\ntrying to open " + docfile);
			f.printStackTrace();
		}
	}

	private void menuAboutWWWActionPerformed(java.awt.event.ActionEvent e) {
		try {
			BrowserLauncher.openURL(ModelTest.URL);
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuAboutCreditsActionPerformed(java.awt.event.ActionEvent e) {
		try {
			String credits = "-- Likelihood calculations with Phyml by Stephane Guindon\n";
			credits += "-- FileIO with ReadSeq by Don Gilbert\n";
			credits += "-- Consensus tree with the consense program from the \nPhylip package by Joe Felsenstein\n";
			credits += "-- Table utilities by Philip Milne\n";
			credits += "-- BrowserLauncher by Eric Albert\n";

			JOptionPane.showMessageDialog(new JFrame(), credits,
					"jModelTest message", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuAboutModelTestActionPerformed(java.awt.event.ActionEvent e) {
		try {
			String about = "jModelTest " + ModelTest.CURRENT_VERSION + "\n";
			about += "Copyright David Posada 2005 onwards ";
			JOptionPane.showMessageDialog(new JFrame(), about,
					"jModelTest message", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	public JTextPane getMainEditorPane() {
		return mainEditorPane;
	}

	public void setLikeLabelText(String text) {
		LabelStatusLikelihoods.setText(text);
	}

	public void setLikeLabelColor(Color color) {
		LabelStatusLikelihoods.setForeground(color);
	}

	public void setDataLabelText(String text) {
		LabelStatusData.setText(text);
	}

	public void enableMenuAIC(boolean enabled) {
		menuAIC.setEnabled(enabled);
	}

	public void enableMenuBIC(boolean enabled) {
		menuBIC.setEnabled(enabled);
	}

	public void enableMenuDT(boolean enabled) {
		menuDT.setEnabled(enabled);
	}

	public void enableMenuhLRT(boolean enabled) {
		menuhLRT.setEnabled(enabled);
	}

	public void enableMenuAveraging(boolean enabled) {
		menuAveraging.setEnabled(enabled);
	}

	public void enableMenuShowModelTable(boolean enabled) {
		menuShowModelTable.setEnabled(enabled);
	}

	public void selectedMenuResultsBLasParameters(boolean selected) {
		menuResultsBLasParameters.setSelected(selected);
	}
}
