package org.usfirst.frc.team25.scouting.data;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;

public class Stats {


    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Calculates the sum of an array of numbers
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
     * Calculates the standard deviations of sets of data from independent events
     *
     * @param datasets An array of <code>double</code> arrays that contain sample data points of a metric
     * @return The standard deviation of the data
     */
    public static double standardDeviation(double[][] datasets) {
        double totalVariance = 0.0;

        for (double[] dataset : datasets) {
            totalVariance += Math.pow(standardDeviation(dataset), 2);
        }

        return Math.sqrt(totalVariance);
    }

    /**
     * @param attempts
     * @param successes
     * @return
     */
    public static double standardDeviation(int attempts, int successes) {
        if (attempts < 2) {
            return 0.0;
        }

        return Math.sqrt(((double) successes * (attempts - successes)) / (attempts * (attempts - 1)));
    }

    /**
     * Calculates the uncorrected standard deviation of an event
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
     *
     * @param dataset
     * @return
     */
    public static double standardError(double[] dataset) {
        return standardError(standardDeviation(dataset), dataset.length);
    }

    /**
     *
     * @param standardDeviation
     * @param sampleSize
     * @return
     */
    public static double standardError(double standardDeviation, double sampleSize) {

        return standardDeviation / Math.sqrt(sampleSize);
    }

    /**
     *
     * @param constant
     * @param standardDeviation
     * @return
     */
    public static double multiplyVariance(double constant, double standardDeviation) {
        return Math.pow(constant, 2) * Math.pow(standardDeviation, 2);
    }

    /**
     *
     * @param percentile
     * @param mean
     * @param standardDeviation
     * @return
     */
    public static double inverseZValue(double percentile, double mean, double standardDeviation) {
        double zScore = inverseZScore(percentile);

        return zScore * standardDeviation + mean;

    }

    /**
     * @param area
     * @return
     */
    public static double inverseZScore(double area) {
        NormalDistribution normalDistribution = new NormalDistribution();
        return normalDistribution.inverseCumulativeProbability(area);
    }

    /**
     *
     * @param mean
     * @param standardDeviation
     * @return
     */
    public static double randomNormalValue(double mean, double standardDeviation) {
        Random r = new Random();
        return Math.max(r.nextGaussian() * standardDeviation + mean, 0);
    }

    /**
     * @param value
     * @param mean
     * @param standardDeviation
     * @return
     */
    public static double rightTailNormalProbability(double value, double mean, double standardDeviation) {
        return normalCumulativeDistribution(value, Double.MAX_VALUE, mean, standardDeviation);
    }

    /**
     *
     * @param lowerBound
     * @param upperBound
     * @param mean
     * @param standardDeviation
     * @return
     */
    public static double normalCumulativeDistribution(double lowerBound, double upperBound, double mean,
                                                      double standardDeviation) {
        NormalDistribution normalDistribution = new NormalDistribution(mean, standardDeviation);
        return normalDistribution.probability(lowerBound, upperBound);
    }

    /**
     * Cumulative distribution function of Student's t-distribution centered at 0.
     *
     * @param degreesOfFreedom The number of degrees of freedom of the model, where 0 <= <code>degreesOfFreedom</code>
     * @param tScore           The upper limit of the cumulative distribution function
     * @return The cumulative distribution of the model from -infinity to <code>tScore</code>
     */
    public static double tCumulativeDistribution(double degreesOfFreedom, double tScore) {
        TDistribution tDistribution = new TDistribution(degreesOfFreedom);
        return tDistribution.cumulativeProbability(tScore);
    }

    /**
     * @param percentile
     * @param dataset
     * @return
     */
    public static double inverseTValue(double percentile, double[] dataset) {
        return inverseTValue(percentile, dataset.length - 1, mean(dataset), standardDeviation(dataset));
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
     * @param percentile
     * @param degreesOfFreedom
     * @param mean
     * @param standardDeviation
     * @return
     */
    public static double inverseTValue(double percentile, double degreesOfFreedom, double mean,
                                       double standardDeviation) {
        double tScore = inverseTScore(percentile, degreesOfFreedom);
        double standardError = standardError(standardDeviation, (int) degreesOfFreedom + 1);
        return tScore * standardError + mean;

    }

    /**
     *
     * @param area
     * @param degreesOfFreedom
     * @return
     */
    public static double inverseTScore(double area, double degreesOfFreedom) {
        TDistribution tDistribution = new TDistribution(degreesOfFreedom);
        return tDistribution.inverseCumulativeProbability(area);
    }

    /**
     *
     * @param confidenceLevel
     * @param degreesOfFreedom
     * @param mean
     * @param standardDeviation
     * @return
     */
    public static double getUpperBoundTConfidenceInverval(double confidenceLevel, double degreesOfFreedom,
                                                          double mean, double standardDeviation) {
        double percentile = confidenceLevel + (1 - confidenceLevel) / 2;
        return inverseTValue(percentile, degreesOfFreedom, mean, standardDeviation);
    }

    /**
     *
     * @param confidenceLevel
     * @param degreesOfFreedom
     * @param mean
     * @param standardDeviation
     * @return
     */
    public static double getLowerBoundTConfidenceInverval(double confidenceLevel, double degreesOfFreedom,
                                                          double mean, double standardDeviation) {
        double percentile = (1 - confidenceLevel) / 2;
        return inverseTValue(percentile, degreesOfFreedom, mean, standardDeviation);
    }

    /**
     * Calculates the t-score of a two-sample t-test with two independent random variables
     *
     * @param meanOne          The sample mean of the first variable
     * @param standardErrorOne The standard error of the first variable
     * @param meanTwo          The sample mean of the second variable
     * @param standardErrorTwo The standard error of the second variable
     * @return The t statistic of the two sample means, based on Welch's t-test
     */
    public static double twoSampleMeanTScore(double meanOne, double standardErrorOne, double meanTwo,
                                             double standardErrorTwo) {
        return (meanOne - meanTwo) / sumStandardDeviation(new double[]{standardErrorOne, standardErrorTwo});
    }

    /**
     *
     * @param standardDeviations
     * @return
     */
    public static double sumStandardDeviation(double[] standardDeviations) {
        double totalVariance = 0.0;

        for (double standardDeviation : standardDeviations) {
            totalVariance += Math.pow(standardDeviation, 2);
        }

        return Math.sqrt(totalVariance);
    }

    /**
     * Calculates the degrees of freedom in a two-sample t-test with two independent random variables
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
                Math.pow(standardErrorOne, 4) * 1 / (sampleSizeOne - 1) + Math.pow(standardErrorTwo, 4) * 1 / (sampleSizeTwo - 1);
        return numerator / denominator;
    }

    /**
     *
     * @param dataset
     * @param metricName
     * @param metricType
     * @return
     */
    public static double[] getDoubleArray(ArrayList<Object> dataset, String metricName, Class metricType) {
        double[] resultArray = new double[dataset.size()];

        int shiftIndex = 3;

        if (metricType.equals(boolean.class)) {
            shiftIndex = 2;
        }

        Method correctMethod = SortersFilters.getCorrectGetter(dataset.get(0).getClass(), metricName, shiftIndex);

        for (int i = 0; i < dataset.size(); i++) {
            try {
                if (metricType.equals(boolean.class)) {
                    resultArray[i] = (boolean) correctMethod.invoke(dataset.get(i)) ? 1.0 : 0.0;
                } else if (metricType.equals(int.class)) {
                    resultArray[i] = ((Integer) correctMethod.invoke(dataset.get(i))).doubleValue();
                } else if (metricType.equals(double.class)) {
                    resultArray[i] = (Double) correctMethod.invoke(dataset.get(i));
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return resultArray;
    }


}
 