package es.uvigo.darwin.jmodeltest.gui;

import java.awt.FileDialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.BorderUIResource.TitledBorderUIResource;

import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.ModelTestConfiguration;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

public class FramePreferences extends JModelTestFrame {

	private static final int WINDOW_WIDTH = 500;
	private static final int OUTER_MARGIN = 10;
	private static final int MARGIN = 20;
	private static final int COMPONENTS_WIDTH = WINDOW_WIDTH - 2 * OUTER_MARGIN;
	private static final int COMPONENTS_HEIGHT = 20;
	private static final int MAX_INNER_WIDTH = COMPONENTS_WIDTH - 2 * MARGIN;
	private static final int BINARIES_PANEL_HEIGHT = MARGIN + 6
			* COMPONENTS_HEIGHT + OUTER_MARGIN;
	private static final int BUTTONS_RESET_WIDTH = 140;
	private static final int BUTTONS_WIDTH = 110;
	private static final int BUTTONS_HEIGHT = 40;
	private static final int BUTTONS_PADDING = 10;
	private static final int BUTTONS_PANEL_WIDTH = BUTTONS_RESET_WIDTH + 2 * BUTTONS_WIDTH + 2 * BUTTONS_PADDING;
	private static final int BUTTONS_VLOC = BINARIES_PANEL_HEIGHT;
	private static final int BUTTONS_HLOC = (COMPONENTS_WIDTH - BUTTONS_PANEL_WIDTH)/2;
	private static final int WINDOW_HEIGHT = 250;
	private static final long serialVersionUID = 1L;
	private static final String BACKUP_FILE = ModelTest.CONFIG_FILE + ".bk";

	private static final String BINARIES_COMMENT = "Set the phyml binary directory. The selected directory "
			+ "should exist and contain a valid phyml binary (\"phyml\" or \""+ Utilities.getBinaryVersion()+"\"). If Global "
			+ "Phyml checkbox is selected, \"phyml\" should exist in the path.";

	private static final String PROPERTIES_COMMENT = "This properties file was modified from "
			+ "jModelTest 2 GUI. A backup of the original configuration file has been "
			+ "stored in "
			+ BACKUP_FILE
			+ ". Replace this file with the backup for "
			+ "restoring the original format.";

	private JTextArea tfBinaryDescription = new JTextArea();
	private JCheckBox cbGlobalPhyml = new JCheckBox();
	private JLabel lbPathToPhyml = new JLabel();
	private JTextField tfPathToPhyml = new JTextField();
	private JPanel binarySettingsPanel = new JPanel();
	private JPanel buttonsPanel = new JPanel();
	private JPanel preferencesPanel = new JPanel();
	private JButton btnSetDefault = new JButton();
	private JButton btnAccept = new JButton();
	private JButton btnCancel = new JButton();
	private JButton btnOpen;

	public FramePreferences() {
		initComponents();
	}

	public void initComponents() {

		preferencesPanel.setLocation(0, 0);
		preferencesPanel.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		preferencesPanel.setVisible(true);
		preferencesPanel.setLayout(null);

		binarySettingsPanel.setLocation(OUTER_MARGIN, OUTER_MARGIN);
		binarySettingsPanel.setSize(COMPONENTS_WIDTH, BINARIES_PANEL_HEIGHT);
		binarySettingsPanel
				.setBorder(new BorderUIResource.TitledBorderUIResource(
						new LineBorder(XManager.PANEL_BORDER_COLOR, 1, false),
						"PhyML Binaries Settings", TitledBorderUIResource.LEFT,
						TitledBorderUIResource.TOP, XManager.FONT_LABEL,
						XManager.LABEL_BLUE_COLOR));
		binarySettingsPanel.setVisible(true);
		binarySettingsPanel.setLayout(null);

		buttonsPanel.setLocation(BUTTONS_HLOC, BUTTONS_VLOC);
		buttonsPanel.setSize(COMPONENTS_WIDTH, BINARIES_PANEL_HEIGHT);
		buttonsPanel.setVisible(true);
		buttonsPanel.setLayout(null);

		tfBinaryDescription.setVisible(true);
		tfBinaryDescription.setEditable(false);
		tfBinaryDescription.setOpaque(false);
		tfBinaryDescription.setForeground(XManager.DARK_GRAY_COLOR);
		tfBinaryDescription.setSize(MAX_INNER_WIDTH, 3 * COMPONENTS_HEIGHT);
		tfBinaryDescription.setLocation(MARGIN, MARGIN);
		tfBinaryDescription.setText(BINARIES_COMMENT);
		tfBinaryDescription.setFont(XManager.FONT_LABEL);
		tfBinaryDescription.setLineWrap(true);
		tfBinaryDescription.setWrapStyleWord(true);

		cbGlobalPhyml.setVisible(true);
		cbGlobalPhyml.setSize(MAX_INNER_WIDTH, COMPONENTS_HEIGHT);
		cbGlobalPhyml.setText("Use global PhyML binary");
		cbGlobalPhyml.setLocation(MARGIN, 3 * COMPONENTS_HEIGHT + MARGIN);
		cbGlobalPhyml.setFont(XManager.FONT_LABEL);

		lbPathToPhyml.setVisible(true);
		lbPathToPhyml.setSize(120, COMPONENTS_HEIGHT);
		lbPathToPhyml.setText("PhyML binary path:");
		lbPathToPhyml.setLocation(2 * MARGIN, MARGIN + 4 * COMPONENTS_HEIGHT);
		lbPathToPhyml.setFont(XManager.FONT_LABEL);

		tfPathToPhyml.setVisible(true);
		tfPathToPhyml.setSize(280, COMPONENTS_HEIGHT);
		tfPathToPhyml.setLocation(2 * MARGIN + 120, MARGIN + 4 * COMPONENTS_HEIGHT);
		tfPathToPhyml.setFont(XManager.FONT_LABEL);
		
		btnOpen = XManager.makeIconButton("Open16", "Explore", "...");
		btnOpen.setLocation(2 * MARGIN + 400, MARGIN + 4 * COMPONENTS_HEIGHT);
		
		btnSetDefault.setText("Default Settings");
		btnSetDefault.setLocation(0, MARGIN);
		btnSetDefault.setSize(BUTTONS_RESET_WIDTH, BUTTONS_HEIGHT);
		btnSetDefault.setVisible(true);

		btnCancel.setText("Cancel");
		btnCancel.setLocation(BUTTONS_RESET_WIDTH+BUTTONS_PADDING, MARGIN);
		btnCancel.setSize(BUTTONS_WIDTH, BUTTONS_HEIGHT);
		btnCancel.setVisible(true);

		btnAccept.setText("Accept");
		btnAccept.setLocation(BUTTONS_RESET_WIDTH + BUTTONS_WIDTH+2*BUTTONS_PADDING, MARGIN);
		btnAccept.setSize(BUTTONS_WIDTH, BUTTONS_HEIGHT);
		btnAccept.setVisible(true);
		
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setResizable(false);
		binarySettingsPanel.add(tfBinaryDescription);
		binarySettingsPanel.add(tfPathToPhyml);
		binarySettingsPanel.add(btnOpen);
		binarySettingsPanel.add(cbGlobalPhyml);
		binarySettingsPanel.add(lbPathToPhyml);
		buttonsPanel.add(btnSetDefault);
		buttonsPanel.add(btnAccept);
		buttonsPanel.add(btnCancel);

		preferencesPanel.add(binarySettingsPanel);
		preferencesPanel.add(buttonsPanel);

		getContentPane().setLayout(null);
		getContentPane().add(preferencesPanel);
		setTitle("Preferences");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLocation(XManager.MAIN_LOCATION);
		getRootPane().setDefaultButton(btnAccept);
		setStatus();

		cbGlobalPhyml.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				globalPhymlActionPerformed(e);
			}
		});

		btnSetDefault.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				cbGlobalPhyml
						.setSelected(ModelTestConfiguration.DEFAULT_GLOBAL_PHYML);
				tfPathToPhyml.setText(ModelTestConfiguration.DEFAULT_EXE_DIR);
				tfPathToPhyml.setEnabled(!cbGlobalPhyml.isSelected());
			}
		});

		btnCancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				cancelActionPerformed(e);
			}
		});

		btnAccept.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				acceptActionPerformed(e);
			}
		});
		
		btnOpen.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				exploreActionPerformed(e);
			}
		});

	}

	private void setStatus() {
		cbGlobalPhyml.setSelected(ModelTestConfiguration.isGlobalPhymlBinary());
		tfPathToPhyml.setText(ModelTestConfiguration.getExeDir());
		tfPathToPhyml.setEnabled(!cbGlobalPhyml.isSelected());
	}

	private void cancelActionPerformed(java.awt.event.ActionEvent e) {
		try {
			setStatus();
			setVisible(false);
			dispose();
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void acceptActionPerformed(java.awt.event.ActionEvent e) {
		try {
			// validate changes
			boolean validated = true;
			if (!cbGlobalPhyml.isSelected()) {
				File testBinDir = new File(tfPathToPhyml.getText());
				if (validated = (testBinDir.exists() && testBinDir
						.isDirectory())) {
					String path = tfPathToPhyml.getText();
					if (!path.endsWith(File.separator)) {
						path += File.separator;
					}
					File localExe = new File(path
							+ Utilities.getBinaryVersion());
					File genericExe = new File(path + "phyml");
					validated = (localExe.exists() && localExe.isFile() && localExe
							.canExecute())
							|| (genericExe.exists() && genericExe.isFile() && genericExe
									.canExecute());
				}
			}
			if (validated) {
				// update properties file
				Properties properties = ModelTestConfiguration.getProperties();
				properties.setProperty(ModelTestConfiguration.EXE_DIR,
						tfPathToPhyml.getText());
				properties.setProperty(ModelTestConfiguration.GLOBAL_PHYML_EXE,
						cbGlobalPhyml.isSelected() ? "true" : "false");
				writeProperties();

				setVisible(false);
				dispose();
			} else {
				JOptionPane
						.showMessageDialog(
								this,
								"There is no phyml binary in the selected location. \n"
										+ "Please make sure that exists an executable file named\n"
										+ "\"phyml\" or "
										+ Utilities.getBinaryVersion() + ".",
								"jModelTest error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void globalPhymlActionPerformed(java.awt.event.ActionEvent e) {
		try {
			tfPathToPhyml.setEnabled(!cbGlobalPhyml.isSelected());
		} catch (Exception f) {
			f.printStackTrace();
		}
	}
	
	private void exploreActionPerformed(java.awt.event.ActionEvent e) {
		FileDialog dialog;
		try {
			try {
				dialog = new FileDialog(this, "Select PhyML binaries directory",
						FileDialog.LOAD);
				dialog.setDirectory(ModelTestConfiguration.DEFAULT_EXE_DIR);
				dialog.setVisible(true);
			} catch (Throwable f) {
				f.printStackTrace();
				return;
			}

			tfPathToPhyml.setText(dialog.getDirectory());

		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	private void writeProperties() {
		Properties properties = ModelTestConfiguration.getProperties();
		try {
			File bkFile = new File(BACKUP_FILE);
			if (!bkFile.exists()) {
				File propertiesFile = new File(ModelTest.CONFIG_FILE);
				InputStream in = new FileInputStream(propertiesFile);
				OutputStream out = new FileOutputStream(bkFile);
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			}
			// Escribier en el archivo los cambios
			FileOutputStream fos = new FileOutputStream(
					ModelTest.CONFIG_FILE.replace("\\", "/"));

			properties.store(fos, PROPERTIES_COMMENT);

		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

}
