package org.usfirst.frc.team25.scouting.client.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;

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

    private File currentDirectory;

    public void initialize() {

        statusTextBox.setText("Client opened!\n\nSelect data folder");

        ToggleGroup reportGenerationGroup = new ToggleGroup();
        teamBasedReport.setToggleGroup(reportGenerationGroup);
        allianceBasedReport.setToggleGroup(reportGenerationGroup);

        ToggleGroup tbaDownloadGroup = new ToggleGroup();
        teamEventsDownload.setToggleGroup(tbaDownloadGroup);
        eventDownload.setToggleGroup(tbaDownloadGroup);

        dataDirectoryDisplay.setText("");

        chooseDataFolderButton.setOnMouseClicked(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(chooseDataFolderButton.getScene().getWindow());

            if (selectedDirectory == null) {
                if (currentDirectory == null) {
                    statusTextBox.setText("Invalid or no data directory selected!");
                } else {
                    statusTextBox.setText(currentDirectory.getAbsolutePath() + " retained as data directory");
                }
            } else {
                currentDirectory = selectedDirectory;
                statusTextBox.setText(selectedDirectory.getAbsolutePath() + " selected as data directory");
                dataDirectoryDisplay.setText(selectedDirectory.getAbsolutePath());
                generateFilesButton.setDisable(false);
                downloadDataButton.setDisable(false);
                displayReportButton.setDisable(false);
            }
        });

        generateFilesButton.setOnMouseClicked(event -> {

        });

        downloadDataButton.setOnMouseClicked(event -> {

        });

        displayReportButton.setOnMouseClicked(event -> {

        });

    }

}
