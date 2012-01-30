package gs.web.school;

import gs.web.BaseControllerTestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: jkirton
 */
public class EnhancedSchoolProfileControllerTest extends BaseControllerTestCase {

    static final Map<String, List<String>> mr1, mr2, feederSchools;

    static {
        mr1 = new HashMap<String, List<String>>();
        mr1.put("key1", Arrays.asList("val1a", "val1b", "val1c"));
        mr1.put("key2", Arrays.asList("val2a"));
        mr1.put("key3", Arrays.asList("val3a", "val3b"));

        mr2 = new HashMap<String, List<String>>();
        mr2.put("sa_1", Arrays.asList("sa1val"));
        mr2.put("sa_1_year", Arrays.asList("1950"));
        mr2.put("sa_2", Arrays.asList("sa2val"));
        mr2.put("sa_2_year", Arrays.asList("1960"));
        mr2.put("sa_3", Arrays.asList("sa3val"));
        mr2.put("sa_3_year", Arrays.asList("1970"));
        
        feederSchools = new HashMap<String, List<String>>();
        feederSchools.put("feeder_school_1", Arrays.asList("Milwaukee French Immersion"));
        feederSchools.put("feeder_school_2", Arrays.asList("Milwaukee Spanish Immersion"));
        feederSchools.put("feeder_school_3", Arrays.asList("Milwaukee German Immersion"));
    }

    //private EnhancedSchoolProfileController _controller;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //_controller = new EnhancedSchoolProfileController();
    }

    public void testMergeValuesForKeys() throws Exception {
        String s = EnhancedSchoolProfileController.mergeValuesForKeys(mr1, "; ", "key1", "key2", "key3");
        assertEquals("val1a; val1b; val1c; val2a; val3a; val3b", s);
    }

    public void testMergeValuesForFeederSchools() throws Exception {
        String s = EnhancedSchoolProfileController.mergeValuesForKeys(feederSchools, "; ", "feeder_school_1", "feeder_school_2", "feeder_school_3");
        assertEquals("Milwaukee French Immersion; Milwaukee Spanish Immersion; Milwaukee German Immersion", s);
    }

    public void testMergeValuesForDualKeys() throws Exception {
        String serviceAwards = EnhancedSchoolProfileController.mergeValuesForDualKeys(mr2, ", ", "; ", new String[][] {
            { "sa_1", "sa_1_year" },
            { "sa_2", "sa_2_year" },
            { "sa_3", "sa_3_year" }
        });
        assertEquals("sa1val, 1950; sa2val, 1960; sa3val, 1970", serviceAwards);
    }
}
