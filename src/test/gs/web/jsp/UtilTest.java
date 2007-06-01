package gs.web.jsp;

import junit.framework.TestCase;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class UtilTest extends TestCase {

    public void testRandomNumber () {
        int rand = Util.randomNumber(1);
        assertTrue(rand == 0);

        try {
            rand = Util.randomNumber(0);
            fail("Upper limit must be a positive number > 0");
        } catch (IllegalArgumentException e) {
            // Do nothing, we're good
        }

        rand = Util.randomNumber(100);
        assertTrue(rand >= 0);
        assertTrue(rand < 100);
    }

    public void testToDelimitedString() {

        assertEquals("", Util.toDelimitedString(null));
        assertEquals("", Util.toDelimitedString(new Object[]{null}));

        String[] in1 = {"this", "is", "an", "array"};
        String out1 = Util.toDelimitedString(in1);
        assertEquals("this,is,an,array", out1);

        Object[] in2 = {"foo", "count", null, "blah"};
        String out2 = Util.toDelimitedString(in2);
        assertEquals("foo,count,,blah", out2);
    }

    public void testToUglyDelimitedString() {

        assertEquals("", Util.toUglyDelimitedString(null));
        assertEquals("", Util.toUglyDelimitedString(new Object[]{null}));

        String[] in1 = {"this", "is", "an", "array"};
        String out1 = Util.toUglyDelimitedString(in1);
        assertEquals("This+Is+An+Array", out1);

        Object[] in2 = {"foo", "count", null, "blah"};
        String out2 = Util.toUglyDelimitedString(in2);
        assertEquals("Foo+Count++Blah", out2);
    }

    public void testCapitalize() {
        assertEquals("A test string", Util.capitalize("a test string"));
        assertEquals("", Util.capitalize(null));
        assertEquals("", Util.capitalize(" "));
    }

    public void unquoteTest() {
        assertEquals(null, Util.unquote(null));
        assertEquals("", Util.unquote(""));
        assertEquals("", Util.unquote("\""));
        assertEquals("A \"quoted\" string", Util.unquote("\"A \"quoted\" string\""));
        assertEquals("'\"inside single quotes\"'", Util.unquote("'\"inside single quotes\"'"));
    }

    public void testAbbreviateAtWhitespace() throws Exception {

        try {
            Util.abbreviateAtWhitespace("1234123", 2);
            fail("max length must be greater than 2");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        // allow null comment args.
        assertNull(Util.abbreviateAtWhitespace(null, 123));
        assertEquals("", Util.abbreviateAtWhitespace("", 123));
        assertEquals("Tester test", Util.abbreviateAtWhitespace("Tester test", 123));

        String comment = "Now it's time. For all good men to come to the ";
        assertEquals("...", Util.abbreviateAtWhitespace(comment, 3));
        assertEquals("...", Util.abbreviateAtWhitespace(comment, 4));
        assertEquals("Now...", Util.abbreviateAtWhitespace(comment, 6));
        assertEquals("Now...", Util.abbreviateAtWhitespace(comment, 7));
        assertEquals("Now...", Util.abbreviateAtWhitespace(comment, 8));
        assertEquals("Now it's...", Util.abbreviateAtWhitespace(comment, 11));
        assertEquals("Now it's time.", Util.abbreviateAtWhitespace(comment, 14));
        assertEquals("Now it's time.", Util.abbreviateAtWhitespace(comment, 15));
        assertEquals("Now it's time.", Util.abbreviateAtWhitespace(comment, 16));
        assertEquals("Now it's time.", Util.abbreviateAtWhitespace(comment, 17));
        assertEquals("Now it's time. For all good...",
                Util.abbreviateAtWhitespace(comment, 33));
        assertEquals("a...", Util.abbreviateAtWhitespace("abcdefgh", 4));
        assertEquals("abc...", Util.abbreviateAtWhitespace("abcdefgh", 6));
        assertEquals("abcdefgh", Util.abbreviateAtWhitespace("abcdefgh", 10));
        assertEquals("abc...", Util.abbreviateAtWhitespace("abc   defgh", 8));
    }

    public void testPluralize() throws Exception {
        assertEquals("cats", Util.pluralize(0, "cat"));
        assertEquals("cat", Util.pluralize(1, "cat"));
        assertEquals("cats", Util.pluralize(2, "cat"));
        assertEquals("too many cats", Util.pluralize(3, "cat", "too many cats"));

        try {
            Util.pluralize(0, "", null);
            fail("passed a null plural form");
        } catch(IllegalArgumentException e) {}

        try {
            Util.pluralize(0, null);
            fail("passed a null word");
        } catch(IllegalArgumentException e) {}

        try {
            Util.pluralize(0, null);
            fail("passed a null word and plural word");
        } catch(IllegalArgumentException e) {}
    }

    public void testPeriodBetweenDates() throws Exception {
        assertEquals("", d("2002-1-2", "2002-1-1"));
        assertEquals("today", d("2002-1-1", "2002-1-1"));
        assertEquals("one year ago", d("2002-1-1", "2003-1-1"));
        assertEquals("two years ago", d("2002-1-1", "2004-1-1"));
        assertEquals("two years and one month ago", d("2002-1-1", "2004-2-1"));
        assertEquals("two years and one month ago", d("2002-1-1", "2004-2-2"));

        assertEquals("one month ago", d("2002-1-1", "2002-2-2"));
        assertEquals("thirty days ago", d("2002-1-2", "2002-2-1"));
        assertEquals("one month ago", d("2002-1-2", "2002-2-2"));
        assertEquals("two months ago", d("2002-1-1", "2002-3-28"));

        assertEquals("one month ago", d("2003-1-1", "2003-2-28"));
        assertEquals("twenty two days ago", d("2002-1-29", "2002-2-20"));

        assertEquals("one day ago", d("2002-1-1", "2002-1-2"));
        assertEquals("two days ago", d("2002-1-1", "2002-1-3"));
    }

    static String d(String start, String end) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return Util.periodBetweenDates(df.parse(start), df.parse(end));
    }

    

}
