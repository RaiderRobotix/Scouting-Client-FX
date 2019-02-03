package org.usfirst.frc.team25.scouting.data.models;


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
    private boolean cargoDroppedCargoShip;
    private boolean cargoDroppedRocket;
    private boolean hatchesDroppedRocket;
    private boolean hatchesDroppedCargoShip;

    public Autonomous(int rocketCargo, int rocketHatches, int cargoShipHatches,
                      int cargoShipCargo, int hatchesDropped, int cargoDropped,
                      boolean reachHabLine, boolean opponentCargoShipLineFoul,
                      boolean sideCargoShipHatchCapable, boolean frontCargoShipHatchCapable,
                      boolean cargoDroppedCargoShip, boolean cargoDroppedRocket,
                      boolean hatchesDroppedRocket, boolean hatchesDroppedCargoShip) {
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
        this.cargoDroppedCargoShip = cargoDroppedCargoShip;
        this.cargoDroppedRocket = cargoDroppedRocket;
        this.hatchesDroppedRocket = hatchesDroppedRocket;
        this.hatchesDroppedCargoShip = hatchesDroppedCargoShip;
    }

    public void setRocketCargo(int rocketCargo) {
        this.rocketCargo = rocketCargo;
    }

    public void setRocketHatches(int rocketHatches) {
        this.rocketHatches = rocketHatches;
    }

    public void setCargoShipHatches(int cargoShipHatches) {
        this.cargoShipHatches = cargoShipHatches;
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

    public void setCargoShipCargo(int cargoShipCargo) {
        this.cargoShipCargo = cargoShipCargo;
    }

    public void setFrontCargoShipHatchCapable(boolean frontCargoShipHatchCapable) {
        this.frontCargoShipHatchCapable = frontCargoShipHatchCapable;
    }

    public boolean isSideCargoShipHatchCapable() {
        return sideCargoShipHatchCapable;
    }

    public void setSideCargoShipHatchCapable(boolean sideCargoShipHatchCapable) {
        this.sideCargoShipHatchCapable = sideCargoShipHatchCapable;
    }

    public boolean isCargoDroppedCargoShip() {
        return cargoDroppedCargoShip;
    }

    public void setCargoDroppedCargoShip(boolean cargoDroppedCargoShip) {
        this.cargoDroppedCargoShip = cargoDroppedCargoShip;
    }

    public int getRocketCargo() {
        return rocketCargo;
    }

    public int getRocketHatches() {
        return rocketHatches;
    }

    public int getCargoShipHatches() {
        return cargoShipHatches;
    }

    public int getCargoShipCargo() {
        return cargoShipCargo;
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

    public boolean isCargoDroppedRocket() {
        return cargoDroppedRocket;
    }

    public boolean isFrontCargoShipHatchCapable() {
        return frontCargoShipHatchCapable;
    }

    public void setCargoDroppedRocket(boolean cargoDroppedRocket) {
        this.cargoDroppedRocket = cargoDroppedRocket;
    }

    public boolean isHatchesDroppedRocket() {
        return hatchesDroppedRocket;
    }

    public void setHatchesDroppedRocket(boolean hatchesDroppedRocket) {
        this.hatchesDroppedRocket = hatchesDroppedRocket;
    }

    public boolean isHatchesDroppedCargoShip() {
        return hatchesDroppedCargoShip;
    }

    public void setHatchesDroppedCargoShip(boolean hatchesDroppedCargoShip) {
        this.hatchesDroppedCargoShip = hatchesDroppedCargoShip;
    }
}
