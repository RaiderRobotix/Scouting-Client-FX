package org.usfirst.frc.team25.scouting.data.models;

/**
 * Object model for autonomous (or sandstorm) period of a match
 */
public class Autonomous {
    private boolean crossInitializationLine;

    private int cargoUpperHub;
    private int cargoLowerHub;

    private int playerLowerHub;
    private int playerUpperHub;


    private int cargoDropped;
    private int cargoPickedUp;

    private boolean commitFoul;


    public Autonomous(boolean crossInitializationLine, int cargoUpperHub
                    , int cargoLowerHub, int playerLowerHub, int playerUpperHub, int cargoPickedUp
                    , int cargoDropped, boolean commitFoul) {
        this.crossInitializationLine = crossInitializationLine;
        this.cargoUpperHub = cargoUpperHub;
        this.cargoLowerHub = cargoLowerHub;
        this.playerUpperHub = playerUpperHub;
        this.playerLowerHub = playerLowerHub;
        this.cargoDropped = cargoDropped;
        this.commitFoul = commitFoul;
        this.cargoPickedUp = cargoPickedUp;
    }

    public int getCargoUpperHub() { return cargoUpperHub; }

    public int getCargoLowerHub() { return cargoLowerHub; }

    public int getPlayerUpperHub() {
        return playerUpperHub;
    }

    public int getPlayerLowerHub() { return playerLowerHub; }

    public int getCargoDropped() {
        return cargoDropped;
    }

    public int getCargoPickedUp() { return cargoPickedUp; }

    public boolean isCommitFoul() {
        return commitFoul;
    }

    public boolean isCrossInitializationLine() { return crossInitializationLine; }



    public void setCommitFoul(boolean commitFoul) {
        this.commitFoul = commitFoul;
    }

    public void setCrossInitializationLine(boolean crossInitializationLine) { this.crossInitializationLine = crossInitializationLine; }

    public void setCargoUpperHub(int cargoUpperHub) {
        this.cargoUpperHub = cargoUpperHub;
    }

    public void setCargoLowerHub(int cargoLowerHub) { this.cargoLowerHub= cargoLowerHub; }

    public void setPlayerUpperHub(int playerUpperHub) {
        this.playerUpperHub = playerUpperHub;
    }

    public void setPlayerLowerHub(int playerLowerHub) {
        this.playerLowerHub = playerLowerHub;
    }

    public void setCargoDropped(int cargoDropped) { this.cargoDropped = cargoDropped; }

}
