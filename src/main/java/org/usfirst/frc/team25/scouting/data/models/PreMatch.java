package org.usfirst.frc.team25.scouting.data.models;


/**
 * General information about a match and scout before it begins
 */
public class PreMatch {

    private String scoutName;
    private String scoutPos;
    private String startingPos;
    private int matchNum, teamNum;

    public PreMatch(String scoutName, String scoutPos, int matchNum, int teamNum, String startingPos) {
        this.scoutName = scoutName;
        this.scoutPos = scoutPos;
        this.matchNum = matchNum;
        this.teamNum = teamNum;
        this.startingPos = startingPos;
    }

    public PreMatch() {
        //Default empty constructor for JSON parsing
    }

    public String getScoutName() {
        return scoutName;
    }

    public String getScoutPos() {
        return scoutPos;
    }

    public int getMatchNum() {
        return matchNum;
    }

    public int getTeamNum() {
        return teamNum;
    }

    public String getStartingPos() {
        return startingPos;
    }
}

