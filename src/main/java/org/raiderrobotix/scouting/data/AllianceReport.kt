package org.raiderrobotix.scouting.data

import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Class for alliance-based calculations, stats, and predictions
 * @param teamReports An array of team reports that a part of the alliance. If this contains less than three team
 * reports, "dummy" alliance partners will be created
 */
class AllianceReport(teamReports: ArrayList<TeamReport>) {
    
    init {
        require(teamReports.size == 3) { "teamReports must contains 3 report, no more, no less" }
        calculateStats()
    }
    
    val teamReports = teamReports.toTypedArray()
    /**
     * The confidence level of not reaching the scoring potential when computing the optimal number of null hatch panels
     * E.g. A value of 0.8 means that an alliance would be able to score more, if they weren't "capped" by the null
     * hatch panels, in 20% of the matches they play. Conversely, the number of hatch panels would benefit them in
     * 80% of the matches they play. Note that if this value is low, the alliance may not be able to place hatch
     * panels at a rate that matches their cargo cycling, thus also being detrimental.
     */
    private val NULL_HATCH_CONFIDENCE = 0.8
    /**
     * The number of Monte Carlo simulation iterations to run to compute standard deviations of functions.
     * A larger number of iterations generally provides greater accuracy.
     */
    private val MONTE_CARLO_ITERATIONS = 1000
    private val numStrNames = arrayOf("One", "Two", "Three", "total")
    /**
     * Denotes the best HAB levels to start from at the beginning of the match in order to maximize points earned.
     * Positions correspond to the alliance member position.
     * Assumes that no more than two robots can start from HAB level 2.
     */
    var bestStartingLevels = IntArray(3)
        private set
    private val predictedValues = HashMap<String, Double>()
    private val expectedValues = HashMap<String, Double>()
    private val standardDeviations = HashMap<String, Double>()
    /**
     * Denotes the best HAB levels to climb at the end of the match in order to maximize points earned.
     * Positions correspond to the alliance member position.
     * Assumes that no more than two robots can climb HAB level 2 and no more than one robot can climb HAB level 3.
     */
    lateinit var bestClimbLevels: IntArray
        private set
    /**
     * The average number of scout entries made for each team in the alliance
     */
    private val avgSampleSize by lazy {
        var ret = 0.0
        var validTeamReports = 0
        for (teamReport in teamReports) {
            if (teamReport.teamNum != 0) {
                ret += teamReport.entries.size.toDouble()
                validTeamReports++
            }
        }
        if (validTeamReports != 0) {
            ret /= validTeamReports.toDouble()
        }
        ret
    }
    /**
     * A string denoting the game pieces each member of the alliance should start with in order to maximize points
     * earned. An "H" refers to a hatch panel, while a "C" refers to a cargo. The position of each character corresponds
     * to the alliance member position.
     */
    var bestSandstormGamePieceCombo: String? = null
        private set
    
    /**
     * Calculates and stores the predicted point breakdowns, expected values, standard deviations, optimal null hatch
     * panels placed, and predicted bonus ranking points of the alliance
     */
    private fun calculateStats() {
        calculateExpectedValues()
        val sandstormPoints = predictedSandstormPoints()
        val teleOpPoints = predictedTeleOpPoints()
        val endgamePoints = predictedEndgamePoints()
        predictedValues["totalPoints"] = sandstormPoints + teleOpPoints + endgamePoints
        calculateStandardDeviations()
        calculatedPredictedBonusRp()
        calculateOptimalNullHatchPanels(NULL_HATCH_CONFIDENCE)
    }
    
    /**
     * Calculates and stores the simple expected values of metrics for an alliance.
     * An expected value of a metric is defined as the sum of the average values of that metric across the teams in
     * an alliance.
     */
    private fun calculateExpectedValues() {
        val metricSets = arrayOf(
			TeamReport.autoMetrics,
			TeamReport.teleMetrics,
			TeamReport.overallMetrics
        )
        val prefixes = arrayOf("auto", "tele", "")
        for (i in metricSets.indices) {
            for (metric in metricSets[i]) {
                var expectedValue = 0.0
                for (report in teamReports) {
                    expectedValue += report.statistics[prefixes[i] + metric.name]!!.mean
                }
                expectedValues[prefixes[i] + metric.name] = expectedValue
            }
        }
    }
    
    /**
     * Calculates and stores the predicted number of points gained during the sandstorm period for an alliance
     *
     * @return The predicted number of sandstorm points
     */
    private fun predictedSandstormPoints(): Double {
        var predictedSandstormPoints = 0.0
        predictedSandstormPoints += predictedSandstormBonus()
        predictedSandstormPoints += predictedSandstormGamePiecePoints()
        predictedValues["sandstormPoints"] = predictedSandstormPoints
        return predictedSandstormPoints
    }
    
    /**
     * Iterates through all possible HAB starting level combinations to determine the optimal one for the alliance
     * Assumption: At most two teams start on HAB level 2
     *
     * @return The point value of the starting combination that yields the largest expected sandstorm bonus
     */
    private fun predictedSandstormBonus(): Double {
        var bestCrossingScore = 0.0
        val levelCombinations = arrayOf(intArrayOf(2, 2, 1), intArrayOf(2, 1, 2), intArrayOf(1, 2, 2), intArrayOf(1, 1, 2), intArrayOf(1, 2, 1), intArrayOf(1, 1, 1))
        for (levelCombo in levelCombinations) {
            var crossingScore = 0.0
            for (i in levelCombo.indices) { // Multiply by attempt-success rates here to get the true expected value per team
                crossingScore += if (levelCombo[i] == 1) {
                    3.0 * teamReports[i].statistics["levelOneCross"]!!.mean
                } else {
                    6.0 * teamReports[i].statistics["levelTwoCross"]!!.mean
                }
            }
            if (crossingScore >= bestCrossingScore) {
                bestCrossingScore = crossingScore
                bestStartingLevels = levelCombo
            }
        }
        predictedValues["sandstormBonus"] = bestCrossingScore
        return bestCrossingScore
    }
    
    /**
     * Iterates through possible starting game piece combinations to determine the optimal one for the alliance
     * during the sandstorm period
     * Assumptions:
     * If teams can place game pieces at a certain location, it doesn't matter where they start
     * A team's attempt-success rate for a game pieces is the same regardless of placement location
     * Bays where hatch panels are placed are pre-populated with cargo
     * Teams score a maximum of one game piece in sandstorm
     *
     * @return The point value of the combination that yields the largest expected score
     */
    private fun predictedSandstormGamePiecePoints(): Double {
        val gamePieceCombinations = arrayOf("HHH", "HHC", "HCH", "HCC", "CHH", "CHC", "CCH", "CCC")
        var bestGamePieceScore = 0.0
        for (gamePieceCombo in gamePieceCombinations) {
            var cargoShipCargo = 0.0
            var cargoShipHatches = 0.0
            var rocketHatches = 0.0
            var frontCargoShipCount = 0
            for (i in gamePieceCombo.indices) {
                if (gamePieceCombo[i] == 'H') { // Validate assignments here
                    if (teamReports[i].getAbility("sideCargoShipHatchSandstorm")) {
                        cargoShipHatches += teamReports[i].statistics["hatchAutoSuccess"]!!.mean
                    } else if (teamReports[i].getAbility("frontCargoShipHatchSandstorm") && frontCargoShipCount < 2) {
                        cargoShipHatches += teamReports[i].statistics["hatchAutoSuccess"]!!.mean
                        frontCargoShipCount++
                    } else if (teamReports[i].getAbility("rocketHatchSandstorm")) {
                        rocketHatches += teamReports[i].statistics["hatchAutoSuccess"]!!.mean
                    }
                } else {
                    cargoShipCargo += teamReports[i].statistics["cargoAutoSuccess"]!!.mean
                }
            }
            val gamePieceScore = 5 * cargoShipHatches + 3 * cargoShipCargo + 2 * rocketHatches
            if (gamePieceScore >= bestGamePieceScore) {
                bestGamePieceScore = gamePieceScore
                bestSandstormGamePieceCombo = gamePieceCombo
                predictedValues["autoCargoShipCargo"] = cargoShipCargo
                predictedValues["autoCargoShipHatches"] = cargoShipHatches
                predictedValues["autoRocketHatches"] = rocketHatches
                predictedValues["autoRocketCargo"] = 0.0
            }
        }
        predictedValues["sandstormGamePiecePoints"] = bestGamePieceScore
        return bestGamePieceScore
    }
    
    /**
     * Calculates and stores the predicted number of points gained from scoring game pieces during the tele-op period
     * for an alliance
     *
     * @return The predicted number of tele-op points
     */
    private fun predictedTeleOpPoints(): Double {
        val teleOpHatches = predictedTeleOpHatchPanels(false)
        val teleOpCargo = calculatePredictedTeleOpCargo(false)
        val predictedTeleOpPoints = 2 * teleOpHatches + 3 * teleOpCargo
        predictedValues["telePoints"] = predictedTeleOpPoints
        return predictedTeleOpPoints
    }
    
    /**
     * Calculates the predicted number of hatch panels scored in each location of the field during the tele-op
     * period, based on the alliance's expected values
     *
     * @param rocketRp Specifies if the alliance is attempting to score as much as possible at each location, or if
     * they are attempting to gain the rocket ranking point. Affects game piece cap for rocket levels.
     * @return The predicted number of hatch panels scored during tele-op
     */
    private fun predictedTeleOpHatchPanels(rocketRp: Boolean): Double {
        var totalHatches = 0.0
        // Carry-over variable between rocket levels
        var excessHatches = 0.0
        // Simulate placement in uppermost rocket levels first
        for (i in 2 downTo 0) {
            var cap = if (rocketRp) 2.0 else 4.0
            if (i == 0) { // Allow cargo ship hatch panels to be interchangeable with level 1 hatch panels
                excessHatches += expectedValues["telecargoShipHatches"]!!
                // Decrease cap due to rocket hatches placed during the sandstorm period
                cap = 0.0.coerceAtLeast(cap - predictedValues["autoRocketHatches"]!!)
            }
            val hatchesPut = (excessHatches + expectedValues["telerocketLevel" + numStrNames[i] +
                "Hatches"]!!).coerceAtMost(cap)
            predictedValues["teleRocketLevel" + numStrNames[i] + "Hatches"] = hatchesPut
            totalHatches += hatchesPut
            excessHatches += expectedValues["telerocketLevel" + numStrNames[i] + "Hatches"]!! - hatchesPut
        }
        val teleCargoShipHatches = excessHatches.coerceAtMost(8 - predictedValues["autoCargoShipHatches"]!!)
        totalHatches += teleCargoShipHatches
        predictedValues["teleCargoShipHatches"] = teleCargoShipHatches
        predictedValues["cargoShipHatches"] = teleCargoShipHatches + predictedValues["autoCargoShipHatches"]!!
        predictedValues["rocketLevelOneHatches"] = predictedValues["teleRocketLevelOneHatches"]!! + predictedValues["autoRocketHatches"]!!
        predictedValues["teleHatches"] = totalHatches
        predictedValues["teleHatchPoints"] = 2 * totalHatches
        return totalHatches
    }
    
    /**
     * Calculates the predicted number of cargo scored in each location of the field during the tele-op
     * period, based on the alliance's expected values
     *
     * @param rocketRp Specifies if the alliance is attempting to score as much as possible at each location, or if
     * they are attempting to gain the rocket ranking point. Affects game piece cap for rocket levels.
     * @return The predicted number of cargo scored during tele-op
     */
    private fun calculatePredictedTeleOpCargo(rocketRp: Boolean): Double {
        var totalCargo = 0.0
        var excessCargo = 0.0
        for (i in 2 downTo 0) { // Cap is affected by predicted tele-op hatch panels for that location
            var cap = if (rocketRp) 2.0 else 4.0.coerceAtMost(predictedValues["teleRocketLevel" + numStrNames[i] +
                "Hatches"]!!)
            if (i == 0) { // Allow cargo ship cargo to be interchangeable with level 1 cargo
                excessCargo += expectedValues["telecargoShipCargo"]!!
                cap = if (rocketRp) 2.0 else 4.0.coerceAtMost(predictedValues["teleRocketLevel" + numStrNames[i] + "Hatches"]!! + predictedValues["autoRocketHatches"]!!)
            }
            val cargoPut = (excessCargo + expectedValues["telerocketLevel" + numStrNames[i] + "Cargo"]!!).coerceAtMost(cap)
            predictedValues["teleRocketLevel" + numStrNames[i] + "Cargo"] = cargoPut
            totalCargo += cargoPut
            excessCargo += expectedValues["telerocketLevel" + numStrNames[i] + "Cargo"]!! - cargoPut
        }
        // We do not cap by hatch panels here due to the possibility of null hatch panels
// We also assume that hatch panels placed during sandstorm will be placed in a bay pre-populated with cargo
        val teleCargoShipCargo = excessCargo.coerceAtMost(8 - predictedValues["autoCargoShipCargo"]!! - predictedValues["autoCargoShipHatches"]!!)
        predictedValues["teleCargoShipCargo"] = teleCargoShipCargo
        totalCargo += teleCargoShipCargo
        predictedValues["teleCargo"] = totalCargo
        predictedValues["teleCargoPoints"] = 3 * totalCargo
        predictedValues["cargoShipCargo"] = predictedValues["teleCargoShipCargo"]!! + predictedValues["autoCargoShipCargo"]!!
        predictedValues["rocketLevelOneCargo"] = predictedValues["teleRocketLevelOneCargo"]!!
        return totalCargo
    }
    
    /**
     * Iterates through all possible HAB climb combinations to determine the optimal one for the alliance
     * Assumption:
     * At most one team climbs to HAB level 3
     * At most two teams climb to HAB level 2
     * A robot either climbs to its assigned level or does not climb the HAB at all
     *
     * @return The point value of the endgame climb combination that yields the largest expected score
     */
    private fun predictedEndgamePoints(): Double {
        var bestEndgamePoints = 0.0
        val climbLevelCombos = arrayOf(intArrayOf(1, 1, 1), intArrayOf(1, 1, 2), intArrayOf(1, 1, 3), intArrayOf(1, 2, 1), intArrayOf(1, 2, 2), intArrayOf(1, 2, 3), intArrayOf(1, 3, 1), intArrayOf(1, 3, 2), intArrayOf(2, 1, 1), intArrayOf(2, 1, 2), intArrayOf(2, 1, 3), intArrayOf(2, 2, 1), intArrayOf(2, 2, 3), intArrayOf(2, 3, 1), intArrayOf(2, 3, 2), intArrayOf(3, 1, 1), intArrayOf(3, 1, 2), intArrayOf(3, 2, 1), intArrayOf(3, 2, 2))
        val climbPointValues = intArrayOf(3, 6, 12)
        for (climbLevelCombo in climbLevelCombos) {
            var endgamePoints = 0.0
            // Iterate through each team on the alliance
            for (i in climbLevelCombo.indices) {
                endgamePoints += climbPointValues[climbLevelCombo[i] - 1] * teamReports[i].statistics["level" + numStrNames[climbLevelCombo[i] - 1] + "Climb"]!!.mean
            }
            if (endgamePoints >= bestEndgamePoints) {
                bestEndgamePoints = endgamePoints
                bestClimbLevels = climbLevelCombo
            }
        }
        predictedValues["endgamePoints"] = bestEndgamePoints
        return bestEndgamePoints
    }
    
    /**
     * Calculates standard deviations for all predicted metrics
     */
    private fun calculateStandardDeviations() {
        val sandstormStdDev = calculateStdDevSandstormPoints()
        val teleOpStdDev = calculateStdDevTeleOpPoints(generateMonteCarloSet(MONTE_CARLO_ITERATIONS))
        val endgameStdDev = calculateStdDevEndgamePoints()
        val totalPointsStdDev = Stats.sumStandardDeviation(doubleArrayOf(sandstormStdDev, teleOpStdDev,
			endgameStdDev))
        standardDeviations["totalPoints"] = totalPointsStdDev
    }
    
    /**
     * Calculates standard deviations for sandstorm period predictions
     *
     * @return The standard deviation of overall predicted sandstorm points
     */
    private fun calculateStdDevSandstormPoints(): Double {
        var sandstormBonusVariance = 0.0
        // Add the variance for each team, which is a function of their starting level's point value and the
// attempt-success rate's standard deviation
        for (i in bestStartingLevels.indices) {
            sandstormBonusVariance += Stats.multiplyVariance(bestStartingLevels[i] * 3.toDouble(),
				teamReports[i].statistics["level" + numStrNames[bestStartingLevels[i] - 1] +
					"Cross"]!!.standardDeviation)
        }
        // Recall that standard deviation of a metric is the square root of its variance
        standardDeviations["sandstormBonus"] = sqrt(sandstormBonusVariance)
        var sandstormGamePieceVariance = 0.0
        var sandstormHatchVariance = 0.0
        for (i in bestSandstormGamePieceCombo!!.indices) {
            if (bestSandstormGamePieceCombo!![i] == 'H') {
                sandstormGamePieceVariance += Stats.multiplyVariance(5.0, teamReports[i].statistics[
					"hatchAutoSuccess"]!!.standardDeviation)
                sandstormHatchVariance += teamReports[i].statistics["hatchAutoSuccess"]!!.standardDeviation.pow(2)
            } else {
                sandstormGamePieceVariance += Stats.multiplyVariance(3.0, teamReports[i].statistics[
					"cargoAutoSuccess"]!!.standardDeviation)
            }
        }
        standardDeviations["autoCargoShipHatches"] = sqrt(sandstormHatchVariance)
        standardDeviations["sandstormGamePiecePoints"] = sqrt(sandstormGamePieceVariance)
        val sandstormPointsStdDev = sqrt(sandstormBonusVariance + sandstormGamePieceVariance)
        standardDeviations["sandstormPoints"] = sandstormPointsStdDev
        return sandstormPointsStdDev
    }
    
    /**
     * Calculates the standard deviation of all tele-op predictions, excluding those found in endgame, through a
     * simulated and randomized data set
     *
     * @param allianceSimulationValues Set of simulated expected values for an alliance, randomly generated from a
     * Normal distribution. Each element of the set is an ArrayList with three
     * HashMaps, corresponding to the metric values of a team on the alliance. These
     * values are used to simulate the standard deviation.
     * @return The standard deviation in the predicted number of tele-op points, given the input values
     */
    private fun calculateStdDevTeleOpPoints(allianceSimulationValues: ArrayList<ArrayList<HashMap<String, Double>>>): Double { // Builds a list of all tele-op prediction metrics
        val metricNames = ArrayList<String>()
        metricNames.add("telePoints")
        val locations = arrayOf("teleRocketLevelOne", "teleRocketLevelTwo", "teleRocketLevelThree",
            "teleCargoShip")
        val pieces = arrayOf("Hatches", "Cargo")
        for (location in locations) {
            for (piece in pieces) {
                metricNames.add(location + piece)
            }
        }
        // Stores arrays of raw generated values for each metric
        val simulationCalculatedValues = HashMap<String, DoubleArray>()
        for (metric in metricNames) {
            simulationCalculatedValues[metric] = DoubleArray(MONTE_CARLO_ITERATIONS)
        }
        // Iterates through each set of randomly generated alliance values
        for (i in allianceSimulationValues.indices) { // Temporarily replaces this class's expectedValue HashMap with simulation values
            calculateMonteCarloExpectedValues(allianceSimulationValues[i])
            // Recalculate predicted values
            predictedTeleOpPoints()
            // Stores this iteration's predicted values into the simulated values HashMap
            for (metric in metricNames) {
                simulationCalculatedValues[metric]!![i] = predictedValues[metric]!!
            }
        }
        // Calculates the standard deviation of the predicted metrics, based on the variation in the results of using
// the 1,000 simulation values
        for (metric in metricNames) {
            standardDeviations[metric] = Stats.standardDeviation(simulationCalculatedValues[metric])
        }
        //Restores values using the original alliance report
        calculateExpectedValues()
        predictedTeleOpPoints()
        return standardDeviations["telePoints"]!!
    }
    
    /**
     * Replaces the values of the `expectedValues` HashMap in this class with those from a Monte
     * Carlo-simulated alliance of three teams
     *
     * @param testSets Set of three HashMaps, representing metric values from an alliance of three teams for a
     * particular match
     */
    private fun calculateMonteCarloExpectedValues(testSets: ArrayList<HashMap<String, Double>>) { // Creates a list of all average value metrics for a team, used to calculated an alliance expected value
        val metricSets = arrayOf(
			TeamReport.autoMetrics,
			TeamReport.teleMetrics,
			TeamReport.overallMetrics
        )
        val prefixes = arrayOf("auto", "tele", "")
        // Iterate through various metric names
        for (i in metricSets.indices) {
            for (metric in metricSets[i]) {
                var value = 0.0
                // Iterate through each team in the alliance
                for (testSet in testSets) {
                    value += testSet[prefixes[i] + metric.name]!!
                }
                expectedValues[prefixes[i] + metric.name] = value
            }
        }
    }
    
    /**
     * Generates a specified number of Monte Carlo simulations of match values for the teams in the current alliance
     *
     * @param numIterations Number of simulations to create
     * @return An array with elements representing the result of a Monte Carlo simulation. Each simulation result is
     * an array of three HashMaps, each representing the values of a team in a match, randomly selected from the
     * team's Normal distribution for that metric.
     */
    private fun generateMonteCarloSet(numIterations: Int = MONTE_CARLO_ITERATIONS): ArrayList<ArrayList<HashMap<String, Double>>> {
        val simulationValues = ArrayList<ArrayList<HashMap<String, Double>>>(numIterations)
        for (i in 0 until numIterations) { // Contains the three alliances and their values in the match
            val sampleMatchValues = ArrayList<HashMap<String, Double>>()
            // Generates values based on the teams that make up this AllianceReport
            for (sample in teamReports) {
                sampleMatchValues.add(sample.generateRandomSample())
            }
            simulationValues.add(sampleMatchValues)
        }
        return simulationValues
    }
    
    /**
     * Calculates the standard deviation of the predicted number of endgame points
     *
     * @return The standard deviation of predicted endgame points
     */
    private fun calculateStdDevEndgamePoints(): Double {
        val climbPointValues = intArrayOf(3, 6, 12)
        var endgameVariance = 0.0
        // Adds the variance for each team
        for (i in bestClimbLevels.indices) {
            endgameVariance += Stats.multiplyVariance(climbPointValues[bestClimbLevels[i] - 1].toDouble(),
				teamReports[i].statistics["level" + numStrNames[bestClimbLevels[i] - 1] + "Climb"]!!.standardDeviation)
        }
        val endgameStdDev = sqrt(endgameVariance)
        standardDeviations["endgamePoints"] = endgameStdDev
        return endgameStdDev
    }
    
    /**
     * Calculates the number of predicted bonus ranking points in a match for the alliance
     *
     * @return Predicted number of bonus ranking points
     */
    private fun calculatedPredictedBonusRp(): Double {
        val bonusRp = calculateClimbRpChance() + calculateRocketRpChance(generateMonteCarloSet(MONTE_CARLO_ITERATIONS))
        predictedValues["bonusRp"] = bonusRp
        return bonusRp
    }
    
    /**
     * Calculates the expected number of ranking points acquired from completing the rocket in a qualification match
     * for the alliance
     *
     * @param allianceSimulationValues Set of Monte Carlo simulation values for the alliance showing its output
     * during the course of a match
     * @return The expected number of rocket ranking points
     */
    private fun calculateRocketRpChance(allianceSimulationValues: ArrayList<ArrayList<HashMap<String, Double>>>): Double { // Counter for the number of simulations in which the RP is attained
        var rocketRpAttainedCount = 0
        for (teamReportSet in allianceSimulationValues) { // Simulates placing game pieces on the rocket
            calculateMonteCarloExpectedValues(teamReportSet)
            predictedTeleOpHatchPanels(true)
            calculatePredictedTeleOpCargo(true)
            var rpAttained = true
            // Checks if there ISN'T at least two of each type of game pieces placed on the rocket
            for (j in 0..2) {
                for (gamePiece in arrayOf("Hatches", "Cargo")) {
                    val threshold: Double = 2.0 - (if (j == 0) predictedValues["autoRocket$gamePiece"]!! else 0.0)
                    val mean = predictedValues["teleRocketLevel" + numStrNames[j] + gamePiece]!!
                    if (mean < threshold) {
                        rpAttained = false
                    }
                }
            }
            if (rpAttained) {
                rocketRpAttainedCount++
            }
        }
        // Replace simulation values with the actual ones
        calculateExpectedValues()
        predictedTeleOpPoints()
        // Overall this method could be improved by making the threshold less strict, or using probability regardless
// of rocket level of placing 6+ of each game piece
        val rocketRpChance = rocketRpAttainedCount.toDouble() / MONTE_CARLO_ITERATIONS
        predictedValues["rocketRp"] = rocketRpChance
        return rocketRpChance
    }
    
    /**
     * Calculates the chance of attaining the HAB docking ranking point for the alliance
     *
     * @return The expected value of the HAB docking ranking point
     */
    private fun calculateClimbRpChance(): Double {
        var climbRpChance = 0.0
        val climbPointValues = intArrayOf(3, 6, 12)
        // Generates a probability tree for the best climb combination in which each team either does or doesn't
// climb to their assigned level of the HAB
        for (teamOneClimb in 0..1) {
            for (teamTwoClimb in 0..1) {
                for (teamThreeClimb in 0..1) {
                    var points = 0
                    val climbStatus = intArrayOf(teamOneClimb, teamTwoClimb, teamThreeClimb)
                    for (i in 0..2) {
                        points += climbStatus[i] * climbPointValues[bestClimbLevels[i] - 1]
                    }
                    if (points >= 15) {
                        var probabilityIteration = 1.0
                        // Determines the exact probability of this combination occurring, based on attempt-success
// rates
                        for (i in 0..2) {
                            probabilityIteration *= if (climbStatus[i] == 1) {
                                (teamReports[i].statistics["level" + numStrNames[bestClimbLevels[i] - 1] + "Climb"]
                                    ?: error("")).mean
                            } else {
                                1 - (teamReports[i].statistics["level" + numStrNames[bestClimbLevels[i] - 1] + "Climb"]
                                    ?: error("")).mean
                            }
                        }
                        climbRpChance += probabilityIteration
                    }
                }
            }
        }
        predictedValues["climbRp"] = climbRpChance
        return climbRpChance
    }
    
    /**
     * Calculates the optimal number of null hatch panels to place on the cargo ship, based on the alliance's average
     * hatch panel output and its variability. Skews towards the optimistic estimate of hatch panels placed, so it is
     * unlikely that a point "cap" is reached by the alliance if it only scores hatch panels on the cargo ship.
     *
     * @param confidenceLevel The confidence level of the t confidence interval created. A higher confidence results
     * in a higher optimistic prediction and lower cap, at the expense of not having enough
     * hatched bays on the cargo ship to place cargo in. A lower confidence results in a
     * higher cap, which means the alliance may run out of hatch panel scoring opportunities.
     */
    private fun calculateOptimalNullHatchPanels(confidenceLevel: Double) {
        val averageCargoShipHatches = predictedValues["autoCargoShipHatches"]!! + predictedValues["teleCargoShipHatches"]!!
        val standardDeviation = Stats.sumStandardDeviation(doubleArrayOf(
			standardDeviations["autoCargoShipHatches"]!!, standardDeviations["teleCargoShipHatches"]!!))
        // This represents the greater endpoint of a t confidence interval with the specified confidence level
        var optimisticCargoShipHatches = averageCargoShipHatches
        if (avgSampleSize > 1) {
            optimisticCargoShipHatches = Stats.inverseTValue(confidenceLevel, avgSampleSize - 1,
				averageCargoShipHatches,
				standardDeviation)
        }
        // Number of null hatch panels to place such that a point cap isn't reached
        val nullHatches = (8 - optimisticCargoShipHatches).coerceAtMost(6.0).coerceAtLeast(0.0)
        predictedValues["optimalNullHatches"] = nullHatches
    }
    
    /**
     * Calculates the number of predicted ranking points in a match against another alliance
     *
     * @param opposingAlliance AllianceReport representing the opposing alliance
     * @return Predicted ranking points for the current alliance
     */
    fun calculatePredictedRp(opposingAlliance: AllianceReport): Double {
        return calculatedPredictedBonusRp() + 2 * calculateWinChance(opposingAlliance)
    }
    
    /**
     * Calculates the confidence in the current alliance winning a match against the specified opposing alliance
     *
     * @param opposingAlliance An `AllianceReport` representing the other alliance
     * @return The confidence in the current alliance winning the match
     */
    fun calculateWinChance(opposingAlliance: AllianceReport): Double {
        val thisStandardError = Stats.standardError(standardDeviations["totalPoints"]!!, avgSampleSize)
        val opposingStandardError = Stats.standardError(opposingAlliance.getStandardDeviation("totalPoints")!!,
			opposingAlliance.avgSampleSize)
        // Calculate the t-score of the win statistic (difference in predicted score for the alliance)
        val tScore = Stats.twoSampleMeanTScore(predictedValues["totalPoints"]!!, thisStandardError,
			opposingAlliance.getPredictedValue("totalPoints")!!, opposingStandardError)
        val degreesOfFreedom = Stats.twoSampleDegreesOfFreedom(thisStandardError, avgSampleSize,
			opposingStandardError, opposingAlliance.avgSampleSize)
        // Integrate along the t-distribution, based on the calculated score
        return Stats.tCumulativeDistribution(degreesOfFreedom, tScore)
    }
    
    /**
     * Generates a text report for the alliance
     *
     * @return An easily-readable string of the key stats of the alliance
     */
    val quickAllianceReport: String by lazy {
        val keys = predictedValues.keys.toTypedArray()
        Arrays.sort(keys)
        val quickReport = StringBuilder(keys.size * 10)
        for (report in teamReports) {
            quickReport.append("Team ${report.teamNum}")
            if (report.teamName.isNotEmpty()) {
                quickReport.append(" - ${report.teamName}")
            }
            quickReport.append('\n')
        }
        
        for ((k, v) in predictedValues) {
            quickReport.append("$k: ${Stats.round(v, 2)}\n")
        }
        quickReport.toString()
    }
    
    /**
     * Retrieves the value of a predicted metric for the alliance
     *
     * @param metric The metric to retrieve
     * @return The metric's predicted value in this alliance
     */
    fun getPredictedValue(metric: String?): Double? = predictedValues[metric]
    
    /**
     * Retrieves the value of the standard deviation of a metric for the alliance
     *
     * @param metric The metric to retrieve
     * @return The metric's standard deviation in this alliance
     */
    fun getStandardDeviation(metric: String?): Double? = standardDeviations[metric]
}
