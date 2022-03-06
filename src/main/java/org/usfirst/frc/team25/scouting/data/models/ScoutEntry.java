package org.usfirst.frc.team25.scouting.data.models;

import java.io.Serializable;

public class ScoutEntry implements Serializable {

    private PreMatch preMatch;
    private Autonomous sandstorm;
    private TeleOp teleOp;
    private PostMatch postMatch;

    private transient int autonCargo;

    private transient int teleOpCargo;

    private transient int totalCargo;
    private transient int totalCargoDropped;

    private transient int calculatedAutonPoints;
    private transient int calculatedTeleOpPoints;
    private transient int calculatedClimbPoints;
    private transient int calculatedPointContribution;

    public void calculateDerivedStats() {
        //Auton
//        sandstorm.setCargoUpperHub(5);
//        System.out.println(sandstorm.getCargoUpperHub());
//
        autonCargo = sandstorm.getCargoLowerHub() + sandstorm.getCargoUpperHub() + sandstorm.getPlayerLowerHub() + sandstorm.getPlayerUpperHub();

        //Tele-Op
        teleOpCargo = teleOp.getCargoLowerHub() + teleOp.getCargoUpperHub();


        //Overall
        totalCargo = autonCargo + teleOpCargo;

        calculatedAutonPoints = sandstorm.getCargoLowerHub() * 2 + sandstorm.getCargoUpperHub() * 4
                + sandstorm.getPlayerUpperHub() * 4 + sandstorm.getPlayerLowerHub() * 2;

        calculatedAutonPoints += 3;

        if (sandstorm.isCrossInitializationLine()) {
            calculatedAutonPoints += 3;
        }

        calculatedTeleOpPoints = teleOp.getCargoLowerHub() + teleOp.getCargoUpperHub() * 2;

        if (teleOp.getClimbLevel() == 0 && teleOp.isSuccessClimb()){
            calculatedClimbPoints = 0;
        }
        else if (teleOp.getClimbLevel() == 1 && teleOp.isSuccessClimb()){
            calculatedClimbPoints = 4;
        }
        else if (teleOp.getClimbLevel() == 2 && teleOp.isSuccessClimb()){
            calculatedClimbPoints = 10;
        }
        else if (teleOp.getClimbLevel() == 3 && teleOp.isSuccessClimb()){
            calculatedClimbPoints = 15;
        }

        calculatedPointContribution = calculatedAutonPoints + calculatedClimbPoints + calculatedTeleOpPoints;

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

    public int getAutonCargo() {
        return autonCargo;
    }

    public int getTeleOpCargo() {
        return teleOpCargo;
    }

    public int getTotalCargo() {
        return totalCargo;
    }

    public int getTotalCargoDropped() {
        return totalCargoDropped;
    }

    public int getCalculatedAutonPoints() {
        return calculatedAutonPoints;
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
