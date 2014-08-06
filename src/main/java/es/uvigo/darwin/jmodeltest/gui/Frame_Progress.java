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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.ModelTestConfiguration;
import es.uvigo.darwin.jmodeltest.exe.ProcessManager;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;
import es.uvigo.darwin.prottest.exe.ExternalExecutionManager;

public class Frame_Progress extends JModelTestFrame implements Observer,
		ActionListener {

	private static final long serialVersionUID = 201102181036L;
	private static final int LABEL_HEIGHT = 20;
	private static final int THREAD_BAR_HEIGHT = 20;
	private static final int PROGRESS_BAR_HEIGHT = 30;
	private static final int WINDOW_WIDTH = 400;
	private static final int H_MARGIN = 20;
	private static final int V_MARGIN = 10;
	private static final int V_INNER_MARGIN = 5;
	private static final int SECTION_WIDTH = WINDOW_WIDTH - 2 * H_MARGIN;
	private static final int HEADER_HEIGHT = LABEL_HEIGHT + 2 * V_INNER_MARGIN;
	private static int FOOTER_HEIGHT = 2 * PROGRESS_BAR_HEIGHT + 3
			* V_INNER_MARGIN;
	private static final int THREADS_SECTIONS_VLOC = HEADER_HEIGHT + 2
			* V_MARGIN;
	private static final int HEIGHT_PER_THREAD = THREAD_BAR_HEIGHT
			+ LABEL_HEIGHT + V_INNER_MARGIN;
	private static final int BUTTON_WIDTH = 100;
	private static final int THREAD_LABEL_WIDTH = 80;

	private static final String NO_MODEL = "idle";
	private static final long lockTime = 3000;
	
	private static final Color[] THREAD_COLOR = { new Color(177, 68, 68),
			new Color(177, 68, 161), new Color(126, 68, 177),
			new Color(68, 97, 177), new Color(68, 177, 162),
			new Color(68, 177, 63), new Color(161, 177, 68),
			new Color(177, 141, 48), new Color(177, 73, 68) };

	private TextOutputStream stream;

	private int numberOfThreads;

	private JPanel headerPanel = new JPanel();
	private JPanel threadsActivityPanel = new JPanel();
	private JPanel footerPanel = new JPanel();

	private JProgressBar threadProgressBar[];
	private JLabel threadProgressLabel[];
	private JLabel threadProgressModelLabel[];
	private JButton progressBarCancelButton = new JButton();
	private JProgressBar progressBarLike = new JProgressBar();
	private JLabel progressBarLikeLabel = new JLabel();
	private JLabel timerLabel = new JLabel();
	private int maxNumOfModelsPerStage[] = new int[] { 1, 15, 10, 6, 3, 1 };
	private JLabel footerStageLabel = new JLabel();

	/** Timer for calculate the elapsed time **/
	private volatile long startTime;
	private volatile long lockTimer = 0;
	
	private Frame_CalcLike frameCalcLike;
	private Timer timer;

	private int completedModels = 0;
	private int totalModels;
	private int currentStage = 1;

	private boolean interrupted;

	private int maximum;

	public Frame_Progress(int numModels, Frame_CalcLike frameCalcLike,
			ApplicationOptions options) {
		this.numberOfThreads = options.getNumberOfThreads();
		this.totalModels = ModelTest.getCandidateModels().length;
		this.interrupted = false;
		this.options = options;
		this.startTime = System.currentTimeMillis();
		this.stream = ModelTest.getMainConsole();
		this.threadProgressBar = new JProgressBar[numberOfThreads];
		this.threadProgressLabel = new JLabel[numberOfThreads];
		this.threadProgressModelLabel = new JLabel[numberOfThreads];
		this.maximum = numModels;
		this.frameCalcLike = frameCalcLike;

		initComponents();
		setVisible(true);

		timer = new Timer(1000, this);
		timer.setRepeats(true);
		timer.start();
	}

	public void initComponents() {

		/* TOP LABEL */
		
		headerPanel.setSize(SECTION_WIDTH, HEADER_HEIGHT);
		headerPanel.setLocation(H_MARGIN, V_MARGIN);
		headerPanel.setBorder(new BorderUIResource.LineBorderUIResource(
				XManager.PANEL_BORDER_COLOR));
		headerPanel.setLayout(null);
		headerPanel.setVisible(true);

		progressBarLikeLabel.setSize(120, LABEL_HEIGHT);
		progressBarLikeLabel.setLocation(H_MARGIN, V_INNER_MARGIN);
		progressBarLikeLabel.setVisible(true);
		progressBarLikeLabel.setFont(XManager.FONT_CONSOLE);
		progressBarLikeLabel.setText("Completed 0/" + totalModels);

		timerLabel.setSize(180, LABEL_HEIGHT);
		timerLabel.setLocation(160, V_INNER_MARGIN);
		timerLabel.setVisible(true);
		timerLabel.setFont(XManager.FONT_CONSOLE);
		timerLabel.setAlignmentX(RIGHT_ALIGNMENT);

		/* THREAD SECTION */

		int threadsPanelHeight = numberOfThreads * HEIGHT_PER_THREAD + 4
				* V_INNER_MARGIN;
		threadsActivityPanel.setSize(SECTION_WIDTH, threadsPanelHeight);
		threadsActivityPanel.setLocation(H_MARGIN, THREADS_SECTIONS_VLOC);
		threadsActivityPanel
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(XManager.PANEL_BORDER_COLOR, 1, false),
						"Thread activity", 4, 2, XManager.FONT_LABEL,
						XManager.LABEL_BLUE_COLOR));
		threadsActivityPanel.setLayout(null);
		threadsActivityPanel.setVisible(true);
		int colorStep = Math.max(THREAD_COLOR.length / numberOfThreads, 1);
		for (int i = 0; i < numberOfThreads; i++) {
			threadProgressLabel[i] = new JLabel();
			threadProgressLabel[i].setSize(THREAD_LABEL_WIDTH, LABEL_HEIGHT);
			threadProgressLabel[i].setLocation(H_MARGIN, LABEL_HEIGHT + (i)
					* HEIGHT_PER_THREAD + V_INNER_MARGIN);
			threadProgressLabel[i].setVisible(true);
			threadProgressLabel[i].setFont(XManager.FONT_CONSOLE);
			threadProgressLabel[i].setText(" Thread " + i + ":");
			threadProgressLabel[i].setOpaque(true);
			threadProgressLabel[i].setBackground(THREAD_COLOR[(i * colorStep)
					% THREAD_COLOR.length]);
			threadProgressLabel[i].setForeground(Color.WHITE);
			threadProgressModelLabel[i] = new JLabel();
			threadProgressModelLabel[i].setSize(SECTION_WIDTH
					- (THREAD_LABEL_WIDTH + 3 * H_MARGIN), LABEL_HEIGHT);
			threadProgressModelLabel[i].setLocation(THREAD_LABEL_WIDTH + 2
					* H_MARGIN, (i) * HEIGHT_PER_THREAD + 2 * V_INNER_MARGIN);
			threadProgressModelLabel[i].setVisible(true);
			threadProgressModelLabel[i].setFont(XManager.FONT_CONSOLE);
			threadProgressModelLabel[i].setText(NO_MODEL);
			threadProgressModelLabel[i].setOpaque(false);
			threadProgressModelLabel[i]
					.setForeground(XManager.LABEL_FAIL_COLOR);
			threadProgressBar[i] = new JProgressBar();
			threadProgressBar[i].setSize(SECTION_WIDTH
					- (THREAD_LABEL_WIDTH + 3 * H_MARGIN), THREAD_BAR_HEIGHT);
			threadProgressBar[i].setStringPainted(false);
			threadProgressBar[i].setIndeterminate(false);
			threadProgressBar[i]
					.setLocation(THREAD_LABEL_WIDTH + 2 * H_MARGIN,
							LABEL_HEIGHT + (i) * HEIGHT_PER_THREAD + 2
									* V_INNER_MARGIN);
			threadProgressBar[i].setVisible(true);
		}

		/* BOTTOM SECTION */
		
		int FOOTER_SECTION_VLOC = THREADS_SECTIONS_VLOC + threadsPanelHeight
				+ V_MARGIN;
		footerPanel.setSize(SECTION_WIDTH,
				FOOTER_HEIGHT + (options.isClusteringSearch() ? 30 : 0));
		footerPanel.setLocation(H_MARGIN, FOOTER_SECTION_VLOC);
		footerPanel.setBorder(new BorderUIResource.LineBorderUIResource(
				XManager.PANEL_BORDER_COLOR));
		footerPanel.setLayout(null);
		footerPanel.setVisible(true);

		if (options.isClusteringSearch()) {
			footerStageLabel.setSize(80, 20);
			footerStageLabel.setLocation(H_MARGIN, V_INNER_MARGIN);
			footerStageLabel.setVisible(true);
			footerStageLabel.setText("Step 1/6");
			footerPanel.add(footerStageLabel);
		}

		int progressBarVPosition = V_INNER_MARGIN;
		if (options.isClusteringSearch()) {
			int scaleFactor = (options.doI ? 2 : 1) * (options.doG ? 2 : 1)
					* (options.doF ? 2 : 1);
			for (int i = 0; i < 6; i++) {
				this.maxNumOfModelsPerStage[i] *= scaleFactor;
			}
			progressBarVPosition += 30;
			FOOTER_HEIGHT += 30;
		}

		int initialMaxValue = options.isClusteringSearch() ? maxNumOfModelsPerStage[0]
				: maximum;
		progressBarLike.setMaximum(initialMaxValue);
		progressBarLike.setValue(0);
		progressBarLike.setStringPainted(true);
		progressBarLike.setString(null);

		progressBarLike.setSize(SECTION_WIDTH - 2 * H_MARGIN,
				PROGRESS_BAR_HEIGHT);
		progressBarLike.setLocation(H_MARGIN, progressBarVPosition);
		progressBarLike.setVisible(true);

		progressBarCancelButton.setVisible(true);
		progressBarCancelButton.setSize(BUTTON_WIDTH, PROGRESS_BAR_HEIGHT);
		progressBarCancelButton.setText("Cancel");
		progressBarCancelButton.setLocation((SECTION_WIDTH - BUTTON_WIDTH) / 2,
				PROGRESS_BAR_HEIGHT + V_INNER_MARGIN + progressBarVPosition);

		/* MAIN WINDOW */

		setLocation(281, 80);
		getContentPane().setLayout(null);
		setTitle("Progress");

		for (JProgressBar progressBar : threadProgressBar) {
			threadsActivityPanel.add(progressBar);
		}

		for (JLabel progressLabel : threadProgressLabel) {
			threadsActivityPanel.add(progressLabel);
		}

		for (JLabel progressModelLabel : threadProgressModelLabel) {
			threadsActivityPanel.add(progressModelLabel);
		}

		headerPanel.add(progressBarLikeLabel);
		headerPanel.add(timerLabel);
		getContentPane().add(headerPanel);
		getContentPane().add(threadsActivityPanel);
		footerPanel.add(progressBarLike);
		footerPanel.add(progressBarCancelButton);
		getContentPane().add(footerPanel);

		setSize(WINDOW_WIDTH, FOOTER_SECTION_VLOC + FOOTER_HEIGHT + 4
				* V_MARGIN);
		setResizable(false);

		/* event handling */
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				thisWindowClosing(e);
			}
		});

		progressBarCancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				progressBarCancel(e);
			}
		});
	}

	private boolean mShown = false;

	public void addNotify() {
		super.addNotify();

		if (mShown)
			return;

		/* resize frame to account for menubar */
		JMenuBar jMenuBar = getJMenuBar();
		if (jMenuBar != null) {
			int jMenuBarHeight = jMenuBar.getPreferredSize().height;
			Dimension dimension = getSize();
			dimension.height += jMenuBarHeight;
			setSize(dimension);

			/* move down components in layered pane */
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

	/* close the window when the close box is clicked */
	private void thisWindowClosing(WindowEvent e) {
		setVisible(false);
		dispose();
	}

	private void progressBarCancel(ActionEvent e) {
		this.setEnabled(false);
		try {
			/* show confirm dialog */
			Object[] options = {"Yes","No"};
			int option;
			option = JOptionPane.showOptionDialog(null, "Are you sure you want to cancel?", 
					"Cancel Execution", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
	        if (option == JOptionPane.YES_OPTION) {
	        	/* cancel execution */
				frameCalcLike.getRunPhyml().interruptThread();
				frameCalcLike.cancelTask();
				setVisible(false);
	        }
		} catch (Exception f) {
			f.printStackTrace();
		}
		this.setEnabled(true);
	}

	@Override
	public synchronized void update(Observable o, Object arg) {
		
		if (Math.abs(System.currentTimeMillis() - lockTimer) < lockTime) return;
		
		if (arg != null) {
			ProgressInfo info = (ProgressInfo) arg;

			switch (info.getType()) {

			case ProgressInfo.BASE_TREE_INIT:
				stream.print("\nEstimating a BIONJ-JC tree ... ");
				System.out.print("estimating a BIONJ-JC tree ... ");
				threadProgressModelLabel[0]
						.setText("Computing BIONJ tree for JC");
				threadProgressModelLabel[0]
						.setForeground(XManager.LABEL_GREEN_COLOR);
				threadProgressBar[0].setIndeterminate(true);
				break;

			case ProgressInfo.BASE_TREE_COMPUTED:
				stream.println("OK");
				System.out.println("OK");
				threadProgressModelLabel[0].setText(NO_MODEL);
				threadProgressModelLabel[0]
						.setForeground(XManager.LABEL_FAIL_COLOR);
				threadProgressBar[0].setIndeterminate(false);
				stream.print(info.getModel().getName() + " tree: "
						+ info.getModel().getTreeString() + "\n");
				break;

			case ProgressInfo.GTR_OPTIMIZATION_INIT:
				stream.println("[Heuristic search] Optimizing "
						+ info.getModel().getName() + " model");
				threadProgressModelLabel[0].setText("Computing "
						+ info.getModel().getName()
						+ " model for heuristic search");
				threadProgressModelLabel[0]
						.setForeground(XManager.LABEL_GREEN_COLOR);
				threadProgressBar[0].setIndeterminate(true);
				break;

			case ProgressInfo.GTR_OPTIMIZATION_COMPLETED:
				stream.println("[Heuristic search] OK");
				threadProgressModelLabel[0].setText(NO_MODEL);
				threadProgressModelLabel[0]
						.setForeground(XManager.LABEL_FAIL_COLOR);
				threadProgressBar[0].setIndeterminate(false);
				break;

			case ProgressInfo.SINGLE_OPTIMIZATION_INIT:
				for (int i = 0; i < numberOfThreads; i++) {
					JLabel progressLabel = threadProgressModelLabel[i];
					if (progressLabel.getText().equals(NO_MODEL)) {
						progressLabel.setText("Computing "
								+ info.getModel().getName() + "...");
						progressLabel.setForeground(XManager.LABEL_GREEN_COLOR);
						threadProgressBar[i].setIndeterminate(true);
						break;
					}
				}
				break;

			case ProgressInfo.OPTIMIZATION_INIT:
				stream.println(" ");
				stream.println("::Progress::");
				stream.println(" ");
				stream.println("Model \t\t Exec. Time \t Total Time \t -lnL");
				stream.println("-------------------------------------------------------------------------");

				ModelTest.setMyAIC(null);
				ModelTest.setMyAICc(null);
				ModelTest.setMyBIC(null);
				ModelTest.setMyDT(null);

				break;

			case ProgressInfo.SINGLE_OPTIMIZATION_COMPLETED:
				this.completedModels++;
				try {
					for (int i = 0; i < numberOfThreads; i++) {
						JLabel progressLabel = threadProgressModelLabel[i];
						if (progressLabel.getText().equals(
								"Computing " + info.getModel().getName()
										+ "...")) {
							progressLabel.setText(NO_MODEL);
							progressLabel
									.setForeground(XManager.LABEL_FAIL_COLOR);
							threadProgressBar[i].setIndeterminate(false);
							break;
						}
					}
					if (options.isClusteringSearch()) {
						if (currentStage != info.getHeuristicStage()) {
							currentStage = info.getHeuristicStage();
							completedModels = 0;
							footerStageLabel.setText("Step " + currentStage
									+ "/6");
							int newMaximum = maxNumOfModelsPerStage[currentStage - 1];
							progressBarLike.setMaximum(newMaximum);
						}
						progressBarLikeLabel.setText("[" + currentStage + "/6]"
								+ completedModels + "/"
								+ info.getNumModelsInStage());
					} else {
						/* update total number of models */
						totalModels = options.getNumModels();
						progressBarLikeLabel.setText("Completed "
								+ completedModels + "/" + totalModels);
					}

					if (ModelTestConfiguration.isCkpEnabled()) {
						try {
							OutputStream file = new FileOutputStream(
									options.getCkpFile());
							OutputStream buffer = new BufferedOutputStream(file);
							ObjectOutput output = new ObjectOutputStream(buffer);
							output.writeObject(ModelTest.getCandidateModels());
							output.close();
						} catch (IOException ex) {
							System.err.println("Cannot perform output.");
						}
					}
					
					progressBarLike.setValue(completedModels);
				} catch (NullPointerException e) {
					/* Ignore... */
				}

				String modelTab = (info.getModel().getName().length() > 10) ? "\t"
						: "\t\t";
				stream.println(info.getModel().getName()
						+ modelTab
						+ info.getMessage()
						+ "\t"
						+ Utilities.calculateRuntime(startTime,
								System.currentTimeMillis())
						+ "\t"
						+ String.format(Locale.ENGLISH, "%5.4f", info
								.getModel().getLnL()));

				// scroll to the bottom
				XManager.getInstance()
						.getPane()
						.setCaretPosition(
								XManager.getInstance().getPane().getDocument()
										.getLength());
				break;

			case ProgressInfo.INTERRUPTED:
				lockTimer = System.currentTimeMillis();
				if (!interrupted) {
					interrupted = true;
					stream.println(" ");

					XManager.getInstance().setLikeLabelColor(
							XManager.LABEL_FAIL_COLOR);

					System.err
							.println("\nComputation of likelihood scores discontinued...");
					Utilities
							.printRed("\nComputation of likelihood scores interrupted. It took "
									+ Utilities.calculateRuntime(startTime,
											System.currentTimeMillis()) + ".\n");
					stream.println(" ");
					XManager.getInstance()
							.getPane()
							.setCaretPosition(
									XManager.getInstance().getPane()
											.getDocument().getLength());
					ProcessManager.getInstance().killAll();
				}
				break;

			case ProgressInfo.ERROR:
				lockTimer = System.currentTimeMillis();
				progressBarCancel(null);
				Utilities.printRed(info.getMessage());
				JOptionPane.showMessageDialog(new JFrame(), info.getMessage(),
						"jModeltest error", JOptionPane.ERROR_MESSAGE);
				break;
			case ProgressInfo.ERROR_BINARY_NOEXECUTE:
				lockTimer = System.currentTimeMillis();
				progressBarCancel(null);
				Utilities.printRed("\n********************\n");
				Utilities.printRed("PhyML binary does not have execution permission: " + info.getMessage());
				Utilities.printRed("\n********************\n");
				JOptionPane.showMessageDialog(new JFrame(),  "PhyML binary does not have execution permission:\n   " +info.getMessage(),
						"jModeltest error", JOptionPane.ERROR_MESSAGE);
				break;
			case ProgressInfo.ERROR_BINARY_NOEXISTS:
				lockTimer = System.currentTimeMillis();
				progressBarCancel(null);
				Utilities.printRed("\n********************\n");
				Utilities.printRed("PhyML binary does not exist: " + info.getMessage());
				Utilities.printRed("\n********************\n");
				JOptionPane.showMessageDialog(new JFrame(),  "PhyML binary does not exist:\n   " +info.getMessage(),
						"jModeltest error", JOptionPane.ERROR_MESSAGE);
				break;

			case ProgressInfo.OPTIMIZATION_COMPLETED_OK:

				if (!interrupted) {

					stream.println(" ");
					stream.println("::Results::");
					stream.println(" ");
					int numComputedModels = 0;
					for (Model model : ModelTest.getCandidateModels()) {
						if (model.getLnL() > 0.0) {
							numComputedModels++;
							model.print(ModelTest.getMainConsole());
							ModelTest.getMainConsole().println(" ");
						}
					}

					String baseTree = "";

					/* update gui status */
					if (!options.fixedTopology && !options.userTopologyExists)
						baseTree = "(optimized trees)";
					else
						baseTree = "(fixed tree)";

					XManager.getInstance()
							.setLikeLabelText(
									"  Likelihood scores loaded for "
											+ numComputedModels + " models "
											+ baseTree);

					if (options.isClusteringSearch()
							|| (numComputedModels == options.getNumModels())) {
						XManager.getInstance().setLikeLabelColor(
								XManager.LABEL_BLUE_COLOR);

						stream.println("\nComputation of likelihood scores completed. It took "
								+ Utilities.calculateRuntime(startTime,
										System.currentTimeMillis()) + ".\n");

						XManager.getInstance()
								.enableMenuhLRT(
										options.getSubstTypeCode() < 4
												& (options.fixedTopology | options.userTopologyExists));
						XManager.getInstance()
								.enableMenuAveraging(
										!(options.fixedTopology | options.userTopologyExists));

						XManager.getInstance().enableMenuAIC(true);
						XManager.getInstance().enableMenuBIC(true);
						XManager.getInstance().enableMenuDT(true);

						/* build results table */
						XManager.getInstance().buildFrameResults();
						XManager.getInstance().enableMenuShowModelTable(true);
						XManager.getInstance().enableMenuHtmlOutput(true);

						System.out.println(" ... OK");

					} else {
						XManager.getInstance().setLikeLabelColor(
								XManager.LABEL_FAIL_COLOR);

						stream.println("\nComputation of likelihood scores interrupted. It took "
								+ Utilities.calculateRuntime(startTime,
										System.currentTimeMillis()) + ".\n");
					}

				}

				XManager.getInstance()
						.getPane()
						.setCaretPosition(
								XManager.getInstance().getPane().getDocument()
										.getLength());
				/* continue */

			case ProgressInfo.OPTIMIZATION_COMPLETED_INTERRUPTED:
				ExternalExecutionManager.getInstance().killProcesses();
				setVisible(false);
				break;
			}
		} else {
			/* dispose */
			setVisible(false);
			dispose();
		}

	}

	public void actionPerformed(ActionEvent e) {
		/* if the timer caused this event */
		timerLabel.setText("Elapsed time: "
				+ Utilities.calculateRuntimeMinutes(startTime,
						System.currentTimeMillis()));
	}

}
