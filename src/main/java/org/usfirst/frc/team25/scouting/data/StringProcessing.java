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
        StringBuilder sentenceCaseString = new StringBuilder();

        for (int i = 0; i < camelCaseString.length(); i++) {
            if (Character.isUpperCase(camelCaseString.charAt(i))) {
                sentenceCaseString.append(" ").append(Character.toLowerCase(camelCaseString.charAt(i)));
            } else {
                sentenceCaseString.append(camelCaseString.charAt(i));
            }
        }

        return sentenceCaseString.toString();
    }


}
