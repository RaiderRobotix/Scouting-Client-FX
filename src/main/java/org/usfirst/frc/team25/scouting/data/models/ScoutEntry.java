package org.usfirst.frc.team25.scouting.data.models;

import java.io.Serializable;

public class ScoutEntry implements Serializable {

    private PreMatch preMatch;
    private Autonomous sandstorm;
    private TeleOp teleOp;
    private PostMatch postMatch;

//    private transient int sandstormHatches;
//    private transient int sandstormCargo;
//
//    private transient int teleOpHatches;
//    private transient int teleOpRocketHatches;
//    private transient int teleOpCargo;
//    private transient int teleOpRocketCargo;
//
//    private transient int totalHatches;
//    private transient int totalCargo;
//    private transient int totalHatchesDropped;
//    private transient int totalCargoDropped;
//    private transient int totalCycles;
//
//    private transient int calculatedSandstormPoints;
//    private transient int calculatedTeleOpPoints;
//    private transient int calculatedClimbPoints;
//    private transient int calculatedPointContribution;


    // Autonomous
    private transient int autonomousCones;
    private transient int autonomousCubes;
    private transient boolean autonomousDocked;
    private transient boolean autonomousDockedEngaged;


    //TeleOp
    private transient int teleOpCones;
    private transient int teleOpCubes;
    private transient boolean teleOpDocked;
    private transient  boolean teleOpDockedEngaged;


    //Overall
    private transient int totalCones;
    private transient int totalCubes;
    private transient int totalConesDropped;
    private transient int totalCubesDropped;

    private transient int totalCycles;

    //Points
    private transient int mobilityPoints;
    private transient int teleOpDockPoints;
    private transient int autonomousDockPoints;
    private transient int calculatedAutonomousPoints;
    private transient int calculatedTeleOpPoints;

    private transient int calculatedTotalDockPoints;
    private transient int calculatedPointContribution;




    public void calculateDerivedStats() {

//        //Sandstorm
//        sandstormCargo = sandstorm.getRocketCargo() + sandstorm.getCargoShipCargo();
//
//        sandstormHatches = sandstorm.getRocketHatches() + sandstorm.getCargoShipHatches();
//
//
//        //Tele-Op
//        teleOpRocketHatches = teleOp.getRocketLevelOneHatches() + teleOp.getRocketLevelTwoHatches()
//                + teleOp.getRocketLevelThreeHatches();
//
//        teleOpRocketCargo = teleOp.getRocketLevelOneCargo() + teleOp.getRocketLevelTwoCargo()
//                + teleOp.getRocketLevelThreeCargo();
//
//        teleOpHatches = teleOpRocketHatches + teleOp.getCargoShipHatches();
//        teleOpCargo = teleOpRocketCargo + teleOp.getCargoShipCargo();
//
//
//        //Overall
//        totalHatches = teleOpHatches + sandstormHatches;
//
//        totalCargo = sandstormCargo + teleOpCargo;
//
//        totalCargoDropped = teleOp.getCargoDropped() + sandstorm.getCargoDropped();
//
//        totalHatchesDropped = teleOp.getHatchesDropped() + sandstorm.getHatchesDropped();
//
//        totalCycles = totalCargo + totalHatches + totalCargoDropped + totalHatchesDropped;
//
//        calculatedSandstormPoints = sandstorm.getRocketHatches() * 2 + sandstorm.getCargoShipHatches() * 5
//                + sandstormCargo * 3;
//
//        if (sandstorm.isCrossHabLine()) {
//            calculatedSandstormPoints += preMatch.getStartingLevel() * 3;
//        }
//
//        calculatedTeleOpPoints = teleOpHatches * 2 + teleOpCargo * 3;
//
//        calculatedClimbPoints = 0;
//
//        if ((teleOp.isSuccessHabClimb()) || teleOp.getNumPartnerClimbAssists() > 0) {
//            calculatedClimbPoints += Math.pow(2, (teleOp.getSuccessHabClimbLevel() - 1)) * 3;
//            calculatedClimbPoints += 3 * teleOp.getNumPartnerClimbAssists() * (Math.pow(2,
//                    teleOp.getPartnerClimbAssistEndLevel() - 1) - Math.pow(2,
//                    teleOp.getPartnerClimbAssistStartLevel() - 1));
//        }
//
//        calculatedPointContribution = calculatedSandstormPoints + calculatedClimbPoints + calculatedTeleOpPoints;

        //Autonomous
        autonomousCones = sandstorm.getConeBttm() + sandstorm.getConeMid() + sandstorm.getConeTop();

        autonomousCubes = sandstorm.getCubeBttm() + sandstorm.getCubeMid() + sandstorm.getCubeTop();

        if(sandstorm.getDockStatus().equals("Robot is docked")){
            autonomousDocked = true;
            autonomousDockedEngaged = false;
        }
        if(sandstorm.getDockStatus().equals("Robot is docked and engaged")){
            autonomousDocked = false;
            autonomousDockedEngaged = true;
        }

        //TeleOP

        teleOpCones = teleOp.getConeBttmTele() + teleOp.getConeMidTele() + teleOp.getConeTopTele();

        teleOpCubes = teleOp.getCubeBttmTele() + teleOp.getCubeMidTele() + teleOp.getCubeTopTele();

        if(teleOp.getDockStatusTele().equals("Robot is docked")){
            teleOpDocked = true;
            teleOpDockedEngaged = false;
        }
        if(teleOp.getDockStatusTele().equals("Robot is docked and engaged")){
            teleOpDocked = false;
            teleOpDockedEngaged = true;
        }

        //Overall

        totalCones = autonomousCones + teleOpCones;

        totalCubes = autonomousCubes + teleOpCubes;

        totalConesDropped = sandstorm.getConeDropped()+ teleOp.getConeDroppedTele();

        totalCubesDropped = sandstorm.getCubeDropped() + teleOp.getCubeDroppedTele();

        totalCycles = totalCones + totalCubes + totalConesDropped + totalCubesDropped;

        //Point Calculation

        if(autonomousDocked){
            autonomousDockPoints = 8;
        }
        if(autonomousDockedEngaged){
            autonomousDockPoints = 12;
        }
        if(teleOpDocked){
            teleOpDockPoints = 6;
        }
        if(teleOpDockedEngaged){
            teleOpDockPoints = 10;
        }
        if(sandstorm.isRobotExitCommunity()){
            mobilityPoints = 3;
        }

        calculatedAutonomousPoints = sandstorm.getConeBttm() * 3 + sandstorm.getConeMid() * 4 + sandstorm.getConeTop() * 6 +
                                    sandstorm.getCubeBttm() * 3 + sandstorm.getCubeMid() * 4 + sandstorm.getCubeTop() * 6  + mobilityPoints;

        calculatedTeleOpPoints =  teleOp.getConeBttmTele() * 2 + teleOp.getConeMidTele() * 3 + teleOp.getConeTopTele() * 5 +
                                  teleOp.getCubeBttmTele() * 2 + teleOp.getCubeMidTele() * 3 + teleOp.getCubeTopTele() * 5 ;

        calculatedTotalDockPoints = autonomousDockPoints + teleOpDockPoints;

        calculatedPointContribution = calculatedAutonomousPoints + calculatedTeleOpPoints + calculatedTotalDockPoints;



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

    public int getTotalCycles() {
        return totalCycles;
    }

    public int getTotalCubesDropped() {
        return totalCubesDropped;
    }

    public int getTotalCubes() {
        return totalCubes;
    }

    public int getTotalConesDropped() {
        return totalConesDropped;
    }

    public int getTotalCones() {
        return totalCones;
    }

    public int getTeleOpCubes() {
        return teleOpCubes;
    }

    public int getTeleOpCones() {
        return teleOpCones;
    }

    public int getCalculatedTeleOpPoints() {
        return calculatedTeleOpPoints;
    }

    public int getCalculatedPointContribution() {
        return calculatedPointContribution;
    }

    public boolean isAutonomousDocked() {
        return autonomousDocked;
    }

    public boolean isAutonomousDockedEngaged() {
        return autonomousDockedEngaged;
    }

    public boolean isTeleOpDocked() {
        return teleOpDocked;
    }

    public boolean isTeleOpDockedEngaged() {
        return teleOpDockedEngaged;
    }

    public int getAutonomousCones() {
        return autonomousCones;
    }

    public int getAutonomousCubes() {
        return autonomousCubes;
    }

    public int getAutonomousDockPoints() {
        return autonomousDockPoints;
    }

    public int getCalculatedAutonomousPoints() {
        return calculatedAutonomousPoints;
    }

    public int getCalculatedTotalDockPoints() {
        return calculatedTotalDockPoints;
    }

    public int getMobilityPoints() {
        return mobilityPoints;
    }

    public int getTeleOpDockPoints() {
        return teleOpDockPoints;
    }
}
