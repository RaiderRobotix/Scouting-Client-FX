package org.usfirst.frc.team25.scouting.data.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * General information about a match and scout before it begins
 */
@Data
@Accessors(fluent = true)
public class PreMatch {

    private String scoutName;
    private int matchNum;
    private String scoutPos;
    private int teamNum;
    private boolean robotNoShow;
    private int startingLevel;
    private String startingPos;
    private String startingGamePiece;

 }
