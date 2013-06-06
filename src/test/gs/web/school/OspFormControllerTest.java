package gs.web.school;
import gs.data.community.User;
import gs.data.school.*;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.ICensusInfo;
import gs.data.school.census.SchoolCensusValue;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.springframework.ui.ModelMap;

import java.util.*;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

/**
 * User: rraker
 * Date: 3/7/13
 */
public class OspFormControllerTest extends BaseControllerTestCase {

    OspFormController _ospFormController;

    private IEspResponseDao _espResponseDao;
    private ICensusInfo _censusInfo;
    private EspFormExternalDataHelper _espFormExternalDataHelper;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _ospFormController = new OspFormController();
        _espFormExternalDataHelper = new EspFormExternalDataHelper();
        _espResponseDao = createMock(IEspResponseDao.class);
        _censusInfo = createMock(ICensusInfo.class);

        _ospFormController.setEspResponseDao(_espResponseDao);
        _ospFormController.setEspFormExternalDataHelper(_espFormExternalDataHelper);
    }

    private void replayAllMocks() {
        replayMocks(_espResponseDao,_censusInfo);
    }

    private void verifyAllMocks() {
        verifyMocks(_espResponseDao,_censusInfo);
    }


    public void testAfterSchoolIndicatorDistrict1() {
        ModelMap modelMap = new ModelMap();
        School school = new School();
        school.setId(1000000);
        school.setDatabaseState(State.CA);
        school.setName("Chabot");
        school.setCity("xxxx");
        school.setStateAbbreviation(State.CA);
        school.setDistrictId(14);
        school.setNewProfileSchool(2);

//        List<EspResponse> l = new ArrayList<EspResponse>();
//        l.add(createEspResponse("academic_award_1", "Award 1"));
//        Map<String, List<EspResponse>> espData = convertToEspData(l);
//        modelMap.put()
        _ospFormController.putAfterSchoolIndicatorInModel(school, modelMap);

        String result = (String)modelMap.get("after_school_qualified");
        assertNotNull(result);
        assertEquals("yes", result);
    }

    public void testAfterSchoolIndicatorDistrict2() {
        ModelMap modelMap = new ModelMap();
        School school = new School();
        school.setId(1000000);
        school.setDatabaseState(State.CA);
        school.setName("Chabot");
        school.setCity("xxx");
        school.setStateAbbreviation(State.CA);
        school.setDistrictId(99);
        school.setNewProfileSchool(2);

//        List<EspResponse> l = new ArrayList<EspResponse>();
//        l.add(createEspResponse("academic_award_1", "Award 1"));
//        Map<String, List<EspResponse>> espData = convertToEspData(l);
//        modelMap.put()
        _ospFormController.putAfterSchoolIndicatorInModel(school, modelMap);

        String result = (String)modelMap.get("after_school_qualified");
        assertNull(result);
    }

    public void testAfterSchoolIndicatorCity1() {
        ModelMap modelMap = new ModelMap();
        School school = new School();
        school.setId(1000000);
        school.setDatabaseState(State.CA);
        school.setName("Chabot");
        school.setCity("xxx");
        school.setStateAbbreviation(State.CA);
        school.setDistrictId(999);
        school.setNewProfileSchool(2);

        _ospFormController.putAfterSchoolIndicatorInModel(school, modelMap);

        String result = (String)modelMap.get("after_school_qualified");
        assertNull(result);
    }

    public void testAfterSchoolIndicatorCity2() {
        ModelMap modelMap = new ModelMap();
        School school = new School();
        school.setId(1000000);
        school.setDatabaseState(State.CA);
        school.setName("Chabot");
        school.setCity("Oakland");
        school.setStateAbbreviation(State.CA);
        school.setDistrictId(999);
        school.setNewProfileSchool(2);

        _ospFormController.putAfterSchoolIndicatorInModel(school, modelMap);

        String result = (String)modelMap.get("after_school_qualified");
        assertNotNull(result);
        assertEquals("yes", result);
    }

    public void testRepeatingFormIndicator1() {
        ModelMap modelMap = new ModelMap();

        Map<String, EspFormResponseStruct> responseMap = new HashMap<String, EspFormResponseStruct>();
        EspFormResponseStruct r1 = new EspFormResponseStruct();
        r1.addValue("x");
        responseMap.put("after_school_name_1", r1);
        modelMap.put("responseMap", responseMap);

        String prefix = "after_school_";

        _ospFormController.putRepeatingFormIndicatorInModel(modelMap, prefix, 5);

        Integer result = (Integer)modelMap.get(prefix);
        assertNotNull(result);
        assertEquals("ResponseMap includes only set 1", new Integer(1), result);
    }

    public void testRepeatingFormIndicator2() {
        ModelMap modelMap = new ModelMap();

        Map<String, EspFormResponseStruct> responseMap = new HashMap<String, EspFormResponseStruct>();
        EspFormResponseStruct r1 = new EspFormResponseStruct();
        modelMap.put("responseMap", responseMap);

        String prefix = "after_school_";

        _ospFormController.putRepeatingFormIndicatorInModel(modelMap, prefix, 5);

        Integer result = (Integer)modelMap.get(prefix);
        assertNotNull(result);
        assertEquals("If there are no entries for this prefix should get 1 back", new Integer(1), result);
    }

    public void testRepeatingFormIndicator3() {
        ModelMap modelMap = new ModelMap();

        Map<String, EspFormResponseStruct> responseMap = new HashMap<String, EspFormResponseStruct>();
        EspFormResponseStruct r1 = new EspFormResponseStruct();
        r1.addValue("x");
        responseMap.put("after_school_name_4", r1);
        modelMap.put("responseMap", responseMap);

        String prefix = "after_school_";

        _ospFormController.putRepeatingFormIndicatorInModel(modelMap, prefix, 5);

        Integer result = (Integer)modelMap.get(prefix);
        assertNotNull(result);
        assertEquals("ResponseMap includes only set 4", new Integer(4), result);
    }

    public void testRepeatingFormIndicator4() {
        ModelMap modelMap = new ModelMap();

        Map<String, EspFormResponseStruct> responseMap = new HashMap<String, EspFormResponseStruct>();
        EspFormResponseStruct r1 = new EspFormResponseStruct();
        r1.addValue("x");
        responseMap.put("after_school_name_6", r1);
        modelMap.put("responseMap", responseMap);

        String prefix = "after_school_";

        _ospFormController.putRepeatingFormIndicatorInModel(modelMap, prefix, 5);

        Integer result = (Integer)modelMap.get(prefix);
        assertNotNull(result);
        assertEquals("Max datasets is 5, expect the default of 1 to be returned", new Integer(1), result);
    }

    // ========================== Utility methods ===============================
    private EspResponse createEspResponse( String key, String value ) {
        EspResponse response = new EspResponse();
        response.setActive( true );
        response.setKey( key );
        response.setValue( value );
        response.setPrettyValue( createPrettyValue(value) );
        return response;
    }

    private Map<String,List<EspResponse>> convertToEspData(List<EspResponse> l) {
        return EspResponse.rollup(l);
    }

    // Create a pretty value by capitalizing thr first character and removing underscores
    private String createPrettyValue( String value ) {
        StringBuilder sb = new StringBuilder();

        sb.append( Character.toUpperCase( value.charAt(0) ) );
        for( int i = 1; i < value.length(); i++ ) {
            char c = value.charAt(i);
            if( c == '_' ) {
                sb.append( ' ' );
            }
            else {
                sb.append( c );
            }
        }

        return sb.toString();
    }

    public void testPutProvisionalResponsesInModel_Non_ExternalData() {
        User user = new User();
        user.setId(2);

        ModelMap modelMap = new ModelMap();

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);
        school.setCensusInfo(_censusInfo);

        //Assume that provisional user modified page 1 and 2.
        //keys on Page 1:- early_childhood_programs, facilities and boys_sports.
        //keys on Page 2:- dress_code, parent_involvement and bullying_policy.
        List<EspResponse> provisionalKeysList = new ArrayList<EspResponse>();
        EspResponse espResponse1 = buildEspResponse("_page_1_keys","early_childhood_programs,facilities,boys_sports",false);
        EspResponse espResponse2 = buildEspResponse("_page_2_keys","dress_code,parent_involvement,bullying_policy",false);
        provisionalKeysList.add(espResponse1);
        provisionalKeysList.add(espResponse2);

        //List of responses that the provisional user made.
        //NOTE: Provisional user did not respond to parent_involvement.parent_involvement has an active value already.
        //NOTE: Provisional user did not respond to bullying_policy.bullying_policy does not have an active value already.
        List<EspResponse> provisionalResponses = new ArrayList<EspResponse>();
        EspResponse espResponse4 = buildEspResponse("early_childhood_programs","no",false);
        EspResponse espResponse5 = buildEspResponse("facilities","gym",false);
        EspResponse espResponse6 = buildEspResponse("dress_code","no_dress_code",false);
        //There should not be a provisionalResponse with active=1. So test this edge case.
        EspResponse espResponse7 = buildEspResponse("boys_sports","basketball",true);
        //There should not be a provisional response where the key is not marked for provisional.So test this edge case.
        EspResponse espResponse8 = buildEspResponse("student_clubs","computer_science",true);
        provisionalResponses.add(espResponse4);
        provisionalResponses.add(espResponse5);
        provisionalResponses.add(espResponse6);
        provisionalResponses.add(espResponse7);
        provisionalResponses.add(espResponse8);

        //List of active responses for the school.
        //NOTE: There are provisional responses for dress_code and parent_involvement.
        List<EspResponse> responses = new ArrayList<EspResponse>();
        EspResponse espResponse9 = buildEspResponse("transportation","none",true);
        EspResponse espResponse10 = buildEspResponse("dress_code","uniform",true);
        EspResponse espResponse11 = buildEspResponse("parent_involvement","ice_cream_social",true);
        responses.add(espResponse9);
        responses.add(espResponse10);
        responses.add(espResponse11);
        Set<EspResponseSource> responseSources = new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.osp));

        expect(_espResponseDao.getAllProvisionalResponseKeysByUserAndSchool(school, user.getId(), true)).andReturn(provisionalKeysList);
        expect(_espResponseDao.getResponses(school, user.getId(), true)).andReturn(provisionalResponses);
        expect(_espResponseDao.getResponses(school, responseSources)).andReturn(responses);

        SchoolCensusValue censusValue = new SchoolCensusValue();
        censusValue.setValueText("abcd@somedomain.com");
        expect(_censusInfo.getManual(school, CensusDataType.HEAD_OFFICIAL_EMAIL)).andReturn(censusValue);

        expect(_censusInfo.getEnrollmentAsInteger(school)).andReturn(12);

        SchoolCensusValue censusValue1 = new SchoolCensusValue();
        censusValue1.setValueText("abcd");
        expect(_censusInfo.getManual(school,CensusDataType.HEAD_OFFICIAL_NAME)).andReturn(censusValue1);

        replayAllMocks();
        _ospFormController.putProvisionalResponsesInModel(user, school, modelMap);
        verifyAllMocks();

        Map<String, EspFormResponseStruct> responseMap = (HashMap<String, EspFormResponseStruct>)modelMap.get("responseMap");

        assertEquals("early_childhood_programs was on the page that the provisional user modified." +
                "Hence display the provisional value","no",responseMap.get("early_childhood_programs").getValue());

        assertEquals("facilities was on the page that the provisional user modified." +
                "Hence display the provisional value","gym",responseMap.get("facilities").getValue());

        assertNull("boys_sports was on the page that the provisional user modified." +
                "However the response is active.This should not happen.Hence ignore the response."
                ,responseMap.get("boys_sports"));

        assertNull("student_clubs was not on the page that the provisional user modified." +
                "However the it is returned as a provisional response.This should not happen.Hence ignore the response."
                ,responseMap.get("student_clubs"));

        assertEquals("dress_code was on the page that the provisional user modified." +
                "Hence display the provisional value. NOTE that dress_code already has an active value." +
                "However the provisional user should see the provisional value","no_dress_code",responseMap.get("dress_code").getValue());

        assertNull("parent_involvement already had an active value.parent_involvement was on the page that the provisional user modified." +
                "Provisional user did not respond to parent_involvement. Hence there is no provisional value to display." +
                "",responseMap.get("parent_involvement"));

        assertNull("bullying_policy was on the page that the provisional user modified." +
                "Provisional user did not respond to bullying_policy. Hence there is no provisional value to display." +
                "",responseMap.get("bullying_policy"));

        assertEquals("transportation was not on the page that the provisional user modified." +
                "Hence display the non-provisional value","none",responseMap.get("transportation").getValue());

    }

    public void testPutProvisionalResponsesInModel_ExternalData() {
        User user = new User();
        user.setId(2);

        ModelMap modelMap = new ModelMap();

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);
        school.setCensusInfo(_censusInfo);
        school.setType(SchoolType.CHARTER);
        school.putMetadata("facebook_url","someurl");

        //Assume that provisional user modified page 1 and 2.
        //keys on Page 1:- student_enrollment, grade_levels.
        //keys on Page 2:- administrator_name, administrator_email, school_phone ,coed and facebook_url.
        List<EspResponse> provisionalKeysList = new ArrayList<EspResponse>();
        EspResponse espResponse1 = buildEspResponse("_page_1_keys","student_enrollment,grade_levels",false);
        EspResponse espResponse2 = buildEspResponse("_page_2_keys","administrator_name,administrator_email,school_phone,coed,facebook_url",false);
        provisionalKeysList.add(espResponse1);
        provisionalKeysList.add(espResponse2);

        //List of responses that the provisional user made.
        //NOTE: Provisional user did not respond to administrator_email and coed.
        //NOTE: There is already an active response to facebook_url.
        List<EspResponse> provisionalResponses = new ArrayList<EspResponse>();
        EspResponse espResponse4 = buildEspResponse("student_enrollment","133",false);
        EspResponse espResponse5 = buildEspResponse("grade_levels","4",false);
        EspResponse espResponse6 = buildEspResponse("grade_levels","5",false);
        EspResponse espResponse7 = buildEspResponse("grade_levels","6",false);
        EspResponse espResponse8 = buildEspResponse("administrator_name","abcd",false);
        EspResponse espResponse9 = buildEspResponse("facebook_url","schoolurl@facebook.com",false);
        //There should not be a provisionalResponse with active=1. So test this edge case.
        EspResponse espResponse10 = buildEspResponse("school_phone","1231231234",true);
        //There should not be a provisional response where the key is not marked for provisional.So test this edge case.
        EspResponse espResponse11 = buildEspResponse("school_fax","1231231237",true);
        provisionalResponses.add(espResponse4);
        provisionalResponses.add(espResponse5);
        provisionalResponses.add(espResponse6);
        provisionalResponses.add(espResponse7);
        provisionalResponses.add(espResponse8);
        provisionalResponses.add(espResponse9);
        provisionalResponses.add(espResponse10);
        provisionalResponses.add(espResponse11);

        //List of active responses for the school.
        List<EspResponse> responses = new ArrayList<EspResponse>();

        Set<EspResponseSource> responseSources = new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.osp));

        expect(_espResponseDao.getAllProvisionalResponseKeysByUserAndSchool(school, user.getId(), true)).andReturn(provisionalKeysList);
        expect(_espResponseDao.getResponses(school, user.getId(), true)).andReturn(provisionalResponses);
        expect(_espResponseDao.getResponses(school, responseSources)).andReturn(responses);

        replayAllMocks();
        _ospFormController.putProvisionalResponsesInModel(user, school, modelMap);
        verifyAllMocks();

        Map<String, EspFormResponseStruct> responseMap = (HashMap<String, EspFormResponseStruct>)modelMap.get("responseMap");

        assertEquals("student_enrollment was on the page that the provisional user modified.Hence display the provisional value",
                "133",responseMap.get("student_enrollment").getValue());

        Map<String, Boolean> gradeValueMap = responseMap.get("grade_levels").getValueMap();

        assertEquals("grade_levels was on the page that the provisional user modified." +
                "Hence display the provisional value",3,gradeValueMap.size());

        assertTrue("grade_levels was on the page that the provisional user modified." +
                "Hence display the provisional value",gradeValueMap.containsKey("4"));

        assertTrue("grade_levels was on the page that the provisional user modified." +
                "Hence display the provisional value",gradeValueMap.containsKey("5"));

        assertTrue("grade_levels was on the page that the provisional user modified." +
                "Hence display the provisional value",gradeValueMap.containsKey("6"));

        assertEquals("administrator_name was on the page that the provisional user modified." +
                "Hence display the provisional value.",
                "abcd",responseMap.get("administrator_name").getValue());

        assertNull("administrator_email was on the page that the provisional user modified.But the user did not respond to the question." +
                "Hence there is no provisional value to display",
                responseMap.get("administrator_email"));

        assertNull("school_phone was on the page that the provisional user modified." +
                "However the response is active.This should not happen.Hence ignore the response."
                ,responseMap.get("school_phone"));

        assertNull("school_fax was not on the page that the provisional user modified." +
                "However the it is returned as a provisional response.This should not happen.Hence ignore the response."
                ,responseMap.get("school_fax"));

        assertNull("coed was on the page that the provisional user modified.But the user did not respond to the question." +
                "Hence there is no provisional value to display"
                ,responseMap.get("coed"));

        assertEquals("facebook_url has an active value.facebook_url was on the page that the provisional user modified." +
                "Hence display the provisional value.",
                "schoolurl@facebook.com",responseMap.get("facebook_url").getValue());

        assertEquals("school_type was not on the page that the provisional user modified." +
                "Hence display the non-provisional value","charter",responseMap.get("school_type").getValue());

    }

    public EspResponse buildEspResponse(String key, String value,boolean active) {
        EspResponse espResponse = new EspResponse();
        espResponse.setKey(key);
        espResponse.setValue(value);
        espResponse.setActive(active);
        return espResponse;
    }

}

