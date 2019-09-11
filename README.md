# FRC Scouting Client [![Build Status](https://travis-ci.com/RaiderRobotix/Scouting-Client-FX.svg?token=o8AnHy8tPTpEiAQq4tFv&branch=master)](https://travis-ci.com/RaiderRobotix/Scouting-Client-FX)

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

**Downloading data from The Blue Alliance**


**Processing scouting data**


**Generating aggregate reports**

## Development

Before beginning development on this project, ensure you have The Blue Alliance API key saved in a text file called `secret.txt` in `/src/main/resources/apikey`. Refer to [the instructions here](https://www.thebluealliance.com/apidocs#apiv3) if you need help obtaining an API key. Your code *will not* compile unless the API key is present!

Refer to the generated Javadoc  
