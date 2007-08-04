package gs.web.jsp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.Date;
import java.util.Random;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class Util {

    private static final Log _log = LogFactory.getLog(Util.class);
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
     * @return a <code>String</code>
     * @param array An array of objects
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
     * @return a <code>String</code>
     * @param array An array of objects
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

    /**
     * Abbreviates a string - if a string is longer than maxLength characters, then
     * truncate at a word boundary and append "..."  The resulting string will be
     * no longer than maxLength <em>inlucding</em> the "..."
     * Null will be returned if a null String is passed as the comment
     * @param s a comment String
     * @param maxLength the maximum lenght the comment may be before truncation, must be
     * 3 or more.
     * @return a formatted String
     */
    public static String abbreviateAtWhitespace(String s, int maxLength) {
        if (maxLength > 2) {
            if (StringUtils.isNotBlank(s)) {
                s = s.trim();
                if (s.length() > maxLength) {
                    int ind = s.lastIndexOf(" ", maxLength);
                    if (ind < 0) ind = maxLength;
                    s = s.substring(0, ind);
                    if (!s.matches(".*[\\.\\?\\!]$")) {
                        if (s.length() > maxLength-3) {
                            int ind2 = s.lastIndexOf(" ", s.length()-3);
                            if (ind2 < 0) { ind2 = s.length()-3; }
                            s = s.substring(0, ind2);
                        }
                        if (!s.matches(".*[\\.\\?\\!]$")) {
                            s = s.trim() + "...";
                        }
                    }
                }
            }
            return s;
        } else {
            throw new IllegalArgumentException("maxLength must be > 2; now: " + maxLength);
        }
    }

    /**
     * Given a count and a word, append 's' to word if count is not equal to 1
     *
     * @param count number of items
     * @param singular singular form
     * @return pluralized word
     *
     */
    public static String pluralize(int count, String singular) {
        if (null == singular) {
            throw new IllegalArgumentException("Input cannot be null");
        } else {
            return (count == 1) ? singular : singular + "s";
        }
    }

    /**
     * Given a count, word, and plural word, return singular form if count is 1
     * and plural form if count is not equal to one
     *
     * @param count number of items
     * @param singular singular form
     * @param plural plural form
     * @return pluralized word
     *
     */
    public static String pluralize(int count, String singular, String plural) {
        if (null == singular || null == plural) {
            throw new IllegalArgumentException("Input cannot be null");
        } else {
            return (count == 1) ? singular : plural;
        }
    }

    /**
     * Wordify the period between two dates.
     *
     * Our GS style is one to nine, we write out the word, for  10 and above we use numerals.
     *
     * If period is over a year, the year and month is part of the return string.
     *      Ex: one year and two months ago, one year and two months ago
     *
     * If period is over a month, the month is part of the return string.
     *      Ex: one month ago, two months ago
     *
     * If period is under a month, the number of days is part of the return string.
     *      Ex: one day ago, 20 days ago
     *
     * If start and end date is the same day, then 'today' is returned
     *
     * @param start Start date
     * @param end End date
     * @return empty string if any dates are null or if end date prior to start date,
     *          period otherwise.
     */
    public static String periodBetweenDates(Date start, Date end) {

        //style guide...0-9 we write out the word, 10 and above we use numerals
        //0 is never used, just so we don't have to offset
        final String [] numAsWord = {
                "zero",
                "one", "two", "three", "four", "five",
                "six", "seven", "eight", "nine", "10",
                "11", "12", "13", "14", "15",
                "16", "17", "18", "19", "20",
                "21", "22", "23", "24", "25",
                "26", "27", "28", "29", "30",
                "31"
        };

        if (null == start || null == end) {
            return "";
        }

        DateTime dtStart = new DateTime(start.getTime());
        DateTime dtEnd = new DateTime(end.getTime());

        if (dtStart.isAfter(dtEnd)) {
            _log.error("End date prior to start date.  Start date: " + dtStart + ".  End date: " + dtEnd);
            return "";
        }

        if (dtStart.isEqual(dtEnd)) {
            return "today";
        }

        Period period = new Period(dtStart, dtEnd, PeriodType.yearMonthDay());
        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        StringBuffer buffer = new StringBuffer(30);

        if (years > 0) {
            buffer.append(numAsWord[years]).append(pluralize(years, " year"));
            if (months > 0) {
                buffer.append(" and ").append(numAsWord[months]).append(pluralize(months, " month"));
            }
        } else if (months > 0) {
            buffer.append(numAsWord[months]).append(pluralize(months, " month"));
        } else {
            buffer.append(numAsWord[days]).append(pluralize(days, " day"));
        }
        buffer.append(" ago");

        return buffer.toString();
    }

    public static String escapeHtml(String input) {
        if (input != null) {
            return input.replace("<", "&lt;");
        }
        return null;
    }
}
