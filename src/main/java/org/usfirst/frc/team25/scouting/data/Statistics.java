package org.usfirst.frc.team25.scouting.data;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * @author sng
 */
class Statistics {

    public static double percentAtLeastOne(double p1, double p2, double p3) {
        return 100 - percentNone(p1, p2, p3);
    }

    public static double percentNone(double p1, double p2, double p3) {
        return 100 * (1 - p1) * (1 - p2) * (1 - p3);
    }

    public static double percentAtLeastTwo(double p1, double p2, double p3) {
        return 100 - percentNone(p1, p2, p3) - percentExactlyOne(p1, p2, p3);
    }

    public static double percentExactlyOne(double p1, double p2, double p3) {
        return 100 * (p1 * (1 - p2) * (1 - p3) + p2 * (1 - p1) * (1 - p3) + p3 * (1 - p1) * (1 - p3));
    }

    public static double percentAll(double p1, double p2, double p3) {
        return 100 * p1 * p2 * p3;
    }


    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


    /**
     * Calculates the uncorrected standard deviation of an event
     *
     * @param dataset Array with data points
     * @return Uncorrected standard deviation
     */
    public static double standardDeviation(ArrayList<Object> dataset, String metricName) {
        double average = average(dataset, metricName);
        double sumSquareDev = 0;

        try {
            Method correctMethod = getCorrectMethod((Class) dataset.get(0).getClass(), metricName);
            if (correctMethod != null) {
                for (Object dataObject : dataset) {
                    sumSquareDev += Math.pow((double) correctMethod.invoke(dataObject) - average, 2);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Math.sqrt(sumSquareDev / (dataset.size() - 1));
    }

    /**
     * Calculates arithmetic mean of a dataset
     *
     * @param dataset Array of numbers
     * @return Average of entries in array, 0 if dataset.size() is 0
     */
    public static double average(ArrayList<Object> dataset, String metricName) {
        if (dataset.size() == 0) {
            return 0;
        }
        return sum(dataset, metricName) / dataset.size();
    }

    /**
     * Calculates the sum of an array of numbers
     *
     * @param dataset Array of numbers to be summed
     * @return Sum of the elements in <code>dataset</code>
     */
    public static double sum(ArrayList<Object> dataset, String metricName) {
        double sum = 0;


        try {
            Method correctMethod = getCorrectMethod(dataset.get(0).getClass(), metricName);
            if (correctMethod != null) {
                for (Object dataObject : dataset) {
                    sum += 1.0 * (Integer) correctMethod.invoke(dataObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sum;

    }

    private static Method getCorrectMethod(Class dataObjectClass, String metricName) {
        Method correctGetter = null;

        for (Method m : dataObjectClass.getMethods()) {
            if (m.getName().substring(3).toLowerCase().equals(metricName.toLowerCase()) && m.getParameterTypes().length == 0) {
                correctGetter = m;
                break;
            }
        }

        return correctGetter;
    }

}
 