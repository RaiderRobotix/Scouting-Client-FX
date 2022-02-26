package org.usfirst.frc.team25.scouting.data.models;

import java.io.Serializable;

public class ScoutEntry implements Serializable {

    private PreMatch preMatch;
    private Autonomous auton;
    private TeleOp teleOp;
    private PostMatch postMatch;


    private transient int AutonCargo;


    private transient int teleOpCargo;



    private transient int totalCargo;

    private transient int totalCargoDropped;
    private transient int totalCycles;

    private transient int calculatedAutonPoints;
    private transient int calculatedTeleOpPoints;
    private transient int calculatedClimbPoints;
    private transient int calculatedPointContribution;


    public void calculateDerivedStats() {
        //Auton
        AutonCargo = auton.getRobotCargoScoredUpperHub() + auton.getRobotCargoScoredLowerHub();



        //Tele-Op
        teleOpCargo = teleOp.getRobotCargoScoredUpperHub() + teleOp.getRobotCargoScoredLowerHub();


        //Overall


        totalCargo = AutonCargo + teleOpCargo;

        totalCargoDropped = teleOp.getRobotCargoDropped() + auton.getRobotCargoDropped();

        totalCycles = totalCargo + totalCargoDropped;

        calculatedAutonPoints = auton.getRobotCargoScoredUpperHub() * 4 + auton.getRobotCargoScoredLowerHub() * 2 +
                auton.getHumanCargoScored() * 4;

        if (auton.isRobotPassTarmac()) {
            calculatedAutonPoints += 2;
        }

        calculatedTeleOpPoints = teleOpCargo;

        calculatedClimbPoints = 0;

        if (teleOp.getSuccessRungClimbLevel() == 0) {
            calculatedClimbPoints += 4;
        }

        else if (teleOp.getSuccessRungClimbLevel() == 1) {
            calculatedClimbPoints += 6;
        }
        else if (teleOp.getSuccessRungClimbLevel() == 2) {
            calculatedClimbPoints += 10;
        }
        else if (teleOp.getSuccessRungClimbLevel() == 3) {
            calculatedClimbPoints += 15;
        }

        calculatedPointContribution = calculatedAutonPoints + calculatedClimbPoints + calculatedTeleOpPoints;

        postMatch.generateQuickCommentStr();

    }

    public PreMatch getPreMatch() {
        return preMatch;
    }

    public Autonomous getAutonomous() {
        return auton;
    }

    public TeleOp getTeleOp() {
        return teleOp;
    }

    public PostMatch getPostMatch() {
        return postMatch;
    }

    public int getSandstormCargo() {
        return AutonCargo;
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

    public int getTotalCycles() {
        return totalCycles;
    }

    public int getCalculatedSandstormPoints() {
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
