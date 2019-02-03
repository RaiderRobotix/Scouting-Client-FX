package org.usfirst.frc.team25.scouting.data;

import org.usfirst.frc.team25.scouting.data.models.Comparison;
import org.usfirst.frc.team25.scouting.data.models.RankingTree;
import org.usfirst.frc.team25.scouting.data.models.ScoutEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class PicklistGenerator {

    /**
     * An ArrayList containing all scout entries, deserialized from JSON files
     */
    private final ArrayList<ScoutEntry> scoutEntries;

    /**
     * A processed list of comparisons, where strict contradictions (A>B and B<A) and equalities (A=B)
     * are removed. However, transitive contradictions (A>B, B>C, but B>A) may exist
     * TODO Find a way to resolve transitive contradictions, without removing a lot of data
     * TODO Find a way to deal with equalities in an elegant way
     */
    private final ArrayList<Comparison> comparisons;

    /**
     * An unsorted list of all team numbers in a given competition
     */
    private final ArrayList<Integer> teamNums;

    /**
     * A HashMap of team numbers as keys and their associated comparisons as values
     * for efficient and easy lookup by methods
     * e.g. C1 = A<B, C2 = B>C. The HashMap would be {A: [C1], B: [C1, C2], C: [C2]}
     */
    private final HashMap<Integer, ArrayList<Comparison>> compLookup;

    /**
     * The directory to output the generated picklists
     */
    private final File outputDirectory;
    private String eventName;

    public PicklistGenerator(ArrayList<ScoutEntry> scoutEntries, File outputDirectory, String eventName) {
        this.scoutEntries = scoutEntries;
        this.outputDirectory = outputDirectory;

        comparisons = new ArrayList<Comparison>();
        teamNums = new ArrayList<Integer>();
        compLookup = new HashMap<>();

        this.eventName = eventName;

        //Create list of all comparisons from the scout entries
        for (ScoutEntry entry : scoutEntries) {

            int t1 = entry.getPostMatch().getTeamOneCompare(), t2 = entry.getPostMatch().getTeamTwoCompare();
            String comparison = entry.getPostMatch().getComparison();

            if (t1 != 0 && t2 != 0 && t1 != t2) { //At the beginning of a shift, one team may be set to 0
                Comparison currentComp = new Comparison(t1, t2, comparison);
                comparisons.add(currentComp);

                //Initialize the ArrayLists inside the HashMap
                if (!compLookup.containsKey(t1)) {
                    compLookup.put(t1, new ArrayList<>());
                }
                if (!compLookup.containsKey(t2)) {
                    compLookup.put(t2, new ArrayList<>());
                }
            }

            //Generate a list of team numbers
            if (!teamNums.contains(t1) && t1 != 0) {
                teamNums.add(t1);
            }
        }

        //Create a second comparison list to iterate through; you can't modify an array list
        // that you're currently iterating through
        ArrayList<Comparison> dupComparisonList = (ArrayList<Comparison>) comparisons.clone();

        //Remove contradictions
        for (Comparison comp : dupComparisonList) {

            //Assume that it's a bad comparison at first, so we remove it
            comparisons.remove(comp);
            boolean isContradiction = false;

            //"Nullify" contradictions by removing a comparison where A>B, then another where A<B
            //Note that if A>B appears twice and A<B appears once, the result is still A>B
            for (Comparison comp2 : dupComparisonList) {
                if (comp.contradicts(comp2)) {
                    isContradiction = true;
                    comparisons.remove(comp2);
                }
            }

            //If the better team is 0, the two teams are equal; we don't want that in our picklist,
            //as we don't want to make how "close" the two teams are on the list a factor (too complicated)
            if (!isContradiction && comp.getBetterTeam() != 0) {
                comparisons.add(comp);
                compLookup.get(comp.getLowerTeam()).add(comp);
                compLookup.get(comp.getHigherTeam()).add(comp);
            }
        }

        //That's it for processing the scout entries; the rest of the methods in the class
        //will make use of the ArrayLists and HashMaps we created
    }

    public static String generateComparisonMatrix(ArrayList<Integer> teamList,
                                                  HashMap<String, Integer> comparisonTable) {
        Collections.sort(teamList);
        StringBuilder compareMatrix = new StringBuilder(",");
        for (int i = 0; i < teamList.size(); i++) {
            compareMatrix.append(teamList.get(i)).append(",");
        }
        for (int i = 0; i < teamList.size(); i++) {
            compareMatrix.append("\n").append(teamList.get(i)).append(",");
            for (int j = 0; j < teamList.size(); j++) {
                String key = getTeamTupleString(teamList.get(i), teamList.get(j));
                if (!comparisonTable.containsKey(key)) {
                    compareMatrix.append("-,");
                } else {
                    compareMatrix.append(comparisonTable.get(key)).append(",");
                }
            }
        }
        return compareMatrix.toString();
    }

    private static String getTeamTupleString(int team1, int team2) {
        if (team1 > team2) {
            return "(" + team2 + ", " + team1 + ")";
        }
        return "(" + team1 + ", " + team2 + ")";
    }

    /**
     * Generates a picklist using a pseudo depth-first topological sort, starting from
     * the comparisons of one team and moving on to the nodes above/below it.
     * Currently not very accurate or fast and results should NOT be used for
     * creating an actual picklist.
     * TODO Improve upon this method, possibly creating a way to insert new nodes BETWEEN existing ones
     * e.g. If C is level 0, A is level 1, and A>B>C, we shouldn't force B to be either level 0 or 1,
     * but have B be level 1 and promote all current nodes on level 1.
     */
    public void generateTopologicalSortList() {
        RankingTree tree = new RankingTree();

        //Start creating tree with a random team
        int targetTeam = teamNums.get(0);
        tree.addNode(targetTeam);

        //Rebuild comparison list from the beginning, so compliance percent can start at 100
        ArrayList<Comparison> comparisons = new ArrayList<Comparison>();

        for (int i = 0; i < teamNums.size(); i++) {
            targetTeam = teamNums.get(i);

            //Ignore starter nodes that have not yet been created
            if (!tree.containsNode(targetTeam)) {
                continue;
            }

            //Iterate through all comparisons involving a single team first
            for (Comparison comp : compLookup.get(targetTeam)) {

                //Rebuild comparison list
                if (!comparisons.contains(comp) && comp.getBetterTeam() != 0) {
                    comparisons.add(comp);
                }

                //comp.printString(); //used for debugging
                try {
                    if (targetTeam == comp.getBetterTeam()) {
                        if (!tree.containsNode(comp.getWorseTeam())) {
                            tree.addNodeBelow(comp.getWorseTeam(), targetTeam);
                            i = 0; //Reset index to ensure changes are compliant with old nodes
                        }
                        //if the comparison is compliant, don't modify the tree
                        else if (!tree.isComparisonCompliant(comp)) {
                            HashMap<Integer, Integer> originalTree = tree.getTreeHashMap();
                            double bestCompliancePercent = tree.getCompliancePercent(comparisons);
                            RankingTree bestTree = new RankingTree(originalTree);

                            //try to demote worse team first
                            while (!tree.isComparisonCompliant(comp) || tree.getLevel(comp.getWorseTeam()) > 0) {
                                tree.demote(comp.getWorseTeam());
                                double currentCompPer = tree.getCompliancePercent(comparisons);
                                if (currentCompPer > bestCompliancePercent) {
                                    bestCompliancePercent = currentCompPer;
                                    bestTree = new RankingTree(bestTree.getTreeHashMap());
                                }
                            }

                            tree = new RankingTree(originalTree);

                            //try to promote better team afterward
                            while (!tree.isComparisonCompliant(comp) || tree.getLevel(comp.getBetterTeam()) < tree.getMaxLevel()) {
                                tree.promote(comp.getBetterTeam());
                                double currentCompPer = tree.getCompliancePercent(comparisons);
                                if (currentCompPer > bestCompliancePercent) {
                                    bestCompliancePercent = currentCompPer;
                                    bestTree = new RankingTree(bestTree.getTreeHashMap());
                                }
                            }

                            tree = new RankingTree(bestTree.getTreeHashMap());

                        }
                    } else if (targetTeam == comp.getWorseTeam()) {
                        if (!tree.containsNode(comp.getBetterTeam())) {
                            tree.addNodeAbove(comp.getBetterTeam(), targetTeam);
                            i = 0;
                        }


                        //if the comparison is compliant, don't modify the tree
                        else if (!tree.isComparisonCompliant(comp)) {
                            HashMap<Integer, Integer> originalTree = tree.getTreeHashMap();
                            double bestCompliancePercent = tree.getCompliancePercent(comparisons);
                            RankingTree bestTree = new RankingTree(originalTree);

                            //try to demote worse team first
                            while (!tree.isComparisonCompliant(comp) || tree.getLevel(comp.getWorseTeam()) > 0) {
                                tree.demote(comp.getWorseTeam());
                                double currentCompPer = tree.getCompliancePercent(comparisons);
                                if (currentCompPer > bestCompliancePercent) {
                                    bestCompliancePercent = currentCompPer;
                                    bestTree = new RankingTree(bestTree.getTreeHashMap());
                                }
                            }

                            tree = new RankingTree(originalTree);

                            //try to promote better team afterward
                            while (!tree.isComparisonCompliant(comp) || tree.getLevel(comp.getBetterTeam()) < tree.getMaxLevel()) {
                                tree.promote(comp.getBetterTeam());
                                double currentCompPer = tree.getCompliancePercent(comparisons);
                                if (currentCompPer > bestCompliancePercent) {
                                    bestCompliancePercent = currentCompPer;
                                    bestTree = new RankingTree(bestTree.getTreeHashMap());
                                }
                            }

                            tree = new RankingTree(bestTree.getTreeHashMap());
                        }

                    } else {
                        int secondTeam = comp.getHigherTeam();
                        if (comp.getLowerTeam() != targetTeam) {
                            secondTeam = comp.getLowerTeam();
                        }

                        if (!tree.containsNode(secondTeam)) {
                            tree.addNodeAlongside(secondTeam, targetTeam);
                            i = 0;
                        } else if (!tree.isComparisonCompliant(comp)) {
                            HashMap<Integer, Integer> originalTree = tree.getTreeHashMap();
                            double bestCompliancePercent = tree.getCompliancePercent(comparisons);
                            RankingTree bestTree = new RankingTree(originalTree);

                            int higherLevelTeam, lowerLevelTeam;

                            if (tree.getLevel(secondTeam) > tree.getLevel(targetTeam)) {
                                higherLevelTeam = secondTeam;
                                lowerLevelTeam = targetTeam;
                            } else {
                                lowerLevelTeam = secondTeam;
                                higherLevelTeam = targetTeam;
                            }
                            //try to demote worse team first
                            while (tree.getLevel(higherLevelTeam) > 0) {
                                tree.demote(higherLevelTeam);
                                double currentCompPer = tree.getCompliancePercent(comparisons);
                                if (currentCompPer > bestCompliancePercent) {
                                    bestCompliancePercent = currentCompPer;
                                    bestTree = new RankingTree(bestTree.getTreeHashMap());
                                }
                            }

                            tree = new RankingTree(originalTree);

                            //try to promote better team afterward
                            while (lowerLevelTeam < tree.getMaxLevel()) {
                                tree.promote(lowerLevelTeam);
                                double currentCompPer = tree.getCompliancePercent(comparisons);
                                if (currentCompPer > bestCompliancePercent) {
                                    bestCompliancePercent = currentCompPer;
                                    bestTree = new RankingTree(bestTree.getTreeHashMap());
                                }
                            }

                            tree = new RankingTree(bestTree.getTreeHashMap());
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }

        FileManager.outputFile(new File(outputDirectory.getAbsolutePath() + "\\Picklist - Topological Sort - " + eventName + ".txt"),
                rankTreeStringToStringList(tree));
    }

    /**
     * Formats a ranking tree into a printable string
     * with the best team having the highest level
     *
     * @param tree RankingTree to process
     * @return Formatted list, with tree levels not clamped down
     */
    private String rankTreeStringToStringList(RankingTree tree) {
        String treeString = tree.toString();
        StringBuilder result = new StringBuilder();
        int rank = 1;
        for (String line : treeString.split("\n")) {
            result.append(rank).append(". ").append(line.split(",")[0]).append(": ");
            result.append(line.split(",")[1]).append(" pts\n");
            rank++;
        }
        return result.toString();
    }

    /**
     * Given a ranked list (generated from any method) of teams, swap teams if there are any
     * head-to-head conflicts with adjacent teams. Only relies on comparisons, not another metric
     * Essentially an implementation of bubble sort.
     *
     * @param orderedList
     */
    public void generateHeadToHeadList(ArrayList<Integer> orderedList, String listTitle) {

        boolean swapsNeeded;
        do {
            swapsNeeded = false;
            for (int i = 0; i < orderedList.size() - 1; i++) {
                int leftTeam = orderedList.get(i);
                int rightTeam = orderedList.get(i + 1);

                for (Comparison comp : compLookup.get(leftTeam)) {
                    if (comp.contains(rightTeam) && comp.getBetterTeam() == rightTeam) {
                        //Swap values in array
                        swapsNeeded = true;
                        Collections.swap(orderedList, i, i + 1);
                    }
                }
            }
        } while (swapsNeeded);

        FileManager.outputFile(new File(outputDirectory.getAbsolutePath() + "\\Picklist - Head to Head - " + eventName + ".txt"),
                arrayListToStringList(orderedList));

    }

    /**
     * Formats an array list into a printable string, with the best team appearing first
     *
     * @param teamNums Array of numbers to process
     * @return Formatted list, with tree levels not clamped down
     */
    private String arrayListToStringList(ArrayList<Integer> teamNums) {
        StringBuilder result = new StringBuilder();
        int rank = 1;
        for (int teamNum : teamNums) {
            result.append(rank).append(". ").append(teamNum).append("\n");
            rank++;
        }
        return result.toString();
    }

    /**
     * Randomly generates many trees , then averages the ranks of teams
     * in trees above a certain compliance percentage (threshold is a function of the number of teams).
     * Needs many iterations, but the final result is 80-90% compliant, depending
     * on the number of transitive contradictions.
     * Running the method multiple times will lead to different lists, but the top and bottom tiers are
     * generally consistent.
     * TODO Improve the final result by taking more averages
     * Note, however, that we may already be reaching the best possible trees
     */
    public void generateBogoCompareList() {

        RankingTree bestTree = new RankingTree(teamNums);
        double bestCompPer = 0;


        HashMap<Integer, Integer> bestRanks = new HashMap<>();

        //Derived through experimentation
        double goodThreshPercent = 98.8 * Math.pow(0.988, teamNums.size());
        int maxIterations = 1000;


        for (int k = 0; k < 100; k++) {

            int goodIterations = 0;
            HashMap<Integer, Integer> totalRanks = new HashMap<>();
            for (int i = 0; i < maxIterations; i++) {

                //Randomly shuffles teamNums
                Collections.shuffle(teamNums);

                RankingTree tree1 = new RankingTree(teamNums);
                double compPer = tree1.getCompliancePercent(comparisons);
                if (compPer > bestCompPer) {
                    bestTree = new RankingTree(tree1.getTreeHashMap());
                    bestCompPer = compPer;
                }

                //Add the levels from a decent generated tree
                if (compPer > goodThreshPercent) {
                    for (int j = 0; j < teamNums.size(); j++) {
                        if (!totalRanks.containsKey(teamNums.get(j))) {
                            totalRanks.put(teamNums.get(j), teamNums.size() - teamNums.indexOf(j));
                        } else {
                            totalRanks.put(teamNums.get(j), totalRanks.get(teamNums.get(j)) + teamNums.size() - j);
                        }

                    }
                    goodIterations++;
                }

            }
            for (int j = 0; j < teamNums.size(); j++) {
                if (goodIterations < 10) {
                    goodIterations = 10;
                }
                if (!bestRanks.containsKey(teamNums.get(j))) {
                    bestRanks.put(teamNums.get(j), totalRanks.get(teamNums.get(j)) / (goodIterations / 10));
                } else if (totalRanks.containsKey(teamNums.get(j))) {
                    bestRanks.put(teamNums.get(j),
                            totalRanks.get(teamNums.get(j)) / (goodIterations / 10) + bestRanks.get(teamNums.get(j)));
                }
            }
        }


        bestTree = new RankingTree(bestRanks);

        //generateHeadToHeadList(bestTree.toArrayList(), "bogo");

        FileManager.outputFile(new File(outputDirectory.getAbsolutePath() + "\\Picklist - Bogo Compare - " + eventName + ".txt"),
                rankTreeStringToStringList(bestTree));
    }

    /**
     * Generates a list where a recommended (better) team gets +1 point for every comparison,
     * while the worse team gets -1 point. Equal teams receive zero points.
     * May be inaccurate due to one team playing consistently adjacent to better teams, etc.
     * Not recommended for serious analysis.
     */
    public void generateComparePointList() {
        HashMap<Integer, Integer> compareList = new HashMap<>();
        for (Comparison comp : comparisons) {
            int better = comp.getBetterTeam(), worse = comp.getWorseTeam();
            if (!compareList.containsKey(better)) {
                compareList.put(better, 1);
            } else {
                compareList.put(better, compareList.get(better) + 1);
            }

            if (!compareList.containsKey(worse)) {
                compareList.put(worse, -1);
            } else {
                compareList.put(worse, compareList.get(worse) - 1);
            }
        }

        FileManager.outputFile(new File(outputDirectory.getAbsolutePath() + "\\Picklist - Compare Points - " + eventName + ".txt"),
                hashMapToStringList(compareList));
    }

    /**
     * Formats a hash map into a printable string, with team numbers as keys
     * and points as values. More points indicate a better team.
     *
     * @param teamPointsMap RankingTree to process
     * @return Formatted list, with tree levels not clamped down
     */
    private String hashMapToStringList(HashMap<Integer, Integer> teamPointsMap) {
        teamPointsMap = Sorters.sortByComparator(teamPointsMap, false);
        StringBuilder result = new StringBuilder();
        int rank = 1;
        for (Map.Entry<Integer, Integer> entry : teamPointsMap.entrySet()) {
            result.append(rank).append(". ").append(entry.getKey()).append(": ");
            result.append(entry.getValue()).append(" pts\n");
            rank++;
        }
        return result.toString();
    }

    /**
     * Generates a list that's the summation of all given pick points
     * May be inaccurate due to some teams receiving more entries than others
     * and the different rating scales of each scouts (some may tend to give higher ratings, etc.)
     * Generally representative of robot ability.
     * TODO Make the points an average rather than a summation
     */
    public void generatePickPointList() {
        HashMap<Integer, Integer> pickPoints = new HashMap<>();

        for (ScoutEntry entry : scoutEntries) {
            try {
                Integer teamNum = entry.getPreMatch().getTeamNum();

                if (!pickPoints.containsKey(teamNum)) {
                    pickPoints.put(teamNum, 0);
                }

                pickPoints.put(teamNum, pickPoints.get(teamNum) + entry.getPostMatch().getPickNumber());

            } catch (NullPointerException e) {

            }
        }

        FileManager.outputFile(new File(outputDirectory.getAbsolutePath() + "\\Picklist - Pick Points" + eventName +
                        ".txt"),
                hashMapToStringList(pickPoints));

    }

}
