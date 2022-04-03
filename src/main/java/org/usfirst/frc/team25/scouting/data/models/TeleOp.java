package org.usfirst.frc.team25.scouting.data.models;


/**
 * Container holding data from the tele-operated period
 * Includes endgame data
 */
public class TeleOp {

    private int cargoUpperHub;
    private int cargoLowerHub;
    private boolean attemptClimb;
    private int climbLevel;
    private boolean successClimb;
    private int climbTime;

    public TeleOp(int cargoUpperHub, int climbTime, int cargoLowerHub, boolean attemptClimb, int climbLevel,
                  boolean successClimb) {
        this.cargoUpperHub = cargoUpperHub;
        this.cargoLowerHub = cargoLowerHub;
        this.attemptClimb = attemptClimb;
        this.climbLevel = climbLevel;
        this.successClimb = successClimb;
        this.climbTime = climbTime;
    }

    public void setCargoUpperHub(int cargoUpperHub) {
        this.cargoUpperHub = cargoUpperHub;
    }

    public void setCargoLowerHub(int cargoLowerHub) {
        this.cargoLowerHub = cargoLowerHub;
    }

    public void setClimbLevel(int climbLevel) {
        this.climbLevel = climbLevel;
    }

    public void setClimbTime(int climbTime) { this.climbTime = climbTime;}

    public void setSuccessClimb(boolean successClimb) {
        this.successClimb = successClimb;
    }

    public void setAttemptClimb(boolean attemptClimb) {
        this.attemptClimb = attemptClimb;
    }

    public int getCargoUpperHub() {
        return cargoUpperHub;
    }

    public int getCargoLowerHub() {
        return cargoLowerHub;
    }

    public int getClimbLevel() {
        return climbLevel;
    }

    public int getClimbTime() {
        return climbTime;
    }

    public boolean isAttemptClimb() {
        return attemptClimb;
    }

    public boolean isSuccessClimb() {
        return successClimb;
    }
}
