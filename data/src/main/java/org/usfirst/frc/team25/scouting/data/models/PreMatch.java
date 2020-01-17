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

    private final String scoutName;
    private final int matchNum;
    private final String scoutPos;
    private final int teamNum;
    private final boolean robotNoShow;
    private final int startingLevel;
    private final String startingPos;
    private final String startingGamePiece;

 }
