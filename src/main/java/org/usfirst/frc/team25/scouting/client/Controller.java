package org.usfirst.frc.team25.scouting.client;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class Controller {

    @FXML
    private Button chooseDataFolderButton;

    public void initialize() {


        chooseDataFolderButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File selectedDirectory = directoryChooser.showDialog(chooseDataFolderButton.getScene().getWindow());

                if (selectedDirectory == null) {
                    //No Directory selected
                } else {
                    System.out.println(selectedDirectory.getAbsolutePath());
                }
            }
        });
    }

}
