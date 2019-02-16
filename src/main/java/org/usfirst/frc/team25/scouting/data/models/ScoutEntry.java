package org.usfirst.frc.team25.scouting.data.models;


import java.io.Serializable;

public class ScoutEntry implements Serializable {

    private PreMatch preMatch;
    private Autonomous sandstorm;
    private TeleOp teleOp;
    private PostMatch postMatch;
    private transient int calculatedPointContribution, totalHatches, autoHatches, totalCargo;

    private transient int teleOpCargo, climbPoints, autoCargo, sandstormPoints;


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

        int teleOpCargo = teleOp.getRocketLevelOneCargo() + teleOp.getRocketLevelTwoCargo()
                + teleOp.getRocketLevelThreeCargo();

        int teleOpHatches = teleOp.getRocketLevelOneHatches() + teleOp.getRocketLevelTwoHatches()
                + teleOp.getRocketLevelThreeHatches();

        if (teleOp.getSuccessHabClimbLevel() == 1) {
            climbPoints = preMatch.getStartingLevel() * 3 + 3;
        } else if (teleOp.getSuccessHabClimbLevel() == 2) {
            climbPoints = preMatch.getStartingLevel() * 3 + 6;
        } else if (teleOp.getSuccessHabClimbLevel() == 3) {
            climbPoints = preMatch.getStartingLevel() * 3 + 12;
        }

        int teleOpPoints = teleOpHatches * 2 + teleOpCargo * 3;

        int autoCargo = sandstorm.getRocketCargo() + sandstorm.getCargoShipCargo();

        int sandstormPoints = sandstorm.getCargoShipCargo() * 3 + sandstorm.getCargoShipHatches() * 2
                + sandstorm.getRocketCargo() * 3 + sandstorm.getRocketHatches() * 2;

        if (sandstorm.isOpponentCargoShipLineFoul()) {
            sandstormPoints = -3;
        }



        postMatch.generateQuickCommentStr();


    }


}
