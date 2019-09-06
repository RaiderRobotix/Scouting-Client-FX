package org.usfirst.frc.team25.scouting.data.models;


/**
 * Container holding data from the tele-operated period
 * Includes endgame data
 */
public class TeleOp {

    private int cargoShipHatches;
    private int rocketLevelOneHatches;
    private int rocketLevelTwoHatches;
    private int rocketLevelThreeHatches;
    private int cargoShipCargo;
    private int rocketLevelOneCargo;
    private int rocketLevelTwoCargo;
    private int rocketLevelThreeCargo;

    private int hatchesDropped;
    private int cargoDropped;

    private boolean attemptHabClimb;
    private int attemptHabClimbLevel;
    private boolean successHabClimb;
    private int successHabClimbLevel;
    private boolean climbAssistedByPartner;
    private int assistingClimbTeamNum;
    private int numPartnerClimbAssists;
    private int partnerClimbAssistStartLevel;
    private int partnerClimbAssistEndLevel;

    public TeleOp(int cargoShipHatches, int rocketLevelOneHatches, int rocketLevelTwoHatches,
                  int rocketLevelThreeHatches, int cargoShipCargo, int rocketLevelOneCargo,
                  int rocketLevelTwoCargo, int rocketLevelThreeCargo, int hatchesDropped,
                  int cargoDropped, boolean attemptHabClimb, int attemptHabClimbLevel,
                  boolean successHabClimb, int successHabClimbLevel,
                  boolean climbAssistedByPartner, int assistingClimbTeamNum,
                  int numPartnerClimbAssists, int partnerClimbAssistEndLevel,
                  int partnerClimbAssistStartLevel) {
        this.cargoShipHatches = cargoShipHatches;
        this.rocketLevelOneHatches = rocketLevelOneHatches;
        this.rocketLevelTwoHatches = rocketLevelTwoHatches;
        this.rocketLevelThreeHatches = rocketLevelThreeHatches;
        this.cargoShipCargo = cargoShipCargo;
        this.rocketLevelOneCargo = rocketLevelOneCargo;
        this.rocketLevelTwoCargo = rocketLevelTwoCargo;
        this.rocketLevelThreeCargo = rocketLevelThreeCargo;
        this.hatchesDropped = hatchesDropped;
        this.cargoDropped = cargoDropped;
        this.attemptHabClimb = attemptHabClimb;
        this.attemptHabClimbLevel = attemptHabClimbLevel;
        this.successHabClimb = successHabClimb;
        this.successHabClimbLevel = successHabClimbLevel;
        this.climbAssistedByPartner = climbAssistedByPartner;
        this.assistingClimbTeamNum = assistingClimbTeamNum;
        this.numPartnerClimbAssists = numPartnerClimbAssists;
        this.partnerClimbAssistEndLevel = partnerClimbAssistEndLevel;
        this.partnerClimbAssistStartLevel = partnerClimbAssistStartLevel;
    }

    public void setCargoShipHatches(int cargoShipHatches) {
        this.cargoShipHatches = cargoShipHatches;
    }

    public void setRocketLevelOneHatches(int rocketLevelOneHatches) {
        this.rocketLevelOneHatches = rocketLevelOneHatches;
    }

    public void setRocketLevelTwoHatches(int rocketLevelTwoHatches) {
        this.rocketLevelTwoHatches = rocketLevelTwoHatches;
    }

    public void setRocketLevelThreeHatches(int rocketLevelThreeHatches) {
        this.rocketLevelThreeHatches = rocketLevelThreeHatches;
    }

    public void setCargoShipCargo(int cargoShipCargo) {
        this.cargoShipCargo = cargoShipCargo;
    }

    public void setRocketLevelOneCargo(int rocketLevelOneCargo) {
        this.rocketLevelOneCargo = rocketLevelOneCargo;
    }

    public void setRocketLevelTwoCargo(int rocketLevelTwoCargo) {
        this.rocketLevelTwoCargo = rocketLevelTwoCargo;
    }

    public void setRocketLevelThreeCargo(int rocketLevelThreeCargo) {
        this.rocketLevelThreeCargo = rocketLevelThreeCargo;
    }

    public void setHatchesDropped(int hatchesDropped) {
        this.hatchesDropped = hatchesDropped;
    }

    public void setCargoDropped(int cargoDropped) {
        this.cargoDropped = cargoDropped;
    }

    public void setAttemptHabClimb(boolean attemptHabClimb) {
        this.attemptHabClimb = attemptHabClimb;
    }

    public void setAttemptHabClimbLevel(int attemptHabClimbLevel) {
        this.attemptHabClimbLevel = attemptHabClimbLevel;
    }

    public void setSuccessHabClimb(boolean successHabClimb) {
        this.successHabClimb = successHabClimb;
    }

    public void setSuccessHabClimbLevel(int successHabClimbLevel) {
        this.successHabClimbLevel = successHabClimbLevel;
    }

    public void setClimbAssistedByPartner(boolean climbAssistedByPartner) {
        this.climbAssistedByPartner = climbAssistedByPartner;
    }

    public void setAssistingClimbTeamNum(int assistingClimbTeamNum) {
        this.assistingClimbTeamNum = assistingClimbTeamNum;
    }

    public void setNumPartnerClimbAssists(int numPartnerClimbAssists) {
        this.numPartnerClimbAssists = numPartnerClimbAssists;
    }

    public void setPartnerClimbAssistStartLevel(int partnerClimbAssistStartLevel) {
        this.partnerClimbAssistStartLevel = partnerClimbAssistStartLevel;
    }

    public void setPartnerClimbAssistEndLevel(int partnerClimbAssistEndLevel) {
        this.partnerClimbAssistEndLevel = partnerClimbAssistEndLevel;
    }

    public int getCargoShipHatches() {
        return cargoShipHatches;
    }

    public int getRocketLevelOneHatches() {
        return rocketLevelOneHatches;
    }

    public int getRocketLevelTwoHatches() {
        return rocketLevelTwoHatches;
    }

    public int getRocketLevelThreeHatches() {
        return rocketLevelThreeHatches;
    }

    public int getCargoShipCargo() {
        return cargoShipCargo;
    }

    public int getRocketLevelOneCargo() {
        return rocketLevelOneCargo;
    }

    public int getRocketLevelTwoCargo() {
        return rocketLevelTwoCargo;
    }

    public int getRocketLevelThreeCargo() {
        return rocketLevelThreeCargo;
    }

    public int getHatchesDropped() {
        return hatchesDropped;
    }

    public int getCargoDropped() {
        return cargoDropped;
    }

    public boolean isAttemptHabClimb() {
        return attemptHabClimb;
    }

    public int getAttemptHabClimbLevel() {
        return attemptHabClimbLevel;
    }

    public boolean isSuccessHabClimb() {
        return successHabClimb;
    }

    public int getSuccessHabClimbLevel() {
        return successHabClimbLevel;
    }

    public boolean isClimbAssistedByPartner() {
        return climbAssistedByPartner;
    }

    public int getAssistingClimbTeamNum() {
        return assistingClimbTeamNum;
    }

    public int getNumPartnerClimbAssists() {
        return numPartnerClimbAssists;
    }

    public int getPartnerClimbAssistEndLevel() {
        return partnerClimbAssistEndLevel;
    }

    public int getPartnerClimbAssistStartLevel() {
        return partnerClimbAssistStartLevel;
    }
}
