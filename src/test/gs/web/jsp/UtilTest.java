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
}
