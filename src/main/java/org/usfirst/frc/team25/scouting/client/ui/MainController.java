package org.usfirst.frc.team25.scouting.client.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.usfirst.frc.team25.scouting.data.AllianceReport;
import org.usfirst.frc.team25.scouting.data.BlueAlliance;
import org.usfirst.frc.team25.scouting.data.EventReport;
import org.usfirst.frc.team25.scouting.data.FileManager;
import org.usfirst.frc.team25.scouting.data.models.ScoutEntry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainController {

    @FXML
    private Button chooseDataFolderButton, generateFilesButton, downloadDataButton, displayReportButton;
    @FXML
    private TextArea statusTextBox;
    @FXML
    private Text dataDirectoryDisplay;
    @FXML
    private CheckBox combineJson, generatePicklists, generateCsv, fixErrors, backupJson, generatePredictions;
    @FXML
    private RadioButton teamBasedReport, allianceBasedReport, teamEventsDownload, eventDownload, matchBasedReport;
    @FXML
    private TextField analysisTeamOne, analysisTeamTwo, analysisTeamThree, teamNumEventCode, analysisOppTeamOne,
            analysisOppTeamTwo, analysisOppTeamThree, analysisMatchNum;

    private EventReport eventReport;
    private ArrayList<File> jsonFileList;
    private String eventName;

    private File currentDataDirectory, teamNameList;

    public void initialize() {

        addStatus("Client opened!\n\nSelect data folder");

        ToggleGroup reportGenerationGroup = new ToggleGroup();
        teamBasedReport.setToggleGroup(reportGenerationGroup);
        allianceBasedReport.setToggleGroup(reportGenerationGroup);
        matchBasedReport.setToggleGroup(reportGenerationGroup);

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

        final TextField[] allianceBasedGroup = new TextField[]{analysisTeamTwo, analysisTeamThree};
        final TextField[] matchBasedGroup = new TextField[]{analysisOppTeamOne, analysisOppTeamTwo,
                analysisOppTeamThree, analysisMatchNum};

        allianceBasedReport.setOnAction(event -> {
            enableTextFieldGroup(allianceBasedGroup, allianceBasedReport.isSelected() || matchBasedReport.isSelected());
            enableTextFieldGroup(matchBasedGroup, matchBasedReport.isSelected());
        });

        teamBasedReport.setOnAction(event -> {
            enableTextFieldGroup(allianceBasedGroup, allianceBasedReport.isSelected() || matchBasedReport.isSelected());
            enableTextFieldGroup(matchBasedGroup, matchBasedReport.isSelected());
        });

        matchBasedReport.setOnAction(event -> {
            enableTextFieldGroup(allianceBasedGroup, allianceBasedReport.isSelected() || matchBasedReport.isSelected());
            enableTextFieldGroup(matchBasedGroup, matchBasedReport.isSelected());
        });

        generateFilesButton.setOnAction(event -> {
            processData();
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
            displayAggregateReport();
        });
    }

    private void enableTextFieldGroup(TextField[] textFields, boolean enable) {
        if (enable) {
            for (TextField textField : textFields) {
                textField.setDisable(false);
            }
        } else {
            for (TextField textField : textFields) {
                textField.setDisable(true);
                textField.setText("");
            }
        }
    }

    private void displayAggregateReport() {
        if (eventReport == null) {
            retrieveEventReport();
        }

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


        } else if (allianceBasedReport.isSelected()) {
            int teamOne, teamTwo, teamThree;

            try {
                teamOne = Integer.parseInt(analysisTeamOne.getText());
                teamTwo = Integer.parseInt(analysisTeamTwo.getText());
                teamThree = Integer.parseInt(analysisTeamThree.getText());
                if (!eventReport.isTeamPlaying(teamOne) || !eventReport.isTeamPlaying(teamTwo) || !eventReport.isTeamPlaying(teamThree)) {
                    addStatus("Invalid team number(s) for event " + eventName + ". Please try again.");
                } else {
                    AllianceReport allianceReport = eventReport.getAllianceReport(new int[]{teamOne, teamTwo,
                            teamThree});
                    addStatus(allianceReport.getQuickAllianceReport());

                }
            } catch (NumberFormatException e) {
                addStatus("Invalid or missing team number(s). Please try again.");

            }

        } else {
            AllianceReport[] alliances = null;
            try {
                if (!analysisMatchNum.getText().isEmpty()) {
                    int matchNum = Integer.parseInt(analysisMatchNum.getText());
                    alliances = eventReport.getAlliancesInMatch(matchNum);
                }
            } catch (NumberFormatException | IOException e) {
                addStatus("Match schedule not found or invalid match number, trying team numbers...");
            }

            try {

                if (alliances == null) {

                }

                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml" +
                        "/match_predictions.fxml"));

                Stage stage = new Stage();

                stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/team_25_logo.png")));
                stage.setTitle("Match Predictions");
                stage.setScene(new Scene(loader.load(), 968, 483));
                MatchPredictionController controller = loader.getController();
                controller.initialize(alliances);
                stage.setResizable(false);
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processData() {

        String status = "";

        retrieveEventReport();

        if (backupJson.isSelected()) {
            FileManager.createBackup(jsonFileList, currentDataDirectory);
            status += "\nBackup JSON files created";
        }

        if (fixErrors.isSelected()) {
            eventReport.generateInaccuracyList(currentDataDirectory);
            if (eventReport.fixInaccuraciesTBA()) {
                status += "\nInaccuracies fixed and inaccuracy list generated";
            } else {
                status += "\nNo inaccuracies found or Internet unavailable";
            }
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


        if (status.isEmpty()) {
            addStatus("Please select data processing options!");
        } else {
            addStatus("Data processing for event " + eventName + " successful:\n" + status);
        }

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

        eventReport.processEntries();
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
