# FRC Scouting Client [![Build Status](https://travis-ci.org/spencerng/Scouting-Client-FX.svg?branch=master)](https://travis-ci.org/spencerng/Scouting-Client-FX)

A Java-based desktop client that processes scouting data from JSON files and pulls data from The Blue Alliance. To be used with the [Android scouting app](https://github.com/spencerng/Scouting-App).

Built with JavaFX for the 2019 FRC season and beyond.

Features:
 
 * Basic GUI to select input/output folders
 * Merges all scouting JSON files into a master file JSON file for export
   * Generates a human-readable CSV spreadsheet for backup
 * Analyzes the performance, consistency, and other metrics for each team
   * Exports results as a CSV spreadsheet and JSON for visualization in Tableau
 * Generates picklists based on scout ranking and pre-determined metrics
 * Downloads a spreadsheet of teams and matches for any FRC event from The Blue Alliance
   * Files used for verification in the scouting app
 * Downloads all files for Team 25's events for the current season

 
## Usage

**Downloading Data from TBA**

* Click Download
* Select the output folder - all downloaded files will go there.
* Type the [event key](https://www.thebluealliance.com/apidocs#event-model) for the event
  * Alternatively, type `25` to download data for all events Raider Robotix is attending for the current year
  * `25+YYYY` may also be used for Raider Robotix events in past years
* Files will be downloaded
  * `Teams - <event>.csv` - a non-readable comma separated list of teams for that event
  * `TeamNames - <event>.csv` - a readable spreadsheet of teams, with numbers and names, attending that event
  * `Matches - <event>.csv` - a readable spreadsheet of matches. Format is `<match number>, <red 1>, <red 2>, <red 3>, <blue 1>, <blue 2>, <blue 3>` for each row. May be empty if data is not available

**Processing Scouting Data**

* Place all files named `Data - <scouting position> - <current event>.json` in a folder called `<current event>`. The data file `TeamNames - <current event>.csv` downloaded by the client should also be there.
  * It is essential that the team names file is not altered.
* Press "Start" and select the folder named `<current event>`.
* Original JSON files should be deleted and the following should be generated:
  * `Data - All - <current event>.json` - contents of all previous JSON files contained in a JSON array
  * `Data - All - <current event>.csv` - raw metrics from each `ScoutEntry` in a spreadsheet, one row per entry
  * `TeamReports - <current event>.json` - analyzed derived metrics of each team (i.e. average gears per match). Contains data sets with raw numbers for some metrics (i.e. an array of gears with the values collected from each match)
 * `TeamReports - <current event>.csv` - a readable spreadsheet with similar contents to the JSON file
   * A key difference - this contains frequent comments concatenated together for easy readability and all other comments
