package org.usfirst.frc.team25.scouting.data;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * @author sng
 */
class Statistics {


    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double percent(ArrayList<Object> dataset, String metricName) {
        double percent = 0;
        try{
            percent = sum(dataset,metricName,true)/ dataset.size() * 100;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return percent;

    }


    /**
     * Calculates the uncorrected standard deviation of an event
     *
     * @param dataset Array with data points
     * @return Uncorrected standard deviation
     */
    public static double standardDeviation(ArrayList<Object> dataset, String metricName, boolean isBoolean) {
        double average = average(dataset, metricName);
        double sumSquareDev = 0;

        try {
            if (isBoolean) {
                Method correctMethod = getCorrectMethod(dataset.get(0).getClass(), metricName, 2);
                if (correctMethod != null) {
                    for (Object dataObject : dataset) {
                        sumSquareDev += Math.pow(((boolean)correctMethod.invoke(dataObject) ? 1.0 : 0.0) - average, 2);
                    }
                }
            } else {
                Method correctMethod = getCorrectMethod(dataset.get(0).getClass(), metricName, 3);
                if (correctMethod != null) {
                    for (Object dataObject : dataset) {
                        sumSquareDev += Math.pow((double) correctMethod.invoke(dataObject) - average, 2);
                    }
                }
            }
        }catch (Exception e) {
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
        return sum(dataset, metricName, false) / dataset.size();
    }

    /**
     * Calculates the sum of an array of numbers
     *
     * @param dataset Array of numbers to be summed
     * @return Sum of the elements in <code>dataset</code>
     */
    public static double sum(ArrayList<Object> dataset, String metricName,boolean isBooleanMetric) {
        double sum = 0;
        try {
            if(isBooleanMetric){
                Method correctMethod = getCorrectMethod(dataset.get(0).getClass(), metricName,2);
                if (correctMethod != null) {
                    for (Object dataObject : dataset) {
                        sum += (boolean)(correctMethod.invoke(dataObject)) ? 1 : 0;
                    }
                }
            }
            else{
                Method correctMethod = getCorrectMethod(dataset.get(0).getClass(), metricName,3);
                if (correctMethod != null) {
                    for (Object dataObject : dataset) {
                        sum += 1.0 * (Integer) correctMethod.invoke(dataObject);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sum;

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

}
 