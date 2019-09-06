package org.usfirst.frc.team25.scouting.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Class for alliance-based calculations, stats, and predictions
 */
public class AllianceReport {

    private final TeamReport[] teamReports;

    /**
     * The confidence level of not reaching the scoring potential when computing the optimal number of null hatch panels
     * E.g. A value of 0.8 means that an alliance would be able to score more, if they weren't "capped" by the null
     * hatch panels, in 20% of the matches they play. Conversely, the number of hatch panels would benefit them in
     * 80% of the matches they play. Note that if this value is low, the alliance may not be able to place hatch
     * panels at a rate that matches their cargo cycling, thus also being detrimental.
     */
    @SuppressWarnings("FieldCanBeLocal")
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
    private final HashMap<String, Double> predictedValues, expectedValues, standardDeviations;
    /**
     * Denotes the best HAB levels to climb at the end of the match in order to maximize points earned.
     * Positions correspond to the alliance member position.
     * Assumes that no more than two robots can climb HAB level 2 and no more than one robot can climb HAB level 3.
     */
    private int[] bestClimbLevels;

    /**
     * The average number of scout entries made for each team in the alliance
     */
    private double avgSampleSize;
    /**
     * A string denoting the game pieces each member of the alliance should start with in order to maximize points
     * earned. An "H" refers to a hatch panel, while a "C" refers to a cargo. The position of each character corresponds
     * to the alliance member position.
     */
    private String bestSandstormGamePieceCombo;

    /**
     * Constructs a report to simulate an in-match alliance of three teams
     *
     * @param teamReports An array of team reports that a part of the alliance. If this contains less than three team
     *                    reports, "dummy" alliance partners will be created
     * @throws NullPointerException If <code>teamReports</code> is <code>null</code> or has a <code>null</code> team
     *                              report as its first element
     */
    public AllianceReport(ArrayList<TeamReport> teamReports) {

        this.teamReports = new TeamReport[3];
        expectedValues = new HashMap<>();
        predictedValues = new HashMap<>();
        bestStartingLevels = new int[3];
        standardDeviations = new HashMap<>();

        // Automatically adds team reports to the created array, skipping null entries
        int i = 0;
        for (TeamReport teamReport : teamReports) {
            if (teamReport != null) {
                this.teamReports[i] = teamReport;
                i++;
            }
        }

        // Auto-populates null team reports with the first (non-null) team report
        for (int j = 0; j < 3; j++) {
            if (this.teamReports[j] == null && this.teamReports[0] != null) {
                this.teamReports[j] = new TeamReport(this.teamReports[0]);
            }
        }

        calculateStats();
    }

    /**
     * Calculates and stores the predicted point breakdowns, expected values, standard deviations, optimal null hatch
     * panels placed, and predicted bonus ranking points of the alliance
     */
    private void calculateStats() {
        calculateAvgSampleSize();
        calculateExpectedValues();

        double sandstormPoints = calculatePredictedSandstormPoints();
        double teleOpPoints = calculatePredictedTeleOpPoints();
        double endgamePoints = calculatePredictedEndgamePoints();
        predictedValues.put("totalPoints", sandstormPoints + teleOpPoints + endgamePoints);

        calculateStandardDeviations();
        calculatePredictedRp();
        calculateOptimalNullHatchPanels(NULL_HATCH_CONFIDENCE);
    }

    /**
     * Calculates the average number of scouting entries collected for each team in the AllianceReport
     */
    private void calculateAvgSampleSize() {
        avgSampleSize = 0;
        int validTeamReports = 0;

        for (TeamReport teamReport : this.teamReports) {
            if (teamReport != null && teamReport.getTeamNum() != 0) {
                avgSampleSize += teamReport.getEntries().size();
                validTeamReports++;
            }
        }

        if (validTeamReports != 0) {
            avgSampleSize /= validTeamReports;
        }
    }

    /**
     * Calculates and stores the simple expected values of metrics for an alliance.
     * An expected value of a metric is defined as the sum of the average values of that metric across the teams in
     * an alliance.
     */
    private void calculateExpectedValues() {
        String[][] metricSets = new String[][]{TeamReport.autoMetricNames, TeamReport.teleMetricNames,
                TeamReport.overallMetricNames};

        String[] prefixes = new String[]{"auto", "tele", ""};

        for (int i = 0; i < metricSets.length; i++) {
            for (String metric : metricSets[i]) {
                double expectedValue = 0.0;

                for (TeamReport report : teamReports) {
                    expectedValue += report.getAverage(prefixes[i] + metric);
                }

                expectedValues.put(prefixes[i] + metric, expectedValue);
            }
        }
    }

    /**
     * Calculates and stores the predicted number of points gained during the sandstorm period for an alliance
     *
     * @return The predicted number of sandstorm points
     */
    private double calculatePredictedSandstormPoints() {
        double predictedSandstormPoints = 0;

        predictedSandstormPoints += calculatePredictedSandstormBonus();
        predictedSandstormPoints += calculatePredictedSandstormGamePiecePoints();

        predictedValues.put("sandstormPoints", predictedSandstormPoints);

        return predictedSandstormPoints;
    }

    /**
     * Iterates through all possible HAB starting level combinations to determine the optimal one for the alliance
     * Assumption: At most two teams start on HAB level 2
     *
     * @return The point value of the starting combination that yields the largest expected sandstorm bonus
     */
    private double calculatePredictedSandstormBonus() {
        double bestCrossingScore = 0.0;

        final int[][] levelCombinations = new int[][]{{2, 2, 1}, {2, 1, 2}, {1, 2, 2}, {1, 1, 2},
                {1, 2, 1}, {1, 1, 1}};

        for (int[] levelCombo : levelCombinations) {
            double crossingScore = 0.0;
            for (int i = 0; i < levelCombo.length; i++) {
                // Multiply by attempt-success rates here to get the true expected value per team
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

    /**
     * Iterates through possible starting game piece combinations to determine the optimal one for the alliance
     * during the sandstorm period
     * Assumptions:
     * If teams can place game pieces at a certain location, it doesn't matter where they start
     * A team's attempt-success rate for a game pieces is the same regardless of placement location
     * Bays where hatch panels are placed are pre-populated with cargo
     * Teams score a maximum of one game piece in sandstorm
     *
     * @return The point value of the combination that yields the largest expected score
     */
    private double calculatePredictedSandstormGamePiecePoints() {
        final String[] gamePieceCombinations = new String[]{"HHH", "HHC", "HCH", "HCC", "CHH", "CHC", "CCH", "CCC"};

        double bestGamePieceScore = 0.0;

        for (String gamePieceCombo : gamePieceCombinations) {
            double cargoShipCargo = 0.0;
            double cargoShipHatches = 0.0;
            double rocketHatches = 0.0;

            int frontCargoShipCount = 0;

            for (int i = 0; i < gamePieceCombo.length(); i++) {
                if (gamePieceCombo.charAt(i) == 'H') {
                    // Validate assignments here
                    if (teamReports[i].getAbility("sideCargoShipHatchSandstorm")) {
                        cargoShipHatches += teamReports[i].getAttemptSuccessRate("hatchAutoSuccess");
                    } else if (teamReports[i].getAbility("frontCargoShipHatchSandstorm") && frontCargoShipCount < 2) {
                        cargoShipHatches += teamReports[i].getAttemptSuccessRate("hatchAutoSuccess");
                        frontCargoShipCount++;
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

    /**
     * Calculates and stores the predicted number of points gained from scoring game pieces during the tele-op period
     * for an alliance
     *
     * @return The predicted number of tele-op points
     */
    private double calculatePredictedTeleOpPoints() {

        double teleOpHatches = calculatePredictedTeleOpHatchPanels(false);
        double teleOpCargo = calculatePredictedTeleOpCargo(false);

        double predictedTeleOpPoints = 2 * teleOpHatches + 3 * teleOpCargo;

        predictedValues.put("telePoints", predictedTeleOpPoints);

        return predictedTeleOpPoints;
    }

    /**
     * Calculates the predicted number of hatch panels scored in each location of the field during the tele-op
     * period, based on the alliance's expected values
     *
     * @param rocketRp Specifies if the alliance is attempting to score as much as possible at each location, or if
     *                 they are attempting to gain the rocket ranking point. Affects game piece cap for rocket levels.
     * @return The predicted number of hatch panels scored during tele-op
     */
    private double calculatePredictedTeleOpHatchPanels(boolean rocketRp) {

        double totalHatches = 0;

        // Carry-over variable between rocket levels
        double excessHatches = 0;

        // Simulate placement in uppermost rocket levels first
        for (int i = 2; i >= 0; i--) {
            double cap = rocketRp ? 2.0 : 4.0;

            if (i == 0) {
                // Allow cargo ship hatch panels to be interchangeable with level 1 hatch panels
                excessHatches += expectedValues.get("telecargoShipHatches");

                // Decrease cap due to rocket hatches placed during the sandstorm period
                cap = Math.max(0, cap - predictedValues.get("autoRocketHatches"));
            }

            double hatchesPut = Math.min(excessHatches + expectedValues.get("telerocketLevel" + numStrNames[i] +
                    "Hatches"), cap);

            predictedValues.put("teleRocketLevel" + numStrNames[i] + "Hatches", hatchesPut);
            totalHatches += hatchesPut;
            excessHatches += expectedValues.get("telerocketLevel" + numStrNames[i] + "Hatches") - hatchesPut;
        }

        double teleCargoShipHatches = Math.min(excessHatches, 8 - predictedValues.get("autoCargoShipHatches"));
        totalHatches += teleCargoShipHatches;

        predictedValues.put("teleCargoShipHatches", teleCargoShipHatches);
        predictedValues.put("cargoShipHatches", teleCargoShipHatches + predictedValues.get("autoCargoShipHatches"));
        predictedValues.put("rocketLevelOneHatches",
                predictedValues.get("teleRocketLevelOneHatches") + predictedValues.get("autoRocketHatches"));

        predictedValues.put("teleHatches", totalHatches);
        predictedValues.put("teleHatchPoints", 2 * totalHatches);

        return totalHatches;
    }

    /**
     * Calculates the predicted number of cargo scored in each location of the field during the tele-op
     * period, based on the alliance's expected values
     *
     * @param rocketRp Specifies if the alliance is attempting to score as much as possible at each location, or if
     *                 they are attempting to gain the rocket ranking point. Affects game piece cap for rocket levels.
     * @return The predicted number of cargo scored during tele-op
     */
    private double calculatePredictedTeleOpCargo(boolean rocketRp) {
        double totalCargo = 0;

        double excessCargo = 0;

        for (int i = 2; i >= 0; i--) {
            // Cap is affected by predicted tele-op hatch panels for that location
            double cap = Math.min(rocketRp ? 2.0 : 4.0, predictedValues.get("teleRocketLevel" + numStrNames[i] +
                    "Hatches"));

            if (i == 0) {
                // Allow cargo ship cargo to be interchangeable with level 1 cargo
                excessCargo += expectedValues.get("telecargoShipCargo");
                cap = Math.min(rocketRp ? 2.0 : 4.0,
                        predictedValues.get("teleRocketLevel" + numStrNames[i] + "Hatches") + predictedValues.get(
                                "autoRocketHatches"));
            }

            double cargoPut = Math.min(excessCargo + expectedValues.get("telerocketLevel" + numStrNames[i] + "Cargo")
                    , cap);

            predictedValues.put("teleRocketLevel" + numStrNames[i] + "Cargo", cargoPut);
            totalCargo += cargoPut;
            excessCargo += expectedValues.get("telerocketLevel" + numStrNames[i] + "Cargo") - cargoPut;
        }

        // We do not cap by hatch panels here due to the possibility of null hatch panels
        // We also assume that hatch panels placed during sandstorm will be placed in a bay pre-populated with cargo
        double teleCargoShipCargo = Math.min(excessCargo,
                8 - predictedValues.get("autoCargoShipCargo") - predictedValues.get("autoCargoShipHatches"));
        predictedValues.put("teleCargoShipCargo", teleCargoShipCargo);

        totalCargo += teleCargoShipCargo;
        predictedValues.put("teleCargo", totalCargo);
        predictedValues.put("teleCargoPoints", 3 * totalCargo);
        predictedValues.put("cargoShipCargo", predictedValues.get("teleCargoShipCargo") + predictedValues.get(
                "autoCargoShipCargo"));
        predictedValues.put("rocketLevelOneCargo", predictedValues.get("teleRocketLevelOneCargo"));

        return totalCargo;
    }

    /**
     * Iterates through all possible HAB climb combinations to determine the optimal one for the alliance
     * Assumption:
     * At most one team climbs to HAB level 3
     * At most two teams climb to HAB level 2
     * A robot either climbs to its assigned level or does not climb the HAB at all
     *
     * @return The point value of the endgame climb combination that yields the largest expected score
     */
    private double calculatePredictedEndgamePoints() {
        double bestEndgamePoints = 0;

        final int[][] climbLevelCombos = new int[][]{{1, 1, 1}, {1, 1, 2}, {1, 1, 3}, {1, 2, 1}, {1, 2, 2}, {1, 2, 3},
                {1, 3, 1}, {1, 3, 2}, {2, 1, 1}, {2, 1, 2}, {2, 1, 3}, {2, 2, 1}, {2, 2, 3}, {2, 3, 1}, {2, 3, 2},
                {3, 1, 1}, {3, 1, 2}, {3, 2, 1}, {3, 2, 2}};

        final int[] climbPointValues = new int[]{3, 6, 12};

        for (int[] climbLevelCombo : climbLevelCombos) {
            double endgamePoints = 0.0;

            // Iterate through each team on the alliance
            for (int i = 0; i < climbLevelCombo.length; i++) {
                endgamePoints += climbPointValues[climbLevelCombo[i] - 1] * teamReports[i].getAttemptSuccessRate(
                        "level" + numStrNames[climbLevelCombo[i] - 1] + "Climb");
            }

            if (endgamePoints >= bestEndgamePoints) {
                bestEndgamePoints = endgamePoints;
                bestClimbLevels = climbLevelCombo;
            }
        }

        predictedValues.put("endgamePoints", bestEndgamePoints);

        return bestEndgamePoints;
    }

    /**
     * Calculates standard deviations for all predicted metrics
     */
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

    /**
     * @param allianceSimulationValues
     * @return
     */
    private double calculateStdDevTeleOpPoints(ArrayList<ArrayList<HashMap<String, Double>>> allianceSimulationValues) {

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

        for (int i = 0; i < allianceSimulationValues.size(); i++) {

            calculateMonteCarloExpectedValues(allianceSimulationValues.get(i));
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

    private double calculatePredictedRp() {

        double bonusRp = calculateClimbRpChance() + calculateRocketRpChance(generateMonteCarloSet());

        predictedValues.put("bonusRp", bonusRp);

        return bonusRp;
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

    private double calculateClimbRpChance() {
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


        return Stats.tCumulativeDistribution(degreesOfFreedom, tScore);
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

    /**
     * @param allianceSimulationValues
     * @return
     */
    private double calculateRocketRpChance(ArrayList<ArrayList<HashMap<String, Double>>> allianceSimulationValues) {

        int rocketRpAttainedCount = 0;

        for (ArrayList<HashMap<String, Double>> teamReportSet : allianceSimulationValues) {

            calculateMonteCarloExpectedValues(teamReportSet);

            calculatePredictedTeleOpHatchPanels(true);
            calculatePredictedTeleOpCargo(true);

            boolean rpAttained = true;

            for (int j = 0; j < 3; j++) {
                for (String gamePiece : new String[]{"Hatches", "Cargo"}) {
                    double threshold = 2.0 - ((j == 0) ? predictedValues.get("autoRocket" + gamePiece) : 0);
                    double mean = predictedValues.get("teleRocketLevel" + numStrNames[j] + gamePiece);

                    if (mean < threshold) {
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

    /**
     * Generates a text report for the alliance
     *
     * @return An easily-readable string of the key stats of the alliance
     */
    public String getQuickAllianceReport() {

        Object[] keys = predictedValues.keySet().toArray();
        Arrays.sort(keys);

        StringBuilder quickReport = new StringBuilder();
        for (TeamReport report : teamReports) {
            quickReport.append("Team ").append(report.getTeamNum());

            if (!report.getTeamName().isEmpty()) {
                quickReport.append(" - ").append(report.getTeamName());
            }
            quickReport.append("\n");
        }

        for (Object key : keys) {
            quickReport.append(key).append(": ").append(Stats.round(predictedValues.get(key), 2)).append("\n");
        }

        return quickReport.toString();
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

    public double getAvgSampleSize() {
        return avgSampleSize;
    }

    public TeamReport[] getTeamReports() {
        return teamReports;
    }

}
