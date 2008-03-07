package gs.web.jsp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class Util {

    private static final Log _log = LogFactory.getLog(Util.class);

    /**
     * Ex: June 25, 2003
     */
    public static DateFormat LONG_DATE_FORMAT = new SimpleDateFormat("MMMMM d, yyyy");

    /**
     * Ex:  June 25
     */
    public static DateFormat NO_YEAR_DATE_FORMAT = new SimpleDateFormat("MMMMM d");

    /**
     *  Ex:  Monday, June 25
     */
    public static DateFormat DAY_OF_WEEK_MONTH_DAY_YEAR_FORMAT = new SimpleDateFormat("EEEE, MMMM d, yyyy");

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
     * Abbreviates a string - if a string is longer than maxLength characters,
     * then truncate at a word boundary and append "..."  The resulting string
     * will be no longer than maxLength <em>inlucding</em> the "..."
     * Null will be returned if a null String is passed as the comment
     * @param s a comment String
     * @param maxLength the maximum lenght the comment may be before
     * truncation, must be 3 or more.
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
                    String END_OF_SENTENCE = ".*[\\.\\?\\!]$";
                    if (!s.matches(END_OF_SENTENCE)) {
                        if (s.length() > maxLength-3) {
                            int ind2 = s.lastIndexOf(" ", s.length()-3);
                            if (ind2 < 0) { ind2 = s.length()-3; }
                            s = s.substring(0, ind2);
                        }
                        if (!s.matches(END_OF_SENTENCE)) {
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
     * Wordify the period between two dates according to these rules-

     For postings in the last 24 hours, display: Today
     For postings the day before, display "yesterday": Yesterday
     For postings within the last week, display the day: Monday, June 25
     For postings before that, a date: June 19
     For postings in the previous year, add the year: June 19, 2006
     *
     *
     * @param start Start date
     * @param end End date
     * @return empty string if any dates are null or if end date prior to start date,
     *          period otherwise.
     */
    public static String periodBetweenDates(Date start, Date end) {
        if (null == start || null == end) {
            return "";
        }

        DateTime dtStart = new DateTime(start.getTime());
        DateTime dtEnd = new DateTime(end.getTime());

        if (dtStart.isAfter(dtEnd)) {
            _log.error("End date prior to start date.  Start date: " + dtStart + ".  End date: " + dtEnd);
            return "";
        }

        Period period = new Period(dtStart, dtEnd, PeriodType.yearWeekDay());
        int years = period.getYears();
        int weeks = period.getWeeks();
        int days = period.getDays();

        String dateAsWord = "";

        if (years > 0) {
            dateAsWord = LONG_DATE_FORMAT.format(start);
        } else if (weeks > 0) {
            dateAsWord = LONG_DATE_FORMAT.format(start);
        } else {
            if (days == 0) {
                dateAsWord = "today";
            } else if (days == 1) {
                dateAsWord = "yesterday";
            } else {
                dateAsWord = DAY_OF_WEEK_MONTH_DAY_YEAR_FORMAT.format(start);
            }
        }

        return dateAsWord;
    }

    /**
     *
     * @param firstName null or firstName
     * @param lastName null or lastName
     * @param poster null or poster
     * @return
     *  See unit test for examples
     */
    public static String createParentReviewDisplayName(String firstName, String lastName, String poster) {
        String displayName = "";

        if (StringUtils.isNotBlank(firstName)) {
            displayName = firstName;
            if (StringUtils.isNotBlank(lastName)) {
                displayName += " " + lastName;
            }

            if (StringUtils.isNotBlank(poster)) {
                displayName += ", a " + poster;
            }
        } else if (StringUtils.isNotBlank(poster)) {
            displayName = "a " + poster;
        }

        return displayName;
    }

    public static String escapeHtml(String input) {
        if (input != null) {
            return input.replace("<", "&lt;");
        }
        return null;
    }
}
