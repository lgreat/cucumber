package gs.web.school;

import gs.data.community.User;
import gs.data.school.*;
import gs.data.state.INoEditDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;

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

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _controller = new EspFormController();

        _espFormExternalDataHelper = new EspFormExternalDataHelper();
        _espFormValidationHelper = new EspFormValidationHelper();
        _noEditDao = createMock(INoEditDao.class);
        _espMembershipDao = createMock(IEspMembershipDao.class);
        _espResponseDao = createMock(IEspResponseDao.class);

        _controller.setEspFormExternalDataHelper(_espFormExternalDataHelper);
        _controller.setEspFormValidationHelper(_espFormValidationHelper);
        _controller.setNoEditDao(_noEditDao);
        _espFormValidationHelper.setEspMembershipDao(_espMembershipDao);
        _controller.setEspResponseDao(_espResponseDao);
    }

    private void replayAllMocks() {
        replayMocks(_noEditDao, _espMembershipDao, _espResponseDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_noEditDao, _espMembershipDao, _espResponseDao);
    }

    public void testSaveEspFormDataBasicUserApproved() {
        User user = new User();
        user.setId(2);
        School school = new School();
        Set<String> keysForPage = new HashSet<String>();
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
        Map<String, Object[]> keyToResponseMap = new HashMap<String, Object[]>();
        State state = State.CA;
        int pageNum = 1;
        Map<String, String> errorFieldToMsgMap = new HashMap<String, String>();

        expect(_noEditDao.isStateLocked(state)).andReturn(false);
        Set<String> keysToDelete = new HashSet<String>();
        keysToDelete.addAll(keysForPage);
        String key = _controller.getKeyForPageKeys(pageNum);
        keysToDelete.add(key);
        _espResponseDao.deleteResponsesForSchoolByUserAndByKeys(school, user.getId(), keysToDelete);
        List<EspResponse> responseList = new ArrayList<EspResponse>();

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
        String key = _controller.getKeyForPageKeys(pageNum);
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

}