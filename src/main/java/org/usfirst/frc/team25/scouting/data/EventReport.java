package org.usfirst.frc.team25.scouting.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thebluealliance.api.v3.models.Match;
import com.thebluealliance.api.v3.models.MatchScoreBreakdown2019Allliance;
import org.usfirst.frc.team25.scouting.data.models.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Object model holding all data for an event
 *
 * @author sng
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

    public EventReport(ArrayList<ScoutEntry> entries, String event, File directory) {
        teamReports = new HashMap<>();
        inaccuracyList = "";

        this.scoutEntries = entries;
        this.event = event;
        this.directory = directory;

        for (ScoutEntry entry : scoutEntries) {

            entry.calculateDerivedStats();

            int teamNum = entry.getPreMatch().getTeamNum();

            if (!teamReports.containsKey(teamNum)) {
                teamReports.put(teamNum, new TeamReport(teamNum));
            }

            teamReports.get(teamNum).addEntry(entry);
        }
    }

    public boolean fixInaccuraciesTBA() {

        try {
            BlueAlliance.downloadQualificationMatchData(event, directory);
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {

            ArrayList<Match> matchData = FileManager.deserializeScoreBreakdown(
                    new File(directory.getAbsoluteFile() + "/ScoreBreakdown - " + event + ".json"));


            for (ScoutEntry entry : scoutEntries) {
                try {

                    //Prefix for the inaccuracy list
                    String prefix =
                            "Q" + entry.getPreMatch().getMatchNum() + "-" + entry.getPreMatch().getScoutPos() + "-" +
                                    entry.getPreMatch().getScoutName() + ": ";

                    String inaccuracies = "";

                    Match match = matchData.get(entry.getPreMatch().getMatchNum() - 1);

                    MatchScoreBreakdown2019Allliance sb;
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


                        boolean actualCrossHabLine = false;
                        int actualEndHabLevel = 0, actualStartHabLevel = 0;



                        if (entry.getPreMatch().getScoutPos().contains("1")) {
                            actualCrossHabLine = sb.getHabLineRobot1().equals("CrossedHabLineInSandstorm");
                            for (int i = 1; i <= 3; i++) {
                                if (sb.getEndgameRobot1().contains(Integer.toString(i))) {
                                    actualEndHabLevel = i;
                                }
                                if (sb.getPreMatchLevelRobot1().contains(Integer.toString(i))) {
                                    actualStartHabLevel = i;
                                }
                            }
                        } else if (entry.getPreMatch().getScoutPos().contains("2")) {
                            actualCrossHabLine = sb.getHabLineRobot2().equals("CrossedHabLineInSandstorm");
                            for (int i = 1; i <= 3; i++) {
                                if (sb.getEndgameRobot2().contains(Integer.toString(i))) {
                                    actualEndHabLevel = i;
                                }
                                if (sb.getPreMatchLevelRobot2().contains(Integer.toString(i))) {
                                    actualStartHabLevel = i;
                                }
                            }
                        } else if (entry.getPreMatch().getScoutPos().contains("3")) {
                            actualCrossHabLine = sb.getHabLineRobot3().equals("CrossedHabLineInSandstorm");
                            for (int i = 0; i <= 3; i++) {
                                if (sb.getEndgameRobot3().contains(Integer.toString(i))) {
                                    actualEndHabLevel = i;
                                }
                                if (sb.getPreMatchLevelRobot3().contains(Integer.toString(i))) {
                                    actualStartHabLevel = i;
                                }
                            }
                        }

                        if (actualCrossHabLine != entry.getAutonomous().isCrossHabLine()) {
                            inaccuracies += "auto cross hab line, ";
                            entry.getAutonomous().setCrossHabLine(actualCrossHabLine);
                        }

                        if (!inaccuracies.isEmpty()) {
                            inaccuracyList += prefix + inaccuracies + "\n";
                        }
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

    public boolean isTeamPlaying(int teamNum) {
        for (int i : teamReports.keySet()) {
            if (teamNum == i) {
                return true;
            }
        }
        return false;
    }


    public void processTeamReports() {

        for (Integer key : teamReports.keySet()) {

            TeamReport report = teamReports.get(key);
            if (teamNameList != null) {

                report.autoGetTeamName(teamNameList);

            }

            report.filterNoShow();
            report.findFrequentComments();
            report.calculateStats();
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

            Object[] dataObjects = {entry.getPreMatch(), entry.getAutonomous(), entry.getTeleOp(),
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
                    if (metric.getType() == HashMap.class) {
                        continue;
                    }

                    for (Method m : dataObject.getClass().getMethods()) {

                        if (m.getName().substring(shiftIndex).toLowerCase().equals(metric.getName().toLowerCase()) &&
                                m.getParameterTypes().length == 0) {
                            try {
                                metricValue = m.invoke(dataObject);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }
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

        String[] shortNames = {"Pre", "Auto", "Tele", "Post"};
        Class[] dataModels = {PreMatch.class, Autonomous.class, TeleOp.class, PostMatch.class};

        for (int i = 0; i < shortNames.length; i++) {
            Field[] fields = dataModels[i].getDeclaredFields();

            for (Field metric : fields) {
                if (metric.getType() == HashMap.class) {
                    continue;
                }

                if (i == 1 || i == 2) {
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
     * @param outputDirectory
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
     * @param outputDirectory
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
        pg.generateBogoCompareList();
        pg.generateComparePointList();
        pg.generatePickPointList();
    }

    public void generateMatchPredictions(File outputDirectory) {
        //TODO write this
    }

    public TeamReport getTeamReport(int teamNum) {
        return teamReports.get(teamNum);
    }

    public AllianceReport getAllianceReport(int teamOne, int teamTwo, int teamThree) {
        return new AllianceReport(teamReports.get(teamOne), teamReports.get(teamTwo), teamReports.get(teamThree));
    }

    public ScoutEntry[] findPartnerEntries(int teamNum, int matchNum) {
        ScoutEntry partnerTeams[] = new ScoutEntry[2];
        int numberFound = 0;
        for (int i = 0; i < scoutEntries.size(); i++) {
            if (scoutEntries.get(i).getPreMatch().getMatchNum() == matchNum && scoutEntries.get(i).getPreMatch()
                    .getScoutPos().contains("red")) {
                partnerTeams[numberFound] = scoutEntries.get(i);
                numberFound++;
                if (numberFound == 2) {
                    return partnerTeams;
                }
            }
        }
        return partnerTeams;
    }

}
