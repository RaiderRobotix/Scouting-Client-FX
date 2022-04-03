package org.usfirst.frc.team25.scouting.client.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.usfirst.frc.team25.scouting.data.*;
import org.usfirst.frc.team25.scouting.data.models.ScoutEntry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Controller for main.fxml
 */
public class MainController {

    @FXML
    private Button chooseDataFolderButton, generateFilesButton, pullFilesButton, downloadDataButton, displayReportButton;
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

    private TextField[] allianceBasedGroup, matchBasedGroup;

    private EventReport eventReport;
    private String eventName;

    private ArrayList<File> jsonFileList;
    private File currentDataDirectory;

    /**
     * Assigns functionality to UI buttons
     */
    public void initialize() {

        addStatus("Client opened!\n\nSelect data folder");

        // ToggleGroups allow only one RadioButton in that group to be selected at a time
        RadioButton[] reportButtons = new RadioButton[]{teamBasedReport, allianceBasedReport, matchBasedReport};
        addToToggleGroup(reportButtons, new ToggleGroup());
        addToToggleGroup(new RadioButton[]{teamEventsDownload, eventDownload}, new ToggleGroup());

        dataDirectoryDisplay.setText("");

        chooseDataFolderButton.setOnAction(event -> chooseDataFolder());
        pullFilesButton.setOnAction(event -> PullFilesButton());
        generateFilesButton.setOnAction(event -> generateFiles());
        displayReportButton.setOnAction(event -> displayAggregateReport());
        downloadDataButton.setOnAction(event -> tryDownloadData());

        // Manually toggle and enable/disable TextFields, depending on the selected RadioButton
        allianceBasedGroup = new TextField[]{analysisTeamTwo, analysisTeamThree};
        matchBasedGroup = new TextField[]{analysisOppTeamOne, analysisOppTeamTwo, analysisOppTeamThree,
                analysisMatchNum};

        for (RadioButton button : reportButtons) {
            button.setOnAction(event -> {
                enableTextFieldGroup(allianceBasedGroup,
                        allianceBasedReport.isSelected() || matchBasedReport.isSelected());
                enableTextFieldGroup(matchBasedGroup, matchBasedReport.isSelected());
            });
        }
    }

    /**
     * Assigns a ToggleGroup to a RadioButton array
     *
     * @param buttons     Array of RadioButtons that are assigned a ToggleGroup
     * @param toggleGroup ToggleGroup to be assigned
     */
    private void addToToggleGroup(RadioButton[] buttons, ToggleGroup toggleGroup) {
        for (RadioButton button : buttons) {
            button.setToggleGroup(toggleGroup);
        }
    }

    /**
     * Enables or disables an array of TextFields, clearing their contents if disabled
     *
     * @param textFields Array of TextFields to be modified
     * @param enable     True if the text fields should be enabled, false if they should be disabled
     */
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

    /**
     * Adds a status display to the user-facing text box, with a separator between statuses
     *
     * @param message Text to display to users
     */
    private void addStatus(String message) {
        statusTextBox.setText(message + "\n====================\n" + statusTextBox.getText());
    }

    /**
     * Creates a popup window to select a data folder, then retrieves event data if a selection is made
     */
    private void chooseDataFolder() {
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
            pullFilesButton.setDisable(false);

        }
        retrieveEventReport();
    }

    /**
     * Retrieves JSON data files from the selected data directory and converts them into an EventReport for data
     * processing
     */
    private void retrieveEventReport() {

        jsonFileList = FileManager.getDataFiles(currentDataDirectory);

        if (jsonFileList.size() == 0) {
            addStatus("No JSON data files found in " + currentDataDirectory.getAbsolutePath() +
                    ".\nPlease select another directory.");
            return;
        }

        // This assumes the JSON files have not been renamed from the app or client output
        eventName = jsonFileList.get(0).getName().split(FileManager.FILE_EXTENSION_REGEX)[0].split(" - ")[2];

        ArrayList<ScoutEntry> scoutEntries = FileManager.deserializeData(jsonFileList);
        eventReport = new EventReport(scoutEntries, eventName, currentDataDirectory);

        File teamNameList = FileManager.getTeamNameList(currentDataDirectory);
        if (teamNameList != null) {
            eventReport.setTeamNameList(teamNameList);
        }

        eventReport.processEntries();
    }

    private void PullFilesButton() {
        String status = "";
        retrieveEventReport();
        if (FileManager.pullFiles(currentDataDirectory)){
            status += "\nGrabbed Scouting data";
        } else {
            status += "\nFailed grabbing Scouting data";
        }
        addStatus(status);
    }

    /**
     * Generates picklists, JSON files, inaccuracy lists, etc. in accordance to the user-checked boxes
     */
    private void generateFiles() {

        String status = "";
        retrieveEventReport();

        if (FileManager.pullFiles(currentDataDirectory)){
            status += "\nGrabbed Scouting data";
        } else {
            status += "\nFailed grabbing Scouting data";
        }

        if (backupJson.isSelected()) {
            if (FileManager.createBackup(jsonFileList, currentDataDirectory)) {
                status += "\nBackup JSON files created";
            } else {
                status += "\nJSON file backup failed";
            }
        }

//        if (fixErrors.isSelected()) {
//            InaccuracyFixer fixer = new InaccuracyFixer(eventReport);
//            if (fixer.fixInaccuraciesTBA()) {
//                fixer.saveInaccuracyList(currentDataDirectory);
//                status += "\nInaccuracies fixed and inaccuracy list generated";
//            } else {
//                status += "\nNo inaccuracies found or Internet unavailable";
//            }
//        }

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
                status += "\nRaw data spreadsheet failed to generate. Is the CSV file currently open?";
            }
        }
//
        if (generatePicklists.isSelected()) {
            eventReport.generatePicklists(currentDataDirectory, new int[]{25});
            status += "\nPicklists generated";
        }
//
//        if (generatePredictions.isSelected()) {
//            if (eventReport.generateMatchPredictions(currentDataDirectory)) {
//                status += "\nFuture match predictions generated";
//            } else {
//                status += "\nMatch prediction generation failed";
//            }
//        }

        if (status.isEmpty()) {
            addStatus("Please select data processing options!");
        } else {
            addStatus("Data processing for event " + eventName + " successful:\n" + status);
        }
    }

    /**
     * Displays a text-based alliance or team report, or a match prediction screen, depending on selected options
     */
    private void displayAggregateReport() {
        if (eventReport == null) {
            retrieveEventReport();
        }

        if (teamBasedReport.isSelected()) {
            try {
                int teamNum = Integer.parseInt(analysisTeamOne.getText());
                if (!eventReport.isTeamPlaying(teamNum)) {
                    addStatus("Invalid team number for event " + eventName + ". Please try again.");
                } else {
                    addStatus(eventReport.getTeamReport(teamNum).getQuickStatus());
                }
            } catch (NumberFormatException e) {
                addStatus("Invalid or missing team number. Please try again.");
            }
        } else if (allianceBasedReport.isSelected()) {
            int[] alliance = new int[3];

            try {
                alliance[0] = Integer.parseInt(analysisTeamOne.getText());
                alliance[1] = Integer.parseInt(analysisTeamTwo.getText());
                alliance[2] = Integer.parseInt(analysisTeamThree.getText());

                for (int teamNum : alliance) {
                    if (!eventReport.isTeamPlaying(teamNum)) {
                        addStatus("Invalid team number(s) for event " + eventName + ". Please try again.");
                        return;
                    }
                }
//
//                AllianceReport allianceReport = eventReport.getAllianceReport(alliance);
//                addStatus(allianceReport.getQuickAllianceReport());

            } catch (NumberFormatException e) {
                addStatus("Invalid or missing team number(s). Please try again.");
            }
        } else {
            AllianceReport[] alliances = null;
            int matchNum = 0;
            try {
                if (!analysisMatchNum.getText().isEmpty()) {
                    matchNum = Integer.parseInt(analysisMatchNum.getText());
                    alliances = eventReport.getAlliancesInMatch(matchNum);
                }
            } catch (NumberFormatException | IOException e) {
                addStatus("Match schedule not found or invalid match number, trying team numbers...");
            }

            try {
                if (alliances == null) {

                    int[] redAlliance = new int[3];
                    redAlliance[0] = Integer.parseInt(analysisTeamOne.getText());
                    redAlliance[1] = Integer.parseInt(analysisTeamTwo.getText());
                    redAlliance[2] = Integer.parseInt(analysisTeamThree.getText());

                    int[] blueAlliance = new int[3];

                    for (int i = 0; i < 3; i++) {
                        blueAlliance[i] = Integer.parseInt(matchBasedGroup[i].getText());
                    }

//                    alliances = new AllianceReport[]{eventReport.getAllianceReport(redAlliance),
//                            eventReport.getAllianceReport(blueAlliance)};
                }

                displayPredictions(matchNum, alliances);
            } catch (Exception e) {
                e.printStackTrace();
                addStatus("Invalid or empty team number(s). Please try again.");
            }
        }
    }

    /**
     * Generates a match prediction screen based on the specified parameters
     *
     * @param matchNum  Match number to display on the screen
     * @param alliances Array of two AllianceReports containing data on the match's red and blue alliances
     * @throws IOException If the logo or FXML assets are not present
     */
    private void displayPredictions(int matchNum, AllianceReport[] alliances) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml" +
                "/match_predictions.fxml"));

        Stage stage = new Stage();
        Scene scene = new Scene(loader.load(), 968, 483);

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/team_25_logo.png")));
        stage.setTitle("Match Predictions");
        stage.setScene(scene);
        stage.setResizable(false);

        MatchPredictionController controller = loader.getController();

        if (matchNum != 0) {
            controller.setMatchNumber(matchNum);
        }

        controller.setEventKey(eventName);
        controller.setScene(scene);
        controller.initialize(alliances[0], alliances[1]);

        stage.show();
    }

    /**
     * Attempts to download event data from The Blue Alliance based on the parameters in the UI
     */
    private void tryDownloadData() {
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
    }

}
