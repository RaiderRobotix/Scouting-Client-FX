package org.usfirst.frc.team25.scouting.data.models;

import java.io.Serializable;

public class ScoutEntry implements Serializable {

    private PreMatch preMatch;
    private Autonomous sandstorm;
    private TeleOp teleOp;
    private PostMatch postMatch;

    private transient int sandstormHatches;
    private transient int sandstormCargo;

    private transient int teleOpHatches;
    private transient int teleOpRocketHatches;
    private transient int teleOpCargo;
    private transient int teleOpRocketCargo;

    private transient int totalHatches;
    private transient int totalCargo;
    private transient int totalHatchesDropped;
    private transient int totalCargoDropped;
    private transient int totalCycles;

    private transient int calculatedSandstormPoints;
    private transient int calculatedTeleOpPoints;
    private transient int calculatedClimbPoints;
    private transient int calculatedPointContribution;


    public void calculateDerivedStats() {
        //Sandstorm
        sandstormCargo = sandstorm.getRocketCargo() + sandstorm.getCargoShipCargo();

        sandstormHatches = sandstorm.getRocketHatches() + sandstorm.getCargoShipHatches();


        //Tele-Op
        teleOpRocketHatches = teleOp.getRocketLevelOneHatches() + teleOp.getRocketLevelTwoHatches()
                + teleOp.getRocketLevelThreeHatches();

        teleOpRocketCargo = teleOp.getRocketLevelOneCargo() + teleOp.getRocketLevelTwoCargo()
                + teleOp.getRocketLevelThreeCargo();

        teleOpHatches = teleOpRocketHatches + teleOp.getCargoShipHatches();
        teleOpCargo = teleOpRocketCargo + teleOp.getCargoShipCargo();


        //Overall
        totalHatches = teleOpHatches + sandstormHatches;

        totalCargo = sandstormCargo + teleOpCargo;

        totalCargoDropped = teleOp.getCargoDropped() + sandstorm.getCargoDropped();

        totalHatchesDropped = teleOp.getHatchesDropped() + sandstorm.getHatchesDropped();

        totalCycles = totalCargo + totalHatches + totalCargoDropped + totalHatchesDropped;

        calculatedSandstormPoints = sandstorm.getRocketHatches() * 2 + sandstorm.getCargoShipHatches() * 5
                + sandstormCargo * 3;

        if (sandstorm.isCrossHabLine()) {
            calculatedSandstormPoints += preMatch.getStartingLevel() * 3;
        }

        calculatedTeleOpPoints = teleOpHatches * 2 + teleOpCargo * 3;

        calculatedClimbPoints = 0;

        if ((teleOp.isSuccessHabClimb()) || teleOp.getNumPartnerClimbAssists() > 0) {
            calculatedClimbPoints += Math.pow(2, (teleOp.getSuccessHabClimbLevel() - 1)) * 3;
            calculatedClimbPoints += 3 * teleOp.getNumPartnerClimbAssists() * (Math.pow(2,
                    teleOp.getPartnerClimbAssistEndLevel() - 1) - Math.pow(2,
                    teleOp.getPartnerClimbAssistStartLevel() - 1));
        }

        calculatedPointContribution = calculatedSandstormPoints + calculatedClimbPoints + calculatedTeleOpPoints;

        postMatch.generateQuickCommentStr();

    }

    public PreMatch getPreMatch() {
        return preMatch;
    }

    public Autonomous getAutonomous() {
        return sandstorm;
    }

    public TeleOp getTeleOp() {
        return teleOp;
    }

    public PostMatch getPostMatch() {
        return postMatch;
    }

    public int getSandstormHatches() {
        return sandstormHatches;
    }

    public int getSandstormCargo() {
        return sandstormCargo;
    }

    public int getTeleOpHatches() {
        return teleOpHatches;
    }

    public int getTeleOpRocketHatches() {
        return teleOpRocketHatches;
    }

    public int getTeleOpCargo() {
        return teleOpCargo;
    }

    public int getTeleOpRocketCargo() {
        return teleOpRocketCargo;
    }

    public int getTotalHatches() {
        return totalHatches;
    }

    public int getTotalCargo() {
        return totalCargo;
    }

    public int getTotalHatchesDropped() {
        return totalHatchesDropped;
    }

    public int getTotalCargoDropped() {
        return totalCargoDropped;
    }

    public int getTotalCycles() {
        return totalCycles;
    }

    public int getCalculatedSandstormPoints() {
        return calculatedSandstormPoints;
    }

    public int getCalculatedTeleOpPoints() {
        return calculatedTeleOpPoints;
    }

    public int getCalculatedClimbPoints() {
        return calculatedClimbPoints;
    }

    public int getCalculatedPointContribution() {
        return calculatedPointContribution;
    }
}
