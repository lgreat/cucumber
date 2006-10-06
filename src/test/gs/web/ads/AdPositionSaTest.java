/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AdPositionSaTest.java,v 1.2 2006/10/06 23:43:37 chriskimm Exp $
 */
package gs.web.ads;

import junit.framework.TestCase;

import java.util.List;

/**
 * Test AdPosition class
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class AdPositionSaTest extends TestCase {
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testAdPosition() {
        assertEquals(AdPosition.X_20, AdPosition.getAdPosition("x20"));
        assertEquals(AdPosition.X_22, AdPosition.getAdPosition("x22"));

        assertEquals(AdPosition.X_24, AdPosition.getAdPosition("x24"));
        assertEquals(AdPosition.X_33, AdPosition.getAdPosition("x33"));

        assertEquals(AdPosition.X_40, AdPosition.getAdPosition("x40"));
        assertEquals(AdPosition.X_48, AdPosition.getAdPosition("x48"));

        assertEquals(AdPosition.X_49, AdPosition.getAdPosition("x49"));
        assertEquals(AdPosition.X_50, AdPosition.getAdPosition("x50"));
        assertEquals(AdPosition.X_66, AdPosition.getAdPosition("x66"));

        assertNotSame(AdPosition.X_66, AdPosition.getAdPosition("x22"));

        assertEquals("x22", AdPosition.X_22.getName());

        try {
            AdPosition.getAdPosition("noadname");
            fail("supposed to fail since ad position does not exist");
        } catch (IllegalArgumentException e) {

        }

        List adPositions = AdPosition.getAllAdPositions();
        assertTrue(adPositions.size() > 5);
    }

}
