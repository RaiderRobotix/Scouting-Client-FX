package org.usfirst.frc.team25.scouting.data;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class for alliance-based calculations and stats
 * Not used during the 2018 season
 */
public class AllianceReport {

    private TeamReport[] teamReports;
    private int[] bestStartingLevels;
    private String bestSandstormGamePieceCombo;
    private int[] bestClimbLevels;
    final String[] numStrNames = new String[]{"One", "Two", "Three", "total"};
    private final double NULL_HATCH_CONFIDENCE = 0.8;
    private final int MONTE_CARLO_ITERATIONS = 5000;

    private HashMap<String, Double> predictedValues;

    private HashMap<String, Double> expectedValues;

    private HashMap<String, Double> standardDeviations;

    public AllianceReport(TeamReport teamOne, TeamReport teamTwo, TeamReport teamThree) {

        this.teamReports = new TeamReport[]{teamOne, teamTwo, teamThree};

        expectedValues = new HashMap<>();
        predictedValues = new HashMap<>();
        bestStartingLevels = new int[3];
        standardDeviations = new HashMap<>();
    }

    public String getQuickAllianceReport() {

        calculateStats();

        String quickReport = "";
        for (TeamReport report : teamReports) {
            quickReport += "Team " + report.getTeamNum();
            if (!report.getTeamName().isEmpty()) {
                quickReport += " - " + report.getTeamName();
            }
            quickReport += "\n";
        }

        for (String key : predictedValues.keySet()) {
            quickReport += key + ": " + Stats.round(predictedValues.get(key), 2) + "\n";
        }

        return quickReport;
    }

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

    private void calculateExpectedValues() {
        String[][] metricSets = new String[][]{TeamReport.autoMetricNames, TeamReport.teleMetricNames,
                TeamReport.overallMetricNames};

        String[] prefixes = new String[]{"auto", "tele", ""};

        for (int i = 0; i < metricSets.length; i++) {
            for (String metric : metricSets[i]) {
                double value = 0;
                for (TeamReport report : teamReports) {
                    value += report.getAverages().get(prefixes[i] + metric);
                }
                expectedValues.put(prefixes[i] + metric, value);
            }
        }

    }

    private double calculatePredictedSandstormPoints() {

        double expectedSandstormPoints = 0;

        double bestCrossingScore = 0.0;


        final int[][] levelCombinations = new int[][]{{2, 2, 1}, {2, 1, 2}, {1, 2, 2}, {1, 1, 2},
                {1, 2, 1}, {1, 1, 1}};

        // Iterate through all starting combinations
        for (int[] levelCombo : levelCombinations) {
            double crossingScore = 0.0;
            for (int i = 0; i < levelCombo.length; i++) {
                if (levelCombo[i] == 1) {
                    crossingScore += 3.0 * teamReports[i].getAttemptSuccessRates().get("levelOneCross");
                } else {
                    crossingScore += 6.0 * teamReports[i].getAttemptSuccessRates().get("levelTwoCross");
                }
            }

            if (crossingScore >= bestCrossingScore) {
                bestCrossingScore = crossingScore;
                bestStartingLevels = levelCombo;
            }
        }

        expectedSandstormPoints += bestCrossingScore;

        predictedValues.put("sandstormBonus", bestCrossingScore);

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
                    if (teamReports[i].getAbilities().get("frontCargoShipHatchSandstorm") && frontCargoShipCount < 2) {
                        cargoShipHatches += teamReports[i].getAttemptSuccessRates().get("hatchAutoSuccess");
                        frontCargoShipCount++;
                    } else if (teamReports[i].getAbilities().get("sideCargoShipHatchSandstorm")) {
                        cargoShipHatches += teamReports[i].getAttemptSuccessRates().get("hatchAutoSuccess");

                    } else if (teamReports[i].getAbilities().get("rocketHatchSandstorm")) {
                        rocketHatches += teamReports[i].getAttemptSuccessRates().get("hatchAutoSuccess");
                    }
                } else {
                    cargoShipCargo += teamReports[i].getAttemptSuccessRates().get("cargoAutoSuccess");
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

        expectedSandstormPoints += bestGamePieceScore;

        predictedValues.put("sandstormGamePiecePoints", bestGamePieceScore);
        predictedValues.put("sandstormPoints", expectedSandstormPoints);

        return expectedSandstormPoints;
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
                endgamePoints += climbPointValues[climbLevelCombo[i] - 1] * teamReports[i].getAttemptSuccessRates().get(
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
            if (teamReports[i].getAbilities().get("doubleBuddyClimb")) {
                endgamePoints += teamReports[i].getAverages().get("numPartnerClimbAssists") * (teamReports[i]
                        .getAbilities().get("levelThreeBuddyClimb") ? 12 : 6);
                climbLevels[i] = teamReports[i].findBestClimbLevel();
                endgamePoints += climbLevels[i] * climbPointValues[climbLevels[i] - 1];
                for (int j = 0; j < climbLevels.length; j++) {
                    if (j != i) {
                        climbLevels[j] = 0;
                    }
                }
            } else if (teamReports[i].getAbilities().get("singleBuddyClimb")) {
                endgamePoints += teamReports[i].getAverages().get("numPartnerClimbAssists") * (teamReports[i]
                        .getAbilities().get("levelThreeBuddyClimb") ? 12 : 6);

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

        totalHatches += cargoShipHatches;
        predictedValues.put("teleHatches", totalHatches);

        return totalHatches;

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

        return totalCargo;
    }

    private void calculateOptimalNullHatchPanels(double confidenceLevel) {
        // Make this a function of hatch panels placed in auto as well
        double averageCargoShipHatches = predictedValues.get("autoCargoShipHatches") + predictedValues.get(
                "teleCargoShipHatches");

        double standardDeviation = Stats.sumStandardDeviation(new double[]{
                standardDeviations.get("autoCargoShipHatches"), standardDeviations.get("teleCargoShipHatches")});
        int minSampleSize = Math.min(Math.min(teamReports[0].getEntries().size(), teamReports[1].getEntries().size())
                , teamReports[2].getEntries().size());

        double optimisticCargoShipHatches = averageCargoShipHatches;
        if (minSampleSize > 1) {
            optimisticCargoShipHatches = Stats.inverseTValue(confidenceLevel, minSampleSize, averageCargoShipHatches,
                    standardDeviation);

        }


        double nullHatches = Math.max(Math.min(8 - optimisticCargoShipHatches, 6), 0);
        predictedValues.put("optimalNullHatches", nullHatches);

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
                    teamReports[i].getStandardDeviations().get("level" + numStrNames[bestStartingLevels[i] - 1] +
                            "Cross"));
        }

        standardDeviations.put("sandstormBonus", Math.sqrt(sandstormBonusVariance));

        double sandstormGamePieceVariance = 0.0;
        double sandstormHatchVariance = 0.0;

        for (int i = 0; i < bestSandstormGamePieceCombo.length(); i++) {
            if (bestSandstormGamePieceCombo.charAt(i) == 'H') {
                sandstormGamePieceVariance += Stats.multiplyVariance(5, teamReports[i].getStandardDeviations().get(
                        "hatchAutoSuccess"));
                sandstormHatchVariance += Math.pow(teamReports[i].getStandardDeviations().get("hatchAutoSuccess"), 2);
            } else {
                sandstormGamePieceVariance += Stats.multiplyVariance(3, teamReports[i].getStandardDeviations().get(
                        "cargoAutoSuccess"));
            }
        }

        standardDeviations.put("autoCargoShipHatches", Math.sqrt(sandstormHatchVariance));
        standardDeviations.put("sandstormGamePiecePoints", Math.sqrt(sandstormGamePieceVariance));

        double sandstormPointsStdDev = Math.sqrt(sandstormBonusVariance + sandstormGamePieceVariance);

        standardDeviations.put("sandstormPoints", sandstormPointsStdDev);

        return sandstormPointsStdDev;
    }

    private double calculateStdDevEndgamePoints() {

        final int[] climbPointValues = new int[]{3, 6, 12};

        double endgameVariance = 0.0;

        for (int i = 0; i < bestClimbLevels.length; i++) {
            if (bestClimbLevels[i] != 0) {
                endgameVariance += Stats.multiplyVariance(climbPointValues[bestClimbLevels[i] - 1],
                        teamReports[i].getStandardDeviations().get("level" + numStrNames[bestClimbLevels[i] - 1] +
                                "Climb"
                        ));
            }
        }

        standardDeviations.put("endgamePoints", Math.sqrt(endgameVariance));

        return endgameVariance;
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


            double averageCargoShipHatches = predictedValues.get("autoCargoShipHatches") + predictedValues.get(
                    "teleCargoShipHatches");

            double nullHatches = Math.max(Math.min(8 - averageCargoShipHatches, 6), 0);

        }


        for (String metric : metricNames) {
            standardDeviations.put(metric, Stats.standardDeviation(simulationCalculatedValues.get(metric)));
        }


        //Restores values using the original alliance report
        calculateExpectedValues();
        calculatePredictedTeleOpPoints();

        return standardDeviations.get("telePoints");
    }

    public void calculateMonteCarloExpectedValues(ArrayList<HashMap<String, Double>> testSets) {
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

    public double calculatePredictedRp() {

        return calculateHabDockChance() + calculateRocketRpChance(generateMonteCarloSet());
    }

    public double calculateRocketRpChance(ArrayList<ArrayList<HashMap<String, Double>>> simulationRawValues) {

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
        predictedValues.put("rocketRpChance", rocketRpChance);

        return rocketRpChance;
    }


    public double calculateWinChance(AllianceReport opposingAlliance) {
        return 1.0;
    }

    public double calculateHabDockChance() {
        double habDockChance;
        try {
            habDockChance = Stats.rightTailNormalProbability(15, predictedValues.get("endgamePoints"),
                    standardDeviations.get("endgamePoints"));
        } catch (NotStrictlyPositiveException e) {
            habDockChance = predictedValues.get("endgamePoints") >= 15 ? 1.0 : 0.0;
        }
        predictedValues.put("habDockChance", habDockChance);

        return habDockChance;
    }

    public double calculatePredictedRp(AllianceReport opposingAlliance) {

        return calculateHabDockChance() + calculateRocketRpChance(generateMonteCarloSet()) + 2 * calculateWinChance(opposingAlliance);
    }
}
