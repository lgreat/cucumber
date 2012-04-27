/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: AdPositionSaTest.java,v 1.10 2012/04/27 15:14:26 yfan Exp $
 */
package gs.web.ads;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test AdPosition class
 *
 * @author David Lee <mailto:dlee@greatschools.org>
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
        assertEquals(AdPosition.X_47, AdPosition.getAdPosition("x47"));
        assertEquals(AdPosition.X_48, AdPosition.getAdPosition("x48"));

        assertEquals(AdPosition.X_49, AdPosition.getAdPosition("x49"));
        assertEquals(AdPosition.X_50, AdPosition.getAdPosition("x50"));
        assertEquals(AdPosition.X_66, AdPosition.getAdPosition("x66"));

        assertNotSame(AdPosition.X_66, AdPosition.getAdPosition("x22"));

        assertEquals(AdPosition.YouTube_381x311, AdPosition.getAdPosition("YouTube_381x311"));

        assertEquals("x22", AdPosition.X_22.getName());

        try {
            AdPosition.getAdPosition("noadname");
            fail("supposed to fail since ad position does not exist");
        } catch (IllegalArgumentException e) {

        }

        List adPositions = AdPosition.getAllAdPositions();
        assertTrue(adPositions.size() > 5);
    }

    public void testSizes() {
        AdPosition adPosition = AdPosition.AboveFold_300x125;
        assertEquals(1, adPosition.getSizes().size());
        assertEquals(300, adPosition.getSize().getWidth());
        assertEquals(125, adPosition.getSize().getHeight());

        adPosition = AdPosition.AboveFold_Left_160x600;
        assertEquals(1, adPosition.getSizes().size());
        assertEquals(160, adPosition.getSize().getWidth());
        assertEquals(600, adPosition.getSize().getHeight());

        adPosition = AdPosition.AboveFold_300x250;
        assertEquals(1, adPosition.getSizes().size());
        assertEquals(1, adPosition.getCompanionSizes().size());
        assertEquals(2, adPosition.getAllSizes().size());
        assertTrue(adPosition.getAllSizes().get(0).equals(AdSize.Size_300x250));
        assertTrue(adPosition.getAllSizes().get(1).equals(AdSize.Size_300x600));

        Set<AdSize> sizes = new HashSet<AdSize>();
        sizes.add(AdSize.Size_300x250);
        sizes.add(AdSize.Size_300x600);
        adPosition = new AdPosition("Test_300xSomething", true, sizes);
        assertEquals(2, adPosition.getSizes().size());
        assertEquals(0, adPosition.getCompanionSizes().size());
        assertEquals(2, adPosition.getAllSizes().size());
        assertTrue(adPosition.getAllSizes().containsAll(sizes));

        AdSize adSize300x125 = new AdSize(300,125);
        sizes = new HashSet<AdSize>();
        sizes.add(AdSize.Size_300x250);
        sizes.add(adSize300x125);
        adPosition = new AdPosition("Test_300xSomething2", true, sizes);
        assertEquals(2, adPosition.getSizes().size());
        assertEquals(1, adPosition.getCompanionSizes().size());
        assertEquals(3, adPosition.getAllSizes().size());
        assertTrue(adPosition.getAllSizes().containsAll(sizes));
        assertTrue(adPosition.getAllSizes().contains(adSize300x125));
    }
}
