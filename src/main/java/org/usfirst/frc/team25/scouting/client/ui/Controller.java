package org.usfirst.frc.team25.scouting.client.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import org.usfirst.frc.team25.scouting.data.BlueAlliance;
import org.usfirst.frc.team25.scouting.data.EventReport;
import org.usfirst.frc.team25.scouting.data.FileManager;
import org.usfirst.frc.team25.scouting.data.models.ScoutEntry;

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

    private EventReport eventReport;
    private ArrayList<File> jsonFileList;
    private String eventName;

    private File currentDataDirectory, teamNameList;

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

            retrieveEventReport();

            if (backupJson.isSelected()) {
                FileManager.createBackup(jsonFileList, currentDataDirectory);
                status += "\nBackup JSON files created";
            }

            if (combineJson.isSelected() && eventReport.generateCombineJson(currentDataDirectory)) {
                status += "\nCombined data JSON file generated";

                if (FileManager.deleteIndividualDataFiles(currentDataDirectory)) {
                    status += "\nIndividual data JSON files deleted";
                }

            }

            if (generateCsv.isSelected()) {
                if (eventReport.generateRawSpreadsheet(currentDataDirectory)) {
                    status += "\nRaw data spreadsheet generated";
                } else {
                    status += "\nRaw data spreadsheet failed to generate. Are you sure the CSV file isn't open?";
                }
            }

            if (generatePicklists.isSelected()) {
                eventReport.generatePicklists(currentDataDirectory);
                status += "\nPicklists generated";
            }

            if (generatePredictions.isSelected()) {
                eventReport.generateMatchPredictions(currentDataDirectory);
                status += "\nFuture match predictions generated";
            }

            if (fixErrors.isSelected()) {
                eventReport.generateInaccuracyList(currentDataDirectory);
                eventReport.fixInaccuraciesTBA();
                status += "\nInaccuracies fixed and inaccuracy list generated";
            }

            if (status.isEmpty()) {
                addStatus("Please select data processing options!");
            } else {
                addStatus("Data processing for event " + eventName + " successful:\n" + status);
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

            retrieveEventReport();

            if (teamBasedReport.isSelected()) {

                int teamNum;
                try {
                    teamNum = Integer.parseInt(analysisTeamOne.getText());
                    if (!eventReport.isTeamPlaying(teamNum)) {
                        addStatus("Invalid team number for event " + eventName + ". Please try again.");
                    } else {
                        addStatus(eventReport.getTeamReport(teamNum).getQuickStatus());
                    }
                } catch (NumberFormatException e) {
                    addStatus("Invalid or missing team number. Please try again.");
                }


            } else {
                int teamOne, teamTwo, teamThree;

                try {
                    teamOne = Integer.parseInt(analysisTeamOne.getText());
                    teamTwo = Integer.parseInt(analysisTeamTwo.getText());
                    teamThree = Integer.parseInt(analysisTeamThree.getText());
                    if (!eventReport.isTeamPlaying(teamOne) || !eventReport.isTeamPlaying(teamTwo) || !eventReport.isTeamPlaying(teamThree)) {
                        addStatus("Invalid team number(s) for event " + eventName + ". Please try again.");
                    } else {
                        addStatus(eventReport.getAllianceReport(teamOne, teamTwo, teamThree).getQuickAllianceReport());
                    }
                } catch (NumberFormatException e) {
                    addStatus("Invalid or missing team number(s). Please try again.");

                }

            }
        });

    }

    /**
     * Retrieves JSON data files from the selected data directory and converts them into an EventReport for data
     * processing
     */
    private void retrieveEventReport() {
        this.jsonFileList = FileManager.getDataFiles(currentDataDirectory);

        if (jsonFileList.size() == 0) {
            addStatus("No JSON data files found in " + currentDataDirectory.getAbsolutePath() +
                    ".\nPlease select another directory.");
            return;
        }

        eventName = jsonFileList.get(0).getName().split(FileManager.FILE_EXTENSION_REGEX)[0].split(" - ")[2];


        ArrayList<ScoutEntry> scoutEntries = FileManager.deserializeData(jsonFileList);

        this.teamNameList = FileManager.getTeamNameList(currentDataDirectory);

        this.eventReport = new EventReport(scoutEntries, eventName, currentDataDirectory);

        if (teamNameList != null) {
            eventReport.setTeamNameList(teamNameList);
        }

        eventReport.processTeamReports();
    }

    /**
     * Adds a status display to the user-facing text box, with a separator between statuses
     *
     * @param message Text to display to the user
     */
    private void addStatus(String message) {
        statusTextBox.setText(message + "\n====================\n" + statusTextBox.getText());
    }

}
