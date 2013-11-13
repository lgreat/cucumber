package gs.web.school;

import gs.data.school.census.CensusDataSet;
import gs.data.school.census.CensusDataType;
import gs.web.BaseControllerTestCase;

import java.util.*;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author aroy@greatschools.org
 */
public class SchoolProfileClimateControllerTest extends BaseControllerTestCase {
    private SchoolProfileClimateController _controller;
    private SchoolProfileCensusHelper _schoolProfileCensusHelper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        _controller = new SchoolProfileClimateController();

        _schoolProfileCensusHelper = createStrictMock(SchoolProfileCensusHelper.class);

        _controller.setSchoolProfileCensusHelper(_schoolProfileCensusHelper);
    }

    private void replayAllMocks() {
        replayMocks(_schoolProfileCensusHelper);
    }

    private void verifyAllMocks() {
        verifyMocks(_schoolProfileCensusHelper);
    }

    public void testBasics() {
        assertNotNull(_controller);
        assertSame(_controller.getSchoolProfileCensusHelper(), _schoolProfileCensusHelper);
    }

    private void verifyPresenceInMapKeys(CensusDataType toVerify, String constantName, String sourceConstantName) throws NoSuchFieldException, IllegalAccessException {
        assertNotNull("Expect " + toVerify + ", which is configured in SchoolProfileClimateController." + sourceConstantName + ", " +
                "to exist as a key in SchoolProfileClimateController." + constantName,
                ((Map) SchoolProfileClimateController.class.getDeclaredField(constantName).get(null)).get(toVerify));
    }

    private void verifyPresenceInMapValues(CensusDataType toVerify, String constantName, String sourceConstantName) throws NoSuchFieldException, IllegalAccessException {
        assertTrue("Expect " + toVerify + ", which is configured in SchoolProfileClimateController." + sourceConstantName + ", " +
                "to exist as a value in SchoolProfileClimateController." + constantName,
                ((Map)SchoolProfileClimateController.class.getDeclaredField(constantName).get(null)).values().contains(toVerify));
    }

    private void verifyPresenceInMapOfListValues(CensusDataType toVerify, String constantName, String sourceConstantName) throws NoSuchFieldException, IllegalAccessException {
        boolean foundBreakdown = false;
        for (Object obj: ((Map)SchoolProfileClimateController.class.getDeclaredField(constantName).get(null)).values()) {
            if (((Collection)obj).contains(toVerify)) {
                foundBreakdown = true;
            }
        }
        assertTrue("Expect " + toVerify + ", which is configured in SchoolProfileClimateController." + sourceConstantName + ", " +
                "to exist as a value in SchoolProfileClimateController." + constantName, foundBreakdown);
    }

    public void testConfigurationMaps() throws NoSuchFieldException, IllegalAccessException {
        /* BREAKDOWN_TO_RESPONDENT_TYPE -- breakdown
         * TOTAL_TO_BREAKDOWN_MAP -- breakdown, total
         * TOTAL_DATA_TYPE_ORDER_MAP -- total
         * BREAKDOWN_TO_TOTAL_MAP -- breakdown, total
         *
         * test breakdowns from/to BREAKDOWN_TO_RESPONDENT_TYPE, TOTAL_TO_BREAKDOWN_MAP, BREAKDOWN_TO_TOTAL_MAP
         * test totals from/to TOTAL_TO_BREAKDOWN_MAP, TOTAL_DATA_TYPE_ORDER_MAP, BREAKDOWN_TO_TOTAL_MAP
         */

        // tests for totals
        for (CensusDataType total: SchoolProfileClimateController.TOTAL_TO_BREAKDOWN_MAP.keySet()) {
            verifyPresenceInMapKeys(total, "TOTAL_DATA_TYPE_ORDER_MAP", "TOTAL_TO_BREAKDOWN_MAP");
            verifyPresenceInMapValues(total, "BREAKDOWN_TO_TOTAL_MAP", "TOTAL_TO_BREAKDOWN_MAP");
            assertTrue("Expect " + total + " to be a climate data type in isDataTypeForClimate",
                    SchoolProfileClimateController.isDataTypeForClimate(total));
        }
        for (CensusDataType total: SchoolProfileClimateController.TOTAL_DATA_TYPE_ORDER_MAP.keySet()) {
            verifyPresenceInMapKeys(total, "TOTAL_TO_BREAKDOWN_MAP", "TOTAL_DATA_TYPE_ORDER_MAP");
            verifyPresenceInMapValues(total, "BREAKDOWN_TO_TOTAL_MAP", "TOTAL_DATA_TYPE_ORDER_MAP");
            assertTrue("Expect " + total + " to be a climate data type in isDataTypeForClimate",
                    SchoolProfileClimateController.isDataTypeForClimate(total));
        }
        for (CensusDataType total: SchoolProfileClimateController.BREAKDOWN_TO_TOTAL_MAP.values()) {
            verifyPresenceInMapKeys(total, "TOTAL_DATA_TYPE_ORDER_MAP", "BREAKDOWN_TO_TOTAL_MAP");
            verifyPresenceInMapKeys(total, "TOTAL_TO_BREAKDOWN_MAP", "BREAKDOWN_TO_TOTAL_MAP");
            assertTrue("Expect " + total + " to be a climate data type in isDataTypeForClimate",
                    SchoolProfileClimateController.isDataTypeForClimate(total));
        }
        // tests for breakdowns
        for (List<CensusDataType> breakdownList: SchoolProfileClimateController.TOTAL_TO_BREAKDOWN_MAP.values()) {
            for (CensusDataType breakdown: breakdownList) {
                verifyPresenceInMapKeys(breakdown, "BREAKDOWN_TO_RESPONDENT_TYPE", "TOTAL_TO_BREAKDOWN_MAP");
                verifyPresenceInMapKeys(breakdown, "BREAKDOWN_TO_TOTAL_MAP", "TOTAL_TO_BREAKDOWN_MAP");
                assertTrue("Expect " + breakdown + " to be a climate data type in isDataTypeForClimate",
                        SchoolProfileClimateController.isDataTypeForClimate(breakdown));
            }
        }
        for (CensusDataType breakdown: SchoolProfileClimateController.BREAKDOWN_TO_TOTAL_MAP.keySet()) {
            verifyPresenceInMapKeys(breakdown, "BREAKDOWN_TO_RESPONDENT_TYPE", "BREAKDOWN_TO_TOTAL_MAP");
            verifyPresenceInMapOfListValues(breakdown, "TOTAL_TO_BREAKDOWN_MAP", "BREAKDOWN_TO_TOTAL_MAP");
            assertTrue("Expect " + breakdown + " to be a climate data type in isDataTypeForClimate",
                    SchoolProfileClimateController.isDataTypeForClimate(breakdown));
        }
        for (CensusDataType breakdown: SchoolProfileClimateController.BREAKDOWN_TO_RESPONDENT_TYPE.keySet()) {
            verifyPresenceInMapKeys(breakdown, "BREAKDOWN_TO_TOTAL_MAP", "BREAKDOWN_TO_RESPONDENT_TYPE");
            verifyPresenceInMapOfListValues(breakdown, "TOTAL_TO_BREAKDOWN_MAP", "BREAKDOWN_TO_RESPONDENT_TYPE");
            assertTrue("Expect " + breakdown + " to be a climate data type in isDataTypeForClimate",
                    SchoolProfileClimateController.isDataTypeForClimate(breakdown));
        }
    }

//    public void testAddInResponseCount() {
//        List<SchoolProfileClimateController.ClimateResponseCount> list = new ArrayList<SchoolProfileClimateController.ClimateResponseCount>();
//        Map<Integer, CensusDataSet> dataTypeIdToDataSet = new HashMap<Integer, CensusDataSet>();
//
////        SchoolProfileClimateController.addInResponseCount(list, dataTypeIdToDataSet, respondentTypeEnum, responseRate, numResponses);
//    }
}
