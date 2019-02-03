package org.usfirst.frc.team25.scouting.data;

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

    private static double percentNone(double p1, double p2, double p3) {
        return 100 * (1 - p1) * (1 - p2) * (1 - p3);
    }

    public static double percentAtLeastTwo(double p1, double p2, double p3) {
        return 100 - percentNone(p1, p2, p3) - percentExactlyOne(p1, p2, p3);
    }

    private static double percentExactlyOne(double p1, double p2, double p3) {
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

    public static ArrayList<Double> toDoubleArrayList(ArrayList<Integer> arr) {
        ArrayList<Double> toReturn = new ArrayList<>();
        for (int i : arr) {
            toReturn.add((double) i);
        }
        return toReturn;
    }

    /**
     * Calculates the uncorrected standard deviation of an event
     *
     * @param dataset Array with data points
     * @return Uncorrected standard deviation
     */

    public static double popStandardDeviation(ArrayList<Double> dataset) {
        double average = average(dataset);
        double sumSquareDev = 0;
        for (double i : dataset) {
            sumSquareDev += Math.pow(i - average, 2);
        }
        return Math.sqrt(sumSquareDev / dataset.size());
    }

    /**
     * Calculates arithmetic mean of a dataset
     *
     * @param dataset Array of numbers
     * @return Average of entries in array, 0 if dataset.size() is 0
     */
    private static double average(ArrayList<Double> dataset) {
        if (dataset.size() == 0) {
            return 0;
        }
        return sum(dataset) / dataset.size();
    }




    /* Calculates the success rate of an event in all matches

      @param success Array with number of successes
     * @param total Array with number of attempts
     * @return Success percentage of event
     */
	/*public static double successPercentage(int[] success, int[] total){
		return (((double)sum(success))/sum(total))*100;
	}
	
	/** Calculates the corrected standard deviation of an event
	 * 
	 * @param dataset Array with data points 
	 * @return Corrected standard deviation (length-1)
	 */
	/*public static double standardDeviation(int[] dataset){
		double average = average(dataset);
		double sumSquareDev = 0;
		for(int i : dataset)
			sumSquareDev+= Math.pow(i-average, 2);
		return Math.sqrt(sumSquareDev/(dataset.length-1));
	}
	
	public static double standardDeviation(double[] dataset){
		double average = average(dataset);
		double sumSquareDev = 0;
		for(double i : dataset)
			sumSquareDev+= Math.pow(i-average, 2);
		return Math.sqrt(sumSquareDev/(dataset.length-1));
	}*/

    /**
     * Calculates the sum of an array of numbers
     *
     * @param dataset Array of numbers to be summed
     * @return Sum of the elements in <code>dataset</code>
     */
    private static double sum(ArrayList<Double> dataset) {
        double sum = 0;

        for (double i : dataset) {
            sum += i;
        }
        return sum;

    }

}
