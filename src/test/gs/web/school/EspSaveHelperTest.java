package gs.web.school;

import gs.data.community.User;
import gs.data.school.*;
import gs.data.security.Role;
import gs.data.state.INoEditDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.school.usp.EspResponseData;
import gs.web.school.usp.EspStatusManager;
import gs.web.school.usp.UspFormHelper;
import org.easymock.classextension.EasyMock;
import org.springframework.beans.factory.BeanFactory;

import static org.easymock.EasyMock.eq;

import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.easymock.EasyMock.*;


public class EspSaveHelperTest extends BaseControllerTestCase {
    private User _user;
    private School _school;
    private State _state;
    private Map<String, Object[]> _responseKeyValues;
    private Set<String> _formFieldNames = UspFormHelper.FORM_FIELD_TITLES.keySet();
    private Set<String> _keysForOspForm = new HashSet<String>() {{
        for (UspFormHelper.SectionResponseKeys sectionResponseKeys : UspFormHelper.SectionResponseKeys.values()) {
            String[] responseKeys = sectionResponseKeys.getResponseKeys();
            if (responseKeys != null) {
                for (String responseKey : responseKeys) {
                    add(responseKey);
                }
            }
        }
        // one more key for the page itself to get the keys for page
        add("_page_osp_gateway_keys");
    }};

    private EspSaveHelper _helper;
    private IEspResponseDao _espResponseDao;
    private INoEditDao _noEditDao;
    private ISchoolDao _schoolDao;
    private EspFormExternalDataHelper _espFormExternalDataHelper;
    private EspFormValidationHelper _espFormValidationHelper;
    private EspFormValidationHelper _espFormValidationHelperMock;
    private BeanFactory _beanFactory;
    EspStatusManager _espStatusManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _helper = new EspSaveHelper();
        _espFormExternalDataHelper = new EspFormExternalDataHelper();
        _espFormValidationHelper = new EspFormValidationHelper();

        _espFormValidationHelperMock = EasyMock.createStrictMock(EspFormValidationHelper.class);
        _espResponseDao = createMock(IEspResponseDao.class);
        _noEditDao = createMock(INoEditDao.class);
        _schoolDao = createMock(ISchoolDao.class);
        _beanFactory = org.easymock.classextension.EasyMock.createStrictMock(BeanFactory.class);
        _espStatusManager = org.easymock.classextension.EasyMock.createStrictMock(EspStatusManager.class);

        _helper.setNoEditDao(_noEditDao);
        _helper.setEspResponseDao(_espResponseDao);
        _helper.setEspFormExternalDataHelper(_espFormExternalDataHelper);
        _helper.setEspFormValidationHelper(_espFormValidationHelper);
        _helper.setBeanFactory(_beanFactory);

        _espFormExternalDataHelper.setSchoolDao(_schoolDao);
    }

    private void resetAllMocks() {
        resetMocks(_noEditDao, _espResponseDao, _beanFactory, _espStatusManager);
    }

    private void replayAllMocks() {
        replayMocks(_noEditDao, _espResponseDao, _beanFactory, _espStatusManager);
    }

    private void verifyAllMocks() {
        verifyMocks(_noEditDao, _espResponseDao, _beanFactory, _espStatusManager);
    }

    public void testSaveOspFormDataBasic_ApprovedUser() {
        //Test with empty keys and responses.
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
        Set<EspResponseSource> responseSources = new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.osp,
                EspResponseSource.datateam));
        expect(_espResponseDao.getResponses(school, responseSources)).andReturn(new ArrayList<EspResponse>());

        replayAllMocks();
        EspSaveBehaviour saveBehaviour = new EspSaveBehaviour(false, false, true);
        _helper.saveOspFormData(user, school, state, pageNum, keysForPage, keyToResponseMap, responseList, errorFieldToMsgMap, saveBehaviour);
        verifyAllMocks();

        assertEquals(true, errorFieldToMsgMap.isEmpty());
    }

    public void testSaveOspFormDataBasic_ProvisionalUser() {
        //Test with empty keys and responses.
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
        String key = _helper.getPageKeys(pageNum);
        keysToDelete.add(key);
        List<EspResponse> responseList = new ArrayList<EspResponse>();

        replayAllMocks();
        EspSaveBehaviour saveBehaviour = new EspSaveBehaviour(true, false, false);
        _helper.saveOspFormData(user, school, state, pageNum, keysForPage, keyToResponseMap, responseList, errorFieldToMsgMap, saveBehaviour);
        verifyAllMocks();

        assertEquals(true, errorFieldToMsgMap.isEmpty());
    }

    public void testSaveOspFormDataBasic_ActivateProvisionalData() {
        //Test with empty keys and responses.
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
        String key = _helper.getPageKeys(pageNum);
        keysToDelete.add(key);
        List<EspResponse> responseList = new ArrayList<EspResponse>();

        replayAllMocks();
        EspSaveBehaviour saveBehaviour = new EspSaveBehaviour(false, true, false);
        _helper.saveOspFormData(user, school, state, pageNum, keysForPage, keyToResponseMap, responseList, errorFieldToMsgMap, saveBehaviour);
        verifyAllMocks();

        assertEquals(true, errorFieldToMsgMap.isEmpty());
    }

    public void testSaveOspFormDataWithValidationErrors_ApprovedUser() {
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
        EspSaveBehaviour saveBehaviour = new EspSaveBehaviour(false, false, true);
        _helper.saveOspFormData(user, school, state, pageNum, keysForPage, keyToResponseMap, responseList, errorFieldToMsgMap, saveBehaviour);
        verifyAllMocks();

        assertEquals("Phone number must be numeric", errorFieldToMsgMap.get("school_phone"));
        assertEquals("Value must be numeric.", errorFieldToMsgMap.get("ethnicity"));
        assertEquals("Must be positive integer", errorFieldToMsgMap.get("average_class_size"));
    }

    public void testSaveOspFormDataWithValidationErrors_ActivateProvisionalData() {
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

        Map<String, String> allKeysWithActiveResponses = new HashMap<String, String>();
        allKeysWithActiveResponses.put("transportation", "");

        Set<EspResponseSource> responseSourcesToDeactivate =
                new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.osp, EspResponseSource.datateam));

        Set<EspResponseSource> responseSourcesToDeactivateIfOspPreferred =
                new HashSet<EspResponseSource>();

        expect(_noEditDao.isStateLocked(state)).andReturn(false);

        expect(_beanFactory.getBean(eq(EspStatusManager.BEAN_NAME), isA(School.class))).andReturn(_espStatusManager);
        expect(_espStatusManager.allOSPQuestionsAnswered(allKeysWithActiveResponses)).andReturn(false);
        _espResponseDao.deactivateResponsesBySourceOrSourceKeys(school, responseSourcesToDeactivateIfOspPreferred, responseSourcesToDeactivate, keysForPage);
        _espResponseDao.deleteResponsesForSchoolByUserAndByKeys(school, user.getId(), new HashSet<String>(Arrays.asList("_page_osp_gateway_keys")));
        _espResponseDao.saveResponses(school, responseList);
        replayAllMocks();
        EspSaveBehaviour saveBehaviour = new EspSaveBehaviour(false, true, false);
        _helper.saveOspFormData(user, school, state, pageNum, keysForPage, keyToResponseMap, responseList, errorFieldToMsgMap, saveBehaviour);
        verifyAllMocks();

        assertEquals("Phone number must be numeric", errorFieldToMsgMap.get("school_phone"));
        assertEquals("Value must be numeric.", errorFieldToMsgMap.get("ethnicity"));
        assertEquals("Must be positive integer", errorFieldToMsgMap.get("average_class_size"));
        assertEquals("No Error in transportation.Hence it should be saved to the database", 1, responseList.size());
        assertEquals("No Error in transportation.Hence it should be saved to the database", "transportation", responseList.get(0).getKey());
    }

    public void testSaveOspFormDataWithExternalDataErrors_ApprovedUser() {
        User user = new User();
        user.setId(2);
        School school = new School();
        Set<String> keysForPage = new HashSet<String>();
        //student_enrollment is external data
        keysForPage.add("student_enrollment");
        Map<String, Object[]> keyToResponseMap = new HashMap<String, Object[]>();
        keyToResponseMap.put("student_enrollment", new Object[]{"abc"});

        State state = State.CA;
        int pageNum = 1;
        Map<String, String> errorFieldToMsgMap = new HashMap<String, String>();

        List<EspResponse> responseList = new ArrayList<EspResponse>();

        expect(_noEditDao.isStateLocked(state)).andReturn(false);

        replayAllMocks();
        EspSaveBehaviour saveBehaviour = new EspSaveBehaviour(false, false, true);
        _helper.saveOspFormData(user, school, state, pageNum, keysForPage, keyToResponseMap, responseList, errorFieldToMsgMap, saveBehaviour);
        verifyAllMocks();

        assertEquals("Must be an integer.", errorFieldToMsgMap.get("student_enrollment"));
    }

    public void testSaveOspFormDataWithExternalDataErrors_ProvisionalUser() {
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
        EspSaveBehaviour saveBehaviour = new EspSaveBehaviour(true, false, false);
        _helper.saveOspFormData(user, school, state, pageNum, keysForPage, keyToResponseMap, responseList, errorFieldToMsgMap, saveBehaviour);
        verifyAllMocks();

        assertEquals("You must select a grade level.", errorFieldToMsgMap.get("grade_levels"));
    }

    public void testSaveOspFormDataWithExternalDataErrors_ActivateProvisionalData() {
        User user = new User();
        user.setId(2);
        School school = new School();
        Set<String> keysForPage = new HashSet<String>();
        // student_enrollment is external data.It is not valid hence it should not be saved to the database.
        keysForPage.add("student_enrollment");
        Map<String, Object[]> keyToResponseMap = new HashMap<String, Object[]>();
        keyToResponseMap.put("student_enrollment", new Object[]{"abc"});

        // grade_levels is external data.It is not valid hence it should not be saved to the database.
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

        Map<String, String> allKeysWithActiveResponses = new HashMap<String, String>();
        allKeysWithActiveResponses.put("student_enrollment", "");
        allKeysWithActiveResponses.put("school_fax", "");
        allKeysWithActiveResponses.put("grade_levels", "");

        Set<EspResponseSource> responseSourcesToDeactivate =
                new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.osp, EspResponseSource.datateam));

        Set<EspResponseSource> responseSourcesToDeactivateIfOspPreferred =
                new HashSet<EspResponseSource>();

        expect(_noEditDao.isStateLocked(state)).andReturn(false);
        expect(_beanFactory.getBean(eq(EspStatusManager.BEAN_NAME), isA(School.class))).andReturn(_espStatusManager);
        expect(_espStatusManager.allOSPQuestionsAnswered(allKeysWithActiveResponses)).andReturn(false);
        _espResponseDao.deactivateResponsesBySourceOrSourceKeys(school, responseSourcesToDeactivateIfOspPreferred, responseSourcesToDeactivate, keysForPage);
        _espResponseDao.deleteResponsesForSchoolByUserAndByKeys(school, user.getId(), new HashSet<String>(Arrays.asList("_page_osp_gateway_keys")));
        _espResponseDao.saveResponses(school, responseList);

        replayAllMocks();
        EspSaveBehaviour saveBehaviour = new EspSaveBehaviour(false, true, false);
        _helper.saveOspFormData(user, school, state, pageNum, keysForPage, keyToResponseMap, responseList, errorFieldToMsgMap, saveBehaviour);
        verifyAllMocks();

        assertEquals("Must be an integer.", errorFieldToMsgMap.get("student_enrollment"));
        assertEquals("You must select a grade level.", errorFieldToMsgMap.get("grade_levels"));
        assertEquals("No Error in school_fax.Hence it should be saved to the database", 1, responseList.size());
        assertEquals("No Error in school_fax.Hence it should be saved to the database", "school_fax", responseList.get(0).getKey());
    }

    public void testSaveOSPResponsesTestBasic() {
        //Test nulls
        replayAllMocks();
        _helper.saveOspResponses(null, null, -1, null, null, null, null, null);
        verifyAllMocks();

        resetAllMocks();

        //Test empty
        User user = new User();
        user.setId(2);
        School school = new School();
        replayAllMocks();
        _helper.saveOspResponses(user, school, -1, new Date(), new HashSet<String>(),new HashMap<String, String>(), new ArrayList<EspResponse>(), null);
        verifyAllMocks();
    }

    public void testSaveOSPResponses_ApprovedUser_NonOSPPreferredStatus() {
        User user = new User();
        user.setId(2);
        School school = new School();
        Set<String> keysForPage = new HashSet<String>();
        keysForPage.add("grade_levels");
        int pageNum = 1;
        List<EspResponse> responseList = new ArrayList<EspResponse>();
        responseList.add(_helper.createEspResponse(user, school, new Date(), "grade_levels", false, "1", EspResponseSource.osp));
        Map<String, String> allKeysWithActiveResponses = new HashMap<String, String>();
        allKeysWithActiveResponses.put("grade_levels", "");
        Set<EspResponseSource> responseSourcesToDeactivate =
                new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.osp, EspResponseSource.datateam));
        Set<EspResponseSource> responseSourcesToDeactivateIfOspPreferred =
                new HashSet<EspResponseSource>();

        expect(_beanFactory.getBean(eq(EspStatusManager.BEAN_NAME), isA(School.class))).andReturn(_espStatusManager);
        //All questions are not answered , hence the school is not in an OSP preferred state.
        expect(_espStatusManager.allOSPQuestionsAnswered(allKeysWithActiveResponses)).andReturn(false);
        _espResponseDao.deactivateResponsesBySourceOrSourceKeys(school, responseSourcesToDeactivateIfOspPreferred, responseSourcesToDeactivate, keysForPage);
        _espResponseDao.saveResponses(school, responseList);

        replayAllMocks();
        EspSaveBehaviour saveBehaviour = new EspSaveBehaviour(false, false, true);
        _helper.saveOspResponses(user, school, pageNum, new Date(), keysForPage, allKeysWithActiveResponses, responseList, saveBehaviour);
        verifyAllMocks();
    }

    public void testSaveOSPResponses_ApprovedUser_OSPPreferredStatus() {
        User user = new User();
        user.setId(2);
        School school = new School();
        Set<String> keysForPage = new HashSet<String>();
        keysForPage.add("grade_levels");
        int pageNum = 1;
        List<EspResponse> responseList = new ArrayList<EspResponse>();
        responseList.add(_helper.createEspResponse(user, school, new Date(), "grade_levels", false, "1", EspResponseSource.osp));
        Map<String, String> allKeysWithActiveResponses = new HashMap<String, String>();
        allKeysWithActiveResponses.put("grade_levels", "");
        Set<EspResponseSource> responseSourcesToDeactivate =
                new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.osp, EspResponseSource.datateam));
        Set<EspResponseSource> responseSourcesToDeactivateIfOspPreferred =
                new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.usp));

        expect(_beanFactory.getBean(eq(EspStatusManager.BEAN_NAME), isA(School.class))).andReturn(_espStatusManager);
        //All questions are answered , hence the school is in an OSP preferred state.
        expect(_espStatusManager.allOSPQuestionsAnswered(allKeysWithActiveResponses)).andReturn(true);
        _espResponseDao.deactivateResponsesBySourceOrSourceKeys(school, responseSourcesToDeactivateIfOspPreferred, responseSourcesToDeactivate, keysForPage);
        _espResponseDao.saveResponses(school, responseList);

        replayAllMocks();
        EspSaveBehaviour saveBehaviour = new EspSaveBehaviour(false, false, true);
        _helper.saveOspResponses(user, school, pageNum, new Date(), keysForPage, allKeysWithActiveResponses, responseList, saveBehaviour);
        verifyAllMocks();
    }

    public void testSaveOSPResponses_ProvisionalUser() {
        User user = new User();
        user.setId(2);
        School school = new School();
        Set<String> keysForPage = new HashSet<String>();
        keysForPage.add("grade_levels");
        int pageNum = 1;
        List<EspResponse> responseList = new ArrayList<EspResponse>();
        responseList.add(_helper.createEspResponse(user, school, new Date(), "grade_levels", false, "1", EspResponseSource.osp));
        Map<String, String> allKeysWithActiveResponses = new HashMap<String, String>();
        allKeysWithActiveResponses.put("grade_levels", "");

        Set<String> keysToDelete = new HashSet<String>();
        keysToDelete.add(_helper.getPageKeys(pageNum));
        keysToDelete.add("grade_levels");
        _espResponseDao.deleteResponsesForSchoolByUserAndByKeys(school,user.getId(),keysToDelete);
        _espResponseDao.saveResponses(school, responseList);

        replayAllMocks();
        EspSaveBehaviour saveBehaviour = new EspSaveBehaviour(true, false, false);
        _helper.saveOspResponses(user, school, pageNum, new Date(), keysForPage, allKeysWithActiveResponses, responseList, saveBehaviour);
        verifyAllMocks();
    }

    public void testSaveOSPResponses_ActivateProvisionalData_NonOSPPreferredStatus() {
        User user = new User();
        user.setId(2);
        School school = new School();
        Set<String> keysForPage = new HashSet<String>();
        keysForPage.add("grade_levels");
        int pageNum = 1;
        List<EspResponse> responseList = new ArrayList<EspResponse>();
        responseList.add(_helper.createEspResponse(user, school, new Date(), "grade_levels", false, "1", EspResponseSource.osp));
        Map<String, String> allKeysWithActiveResponses = new HashMap<String, String>();
        allKeysWithActiveResponses.put("grade_levels", "");
        Set<EspResponseSource> responseSourcesToDeactivate =
                new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.osp, EspResponseSource.datateam));
        Set<EspResponseSource> responseSourcesToDeactivateIfOspPreferred =
                new HashSet<EspResponseSource>();

        expect(_beanFactory.getBean(eq(EspStatusManager.BEAN_NAME), isA(School.class))).andReturn(_espStatusManager);
        //All questions are not answered , hence the school is not in an OSP preferred state.
        expect(_espStatusManager.allOSPQuestionsAnswered(allKeysWithActiveResponses)).andReturn(false);
        _espResponseDao.deactivateResponsesBySourceOrSourceKeys(school, responseSourcesToDeactivateIfOspPreferred, responseSourcesToDeactivate, keysForPage);
        _espResponseDao.deleteResponsesForSchoolByUserAndByKeys(school, user.getId(), new HashSet<String>(Arrays.asList("_page_osp_gateway_keys")));
        _espResponseDao.saveResponses(school, responseList);

        replayAllMocks();
        EspSaveBehaviour saveBehaviour = new EspSaveBehaviour(false, true, false);
        _helper.saveOspResponses(user, school, pageNum, new Date(), keysForPage, allKeysWithActiveResponses, responseList, saveBehaviour);
        verifyAllMocks();
    }

    public void testSaveOSPResponses_ActivateProvisionalData_OSPPreferredStatus() {
        User user = new User();
        user.setId(2);
        School school = new School();
        Set<String> keysForPage = new HashSet<String>();
        keysForPage.add("grade_levels");
        int pageNum = 1;
        List<EspResponse> responseList = new ArrayList<EspResponse>();
        responseList.add(_helper.createEspResponse(user, school, new Date(), "grade_levels", false, "1", EspResponseSource.osp));
        Map<String, String> allKeysWithActiveResponses = new HashMap<String, String>();
        allKeysWithActiveResponses.put("grade_levels", "");
        Set<EspResponseSource> responseSourcesToDeactivate =
                new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.osp, EspResponseSource.datateam));
        Set<EspResponseSource> responseSourcesToDeactivateIfOspPreferred =
                new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.usp));

        expect(_beanFactory.getBean(eq(EspStatusManager.BEAN_NAME), isA(School.class))).andReturn(_espStatusManager);
        //All questions are not answered , hence the school is not in an OSP preferred state.
        expect(_espStatusManager.allOSPQuestionsAnswered(allKeysWithActiveResponses)).andReturn(true);
        _espResponseDao.deactivateResponsesBySourceOrSourceKeys(school, responseSourcesToDeactivateIfOspPreferred, responseSourcesToDeactivate, keysForPage);
        _espResponseDao.deleteResponsesForSchoolByUserAndByKeys(school, user.getId(), new HashSet<String>(Arrays.asList("_page_osp_gateway_keys")));
        _espResponseDao.saveResponses(school, responseList);

        replayAllMocks();
        EspSaveBehaviour saveBehaviour = new EspSaveBehaviour(false, true, false);
        _helper.saveOspResponses(user, school, pageNum, new Date(), keysForPage, allKeysWithActiveResponses, responseList, saveBehaviour);
        verifyAllMocks();
    }


//    public void testSaveESPResponsesProvisionalUser() {
//        User user = new User();
//        user.setId(2);
//        int pageNum = 1;
//        School school = new School();
//        Set<String> keysForPage = new HashSet<String>();
//        keysForPage.add("instructional_model");
//        Set<String> keysToDelete = new HashSet<String>();
//        keysToDelete.addAll(keysForPage);
//        String key = _helper.getPageKeys(pageNum);
//        keysToDelete.add(key);
//        keysToDelete.add(key);
//
//        List<EspResponse> responseList = new ArrayList<EspResponse>();
//        EspResponse response = new EspResponse();
//        response.setKey("instructional_model");
//        response.setValue("none");
//        responseList.add(response);
//        _espResponseDao.deleteResponsesForSchoolByUserAndByKeys(school, user.getId(), keysToDelete);
//        _espResponseDao.saveResponses(school, responseList);
//        replayAllMocks();
//        _helper.saveESPResponses(school, keysForPage, responseList, true, user, pageNum, new Date());
//        verifyAllMocks();
//    }

//    public void testSaveUspFormDataForUspUser() throws NoSuchAlgorithmException {
//        List<EspResponseSource> espResponses = new ArrayList<EspResponseSource>(){{
//            add(EspResponseSource.usp);
//        }};
//        _helper.setEspFormValidationHelper(_espFormValidationHelperMock);
//        /**
//         * provisional user expectations:
//         * - is not osp provisional
//         * - save responses (in inactive state)
//         */
//        resetAllMocks();
//        reset(_schoolDao);
//        EasyMock.reset(_espFormValidationHelperMock);
//        uspFormDataSetters();
//        _user.setEmailProvisional("ahdld");
//
//        expect(_espFormValidationHelperMock.isUserProvisional(_user)).andReturn(false);
//        _espResponseDao.saveResponses(isA(School.class), isA(ArrayList.class));
//        expectLastCall();
//
//        replayAllMocks();
//        replay(_schoolDao);
//        EasyMock.replay(_espFormValidationHelperMock);
//        _helper.saveUspFormData(_user, _school, _responseKeyValues, _formFieldNames);
//        verifyAllMocks();
//        verify(_schoolDao);
//        EasyMock.verify(_espFormValidationHelperMock);
//
//        /*
//         * email verified usp user expectations:
//         * - is not osp provisional
//         * - deactivate active usp responses of that user for the school
//         * - save new responses
//         */
//        resetAllMocks();
//        reset(_schoolDao);
//        EasyMock.reset(_espFormValidationHelperMock);
//        uspFormDataSetters();
//
//        expect(_espFormValidationHelperMock.isUserProvisional(_user)).andReturn(false);
//        _espResponseDao.deactivateResponsesByUserSourceKeys(_school, _user.getId(), espResponses,null);
//        expectLastCall();
//        _espResponseDao.saveResponses(isA(School.class), isA(ArrayList.class));
//        expectLastCall();
//
//        replayAllMocks();
//        replay(_schoolDao);
//        EasyMock.replay(_espFormValidationHelperMock);
//        _helper.saveUspFormData(_user, _school, _responseKeyValues, _formFieldNames);
//        verifyAllMocks();
//        verify(_schoolDao);
//        EasyMock.verify(_espFormValidationHelperMock);
//    }

//    public void testSaveUspFormDataForOspUser() throws NoSuchAlgorithmException {
//        List<EspResponseSource> espResponses = new ArrayList<EspResponseSource>(){{
//            add(EspResponseSource.osp);
//            add(EspResponseSource.datateam);
//        }};
//        Role role = new Role();
//        role.setKey(Role.ESP_MEMBER);
//        Set<Role> roles = new HashSet<Role>();
//        roles.add(role);
//        _helper.setEspFormValidationHelper(_espFormValidationHelperMock);
//        /**
//         * osp members expectations:
//         * - has role set, so no need to validate osp user state (no expectation set)
//         * - deactivate active responses for school from osp and datateam sources
//         * - save new responses
//         */
//        resetAllMocks();
//        reset(_schoolDao);
//        EasyMock.reset(_espFormValidationHelperMock);
//        uspFormDataSetters();
//        _user.setRoles(roles);
//
//        _espResponseDao.deactivateResponsesByUserSourceKeys(_school, null, espResponses,null);
//        expectLastCall();
//        _espResponseDao.saveResponses(isA(School.class), isA(ArrayList.class));
//        expectLastCall();
//
//        replayAllMocks();
//        replay(_schoolDao);
//        EasyMock.replay(_espFormValidationHelperMock);
//        _helper.saveUspFormData(_user, _school, _responseKeyValues, _formFieldNames);
//        verifyAllMocks();
//        verify(_schoolDao);
//        EasyMock.verify(_espFormValidationHelperMock);
//
//        /**
//         * osp provisional expectations:
//         * - user is osp provisional
//         * - delete all previous osp form page responses for the school saved previously by this user
//         * - save responses
//         */
//        resetAllMocks();
//        reset(_schoolDao);
//        EasyMock.reset(_espFormValidationHelperMock);
//        uspFormDataSetters();
//
//        expect(_espFormValidationHelperMock.isUserProvisional(_user)).andReturn(true);
//        _espResponseDao.deleteResponsesForSchoolByUserAndByKeys(_school, _user.getId(), _keysForOspForm);
//        expectLastCall();
//        _espResponseDao.saveResponses(isA(School.class), isA(ArrayList.class));
//        expectLastCall();
//
//        replayAllMocks();
//        replay(_schoolDao);
//        EasyMock.replay(_espFormValidationHelperMock);
//        _helper.saveUspFormData(_user, _school, _responseKeyValues, _formFieldNames);
//        verifyAllMocks();
//        verify(_schoolDao);
//        EasyMock.verify(_espFormValidationHelperMock);
//    }

    private void uspFormDataSetters() throws NoSuchAlgorithmException {
        _user = new User();
        _user.setId(1);
        _user.setPlaintextPassword("ahdld");

        _state = State.CA;

        _school = new School();
        _school.setId(1);
        _school.setDatabaseState(_state);

        _responseKeyValues = new HashMap<String, Object[]>() {{
            put(UspFormHelper.ARTS_MUSIC_PARAM, new Object[]{UspFormHelper.ARTS_VISUAL_RESPONSE_KEY + "__" + UspFormHelper.ARTS_VISUAL_PHOTO_RESPONSE_VALUE,
                    UspFormHelper.ARTS_MEDIA_RESPONSE_KEY + "__" + UspFormHelper.ARTS_MEDIA_GRAPHICS_RESPONSE_VALUE});
            put(UspFormHelper.FACILITIES_PARAM, new Object[]{UspFormHelper.FACILITIES_RESPONSE_KEY, UspFormHelper.FACILITIES_ARTS_RESPONSE_VALUE});
            put("asdd", new Object[]{"", ""});
        }};
    }
}

