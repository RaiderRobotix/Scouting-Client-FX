package org.usfirst.frc.team25.scouting.data.models;


import java.io.Serializable;

public class ScoutEntry implements Serializable {

    private PreMatch preMatch;
    private Autonomous autonomous;
    private TeleOp teleOp;
    private PostMatch postMatch;
    private transient int sandstormPoints, teleOpPoints, climbPoints, calculatedPointContribution, autoHatches,
            autoCargo,
            teleOpHatches, teleOpCargo, totalHatches, totalCargo;


    private transient String autoMode;


    public PreMatch getPreMatch() {
        return preMatch;
    }

    public Autonomous getAutonomous() {
        return autonomous;
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


}
