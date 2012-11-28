package gs.web.school;

import gs.data.school.*;
import gs.data.state.State;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.easymock.EasyMock.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = {"classpath:gs/data/dao/hibernate/applicationContext-hibernate.xml", "classpath:gs/data/applicationContext-data.xml", "classpath:applicationContext.xml", "classpath:annotated-tests.xml", "classpath:pages-servlet.xml"})
public class PrintYourOwnChooserControllerTest {

    @Autowired
    PrintYourOwnChooserController _pdfController;

    IEspResponseDao _espResponseDaoHibernate;

    ISchoolDao _schoolDaoHibernate;

    public School getASchool() {
        School s = new School();
        s.setId(1000000);
        s.setDatabaseState(State.CA);
        s.setName("Test school");
        return s;
    }

    public List<EspResponse> getAnEspResponse(String key, String value) {
        List<EspResponse> espResponses = new ArrayList<EspResponse>();
        EspResponse response = new EspResponse();
        response.setKey(key);
        response.setValue(value);
        response.setPrettyValue(value);
        espResponses.add(response);
        return espResponses;
    }

    @Before
    public void setUp() {

        _schoolDaoHibernate = createStrictMock(ISchoolDao.class);

        _espResponseDaoHibernate = createStrictMock(IEspResponseDao.class);

        ReflectionTestUtils.setField(_pdfController, "_schoolDaoHibernate", _schoolDaoHibernate);
        ReflectionTestUtils.setField(_pdfController, "_espResponseDao", _espResponseDaoHibernate);
    }


    @Test
    public void testGetSchoolsFromParams() throws Exception {

        String states = "CA,DC,AK";
        String ids = "1,2,3";

        expect(_schoolDaoHibernate.getSchoolById(eq(State.CA), eq(1))).andReturn(new School());
        expect(_schoolDaoHibernate.getSchoolById(eq(State.DC), eq(2))).andReturn(new School());
        expect(_schoolDaoHibernate.getSchoolById(eq(State.AK), eq(3))).andReturn(new School());

        replay(_schoolDaoHibernate);

        List<School> schools = _pdfController.getSchoolsFromParams(states, ids);
        assertEquals("Expect number of schools returned to equal to number of IDs provided", 3, schools.size());

        verify(_schoolDaoHibernate);
    }

    @Test
    public void testGetSchoolsFromParams_onlyOneState() throws Exception {

        String states = "CA";
        String ids = "1,2,3";

        expect(_schoolDaoHibernate.getSchoolById(eq(State.CA), eq(1))).andReturn(new School());
        expect(_schoolDaoHibernate.getSchoolById(eq(State.CA), eq(2))).andReturn(new School());
        expect(_schoolDaoHibernate.getSchoolById(eq(State.CA), eq(3))).andReturn(new School());

        replay(_schoolDaoHibernate);

        List<School> schools = _pdfController.getSchoolsFromParams(states, ids);
        assertEquals("Expect number of schools returned to equal to number of IDs provided", 3, schools.size());

        verify(_schoolDaoHibernate);
    }

    @Test
    public void testException_nullInputs() throws Exception {

        String states = null;
        String ids = "1,2,3";

        try {
            List<School> schools = _pdfController.getSchoolsFromParams(states, ids);
            fail("Expect exception when multiple states were provided, but number of states != number of IDs");
        } catch (IllegalArgumentException e) {
            // pass
        }
    }

    @Test
    public void testException_emptyInputs() throws Exception {

        String states = "";
        String ids = "";

        try {
            List<School> schools = _pdfController.getSchoolsFromParams(states, ids);
            fail("Expect exception when multiple states were provided, but number of states != number of IDs");
        } catch (IllegalArgumentException e) {
            // pass
        }
    }

    @Test
    public void testException_stateAndIDsInequal_moreThanOneState() throws Exception {

        String states = "CA,AK";
        String ids = "1,2,3";

        try {
            List<School> schools = _pdfController.getSchoolsFromParams(states, ids);
            fail("Expect exception when multiple states were provided, but number of states != number of IDs");
        } catch (IllegalArgumentException e) {
            // pass
        }
    }

    @Test
    public void testTuitionLow() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("tuition_low", "100");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "100", data.get("tuition_low"));
    }

    @Test
    public void testTuitionHigh() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("tuition_high", "100");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "100", data.get("tuition_high"));
    }

    @Test
    public void testFinancialAid() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("financial_aid", "100");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "100", data.get("financial_aid"));
    }

    @Test
    public void testStudentsVouchers() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("students_vouchers", "Yes");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("students_vouchers"));
    }

    @Test
    public void testEllLevel() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("ell_level", "Yes");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("ell_level"));
    }

    @Test
    public void testBestKnownForQuote() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("best_known_for", "<script>Providing a great education!");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "&lt;script&gt;Providing a great education!", data.get("bestKnownFor"));
    }

    @Test
    public void testApplicationDeadline() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DATE, 2);
        calendar.set(Calendar.YEAR, 2000);
        calendar.set(Calendar.MINUTE, 1); // I didnt expect this, but apparently SimpleDateFormat sets minute at 1 if no time specified
        Date date = calendar.getTime();

        List<EspResponse> espResponse = getAnEspResponse("application_deadline_date", "01/02/2000");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", date, data.get("application_deadline_date"));
    }


    @Test
    public void testDestinationSchools() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("destination_school_1", "One");
        espResponse.addAll(getAnEspResponse("destination_school_2", "Two"));
        espResponse.addAll(getAnEspResponse("destination_school_3", "Three"));

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "One; Two; Three", data.get("destination_schools"));
    }

    @Test
    public void testDestinationSchools_OnlyOne() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("destination_school_1", "One");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "One", data.get("destination_schools"));
    }

    @Test
    public void testDestinationSchools_None() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = new ArrayList<EspResponse>();

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "", data.get("destination_schools"));
    }

    @Test
    public void testBeforeAfterCare() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("before_after_care_start", "5:00");
        espResponse.addAll(getAnEspResponse("before_after_care_end", "7:00"));

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "5:00", data.get("before_after_care_start"));
        assertEquals("Expect OSP data to have been inserted into map", "7:00", data.get("before_after_care_end"));
    }

    @Test
    public void testClassHours() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("start_time", "5:00");
        espResponse.addAll(getAnEspResponse("end_time", "7:00"));

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "5:00", data.get("start_time"));
        assertEquals("Expect OSP data to have been inserted into map", "7:00", data.get("end_time"));
    }

    @Test
    public void testSpecialEdServices() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("special_ed_services", "One");
        espResponse.addAll(getAnEspResponse("special_ed_services", "Two"));
        espResponse.addAll(getAnEspResponse("special_ed_services", "Three"));

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "One; Two; Three", data.get("special_ed_services"));
    }

    @Test
    public void testSpecialEdServices_one() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("special_ed_services", "One");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "One", data.get("special_ed_services"));
    }

    @Test
    public void testSpecialEdServices_none() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = new ArrayList<EspResponse>();

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", null, data.get("special_ed_services"));
    }

    @Test
    public void testDressCode_uniform() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("dress_code", "Uniform");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("dress_code"));
    }

    @Test
    public void testDressCode_dress_code() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("dress_code", "dress_code");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("dress_code"));
    }

    @Test
    public void testDressCode_no_dress_code() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("dress_code", "no_dress_code");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "No", data.get("dress_code"));
    }

    @Test
    public void testTransportation_not_none() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("transportation", "alkfjds");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("transportation"));
    }

    @Test
    public void testTransportation_none() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("transportation", "none");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "No", data.get("transportation"));
    }

    @Test
    public void testTransportation_null() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = new ArrayList<EspResponse>();

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "No", data.get("transportation"));
    }
}
