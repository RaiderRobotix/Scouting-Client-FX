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

    public final static String[] autoMetricNames = new String[]{"cargoShipHatches", "rocketHatches", "cargoShipCargo",
            "rocketCargo"};
    public final static String[] teleMetricNames = new String[]{"cargoShipHatches", "rocketLevelOneHatches",
            "rocketLevelTwoHatches", "rocketLevelThreeHatches", "cargoShipCargo", "rocketLevelOneCargo",
            "rocketLevelTwoCargo", "rocketLevelThreeCargo", "numPartnerClimbAssists"};
    public final static String[] levelPrefixes = new String[]{"levelOne", "levelTwo", "levelThree", "total"};
    public final static String[] overallMetricNames = new String[]{"calculatedPointContribution",
            "calculatedSandstormPoints", "calculatedTeleOpPoints", "totalHatches", "totalCargo"};
    private final HashMap<String, Double> averages;
    private final HashMap<String, Double> standardDeviations;
    private final HashMap<String, Double> attemptSuccessRates;
    private final HashMap<String, Integer> counts;
    private final HashMap<String, Boolean> abilities;
    private String teamName, frequentCommentStr, allComments;
    private ArrayList<String> frequentComments;

    /**
     * An empty constructor that creates an TeamReport based on the metrics calculated in the model team report
     * Used to simulate additional alliance members
     *
     * @param model A team report with calculations performed on a real set of scouting entries
     */
    public TeamReport(TeamReport model) {
        averages = new HashMap<>();
        for (String key : model.averages.keySet()) {
            averages.put(key, 0.0);
        }

        standardDeviations = new HashMap<>();
        for (String key : model.standardDeviations.keySet()) {
            standardDeviations.put(key, 0.0);
        }

        attemptSuccessRates = new HashMap<>();
        for (String key : model.attemptSuccessRates.keySet()) {
            attemptSuccessRates.put(key, 0.0);
        }

        counts = new HashMap<>();
        for (String key : model.counts.keySet()) {
            counts.put(key, 0);
        }

        abilities = new HashMap<>();
        for (String key : model.abilities.keySet()) {
            abilities.put(key, false);
        }
        this.teamNum = 0;
        this.teamName = "";
        this.frequentCommentStr = "";
        this.allComments = "";
        this.frequentComments = new ArrayList<>();
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
        frequentCommentStr = "";

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

        StringBuilder statusString = new StringBuilder("Team " + getTeamNum());

        if (!getTeamName().isEmpty()) {
            statusString.append(" - ").append(getTeamName());
        }

        statusString.append("\n\nSandstorm:");

        for (String metric : autoMetricNames) {
            statusString.append("\nAvg. ").append(StringProcessing.convertCamelToSentenceCase(metric)).append(": ").append(Stats.round
                    (averages.get("auto" + metric), 2));
        }

        statusString.append("\nHAB line cross: ").append(Stats.round(attemptSuccessRates.get("totalCross") * 100, 2)).append("% (").append(counts.get("totalCross")).append("/").append(entries.size()).append(")");

        for (int i = 0; i < 2; i++) {
            statusString.append("\nHAB lvl ").append(i + 1).append(" cross: ");
            statusString.append(Stats.round(attemptSuccessRates.get(levelPrefixes[i] + "Cross") * 100, 2)).append("% "
            ).append("(").append(counts.get(levelPrefixes[i] + "Cross")).append("/").append(counts.get(levelPrefixes[i] + "Start")).append(")");
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
            ).append("(").append(counts.get(levelPrefixes[i] + "ClimbSuccess")).append("/").append(counts.get(levelPrefixes[i] +
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
    private void filterNoShow() {
        counts.put("noShow", 0);
        counts.put("dysfunctional", 0);
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getPreMatch().isRobotNoShow()) {
                entries.remove(i);
                i--;
                incrementCount("noShow");
                incrementCount("dysfunctional");
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
    private void calculateStats() {

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

            if (entry.getPostMatch().getRobotQuickCommentSelections().get("Lost communications") || entry.getPostMatch().getRobotQuickCommentSelections().get("Tipped over")) {
                incrementCount("dysfunctional");
            }

        }
    }

    private void findFrequentComments() {

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


    public boolean getAbility(String metric) {
        return abilities.get(metric);
    }

    public double getAverage(String metric) {
        return averages.get(metric);
    }

    public double getStandardDeviation(String metric) {
        return standardDeviations.get(metric);
    }

    public int getCount(String metric) {
        return counts.get(metric);
    }

    public double getAttemptSuccessRate(String metric) {
        return attemptSuccessRates.get(metric);
    }

    /**
     * Generates a random sample of various metrics computed in <code>averages</code>, assuming a normal distribution
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
