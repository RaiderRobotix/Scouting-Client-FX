package org.usfirst.frc.team25.scouting.client.models;


/**
 * Object model for autonomous period of a match
 */
public class Autonomous {

    private final int switchCubes;
    private final int scaleCubes;
    private final int exchangeCubes;
    private final int powerCubePilePickup;
    private final int switchAdjacentPickup;
    private final int cubesDropped;
    private final boolean nullTerritoryFoul;
    private final boolean cubeDropOpponentSwitchPlate;
    private final boolean isCubeDropOpponentScalePlate;
    private boolean autoLineCross;

    public Autonomous(int switchCubes, int scaleCubes, int exchangeCubes, int powerCubePilePickup,
                      int switchAdjacentPickup,
                      int cubesDropped, boolean autoLineCross, boolean nullTerritoryFoul,
                      boolean cubeDropOpponentSwitchPlate, boolean isCubeDropOpponentScalePlate) {
        this.switchCubes = switchCubes;
        this.scaleCubes = scaleCubes;
        this.exchangeCubes = exchangeCubes;
        this.powerCubePilePickup = powerCubePilePickup;
        this.switchAdjacentPickup = switchAdjacentPickup;
        this.cubesDropped = cubesDropped;
        this.autoLineCross = autoLineCross;
        this.nullTerritoryFoul = nullTerritoryFoul;
        this.cubeDropOpponentSwitchPlate = cubeDropOpponentSwitchPlate;
        this.isCubeDropOpponentScalePlate = isCubeDropOpponentScalePlate;
    }

    public int getSwitchCubes() {
        return switchCubes;
    }

    public int getScaleCubes() {
        return scaleCubes;
    }

    public int getExchangeCubes() {
        return exchangeCubes;
    }

    public int getPowerCubePilePickup() {
        return powerCubePilePickup;
    }

    public int getSwitchAdjacentPickup() {
        return switchAdjacentPickup;
    }

    public int getCubesDropped() {
        return cubesDropped;
    }

    public boolean isAutoLineCross() {
        return autoLineCross;
    }

    public void setAutoLineCross(boolean autoLineCross) {
        this.autoLineCross = autoLineCross;
    }

    public boolean isNullTerritoryFoul() {
        return nullTerritoryFoul;
    }

    public boolean isCubeDropOpponentSwitchPlate() {
        return cubeDropOpponentSwitchPlate;
    }

    public boolean isCubeDropOpponentScalePlate() {
        return isCubeDropOpponentScalePlate;
    }


}
