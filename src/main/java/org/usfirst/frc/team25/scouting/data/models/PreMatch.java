package org.usfirst.frc.team25.scouting.data.models;


/**
 * General information about a match and scout before it begins
 */
public class PreMatch {

    private String scoutName;
    private int matchNum;
    private String scoutPos;
    private int teamNum;
    private boolean robotNoShow;
    private int startingLevel;
    private String startingPos;
    private String startingGamePiece;

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

    public void setScoutName(String scoutName) {
        this.scoutName = scoutName;
    }

    public void setMatchNum(int matchNum) {
        this.matchNum = matchNum;
    }

    public void setScoutPos(String scoutPos) {
        this.scoutPos = scoutPos;
    }

    public void setTeamNum(int teamNum) {
        this.teamNum = teamNum;
    }

    public void setRobotNoShow(boolean robotNoShow) {
        this.robotNoShow = robotNoShow;
    }

    public void setStartingLevel(int startingLevel) {
        this.startingLevel = startingLevel;
    }

    public void setStartingPos(String startingPos) {
        this.startingPos = startingPos;
    }

    public void setStartingGamePiece(String startingGamePiece) {
        this.startingGamePiece = startingGamePiece;
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
