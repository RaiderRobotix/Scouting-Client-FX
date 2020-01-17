package org.usfirst.frc.team25.scouting.data.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@AllArgsConstructor
@Accessors(fluent = true)
public class Autonomous {

    private final int cellsScoredBottom;
    private final int cellsScoredInner;
    private final int cellsScoredOuter;
    private final int cellPickupRpoint;
    private final int cellPickupTrench;
    private final int cellsDropped;

    private final boolean crossInitLine;
    private final boolean crossOpponentSector;

}

