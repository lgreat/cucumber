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
     * Method that pluralizes a singular word by appending a 's'.
     *
     * Example:
     *      pluralize(1, "cat") returns cat
     *      pluralize(2, "cats") returns cats
     *
     *      pluralize(1, "potato") returns potato
     *      pluralize(2, "potato") returns potatos
     *      pluralize(2, "potato", "potatoes") returns potatoes
     *
     * @param count number of items
     * @param arguments The a non-nullable word to pluralize (required), the non nullable plural form (optional)
     * @return pluralized word
     */
    public static String pluralize(int count, String... arguments) {
        if (null == arguments) {
            throw new IllegalArgumentException("word to pluralize cannot be null");
        } else if (arguments.length == 0) {
            throw new IllegalArgumentException("You did not specify a word to pluralize");
        } else if (arguments.length > 2) {
            throw new IllegalArgumentException("You can only pass up to 2 arguments: the word to pluralize and it's plural form.");
        } else if (arguments.length == 2 && null == arguments[1]) {
            throw new IllegalArgumentException("Plural form of word cannot be null");
        }
        return (count == 1) ? arguments[0] : ((arguments.length ==1) ? arguments[0] + "s" : arguments[1]);
    }

    /**
     * Only here because jsp utility method defined in gsweb.tld cannot use varargs yet
     * Backed by #pluralize
     *
     * @param count number of items
     * @param singular word form
     * @return pluralized word
     * 
     * @deprecated Use #pluralize
     */
    public static String pluralizeWord(int count, String singular) {
        return pluralize(count, singular);
    }

    /**
     * Only here because jsp utility method defined in gsweb.tld cannot use varags yet.
     * Backed by #pluralize
     *
     * @param count number of items
     * @param singular singular form
     * @param plural plural form
     * @return pluralized word
     *
     * @deprecated Use #pluralize
     */
    public static String pluralizeWordWithPluralForm(int count, String singular, String plural) {
        return pluralize(count, singular, plural);
    }

    /**
     * Wordify the period between two dates.
     *
     * If period is over a year, the year and month is part of the return string.
     *      Ex: one year and 2 months ago, one year and 1 month ago
     *
     * If period is over a month, the month is part of the return string.
     *      Ex: one month ago, 2 months ago
     *
     * If period is under a month, the number of days is part of the return string.
     *      Ex: 1 day ago, 23 days ago
     *
     * If start and end date is the same day, then 'today' is returned
     *
     * @param start Start date
     * @param end End date
     * @return empty string if any dates are null or if end date prior to start date,
     *          period otherwise.
     */
    public static String periodBetweenDates(Date start, Date end) {

        final String [] numAsWord = {
                "zero",
                "one", "two", "three", "four", "five",
                "six", "seven", "eight", "nine", "ten",
                "eleven", "twelve", "thirteen", "fourteen", "fifteen",
                "sixteen", "seventeen", "eighteen", "nineteen", "twenty",
                "twenty one", "twenty two", "twenty three", "twenty four", "twenty five",
                "twenty six", "twenty seven", "twenty eight", "twenty nine", "thirty",
                "thirty one"
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
}
