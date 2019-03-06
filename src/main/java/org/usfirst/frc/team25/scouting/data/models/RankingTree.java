package org.usfirst.frc.team25.scouting.data.models;

import org.usfirst.frc.team25.scouting.data.SortersFilters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class representation of a tree with multiple nodes, each having a level
 *
 * @author sng
 */
public class RankingTree {

    private HashMap<Integer, Integer> ranks;
    private int maxLevel = 0;

    public RankingTree() {
        this.ranks = new HashMap<>();

    }

    //Initializes a RankingTree with the structure of an old one
    public RankingTree(HashMap<Integer, Integer> ranks) {
        this.ranks = ranks;

    }

    /**
     * Initializes a RankingTree in which the each element of teamOrder is a numbered node,
     * where the first element has the highest level and the last element has the lowest
     *
     * @param teamOrder An ArrayList that determines the levels and nodes of a new RankingTree
     */
    public RankingTree(ArrayList<Integer> teamOrder) {
        this.ranks = new HashMap<>();
        for (int i = 0; i < teamOrder.size(); i++) {
            ranks.put(teamOrder.get(i), teamOrder.size() - i);
        }

    }

    /**
     * @return The number of nodes currently in the tree
     */
    public int getNumberOfNodes() {
        return ranks.keySet().size();
    }

    /**
     * @return A deep copy of the HashMap that is the foundation of the tree
     * Useful for creating a duplicate tree for backup.
     */
    public HashMap<Integer, Integer> getTreeHashMap() {
        return (HashMap<Integer, Integer>) ranks.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    }

    /**
     * Creates a new node at level 0
     *
     * @param teamNum Label of the new node
     */
    public void addNode(int teamNum) {
        if (!containsNode(teamNum)) {
            ranks.put(teamNum, 0);
        }
    }

    /**
     * @param teamNum The node label to be queried
     * @return true if there is a node in the tree labeled with teamNum, false otherwise
     */
    public boolean containsNode(int teamNum) {
        return this.ranks.containsKey(teamNum);
    }

    /**
     * Creates a new node at the specified level
     *
     * @param teamNum Label of the new node
     * @param level   Level of the new node
     */
    public void addNode(int teamNum, int level) {
        if (!containsNode(teamNum)) {
            ranks.put(teamNum, 0);
            setLevel(teamNum, level);
        }
    }

    /**
     * @param teamNum Node label of the node whose level is being changed
     * @param level   The desired level of the node
     */
    private void setLevel(int teamNum, int level) {
        if (level < 0) {
            level = 0;
        }
        this.ranks.put(teamNum, level);
        if (level > this.maxLevel) {
            this.maxLevel = level;
        }

    }

    /**
     * Creates a new node at the same level of the old one
     *
     * @param newNodeNum Label of the new node
     * @param oldNode    Node that the new node should be created alongside
     * @throws Exception When oldNode does not exist in the tree
     */
    public void addNodeAlongside(int newNodeNum, int oldNode) throws Exception {
        if (!containsNode(newNodeNum)) {
            ranks.put(newNodeNum, getLevel(oldNode));
        }
    }

    /**
     * Creates a new node at the one level above the old one
     *
     * @param newNodeNum Label of the new node
     * @param oldNode    Node that the new node should be created above
     * @throws Exception When oldNode does not exist in the tree
     */
    public void addNodeAbove(int newNodeNum, int oldNode) throws Exception {
        if (!containsNode(newNodeNum)) {
            addNodeAlongside(newNodeNum, oldNode);
            promote(newNodeNum);
        }

    }

    /**
     * Creates a new node one level below the old one
     *
     * @param newNodeNum Label of the new node
     * @param oldNode    Node that the new node should be created below
     * @throws Exception When oldNode does not exist in the tree
     */
    public void addNodeBelow(int newNodeNum, int oldNode) throws Exception {
        if (!containsNode(newNodeNum)) {
            addNodeAlongside(newNodeNum, oldNode);
            demote(newNodeNum);
        }

    }

    /**
     * @return A sorted (descending) string representation of the current tree, with
     * a node on each line, followed by a comma and its level
     */
    public String toString() {
        ranks = SortersFilters.sortByComparator(ranks, false);
        StringBuilder result = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : ranks.entrySet()) {
            try {
                result.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    /**
     * @return A sorted (descending) ArrayList representation of the current tree,
     * with the label of each node as an element of the list.
     * Nodes with the same level are sorted randomly.
     */
    public ArrayList<Integer> toArrayList() {
        ArrayList<Integer> result = new ArrayList<>();
        ranks = SortersFilters.sortByComparator(ranks, false);
        for (Map.Entry<Integer, Integer> entry : ranks.entrySet()) {
            try {
                result.add(entry.getKey());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Determines the percentage of comparisons in a list that are being followed in the tree.
     * Ignores comparisons in which either team does not have a node in the tree
     *
     * @param comparisons List of comparisons to be evaluated
     * @return Compliance percentage: comparisons followed/valid comparisons * 100
     */
    public double getCompliancePercent(ArrayList<Comparison> comparisons) {
        double compliant = 0.0;
        int validComparisons = 0;

        for (Comparison comparison : comparisons) {
            try {
                if (isComparisonCompliant(comparison)) {
                    compliant++;
                }
                validComparisons++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return compliant / validComparisons * 100;
    }

    /**
     * Determines if a given comparison (e.g. A>B) is followed in the tree
     *
     * @param comparison Comparison to be queried
     * @return true if the comparison is followed in the tree, false otherwise
     * @throws Exception if either team involved in the comparison does not have a node in the tree
     */
    public boolean isComparisonCompliant(Comparison comparison) throws Exception {
        if (comparison.getBetterTeam() == 0) {
            return getLevel(comparison.getHigherTeam()) == getLevel(comparison.getLowerTeam());
        } else {
            return getLevel(comparison.getBetterTeam()) > getLevel(comparison.getWorseTeam());
        }
    }

    /**
     * @param teamNum Node label to be queried
     * @return The level of the specified node
     * @throws Exception if the node labeled by teamNum does not exist
     */
    public int getLevel(int teamNum) throws Exception {
        if (!containsNode(teamNum)) {
            throw new Exception("Invalid level request for" + teamNum);
        }
        return this.ranks.get(teamNum);
    }

    /**
     * @return The maximum level of among all nodes in the tree
     */
    public int getMaxLevel() {
        return this.maxLevel;
    }

    /**
     * Increments the level of the given node by 1
     *
     * @param teamNum The node to be promoted
     * @throws Exception if the node does not exist
     */
    public void promote(int teamNum) throws Exception {
        setLevel(teamNum, getLevel(teamNum) + 1);
    }

    /**
     * Decrements the level of the given node by 1,
     * or increments the level of all nodes above it if it is level 0 currently
     *
     * @param teamNum The node to be demoted
     * @throws Exception if the node does not exist
     */
    public void demote(int teamNum) throws Exception {
        if (getLevel(teamNum) == 0) { //lowest level is 0
            for (int key : ranks.keySet()) {
                if (key != teamNum) {
                    promote(key);
                }
            }
        }

        setLevel(teamNum, getLevel(teamNum) - 1);

    }

}
