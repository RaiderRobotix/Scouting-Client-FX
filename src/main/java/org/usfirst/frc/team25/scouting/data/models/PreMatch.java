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
    private boolean robotStartCargo;

    public PreMatch(String scoutName, String scoutPos, int matchNum,
                    int teamNum, boolean robotNoShow, boolean robotStartCargo) {
        this.scoutName = scoutName;
        this.scoutPos = scoutPos;
        this.matchNum = matchNum;
        this.teamNum = teamNum;
        this.robotNoShow = robotNoShow;
        this.robotStartCargo = robotStartCargo;
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


    public void setRobotStartBall(boolean robotStartCargo) { this.robotStartCargo = robotStartCargo; }

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

    public boolean isRobotNoShow() {
        return robotNoShow;
    }

    public boolean isRobotStartCargo() {
        return robotStartCargo;
    }
}
