package org.usfirst.frc.team25.scouting.data.models;


import jdk.jfr.DataAmount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Container holding data from the tele-operated period
 * Includes endgame data
 */
@Data
@Accessors(fluent = true)
public class TeleOp {

    private int cellsScoredBottom;
    private int cellsScoredInner;
    private int cellsScoredOuter;
    private int cellPickupRpoint;
    private int cellPickupTrench;
    private int cellsDropped;

    private boolean rotationControl;
    private boolean positionControl;

    private boolean attemptHang;
    private boolean successHang;
    private boolean hangAssisted;
    private int assistingClimbTeamNum;
    private int numPartnerClimbAssists;

}
