package gs.web.status;

import junit.framework.TestCase;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class IdentityTest extends TestCase {
    /**
     * It seems that these methods are not picked up by clover
     * when they are used in the LoginController test.
     */
    public void testIdentity() {
        Identity ident = new Identity();
        ident.setUsername("foo");
        ident.setPassword("bar");

        assertEquals("foo", ident.getUsername());
        assertEquals("bar", ident.getPassword());
        assertEquals("foo", ident.toString());
    }
}
