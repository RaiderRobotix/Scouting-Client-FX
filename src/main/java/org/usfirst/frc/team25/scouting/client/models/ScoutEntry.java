package org.usfirst.frc.team25.scouting.client.models;


import java.io.Serializable;

public class ScoutEntry implements Serializable {

    private PreMatch preMatch;
    private Autonomous auto;
    private TeleOp teleOp;
    private PostMatch postMatch;
    private transient int calculatedPointContribution, totalHatches, totalCargo;


    public PreMatch getPreMatch() {
        return preMatch;
    }

    public Autonomous getAuto() {
        return auto;
    }

    public TeleOp getTeleOp() {
        return teleOp;
    }

    public PostMatch getPostMatch() {
        return postMatch;
    }

    public void calculateDerivedStats() {
        int teleOpPoints =
                2 * (teleOp.getRocketLevelOneHatches() + teleOp.getRocketLevelTwoHatches() + teleOp.getRocketLevelThreeHatches());

        postMatch.generateQuickCommentStr();

    }


}
