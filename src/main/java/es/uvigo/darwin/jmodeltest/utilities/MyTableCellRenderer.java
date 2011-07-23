/** 
 * MyTableCellRenderer.java
 *
 * Title:			MyTableCellRenderer
 * Description:		Format cells in table
 * @author			
 * @version			
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
