package org.usfirst.frc.team25.scouting.data.models;

/**
 * Object model for autonomous (or sandstorm) period of a match
 */
public class Autonomous {


    private boolean crossHabLine;

    private int cargoShipHatches;
    private int rocketHatches;

    private int cargoShipCargo;
    private int rocketCargo;

    private boolean frontCargoShipHatchCapable;
    private boolean sideCargoShipHatchCapable;

    private int hatchesDropped;
    private int cargoDropped;

    private boolean hatchesDroppedCargoShip;
    private boolean hatchesDroppedRocket;
    private boolean cargoDroppedCargoShip;
    private boolean cargoDroppedRocket;

    private boolean opponentCargoShipLineFoul;


    public Autonomous(int rocketCargo, int rocketHatches, int cargoShipHatches,
                      int cargoShipCargo, int hatchesDropped, int cargoDropped,
                      boolean crossHabLine, boolean opponentCargoShipLineFoul,
                      boolean sideCargoShipHatchCapable, boolean frontCargoShipHatchCapable,
                      boolean cargoDroppedCargoShip, boolean cargoDroppedRocket,
                      boolean hatchesDroppedRocket, boolean hatchesDroppedCargoShip) {
        this.rocketCargo = rocketCargo;
        this.rocketHatches = rocketHatches;
        this.cargoShipHatches = cargoShipHatches;
        this.cargoShipCargo = cargoShipCargo;
        this.hatchesDropped = hatchesDropped;
        this.cargoDropped = cargoDropped;
        this.crossHabLine = crossHabLine;
        this.opponentCargoShipLineFoul = opponentCargoShipLineFoul;
        this.sideCargoShipHatchCapable = sideCargoShipHatchCapable;
        this.frontCargoShipHatchCapable = frontCargoShipHatchCapable;
        this.cargoDroppedCargoShip = cargoDroppedCargoShip;
        this.cargoDroppedRocket = cargoDroppedRocket;
        this.hatchesDroppedRocket = hatchesDroppedRocket;
        this.hatchesDroppedCargoShip = hatchesDroppedCargoShip;
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

    public boolean isCrossHabLine() {
        return crossHabLine;
    }

    public boolean isOpponentCargoShipLineFoul() {
        return opponentCargoShipLineFoul;
    }

    public boolean isFrontCargoShipHatchCapable() {
        return frontCargoShipHatchCapable;
    }

    public boolean isSideCargoShipHatchCapable() {
        return sideCargoShipHatchCapable;
    }

    public boolean isHatchesDroppedRocket() {
        return hatchesDroppedRocket;
    }

    public boolean isHatchesDroppedCargoShip() {
        return hatchesDroppedCargoShip;
    }

    public boolean isCargoDroppedCargoShip() {
        return cargoDroppedCargoShip;
    }

    public boolean isCargoDroppedRocket() {
        return cargoDroppedRocket;
    }

    public void setCrossHabLine(boolean crossHabLine) {
        this.crossHabLine = crossHabLine;
    }

    public void setSideCargoShipHatchCapable(boolean sideCargoShipHatchCapable) {
        this.sideCargoShipHatchCapable = sideCargoShipHatchCapable;
    }

    public void setCargoDroppedCargoShip(boolean cargoDroppedCargoShip) {
        this.cargoDroppedCargoShip = cargoDroppedCargoShip;
    }

    public void setCargoDroppedRocket(boolean cargoDroppedRocket) {
        this.cargoDroppedRocket = cargoDroppedRocket;
    }

    public void setCargoShipHatches(int cargoShipHatches) {
        this.cargoShipHatches = cargoShipHatches;
    }

    public void setRocketHatches(int rocketHatches) {
        this.rocketHatches = rocketHatches;
    }

    public void setCargoShipCargo(int cargoShipCargo) {
        this.cargoShipCargo = cargoShipCargo;
    }

    public void setRocketCargo(int rocketCargo) {
        this.rocketCargo = rocketCargo;
    }

    public void setFrontCargoShipHatchCapable(boolean frontCargoShipHatchCapable) {
        this.frontCargoShipHatchCapable = frontCargoShipHatchCapable;
    }

    public void setHatchesDroppedCargoShip(boolean hatchesDroppedCargoShip) {
        this.hatchesDroppedCargoShip = hatchesDroppedCargoShip;
    }

    public void setHatchesDropped(int hatchesDropped) {
        this.hatchesDropped = hatchesDropped;
    }

    public void setCargoDropped(int cargoDropped) {
        this.cargoDropped = cargoDropped;
    }

    public void setHatchesDroppedRocket(boolean hatchesDroppedRocket) {
        this.hatchesDroppedRocket = hatchesDroppedRocket;
    }

    public void setOpponentCargoShipLineFoul(boolean opponentCargoShipLineFoul) {
        this.opponentCargoShipLineFoul = opponentCargoShipLineFoul;
    }

}
