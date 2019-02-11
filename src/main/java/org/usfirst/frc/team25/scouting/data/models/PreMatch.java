package org.usfirst.frc.team25.scouting.data.models;


/**
 * General information about a match and scout before it begins
 */
public class PreMatch {

    private String scoutName;
    private String scoutPos;
    private String startingPos;
    private int matchNum, teamNum;
    private int startingLevel;
    private boolean robotNoShow;
    private String startingGamePiece;

    public String getScoutName() {
        return scoutName;
    }

    public String getScoutPos() {
        return scoutPos;
    }

    public PreMatch(String scoutName, String scoutPos, String startingPos, int matchNum,
                    int teamNum, int startingLevel, boolean robotNoShow, String startingGamePiece) {
        this.scoutName = scoutName;
        this.scoutPos = scoutPos;
        this.startingPos = startingPos;
        this.matchNum = matchNum;
        this.teamNum = teamNum;
        this.startingLevel = startingLevel;
        this.robotNoShow = robotNoShow;
        this.startingGamePiece = startingGamePiece;
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

    public int getStartingLevel() {
        return startingLevel;
    }

    public boolean isRobotNoShow() {
        return robotNoShow;
    }

    public String getStartingGamePiece() {
        return startingGamePiece;
    }
}


