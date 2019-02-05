package org.usfirst.frc.team25.scouting.data;

/**
 * Class for alliance-based calculations and stats
 * Not used during the 2018 season
 */
public class AllianceReport {

    private TeamReport[] teamReports;

    //Add more here
    private double expectedScore, expectedAutoPoints, expectedClimbPoints, expectedTeleOpPoints, expectedBonusRp;

    public AllianceReport(TeamReport teamOne, TeamReport teamTwo, TeamReport teamThree) {

        this.teamReports = new TeamReport[]{teamOne, teamTwo, teamThree};
    }


    public void calculateStats() {
        //Calculate declared variables here
    }

    public String getQuickAllianceReport() {
        //TODO write this
        return "";
    }
}
