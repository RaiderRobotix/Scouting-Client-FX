package org.usfirst.frc.team25.scouting.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thebluealliance.api.v3.models.Match;
import org.usfirst.frc.team25.scouting.data.models.ScoutEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Class of static methods used for file I/O
 */
public class FileManager {

    /**
     * String containing a regular expression that separates a file name with its extension
     */
    public static final String FILE_EXTENSION_REGEX = "\\.(?=[^.]+$)";

    /**
     * Writes a string to an output target file
     *
     * @param rootDirectory File object containing the output root directory
     * @param fileName      String name of the output file
     * @param extension     String extension of the output file, without the period
     * @param fileContents  String contents of the output file
     * @throws FileNotFoundException if the output directory is invalid
     */
    public static void outputFile(File rootDirectory, String fileName, String extension, String fileContents) throws FileNotFoundException {
        outputFile(new File(rootDirectory.getAbsolutePath() + "//" + fileName + "." + extension), fileContents);
    }

    /**
     * Writes a string to an output target file
     *
     * @param file         File object containing the output filename and directory
     * @param fileContents String contents of output file
     * @throws FileNotFoundException if the file object does not contain a valid location
     */
    public static void outputFile(File file, String fileContents) throws FileNotFoundException {
        PrintWriter outputFile = new PrintWriter(file);
        outputFile.write(fileContents);
        outputFile.close();
    }

    /**
     * Deserializes and combines contents of JSON files exported by the Android scouting app
     *
     * @param fileNames List of JSON File objects to be parsed
     * @return ArrayList of all ScoutEntries in the JSON files
     */
    public static ArrayList<ScoutEntry> deserializeData(ArrayList<File> fileNames) {
        ArrayList<ScoutEntry> allEntries = new ArrayList<>();

        for (File file : fileNames) {
            ArrayList<ScoutEntry> fileEntries = new Gson().fromJson(getFileString(file),
                    new TypeToken<ArrayList<ScoutEntry>>() {
                    }.getType());
            allEntries.addAll(fileEntries);
        }

        return allEntries;
    }

    /**
     * Gets the contents of a file as a string
     *
     * @param file File object of the file to be parsed
     * @return Contents of the file as a string
     */
    public static String getFileString(File file) {
        String contents = "";
        try {
            contents = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    /**
     * Deserializes a score breakdown JSON file downloaded from TBA into an array of TBA Match objects
     *
     * @param fileName File object containing the score breakdown JSON file
     * @return Array of TBA Match objects containing individual score breakdowns
     */
    public static ArrayList<Match> deserializeScoreBreakdown(File fileName) {
        Gson gson = new Gson();
        return gson.fromJson(FileManager.getFileString(fileName),
                new TypeToken<ArrayList<Match>>() {
                }.getType());
    }

    /**
     * Gets the team name list from a data directory
     *
     * @param directory Directory containing all data files for an event
     * @return File object containing the team name list (if downloaded) for the event; null if not downloaded
     */
    public static File getTeamNameList(File directory) {
        for (File file : FileManager.getFilesFromDirectory(directory)) {
            String fileName = file.getName();
            try {
                if (fileName.split(FILE_EXTENSION_REGEX)[1].equals("csv") && fileName.contains("TeamNames")) {
                    return file;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Gets list of files from a directory
     *
     * @param directory Directory that filenames are read from
     * @return Array of File objects from the directory
     */
    public static File[] getFilesFromDirectory(File directory) {
        return directory.listFiles();
    }

    /**
     * Gets the match schedule from a data directory
     *
     * @param directory Directory containing all data files for an event
     * @return File object containing the match schedule (if downloaded) for the event; null if not downloaded
     */
    public static File getMatchList(File directory) {
        for (File file : FileManager.getFilesFromDirectory(directory)) {
            String fileName = file.getName();
            try {
                if (fileName.split(FILE_EXTENSION_REGEX)[1].equals("csv") && fileName.contains("Matches")) {
                    return file;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Deletes all JSON data files from a data directory that come from individual scouting apps, only retaining the
     * combined JSON file generated by the client
     *
     * @param directory Directory containing all data files for an event
     * @return True if the operation was successful, false otherwise
     */
    public static boolean deleteIndividualDataFiles(File directory) {
        for (File file : getDataFiles(directory)) {
            if (!file.getName().contains("All")) {
                if (!file.delete()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Gets the JSON scouting entry data files from a data directory
     *
     * @param directory Directory containing all data files for an event
     * @return Array of file objects containing data JSON files from the directory
     */
    public static ArrayList<File> getDataFiles(File directory) {
        ArrayList<File> jsonFileList = new ArrayList<>();

        for (File file : FileManager.getFilesFromDirectory(directory)) {
            String fileName = file.getName();
            try {
                if (fileName.split(FILE_EXTENSION_REGEX)[1].equals("json") && fileName.contains("Data")) {
                    jsonFileList.add(file);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        return jsonFileList;
    }

    /**
     * Creates a timestamped backup of all specified files in a backup folder
     *
     * @param files         ArrayList of file objects to be backed up
     * @param rootDirectory Data directory that the backup directory is created in
     * @return True if the operation is successful, false otherwise
     */
    public static boolean createBackup(ArrayList<File> files, File rootDirectory) {

        File backupDirectory = new File(rootDirectory.getAbsolutePath() + "/backup");
        if (!backupDirectory.exists()) {
            backupDirectory.mkdirs();
        }

        for (File file : files) {
            String dateString = new SimpleDateFormat("MM-dd HH-mm").format(new Date());
            String originalFileName = file.getName().split(FILE_EXTENSION_REGEX)[0];
            String newFileName =
                    originalFileName + " - Backup " + dateString + "." + file.getName().split(FILE_EXTENSION_REGEX)[1];

            File backupFile = new File(backupDirectory.getAbsolutePath() + "/" + newFileName);
            try {
                outputFile(backupFile, getFileString(file));
            } catch (FileNotFoundException e) {
                return false;
            }
        }
        return true;
    }
}
