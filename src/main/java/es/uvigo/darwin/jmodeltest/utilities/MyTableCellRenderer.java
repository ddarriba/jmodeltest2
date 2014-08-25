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
package es.uvigo.darwin.jmodeltest.utilities;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import es.uvigo.darwin.jmodeltest.ModelTest;

/************************* MyTableCellRenderer  ********************************/
/* This class formats the cells in the table
I use this class to highlight the selected model by every criteria */

public class MyTableCellRenderer extends DefaultTableCellRenderer { 

	private static final long serialVersionUID = 8637781802600263981L;

	private JTable whichTable;
	private Object whichValue, val;
	private String whichTableName;
	
	public MyTableCellRenderer (JTable mTable, String mTableName)
		{
		whichTable = mTable;
		whichTableName = mTableName;
		}
	
	public Component getTableCellRendererComponent (JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column)
		{
		setOpaque(true);
		setText(value.toString());
		// set cell's foreground to default cell foreground color
		setForeground(table.getForeground()); 

		if (whichTableName.equals("AIC"))
			whichValue = new Integer(ModelTest.getMinAIC().getId());
		else if (whichTableName.equals("AICc"))
			whichValue = new Integer(ModelTest.getMinAICc().getId());
		else if (whichTableName.equals("BIC"))
			whichValue = new Integer(ModelTest.getMinBIC().getId());
		else if (whichTableName.equals("DT"))
			whichValue = new Integer(ModelTest.getMinDT().getId());
		else
			whichValue = new Integer(0);	
						
		val = whichTable.getValueAt(row, 0);
		// if cell is selected, set background color to default cell selection background color
		if (isSelected) 
			{
			setBackground(table.getSelectionBackground());
			}
		// otherwise, set cell background color to some custom color
		else
			{
			// set rows background to some color
			if (val.equals(whichValue))
				{
				setForeground(Color.red);
				}
			// set even rows background to white
			else
				{ 
				setForeground(Color.black);
				}
			}
		return this;
		}
}
