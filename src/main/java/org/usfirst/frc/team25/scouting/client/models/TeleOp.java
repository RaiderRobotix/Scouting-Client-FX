package org.usfirst.frc.team25.scouting.client.models;

/**
 * Container holding data from the tele-operated period
 * Includes endgame data
 */
public class TeleOp {

    private final int cargoShipHatches;
    private final int cargoShipCargo;
    private final int rocketLevelOneCargo;
    private final int rocketLevelOneHatches;
    private final int rocketLevelTwoCargo;
    private final int rocketLevelTwoHatches;
    private final int rocketLevelThreeCargo;
    private final int rocketLevelThreeHatches;
    private final int hatchesDropped;
    private final int cargoDropped;

    private final boolean climbAssisted;
    private final int attemptHabClimbLevel;
    private final int successHabClimbLevel;
    private final boolean attemptHabClimb;
    private final boolean successHabClimb;
    private final int assistingClimbTeamNumber;
    private final int otherRobotClimbsAssisted;
    private final int highestClimbAssisted;

    public TeleOp(int cargoShipHatches, int cargoShipCargo, int rocketLevelOneCargo,
                  int rocketLevelOneHatches, int rocketLevelTwoCargo, int rocketLevelTwoHatches,
                  int rocketLevelThreeCargo, int rocketLevelThreeHatches, int hatchesDropped,
                  int cargoDropped, boolean climbAssisted, int attemptHabClimbLevel,
                  int successHabClimbLevel, boolean attemptHabClimb, boolean successHabClimb,
                  int assistingClimbTeamNumber, int otherRobotClimbsAssisted,
                  int highestClimbAssisted) {
        this.cargoShipHatches = cargoShipHatches;
        this.cargoShipCargo = cargoShipCargo;
        this.rocketLevelOneCargo = rocketLevelOneCargo;
        this.rocketLevelOneHatches = rocketLevelOneHatches;
        this.rocketLevelTwoCargo = rocketLevelTwoCargo;
        this.rocketLevelTwoHatches = rocketLevelTwoHatches;
        this.rocketLevelThreeCargo = rocketLevelThreeCargo;
        this.rocketLevelThreeHatches = rocketLevelThreeHatches;
        this.hatchesDropped = hatchesDropped;
        this.cargoDropped = cargoDropped;
        this.climbAssisted = climbAssisted;
        this.attemptHabClimbLevel = attemptHabClimbLevel;
        this.successHabClimbLevel = successHabClimbLevel;
        this.attemptHabClimb = attemptHabClimb;
        this.successHabClimb = successHabClimb;
        this.assistingClimbTeamNumber = assistingClimbTeamNumber;
        this.otherRobotClimbsAssisted = otherRobotClimbsAssisted;
        this.highestClimbAssisted = highestClimbAssisted;
    }

    public int getCargoShipHatches() {
        return cargoShipHatches;
    }

    public int getCargoShipCargo() {
        return cargoShipCargo;
    }

    public int getRocketLevelOneCargo() {
        return rocketLevelOneCargo;
    }

    public int getRocketLevelOneHatches() {
        return rocketLevelOneHatches;
    }

    public int getRocketLevelTwoCargo() {
        return rocketLevelTwoCargo;
    }

    public int getRocketLevelTwoHatches() {
        return rocketLevelTwoHatches;
    }

    public int getRocketLevelThreeCargo() {
        return rocketLevelThreeCargo;
    }

    public int getRocketLevelThreeHatches() {
        return rocketLevelThreeHatches;
    }

    public int getHatchesDropped() {
        return hatchesDropped;
    }

    public int getCargoDropped() {
        return cargoDropped;
    }

    public boolean isClimbAssisted() {
        return climbAssisted;
    }

    public int getAttemptHabClimbLevel() {
        return attemptHabClimbLevel;
    }

    public int getSuccessHabClimbLevel() {
        return successHabClimbLevel;
    }

    public boolean isAttemptHabClimb() {
        return attemptHabClimb;
    }

    public boolean isSuccessHabClimb() {
        return successHabClimb;
    }

    public int getAssistingClimbTeamNumber() {
        return assistingClimbTeamNumber;
    }

    public int getOtherRobotClimbsAssisted() {
        return otherRobotClimbsAssisted;
    }

    public int getHighestClimbAssisted() {
        return highestClimbAssisted;
    }
}