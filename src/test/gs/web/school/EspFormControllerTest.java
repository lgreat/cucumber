package gs.web.school;

import gs.data.community.User;
import gs.data.school.*;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.ICensusDataSetDao;
import gs.data.school.census.ICensusInfo;
import gs.data.school.census.SchoolCensusValue;
import gs.data.state.INoEditDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.springframework.ui.ModelMap;

import java.util.*;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

public class EspFormControllerTest extends BaseControllerTestCase {
    private EspFormController _controller;

    private EspFormExternalDataHelper _espFormExternalDataHelper;
    private EspFormValidationHelper _espFormValidationHelper;
    private INoEditDao _noEditDao;
    private IEspMembershipDao _espMembershipDao;
    private IEspResponseDao _espResponseDao;
    private ICensusDataSetDao _censusDataSetDao;
    private ICensusInfo _censusInfo;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _controller = new EspFormController();

        _espFormExternalDataHelper = new EspFormExternalDataHelper();
        _espFormValidationHelper = new EspFormValidationHelper();
        _noEditDao = createMock(INoEditDao.class);
        _espMembershipDao = createMock(IEspMembershipDao.class);
        _espResponseDao = createMock(IEspResponseDao.class);
        _censusDataSetDao = createMock(ICensusDataSetDao.class);
        _censusInfo = createMock(ICensusInfo.class);

        _controller.setEspFormExternalDataHelper(_espFormExternalDataHelper);
        _controller.setEspFormValidationHelper(_espFormValidationHelper);
        _controller.setNoEditDao(_noEditDao);
        _espFormValidationHelper.setEspMembershipDao(_espMembershipDao);
        _controller.setEspResponseDao(_espResponseDao);
    }

    private void replayAllMocks() {
        replayMocks(_noEditDao, _espMembershipDao, _espResponseDao,_censusInfo);
    }

    private void verifyAllMocks() {
        verifyMocks(_noEditDao, _espMembershipDao, _espResponseDao,_censusInfo);
    }

    public void testSaveEspFormDataBasicUserApproved() {
        User user = new User();
        user.setId(2);
        School school = new School();
        Set<String> keysForPage = new HashSet<String>();
        keysForPage.add("instructional_model");
        Map<String, Object[]> keyToResponseMap = new HashMap<String, Object[]>();
        State state = State.CA;
        int pageNum = 1;
        Map<String, String> errorFieldToMsgMap = new HashMap<String, String>();

        List<EspResponse> responseList = new ArrayList<EspResponse>();

        expect(_noEditDao.isStateLocked(state)).andReturn(false);
        _espResponseDao.deactivateResponsesByKeys(school, keysForPage);

        replayAllMocks();
        _controller.saveEspFormData(user, school, keysForPage, keyToResponseMap, state, pageNum, errorFieldToMsgMap, responseList, false);
        verifyAllMocks();

        assertEquals(true, errorFieldToMsgMap.isEmpty());
    }

    public void testSaveEspFormDataBasicUserProvisional() {
        User user = new User();
        user.setId(2);
        School school = new School();
        Set<String> keysForPage = new HashSet<String>();
        keysForPage.add("instructional_model");
        Map<String, Object[]> keyToResponseMap = new HashMap<String, Object[]>();
        State state = State.CA;
        int pageNum = 1;
        Map<String, String> errorFieldToMsgMap = new HashMap<String, String>();

        expect(_noEditDao.isStateLocked(state)).andReturn(false);
        Set<String> keysToDelete = new HashSet<String>();
        keysToDelete.addAll(keysForPage);
        String key = _controller.getPageKeys(pageNum);
        keysToDelete.add(key);
        _espResponseDao.deleteResponsesForSchoolByUserAndByKeys(school, user.getId(), keysToDelete);
        List<EspResponse> responseList = new ArrayList<EspResponse>();
        _espResponseDao.saveResponses(school, responseList);

        replayAllMocks();
        _controller.saveEspFormData(user, school, keysForPage, keyToResponseMap, state, pageNum,
                errorFieldToMsgMap, responseList, true);
        verifyAllMocks();

        assertEquals(true, errorFieldToMsgMap.isEmpty());
    }

    public void testSaveEspFormDataWithValidationErrors() {
        User user = new User();
        user.setId(2);
        School school = new School();
        Set<String> keysForPage = new HashSet<String>();
        Map<String, Object[]> keyToResponseMap = new HashMap<String, Object[]>();
        State state = State.CA;
        int pageNum = 1;
        Map<String, String> errorFieldToMsgMap = new HashMap<String, String>();
        List<EspResponse> responseList = new ArrayList<EspResponse>();

        //Error in school phone
        keyToResponseMap.put("school_phone_area_code", new Object[]{"abc"});
        keyToResponseMap.put("school_phone_office_code", new Object[]{"abc"});
        keyToResponseMap.put("school_phone_last_four", new Object[]{"abc"});

        //Error in census
        keyToResponseMap.put("ethnicity_6", new Object[]{"abc"});
        keyToResponseMap.put("census_ethnicity_unavailable", new Object[]{1});
        keysForPage.add("census_ethnicity_unavailable");
        keysForPage.add("ethnicity_6");

        //Error in avg class size
        keysForPage.add("average_class_size");

        replayAllMocks();
        _controller.saveEspFormData(user, school, keysForPage, keyToResponseMap, state, pageNum,
                errorFieldToMsgMap, responseList, false);
        verifyAllMocks();

        assertEquals("Phone number must be numeric", errorFieldToMsgMap.get("school_phone"));
        assertEquals("Value must be numeric.", errorFieldToMsgMap.get("ethnicity"));
        assertEquals("Must be positive integer", errorFieldToMsgMap.get("Average class size"));
    }

    public void testSaveEspFormDataWithExternalDataErrorsApprovedUser() {
        User user = new User();
        user.setId(2);
        School school = new School();
        Set<String> keysForPage = new HashSet<String>();
        // student_enrollment is external data
        keysForPage.add("student_enrollment");
        Map<String, Object[]> keyToResponseMap = new HashMap<String, Object[]>();
        keyToResponseMap.put("student_enrollment", new Object[]{"abc"});

        State state = State.CA;
        int pageNum = 1;
        Map<String, String> errorFieldToMsgMap = new HashMap<String, String>();

        List<EspResponse> responseList = new ArrayList<EspResponse>();

        expect(_noEditDao.isStateLocked(state)).andReturn(false);

        replayAllMocks();
        _controller.saveEspFormData(user, school, keysForPage, keyToResponseMap, state, pageNum, errorFieldToMsgMap,
                responseList, false);
        verifyAllMocks();

        assertEquals("Must be an integer.", errorFieldToMsgMap.get("student_enrollment"));
    }


    public void testSaveEspFormDataWithExternalDataErrorsProvisionalUser() {
        User user = new User();
        user.setId(2);
        School school = new School();
        Set<String> keysForPage = new HashSet<String>();
        // grade_levels is external data
        keysForPage.add("grade_levels");
        Map<String, Object[]> keyToResponseMap = new HashMap<String, Object[]>();
        keyToResponseMap.put("grade_levels", new String[]{"abc"});

        State state = State.CA;
        int pageNum = 1;
        Map<String, String> errorFieldToMsgMap = new HashMap<String, String>();

        List<EspResponse> responseList = new ArrayList<EspResponse>();

        expect(_noEditDao.isStateLocked(state)).andReturn(false);

        replayAllMocks();
        _controller.saveEspFormData(user, school, keysForPage, keyToResponseMap, state, pageNum, errorFieldToMsgMap,
                responseList, true);
        verifyAllMocks();

        assertEquals("You must select a grade level.", errorFieldToMsgMap.get("grade_levels"));
    }

    public void testSaveESPResponsesApprovedUser() {
        User user = new User();
        user.setId(2);
        School school = new School();
        Set<String> keysForPage = new HashSet<String>();
        keysForPage.add("grade_levels");
        int pageNum = 1;
        List<EspResponse> responseList = new ArrayList<EspResponse>();
        _espResponseDao.deactivateResponsesByKeys(school, keysForPage);

        replayAllMocks();
        _controller.saveESPResponses(school, keysForPage, responseList, false, user, pageNum, new Date());
        verifyAllMocks();
    }

    public void testSaveESPResponsesProvisionalUser() {
        User user = new User();
        user.setId(2);
        int pageNum = 1;
        School school = new School();
        Set<String> keysForPage = new HashSet<String>();
        keysForPage.add("instructional_model");
        Set<String> keysToDelete = new HashSet<String>();
        keysToDelete.addAll(keysForPage);
        String key = _controller.getPageKeys(pageNum);
        keysToDelete.add(key);
        keysToDelete.add(key);

        List<EspResponse> responseList = new ArrayList<EspResponse>();
        EspResponse response = new EspResponse();
        response.setKey("instructional_model");
        response.setValue("none");
        responseList.add(response);
        _espResponseDao.deleteResponsesForSchoolByUserAndByKeys(school, user.getId(), keysToDelete);
        _espResponseDao.saveResponses(school, responseList);
        replayAllMocks();
        _controller.saveESPResponses(school, keysForPage, responseList, true, user, pageNum, new Date());
        verifyAllMocks();
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
        //keys on Page 1:- early_childhood_programs, coed and boys_sports.
        //keys on Page 2:- dress_code, parent_involvement and bullying_policy.
        List<EspResponse> provisionalKeysList = new ArrayList<EspResponse>();
        EspResponse espResponse1 = buildEspResponse("_page_1_keys","early_childhood_programs,coed,boys_sports",false);
        EspResponse espResponse2 = buildEspResponse("_page_2_keys","dress_code,parent_involvement,bullying_policy",false);
        provisionalKeysList.add(espResponse1);
        provisionalKeysList.add(espResponse2);

        //List of responses that the provisional user made.
        //NOTE: Provisional user did not respond to parent_involvement.parent_involvement has an active value already.
        //NOTE: Provisional user did not respond to bullying_policy.bullying_policy does not have an active value already.
        List<EspResponse> provisionalResponses = new ArrayList<EspResponse>();
        EspResponse espResponse4 = buildEspResponse("early_childhood_programs","no",false);
        EspResponse espResponse5 = buildEspResponse("coed","all_boys",false);
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

        expect(_espResponseDao.getAllProvisionalResponseKeysByUserAndSchool(school, user.getId(), true)).andReturn(provisionalKeysList);
        expect(_espResponseDao.getResponsesByUserAndSchool(school, user.getId(), true)).andReturn(provisionalResponses);
        expect(_espResponseDao.getResponses(school)).andReturn(responses);

        SchoolCensusValue censusValue = new SchoolCensusValue();
        censusValue.setValueText("somemeail");
        expect(_censusInfo.getManual(school,CensusDataType.HEAD_OFFICIAL_EMAIL)).andReturn(censusValue);

        expect(_censusInfo.getEnrollmentAsInteger(school)).andReturn(12);

        SchoolCensusValue censusValue1 = new SchoolCensusValue();
        censusValue1.setValueText("somename");
        expect(_censusInfo.getManual(school,CensusDataType.HEAD_OFFICIAL_NAME)).andReturn(censusValue1);

        replayAllMocks();
        _controller.putProvisionalResponsesInModel(user, school, modelMap);
        verifyAllMocks();

        Map<String, EspFormResponseStruct> responseMap = (HashMap<String, EspFormResponseStruct>)modelMap.get("responseMap");

        assertEquals("early_childhood_programs was on the page that the provisional user modified." +
                "Hence display the provisional value","no",responseMap.get("early_childhood_programs").getValue());

        assertEquals("coed was on the page that the provisional user modified." +
                "Hence display the provisional value","all_boys",responseMap.get("coed").getValue());

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


    public EspResponse buildEspResponse(String key, String value,boolean active) {
        EspResponse espResponse = new EspResponse();
        espResponse.setKey(key);
        espResponse.setValue(value);
        espResponse.setActive(active);
        return espResponse;
    }





































}