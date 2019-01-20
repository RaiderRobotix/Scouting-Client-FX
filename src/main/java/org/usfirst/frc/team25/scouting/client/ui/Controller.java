package org.usfirst.frc.team25.scouting.client.ui;

import com.thebluealliance.api.v3.TBA;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import org.usfirst.frc.team25.scouting.client.data.BlueAlliance;

import java.io.File;

public class Controller {

    @FXML
    private Button chooseDataFolderButton, generateFilesButton, downloadDataButton, displayReportButton;
    @FXML
    private TextArea statusTextBox;
    @FXML
    private Text dataDirectoryDisplay;
    @FXML
    private CheckBox combineJson, generatePicklists, generateCsv, fixErrors, backupJson, generatePredictions;
    @FXML
    private RadioButton teamBasedReport, allianceBasedReport, teamEventsDownload, eventDownload;
    @FXML
    private TextField analysisTeamOne, analysisTeamTwo, analysisTeamThree, teamNumEventCode;

    private File currentDataDirectory;

    public void initialize() {

        statusTextBox.setText("Client opened!\n\nSelect data folder");

        ToggleGroup reportGenerationGroup = new ToggleGroup();
        teamBasedReport.setToggleGroup(reportGenerationGroup);
        allianceBasedReport.setToggleGroup(reportGenerationGroup);

        ToggleGroup tbaDownloadGroup = new ToggleGroup();
        teamEventsDownload.setToggleGroup(tbaDownloadGroup);
        eventDownload.setToggleGroup(tbaDownloadGroup);

        dataDirectoryDisplay.setText("");

        chooseDataFolderButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(chooseDataFolderButton.getScene().getWindow());

            if (selectedDirectory == null) {
                if (currentDataDirectory == null) {
                    statusTextBox.setText("Invalid or no data directory selected!");
                } else {
                    statusTextBox.setText(currentDataDirectory.getAbsolutePath() + " retained as data directory");
                }
            } else {
                currentDataDirectory = selectedDirectory;
                statusTextBox.setText(selectedDirectory.getAbsolutePath() + " selected as data directory");
                dataDirectoryDisplay.setText(selectedDirectory.getAbsolutePath());
                generateFilesButton.setDisable(false);
                downloadDataButton.setDisable(false);
                displayReportButton.setDisable(false);
            }
        });

        allianceBasedReport.setOnAction(event -> {
            analysisTeamTwo.setDisable(!allianceBasedReport.isSelected());
            analysisTeamThree.setDisable(!allianceBasedReport.isSelected());
        });

        teamBasedReport.setOnAction(event -> {
            analysisTeamTwo.setDisable(teamBasedReport.isSelected());
            analysisTeamThree.setDisable(teamBasedReport.isSelected());
            analysisTeamTwo.setText("");
            analysisTeamThree.setText("");
        });

        generateFilesButton.setOnAction(event -> {

        });

        downloadDataButton.setOnAction(event -> {
            if (teamEventsDownload.isSelected()) {

            } else {
                String response = BlueAlliance.downloadEventTeamData(currentDataDirectory, teamNumEventCode.getText()
                        , new TBA(BlueAlliance.API_KEY));

            }
        });

        displayReportButton.setOnAction(event -> {

        });

    }

}
