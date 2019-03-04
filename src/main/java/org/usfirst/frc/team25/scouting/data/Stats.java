package org.usfirst.frc.team25.scouting.data;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;

class Stats {


    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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
     * Calculates the uncorrected standard deviation of an event
     *
     * @param dataset Array with data points
     * @return Uncorrected standard deviation
     */
    public static double standardDeviation(double[] dataset) {
        double average = average(dataset);
        double sumSquareDev = 0;

        for (double num : dataset) {
            sumSquareDev += Math.pow(num - average, 2);
        }

        return Math.sqrt(sumSquareDev / (dataset.length - 1));
    }

    public static double standardError(double[] dataset) {
        return standardError(standardDeviation(dataset), dataset.length);
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

    public static double[] getDoubleArray(ArrayList<Object> dataset, String metricName, Class metricType) {
        double[] resultArray = new double[dataset.size()];

        int shiftIndex = 3;

        if (metricType.equals(boolean.class)) {
            shiftIndex = 2;
        }

        Method correctMethod = getCorrectMethod(dataset.get(0).getClass(), metricName, shiftIndex);

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

    public static Method getCorrectMethod(Class dataObjectClass, String metricName, int shiftIndex) {
        Method correctGetter = null;

        for (Method m : dataObjectClass.getMethods()) {
            if (m.getName().substring(shiftIndex).equalsIgnoreCase(metricName) && m.getParameterTypes().length == 0) {
                correctGetter = m;
                break;
            }
        }

        return correctGetter;
    }

    public static double randomNormalValue(double[] dataset) {
        return randomNormalValue(average(dataset), standardDeviation(dataset));
    }

    public static double randomNormalValue(double mean, double standardDeviation) {
        Random r = new Random();
        return Math.max(r.nextGaussian() * standardDeviation + mean, 0);
    }

    public static double randomTValue(double[] dataset) {
        TDistribution tDistribution = new TDistribution(dataset.length - 1);
        return Math.max(tDistribution.sample() * standardError(dataset) + average(dataset), 0);
    }

    public static double standardError(double standardDeviation, int sampleSize) {
        return standardDeviation / Math.sqrt(sampleSize);
    }

    public static double inverseTValue(double percentile, double[] dataset) {
        return inverseTValue(percentile, dataset.length - 1, average(dataset), standardDeviation(dataset));
    }

    public static double standardDeviation(int attempts, int successes) {
        if (attempts < 2) {
            return 0.0;
        }

        return Math.sqrt(((double) successes * (attempts - successes)) / (attempts * (attempts - 1)));
    }

    public static double sumStandardDeviation(double[] standardDeviations) {
        double totalVariance = 0.0;

        for (double standardDeviation : standardDeviations) {
            totalVariance += Math.pow(standardDeviation, 2);
        }

        return Math.sqrt(totalVariance);
    }

    public static double multiplyVariance(double constant, double standardDeviation) {
        return Math.pow(constant, 2) * Math.pow(standardDeviation, 2);
    }

    public static double rightTailNormalProbability(double value, double mean, double standardDeviation) {
        return normalCumulativeDistributionFunction(value, Double.MAX_VALUE, mean, standardDeviation);
    }

    public static double normalCumulativeDistributionFunction(double lowerBound, double upperBound, double mean,
                                                              double standardDeviation) {
        NormalDistribution normalDistribution = new NormalDistribution(mean, standardDeviation);
        return normalDistribution.probability(lowerBound, upperBound);
    }

    /**
     * Calculates the arithmetic mean of a dataset
     *
     * @param dataset Array of numbers
     * @return Average of entries in array, 0 if dataset.size() is 0
     */
    public static double average(double[] dataset) {
        if (dataset.length == 0) {
            return 0;
        }

        return sum(dataset) / dataset.length;
    }

    public static double inverseTValue(double percentile, double degreesOfFreedom, double mean,
                                       double standardDeviation) {
        double tScore = inverseTScore(percentile, degreesOfFreedom);
        double standardError = standardError(standardDeviation, (int) degreesOfFreedom + 1);
        return tScore * standardError + mean;

    }

    public static double inverseTScore(double area, double degreesOfFreedom) {
        TDistribution tDistribution = new TDistribution(degreesOfFreedom);
        return tDistribution.inverseCumulativeProbability(area);
    }

    public static double getUpperBoundZConfidenceInverval(double confidenceLevel, double mean,
                                                          double standardDeviation) {
        double percentile = confidenceLevel + (1 - confidenceLevel) / 2;
        return inverseZValue(percentile, mean, standardDeviation);
    }

    public static double inverseZValue(double percentile, double mean, double standardDeviation) {
        double zScore = inverseZScore(percentile);

        return zScore * standardDeviation + mean;

    }

    public static double inverseZScore(double area) {
        NormalDistribution normalDistribution = new NormalDistribution();
        return normalDistribution.inverseCumulativeProbability(area);
    }

    public static double getUpperBoundTConfidenceInverval(double confidenceLevel, double degreesOfFreedom,
                                                          double mean, double standardDeviation) {
        double percentile = confidenceLevel + (1 - confidenceLevel) / 2;
        return inverseTValue(percentile, degreesOfFreedom, mean, standardDeviation);
    }

    public static double getLowerBoundTConfidenceInverval(double confidenceLevel, double degreesOfFreedom,
                                                          double mean, double standardDeviation) {
        double percentile = (1 - confidenceLevel) / 2;
        return inverseTValue(percentile, degreesOfFreedom, mean, standardDeviation);
    }


}
 