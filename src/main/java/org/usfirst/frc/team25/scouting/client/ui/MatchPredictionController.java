package org.usfirst.frc.team25.scouting.client.ui;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import org.usfirst.frc.team25.scouting.data.AllianceReport;
import org.usfirst.frc.team25.scouting.data.Stats;
import org.usfirst.frc.team25.scouting.data.TeamReport;

/**
 * Controller for <code>match_predictions.fxml</code>
 */
public class MatchPredictionController {

    @FXML
    Text eventKey, matchNumText;
    private Scene scene;

    /**
     * Responsible for extracting values from alliance reports
     * and displaying them on the match prediction screen.
     *
     * @param redAlliance  Red alliance data for the current match
     * @param blueAlliance Blue alliance data for the current match
     */
    public void initialize(AllianceReport redAlliance, AllianceReport blueAlliance) {

        // Arrays to help iterate over common words used in ID names
        final String[] locations = new String[]{"rocketLevelOne", "teleRocketLevelTwo", "teleRocketLevelThree",
                "cargoShip"};
        final String[] pieces = new String[]{"Hatches", "Cargo"};

        // Hard-coded overall metrics that don't correspond to a location
        final String[] predictedMetrics = new String[]{"totalPoints", "rocketRp", "climbRp",
                "sandstormBonus", "sandstormGamePiecePoints", "teleHatchPoints", "teleCargoPoints", "endgamePoints"};
        final String[] numStrNames = new String[]{"One", "Two", "Three"};

        for (String color : new String[]{"red", "blue"}) {
            // This is a ternary operator - easy replacement for a single-line if/else statement
            AllianceReport alliance = color.equals("red") ? redAlliance : blueAlliance;
            AllianceReport otherAlliance = color.equals("red") ? blueAlliance : redAlliance;

            for (String location : locations) {
                for (String piece : pieces) {
                    setText(color, location + piece, displayDouble(alliance.getPredictedValue(location + piece)));
                }
            }

            for (String predictedMetric : predictedMetrics) {
                setText(color, predictedMetric, displayDouble(alliance.getPredictedValue(predictedMetric)));
            }

            setText(color, "optimalNullHatches", displayDouble(alliance.getPredictedValue("optimalNullHatches")) + " " +
                    "NULL");

            // Miscellaneous metrics dependent on winPercent
            double winPercent = alliance.calculateWinChance(otherAlliance) * 100;

            setText(color, "winChance", displayDouble(winPercent) + "%");
            setText(color, "winRp", displayDouble(2 * winPercent / 100));
            setText(color, "predictedRp", displayDouble(alliance.calculatePredictedRp(otherAlliance)) + " RP");

            // Begin displaying individual team metric values
            for (int i = 0; i < 3; i++) {
                TeamReport currentTeam = alliance.getTeamReports()[i];

                setText(color, "team" + numStrNames[i] + "Num",
                        Integer.toString(alliance.getTeamReports()[i].getTeamNum()));

                setText(color, "team" + numStrNames[i] + "Hatches",
                        displayDouble(currentTeam.getAverage("totalHatches")));
                setText(color, "team" + numStrNames[i] + "Cargo",
                        displayDouble(currentTeam.getAverage("totalCargo")));

                // Generate display string for starting position and HAB crossing percentage
                String startString = "";
                char assignedGamePiece = alliance.getBestSandstormGamePieceCombo().charAt(i);
                startString += alliance.getBestStartingLevels()[i] + Character.toString(assignedGamePiece) + " (";

                if (assignedGamePiece == 'H') {
                    startString += (int) Math.round(100 * currentTeam.getAttemptSuccessRate("hatchAutoSuccess"));
                } else {
                    startString += (int) Math.round(100 * currentTeam.getAttemptSuccessRate("cargoAutoSuccess"));
                }
                startString += "%)";

                setText(color, "team" + numStrNames[i] + "Start", startString);

                // Generate display string for HAB climbing
                int bestLevel = alliance.getTeamReports()[i].findBestClimbLevel();
                String climbString = bestLevel + " (";
                climbString += (int) Math.round(100 * currentTeam.getAttemptSuccessRate("level" + numStrNames[bestLevel - 1] + "Climb"));
                climbString += "%)";

                setText(color, "team" + numStrNames[i] + "Climb", climbString);
            }
        }


    }

    /**
     * Sets the UI <code>Text</code> component to the specified metric's value
     *
     * @param color      Either <code>red</code> or <code>blue</code>, corresponding to the alliance that the value
     *                   belongs to
     * @param metricName ID of the <code>Text</code> object, corresponding to a data metric
     * @param text       String to display in the specified field
     */
    private void setText(String color, String metricName, String text) {
        String modifiedMetricName = Character.toUpperCase(metricName.charAt(0)) + metricName.substring(1);
        String idName = color + modifiedMetricName;
        ((Text) scene.lookup("#" + idName)).setText(text);
    }

    /**
     * Converts floating-point numbers into easily-displayable strings
     *
     * @param value Number to display
     * @return Value rounded to the tenths place, as a <code>String</code>
     */
    private String displayDouble(double value) {
        return Double.toString(Stats.round(value, 1));
    }

    /**
     * Sets the event key text on the screen
     *
     * @param eventKey Current event's fully-qualified key
     */
    public void setEventKey(String eventKey) {
        this.eventKey.setText(eventKey);
    }

    /**
     * Sets the match number text on the screen
     *
     * @param matchNumber Match number for the current prediction
     */
    public void setMatchNumber(int matchNumber) {
        matchNumText.setText("Match " + matchNumber + " Predictions");
        matchNumText.setVisible(true);
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
