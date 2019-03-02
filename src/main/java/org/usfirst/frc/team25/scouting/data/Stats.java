package org.usfirst.frc.team25.scouting.data;

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

    /**
     * Calculates arithmetic mean of a dataset
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

    public static double standardError(double[] dataset) {
        return standardDeviation(dataset) / Math.sqrt(dataset.length);
    }

}
 