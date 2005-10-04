package gs.web.jsp;

import junit.framework.TestCase;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class UtilTest extends TestCase {
    public void testToLowercase() {
        String capString = "THIS IS A STRING IN ALL CAPS";
        String lowerString = "this is a string in all caps";
        String outString = Util.toLowercase(capString);
        assertEquals(lowerString, outString);
    }

    public void testOddOrEven() {
        Integer testNum = null;
        try {
            Util.oddOrEven(testNum);
            fail("Should have thrown an NPE");
        } catch (NullPointerException e) {            
        }
        testNum = new Integer(1);
        assertEquals("odd", Util.oddOrEven(testNum));
        testNum = new Integer(2);
        assertEquals("even", Util.oddOrEven(testNum));
    }

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
        assertTrue(rand > 0);
        assertTrue(rand < 100);
    }
}
