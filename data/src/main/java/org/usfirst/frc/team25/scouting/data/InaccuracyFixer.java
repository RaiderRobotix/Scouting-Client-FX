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

/**
 * Class that fixes the inaccurate scout entries found in an event report and outputs a list of inaccuracies
 */
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
     * @return <code>true</code> if inaccuracies are found, <code>false</code> otherwise or if the score breakdown
     * data file does not exist or cannot be downloaded
     */
    public boolean fixInaccuraciesTBA() { // TODO Think of all possible inaccuracies and check for them

        try {
            // Downloads the most recent match data with score breakdowns
            BlueAlliance.downloadQualificationMatchData(eventReport.getEvent(), eventReport.getDirectory());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ArrayList<Match> matchData = FileManager.deserializeScoreBreakdown(
                    new File(eventReport.getDirectory().getAbsoluteFile() + "/ScoreBreakdown - " + eventReport.getEvent() + ".json"));

            for (ScoutEntry entry : eventReport.getScoutEntries()) {
                try {
                    // Prefix for the match in the inaccuracy report
                    String prefix =
                            "Q" + entry.preMatch().matchNum() + "-" + entry.preMatch().scoutPos() + "-" +
                                    entry.preMatch().scoutName() + ": ";
                    String inaccuracies = "";

                    MatchScoreBreakdown2019Allliance sb = getMatchScoreBreakdown(matchData, entry);

                    if (sb != null) {

//                        if (actual_prop != scout_data) {
//                            inaccuracies += "oops";
//                            set_value(actual_prop);
//                        }

                    }

                    if (!inaccuracies.isEmpty()) {
                        inaccuracyList += prefix + inaccuracies + "\n";
                    }

                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            if (!inaccuracyList.isEmpty()) {
                // Recalculates aggregate stats after correcting errors
                eventReport.processEntries();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets the match score breakdown associated with the team and match recorded in a scouting entry
     *
     * @param matchData ArrayList of TBA match data objects
     * @param entry     Scout entry that contains a match and team number
     * @return The appropriate match score breakdown object for the entry
     */
    private MatchScoreBreakdown2019Allliance getMatchScoreBreakdown(ArrayList<Match> matchData, ScoutEntry entry) {
        Match match = matchData.get(entry.preMatch().matchNum() - 1);

        // Validates if the team in the scout entry matches with the one in a match object
        int positionIndex = Integer.parseInt(entry.preMatch().scoutPos().split(" ")[1]) - 1;

        boolean correctTeamRed = entry.preMatch().scoutPos().contains("Red") && match.getRedAlliance()
                .getTeamKeys()[positionIndex].equals("frc" + entry.preMatch().teamNum());
        boolean correctTeamBlue =
                entry.preMatch().scoutPos().contains("Blue") && match.getBlueAlliance()
                        .getTeamKeys()[positionIndex].equals("frc" + entry.preMatch().teamNum());

        if (correctTeamBlue || correctTeamRed) {
            if (entry.preMatch().scoutPos().contains("Red")) {
                return match.getScoreBreakdown().getRed();
            } else {
                return match.getScoreBreakdown().getBlue();
            }
        }

        return null;
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
            boolean matchNumMatches = searchEntry.preMatch().matchNum() == entry.preMatch().matchNum();
            boolean allianceMatches =
                    searchEntry.preMatch().scoutPos().charAt(0) == entry.preMatch().scoutPos().charAt(0);
            if (matchNumMatches && allianceMatches && searchEntry.preMatch().teamNum() != entry.preMatch().teamNum()) {
                partnerTeams[numberFound] = searchEntry;
                numberFound++;
                if (numberFound == 2) {
                    return partnerTeams;
                }
            }
        }

        return partnerTeams;
    }

    /**
     * Finds the HAB level to which a team actually climbed to in a match
     *
     * @param scoutEntry Scout entry to be checked
     * @param sb         Score breakdown object associated with the scout entry
     * @return The team's actual ending HAB level, 0 if the team did not climb on the HAB
     */
    private int findActualEndHabLevel(ScoutEntry scoutEntry, MatchScoreBreakdown2019Allliance sb) {
        try {
            if (scoutEntry.preMatch().scoutPos().contains("1")) {
                return Integer.parseInt(sb.getEndgameRobot1().substring(sb.getEndgameRobot1().length() - 1));
            } else if (scoutEntry.preMatch().scoutPos().contains("2")) {
                return Integer.parseInt(sb.getEndgameRobot2().substring(sb.getEndgameRobot2().length() - 1));
            } else if (scoutEntry.preMatch().scoutPos().contains("3")) {
                return Integer.parseInt(sb.getEndgameRobot3().substring(sb.getEndgameRobot3().length() - 1));
            }
        } catch (NumberFormatException e) {
            return 0;
        }
        return 0;
    }

    /**
     * Finds the HAB level to which a team actually started on in a match
     *
     * @param scoutEntry Scout entry to be checked
     * @param sb         Score breakdown object associated with the scout entry
     * @return The team's actual starting HAB level, 0 if the team did not show up
     */
    private int findActualStartHabLevel(ScoutEntry scoutEntry, MatchScoreBreakdown2019Allliance sb) {
        try {
            if (scoutEntry.preMatch().scoutPos().contains("1")) {
                return Integer.parseInt(sb.getPreMatchLevelRobot1().substring(sb.getPreMatchLevelRobot1().length() -
                        1));
            } else if (scoutEntry.preMatch().scoutPos().contains("2")) {
                return Integer.parseInt(sb.getPreMatchLevelRobot2().substring(sb.getPreMatchLevelRobot2().length() -
                        1));
            } else if (scoutEntry.preMatch().scoutPos().contains("3")) {
                return Integer.parseInt(sb.getPreMatchLevelRobot3().substring(sb.getPreMatchLevelRobot3().length() -
                        1));
            }
        } catch (NumberFormatException e) {
            return 0;
        }
        return 0;
    }

    /**
     * Finds if a team actually did not show up to a match via its starting position
     *
     * @param scoutEntry Scout entry to be checked
     * @param sb         Score breakdown object associated with the scout entry
     * @return True if the team did not show up, false otherwise
     */
    private boolean isActualNoShow(ScoutEntry scoutEntry, MatchScoreBreakdown2019Allliance sb) {
        if (scoutEntry.preMatch().scoutPos().contains("1") && (sb.getPreMatchLevelRobot1().contains("None") || sb
                .getPreMatchLevelRobot1().contains("Unknown"))) {
            return true;
        } else if (scoutEntry.preMatch().scoutPos().contains("2") && (sb.getPreMatchLevelRobot2().contains("None")
                || sb.getPreMatchLevelRobot2().contains("Unknown"))) {
            return true;
        } else {
            return scoutEntry.preMatch().scoutPos().contains("3") && (sb.getPreMatchLevelRobot3().contains("None")
                    || sb.getPreMatchLevelRobot3().contains("Unknown"));
        }
    }

    /**
     * Finds if a team actually crossed the HAB line during the sandstorm period
     *
     * @param entry Scout entry to be checked
     * @param sb    Score breakdown object associated with the scout entry
     * @return True if the team crossed the HAB line during the sandstorm period, false otherwise
     */
    private boolean isActualCrossHabLine(ScoutEntry entry, MatchScoreBreakdown2019Allliance sb) {
        if (entry.preMatch().scoutPos().contains("1") && sb.getHabLineRobot1().equals(
                "CrossedHabLineInSandstorm")) {
            return true;
        }
        if (entry.preMatch().scoutPos().contains("2") && sb.getHabLineRobot2().equals(
                "CrossedHabLineInSandstorm")) {
            return true;
        }
        return entry.preMatch().scoutPos().contains("3") && sb.getHabLineRobot3().equals(
                "CrossedHabLineInSandstorm");
    }

    public String getInaccuracyList() {
        return inaccuracyList;
    }

    /**
     * Saves a detailed list of inaccuracies to a text file
     *
     * @param outputDirectory Directory in which the inaccuracy list is saved
     */
    public void saveInaccuracyList(File outputDirectory) {
        try {
            if (!inaccuracyList.isEmpty()) {
                FileManager.outputFile(eventReport.getDirectory(), "Inaccuracies - " + eventReport.getEvent(), "txt",
                        inaccuracyList);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
