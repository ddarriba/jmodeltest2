/** 
 * MyTableModel.java
 *
 * Title:			Modeltest
 * Description:		Select models of nucleotide substitition
 * @author			
 * @version			
 */


package es.uvigo.darwin.jmodeltest.utilities;

import java.math.BigDecimal;

import javax.swing.table.AbstractTableModel;

import es.uvigo.darwin.jmodeltest.ModelTest;


/************************* MyTableModel  ********************************/
/* This class fills in the data in the table */

public class MyTableModel extends AbstractTableModel 
	{

	private static final long serialVersionUID = -8310097889316872278L;

	int i;

	final String[] columnNamesModel = 
		{"ID","Name","Partition","-lnL","p", 
		 "fA", "fC", "fG", "fT", "ti/tv",
		 "R(a)","R(b)","R(c)","R(d)","R(e)","R(f)","p-inv","shape"};
	final String[] columnNamesAIC = 
		{"ID","Name","Partition","-lnL","p", 
		 "AIC", "deltaAIC", "weight", "cumWeight"};
	
	final String[] columnNamesAICc = 
		{"ID","Name","Partition","-lnL","p", 
		 "AICc", "deltaAICc", "weight", "cumWeight"};

	final String[] columnNamesBIC = 
		{"ID","Name","Partition","-lnL","p", 
		 "BIC", "deltaBIC", "weight", "cumWeight"};

	final String[] columnNamesDT = 
		{"ID","Name","Partition","-lnL","p", 
		 "DT", "deltaDT", "weight", "cumWeight"};
		
	private  String[] columnNames;
	private Object[][] data;
	private int size;

	public MyTableModel (String whichTable, int size)
		{
		this.size = size;
		int precision = ModelTest.PRECISION;

		if (whichTable.equals("Model"))
			{
			columnNames = columnNamesModel;
			data = new Object[size][18];
			for (i=0; i<size; i++)
				{
				data[i][0] = new Integer (ModelTest.model[i].getId());
				data[i][1] = ModelTest.model[i].getName();
				data[i][2] = ModelTest.model[i].getPartition();
				data[i][3] = new Double(Round(ModelTest.model[i].getLnL(),precision));
				data[i][4] = new Integer (ModelTest.model[i].getK());

				if (ModelTest.model[i].ispF())
					{
					data[i][5] = new Double(Round(ModelTest.model[i].getfA(),precision));
					data[i][6] = new Double(Round(ModelTest.model[i].getfC(),precision));
					data[i][7] = new Double(Round(ModelTest.model[i].getfG(),precision));
					data[i][8] = new Double(Round(ModelTest.model[i].getfT(),precision));
					}
				else	
					{
					data[i][5] = "-";
					data[i][6] = "-";
					data[i][7] = "-";
					data[i][8] = "-";
					}
				
				if (ModelTest.model[i].ispT())
					data[i][9] = new Double (Round(ModelTest.model[i].getTitv(),precision));
				else	
					data[i][9] = "-";

				if (ModelTest.model[i].ispR())
					{
					data[i][10]  = new Double(Round(ModelTest.model[i].getRa(),6));
					data[i][11] = new Double(Round(ModelTest.model[i].getRb(),6));
					data[i][12] = new Double(Round(ModelTest.model[i].getRc(),6));
					data[i][13] = new Double(Round(ModelTest.model[i].getRd(),6));
					data[i][14] = new Double(Round(ModelTest.model[i].getRe(),6));
					data[i][15] = new Double(1);
					}
				else	
					{
					data[i][10] =  "-";
					data[i][11] = "-";
					data[i][12] = "-";
					data[i][13] = "-";
					data[i][14] = "-";
					data[i][15] = "-";
					}
				
				if (ModelTest.model[i].ispI())
					{
					data[i][16] = new Double(Round(ModelTest.model[i].getPinv(),precision));
					}
				else	
					{
					data[i][16] = "-";	
					}
					
				if (ModelTest.model[i].ispI())
					{
					if (ModelTest.model[i].getShape() != ModelTest.INFINITY)
						{
						data[i][17] = new Double(Round(ModelTest.model[i].getShape(),precision));
						}
					else
						{
						data[i][17] = "inf";
						}
					}
				else	
					{
					data[i][17] = "-";
					}
				}
			}
		else if (whichTable.equals("AIC"))
			{
			columnNames = columnNamesAIC;
			data = new Object[size][9];
			}
		else if (whichTable.equals("AICc"))
			{
			columnNames = columnNamesAICc;
			data = new Object[size][9];
			}
		else if (whichTable.equals("BIC"))
			{
			columnNames = columnNamesBIC;
			data = new Object[size][9];
			}
		else if (whichTable.equals("DT"))
			{
			columnNames = columnNamesDT;
			data = new Object[size][9];
			}
		}
		
    public int getColumnCount() 
    	{
        return columnNames.length;
   	 	}
    
    public int getRowCount() 
    	{
        return data.length;
    	}

    public String getColumnName(int col) 
    	{
        return columnNames[col];
    	}

    public Object getValueAt(int row, int col) 
    	{
        return data[row][col];
    	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int c) 
    	{
        return getValueAt(0, c).getClass();
    	}

	public double Round (double number, int decimalPlace)
		{
	    BigDecimal bd = new BigDecimal(number);
	    bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
	    return (bd.doubleValue());
		}


	public void populate (String whichTable)
		{
		int precision = ModelTest.PRECISION;

		if (whichTable.equals("AIC"))
			{
			for (i=0; i<size; i++)
				{
				data[i][0] = new Integer(ModelTest.model[i].getId());
				data[i][1] = ModelTest.model[i].getName();
				data[i][2] = ModelTest.model[i].getPartition();
				data[i][3] = new Double(Round(ModelTest.model[i].getLnL(),precision));
				data[i][4] = new Integer(ModelTest.model[i].getK());
				data[i][5] = new Double(Round(ModelTest.model[i].getAIC(),precision));
				data[i][6] = new Double(Round(ModelTest.model[i].getAICd(),precision));
				data[i][7] = new Double(Round(ModelTest.model[i].getAICw(),precision));
				data[i][8] = new Double(Round(ModelTest.model[i].getCumAICw(),precision));
				}
			}
		else if (whichTable.equals("AICc"))
			{
			for (i=0; i<size; i++)
				{
				data[i][0] = new Integer(ModelTest.model[i].getId());
				data[i][1] = ModelTest.model[i].getName();
				data[i][2] = ModelTest.model[i].getPartition();
				data[i][3] = new Double(Round(ModelTest.model[i].getLnL(),precision));
				data[i][4] = new Integer(ModelTest.model[i].getK());
				data[i][5] = new Double(Round(ModelTest.model[i].getAICc(),precision));
				data[i][6] = new Double(Round(ModelTest.model[i].getAICcd(),precision));
				data[i][7] = new Double(Round(ModelTest.model[i].getAICcw(),precision));
				data[i][8] = new Double(Round(ModelTest.model[i].getCumAICcw(),precision));
				}
			}
		else if (whichTable.equals("BIC"))
			{
			for (i=0; i<size; i++)
				{
				data[i][0] = new Integer(ModelTest.model[i].getId());
				data[i][1] = ModelTest.model[i].getName();
				data[i][2] = ModelTest.model[i].getPartition();
				data[i][3] = new Double(Round(ModelTest.model[i].getLnL(),precision));
				data[i][4] = new Integer(ModelTest.model[i].getK());
				data[i][5] = new Double(Round(ModelTest.model[i].getBIC(),precision));
				data[i][6] = new Double(Round(ModelTest.model[i].getBICd(),precision));
				data[i][7] = new Double(Round(ModelTest.model[i].getBICw(),4));
				data[i][8] = new Double(Round(ModelTest.model[i].getCumBICw(),precision));
				}
			}
		else if (whichTable.equals("DT"))
			{
			for (i=0; i<size; i++)
				{
				data[i][0] = new Integer(ModelTest.model[i].getId());
				data[i][1] = ModelTest.model[i].getName();
				data[i][2] = ModelTest.model[i].getPartition();
				data[i][3] = new Double(Round(ModelTest.model[i].getLnL(),precision));
				data[i][4] = new Integer(ModelTest.model[i].getK());
				data[i][5] = new Double(Round(ModelTest.model[i].getDT(),precision));
				data[i][6] = new Double(Round(ModelTest.model[i].getDTd(),precision));
				data[i][7] = new Double(Round(ModelTest.model[i].getDTw(),4));
				data[i][8] = new Double(Round(ModelTest.model[i].getCumDTw(),precision));
				}
			}
		}

	}
