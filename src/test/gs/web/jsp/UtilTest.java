package gs.web.jsp;

import junit.framework.TestCase;

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
}
