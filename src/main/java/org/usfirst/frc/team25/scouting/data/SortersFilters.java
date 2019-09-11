package org.usfirst.frc.team25.scouting.data;


import com.thebluealliance.api.v3.models.Match;
import com.thebluealliance.api.v3.models.SimpleMatch;
import com.thebluealliance.api.v3.models.SimpleTeam;
import com.thebluealliance.api.v3.models.Team;
import org.usfirst.frc.team25.scouting.data.models.ScoutEntry;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Collection of static methods to sort, search, and filter through various data structures
 */
public class SortersFilters {

    /**
     * Method implementing a Comparator to sort Matches
     *
     * @param matches ArrayList of Match objects
     * @return Same list, sorted by ascending match number
     */
    static ArrayList<Match> sortByMatchNum(ArrayList<Match> matches) {
        matches.sort(Comparator.comparingInt(SimpleMatch::getMatchNumber));
        return matches;
    }

    /**
     * Method implementing a Comparator to sort Teams
     *
     * @param events List of Team objects
     * @return Same list, sorted by ascending team number
     */
    static ArrayList<Team> sortByTeamNum(ArrayList<Team> events) {

        events.sort(Comparator.comparingInt(SimpleTeam::getTeamNumber));
        return events;
    }


    /**
     * Sorts the keys of a HashMap in ascending or descending order, based on their values
     *
     * @param unsortedMap Unsorted HashMap
     * @param order       true to sort the values of the HashMap in ascending order, false for descending order
     * @return A sorted version of unsortedMap
     */
    public static HashMap<Integer, Double> sortByComparatorDouble(HashMap<Integer, Double> unsortedMap,
                                                                  final boolean order) {

        List<Map.Entry<Integer, Double>> list = new LinkedList<>(unsortedMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> {
            if (order) {
                return o1.getValue().compareTo(o2.getValue());
            } else {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<Integer, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    /**
     * Sorts the keys of a HashMap in ascending or descending order, based on their values
     *
     * @param unsortedMap Unsorted HashMap
     * @param order       True to sort the values of the HashMap in ascending order, false for descending order
     * @return A sorted version of unsortedMap
     */
    public static HashMap<Integer, Integer> sortByComparator(HashMap<Integer, Integer> unsortedMap,
                                                             final boolean order) {

        List<Map.Entry<Integer, Integer>> list = new LinkedList<>(unsortedMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> {
            if (order) {
                return o1.getValue().compareTo(o2.getValue());
            } else {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<Integer, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    /**
     * Removes all Matches from a list besides qualification matches.
     * Qualification matches are denoted by <code>comp_level</code> "qm"
     *
     * @param matches ArrayList of Match objects
     * @return Modified ArrayList with only qualification matches
     */
    static ArrayList<Match> filterQualification(ArrayList<Match> matches) {
        for (int i = 0; i < matches.size(); i++) {

            if (!matches.get(i).getCompLevel().equals("qm")) {
                matches.remove(i);
                i--;
            }
        }
        return matches;
    }

    /**
     * Extracts a particular data object model from an array of scout entries
     *
     * @param scoutEntries ArrayList of all scout entries from an event to filter
     * @param objectClass  Class reference to the desired data object model to filter (e.g. TeleOp.class)
     * @return ArrayList of the desired data object from all entries
     */
    public static ArrayList<Object> filterDataObject(ArrayList<ScoutEntry> scoutEntries, Class objectClass) {
        ArrayList<Object> filteredList = new ArrayList<>();

        Method correctGetter = null;

        for (Method m : ScoutEntry.class.getMethods()) {
            if (m.getName().substring(3).toLowerCase().equals(objectClass.getSimpleName().toLowerCase()) && m.getParameterTypes().length == 0) {
                correctGetter = m;
                break;
            }
        }

        try {
            if (correctGetter != null) {
                for (ScoutEntry entry : scoutEntries) {
                    filteredList.add(correctGetter.invoke(entry));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filteredList;
    }

    /**
     * Extracts the method object associated with the getter for a particular metric
     *
     * @param dataObjectClass Data object model class containing the desired getter method
     * @param metricName      String name of the metric to be queried, in camel case
     * @param shiftIndex      Number of letters in the prefix of the IntelliJ auto-generated getter name (i.e. 2 for
     *                        "is" and 3 for "get")
     * @return The desired method object, null if the class does not contain the getter
     */
    public static Method getCorrectGetter(Class dataObjectClass, String metricName, int shiftIndex) {
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
