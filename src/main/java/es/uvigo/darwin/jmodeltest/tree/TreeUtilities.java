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
package es.uvigo.darwin.jmodeltest.tree;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import pal.io.FormattedOutput;
import pal.tree.Node;
import pal.tree.ReadTree;
import pal.tree.Tree;
import pal.tree.TreeParseException;
import es.uvigo.darwin.jmodeltest.io.TextOutputStream;

public class TreeUtilities {

	public static final int DEFAULT_COLUMN_WIDTH = 70;
	public static final String TREE_WEIGHT_ATTRIBUTE = "weight";
	public static final String TREE_CLADE_SUPPORT_ATTRIBUTE = "support";
	public static final String TREE_NAME_ATTRIBUTE = "treeName";

	public static int intNodeNum, tipNodeNum;
	public static TextOutputStream stream;

	public TreeUtilities() {
	}

	/****************************
	 * treeToSplits ****************************** * Converts a tree in Newick
	 * format in a system of splits * * * *
	 ************************************************************************/

	public static void treeToSplits(String treefilename) {

	}

	/*
	 * DP 161107: in progress
	 * 
	 * NOTAS: usar Vector, Enumeration or List ?
	 * 
	 * 
	 * 
	 * ((A,B),(C,D))
	 * 
	 * root A B C D node 1 1 0 0 1 1 node 2 0 0 1 1 node 3 0 1 1 1
	 * 
	 * COMPlETAR!!!
	 */

	/****************************
	 * readTree ********************************** * Reads a tree in Newick
	 * format, rooted or unroored, binary or with * polytomies, and with or
	 * without branch lengths. Returns the root * node *
	 * 
	 * @throws IOException
	 * @throws TreeParseException
	 *             *
	 ************************************************************************/

	public static Tree readTree(String treefilename) throws IOException,
			TreeParseException {
		Tree tree;
		try {
			tree = new ReadTree(treefilename);
		} catch (TreeParseException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
		return tree;
	}

	public static void printNH(PrintWriter out, Tree tree, Node node,
			boolean printLengths, boolean printInternalLabels,
			boolean printCladeSupport) {

		if (!node.isLeaf()) {
			out.print("(");

			for (int i = 0; i < node.getChildCount(); i++) {
				if (i != 0) {
					out.print(",");
				}

				printNH(out, tree, node.getChild(i), printLengths,
						printInternalLabels, printCladeSupport);
			}

			out.print(")");
		}

		if (!node.isRoot()) {
			if (node.isLeaf() || printInternalLabels) {

				String id = node.getIdentifier().toString();
				out.print(id);
			}

			if (printCladeSupport) {
				if (tree.getAttribute(node, TREE_CLADE_SUPPORT_ATTRIBUTE) != null) {
					double support = (Double) tree.getAttribute(node,
							TREE_CLADE_SUPPORT_ATTRIBUTE);
					out.printf(":" + FormattedOutput.getInstance().getDecimalString(support, 4));
				}
			}

			if (printLengths) {
				out.printf(":" + FormattedOutput.getInstance().getDecimalString(node.getBranchLength(), 10));
			}
		}

	}

	public static String toNewick(Tree tree, boolean printLengths,
			boolean printInternalLabels, boolean printCladeSupport) {

		StringWriter sw = new StringWriter();
		PrintWriter mp = new PrintWriter(sw);

		printNH(mp, tree, tree.getRoot(), printLengths, printInternalLabels,
				printCladeSupport);

		sw.append(';');
		return sw.toString();
	}

	/******************
	 * FinishTree **************************
	 * 
	 * Reindex nodes, count nodes to tips, and initialize components
	 *********************************************************/

	// public static void finishTree(Node p) {
	// int i;
	//
	// if (p != null) {
	// for (i = 0; i < p.numChildren; i++)
	// finishTree(p.child[i]);
	// if (p.numChildren == 0)
	// p.index = tipNodeNum++;
	// else
	// p.index = intNodeNum++;
	// }
	// }

	/*****************
	 * treeStringIsFine *******************
	 * 
	 * Checks for an error in the input tree string Prints message when there is
	 * an error in the tree and prints correct part of the tree
	 *********************************************************/

	public static boolean treeStringIsFine(String treeString) {
		int i, k;
		int numLeftPar, numRightPar;
		char previous_ch, current_ch, next_ch;
		boolean thereIsError;

		thereIsError = false;
		numLeftPar = 1;
		numRightPar = 0;
		i = 1;

		// make some general checks
		if (treeString.length() < 8) {
			System.err.println(" ");
			System.err.println("ERROR: Tree string seems to short: "
					+ treeString);
			return false;
		} else if (treeString.lastIndexOf('(') == -1) {
			JOptionPane
					.showMessageDialog(
							new JFrame(),
							"No parentheses found. It does not seem to be a valid Newick tree",
							"jModeltest error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		while (treeString.charAt(i) != ';') {
			current_ch = treeString.charAt(i);
			previous_ch = treeString.charAt(i - 1);
			next_ch = treeString.charAt(i + 1);

			if (current_ch == '(') {
				if (previous_ch != '(' && previous_ch != ',')
					thereIsError = true;
				if (next_ch != '('
						&& !Character.isLetterOrDigit(next_ch))
					thereIsError = true;
				numLeftPar++;
			} else if (current_ch == ')') {
				if (previous_ch != ')'
						&& !Character.isLetterOrDigit(previous_ch))
					thereIsError = true;
				if (next_ch != ')' && next_ch != ',' && next_ch != ':')
					thereIsError = true;
				if (next_ch == ';' && i == treeString.length() - 2)
					thereIsError = false;
				numRightPar++;
			} else if (current_ch == ',') {
				if (previous_ch != ')'
						&& !Character.isLetterOrDigit(previous_ch))
					thereIsError = true;
				if (next_ch != '('
						&& !Character.isLetterOrDigit(next_ch))
					thereIsError = true;
			} else if (current_ch == ':') {
				if (previous_ch != ')'
						&& !Character.isLetterOrDigit(previous_ch))
					thereIsError = true;
				if (!Character.isDigit(next_ch))
					thereIsError = true;
			}

			if (thereIsError) {
				System.err.println(" ");
				System.err
						.println("ERROR: There is something wrong in the tree:");
				for (k = 0; k <= i; k++)
					System.err.print(treeString.charAt(k));
				System.err.println(" <- HERE");

				return false;
			}

			i++;
		}

		if (numLeftPar != numRightPar) {
			JOptionPane.showMessageDialog(new JFrame(),
					"Tree seems unbalanced (" + numLeftPar + " left and "
							+ numRightPar + " right parentheses)"
							+ "\nIt has to be in Newick format",
					"jModeltest error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;

	}

} // end of class
