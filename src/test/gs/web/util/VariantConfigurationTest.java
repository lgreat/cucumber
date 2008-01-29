package gs.web.util;

import gs.data.admin.IPropertyDao;
import gs.web.BaseControllerTestCase;

import static org.easymock.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class VariantConfigurationTest extends BaseControllerTestCase {
    private IPropertyDao _propertyDao;

    public void setUp() throws Exception {
        super.setUp();
        _propertyDao = createMock(IPropertyDao.class);
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
        assertEquals(1, ar.length);
        assertEquals(1, ar[0]);
        assertEquals(1, VariantConfiguration._cutoffTotal);

        VariantConfiguration._abCutoffs = null;
        VariantConfiguration.convertABConfigToArray("110/5/5");
        assertEquals(1, ar.length);
        assertEquals(1, ar[0]);
        assertEquals(1, VariantConfiguration._cutoffTotal);

        VariantConfiguration._abCutoffs = null;
        VariantConfiguration.convertABConfigToArray("0/5/5");
        assertEquals(1, ar.length);
        assertEquals(1, ar[0]);
        assertEquals(1, VariantConfiguration._cutoffTotal);

        VariantConfiguration._abCutoffs = null;
        VariantConfiguration.convertABConfigToArray("1/2/1/2/1/2/1/2/1/2/1/2/1/2/1/2/1/2/1/2/1/2/1/2/1/2/1");
        assertEquals(1, ar.length);
        assertEquals(1, ar[0]);
        assertEquals(1, VariantConfiguration._cutoffTotal);

        VariantConfiguration._abCutoffs = null;
        VariantConfiguration.convertABConfigToArray(null); // no crash on null
        assertEquals(1, ar.length);
        assertEquals(1, ar[0]);
        assertEquals(1, VariantConfiguration._cutoffTotal);
    }

    private void assertVariant(String variant, long secondsSinceEpoch) {
        assertEquals(variant, VariantConfiguration.getVariant(secondsSinceEpoch, _propertyDao));
    }

    public void testGetVariant() {
        // this tricks VC into thinking the configuration hasn't changed, so it won't bother
        // rebuilding it
        VariantConfiguration._lastConfiguration = "1";
        expect(_propertyDao.getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("1").anyTimes();
        replay(_propertyDao);
        VariantConfiguration._abCutoffs = new int[] {50,50};
        VariantConfiguration._cutoffTotal = 100;
        assertVariant("a", 0);
        assertVariant("a", 49);
        assertVariant("b", 50);
        assertVariant("b", 99);
        assertVariant("a", 100);

        VariantConfiguration._abCutoffs = new int[] {70,15,15};
        assertVariant("a", 0);
        assertVariant("b", 70);
        assertVariant("c", 85);

        VariantConfiguration._abCutoffs = new int[] {1,1};
        VariantConfiguration._cutoffTotal = 2;
        assertVariant("a", 0);
        assertVariant("b", 1);
        assertVariant("a", 2);
        assertNotNull("Real long value should provide result",
                VariantConfiguration.getVariant(System.currentTimeMillis() / 1000, _propertyDao));

        VariantConfiguration._abCutoffs = new int[] {1};
        VariantConfiguration._cutoffTotal = 1;
        assertVariant("a", 0);
        assertVariant("a", 1);
        assertVariant("a", 12345678);

        VariantConfiguration._abCutoffs = new int[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
        VariantConfiguration._cutoffTotal = 26;
        assertVariant("a", 0);
        assertVariant("r", 17);
        assertVariant("o", 14);
        assertVariant("y", 24);
        assertVariant("z", 25);
        assertVariant("a", 26);
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
