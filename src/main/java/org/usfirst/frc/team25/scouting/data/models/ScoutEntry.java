package org.usfirst.frc.team25.scouting.data.models;


import java.io.Serializable;

public class ScoutEntry implements Serializable {

    private PreMatch preMatch;
    private Autonomous sandstorm;
    private TeleOp teleOp;
    private PostMatch postMatch;
    private transient int sandstormPoints, teleOpPoints, climbPoints, calculatedPointContribution, autoHatches,
            autoCargo, teleOpHatches, teleOpCargo, totalHatches, totalCargo;

    private transient String autoMode;


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
        //TODO calculate the above

        postMatch.generateQuickCommentStr();

    }

    public Autonomous getSandstorm() {
        return sandstorm;
    }

    public int getSandstormPoints() {
        return sandstormPoints;
    }

    public int getTeleOpPoints() {
        return teleOpPoints;
    }

    public int getClimbPoints() {
        return climbPoints;
    }

    public int getCalculatedPointContribution() {
        return calculatedPointContribution;
    }

    public int getAutoHatches() {
        return autoHatches;
    }

    public int getAutoCargo() {
        return autoCargo;
    }

    public int getTeleOpHatches() {
        return teleOpHatches;
    }

    public int getTeleOpCargo() {
        return teleOpCargo;
    }

    public int getTotalHatches() {
        return totalHatches;
    }

    public int getTotalCargo() {
        return totalCargo;
    }

    public String getAutoMode() {
        return autoMode;
    }


}
