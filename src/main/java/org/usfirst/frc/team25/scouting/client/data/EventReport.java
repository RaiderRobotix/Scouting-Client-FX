package org.usfirst.frc.team25.scouting.client.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thebluealliance.api.v3.models.Match;
import com.thebluealliance.api.v3.models.MatchScoreBreakdown2018Alliance;
import org.usfirst.frc.team25.scouting.client.models.*;

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
    private HashMap<Integer, Integer> pickPoints;

    public EventReport(ArrayList<ScoutEntry> entries, String event, File directory) {
        teamReports = new HashMap<>();
        inaccuracyList = "";

        scoutEntries = entries;
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

    public void fixInaccuraciesTBA() {

        try {
            BlueAlliance.downloadEventMatchData(event, directory);
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {

            ArrayList<Match> matchData = FileManager.deserializeScoreBreakdown(
                    new File(directory.getAbsoluteFile() + "\\ScoreBreakdown - " + event + ".json"));


            for (ScoutEntry entry : scoutEntries) {
                try {
                    String prefix =
                            "Q" + entry.getPreMatch().getMatchNum() + "-" + entry.getPreMatch().getScoutPos() + "-" +
                                    entry.getPreMatch().getScoutName() + ": ";
                    String inaccuracies = "";
                    Match match = matchData.get(entry.getPreMatch().getMatchNum() - 1);
                    MatchScoreBreakdown2018Alliance sb;
                    boolean correctTeamRed = entry.getPreMatch().getScoutPos().contains("Red") && match.getRedAlliance()
                            .getTeamKeys()[Integer.parseInt(entry.getPreMatch().getScoutPos().split(" ")[1]) - 1].equals("frc" + entry.getPreMatch().getTeamNum());
                    boolean correctTeamBlue =
                            entry.getPreMatch().getScoutPos().contains("Blue") && match.getBlueAlliance()
                                    .getTeamKeys()[Integer.parseInt(entry.getPreMatch().getScoutPos().split(" ")[1]) - 1].equals("frc" + entry.getPreMatch().getTeamNum());
                    if (correctTeamBlue || correctTeamRed) {

                        if (entry.getPreMatch().getScoutPos().contains("Red")) {
                            sb = match.getScoreBreakdown().getRed();
                        } else {
                            sb = match.getScoreBreakdown().getBlue();
                        }


                        boolean actualAutoRun = false;
                        boolean actualClimb = false;
                        boolean actualLevitate = false;
                        boolean actualPark = false;
                        boolean partnersClimb = false;


                        if (entry.getPreMatch().getScoutPos().contains("1")) {
                            actualAutoRun = sb.getAutoRobot1().equals("AutoRun");
                            actualClimb = sb.getEndgameRobot1().equals("Climbing");
                            actualLevitate = sb.getEndgameRobot1().equals("Levitate");
                            actualPark = sb.getEndgameRobot1().equals("Parking");
                            partnersClimb = sb.getEndgameRobot2().equals("Climbing") && sb.getEndgameRobot3().equals(
                                    "Climbing");
                        } else if (entry.getPreMatch().getScoutPos().contains("2")) {
                            actualAutoRun = sb.getAutoRobot2().equals("AutoRun");
                            actualClimb = sb.getEndgameRobot2().equals("Climbing");
                            actualLevitate = sb.getEndgameRobot2().equals("Levitate");
                            actualPark = sb.getEndgameRobot2().equals("Parking");
                            partnersClimb = sb.getEndgameRobot1().equals("Climbing") && sb.getEndgameRobot3().equals(
                                    "Climbing");
                        } else if (entry.getPreMatch().getScoutPos().contains("3")) {
                            actualAutoRun = sb.getAutoRobot3().equals("AutoRun");
                            actualClimb = sb.getEndgameRobot3().equals("Climbing");
                            actualLevitate = sb.getEndgameRobot3().equals("Levitate");
                            actualPark = sb.getEndgameRobot3().equals("Parking");
                            partnersClimb = sb.getEndgameRobot2().equals("Climbing") && sb.getEndgameRobot1().equals(
                                    "Climbing");
                        }

                        if (actualAutoRun != entry.getAuto().isReachHabLine()) {
                            inaccuracies += "auto run, ";
                            entry.getAuto().setReachHabLine(actualAutoRun);
                        }


                        if (actualLevitate && partnersClimb && !entry.getPostMatch().robotQuickCommentSelections.get(
                                "Climb/park unneeded (levitate used and others climbed)")) {
                            entry.getPostMatch().robotQuickCommentSelections.put("Climb/park unneeded (levitate used " +
                                    "and others climbed)", true);
                            inaccuracies += "climb/park unneeded, ";
                        }


                        if (!inaccuracies.isEmpty()) {
                            inaccuracyList += prefix + inaccuracies + "\n";
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) {

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isTeamPlaying(int teamNum) {
        for (int i : teamReports.keySet()) {
            if (teamNum == i) {
                return true;
            }
        }
        return false;
    }


    // TODO Update this
    public String quickTeamReport(int teamNum) {
        StringBuilder formatString = new StringBuilder("<html>");
        TeamReport report = teamReports.get(teamNum);

        formatString.append("<h2>Team ").append(teamNum);
        if (report.getTeamName() != null) {
            formatString.append(" - ").append(report.getTeamName());
        }
        formatString.append("</h2><h3>Auto</h3>");


        //        formatString.append("Cross baseline: ").append(Statistics.round(report.autoRunPercentage, 2))
        //        .append("% (").append(report.totalReachBaseline).append("/").append(report.entries.size()).append
        //        (")").append("<br>");
        formatString.append("<h3>Tele-Op</h3>");

        //        formatString.append("Avg. gears: ").append(Statistics.round(report.avgTeleOpGears, 2)).append("<br>");
        //        formatString.append("Gear counts: ");
        //


        formatString.append("</html>");
        return formatString.toString();

    }

    //TODO update this
    public String allianceReport(int t1, int t2, int t3) {
        String formatString = "<html>";
        TeamReport r1 = teamReports.get(t1), r2 = teamReports.get(t2), r3 = teamReports.get(t3);

        Alliance a = new Alliance(r1, r2, r3);
        a.calculateStats();

        formatString += "<h2>" + t1 + ", " + t2 + ", " + t3 + "</h2><h3>Auto</h3>";

        //        formatString += "1+ BL cross: "
        //                + Statistics.round(a.atLeastOneBaselinePercent, 2)
        //                + "%<br>";
        //        formatString += "2+ BL cross: "
        //                + Statistics.round(a.atLeastTwoBaselinePercent, 2)
        //                + "%<br>";
        //        formatString += "3 BL cross: "
        //                + Statistics.round(a.allBaselinePercent, 2)
        //                + "%<br>";
        //        formatString += "Place gear: "
        //                + Statistics.round(a.autoGearPercent, 2)
        //                + "%<br>";
        //        formatString += "Avg. kPa: " + Statistics.round(a.autoKpa, 2) + "<br>";

        formatString += "<h3>Tele-Op</h3>";
        //        formatString += "Avg. kPa: " + Statistics.round(a.teleopKpa, 2) + "<br>";
        //        formatString += "1+ takeoff: "
        //                + Statistics.round(a.atLeastOneTakeoffPercent, 2)
        //                + "%<br>";
        //        formatString += "2+ takeoff: "
        //                + Statistics.round(a.atLeastTwoTakeoffPercent, 2)
        //                + "%<br>";
        //        formatString += "3 takeoff: "
        //                + Statistics.round(a.allTakeoffPercent, 2)
        //                + "%<br>";
        //        formatString += "<h3>Overall</h3>";
        //        formatString += "Total gears: " + Statistics.round(a.totalGears, 2) + "<br>";
        //        formatString += "Total kPa: " + Statistics.round(a.totalKpa, 2) + "<br>";
        //        formatString += "Avg. score (predicted): " + Statistics.round(a.predictedScore, 2) + "<br>";

        formatString += "</html>";
        return formatString;

    }

    public void processTeamReports() {

        for (Integer key : teamReports.keySet()) {

            TeamReport report = teamReports.get(key);
            if (teamNameList != null) {
                report.autoGetTeamName(teamNameList);

            }
            report.calculateStats();


            teamReports.put(key, report);

        }


    }

    public void setTeamNameList(File list) {
        teamNameList = list;
    }


    /**
     * Generates summary and team Excel spreadsheets
     *
     * @param outputDirectory Output directory for generated fields
     */
    public void generateRawSpreadsheet(File outputDirectory) {
        final String COMMA = ",";
        //TODO programatically generate this
        StringBuilder header = new StringBuilder("Scout Name,Match Num,Scouting Pos,Team Num,Starting Pos,Field Layout,"
                + "Near Switch Auto,Far Switch Auto,Near Scale Auto,Far Scale Auto,Center Switch Auto,Center Scale " +
                "Auto,"
                + "Auto Switch Cubes,Auto Scale Cubes,Auto Exchange Cubes,Auto PCP Pickup,"
                + "Auto Switch Adj Pickup,"
                + "Auto Cubes Dropped,Auto Line Cross,Auto Null Territory Foul,"
                + "Auto Drop Opponent Switch,Auto Drop Opponent Scale,"
                + "Tele First Cube Time,Tele Own Switch Cubes,Tele Scale Cubes,"
                + "Tele Opponent Switch Cubes,Tele Exchange Cubes,Tele Cubes Dropped,"
                + "Climbs Assisted,Parked,Attempt Rung Climb,Success Rung Climb,"
                + "Climb on Other Robot,Other Robot Climb Type,"
                + "Focus,Robot Comment,Robot Quick Comment Str,Pick Points,");

        ArrayList<String> keys = new ArrayList<>();


        for (String key : scoutEntries.get(0).getPostMatch().getRobotQuickCommentSelections().keySet()) {
            header.append(removeCommas(key)).append(",");
            keys.add(key);
        }


        StringBuilder fileContents = new StringBuilder(header + "\n");
        for (ScoutEntry entry : scoutEntries) {
            PreMatch pre = entry.getPreMatch();
            Autonomous auto = entry.getAuto();
            TeleOp tele = entry.getTeleOp();
            PostMatch post = entry.getPostMatch();

            Object[] dataObjects = {entry.getPreMatch(), entry.getAuto(), entry.getTeleOp(), entry.getPostMatch()};


            for (Object dataObject : dataObjects) {
                // returns all members including private members but not inherited members.
                Field[] fields = dataObject.getClass().getDeclaredFields();

                for (Field metric : fields) {
                    Object metricValue = "";
                    for (Method m : TeleOp.class.getMethods()) {
                        if (m.getName().contains(metric.getName()) && m.getParameterTypes().length == 0) {
                            try {
                                metricValue = m.invoke(dataObject);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                    fileContents.append(metricValue).append(",");
                }

            }

            for (String key : keys) {
                fileContents.append(post.getRobotQuickCommentSelections().get(key)).append(COMMA);
            }


            fileContents.append('\n');
        }


        try {
            FileManager.outputFile(outputDirectory.getAbsolutePath() + "\\Data - All - " + event, "csv",
                    fileContents.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to prevent manual comments with commas
     * from changing CSV format
     *
     * @param s String to be processed
     * @return String without commas
     */
    private String removeCommas(String s) {
        StringBuilder newString = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ',') {
                newString.append(s.charAt(i));
            } else {
                newString.append("; ");
            }
        }
        return newString.toString();
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
            FileManager.outputFile(outputDirectory.getAbsolutePath() + "\\Data - All - " + event, "json", jsonString);
        } catch (FileNotFoundException e) {

            return false;
        }
        return true;
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
            FileManager.outputFile(outputDirectory.getAbsolutePath() + "\\TeamReports - " + event, "json", jsonString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void generateInaccuracyList(File outputDirectory) {
        if (!inaccuracyList.isEmpty()) {
            FileManager.outputFile(new File(outputDirectory.getAbsolutePath() + "\\inaccuracies.txt"), inaccuracyList);
        }
    }

    public void generatePicklists(File outputDirectory) {
        PicklistGenerator pg = new PicklistGenerator(scoutEntries, outputDirectory, event);
        pg.generateBogoCompareList();
        pg.generateComparePointList();
        pg.generatePickPointList();
    }

    public void generateMatchPredictions(File outputDirectory) {

    }

    public TeamReport getTeamReport(int teamNum) {
        return teamReports.get(teamNum);
    }

}
