package gs.web.jsp;

import gs.data.state.State;
import gs.data.state.StateManager;

import java.util.Random;

import org.apache.commons.lang.StringUtils;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class Util {

    /**
     * Lowercases a String.
     * @param word - may be null
     * @return a lowercase <code>String</code> or null
     * if the word argument is null.
     */
    public static String toLowercase(String word) {
        String lowered = null;
        if (word != null) {
            lowered = word.toLowerCase();
        }
        return lowered;
    }

    /**
     * Returns the string odd or even depending on the Integer
     * Will throw NPE if null is passed
     *
     * @param number
     * @return "odd", "even", or "null"
     * @throws NullPointerException if null is passed
     */
    public static String oddOrEven(Integer number) {
        String oddOrEven = "odd";
        if (number.intValue() % 2 == 0) {
            oddOrEven = "even";
        }
        return oddOrEven;
    }

    /**
     * Generate a random integer between 0 (inclusive) and upperLimit (exclusive)
     * @param upperLimit Th
     * @return random number
     */
    public static int randomNumber(int upperLimit) {
        Random r = new Random();
        return r.nextInt(upperLimit);
    }

    /**
     * A utility method to return a state's long name from a 2-char
     * abbreviation.  Uses State getDatabaseState(abbrev) to first get the State.
     *
     * @param abbr The 2 letter abbreviation (case-insensitive)
     * @return a <code>String</code> or null if the provide string doesn't
     * match any state.
     */
    public static String getStateName(String abbr) {
        String stateName = null;
        StateManager sm = new StateManager();
        if (abbr != null) {
            State state = sm.getState(abbr);
            if (state != null) {
                stateName = state.getLongName();
            }
        }
        return stateName;
    }

    /**
     * Takes an array of objects and returns a comma-delimited string of the
     * objects String values concatinated together.  This method will always
     * return an non-null String.  If the array argument is null or empty, an
     * empty String is returned.
     */
    public static String toDelimitedString(Object[] array) {
        StringBuffer buffer = new StringBuffer("");
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] != null) {
                    buffer.append(array[i].toString());
                }
                if (i < array.length-1) {
                    buffer.append(",");
                }
            }
        }
        return buffer.toString();
    }
    /**
     * Takes an array of objects and returns a +-delimited string of the
     * objects String values concatinated together.  This method will always
     * return an non-null String.  If the array argument is null or empty, an
     * empty String is returned. The individual String values are capitalized
     * first.
     */
    public static String toUglyDelimitedString(Object[] array) {
        StringBuffer buffer = new StringBuffer("");
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] != null) {
                    buffer.append(capitalize(array[i].toString()));
                }
                if (i < array.length-1) {
                    buffer.append("+");
                }
            }
        }
        return buffer.toString();
    }

    /**
     * Returns a capitalized version of the supplied String.  If the argument
     * is null, an empty String is returned.
     * @param s A <code>String</code> object
     * @return a non-null <code>String</code> type
     */
    public static String capitalize(String s) {
        String capString = "";
        if (!StringUtils.isBlank(s)) {
            StringBuffer buffer = new StringBuffer(s.length());
            buffer.append(s.substring(0, 1).toUpperCase());
            buffer.append(s.substring(1));
            capString = buffer.toString();
        }
        return capString;
    }
}
