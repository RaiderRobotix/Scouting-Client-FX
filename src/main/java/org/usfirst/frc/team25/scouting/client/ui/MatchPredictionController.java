package org.usfirst.frc.team25.scouting.client.ui;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import org.usfirst.frc.team25.scouting.data.AllianceReport;

public class MatchPredictionController {

    @FXML
    Text redTeamOneNum;

    public void initialize(AllianceReport[] alliances) {
        redTeamOneNum.setText(Integer.toString(alliances[0].getTeamReports()[0].getTeamNum()));
    }
}
