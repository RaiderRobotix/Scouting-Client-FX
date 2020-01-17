package org.usfirst.frc.team25.scouting.data.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Qualitative reflection on the robot's performance after a match
 * Not to be used for end game actions
 */
@Data
@Accessors(fluent = true)
public class PostMatch {

    private final int teamOneCompare;
    private final int teamTwoCompare;
    private final int pickNumber;
    private final char comparison;
    private final HashMap<String,Boolean> robotQuickCommentSelections = new HashMap<>();
    private transient String robotQuickCommentStr = createCommentStr();
    private String robotComment;
    private final String focus;

    String createCommentStr() {
        var ret = new StringBuilder(12*robotQuickCommentSelections.size());
        robotQuickCommentSelections().forEach(
                (k, v) -> { if (v) ret.append(k + "; "); }
        );
        return ret.toString();
    }

}