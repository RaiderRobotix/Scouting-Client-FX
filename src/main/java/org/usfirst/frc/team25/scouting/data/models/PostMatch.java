package org.usfirst.frc.team25.scouting.data.models;

import java.util.HashMap;

/**
 * Qualitative reflection on the robot's performance after a match
 * Not to be used for end game actions
 */
public class PostMatch {


    private final int teamOneCompare;
    private final int teamTwoCompare;
    private final int pickNumber;
    private final String comparison;
    private HashMap<String, Boolean> robotQuickCommentSelections;
    private transient String robotQuickCommentStr;
    private String robotComment, focus;

    public PostMatch(String robotComment, HashMap<String, Boolean> robotQuickCommentSelections,
                     String focus, int teamOneCompare, int teamTwoCompare, String comparison, int pickNumber) {
        this.robotComment = robotComment;
        this.robotQuickCommentSelections = robotQuickCommentSelections;
        this.focus = focus;
        this.teamOneCompare = teamOneCompare;
        this.teamTwoCompare = teamTwoCompare;
        this.comparison = comparison;
        this.pickNumber = pickNumber;
    }

    public HashMap<String, Boolean> getRobotQuickCommentSelections() {
        return robotQuickCommentSelections;
    }

    public void setRobotQuickCommentSelections(HashMap<String, Boolean> robotQuickCommentSelections) {
        this.robotQuickCommentSelections = robotQuickCommentSelections;
    }

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }

    public String getRobotQuickCommentStr() {
        return robotQuickCommentStr;
    }

    public void setRobotQuickCommentStr(String robotQuickCommentStr) {
        this.robotQuickCommentStr = robotQuickCommentStr;
    }

    public String getRobotComment() {
        return robotComment;
    }

    public void setRobotComment(String robotComment) {
        this.robotComment = robotComment;
    }

    void generateQuickCommentStr() {
        robotQuickCommentStr = "";
        for (String comment : robotQuickCommentSelections.keySet()) {
            if (robotQuickCommentSelections.get(comment)) {
                robotQuickCommentStr += comment + "; ";
            }
        }
    }

    public int getTeamOneCompare() {
        return teamOneCompare;
    }

    public int getTeamTwoCompare() {
        return teamTwoCompare;
    }

    public int getPickNumber() {
        return pickNumber;
    }

    public String getComparison() {
        return comparison;
    }


}