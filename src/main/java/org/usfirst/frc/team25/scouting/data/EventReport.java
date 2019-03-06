package org.usfirst.frc.team25.scouting.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.usfirst.frc.team25.scouting.data.models.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

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
    private final HashMap<Integer, TeamReport> teamReports;
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

        this.scoutEntries = entries;
        this.event = event;
        this.directory = directory;

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
            FileManager.outputFile(outputDirectory, "Data - All - " + event, "csv", fileContents.toString());
            FileManager.outputFile(outputDirectory, "Data - No Show Removed - " + event, "csv",
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
                    header.append(shortNames[i]).append(" - ");
                }
                header.append(StringProcessing.convertCamelToSentenceCase(metric.getName())).append(",");
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
            FileManager.outputFile(outputDirectory, "Data - All - " + event, "json", jsonString);
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
            FileManager.outputFile(outputDirectory, "TeamReports - " + event, "json", jsonString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void generatePicklists(File outputDirectory) {
        PicklistGenerator pg = new PicklistGenerator(scoutEntries, teamReports, outputDirectory, event);
        pg.generateComparePointList();
        pg.generatePickPointList();

        ArrayList<TeamReport> knownAlliancePartners = new ArrayList<>();
        if (teamReports.containsKey(25)) {
            knownAlliancePartners.add(teamReports.get(25));
        }

        pg.generateCalculatedFirstPicklist(knownAlliancePartners);
    }

    public boolean generateMatchPredictions(File outputDirectory) {

        int greatestMatchNum = 0;
        for (ScoutEntry entry : scoutEntries) {
            if (entry.getPreMatch().getMatchNum() > greatestMatchNum) {
                greatestMatchNum = entry.getPreMatch().getMatchNum();
            }
        }

        try {
            StringBuilder predictions = new StringBuilder();

            File matchList = FileManager.getMatchList(directory);

            String[] matches = FileManager.getFileString(matchList).split("\n");

            for (int i = greatestMatchNum + 1; i < matches.length - 1; i++) {
                AllianceReport[] allianceReports = getAlliancesInMatch(i);
                predictions.append("Match ").append(i).append(": ");
                for (int j = 0; j < allianceReports.length; j++) {
                    predictions.append(allianceReports[j].getTeamReports()[0].getTeamNum()).append("-");
                    predictions.append(allianceReports[j].getTeamReports()[1].getTeamNum()).append("-");
                    predictions.append(allianceReports[j].getTeamReports()[2].getTeamNum()).append(" (");
                    predictions.append(Stats.round(allianceReports[j].calculatePredictedRp(allianceReports[Math.abs(j - 1)]), 1)).append(" RP, ");
                    predictions.append(Stats.round(allianceReports[j].getPredictedValue("totalPoints"), 1)).append(" " +
                            "pts)");
                    if (j == 0) {
                        predictions.append(" vs. ");
                    }
                }
                double redWinChance = allianceReports[0].calculateWinChance(allianceReports[1]);
                if (redWinChance > 0.5) {
                    predictions.append(" - Red win, ").append(Stats.round(redWinChance * 100, 2)).append("%\n");
                } else {
                    predictions.append(" - Blue win, ").append(Stats.round((1 - redWinChance) * 100, 2)).append("%\n");
                }

            }

            if (predictions.length() > 0) {
                FileManager.outputFile(outputDirectory, "MatchPredictions", "txt", predictions.toString());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
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
        ArrayList<TeamReport> teamReports = new ArrayList<>();
        for (int teamNum : teamNums) {
            if (getTeamReport(teamNum) != null) {
                teamReports.add(getTeamReport(teamNum));
            }
        }

        return new AllianceReport(teamReports);
    }

    public ArrayList<ScoutEntry> getScoutEntries() {
        return scoutEntries;
    }

    public String getEvent() {
        return event;
    }

    public File getDirectory() {
        return directory;
    }


}
