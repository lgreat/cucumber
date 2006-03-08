package gs.web.jsp;

import gs.web.BaseTestCase;
import gs.data.state.State;
import gs.data.school.School;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class BaseTagHandlerTest extends BaseTestCase {

    private BaseTagHandler _baseTagHandler;

    protected void setUp() {
        _baseTagHandler = new BaseTagHandler() {
        };
        _baseTagHandler.setApplicationContext(getApplicationContext());
    }

    public void testEscapeLongState() {
        String s = "This is a $LONGSTATE string";
        // note two spaces between "a" and "california"
        String expected = "This is a  California string";
        String escapedString = _baseTagHandler.escapeLongstate(s);
        assertEquals(expected, escapedString);
    }

    public void testGetSchool() {
        assertNotNull(_baseTagHandler.getSchool(State.CA, new Integer(10)));
        assertNull(_baseTagHandler.getSchool(State.CA, null));
        assertNull(_baseTagHandler.getSchool(null, new Integer(10)));
        assertNull(_baseTagHandler.getSchool(null, null));
    }
}

