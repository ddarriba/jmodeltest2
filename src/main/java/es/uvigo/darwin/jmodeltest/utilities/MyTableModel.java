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

import java.math.BigDecimal;

import javax.swing.table.AbstractTableModel;

import es.uvigo.darwin.jmodeltest.ModelTest;

/************************* MyTableModel ********************************/
/* This class fills in the data in the table */

public class MyTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -8310097889316872278L;

	int i;

	final String[] columnNamesModel = { "ID", "Name", "Partition", "-lnL", "p",
			"fA", "fC", "fG", "fT", "ti/tv", "R(a)", "R(b)", "R(c)", "R(d)",
			"R(e)", "R(f)", "p-inv", "shape" };
	final String[] columnNamesAIC = { "ID", "Name", "Partition", "-lnL", "p",
			"AIC", "deltaAIC", "weight", "cumWeight" };

	final String[] columnNamesAICc = { "ID", "Name", "Partition", "-lnL", "p",
			"AICc", "deltaAICc", "weight", "cumWeight" };

	final String[] columnNamesBIC = { "ID", "Name", "Partition", "-lnL", "p",
			"BIC", "deltaBIC", "weight", "cumWeight" };

	final String[] columnNamesDT = { "ID", "Name", "Partition", "-lnL", "p",
			"DT", "deltaDT", "weight", "cumWeight" };

	private String[] columnNames;
	private Object[][] data;
	private int size;

	private ModelTest modelTest;

	public MyTableModel(String whichTable, int size, ModelTest modelTest) {
		this.size = size;
		this.modelTest = modelTest;
		int precision = ModelTest.PRECISION;

		if (whichTable.equals("Model")) {
			columnNames = columnNamesModel;
			data = new Object[size][18];
			for (i = 0; i < size; i++) {
				data[i][0] = new Integer(
						modelTest.getCandidateModels()[i].getId());
				data[i][1] = modelTest.getCandidateModels()[i].getName();
				data[i][2] = modelTest.getCandidateModels()[i].getPartition();
				data[i][3] = new Double(Round(
						modelTest.getCandidateModels()[i].getLnL(), precision));
				data[i][4] = new Integer(
						modelTest.getCandidateModels()[i].getK());

				if (modelTest.getCandidateModels()[i].ispF()) {
					data[i][5] = new Double(Round(
							modelTest.getCandidateModels()[i].getfA(),
							precision));
					data[i][6] = new Double(Round(
							modelTest.getCandidateModels()[i].getfC(),
							precision));
					data[i][7] = new Double(Round(
							modelTest.getCandidateModels()[i].getfG(),
							precision));
					data[i][8] = new Double(Round(
							modelTest.getCandidateModels()[i].getfT(),
							precision));
				} else {
					data[i][5] = "-";
					data[i][6] = "-";
					data[i][7] = "-";
					data[i][8] = "-";
				}

				if (modelTest.getCandidateModels()[i].ispT())
					data[i][9] = new Double(Round(
							modelTest.getCandidateModels()[i].getTitv(),
							precision));
				else
					data[i][9] = "-";

				if (modelTest.getCandidateModels()[i].ispR()) {
					data[i][10] = new Double(Round(
							modelTest.getCandidateModels()[i].getRa(), 6));
					data[i][11] = new Double(Round(
							modelTest.getCandidateModels()[i].getRb(), 6));
					data[i][12] = new Double(Round(
							modelTest.getCandidateModels()[i].getRc(), 6));
					data[i][13] = new Double(Round(
							modelTest.getCandidateModels()[i].getRd(), 6));
					data[i][14] = new Double(Round(
							modelTest.getCandidateModels()[i].getRe(), 6));
					data[i][15] = new Double(1);
				} else {
					data[i][10] = "-";
					data[i][11] = "-";
					data[i][12] = "-";
					data[i][13] = "-";
					data[i][14] = "-";
					data[i][15] = "-";
				}

				if (modelTest.getCandidateModels()[i].ispI()) {
					data[i][16] = new Double(Round(
							modelTest.getCandidateModels()[i].getPinv(),
							precision));
				} else {
					data[i][16] = "-";
				}

				if (modelTest.getCandidateModels()[i].ispI()) {
					if (modelTest.getCandidateModels()[i].getShape() != ModelTest.INFINITY) {
						data[i][17] = new Double(Round(
								modelTest.getCandidateModels()[i].getShape(),
								precision));
					} else {
						data[i][17] = "inf";
					}
				} else {
					data[i][17] = "-";
				}
			}
		} else if (whichTable.equals("AIC")) {
			columnNames = columnNamesAIC;
			data = new Object[size][9];
		} else if (whichTable.equals("AICc")) {
			columnNames = columnNamesAICc;
			data = new Object[size][9];
		} else if (whichTable.equals("BIC")) {
			columnNames = columnNamesBIC;
			data = new Object[size][9];
		} else if (whichTable.equals("DT")) {
			columnNames = columnNamesDT;
			data = new Object[size][9];
		}
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return data.length;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public double Round(double number, int decimalPlace) {
		double returnValue = number;
		if (!(Double.isNaN(number) || Double.isInfinite(number))) {
			BigDecimal bd = new BigDecimal(number);
			bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
			returnValue = bd.doubleValue();
		}
		return (returnValue);
	}

	public void populate(String whichTable) {
		int precision = ModelTest.PRECISION;

		if (whichTable.equals("AIC")) {
			for (i = 0; i < size; i++) {
				data[i][0] = new Integer(
						modelTest.getCandidateModels()[i].getId());
				data[i][1] = modelTest.getCandidateModels()[i].getName();
				data[i][2] = modelTest.getCandidateModels()[i].getPartition();
				data[i][3] = new Double(Round(
						modelTest.getCandidateModels()[i].getLnL(), precision));
				data[i][4] = new Integer(
						modelTest.getCandidateModels()[i].getK());
				data[i][5] = new Double(Round(
						modelTest.getCandidateModels()[i].getAIC(), precision));
				data[i][6] = new Double(Round(
						modelTest.getCandidateModels()[i].getAICd(), precision));
				data[i][7] = new Double(Round(
						modelTest.getCandidateModels()[i].getAICw(), precision));
				data[i][8] = new Double(Round(
						modelTest.getCandidateModels()[i].getCumAICw(),
						precision));
			}
		} else if (whichTable.equals("AICc")) {
			for (i = 0; i < size; i++) {
				data[i][0] = new Integer(
						modelTest.getCandidateModels()[i].getId());
				data[i][1] = modelTest.getCandidateModels()[i].getName();
				data[i][2] = modelTest.getCandidateModels()[i].getPartition();
				data[i][3] = new Double(Round(
						modelTest.getCandidateModels()[i].getLnL(), precision));
				data[i][4] = new Integer(
						modelTest.getCandidateModels()[i].getK());
				data[i][5] = new Double(Round(
						modelTest.getCandidateModels()[i].getAICc(), precision));
				data[i][6] = new Double(
						Round(modelTest.getCandidateModels()[i].getAICcd(),
								precision));
				data[i][7] = new Double(
						Round(modelTest.getCandidateModels()[i].getAICcw(),
								precision));
				data[i][8] = new Double(Round(
						modelTest.getCandidateModels()[i].getCumAICcw(),
						precision));
			}
		} else if (whichTable.equals("BIC")) {
			for (i = 0; i < size; i++) {
				data[i][0] = new Integer(
						modelTest.getCandidateModels()[i].getId());
				data[i][1] = modelTest.getCandidateModels()[i].getName();
				data[i][2] = modelTest.getCandidateModels()[i].getPartition();
				data[i][3] = new Double(Round(
						modelTest.getCandidateModels()[i].getLnL(), precision));
				data[i][4] = new Integer(
						modelTest.getCandidateModels()[i].getK());
				data[i][5] = new Double(Round(
						modelTest.getCandidateModels()[i].getBIC(), precision));
				data[i][6] = new Double(Round(
						modelTest.getCandidateModels()[i].getBICd(), precision));
				data[i][7] = new Double(Round(
						modelTest.getCandidateModels()[i].getBICw(), 4));
				data[i][8] = new Double(Round(
						modelTest.getCandidateModels()[i].getCumBICw(),
						precision));
			}
		} else if (whichTable.equals("DT")) {
			for (i = 0; i < size; i++) {
				data[i][0] = new Integer(
						modelTest.getCandidateModels()[i].getId());
				data[i][1] = modelTest.getCandidateModels()[i].getName();
				data[i][2] = modelTest.getCandidateModels()[i].getPartition();
				data[i][3] = new Double(Round(
						modelTest.getCandidateModels()[i].getLnL(), precision));
				data[i][4] = new Integer(
						modelTest.getCandidateModels()[i].getK());
				data[i][5] = new Double(Round(
						modelTest.getCandidateModels()[i].getDT(), precision));
				data[i][6] = new Double(Round(
						modelTest.getCandidateModels()[i].getDTd(), precision));
				data[i][7] = new Double(Round(
						modelTest.getCandidateModels()[i].getDTw(), 4));
				data[i][8] = new Double(Round(
						modelTest.getCandidateModels()[i].getCumDTw(),
						precision));
			}
		}
	}

}
