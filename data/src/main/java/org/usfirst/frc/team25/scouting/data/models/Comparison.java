package org.usfirst.frc.team25.scouting.data.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Class that holds a comparison between two teams
 */
@Data
public class Comparison {

    private final int lowerTeam;
    private final int higherTeam;
    private final char compareChar;

    public Comparison(int teamOne, int teamTwo, char comparator) {
        if (teamTwo > teamOne) {
            this.lowerTeam = teamOne;
            this.higherTeam = teamTwo;
            this.compareChar = comparator;
        } else {
            this.lowerTeam = teamTwo;
            this.higherTeam = teamOne;
            switch (comparator) {
                case '<':
                    this.compareChar = '>';
                    break;
                case '>':
                    this.compareChar = '<';
                    break;
                default:
                    this.compareChar = '=';
                    break;
            }
        }
    }

    /**
     * @param teamNum The team number that you're querying
     * @return True if the team is involved in the comparison, false otherwise
     */
    public boolean contains(int teamNum) {
        return lowerTeam == teamNum || higherTeam == teamNum;
    }

    /**
     * Compares two comparisons and sees if they contradict each other in a partially ordered list.
     * e.g. A&lt;B would contradict B&gt;A, but it would not contradict A=B
     *
     * @param secondComp The second comparison that you want to compare with this one
     * @return true if secondComp contradicts this one logically, false otherwise
     */
    public boolean contradicts(Comparison secondComp) {
        return this.getBetterTeam() == secondComp.getWorseTeam() && this.getWorseTeam() == secondComp.getBetterTeam() && !(this.compareChar == '=');
    }

    /**
     * @return The team number of the better team in the comparison; 0 if both teams are equal
     */
    public int getBetterTeam() {
        switch (compareChar) {
            case '<': return higherTeam;
            case '>': return lowerTeam;
            default: return 0;
        }
    }

    /**
     * @return The team number of the worse team in the comparison; 0 if both teams are equal
     */
    public int getWorseTeam() {
        switch (compareChar) {
            case '<':
                return lowerTeam;
            case '>':
                return higherTeam;
            default:
                return 0;
        }
    }


    /**
     * @return The string "A?B", where A is the lower team, ? is the comparator, and B is the higher team
     */
    public String toString() {
        return String.format("%d %s %d", lowerTeam, compareChar, higherTeam);
    }

}
