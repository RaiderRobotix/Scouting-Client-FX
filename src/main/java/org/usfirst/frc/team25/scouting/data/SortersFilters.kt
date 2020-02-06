package org.usfirst.frc.team25.scouting.data

import com.thebluealliance.api.v3.models.SimpleMatch
import com.thebluealliance.api.v3.models.SimpleTeam
import java.util.*
import java.util.function.Consumer

/**
 * Collection of static methods to sort, search, and filter through various data structures
 */
object SortersFilters {
	@JvmField
	val byMatchNum: Comparator<SimpleMatch> = Comparator.comparingInt { obj: SimpleMatch -> obj.matchNumber }
	@JvmField
	val byTeamNum: Comparator<SimpleTeam> = Comparator.comparingInt { obj: SimpleTeam -> obj.teamNumber }
	
	@JvmStatic
	fun <K, V : Comparable<V>?> Map<K, V>.sortByValue(): LinkedHashMap<K, V> {
		val entries = Vector(this.entries)
		entries.sortWith(java.util.Map.Entry.comparingByValue())
		val sorted = LinkedHashMap<K, V>()
		entries.forEach(Consumer { sorted[it.key] = it.value })
		return sorted
	}
}