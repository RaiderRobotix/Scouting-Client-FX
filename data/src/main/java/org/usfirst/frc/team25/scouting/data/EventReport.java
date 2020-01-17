package org.usfirst.frc.team25.scouting.data;

import com.google.gson.Gson;
import org.usfirst.frc.team25.scouting.data.models.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Object model holding all data for an event. Responsible for generating event-wide files
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

    /**
     * Calculates derived stats of each scout entry, populating team reports, and processing team reports.
     * Should be called upon populating the event report with all scout entries.
     */
    public void processEntries() {

        for (ScoutEntry entry : scoutEntries) {
            entry.calculateDerivedStats();

            int teamNum = entry.preMatch().teamNum();

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
     * Generates a spreadsheet of all values from scout entries, with one row per entry.
     * Columns are metric names, and cells contain individual values.
     * Also creates a spreadsheet with "no show" entries removed.
     *
     * @param outputDirectory Output directory for generated fields
     * @return True if the method was successfully executed, false otherwise
     */
    public boolean generateRawSpreadsheet(File outputDirectory) {

        StringBuilder fileContents = new StringBuilder(generateSpreadsheetHeader() + "\n");
        StringBuilder noShowFileContents = new StringBuilder(fileContents);

        // Create rows of entries inside this loop
        for (ScoutEntry entry : scoutEntries) {

            StringBuilder entryContents = new StringBuilder();

            Object[] dataObjects = {entry.preMatch(), entry, entry.autonomous(), entry.teleOp(),
                    entry.postMatch()};

            // Populates each "block" of values based on the section of the match
            for (Object dataObject : dataObjects) {
                // Returns all members, including private members, but not inherited members
                // This fetches the metric names from the fields of the four main data models
                Field[] fields = dataObject.getClass().getDeclaredFields();

                for (Field metric : fields) {
                    Object metricValue = "";

                    // Index to account for the substring shift from "is" or "get"
                    // The correct getter method is later found with this value
                    int shiftIndex = 3;

                    if (metric.getType() == boolean.class) {
                        shiftIndex = 2;
                    }

                    // Only the primitives are added to the output entry for now
                    // Values from HashMaps for quick comments are added later
                    if (metric.getType() != boolean.class && metric.getType() != int.class && metric.getType() != String.class) {
                        continue;
                    }

                    try {
                        // Retrieves the correct getter for the variable, then retrieves its value from the data object
                        metricValue =
                                SortersFilters.getCorrectGetter(dataObject.getClass(), metric.getName(), shiftIndex).invoke(dataObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    entryContents.append(metricValue).append(",");
                }

            }

            // Adds the true or false values for the robot quick comments
            for (String key : scoutEntries.get(0).postMatch().robotQuickCommentSelections().keySet()) {
                entryContents.append(entry.postMatch().robotQuickCommentSelections().get(key)).append(",");
            }

            entryContents.append('\n');
            fileContents.append(entryContents);

            if (!entry.preMatch().robotNoShow()) {
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
     * Automatically generates the spreadsheet header based on object model field variables
     *
     * @return The spreadsheet header as a String
     */
    private String generateSpreadsheetHeader() {
        StringBuilder header = new StringBuilder();

        // Prefixes to append to sandstorm and tele-op sections, as metrics like "rocketCargo" may be the same across
        // data models
        String[] shortNames = {"Pre", "Overall", "Auto", "Tele", "Post"};
        Class[] dataModels = {PreMatch.class, ScoutEntry.class, Autonomous.class, TeleOp.class, PostMatch.class};

        for (int i = 0; i < shortNames.length; i++) {
            Field[] fields = dataModels[i].getDeclaredFields();

            for (Field metric : fields) {
                if (metric.getType() != int.class && metric.getType() != boolean.class && metric.getType() != String.class) {
                    continue;
                }

                // Only add in prefixes to sandstorm and tele-op
                if (i == 2 || i == 3) {
                    header.append(shortNames[i]).append(" - ");
                }

                // Adds the "title case" version of the metric name, rather than the camel case version
                header.append(StringProcessing.convertCamelToSentenceCase(metric.getName())).append(",");
            }
        }

        // Generates the quick comment portion of the header
        for (String key : scoutEntries.get(0).postMatch().robotQuickCommentSelections().keySet()) {
            header.append(StringProcessing.removeCommasBreaks(key)).append(",");
        }

        return header.toString();
    }

    /**
     * Serializes the ArrayList of all scout entries into a JSON file
     *
     * @param outputDirectory The directory to write the combined JSON file to
     * @return True if operation is successful, false otherwise
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

    /**
     * Creates a compare points list, a pick points list, and a predicted point contribution list for the teams at
     * the current event
     *
     * @param outputDirectory Directory where the picklists are saved to
     * @param knownPartners   Array of team numbers that are already in the playoff alliance
     */
    public void generatePicklists(File outputDirectory, int[] knownPartners) {
        PicklistGenerator pg = new PicklistGenerator(scoutEntries, teamReports, outputDirectory, event);
        pg.generateComparePointList();
        pg.generatePickPointList();

        ArrayList<TeamReport> knownPartnersArray = new ArrayList<>();

        for (int team : knownPartners) {
            knownPartnersArray.add(teamReports.get(team));
        }

        pg.generateCalculatedPickAbilityList(knownPartnersArray);
    }

    /**
     * Creates a text file of future predicted match scores and ranking points, the teams playing on each alliance in
     * the match, the predicted winner of the match, and the confidence in that prediction.
     * Match schedule must be downloaded for this method to work.
     *
     * @param outputDirectory Directory to save the prediction text file
     * @return True if the file was created successfully, false otherwise
     */
    public boolean generateMatchPredictions(File outputDirectory) {

        // Finds the number of the next qualification match
        int greatestMatchNum = 0;
        for (ScoutEntry entry : scoutEntries) {
            if (entry.preMatch().matchNum() > greatestMatchNum) {
                greatestMatchNum = entry.preMatch().matchNum();
            }
        }

        try {
            StringBuilder predictions = new StringBuilder();
            File matchList = FileManager.getMatchList(directory);

            String[] matches = FileManager.getFileString(matchList).split("\n");

            for (int i = greatestMatchNum + 1; i < matches.length + 1; i++) {
                AllianceReport[] allianceReports = getAlliancesInMatch(i);
                predictions.append("Match ").append(i).append(": ");

                // Generates values for each alliance
                for (int j = 0; j < allianceReports.length; j++) {
                    predictions.append(allianceReports[j].getTeamReports()[0].getTeamNum()).append("-");
                    predictions.append(allianceReports[j].getTeamReports()[1].getTeamNum()).append("-");
                    predictions.append(allianceReports[j].getTeamReports()[2].getTeamNum()).append(" (");

                    // THe Math.abs() hack essentially makes the opposing alliance the other value in allianceReports
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

    /**
     * Gets a two-element array of alliance reports representing the alliances playing in the specified match
     *
     * @param matchNum Number of the qualification match to be queried
     * @return An array of alliance reports
     * @throws FileNotFoundException If the match schedule is not downloaded or scouting data cannot be found
     */
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
                            // Multiply by i here to represent the two alliances
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

    /**
     * Creates an alliance report from the specified team numbers
     *
     * @param teamNums Array of integers representing the team numbers on a particular alliance
     * @return Alliance report object of the specified teams
     */
    public AllianceReport getAllianceReport(int[] teamNums) {
        ArrayList<TeamReport> teamReports = new ArrayList<>();
        for (int teamNum : teamNums) {
            if (getTeamReport(teamNum) != null) {
                teamReports.add(getTeamReport(teamNum));
            }
        }

        return new AllianceReport(teamReports);
    }

    public TeamReport getTeamReport(int teamNum) {
        return teamReports.get(teamNum);
    }

    /**
     * Sets the location of the CSV file containing team name and number pairings
     *
     * @param list File object representing the team name list file
     */
    public void setTeamNameList(File list) {
        this.teamNameList = list;
    }

    /**
     * Determines if the specified team is playing at the event
     *
     * @param teamNum Team number to check
     * @return True if the team is playing at the event, false otherwise
     */
    public boolean isTeamPlaying(int teamNum) {
        for (int i : teamReports.keySet()) {
            if (teamNum == i) {
                return true;
            }
        }
        return false;
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
