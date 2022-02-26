package org.usfirst.frc.team25.scouting.data.models;

/**
 * Object model for autonomous (or sandstorm) period of a match
 */
public class Autonomous {


    private int RobotCargoPickedup;
    private int RobotCargoScoredUpperHub;
    private int RobotCargoScoredLowerHub;
    private int robotCargoDropped;
    private int humanCargoScored;
    private int humanCargoMissed;
    private boolean robotPassTarmac;
    private boolean robotCommitFoul;


    public Autonomous( int RobotCargoPickedup, int RobotCargoScoredUpperHub,
                       int RobotCargoScoredLowerHub, int robotCargoDropped,int humanCargoScored, int humanCargoMissed,
                       boolean robotPassTarmac, boolean robotCommitFoul) {
        this.robotCommitFoul = robotCommitFoul;
        this.RobotCargoPickedup = RobotCargoPickedup;
        this.RobotCargoScoredUpperHub = RobotCargoScoredUpperHub;
        this.RobotCargoScoredLowerHub = RobotCargoScoredLowerHub;
        this.robotCargoDropped = robotCargoDropped;
        this.humanCargoScored = humanCargoScored;
        this.humanCargoMissed = humanCargoMissed;
        this.robotPassTarmac = robotPassTarmac;

    }

    public boolean isRobotCommitFoul() {
        return robotCommitFoul;
    }

    public int getRobotCargoPickedup() {
        return RobotCargoPickedup;
    }

    public int getRobotCargoScoredUpperHub() {
        return RobotCargoScoredUpperHub;
    }

    public int getRobotCargoScoredLowerHub() {
        return RobotCargoScoredLowerHub;
    }

    public int getRobotCargoDropped() {
        return robotCargoDropped;
    }

    public int getHumanCargoScored() {
        return humanCargoScored;
    }

    public int getHumanCargoMissed() {
        return humanCargoMissed;
    }

    public boolean isRobotPassTarmac() {
        return robotPassTarmac;
    }

    public void setHumanCargoMissed(int humanCargoMissed) {
        this.humanCargoMissed = humanCargoMissed;
    }

    public void setHumanCargoScored(int humanCargoScored) {
        this.humanCargoScored = humanCargoScored;
    }

    public void setRobotCargoPickedup(int robotCargoPickedup) {
        RobotCargoPickedup = robotCargoPickedup;
    }

    public void setRobotCargoDropped(int robotCargoDropped) {
        this.robotCargoDropped = robotCargoDropped;
    }

    public void setRobotCargoScoredLowerHub(int robotCargoScoredLowerHub) {
        RobotCargoScoredLowerHub = robotCargoScoredLowerHub;
    }

    public void setRobotCargoScoredUpperHub(int robotCargoScoredUpperHub) {
        RobotCargoScoredUpperHub = robotCargoScoredUpperHub;
    }

    public void setRobotCommitFoul(boolean robotCommitFoul) {
        this.robotCommitFoul = robotCommitFoul;
    }

    public void setRobotPassTarmac(boolean robotPassTarmac) {
        this.robotPassTarmac = robotPassTarmac;
    }

}
