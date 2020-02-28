package org.usfirst.frc.team25.scouting.data;

import com.google.gson.Gson;
import com.raiderrobotix.BuildConfig;
import com.thebluealliance.api.v3.TBA;
import com.thebluealliance.api.v3.models.Event;
import com.thebluealliance.api.v3.models.Match;
import com.thebluealliance.api.v3.models.Team;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;


/**
 * Class of static methods used to download online data from The Blue Alliance
 */
public class BlueAlliance {
	
	private static final TBA TBA = new TBA(BuildConfig.TBA_API_KEY);
	
	/**
	 * Downloads all data from events that the specified team is playing in for the current calendar year
	 *
	 * @param outputFolder Output folder for downloaded files
	 * @param teamNum      Team number for the team whose event data will be downloaded
	 * @return Status string of the method
	 */
	@NonNull
	public static String downloadTeamEvents(File outputFolder, int teamNum) {
		int year = Calendar.getInstance().get(Calendar.YEAR);
		StringBuilder response = new StringBuilder("Downloading event data for Team " + teamNum + " in " + year +
			"\n");
		try {
			for (Event event : TBA.teamRequest.getEvents(teamNum, year)) {
				response.append("\n").append(downloadEventTeamData(outputFolder, event.getKey())).append("\n");
			}
		} catch (IOException | NullPointerException e) {
			e.printStackTrace();
			response = new StringBuilder(teamNum + " is an invalid team number.\nPlease try again.");
		}
		
		return response.toString();
	}
	
	/**
	 * Downloads the team lists and match lists for an FRC event
	 * Generates appropriate file names from TBA based on the short_name of the event
	 *
	 * @param outputFolder Output folder for downloaded files
	 * @param eventCode    Fully qualified event key
	 * @return Status text of the download
	 */
	@NotNull
	public static String downloadEventTeamData(File outputFolder, String eventCode) {
		
		String response = "Successfully downloaded data for event " + eventCode;
		
		try {
			String eventShortName = TBA.eventRequest.getEvent(eventCode).getKey();
			if (exportSimpleTeamList(eventCode, outputFolder, "Teams - " + eventShortName)) {
				response += "\nSimple team list downloaded";
			}
			if (exportTeamList(eventCode, outputFolder, "TeamNames - " + eventShortName)) {
				response += "\nTeam names downloaded";
			}
			if (exportMatchList(eventCode, outputFolder, "Matches - " + eventShortName)) {
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
	 * @param eventCode       Fully qualified event key, i.e. "2016pahat" for Hatboro-Horsham in 2016
	 * @param outputDirectory Location that the team list is saved in
	 * @param fileName        File name of output file, without extension
	 * @return True if the export was successful, false otherwise
	 * @throws FileNotFoundException if <code>outputDirectory</code> is invalid
	 */
	public static boolean exportSimpleTeamList(String eventCode, File outputDirectory, String fileName) throws FileNotFoundException {
		
		Team[] teams;
		try {
			teams = TBA.eventRequest.getTeams(eventCode);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (teams.length == 0) {
			return false;
		}
		
		Arrays.sort(teams, SortersFilters.byTeamNum);
		
		StringJoiner joiner = new StringJoiner(",");
		for (Team team : teams) {
			joiner.add(Integer.toString(team.getTeam_number()));
		}
		
		FileManager.outputFile(outputDirectory, fileName, "csv", joiner.toString());
		return true;
	}
	
	
	/**
	 * Exports a comma and line break delimited file of team numbers and names at an event.
	 * Each line contains a comma delimited pair of team number and team nickname.
	 *
	 * @param eventCode       Fully qualified event key, i.e. "2016pahat" for Hatboro-Horsham in 2016
	 * @param outputDirectory Location that the team list is saved in
	 * @param fileName        File name of output file, without extension
	 * @return True if the export was successful, false otherwise
	 * @throws FileNotFoundException if <code>outputDirectory</code> is invalid
	 */
	private static boolean exportTeamList(String eventCode, File outputDirectory, String fileName) throws FileNotFoundException {
		Team[] teams;
		try {
			teams = TBA.eventRequest.getTeams(eventCode);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (teams.length == 0) {
			return false;
		}
		Arrays.sort(teams, SortersFilters.byTeamNum);
		StringJoiner joiner = new StringJoiner(", \n");
		for (Team team : teams) {
			joiner.add(team.getTeam_number() + "," + team.getNickname());
		}
		
		FileManager.outputFile(outputDirectory, fileName, "csv", joiner.toString());
		return true;
	}
	
	/**
	 * Generates a file with list of teams playing in each match
	 * Each line contains comma delimited match number, then team numbers for red alliance, then blue alliance.
	 *
	 * @param eventCode       Fully qualified event key, i.e. "2016pahat" for Hatboro-Horsham in 2016
	 * @param outputDirectory Location that the team list is saved in
	 * @param fileName        File name of output file, without extension
	 * @return True if the export was successful, false otherwise
	 * @throws FileNotFoundException if <code>outputDirectory</code> is invalid
	 */
	private static boolean exportMatchList(String eventCode, File outputDirectory, String fileName) throws FileNotFoundException {
		val nlJoiner = new StringJoiner(",\n");
		final List<Match> matches;
		try {
			matches = Arrays.stream(TBA.eventRequest.getMatches(eventCode))
				.filter(m -> !m.getComp_level().equals("qm"))
				.sorted(SortersFilters.byMatchNum)
				.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		for (Match match : matches) {
			val commaJoiner = new StringJoiner(",");
			
			commaJoiner.add(Integer.toString(match.getMatch_number()));
			for (val team : match.getRedAlliance().getTeam_keys()) {
				val teamNum = team.split("frc")[1];
				commaJoiner.add(teamNum);
			}
			for (val team : match.getBlueAlliance().getTeam_keys()) {
				val teamNum = team.split("frc")[1];
				commaJoiner.add(teamNum);
			}
			
			nlJoiner.add(commaJoiner.toString());
			
			
		}
		FileManager.outputFile(outputDirectory, fileName, "csv", nlJoiner.toString());
		return true;
		
	}
	
	/**
	 * Downloads the qualification match game data for matches that have been played at the current event
	 *
	 * @param eventCode       Fully qualified event key, i.e. "2016pahat" for Hatboro-Horsham in 2016
	 * @param outputDirectory Output folder for downloaded file
	 * @throws IOException if <code>outputDirectory</code> does not exist
	 */
	public static void downloadQualificationMatchData(String eventCode, File outputDirectory) throws IOException {
		val matches = Arrays.stream(TBA.eventRequest.getMatches(eventCode))
			.filter(m -> !m.getComp_level().equals("qm"))
			.sorted(SortersFilters.byMatchNum)
			.collect(Collectors.toList());
		Gson gson = new Gson();
		String jsonString = gson.toJson(matches);
		FileManager.outputFile(outputDirectory, "ScoreBreakdown - " + eventCode, "json", jsonString);
	}
}
