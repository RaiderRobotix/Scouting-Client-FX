package org.usfirst.frc.team25.scouting.client.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import org.usfirst.frc.team25.scouting.client.data.BlueAlliance;
import org.usfirst.frc.team25.scouting.client.data.EventReport;
import org.usfirst.frc.team25.scouting.client.data.FileManager;
import org.usfirst.frc.team25.scouting.client.models.ScoutEntry;

import java.io.File;
import java.util.ArrayList;

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

        addStatus("Client opened!\n\nSelect data folder");

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
                    addStatus("Invalid or no data directory selected!");
                } else {
                    addStatus(currentDataDirectory.getAbsolutePath() + " retained as data directory");
                }
            } else {
                currentDataDirectory = selectedDirectory;
                addStatus(selectedDirectory.getAbsolutePath() + " selected as data directory");
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

            String status = "";

            ArrayList<File> jsonFileList = FileManager.getDataFiles(currentDataDirectory);

            if (jsonFileList.size() == 0) {
                addStatus("No JSON data files found in " + currentDataDirectory.getAbsolutePath() +
                        ".\nPlease select another directory.");
                return;
            }

            String eventName = jsonFileList.get(0).getName().split(FileManager.FILE_EXTENSION_REGEX)[0].split(" - ")[2];


            ArrayList<ScoutEntry> scoutEntries = FileManager.deserializeData(jsonFileList);


            EventReport report = new EventReport(scoutEntries, eventName, currentDataDirectory);

            File teamNameList = FileManager.getTeamNameList(currentDataDirectory);

            if (teamNameList != null) {
                report.setTeamNameList(teamNameList);
            }

            if (backupJson.isSelected()) {
                FileManager.createBackup(jsonFileList, currentDataDirectory);
                status += "Backup JSON files created";
            }

            if (combineJson.isSelected() && report.generateCombineJson(currentDataDirectory)) {
                status += "\nCombined data JSON file generated";

                if (FileManager.deleteIndividualDataFiles(currentDataDirectory)) {
                    status += "\nIndividual data JSON files deleted";
                }

            }

            if (generateCsv.isSelected()) {
                report.generateRawSpreadsheet(currentDataDirectory);
                status += "\nRaw data spreadsheet generated";
            }

            if (generatePicklists.isSelected()) {
                report.generatePicklists(currentDataDirectory);
                status += "\nPicklists generated";
            }

            if (generatePredictions.isSelected()) {
                report.generateMatchPredictions(currentDataDirectory);
                status += "\nFuture match predictions generated";
            }

            if (fixErrors.isSelected()) {
                report.generateInaccuracyList(currentDataDirectory);
                report.fixInaccuraciesTBA();
                status += "\nInaccuracies fixed and inaccuracy list generated";
            }

            if (status.isEmpty()) {
                addStatus("Please select data processing options!");
            } else {
                addStatus("Data processing for event " + eventName + " successful:\n\n" + status);
            }

        });

        downloadDataButton.setOnAction(event -> {
            String response;
            if (teamNumEventCode.getText().isEmpty()) {
                response = "Please enter a team number or event key.";
            } else if (teamEventsDownload.isSelected()) {
                try {
                    response = BlueAlliance.downloadTeamEvents(currentDataDirectory,
                            Integer.parseInt(teamNumEventCode.getText()));
                } catch (NumberFormatException e) {
                    response = teamNumEventCode.getText() + " is not a valid team number";
                }
            } else {
                response = BlueAlliance.downloadEventTeamData(currentDataDirectory, teamNumEventCode.getText());

            }

            addStatus(response);
        });

        displayReportButton.setOnAction(event -> {

        });

    }

    private void addStatus(String message) {
        statusTextBox.setText(message + "\n====================\n" + statusTextBox.getText());
    }

}
