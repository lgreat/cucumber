package gs.web.school;

import gs.data.admin.IPropertyDao;
import gs.data.school.EspResponse;
import gs.data.school.School;
import gs.data.school.census.*;
import gs.data.state.State;
import gs.data.util.ListUtils;
import gs.web.BaseControllerTestCase;
import gs.web.request.RequestAttributeHelper;

import java.util.*;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author aroy@greatschools.org
 */
public class SchoolProfileStatsControllerTest extends BaseControllerTestCase {
    SchoolProfileStatsController _controller;

    SchoolProfileDataHelper _schoolProfileDataHelper;
    SchoolProfileCensusHelper _schoolProfileCensusHelper;
    ICensusCacheDao _censusCacheDao;
    IPropertyDao _propertyDao;
    RequestAttributeHelper _requestAttributeHelper;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new SchoolProfileStatsController();
        _requestAttributeHelper = org.easymock.classextension.EasyMock.createStrictMock(RequestAttributeHelper.class);

        _schoolProfileDataHelper = org.easymock.classextension.EasyMock.createStrictMock(SchoolProfileDataHelper.class);
        _schoolProfileCensusHelper = org.easymock.classextension.EasyMock.createStrictMock(SchoolProfileCensusHelper.class);
        _censusCacheDao = createStrictMock(ICensusCacheDao.class);
        _propertyDao = createStrictMock(IPropertyDao.class);

        _controller.setRequestAttributeHelper(_requestAttributeHelper);
        _controller.setSchoolProfileDataHelper(_schoolProfileDataHelper);
        _controller.setSchoolProfileCensusHelper(_schoolProfileCensusHelper);
        _controller.setCensusCacheDao(_censusCacheDao);
        _controller.setPropertyDao(_propertyDao);

    }

    public void testHandle_cachingOn() throws Exception {
        CensusStateConfig config = new CensusStateConfig(State.CA, new ArrayList<ICensusDataConfigEntry>());

        Map<Integer, CensusDataSet> censusDataSets = new HashMap<Integer, CensusDataSet>();
        censusDataSets.put(1, new CensusDataSet());

        School school = new School();
        school.setDatabaseState(State.CA);
        school.setId(1);

        // build some empty "display rows", mapped to the correct CensusGroups and students/teachers tabs
        Map<CensusGroup, GroupOfStudentTeacherViewRows> groupToGroupOfStudentTeacherViewRows =
            new LinkedHashMap<CensusGroup, GroupOfStudentTeacherViewRows>();

        GroupOfStudentTeacherViewRows studentRows = new GroupOfStudentTeacherViewRows(
                CensusGroup.Student_Ethnicity, new ArrayList<SchoolProfileStatsDisplayRow>()
        );
        GroupOfStudentTeacherViewRows teacherRows = new GroupOfStudentTeacherViewRows(
                CensusGroup.Teacher_Credentials, new ArrayList<SchoolProfileStatsDisplayRow>()
        );

        Map<CensusGroup, GroupOfStudentTeacherViewRows> mapOfStudentRows = new LinkedHashMap<CensusGroup, GroupOfStudentTeacherViewRows>();
        mapOfStudentRows.put(CensusGroup.Student_Ethnicity, studentRows);

        Map<CensusGroup, GroupOfStudentTeacherViewRows> mapOfTeacherRows = new LinkedHashMap<CensusGroup, GroupOfStudentTeacherViewRows>();
        mapOfTeacherRows.put(CensusGroup.Teacher_Credentials, teacherRows);

        groupToGroupOfStudentTeacherViewRows.putAll(mapOfStudentRows);
        groupToGroupOfStudentTeacherViewRows.putAll(mapOfTeacherRows);

        Map<String,Object> mockModel = new HashMap<String, Object>();

        // Start expectations:

        CensusDataHolder holder = org.easymock.classextension.EasyMock.createStrictMock(CensusDataHolder.class);

        // controller gets the School from RequestAttributeHelper
        expect(_requestAttributeHelper.getSchool(_request)).andReturn(school);

        // controller consults property DAO for cache enabled
        // test caching off
        expect(_propertyDao.getProperty(IPropertyDao.CENSUS_CACHE_ENABLED_KEY)).andReturn(String.valueOf("true"));

        // controller asks CensusCacheDao for the serialized model map, since caching is on
        expect(_censusCacheDao.getMapForSchool(eq(school))).andReturn(mockModel);

        // controller fetches ESP data
        Map<String, List<EspResponse>> espResults = new HashMap<String, List<EspResponse>>();
        expect(_schoolProfileDataHelper.getEspDataForSchool(_request)).andReturn(espResults);
        mockModel.put(SchoolProfileStatsController.MODEL_ESP_RESULTS_MAP_KEY, espResults);

        org.easymock.classextension.EasyMock.replay(holder);
        replayAllMocks();

        // go!
        Map<String, Object> model = _controller.handle(_request);

        assertEquals("Expect model returned from controller same one from cache with espResults", mockModel, model);

        verifyAllMocks();
        org.easymock.classextension.EasyMock.verify(holder);
        assertEquals("Expect EspResults to be set", espResults, model.get(SchoolProfileStatsController.MODEL_ESP_RESULTS_MAP_KEY));
    }

    public void testHandle_cachingOff() throws Exception {
        CensusStateConfig config = new CensusStateConfig(State.CA, new ArrayList<ICensusDataConfigEntry>());

        Map<Integer, CensusDataSet> censusDataSets = new HashMap<Integer, CensusDataSet>();
        censusDataSets.put(1, new CensusDataSet());

        School school = new School();
        school.setDatabaseState(State.CA);
        school.setId(1);

        // build some empty "display rows", mapped to the correct CensusGroups and students/teachers tabs
        Map<CensusGroup, GroupOfStudentTeacherViewRows> groupToGroupOfStudentTeacherViewRows =
                new LinkedHashMap<CensusGroup, GroupOfStudentTeacherViewRows>();

        GroupOfStudentTeacherViewRows studentRows = new GroupOfStudentTeacherViewRows(
                CensusGroup.Student_Ethnicity, new ArrayList<SchoolProfileStatsDisplayRow>()
        );
        GroupOfStudentTeacherViewRows teacherRows = new GroupOfStudentTeacherViewRows(
                CensusGroup.Teacher_Credentials, new ArrayList<SchoolProfileStatsDisplayRow>()
        );

        Map<CensusGroup, GroupOfStudentTeacherViewRows> mapOfStudentRows = new LinkedHashMap<CensusGroup, GroupOfStudentTeacherViewRows>();
        mapOfStudentRows.put(CensusGroup.Student_Ethnicity, studentRows);

        Map<CensusGroup, GroupOfStudentTeacherViewRows> mapOfTeacherRows = new LinkedHashMap<CensusGroup, GroupOfStudentTeacherViewRows>();
        mapOfTeacherRows.put(CensusGroup.Teacher_Credentials, teacherRows);

        groupToGroupOfStudentTeacherViewRows.putAll(mapOfStudentRows);
        groupToGroupOfStudentTeacherViewRows.putAll(mapOfTeacherRows);


        // Start expectations:

        CensusDataHolder holder = org.easymock.classextension.EasyMock.createStrictMock(CensusDataHolder.class);

        // controller gets the School from RequestAttributeHelper
        expect(_requestAttributeHelper.getSchool(_request)).andReturn(school);

        // controller consults property DAO for cache enabled
        // test caching off
        expect(_propertyDao.getProperty(IPropertyDao.CENSUS_CACHE_ENABLED_KEY)).andReturn("false");

        // controller gets CensusDataHolder (bucket of data sets for multiple profile tabs) from helper
        expect(_schoolProfileCensusHelper.getCensusDataHolder(_request)).andReturn(holder);

        // controller tells CensusDataHolder to grab school|district|state values that it doesnt have yet (ones other tabs haven't loaded)
        expect(holder.retrieveDataSetsAndAllData()).andReturn(null);
        expect(holder.getAllCensusDataSets()).andReturn(censusDataSets);

        // controller gets the Census Config data, which tells the view how to display data
        expect(_schoolProfileCensusHelper.getCensusStateConfig(_request)).andReturn(config);


        // controller tells the shared buildDisplayRows(...) to construct a map of CensusGroups and "display rows"
        // using the data sets it has
        expect(_schoolProfileCensusHelper.buildDisplayRows(config, censusDataSets)).andReturn(groupToGroupOfStudentTeacherViewRows);

        // controller fetches ESP data
        Map<String, List<EspResponse>> espResults = new HashMap<String, List<EspResponse>>();
        expect(_schoolProfileDataHelper.getEspDataForSchool(_request)).andReturn(espResults);


        org.easymock.classextension.EasyMock.replay(holder);
        replayAllMocks();

        // go!
        Map<String, Object> model = _controller.handle(_request);

        verifyAllMocks();
        org.easymock.classextension.EasyMock.verify(holder);

        assertEquals("Expect model to have correct TabToGroupsView for students tab",
                mapOfStudentRows, ((SchoolProfileStatsController.TabToGroupsView)
                model.get(SchoolProfileStatsController.MODEL_STUDENTS_TAB_KEY)).getGroupToGroupOfRowsMap());

        assertEquals("Expect model to have correct TabToGroupsView for teachers tab",
                mapOfTeacherRows, ((SchoolProfileStatsController.TabToGroupsView)
                model.get(SchoolProfileStatsController.MODEL_TEACHERS_TAB_KEY)).getGroupToGroupOfRowsMap());

        assertEquals("Expect EspResults to be set", espResults, model.get(SchoolProfileStatsController.MODEL_ESP_RESULTS_MAP_KEY));
        assertNotNull("Expect footnotes to be set", model.get(SchoolProfileStatsController.MODEL_FOOTNOTES_MAP_KEY));
    }


    public void replayAllMocks() {
        org.easymock.classextension.EasyMock.replay(_requestAttributeHelper);
        org.easymock.classextension.EasyMock.replay(_schoolProfileDataHelper);
        org.easymock.classextension.EasyMock.replay(_schoolProfileCensusHelper);
        replayMocks(_censusCacheDao, _propertyDao);
    }

    public void verifyAllMocks() {
        org.easymock.classextension.EasyMock.verify(_requestAttributeHelper);
        org.easymock.classextension.EasyMock.verify(_schoolProfileDataHelper);
        org.easymock.classextension.EasyMock.verify(_schoolProfileCensusHelper);
        verifyMocks(_censusCacheDao, _propertyDao);
    }

    public void testBasics() {
        assertNotNull(_controller);
    }




}
