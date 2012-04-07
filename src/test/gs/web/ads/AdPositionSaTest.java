/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: AdPositionSaTest.java,v 1.8 2012/04/07 01:52:48 yfan Exp $
 */
package gs.web.ads;

import junit.framework.TestCase;

import java.util.List;

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

    public void testGetWidth() {
        assertEquals((Integer)1, AdPosition.X_20.getWidth());
        assertEquals((Integer)1, AdPosition.Interstitial.getWidth());
        assertEquals((Integer)1, AdPosition.Interstitial_Search.getWidth());
        assertEquals((Integer)1, AdPosition.Custom_Peelback_Ad.getWidth());
        assertEquals((Integer)1, AdPosition.Custom_Welcome_Ad.getWidth());
        assertEquals((Integer)1, AdPosition.Generic_640.getWidth());

        // TODO-12415 what if widthxheight at beginning of ad position name? no existing AdPosition has such a name
        
        assertEquals((Integer)300, AdPosition.Top_300x137.getWidth());
        assertEquals((Integer)300, AdPosition.AboveFold_300x250.getWidth());
        assertEquals((Integer)300, AdPosition.House_Ad_300x137.getWidth());
        assertEquals((Integer)728, AdPosition.Header_728x90.getWidth());
        assertEquals((Integer)728, AdPosition.Header_728x90_B_Test.getWidth());
        assertEquals((Integer)311, AdPosition.BelowFold_311x250.getWidth());
        assertEquals((Integer)598, AdPosition.Homepage_House_Ad_598x102.getWidth());
        assertEquals((Integer)160, AdPosition.AboveFold_Left_160x600_B_Test.getWidth());
        assertEquals((Integer)311, AdPosition.countdown_b2s_311x250.getWidth());
        assertEquals((Integer)88, AdPosition.Homepage_FindASchool_Sponsor_88x31.getWidth());
    }
    
    public void testGetHeight() {
        assertEquals((Integer)1, AdPosition.X_20.getWidth());
        assertEquals((Integer)1, AdPosition.Interstitial.getWidth());
        assertEquals((Integer)1, AdPosition.Interstitial_Search.getWidth());
        assertEquals((Integer)1, AdPosition.Custom_Peelback_Ad.getWidth());
        assertEquals((Integer)1, AdPosition.Custom_Welcome_Ad.getWidth());
        assertEquals((Integer)1, AdPosition.Generic_640.getWidth());

        // TODO-12415 what if widthxheight at beginning of ad position name? no existing AdPosition has such a name

        assertEquals((Integer)137, AdPosition.Top_300x137.getHeight());
        assertEquals((Integer)250, AdPosition.AboveFold_300x250.getHeight());
        assertEquals((Integer)137, AdPosition.House_Ad_300x137.getHeight());
        assertEquals((Integer)90, AdPosition.Header_728x90.getHeight());
        assertEquals((Integer)90, AdPosition.Header_728x90_B_Test.getHeight());
        assertEquals((Integer)250, AdPosition.BelowFold_311x250.getHeight());
        assertEquals((Integer)102, AdPosition.Homepage_House_Ad_598x102.getHeight());
        assertEquals((Integer)600, AdPosition.AboveFold_Left_160x600_B_Test.getHeight());
        assertEquals((Integer)250, AdPosition.countdown_b2s_311x250.getHeight());
        assertEquals((Integer)31, AdPosition.Homepage_FindASchool_Sponsor_88x31.getHeight());
    }
}
