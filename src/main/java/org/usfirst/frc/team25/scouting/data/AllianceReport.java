package org.usfirst.frc.team25.scouting.data;

import java.util.HashMap;

/**
 * Class for alliance-based calculations and stats
 * Not used during the 2018 season
 */
public class AllianceReport {

    private TeamReport[] teamReports;

    private HashMap<String, Double> predictedValues;

    private HashMap<String, Double> expectedValues;

    public AllianceReport(TeamReport teamOne, TeamReport teamTwo, TeamReport teamThree) {

        this.teamReports = new TeamReport[]{teamOne, teamTwo, teamThree};

        expectedValues = new HashMap<>();
        predictedValues = new HashMap<>();
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

        //TODO incorporate auto hatches into calculation, allow swap between lvl 1 and cargo ship hatches/cargo
        for (int i = 2; i >= 0; i--) {
            if (excessHatches + expectedValues.get("telerocketLevel" + TeamReport.numberStringNames[i] + "Hatches") > 4) {
                excessHatches += expectedValues.get("telerocketLevel" + TeamReport.numberStringNames[i] +
                        "Hatches") - 4;
                predictedValues.put("teleRocketLevel" + TeamReport.numberStringNames[i] +
                        "Hatches", 4.0);
                totalHatches += 4.0;
            } else {
                predictedValues.put("teleRocketLevel" + TeamReport.numberStringNames[i] +
                                "Hatches",
                        expectedValues.get("telerocketLevel" + TeamReport.numberStringNames[i] + "Hatches") + excessHatches);
                totalHatches += expectedValues.get("telerocketLevel" + TeamReport.numberStringNames[i] + "Hatches") + excessHatches;
                excessHatches = 0;
            }
        }

        double cargoShipHatches = Math.min(expectedValues.get("telecargoShipHatches") + excessHatches,
                8 - predictedValues.get("autoCargoShipHatches"));
        totalHatches += cargoShipHatches;

        predictedValues.put("teleCargoShipHatches", cargoShipHatches);

        predictedValues.put("teleHatches", totalHatches);

        for (int i = 2; i >= 0; i--) {
            double cap = predictedValues.get("teleRocketLevel" + TeamReport.numberStringNames[i] + "Hatches");
            if (excessCargo + expectedValues.get("telerocketLevel" + TeamReport.numberStringNames[i] + "Cargo") > cap) {
                excessCargo += expectedValues.get("telerocketLevel" + TeamReport.numberStringNames[i] +
                        "Cargo") - cap;
                predictedValues.put("teleRocketLevel" + TeamReport.numberStringNames[i] +
                        "Cargo", cap);
                totalCargo += cap;
            } else {
                predictedValues.put("teleRocketLevel" + TeamReport.numberStringNames[i] +
                                "Cargo",
                        expectedValues.get("telerocketLevel" + TeamReport.numberStringNames[i] + "Cargo") + excessCargo);
                totalCargo += expectedValues.get("telerocketLevel" + TeamReport.numberStringNames[i] + "Cargo") + excessCargo;
                excessCargo = 0;
            }
        }

        double cargoShipCargo = Math.min(Math.min(expectedValues.get("telecargoShipCargo"), 8 - predictedValues.get(
                "autoCargoShipCargo")), predictedValues.get("teleCargoShipHatches"));

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
        predictedValues.put("autoCargoShipCargo", 0.0);
        predictedValues.put("autoCargoShipHatches", 0.0);

        predictedValues.put("sandstormPoints", 0.0);
    }
}
