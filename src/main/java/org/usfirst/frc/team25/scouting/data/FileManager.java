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
 *
 * @author sng
 */
public class FileManager {

    public static final String FILE_EXTENSION_REGEX = "\\.(?=[^.]+$)";


    /**
     * Writes a string to a output target file
     *
     * @param file         File object containing the output filename and directory
     * @param fileContents String contents of output file
     */
    public static void outputFile(File file, String fileContents) {
        try {
            PrintWriter outputFile = new PrintWriter(file);
            outputFile.write(fileContents);
            outputFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes a string to a output target file
     *
     * @param fileName     File name of output file
     * @param extension    File extension, without the dot ('.')
     * @param fileContents String contents of output file
     */
    public static void outputFile(String fileName, String extension, String fileContents) throws FileNotFoundException {
        PrintWriter outputFile = new PrintWriter(fileName + "." + extension);
        outputFile.write(fileContents);
        outputFile.close();

    }



    /**
     * Gets list of files from directory
     *
     * @param directory Directory that filenames are read from
     * @return Array of Files
     */
    public static File[] getFilesFromDirectory(File directory) {
        return directory.listFiles();
    }

    /**
     * Executes a command line statement to generate a LaTeX file
     * Deletes excess files as well
     *
     * @param outputDirectory Directory for the output PDF file
     * @param filePath        Path to TeX source file
     */
    public static void compilePdfLatex(String outputDirectory, String filePath) {
        try {
            Runtime.getRuntime().exec("pdflatex -output-directory=" + outputDirectory + " " + filePath);
            String[] directories = filePath.split("\\\\");
            String fileName = directories[directories.length - 1].split(FILE_EXTENSION_REGEX)[0];

            Thread.sleep(500); //Needed to register files into filesystem

            deleteFile(outputDirectory + "\\" + fileName + ".aux");
            deleteFile(outputDirectory + "\\" + fileName + ".log");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private static void deleteFile(String filePath) {
        try {
            File file = new File(filePath);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Deserializes and combines contents of JSON files exported by the Android scouting app
     *
     * @param fileNames List of JSON File objects to be parsed
     * @return ArrayList of all ScoutEntrys in the JSON files
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

    public static ArrayList<Match> deserializeScoreBreakdown(File fileName) {
        Gson gson = new Gson();
        return gson.fromJson(FileManager.getFileString(fileName),
                new TypeToken<ArrayList<Match>>() {
                }.getType());
    }

    public static File getTeamNameList(File directory) {

        for (File file : FileManager.getFilesFromDirectory(directory)) {
            String fileName = file.getName();
            try {
                if (fileName.split(FILE_EXTENSION_REGEX)[1].equals("csv") && fileName.contains("TeamNames")) {
                    return file;
                }
            } catch (Exception e) {

            }

        }

        return null;
    }

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

    public static ArrayList<File> getDataFiles(File directory) {

        ArrayList<File> jsonFileList = new ArrayList<>();

        for (File file : FileManager.getFilesFromDirectory(directory)) {
            String fileName = file.getName();
            try {
                if (fileName.split(FILE_EXTENSION_REGEX)[1].equals("json") && fileName.contains("Data")) {
                    jsonFileList.add(file);
                }
            } catch (ArrayIndexOutOfBoundsException e) {

            }
        }

        return jsonFileList;
    }

    public static boolean createBackup(ArrayList<File> files, File rootDirectory) {

        File backupDirectory = new File(rootDirectory.getAbsolutePath() + "/backup");
        if (!backupDirectory.exists()) {
            backupDirectory.mkdirs();
        }

        for (File file : files) {
            String newFileName = file.getName().split(FILE_EXTENSION_REGEX)[0] + " - Backup " + new SimpleDateFormat(
                    "MM-dd HH-mm").format(new Date()) + "." + file.getName().split(FILE_EXTENSION_REGEX)[1];
            File backupFile = new File(backupDirectory.getAbsolutePath() + "/" + newFileName);
            outputFile(backupFile, getFileString(file));
        }

        return true;
    }
}
