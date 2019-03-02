package org.usfirst.frc.team25.scouting.data;

import java.util.HashMap;

/**
 * Class for alliance-based calculations and stats
 * Not used during the 2018 season
 */
public class AllianceReport {

    private TeamReport[] teamReports;
    private int[] bestStartingLevels;
    final String[] numStrNames = new String[]{"One", "Two", "Three", "total"};
    private final double NULL_HATCH_CONFIDENCE = 0.9;

    private HashMap<String, Double> predictedValues;

    private HashMap<String, Double> expectedValues;

    public AllianceReport(TeamReport teamOne, TeamReport teamTwo, TeamReport teamThree) {

        this.teamReports = new TeamReport[]{teamOne, teamTwo, teamThree};

        expectedValues = new HashMap<>();
        predictedValues = new HashMap<>();
        bestStartingLevels = new int[3];
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
        calculateOptimalNullHatchPanels(NULL_HATCH_CONFIDENCE);

        predictedValues.put("totalPoints", sandstormPoints + teleOpPoints + sandstormPoints);

    }

    private void calculateOptimalNullHatchPanels(double confidenceLevel) {
        predictedValues.put("optimalNullHatches",
                Math.max(Math.min(8 - predictedValues.get("autoCargoShipHatches") - predictedValues.get(
                        "teleCargoShipHatches"), 6), 0));
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

    private double calculatePredictedTeleOpPoints() {

        double teleOpHatches = calculatePredictedTeleOpHatchPanels();
        double teleOpCargo = calculatePredictedTeleOpCargo();

        double expectedTeleOpPoints = 2 * teleOpHatches + 3 * teleOpCargo;

        predictedValues.put("telePoints", expectedTeleOpPoints);

        return expectedTeleOpPoints;
    }

    private double calculatePredictedTeleOpHatchPanels() {

        double excessHatches = 0;
        double totalHatches = 0;


        for (int i = 2; i >= 0; i--) {
            double cap = 4.0;

            if (i == 0) {
                // Allow cargo ship hatches to be interchangeable with lvl 1 hatches
                excessHatches += expectedValues.get("telecargoShipHatches");

                // Decrease cap due to rocket hatches placed
                cap -= predictedValues.get("autoRocketHatches");
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

    private double calculatePredictedTeleOpCargo() {
        double excessCargo = 0;
        double totalCargo = 0;

        for (int i = 2; i >= 0; i--) {
            double cap = predictedValues.get("teleRocketLevel" + numStrNames[i] + "Hatches");

            if (i == 0) {
                // Allow cargo ship cargo to be interchangeable with lvl 1 cargo
                excessCargo += expectedValues.get("telecargoShipCargo");

                // Decrease cap due to rocket hatches placed
                cap -= predictedValues.get("autoRocketCargo");
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

    private double calculatePredictedEndgamePoints() {
        double expectedEndgamePoints = 0;
        predictedValues.put("endgamePoints", 0.0);

        return expectedEndgamePoints;
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
                    crossingScore += 3.0 *
                            (double) teamReports[i].getCounts().get("levelOneCross") / teamReports[i].getCounts().get("levelOneStart");
                } else {
                    crossingScore += 6.0 *
                            (double) teamReports[i].getCounts().get("levelTwoCross") / teamReports[i].getCounts().get("levelTwoStart");
                }
            }

            if (crossingScore >= bestCrossingScore) {
                bestCrossingScore = crossingScore;
                bestStartingLevels = levelCombo;
            }
        }

        predictedValues.put("autoCargoShipCargo", 0.0);
        predictedValues.put("autoCargoShipHatches", 0.0);
        predictedValues.put("autoRocketHatches", 0.0);
        predictedValues.put("autoRocketCargo", 0.0);

        predictedValues.put("sandstormPoints", 0.0);

        return expectedSandstormPoints;
    }
}
