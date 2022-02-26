package org.usfirst.frc.team25.scouting.data.models;


/**
 * Container holding data from the tele-operated period
 * Includes endgame data
 */
public class TeleOp {

    private int robotCargoPickedUp;
    private int robotCargoScoredUpperHub;
    private int robotCargoScoredLowerHub;
    private int robotCargoDropped;
    private int successRungClimbLevel;
    private int attemptRungClimbLevel;
    private int climbTime;

    private boolean attemptRungClimb;




    public TeleOp(int robotCargoPickedUp , int robotCargoScoredUpperHub , int robotCargoScoredLowerHub ,
                  int robotCargoDropped , int successRungClimbLevel ,
                  boolean attemptRungClimb , int attemptRungClimbLevel , int climbTime) {
        this.robotCargoPickedUp = robotCargoPickedUp;
        this.robotCargoScoredUpperHub = robotCargoScoredUpperHub;
        this.robotCargoScoredLowerHub = robotCargoScoredLowerHub;
        this.robotCargoDropped = robotCargoDropped;
        this.attemptRungClimbLevel = attemptRungClimbLevel;
        this.successRungClimbLevel = successRungClimbLevel;
        this.attemptRungClimb = attemptRungClimb;
        this.climbTime = climbTime;
    }

    public long getClimbTime(){return climbTime;}

    public int getRobotCargoPickedUp() {
        return robotCargoPickedUp;
    }

    public int getRobotCargoScoredUpperHub() {
        return robotCargoScoredUpperHub;
    }

    public int getRobotCargoScoredLowerHub() {
        return robotCargoScoredLowerHub;
    }

    public int getRobotCargoDropped() {
        return robotCargoDropped;
    }

    public int getAttemptRungClimbLevel() {
        return attemptRungClimbLevel;
    }

    public int getSuccessRungClimbLevel() {
        return successRungClimbLevel;
    }

    public boolean isAttemptRungClimb() {
        return attemptRungClimb;
    }

    public void setRobotCargoScoredUpperHub(int robotCargoScoredUpperHub) {
        this.robotCargoScoredUpperHub = robotCargoScoredUpperHub;
    }

    public void setRobotCargoScoredLowerHub(int robotCargoScoredLowerHub) {
        this.robotCargoScoredLowerHub = robotCargoScoredLowerHub;
    }

    public void setRobotCargoDropped(int robotCargoDropped) {
        this.robotCargoDropped = robotCargoDropped;
    }

    public void setRobotCargoPickedUp(int robotCargoPickedUp) {
        this.robotCargoPickedUp = robotCargoPickedUp;
    }

    public void setClimbTime(int climbTime) {
        this.climbTime = climbTime;
    }

    public void setAttemptRungClimb(boolean attemptRungClimb) {
        this.attemptRungClimb = attemptRungClimb;
    }

    public void setAttemptRungClimbLevel(int attemptRungClimbLevel) {
        this.attemptRungClimbLevel = attemptRungClimbLevel;
    }

    public void setSuccessRungClimbLevel(int successRungClimbLevel) {
        this.successRungClimbLevel = successRungClimbLevel;
    }
}
