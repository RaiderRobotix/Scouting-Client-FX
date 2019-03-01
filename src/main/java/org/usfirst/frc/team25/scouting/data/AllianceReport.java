package org.usfirst.frc.team25.scouting.data;

import java.util.HashMap;

/**
 * Class for alliance-based calculations and stats
 * Not used during the 2018 season
 */
public class AllianceReport {

    private TeamReport[] teamReports;
    private int[] bestStartingLevels;

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
            quickReport += key + ": " + Statistics.round(predictedValues.get(key), 2) + "\n";
        }

        return quickReport;
    }

    public void calculateStats() {

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


        calculatePredictedSandstormPoints();
        calculatePredictedTeleOpPoints();
        calculatePredictedEndgamePoints();

        predictedValues.put("totalPoints", predictedValues.get("endgamePoints") + predictedValues.get(
                "sandstormPoints") + predictedValues.get("telePoints"));


    }

    private void calculatePredictedTeleOpPoints() {
        double expectedTeleOpPoints = 0;

        double excessHatches = 0, excessCargo = 0, totalHatches = 0, totalCargo = 0;

        for (int i = 2; i >= 0; i--) {
            double cap = 4.0;

            if (i == 0) {
                // Allow cargo ship hatches to be interchangeable with lvl 1 hatches
                excessHatches += expectedValues.get("telecargoShipHatches");

                // Decrease cap due to rocket hatches placed
                cap -= predictedValues.get("autoRocketHatches");
            }

            if (excessHatches + expectedValues.get("telerocketLevel" + TeamReport.numberStringNames[i] + "Hatches") > cap) {

                // Cap is reached, place max remaining into the level

                excessHatches += expectedValues.get("telerocketLevel" + TeamReport.numberStringNames[i] + "Hatches") - cap;
                predictedValues.put("teleRocketLevel" + TeamReport.numberStringNames[i] + "Hatches", cap);
                totalHatches += cap;
            } else {

                // Place everything into the level
                predictedValues.put("teleRocketLevel" + TeamReport.numberStringNames[i] + "Hatches",
                        expectedValues.get("telerocketLevel" + TeamReport.numberStringNames[i] + "Hatches") + excessHatches);
                totalHatches += expectedValues.get("telerocketLevel" + TeamReport.numberStringNames[i] + "Hatches") + excessHatches;
                excessHatches = 0;
            }
        }

        double cargoShipHatches = Math.min(excessHatches, 8 - predictedValues.get("autoCargoShipHatches"));

        totalHatches += cargoShipHatches;

        predictedValues.put("teleCargoShipHatches", cargoShipHatches);

        predictedValues.put("teleHatches", totalHatches);

        for (int i = 2; i >= 0; i--) {
            double cap = predictedValues.get("teleRocketLevel" + TeamReport.numberStringNames[i] + "Hatches");

            if (i == 0) {
                // Allow cargo ship cargo to be interchangeable with lvl 1 cargo
                excessCargo += expectedValues.get("telecargoShipCargo");

                // Decrease cap due to rocket hatches placed
                cap -= predictedValues.get("autoRocketCargo");
            }

            if (excessCargo + expectedValues.get("telerocketLevel" + TeamReport.numberStringNames[i] + "Cargo") > cap) {

                excessCargo += expectedValues.get("telerocketLevel" + TeamReport.numberStringNames[i] + "Cargo") - cap;
                predictedValues.put("teleRocketLevel" + TeamReport.numberStringNames[i] + "Cargo", cap);

                totalCargo += cap;
            } else {

                predictedValues.put("teleRocketLevel" + TeamReport.numberStringNames[i] + "Cargo",
                        expectedValues.get("telerocketLevel" + TeamReport.numberStringNames[i] + "Cargo") + excessCargo);
                totalCargo += expectedValues.get("telerocketLevel" + TeamReport.numberStringNames[i] + "Cargo") + excessCargo;
                excessCargo = 0;
            }
        }

        double cargoShipCargo = Math.min(Math.min(excessCargo, 8 - predictedValues.get("autoCargoShipCargo")),
                predictedValues.get("teleCargoShipHatches"));

        totalCargo += cargoShipCargo;

        predictedValues.put("teleCargoShipCargo", cargoShipCargo);
        predictedValues.put("teleCargo", totalCargo);

        expectedTeleOpPoints = 2 * totalHatches + 3 * totalCargo;

        predictedValues.put("telePoints", expectedTeleOpPoints);
        predictedValues.put("optimalNullHatches",
                Math.max(Math.min(8 - predictedValues.get("autoCargoShipHatches") - cargoShipHatches, 6), 0));
    }

    private void calculatePredictedEndgamePoints() {
        double expectedEndgamePoints = 0;
        predictedValues.put("endgamePoints", 0.0);
    }

    private void calculatePredictedSandstormPoints() {
        double expectedSandstormPoints = 0;

        double bestCrossingScore = 0.0;


        final int[][] levelCombinations = new int[][]{{2, 2, 1}, {2, 1, 2}, {1, 2, 2}, {1, 1, 2}, {1, 2, 1}, {1, 1, 1}};

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

        predictedValues.put("sandstormPoints", 0.0);
    }
}
