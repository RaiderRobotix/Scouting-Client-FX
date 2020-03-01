package org.usfirst.frc.team25.scouting.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.usfirst.frc.team25.scouting.data.models.Autonomous;
import org.usfirst.frc.team25.scouting.data.models.ScoutEntry;
import org.usfirst.frc.team25.scouting.data.models.TeleOp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class JTeamReport {
	
	private static final Method[] autoMetrics;
	private static final Method[] teleMetrics;
	
	static {
		try {
			// USE INTELLIJ HINTS TO CHECK METHODS EXIST
			autoMetrics = new Method[]{
				Autonomous.class.getDeclaredMethod("getCellsScoredBottom"),
				Autonomous.class.getDeclaredMethod("getCellsScoredOuter"),
				Autonomous.class.getDeclaredMethod("getCellsScoredInner"),
				Autonomous.class.getDeclaredMethod("getCrossInitLine"),
			};
			teleMetrics = new Method[]{
				TeleOp.class.getDeclaredMethod("getCellsDropped"),
				TeleOp.class.getDeclaredMethod("getCellsScoredBottom"),
				TeleOp.class.getDeclaredMethod("getCellsScoredOuter"),
				TeleOp.class.getDeclaredMethod("getCellsScoredInner"),
				TeleOp.class.getDeclaredMethod("getRotationControl"),
				TeleOp.class.getDeclaredMethod("getSuccessHang"),
			};
		} catch (NoSuchMethodException nsm) {
			throw new Error("A method couldn't be found reflectively");
		}
	}
	
	private final transient Set<ScoutEntry> entries;
	@Getter
	private final int teamNum;
	@Getter
	private final String teamName;
	@Getter
	private final Map<String, StatisticalSummary> statistics;
	@Getter
	private final Map<String, Integer> counts = new HashMap<>();
	@Getter
	private final Map<String, Boolean> abilities = new HashMap<>();
	private String frequentCommentStr;
	private String allComments;
	private Set<String> frequentComments = new HashSet<>();
	
	public JTeamReport(Collection<ScoutEntry> entries, int teamNum, String teamName) {
		this.entries = new HashSet<>(entries);
		this.teamNum = teamNum;
		this.teamName = teamName;
		//Add abilities
		entries.forEach(entry -> {
			if (Objects.requireNonNull(entry.getAutonomous()).getCrossInitLine()) // Example code to calculate if a
			// skill is present
			{
				abilities.putIfAbsent("crossInitLine", true);
			}
		});
		val statistics = new HashMap<String, StatisticalSummary>(autoMetrics.length + teleMetrics.length);
		// calculate stats
		val autos = entries.parallelStream()
			.map(ScoutEntry::getAutonomous).collect(Collectors.toUnmodifiableList());
		
		val teles = entries.parallelStream()
			.map(ScoutEntry::getTeleOp).collect(Collectors.toUnmodifiableList());
		
		val autoStats = extractStats(autos, Arrays.asList(autoMetrics));
		val teleStats = extractStats(teles, Arrays.asList(teleMetrics));
		
		statistics.putAll(autoStats);
		statistics.putAll(teleStats);
		
		this.statistics = Collections.unmodifiableMap(statistics);
		
	}
	
	@NonNull
	private static <T> Double toDouble(@NonNull T primitive) {
		if (primitive instanceof Number) {
			return ((Number) primitive).doubleValue();
		} else if (primitive instanceof Boolean) {
			return ((Boolean) primitive) ? 1.0 : 0.0;
		} else {
			throw new IllegalArgumentException("Isn't a primitive");
		}
	}
	
	@NonNull
	private static <T> Map<String, StatisticalSummary> extractStats(Collection<T> sourceObjects,
																	Collection<Method> properties) {
		val ret = new HashMap<String, StatisticalSummary>(properties.size());
		for (val prop : properties) {
			val summaryStatistics = new SummaryStatistics();
			sourceObjects
				.stream()
				.map(it -> {
					try {
						return prop.invoke(it);
					} catch (IllegalAccessException | InvocationTargetException e) {
						return null;
					}
				})
				.mapToDouble(JTeamReport::toDouble)
				.forEach(summaryStatistics::addValue);
			ret.putIfAbsent("auto" + prop.getName(), summaryStatistics.getSummary());
		}
		return ret;
	}
	
}
