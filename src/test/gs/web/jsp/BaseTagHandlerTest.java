package gs.web.jsp;

import junit.framework.TestCase;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class BaseTagHandlerTest extends TestCase {

    protected void setUp() {

    }

    public void testEscapeLongState() {
        BaseTagHandler bth = new BaseTagHandler() {
        };

        String s = "This is a $LONGSTATE string";
        // note two spaces between "a" and "california"
        String expected = "This is a  California string";
        String escapedString = bth.escapeLongstate(s);
        assertEquals(expected, escapedString);
    }
}

