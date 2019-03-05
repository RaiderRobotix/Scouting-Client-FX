package org.usfirst.frc.team25.scouting.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Class for alliance-based calculations, stats, and predictions
 */
public class AllianceReport {

    private TeamReport[] teamReports;

    /**
     * The confidence level of not reaching the scoring potential when computing the optimal number of null hatch panels
     * E.g. A value of 0.8 means that an alliance would be able to score more, if they weren't "capped" by the null
     * hatch panels, in 20% of the matches they play. Conversely, the number of hatch panels would benefit them in
     * 80% of the matches they play. Note that if this value is high, the alliance may not be able to place hatch
     * panels at a rate that matches their cargo cycling, thus also being detrimental.
     */
    private final double NULL_HATCH_CONFIDENCE = 0.8;
    /**
     * The number of Monte Carlo simulation iterations to run to compute standard deviations of functions.
     * A larger number of iterations generally provides greater accuracy.
     */
    private final int MONTE_CARLO_ITERATIONS = 1000;
    private final String[] numStrNames = new String[]{"One", "Two", "Three", "total"};
    /**
     * Denotes the best HAB levels to start from at the beginning of the match in order to maximize points earned.
     * Positions correspond to the alliance member position.
     * Assumes that no more than two robots can start from HAB level 2.
     */
    private int[] bestStartingLevels;
    /**
     * Denotes the best HAB levels to climb at the end of the match in order to maximize points earned.
     * Positions correspond to the alliance member position.
     * Assumes that no more than two robots can climb HAB level 2 and no more than one robot can climb HAB level 1.
     */
    private int[] bestClimbLevels;
    /**
     * A string denoting the game pieces each member of the alliance should start with in order to maximize points
     * earned. An "H" refers to a hatch panel, while a "C" refers to a cargo. The position of each character refers
     * to the alliance member position.
     */
    private String bestSandstormGamePieceCombo;
    /**
     * The average number of scout entries made for each team in the alliance
     */
    private double avgSampleSize;
    private HashMap<String, Double> predictedValues, expectedValues, standardDeviations;


    /**
     * Constructs a report to simulate an in-match alliance of three teams
     *
     * @param teamReports An array of team reports that a part of the alliance. If this contains less than three team
     *                    reports, "dummy" alliance partners will be created
     * @throws NullPointerException If <code>teamReports</code> is <code>null</code> or has a <code>null</code> team
     *                              report as its first element
     */
    public AllianceReport(TeamReport[] teamReports) throws NullPointerException {

        TeamReport nonNullTeamReport = null;

        for (TeamReport teamReport : teamReports) {
            if (teamReport != null) {
                nonNullTeamReport = teamReport;
            }
        }

        this.teamReports = new TeamReport[3];

        for (int i = 0; i < 3; i++) {
            try {
                if (teamReports[i] == null) {
                    this.teamReports[i] = new TeamReport(nonNullTeamReport);
                } else {
                    this.teamReports[i] = teamReports[i];
                }
            } catch (IndexOutOfBoundsException e) {
                if (i == 0) {
                    throw new NullPointerException("Alliance report must be initialized with at least one team");
                }
                this.teamReports[i] = new TeamReport(nonNullTeamReport);
            }
        }

        expectedValues = new HashMap<>();
        predictedValues = new HashMap<>();
        bestStartingLevels = new int[3];
        standardDeviations = new HashMap<>();

        avgSampleSize = 0;
        int validTeamReports = 0;

        for (TeamReport teamReport : this.teamReports) {
            if (teamReport != null && teamReport.getTeamNum() != 0) {
                avgSampleSize += teamReport.getEntries().size();
                validTeamReports++;
            }
        }

        avgSampleSize /= validTeamReports;

        calculateStats();
    }

    /**
     * Calculates and stores the predicted point breakdowns, expected values, standard deviations, optimal null hatch
     * panels
     * placed, and predicted bonus ranking points of the alliance
     */
    public void calculateStats() {

        calculateExpectedValues();

        double sandstormPoints = calculatePredictedSandstormPoints();
        double teleOpPoints = calculatePredictedTeleOpPoints();
        double endgamePoints = calculatePredictedEndgamePoints();

        calculateStandardDeviations();
        calculatePredictedRp();

        calculateOptimalNullHatchPanels(NULL_HATCH_CONFIDENCE);

        predictedValues.put("totalPoints", sandstormPoints + teleOpPoints + endgamePoints);

    }

    private double calculatePredictedSandstormBonus() {
        double bestCrossingScore = 0.0;


        final int[][] levelCombinations = new int[][]{{2, 2, 1}, {2, 1, 2}, {1, 2, 2}, {1, 1, 2},
                {1, 2, 1}, {1, 1, 1}};

        // Iterate through all starting combinations
        for (int[] levelCombo : levelCombinations) {
            double crossingScore = 0.0;
            for (int i = 0; i < levelCombo.length; i++) {
                if (levelCombo[i] == 1) {
                    crossingScore += 3.0 * teamReports[i].getAttemptSuccessRate("levelOneCross");
                } else {
                    crossingScore += 6.0 * teamReports[i].getAttemptSuccessRate("levelTwoCross");
                }
            }

            if (crossingScore >= bestCrossingScore) {
                bestCrossingScore = crossingScore;
                bestStartingLevels = levelCombo;
            }
        }

        predictedValues.put("sandstormBonus", bestCrossingScore);

        return bestCrossingScore;
    }

    private double calculatePredictedSandstormPoints() {
        double expectedSandstormPoints = 0;

        expectedSandstormPoints += calculatePredictedSandstormBonus();
        expectedSandstormPoints += calculatePredictedSandstormGamePiecePoints();

        predictedValues.put("sandstormPoints", expectedSandstormPoints);

        return expectedSandstormPoints;
    }

    private double calculatePredictedSandstormGamePiecePoints() {
        // TODO Make this model more rigorous, with multi game piece autos
        // Assumptions:
        // If teams can place game pieces at a certain location, it doesn't matter where they start
        // A team's attempt-success rate for a game pieces is the same regardless of placement location
        // Bays where hatch panels are placed are pre-populated with cargo
        // Teams can score a maximum of one game piece in sandstorm
        final String[] gamePieceCombinations = new String[]{"HHH", "HHC", "HCH", "HCC", "CHH", "CHC", "CCH", "CCC"};

        double bestGamePieceScore = 0.0;

        for (String gamePieceCombo : gamePieceCombinations) {
            double cargoShipCargo = 0.0;
            double cargoShipHatches = 0.0;
            double rocketHatches = 0.0;

            int frontCargoShipCount = 0;

            for (int i = 0; i < gamePieceCombo.length(); i++) {

                if (gamePieceCombo.charAt(i) == 'H') {
                    if (teamReports[i].getAbility("frontCargoShipHatchSandstorm") && frontCargoShipCount < 2) {
                        cargoShipHatches += teamReports[i].getAttemptSuccessRate("hatchAutoSuccess");
                        frontCargoShipCount++;
                    } else if (teamReports[i].getAbility("sideCargoShipHatchSandstorm")) {
                        cargoShipHatches += teamReports[i].getAttemptSuccessRate("hatchAutoSuccess");

                    } else if (teamReports[i].getAbility("rocketHatchSandstorm")) {
                        rocketHatches += teamReports[i].getAttemptSuccessRate("hatchAutoSuccess");
                    }
                } else {
                    cargoShipCargo += teamReports[i].getAttemptSuccessRate("cargoAutoSuccess");
                }
            }

            double gamePieceScore = 5 * cargoShipHatches + 3 * cargoShipCargo + 2 * rocketHatches;


            if (gamePieceScore >= bestGamePieceScore) {
                bestGamePieceScore = gamePieceScore;
                bestSandstormGamePieceCombo = gamePieceCombo;

                predictedValues.put("autoCargoShipCargo", cargoShipCargo);
                predictedValues.put("autoCargoShipHatches", cargoShipHatches);
                predictedValues.put("autoRocketHatches", rocketHatches);
                predictedValues.put("autoRocketCargo", 0.0);
            }
        }
        predictedValues.put("sandstormGamePiecePoints", bestGamePieceScore);

        return bestGamePieceScore;
    }

    private double calculatePredictedEndgamePoints() {
        double bestEndgamePoints = 0;

        final int[][] climbLevelCombos = new int[][]{{1, 1, 1}, {1, 1, 2}, {1, 1, 3}, {1, 2, 1}, {1, 2, 2}, {1, 2, 3},
                {1, 3, 1}, {1, 3, 2}, {2, 1, 1}, {2, 1, 2}, {2, 1, 3}, {2, 2, 1}, {2, 2, 3}, {2, 3, 1}, {2, 3, 2},
                {3, 1, 1}, {3, 1, 2}, {3, 2, 1}, {3, 2, 2}};

        final int[] climbPointValues = new int[]{3, 6, 12};

        for (int[] climbLevelCombo : climbLevelCombos) {
            double endgamePoints = 0.0;
            for (int i = 0; i < climbLevelCombo.length; i++) {
                endgamePoints += climbPointValues[climbLevelCombo[i] - 1] * teamReports[i].getAttemptSuccessRate(
                        "level" + numStrNames[climbLevelCombo[i] - 1] + "Climb");
            }

            if (endgamePoints >= bestEndgamePoints) {
                bestEndgamePoints = endgamePoints;
                bestClimbLevels = climbLevelCombo;
            }
        }

        // Assume no assisted climbs
        /*for (int i = 0; i < teamReports.length; i++) {
            double endgamePoints = 0.0;
            int[] climbLevels = new int[3];
            if (teamReports[i].getAbility("doubleBuddyClimb")) {
                endgamePoints += teamReports[i].getAverage("numPartnerClimbAssists") * (teamReports[i]
                        .getAbility("levelThreeBuddyClimb") ? 12 : 6);
                climbLevels[i] = teamReports[i].findBestClimbLevel();
                endgamePoints += climbLevels[i] * climbPointValues[climbLevels[i] - 1];
                for (int j = 0; j < climbLevels.length; j++) {
                    if (j != i) {
                        climbLevels[j] = 0;
                    }
                }
            } else if (teamReports[i].getAbility("singleBuddyClimb")) {
                endgamePoints += teamReports[i].getAverage("numPartnerClimbAssists") * (teamReports[i]
                        .getAbility("levelThreeBuddyClimb") ? 12 : 6);

                double bestStandaloneClimbPoints = 0.0;

                int bestStandaloneClimbLevel


            }
        }*/

        predictedValues.put("endgamePoints", bestEndgamePoints);

        return bestEndgamePoints;
    }

    private double calculatePredictedTeleOpPoints() {

        double teleOpHatches = calculatePredictedTeleOpHatchPanels(false);
        double teleOpCargo = calculatePredictedTeleOpCargo(false);

        double expectedTeleOpPoints = 2 * teleOpHatches + 3 * teleOpCargo;

        predictedValues.put("telePoints", expectedTeleOpPoints);

        return expectedTeleOpPoints;
    }

    private void calculateStandardDeviations() {

        double sandstormStdDev = calculateStdDevSandstormPoints();
        double teleOpStdDev = calculateStdDevTeleOpPoints(generateMonteCarloSet());
        double endgameStdDev = calculateStdDevEndgamePoints();

        double totalPointsStdDev = Stats.sumStandardDeviation(new double[]{sandstormStdDev, teleOpStdDev,
                endgameStdDev});


        standardDeviations.put("totalPoints", totalPointsStdDev);
    }

    private double calculateStdDevSandstormPoints() {
        double sandstormBonusVariance = 0.0;

        for (int i = 0; i < bestStartingLevels.length; i++) {
            sandstormBonusVariance += Stats.multiplyVariance(bestStartingLevels[i] * 3,
                    teamReports[i].getStandardDeviation("level" + numStrNames[bestStartingLevels[i] - 1] +
                            "Cross"));
        }

        standardDeviations.put("sandstormBonus", Math.sqrt(sandstormBonusVariance));

        double sandstormGamePieceVariance = 0.0;
        double sandstormHatchVariance = 0.0;

        for (int i = 0; i < bestSandstormGamePieceCombo.length(); i++) {
            if (bestSandstormGamePieceCombo.charAt(i) == 'H') {
                sandstormGamePieceVariance += Stats.multiplyVariance(5, teamReports[i].getStandardDeviation(
                        "hatchAutoSuccess"));
                sandstormHatchVariance += Math.pow(teamReports[i].getStandardDeviation("hatchAutoSuccess"), 2);
            } else {
                sandstormGamePieceVariance += Stats.multiplyVariance(3, teamReports[i].getStandardDeviation(
                        "cargoAutoSuccess"));
            }
        }

        standardDeviations.put("autoCargoShipHatches", Math.sqrt(sandstormHatchVariance));
        standardDeviations.put("sandstormGamePiecePoints", Math.sqrt(sandstormGamePieceVariance));

        double sandstormPointsStdDev = Math.sqrt(sandstormBonusVariance + sandstormGamePieceVariance);

        standardDeviations.put("sandstormPoints", sandstormPointsStdDev);

        return sandstormPointsStdDev;
    }

    private void calculateOptimalNullHatchPanels(double confidenceLevel) {

        double averageCargoShipHatches = predictedValues.get("autoCargoShipHatches") + predictedValues.get(
                "teleCargoShipHatches");

        double standardDeviation = Stats.sumStandardDeviation(new double[]{
                standardDeviations.get("autoCargoShipHatches"), standardDeviations.get("teleCargoShipHatches")});


        double optimisticCargoShipHatches = averageCargoShipHatches;
        if (avgSampleSize > 1) {
            optimisticCargoShipHatches = Stats.inverseTValue(confidenceLevel, avgSampleSize, averageCargoShipHatches,
                    standardDeviation);

        }


        double nullHatches = Math.max(Math.min(8 - optimisticCargoShipHatches, 6), 0);
        predictedValues.put("optimalNullHatches", nullHatches);

    }

    /**
     * @return
     */
    private double calculateStdDevEndgamePoints() {

        final int[] climbPointValues = new int[]{3, 6, 12};

        double endgameVariance = 0.0;

        for (int i = 0; i < bestClimbLevels.length; i++) {
            if (bestClimbLevels[i] != 0) {
                endgameVariance += Stats.multiplyVariance(climbPointValues[bestClimbLevels[i] - 1],
                        teamReports[i].getStandardDeviation("level" + numStrNames[bestClimbLevels[i] - 1] +
                                "Climb"
                        ));
            }
        }

        standardDeviations.put("endgamePoints", Math.sqrt(endgameVariance));

        return endgameVariance;
    }

    public double calculatePredictedRp() {

        double bonusRp = calculateClimbRpChance() + calculateRocketRpChance(generateMonteCarloSet());

        predictedValues.put("bonusRp", bonusRp);

        return bonusRp;
    }

    /**
     * @param simulationRawValues
     * @return
     */
    private double calculateStdDevTeleOpPoints(ArrayList<ArrayList<HashMap<String, Double>>> simulationRawValues) {

        ArrayList<String> metricNames = new ArrayList<>();

        metricNames.add("telePoints");

        final String[] locations = new String[]{"teleRocketLevelOne", "teleRocketLevelTwo", "teleRocketLevelThree",
                "teleCargoShip"};
        final String[] pieces = new String[]{"Hatches", "Cargo"};

        for (String location : locations) {
            for (String piece : pieces) {
                metricNames.add(location + piece);
            }
        }

        HashMap<String, double[]> simulationCalculatedValues = new HashMap<>();

        for (String metric : metricNames) {
            simulationCalculatedValues.put(metric, new double[MONTE_CARLO_ITERATIONS]);
        }

        for (int i = 0; i < simulationRawValues.size(); i++) {

            calculateMonteCarloExpectedValues(simulationRawValues.get(i));
            calculatePredictedTeleOpPoints();

            for (String metric : metricNames) {

                simulationCalculatedValues.get(metric)[i] = predictedValues.get(metric);
            }

        }


        for (String metric : metricNames) {
            standardDeviations.put(metric, Stats.standardDeviation(simulationCalculatedValues.get(metric)));
        }


        //Restores values using the original alliance report
        calculateExpectedValues();
        calculatePredictedTeleOpPoints();

        return standardDeviations.get("telePoints");
    }

    /**
     * Calculates and stores the expected values of metrics for an alliance.
     * An expected value of a metric is defined as the sum of the average values of that metric across the teams in
     * an alliance.
     */
    private void calculateExpectedValues() {
        String[][] metricSets = new String[][]{TeamReport.autoMetricNames, TeamReport.teleMetricNames,
                TeamReport.overallMetricNames};

        String[] prefixes = new String[]{"auto", "tele", ""};

        for (int i = 0; i < metricSets.length; i++) {
            for (String metric : metricSets[i]) {
                double expectedValue = 0;

                for (TeamReport report : teamReports) {
                    expectedValue += report.getAverage(prefixes[i] + metric);
                }

                expectedValues.put(prefixes[i] + metric, expectedValue);
            }
        }
    }

    private ArrayList<ArrayList<HashMap<String, Double>>> generateMonteCarloSet() {

        ArrayList<ArrayList<HashMap<String, Double>>> simulationValues = new ArrayList<>();

        for (int i = 0; i < MONTE_CARLO_ITERATIONS; i++) {
            ArrayList<HashMap<String, Double>> sampleMatchValues = new ArrayList<>();
            for (TeamReport sample : teamReports) {
                sampleMatchValues.add(sample.generateRandomSample());
            }
            simulationValues.add(sampleMatchValues);
        }

        return simulationValues;
    }

    public double calculateClimbRpChance() {
        double climbRpChance = 0.0;

        final int[] climbPointValues = new int[]{3, 6, 12};

        for (int teamOneClimb = 0; teamOneClimb < 2; teamOneClimb++) {
            for (int teamTwoClimb = 0; teamTwoClimb < 2; teamTwoClimb++) {
                for (int teamThreeClimb = 0; teamThreeClimb < 2; teamThreeClimb++) {
                    int points = 0;
                    int[] climbStatus = new int[]{teamOneClimb, teamTwoClimb, teamThreeClimb};
                    for (int i = 0; i < 3; i++) {
                        points += climbStatus[i] * climbPointValues[bestClimbLevels[i] - 1];
                    }
                    if (points >= 15) {
                        double probabilityIteration = 1.0;
                        for (int i = 0; i < 3; i++) {
                            if (climbStatus[i] == 1) {
                                probabilityIteration *= teamReports[i].getAttemptSuccessRate("level" + numStrNames[bestClimbLevels[i] - 1] + "Climb");
                            } else {
                                probabilityIteration *= 1 - teamReports[i].getAttemptSuccessRate("level" + numStrNames[bestClimbLevels[i] - 1] + "Climb");
                            }
                        }
                        climbRpChance += probabilityIteration;
                    }
                }
            }
        }

        predictedValues.put("climbRp", climbRpChance);

        return climbRpChance;
    }

    /**
     * Generates a text report for the alliance
     *
     * @return An easily-readable string of the key stats of the alliance
     */
    public String getQuickAllianceReport() {

        Object[] keys = predictedValues.keySet().toArray();
        Arrays.sort(keys);

        String quickReport = "";
        for (TeamReport report : teamReports) {
            quickReport += "Team " + report.getTeamNum();

            if (!report.getTeamName().isEmpty()) {
                quickReport += " - " + report.getTeamName();
            }
            quickReport += "\n";
        }

        for (Object key : keys) {
            quickReport += key + ": " + Stats.round(predictedValues.get(key), 2) + "\n";
        }

        return quickReport;
    }

    /**
     * @param simulationRawValues
     * @return
     */
    private double calculateRocketRpChance(ArrayList<ArrayList<HashMap<String, Double>>> simulationRawValues) {

        int rocketRpAttainedCount = 0;

        for (ArrayList<HashMap<String, Double>> teamReportSet : simulationRawValues) {

            calculateMonteCarloExpectedValues(teamReportSet);

            calculatePredictedTeleOpHatchPanels(true);
            calculatePredictedTeleOpCargo(true);

            boolean rpAttained = true;

            for (int j = 0; j < 3; j++) {
                for (String gamePiece : new String[]{"Hatches", "Cargo"}) {
                    double threshhold = 2.0 - ((j == 0) ? predictedValues.get("autoRocket" + gamePiece) : 0);
                    double mean = predictedValues.get("teleRocketLevel" + numStrNames[j] + gamePiece);

                    if (mean < threshhold) {
                        rpAttained = false;
                    }
                }
            }

            if (rpAttained) {
                rocketRpAttainedCount++;
            }

        }

        calculateExpectedValues();
        calculatePredictedTeleOpPoints();

        double rocketRpChance = ((double) rocketRpAttainedCount) / MONTE_CARLO_ITERATIONS;
        predictedValues.put("rocketRp", rocketRpChance);

        return rocketRpChance;
    }

    private double calculatePredictedTeleOpHatchPanels(boolean rocketRp) {

        double excessHatches = 0;
        double totalHatches = 0;


        for (int i = 2; i >= 0; i--) {
            double cap = rocketRp ? 2.0 : 4.0;

            if (i == 0) {
                // Allow cargo ship hatches to be interchangeable with lvl 1 hatches
                excessHatches += expectedValues.get("telecargoShipHatches");

                // Decrease cap due to rocket hatches placed
                cap = Math.max(0, cap - predictedValues.get("autoRocketHatches"));
            }

            if (excessHatches + expectedValues.get("telerocketLevel" + numStrNames[i] + "Hatches") > cap) {

                // Cap is reached, place max remaining into the level

                excessHatches += expectedValues.get("telerocketLevel" + numStrNames[i] + "Hatches") - cap;
                predictedValues.put("teleRocketLevel" + numStrNames[i] + "Hatches", cap);
                totalHatches += cap;
            } else {

                // Place everything into the level
                predictedValues.put("teleRocketLevel" + numStrNames[i] + "Hatches",
                        expectedValues.get("telerocketLevel" + numStrNames[i] + "Hatches") + excessHatches);
                totalHatches += expectedValues.get("telerocketLevel" + numStrNames[i] + "Hatches") + excessHatches;
                excessHatches = 0;
            }
        }

        double cargoShipHatches = Math.min(excessHatches, 8 - predictedValues.get("autoCargoShipHatches"));
        predictedValues.put("teleCargoShipHatches", cargoShipHatches);
        predictedValues.put("cargoShipHatches", cargoShipHatches + predictedValues.get("autoCargoShipHatches"));
        predictedValues.put("rocketLevelOneHatches",
                predictedValues.get("teleRocketLevelOneHatches") + predictedValues.get("autoRocketHatches"));

        totalHatches += cargoShipHatches;
        predictedValues.put("teleHatches", totalHatches);
        predictedValues.put("teleHatchPoints", 2 * totalHatches);

        return totalHatches;

    }

    /**
     * @param testSets
     */
    private void calculateMonteCarloExpectedValues(ArrayList<HashMap<String, Double>> testSets) {
        String[][] metricSets = new String[][]{TeamReport.autoMetricNames, TeamReport.teleMetricNames,
                TeamReport.overallMetricNames};

        String[] prefixes = new String[]{"auto", "tele", ""};

        for (int i = 0; i < metricSets.length; i++) {
            for (String metric : metricSets[i]) {
                double value = 0;
                for (HashMap<String, Double> testSet : testSets) {
                    value += testSet.get(prefixes[i] + metric);
                }
                expectedValues.put(prefixes[i] + metric, value);
            }
        }

    }

    private double calculatePredictedTeleOpCargo(boolean rocketRp) {
        double excessCargo = 0;
        double totalCargo = 0;

        for (int i = 2; i >= 0; i--) {
            double cap = Math.min(rocketRp ? 2.0 : 4, predictedValues.get("teleRocketLevel" + numStrNames[i] + "Hatches"
            ));

            if (i == 0) {
                // Allow cargo ship cargo to be interchangeable with lvl 1 cargo
                excessCargo += expectedValues.get("telecargoShipCargo");
                cap = Math.min(rocketRp ? 2.0 : 4,
                        predictedValues.get("teleRocketLevel" + numStrNames[i] + "Hatches") + predictedValues.get(
                                "autoRocketHatches"));
            }

            if (excessCargo + expectedValues.get("telerocketLevel" + numStrNames[i] + "Cargo") > cap) {

                excessCargo += expectedValues.get("telerocketLevel" + numStrNames[i] + "Cargo") - cap;
                predictedValues.put("teleRocketLevel" + numStrNames[i] + "Cargo", cap);

                totalCargo += cap;
            } else {

                predictedValues.put("teleRocketLevel" + numStrNames[i] + "Cargo",
                        expectedValues.get("telerocketLevel" + numStrNames[i] + "Cargo") + excessCargo);
                totalCargo += expectedValues.get("telerocketLevel" + numStrNames[i] + "Cargo") + excessCargo;
                excessCargo = 0;
            }
        }

        // We're not capping by hatch panels here due to the possibility of null hatch panels
        // We also assume that hatch panels placed during sandstorm will be placed in a bay pre-populated with cargo
        double cargoShipCargo = Math.min(excessCargo,
                8 - predictedValues.get("autoCargoShipCargo") - predictedValues.get("autoCargoShipHatches"));
        predictedValues.put("teleCargoShipCargo", cargoShipCargo);

        totalCargo += cargoShipCargo;
        predictedValues.put("teleCargo", totalCargo);
        predictedValues.put("teleCargoPoints", 3 * totalCargo);
        predictedValues.put("cargoShipCargo", predictedValues.get("teleCargoShipCargo") + predictedValues.get(
                "autoCargoShipCargo"));
        predictedValues.put("rocketLevelOneCargo", predictedValues.get("teleRocketLevelOneCargo"));

        return totalCargo;
    }

    public double calculatePredictedRp(AllianceReport opposingAlliance) {

        return calculateClimbRpChance() + calculateRocketRpChance(generateMonteCarloSet()) + 2 * calculateWinChance(opposingAlliance);
    }

    /**
     * @param opposingAlliance
     * @return
     */
    public double calculateWinChance(AllianceReport opposingAlliance) {

        double thisStandardError = Stats.standardError(standardDeviations.get("totalPoints"), avgSampleSize);
        double opposingStandardError = Stats.standardError(opposingAlliance.getStandardDeviation("totalPoints"),
                opposingAlliance.getAvgSampleSize());

        double tScore = Stats.twoSampleMeanTScore(predictedValues.get("totalPoints"), thisStandardError,
                opposingAlliance.getPredictedValue("totalPoints"), opposingStandardError);
        double degreesOfFreedom = Stats.twoSampleDegreesOfFreedom(thisStandardError, avgSampleSize,
                opposingStandardError, opposingAlliance.getAvgSampleSize());


        double winChance = Stats.tCumulativeDistribution(degreesOfFreedom, tScore);

        return winChance;
    }

    /**
     * @param metric
     * @return
     */
    public double getPredictedValue(String metric) {
        return predictedValues.get(metric);
    }

    /**
     * @param metric
     * @return
     */
    public double getStandardDeviation(String metric) {
        return standardDeviations.get(metric);
    }

    public int[] getBestStartingLevels() {
        return bestStartingLevels;
    }

    public int[] getBestClimbLevels() {
        return bestClimbLevels;
    }

    public String getBestSandstormGamePieceCombo() {
        return bestSandstormGamePieceCombo;
    }

    /**
     * @return
     */
    public double getAvgSampleSize() {
        return avgSampleSize;
    }

    public TeamReport[] getTeamReports() {
        return teamReports;
    }


}
