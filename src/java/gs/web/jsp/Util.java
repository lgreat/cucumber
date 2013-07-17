package gs.web.jsp;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.htmlcleaner.BrowserCompactXmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;

import javax.servlet.jsp.PageContext;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.text.BreakIterator;
import java.util.*;
import java.io.StringReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
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
     * Returns a lowercased version of the supplied String. The following acronyms will have letters in
     * uppercase/lowercase as shown: LD, LDs, ADD, ADHD, AD/HD.
     * @param s A <code>String</code> object to be lowercased
     * @return a <code>String</code, null if input is null
     */
    public static String toLowerCase(String s) {
        if (s == null) {
            return s;
        }
        s = s.toLowerCase();
        s = s.replaceAll("\\bld\\b", "LD");
        s = s.replaceAll("\\blds\\b", "LDs");
        s = s.replaceAll("\\badd\\b", "ADD");
        s = s.replaceAll("\\badhd\\b", "ADHD");
        s = s.replaceAll("\\bad/hd\\b", "AD/HD");
        s = s.replaceAll("\\biep\\b", "IEP");
        s = s.replaceAll("\\bieps\\b", "IEPs");
        s = s.replaceAll("\\bspanish\\b", "Spanish");
        s = s.replaceAll("\\bada\\b", "ADA");
        s = s.replaceAll("\\(idea\\)", "(IDEA)");

        return s; 
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
     * Abbreviates a string - if a string is longer than maxWords words,
     * then truncate at a word boundary and append "..."  The resulting string
     * will be no longer than maxWords <em>excluding</em> the "..."
     * Null will be returned if a null String is passed
     * @param s
     * @param maxWords
     * @return
     */
    public static String abbreviate(String s, int maxWords) {
        if (s == null || s.length() == 0) {
            return s;
        }

        // http://www.devx.com/tips/Tip/5577
        // http://java.sun.com/j2se/1.5.0/docs/api/java/text/BreakIterator.html
        StringBuilder sb = new StringBuilder();
        BreakIterator bi = BreakIterator.getWordInstance();
        bi.setText(s);

        int index = 0;
        int numWordsSoFar = 0;
		while (bi.next() != BreakIterator.DONE && numWordsSoFar < maxWords) {
            sb.append(s.substring(index, bi.current()));

            // determine if this is a word by using a heuristic that it's a word if there is a letter or number in it
            for (int p = index; p < bi.current(); p++) {
                if (Character.isLetter(s.codePointAt(p)) || Character.isDigit(s.codePointAt(p))) {
                    numWordsSoFar++;
                    break;
                }
            }

            index = bi.current();
		}

        if (sb.length() < s.length()) {
            sb.append("...");
        }

        return sb.toString();
    }

    public static String generateTeaserText(String textToDisplay, int minLength, int maxLength) throws IOException {
        String teaserBody = null;
        String teaserEnder = "... ";
        if (StringUtils.length(textToDisplay) > minLength) {
            teaserBody = WordUtils.abbreviate(textToDisplay, minLength, maxLength, teaserEnder);
        } else {
            teaserBody = textToDisplay;
        }

        HtmlCleaner cleaner = new HtmlCleaner();

        CleanerProperties props = cleaner.getProperties();
        //props.setOmitHtmlEnvelope(true);
        props.setOmitDoctypeDeclaration(true);
        props.setOmitXmlDeclaration(true);

        TagNode node = cleaner.clean(new StringReader(teaserBody));
        BrowserCompactXmlSerializer ser = new BrowserCompactXmlSerializer(props);

        String output = ser.getXmlAsString(node);
//        System.out.println("Pre-Output: '" + output + "'");
        // Strip off the html wrapper
        if (output.length() >= 34) { // At 34 chars our indexes will be 20,20 which will result in no output.
            output = output.substring(20, output.length() - 14);
        }
        output = output.replace("&amp;apos;", "&#39;"); // Fix for IE which doesn't handle all xml entities
        output = output.replace("&apos;", "&#39;"); // Fix for IE which doesn't handle all xml entities

        output = output.replaceAll("(<br>|<br\\s?/>|<p>|</p>)", " "); // strip out line breaks and paragraph tags
        teaserBody = output;

        return teaserBody;
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

    public static boolean dateWithinXMinutes(Date date, int numMinutes) {
        DateTime xMinutesAgo = new DateTime(new Date().getTime() - (1000 * 60 * numMinutes));
        DateTime dateInQuestion = new DateTime(date.getTime());

        return dateInQuestion.isAfter(xMinutesAgo);
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

    public static String detailedPeriodBetweenDates(Date start, Date end) {
        if (null == start || null == end) {
            return "";
        }

        DateTime dtStart = new DateTime(start.getTime());
        DateTime dtEnd = new DateTime(end.getTime());

        if (dtStart.isAfter(dtEnd)) {
            _log.error("End date prior to start date.  Start date: " + dtStart + ".  End date: " + dtEnd);
            return "";
        }

        Period period = new Period(dtStart, dtEnd, PeriodType.yearWeekDayTime());
        int aDayOrMore = period.getDays() + period.getWeeks() + period.getYears();
        int hours = period.getHours();
        int minutes = period.getMinutes();
        String commentTimeStr;
        String plural = "";

        if (aDayOrMore > 0) {
            // display timestamp as Month, DD, YYYY: September 30, 2009
            commentTimeStr = LONG_DATE_FORMAT.format(start);
        } else if (hours > 0) {
            // display timestamp as "1 hour ago, 2 hours ago, 14 hours ago, 23 hours ago"
            if (hours > 1) {
                plural = "s";
            }
            commentTimeStr = hours + " hour" + plural + " ago";
        } else if (minutes > 0) {
            // display timestamp as "1 minute ago, 20 minutes ago, 59 minutes ago"
            if (minutes > 1) {
                plural = "s";
            }
            commentTimeStr = minutes + " minute" + plural + " ago";
        } else {
            // display timestamp as "a moment ago"
            commentTimeStr = "a moment ago";
        }
        return commentTimeStr;
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
            displayName = poster.equals("principal")? "the "+ poster : (poster.equals("administrator")? "an "+ poster: "a " + poster);
        }

        return displayName;
    }

    public static String escapeHtml(String input) {
        if (input != null) {
            return input.replace("<", "&lt;");
        }
        return null;
    }

    /**
     * Take an input string and splits the string after the first x number of
     * words, returning a string array with two elements.  Will always return
     * empty strings instead of nulls
     * @param input
     * @param numWords
     * @return
     */
    public static String[] splitAfterXWords(String input, int numWords) {
        String[] value = new String[]{"",""};
        String operatingString = StringUtils.trim(input);

        if (StringUtils.isBlank(operatingString) || StringUtils.length(operatingString) == 0 || numWords < 1) {
            value[0] = input;
            return value;
        }

        int wordCount = 0;
        boolean lastCharWhitespace = false;
        for (int x=1; x < operatingString.length(); x++) {
            if (Character.isWhitespace(operatingString.charAt(x))) {
                if (!lastCharWhitespace) {
                    wordCount++;
                    if (wordCount == numWords) {
                        value[0] = StringUtils.substring(operatingString, 0, x);
                        value[1] = StringUtils.substring(operatingString, x);
                        break;
                    }
                }
                lastCharWhitespace = true;
            } else {
                lastCharWhitespace = false;
            }
        }
        if (StringUtils.isBlank(value[0])) {
            value[0] = operatingString;
            value[1] = "";
        }

        return value;
    }

    /**
     * boldify the frist words in an input string
     * @param input
     * @param numWords
     * @return
     */
    public static String boldifyFirstXWords(String input, int numWords) {
        String[] result = splitAfterXWords(input, numWords);
        // return original string under these conditions
        if ( numWords < 1 || (result.length == 2 && StringUtils.isBlank(result[0])) ){
            return input;
        }
        return "<strong>" + result[0] + "</strong>" + result[1];
    }

    public static boolean contains(Set set, Object o) {
        if (set == null) {
            throw new IllegalArgumentException("Set cannot be null");
        }
        return set.contains(o);
    }

    public static Object get(Map map, Object o) {
        if (map == null) {
            throw new IllegalArgumentException("Map cannot be null");
        }
        return map.get(o);
    }

    public static boolean separatedListContains(String stringToSearch, String stringToFind, String separaterChars) {
        String[] tokens = StringUtils.split(stringToSearch, separaterChars);

        return ArrayUtils.contains(tokens, stringToFind);
    }

    public static boolean showBasicError404Page(PageContext context) {
        try {
            // getErrorData() seems to throw a NPE in some versions of tomcat
            String uri = context.getErrorData().getRequestURI();
            return showBasicError404Page(uri);
        } catch (NullPointerException npe) {
            return false;
        }
    }

    private final static Pattern IMAGE_PATTERN = Pattern.compile("^.*\\.(png|jpg|gif|js|css|ico)$", Pattern.CASE_INSENSITIVE);

    public static boolean showBasicError404Page(String requestUri) {
        if (requestUri != null) {
            return IMAGE_PATTERN.matcher(requestUri).matches();
        }
        return false;
    }

    public static int min(int i, int j) {
        return Math.min(i,j);
    }

    public static double roundTwoDecimal(double num) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return Double.valueOf(decimalFormat.format(num));
    }

    public static long getNumPages(int items, int itemsPerPage) {
        if (items < 1 || itemsPerPage < 1) {
            throw new IllegalArgumentException("Both items and itemsPerPage must be greater than 0");
        }
        return (long) Math.ceil((double) items / itemsPerPage);
    }

    public static List<String> addToList(List<String> list, String val) {
        if(list == null) {
            list = new ArrayList<String>();
        }

        if(val != null) {
            list.add(val);
        }

        return list;
    }

    public static List<String> addToOmnitureList(List<String> omnitureList, String omnitureData) {
        // empty strings need not be added to the omniture list
        if(omnitureData == null || "".equals(omnitureData.trim())) {
            return addToList(omnitureList, null);
        }

        return addToList(omnitureList, omnitureData);
    }

    public static String convertListToString(List<String> list, String delimiter) {
        if(list == null) {
            return "";
        }

        // by default use comma as delimiter
        if(delimiter == null) {
            delimiter = ",";
        }

        return StringUtils.join(list, delimiter);
    }
}