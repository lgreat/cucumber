package gs.web.school;

import gs.data.community.User;
import gs.data.school.EspResponse;
import gs.data.school.IEspResponseDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.INoEditDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;

import java.util.*;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;


public class EspHelperTest extends BaseControllerTestCase {
    private EspHelper _helper;
    private IEspResponseDao _espResponseDao;
    private INoEditDao _noEditDao;
    private ISchoolDao _schoolDao;
    private EspFormExternalDataHelper _espFormExternalDataHelper;
    private EspFormValidationHelper _espFormValidationHelper;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _helper = new EspHelper();
        _espFormExternalDataHelper = new EspFormExternalDataHelper();
        _espFormValidationHelper = new EspFormValidationHelper();
        _espResponseDao = createMock(IEspResponseDao.class);
        _noEditDao = createMock(INoEditDao.class);
        _schoolDao = createMock(ISchoolDao.class);

        _helper.setNoEditDao(_noEditDao);
        _helper.setEspResponseDao(_espResponseDao);
        _helper.setEspFormExternalDataHelper(_espFormExternalDataHelper);
        _helper.setEspFormValidationHelper(_espFormValidationHelper);

        _espFormExternalDataHelper.setSchoolDao(_schoolDao);
    }

    private void replayAllMocks() {
        replayMocks(_noEditDao, _espResponseDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_noEditDao, _espResponseDao);
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
        _helper.saveEspFormData(user, school, keysForPage, keyToResponseMap, state, pageNum, errorFieldToMsgMap, responseList, false,false);
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
        String key = _helper.getPageKeys(pageNum);
        keysToDelete.add(key);
        _espResponseDao.deleteResponsesForSchoolByUserAndByKeys(school, user.getId(), keysToDelete);
        List<EspResponse> responseList = new ArrayList<EspResponse>();
        _espResponseDao.saveResponses(school, responseList);

        replayAllMocks();
        _helper.saveEspFormData(user, school, keysForPage, keyToResponseMap, state, pageNum,
                errorFieldToMsgMap, responseList, true,false);
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
        _helper.saveEspFormData(user, school, keysForPage, keyToResponseMap, state, pageNum,
                errorFieldToMsgMap, responseList, false,false);
        verifyAllMocks();

        assertEquals("Phone number must be numeric", errorFieldToMsgMap.get("school_phone"));
        assertEquals("Value must be numeric.", errorFieldToMsgMap.get("ethnicity"));
        assertEquals("Must be positive integer", errorFieldToMsgMap.get("average_class_size"));
    }

    public void testSaveEspFormDataWithValidationErrorsIgnoreErrors() {
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
        keysForPage.add("ethnicity_6");

        //Error in avg class size
        keysForPage.add("average_class_size");

        //No Error in transportation.Hence it should be saved to the database
        keyToResponseMap.put("transportation", new Object[]{"none"});
        keysForPage.add("transportation");

        expect(_noEditDao.isStateLocked(state)).andReturn(false);
        _espResponseDao.deactivateResponsesByKeys(school,keysForPage);
        _espResponseDao.saveResponses(school, responseList);

        replayAllMocks();
        _helper.saveEspFormData(user, school, keysForPage, keyToResponseMap, state, pageNum,
                errorFieldToMsgMap, responseList, false, true);
        verifyAllMocks();

        assertEquals("Phone number must be numeric", errorFieldToMsgMap.get("school_phone"));
        assertEquals("Value must be numeric.", errorFieldToMsgMap.get("ethnicity"));
        assertEquals("Must be positive integer", errorFieldToMsgMap.get("average_class_size"));
        assertEquals("No Error in transportation.Hence it should be saved to the database",1,responseList.size());
        assertEquals("No Error in transportation.Hence it should be saved to the database","transportation",responseList.get(0).getKey());
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
        _helper.saveEspFormData(user, school, keysForPage, keyToResponseMap, state, pageNum, errorFieldToMsgMap,
                responseList, false,false);
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
        _helper.saveEspFormData(user, school, keysForPage, keyToResponseMap, state, pageNum, errorFieldToMsgMap,
                responseList, true,false);
        verifyAllMocks();

        assertEquals("You must select a grade level.", errorFieldToMsgMap.get("grade_levels"));
    }

    public void testSaveEspFormDataWithExternalDataErrorsIgnoreErrors() {
        User user = new User();
        user.setId(2);
        School school = new School();
        Set<String> keysForPage = new HashSet<String>();
        // student_enrollment is external data
        keysForPage.add("student_enrollment");
        Map<String, Object[]> keyToResponseMap = new HashMap<String, Object[]>();
        keyToResponseMap.put("student_enrollment", new Object[]{"abc"});

        // grade_levels is external data
        keysForPage.add("grade_levels");
        keyToResponseMap.put("grade_levels", new String[]{"abc"});

        //No Error in school_fax.Hence it should be saved to the database
        keyToResponseMap.put("school_fax_area_code", new Object[]{"123"});
        keyToResponseMap.put("school_fax_office_code", new Object[]{"123"});
        keyToResponseMap.put("school_fax_last_four", new Object[]{"1234"});

        State state = State.CA;
        int pageNum = 1;
        Map<String, String> errorFieldToMsgMap = new HashMap<String, String>();

        List<EspResponse> responseList = new ArrayList<EspResponse>();

        expect(_noEditDao.isStateLocked(state)).andReturn(false);
        _espResponseDao.deactivateResponsesByKeys(school,keysForPage);
        _espResponseDao.saveResponses(school, responseList);

        replayAllMocks();
        _helper.saveEspFormData(user, school, keysForPage, keyToResponseMap, state, pageNum, errorFieldToMsgMap,
                responseList, false,true);
        verifyAllMocks();

        assertEquals("Must be an integer.", errorFieldToMsgMap.get("student_enrollment"));
        assertEquals("You must select a grade level.", errorFieldToMsgMap.get("grade_levels"));
        assertEquals("No Error in school_fax.Hence it should be saved to the database",1,responseList.size());
        assertEquals("No Error in school_fax.Hence it should be saved to the database","school_fax",responseList.get(0).getKey());
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
        _helper.saveESPResponses(school, keysForPage, responseList, false, user, pageNum, new Date());
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
        String key = _helper.getPageKeys(pageNum);
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
        _helper.saveESPResponses(school, keysForPage, responseList, true, user, pageNum, new Date());
        verifyAllMocks();
    }

}

