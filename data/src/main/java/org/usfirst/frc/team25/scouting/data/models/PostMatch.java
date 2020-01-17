package org.usfirst.frc.team25.scouting.data.models;

import lombok.Data;
import lombok.experimental.Accessors;

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
    private final String comparison;
    private final HashSet<String> robotQuickCommentSelections = new HashSet<>();
    private transient String robotQuickCommentStr = createCommentStr();
    private final String robotComment;
    private final String focus;

    String createCommentStr() {
        var ret = new StringBuilder(12*robotQuickCommentSelections.size());
        robotQuickCommentSelections().forEach(
                it -> ret.append(it + "; ")
        );
        return ret.toString();
    }

}