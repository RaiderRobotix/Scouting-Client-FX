package org.usfirst.frc.team25.scouting.data;

import com.google.gson.Gson;
import com.thebluealliance.api.v3.TBA;
import com.thebluealliance.api.v3.models.Event;
import com.thebluealliance.api.v3.models.Match;
import com.thebluealliance.api.v3.models.Team;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;


/**
 * Class of static methods used to interface with online data from The Blue Alliance
 *
 * @author sng
 */
public class BlueAlliance {

    private static TBA TBA;

    public static void initializeApi(Class c) throws IOException {


        String apiKey = IOUtils.toString(c.getClassLoader().getResourceAsStream("apikey/secret.txt"), "utf-8");
        TBA = new TBA(apiKey);

    }

    /**
     * Downloads all data from events that Team 25 is playing in for the current calendar year
     *
     * @param outputFolder Output folder for downloaded files
     */
    public static String downloadTeamEvents(File outputFolder, int teamNum) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String response = "Downloading event data for Team " + teamNum + " in " + year + "\n";
        try {
            for (Event event : TBA.teamRequest.getEvents(teamNum, year)) {
                response += "\n" + downloadEventTeamData(outputFolder, event.getKey()) + "\n";
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            response = teamNum + " is an invalid team number.\nPlease try again.";
        }

        return response;

    }

    /**
     * Downloads the team lists and match lists for an FRC event
     * Generates appropriate file names from TBA based on the short_name of the event
     *
     * @param outputFolder Output folder for downloaded files
     * @param eventCode    Fully qualified event key
     * @return Status text of the download
     */
    public static String downloadEventTeamData(File outputFolder, String eventCode) {

        String response = "Successfully downloaded data for event " + eventCode;

        try {
            String eventShortName = TBA.eventRequest.getEvent(eventCode).getKey();
            if (exportSimpleTeamList(eventCode, outputFolder.getAbsolutePath() + "/Teams - " + eventShortName)) {
                response += "\nSimple team list downloaded";
            }
            if (exportTeamList(eventCode, outputFolder.getAbsolutePath() + "/TeamNames - " + eventShortName)) {
                response += "\nTeam names downloaded";
            }
            if (exportMatchList(eventCode, outputFolder.getAbsolutePath() + "/Matches - " + eventShortName)) {
                response += "\nMatch schedule downloaded";
            }

        } catch (Exception e) {
            response = "Data download for event " + eventCode + " failed.\nInvalid event key or no Internet access" +
                    ".\nPlease try again.";
        }

        return response;
    }

    /**
     * Exports a simple comma delimited sorted file of teams playing at an event.
     * Output file intended to be read by Scouting App
     *
     * @param eventCode Fully qualified event key, i.e. "2016pahat" for Hatboro-Horsham in 2016
     * @param fileName  File name of output file, without extension
     */
    public static boolean exportSimpleTeamList(String eventCode, String fileName) throws FileNotFoundException {


        StringBuilder teamList = new StringBuilder();
        ArrayList<Team> teams;
        try {
            teams = SortersFilters.sortByTeamNum(new ArrayList<>(Arrays.asList(TBA.eventRequest.getTeams(eventCode))));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        for (Team team : teams) {
            teamList.append(team.getTeamNumber()).append(",");
        }
        StringBuilder output = new StringBuilder(teamList.toString());
        output.setCharAt(output.length() - 1, ' ');

        if (!teamList.toString().isEmpty()) {
            FileManager.outputFile(fileName, "csv", teamList.toString());
            return true;
        }

        return false;
    }

    /**
     * Exports a comma and line break delimited file of team numbers and names at an event.
     * Each line contains a comma delimited pair of team number and team nickname.
     *
     * @param eventCode Fully qualified event key, i.e. "2016pahat" for Hatboro-Horsham in 2016
     * @param fileName  File name of output file, without extension
     */

    private static boolean exportTeamList(String eventCode, String fileName) throws FileNotFoundException {

        StringBuilder teamList = new StringBuilder();

        try {
            for (Team team :
                    SortersFilters.sortByTeamNum(new ArrayList<>(Arrays.asList(TBA.eventRequest.getTeams(eventCode))))) {
                teamList.append(team.getTeamNumber()).append(",").append(team.getNickname()).append(",\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (!teamList.toString().isEmpty()) {
            FileManager.outputFile(fileName, "csv", teamList.toString());
            return true;
        }

        return false;

    }

    /**
     * Generates a file with list of teams playing in each match
     * Each line contains comma delimited match number, then team numbers for red alliance, then blue alliance.
     *
     * @param eventCode Fully qualified event key, i.e. "2016pahat" for Hatboro-Horsham in 2016
     * @param fileName  File name of output, without extension
     */
    private static boolean exportMatchList(String eventCode, String fileName) throws FileNotFoundException {
        StringBuilder matchList = new StringBuilder();
        try {
            for (Match match :
                    SortersFilters.sortByMatchNum(SortersFilters.filterQualification(new ArrayList<>(Arrays.asList(TBA.eventRequest.getMatches(eventCode)))))) {

                matchList.append(match.getMatchNumber()).append(",");
                for (int i = 0; i < 2; i++) //iterate through two alliances
                {
                    for (int j = 0; j < 3; j++) { //iterate through teams in alliance
                        if (i == 0) {
                            matchList.append(match.getRedAlliance().getTeamKeys()[j].split("frc")[1]).append(",");
                        } else {
                            matchList.append(match.getBlueAlliance().getTeamKeys()[j].split("frc")[1]).append(",");
                        }
                    }
                }
                matchList.append(",\n");


            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (!matchList.toString().isEmpty()) {
            FileManager.outputFile(fileName, "csv", matchList.toString());
            return true;
        }

        return false;

    }

    /**
     * Downloads all data from events that Team 25 is playing in for the specified year
     *
     * @param outputFolder Output folder for downloaded files
     */
    public static void downloadTeamEvents(File outputFolder, int year, int teamNum) {

        try {
            for (Event event : TBA.teamRequest.getEvents(teamNum, year)) {
                downloadEventTeamData(outputFolder, event.getKey());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static boolean downloadQualificationMatchData(String eventCode, File outputDirectory) throws IOException {
        ArrayList<Match> matches =
                SortersFilters.sortByMatchNum(SortersFilters.filterQualification(new ArrayList<>(Arrays.asList(TBA.eventRequest.getMatches(eventCode)))));

        if (matches.size() == 0) {
            return false;
        }

        Gson gson = new Gson();
        String jsonString = gson.toJson(matches);
        FileManager.outputFile(outputDirectory.getAbsolutePath() + "//ScoreBreakdown - " + eventCode, "json",
                jsonString);

        return true;
    }


}
