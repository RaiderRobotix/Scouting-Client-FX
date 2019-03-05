package org.usfirst.frc.team25.scouting.client.ui;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import org.usfirst.frc.team25.scouting.data.AllianceReport;
import org.usfirst.frc.team25.scouting.data.Stats;
import org.usfirst.frc.team25.scouting.data.TeamReport;

public class MatchPredictionController {

    @FXML
    Text redTeamOneNum, eventKey, matchNumText;
    Scene scene;

    public void initialize(AllianceReport redAlliance, AllianceReport blueAlliance) {

        final String[] locations = new String[]{"rocketLevelOne", "teleRocketLevelTwo", "teleRocketLevelThree",
                "cargoShip"};
        final String[] pieces = new String[]{"Hatches", "Cargo"};

        final String[] predictedMetrics = new String[]{"totalPoints", "rocketRp", "climbRp",
                "sandstormBonus", "sandstormGamePiecePoints", "teleHatchPoints", "teleCargoPoints", "endgamePoints"};
        final String[] numStrNames = new String[]{"One", "Two", "Three"};

        for (String color : new String[]{"red", "blue"}) {
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

            double winPercent = alliance.calculateWinChance(otherAlliance) * 100;

            setText(color, "winChance", displayDouble(winPercent) + "%");
            setText(color, "winRp", displayDouble(2 * winPercent / 100));
            setText(color, "predictedRp", displayDouble(alliance.calculatePredictedRp(otherAlliance)) + " RP");

            for (int i = 0; i < 3; i++) {
                TeamReport currentTeam = alliance.getTeamReports()[i];

                setText(color, "team" + numStrNames[i] + "Num",
                        Integer.toString(alliance.getTeamReports()[i].getTeamNum()));

                setText(color, "team" + numStrNames[i] + "Hatches",
                        displayDouble(currentTeam.getAverage("totalHatches")));
                setText(color, "team" + numStrNames[i] + "Cargo",
                        displayDouble(currentTeam.getAverage("totalCargo")));

                String startString = "";
                startString += alliance.getBestStartingLevels()[i] + Character.toString(alliance.getBestSandstormGamePieceCombo().charAt(i)) + " (";

                if (alliance.getBestSandstormGamePieceCombo().charAt(i) == 'H') {
                    startString += (int) Math.round(100 * currentTeam.getAttemptSuccessRate("hatchAutoSuccess"));
                } else {
                    startString += (int) Stats.round(100 * currentTeam.getAttemptSuccessRate("cargoAutoSuccess"), 0);
                }
                startString += "%)";


                setText(color, "team" + numStrNames[i] + "Start", startString);

                int bestLevel = alliance.getTeamReports()[i].findBestClimbLevel();
                String climbString = bestLevel + " (";
                climbString += (int) Stats.round(100 * currentTeam.getAttemptSuccessRate("level" + numStrNames[bestLevel - 1] + "Climb"), 0);
                climbString += "%)";

                setText(color, "team" + numStrNames[i] + "Climb", climbString);

            }
        }


    }

    private void setText(String color, String metricName, String value) {
        String modifiedMetricName = Character.toUpperCase(metricName.charAt(0)) + metricName.substring(1);
        String idName = color + modifiedMetricName;
        ((Text) scene.lookup("#" + idName)).setText(value);
    }

    private String displayDouble(double value) {
        return Double.toString(Stats.round(value, 1));
    }

    public void setEventKey(String eventKey) {
        this.eventKey.setText(eventKey);
    }

    public void setMatchNumber(int matchNumber) {
        matchNumText.setText("Match " + matchNumber + " Predictions");
        matchNumText.setVisible(true);
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
