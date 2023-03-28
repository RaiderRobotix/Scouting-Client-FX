package org.usfirst.frc.team25.scouting.data.models;


/**
 * Container holding data from the tele-operated period
 * Includes endgame data
 */
public class TeleOp {

    private int coneTopTele;
    private int cubeTopTele;

    private int coneMidTele;
    private int cubeMidTele;

    private int coneBttmTele;
    private int cubeBttmTele;

    private int coneDroppedTele;
    private int cubeDroppedTele;

    private boolean dockAttemptTele;
    private String dockStatusTele;

    private boolean robotCommitedFoulTele;
    private String foulTypeTele;

    public TeleOp(int coneTop, int cubeTop, int coneMid, int cubeMid, int coneBttm, int cubeBttm, int coneDropped,
                  int cubeDropped, boolean dockAttempt, String dockStatus, boolean robotCommitedFoul, String foulType

    ) {
        this.coneTopTele = coneTop;
        this.cubeTopTele = cubeTop;
        this.coneMidTele = coneMid;
        this.cubeMidTele = cubeMid;
        this.coneBttmTele = coneBttm;
        this.cubeBttmTele = cubeBttm;
        this.coneDroppedTele = coneDropped;
        this.cubeDroppedTele = cubeDropped;
        this.dockAttemptTele = dockAttempt;
        this.dockStatusTele = dockStatus;
        this.robotCommitedFoulTele = robotCommitedFoul;
        this.foulTypeTele = foulType;

    }

    public void setRobotCommitedFoulTele(boolean robotCommitedFoulTele) {
        this.robotCommitedFoulTele = robotCommitedFoulTele;
    }

    public void setFoulTypeTele(String foulTypeTele) {
        this.foulTypeTele = foulTypeTele;
    }

    public void setDockStatusTele(String dockStatusTele) {
        this.dockStatusTele = dockStatusTele;
    }

    public void setDockAttemptTele(boolean dockAttemptTele) {
        this.dockAttemptTele = dockAttemptTele;
    }

    public void setCubeTopTele(int cubeTopTele) {
        this.cubeTopTele = cubeTopTele;
    }

    public void setCubeMidTele(int cubeMidTele) {
        this.cubeMidTele = cubeMidTele;
    }

    public void setCubeDroppedTele(int cubeDroppedTele) {
        this.cubeDroppedTele = cubeDroppedTele;
    }

    public void setCubeBttmTele(int cubeBttmTele) {
        this.cubeBttmTele = cubeBttmTele;
    }

    public void setConeTopTele(int coneTopTele) {
        this.coneTopTele = coneTopTele;
    }

    public void setConeMidTele(int coneMidTele) {
        this.coneMidTele = coneMidTele;
    }

    public void setConeDroppedTele(int coneDroppedTele) {
        this.coneDroppedTele = coneDroppedTele;
    }

    public void setConeBttmTele(int coneBttmTele) {
        this.coneBttmTele = coneBttmTele;
    }

    public boolean isDockAttemptTele() {
        return dockAttemptTele;
    }

    public boolean isRobotCommitedFoulTele() {
        return robotCommitedFoulTele;
    }

    public int getConeBttmTele() {
        return coneBttmTele;
    }

    public int getConeDroppedTele() {
        return coneDroppedTele;
    }

    public int getConeMidTele() {
        return coneMidTele;
    }

    public int getConeTopTele() {
        return coneTopTele;
    }

    public int getCubeBttmTele() {
        return cubeBttmTele;
    }

    public int getCubeDroppedTele() {
        return cubeDroppedTele;
    }

    public int getCubeMidTele() {
        return cubeMidTele;
    }

    public int getCubeTopTele() {
        return cubeTopTele;
    }

    public String getDockStatusTele() {
        return dockStatusTele;
    }

    public String getFoulTypeTele() {
        return foulTypeTele;
    }
}