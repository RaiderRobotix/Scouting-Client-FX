package org.usfirst.frc.team25.scouting.data.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class ScoutEntry implements Serializable {

    private final PreMatch preMatch;
    private final Autonomous sandstorm;
    private final TeleOp teleOp;
    private final PostMatch postMatch;

    private transient int totalCells;
    private transient int cellsDropped;
    private transient int cycles;
    private transient int pointContribution;


    public void calculateDerivedStats() {
    }
}
