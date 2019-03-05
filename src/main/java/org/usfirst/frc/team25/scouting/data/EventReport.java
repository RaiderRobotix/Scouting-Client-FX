package org.usfirst.frc.team25.scouting.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thebluealliance.api.v3.models.Match;
import com.thebluealliance.api.v3.models.MatchScoreBreakdown2019Allliance;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.usfirst.frc.team25.scouting.data.models.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Object model holding all data for an event
 */
public class EventReport {

    /**
     * Unsorted list of ScoutEntries
     */
    private final ArrayList<ScoutEntry> scoutEntries;
    private final String event;
    private final File directory;
    private HashMap<Integer, TeamReport> teamReports;
    private String inaccuracyList;
    private File teamNameList;

    /**
     * Constructs an <code>EventReport</code> based on scouting data
     *
     * @param entries   List of all scouting entries for that particular event
     * @param event     Event key of the current event (e.g. <code>2019njfla</code>)
     * @param directory Working data directory for reading/writing files
     */
    public EventReport(ArrayList<ScoutEntry> entries, String event, File directory) {
        teamReports = new HashMap<>();
        inaccuracyList = "";

        this.scoutEntries = entries;
        this.event = event;
        this.directory = directory;

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
            BlueAlliance.downloadQualificationMatchData(event, directory);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            ArrayList<Match> matchData = FileManager.deserializeScoreBreakdown(
                    new File(directory.getAbsoluteFile() + "/ScoreBreakdown - " + event + ".json"));


            for (ScoutEntry entry : scoutEntries) {
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
                                for (int i = 0; i < partnerTeams.length; i++) {
                                    if (findActualEndHabLevel(partnerTeams[i], sb) > maxActualHabClimbLevel) {
                                        maxActualHabClimbLevel = findActualEndHabLevel(partnerTeams[i], sb);
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

                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            if (!inaccuracyList.isEmpty()) {
                FileManager.outputFile(directory.getAbsolutePath() + "/Inaccuracies - " + event, "txt",
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
    public ScoutEntry[] findPartnerEntries(ScoutEntry entry) {
        ScoutEntry[] partnerTeams = new ScoutEntry[2];
        int numberFound = 0;
        for (ScoutEntry searchEntry : scoutEntries) {
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

    public int findActualEndHabLevel(ScoutEntry scoutEntry, MatchScoreBreakdown2019Allliance sb) {

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

    public int findActualStartHabLevel(ScoutEntry teamNum, MatchScoreBreakdown2019Allliance sb) {
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

    public boolean isActualNoShow(ScoutEntry teamNum, MatchScoreBreakdown2019Allliance sb) {
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

    public boolean isActualCrossHabLine(ScoutEntry entry, MatchScoreBreakdown2019Allliance sb) {
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

    public boolean isTeamPlaying(int teamNum) {
        for (int i : teamReports.keySet()) {
            if (teamNum == i) {
                return true;
            }
        }
        return false;
    }


    public void processEntries() {

        for (ScoutEntry entry : scoutEntries) {

            entry.calculateDerivedStats();

            int teamNum = entry.getPreMatch().getTeamNum();

            if (!teamReports.containsKey(teamNum)) {
                teamReports.put(teamNum, new TeamReport(teamNum));
            }

            teamReports.get(teamNum).addEntry(entry);
        }


        for (Integer key : teamReports.keySet()) {

            TeamReport report = teamReports.get(key);
            if (teamNameList != null) {

                report.autoGetTeamName(teamNameList);

            }

            report.processReport();
        }


    }

    /**
     * Generates summary and team Excel spreadsheets
     *
     * @param outputDirectory Output directory for generated fields
     */
    public boolean generateRawSpreadsheet(File outputDirectory) {

        StringBuilder fileContents = new StringBuilder(generateSpreadsheetHeader() + "\n");
        StringBuilder noShowFileContents = new StringBuilder(fileContents);

        for (ScoutEntry entry : scoutEntries) {

            StringBuilder entryContents = new StringBuilder();

            Object[] dataObjects = {entry.getPreMatch(), entry, entry.getAutonomous(), entry.getTeleOp(),
                    entry.getPostMatch()};


            for (Object dataObject : dataObjects) {

                // returns all members including private members but not inherited members.
                Field[] fields = dataObject.getClass().getDeclaredFields();

                for (Field metric : fields) {
                    Object metricValue = "";

                    //Index to account  for the substring shift from "is" or "get"
                    int shiftIndex = 3;

                    if (metric.getType() == boolean.class) {
                        shiftIndex = 2;
                    }

                    //We'll output the quick comment HashMap separately
                    if (metric.getType() != boolean.class && metric.getType() != int.class && metric.getType() != String.class) {
                        continue;
                    }

                    try {
                        metricValue =
                                Stats.getCorrectMethod(dataObject.getClass(), metric.getName(), shiftIndex).invoke(dataObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    entryContents.append(metricValue).append(",");
                }

            }

            for (String key : scoutEntries.get(0).getPostMatch().getRobotQuickCommentSelections().keySet()) {
                entryContents.append(entry.getPostMatch().getRobotQuickCommentSelections().get(key)).append(",");
            }

            entryContents.append('\n');

            fileContents.append(entryContents);

            if (!entry.getPreMatch().isRobotNoShow()) {
                noShowFileContents.append(entryContents);
            }


        }

        try {
            FileManager.outputFile(outputDirectory.getAbsolutePath() + "/Data - All - " + event, "csv",
                    fileContents.toString());
            FileManager.outputFile(outputDirectory.getAbsolutePath() + "/Data - No Show Removed - " + event, "csv",
                    noShowFileContents.toString());
        } catch (FileNotFoundException e) {

            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Automatically generates spreadsheet header based on object model field variables
     */
    private String generateSpreadsheetHeader() {
        StringBuilder header = new StringBuilder();

        String[] shortNames = {"Pre", "Overall", "Auto", "Tele", "Post"};
        Class[] dataModels = {PreMatch.class, ScoutEntry.class, Autonomous.class, TeleOp.class, PostMatch.class};

        for (int i = 0; i < shortNames.length; i++) {
            Field[] fields = dataModels[i].getDeclaredFields();

            for (Field metric : fields) {
                if (metric.getType() != int.class && metric.getType() != boolean.class && metric.getType() != String.class) {
                    continue;
                }

                if (i == 2 || i == 3) {
                    header.append(shortNames[i] + " - ");
                }
                header.append(StringProcessing.convertCamelToSentenceCase(metric.getName()) + ",");
            }
        }

        for (String key : scoutEntries.get(0).getPostMatch().getRobotQuickCommentSelections().keySet()) {
            header.append(StringProcessing.removeCommasBreaks(key)).append(",");
        }


        return header.toString();
    }


    /**
     * Serializes the ArrayList of all ScoutEntrys into a JSON file
     *
     * @param outputDirectory The directory to write the combined JSON file to
     * @return true if operation is successful, false otherwise
     */
    public boolean generateCombineJson(File outputDirectory) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(scoutEntries);
        try {
            FileManager.outputFile(outputDirectory.getAbsolutePath() + "/Data - All - " + event, "json", jsonString);
        } catch (FileNotFoundException e) {

            return false;
        }
        return true;
    }

    public void setTeamNameList(File list) {
        this.teamNameList = list;
    }


    /**
     * Serializes the HashMap of all TeamReports
     *
     * @param outputDirectory The directory to write the combined TeamReport JSON files to
     */
    public void generateTeamReportJson(File outputDirectory) {

        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();

        ArrayList<TeamReport> teamReportList = new ArrayList<>();

        for (int key : teamReports.keySet()) {
            teamReportList.add(teamReports.get(key));
        }

        String jsonString = gson.toJson(teamReportList);
        try {
            FileManager.outputFile(outputDirectory.getAbsolutePath() + "/TeamReports - " + event, "json", jsonString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void generateInaccuracyList(File outputDirectory) {
        if (!inaccuracyList.isEmpty()) {
            FileManager.outputFile(new File(outputDirectory.getAbsolutePath() + "/inaccuracies.txt"), inaccuracyList);
        }
    }

    public void generatePicklists(File outputDirectory) {
        PicklistGenerator pg = new PicklistGenerator(scoutEntries, outputDirectory, event);
        pg.generateComparePointList();
        pg.generatePickPointList();
    }

    public void generateMatchPredictions(File outputDirectory) {

        int greatestMatchNum = 0;
        for (ScoutEntry entry : scoutEntries) {
            if (entry.getPreMatch().getMatchNum() > greatestMatchNum) {
                greatestMatchNum = entry.getPreMatch().getMatchNum();
            }
        }
        File matchList = FileManager.getMatchList(outputDirectory);
        String[] blocksOfStringMatchesArray;
        List<String> futureMatchesBlockList = new ArrayList<>();
        try {
            String matchesString = FileManager.getFileString(matchList);
            blocksOfStringMatchesArray = matchesString.split("\n");
            for (String element : blocksOfStringMatchesArray) {
                String[] tempArray = element.split(",");
                for (String tempElement : tempArray) {
                    if (tempElement.equals("25") && Integer.parseInt(tempArray[0]) > greatestMatchNum) {
                        futureMatchesBlockList.add(element);
                    }
                }
            }
            for (String element : futureMatchesBlockList) {
                String[] tempSplitElement = element.split(",");
                for (int i = 1; i < 4; i++) {
                    if (tempSplitElement[i].equals("25")) {
                        AllianceReport futureMatchPredictions1 =
                                new AllianceReport(new TeamReport[]{
                                        teamReports.get(Integer.parseInt(tempSplitElement[1])),
                                        teamReports.get(Integer.parseInt(tempSplitElement[2])),
                                        teamReports.get(Integer.parseInt(tempSplitElement[3]))});
                        futureMatchPredictions1.getQuickAllianceReport();

                        AllianceReport futureMatchPredictions2 =
                                new AllianceReport(new TeamReport[]{
                                        teamReports.get(Integer.parseInt(tempSplitElement[4])),
                                        teamReports.get(Integer.parseInt(tempSplitElement[5])),
                                        teamReports.get(Integer.parseInt(tempSplitElement[6]))});
                        futureMatchPredictions2.getQuickAllianceReport();
                    } else {
                        AllianceReport futureMatchPredictions1 =
                                new AllianceReport(new TeamReport[]{
                                        teamReports.get(Integer.parseInt(tempSplitElement[4])),
                                        teamReports.get(Integer.parseInt(tempSplitElement[5])),
                                        teamReports.get(Integer.parseInt(tempSplitElement[6]))});
                        futureMatchPredictions1.getQuickAllianceReport();

                        AllianceReport futureMatchPrediction2 =
                                new AllianceReport(new TeamReport[]{
                                        teamReports.get(Integer.parseInt(tempSplitElement[1])),
                                        teamReports.get(Integer.parseInt(tempSplitElement[2])),
                                        teamReports.get(Integer.parseInt(tempSplitElement[3]))});
                        futureMatchPrediction2.getQuickAllianceReport();
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    public AllianceReport[] getAlliancesInMatch(int matchNum) throws FileNotFoundException {
        AllianceReport[] allianceReports = new AllianceReport[2];
        try {
            File matchList = FileManager.getMatchList(directory);

            String[] matches = FileManager.getFileString(matchList).split("\n");
            for (String match : matches) {
                String[] terms = match.split(",");

                if (Integer.parseInt(terms[0]) == matchNum) {
                    for (int i = 0; i < 2; i++) {
                        int[] teamNums = new int[3];
                        for (int j = 0; j < 3; j++) {
                            teamNums[j] = Integer.parseInt(terms[j + 1 + 3 * i]);
                        }
                        allianceReports[i] = getAllianceReport(teamNums);
                    }

                    return allianceReports;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw new FileNotFoundException("Match list or scouting data not found");
        }

        return allianceReports;

    }


    public TeamReport getTeamReport(int teamNum) {
        return teamReports.get(teamNum);
    }

    public AllianceReport getAllianceReport(int[] teamNums) {
        TeamReport[] teamReports = new TeamReport[teamNums.length];
        for (int i = 0; i < teamNums.length; i++) {
            if (getTeamReport(teamNums[i]) != null) {
                teamReports[i] = getTeamReport(teamNums[i]);
            }
        }

        return new AllianceReport(teamReports);
    }


}
