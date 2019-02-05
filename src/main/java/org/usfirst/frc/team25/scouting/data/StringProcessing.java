package org.usfirst.frc.team25.scouting.data;

public class StringProcessing {

    /**
     * Helper method to prevent manual comments with commas or line breaks
     * from changing CSV format
     *
     * @param s String to be processed
     * @return String without commas
     */
    public static String removeCommasBreaks(String s) {
        StringBuilder newString = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ',' && s.charAt(i) != '\n') {
                newString.append(s.charAt(i));
            } else {
                newString.append("; ");
            }
        }
        return newString.toString();
    }


    /**
     * @param camelCaseString A string in lower camelCase
     * @return The string in sentence case, with space separations. E.g., "numRocketHatches" becomes "Num rocket
     * hatches"
     */
    public static String convertCamelToSentenceCase(String camelCaseString) {
        //TODO write this

        return camelCaseString;
    }


}
