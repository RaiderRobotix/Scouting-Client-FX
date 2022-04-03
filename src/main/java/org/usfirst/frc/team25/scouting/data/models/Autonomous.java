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

    private int cargoPickedUp;

    private boolean commitFoul;


    public Autonomous( int cargoUpperHub, int cargoLowerHub,
                       int playerLowerHub, int playerUpperHub,
                       boolean commitFoul, boolean crossInitializationLine) {
        this.crossInitializationLine = crossInitializationLine;
        this.cargoUpperHub = cargoUpperHub;
        this.cargoLowerHub = cargoLowerHub;
        this.playerUpperHub = playerUpperHub;
        this.playerLowerHub = playerLowerHub;
        this.commitFoul = commitFoul;
    }

    public int getCargoUpperHub() { return cargoUpperHub; }

    public int getCargoLowerHub() { return cargoLowerHub; }

    public int getPlayerUpperHub() {
        return playerUpperHub;
    }

    public int getPlayerLowerHub() { return playerLowerHub; }

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
}
