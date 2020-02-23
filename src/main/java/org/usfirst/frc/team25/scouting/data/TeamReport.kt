package org.usfirst.frc.team25.scouting.data

import org.apache.commons.math3.stat.descriptive.StatisticalSummary
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import org.usfirst.frc.team25.scouting.data.StringProcessing.removeCommasBreaks
import org.usfirst.frc.team25.scouting.data.models.Autonomous
import org.usfirst.frc.team25.scouting.data.models.ScoutEntry
import org.usfirst.frc.team25.scouting.data.models.TeleOp
import java.io.File
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KProperty1

/**
 * Object model containing individual reports of teams in events and methods to process data and calculate team-based
 * statistics
 */
class TeamReport(val teamNum: Int) {
	
	@Transient
	val entries = ArrayList<ScoutEntry>()
	// HashMaps containing metric name and value pairings
	var teamName = ""
		private set
	private var frequentCommentStr = ""
	private var allComments: String? = null
	private val frequentComments = mutableListOf<String>()
	var counts = mapOf<String, Int>()
		private set
	private val abilities by lazy {
		val abilities = mutableMapOf(
			"cargoFloorIntake" to frequentComments.contains("Cargo floor intake"),
			"hatchPanelFloorIntake" to frequentComments.contains("Hatch panel floor intake")
		)
		
		for (entry in entries) {
			if (entry.autonomous.crossInitLine) // Example code to calculate if a skill is present
				abilities["crossInitLine"] = true
		}
		abilities.toMap()
	}
	
	val statistics: Map<String, StatisticalSummary> by lazy {
		val statistics = mutableMapOf<String, StatisticalSummary>()
		fun <T> Collection<T>.toDoubles() = when (first()) {
			is Number -> (this as Collection<Int>).map { it.toDouble() }
			is Boolean -> (this as Collection<Boolean>).map { if (it) 1.0 else 0.0 }
			else -> null
		}
		
		val autoList = entries.map { it.autonomous }
		for (prop in autoMetrics) {
			val summaryStatistics = SummaryStatistics()
			autoList.map { prop.call(it) }.toDoubles()?.forEach(summaryStatistics::addValue)
			statistics["auto${prop.name}"] = summaryStatistics.summary
		}
		
		val teleList = entries.map { it.teleOp }
		for (prop in teleMetrics) {
			val summaryStatistics = SummaryStatistics()
			teleList.map { prop.call(it) }.toDoubles()?.forEach(summaryStatistics::addValue)
			statistics["tele${prop.name}"] = summaryStatistics.summary
		}
		
		for (prop in overallMetrics) {
			val summaryStatistics = SummaryStatistics()
			entries.map { prop.call(it) }.toDoubles()?.forEach(summaryStatistics::addValue)
			statistics[prop.name] = summaryStatistics.summary
		}
		statistics
	}
	
	val stats
		get() = statistics
	
	/**
	 * Processes the scout entries within the team report by filtering out no shows, calculating stats, and finding
	 * abilities and frequent comments
	 */
	fun processReport() {
		findFrequentComments()
	}
	
	/**
	 * Increments a count metric by 1 and creates it if it currently does not exist
	 *
	 * @param metric The count metric to increment
	 */
	private fun incrementCount(metric: String) {
		counts = counts.toMutableMap().apply {
			if (containsKey(metric)) {
				this[metric] = this[metric]!! + 1
			} else {
				this[metric] = 1
			}
		}.toMap()
	}
	
	/**
	 * Populates the frequent comment array with quick comments that appear at least 25% of the time in a team's
	 * scouting entries
	 * Also concatenates all custom comments made into the `allComments` string
	 */
	private fun findFrequentComments() {
		val commentFrequencies = HashMap<String, Int>()
		if (entries.size > 0) {
			for (key in entries[0].postMatch.robotQuickCommentSelections.keys) {
				commentFrequencies[key] = 0
				for (entry in entries) {
					if (entry.postMatch.robotQuickCommentSelections.containsKey(key)) {
						commentFrequencies[key] = commentFrequencies[key]!! + 1
					}
				}
			}
		}
		for (key in commentFrequencies.keys) {
			if (commentFrequencies[key]!! >= entries.size / 4.0) {
				frequentComments.add(key)
			}
		}
		for (comment in frequentComments) {
			frequentCommentStr += comment.removeCommasBreaks() + " \n"
		}
		allComments = ""
		for (entry in entries) {
			if (entry.postMatch.robotComment != "") {
				allComments += entry.postMatch.robotComment + "; "
			}
		}
	}
	
	/**
	 * Generates a random sample of various metrics computed in `averages`, assuming a Normal distribution
	 * with standard deviations specified by the team's `standardDeviations`
	 *
	 * @return A HashMap with metric names as keys and their associated random values
	 */
	fun generateRandomSample(): HashMap<String, Double> {
		val randomSample = HashMap<String, Double>()
		statistics.forEach { (t, u) -> randomSample[t] = Stats.randomNormalValue(u.mean, u.standardDeviation) }
		return randomSample
	}
	
	/**
	 * Generates an easily-readable report with relevant stats on an team's capability
	 *
	 * @return A formatted string with relevant aggregate team stats
	 */
	val quickStatus: String by lazy {
		val statusString = StringBuilder("Team $teamNum")
		val append: (Any) -> StringBuilder = statusString::append
		if (teamName.isNotEmpty()) {
			append(" - $teamName")
		}
		statusString.append("\n\nSandstorm:")
		for (metric in autoMetrics) {
			append("\nAvg. ${metric.name}: ${Stats.round(statistics["auto$metric"]!!.mean, 2)}")
		}
		statusString.toString()
	}
	
	/**
	 * Adds entries to the scouting entry list of this team
	 *
	 * @param entry `ScoutEntry` to be added to this team report
	 */
	fun addEntry(entry: ScoutEntry) {
		if (!entry.preMatch.noShow) {
			// sanitize user input before adding entry
			entries.add(
				entry.copy( // All the data classes are intentionally immutable!
					postMatch = entry.postMatch.copy(
						robotComment = entry.postMatch.robotComment.removeCommasBreaks()
					)
				)
			)
		}
	}
	
	/**
	 * Fetches the nickname of the team from the specified team list and assigns it to `teamName`
	 *
	 * @param dataLocation location of the TeamNameList file generated by `exportTeamList`
	 */
	fun autoGetTeamName(dataLocation: File) {
		val data = FileManager.getFileString(dataLocation)
		val values = data.split(",\n").toTypedArray()
		for (value in values) {
			if (value.split(",").toTypedArray()[0] == teamNum.toString()) {
				teamName = value.split(",").toTypedArray()[1]
				return
			}
		}
	}
	
	fun getCount(name: String): Int = counts[name] ?: error("No such count")
	
	
	/**
	 * Retrieves the value of the specified ability metric
	 *
	 * @param metric String name of the desired metric
	 * @return The value of the ability metric, false if the metric name does not exist
	 */
	fun getAbility(metric: String): Boolean = abilities[metric]!!
	
	companion object {
		// Metric defined to assist with iterating over values
		val autoMetrics = arrayOf(
			Autonomous::cellsScoredBottom,
			Autonomous::cellsScoredOuter,
			Autonomous::cellsScoredInner,
			Autonomous::crossInitLine
		)
		val teleMetrics = arrayOf(
			TeleOp::cellsDropped,
			TeleOp::cellsScoredBottom,
			TeleOp::cellsScoredOuter,
			TeleOp::cellsScoredInner,
			TeleOp::rotationControl,
			TeleOp::positionControl,
			TeleOp::successHang
		)
		val overallMetrics = arrayOf<KProperty1<ScoutEntry, Any>>()
	}
	
}

