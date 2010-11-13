package gs.web.search;

import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.PageHelper;
import junit.framework.TestCase;
import org.apache.commons.collections.MultiMap;
import org.apache.lucene.document.Document;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import static org.easymock.EasyMock.*;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class SchoolSearchControllerTest extends BaseControllerTestCase {
    private SchoolSearchController _controller;
    private IDistrictDao _districtDao;
    private SchoolSearchService _schoolSearchService;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new SchoolSearchController();
        _districtDao = createStrictMock(IDistrictDao.class);

        _schoolSearchService = createStrictMock(SchoolSearchService.class);

        _controller.setDistrictDao(_districtDao);
        _controller.setSchoolSearchService(_schoolSearchService);
        _controller.setStateManager(new StateManager());

        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
    }

    public void testHandle() throws Exception {

        SchoolSearchCommand schoolSearchCommand = new SchoolSearchCommand();
        schoolSearchCommand.setSortBy(FieldSort.GS_RATING.name());
        schoolSearchCommand.setState("ca");
        schoolSearchCommand.setPageSize(new Integer(5));
        schoolSearchCommand.setSearchString("alameda");
        schoolSearchCommand.setStart(10);

        Map<FieldConstraint, String> fieldConstraints = new HashMap<FieldConstraint,String>();
        fieldConstraints.put(FieldConstraint.STATE, "ca");

        List<ISchoolSearchResult> listResults = new ArrayList<ISchoolSearchResult>();
        int i = 0;
        while (i++ < schoolSearchCommand.getPageSize()) {
            listResults.add(new LuceneSchoolSearchResult());
        }
        SearchResultsPage page = new SearchResultsPage(50, listResults);

        expect(_schoolSearchService.search(eq(schoolSearchCommand.getSearchString()), eq(fieldConstraints), isA(FieldFilter[].class), eq(FieldSort.GS_RATING))).andReturn(page);
        replay(_schoolSearchService);
        BindException errors = new BindException(schoolSearchCommand, "");
        ModelAndView modelAndView = _controller.handle(getRequest(), getResponse(), schoolSearchCommand, errors);
        verify(_schoolSearchService);

        assertEquals("Model should contain correct view name", "/search/schoolSearchResults", modelAndView.getViewName());
    }

    public void testHandle2() throws Exception {

        SchoolSearchCommand schoolSearchCommand = new SchoolSearchCommand();
        schoolSearchCommand.setSortBy(FieldSort.GS_RATING.name());
        schoolSearchCommand.setState("ca");
        schoolSearchCommand.setPageSize(new Integer(5));
        schoolSearchCommand.setSearchString("alameda");
        schoolSearchCommand.setStart(10);
        schoolSearchCommand.setFormat("json");

        Map<FieldConstraint, String> fieldConstraints = new HashMap<FieldConstraint,String>();
        fieldConstraints.put(FieldConstraint.STATE, "ca");

        List<ISchoolSearchResult> listResults = new ArrayList<ISchoolSearchResult>();
        int i = 0;
        while (i++ < schoolSearchCommand.getPageSize()) {
            listResults.add(new LuceneSchoolSearchResult(new Document()));
        }
        SearchResultsPage page = new SearchResultsPage(50, listResults);

        expect(_schoolSearchService.search(eq(schoolSearchCommand.getSearchString()), eq(fieldConstraints), isA(FieldFilter[].class), eq(FieldSort.GS_RATING))).andReturn(page);
        replay(_schoolSearchService);
        BindException errors = new BindException(schoolSearchCommand, "");
        ModelAndView modelAndView = _controller.handle(getRequest(), getResponse(), schoolSearchCommand, errors);
        verify(_schoolSearchService);

        assertNull("Model should be null", modelAndView);
    }

    public void testAddGamAttributes() {
        Map<FieldConstraint,String> constraints = new HashMap<FieldConstraint,String>();
        List<FieldFilter> filters = new ArrayList<FieldFilter>();
        PageHelper referencePageHelper;
        PageHelper actualPageHelper;

        // basic case: null checks
        boolean threwException = false;
        try {
            _controller.addGamAttributes(null, null, null);
        } catch (IllegalArgumentException e) {
            threwException = true;
        }
        assertTrue(threwException);

        // school type

        filters.add(FieldFilter.SchoolTypeFilter.PUBLIC);
        filters.add(FieldFilter.SchoolTypeFilter.CHARTER);

        actualPageHelper = new PageHelper(_sessionContext, _request);
        _controller.addGamAttributes(actualPageHelper, constraints, filters);

        referencePageHelper = new PageHelper(_sessionContext, _request);
        referencePageHelper.addAdKeywordMulti("type","public");
        referencePageHelper.addAdKeywordMulti("type","charter");

        Collection actualTypeKeywords = (Collection)actualPageHelper.getAdKeywords().get("type");
        assertNotNull(actualTypeKeywords);
        assertEquals(2,actualTypeKeywords.size());
        assertTrue(actualTypeKeywords.contains("public"));
        assertTrue(actualTypeKeywords.contains("charter"));

        // district browse

        filters.clear();
        constraints.clear();

        constraints.put(FieldConstraint.DISTRICT_ID, "3");

        actualPageHelper = new PageHelper(_sessionContext, _request);
        _controller.addGamAttributes(actualPageHelper, constraints, filters);

        referencePageHelper = new PageHelper(_sessionContext, _request);
        referencePageHelper.addAdKeyword("district_name","San Francisco Unified School District");
        referencePageHelper.addAdKeyword("district_id","3");

        Collection actualDistrictIdKeywords = (Collection)actualPageHelper.getAdKeywords().get("district_id");
        assertNotNull(actualDistrictIdKeywords);
        assertEquals(1,actualDistrictIdKeywords.size());
        // TODO:fixme
        //assertEquals("3", (String)actualDistrictIdKeywords.get(0));
/*
        Collection actualDistrictNameKeywords = (Collection)actualPageHelper.getAdKeywords().get("district_name");
        assertNotNull(actualDistrictNameKeywords);
        assertEquals(1,actualDistrictNameKeywords.size());
*/

/*
        referencePageHelper.addAdKeyword("state", "CA");
        referencePageHelper.addAdKeywordMulti("editorial", "Category 1");
        referencePageHelper.addAdKeywordMulti("editorial", "Category 2");
        referencePageHelper.addAdKeywordMulti("editorial", "Category 3");
        referencePageHelper.addAdKeywordMulti("editorial", "2ndCat1");
        referencePageHelper.addAdKeywordMulti("editorial", "2ndCat2");
        referencePageHelper.addAdKeywordMulti("editorial", "2ndCatA");
        referencePageHelper.addAdKeywordMulti("editorial", "2ndCatB");
        referencePageHelper.addAdKeyword("article_id", "23");

        Collection referenceEditorialKeywords = (Collection)referencePageHelper.getAdKeywords().get("editorial");
        Collection actualEditorialKeywords = (Collection)pageHelper.getAdKeywords().get("editorial");
        assertEquals(referenceEditorialKeywords.size(), actualEditorialKeywords.size());
*/
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

    public void testGetChosenSort() {
        SchoolSearchCommand schoolSearchCommand = new SchoolSearchCommand();
        schoolSearchCommand.setSortBy("SCHOOL_NAME_ASCENDING");
        FieldSort sort = _controller.getChosenSort(schoolSearchCommand);
        assertEquals("Should return correct sort", FieldSort.SCHOOL_NAME_ASCENDING, sort);
    }

    public void testGetFieldConstraints1() {
        SchoolSearchCommand schoolSearchCommand = new SchoolSearchCommand();
        schoolSearchCommand.setState("ca");
        HttpServletRequest request = getRequest();
        Map<FieldConstraint,String> fieldConstraints = _controller.getFieldConstraints(schoolSearchCommand, request);

        assertTrue("fieldConstraints should contain state", fieldConstraints.containsKey(FieldConstraint.STATE));
    }

    public void testGetFieldConstraints2() {
        SchoolSearchCommand schoolSearchCommand = new SchoolSearchCommand();
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/california/alameda/schools");
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(request);
        request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        Map<FieldConstraint,String> fieldConstraints = _controller.getFieldConstraints(schoolSearchCommand, request);

        assertTrue("fieldConstraints should contain state", fieldConstraints.containsKey(FieldConstraint.STATE));
        assertTrue("fieldConstraints should contain city", fieldConstraints.containsKey(FieldConstraint.CITY));
    }

    public void testGetFieldConstraints3() {
        SchoolSearchCommand schoolSearchCommand = new SchoolSearchCommand();
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/california/alameda/Alameda-City-Unified-School-District/schools");
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(request);
        request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        District district = new District();
        district.setId(1);

        expect(_districtDao.findDistrictByNameAndCity(State.CA, "Alameda City Unified School District", "alameda")).andReturn(district);
        replay(_districtDao);

        Map<FieldConstraint,String> fieldConstraints = _controller.getFieldConstraints(schoolSearchCommand, request);

        verify(_districtDao);
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

    public void testAddPagingDataToModel() {
        int start = 11;
        int pageSize = 3;
        int totalResults = 17;

        List<ISchoolSearchResult> results = new ArrayList<ISchoolSearchResult>();
        results.add(new LuceneSchoolSearchResult());
        results.add(new LuceneSchoolSearchResult());
        results.add(new LuceneSchoolSearchResult());

        Map<String,Object> model = new HashMap<String,Object>();

        _controller.addPagingDataToModel(start, pageSize, totalResults, model);

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

        Map<String,Object> model = new HashMap<String,Object>();

        _controller.addPagingDataToModel(start, pageSize, totalResults, model);

        assertTrue("model should have 'use paging' value", model.containsKey(SchoolSearchController.MODEL_USE_PAGING));
        assertFalse("should not be using paging", ((Boolean)(model.get(SchoolSearchController.MODEL_USE_PAGING))).booleanValue());
    }
}
