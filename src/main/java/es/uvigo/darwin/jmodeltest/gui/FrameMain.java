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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.BorderUIResource;
import javax.swing.text.DefaultCaret;

import pal.tree.Tree;
import edu.stanford.ejalbert.BrowserLauncher;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.ModelTestConfiguration;
import es.uvigo.darwin.jmodeltest.ModelTestService;
import es.uvigo.darwin.jmodeltest.io.DocumentOutputStream;
import es.uvigo.darwin.jmodeltest.io.HtmlReporter;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.tree.TreeSummary;
import es.uvigo.darwin.jmodeltest.utilities.InitialFocusSetter;
import es.uvigo.darwin.jmodeltest.utilities.PrintUtilities;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;
import es.uvigo.darwin.prottest.util.fileio.AlignmentReader;

/* This class sets the main GUI */
public class FrameMain extends JModelTestFrame {

	private static final int HOTKEY_MODIFIER;
	private static final long serialVersionUID = 201103171450L;
	public static final File LOG_DIR = new File(System.getProperty("user.dir")
			+ File.separator + "log" + File.separator);

	public static JPanel Panel = new JPanel();
	private JTabbedPane tabbedPane = new JTabbedPane();
	private JScrollPane scrollPane = new JScrollPane();
	private JScrollPane phymlScrollPane = new JScrollPane();
	private JTextPane mainEditorPane = new JTextPane();
	private JTextArea phymlEditorPane = new JTextArea();

	private JPanel StatusPanel = new JPanel();
	private JLabel LabelStatusLikelihoods = new JLabel();
	private JLabel LabelStatusData = new JLabel();
	private JMenuBar menuBar = new JMenuBar();
	private JMenu menuFile = new JMenu();
	private JMenuItem menuFileOpenDataFile = new JMenuItem();
	private JMenuItem menuFileOpenCkpFile = new JMenuItem();
	private JSeparator menuFileSeparator1 = new JSeparator();
	private JMenuItem menuFileQuit = new JMenuItem();
	private JMenu menuEdit = new JMenu();
	private JMenuItem menuEditCut = new JMenuItem();
	private JMenuItem menuEditCopy = new JMenuItem();
	private JMenuItem menuEditPaste = new JMenuItem();
	private JMenuItem menuEditSelectAll = new JMenuItem();
	private JMenuItem menuEditClear = new JMenuItem();
	private JMenuItem menuResultsHtmlOutput = new JMenuItem();
	private JSeparator menuEditSeparator1 = new JSeparator();
	private JMenuItem menuEditSaveConsole = new JMenuItem();
	private JMenuItem menuEditPrintConsole = new JMenuItem();
	private JSeparator menuEditSeparator2 = new JSeparator();
	private JMenuItem menuEditPreferences = new JMenuItem();
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
	private JMenuItem menuHelpDiscussionGroup = new JMenuItem();
	private JMenuItem menuAboutCredits = new JMenuItem();
	private JSeparator menuAboutSeparator = new JSeparator();
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

	static {
		if (Utilities.findCurrentOS() == Utilities.OS_OSX) {
			HOTKEY_MODIFIER = ActionEvent.META_MASK;
		} else {
			HOTKEY_MODIFIER = ActionEvent.CTRL_MASK;
		}
	}

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
		menuBar.setBackground(XManager.MENU_COLOR);
		menuBar.setFont(XManager.FONT_MENU);

		// menu File
		menuFile.setVisible(true);
		menuFile.setText("File");
		menuFile.setBackground(XManager.MENU_COLOR);
		menuFileOpenDataFile
				.setToolTipText("Load a DNA alignment in sequential or interleaved Phylip format");
		menuFileOpenDataFile
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(3, 3, 6, 3)));
		menuFileOpenDataFile.setVisible(true);
		menuFileOpenDataFile.setText("Load DNA alignment");
		menuFileOpenDataFile.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_O, HOTKEY_MODIFIER));

		menuFileOpenCkpFile
				.setToolTipText("Load a checkpoint file from a previous run");
		menuFileOpenCkpFile
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(3, 3, 6, 3)));
		menuFileOpenCkpFile.setVisible(true);
		menuFileOpenCkpFile.setEnabled(false);
		menuFileOpenCkpFile.setText("Load checkpoint file");

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
				HOTKEY_MODIFIER));

		// menu Edit
		menuEdit.setVisible(true);
		menuEdit.setText("Edit");
		menuEdit.setBackground(XManager.MENU_COLOR);
		menuEditCut.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(6, 3, 3, 3)));
		menuEditCut.setVisible(true);
		menuEditCut.setText("Cut");
		menuEditCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				HOTKEY_MODIFIER));
		menuEditCopy.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(3, 3, 3, 3)));
		menuEditCopy.setVisible(true);
		menuEditCopy.setText("Copy");
		menuEditCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				HOTKEY_MODIFIER));
		menuEditPaste.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(3, 3, 3, 3)));
		menuEditPaste.setVisible(true);
		menuEditPaste.setText("Paste");
		menuEditPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				HOTKEY_MODIFIER));
		menuEditSelectAll.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(3, 3, 3, 3)));
		menuEditSelectAll.setVisible(true);
		menuEditSelectAll.setText("Select All");
		menuEditSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				HOTKEY_MODIFIER));
		menuEditClear.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(3, 3, 6, 3)));
		menuEditClear.setVisible(true);
		menuEditClear.setText("Clear");
		menuEditClear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				HOTKEY_MODIFIER));
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
				KeyEvent.VK_S, HOTKEY_MODIFIER));
		menuEditPrintConsole
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(6, 3, 6, 3)));
		menuEditPrintConsole.setVisible(true);
		menuEditPrintConsole.setText("Print console");
		menuEditPrintConsole.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_P, HOTKEY_MODIFIER));
		menuEditSeparator2
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(6, 3, 6, 3)));
		menuEditSeparator2.setVisible(true);
		menuEditPreferences
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(6, 3, 3, 3)));
		menuEditPreferences.setVisible(true);
		menuEditPreferences.setText("Preferences");

		// menu Analysis
		menuAnalysis.setVisible(true);
		menuAnalysis.setText("Analysis");
		menuAnalysis.setBackground(XManager.MENU_COLOR);

		menuAnalysisCalculateLikelihoods
				.setToolTipText("Compute model likelihoods and parameter estimates using Phyml");
		menuAnalysisCalculateLikelihoods
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(5, 5, 5, 5)));
		menuAnalysisCalculateLikelihoods.setVisible(true);
		menuAnalysisCalculateLikelihoods.setText("Compute likelihood scores");
		menuAnalysisCalculateLikelihoods.setEnabled(false);
		menuAnalysisCalculateLikelihoods.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_L, HOTKEY_MODIFIER));
		menuAnalysisSeparator1.setVisible(true);
		menuAnalysisAIC.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuAnalysisAIC.setText("Do AIC calculations ...");
		menuAnalysisAIC.setVisible(true);
		menuAnalysisAIC.setEnabled(false);
		menuAnalysisAIC.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
				HOTKEY_MODIFIER));
		menuAnalysisBIC.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuAnalysisBIC.setText("Do BIC calculations ...");
		menuAnalysisBIC.setVisible(true);
		menuAnalysisBIC.setEnabled(false);
		menuAnalysisBIC.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
				HOTKEY_MODIFIER));
		menuAnalysisDT.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuAnalysisDT.setText("Do DT calculations ...");
		menuAnalysisDT.setVisible(true);
		menuAnalysisDT.setEnabled(false);
		menuAnalysisDT.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				HOTKEY_MODIFIER));
		menuAnalysishLRT.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuAnalysishLRT.setText("Do hLRT calculations ...");
		menuAnalysishLRT
				.setToolTipText("Only available when likelihoods are calculated on the same tree (i.e., models are nested)");
		menuAnalysishLRT.setVisible(true);
		menuAnalysishLRT.setEnabled(false);
		menuAnalysishLRT.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				HOTKEY_MODIFIER));

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
				KeyEvent.VK_Z, HOTKEY_MODIFIER));

		// menu Results
		menuResults.setVisible(true);
		menuResults.setText("Results");
		menuResults.setBackground(XManager.MENU_COLOR);
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
				KeyEvent.VK_M, HOTKEY_MODIFIER));
		menuResultsHtmlOutput
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(6, 3, 6, 3)));
		menuResultsHtmlOutput.setVisible(true);
		menuResultsHtmlOutput.setText("Build HTML log");
		menuResultsHtmlOutput.setEnabled(false);
		menuResultsHtmlOutput.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_H, HOTKEY_MODIFIER));

		// menu Tools
		menuTools.setVisible(true);
		menuTools.setText("Tools");
		menuTools.setBackground(XManager.MENU_COLOR);
		menuToolsLRT.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuToolsLRT.setVisible(true);
		menuToolsLRT.setText("LRT calculator");
		menuToolsLRT.setEnabled(true);
		menuToolsLRT.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				HOTKEY_MODIFIER));

		// menu Help
		menuHelp.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuHelp.setVisible(true);
		menuHelp.setText("Help");
		menuHelp.setBackground(XManager.MENU_COLOR);
		menuHelpOpen.setVisible(true);
		menuHelpOpen.setText("Open documentation");
		menuHelpOpen.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuHelpOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K,
				HOTKEY_MODIFIER));

		// menu About
		menuAbout.setVisible(true);
		menuAbout.setText("About");
		menuAbout.setBackground(XManager.MENU_COLOR);
		menuAboutWWW.setVisible(true);
		menuAboutWWW.setText("WWW home page");
		menuAboutWWW.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuAboutWWW.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				HOTKEY_MODIFIER));
		menuHelpDiscussionGroup.setVisible(true);
		menuHelpDiscussionGroup.setText("Discussion group");
		menuHelpDiscussionGroup
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(5, 5, 5, 5)));
		menuHelpDiscussionGroup.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_G, HOTKEY_MODIFIER));
		menuAboutCredits.setVisible(true);
		menuAboutCredits.setText("Credits");
		menuAboutCredits.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(5, 5, 5, 5)));
		menuAboutCredits.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J,
				HOTKEY_MODIFIER));
		menuAboutSeparator.setVisible(true);
		menuAboutModelTest.setVisible(true);
		menuAboutModelTest.setText("jModelTest");
		menuAboutModelTest
				.setBorder(new BorderUIResource.EmptyBorderUIResource(
						new java.awt.Insets(5, 5, 5, 5)));
		menuAboutModelTest.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
				HOTKEY_MODIFIER));

		Panel.setBorder(new BorderUIResource.EmptyBorderUIResource(
				new java.awt.Insets(20, 20, 20, 20)));
		Panel.setLocation(new java.awt.Point(10, -10));
		Panel.setVisible(true);
		Panel.setAutoscrolls(true);
		Panel.setLayout(new BorderLayout());
		Panel.setBackground(null);

		tabbedPane.setSize(590, 610);
		tabbedPane.setLocation(20, 10);
		tabbedPane.setVisible(true);

		scrollPane.setVisible(true);
		scrollPane.setAutoscrolls(true);

		phymlScrollPane.setVisible(true);
		phymlScrollPane.setAutoscrolls(true);
		DefaultCaret caret = (DefaultCaret) phymlEditorPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		mainEditorPane.setMargin(new Insets(5, 5, 5, 5));
		mainEditorPane.setFont(XManager.FONT_CONSOLE);

		mainEditorPane.setBackground(XManager.PANE_BACK_COLOR);
		mainEditorPane.setEditable(false);
		mainEditorPane.setSize(15, 10);
		mainEditorPane.setAutoscrolls(true);
		mainEditorPane.setVisible(true);

		phymlEditorPane.setMargin(new Insets(5, 5, 5, 5));
		phymlEditorPane.setFont(XManager.FONT_CONSOLE);

		phymlEditorPane.setBackground(XManager.PANE_BACK_COLOR);
		phymlEditorPane.setEditable(false);
		phymlEditorPane.setSize(15, 10);
		phymlEditorPane.setAutoscrolls(true);
		phymlEditorPane.setVisible(true);
		ModelTest.setPhymlConsole(new TextOutputStream(new PrintStream(
				new DocumentOutputStream(phymlEditorPane.getDocument()))));

		StatusPanel.setPreferredSize(new java.awt.Dimension(592, 30));
		StatusPanel.setBorder(new BorderUIResource.EtchedBorderUIResource(1,
				XManager.INNER_BORDER_COLOR, XManager.OUTER_BORDER_COLOR));
		StatusPanel.setLocation(new java.awt.Point(20, 630));
		StatusPanel.setVisible(true);
		StatusPanel.setLayout(new BorderLayout());
		StatusPanel.setForeground(java.awt.Color.blue);
		StatusPanel.setBackground(XManager.STATUS_BACK_COLOR);
		StatusPanel.setFont(XManager.FONT_STATUS);

		LabelStatusLikelihoods.setSize(new java.awt.Dimension(270, 40));
		LabelStatusLikelihoods.setVisible(true);
		LabelStatusLikelihoods.setText("  Likelihood scores not available");
		LabelStatusLikelihoods
				.setToolTipText("Status of likelihood scores calculations");
		LabelStatusLikelihoods.setForeground(XManager.LABEL_FAIL_COLOR);
		LabelStatusLikelihoods.setFont(XManager.FONT_LABEL);
		LabelStatusData.setSize(new java.awt.Dimension(150, 40));
		LabelStatusData.setVisible(true);
		LabelStatusData.setText("No data file loaded  ");
		LabelStatusData.setToolTipText("Active current data file");
		LabelStatusData.setForeground(XManager.LABEL_FAIL_COLOR);
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
		menuFile.add(menuFileOpenCkpFile);
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
		menuEdit.add(menuEditSeparator2);
		menuEdit.add(menuEditPreferences);

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
		menuResults.add(menuResultsHtmlOutput);

		menuHelp.add(menuHelpOpen);
		menuHelp.add(menuHelpDiscussionGroup);

		menuAbout.add(menuAboutWWW);
		menuAbout.add(menuAboutCredits);
		menuAbout.add(menuAboutSeparator);
		menuAbout.add(menuAboutModelTest);

		menuTools.add(menuToolsLRT);

		tabbedPane.addTab("Main", scrollPane);
		tabbedPane.addTab("PhyML-log", phymlScrollPane);
		Panel.add(tabbedPane, BorderLayout.CENTER);
		Panel.add(StatusPanel, BorderLayout.PAGE_END);
		scrollPane.getViewport().add(mainEditorPane);
		phymlScrollPane.getViewport().add(phymlEditorPane);
		StatusPanel.add(LabelStatusLikelihoods, BorderLayout.LINE_START);
		StatusPanel.add(LabelStatusData, BorderLayout.LINE_END);

		setLayout(new BorderLayout());
		getContentPane().add(Panel);

		setLocation(XManager.MAIN_LOCATION);
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

		menuFileOpenCkpFile
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						menuFileOpenCkpFileActionPerformed(e);
					}
				});

		menuFileQuit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				menuFileQuitActionPerformed(e);
			}
		});

		menuEditPreferences
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						menuEditPreferencesActionPerformed(e);
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

		menuResultsHtmlOutput
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						menuResultsHtmlOutputActionPerformed(e);
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

		menuHelpDiscussionGroup
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						menuAboutDiscussionGroupActionPerformed(e);
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

		JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
		fc.setFileFilter(new FileNameExtensionFilter("Sequence alignment (*.phy, *.fas, *.nex)", "phy", "fas", "nex"));
		
		int returnVal = fc.showOpenDialog(this);

		File inputFile = null;
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	inputFile = fc.getSelectedFile();
        }   

		if (inputFile != null) // menu not canceled
		{
			ModelTest.getMainConsole().print(
					"Reading data file \"" + inputFile.getName() + "\"...");

			if (inputFile.exists()) // file exists
			{
				options.setInputFile(inputFile);
				try {
					ModelTestService.readAlignment(inputFile,
							options.getAlignmentFile());

					options.setAlignment(AlignmentReader
							.readAlignment(new PrintWriter(System.err), options
									.getAlignmentFile().getAbsolutePath(), true));

					LabelStatusData.setText(inputFile.getName() + "  ");
					LabelStatusData.setForeground(new java.awt.Color(102, 102,
							153));
					menuAnalysisCalculateLikelihoods.setEnabled(true);
					enableMenuShowModelTable(false);
					enableMenuHtmlOutput(false);
					ModelTest.getMainConsole().println(" OK.");
					ModelTest.getMainConsole().println(
							"  number of sequences: " + options.getNumTaxa());
					ModelTest.getMainConsole().println(
							"  number of sites: " + options.getNumSites());
					menuFileOpenCkpFile.setEnabled(true);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(this, "The specified file \""
							+ inputFile.getAbsolutePath()
							+ "\" cannot be read as an alignment",
							"jModelTest error", JOptionPane.ERROR_MESSAGE);
					ModelTest.getMainConsole().println(" failed.\n");
					menuFileOpenCkpFile.setEnabled(false);
				}
			} else {
				JOptionPane.showMessageDialog(this, "The specified file \""
						+ inputFile.getAbsolutePath() + "\" cannot be found",
						"jModelTest error", JOptionPane.ERROR_MESSAGE);
				ModelTest.getMainConsole().println(" failed.\n");
				menuFileOpenCkpFile.setEnabled(false);
			}
		}
		
	}

	private void menuFileOpenCkpFileActionPerformed(java.awt.event.ActionEvent e) {
		
		JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
		fc.setFileFilter(new FileNameExtensionFilter("jModelTest checkpoint (*.ckp)", "ckp"));
		
		int returnVal = fc.showOpenDialog(this);

		File file = null;
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        }        

		if (file != null) // menu not canceled
		{
			ModelTest.getMainConsole().print(
					"Loading checkpoint from \"" + file.getName() + "\"...");

			if (file.exists()) // file exists
			{
				ModelTest.loadCheckpoint(file);
			} else {
				JOptionPane.showMessageDialog(this, "The specified file \""
						+ file.getName() + "\" cannot be found",
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

	private void menuEditPreferencesActionPerformed(java.awt.event.ActionEvent e) {
		try {
			XManager.getInstance().loadFramePreferences();
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
			ModelTest.printCitation(ModelTest.getMainConsole());
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

	private void menuResultsHtmlOutputActionPerformed(
			java.awt.event.ActionEvent e) {

		FileDialog dialog;
		try {
			try {
				dialog = new FileDialog(this, "Open file to save HTML log",
						FileDialog.SAVE);
				dialog.setFile(options.getInputFile().getName()
						+ ".jmodeltest.html");
				dialog.setDirectory("log");
				dialog.setVisible(true);
			} catch (Throwable f) {
				JOptionPane.showMessageDialog(this,
						"It appears your VM does not allow file saving",
						"jModelTest error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (!dialog.getDirectory().equals(
					LOG_DIR.getAbsolutePath() + File.separator)) {
				Utilities
						.printRed("\nWarning: If you save the log files out from the log directory, make sure to copy the \"log"
								+ File.separator
								+ "resources\" folder with the log file\n");
			}

			if (dialog.getFile() != null
					&& ModelTestConfiguration.isHtmlLogEnabled()) /*
																 * a file was
																 * selected
																 */
			{
				Tree bestAIC = ModelTest.getMinAIC() != null ? ModelTest
						.getMinAIC().getTree() : null;
				Tree bestAICc = ModelTest.getMinAICc() != null ? ModelTest
						.getMinAICc().getTree() : null;
				Tree bestBIC = ModelTest.getMinBIC() != null ? ModelTest
						.getMinBIC().getTree() : null;
				Tree bestDT = ModelTest.getMinDT() != null ? ModelTest
						.getMinDT().getTree() : null;

				TreeSummary treeSummary = new TreeSummary(bestAIC, bestAICc,
						bestBIC, bestDT, ModelTest.getCandidateModels());

				HtmlReporter.buildReport(options,
						ModelTest.getCandidateModels(),
						new File(dialog.getDirectory() + dialog.getFile()),
						treeSummary);
			}
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
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				URI wikiURI = new URI(ModelTest.WIKI);
				desktop.browse(wikiURI);
			} else {
				BrowserLauncher launcher = new BrowserLauncher();
				launcher.openURLinBrowser(ModelTest.WIKI);
			}
		} catch (Exception f) {
			JOptionPane.showMessageDialog(new JFrame(), f.getMessage(),
					"Error loading webpage", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void menuAboutDiscussionGroupActionPerformed(
			java.awt.event.ActionEvent e) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				URI groupURI = new URI(ModelTest.DISCUSSION_GROUP);
				desktop.browse(groupURI);
			} else {
				BrowserLauncher launcher = new BrowserLauncher();
				launcher.openURLinBrowser(ModelTest.DISCUSSION_GROUP);
			}
		} catch (Exception f) {
			JOptionPane.showMessageDialog(new JFrame(), f.getMessage(),
					"Error loading webpage", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void menuAboutWWWActionPerformed(java.awt.event.ActionEvent e) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				URI jModelTestURI = new URI(ModelTest.URL);
				desktop.browse(jModelTestURI);
			} else {
				BrowserLauncher launcher = new BrowserLauncher();
				launcher.openURLinBrowser(ModelTest.URL);
			}
		} catch (Exception f) {
			JOptionPane.showMessageDialog(new JFrame(), f.getMessage(),
					"Error loading webpage", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void menuAboutCreditsActionPerformed(java.awt.event.ActionEvent e) {
		try {
			String credits = "Likelihood calculations with Phyml by Stephane Guindon et al.\n";
			credits += "Alignment conversion with ALTER by Daniel Glez-Peña et al.\n";
			credits += "Phylogenetic trees management with PAL: Phylogenetic Analysis Library by A. Drummond and K. Strimmer\n";
			credits += "Table utilities by Philip Milne\n";
			credits += "BrowserLauncher by Eric Albert and Jeff Chapman\n";

			JOptionPane.showMessageDialog(new JFrame(), credits,
					"jModelTest - CREDITS", JOptionPane.INFORMATION_MESSAGE,
					XManager.makeIcon("JMT48", "JMT2"));
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void menuAboutModelTestActionPerformed(java.awt.event.ActionEvent e) {
		try {
			String about = "jModelTest " + ModelTest.CURRENT_VERSION + "\n";
			about += "(c) 2011-onwards D.Darriba, G.L.Taboada, R.Doallo and D.Posada\n";
			about += "Department of Biochemistry, Genetics and Immunology\n";
			about += "University of Vigo, 36310 Vigo, Spain.\n";
			about += "Department of Electronics and Systems\n";
			about += "University of A Coruna, 15071 A Coruna, Spain.\n";
			about += "e-mail: ddarriba@udc.es, dposada@uvigo.es\n\n";
			about += "Citation: Darriba D, Taboada GL, Doallo R and Posada D. 2012.\n"
					+ "\"jModelTest 2: more models, new heuristics and parallel computing\".\n"
					+ "Nature Methods 9(8), 772.\n";
			JOptionPane.showMessageDialog(new JFrame(), about, "jModelTest",
					JOptionPane.INFORMATION_MESSAGE,
					XManager.makeIcon("JMT48", "JMT2"));
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

	public void enableMenuHtmlOutput(boolean enabled) {
		menuResultsHtmlOutput.setEnabled(enabled);
	}

	public void selectedMenuResultsBLasParameters(boolean selected) {
		menuResultsBLasParameters.setSelected(selected);
	}
}
