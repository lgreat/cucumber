package gs.web.ads;

import junit.framework.TestCase;

public class AdSizeTest extends TestCase {
    public void testConstructor() {
        boolean threwException = false;
        try {
            new AdSize(0,0);
        } catch (IllegalArgumentException e) {
            threwException = true;
        }
        assertTrue(threwException);
    }

    public void testGetWidth() {
        assertEquals(1, (new AdSize("X_20")).getWidth());
        assertEquals(1, (new AdSize("Interstitial")).getWidth());
        assertEquals(1, (new AdSize("Interstitial_Search")).getWidth());
        assertEquals(1, (new AdSize("Custom_Peelback_Ad")).getWidth());
        assertEquals(1, (new AdSize("Custom_Welcome_Ad")).getWidth());
        assertEquals(1, (new AdSize("Generic_640")).getWidth());

        assertEquals(300, (new AdSize("Top_300x137")).getWidth());
        assertEquals(300, (new AdSize("AboveFold_300x250")).getWidth());
        assertEquals(300, (new AdSize("House_Ad_300x137")).getWidth());
        assertEquals(728, (new AdSize("Header_728x90")).getWidth());
        assertEquals(728, (new AdSize("Header_728x90_B_Test")).getWidth());
        assertEquals(311, (new AdSize("BelowFold_311x250")).getWidth());
        assertEquals(598, (new AdSize("Homepage_House_Ad_598x102")).getWidth());
        assertEquals(160, (new AdSize("AboveFold_Left_160x600_B_Test")).getWidth());
        assertEquals(311, (new AdSize("countdown_b2s_311x250")).getWidth());
        assertEquals(88, (new AdSize("HeaderLogo_88x33")).getWidth());

        assertEquals(300, (new AdSize("300x137")).getWidth());
        assertEquals(300, (new AdSize("300x137_SizeAtStart")).getWidth());

        assertEquals(300, (new AdSize(300,250)).getWidth());
    }

    public void testGetHeight() {
        assertEquals(1, (new AdSize("X_20")).getHeight());
        assertEquals(1, (new AdSize("Interstitial")).getHeight());
        assertEquals(1, (new AdSize("Interstitial_Search")).getHeight());
        assertEquals(1, (new AdSize("Custom_Peelback_Ad")).getHeight());
        assertEquals(1, (new AdSize("Custom_Welcome_Ad")).getHeight());
        assertEquals(1, (new AdSize("Generic_640")).getHeight());

        assertEquals(137, (new AdSize("Top_300x137")).getHeight());
        assertEquals(250, (new AdSize("AboveFold_300x250")).getHeight());
        assertEquals(137, (new AdSize("House_Ad_300x137")).getHeight());
        assertEquals(90, (new AdSize("Header_728x90")).getHeight());
        assertEquals(90, (new AdSize("Header_728x90_B_Test")).getHeight());
        assertEquals(250, (new AdSize("BelowFold_311x250")).getHeight());
        assertEquals(102, (new AdSize("Homepage_House_Ad_598x102")).getHeight());
        assertEquals(600, (new AdSize("AboveFold_Left_160x600_B_Test")).getHeight());
        assertEquals(250, (new AdSize("countdown_b2s_311x250")).getHeight());
        assertEquals(33, (new AdSize("HeaderLogo_88x33")).getHeight());

        assertEquals(137, (new AdSize("300x137")).getHeight());
        assertEquals(137, (new AdSize("300x137_SizeAtStart")).getHeight());

        assertEquals(250, (new AdSize(300,250)).getHeight());
    }

    public void testGetCompanionSize() {
        AdSize size = new AdSize("AboveFold_300x250");
        AdSize companionSize = size.getCompanionSize();
        assertNotNull(companionSize);
        assertEquals(300, companionSize.getWidth());
        assertEquals(600, companionSize.getHeight());

        size = new AdSize("AboveFold_300x600");
        companionSize = size.getCompanionSize();
        assertNotNull(companionSize);
        assertEquals(300, companionSize.getWidth());
        assertEquals(250, companionSize.getHeight());

        size = new AdSize("Promo_300x250");
        companionSize = size.getCompanionSize();
        assertNotNull(companionSize);
        assertEquals(300, companionSize.getWidth());
        assertEquals(600, companionSize.getHeight());

        size = new AdSize("AboveFold_Left_160x600");
        companionSize = size.getCompanionSize();
        assertNull(companionSize);

        size = new AdSize("Sponsor_630x40");
        companionSize = size.getCompanionSize();
        assertNotNull(companionSize);
        assertEquals(630, companionSize.getWidth());
        assertEquals(145, companionSize.getHeight());

        size = new AdSize("Sponsor_630x145");
        companionSize = size.getCompanionSize();
        assertNotNull(companionSize);
        assertEquals(630, companionSize.getWidth());
        assertEquals(40, companionSize.getHeight());
    }
}
