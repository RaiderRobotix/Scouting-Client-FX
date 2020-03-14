package org.usfirst.frc.team25.scouting.data.models;

/**
 * Class that holds a comparison between two teams
 */
public class Comparison {

    private final int lowerTeam;
    private final int higherTeam;
    private final String compareChar;

    public Comparison(int teamOne, int teamTwo, String compareChar) {
        if (teamTwo > teamOne) {
            this.lowerTeam = teamOne;
            this.higherTeam = teamTwo;
            this.compareChar = compareChar;
        } else {
            this.lowerTeam = teamTwo;
            this.higherTeam = teamOne;
            if (compareChar.equals("<")) {
                this.compareChar = ">";
            } else if (compareChar.equals(">")) {
                this.compareChar = "<";
            } else {
                this.compareChar = "=";
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
     * @return The team number that's lower of the two teams in the comparison
     */
    public int getLowerTeam() {
        return lowerTeam;
    }

    /**
     * @return The team number that's higher of the two teams in the comparison
     */
    public int getHigherTeam() {
        return higherTeam;
    }

    /**
     * Compares two comparisons and sees if they contradict each other in a partially ordered list.
     * e.g. A&lt;B would contradict B&gt;A, but it would not contradict A=B
     *
     * @param secondComp The second comparison that you want to compare with this one
     * @return true if secondComp contradicts this one logically, false otherwise
     */
    public boolean contradicts(Comparison secondComp) {
        return this.getBetterTeam() == secondComp.getWorseTeam() && this.getWorseTeam() == secondComp.getBetterTeam() && !this.compareChar.equals("=");
    }

    /**
     * @return The team number of the better team in the comparison; 0 if both teams are equal
     */
    public int getBetterTeam() {
        if (compareChar.equals("<")) {
            return higherTeam;
        }
        if (compareChar.equals(">")) {
            return lowerTeam;
        }
        return 0;
    }

    /**
     * @return The team number of the worse team in the comparison; 0 if both teams are equal
     */
    public int getWorseTeam() {
        if (compareChar.equals("<")) {
            return lowerTeam;
        }
        if (compareChar.equals(">")) {
            return higherTeam;
        }
        return 0;
    }

    /**
     * Compares two comparisons and sees if they are identical
     *
     * @param secondComp The second comparison that you want to compare with this one
     * @return true if secondComp is equal, false otherwise
     */
    public boolean equals(Comparison secondComp) {
        return this.toString().equals(secondComp.toString());
    }

    /**
     * @return The string "A?B", where A is the lower team, ? is the comparator, and B is the higher team
     */
    public String toString() {
        return lowerTeam + compareChar + higherTeam;
    }

}
