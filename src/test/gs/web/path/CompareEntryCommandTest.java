package gs.web.path;

import junit.framework.TestCase;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CompareEntryCommandTest extends TestCase {
    public void testGettersAndSetters() {
        CompareEntryCommand command = new CompareEntryCommand();
        command.setSchoolType("public");
        command.setArea("address");

        assertEquals("public", command.getSchoolType());
        assertEquals("address", command.getArea());
    }
}
