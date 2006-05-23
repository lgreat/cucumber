package gs.web.jsp;

import org.apache.commons.lang.StringUtils;

import java.util.Random;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class Util {


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
     * @deprecated
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

    /**
     * Returns the supplied string with leading and trailing double quotes removed.
     * Single quotes are ingored, as are double quotes within the string.  This method
     * first calls trim() on the string to removed leading and trailing whitespace.
     * A lone double quote returns an empty string.
     * An empty string returns an empty string.
     * null returns null.
     * @param quoted - a quoted <code>String</code>
     * @return an "unquoted" <code>String</code>
     */
    public static String unquote(String quoted) {
        String s = quoted;
        if (StringUtils.isNotBlank(s)) {
            s = s.trim();
            s = s.replaceAll("^\"", ""); // remove leading quote
            s = s.replaceAll("\"$", ""); // remove trailing quote
        }
        return s;
    }
}
