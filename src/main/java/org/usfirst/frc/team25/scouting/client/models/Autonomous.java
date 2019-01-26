package org.usfirst.frc.team25.scouting.client.models;


/**
 * Object model for autonomous period of a match
 */
public class Autonomous {

    private int cargoToRocket;
    private int hatchPanelsToRocket;
    private int hatchesToCargoShip;
    private int cargoToCargoShip;
    private int hatchesDropped;
    private int cargoDropped;
    private boolean reachHabLine;
    private boolean opponentCargoShipLineFoul;
    private boolean hatchesSideCargo;
    private boolean hatchesFrontCargo;

    public Autonomous(int cargoToRocket, int hatchPanelsToRocket, int hatchesToCargoShip,
                      int cargoToCargoShip, int hatchesDropped, int cargoDropped,
                      boolean reachHabLine, boolean opponentCargoShipLineFoul,
                      boolean hatchesSideCargo, boolean hatchesFrontCargo) {
        this.cargoToRocket = cargoToRocket;
        this.hatchPanelsToRocket = hatchPanelsToRocket;
        this.hatchesToCargoShip = hatchesToCargoShip;
        this.cargoToCargoShip = cargoToCargoShip;
        this.hatchesDropped = hatchesDropped;
        this.cargoDropped = cargoDropped;
        this.reachHabLine = reachHabLine;
        this.opponentCargoShipLineFoul = opponentCargoShipLineFoul;
        this.hatchesSideCargo = hatchesSideCargo;
        this.hatchesFrontCargo = hatchesFrontCargo;
    }

    public int getCargoToRocket() {
        return cargoToRocket;
    }

    public void setCargoToRocket(int cargoToRocket) {
        this.cargoToRocket = cargoToRocket;
    }

    public int getHatchPanelsToRocket() {
        return hatchPanelsToRocket;
    }

    public void setHatchPanelsToRocket(int hatchPanelsToRocket) {
        this.hatchPanelsToRocket = hatchPanelsToRocket;
    }

    public int getHatchesToCargoShip() {
        return hatchesToCargoShip;
    }

    public void setHatchesToCargoShip(int hatchesToCargoShip) {
        this.hatchesToCargoShip = hatchesToCargoShip;
    }

    public int getCargoToCargoShip() {
        return cargoToCargoShip;
    }

    public void setCargoToCargoShip(int cargoToCargoShip) {
        this.cargoToCargoShip = cargoToCargoShip;
    }

    public int getHatchesDropped() {
        return hatchesDropped;
    }

    public void setHatchesDropped(int hatchesDropped) {
        this.hatchesDropped = hatchesDropped;
    }

    public int getCargoDropped() {
        return cargoDropped;
    }

    public void setCargoDropped(int cargoDropped) {
        this.cargoDropped = cargoDropped;
    }

    public boolean isReachHabLine() {
        return reachHabLine;
    }

    public void setReachHabLine(boolean reachHabLine) {
        this.reachHabLine = reachHabLine;
    }

    public boolean isOpponentCargoShipLineFoul() {
        return opponentCargoShipLineFoul;
    }

    public void setOpponentCargoShipLineFoul(boolean opponentCargoShipLineFoul) {
        this.opponentCargoShipLineFoul = opponentCargoShipLineFoul;
    }

    public boolean isHatchesSideCargo() {
        return hatchesSideCargo;
    }

    public void setHatchesSideCargo(boolean hatchesSideCargo) {
        this.hatchesSideCargo = hatchesSideCargo;
    }

    public boolean isHatchesFrontCargo() {
        return hatchesFrontCargo;
    }

    public void setHatchesFrontCargo(boolean hatchesFrontCargo) {
        this.hatchesFrontCargo = hatchesFrontCargo;
    }
}