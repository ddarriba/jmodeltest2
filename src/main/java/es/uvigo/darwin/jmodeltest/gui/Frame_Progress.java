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
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.XManager;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.observer.ProgressInfo;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public class Frame_Progress extends JModelTestFrame implements Observer {

	private static final long serialVersionUID = 201102181036L;
	private TextOutputStream stream;

	private JProgressBar progressBarLikelihoods = new JProgressBar();
	private JLabel progressBarLikelihoodsLabel = new JLabel();
	private JButton progressBarCancelButton = new JButton();
	private JProgressBar progressBarLike;
	private JLabel progressBarLikeLabel;
	/** Timer for calculate the elapsed time **/
	private long startTime;
	private Frame_CalcLike frameCalcLike;

	private boolean interrupted;

	private int maximum;

	public Frame_Progress(int numModels, Frame_CalcLike frameCalcLike,
			ApplicationOptions options) {
		this.interrupted = false;
		this.options = options;
		this.startTime = System.currentTimeMillis();
		this.stream = ModelTest.getMainConsole();
		this.progressBarLike = progressBarLikelihoods;
		this.progressBarLikeLabel = progressBarLikelihoodsLabel;
		this.maximum = numModels;
		this.frameCalcLike = frameCalcLike;

		initComponents();
		setVisible(true);

		this.progressBarLike.setMaximum(maximum);
		this.progressBarLike.setValue(0);
		this.progressBarLike.setStringPainted(true);
		this.progressBarLike.setString(null);
	}

	public void initComponents() {
		// the following code sets the frame's initial state
		progressBarLikelihoods.setSize(new java.awt.Dimension(300, 30));
		progressBarLikelihoods.setString("");
		progressBarLikelihoods.setLocation(new java.awt.Point(40, 40));
		progressBarLikelihoods.setVisible(true);
		progressBarLikelihoodsLabel.setSize(new java.awt.Dimension(300, 20));
		progressBarLikelihoodsLabel.setLocation(new java.awt.Point(40, 20));
		progressBarLikelihoodsLabel.setVisible(true);
		progressBarLikelihoodsLabel.setFont(XManager.FONT_CONSOLE);
		progressBarCancelButton.setVisible(true);
		progressBarCancelButton.setSize(new java.awt.Dimension(100, 35));
		progressBarCancelButton.setText("Cancel");
		progressBarCancelButton.setLocation(new java.awt.Point(140, 80));

		setLocation(new java.awt.Point(281, 80));
		getContentPane().setLayout(null);
		setTitle("Progress");

		getContentPane().add(progressBarLikelihoods);
		getContentPane().add(progressBarLikelihoodsLabel);
		getContentPane().add(progressBarCancelButton);

		setSize(new java.awt.Dimension(370, 152));
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
				progressBarLike.setValue(info.getValue());
				progressBarLikeLabel.setText("\n"
						+ info.getModel().getName()
						+ " ("
						+ (info.getValue() + 1)
						+ "/"
						+ (options.numModels)
						+ ")"
						+ "  total time = "
						+ Utilities.calculateRuntime(startTime,
								System.currentTimeMillis()));

				stream.println("\nMaximum likelihod estimation for the "
						+ info.getModel().getName() + " model.");

				if (options.userTopologyExists)
					stream.println("  User tree topology");
				else if (options.fixedTopology)
					stream.println("  BIONJ-JC tree topology");
				else if (options.optimizeMLTopology)
					stream.println("  ML optimized tree topology");
				else
					stream.println("  BIONJ tree topology");
				break;

			case ProgressInfo.SINGLE_OPTIMIZATION_COMPLETED:
				stream.print("  Computation time = " + info.getMessage());
				stream.println("  ("
						+ Utilities.calculateRuntime(startTime,
								System.currentTimeMillis()) + ")");

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
					stream.println("\nComputation of likelihood scores discontinued ...");
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

				int numComputedModels = 0;
				for (Model model : ModelTest.model) {
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

				XManager.getInstance().setLikeLabelText(
						"  Likelihood scores loaded for "
								+ numComputedModels + " models " + baseTree);
				if (numComputedModels == options.numModels) {
					XManager.getInstance().setLikeLabelColor(
							XManager.LABEL_BLUE_COLOR);
				
				stream.println("\nComputation of likelihood scores completed. It took "
						+ Utilities.calculateRuntime(startTime,
								System.currentTimeMillis()) + ".\n");
				} else {
					XManager.getInstance().setLikeLabelColor(
							XManager.LABEL_FAIL_COLOR);
					
					stream.println("\nComputation of likelihood scores interrupted. It took "
							+ Utilities.calculateRuntime(startTime,
									System.currentTimeMillis()) + ".\n");
				}

				// calculations
				if (options.fixedTopology) {
					XManager.getInstance().enableMenuhLRT(true);
					XManager.getInstance().enableMenuAveraging(false);
				}

				XManager.getInstance().enableMenuAIC(true);
				XManager.getInstance().enableMenuBIC(true);
				XManager.getInstance().enableMenuDT(true);

				ModelTest.setMyAIC(null);
				ModelTest.setMyAICc(null);
				ModelTest.setMyBIC(null);
				ModelTest.setMyDT(null);

				// build results table
				XManager.getInstance().buildFrameResults();
				XManager.getInstance().enableMenuShowModelTable(true);

				XManager.getInstance()
						.getPane()
						.setCaretPosition(
								XManager.getInstance().getPane().getDocument()
										.getLength());
				System.out.println(" ... OK");
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

}
