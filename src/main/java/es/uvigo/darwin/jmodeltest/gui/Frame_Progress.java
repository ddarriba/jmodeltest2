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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.XManager;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public class Frame_Progress extends JModelTestFrame implements Observer, ActionListener {

	private static final long serialVersionUID = 201102181036L;
	private static final int HEIGHT_PER_THREAD = 25;
	private static final String NO_MODEL = "iddle";

	private TextOutputStream stream;

	private int numberOfThreads;
	private JProgressBar threadProgressBar[];
	private JLabel threadProgressModelLabel[];
	private JButton progressBarCancelButton = new JButton();
	private JProgressBar progressBarLike = new JProgressBar();
	private JLabel progressBarLikeLabel = new JLabel();
	private JLabel timerLabel = new JLabel();
	/** Timer for calculate the elapsed time **/
	private long startTime;
	private Frame_CalcLike frameCalcLike;
	private Timer timer;
	
	private int completedModels = 0;
	private int totalModels;

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
		this.threadProgressModelLabel = new JLabel[numberOfThreads];
		this.maximum = numModels;
		this.frameCalcLike = frameCalcLike;

		initComponents();
		setVisible(true);

		this.progressBarLike.setMaximum(maximum);
		this.progressBarLike.setValue(0);
		this.progressBarLike.setStringPainted(true);
		this.progressBarLike.setString(null);
		
		timer = new Timer(1000, this);
		timer.setRepeats(true);
		timer.start();
	}

	public void initComponents() {
		// the following code sets the frame's initial state
		progressBarLike.setSize(new java.awt.Dimension(300, 30));
		progressBarLike.setString("");
		progressBarLike.setLocation(new java.awt.Point(40, 40
				+ HEIGHT_PER_THREAD * numberOfThreads));
		progressBarLike.setVisible(true);
		progressBarLikeLabel.setSize(new java.awt.Dimension(120, 20));
		progressBarLikeLabel.setLocation(new java.awt.Point(40, 20));
		progressBarLikeLabel.setVisible(true);
		progressBarLikeLabel.setFont(XManager.FONT_CONSOLE);
		timerLabel.setSize(new java.awt.Dimension(180, 20));
		timerLabel.setLocation(new java.awt.Point(160, 20));
		timerLabel.setVisible(true);
		timerLabel.setFont(XManager.FONT_CONSOLE);
		timerLabel.setAlignmentX(RIGHT_ALIGNMENT);
		progressBarCancelButton.setVisible(true);
		progressBarCancelButton.setSize(new java.awt.Dimension(100, 35));
		progressBarCancelButton.setText("Cancel");
		progressBarCancelButton.setLocation(new java.awt.Point(140, 80
				+ HEIGHT_PER_THREAD * numberOfThreads));
		progressBarLikeLabel.setText("Completed 0/" + totalModels);
		for (int i = 0; i < numberOfThreads; i++) {
			threadProgressModelLabel[i] = new JLabel();
			threadProgressModelLabel[i].setSize(new java.awt.Dimension(80, 20));
			threadProgressModelLabel[i].setLocation(new java.awt.Point(40, 40
					+ (i) * HEIGHT_PER_THREAD));
			threadProgressModelLabel[i].setVisible(true);
			threadProgressModelLabel[i].setFont(XManager.FONT_CONSOLE);
			threadProgressModelLabel[i].setText("Thread " + i + ":");
			threadProgressBar[i] = new JProgressBar();
			threadProgressBar[i].setSize(new java.awt.Dimension(220, 20));
			threadProgressBar[i].setString(NO_MODEL);
			threadProgressBar[i].setStringPainted(true);
			threadProgressBar[i].setIndeterminate(false);
			threadProgressBar[i].setLocation(new java.awt.Point(120, 40 + (i)
					* HEIGHT_PER_THREAD));
			threadProgressBar[i].setVisible(true);
		}

		setLocation(new java.awt.Point(281, 80));
		getContentPane().setLayout(null);
		setTitle("Progress");

		for (JProgressBar progressBar : threadProgressBar) {
			getContentPane().add(progressBar);
		}
		// for (JLabel progressLabel : threadProgressLabel) {
		// getContentPane().add(progressLabel);
		// }
		for (JLabel progressLabel : threadProgressModelLabel) {
			getContentPane().add(progressLabel);
		}
		getContentPane().add(progressBarLike);
		getContentPane().add(progressBarLikeLabel);
		getContentPane().add(progressBarCancelButton);
		getContentPane().add(timerLabel);

		setSize(new java.awt.Dimension(370, 152 + HEIGHT_PER_THREAD
				* numberOfThreads));
		setResizable(false);

		// event handling
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});

		progressBarCancelButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						progressBarCancel(e);
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
		// System.exit(0);
	}

	private void progressBarCancel(java.awt.event.ActionEvent e) {
		try {
			frameCalcLike.getRunPhyml().interruptThread();
			frameCalcLike.cancelTask();
			setVisible(false);
			dispose();
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	@Override
	public void update(Observable o, Object arg) {

		if (arg != null) {
			ProgressInfo info = (ProgressInfo) arg;

			switch (info.getType()) {

			case ProgressInfo.BASE_TREE_INIT:
				stream.print("\nEstimating a BIONJ-JC tree ... ");
				System.out.print("estimating a BIONJ-JC tree ... ");
				break;

			case ProgressInfo.BASE_TREE_COMPUTED:
				stream.println("OK");
				System.out.println("OK");
				stream.print(info.getModel().getName() + " tree: "
						+ info.getModel().getTreeString() + "\n");
				break;

			case ProgressInfo.SINGLE_OPTIMIZATION_INIT:
				for (JProgressBar progress : threadProgressBar) {
					if (progress.getString().equals(NO_MODEL)) {
						progress.setString("Computing "
								+ info.getModel().getName() + "...");
						progress.setIndeterminate(true);
						break;
					}
				}
				/*
				 * stream.println("\nMaximum likelihod estimation for the " +
				 * info.getModel().getName() + " model.");
				 * 
				 * if (options.userTopologyExists)
				 * stream.println("  User tree topology"); else if
				 * (options.fixedTopology)
				 * stream.println("  BIONJ-JC tree topology"); else if
				 * (options.optimizeMLTopology)
				 * stream.println("  ML optimized tree topology"); else
				 * stream.println("  BIONJ tree topology");
				 */
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
				int value = (completedModels * 100) / totalModels;
				for (JProgressBar progress : threadProgressBar) {
					if (progress.getString().equals(
							"Computing " + info.getModel().getName() + "...")) {
						progress.setString(NO_MODEL);
						progress.setIndeterminate(false);
						break;
					}
				}
				
				progressBarLikeLabel.setText("Completed "
						+ completedModels + "/" + totalModels);
				progressBarLike.setValue(value);

				stream.println(info.getModel().getName()
						+ "\t\t"
						+ info.getMessage()
						+ "\t"
						+ Utilities.calculateRuntime(startTime,
								System.currentTimeMillis()) + "\t"
						+ String.format("%5.4f", info.getModel().getLnL()));

				// scroll to the bottom
				XManager.getInstance()
						.getPane()
						.setCaretPosition(
								XManager.getInstance().getPane().getDocument()
										.getLength());
				break;

			case ProgressInfo.INTERRUPTED:
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
				}
				break;

			case ProgressInfo.ERROR:
				stream.println(info.getMessage());

				JOptionPane.showMessageDialog(new JFrame(), info.getMessage(),
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

					// update gui status
					if (!options.fixedTopology && !options.userTopologyExists)
						baseTree = "(optimized trees)";
					else
						baseTree = "(fixed tree)";

					XManager.getInstance()
							.setLikeLabelText(
									"  Likelihood scores loaded for "
											+ numComputedModels + " models "
											+ baseTree);

					if (numComputedModels == options.numModels) {
						XManager.getInstance().setLikeLabelColor(
								XManager.LABEL_BLUE_COLOR);

						stream.println("\nComputation of likelihood scores completed. It took "
								+ Utilities.calculateRuntime(startTime,
										System.currentTimeMillis()) + ".\n");

						// calculations
						if (options.fixedTopology) {
							XManager.getInstance().enableMenuhLRT(true);
							XManager.getInstance().enableMenuAveraging(false);
						}

						XManager.getInstance().enableMenuAIC(true);
						XManager.getInstance().enableMenuBIC(true);
						XManager.getInstance().enableMenuDT(true);

						// build results table
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
				// continue

			case ProgressInfo.OPTIMIZATION_COMPLETED_INTERRUPTED:
				// dispose
				setVisible(false);
				dispose();
				break;
			}
		} else {
			// dispose
			setVisible(false);
			dispose();
		}

	}
	
	public void actionPerformed(ActionEvent e) {
		// If the timer caused this event.
		timerLabel.setText("Elapsed time: "
				+ Utilities.calculateRuntimeMinutes(startTime,
						System.currentTimeMillis()));
	}

}
