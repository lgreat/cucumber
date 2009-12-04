package gs.web.search;

import junit.framework.TestCase;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public class FeedbackCommandTest extends TestCase {
    public void testFields() {
        FeedbackCommand fc = new FeedbackCommand();
        fc.setComment("This is a comment");
        fc.setDescription("A multiline \n description");
        fc.setEmail("programmers@greatschools.org");
        fc.setExpected("This is what I expected: a lolipop");
        fc.setQuery("foo bar");
        fc.setState("CA");

        assertEquals("This is a comment", fc.getComment());
        assertEquals("A multiline \n description", fc.getDescription());
        assertEquals("programmers@greatschools.org", fc.getEmail());
        assertEquals("This is what I expected: a lolipop", fc.getExpected());
        assertEquals("foo bar", fc.getQuery());
        assertEquals("CA", fc.getState());
    }
}
