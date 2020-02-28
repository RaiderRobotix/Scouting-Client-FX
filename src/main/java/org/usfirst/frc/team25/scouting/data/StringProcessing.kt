package org.usfirst.frc.team25.scouting.data

import org.jetbrains.annotations.Contract

/**
 * Collection of static methods that manipulate strings
 */
object StringProcessing {
	/**
	 * Helper method to prevent manual comments with commas or line breaks
	 * from changing CSV format
	 *
	 * @param s String to be processed
	 * @return String without commas
	 */
	@JvmStatic
	@Contract(pure = true)
	fun String.removeCommasBreaks(): String =
		replace("[\n,]".toRegex(), "; ")
}