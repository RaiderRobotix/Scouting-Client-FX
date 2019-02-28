package org.usfirst.frc.team25.scouting.data;

import java.util.HashMap;

/**
 * Class for alliance-based calculations and stats
 * Not used during the 2018 season
 */
public class AllianceReport {

    private TeamReport[] teamReports;
    private final String[] expectedValueMetrics = new String[]{
            "totalHatches", "totalCargo", "totalCycles", "totalHatchesDropped", "teleOpHatches", "teleOpCargo", "telerocketLevelOneCargo"
    };
    private HashMap<String, Double> predictedValues;

    private HashMap<String, Double> expectedValues;

    public AllianceReport(TeamReport teamOne, TeamReport teamTwo, TeamReport teamThree) {

        this.teamReports = new TeamReport[]{teamOne, teamTwo, teamThree};

        expectedValues = new HashMap<>();
    }

    public String getQuickAllianceReport() {

        calculateStats();

        String quickReport = "";
        for (TeamReport report : teamReports) {
            quickReport += "Team " + report.getTeamNum();
            if (!report.getTeamName().isEmpty()) {
                quickReport += " - " + report.getTeamName();
            }
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
                expectedValues.put(metric, value);
            }
        }


        calculatePredictedSandsttormPoints();
        calculatePredictedTeleOpPoints();
        calculatePredictedEndgamePoints();

        predictedValues.put("totalPoints", predictedValues.get("endgamePoints") + predictedValues.get(
                "sandstormPoints") + predictedValues.get("teleOpPoints"));


    }

    private void calculatePredictedTeleOpPoints() {
        double expectedTeleOpPoints = 0;

        String[] numberStringNames = new String[]{"One", "Two", "Three", "total"};

        for (int i = 2; i >= 0; i--) {
            double expectedHatchValue = 0;

            for (TeamReport teamReport : teamReports) {
                expectedHatchValue += teamReport.getAverages().get("");
            }

        }


    }

    private void calculatePredictedEndgamePoints() {
        double expectedEndgamePoints = 0;
    }

    private void calculatePredictedSandsttormPoints() {
        double expectedSandstormPoints = 0;
    }
}
