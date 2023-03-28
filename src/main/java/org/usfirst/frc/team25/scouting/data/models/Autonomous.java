package org.usfirst.frc.team25.scouting.data.models;

/**
 * Object model for autonomous (or sandstorm) period of a match
 */
public class Autonomous {


    private boolean robotExitCommunity;

    private int coneTop;
    private int cubeTop;

    private int coneMid;
    private int cubeMid;

    private int coneBttm;
    private int cubeBttm;

    private int coneDropped;
    private int cubeDropped;

    private boolean dockAttempt;
    private String dockStatus;


    public Autonomous(int coneTop, int cubeTop, int coneMid,
                      int cubeMid, int coneBttm, int cubeBttm,
                      int coneDropped, int cubeDropped,
                      boolean robotExitCommunity, boolean dockAttempt, String dockStatus) {
        this.coneTop = coneTop;
        this.cubeTop = cubeTop;
        this.coneMid = coneMid;
        this.cubeMid = cubeMid;
        this.coneBttm = coneBttm;
        this.cubeBttm = cubeBttm;
        this.coneDropped = coneDropped;
        this.cubeDropped = cubeDropped;
        this.robotExitCommunity = robotExitCommunity;
        this.dockAttempt = dockAttempt;
        this.dockStatus = dockStatus;
    }

    public void setCubeBttm(int cubeBttm) {
        this.cubeBttm = cubeBttm;
    }

    public void setDockStatus(String dockStatus) {
        this.dockStatus = dockStatus;
    }

    public void setDockAttempt(boolean dockAttempt) {
        this.dockAttempt = dockAttempt;
    }

    public void setCubeDropped(int cubeDropped) {
        this.cubeDropped = cubeDropped;
    }

    public void setRobotExitCommunity(boolean robotExitCommunity) {
        this.robotExitCommunity = robotExitCommunity;
    }

    public void setCubeTop(int cubeTop) {
        this.cubeTop = cubeTop;
    }

    public void setCubeMid(int cubeMid) {
        this.cubeMid = cubeMid;
    }

    public void setConeTop(int coneTop) {
        this.coneTop = coneTop;
    }

    public void setConeMid(int coneMid) {
        this.coneMid = coneMid;
    }

    public void setConeDropped(int coneDropped) {
        this.coneDropped = coneDropped;
    }

    public void setConeBttm(int coneBttm) {
        this.coneBttm = coneBttm;
    }

    public boolean isDockAttempt() {
        return dockAttempt;
    }

    public boolean isRobotExitCommunity() {
        return robotExitCommunity;
    }

    public int getConeBttm() {
        return coneBttm;
    }

    public int getConeDropped() {
        return coneDropped;
    }

    public int getConeMid() {
        return coneMid;
    }

    public int getConeTop() {
        return coneTop;
    }

    public int getCubeBttm() {
        return cubeBttm;
    }

    public int getCubeDropped() {
        return cubeDropped;
    }

    public int getCubeMid() {
        return cubeMid;
    }

    public int getCubeTop() {
        return cubeTop;
    }

    public String getDockStatus() {
        return dockStatus;
    }
}
