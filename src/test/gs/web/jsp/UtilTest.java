package gs.web.jsp;

import junit.framework.TestCase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
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

    public void testToLowerCase() {
        assertEquals(null, Util.toLowerCase(null));
        assertEquals("", Util.toLowerCase(""));
        assertEquals("foo LD bar", Util.toLowerCase("Foo LD Bar"));
        assertEquals("foo LDs bar", Util.toLowerCase("Foo LDs Bar"));
        assertEquals("foo ADD bar", Util.toLowerCase("Foo ADD Bar"));
        assertEquals("foo ADHD bar", Util.toLowerCase("Foo ADHD Bar"));
        assertEquals("foo AD/HD bar", Util.toLowerCase("Foo AD/HD Bar"));
        assertEquals("ADHD bar", Util.toLowerCase("ADHD Bar"));
        assertEquals("LD basics", Util.toLowerCase("LD basics"));
        assertEquals("ADHD", Util.toLowerCase("ADHD"));
        assertEquals("addition", Util.toLowerCase("Addition")); // we don't want "ADDition"
        assertEquals("foo IEP bar", Util.toLowerCase("Foo IEP Bar"));
        assertEquals("foo IEPs bar", Util.toLowerCase("Foo IEPs Bar"));
        assertEquals("foo Spanish bar", Util.toLowerCase("Foo Spanish Bar"));
    }

    public void unquoteTest() {
        assertEquals(null, Util.unquote(null));
        assertEquals("", Util.unquote(""));
        assertEquals("", Util.unquote("\""));
        assertEquals("A \"quoted\" string", Util.unquote("\"A \"quoted\" string\""));
        assertEquals("'\"inside single quotes\"'", Util.unquote("'\"inside single quotes\"'"));
    }

    public void testAbbreviate() {
        assertEquals(null, Util.abbreviate(null, 3));
        assertEquals("", Util.abbreviate("", 3));
        assertEquals("first it's here...", Util.abbreviate("first it's here second", 3));
        assertEquals("first second third", Util.abbreviate("first second third", 3));
        assertEquals("first second third...", Util.abbreviate("first second third fourth", 3));
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
            Util.pluralize(0, null, null);
            fail("passed a null word and plural word");
        } catch(IllegalArgumentException e) {}
    }

    public void testDateWithinXMinutes() throws Exception {
        Date now = new Date();
        assertTrue("Current date should be within 120 minutes of now", Util.dateWithinXMinutes(now, 120));
        Date h1 = new Date(now.getTime() - (1000 * 60 * 60));
        assertTrue("One hour ago should be within 120 minutes of now", Util.dateWithinXMinutes(h1, 120));
        Date h3 = new Date(now.getTime() - (1000 * 60 * 60 * 3));
        assertFalse("Three hours ago should not be within 120 minutes of now", Util.dateWithinXMinutes(h3, 120));
        assertFalse("Three hours ago should not be within 179 minutes of now", Util.dateWithinXMinutes(h3, 179));
        assertTrue("Three hours ago should be within 181 minutes of now", Util.dateWithinXMinutes(h3, 181));
        assertTrue("Three hours ago should be within 240 minutes of now", Util.dateWithinXMinutes(h3, 240));
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date longAgo = df.parse("2008-04-25");
        assertFalse("Really old date should not be within 120 minutes of now", Util.dateWithinXMinutes(longAgo, 120));
        assertFalse("Really old date should not be within 240 minutes of now", Util.dateWithinXMinutes(longAgo, 240));
        assertFalse("Really old date should not be within 1440 minutes of now", Util.dateWithinXMinutes(longAgo, 1440));
    }

    public void testPeriodBetweenDates() throws Exception {
        assertEquals("", d("2002-1-2", "2002-1-1"));
        assertEquals("today", d("2002-1-1", "2002-1-1"));
        assertEquals("January 1, 2002", d("2002-1-1", "2003-1-1"));
        assertEquals("January 1, 2002", d("2002-1-1", "2004-1-1"));
        assertEquals("January 1, 2002", d("2002-1-1", "2004-2-1"));
        assertEquals("January 1, 2002", d("2002-1-1", "2004-2-2"));

        assertEquals("January 1, 2002", d("2002-1-1", "2002-2-2"));
        assertEquals("January 2, 2002", d("2002-1-2", "2002-2-1"));
        assertEquals("January 2, 2002", d("2002-1-2", "2002-2-2"));
        assertEquals("January 1, 2002", d("2002-1-1", "2002-3-28"));

        assertEquals("January 1, 2003", d("2003-1-1", "2003-2-28"));
        assertEquals("January 29, 2002", d("2002-1-29", "2002-2-20"));

        assertEquals("yesterday", d("2002-1-1", "2002-1-2"));
        assertEquals("Tuesday, January 1, 2002", d("2002-1-1", "2002-1-3"));
        assertEquals("Tuesday, January 1, 2002", d("2002-1-1", "2002-1-4"));
        assertEquals("Tuesday, January 1, 2002", d("2002-1-1", "2002-1-5"));
        assertEquals("Tuesday, January 1, 2002", d("2002-1-1", "2002-1-6"));
        assertEquals("Tuesday, January 1, 2002", d("2002-1-1", "2002-1-7"));
        assertEquals("January 1, 2002", d("2002-1-1", "2002-1-8"));

        assertEquals("yesterday", d("2002-1-31", "2002-2-1"));
        assertEquals("Thursday, January 31, 2002", d("2002-1-31", "2002-2-3"));
    }

    public void testDetailedPeriodBetweenDates() throws ParseException {
        assertEquals("a moment ago", dd("2009-09-23 12:05:15", "2009-09-23 12:05:18"));
        assertEquals("a moment ago", dd("2009-09-23 12:05:15", "2009-09-23 12:06:14"));

        assertEquals("1 minute ago",   dd("2009-09-23 12:05:15", "2009-09-23 12:06:15"));
        assertEquals("1 minute ago",   dd("2009-09-23 12:05:15", "2009-09-23 12:07:14"));
        assertEquals("2 minutes ago",  dd("2009-09-23 12:05:15", "2009-09-23 12:07:15"));
        assertEquals("59 minutes ago", dd("2009-09-23 12:05:15", "2009-09-23 13:05:14"));

        assertEquals("1 hour ago",   dd("2009-09-23 12:05:15", "2009-09-23 13:05:15"));
        assertEquals("1 hour ago",   dd("2009-09-23 12:05:15", "2009-09-23 14:05:14"));
        assertEquals("2 hours ago",  dd("2009-09-23 12:05:15", "2009-09-23 14:05:15"));
        assertEquals("23 hours ago", dd("2009-09-23 12:05:15", "2009-09-24 11:05:15"));
        assertEquals("23 hours ago", dd("2009-09-23 12:05:15", "2009-09-24 12:05:14"));

        assertEquals("September 23, 2009", dd("2009-09-23 12:05:15", "2009-09-24 12:05:15"));
        assertEquals("September 23, 2009", dd("2009-09-23 12:05:15", "2009-10-21 12:05:15"));
        assertEquals("September 23, 2009", dd("2009-09-23 12:05:15", "2009-11-18 12:05:15"));
        assertEquals("September 23, 2009", dd("2009-09-23 12:05:15", "2010-09-23 12:05:15"));

        assertEquals("a moment ago", dd("2009-12-31 23:59:38", "2010-01-01 00:00:18"));        
    }

    static String d(String start, String end) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return Util.periodBetweenDates(df.parse(start), df.parse(end));
    }

    static String dd(String start, String end) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return Util.detailedPeriodBetweenDates(df.parse(start), df.parse(end));
    }

    public void testEscapeHtml() {
        assertEquals("hi", Util.escapeHtml("hi"));
        assertEquals("&lt;/body>", Util.escapeHtml("</body>"));
    }

    public void testDisplayName() {
        assertEquals("", Util.createParentReviewDisplayName(null, null, null));
        assertEquals("Barry", Util.createParentReviewDisplayName("Barry", null, null));
        assertEquals("", Util.createParentReviewDisplayName(null, "Bonds", null));
        assertEquals("Barry Bonds", Util.createParentReviewDisplayName("Barry", "Bonds", null));
        assertEquals("Barry Bonds, a student", Util.createParentReviewDisplayName("Barry", "Bonds", "student"));
        assertEquals("a student", Util.createParentReviewDisplayName(null, null, "student"));
    }

    public void testGenerateTeaserText() throws Exception {
        assertEquals("Expect abbreviation at first whitespace after 10 characters",
                     "This is some... ", Util.generateTeaserText("This is some text to display.", 10, 20));
        assertEquals("Expect abbreviation at first whitespace after 13 characters",
                     "This is some text... ", Util.generateTeaserText("This is some text to display.", 13, 20));
        assertEquals("Expect forced abbreviation at 15 characters",
                     "This is some te... ", Util.generateTeaserText("This is some text to display.", 13, 15));

        assertEquals("Expect abbreviation at first whitespace after 15 characters",
                     "There is some text... ", Util.generateTeaserText  ("There is some text to display.", 15, 29));
        // following test fails because of if (StringUtils.length(textToDisplay) > maxLength)
        assertEquals("Expect abbreviation at first whitespace after 15 characters",
                     "This is some text... ", Util.generateTeaserText("This is some text to display.", 15, 29));

        assertEquals("Expect original string to display since first whitespace after 23 chars is end of string",
                     "This is some text to display.", Util.generateTeaserText("This is some text to display.", 23, 40));
        assertEquals("Expect original string to display since length is <= minlength",
                     "This is some text to display.", Util.generateTeaserText("This is some text to display.", 29, 40));
    }

    public void testSplitAfterFirstXWords() {
        String[] result = Util.splitAfterXWords("", 5);
        assertEquals(2, result.length);
        assertEquals("", result[0]);
        assertEquals("", result[1]);
        result = Util.splitAfterXWords("Testing testing", 0);
        assertEquals("Testing testing", result[0]);
        assertEquals("", result[1]);
        result = Util.splitAfterXWords("Testing testing", 2);
        assertEquals("Testing testing", result[0]);
        assertEquals("", result[1]);
        result = Util.splitAfterXWords("Testing testing", 1);
        assertEquals("Testing", result[0]);
        assertEquals(" testing", result[1]);
        result = Util.splitAfterXWords("Testing testing", Integer.MAX_VALUE);
        assertEquals("Testing testing", result[0]);
        assertEquals("", result[1]);
    }

    public void testBoldifyFirstXWords() {
        String input = "This is a  sentence.\nThis is another sentence.";
        String expected1 = "<strong>This</strong> is a  sentence.\nThis is another sentence.";
        String expected4 = "<strong>This is a  sentence.</strong>\nThis is another sentence.";
        String expected5 = "<strong>This is a  sentence.\nThis</strong> is another sentence.";
        String expected7 = "<strong>This is a  sentence.\nThis is another</strong> sentence.";
        String expectedLong = "<strong>This is a  sentence.\nThis is another sentence.</strong>";
        assertEquals(input, Util.boldifyFirstXWords(input, 0));
        assertEquals("", Util.boldifyFirstXWords("", 5));
        assertEquals(expected1, Util.boldifyFirstXWords(input, 1));
        assertEquals(expected4, Util.boldifyFirstXWords(input, 4));
        assertEquals(expected5, Util.boldifyFirstXWords(input, 5));
        assertEquals(expected7, Util.boldifyFirstXWords(input, 7));
        assertEquals(expectedLong, Util.boldifyFirstXWords(input, 8));
        assertEquals(expectedLong, Util.boldifyFirstXWords(input, 10));
    }

    public void testSeparatedListContains() {
        String test = "one,two,three";
        String token = "one";
        String token2 = "two";
        String token3 = "three";
        String token4 = "four";
        String separater = ",";

        assertTrue("Expect to find token", Util.separatedListContains(test, token, separater));
        assertTrue("Expect to find token", Util.separatedListContains(test, token2, separater));
        assertTrue("Expect to find token", Util.separatedListContains(test, token3, separater));
        assertFalse("Shouldn't find nonexistent token", Util.separatedListContains(test, token4, separater));

        assertFalse("Shouldn't find token", Util.separatedListContains(test, token, null));
        assertFalse("Should be null safe and return false", Util.separatedListContains(test, null, separater));
        assertFalse("Should be null safe and return false", Util.separatedListContains(null, token, separater));
    }

    public void testShowBasicError404Page() throws Exception {
        assertTrue(Util.showBasicError404Page("/somepath.png"));
        assertTrue(Util.showBasicError404Page("/somepath.jpg"));
        assertTrue(Util.showBasicError404Page("/somepath.gif"));
        assertTrue(Util.showBasicError404Page("/somepath.pNg"));
        assertTrue(Util.showBasicError404Page("/somepath.JPg"));
        assertTrue(Util.showBasicError404Page("/somepath.GIF"));
        assertFalse(Util.showBasicError404Page("/somepath.page"));
        assertFalse(Util.showBasicError404Page("/somejpg.page"));
        assertFalse(Util.showBasicError404Page("/somepng.page"));
    }
}
