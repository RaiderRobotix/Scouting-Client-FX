package org.usfirst.frc.team25.scouting.data;

import org.usfirst.frc.team25.scouting.data.models.Autonomous;
import org.usfirst.frc.team25.scouting.data.models.ScoutEntry;
import org.usfirst.frc.team25.scouting.data.models.TeleOp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Object model containing individual reports of teams in events and methods to process data and calculate team-based
 * statistics
 */
public class TeamReport {

    private final transient ArrayList<ScoutEntry> entries;
    private final int teamNum;

    // Metric names defined to assist with iterating over values
    public final static String[] autoMetricNames = new String[]{"cargoShipHatches", "rocketHatches", "cargoShipCargo",
            "rocketCargo"};
    public final static String[] levelPrefixes = new String[]{"levelOne", "levelTwo", "levelThree", "total"};
    // HashMaps containing metric name and value pairings
    private final HashMap<String, Double> averages;
    public final static String[] teleMetricNames = new String[]{"cargoShipHatches", "rocketLevelOneHatches",
            "rocketLevelTwoHatches", "rocketLevelThreeHatches", "cargoShipCargo", "rocketLevelOneCargo",
            "rocketLevelTwoCargo", "rocketLevelThreeCargo", "numPartnerClimbAssists"};
    public final static String[] overallMetricNames = new String[]{"calculatedPointContribution",
            "calculatedSandstormPoints", "calculatedTeleOpPoints", "totalHatches", "totalCargo"};
    private String teamName, frequentCommentStr, allComments;
    private ArrayList<String> frequentComments;
    private final HashMap<String, Double> standardDeviations;
    private final HashMap<String, Double> attemptSuccessRates;
    private final HashMap<String, Integer> counts;
    private final HashMap<String, Boolean> abilities;

    /**
     * An copy constructor that creates an empty TeamReport based on the metrics calculated in the model team report
     * Used to simulate additional alliance members that contribute no points
     *
     * @param model A team report with calculations performed on a real set of scouting entries
     */
    public TeamReport(TeamReport model) {
        this(0);

        for (String key : model.averages.keySet()) {
            averages.put(key, 0.0);
        }

        for (String key : model.standardDeviations.keySet()) {
            standardDeviations.put(key, 0.0);
        }

        for (String key : model.attemptSuccessRates.keySet()) {
            attemptSuccessRates.put(key, 0.0);
        }

        for (String key : model.counts.keySet()) {
            counts.put(key, 0);
        }

        for (String key : model.abilities.keySet()) {
            abilities.put(key, false);
        }
    }

    /**
     * Constructs a new team report, with empty calculated statistics
     *
     * @param teamNum Team number of the team that is being reported
     */
    public TeamReport(int teamNum) {
        this.teamNum = teamNum;
        entries = new ArrayList<>();
        teamName = "";
        frequentCommentStr = "";

        averages = new HashMap<>();
        standardDeviations = new HashMap<>();
        counts = new HashMap<>();
        attemptSuccessRates = new HashMap<>();
        abilities = new HashMap<>();
        frequentComments = new ArrayList<>();
    }

    /**
     * Processes the scout entries within the team report by filtering out no shows, calculating stats, and finding
     * abilities and frequent comments
     */
    public void processReport() {
        filterNoShow();
        findFrequentComments();
        calculateStats();
        //findAbilities();
    }

    /**
     * Removes scouting entries where the robot did not show up and increments the "no show" and "dysfunctional" counts
     */
    private void filterNoShow() {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getPreMatch().isRobotNoShow()) {
                entries.remove(i);

                // Decrement counter to prevent skipping over entries
                i--;
                incrementCount("noShow");
                incrementCount("dysfunctional");
            }
        }
    }

    /**
     * Increments a count metric by 1 and creates it if it currently does not exist
     *
     * @param metricName The count metric to increment
     */
    private void incrementCount(String metricName) {
        if (counts.containsKey(metricName)) {
            counts.put(metricName, getCount(metricName) + 1);
        } else {
            counts.put(metricName, 1);
        }
    }

    /**
     * Retrieves the value of the specified count metric
     *
     * @param metric String name of the desired metric
     * @return The value of the count metric, 0 if the metric name does not exist
     */
    public int getCount(String metric) {
        if (!counts.containsKey(metric)) {
            return 0;
        }
        return counts.get(metric);
    }

    /**
     * Calculates the counts, averages, standard deviations, and attempt-success rates of data in stored scouting
     * entries, provided that data exists
     */
    private void calculateStats() {
        if (entries.size() > 0) {
            //calculateCounts();
            calculateAverages();
            calculateStandardDeviations();
            calculateAttemptSuccessRates();
        }
    }

    /**
     * Populates the frequent comment array with quick comments that appear at least 25% of the time in a team's
     * scouting entries
     * Also concatenates all custom comments made into the <code>allComments</code> string
     */
    private void findFrequentComments() {
        HashMap<String, Integer> commentFrequencies = new HashMap<>();
        if (entries.size() > 0) {
            for (String key : entries.get(0).getPostMatch().getRobotQuickCommentSelections().keySet()) {

                commentFrequencies.put(key, 0);

                for (ScoutEntry entry : entries) {
                    if (entry.getPostMatch().getRobotQuickCommentSelections().get(key)) {
                        commentFrequencies.put(key, commentFrequencies.get(key) + 1);
                    }
                }
            }
        }

        for (String key : commentFrequencies.keySet()) {
            if (commentFrequencies.get(key) >= entries.size() / 4.0) {
                frequentComments.add(key);
            }
        }

        for (String comment : frequentComments) {
            frequentCommentStr += StringProcessing.removeCommasBreaks(comment) + " \n";
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
            double[] values = SortersFilters.getDoubleArray(autoList, metric, int.class);
            averages.put("auto" + metric, Stats.mean(values));
        }
        for (String metric : teleMetricNames) {
            double[] values = SortersFilters.getDoubleArray(teleList, metric, int.class);
            averages.put("tele" + metric, Stats.mean(values));
        }
        for (String metric : overallMetricNames) {
            double[] values = SortersFilters.getDoubleArray(overallList, metric, int.class);
            averages.put(metric, Stats.mean(values));
        }
    }

    /**
     * Determines if teams are capable of intaking game pieces from the floor and their potential sandstorm and climb
     * modes
     */
//    private void findAbilities() {
//        abilities.put("cargoFloorIntake", frequentComments.contains("Cargo floor intake"));
//        abilities.put("hatchPanelFloorIntake", frequentComments.contains("Hatch panel floor intake"));
//
//        for (ScoutEntry entry : entries) {
//            if (entry.getAutonomous().isFrontCargoShipHatchCapable()) {
//                abilities.put("frontCargoShipHatchSandstorm", true);
//            }
//            if (entry.getAutonomous().isSideCargoShipHatchCapable()) {
//                abilities.put("sideCargoShipHatchSandstorm", true);
//            }
//            if (entry.getAutonomous().getRocketHatches() >= 1) {
//                abilities.put("rocketHatchSandstorm", true);
//            }
//            if (entry.getAutonomous().getCargoShipCargo() >= 1) {
//                abilities.put("cargoShipCargoSandstorm", true);
//            }
//            if (entry.getAutonomous().getRocketCargo() >= 1) {
//                abilities.put("rocketCargoSandstorm", true);
//            }
//            if (entry.getTeleOp().getNumPartnerClimbAssists() == 1) {
//                abilities.put("singleBuddyClimb", true);
//            }
//            if (entry.getTeleOp().getNumPartnerClimbAssists() == 2) {
//                abilities.put("doubleBuddyClimb", true);
//            }
//            if (entry.getTeleOp().getPartnerClimbAssistEndLevel() == 2) {
//                abilities.put("levelTwoBuddyClimb", true);
//            }
//            if (entry.getTeleOp().getPartnerClimbAssistEndLevel() == 3) {
//                abilities.put("levelThreeBuddyClimb", true);
//            }
//        }
//    }

    /**
     * Calculates the number of times a team starts/crosses a particular level of the HAB during the sandstorm period
     * and the number of HAB climb attempts/successes
     */
//    private void calculateCounts() {
//
//        for (ScoutEntry entry : entries) {
//
//            incrementCount(levelPrefixes[entry.getPreMatch().getStartingLevel() - 1] + "Start");
//
//            if (entry.getPreMatch().getStartingGamePiece().equals("Cargo")) {
//                incrementCount("cargoStart");
//                if (entry.getSandstormCargo() >= 1) {
//                    incrementCount("cargoAutoSuccess");
//                }
//            }
//
//            if (entry.getPreMatch().getStartingGamePiece().equals("Hatch panel")) {
//                incrementCount("hatchStart");
//                if (entry.getSandstormHatches() >= 1) {
//                    incrementCount("hatchAutoSuccess");
//                }
//            }
//
//            // Increase level one count if the robot crosses on either level 1 or 2
//            if (entry.getAutonomous().isCrossHabLine()) {
//                if (entry.getPreMatch().getStartingLevel() == 2) {
//                    incrementCount("levelOneCross");
//                    incrementCount("levelOneStart");
//                }
//                incrementCount(levelPrefixes[entry.getPreMatch().getStartingLevel() - 1] + "Cross");
//                incrementCount("totalCross");
//            }
//
//            if (entry.getTeleOp().isAttemptHabClimb()) {
//                incrementCount(levelPrefixes[entry.getTeleOp().getAttemptHabClimbLevel() - 1] +
//                        "ClimbAttempt");
//                incrementCount("totalClimbAttempt");
//            }
//
//            if (entry.getTeleOp().isSuccessHabClimb()) {
//                incrementCount(levelPrefixes[entry.getTeleOp().getSuccessHabClimbLevel() - 1] +
//                        "ClimbSuccess");
//
//                // For cases in which a robot attempts level 3, but only manages to get level 2
//                if (entry.getTeleOp().getSuccessHabClimbLevel() != entry.getTeleOp().getAttemptHabClimbLevel()) {
//                    incrementCount(levelPrefixes[entry.getTeleOp().getSuccessHabClimbLevel() - 1] +
//                            "ClimbAttempt");
//                }
//
//                incrementCount("totalClimbSuccess");
//            }
//
//            if (entry.getPostMatch().getRobotQuickCommentSelections().get("Lost communications") || entry.getPostMatch().getRobotQuickCommentSelections().get("Tipped over")) {
//                incrementCount("dysfunctional");
//            }
//        }
//    }

    /**
     * Calculates the sample standard of metrics specified in <code>autoMetricNames</code> and
     * <code>teleMetricNames</code>
     */
    private void calculateStandardDeviations() {
        ArrayList<Object> autoList = SortersFilters.filterDataObject(entries, Autonomous.class);
        ArrayList<Object> teleList = SortersFilters.filterDataObject(entries, TeleOp.class);
        ArrayList<Object> overallList = new ArrayList<>(entries);

        for (String metric : autoMetricNames) {
            double[] values = SortersFilters.getDoubleArray(autoList, metric, int.class);
            standardDeviations.put("auto" + metric, Stats.standardDeviation(values));
        }
        for (String metric : teleMetricNames) {
            double[] values = SortersFilters.getDoubleArray(teleList, metric, int.class);
            standardDeviations.put("tele" + metric, Stats.standardDeviation(values));
        }
        for (String metric : overallMetricNames) {
            double[] values = SortersFilters.getDoubleArray(overallList, metric, int.class);
            standardDeviations.put(metric, Stats.standardDeviation(values));
        }
    }

    /**
     * Calculates the attempt-success rates of HAB climb, sandstorm bonus, and/or sandstorm game piece placing for a
     * team
     */
    private void calculateAttemptSuccessRates() {
        for (int i = 0; i < 4; i++) {
            // Calculating HAB line crosses; skips level 3
            if (i != 2) {
                double crossRate = 0.0;

                int attempts = 0;

                if (i == 3 && entries.size() != 0) {
                    attempts = entries.size();
                    crossRate = (double) getCount(levelPrefixes[i] + "Cross") / attempts;
                } else if (getCount(levelPrefixes[i] + "Start") != 0) {
                    attempts = getCount(levelPrefixes[i] + "Start");
                    crossRate = (double) getCount(levelPrefixes[i] + "Cross") / attempts;
                }

                // Calculate the standard deviation of a sample proportion
                standardDeviations.put(levelPrefixes[i] + "Cross", Stats.standardDeviation(attempts,
                        getCount(levelPrefixes[i] + "Cross")));

                attemptSuccessRates.put(levelPrefixes[i] + "Cross", crossRate);
            }

            double climbRate = 0.0;

            if (getCount(levelPrefixes[i] + "ClimbAttempt") != 0) {
                climbRate = (double) getCount(levelPrefixes[i] + "ClimbSuccess") / getCount(levelPrefixes[i] +
                        "ClimbAttempt");
            }

            standardDeviations.put(levelPrefixes[i] + "Climb", Stats.standardDeviation(getCount(levelPrefixes[i] +
                    "ClimbAttempt"), getCount(levelPrefixes[i] + "ClimbSuccess")));
            attemptSuccessRates.put(levelPrefixes[i] + "Climb", climbRate);
        }

        for (String prefix : new String[]{"cargo", "hatch"}) {

            double placeRate = 0.0;

            if (getCount(prefix + "Start") != 0) {
                placeRate = (double) getCount(prefix + "AutoSuccess") / getCount(prefix + "Start");
            }

            standardDeviations.put(prefix + "AutoSuccess", Stats.standardDeviation(getCount(prefix + "Start"),
                    getCount(prefix + "AutoSuccess")));
            attemptSuccessRates.put(prefix + "AutoSuccess", placeRate);
        }
    }

    /**
     * Finds the endgame HAB climb level for this team that results in the greatest expected endgame contribution
     * Expected contribution is equal to the team's attempt-success rate for climbing a particular HAB level
     * multiplied by the point value of that level
     *
     * @return The HAB climb level that yields the greatest expected contribution, 3 if the team has not climbed before
     */
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

    /**
     * Generates a random sample of various metrics computed in <code>averages</code>, assuming a Normal distribution
     * with standard deviations specified by the team's <code>standardDeviations</code>
     *
     * @return A HashMap with metric names as keys and their associated random values
     */
    public HashMap<String, Double> generateRandomSample() {
        HashMap<String, Double> randomSample = new HashMap<>();

        for (String value : averages.keySet()) {
            randomSample.put(value, Stats.randomNormalValue(averages.get(value), standardDeviations.get(value)));
        }

        return randomSample;
    }

    /**
     * Generates an easily-readable report with relevant stats on an team's capability
     *
     * @return A formatted string with relevant aggregate team stats
     */
    public String getQuickStatus() {

        StringBuilder statusString = new StringBuilder("Team " + getTeamNum());

        if (!getTeamName().isEmpty()) {
            statusString.append(" - ").append(getTeamName());
        }

        statusString.append("\n\nSandstorm:");

        for (String metric : autoMetricNames) {
            statusString.append("\nAvg. ").append(StringProcessing.convertCamelToSentenceCase(metric)).append(": ").append(Stats.round
                    (averages.get("auto" + metric), 2));
        }

        statusString.append("\nHAB line cross: ").append(Stats.round(attemptSuccessRates.get("totalCross") * 100, 2)).append("% (").append(getCount("totalCross")).append("/").append(entries.size()).append(")");

        for (int i = 0; i < 2; i++) {
            statusString.append("\nHAB lvl ").append(i + 1).append(" cross: ");
            statusString.append(Stats.round(attemptSuccessRates.get(levelPrefixes[i] + "Cross") * 100, 2)).append("% "
            ).append("(").append(getCount(levelPrefixes[i] + "Cross")).append("/").append(getCount(levelPrefixes[i] + "Start")).append(")");
        }

        statusString.append("\n\nTele-Op:");

        for (String metric : teleMetricNames) {
            statusString.append("\nAvg. ").append(StringProcessing.convertCamelToSentenceCase(metric)).append(": ").append(Stats.round
                    (averages.get("tele" + metric), 2));
        }

        statusString.append("\n\nEndgame:");

        for (int i = 0; i < 4; i++) {

            if (i == 3) {
                statusString.append("\nTotal climb success: ");
            } else {
                statusString.append("\nLvl ").append(i + 1).append(" climb success: ");
            }
            statusString.append(Stats.round(attemptSuccessRates.get(levelPrefixes[i] + "Climb") * 100, 0)).append("% "
            ).append("(").append(getCount(levelPrefixes[i] + "ClimbSuccess")).append("/").append(getCount(levelPrefixes[i] +
                    "ClimbAttempt")).append(")");

        }

        statusString.append("\n\nOverall:");

        for (String metric : overallMetricNames) {
            statusString.append("\nAvg. ").append(StringProcessing.convertCamelToSentenceCase(metric)).append(": ").append(Stats.round
                    (averages.get(metric), 2));
        }

        if (!frequentCommentStr.isEmpty()) {
            statusString.append("\n\nCommon quick comments:\n").append(frequentCommentStr);
        }
        if (!allComments.isEmpty()) {
            statusString.append("\nAll comments:\n").append(allComments);
        }

        return statusString.toString();

    }

    public String getTeamName() {
        return teamName;
    }

    public int getTeamNum() {
        return teamNum;
    }

    /**
     * Adds entries to the scouting entry list of this team
     *
     * @param entry <code>ScoutEntry</code> to be added to this team report
     */
    public void addEntry(ScoutEntry entry) {
        entry.getPostMatch().setRobotComment(StringProcessing.removeCommasBreaks(entry.getPostMatch().getRobotComment
                ()));

        entries.add(entry);
    }

    public ArrayList<ScoutEntry> getEntries() {
        return this.entries;
    }

    /**
     * Fetches the nickname of the team from the specified team list and assigns it to <code>teamName</code>
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
     * Retrieves the value of the specified ability metric
     *
     * @param metric String name of the desired metric
     * @return The value of the ability metric, false if the metric name does not exist
     */
    public boolean getAbility(String metric) {
        if (!abilities.containsKey(metric)) {
            return false;
        }
        return abilities.get(metric);
    }

    public double getAverage(String metric) {
        return averages.get(metric);
    }

    public double getStandardDeviation(String metric) {
        return standardDeviations.get(metric);
    }

    public double getAttemptSuccessRate(String metric) {
        return attemptSuccessRates.get(metric);
    }
}
