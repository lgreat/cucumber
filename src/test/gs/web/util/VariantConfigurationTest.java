package gs.web.util;

import gs.web.BaseControllerTestCase;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class VariantConfigurationTest extends BaseControllerTestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testConvertABConfigToArray() {
        VariantConfiguration._abCutoffs = null;
        assertNull(VariantConfiguration._abCutoffs);
        VariantConfiguration.convertABConfigToArray("70/15/15");
        assertNotNull(VariantConfiguration._abCutoffs);
        int[] ar = VariantConfiguration._abCutoffs;
        assertEquals(3, ar.length);
        assertEquals(70, ar[0]);
        assertEquals(15, ar[1]);
        assertEquals(15, ar[2]);
        assertEquals(100, VariantConfiguration._cutoffTotal);

        VariantConfiguration._abCutoffs = null;
        VariantConfiguration.convertABConfigToArray("33/33/33");
        assertNotNull(VariantConfiguration._abCutoffs);
        ar = VariantConfiguration._abCutoffs;
        assertEquals(3, ar.length);
        assertEquals(33, ar[0]);
        assertEquals(33, ar[1]);
        assertEquals(33, ar[2]);
        assertEquals(99, VariantConfiguration._cutoffTotal);

        VariantConfiguration._abCutoffs = null;
        VariantConfiguration.convertABConfigToArray("1/1");
        assertNotNull(VariantConfiguration._abCutoffs);
        ar = VariantConfiguration._abCutoffs;
        assertEquals(2, ar.length);
        assertEquals(1, ar[0]);
        assertEquals(1, ar[1]);
        assertEquals(2, VariantConfiguration._cutoffTotal);

        VariantConfiguration._abCutoffs = null;
        VariantConfiguration.convertABConfigToArray("50");
        ar = VariantConfiguration._abCutoffs;
        assertEquals(1, ar.length);
        assertEquals(50, ar[0]);
        assertEquals(50, VariantConfiguration._cutoffTotal);

        VariantConfiguration._abCutoffs = null;
        VariantConfiguration.convertABConfigToArray("1");
        ar = VariantConfiguration._abCutoffs;
        assertEquals(1, ar.length);
        assertEquals(1, ar[0]);
        assertEquals(1, VariantConfiguration._cutoffTotal);

        VariantConfiguration._abCutoffs = null;
        VariantConfiguration.convertABConfigToArray("70/20/20");
        assertNull(VariantConfiguration._abCutoffs);

        VariantConfiguration._abCutoffs = null;
        VariantConfiguration.convertABConfigToArray("110/5/5");
        assertNull("Expect no result from values over 100", VariantConfiguration._abCutoffs);

        VariantConfiguration._abCutoffs = null;
        VariantConfiguration.convertABConfigToArray("0/5/5");
        assertNull("Expect no result from values less than 1", VariantConfiguration._abCutoffs);

        VariantConfiguration._abCutoffs = null;
        VariantConfiguration.convertABConfigToArray(null); // no crash on null
        assertNull(VariantConfiguration._abCutoffs);
    }

    public void testDetermineVariantFromConfiguration() {
        VariantConfiguration._abCutoffs = new int[] {50,50};
        VariantConfiguration._cutoffTotal = 100;
        VariantConfiguration.determineVariantFromConfiguration(0, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());
        VariantConfiguration.determineVariantFromConfiguration(49, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());
        VariantConfiguration.determineVariantFromConfiguration(50, _sessionContext);
        assertEquals("b", _sessionContext.getABVersion());
        VariantConfiguration.determineVariantFromConfiguration(99, _sessionContext);
        assertEquals("b", _sessionContext.getABVersion());
        VariantConfiguration.determineVariantFromConfiguration(100, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());

        VariantConfiguration._abCutoffs = new int[] {70,15,15};
        VariantConfiguration.determineVariantFromConfiguration(0, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());
        VariantConfiguration.determineVariantFromConfiguration(70, _sessionContext);
        assertEquals("b", _sessionContext.getABVersion());
        VariantConfiguration.determineVariantFromConfiguration(85, _sessionContext);
        assertEquals("c", _sessionContext.getABVersion());

        VariantConfiguration._abCutoffs = new int[] {1,1};
        VariantConfiguration._cutoffTotal = 2;
        VariantConfiguration.determineVariantFromConfiguration(0, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());
        VariantConfiguration.determineVariantFromConfiguration(1, _sessionContext);
        assertEquals("b", _sessionContext.getABVersion());
        VariantConfiguration.determineVariantFromConfiguration(2, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());
        _sessionContext.setAbVersion(null);
        VariantConfiguration.determineVariantFromConfiguration(System.currentTimeMillis() / 1000, _sessionContext);
        assertNotNull(_sessionContext.getABVersion());

        VariantConfiguration._abCutoffs = new int[] {1};
        VariantConfiguration._cutoffTotal = 1;
        VariantConfiguration.determineVariantFromConfiguration(0, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());
        VariantConfiguration.determineVariantFromConfiguration(1, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());
        VariantConfiguration.determineVariantFromConfiguration(12345678, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());

        VariantConfiguration._abCutoffs = new int[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
        VariantConfiguration._cutoffTotal = 26;
        VariantConfiguration.determineVariantFromConfiguration(0, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());
        VariantConfiguration.determineVariantFromConfiguration(17, _sessionContext);
        assertEquals("r", _sessionContext.getABVersion());
        VariantConfiguration.determineVariantFromConfiguration(14, _sessionContext);
        assertEquals("o", _sessionContext.getABVersion());
        VariantConfiguration.determineVariantFromConfiguration(24, _sessionContext);
        assertEquals("y", _sessionContext.getABVersion());
        VariantConfiguration.determineVariantFromConfiguration(25, _sessionContext);
        assertEquals("z", _sessionContext.getABVersion());
        VariantConfiguration.determineVariantFromConfiguration(26, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());
    }

    public void testConvertABConfigurationToString() {
        VariantConfiguration._abCutoffs = new int[] {1,1};
        VariantConfiguration._cutoffTotal = 2;
        assertEquals("A/B: 50/50", VariantConfiguration.convertABConfigurationToString());

        VariantConfiguration._abCutoffs = new int[] {4,1};
        VariantConfiguration._cutoffTotal = 5;
        assertEquals("A/B: 80/20", VariantConfiguration.convertABConfigurationToString());

        VariantConfiguration._abCutoffs = new int[] {14,3,3};
        VariantConfiguration._cutoffTotal = 20;
        assertEquals("A/B/C: 70/15/15", VariantConfiguration.convertABConfigurationToString());
    }
}
