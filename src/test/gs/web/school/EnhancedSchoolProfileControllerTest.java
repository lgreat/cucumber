package gs.web.school;

import gs.data.school.EspResponse;
import gs.web.BaseControllerTestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: jkirton
 */
public class EnhancedSchoolProfileControllerTest extends BaseControllerTestCase {

    static final Map<String, List<EspResponse>> mr1, mr2, feederSchools;

    static {
        EspResponse er1 = new EspResponse();
        er1.setKey("key1");
        er1.setValue("value_1");
        er1.setPrettyValue("Value 1");
        
        EspResponse er2 = new EspResponse();
        er2.setKey("key1");
        er2.setValue("value_2");
        er2.setPrettyValue("Value 2");
        
        EspResponse er3 = new EspResponse();
        er3.setKey("key2");
        er3.setValue("value_3");
        er3.setPrettyValue("Value 3");
        
        EspResponse er4 = new EspResponse();
        er4.setKey("key2");
        er4.setValue("value_4");
        er4.setPrettyValue("Value 4");
        
        EspResponse er5 = new EspResponse();
        er5.setKey("key3");
        er5.setValue("value_5");
        er5.setPrettyValue("Value 5");
        
        EspResponse sa1 = new EspResponse();
        sa1.setKey("sa_1");
        sa1.setValue("sa1val");
        EspResponse sa1yr = new EspResponse();
        sa1yr.setKey("sa_1_year");
        sa1yr.setValue("1950");
        EspResponse sa2 = new EspResponse();
        sa2.setKey("sa_2");
        sa2.setValue("sa2val");
        EspResponse sa2yr = new EspResponse();
        sa2yr.setKey("sa_2_year");
        sa2yr.setValue("1960");
        EspResponse sa3 = new EspResponse();
        sa3.setKey("sa_3");
        sa3.setValue("sa3val");
        EspResponse sa3yr = new EspResponse();
        sa3yr.setKey("sa_3_year");
        sa3yr.setValue("1970");
        
        EspResponse fs1 = new EspResponse();
        fs1.setKey("feeder_school_1");
        fs1.setPrettyValue("Milwaukee French Immersion");
        EspResponse fs2 = new EspResponse();
        fs2.setKey("feeder_school_2");
        fs2.setPrettyValue("Milwaukee Spanish Immersion");
        EspResponse fs3 = new EspResponse();
        fs3.setKey("feeder_school_3");
        fs3.setPrettyValue("Milwaukee German Immersion");

        mr1 = new HashMap<String, List<EspResponse>>();
        mr1.put("key1", Arrays.asList(er1, er2));
        mr1.put("key2", Arrays.asList(er3, er4));
        mr1.put("key3", Arrays.asList(er5));

        mr2 = new HashMap<String, List<EspResponse>>();
        mr2.put("sa_1", Arrays.asList(sa1));
        mr2.put("sa_1_year", Arrays.asList(sa1yr));
        mr2.put("sa_2", Arrays.asList(sa2));
        mr2.put("sa_2_year", Arrays.asList(sa2yr));
        mr2.put("sa_3", Arrays.asList(sa3));
        mr2.put("sa_3_year", Arrays.asList(sa3yr));
        
        feederSchools = new HashMap<String, List<EspResponse>>();
        feederSchools.put("feeder_school_1", Arrays.asList(fs1));
        feederSchools.put("feeder_school_2", Arrays.asList(fs2));
        feederSchools.put("feeder_school_3", Arrays.asList(fs3));
    }

    //private EnhancedSchoolProfileController _controller;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //_controller = new EnhancedSchoolProfileController();
    }

    public void testMergeValuesForKeys() throws Exception {
        List<String> slist = EnhancedSchoolProfileController.mergeValuesForKeys(mr1, false, "key1", "key2", "key3");
        assertTrue(slist.size() == 5);
        assertEquals(slist.get(0), "value_1");
        assertEquals(slist.get(1), "value_2");
        assertEquals(slist.get(2), "value_3");
        assertEquals(slist.get(3), "value_4");
        assertEquals(slist.get(4), "value_5");
    }

    public void testMergeValuesForFeederSchools() throws Exception {
        List<String> slist = EnhancedSchoolProfileController.mergeValuesForKeys(feederSchools, true, "feeder_school_1", "feeder_school_2", "feeder_school_3");
        assertTrue(slist.size() == 3);
        assertEquals(slist.get(0), "Milwaukee French Immersion");
        assertEquals(slist.get(1), "Milwaukee Spanish Immersion");
        assertEquals(slist.get(2), "Milwaukee German Immersion");
    }

    public void testMergeValuesForDualKeys() throws Exception {
        String serviceAwards = EnhancedSchoolProfileController.mergeValuesForDualKeys(mr2, ", ", "; ", false, new String[][] {
            { "sa_1", "sa_1_year" },
            { "sa_2", "sa_2_year" },
            { "sa_3", "sa_3_year" }
        });
        assertEquals("sa1val, 1950; sa2val, 1960; sa3val, 1970", serviceAwards);
    }
}
