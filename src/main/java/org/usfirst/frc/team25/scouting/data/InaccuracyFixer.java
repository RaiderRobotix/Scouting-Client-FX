package org.usfirst.frc.team25.scouting.data;

import com.thebluealliance.api.v3.models.Match;
import com.thebluealliance.api.v3.models.MatchScoreBreakdown2019Allliance;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.usfirst.frc.team25.scouting.data.models.ScoutEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Optional;

public class InaccuracyFixer {

    private final EventReport eventReport;

    private String inaccuracyList;

    public InaccuracyFixer(EventReport eventReport) {
        this.eventReport = eventReport;
        inaccuracyList = "";
    }

    /**
     * Fixes errors made in scouting entries based on match details from The Blue Alliance
     * For the 2019 season, this fixes HAB line crossings, starting levels, partner climbs assisted, and HAB climbs
     * Generates a list of inaccuracies, along with scout names, team numbers and match numbers
     *
     * @return <code>true</code> if inaccuracies are found, false otherwise
     */
    public boolean fixInaccuraciesTBA() {

        try {
            // Downloads the most recent match breakdowns
            BlueAlliance.downloadQualificationMatchData(eventReport.getEvent(), eventReport.getDirectory());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            ArrayList<Match> matchData = FileManager.deserializeScoreBreakdown(
                    new File(eventReport.getDirectory().getAbsoluteFile() + "/ScoreBreakdown - " + eventReport.getEvent() + ".json"));


            for (ScoutEntry entry : eventReport.getScoutEntries()) {
                try {
                    String prefix =
                            "Q" + entry.getPreMatch().getMatchNum() + "-" + entry.getPreMatch().getScoutPos() + "-" +
                                    entry.getPreMatch().getScoutName() + ": ";
                    String inaccuracies = "";

                    MatchScoreBreakdown2019Allliance sb;
                    Match match = matchData.get(entry.getPreMatch().getMatchNum() - 1);

                    // Matches scout position with score breakdown objects
                    boolean correctTeamRed = entry.getPreMatch().getScoutPos().contains("Red") && match.getRedAlliance()
                            .getTeamKeys()[Integer.parseInt(entry.getPreMatch().getScoutPos().split(" ")[1]) - 1]
                            .equals("frc" + entry.getPreMatch().getTeamNum());
                    boolean correctTeamBlue =
                            entry.getPreMatch().getScoutPos().contains("Blue") && match.getBlueAlliance()
                                    .getTeamKeys()[Integer.parseInt(entry.getPreMatch().getScoutPos().split(" ")[1])
                                    - 1].equals("frc" + entry.getPreMatch().getTeamNum());

                    if (correctTeamBlue || correctTeamRed) {

                        if (entry.getPreMatch().getScoutPos().contains("Red")) {
                            sb = match.getScoreBreakdown().getRed();
                        } else {
                            sb = match.getScoreBreakdown().getBlue();
                        }

                        if (isActualNoShow(entry, sb) != entry.getPreMatch().isRobotNoShow()) {
                            inaccuracies += "ROBOT NO SHOW, ";
                            entry.getPreMatch().setRobotNoShow(isActualNoShow(entry, sb));
                        }

                        if (!entry.getPreMatch().isRobotNoShow()) {
                            if (findActualStartHabLevel(entry, sb) != entry.getPreMatch().getStartingLevel()) {
                                inaccuracies += "starting HAB level, ";
                                entry.getPreMatch().setStartingLevel(findActualStartHabLevel(entry, sb));
                            }

                            if (isActualCrossHabLine(entry, sb) != entry.getAutonomous().isCrossHabLine()) {
                                inaccuracies += "auto cross hab line, ";
                                entry.getAutonomous().setCrossHabLine(isActualCrossHabLine(entry, sb));
                            }

                            // This doesn't check for the case where the scout put 2 assists, but only 1 occurred
                            if (entry.getTeleOp().getNumPartnerClimbAssists() > 0) {
                                ScoutEntry[] partnerTeams = findPartnerEntries(entry);
                                int maxActualHabClimbLevel = 0;
                                for (ScoutEntry partnerTeam : partnerTeams) {
                                    if (findActualEndHabLevel(partnerTeam, sb) > maxActualHabClimbLevel) {
                                        maxActualHabClimbLevel = findActualEndHabLevel(partnerTeam, sb);
                                    }
                                }
                                if (maxActualHabClimbLevel < entry.getTeleOp().getPartnerClimbAssistEndLevel()) {
                                    inaccuracies += "partner climb assist level, ";
                                    if (maxActualHabClimbLevel > 1) {
                                        // Assisted to level 2
                                        entry.getTeleOp().setPartnerClimbAssistEndLevel(maxActualHabClimbLevel);
                                    } else {
                                        // Can't assist to level 1
                                        entry.getTeleOp().setPartnerClimbAssistEndLevel(0);
                                        entry.getTeleOp().setNumPartnerClimbAssists(0);
                                    }
                                }

                            }

                            if (entry.getTeleOp().getSuccessHabClimbLevel() != findActualEndHabLevel(entry, sb)) {

                                int actualEndHabLevel = findActualEndHabLevel(entry, sb);
                                boolean correctionNeeded = true;


                                // Case 1: Partners assisted to level
                                if (entry.getTeleOp().isClimbAssistedByPartner()) {
                                    ScoutEntry[] partners = findPartnerEntries(entry);
                                    for (ScoutEntry partner : partners) {
                                        if (partner.getPreMatch().getTeamNum() == entry.getTeleOp().getAssistingClimbTeamNum()) {
                                            if (partner.getTeleOp().getNumPartnerClimbAssists() >= 1 && partner.getTeleOp().getPartnerClimbAssistEndLevel() >= actualEndHabLevel) {
                                                correctionNeeded = false;
                                            }
                                        }
                                    }
                                }

                                if (correctionNeeded) {

                                    // Case 2: HAB line foul & Case 3: Scout is inaccurate
                                    if (actualEndHabLevel == 3) {

                                        Alert alert = new Alert(Alert.AlertType.NONE);
                                        alert.setTitle("Inaccurate HAB Climb Level");
                                        alert.setHeaderText("Team " + entry.getPreMatch().getTeamNum() + "\nMatch " +
                                                "Number " + entry.getPreMatch().getMatchNum());
                                        alert.setContentText("Choose the correct ending level");

                                        ButtonType buttonTypeOne = new ButtonType("Level 1");
                                        ButtonType buttonTypeTwo = new ButtonType("Level 2");
                                        ButtonType buttonTypeThree = new ButtonType("Level 3");
                                        ButtonType buttonTypeNone = new ButtonType("No climb");

                                        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeThree,
                                                buttonTypeNone);

                                        Optional<ButtonType> result = alert.showAndWait();
                                        if (result.get() == buttonTypeOne) {
                                            actualEndHabLevel = 1;
                                        } else if (result.get() == buttonTypeTwo) {
                                            actualEndHabLevel = 2;
                                        } else if (result.get() == buttonTypeThree) {
                                            actualEndHabLevel = 3;
                                        } else {
                                            actualEndHabLevel = 0;
                                        }
                                    }

                                    if (entry.getTeleOp().getSuccessHabClimbLevel() != actualEndHabLevel) {

                                        inaccuracies += "success climb level ";
                                        entry.getTeleOp().setSuccessHabClimbLevel(actualEndHabLevel);
                                        if (actualEndHabLevel > 0) {
                                            entry.getTeleOp().setSuccessHabClimb(true);
                                            entry.getTeleOp().setAttemptHabClimb(true);
                                            if (entry.getTeleOp().getAttemptHabClimbLevel() < actualEndHabLevel) {
                                                entry.getTeleOp().setAttemptHabClimbLevel(actualEndHabLevel);
                                            }
                                        } else {
                                            entry.getTeleOp().setSuccessHabClimb(false);
                                            entry.getTeleOp().setAttemptHabClimb(false);
                                            entry.getTeleOp().setAttemptHabClimbLevel(0);
                                        }
                                    }
                                }

                            }
                        }
                    }
                    if (!inaccuracies.isEmpty()) {
                        inaccuracyList += prefix + inaccuracies + "\n";
                    }

                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            if (!inaccuracyList.isEmpty()) {
                eventReport.processEntries();
                FileManager.outputFile(eventReport.getDirectory(), "Inaccuracies - " + eventReport.getEvent(), "txt",
                        inaccuracyList);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Finds the scouting entries of alliance partners alongside the current team
     *
     * @param entry Scouting entry of the team in the match to be queried
     * @return <code>ScoutEntry</code> array of the two partner scout entries
     */
    private ScoutEntry[] findPartnerEntries(ScoutEntry entry) {
        ScoutEntry[] partnerTeams = new ScoutEntry[2];
        int numberFound = 0;
        for (ScoutEntry searchEntry : eventReport.getScoutEntries()) {
            if ((searchEntry.getPreMatch().getMatchNum() == entry.getPreMatch().getMatchNum() && searchEntry
                    .getPreMatch().getScoutPos().charAt(0) == entry.getPreMatch().getScoutPos().charAt(0)) &&
                    searchEntry.getPreMatch().getTeamNum() != entry.getPreMatch().getTeamNum()) {
                partnerTeams[numberFound] = searchEntry;
                numberFound++;
                if (numberFound == 2) {
                    return partnerTeams;
                }
            }
        }

        return partnerTeams;
    }

    private int findActualEndHabLevel(ScoutEntry scoutEntry, MatchScoreBreakdown2019Allliance sb) {

        try {
            if (scoutEntry.getPreMatch().getScoutPos().contains("1")) {
                return Integer.parseInt(sb.getEndgameRobot1().substring(sb.getEndgameRobot1().length() - 1));
            } else if (scoutEntry.getPreMatch().getScoutPos().contains("2")) {
                return Integer.parseInt(sb.getEndgameRobot2().substring(sb.getEndgameRobot2().length() - 1));
            } else if (scoutEntry.getPreMatch().getScoutPos().contains("3")) {
                return Integer.parseInt(sb.getEndgameRobot3().substring(sb.getEndgameRobot3().length() - 1));
            }
        } catch (NumberFormatException e) {
            return 0;
        }
        return 0;
    }

    private int findActualStartHabLevel(ScoutEntry teamNum, MatchScoreBreakdown2019Allliance sb) {
        try {
            if (teamNum.getPreMatch().getScoutPos().contains("1")) {
                return Integer.parseInt(sb.getPreMatchLevelRobot1().substring(sb.getPreMatchLevelRobot1().length() -
                        1));

            } else if (teamNum.getPreMatch().getScoutPos().contains("2")) {
                return Integer.parseInt(sb.getPreMatchLevelRobot2().substring(sb.getPreMatchLevelRobot2().length() -
                        1));
            } else if (teamNum.getPreMatch().getScoutPos().contains("3")) {
                return Integer.parseInt(sb.getPreMatchLevelRobot3().substring(sb.getPreMatchLevelRobot3().length() -
                        1));
            }
        } catch (NumberFormatException e) {
            return 0;
        }
        return 0;
    }

    private boolean isActualNoShow(ScoutEntry teamNum, MatchScoreBreakdown2019Allliance sb) {
        if (teamNum.getPreMatch().getScoutPos().contains("1") && (sb.getPreMatchLevelRobot1().contains("None") || sb
                .getPreMatchLevelRobot1().contains("Unknown"))) {
            return true;
        } else if (teamNum.getPreMatch().getScoutPos().contains("2") && (sb.getPreMatchLevelRobot2().contains("None")
                || sb.getPreMatchLevelRobot2().contains("Unknown"))) {
            return true;
        } else {
            return teamNum.getPreMatch().getScoutPos().contains("3") && (sb.getPreMatchLevelRobot3().contains("None")
                    || sb.getPreMatchLevelRobot3().contains("Unknown"));
        }
    }

    private boolean isActualCrossHabLine(ScoutEntry entry, MatchScoreBreakdown2019Allliance sb) {
        if (entry.getPreMatch().getScoutPos().contains("1") && sb.getHabLineRobot1().equals(
                "CrossedHabLineInSandstorm")) {
            return true;
        }
        if (entry.getPreMatch().getScoutPos().contains("2") && sb.getHabLineRobot2().equals(
                "CrossedHabLineInSandstorm")) {
            return true;
        }
        return entry.getPreMatch().getScoutPos().contains("3") && sb.getHabLineRobot3().equals(
                "CrossedHabLineInSandstorm");

    }

    public String getInaccuracyList() {
        return inaccuracyList;
    }

    public void generateInaccuracyList(File outputDirectory) {
        try {
            if (!inaccuracyList.isEmpty()) {
                FileManager.outputFile(new File(outputDirectory.getAbsolutePath() + "/inaccuracies.txt"),
                        inaccuracyList);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


}
