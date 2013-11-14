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

    public void testAddInResponseCount() {
        List<SchoolProfileClimateController.ClimateResponseCount> list = new ArrayList<SchoolProfileClimateController.ClimateResponseCount>();
        Map<CensusDataType, CensusDataSet> dataTypeToDataSet = new HashMap<CensusDataType, CensusDataSet>();
        SchoolProfileClimateController.ClimateRespondentType respondentType = SchoolProfileClimateController.ClimateRespondentType.parents;
        CensusDataType responseRateDataType = CensusDataType.CLIMATE_RESPONSE_RATE_PARENT;
        CensusDataType numResponsesDataType = CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_PARENT;
        CensusDataSet responseRate = new CensusDataSet(responseRateDataType, 2013);
        CensusDataSet numResponses = new CensusDataSet(numResponsesDataType, 2013);
        dataTypeToDataSet.put(responseRateDataType, responseRate);
        dataTypeToDataSet.put(numResponsesDataType, numResponses);

        SchoolProfileClimateController.addInResponseCount(list, dataTypeToDataSet, respondentType, responseRateDataType, numResponsesDataType);
        assertEquals(1, list.size());
        SchoolProfileClimateController.ClimateResponseCount rval = list.get(0);
        assertSame(responseRate, rval.getResponseRate());
        assertSame(numResponses, rval.getNumberOfResponses());
        assertSame(respondentType, rval.getRespondentType());
    }

    public void testGetClimateResponseCounts() {
        // create a mock map that responds to any get with a data set
        Map<CensusDataType, CensusDataSet> dataTypeToDataSet = createMock(Map.class);
        expect(dataTypeToDataSet.get(isA(CensusDataType.class))).andReturn(new CensusDataSet()).anyTimes();
        replay(dataTypeToDataSet);

        // I expect the following to create a ClimateResponseCount for every respondent type
        List<SchoolProfileClimateController.ClimateResponseCount> responseCounts =
                SchoolProfileClimateController.getClimateResponseCounts(dataTypeToDataSet);

        // now verify that every respondent is represented in responseCounts
        verify(dataTypeToDataSet);
        List<SchoolProfileClimateController.ClimateRespondentType> sortedRespondentTypes = Arrays.asList(SchoolProfileClimateController.ClimateRespondentType.values());
        Collections.sort(sortedRespondentTypes, new Comparator<SchoolProfileClimateController.ClimateRespondentType>() {
            public int compare(SchoolProfileClimateController.ClimateRespondentType o1, SchoolProfileClimateController.ClimateRespondentType o2) {
                return o1.getSortOrder().compareTo(o2.getSortOrder());
            }
        });
        assertEquals("Expect every respondent to be represented", sortedRespondentTypes.size(), responseCounts.size());
        int index=0;
        for (SchoolProfileClimateController.ClimateRespondentType respondent: sortedRespondentTypes) {
            SchoolProfileClimateController.ClimateResponseCount count = responseCounts.get(index);
            assertEquals("Expect every respondent to be represented in sorted order", respondent, count.getRespondentType());
            index++;
        }
    }
}
