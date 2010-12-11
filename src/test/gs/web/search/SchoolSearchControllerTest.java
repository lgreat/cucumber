package gs.web.search;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.LevelCode;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.search.Indexer;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import static org.easymock.EasyMock.*;

import java.util.*;

public class SchoolSearchControllerTest extends BaseControllerTestCase {
    private SchoolSearchController _controller;
    private IDistrictDao _districtDao;
    private IGeoDao _geoDao;
    private SchoolSearchService _schoolSearchService;
    private CitySearchService _citySearchService;
    private DistrictSearchService _districtSearchService;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new SchoolSearchController();
        _districtDao = createStrictMock(IDistrictDao.class);
        _geoDao = createStrictMock(IGeoDao.class);

        _schoolSearchService = createStrictMock(SchoolSearchService.class);
        _citySearchService = createStrictMock(CitySearchService.class);
        _districtSearchService = createStrictMock(DistrictSearchService.class);

        _controller.setDistrictDao(_districtDao);
        _controller.setGeoDao(_geoDao);
        _controller.setSchoolSearchService(_schoolSearchService);
        _controller.setCitySearchService(_citySearchService);
        _controller.setDistrictSearchService(_districtSearchService);
        _controller.setStateManager(new StateManager());

        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
    }

    private void resetAllMocks() {
        resetMocks(_districtDao, _geoDao);
    }

    private void replayAllMocks() {
        replayMocks(_districtDao, _geoDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_districtDao, _geoDao);
    }

    public void testHandle() throws Exception {

        SchoolSearchCommand schoolSearchCommand = new SchoolSearchCommand();
        schoolSearchCommand.setSortBy(FieldSort.GS_RATING_DESCENDING.name());
        schoolSearchCommand.setState("ca");
        schoolSearchCommand.setPageSize(new Integer(5));
        schoolSearchCommand.setSearchString("alameda");
        schoolSearchCommand.setStart(10);

        Map<FieldConstraint, String> fieldConstraints = new HashMap<FieldConstraint,String>();
        fieldConstraints.put(FieldConstraint.STATE, "ca");

        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(getRequest());
        getRequest().setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        List<ISchoolSearchResult> listResults = new ArrayList<ISchoolSearchResult>();
        int i = 0;
        while (i++ < schoolSearchCommand.getPageSize()) {
            listResults.add(new LuceneSchoolSearchResult(new Document()));
        }
        SearchResultsPage page = new SearchResultsPage(50, listResults);

        expect(_schoolSearchService.search(eq(schoolSearchCommand.getSearchString()), eq(fieldConstraints), isA(List.class), eq(FieldSort.GS_RATING_DESCENDING), eq(10), eq(5))).andReturn(page);
        replay(_schoolSearchService);
        BindException errors = new BindException(schoolSearchCommand, "");
        ModelAndView modelAndView = _controller.handle(getRequest(), getResponse(), schoolSearchCommand, errors);
        verify(_schoolSearchService);

        assertEquals("Model should contain correct view name", "/search/schoolSearchResults", modelAndView.getViewName());
    }

    public void testHandle2() throws Exception {

        SchoolSearchCommand schoolSearchCommand = new SchoolSearchCommand();
        schoolSearchCommand.setSortBy(FieldSort.GS_RATING_DESCENDING.name());
        schoolSearchCommand.setState("ca");
        schoolSearchCommand.setPageSize(5);
        schoolSearchCommand.setSearchString("alameda");
        schoolSearchCommand.setStart(10);
        schoolSearchCommand.setRequestType("json");

        Map<FieldConstraint, String> fieldConstraints = new HashMap<FieldConstraint,String>();
        fieldConstraints.put(FieldConstraint.STATE, "ca");

        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(getRequest());
        getRequest().setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        List<ISchoolSearchResult> listResults = new ArrayList<ISchoolSearchResult>();
        int i = 0;
        while (i++ < schoolSearchCommand.getPageSize()) {
            listResults.add(new LuceneSchoolSearchResult(new Document()));
        }
        SearchResultsPage page = new SearchResultsPage(50, listResults);

        expect(_schoolSearchService.search(eq(schoolSearchCommand.getSearchString()), eq(fieldConstraints), isA(List.class), eq(FieldSort.GS_RATING_DESCENDING), eq(10), eq(5))).andReturn(page);
        replay(_schoolSearchService);
        BindException errors = new BindException(schoolSearchCommand, "");
        ModelAndView modelAndView = _controller.handle(getRequest(), getResponse(), schoolSearchCommand, errors);
        verify(_schoolSearchService);

        assertNotNull("Model should be no longer", modelAndView);
    }

    public void testHandle3() throws Exception {

        SchoolSearchCommand schoolSearchCommand = new SchoolSearchCommand();
        schoolSearchCommand.setSortBy(FieldSort.GS_RATING_DESCENDING.name());
        schoolSearchCommand.setState("ca");
        schoolSearchCommand.setPageSize(-5);
        schoolSearchCommand.setSearchString("alameda");
        schoolSearchCommand.setStart(10);
        schoolSearchCommand.setRequestType("json");

        Map<FieldConstraint, String> fieldConstraints = new HashMap<FieldConstraint,String>();
        fieldConstraints.put(FieldConstraint.STATE, "ca");

        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(getRequest());
        getRequest().setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        List<ISchoolSearchResult> listResults = new ArrayList<ISchoolSearchResult>();
        int i = 0;
        while (i++ < SchoolSearchCommand.DEFAULT_PAGE_SIZE) {
            listResults.add(new LuceneSchoolSearchResult(new Document()));
        }
        SearchResultsPage page = new SearchResultsPage(50, listResults);

        expect(_schoolSearchService.search(eq(schoolSearchCommand.getSearchString()), eq(fieldConstraints), isA(List.class), eq(FieldSort.GS_RATING_DESCENDING), eq(10), eq(SchoolSearchCommand.DEFAULT_PAGE_SIZE))).andReturn(page);
        replay(_schoolSearchService);
        BindException errors = new BindException(schoolSearchCommand, "");
        errors.rejectValue("pageSize","pageSize");
        ModelAndView modelAndView = _controller.handle(getRequest(), getResponse(), schoolSearchCommand, errors);
        verify(_schoolSearchService);

        assertNotNull("Model should be no longer", modelAndView);
    }

    public void testAddGamAttributes() throws Exception {
        Map<FieldConstraint,String> constraints = new HashMap<FieldConstraint,String>();
        List<FilterGroup> filterGroups = new ArrayList<FilterGroup>();
        List<FieldFilter> filters = new ArrayList<FieldFilter>();
        String searchString = null;
        List<ISchoolSearchResult> schoolResults = new ArrayList<ISchoolSearchResult>();
        PageHelper referencePageHelper;
        PageHelper actualPageHelper;

        // basic case: null checks

        // reset
        actualPageHelper = new PageHelper(_sessionContext, _request);
        filterGroups.clear();
        constraints.clear();
        searchString = null;
        schoolResults.clear();

        boolean threwException = false;
        try {
            // technically, this is an incomplete test, because none of these except searchString can be null
            _controller.addGamAttributes(null, null, null, null, null, null, null, null, null);
        } catch (IllegalArgumentException e) {
            threwException = true;
        }
        assertTrue(threwException);

        // GS-10003 - school type

        // reset
        actualPageHelper = new PageHelper(_sessionContext, _request);
        filterGroups.clear();
        constraints.clear();
        searchString = null;
        schoolResults.clear();

        filters.add(FieldFilter.SchoolTypeFilter.PUBLIC);
        filters.add(FieldFilter.SchoolTypeFilter.CHARTER);
        FilterGroup filterGroup = new FilterGroup();
        filterGroup.setFieldFilters(filters.toArray(new FieldFilter[0]));
        filterGroups.add(filterGroup);

        resetAllMocks();
        replayAllMocks();
        _controller.addGamAttributes(getRequest(), getResponse(), actualPageHelper, constraints, filterGroups, searchString, schoolResults, null, null);
        verifyAllMocks();

        Collection actualTypeKeywords = (Collection)actualPageHelper.getAdKeywords().get("type");
        assertNotNull(actualTypeKeywords);
        assertEquals(2,actualTypeKeywords.size());
        assertTrue(actualTypeKeywords.contains("public"));
        assertTrue(actualTypeKeywords.contains("charter"));

        // GS-6875 - level

        // reset
        actualPageHelper = new PageHelper(_sessionContext, _request);
        filterGroups.clear();
        constraints.clear();
        searchString = null;
        schoolResults.clear();

        resetAllMocks();

        replayAllMocks();
        _controller.addGamAttributes(getRequest(), getResponse(), actualPageHelper, constraints, filterGroups, searchString, schoolResults, null, null);
        verifyAllMocks();

        Collection actualLevelKeywords = (Collection)actualPageHelper.getAdKeywords().get("level");
        assertNotNull(actualLevelKeywords);
        assertEquals(4,actualLevelKeywords.size());
        assertTrue(actualLevelKeywords.contains("p"));
        assertTrue(actualLevelKeywords.contains("e"));
        assertTrue(actualLevelKeywords.contains("m"));
        assertTrue(actualLevelKeywords.contains("h"));

        // reset
        actualPageHelper = new PageHelper(_sessionContext, _request);
        filterGroups.clear();
        constraints.clear();
        searchString = null;
        schoolResults.clear();

        filters.add(FieldFilter.GradeLevelFilter.ELEMENTARY);
        filters.add(FieldFilter.GradeLevelFilter.HIGH);
        filterGroup.setFieldFilters(filters.toArray(new FieldFilter[0]));
        filterGroups.add(filterGroup);

        resetAllMocks();

        replayAllMocks();
        _controller.addGamAttributes(getRequest(), getResponse(), actualPageHelper, constraints, filterGroups, searchString, schoolResults, null, null);
        verifyAllMocks();

        actualLevelKeywords = (Collection)actualPageHelper.getAdKeywords().get("level");
        assertNotNull(actualLevelKeywords);
        assertEquals(2,actualLevelKeywords.size());
        assertTrue(actualLevelKeywords.contains("e"));
        assertTrue(actualLevelKeywords.contains("h"));

        // GS-10157 - district browse

        // reset
        actualPageHelper = new PageHelper(_sessionContext, _request);
        filterGroups.clear();
        constraints.clear();
        searchString = null;
        schoolResults.clear();

        constraints.put(FieldConstraint.STATE, "CA");
        constraints.put(FieldConstraint.DISTRICT_ID, "3");

        District district = new District();
        district.setId(1);
        district.setName("San Francisco Unified School District");

        resetAllMocks();

        replayAllMocks();
        _controller.addGamAttributes(getRequest(), getResponse(), actualPageHelper, constraints, filterGroups, searchString, schoolResults, null, district);
        verifyAllMocks();

        referencePageHelper = new PageHelper(_sessionContext, _request);
        referencePageHelper.addAdKeyword("district_name","San Francisco Unified School District");
        referencePageHelper.addAdKeyword("district_id","3");

        Collection actualDistrictIdKeywords = (Collection)actualPageHelper.getAdKeywords().get("district_id");
        Collection referenceDistrictIdKeywords = (Collection)referencePageHelper.getAdKeywords().get("district_id");
        assertNotNull(actualDistrictIdKeywords);
        assertEquals(1,actualDistrictIdKeywords.size());
        assertEquals((referenceDistrictIdKeywords.toArray())[0], (actualDistrictIdKeywords.toArray())[0]);
        Collection actualDistrictNameKeywords = (Collection)actualPageHelper.getAdKeywords().get("district_name");
        Collection referenceDistrictNameKeywords = (Collection)referencePageHelper.getAdKeywords().get("district_name");
        assertNotNull(actualDistrictNameKeywords);
        assertEquals(1,actualDistrictNameKeywords.size());
        assertEquals((referenceDistrictNameKeywords.toArray())[0], (actualDistrictNameKeywords.toArray())[0]);

        // city GAM attributes

        // GS-10448 - search results

        // reset
        actualPageHelper = new PageHelper(_sessionContext, _request);
        filterGroups.clear();
        constraints.clear();
        searchString = null;
        schoolResults.clear();

        Document doc;
        ISchoolSearchResult result;

        doc = new Document();
        doc.add(new Field(Indexer.CITY, "San Francisco", Field.Store.YES, Field.Index.TOKENIZED));
        result = new LuceneSchoolSearchResult(doc);
        schoolResults.add(result);
        doc = new Document();
        doc.add(new Field(Indexer.CITY, "San Francisco", Field.Store.YES, Field.Index.TOKENIZED));
        result = new LuceneSchoolSearchResult(doc);
        schoolResults.add(result);
        doc = new Document();
        doc.add(new Field(Indexer.CITY, "Bolinas", Field.Store.YES, Field.Index.TOKENIZED));
        result = new LuceneSchoolSearchResult(doc);
        schoolResults.add(result);
        doc = new Document();
        doc.add(new Field(Indexer.CITY, "Sacramento", Field.Store.YES, Field.Index.TOKENIZED));
        result = new LuceneSchoolSearchResult(doc);
        schoolResults.add(result);
        doc = new Document();
        doc.add(new Field(Indexer.CITY, "Berkeley", Field.Store.YES, Field.Index.TOKENIZED));
        result = new LuceneSchoolSearchResult(doc);
        schoolResults.add(result);

        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(getRequest());
        getRequest().setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        searchString = "school";

        resetAllMocks();
        replayAllMocks();
        _controller.addGamAttributes(getRequest(), getResponse(), actualPageHelper, constraints, filterGroups, searchString, schoolResults, null, null);
        verifyAllMocks();

        Collection actualCityKeywords = (Collection)actualPageHelper.getAdKeywords().get("city");
        assertNotNull(actualCityKeywords);
        assertEquals(4,actualCityKeywords.size());
        assertTrue(actualCityKeywords.contains("SanFrancis"));
        assertTrue(actualCityKeywords.contains("Bolinas"));
        assertTrue(actualCityKeywords.contains("Sacramento"));
        assertTrue(actualCityKeywords.contains("Berkeley"));

        // GS-5786 - city browse, GS-7809 - adsense hints for realtor.com, GS-6971 - city id cookie

        // reset
        actualPageHelper = new PageHelper(_sessionContext, _request);
        filterGroups.clear();
        constraints.clear();
        searchString = null;
        schoolResults.clear();

        resetAllMocks();

        constraints.put(FieldConstraint.STATE, "CA");
        constraints.put(FieldConstraint.CITY, "san francisco");

        City city = new City();
        city.setId(15432);
        city.setName("San Francisco");

        replayAllMocks();
        _controller.addGamAttributes(getRequest(), getResponse(), actualPageHelper, constraints, filterGroups, searchString, schoolResults, city, null);
        verifyAllMocks();

        referencePageHelper = new PageHelper(_sessionContext, _request);
        referencePageHelper.addAdKeyword("city","San Francisco");

        actualCityKeywords = (Collection)actualPageHelper.getAdKeywords().get("city");
        assertNotNull(actualCityKeywords);
        assertEquals(1,actualCityKeywords.size());
        assertTrue(actualCityKeywords.contains("SanFrancis"));

        String actualAdSenseHint = actualPageHelper.getAdSenseHint();
        assertEquals("san francisco california real estate house homes for sale", actualAdSenseHint);

        SessionContext sessionContext = SessionContextUtil.getSessionContext(getRequest());
        assertEquals(new Integer(15432), sessionContext.getCityId());

        // GS-10642 - query

        // reset
        actualPageHelper = new PageHelper(_sessionContext, _request);
        filterGroups.clear();
        constraints.clear();
        searchString = null;
        schoolResults.clear();

        resetAllMocks();

        searchString = " hi-tech   toys";

        replayAllMocks();
        _controller.addGamAttributes(getRequest(), getResponse(), actualPageHelper, constraints, filterGroups, searchString, schoolResults, null, null);
        verifyAllMocks();

        Collection actualQueryKeywords = (Collection)actualPageHelper.getAdKeywords().get("query");
        assertNotNull(actualQueryKeywords);
        assertEquals(3,actualQueryKeywords.size());
        assertTrue(actualQueryKeywords.contains("hi"));
        assertTrue(actualQueryKeywords.contains("tech"));
        assertTrue(actualQueryKeywords.contains("toys"));

       // GS-9323 - zip code

        // reset
        actualPageHelper = new PageHelper(_sessionContext, _request);
        filterGroups.clear();
        constraints.clear();
        searchString = null;
        schoolResults.clear();

        resetAllMocks();

        searchString = " 94105 ";

        replayAllMocks();
        _controller.addGamAttributes(getRequest(), getResponse(), actualPageHelper, constraints, filterGroups, searchString, schoolResults, null, null);
        verifyAllMocks();

        Collection actualZipcodeKeywords = (Collection)actualPageHelper.getAdKeywords().get("zipcode");
        assertNotNull(actualZipcodeKeywords);
        assertEquals(1,actualZipcodeKeywords.size());
        assertTrue(actualZipcodeKeywords.contains("94105"));
    }

    public void testGetRelCanonical() throws Exception {
        // TODO
    }

    public void testGetGradeLevelFilters() {
        String[] gradeLevels = new String[] {"p","h", "blah"};

        List<FieldFilter> filters = _controller.getGradeLevelFilters(gradeLevels);

        assertEquals("Filters list should contain two filters", 2, filters.size());

        assertTrue(filters.contains(FieldFilter.GradeLevelFilter.PRESCHOOL));
        assertTrue(filters.contains(FieldFilter.GradeLevelFilter.HIGH));
    }

    public void testGetSchoolTypeFilters() {
        String[] gradeLevels = new String[] {"public", "private", "charter", "blah"};

        List<FieldFilter> filters = _controller.getSchoolTypeFilters(gradeLevels);

        assertEquals("Filters list should contain two filters", 3, filters.size());

        assertTrue(filters.contains(FieldFilter.SchoolTypeFilter.PUBLIC));
        assertTrue(filters.contains(FieldFilter.SchoolTypeFilter.PRIVATE));
        assertTrue(filters.contains(FieldFilter.SchoolTypeFilter.CHARTER));
    }

    public void testGetSchoolTypeFilterNull() throws Exception {
        try {
            FieldFilter filter = _controller.getSchoolTypeFilter(null);
            fail("getSchoolTypeFilter() should have thrown exception");
        } catch(Exception e) {
            
        }
    }

    public void testGetGradeLevelFilterNull() throws Exception {
        try {
            FieldFilter filter = _controller.getGradeLevelFilter(null);
            fail("getGradeLevelFilter() should have thrown exception");
        } catch (Exception e) {

        }
    }

    public void testGetFieldConstraints1() {
        Map<FieldConstraint,String> fieldConstraints = _controller.getFieldConstraints(State.CA, null, null);

        assertTrue("fieldConstraints should contain state", fieldConstraints.containsKey(FieldConstraint.STATE));
    }

    public void testGetFieldConstraints2() {
        City city = new City();
        city.setId(1);
        city.setName("Alameda");

        Map<FieldConstraint,String> fieldConstraints = _controller.getFieldConstraints(State.CA, city, null);

        assertTrue("fieldConstraints should contain state", fieldConstraints.containsKey(FieldConstraint.STATE));
        assertTrue("fieldConstraints should contain city", fieldConstraints.containsKey(FieldConstraint.CITY));
    }

    public void testGetFieldConstraints3() {
        City city = new City();
        city.setId(1);
        city.setName("Alameda");
        District district = new District();
        district.setId(1);
        district.setName("Alameda City Unified School District");

        Map<FieldConstraint,String> fieldConstraints = _controller.getFieldConstraints(State.CA, city, district);

        assertTrue("fieldConstraints should contain state", fieldConstraints.containsKey(FieldConstraint.STATE));
        assertTrue("fieldConstraints should contain city", fieldConstraints.containsKey(FieldConstraint.CITY));
        assertTrue("fieldConstraints should contain city", fieldConstraints.containsKey(FieldConstraint.DISTRICT_ID));
        assertEquals("District should have correct id", 1, Integer.valueOf(fieldConstraints.get(FieldConstraint.DISTRICT_ID)).intValue());
    }

    public void testShouldHandleRequest1() {
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/california/alameda/Alameda-City-Unified-School-District/schools");
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(request);
        assertTrue("Should handle district browse", _controller.shouldHandleRequest(fields));

        request.setRequestURI("/california/alameda/schools");
        fields = new DirectoryStructureUrlFields(request);
        assertTrue("Should handle city browse", _controller.shouldHandleRequest(fields));

        request.setRequestURI("/california/alameda/Alameda-City-Unified-School-District");
        fields = new DirectoryStructureUrlFields(request);
        assertFalse("Should not handle district home", _controller.shouldHandleRequest(fields));

        request.setRequestURI("/california/alameda");
        fields = new DirectoryStructureUrlFields(request);
        assertFalse("Should handle city home", _controller.shouldHandleRequest(fields));

    }

    public void testGetOmnitureQuery() {
        String testSearchString = "</script>";
        String result = SchoolSearchController.getOmnitureQuery(testSearchString);
        assertFalse("Result should be escaped.", StringUtils.contains(result, '<'));
    }

    public void testAddPagingDataToModel() {
        int start = 11;
        int pageSize = 3;
        int totalResults = 17;

        SchoolSearchCommand command = new SchoolSearchCommand();
        command.setStart(11);
        command.setPageSize(3);
        int currentPage = command.getCurrentPage();

        List<ISchoolSearchResult> results = new ArrayList<ISchoolSearchResult>();
        results.add(new LuceneSchoolSearchResult());
        results.add(new LuceneSchoolSearchResult());
        results.add(new LuceneSchoolSearchResult());

        Map<String,Object> model = new HashMap<String,Object>();

        _controller.addPagingDataToModel(start, pageSize, currentPage, totalResults, model);

        assertTrue("model should contain start value", model.containsKey(SchoolSearchController.MODEL_START));
        assertEquals("model should contain correct start value", 11, ((Integer)(model.get(SchoolSearchController.MODEL_START))).intValue());

        assertTrue("model should contain 'total pages' value", model.containsKey(SchoolSearchController.MODEL_TOTAL_PAGES));
        assertEquals("model should contain correct 'total pages' value", 6, ((Integer)(model.get(SchoolSearchController.MODEL_TOTAL_PAGES))).intValue());
        
        assertTrue("model should contain 'current page' value", model.containsKey(SchoolSearchController.MODEL_CURRENT_PAGE));
        assertEquals("model should contain correct 'current page' value", 4, ((Integer)(model.get(SchoolSearchController.MODEL_CURRENT_PAGE))).intValue());

        assertTrue("model should contain 'page size' value", model.containsKey(SchoolSearchController.MODEL_PAGE_SIZE));
        assertEquals("model should contain correct 'page size' value", pageSize, ((Integer)(model.get(SchoolSearchController.MODEL_PAGE_SIZE))).intValue());

        assertTrue("model should have 'use paging' value", model.containsKey(SchoolSearchController.MODEL_USE_PAGING));
        assertTrue("should not be using paging", ((Boolean)(model.get(SchoolSearchController.MODEL_USE_PAGING))).booleanValue());

    }

    public void testAddPagingDataToModel2() {
        int pageSize = 0;
        int start = 0;
        int totalResults = 12;

        List<ISchoolSearchResult> results = new ArrayList<ISchoolSearchResult>();
        results.add(new LuceneSchoolSearchResult());
        results.add(new LuceneSchoolSearchResult());
        results.add(new LuceneSchoolSearchResult());

        int currentPage = 1;

        Map<String,Object> model = new HashMap<String,Object>();

        _controller.addPagingDataToModel(start, pageSize, currentPage, totalResults, model);

        assertTrue("model should have 'use paging' value", model.containsKey(SchoolSearchController.MODEL_USE_PAGING));
        assertFalse("should not be using paging", ((Boolean)(model.get(SchoolSearchController.MODEL_USE_PAGING))).booleanValue());
    }

    public void testAddPagingDataToModel3() {
        int pageSize = 25;
        int start = 0;
        int totalResults = 50;

        List<ISchoolSearchResult> results = new ArrayList<ISchoolSearchResult>();
        results.add(new LuceneSchoolSearchResult());
        results.add(new LuceneSchoolSearchResult());
        results.add(new LuceneSchoolSearchResult());

        Map<String,Object> model = new HashMap<String,Object>();

        int currentPage = 1;

        _controller.addPagingDataToModel(start, pageSize, currentPage, totalResults, model);

        assertTrue("model should contain start value", model.containsKey(SchoolSearchController.MODEL_START));
        assertEquals("model should contain correct start value", 0, ((Integer)(model.get(SchoolSearchController.MODEL_START))).intValue());

        assertTrue("model should contain 'total pages' value", model.containsKey(SchoolSearchController.MODEL_TOTAL_PAGES));
        assertEquals("model should contain correct 'total pages' value", 2, ((Integer)(model.get(SchoolSearchController.MODEL_TOTAL_PAGES))).intValue());

        assertTrue("model should contain 'current page' value", model.containsKey(SchoolSearchController.MODEL_CURRENT_PAGE));
        assertEquals("model should contain correct 'current page' value", 1, ((Integer)(model.get(SchoolSearchController.MODEL_CURRENT_PAGE))).intValue());

        assertTrue("model should contain 'page size' value", model.containsKey(SchoolSearchController.MODEL_PAGE_SIZE));
        assertEquals("model should contain correct 'page size' value", pageSize, ((Integer)(model.get(SchoolSearchController.MODEL_PAGE_SIZE))).intValue());

        assertTrue("model should have 'use paging' value", model.containsKey(SchoolSearchController.MODEL_USE_PAGING));
        assertTrue("should be using paging", ((Boolean)(model.get(SchoolSearchController.MODEL_USE_PAGING))).booleanValue());
    }

    public void testAddPagingDataToModelWhenStartGreaterThanTotalResults() {
        int pageSize = 5;
        int start = 132342;
        int totalResults = 12;

        List<ISchoolSearchResult> results = new ArrayList<ISchoolSearchResult>();
        results.add(new LuceneSchoolSearchResult());
        results.add(new LuceneSchoolSearchResult());
        results.add(new LuceneSchoolSearchResult());

        Map<String,Object> model = new HashMap<String,Object>();

        _controller.addPagingDataToModel(start, pageSize, 1, totalResults, model);

        assertTrue("model should have 'use paging' value", model.containsKey(SchoolSearchController.MODEL_USE_PAGING));
        assertEquals("start should be less than total results", 0, model.get(SchoolSearchController.MODEL_START));
    }

    public void testAddPagingDataToModelWhenStartGreaterThanTotalResults2() {
        int pageSize = 5;
        //start is zero-based
        int start = 12;
        int totalResults = 12;

        List<ISchoolSearchResult> results = new ArrayList<ISchoolSearchResult>();
        results.add(new LuceneSchoolSearchResult());
        results.add(new LuceneSchoolSearchResult());
        results.add(new LuceneSchoolSearchResult());

        Map<String,Object> model = new HashMap<String,Object>();

        _controller.addPagingDataToModel(start, pageSize, 1, totalResults, model);

        assertTrue("model should have 'use paging' value", model.containsKey(SchoolSearchController.MODEL_USE_PAGING));
        assertEquals("start should be less than total results", 0, model.get(SchoolSearchController.MODEL_START));
    }


    public void testTitleCalcCode() {
        // These all have standard headers
        assertEquals("San Francisco Schools - San Francisco, CA | GreatSchools", SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, null, null));
        assertEquals("San Francisco Schools - San Francisco, CA | GreatSchools", SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.ELEMENTARY_MIDDLE, null));
        assertEquals("San Francisco Schools - San Francisco, CA | GreatSchools", SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.MIDDLE_HIGH, null));

        // These useful views get nice SEO friendly titles
        assertEquals("San Francisco Elementary Schools - San Francisco, CA | GreatSchools", SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.ELEMENTARY, null));
        assertEquals("San Francisco Middle Schools - San Francisco, CA | GreatSchools", SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.MIDDLE, null));
        assertEquals("San Francisco High Schools - San Francisco, CA | GreatSchools", SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.HIGH, null));
        assertEquals("San Francisco Preschools and Daycare Centers - San Francisco, CA | GreatSchools", SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.PRESCHOOL, null));

        assertEquals("San Francisco Public Schools - San Francisco, CA | GreatSchools", SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, null, new String[]{"public"}));
        assertEquals("San Francisco Private Schools - San Francisco, CA | GreatSchools", SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, null, new String[]{"private"}));
        assertEquals("San Francisco Public Charter Schools - San Francisco, CA | GreatSchools", SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, null, new String[]{"charter"}));

        assertEquals("San Francisco Public and Private Schools - San Francisco, CA | GreatSchools",
                     SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, null, new String[]{"public", "private"}));
        assertEquals("San Francisco Public and Public Charter Schools - San Francisco, CA | GreatSchools",
                     SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, null, new String[]{"public", "charter"}));
        assertEquals("San Francisco Private and Public Charter Schools - San Francisco, CA | GreatSchools",
                     SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, null, new String[]{"private", "charter"}));

        assertEquals("San Francisco Public and Private Elementary Schools - San Francisco, CA | GreatSchools",
                     SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.ELEMENTARY, new String[]{"public", "private"}));
        assertEquals("San Francisco Public and Public Charter Middle Schools - San Francisco, CA | GreatSchools",
                     SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.MIDDLE, new String[]{"public", "charter"}));
        assertEquals("San Francisco Private and Public Charter High Schools - San Francisco, CA | GreatSchools",
                     SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.HIGH, new String[]{"private", "charter"}));
        assertEquals("San Francisco Private Preschools and Daycare Centers - San Francisco, CA | GreatSchools",
                     SchoolSearchController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.PRESCHOOL, new String[]{"private"}));
    }

    public void testMetaDescCalc() {
        assertEquals("View and map all San Francisco schools. Plus, compare or save schools.",
                SchoolSearchController.calcMetaDesc(null, "San Francisco", State.CA, null, null));
        assertEquals("View and map all San Francisco middle schools. Plus, compare or save middle schools.",
                SchoolSearchController.calcMetaDesc(null, "San Francisco", State.CA, LevelCode.MIDDLE, null));
        assertEquals("View and map all San Francisco public elementary schools. Plus, compare or save public elementary schools.",
                SchoolSearchController.calcMetaDesc(null, "San Francisco", State.CA, LevelCode.ELEMENTARY, new String[]{"public"}));

        assertEquals("Find the best preschools in San Francisco, California (CA) - view preschool ratings, reviews and map locations.",
                SchoolSearchController.calcMetaDesc(null, "San Francisco", State.CA, LevelCode.PRESCHOOL, null));

        assertEquals("View and map all schools in the Oakland Unified School District. Plus, compare or save schools in this district.",
                SchoolSearchController.calcMetaDesc("Oakland Unified School District", "Oakland", State.CA, null, null));
        assertEquals("View and map all middle schools in the Oakland Unified School District. Plus, compare or save middle schools in this district.",
                SchoolSearchController.calcMetaDesc("Oakland Unified School District", "Oakland", State.CA, LevelCode.MIDDLE, null));
        assertEquals("View and map all public elementary schools in the Oakland Unified School District. Plus, compare or save public elementary schools in this district.",
                SchoolSearchController.calcMetaDesc("Oakland Unified School District", "Oakland", State.CA, LevelCode.ELEMENTARY, new String[]{"public"}));
    }
}
