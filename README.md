# FRC Scouting Client [![Build Status](https://travis-ci.com/RaiderRobotix/Scouting-Client-FX.svg?token=o8AnHy8tPTpEiAQq4tFv&branch=master)](https://travis-ci.com/RaiderRobotix/Scouting-Client-FX)

A Java-based desktop client that processes scouting data from JSON files and pulls data from The Blue Alliance. To be used with the [Android scouting app](https://github.com/spencerng/Scouting-App).

Built with JavaFX for the 2019 FRC season and beyond.

<img src="/assets/menu.png" width="600" align="center"/>

## Features
 
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

Begin by clicking "Choose Data Folder" and selecting the folder that you wish to use. This is where event data files will be generated and where JSON files from the scouting app are placed beforehand.

#### Downloading data from The Blue Alliance

For each event, a team list with team names, a match schedule, and a score breakdown of qualification matches played so far will be downloaded. Data not yet available on TBA will not be downloaded, and those files will not be created.

**All events**

1. Select the left radio button for "All events in current year"
2. Input the desired team number in the text box
3. Click "Download"

**Specific event**

1. Select the right radio button for "Individual event"
2. Input the fully-qualified event code in the text box (e.g. `2019njfla`)
3. Click "Download"

#### Processing scouting data

If the selected data folder contains JSON scout entries from the scouting client, it can be processed through the client. Options include:

-  **Combine JSON**: Combine individual scouting entry JSON files into one JSON file. Unless the backup option is selected, individual JSON files will then be deleted.
- **Backup JSON**: Saves the individual JSON files into a `/backup` folder before deleting them
- **Picklists**: Generates a picklist of teams based on three separate metrics (comparison, pick points, calculated point contributions)
- **CSV**: Generates a spreadsheet of raw metric values from each entry
- **Fix errors**: Corrects errors found in scouting entries with the help of The Blue Alliance
- **Predictions**: Generates a list of predictions for future qualification matches

Select the options you want, then press the "Generate Files" button. Those options will be processed in a logical order (e.g. fixing errors before other operations), and the appropriate files will be generated in the directory.


#### Generating aggregate reports

Text-based and graphical reports of aggregate stats can be viewed by selecting one of three radio buttons, inputting the desired team or match number(s), then pressing the "Display" button.

- **Team-based**: text-based report of one team's summary stats and abilities
- **Alliance-based**: text-based report of an alliance's predicted output
- **Match-based**: graphical display of what both alliances in a match are capable of (see below) 

<img src="/assets/prediction.png" width="600" align="center"/>

## Development

Before beginning development on this project, ensure you have The Blue Alliance API key saved in a text file called `secret.txt` in `/src/main/resources/apikey/secret.txt`. You may need to create the `apikey` folder as well. Refer to [the instructions here](https://www.thebluealliance.com/apidocs#apiv3) if you need help obtaining an API key. Your code *will not* run properly unless the API key is present!

Refer to [the generated Javadoc](https://raiderrobotix.github.io/Scouting-Client-FX/index.html) for more information about how classes interact with each other and available methods.
