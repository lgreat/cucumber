package gs.web;

import junit.framework.TestCase;

/**
 * This is a simple JUnit test case to ensure that the environment is okay.
 *
 * @author <a href="trajano@yahoo.com">Archimedes Trajano</a>
 * @version $Id: SanityTest.java,v 1.1 2005/04/26 03:54:06 apeterson Exp $
 */
public class SanityTest extends TestCase {
    public void testSanity() {
        assertEquals("test", "test");
    }
}
