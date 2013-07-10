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
import java.awt.Insets;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

import es.uvigo.darwin.jmodeltest.utilities.MyTableCellRenderer;
import es.uvigo.darwin.jmodeltest.utilities.MyTableModel;
import es.uvigo.darwin.jmodeltest.utilities.TableSorter;

public class FrameResults extends JModelTestFrame {

	public static final int TAB_AIC = 1;
	public static final int TAB_AICc = 2;
	public static final int TAB_BIC = 3;
	public static final int TAB_DT = 4;
	
	private static final Dimension TABBED_PANE_DIM = new Dimension(700+100, 400-20);
	private static final Dimension SCROLL_PANE_DIM = new Dimension(670+100, 320);
	private static final Dimension PANEL_INFO_DIM = new Dimension(700+100, 30);
	private static final Dimension LABEL_INFO_DIM = new Dimension(680+100, 20);
	private static final Dimension LABEL_DATE_DIM = new Dimension(200+100, 20);
	private static final Dimension FRAME_DIM = new Dimension(700+100, 460);
	
	private static final long serialVersionUID = 7368405541555631433L;

	private javax.swing.JPanel panelInfo = new javax.swing.JPanel();
	private javax.swing.JLabel labelInfo = new javax.swing.JLabel();
	private javax.swing.JLabel labelDate = new javax.swing.JLabel();
	private javax.swing.JTabbedPane tabbedPane = new javax.swing.JTabbedPane();
	private javax.swing.JPanel panelModels = new javax.swing.JPanel();
	private javax.swing.JScrollPane scrollPaneModels = new javax.swing.JScrollPane();
	private javax.swing.JTable tableModels = new javax.swing.JTable();
	private javax.swing.JPanel panelAIC = new javax.swing.JPanel();
	private javax.swing.JScrollPane scrollPaneAIC = new javax.swing.JScrollPane();
	private javax.swing.JTable tableAIC = new javax.swing.JTable();
	private javax.swing.JPanel panelAICc = new javax.swing.JPanel();
	private javax.swing.JScrollPane scrollPaneAICc = new javax.swing.JScrollPane();
	private javax.swing.JTable tableAICc = new javax.swing.JTable();
	private javax.swing.JPanel panelBIC = new javax.swing.JPanel();
	private javax.swing.JScrollPane scrollPaneBIC = new javax.swing.JScrollPane();
	private javax.swing.JTable tableBIC = new javax.swing.JTable();
	private javax.swing.JPanel panelDT = new javax.swing.JPanel();
	private javax.swing.JScrollPane scrollPaneDT = new javax.swing.JScrollPane();
	private  javax.swing.JTable tableDT = new javax.swing.JTable();

	private  MyTableModel modelModels = new MyTableModel("Model", options.getNumModels());
	TableSorter sorterModels = new TableSorter(modelModels);
	JTable tempTableModels = new JTable(sorterModels);

	private MyTableModel modelAIC = new MyTableModel("AIC", options.getNumModels());
	TableSorter sorterAIC = new TableSorter(modelAIC);
	JTable tempTableAIC = new JTable(sorterAIC);
	MyTableCellRenderer AICRenderer = new MyTableCellRenderer(tempTableAIC,"AIC"); 

	private MyTableModel modelAICc = new MyTableModel("AICc", options.getNumModels());
	TableSorter sorterAICc = new TableSorter(modelAICc);
	JTable tempTableAICc = new JTable(sorterAICc);
	MyTableCellRenderer AICcRenderer = new MyTableCellRenderer(tempTableAICc,"AICc"); 

	private MyTableModel modelBIC = new MyTableModel("BIC", options.getNumModels());
	TableSorter sorterBIC = new TableSorter(modelBIC);
	JTable tempTableBIC = new JTable(sorterBIC);
	MyTableCellRenderer BICRenderer = new MyTableCellRenderer(tempTableBIC,"BIC"); 
	
	private MyTableModel modelDT = new MyTableModel("DT", options.getNumModels());
	TableSorter sorterDT = new TableSorter(modelDT);
	JTable tempTableDT = new JTable(sorterDT);
	MyTableCellRenderer DTRenderer = new MyTableCellRenderer(tempTableDT,"DT"); 

	public void initComponents() throws Exception {

	   	tableModels = tempTableModels;
	    tableAIC = tempTableAIC;
	   	tableAICc = tempTableAICc;
		tableBIC = tempTableBIC;
		tableDT = tempTableDT;
				
		// set format for all columns
		for (int i = 0; i < 8; i++) 
			{ 
			TableColumn AICtableColumn = tableAIC.getColumnModel().getColumn(i); 
			AICtableColumn.setCellRenderer((javax.swing.table.TableCellRenderer) AICRenderer); 

			TableColumn AICctableColumn = tableAICc.getColumnModel().getColumn(i); 
			AICctableColumn.setCellRenderer((javax.swing.table.TableCellRenderer) AICcRenderer); 

			TableColumn BICtableColumn = tableBIC.getColumnModel().getColumn(i); 
			BICtableColumn.setCellRenderer((javax.swing.table.TableCellRenderer) BICRenderer); 

			TableColumn DTtableColumn = tableDT.getColumnModel().getColumn(i); 
			DTtableColumn.setCellRenderer((javax.swing.table.TableCellRenderer) DTRenderer); 
			}

 		panelInfo.setSize(PANEL_INFO_DIM);
 		panelInfo.setLocation(new java.awt.Point(0, 390));
 		panelInfo.setVisible(true);
 		panelInfo.setLayout(null);
 		labelInfo.setSize(LABEL_INFO_DIM);
 		labelInfo.setLocation(new java.awt.Point(40, 0));
 		labelInfo.setVisible(true);
 		labelInfo.setText("Decimal numbers are rounded. Click on column headers to sort data in ascending or descending order (+Shift)");
 		labelInfo.setForeground(java.awt.Color.gray);
 		labelInfo.setHorizontalTextPosition(javax.swing.JLabel.CENTER);
 		labelInfo.setFont(XManager.FONT_LABEL_BIG);
 		labelDate.setSize(LABEL_DATE_DIM);
 		labelDate.setLocation(new java.awt.Point(40, 10));
 		labelDate.setVisible(true);
 		labelDate.setText("Date");
 		labelDate.setForeground(java.awt.Color.gray);
 		labelDate.setFont(XManager.FONT_LABEL_BIG);
 		tabbedPane.setSize(TABBED_PANE_DIM);
 		tabbedPane.setLocation(new java.awt.Point(0, 0));
 		tabbedPane.setVisible(true);
 		tabbedPane.setAutoscrolls(true);
 		panelModels.setVisible(true);
 		panelModels.setLayout(null);
		panelModels.setFont(XManager.FONT_CONSOLE);

 		scrollPaneModels.setSize(SCROLL_PANE_DIM);
 		scrollPaneModels.setLocation(new java.awt.Point(12, 14));
 		scrollPaneModels.setVisible(true);
 		scrollPaneModels.setAutoscrolls(true);
 		scrollPaneModels.setForeground(java.awt.Color.blue);
 		scrollPaneModels.setBackground(null);
 		scrollPaneModels.setFont(XManager.FONT_TABULAR);
 		tableModels.setColumnSelectionAllowed(true);
 		tableModels.setToolTipText("Click and Shift+Click on headers to order up and down");
 		tableModels.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
 		tableModels.setCellSelectionEnabled(true);
 		tableModels.setVisible(true);
 		tableModels.setPreferredScrollableViewportSize(new java.awt.Dimension(600, 300));
 		tableModels.setGridColor(java.awt.Color.gray);
 		tableModels.setFont(XManager.FONT_TABULAR);
 		panelAIC.setVisible(true);
		panelAIC.setLayout(null);
 		scrollPaneAIC.setSize(SCROLL_PANE_DIM);
 		scrollPaneAIC.setLocation(new java.awt.Point(12, 14));
 		scrollPaneAIC.setVisible(true);
 		scrollPaneAIC.setAutoscrolls(true);
 		scrollPaneAIC.setForeground(java.awt.Color.blue);
 		scrollPaneAIC.setBackground(null);
 		scrollPaneAIC.setFont(XManager.FONT_TABULAR);
 		tableAIC.setColumnSelectionAllowed(true);
 		tableAIC.setToolTipText("Click and Shift+Click on headers to order up and down");
 		tableAIC.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
 		tableAIC.setCellSelectionEnabled(true);
 		tableAIC.setVisible(true);
 		tableAIC.setPreferredScrollableViewportSize(new java.awt.Dimension(675, 350));
 		tableAIC.setGridColor(java.awt.Color.gray);
 		tableAIC.setFont(XManager.FONT_TABULAR);
 		panelAICc.setVisible(true);
 		panelAICc.setLayout(null);
 		scrollPaneAICc.setSize(SCROLL_PANE_DIM);
 		scrollPaneAICc.setLocation(new java.awt.Point(12, 14));
 		scrollPaneAICc.setVisible(true);
 		scrollPaneAICc.setAutoscrolls(true);
 		scrollPaneAICc.setForeground(java.awt.Color.blue);
 		scrollPaneAICc.setBackground(null);
 		scrollPaneAICc.setFont(XManager.FONT_TABULAR);
 		tableAICc.setColumnSelectionAllowed(true);
 		tableAICc.setToolTipText("Click and Shift+Click on headers to order up and down");
 		tableAICc.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
 		tableAICc.setCellSelectionEnabled(true);
 		tableAICc.setVisible(true);
 		tableAICc.setPreferredScrollableViewportSize(new java.awt.Dimension(675, 350));
 		tableAICc.setGridColor(java.awt.Color.gray);
 		tableAICc.setFont(XManager.FONT_TABULAR);
 		panelBIC.setVisible(true);
 		panelBIC.setLayout(null);
 		scrollPaneBIC.setSize(SCROLL_PANE_DIM);
 		scrollPaneBIC.setLocation(new java.awt.Point(12, 14));
 		scrollPaneBIC.setVisible(true);
 		scrollPaneBIC.setAutoscrolls(true);
 		scrollPaneBIC.setForeground(java.awt.Color.blue);
 		scrollPaneBIC.setBackground(null);
 		scrollPaneBIC.setFont(XManager.FONT_TABULAR);
 		tableBIC.setColumnSelectionAllowed(true);
 		tableBIC.setToolTipText("Click and Shift+Click on headers to order up and down");
 		tableBIC.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
 		tableBIC.setCellSelectionEnabled(true);
 		tableBIC.setVisible(true);
 		tableBIC.setPreferredScrollableViewportSize(new java.awt.Dimension(675, 350));
 		tableBIC.setGridColor(java.awt.Color.gray);
 		tableBIC.setFont(XManager.FONT_TABULAR);
  		panelDT.setVisible(true);
 		panelDT.setLayout(null);
 		scrollPaneDT.setSize(SCROLL_PANE_DIM);
 		scrollPaneDT.setLocation(new java.awt.Point(12, 14));
 		scrollPaneDT.setVisible(true);
 		scrollPaneDT.setAutoscrolls(true);
 		scrollPaneDT.setForeground(java.awt.Color.blue);
 		scrollPaneDT.setBackground(null);
 		scrollPaneDT.setFont(XManager.FONT_TABULAR);
 		tableDT.setColumnSelectionAllowed(true);
 		tableDT.setToolTipText("Click and Shift+Click on headers to order up and down");
 		tableDT.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
 		tableDT.setCellSelectionEnabled(true);
 		tableDT.setVisible(true);
 		tableDT.setPreferredScrollableViewportSize(new java.awt.Dimension(675, 350));
 		tableDT.setGridColor(java.awt.Color.gray);
 		tableDT.setFont(XManager.FONT_TABULAR);
 		setLocation(new java.awt.Point(281, 80));
 		setResizable(true);
 		setFont(XManager.FONT_TABULAR);
 		setLayout(null);
 		setTitle("Results");
		setResizable(false);
		
 		panelInfo.add(labelInfo);
 		panelInfo.add(labelDate);
 		tabbedPane.add(panelModels);
 		tabbedPane.setTitleAt(tabbedPane.getTabCount() - 1, "Models");
 		tabbedPane.add(panelAIC);
		tabbedPane.setTitleAt(tabbedPane.getTabCount() - 1, "AIC");
		tabbedPane.setEnabledAt(tabbedPane.getTabCount() - 1, false);
 		tabbedPane.setForegroundAt(tabbedPane.getTabCount() - 1, Color.gray);
		tabbedPane.add(panelAICc);
 		tabbedPane.setTitleAt(tabbedPane.getTabCount() - 1, "AICc");
 		tabbedPane.setEnabledAt(tabbedPane.getTabCount() - 1, false);
 		tabbedPane.setForegroundAt(tabbedPane.getTabCount() - 1, Color.gray);
		tabbedPane.add(panelBIC);
 		tabbedPane.setTitleAt(tabbedPane.getTabCount() - 1, "BIC");
		tabbedPane.setEnabledAt(tabbedPane.getTabCount() - 1, false);
		tabbedPane.setForegroundAt(tabbedPane.getTabCount() - 1, Color.gray);
		tabbedPane.add(panelDT);
 		tabbedPane.setTitleAt(tabbedPane.getTabCount() - 1, "DT");
		tabbedPane.setEnabledAt(tabbedPane.getTabCount() - 1, false);
		tabbedPane.setForegroundAt(tabbedPane.getTabCount() - 1, Color.gray);

		panelModels.add(scrollPaneModels);
 		scrollPaneModels.getViewport().add(tableModels);
 		panelAIC.add(scrollPaneAIC);
 		scrollPaneAIC.getViewport().add(tableAIC);
 		panelAICc.add(scrollPaneAICc);
 		scrollPaneAICc.getViewport().add(tableAICc);
 		panelBIC.add(scrollPaneBIC);
 		scrollPaneBIC.getViewport().add(tableBIC);
 		panelDT.add(scrollPaneDT);
 		scrollPaneDT.getViewport().add(tableDT);

 		add(panelInfo);
 		add(tabbedPane);
 
 		tabbedPane.setSelectedIndex(0);
 		setSize(FRAME_DIM);
 
 		// event handling
 		addWindowListener(new java.awt.event.WindowAdapter() {
 			public void windowClosing(java.awt.event.WindowEvent e) {
 				thisWindowClosing(e);
 			}
 		});
 
 		 sorterModels.addMouseListenerToHeaderInTable(tableModels);
 		 sorterAIC.addMouseListenerToHeaderInTable(tableAIC);
 		 sorterAICc.addMouseListenerToHeaderInTable(tableAICc);
 		 sorterBIC.addMouseListenerToHeaderInTable(tableBIC);
 		 sorterDT.addMouseListenerToHeaderInTable(tableDT);	

		// set date
		Date today = new Date();
	    SimpleDateFormat formatter = new SimpleDateFormat("dd MMMMM yyyy");
	    String datenewformat = formatter.format(today);
	 	labelDate.setText(datenewformat);

	}
  
  	private boolean mShown = false;
  	
	public void addNotify() {
		super.addNotify();
		
		if (mShown)
			return;
			
		// move components to account for insets
		Insets insets = getInsets();
		Component[] components = getComponents();
		for (int i = 0; i < components.length; i++) {
			Point location = components[i].getLocation();
			location.move(location.x, location.y + insets.top);
			components[i].setLocation(location);
		}

		mShown = true;
	}

	// Close the window when the close box is clicked
	void thisWindowClosing(java.awt.event.WindowEvent e) {
		setVisible(false);
		dispose();
		//System.exit(0);
	}
	
	public void enablePane(int pane) {
		tabbedPane.setEnabledAt(pane, true);
		tabbedPane.setForegroundAt(pane, Color.black);
	}
	
	public void disablePane(int pane) {
		tabbedPane.setEnabledAt(pane, false);
		tabbedPane.setForegroundAt(pane, Color.gray);
	}

	public void populate(int pane) {
		switch(pane) {
		case TAB_AIC:
			modelAIC.populate("AIC");
			break;
		case TAB_AICc:
			modelAICc.populate("AICc");
			break;
		case TAB_BIC:
			modelBIC.populate("BIC");
			break;
		case TAB_DT:
			modelDT.populate("DT");
			break;
		}
	}

}





