package org.usfirst.frc.team25.scouting.data.models;


/**
 * Container holding data from the tele-operated period
 * Includes endgame data
 */
public class TeleOp {

    private int cargoShipHatches;
    private int cargoShipCargo;
    private int rocketLevelOneCargo;
    private int rocketLevelOneHatches;
    private int rocketLevelTwoCargo;
    private int rocketLevelTwoHatches;
    private int rocketLevelThreeCargo;
    private int rocketLevelThreeHatches;
    private int hatchesDropped;
    private int cargoDropped;

    private boolean climbAssistedByPartners;
    private int attemptHabClimbLevel;
    private int successHabClimbLevel;
    private boolean attemptHabClimb;
    private boolean successHabClimb;
    private int assistingClimbTeamNumber;
    private int numberOfPartnerClimbsAssisted;
    private int highestClimbAssisted;

    public TeleOp(int cargoShipHatches, int cargoShipCargo, int rocketLevelOneCargo,
                  int rocketLevelOneHatches, int rocketLevelTwoCargo, int rocketLevelTwoHatches,
                  int rocketLevelThreeCargo, int rocketLevelThreeHatches, int hatchesDropped,
                  int cargoDropped, boolean climbAssistedByPartners, int attemptHabClimbLevel,
                  int successHabClimbLevel, boolean attemptHabClimb, boolean successHabClimb,
                  int assistingClimbTeamNumber, int numberOfPartnerClimbsAssisted,
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
        this.climbAssistedByPartners = climbAssistedByPartners;
        this.attemptHabClimbLevel = attemptHabClimbLevel;
        this.successHabClimbLevel = successHabClimbLevel;
        this.attemptHabClimb = attemptHabClimb;
        this.successHabClimb = successHabClimb;
        this.assistingClimbTeamNumber = assistingClimbTeamNumber;
        this.numberOfPartnerClimbsAssisted = numberOfPartnerClimbsAssisted;
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

    public boolean isClimbAssistedByPartners() {
        return climbAssistedByPartners;
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

    public int getNumberOfPartnerClimbsAssisted() {
        return numberOfPartnerClimbsAssisted;
    }

    public int getHighestClimbAssisted() {
        return highestClimbAssisted;
    }
}
