package org.usfirst.frc.team25.scouting.data;

import org.usfirst.frc.team25.scouting.data.models.Autonomous;
import org.usfirst.frc.team25.scouting.data.models.ScoutEntry;
import org.usfirst.frc.team25.scouting.data.models.TeleOp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Object model containing individual reports of teams in events and methods to process data
 */
public class TeamReport {

    private transient ArrayList<ScoutEntry> entries;
    private final int teamNum;

    public static final String[] autoMetricNames = new String[]{"cargoShipHatches", "rocketHatches", "cargoShipCargo",
            "rocketCargo"};
    public static final String[] teleMetricNames = new String[]{"cargoShipHatches", "rocketLevelOneHatches",
            "rocketLevelTwoHatches", "rocketLevelThreeHatches", "cargoShipCargo", "rocketLevelOneCargo",
            "rocketLevelTwoCargo", "rocketLevelThreeCargo", "numPartnerClimbAssists"};
    public static String[] levelPrefixes = new String[]{"levelOne", "levelTwo", "levelThree", "total"};
    public static final String[] overallMetricNames = new String[]{"calculatedPointContribution",
            "calculatedSandstormPoints",
            "calculatedTeleOpPoints"};
    private String teamName, frequentRobotCommentStr, allComments;
    private HashMap<String, Double> averages, standardDeviations, attemptSuccessRates;
    private HashMap<String, Integer> counts;
    private HashMap<String, Boolean> abilities;
    private ArrayList<String> frequentComments;

    /**
     * Constructs an empty TeamReport based on the metrics calculated in the model team report
     * Used to simulate additional alliance members
     *
     * @param model A team report with calculations performed on a real set of scouting entries
     */
    public TeamReport(TeamReport model) {
        averages = new HashMap<>();
        for (String key : model.getAverages().keySet()) {
            averages.put(key, 0.0);
        }

        standardDeviations = new HashMap<>();
        for (String key : model.getStandardDeviations().keySet()) {
            standardDeviations.put(key, 0.0);
        }

        attemptSuccessRates = new HashMap<>();
        for (String key : model.getAttemptSuccessRates().keySet()) {
            attemptSuccessRates.put(key, 0.0);
        }

        counts = new HashMap<>();
        for (String key : model.getCounts().keySet()) {
            counts.put(key, 0);
        }

        abilities = new HashMap<>();
        for (String key : model.getAbilities().keySet()) {
            abilities.put(key, false);
        }
        teamNum = 0;
        teamName = "";
    }


    /**
     * Constructs a team report
     *
     * @param teamNum Team number of the team that is being reported
     */
    public TeamReport(int teamNum) {
        this.teamNum = teamNum;
        entries = new ArrayList<>();
        teamName = "";
        frequentRobotCommentStr = "";

        averages = new HashMap<>();
        standardDeviations = new HashMap<>();
        counts = new HashMap<>();
        attemptSuccessRates = new HashMap<>();
        abilities = new HashMap<>();
        frequentComments = new ArrayList<>();

    }

    /**
     * Generates an easily-readable report with relevant stats on an team's capability
     *
     * @return A formatted string with relevant aggregate team stats
     */
    public String getQuickStatus() {

        String statusString = "Team " + getTeamNum();

        if (!getTeamName().isEmpty()) {
            statusString += " - " + getTeamName();
        }

        statusString += "\n\nSandstorm:";

        for (String metric : autoMetricNames) {
            statusString += "\nAvg. " + StringProcessing.convertCamelToSentenceCase(metric) + ": " + Stats.round
                    (averages.get("auto" + metric), 2);
        }

        statusString += "\nHAB line cross: " + Stats.round(attemptSuccessRates.get("totalCross") * 100, 2) + "% ("
                + counts.get("totalCross") + "/" + entries.size() + ")";

        for (int i = 0; i < 2; i++) {
            statusString += "\nHAB lvl " + (i + 1) + " cross: ";
            statusString += Stats.round(attemptSuccessRates.get(levelPrefixes[i] + "Cross") * 100, 2) + "% " +
                    "(" + counts.get(levelPrefixes[i] + "Cross") + "/" + counts.get(levelPrefixes[i] + "Start") + ")";
        }

        statusString += "\n\nTele-Op:";

        for (String metric : teleMetricNames) {
            statusString += "\nAvg. " + StringProcessing.convertCamelToSentenceCase(metric) + ": " + Stats.round
                    (averages.get("tele" + metric), 2);
        }

        statusString += "\n\nEndgame:";

        for (int i = 0; i < 4; i++) {

            if (i == 3) {
                statusString += "\nTotal climb success: ";
            } else {
                statusString += "\nLvl " + (i + 1) + " climb success: ";
            }
            statusString += Stats.round(attemptSuccessRates.get(levelPrefixes[i] + "Climb") * 100, 0) + "% " +
                    "(" + counts.get(levelPrefixes[i] + "ClimbSuccess") + "/" + counts.get(levelPrefixes[i] +
                    "ClimbAttempt") + ")";

        }

        statusString += "\n\nOverall:";

        for (String metric : overallMetricNames) {
            statusString += "\nAvg. " + StringProcessing.convertCamelToSentenceCase(metric) + ": " + Stats.round
                    (averages.get(metric), 2);
        }

        if (!frequentRobotCommentStr.isEmpty()) {
            statusString += "\n\nCommon quick comments:\n" + frequentRobotCommentStr;
        }
        if (!allComments.isEmpty()) {
            statusString += "\nAll comments:\n" + allComments;
        }

        return statusString;

    }

    public String getTeamName() {
        return teamName;
    }

    /**
     * Adds entries to the scouting entry list of this team
     *
     * @param entry <code>ScoutEntry</code> to be added to tis team report
     */
    public void addEntry(ScoutEntry entry) {
        entry.getPostMatch().setRobotComment(StringProcessing.removeCommasBreaks(entry.getPostMatch().getRobotComment
                ()));

        entries.add(entry);
    }

    public ArrayList<ScoutEntry> getEntries() {
        return this.entries;
    }

    public int getTeamNum() {
        return teamNum;
    }

    /**
     * Method to fetch the nickname of a team from a file
     *
     * @param dataLocation location of the TeamNameList file generated by <code>exportTeamList</code>
     */
    public void autoGetTeamName(File dataLocation) {
        String data = FileManager.getFileString(dataLocation);
        String[] values = data.split(",\n");

        for (String value : values) {

            if (value.split(",")[0].equals(Integer.toString(teamNum))) {

                teamName = value.split(",")[1];
                return;
            }
        }
    }

    /**
     * Processes the scout entries within the team report by filtering out no shows, calculating stats, and finding
     * abilities
     */
    public void processReport() {
        filterNoShow();
        findFrequentComments();
        calculateStats();
        findAbilities();
    }

    /**
     * Removes scouting entries where the robot did not show up and increments the "no show" count
     */
    public void filterNoShow() {
        counts.put("noShow", 0);
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getPreMatch().isRobotNoShow()) {
                entries.remove(i);
                i--;
                incrementCount("noShow");
            }
        }
    }

    private void incrementCount(String metricName) {
        if (counts.containsKey(metricName)) {
            counts.put(metricName, counts.get(metricName) + 1);
        } else {
            counts.put(metricName, 1);
        }
    }

    /**
     * Calculates the counts, averages, standard deviations, and attempt-success rates of data in stored scouting
     * entries, provided that data exists
     */
    public void calculateStats() {

        if (entries.size() > 0) {
            calculateCounts();
            calculateAverages();
            calculateStandardDeviations();
            calculateAttemptSuccessRates();
        }

    }

    /**
     * Calculates the attempt-success rates of HAB climb, sandstorm bonus, and/or sandstorm game piece placing for a
     * team
     */
    private void calculateAttemptSuccessRates() {

        for (int i = 0; i < 4; i++) {
            if (i != 2) {
                double crossRate = 0.0;

                int attempts = 0;

                if (i == 3 && entries.size() != 0) {
                    attempts = entries.size();
                    crossRate = (double) counts.get(levelPrefixes[i] + "Cross") / attempts;
                } else if (counts.get(levelPrefixes[i] + "Start") != 0) {
                    attempts = counts.get(levelPrefixes[i] + "Start");
                    crossRate = (double) counts.get(levelPrefixes[i] + "Cross") / attempts;
                }

                standardDeviations.put(levelPrefixes[i] + "Cross", Stats.standardDeviation(attempts,
                        counts.get(levelPrefixes[i] + "Cross")));

                attemptSuccessRates.put(levelPrefixes[i] + "Cross", crossRate);
            }

            double climbRate = 0.0;

            if (counts.get(levelPrefixes[i] + "ClimbAttempt") != 0) {
                climbRate = (double) counts.get(levelPrefixes[i] + "ClimbSuccess") / counts.get(levelPrefixes[i] +
                        "ClimbAttempt");
            }

            standardDeviations.put(levelPrefixes[i] + "Climb", Stats.standardDeviation(counts.get(levelPrefixes[i] +
                    "ClimbAttempt"), counts.get(levelPrefixes[i] + "ClimbSuccess")));
            attemptSuccessRates.put(levelPrefixes[i] + "Climb", climbRate);
        }

        for (String prefix : new String[]{"cargo", "hatch"}) {

            double placeRate = 0.0;

            if (counts.get(prefix + "Start") != 0) {
                placeRate = (double) counts.get(prefix + "AutoSuccess") / counts.get(prefix + "Start");
            }

            standardDeviations.put(prefix + "AutoSuccess", Stats.standardDeviation(counts.get(prefix + "Start"),
                    counts.get(prefix + "AutoSuccess")));
            attemptSuccessRates.put(prefix + "AutoSuccess", placeRate);
        }
    }

    /**
     * Calculates the number of times a team starts/crosses a particular level of the HAB and climb attempts/successes
     */
    private void calculateCounts() {
        final String[] levelMetricSuffixes = new String[]{"Start", "Cross", "ClimbAttempt", "ClimbSuccess"};

        for (String prefix : levelPrefixes) {
            for (String suffix : levelMetricSuffixes) {
                counts.put(prefix + suffix, 0);
            }
        }

        for (String prefix : new String[]{"cargo", "hatch"}) {
            for (String suffix : new String[]{"Start", "AutoSuccess"}) {
                counts.put(prefix + suffix, 0);
            }
        }

        for (ScoutEntry entry : entries) {

            incrementCount(levelPrefixes[entry.getPreMatch().getStartingLevel() - 1] + "Start");

            if (entry.getPreMatch().getStartingGamePiece().equals("Cargo")) {
                incrementCount("cargoStart");
                if (entry.getSandstormCargo() >= 1) {
                    incrementCount("cargoAutoSuccess");
                }
            }

            if (entry.getPreMatch().getStartingGamePiece().equals("Hatch panel")) {
                incrementCount("hatchStart");
                if (entry.getSandstormHatches() >= 1) {
                    incrementCount("hatchAutoSuccess");
                }
            }

            if (entry.getAutonomous().isCrossHabLine()) {
                if (entry.getPreMatch().getStartingLevel() == 2) {
                    incrementCount("levelOneCross");
                    incrementCount("levelOneStart");
                }
                incrementCount(levelPrefixes[entry.getPreMatch().getStartingLevel() - 1] + "Cross");
                incrementCount("totalCross");
            }

            if (entry.getTeleOp().isAttemptHabClimb()) {
                incrementCount(levelPrefixes[entry.getTeleOp().getAttemptHabClimbLevel() - 1] +
                        "ClimbAttempt");

                incrementCount("totalClimbAttempt");
            }

            if (entry.getTeleOp().isSuccessHabClimb()) {
                incrementCount(levelPrefixes[entry.getTeleOp().getSuccessHabClimbLevel() - 1] +
                        "ClimbSuccess");

                if (entry.getTeleOp().getSuccessHabClimbLevel() != entry.getTeleOp().getAttemptHabClimbLevel()) {
                    incrementCount(levelPrefixes[entry.getTeleOp().getSuccessHabClimbLevel() - 1] +
                            "ClimbAttempt");
                }

                incrementCount("totalClimbSuccess");
            }


        }
    }

    public void findFrequentComments() {

        HashMap<String, Integer> commentFrequencies = new HashMap<>();
        if (entries.size() > 0) {
            for (String key : entries.get(0).getPostMatch().getRobotQuickCommentSelections().keySet()) {
                commentFrequencies.put(key, 0);
                for (ScoutEntry entry : entries) {
                    if (entry.getPostMatch().getRobotQuickCommentSelections().get(key)) {
                        commentFrequencies.put(key, 1 + commentFrequencies.get(key));
                    }
                }
            }
        }


        for (String key : commentFrequencies.keySet()) {

            // Feel free to change this ratio
            if (commentFrequencies.get(key) >= entries.size() / 4.0) {
                frequentComments.add(key);
            }
        }

        for (String comment : frequentComments) {

            frequentRobotCommentStr += StringProcessing.removeCommasBreaks(comment) + " \n";
        }

        allComments = "";
        for (ScoutEntry entry : entries) {
            if (!entry.getPostMatch().getRobotComment().equals("")) {
                allComments += entry.getPostMatch().getRobotComment() + "; ";
            }

        }

    }

    /**
     * Calculates the averages of metrics specified in <code>autoMetricNames</code> and  <code>teleMetricNames</code>
     */
    private void calculateAverages() {
        ArrayList<Object> autoList = SortersFilters.filterDataObject(entries, Autonomous.class);
        ArrayList<Object> teleList = SortersFilters.filterDataObject(entries, TeleOp.class);
        ArrayList<Object> overallList = new ArrayList<>(entries);

        for (String metric : autoMetricNames) {
            double[] values = Stats.getDoubleArray(autoList, metric, int.class);
            averages.put("auto" + metric, Stats.mean(values));
        }
        for (String metric : teleMetricNames) {
            double[] values = Stats.getDoubleArray(teleList, metric, int.class);
            averages.put("tele" + metric, Stats.mean(values));
        }
        for (String metric : overallMetricNames) {
            double[] values = Stats.getDoubleArray(overallList, metric, int.class);
            averages.put(metric, Stats.mean(values));
        }


    }

    /**
     * Calculates the sample standard of metrics specified in <code>autoMetricNames</code> and
     * <code>teleMetricNames</code>
     */
    private void calculateStandardDeviations() {
        ArrayList<Object> autoList = SortersFilters.filterDataObject(entries, Autonomous.class);
        ArrayList<Object> teleList = SortersFilters.filterDataObject(entries, TeleOp.class);
        ArrayList<Object> overallList = new ArrayList<>(entries);

        for (String metric : autoMetricNames) {
            double[] values = Stats.getDoubleArray(autoList, metric, int.class);
            standardDeviations.put("auto" + metric, Stats.standardDeviation(values));
        }
        for (String metric : teleMetricNames) {
            double[] values = Stats.getDoubleArray(teleList, metric, int.class);
            standardDeviations.put("tele" + metric, Stats.standardDeviation(values));
        }
        for (String metric : overallMetricNames) {
            double[] values = Stats.getDoubleArray(overallList, metric, int.class);
            standardDeviations.put(metric, Stats.standardDeviation(values));
        }


    }

    public HashMap<String, Boolean> getAbilities() {
        return abilities;
    }

    /**
     * Determines if teams are capable of intaking game pieces from the floor and their potential sandstorm modes
     */
    private void findAbilities() {
        abilities.put("cargoFloorIntake", frequentComments.contains("Cargo floor intake"));
        abilities.put("hatchPanelFloorIntake", frequentComments.contains("Hatch panel floor intake"));

        abilities.put("frontCargoShipHatchSandstorm", false);
        abilities.put("sideCargoShipHatchSandstorm", false);
        abilities.put("rocketHatchSandstorm", false);
        abilities.put("cargoShipCargoSandstorm", false);
        abilities.put("rocketCargoSandstorm", false);
        abilities.put("singleBuddyClimb", false);
        abilities.put("doubleBuddyClimb", false);
        abilities.put("levelTwoBuddyClimb", false);
        abilities.put("levelThreeBuddyClimb", false);

        for (ScoutEntry entry : entries) {
            if (entry.getAutonomous().isFrontCargoShipHatchCapable()) {
                abilities.put("frontCargoShipHatchSandstorm", true);
            }
            if (entry.getAutonomous().isSideCargoShipHatchCapable()) {
                abilities.put("sideCargoShipHatchSandstorm", true);
            }
            if (entry.getAutonomous().getRocketHatches() >= 1) {
                abilities.put("rocketHatchSandstorm", true);
            }
            if (entry.getAutonomous().getCargoShipCargo() >= 1) {
                abilities.put("cargoShipCargoSandstorm", true);
            }
            if (entry.getAutonomous().getRocketCargo() >= 1) {
                abilities.put("rocketCargoSandstorm", true);
            }
            if (entry.getTeleOp().getNumPartnerClimbAssists() == 1) {
                abilities.put("singleBuddyClimb", true);
            }
            if (entry.getTeleOp().getNumPartnerClimbAssists() == 2) {
                abilities.put("doubleBuddyClimb", true);
            }
            if (entry.getTeleOp().getPartnerClimbAssistEndLevel() == 2) {
                abilities.put("levelTwoBuddyClimb", true);
            }
            if (entry.getTeleOp().getPartnerClimbAssistEndLevel() == 3) {
                abilities.put("levelThreeBuddyClimb", true);
            }
        }
    }


    public HashMap<String, Double> getAverages() {
        return averages;
    }

    public HashMap<String, Double> getStandardDeviations() {
        return standardDeviations;
    }

    public HashMap<String, Integer> getCounts() {
        return counts;
    }

    public HashMap<String, Double> getAttemptSuccessRates() {
        return attemptSuccessRates;
    }

    public HashMap<String, Double> generateRandomSample() {
        HashMap<String, Double> randomSample = new HashMap<>();

        for (String value : averages.keySet()) {
            randomSample.put(value, Stats.randomNormalValue(averages.get(value), standardDeviations.get(value)));
        }

        return randomSample;
    }

    public int findBestClimbLevel() {
        int bestLevel = 0;
        double bestClimbPoints = 0.0;

        final int[] climbPointValues = new int[]{3, 6, 12};

        for (int i = 0; i < 3; i++) {
            double potentialPoints = attemptSuccessRates.get(levelPrefixes[i] + "Climb") * climbPointValues[i];
            if (potentialPoints >= bestClimbPoints) {
                bestClimbPoints = potentialPoints;
                bestLevel = i + 1;
            }
        }

        return bestLevel;
    }
}
