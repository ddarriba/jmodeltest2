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

import pal.io.FormattedOutput;
import pal.misc.IdGroup;
import pal.misc.Identifier;
import pal.tree.Node;
import pal.tree.NodeUtils;
import pal.tree.ReadTree;
import pal.tree.SplitSystem;
import pal.tree.SplitUtils;
import pal.tree.Tree;
import pal.tree.TreeParseException;

public class TreeUtilities {

    public static final int DEFAULT_COLUMN_WIDTH = 70;
    public static final String TREE_WEIGHT_ATTRIBUTE = "weight";
    public static final String TREE_CLADE_SUPPORT_ATTRIBUTE = "support";
    public static final String TREE_NAME_ATTRIBUTE = "treeName";

	public TreeUtilities() {
	}

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

	/**
     * Make sure subtree below node has consistent heights, i.e. node height is higher than it's descendants
     * 
     * @param tree the tree
     * @param node the node
     * 
     * @return height of node
     */
    public static double insureConsistency(Tree tree, Node node) {
        double height = TreeUtilities.safeNodeHeight(tree, node);
        if (node.isLeaf()) {
            return height;
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                Node n = node.getChild(i);
                final double childHeight = insureConsistency(tree, n);
                height = Math.max(height, childHeight);
            }
        }

        node.setNodeHeight(height);
        return height;
    }
    
    /**
     * Calculates the number of branches from node to most remote tip.
     * 
     * @param node the starting node
     * 
     * @return the node distance
     */
    public static int nodeDistance(final Node node) {
        if (node.isLeaf()) {
            return 0;
        }

        int d = 0;
        for (int i = 0; i < node.getChildCount(); i++) {
            Node n = node.getChild(i);
            d = Math.max(d, nodeDistance(n));
        }
        return d + 1;
    }
    /**
     * Calculates the safe node height.
     * 
     * @param tree the tree
     * @param node the node
     * 
     * @return the height of the node
     */
    public static double safeNodeHeight(final Tree tree, final Node node) {
        if (node.getNodeHeight() > 0.0) {
            return node.getNodeHeight();
        }
        return TreeUtilities.nodeDistance(node);
    }
    
	private static void putCharAtLevel(PrintWriter out, int level, char c,
            int[] position) {
        int n = position[level] - 1;
        for (int i = 0; i < n; i++) {
            out.print(' ');
        }
        out.print(c);
    }
	
	private static void printlnNodeWithNumberAndLabel(PrintWriter out, Node node, int level,
            int numExternalNodes, boolean[] umbrella, int[] position) {
        for (int i = 0; i < level - 1; i++) {
            if (umbrella[i]) {
                putCharAtLevel(out, i, '|', position);
            } else {
                putCharAtLevel(out, i, ' ', position);
            }
        }

        putCharAtLevel(out, level - 1, '+', position);

        int branchNumber;
        if (node.isLeaf()) {
            branchNumber = node.getNumber() + 1;
        } else {
            branchNumber = node.getNumber() + 1 + numExternalNodes;
        }

        String numberAsString = Integer.toString(branchNumber);

        int numDashs = position[level] - numberAsString.length();

        for (int i = 0; i < numDashs ; i++) {
            out.print('-');
        }
        out.print(numberAsString);

        if (node.isLeaf()) {
            out.println(" " + node.getIdentifier());
        } else {
            if (!node.getIdentifier().equals(Identifier.ANONYMOUS)) {
                out.print("(" + node.getIdentifier() + ")");
            }
            out.println();
        }
    }
	private static void printNodeInASCII(PrintWriter out, Node node, int level, int m, int maxm,
            int numExternalNodes, boolean[] umbrella, int[] position, double proportion, int minLength) {
        position[level] = (int) (node.getBranchLength() * proportion);

        if (position[level] < minLength) {
            position[level] = minLength;
        }

        if (node.isLeaf()) // external branch
        {
            if (m == maxm - 1) {
                umbrella[level - 1] = true;
            }

            printlnNodeWithNumberAndLabel(out, node, level, numExternalNodes, umbrella, position);

            if (m == 0) {
                umbrella[level - 1] = false;
            }
        } else // internal branch
        {
            for (int n = node.getChildCount() - 1; n > -1; n--) {
                printNodeInASCII(out, node.getChild(n), level + 1, n, node.getChildCount(),
                        numExternalNodes, umbrella, position, proportion, minLength);

                if (m == maxm - 1 && n == node.getChildCount() / 2) {
                    umbrella[level - 1] = true;
                }

                if (n != 0) {
                    if (n == node.getChildCount() / 2) {
                        printlnNodeWithNumberAndLabel(out, node, level, numExternalNodes, umbrella, position);
                    } else {
                        for (int i = 0; i < level + 1; i++) {
                            if (umbrella[i]) {
                                putCharAtLevel(out, i, '|', position);
                            } else {
                                putCharAtLevel(out, i, ' ', position);
                            }
                        }
                        out.println();
                    }
                }

                if (m == 0 && n == node.getChildCount() / 2) {
                    umbrella[level - 1] = false;
                }
            }
        }
    }
	
	// Print picture of current tree in ASCII
    public static void printASCII(Tree tree, PrintWriter out) {
        tree.createNodeList();

        int numExternalNodes = tree.getExternalNodeCount();
        int numInternalNodes = tree.getInternalNodeCount();
        int numBranches = numInternalNodes + numExternalNodes - 1;

        boolean[] umbrella = new boolean[numExternalNodes];
        int[] position = new int[numExternalNodes];

        int minLength = (Integer.toString(numBranches)).length() + 1;

        int MAXCOLUMN = 40;
        Node root = tree.getRoot();
        if (root.getNodeHeight() == 0.0) {
            NodeUtils.lengths2Heights(root);
        }
        double proportion = (double) MAXCOLUMN / root.getNodeHeight();

        for (int n = 0; n < numExternalNodes; n++) {
            umbrella[n] = false;
        }

        position[0] = 1;
        for (int i = root.getChildCount() - 1; i > -1; i--) {
            printNodeInASCII(out, root.getChild(i), 1, i, root.getChildCount(),
                    numExternalNodes, umbrella, position, proportion, minLength);
            if (i != 0) {
                putCharAtLevel(out, 0, '|', position);
                out.println();
            }
        }
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

	public static double getEuclideanTreeDistance(Tree t1, Tree t2) {
		double sum = 0.0;
        int numberOfInternalNodes = t1.getInternalNodeCount();
        if (numberOfInternalNodes != t2.getInternalNodeCount()) {
            throw new RuntimeException("Different number of internal nodes: " +
                    t1.getInternalNodeCount() + " vs " + t2.getInternalNodeCount());
        }
        int numberOfExternalNodes = t1.getExternalNodeCount();
        if (numberOfExternalNodes != t2.getExternalNodeCount()) {
            throw new RuntimeException("Different number of external nodes: " +
                    t1.getInternalNodeCount() + " vs " + t2.getInternalNodeCount());
        }
        for (int i = 0; i < numberOfInternalNodes; i++) {
            double bl1 = t1.getInternalNode(i).getBranchLength();
            double bl2 = t2.getInternalNode(i).getBranchLength();
            sum += (bl1 - bl2) * (bl1 - bl2);
        }
        for (int i = 0; i < numberOfExternalNodes; i++) {
            double bl1 = t1.getExternalNode(i).getBranchLength();
            double bl2 = t2.getExternalNode(i).getBranchLength();
            sum += (bl1 - bl2) * (bl1 - bl2);
        }
        return Math.sqrt(sum);
	}
	
	public static double getRobinsonFouldsTreeDistance(Tree t1, Tree t2) {
		SplitSystem s1 = SplitUtils.getSplits(t1);
		IdGroup idGroup = s1.getIdGroup();
		SplitSystem s2 = SplitUtils.getSplits(idGroup, t2);

		if (s1.getLabelCount() != s2.getLabelCount())
			throw new IllegalArgumentException("Number of labels must be the same!");

		int ns1 = s1.getSplitCount();
		int ns2 = s2.getSplitCount();

		// number of splits in t1 missing in t2
		int fn = 0;
		for (int i = 0; i < ns1; i++)
		{
			if (!s2.hasSplit(s1.getSplit(i))) fn++;
		}

		// number of splits in t2 missing in t1
		int fp = 0;
		for (int i = 0; i < ns2; i++)
		{
			if (!s1.hasSplit(s2.getSplit(i))) fp++;
		}


		return ((double) fp + (double) fn);
	}
} // end of class
