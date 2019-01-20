package org.usfirst.frc.team25.scouting.client.models;


import java.io.Serializable;

public class ScoutEntry implements Serializable {

    private PreMatch preMatch;
    private Autonomous auto;
    private TeleOp teleOp;
    private PostMatch postMatch;
    private transient boolean centerSwitchAuto = false, centerScaleAuto = false,
            farSwitchAuto = false, farScaleAuto = false, nearSwitchAuto = false, nearScaleAuto = false;

    //Actual member variables will be set using setters as data is filled in
    public ScoutEntry() {
    }

    public PreMatch getPreMatch() {
        return preMatch;
    }

    public void setPreMatch(PreMatch preMatch) {
        this.preMatch = preMatch;
    }

    public Autonomous getAuto() {
        return auto;
    }

    public void setAuto(Autonomous auto) {
        this.auto = auto;
    }

    public TeleOp getTeleOp() {
        return teleOp;
    }

    public void setTeleOp(TeleOp teleOp) {
        this.teleOp = teleOp;
    }

    public PostMatch getPostMatch() {
        return postMatch;
    }

    public void setPostMatch(PostMatch postMatch) {
        this.postMatch = postMatch;
    }


    public void calculateDerivedStats() {
        if (preMatch.getStartingPos().equals("Center")) {
            centerSwitchAuto = true;
            centerScaleAuto = true;
        } else {
            if (preMatch.getStartingPos().charAt(0) == teleOp.getFieldLayout().charAt(0)) {
                nearSwitchAuto = true;
            } else {
                farSwitchAuto = true;
            }

            if (preMatch.getStartingPos().charAt(0) == teleOp.getFieldLayout().charAt(1)) {
                nearScaleAuto = true;
            } else {
                farScaleAuto = true;
            }
        }


        postMatch.generateQuickCommentStr();

    }

    public boolean isCenterSwitchAuto() {
        return centerSwitchAuto;
    }

    public boolean isCenterScaleAuto() {
        return centerScaleAuto;
    }

    public boolean isFarSwitchAuto() {
        return farSwitchAuto;
    }

    public boolean isFarScaleAuto() {
        return farScaleAuto;
    }

    public boolean isNearSwitchAuto() {
        return nearSwitchAuto;
    }

    public boolean isNearScaleAuto() {
        return nearScaleAuto;
    }


}
