package org.usfirst.frc.team25.scouting.data;


import com.thebluealliance.api.v3.models.Match;
import com.thebluealliance.api.v3.models.SimpleMatch;
import com.thebluealliance.api.v3.models.SimpleTeam;
import com.thebluealliance.api.v3.models.Team;
import org.usfirst.frc.team25.scouting.data.models.ScoutEntry;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Collection of static methods to sort and filter ArrayLists of object models
 *
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
     * Removes all Matches from a list besides qualification matches.
     * Qualification matches denoted by <code>comp_level</code> "qm"
     *
     * @param matches ArrayList of Match objects
     * @return Modified ArrayList
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
     * Sorts the keys of a HashMap in ascending or descending order, based on their values
     *
     * @param unsortMap Unsorted HashMap
     * @param order     true to sort the values of the HashMap in ascending order, false for descending order
     * @return A sorted version of unsortMap
     */
    public static HashMap<Integer, Integer> sortByComparator(HashMap<Integer, Integer> unsortMap, final boolean order) {

        List<Map.Entry<Integer, Integer>> list = new LinkedList<>(unsortMap.entrySet());

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
}
