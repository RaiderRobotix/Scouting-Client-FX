package org.usfirst.frc.team25.scouting.data.models;


import java.io.Serializable;

public class ScoutEntry implements Serializable {

    private PreMatch preMatch;
    private Autonomous sandstorm;
    private TeleOp teleOp;
    private PostMatch postMatch;

    private transient int teleOpCargo, climbPoints, autoCargo, autoPoints, teleOpHatches, totalHatches,
            totalCargo, autoHatches, totalPoints;


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


    public void calculateDerivedStats() {

        teleOpCargo = teleOp.getRocketLevelOneCargo()
                + teleOp.getRocketLevelTwoCargo()
                + teleOp.getRocketLevelThreeCargo();

        teleOpHatches = teleOp.getRocketLevelOneHatches()
                + teleOp.getRocketLevelTwoHatches()
                + teleOp.getRocketLevelThreeHatches();

        if (teleOp.getSuccessHabClimbLevel() == 1) {
            climbPoints = preMatch.getStartingLevel() * 3 + 3;
        } else if (teleOp.getSuccessHabClimbLevel() == 2) {
            climbPoints = preMatch.getStartingLevel() * 3 + 6;
        } else if (teleOp.getSuccessHabClimbLevel() == 3) {
            climbPoints = preMatch.getStartingLevel() * 3 + 12;
        }

        autoCargo = sandstorm.getRocketCargo()
                + sandstorm.getCargoShipCargo();

        autoPoints = sandstorm.getCargoShipCargo() * 3
                + sandstorm.getCargoShipHatches() * 2
                + sandstorm.getRocketCargo() * 3
                + sandstorm.getRocketHatches() * 2;

        if (sandstorm.isOpponentCargoShipLineFoul()) {
            autoPoints = -3;
        }

        totalHatches = sandstorm.getRocketHatches()
                + sandstorm.getCargoShipHatches()
                + teleOp.getRocketLevelThreeHatches()
                + teleOp.getRocketLevelTwoHatches()
                + teleOp.getRocketLevelOneHatches()
                + teleOp.getCargoShipHatches();

        autoHatches = sandstorm.getRocketHatches()
                + sandstorm.getCargoShipHatches();

        totalCargo = sandstorm.getRocketCargo()
                + sandstorm.getCargoShipHatches()
                + teleOp.getCargoShipHatches()
                + teleOp.getRocketLevelOneHatches()
                + teleOp.getRocketLevelTwoHatches()
                + teleOp.getRocketLevelThreeHatches();

        totalPoints = totalHatches * 2
                + totalCargo * 3
                + climbPoints;

        postMatch.generateQuickCommentStr();


    }


}
