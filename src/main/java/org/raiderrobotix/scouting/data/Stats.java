package org.raiderrobotix.scouting.data;

import org.apache.commons.math3.distribution.TDistribution;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * Collection of static methods to perform statistical computations and operations
 */
public class Stats {
	
	/**
	 * Rounds a value to the specified number of decimal places
	 *
	 * @param value  Double value to be rounded
	 * @param places Number of decimal places to round the value. Must be a non-negative integer.
	 * @return Value rounded to the number of places
	 */
	public static double round(double value, int places) {
		if (places < 0) {
			throw new IllegalArgumentException();
		}
		
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	/**
	 * Calculates the sample standard deviation of an attempt-success metric or proportion
	 *
	 * @param attempts  Number of attempts for a task
	 * @param successes Number of successes for a task
	 * @return The standard deviation of the attempt-success metric
	 */
	public static double standardDeviation(int attempts, int successes) {
		if (attempts < 2) {
			return 0.0;
		}
		
		return Math.sqrt((successes * (1 - ((double) successes) / attempts)) / (attempts - 1));
	}
	
	/**
	 * Calculates the standard error for a sample dataset of a metric
	 *
	 * @param dataset Array of sample values
	 * @return Standard error of those values
	 */
	public static double standardError(double[] dataset) {
		return standardError(standardDeviation(dataset), dataset.length);
	}
	
	/**
	 * Calculates the uncorrected sample standard deviation of an event
	 *
	 * @param dataset Array with data points
	 * @return Uncorrected standard deviation
	 */
	public static double standardDeviation(double[] dataset) {
		double average = mean(dataset);
		double sumSquareDev = 0;
		
		for (double num : dataset) {
			sumSquareDev += Math.pow(num - average, 2);
		}
		
		if (dataset.length == 1) {
			return 0;
		}
		
		return Math.sqrt(sumSquareDev / (dataset.length - 1));
	}
	
	/**
	 * Calculates the arithmetic mean of a dataset
	 *
	 * @param dataset Array of numbers
	 * @return Average of entries in array, 0 if dataset.size() is 0
	 */
	public static double mean(double[] dataset) {
		if (dataset.length == 0) {
			return 0;
		}
		
		return sum(dataset) / dataset.length;
	}
	
	/**
	 * Calculates the sum of an array of doubles
	 *
	 * @param dataset Array of numbers to be summed
	 * @return Sum of the elements in <code>dataset</code>
	 */
	public static double sum(double[] dataset) {
		double sum = 0;
		for (double num : dataset) {
			sum += num;
		}
		
		return sum;
	}
	
	/**
	 * Calculates the standard error of a metric, given its standard deviation and sample size
	 *
	 * @param standardDeviation Metric's standard deviation
	 * @param sampleSize        Metric's sample size (or average sample size)
	 * @return Standard error of the metric
	 */
	public static double standardError(double standardDeviation, double sampleSize) {
		return standardDeviation / Math.sqrt(sampleSize);
	}
	
	/**
	 * Multiplies a standard deviation by a constant, than returns its variance
	 * Equal to the variance of c * SD(metric)
	 *
	 * @param constant          Constant that the standard deviation is multiplied by (e.g. for point values of a
	 *                          level)
	 * @param standardDeviation Standard deviation that is multiplied
	 * @return The variance of the standard deviation multiplied by a constant
	 */
	public static double multiplyVariance(double constant, double standardDeviation) {
		return Math.pow(constant, 2) * Math.pow(standardDeviation, 2);
	}
	
	/**
	 * Generates a pseudorandom value from the specified Normal probability distribution
	 *
	 * @param mean              Center of the probability distribution
	 * @param standardDeviation Standard deviation of the distribution
	 * @return A random value along a Normal probability distribution with the given parameters
	 */
	public static double randomNormalValue(double mean, double standardDeviation) {
		Random r = new Random();
		return Math.max(r.nextGaussian() * standardDeviation + mean, 0);
	}
	
	/**
	 * Retrieves the upper bound of a t confidence interval with the specified confidence level
	 *
	 * @param confidenceLevel   Confidence level of the interval, must be between 0.0 and 1.0
	 * @param degreesOfFreedom  Degrees of freedom of the t distribution
	 * @param mean              Center value of the t distribution
	 * @param standardDeviation Standard deviation of the t distribution
	 * @return Greatest upper bound of the generated t confidence interval
	 */
	public static double getUpperBoundTConfidenceInverval(double confidenceLevel, double degreesOfFreedom,
														  double mean, double standardDeviation) {
		double percentile = confidenceLevel + (1 - confidenceLevel) / 2;
		return inverseTValue(percentile, degreesOfFreedom, mean, standardDeviation);
	}
	
	/**
	 * Calculates the value for a particular percentile score in a t distributions with the specified parameters
	 *
	 * @param percentile        Percentile to retrieve the value for
	 * @param degreesOfFreedom  Degrees of freedom of the t distribution
	 * @param mean              Center value of the t distribution
	 * @param standardDeviation Standard deviation of the t distribution
	 * @return The value for the specified percentile in the distribution
	 */
	public static double inverseTValue(double percentile, double degreesOfFreedom, double mean,
									   double standardDeviation) {
		double tScore = inverseTScore(percentile, degreesOfFreedom);
		double standardError = standardError(standardDeviation, (int) degreesOfFreedom + 1);
		return tScore * standardError + mean;
	}
	
	/**
	 * Calculates the t score at which the specified area in a t cumulative distribution function is reached in a t
	 * probability distribution with the specified degrees of freedom
	 *
	 * @param area             Area or percentile for the desired t score
	 * @param degreesOfFreedom Degrees of freedom of the t distribution
	 * @return The t score for the given area, computed via an inverse probability function
	 */
	public static double inverseTScore(double area, double degreesOfFreedom) {
		TDistribution tDistribution = new TDistribution(degreesOfFreedom);
		return tDistribution.inverseCumulativeProbability(area);
	}
	
	/**
	 * Cumulative distribution function of Student's t-distribution centered at 0.
	 *
	 * @param degreesOfFreedom The number of degrees of freedom of the model, where 0 &lt;=
	 *                         <code>degreesOfFreedom</code>
	 * @param tScore           The upper limit of the cumulative distribution function
	 * @return The cumulative distribution of the model from -infinity to <code>tScore</code>
	 */
	public static double tCumulativeDistribution(double degreesOfFreedom, double tScore) {
		TDistribution tDistribution = new TDistribution(degreesOfFreedom);
		return tDistribution.cumulativeProbability(tScore);
	}
	
	/**
	 * Retrieves the lower bound of a t confidence interval with the specified confidence level
	 *
	 * @param confidenceLevel   Confidence level of the interval, must be between 0.0 and 1.0
	 * @param degreesOfFreedom  Degrees of freedom of the t distribution
	 * @param mean              Center value of the t distribution
	 * @param standardDeviation Standard deviation of the t distribution
	 * @return Lower bound of the generated t confidence interval
	 */
	public static double getLowerBoundTConfidenceInverval(double confidenceLevel, double degreesOfFreedom,
														  double mean, double standardDeviation) {
		double percentile = (1 - confidenceLevel) / 2;
		return inverseTValue(percentile, degreesOfFreedom, mean, standardDeviation);
	}
	
	/**
	 * Calculates the t-score of a two-sample t-test of the difference of two independent
	 * random variables
	 *
	 * @param meanOne          The sample mean of the first variable
	 * @param standardErrorOne The standard error of the first variable
	 * @param meanTwo          The sample mean of the second variable
	 * @param standardErrorTwo The standard error of the second variable
	 * @return The t statistic of the difference of the two sample means, based on Welch's t-test
	 */
	public static double twoSampleMeanTScore(double meanOne, double standardErrorOne, double meanTwo,
											 double standardErrorTwo) {
		return (meanOne - meanTwo) / sumStandardDeviation(new double[]{standardErrorOne, standardErrorTwo});
	}
	
	/**
	 * Computes the standard deviation of a random variable that is the sum of several independent random variables
	 * e.g. Returns SD(X) where X = Y + Z and {SD(Y), SD(Z)} is the argument
	 *
	 * @param standardDeviations The standard deviations of the random variables that make up the larger random
	 *                           variable
	 * @return The standard deviation of the composite random variable
	 */
	public static double sumStandardDeviation(double[] standardDeviations) {
		double totalVariance = 0.0;
		
		for (double standardDeviation : standardDeviations) {
			totalVariance += Math.pow(standardDeviation, 2);
		}
		
		return Math.sqrt(totalVariance);
	}
	
	/**
	 * Calculates the degrees of freedom in the distribution of a two-sample t-test with two independent random
	 * variables, as given by the Welch-Satterthwaite equation
	 *
	 * @param standardErrorOne The standard error of the first variable
	 * @param sampleSizeOne    The sample size of the first variable
	 * @param standardErrorTwo The standard error of the second variable
	 * @param sampleSizeTwo    The standard error of the second variable
	 * @return The degrees of freedom of the model as given by the Welch–Satterthwaite equation
	 */
	public static double twoSampleDegreesOfFreedom(double standardErrorOne, double sampleSizeOne,
												   double standardErrorTwo, double sampleSizeTwo) {
		double numerator = Math.pow(Math.pow(standardErrorOne, 2) + Math.pow(standardErrorTwo, 2), 2);
		double denominator =
			Math.pow(standardErrorOne, 4) / (sampleSizeOne - 1) + Math.pow(standardErrorTwo, 4) / (sampleSizeTwo - 1);
		return numerator / denominator;
	}
}
