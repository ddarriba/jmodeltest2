/*
Copyright (C) 2009  Diego Darriba

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import pal.misc.IdGroup;
import pal.tree.Node;
import pal.tree.NodeFactory;
import pal.tree.SimpleTree;
import pal.tree.Tree;
import es.uvigo.darwin.jmodeltest.exception.InternalException;
import es.uvigo.darwin.jmodeltest.model.Model;
import es.uvigo.darwin.jmodeltest.selection.InformationCriterion;
import es.uvigo.darwin.jmodeltest.utilities.FixedBitSet;
import es.uvigo.darwin.jmodeltest.utilities.MyFormattedOutput;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

/**
 * Phylogenetic consensus tree builder.
 * 
 * @author Diego Darriba
 * @since 3.0
 */
public class Consensus {

    /** Display branch suport as percent. */
    final static public boolean SUPPORT_AS_PERCENT = false;
    /** Calculate branch lengths as weighted average. */
    final static public int BRANCH_LENGTHS_AVERAGE = 1;
    /** Calculate branch lengths as weighted median. */
    final static public int BRANCH_LENGTHS_MEDIAN = 2;
    /** Default branch lengths algorithm */
    private static final BranchDistances DEFAULT_BRANCH_DISTANCES =
            BranchDistances.WeightedMedian;
    /** The Constant FIRST (just for source code visibility). */
    private static final int FIRST = 0;
    /** The weighted trees in consensus. */
    private List<WeightedTree> trees;
    /** The cummulative weight. */
    private double cumWeight = 0.0;
    /** The number of taxa. */
    private int numTaxa;
    /** The common id group of the tree set. */
    private IdGroup idGroup;
    /** The set of clade supports. */
    private Map<FixedBitSet, Support> support =
            new HashMap<FixedBitSet, Support>();
    /** The set of clade supports to get from outside this class. */
    private Map<FixedBitSet, Double> cladeSupport;
    /** The inner consensus tree. */
    private Tree consensusTree;
    /** The splits included in consensus tree */
    private List<FixedBitSet> splitsInConsensus = new ArrayList<FixedBitSet>();
    /** The splits not included in consensus tree */
    private List<FixedBitSet> splitsOutFromConsensus = new ArrayList<FixedBitSet>();

    /**
     * Gets the clade support, with Support instances
     * 
     * @return the map of the support for each bitSet
     */
    private Map<FixedBitSet, Support> getSupport() {
        return support;
    }

    /**
     * Gets the double precision clade support
     * 
     * @return the map of the support for each bitSet
     */
    public Map<FixedBitSet, Double> getCladeSupport() {

        if (cladeSupport == null) {
            cladeSupport = new HashMap<FixedBitSet, Double>(support.size());
            FixedBitSet[] keys = support.keySet().toArray(new FixedBitSet[0]);
            Arrays.sort(keys);

            for (FixedBitSet fbs : keys) {
                cladeSupport.put(fbs, support.get(fbs).treesWeightWithClade / cumWeight);
            }
        }
        return cladeSupport;
    }

    /**
     * Gets the Id Group of the set of trees
     * 
     * @return the id group
     */
    public IdGroup getIdGroup() {
        return idGroup;
    }

    /**
     * Gets the consensus tree
     * 
     * @return the consensus tree
     */
    public Tree getConsensusTree() {
        return consensusTree;
    }

    /**
     * Gets the set of trees included in the consensus.
     * 
     * @return the trees
     */
    public Collection<WeightedTree> getTrees() {
        return trees;
    }

    /**
     * Adds a weighted tree to the set.
     * 
     * @param wTree the weighted tree
     * 
     * @return true, if successful
     */
    private boolean addTree(WeightedTree wTree) {
        //check integrity
        if (wTree.getTree() == null || wTree.getWeight() < 0.0) {
            throw new InternalException();
        }
        //check compatibility
        if (trees.isEmpty()) {
            trees.add(wTree);
            numTaxa = wTree.getTree().getIdCount();
            idGroup = pal.tree.TreeUtils.getLeafIdGroup(wTree.getTree());
        } else {
            if (wTree.getTree().getIdCount() != numTaxa) {
                return false;
            }
            Tree pTree = trees.get(FIRST).getTree();
            for (int i = 0; i < numTaxa; i++) {
                boolean found = false;
                for (int j = 0; j < numTaxa; j++) {
                    if (wTree.getTree().getIdentifier(i).equals(pTree.getIdentifier(j))) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.out.println("NOT COMPATIBLE TREES");
                    return false;
                }
            }
            trees.add(wTree);
        }
        cumWeight += wTree.getWeight();
        return true;
    }

    /**
     * Instantiates a new consensus tree builder.
     * 
     * @param ic the information criterion to build the weighted trees
     * @param supportThreshold the minimum support for a clade
     */
    public Consensus(InformationCriterion ic, double supportThreshold) {
        this(ic, supportThreshold, 0);
    }

    /**
     * Instantiates a new consensus tree builder.
     * 
     * @param ic the information criterion to build the weighted trees
     * @param supportThreshold the minimum support for a clade
     * @param branchDistances the method to get the consensus branch lengths
     */
    public Consensus(InformationCriterion ic, double supportThreshold, int branchDistances) {
        this.trees = new ArrayList<WeightedTree>();
        for (Model model : ic.getConfidenceModels()) {
        	WeightedTree wTree = new WeightedTree(
                                       model.getTree(),
                                       ic.getWeight(model));
            this.addTree(wTree);
        }
        consensusTree = buildTree(supportThreshold, getBranchDistances(branchDistances));
    }

    /**
     * Instantiates a new unweighted consensus builder.
     *
     * @param trees the trees
     * @param supportThreshold the minimum support for a clade
     * @param branchDistances the method to get the consensus branch lengths
     */
    public Consensus(List<WeightedTree> trees, double supportThreshold, int branchDistances) {
        this.trees = new ArrayList<WeightedTree>();
        for (WeightedTree tree : trees) {
            this.addTree(tree);
        }

        consensusTree = buildTree(supportThreshold, getBranchDistances(branchDistances));
    }

    /**
     * Calculates rooted support.
     * 
     * @param wTree the weighted tree instance
     * @param node the node
     * @param support the support
     * 
     * @return the fixed bit set
     */
    private FixedBitSet rootedSupport(WeightedTree wTree, Node node, Map<FixedBitSet, Support> support) {
        FixedBitSet clade = new FixedBitSet(numTaxa);
        if (node.isLeaf()) {
            clade.set(idGroup.whichIdNumber(node.getIdentifier().getName()));
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                Node n = node.getChild(i);
                FixedBitSet childClade = rootedSupport(wTree, n, support);
                clade.union(childClade);
            }
        }

        Support s = support.get(clade);
        if (s == null) {
            s = new Support();
            support.put(clade, s);
        }
        s.add(wTree.getWeight(), TreeUtilities.safeNodeHeight(wTree.getTree(), node), node.getBranchLength());
        return clade;
    }

    /**
     * Detach the children of a tree.
     * 
     * @param tree the tree
     * @param node the node to detach
     * @param split the split
     * 
     * @return the node
     */
    public Node detachChildren(Tree tree, Node node, List<Integer> split) {
        assert (split.size() > 1);

        List<Node> detached = new ArrayList<Node>();

        for (int n : split) {
            detached.add(node.getChild(n));
        }

        Node saveRoot = tree.getRoot();

        List<Integer> toRemove = new ArrayList<Integer>();
        for (int i = 0; i < node.getChildCount(); i++) {
            Node n = node.getChild(i);
            if (detached.contains(n)) {
                toRemove.add(0, i);
            }
        }
        for (int i : toRemove) {
            node.removeChild(i);
        }

        Node dnode = NodeFactory.createNode(detached.toArray(new Node[0]));
        node.addChild(dnode);

        tree.setRoot(saveRoot);

        return dnode;
    }

    /**
     * Builds the consensus tree over a set of weighted trees.
     * 
     * @param supportThreshold the minimum support to consider a split into the consensus tree
     * 
     * @return the consensus tree
     */
    private Tree buildTree(double supportThreshold, BranchDistances branchDistances) {

        if (trees.isEmpty()) {
            throw new InternalException("There are no trees to consense");
        }

        if (supportThreshold < 0.5 || supportThreshold > 1.0) {
            throw new InternalException("Invalid threshold value: " + supportThreshold);
        }
        
        double effectiveThreshold = supportThreshold;
        if (supportThreshold == 0.5) {
            effectiveThreshold += 1.0/(numTaxa+1);
        } else if (supportThreshold == 1.0) {
            effectiveThreshold -= 1.0/(numTaxa+1);
        }

        // establish support
        support = new HashMap<FixedBitSet, Support>();
        for (WeightedTree wTree : trees) {
            rootedSupport(wTree, wTree.getTree().getRoot(), support);
        }

        Tree cons = new SimpleTree();

        // Contains all internal nodes in the tree so far, ordered so descendants
        // appear later than ancestors
        List<Node> internalNodes = new ArrayList<Node>(numTaxa);

        // For each internal node, a bit-set with the complete set of tips for it's clade
        List<FixedBitSet> internalNodesTips = new ArrayList<FixedBitSet>(numTaxa);
        assert idGroup.getIdCount() == numTaxa;

        // establish a tree with one root having all tips as descendants
        internalNodesTips.add(new FixedBitSet(numTaxa));
        FixedBitSet rooNode = internalNodesTips.get(0);
        Node[] nodes = new Node[numTaxa];
        for (int nt = 0; nt < numTaxa; ++nt) {
            nodes[nt] = NodeFactory.createNode(idGroup.getIdentifier(nt));
            rooNode.set(nt);
        }

        Node rootNode = NodeFactory.createNode(nodes);
        internalNodes.add(rootNode);
        cons.setRoot(rootNode);
        // sorts support from largest to smallest
        final Comparator<Map.Entry<FixedBitSet, Support>> comparator = new Comparator<Map.Entry<FixedBitSet, Support>>() {

            @Override
            public int compare(Map.Entry<FixedBitSet, Support> o1, Map.Entry<FixedBitSet, Support> o2) {
                double diff = o2.getValue().treesWeightWithClade - o1.getValue().treesWeightWithClade;
                if (diff > 0.0) {
                    return 1;
                } else if (diff < 0.0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };

        // add everything to queue
        PriorityQueue<Map.Entry<FixedBitSet, Support>> queue =
                new PriorityQueue<Map.Entry<FixedBitSet, Support>>(support.size(), comparator);

        for (Map.Entry<FixedBitSet, Support> se : support.entrySet()) {
            Support s = se.getValue();
            FixedBitSet clade = se.getKey();
            final int cladeSize = clade.cardinality();
            if (cladeSize == numTaxa) {
                // root
                cons.getRoot().setNodeHeight(s.sumBranches / trees.size());
                cons.getRoot().setBranchLength(branchDistances.build(s.branchLengths));
                continue;
            }

            if (Math.abs(s.treesWeightWithClade - this.cumWeight) < 1e-5 && cladeSize == 1) {
                // leaf/external node
                final int nt = clade.nextOnBit(FIRST);
                final Node leaf = cons.getExternalNode(nt);
                leaf.setNodeHeight(s.sumBranches / trees.size());
                leaf.setBranchLength(branchDistances.build(s.branchLengths));
            } else {
                queue.add(se);
            }
        }

        while (queue.peek() != null) {
            Map.Entry<FixedBitSet, Support> e = queue.poll();
            final Support s = e.getValue();

            final double psupport = (1.0 * s.treesWeightWithClade) / cumWeight;
            if (psupport < effectiveThreshold) {
                break;
            }

            final FixedBitSet cladeTips = e.getKey();

            boolean found = false;

            /* locate the node containing the clade. going in reverse order 
            ensures the lowest one is hit first */
            for (int nsub = internalNodesTips.size() - 1; nsub >= 0; --nsub) {

                FixedBitSet allNodeTips = internalNodesTips.get(nsub);

                // size of intersection between tips & split
                final int nSplit = allNodeTips.intersectCardinality(cladeTips);

                if (nSplit == cladeTips.cardinality()) {
                    // node contains all of clade

                    // Locate node descendants containing the split
                    found = true;
                    List<Integer> split = new ArrayList<Integer>();

                    Node n = internalNodes.get(nsub);
                    int l = 0;

                    for (int j = 0; j < n.getChildCount(); j++) {
                        Node ch = n.getChild(j);

                        if (ch.isLeaf()) {
                            if (cladeTips.contains(idGroup.whichIdNumber(ch.getIdentifier().getName()))) {
                                split.add(l);
                            }
                        } else {
                            // internal
                            final int o = internalNodes.indexOf(ch);
                            final int i = internalNodesTips.get(o).intersectCardinality(cladeTips);
                            if (i == internalNodesTips.get(o).cardinality()) {
                                split.add(l);
                            } else if (i > 0) {
                                // Non compatible
                                found = false;
                                break;
                            }
                        }
                        ++l;
                    }


                    if (!(found && split.size() < n.getChildCount())) {
                        found = false;
                        break;
                    }

                    if (split.isEmpty()) {
                        System.err.println("Bug??");
                        assert (false);
                    }

                    final Node detached = detachChildren(cons, n, split);

                    final double height = s.sumBranches / s.nTreesWithClade;
                    detached.setNodeHeight(height);
                    detached.setBranchLength(branchDistances.build(s.branchLengths));

                    cons.setAttribute(detached, TreeUtilities.TREE_CLADE_SUPPORT_ATTRIBUTE, SUPPORT_AS_PERCENT ? 100 * psupport : psupport);

                    // insert just after parent, so before any descendants
                    internalNodes.add(nsub + 1, detached);
                    internalNodesTips.add(nsub + 1, new FixedBitSet(cladeTips));

                    break;
                }
            }
        }

        TreeUtilities.insureConsistency(cons, cons.getRoot());

        String thresholdAsPercent = String.valueOf(supportThreshold * 100);
        cons.setAttribute(cons.getRoot(), TreeUtilities.TREE_NAME_ATTRIBUTE,
                "cons_" + thresholdAsPercent + "_majRule");

        Set<FixedBitSet> keySet = getSupport().keySet();
        FixedBitSet[] keys = keySet.toArray(new FixedBitSet[0]);
        Arrays.sort(keys);

        for (FixedBitSet fbs : keys) {
            if (fbs.cardinality() > 1) {
                double psupport = (1.0 * getSupport().get(fbs).getTreesWeightWithClade()) / cumWeight;
                if (psupport < effectiveThreshold) {
                    splitsOutFromConsensus.add(fbs);
                } else {
                    splitsInConsensus.add(fbs);
                }
            }
        }

        return cons;

    }

    /**
     * Enum to calculate the branch lengths
     */
    private enum BranchDistances {

        WeightedAverage {

            /**
             * Calculates the weighted average.
             * 
             * @param values the weighted values
             * @param cumWeight the sum of weights
             * 
             * @return the weighted average of the set
             */
            @Override
            public double build(List<WeightLengthPair> values) {
                double avg = 0.0;
                double cumWeight = 0.0;
                for (WeightLengthPair pair : values) {
                    avg += pair.branchLength * pair.weight;
                    cumWeight += pair.weight;
                }
                avg /= cumWeight;
                return avg;
            }
        },
        WeightedMedian {

            /**
             * Calculates the weighted median.
             * 
             * @param values the weighted values
             * @param cumWeight the sum of weights
             * 
             * @return the weighted median of the set
             */
            @Override
            public double build(List<WeightLengthPair> values) {
                Collections.sort(values);
                double median = -1;
                double cumWeight = 0.0;
                for (WeightLengthPair pair : values) {
                    cumWeight += pair.weight;
                }
                double halfWeight = cumWeight / 2.0;
                double cumValue = 0.0;
                for (WeightLengthPair pair : values) {
                    cumValue += pair.weight;
                    if (cumValue >= halfWeight) {
                        median = pair.branchLength;
                        break;
                    }
                }
                return median;
            }
        };

        public abstract double build(List<WeightLengthPair> values);
    }

    /**
     * One clade support.
     */
    static final class Support {

        /** number of trees containing the clade. */
        private int nTreesWithClade;
        /** The trees weight with clade. */
        private double treesWeightWithClade;
        /** The branch lengths. */
        private ArrayList<WeightLengthPair> branchLengths;
        /** Sum of node heights of trees containing the clade. */
        private double sumBranches;

        public double getTreesWeightWithClade() {
            return treesWeightWithClade;
        }

        /**
         * Instantiates a new support.
         */
        Support() {
            sumBranches = 0.0;
            treesWeightWithClade = 0.0;
            nTreesWithClade = 0;
            branchLengths = new ArrayList<WeightLengthPair>();
        }

        /**
         * Adds the branch to the map of branch lengths.
         * 
         * @param weight the weight
         * @param height the height
         * @param branchLength the branch length
         */
        public final void add(double weight, double height, double branchLength) {
            sumBranches += height;
            branchLengths.add(new WeightLengthPair(weight, branchLength));
            treesWeightWithClade += weight;
            ++nTreesWithClade;

//            double testW = 0.0;
//            for (WeightLengthPair wlp : branchLengths) {
//                testW += wlp.weight;
//            }
        }
    }

    static class WeightLengthPair implements Comparable<WeightLengthPair> {

        private double weight;
        private double branchLength;

        WeightLengthPair(double weight, double branchLength) {
            this.weight = weight;
            this.branchLength = branchLength;
        }

        @Override
        public int compareTo(WeightLengthPair o) {
            if (branchLength < o.branchLength) {
                return -1;
            } else if (branchLength > o.branchLength) {
                return 1;
            }
            return 0;
        }
    }

    /**
     * A extension of Weighted tree but every tree
     * has the same weight.
     */
    static class UnweightedTree extends WeightedTree {

        /**
         * Instantiates a new unweighted tree.
         * 
         * @param tree the tree
         */
        UnweightedTree(Tree tree) {
            super(tree, 1.0);
        }
    }

    public String getTaxaHeader() {
        StringBuilder taxaHeader = new StringBuilder();
        for (int i = 0; i < numTaxa; i++) {
            taxaHeader.append(String.valueOf(i + 1).charAt(0));
        }
        if (numTaxa >= 10) {
            taxaHeader.append('\n');
            taxaHeader.append(MyFormattedOutput.space(4 + 9, ' '));
            for (int i = 9; i < numTaxa; i++) {
                taxaHeader.append(String.valueOf(i + 1).charAt(1));
            }
        }
        if (numTaxa >= 100) {
            taxaHeader.append('\n');
            taxaHeader.append(MyFormattedOutput.space(4 + 99, ' '));
            for (int i = 99; i < numTaxa; i++) {
                taxaHeader.append(String.valueOf(i + 1).charAt(2));
            }
        }
        if (numTaxa >= 1000) {
            taxaHeader.append('\n');
            taxaHeader.append(MyFormattedOutput.space(4 + 999, ' '));
            for (int i = 999; i < numTaxa; i++) {
                taxaHeader.append(String.valueOf(i + 1).charAt(3));
            }
        }

        return taxaHeader.toString();
    }

    public String getSetsIncluded() {
        StringBuilder setsIncluded = new StringBuilder();
        setsIncluded.append("    ");
        setsIncluded.append(getTaxaHeader());

        setsIncluded.append('\n');
        for (FixedBitSet fbs : splitsInConsensus) {
            setsIncluded.append("    ")
                    .append(fbs.splitRepresentation())
                    .append(" ( ")
                    .append(Utilities.roundDoubleTo(getCladeSupport().get(fbs), 5))
                    .append(" )")
                    .append('\n');
        }
        return setsIncluded.toString();
    }

    public String getSetsNotIncluded() {
        StringBuilder setsIncluded = new StringBuilder();
        setsIncluded.append("    ");
        setsIncluded.append(getTaxaHeader());

        setsIncluded.append('\n');
        for (FixedBitSet fbs : splitsOutFromConsensus) {
            setsIncluded.append("    ")
                    .append(fbs.splitRepresentation())
                    .append(" ( ")
                    .append(Utilities.roundDoubleTo(getCladeSupport().get(fbs), 5))
                    .append(" )")
                    .append('\n');
        }
        return setsIncluded.toString();
    }

    private BranchDistances getBranchDistances(int value) {
        BranchDistances bd;
        switch (value) {
            case BRANCH_LENGTHS_AVERAGE:
                bd = BranchDistances.WeightedAverage;
                break;
            case BRANCH_LENGTHS_MEDIAN:
                bd = BranchDistances.WeightedMedian;
                break;
            default:
                // Weighted average
                bd = DEFAULT_BRANCH_DISTANCES;
        }
        return bd;
    }
}
