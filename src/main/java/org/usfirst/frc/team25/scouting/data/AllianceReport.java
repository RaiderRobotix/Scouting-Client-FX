package org.usfirst.frc.team25.scouting.data;

import java.util.HashMap;

/**
 * Class for alliance-based calculations and stats
 * Not used during the 2018 season
 */
public class AllianceReport {

    private TeamReport[] teamReports;
    private final String[] expectedValueMetrics = new String[]{
            "totalHatches", "totalCargo", "totalCycles", "totalHatchesDropped", "teleOpHatches", "teleOpCargo"
    };


    private HashMap<String, Double> expectedValues;

    public AllianceReport(TeamReport teamOne, TeamReport teamTwo, TeamReport teamThree) {

        this.teamReports = new TeamReport[]{teamOne, teamTwo, teamThree};

        expectedValues = new HashMap<>();
    }


    public void calculateStats() {

    }

    public String getQuickAllianceReport() {

        return "";
    }
}
