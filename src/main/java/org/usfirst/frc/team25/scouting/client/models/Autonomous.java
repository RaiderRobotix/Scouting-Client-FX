package org.usfirst.frc.team25.scouting.client.models;

/**
 * Object model for autonomous period of a match
 */
public class Autonomous {

    private int rocketCargo;
    private int rocketHatches;
    private int cargoShipHatches;
    private int cargoShipCargo;
    private int hatchesDropped;
    private int cargoDropped;
    private boolean reachHabLine;
    private boolean opponentCargoShipLineFoul;
    private boolean sideCargoShipHatchCapable;
    private boolean frontCargoShipHatchCapable;

    public Autonomous(int rocketCargo, int rocketHatches, int cargoShipHatches,
                      int cargoShipCargo, int hatchesDropped, int cargoDropped,
                      boolean reachHabLine, boolean opponentCargoShipLineFoul,
                      boolean sideCargoShipHatchCapable, boolean frontCargoShipHatchCapable) {
        this.rocketCargo = rocketCargo;
        this.rocketHatches = rocketHatches;
        this.cargoShipHatches = cargoShipHatches;
        this.cargoShipCargo = cargoShipCargo;
        this.hatchesDropped = hatchesDropped;
        this.cargoDropped = cargoDropped;
        this.reachHabLine = reachHabLine;
        this.opponentCargoShipLineFoul = opponentCargoShipLineFoul;
        this.sideCargoShipHatchCapable = sideCargoShipHatchCapable;
        this.frontCargoShipHatchCapable = frontCargoShipHatchCapable;
    }

    public void setHatchesDropped(int hatchesDropped) {
        this.hatchesDropped = hatchesDropped;
    }

    public void setCargoDropped(int cargoDropped) {
        this.cargoDropped = cargoDropped;
    }

    public void setReachHabLine(boolean reachHabLine) {
        this.reachHabLine = reachHabLine;
    }

    public void setOpponentCargoShipLineFoul(boolean opponentCargoShipLineFoul) {
        this.opponentCargoShipLineFoul = opponentCargoShipLineFoul;
    }

    public int getRocketCargo() {
        return rocketCargo;
    }

    public void setRocketCargo(int rocketCargo) {
        this.rocketCargo = rocketCargo;
    }

    public int getRocketHatches() {
        return rocketHatches;
    }

    public void setRocketHatches(int rocketHatches) {
        this.rocketHatches = rocketHatches;
    }

    public int getCargoShipHatches() {
        return cargoShipHatches;
    }

    public void setCargoShipHatches(int cargoShipHatches) {
        this.cargoShipHatches = cargoShipHatches;
    }

    public int getCargoShipCargo() {
        return cargoShipCargo;
    }

    public void setCargoShipCargo(int cargoShipCargo) {
        this.cargoShipCargo = cargoShipCargo;
    }

    public boolean isSideCargoShipHatchCapable() {
        return sideCargoShipHatchCapable;
    }

    public int getHatchesDropped() {
        return hatchesDropped;
    }

    public int getCargoDropped() {
        return cargoDropped;
    }

    public boolean isReachHabLine() {
        return reachHabLine;
    }

    public boolean isOpponentCargoShipLineFoul() {
        return opponentCargoShipLineFoul;
    }

    public void setSideCargoShipHatchCapable(boolean sideCargoShipHatchCapable) {
        this.sideCargoShipHatchCapable = sideCargoShipHatchCapable;
    }

    public boolean isFrontCargoShipHatchCapable() {
        return frontCargoShipHatchCapable;
    }

    public void setFrontCargoShipHatchCapable(boolean frontCargoShipHatchCapable) {
        this.frontCargoShipHatchCapable = frontCargoShipHatchCapable;
    }
}