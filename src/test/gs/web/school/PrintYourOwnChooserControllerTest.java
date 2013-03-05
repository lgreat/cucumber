package gs.web.school;

import gs.data.school.*;
import gs.data.state.State;
import gs.data.util.ListUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
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

    public School getASchool(State state, Integer id) {
        School s = new School();
        s.setId(id);
        s.setDatabaseState(state);
        s.setName("Test school for " + state.getAbbreviation() + " - " + id);
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

        _schoolDaoHibernate = createMock(ISchoolDao.class);

        _espResponseDaoHibernate = createStrictMock(IEspResponseDao.class);

        ReflectionTestUtils.setField(_pdfController, "_schoolDaoHibernate", _schoolDaoHibernate);
        ReflectionTestUtils.setField(_pdfController, "_espResponseDao", _espResponseDaoHibernate);
    }

    @Test
    public void testGetSchoolsFromParams() throws Exception {

        String states = "CA,DC,AK";
        String ids = "1,2,3";

        expect(_schoolDaoHibernate.getSchoolsByIds(eq(State.CA), eq("1"), eq(true))).andReturn(ListUtils.newArrayList(getASchool(State.CA, 1)));
        expect(_schoolDaoHibernate.getSchoolsByIds(eq(State.DC), eq("2"), eq(true))).andReturn(ListUtils.newArrayList(getASchool(State.DC, 2)));
        expect(_schoolDaoHibernate.getSchoolsByIds(eq(State.AK), eq("3"), eq(true))).andReturn(ListUtils.newArrayList(getASchool(State.AK, 3)));

        replay(_schoolDaoHibernate);

        List<School> schools = _pdfController.getSchoolsFromParams(states, ids);
        assertEquals("Expect number of schools returned to equal to number of IDs provided", 3, schools.size());

        verify(_schoolDaoHibernate);
    }

    @Test
    public void testGetSchoolsFromParams_missing_schools() throws Exception {

        String states = "CA,CA,CA";
        String ids = "1,2,3";

        expect(_schoolDaoHibernate.getSchoolsByIds(eq(State.CA), eq("1,2,3"), eq(true))).andReturn(ListUtils.newArrayList(getASchool(State.CA, 3)));

        replay(_schoolDaoHibernate);

        List<School> schools = _pdfController.getSchoolsFromParams(states, ids);
        assertEquals("Expect number of schools returned to equal to number of IDs provided", 1, schools.size());

        verify(_schoolDaoHibernate);
    }

    @Test
    public void testGetSchoolsFromParams_preserve_order() throws Exception {

        String states = "CA,DC,AK";
        String ids = "1,2,3";

        expect(_schoolDaoHibernate.getSchoolsByIds(eq(State.CA), eq("1"), eq(true))).andReturn(ListUtils.newArrayList(getASchool(State.AK, 3)));
        expect(_schoolDaoHibernate.getSchoolsByIds(eq(State.DC), eq("2"), eq(true))).andReturn(ListUtils.newArrayList(getASchool(State.DC, 2)));
        expect(_schoolDaoHibernate.getSchoolsByIds(eq(State.AK), eq("3"), eq(true))).andReturn(ListUtils.newArrayList(getASchool(State.CA, 1)));

        replay(_schoolDaoHibernate);

        List<School> schools = _pdfController.getSchoolsFromParams(states, ids);
        assertEquals("Expect order to be preserved", 1, schools.get(0).getId().intValue());
        assertEquals("Expect order to be preserved", 2, schools.get(1).getId().intValue());
        assertEquals("Expect order to be preserved", 3, schools.get(2).getId().intValue());
        assertEquals("Expect number of schools returned to equal to number of IDs provided", 3, schools.size());

        verify(_schoolDaoHibernate);
    }

    @Test
    public void testGetSchoolsFromParams_too_many() throws Exception {
        StringBuffer states = new StringBuffer();
        StringBuffer ids = new StringBuffer();
        List<School> results = new ArrayList<School>();

        for (int i = 0; i < 1000; i++) {
            if (i > 0) {
                states.append(",");
                ids.append(",");
            }
            states.append("CA");
            ids.append(i);
        }

        for (int i = 0; i < 100; i++) {
            results.add(getASchool(State.CA, i));
        }


        expect(_schoolDaoHibernate.getSchoolsByIds(eq(State.CA), eq("0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99"), eq(true))).andReturn(results).times(1);

        replay(_schoolDaoHibernate);

        List<School> schools = _pdfController.getSchoolsFromParams(states.toString(), ids.toString());
        assertEquals("Expect number of schools returned to equal to number of IDs provided", PrintYourOwnChooserController.MAX_ALLOWED_SCHOOLS, schools.size());

        verify(_schoolDaoHibernate);
    }

    @Test
    public void testGetSchoolsFromParams_removes_duplicates() throws Exception {
        StringBuffer states = new StringBuffer();
        StringBuffer ids = new StringBuffer();

        for (int i = 0; i < 50; i++) {
            if (i > 0) {
                states.append(",");
                ids.append(",");
            }
            states.append("CA");
            ids.append("1");
        }

        expect(_schoolDaoHibernate.getSchoolsByIds(eq(State.CA), eq("1"), eq(true))).andReturn(ListUtils.newArrayList(getASchool(State.CA, 1))).times(1);

        replay(_schoolDaoHibernate);

        List<School> schools = _pdfController.getSchoolsFromParams(states.toString(), ids.toString());
        assertEquals("Expect number of schools returned to equal to number of unique IDs provided", 1, schools.size());

        verify(_schoolDaoHibernate);
    }

    @Test
    public void testGetSchoolsFromParams_onlyOneState() throws Exception {

        String states = "CA";
        String ids = "1,2,3";

        expect(_schoolDaoHibernate.getSchoolsByIds(eq(State.CA), eq("1,2,3"), eq(true))).andReturn(
            ListUtils.newArrayList(
                    getASchool(State.CA, 1),
                    getASchool(State.CA, 2),
                    getASchool(State.CA, 3)
            )
        );

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
        Date date = calendar.getTime();

        List<EspResponse> espResponse = getAnEspResponse("application_deadline_date", "01/02/2000");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", date, data.get("application_deadline_date"));
    }

    @Test
    public void testDestinationSchools_schoolsAndColleges() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("destination_school_1", "School One");
        espResponse.addAll(getAnEspResponse("destination_school_2", "School Two"));
        espResponse.addAll(getAnEspResponse("destination_school_3", "School Three"));
        espResponse.addAll(getAnEspResponse("college_destination_1", "College One"));
        espResponse.addAll(getAnEspResponse("college_destination_2", "College Two"));
        espResponse.addAll(getAnEspResponse("college_destination_3", "College Three"));

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "School One; School Two; School Three", data.get("destinations"));
    }

    @Test
    public void testDestinationSchools_schools() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("destination_school_1", "School One");
        espResponse.addAll(getAnEspResponse("destination_school_2", "School Two"));
        espResponse.addAll(getAnEspResponse("destination_school_3", "School Three"));

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "School One; School Two; School Three", data.get("destinations"));
    }

    @Test
    public void testDestinationSchools_colleges() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("college_destination_1", "College One");
        espResponse.addAll(getAnEspResponse("college_destination_2", "College Two"));
        espResponse.addAll(getAnEspResponse("college_destination_3", "College Three"));

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "College One; College Two; College Three", data.get("destinations"));
    }

    @Test
    public void testDestinationSchools_OnlyOneSchool() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("destination_school_1", "School One");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "School One", data.get("destinations"));
    }

    @Test
    public void testDestinationSchools_OnlyOneCollege() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("college_destination_1", "College One");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "College One", data.get("destinations"));
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

        assertEquals("Expect OSP data to have been inserted into map", "", data.get("destinations"));
    }

    @Test
    public void testBeforeAfterCare() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("before_after_care_start", "5:00 AM");
        espResponse.addAll(getAnEspResponse("before_after_care_end", "7:00 PM"));
        espResponse.addAll(getAnEspResponse("before_after_care", "before"));
        espResponse.addAll(getAnEspResponse("before_after_care", "after"));

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Starts 5:00 a.m.", data.get("before_care"));
        assertEquals("Expect OSP data to have been inserted into map", "Ends 7:00 p.m.", data.get("after_care"));

        espResponse.clear();
        data.clear();
        espResponse = getAnEspResponse("before_after_care_start", "5:00 AM");
        espResponse.addAll(getAnEspResponse("before_after_care", "before"));
        espResponse.addAll(getAnEspResponse("before_after_care", "after"));

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Starts 5:00 a.m.", data.get("before_care"));
        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("after_care"));

        espResponse.clear();
        data.clear();
        espResponse = getAnEspResponse("before_after_care_end", "7:00 PM");
        espResponse.addAll(getAnEspResponse("before_after_care", "before"));
        espResponse.addAll(getAnEspResponse("before_after_care", "after"));

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("before_care"));
        assertEquals("Expect OSP data to have been inserted into map", "Ends 7:00 p.m.", data.get("after_care"));

        espResponse.clear();
        data.clear();
        espResponse = getAnEspResponse("before_after_care_start", "5:00 AM");
        espResponse.addAll(getAnEspResponse("before_after_care", "before"));

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Starts 5:00 a.m.", data.get("before_care"));
        assertEquals("Expect OSP data to have been inserted into map", "No", data.get("after_care"));

        espResponse.clear();
        data.clear();
        espResponse = getAnEspResponse("before_after_care_end", "7:00 PM");
        espResponse.addAll(getAnEspResponse("before_after_care", "after"));

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "No", data.get("before_care"));
        assertEquals("Expect OSP data to have been inserted into map", "Ends 7:00 p.m.", data.get("after_care"));

        espResponse.clear();
        data.clear();
        espResponse = getAnEspResponse("before_after_care", "before");
        espResponse.addAll(getAnEspResponse("before_after_care", "after"));

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("before_care"));
        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("after_care"));

        espResponse.clear();
        data.clear();
        espResponse = getAnEspResponse("before_after_care", "before");
        espResponse.addAll(getAnEspResponse("before_after_care", "after"));

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("before_care"));
        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("after_care"));

        espResponse.clear();
        data.clear();
        espResponse = getAnEspResponse("before_after_care", "before");

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("before_care"));
        assertEquals("Expect OSP data to have been inserted into map", "No", data.get("after_care"));

        espResponse.clear();
        data.clear();
        espResponse = getAnEspResponse("before_after_care", "after");

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "No", data.get("before_care"));
        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("after_care"));

        espResponse.clear();
        data.clear();
        espResponse = getAnEspResponse("before_after_care", "neither");

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "No", data.get("before_care"));
        assertEquals("Expect OSP data to have been inserted into map", "No", data.get("after_care"));

        espResponse.clear();
        data.clear();
        espResponse = getAnEspResponse("unrelated_key", "some_value");

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertNull("Expect no OSP data to have been inserted into map", data.get("before_care"));
        assertNull("Expect no OSP data to have been inserted into map", data.get("after_care"));
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
    public void testSpecialEd_level_only() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("spec_ed_level", "none");

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "None", data.get("special_ed"));

        data.clear();
        espResponse = getAnEspResponse("spec_ed_level", "basic");

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Basic", data.get("special_ed"));

        data.clear();
        espResponse = getAnEspResponse("spec_ed_level", "moderate");

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Moderate", data.get("special_ed"));

        data.clear();
        espResponse = getAnEspResponse("spec_ed_level", "intensive");

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Intensive", data.get("special_ed"));
    }

    @Test
    public void testSpecialEd_level_and_exists() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("spec_ed_level", "none");
        espResponse.addAll(getAnEspResponse("special_ed_programs_exists", "Yes"));

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "None", data.get("special_ed"));

        data.clear();
        espResponse = getAnEspResponse("spec_ed_level", "basic");
        espResponse.addAll(getAnEspResponse("special_ed_programs_exists", "Yes"));

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Basic", data.get("special_ed"));

        data.clear();
        espResponse = getAnEspResponse("spec_ed_level", "moderate");
        espResponse.addAll(getAnEspResponse("special_ed_programs_exists", "Yes"));

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Moderate", data.get("special_ed"));

        data.clear();
        espResponse = getAnEspResponse("spec_ed_level", "intensive");
        espResponse.addAll(getAnEspResponse("special_ed_programs_exists", "Yes"));

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Intensive", data.get("special_ed"));

        data.clear();
        espResponse = getAnEspResponse("spec_ed_level", "none");
        espResponse.addAll(getAnEspResponse("special_ed_programs_exists", "No"));

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "None", data.get("special_ed"));

        data.clear();
        espResponse = getAnEspResponse("spec_ed_level", "basic");
        espResponse.addAll(getAnEspResponse("special_ed_programs_exists", "No"));

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Basic", data.get("special_ed"));

        data.clear();
        espResponse = getAnEspResponse("spec_ed_level", "moderate");
        espResponse.addAll(getAnEspResponse("special_ed_programs_exists", "No"));

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Moderate", data.get("special_ed"));

        data.clear();
        espResponse = getAnEspResponse("spec_ed_level", "intensive");
        espResponse.addAll(getAnEspResponse("special_ed_programs_exists", "No"));

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Intensive", data.get("special_ed"));
    }

    @Test
    public void testSpecialEd_exists_only() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("special_ed_programs_exists", "yes");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("special_ed"));

        data.clear();
        espResponse = getAnEspResponse("special_ed_programs_exists", "no");

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "No", data.get("special_ed"));
    }

    @Test
    public void testSpecialEd_no_info() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = new ArrayList<EspResponse>();

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", null, data.get("special_ed"));

        // ignoring the 'special_ed_programs' data point, per spec - GS-13327
        data.clear();
        espResponse = getAnEspResponse("special_ed_programs", "any-valid-or-invalid-value");

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", null, data.get("special_ed"));
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
    public void testTransportation_invalid_value() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("transportation", "alkfjds");

        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertNull("Expect no OSP data to have been inserted into map", data.get("transportation"));
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
    public void testTransportation_special_ed_only() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("transportation", "special_ed_only");

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

        assertNull("Expect no OSP data to have been inserted into map", data.get("transportation"));
    }

    @Test
    public void testTransportation_valid_value() throws Exception {
        Map<String, Object> data = new HashMap<String,Object>();
        School school = getASchool();

        List<EspResponse> espResponse = getAnEspResponse("transportation", "passes");

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("transportation"));

        data.clear();
        espResponse = getAnEspResponse("transportation", "busses");

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("transportation"));

        data.clear();
        espResponse = getAnEspResponse("transportation", "shared_bus");

        reset(_espResponseDaoHibernate);
        expect(_espResponseDaoHibernate.getResponses(eq(school))).andReturn(espResponse);
        replay(_espResponseDaoHibernate);
        _pdfController.addOspDataToModel(school, data);
        verify(_espResponseDaoHibernate);

        assertEquals("Expect OSP data to have been inserted into map", "Yes", data.get("transportation"));
    }
}
