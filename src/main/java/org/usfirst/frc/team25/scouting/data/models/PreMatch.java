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
    private String startingPos;
    private boolean robotStartBall;



    public PreMatch(String scoutName, String scoutPos, int matchNum,
                    int teamNum, boolean robotNoShow , boolean robotStartBall ) {
        this.scoutName = scoutName;
        this.scoutPos = scoutPos;
        this.startingPos = startingPos;
        this.matchNum = matchNum;
        this.teamNum = teamNum;
        this.robotNoShow = robotNoShow;
        this.robotStartBall = robotStartBall;

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

    public void setRobotStartBall(boolean robotStartBall) {
        this.robotStartBall = robotStartBall;
    }

    public void setStartingPos(String startingPos) {
        this.startingPos = startingPos;
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

    public boolean isRobotStartBall() {
        return robotStartBall;
    }

    public boolean isRobotNoShow() {
        return robotNoShow;
    }


}
