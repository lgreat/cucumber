package gs.web.school;

import gs.data.school.EspResponse;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.BaseControllerTestCase;
import gs.web.request.RequestAttributeHelper;
import org.springframework.ui.ModelMap;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 9/6/12
 * Time: 4:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class SchoolProfileEnrollmentControllerTest extends BaseControllerTestCase {
    private SchoolProfileEnrollmentController _schoolProfileEnrollmentController;
    private SchoolProfileDataHelper _schoolProfileDataHelper;
    private School _school;
    private State _state;

    private static final String VIEW = "school/profileEnrollment";
    private static final String APPLICATION_PROCESS = "application_process";
    private static final String ADMISSIONS_URL = "admissions_url";
    private static final String HAS_APPLICATION_PROCESS = "hasApplicationProcess";
    private static final String LEARN_MORE_URL = "learnMoreUrl";
    private static final String APPL_DEADLINE = "application_deadline";
    private static final String APPL_DEADLINE_DATE = "application_deadline_date";
    private static final String ENROLLMENT_STATE = "enrollmentState";
    private static final String DESTINATION_SCHOOL = "destination_school_";
    private static final String COLLEGE_DESTINATION = "college_destination_";
    private static final String COLLEGE_PREP = "college_prep";
    private static final String COLLEGE_PREP_OTHER = "college_prep_other";
    private static final String PG_PLANS = "post_graduation";
    private static final String STUDENTS_ACCEPTED = "students_accepted";
    private static final String APPLICATIONS_RECEIVED = "applications_received";
    private static final String FEEDER_SCHOOL = "feeder_school_";
    private static final String ACCEPTANCE_RATE = "acceptanceRate";
    private static final String ACCEPTANCE_RATE_YEAR = "acceptanceRateYear";
    private static final String TUITION_YEAR = "tuition_year";
    private static final String TUITION_LOW = "tuition_low";
    private static final String TUITION_HIGH = "tuition_high";
    private static final String STUDENTS_VOUCHERS = "students_vouchers";
    private static final String FINANCIAL_AID = "financial_aid";
    private static final String FINANCIAL_AID_TYPE = "financial_aid_type";
    private static final String APPLICATION_FEE = "application_fee";
    private static final String FEE_WAIVERS = "fee_waivers";
    private static final String OUTSMARTING_ARTICLE_ID = "outsmartingArticleId";

    public void setUp() throws Exception {
        super.setUp();

        _schoolProfileDataHelper = createMock(SchoolProfileDataHelper.class);
        _schoolProfileEnrollmentController = new SchoolProfileEnrollmentController();

        _schoolProfileEnrollmentController.setSchoolProfileDataHelper(_schoolProfileDataHelper);
        _schoolProfileEnrollmentController.setRequestAttributeHelper(new RequestAttributeHelper());

        StateManager stateManager = new StateManager();
        _state = stateManager.getState("WI");
        _school = new School();
        getRequest().setAttribute("school", _school);
    }

    public void replayAll() {
        super.replayMocks(_schoolProfileDataHelper);
    }

    public void verifyAll() {
        super.verifyMocks(_schoolProfileDataHelper);
    }

    public void resetAll() {
        super.resetMocks(_schoolProfileDataHelper);
    }

    public void testNonEspPage() {
        Map<String, Object> model = _schoolProfileEnrollmentController.getApplInfoNonEspTile(_request, _school);
        assertEquals(model.get(LEARN_MORE_URL), "");

        String website = "www.qwerty.org";
        _school.setWebSite(website);
        model = _schoolProfileEnrollmentController.getApplInfoNonEspTile(_request, _school);
        assertEquals(model.get(LEARN_MORE_URL), website);

        Map<String, List<EspResponse>> espData = null;
        ModelMap modelMap = handleEnrollment(espData);
        assertFalse((Boolean) modelMap.get("hasEsp"));

        List<EspResponse> espResponses = new ArrayList<EspResponse>();
        espData = convertToEspData(espResponses);
        modelMap = handleEnrollment(espData);
        assertFalse((Boolean) modelMap.get("hasEsp"));
    }

    public void testApplicationInfoTile() {
        Calendar day = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        List<EspResponse> espResponses = new ArrayList<EspResponse>();
        espResponses.add(createEspResponse(APPLICATION_PROCESS, "no"));
        Map<String, List<EspResponse>> espData = convertToEspData(espResponses);
        Map<String, Object> model = _schoolProfileEnrollmentController.getApplInfoEspTile(_request, _school, espData);
        assertFalse((Boolean) model.get(HAS_APPLICATION_PROCESS));

        espResponses = new ArrayList<EspResponse>();
        espResponses.add(createEspResponse(APPLICATION_PROCESS, "yes"));
        espResponses.add(createEspResponse(ADMISSIONS_URL, "www.querty.org"));
        espResponses.add(createEspResponse(APPL_DEADLINE, "DAte"));
        day.add(Calendar.MONTH, 2);
        espResponses.add(createEspResponse(APPL_DEADLINE_DATE, dateFormat.format(day.getTime())));
        espData = convertToEspData(espResponses);
        model = _schoolProfileEnrollmentController.getApplInfoEspTile(_request, _school, espData);
        assertTrue((Boolean) model.get(HAS_APPLICATION_PROCESS));
        assertEquals("http://www.querty.org", model.get(LEARN_MORE_URL));
        assertEquals(1, model.get(ENROLLMENT_STATE));

        espResponses = new ArrayList<EspResponse>();
        espData = convertToEspData(espResponses);
        model = _schoolProfileEnrollmentController.getApplInfoEspTile(_request, _school, espData);
        assertTrue((Boolean) model.get(HAS_APPLICATION_PROCESS));
        assertEquals(3, model.get(ENROLLMENT_STATE));

        espResponses = new ArrayList<EspResponse>();
        espResponses.add(createEspResponse(APPL_DEADLINE, "YearRound"));
        espData = convertToEspData(espResponses);
        model = _schoolProfileEnrollmentController.getApplInfoEspTile(_request, _school, espData);
        assertTrue((Boolean) model.get(HAS_APPLICATION_PROCESS));
        assertEquals(4, model.get(ENROLLMENT_STATE));

        espResponses = new ArrayList<EspResponse>();
        espResponses.add(createEspResponse(APPLICATION_PROCESS, "yes"));
        espResponses.add(createEspResponse(APPL_DEADLINE, "parents_contacts"));
        espData = convertToEspData(espResponses);
        model = _schoolProfileEnrollmentController.getApplInfoEspTile(_request, _school, espData);
        assertTrue((Boolean) model.get(HAS_APPLICATION_PROCESS));
        assertEquals(3, model.get(ENROLLMENT_STATE));

        espResponses = new ArrayList<EspResponse>();
        espResponses.add(createEspResponse(APPLICATION_PROCESS, "Yes"));
        espResponses.add(createEspResponse(APPL_DEADLINE, "date"));
        espResponses.add(createEspResponse(APPL_DEADLINE_DATE, "asdfg"));
        espData = convertToEspData(espResponses);
        model = _schoolProfileEnrollmentController.getApplInfoEspTile(_request, _school, espData);
        assertTrue((Boolean) model.get(HAS_APPLICATION_PROCESS));
        assertEquals(3, model.get(ENROLLMENT_STATE));

        espResponses = new ArrayList<EspResponse>();
        espResponses.add(createEspResponse(ADMISSIONS_URL, "www.querty.org"));
        espResponses.add(createEspResponse(APPL_DEADLINE, "date"));
        espResponses.add(createEspResponse(APPL_DEADLINE_DATE, "11/22/2011"));
        espData = convertToEspData(espResponses);
        model = _schoolProfileEnrollmentController.getApplInfoEspTile(_request, _school, espData);
        assertTrue((Boolean) model.get(HAS_APPLICATION_PROCESS));
        assertEquals("http://www.querty.org", model.get(LEARN_MORE_URL));
        assertEquals(1, model.get(ENROLLMENT_STATE));

        espResponses = new ArrayList<EspResponse>();
        espResponses.add(createEspResponse(APPL_DEADLINE, "date"));
        day = Calendar.getInstance();
        day.add(Calendar.MONTH, -1);
        espResponses.add(createEspResponse(APPL_DEADLINE_DATE, dateFormat.format(day.getTime())));
        espData = convertToEspData(espResponses);
        model = _schoolProfileEnrollmentController.getApplInfoEspTile(_request, _school, espData);
        assertTrue((Boolean) model.get(HAS_APPLICATION_PROCESS));
        assertEquals(2, model.get(ENROLLMENT_STATE));

        espResponses = new ArrayList<EspResponse>();
        espResponses.add(createEspResponse(APPL_DEADLINE, "date"));
        day = Calendar.getInstance();
        day.add(Calendar.MONTH, -4);
        espResponses.add(createEspResponse(APPL_DEADLINE_DATE, dateFormat.format(day.getTime())));
        espData = convertToEspData(espResponses);
        model = _schoolProfileEnrollmentController.getApplInfoEspTile(_request, _school, espData);
        assertTrue((Boolean) model.get(HAS_APPLICATION_PROCESS));
        assertEquals(1, model.get(ENROLLMENT_STATE));
    }

    public void testPlanningAheadTile() {
        List<EspResponse> espResponses = new ArrayList<EspResponse>();
        for(int i = 1; i < 4; i++) {
            espResponses.add(createEspResponse(DESTINATION_SCHOOL + i, "destSchool" + i));
        }
        for(int i = 1; i < 4; i++) {
            espResponses.add(createEspResponse(COLLEGE_DESTINATION + i, "destCollege" + i));
        }
        espResponses.add(createEspResponse(COLLEGE_PREP, "SAT/ACT; Summer college"));
        espResponses.add(createEspResponse(COLLEGE_PREP_OTHER, "assdgh"));
        espResponses.add(createEspResponse(PG_PLANS + "_year", "2012"));
        espResponses.add(createEspResponse(PG_PLANS + "_4yr", "50"));
        espResponses.add(createEspResponse(PG_PLANS + "_workforce", "50"));
        Map<String, List<EspResponse>> espData = convertToEspData(espResponses);
        List<Map<String, Object>> rows = _schoolProfileEnrollmentController.getPlanningAheadEspTile(_request, _school, espData);
        assertEquals(rows.size(), 3);

        espResponses = new ArrayList<EspResponse>();
        for(int i = 1; i < 4; i++) {
            espResponses.add(createEspResponse(COLLEGE_DESTINATION + i, "destCollege" + i));
        }
        espResponses.add(createEspResponse(PG_PLANS + "_2yr", "50"));
        espResponses.add(createEspResponse(PG_PLANS + "_4yr", "50"));
        espData = convertToEspData(espResponses);
        rows = _schoolProfileEnrollmentController.getPlanningAheadEspTile(_request, _school, espData);
        assertEquals(rows.size(), 1);

        espResponses = new ArrayList<EspResponse>();
        espData = convertToEspData(espResponses);
        rows = _schoolProfileEnrollmentController.getPlanningAheadEspTile(_request, _school, espData);
        assertEquals(rows.size(), 0);
    }

    public void testChancesEspTile() {
        List<EspResponse> espResponses = new ArrayList<EspResponse>();
        espResponses.add(createEspResponse(STUDENTS_ACCEPTED + "_year", "2011-2012"));
        espResponses.add(createEspResponse(STUDENTS_ACCEPTED, "207"));
        espResponses.add(createEspResponse(APPLICATIONS_RECEIVED + "_year", "2011-2012"));
        espResponses.add(createEspResponse(APPLICATIONS_RECEIVED, "243"));
        for(int i = 0; i < 4; i++) {
            espResponses.add(createEspResponse(FEEDER_SCHOOL + i, "feederSchool" + i));
        }
        Map<String, List<EspResponse>> espData = convertToEspData(espResponses);
        ModelMap model = new ModelMap();
        List<Map<String, Object>> rows = _schoolProfileEnrollmentController.getChancesEspTile(model, _request, _school, espData);
        assertEquals(rows.size(), 3);
        assertEquals(model.get(ACCEPTANCE_RATE), 9);
        assertEquals(model.get(ACCEPTANCE_RATE_YEAR), "2011-2012");

        espResponses = new ArrayList<EspResponse>();
        espResponses.add(createEspResponse(STUDENTS_ACCEPTED + "_year", "2011-2012"));
        espResponses.add(createEspResponse(STUDENTS_ACCEPTED, "207"));
        espResponses.add(createEspResponse(APPLICATIONS_RECEIVED + "_year", "2012-2013"));
        espResponses.add(createEspResponse(APPLICATIONS_RECEIVED, "243"));
        espData = convertToEspData(espResponses);
        model = new ModelMap();
        rows = _schoolProfileEnrollmentController.getChancesEspTile(model, _request, _school, espData);
        assertEquals(rows.size(), 2);
        assertNull(model.get(ACCEPTANCE_RATE));
        assertNull(model.get(ACCEPTANCE_RATE_YEAR));

        espResponses = new ArrayList<EspResponse>();
        espResponses.add(createEspResponse(APPLICATIONS_RECEIVED + "_year", "2012-2013"));
        espResponses.add(createEspResponse(APPLICATIONS_RECEIVED, "243"));
        for(int i = 0; i < 4; i++) {
            espResponses.add(createEspResponse(FEEDER_SCHOOL + i, "feederSchool" + i));
        }
        espData = convertToEspData(espResponses);
        model = new ModelMap();
        rows = _schoolProfileEnrollmentController.getChancesEspTile(model, _request, _school, espData);
        assertEquals(rows.size(), 2);
        assertNull(model.get(ACCEPTANCE_RATE));
        assertNull(model.get(ACCEPTANCE_RATE_YEAR));

        espResponses = new ArrayList<EspResponse>();
        espData = convertToEspData(espResponses);
        rows = _schoolProfileEnrollmentController.getChancesEspTile(model, _request, _school, espData);
        assertEquals(rows.size(), 0);
    }

    public void testCostEspTile() {
        List<EspResponse> espResponses = new ArrayList<EspResponse>();
        espResponses.add(createEspResponse(TUITION_YEAR, "2011-2012"));
        espResponses.add(createEspResponse(TUITION_LOW, "10000"));
        espResponses.add(createEspResponse(TUITION_HIGH, "15000"));
        espResponses.add(createEspResponse(STUDENTS_VOUCHERS, "yes"));
        espResponses.add(createEspResponse(FINANCIAL_AID, "yes"));
        espResponses.add(createEspResponse(FINANCIAL_AID_TYPE, "tax credits"));
        espResponses.add(createEspResponse(FINANCIAL_AID_TYPE + "_other", "assistantship"));
        espResponses.add(createEspResponse(APPLICATION_FEE, "yes"));
        espResponses.add(createEspResponse(APPLICATION_FEE + "_amount", "100"));
        espResponses.add(createEspResponse(FEE_WAIVERS, "no"));
        Map<String, List<EspResponse>> espData = convertToEspData(espResponses);
        List<Map<String, Object>> rows = _schoolProfileEnrollmentController.getCostEspTile(_request, _school, espData);
        assertEquals(rows.size(), 4);

        espResponses = new ArrayList<EspResponse>();
        espResponses.add(createEspResponse(STUDENTS_VOUCHERS, "no"));
        espResponses.add(createEspResponse(FINANCIAL_AID, "no"));
        espResponses.add(createEspResponse(APPLICATION_FEE, "no"));
        espData = convertToEspData(espResponses);
        rows = _schoolProfileEnrollmentController.getCostEspTile(_request, _school, espData);
        assertEquals(rows.size(), 3);

        espResponses = new ArrayList<EspResponse>();
        espData = convertToEspData(espResponses);
        rows = _schoolProfileEnrollmentController.getCostEspTile(_request, _school, espData);
        assertEquals(rows.size(), 0);
    }

    public void testGetOutsmartingArticle() {
        _school.setStateAbbreviation(State.IN);
        _school.setCounty("marion");
        Map<String, Object> model = _schoolProfileEnrollmentController.getOutsmartingArticle(_school);
        assertEquals(6996, model.get(OUTSMARTING_ARTICLE_ID));

        _school.setStateAbbreviation(State.WI);
        _school.setCity("milwaukee");
        model = _schoolProfileEnrollmentController.getOutsmartingArticle(_school);
        assertEquals(3428, model.get(OUTSMARTING_ARTICLE_ID));

        _school.setStateAbbreviation(State.IN);
        _school.setCounty("hamilton");
        model = _schoolProfileEnrollmentController.getOutsmartingArticle(_school);
        assertNull(model.get(OUTSMARTING_ARTICLE_ID));
    }

    private ModelMap handleEnrollment(Map<String, List<EspResponse>> espData) {
        ModelMap modelMap = new ModelMap();
        resetAll();
        expect(_schoolProfileDataHelper.getEspDataForSchool(_request)).andReturn(espData);

        replayAll();
        String view = _schoolProfileEnrollmentController.handle(modelMap, _request);
        verifyAll();
        assertEquals(VIEW, view);
        return modelMap;
    }

    private ModelMap runController( Map<String, List<EspResponse>> espData) {
        ModelMap map = new ModelMap();
        expect(_schoolProfileDataHelper.getEspDataForSchool(getRequest())).andReturn(espData);

        return map;
    }

    private EspResponse createEspResponse( String key, String value ) {
        EspResponse response = new EspResponse();
        response.setActive( true );
        response.setKey( key );
        response.setValue( value );
        if(value != null && value.length() > 0) {
            response.setPrettyValue( createPrettyValue(value) );
        }
        return response;
    }

    private Map<String,List<EspResponse>> convertToEspData(List<EspResponse> l) {
        return EspResponse.rollup(l);
    }

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
}
