/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: SchoolsControllerTest.java,v 1.63 2012/10/11 15:06:08 yfan Exp $
 */

package gs.web.school;

import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.search.SchoolSearchResult;
import gs.data.search.Searcher;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.geo.StateSpecificFooterHelper;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.RedirectView301;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;

/**
 * Tests SchoolsController.
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 */
public class SchoolsControllerTest extends BaseControllerTestCase {

    private SchoolsController _controller;
    private SessionContextUtil _sessionContextUtil;

    protected void setUp() throws Exception {
        super.setUp();

        _controller = new SchoolsController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setDistrictDao((IDistrictDao) getApplicationContext().getBean(IDistrictDao.BEAN_ID));
        _controller.setSchoolDao((ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID));
        _controller.setSearcher((Searcher) getApplicationContext().getBean(Searcher.BEAN_ID));
        _controller.setGeoDao((IGeoDao) getApplicationContext().getBean(IGeoDao.BEAN_ID));
        _controller.setStateSpecificFooterHelper(createMock(StateSpecificFooterHelper.class));

        _sessionContextUtil = (SessionContextUtil) getApplicationContext().getBean(SessionContextUtil.BEAN_ID);
    }

    public void testHandleRequestInternalRedirects() throws Exception {
        GsMockHttpServletRequest request = getRequest();

        // old-style to new-style city browse urls
        request.removeAllParameters();
        request.setRequestURI("/schools.page");
        request.setParameter(SchoolsController.PARAM_CITY, "Anchorage");
        request.setParameter("state", "AK");
        request.setQueryString("city=Anchorage&state=AK");
        _sessionContextUtil.updateStateFromParam(getSessionContext(), request, getResponse());
        ModelAndView mAndV = _controller.handleRequestInternal(request, getResponse());
        assertTrue(mAndV.getView() instanceof RedirectView301);
        assertEquals("/alaska/anchorage/schools/", ((RedirectView) mAndV.getView()).getUrl());

        // district browse with multiple level codes in same parameter, comma-separated
        request.removeAllParameters();
        request.setRequestURI("/schools.page");
        request.setParameter(SchoolsController.PARAM_DISTRICT, "717");
        request.setParameter("state", "CA");
        request.setParameter(SchoolsController.PARAM_LEVEL_CODE, "e,m");
        request.setParameter(SchoolsController.PARAM_SHOW_ALL, "1");
        request.setQueryString("district=717&state=CA&lc=e%2Cm&showall=1");
        _sessionContextUtil.updateStateFromParam(getSessionContext(), request, getResponse());
        mAndV = _controller.handleRequestInternal(request, getResponse());
        assertTrue((mAndV.getView() instanceof RedirectView) && !(mAndV.getView() instanceof RedirectView301));
        assertEquals("/schools.page?district=717&state=CA&showall=1", ((RedirectView) mAndV.getView()).getUrl());

        // district browse with multiple level codes in repeated "lc" parameters
        request.removeAllParameters();
        request.setRequestURI("/schools.page");
        request.setParameter(SchoolsController.PARAM_DISTRICT, "717");
        request.setParameter("state", "CA");
        request.addParameter(SchoolsController.PARAM_LEVEL_CODE, "e");
        request.addParameter(SchoolsController.PARAM_LEVEL_CODE, "m");
        request.setParameter(SchoolsController.PARAM_SHOW_ALL, "1");
        request.setQueryString("district=717&state=CA&lc=e&lc=m&showall=1");
        _sessionContextUtil.updateStateFromParam(getSessionContext(), request, getResponse());
        mAndV = _controller.handleRequestInternal(request, getResponse());
        assertTrue((mAndV.getView() instanceof RedirectView) && !(mAndV.getView() instanceof RedirectView301));
        assertEquals("/schools.page?district=717&state=CA&showall=1", ((RedirectView) mAndV.getView()).getUrl());
    }

    public void testIsDistrictBrowseRequest() throws Exception {
        GsMockHttpServletRequest request = getRequest();

        request.setRequestURI(null);
        boolean foundException = false;
        try {
            SchoolsController.isDistrictBrowseRequest(request);
        } catch (IllegalArgumentException e) {
            foundException = true;
        }
        assertTrue("Expected IllegalArgumentException when request URI null", foundException);

        request.setRequestURI("/schools.page");
        request.setParameter(SchoolsController.PARAM_DISTRICT, "717");

        boolean isDistrictBrowse = SchoolsController.isDistrictBrowseRequest(request);
        assertTrue("Expected request to be identified as district browse", isDistrictBrowse);

        request.removeParameter(SchoolsController.PARAM_DISTRICT);
        isDistrictBrowse = SchoolsController.isDistrictBrowseRequest(request);
        assertFalse("Expected request to be identified not as district browse due to missing PARAM_DISTRICT", isDistrictBrowse);

        request.setRequestURI("/california/san-francisco/schools/");
        request.setParameter(SchoolsController.PARAM_DISTRICT, "717");
        isDistrictBrowse = SchoolsController.isDistrictBrowseRequest(request);
        assertFalse("Expected request to be identified not as district browse due to requestURI not containing /schools.page", isDistrictBrowse);
    }

    public void testIsOldStyleCityBrowseRequest() throws Exception {
        GsMockHttpServletRequest request = getRequest();

        request.setRequestURI(null);
        boolean foundException = false;
        try {
            SchoolsController.isOldStyleCityBrowseRequest(request);
        } catch (IllegalArgumentException e) {
            foundException = true;
        }
        assertTrue("Expected IllegalArgumentException when request URI null", foundException);

        request.setRequestURI("/schools.page");
        request.removeParameter(SchoolsController.PARAM_DISTRICT);
        request.setParameter(SchoolsController.PARAM_CITY, "Portland");
        assertTrue("Expected request considered old-style city browse", SchoolsController.isOldStyleCityBrowseRequest(request));

        request.setRequestURI("/schools.page");
        request.removeParameter(SchoolsController.PARAM_DISTRICT);
        request.removeParameter(SchoolsController.PARAM_CITY);
        assertFalse("Expected request not considered old-style city browse", SchoolsController.isOldStyleCityBrowseRequest(request));

        request.setRequestURI("/schools.page");
        request.setParameter(SchoolsController.PARAM_DISTRICT, "717");
        request.removeParameter(SchoolsController.PARAM_CITY);
        assertFalse("Expected request not considered old-style city browse", SchoolsController.isOldStyleCityBrowseRequest(request));
    }

    public void testCreateNewCityBrowseURIFromRequest() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        getSessionContext().setState(State.CA);
        request.setParameter(SchoolsController.PARAM_CITY, "San Francisco");

        // no filters

        String expectedRedirectURI = "/california/san-francisco/schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        // type filters

        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE, SchoolType.PUBLIC.getSchoolTypeName());
        expectedRedirectURI = "/california/san-francisco/schools/?st=public";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE, SchoolType.PRIVATE.getSchoolTypeName());
        expectedRedirectURI = "/california/san-francisco/schools/?st=private";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE, SchoolType.CHARTER.getSchoolTypeName());
        expectedRedirectURI = "/california/san-francisco/schools/?st=charter";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        // multiple st values in public,private,charter order
        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE,
                new String[]{SchoolType.PUBLIC.getSchoolTypeName(), SchoolType.CHARTER.getSchoolTypeName()});
        expectedRedirectURI = "/california/san-francisco/schools/?st=public&st=charter";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE,
                new String[]{SchoolType.PRIVATE.getSchoolTypeName(), SchoolType.CHARTER.getSchoolTypeName()});
        expectedRedirectURI = "/california/san-francisco/schools/?st=private&st=charter";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE,
                new String[]{SchoolType.PUBLIC.getSchoolTypeName(), SchoolType.PRIVATE.getSchoolTypeName()});
        expectedRedirectURI = "/california/san-francisco/schools/?st=public&st=private";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE,
                new String[]{SchoolType.PUBLIC.getSchoolTypeName(), SchoolType.PRIVATE.getSchoolTypeName(), SchoolType.CHARTER.getSchoolTypeName()});
        expectedRedirectURI = "/california/san-francisco/schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        // level filters

        request.removeParameter(SchoolsController.PARAM_SCHOOL_TYPE);

        request.setParameter(SchoolsController.PARAM_LEVEL_CODE, LevelCode.PRESCHOOL.getLowestLevel().getName());
        expectedRedirectURI = "/california/san-francisco/schools/?gradeLevels=p";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_LEVEL_CODE, LevelCode.ELEMENTARY.getLowestLevel().getName());
        expectedRedirectURI = "/california/san-francisco/schools/?gradeLevels=e";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_LEVEL_CODE, LevelCode.MIDDLE.getLowestLevel().getName());
        expectedRedirectURI = "/california/san-francisco/schools/?gradeLevels=m";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_LEVEL_CODE, LevelCode.HIGH.getLowestLevel().getName());
        expectedRedirectURI = "/california/san-francisco/schools/?gradeLevels=h";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        // multiple level codes - values in p,e,m,h sorted order
        request.setParameter(SchoolsController.PARAM_LEVEL_CODE,
                new String[]{LevelCode.MIDDLE.getLowestLevel().getName(), LevelCode.HIGH.getLowestLevel().getName()});
        expectedRedirectURI = "/california/san-francisco/schools/?gradeLevels=m&gradeLevels=h";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        // combined filters

        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE,
                new String[]{SchoolType.PUBLIC.getSchoolTypeName(), SchoolType.CHARTER.getSchoolTypeName()});
        request.removeParameter(SchoolsController.PARAM_LEVEL_CODE);
        request.setParameter(SchoolsController.PARAM_LEVEL_CODE, LevelCode.ELEMENTARY.getLowestLevel().getName());
        expectedRedirectURI = "/california/san-francisco/schools/?gradeLevels=e&st=public&st=charter";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.removeParameter(SchoolsController.PARAM_SCHOOL_TYPE);
        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE, SchoolType.CHARTER.getSchoolTypeName());
        request.removeParameter(SchoolsController.PARAM_LEVEL_CODE);
        request.setParameter(SchoolsController.PARAM_LEVEL_CODE, LevelCode.MIDDLE.getLowestLevel().getName());
        expectedRedirectURI = "/california/san-francisco/schools/?gradeLevels=m&st=charter";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        // hyphenated city
        request.removeParameter(SchoolsController.PARAM_SCHOOL_TYPE);
        request.removeParameter(SchoolsController.PARAM_LEVEL_CODE);
        request.setParameter(SchoolsController.PARAM_CITY, "Cardiff-By-The-Sea");
        expectedRedirectURI = "/california/cardiff_by_the_sea/schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));
    }

    public void testErrorPageOnNoCity() throws Exception {
        GsMockHttpServletRequest request = getRequest();

        request.removeAllParameters();
        request.setRequestURI("/schools.page");
        request.setParameter("state", "AK");
        request.setQueryString("state=AK");
        _sessionContextUtil.updateStateFromParam(getSessionContext(), request, getResponse());
        ModelAndView mAndV = _controller.handleRequestInternal(request, getResponse());
        assertEquals("status/error", mAndV.getViewName());
    }

    public void testGetCheckedSchools() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        List<School> schools;

        request.removeAllParameters();
        request.setRequestURI("/schools.page");
        request.setParameter("cmp", "AK508,AK510,AK509");
        //request.setQueryString("cmp=AK508,AK510,AK509");
        schools = _controller.getCheckedSchools(request);
        assertEquals(schools.size(), 3);
        assertEquals(State.AK, schools.get(0).getDatabaseState());
        assertEquals(new Integer(508), schools.get(0).getId());
        assertEquals(State.AK, schools.get(1).getDatabaseState());
        assertEquals(new Integer(510), schools.get(1).getId());
        assertEquals(State.AK, schools.get(2).getDatabaseState());
        assertEquals(new Integer(509), schools.get(2).getId());

        request.setParameter("cmp", "XX,XY100,AK508,XYzzz,AKzz,AK");
        schools = _controller.getCheckedSchools(request);
        assertEquals(schools.size(), 1);
        assertEquals(State.AK, schools.get(0).getDatabaseState());
        assertEquals(new Integer(508), schools.get(0).getId());

        request.setParameter("cmp", "");
        schools = _controller.getCheckedSchools(request);
        assertEquals(schools.size(), 0);

        request.setParameter("cmp", ",,");
        schools = _controller.getCheckedSchools(request);
        assertEquals(schools.size(), 0);

    }

    public void testCityBrowseModel() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/alaska/anchorage/public/schools/");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        assertNotNull("Expected uri root", model.get(SchoolsController.MODEL_CITY_BROWSE_URI_ROOT));
        assertNotNull("Expected uri level label", model.get(SchoolsController.MODEL_CITY_BROWSE_URI_LEVEL_LABEL));
        assertNotNull("Expected uri", model.get(SchoolsController.MODEL_CITY_BROWSE_URI));
        assertEquals("Expected city id", "133917", model.get(SchoolsController.MODEL_CITY_ID).toString());
    }

    public void testDistrictBrowseRedirect() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/schools.page");
        request.setParameter(SchoolsController.PARAM_DISTRICT, "10");
        request.setParameter("state", "ak");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());
        assertTrue("ModelAndView should be a RedirectView301", mav.getView() instanceof RedirectView301);
        assertEquals("Incorrect redirect url", "/alaska/tok/Alaska-Gateway-School-District/", ((RedirectView) mav.getView()).getUrl());
    }

    public void testDistrictBrowseModel() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/california/alameda/Alameda-City-Unified/schools/");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());
        Map model = mav.getModel();
        assertEquals("Expected city id", "135457", model.get(SchoolsController.MODEL_CITY_ID).toString());
        assertEquals("Expected city display name", "Alameda", model.get(SchoolsController.MODEL_CITY_DISPLAY_NAME).toString());
    }

    public void testCreateNewCityBrowseQueryString() throws Exception {
        GsMockHttpServletRequest request = getRequest();

        // no params

        String expectedQueryString = "";
        assertEquals(expectedQueryString, SchoolsController.createNewCityBrowseQueryString(request));

        // individual params

        request.setParameter(SchoolsController.PARAM_PAGE, "1");
        expectedQueryString = SchoolsController.PARAM_PAGE + "=1";
        assertEquals(expectedQueryString, SchoolsController.createNewCityBrowseQueryString(request));

        request.removeParameter(SchoolsController.PARAM_PAGE);
        request.setParameter(SchoolsController.PARAM_RESULTS_PER_PAGE, "10");
        expectedQueryString = SchoolsController.PARAM_RESULTS_PER_PAGE + "=10";
        assertEquals(expectedQueryString, SchoolsController.createNewCityBrowseQueryString(request));

        request.removeParameter(SchoolsController.PARAM_PAGE);
        request.removeParameter(SchoolsController.PARAM_RESULTS_PER_PAGE);
        request.setParameter(SchoolsController.PARAM_SHOW_ALL, "true");
        expectedQueryString = SchoolsController.PARAM_SHOW_ALL + "=true";
        assertEquals(expectedQueryString, SchoolsController.createNewCityBrowseQueryString(request));

        request.removeParameter(SchoolsController.PARAM_PAGE);
        request.removeParameter(SchoolsController.PARAM_RESULTS_PER_PAGE);
        request.removeParameter(SchoolsController.PARAM_SHOW_ALL);
        request.setParameter(SchoolsController.PARAM_SORT_COLUMN, "schoolResultsHeader");
        expectedQueryString = SchoolsController.PARAM_SORT_COLUMN + "=schoolResultsHeader";
        assertEquals(expectedQueryString, SchoolsController.createNewCityBrowseQueryString(request));

        request.removeParameter(SchoolsController.PARAM_PAGE);
        request.removeParameter(SchoolsController.PARAM_RESULTS_PER_PAGE);
        request.removeParameter(SchoolsController.PARAM_SHOW_ALL);
        request.removeParameter(SchoolsController.PARAM_SORT_COLUMN);
        request.setParameter(SchoolsController.PARAM_SORT_DIRECTION, "asc");
        expectedQueryString = SchoolsController.PARAM_SORT_DIRECTION + "=asc";
        assertEquals(expectedQueryString, SchoolsController.createNewCityBrowseQueryString(request));

        // multiple params
        request.setParameter(SchoolsController.PARAM_PAGE, "1");
        request.setParameter(SchoolsController.PARAM_RESULTS_PER_PAGE, "10");
        request.setParameter(SchoolsController.PARAM_SHOW_ALL, "true");
        request.setParameter(SchoolsController.PARAM_SORT_COLUMN, "schoolResultsHeader");
        request.setParameter(SchoolsController.PARAM_SORT_DIRECTION, "asc");
        expectedQueryString = SchoolsController.PARAM_PAGE + "=1" +
                "&" + SchoolsController.PARAM_RESULTS_PER_PAGE + "=10" +
                "&" + SchoolsController.PARAM_SHOW_ALL + "=true" +
                "&" + SchoolsController.PARAM_SORT_COLUMN + "=schoolResultsHeader" +
                "&" + SchoolsController.PARAM_SORT_DIRECTION + "=asc";
        assertEquals(expectedQueryString, SchoolsController.createNewCityBrowseQueryString(request));

    }

    public void testShowAllAndCrawlers() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/alaska/anchorage/schools/");
        request.setParameter(SchoolsController.PARAM_SHOW_ALL, "true");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);
        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());
        Map modelResults = (Map) mav.getModel().get("results");
        assertEquals(102, ((List) modelResults.get(SchoolsController.MODEL_SCHOOLS)).size());
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_TOTAL));

        // Switch it back to paging
        request.setParameter(SchoolsController.PARAM_SHOW_ALL, (String) null);
        mav = _controller.handleRequestInternal(request, getResponse());
        modelResults = (Map) mav.getModel().get("results");
        assertEquals(25, ((List) modelResults.get(SchoolsController.MODEL_SCHOOLS)).size());
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_TOTAL));
    }

    public void testByCity() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/california/alameda/schools/");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        Map modelResults = (Map) model.get("results");

        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(25), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_TOTAL));
        assertTrue((Boolean)model.get(SchoolsController.MODEL_IS_CITY_BROWSE));
        assertFalse((Boolean)model.get(SchoolsController.MODEL_IS_DISTRICT_BROWSE));
        List<SchoolSearchResult> list = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (SchoolSearchResult schoolResult : list) {
            School s = schoolResult.getSchool();
            assertEquals("Wrong city for " + s, "Alameda", s.getPhysicalAddress().getCity());
        }

        // Check for other important stuff
        assertEquals("1", model.get(SchoolsController.MODEL_PAGE));
        assertEquals("Alameda", model.get(SchoolsController.MODEL_CITY_NAME));
        assertEquals("135457", model.get(SchoolsController.MODEL_CITY_ID).toString());
        assertEquals(null, model.get(SchoolsController.MODEL_LEVEL_CODE));
        assertEquals(null, model.get(SchoolsController.MODEL_SCHOOL_TYPE));
    }

    public void testBadDistrict() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/schools.page");
        request.setParameter("state", "CA");
        request.setParameter("district", "987654321");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        BindException errors = null;

        errors = (BindException) model.get("errors");
        assertNotNull(errors);
        assertEquals(Integer.valueOf("1"), new Integer(errors.getAllErrors().size()));
        assertEquals("error_no_district", errors.getGlobalError().getCode());

        Boolean showSearchControl = (Boolean) model.get("showSearchControl");
        assertTrue(showSearchControl.booleanValue());

        String title = (String) model.get("title");
        assertNotNull(title);
        assertEquals(title, "District not found");

        assertEquals("status/error", mav.getViewName());

    }

    public void testByDistrict() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/california/alameda/Alameda-City-Unified/schools/");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        Map modelResults = (Map) model.get("results");

        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(25), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_TOTAL));
        assertTrue((Boolean)model.get(SchoolsController.MODEL_IS_DISTRICT_BROWSE));
        assertFalse((Boolean)model.get(SchoolsController.MODEL_IS_CITY_BROWSE));
        List<SchoolSearchResult> list = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (SchoolSearchResult schoolResult : list) {
            School s = schoolResult.getSchool();
            assertEquals("Wrong district for " + s, "Alameda", s.getPhysicalAddress().getCity());
        }

        // Check for other important stuff
        assertEquals("1", model.get(SchoolsController.MODEL_PAGE));
        assertEquals("Alameda City Unified", model.get(SchoolsController.MODEL_DISTNAME));
        assertEquals("135457", model.get(SchoolsController.MODEL_CITY_ID).toString());
        assertEquals(null, model.get(SchoolsController.MODEL_LEVEL_CODE));
        assertEquals(null, model.get(SchoolsController.MODEL_SCHOOL_TYPE));
    }

    public void testDistrictShouldBeInModel() throws Exception {
        District district = new District();
        district.setId(1);
        district.setName("Some District Name");
        district.setDatabaseState(State.CA);
        Address address = new Address();
        address.setCity("Alameda");
        district.setPhysicalAddress(address);

        IDistrictDao districtDao = createMock(IDistrictDao.class);
        expect(districtDao.findDistrictByNameAndCity( State.CA, "Some District Name", "alameda")).andReturn(district);
        replay(districtDao);
        _controller.setDistrictDao(districtDao);

        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/california/alameda/Some-District-Name/schools/");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        assertEquals("Expected district object", district, model.get(SchoolsController.MODEL_DISTRICT_OBJECT));
    }

    public void testPaging() throws Exception {
        String testPageUrl = "/alaska/anchorage/schools/";
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI(testPageUrl);
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        Map modelResults = (Map) model.get("results");

        assertEquals(102, modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(25, modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(102, modelResults.get(SchoolsController.MODEL_TOTAL));
        List<SchoolSearchResult> p1schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        assertEquals(25, p1schools.size());
        for (SchoolSearchResult schoolResult : p1schools) {
            School s = schoolResult.getSchool();
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
        }

        // Check for other important stuff
        assertEquals("1", model.get(SchoolsController.MODEL_PAGE));
        assertEquals("Anchorage", model.get(SchoolsController.MODEL_CITY_NAME));
        assertEquals(null, model.get(SchoolsController.MODEL_LEVEL_CODE));
        assertEquals(null, model.get(SchoolsController.MODEL_SCHOOL_TYPE));

        // OK, now go to page #2
        request.setParameter("p", "2");
        mav = _controller.handleRequestInternal(request, getResponse());

        model = mav.getModel();
        modelResults = (Map) model.get("results");

        assertEquals(102, modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(25, modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(102, modelResults.get(SchoolsController.MODEL_TOTAL));
        List<SchoolSearchResult> p2schools = (List<SchoolSearchResult>) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        assertEquals(25, p2schools.size());
        for (SchoolSearchResult schoolResult : p2schools) {
            School s = schoolResult.getSchool();
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
            assertTrue("Found the same school on two pages", p1schools.indexOf(schoolResult) == -1);
        }

        // OK, jump to page #4
        request.setParameter("p", "4");
        mav = _controller.handleRequestInternal(request, getResponse());

        model = mav.getModel();
        modelResults = (Map) model.get("results");

        assertEquals(102, modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(25, modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(102, modelResults.get(SchoolsController.MODEL_TOTAL));
        List<SchoolSearchResult> p4schools = (List<SchoolSearchResult>) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        assertEquals(25, p4schools.size());
        for (SchoolSearchResult schoolResult : p4schools) {
            School s = schoolResult.getSchool();
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
            assertTrue("Found the same school on two pages", p1schools.indexOf(schoolResult) == -1);
            assertTrue("Found the same school on two pages", p2schools.indexOf(schoolResult) == -1);
        }

        // Check page #5
        request.setParameter("p", "5");
        mav = _controller.handleRequestInternal(request, getResponse());

        model = mav.getModel();
        modelResults = (Map) model.get("results");

        assertEquals(102, modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(25, modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(102, modelResults.get(SchoolsController.MODEL_TOTAL));
        List<SchoolSearchResult> p5schools = (List<SchoolSearchResult>) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        assertEquals(2, p5schools.size());
        for (SchoolSearchResult schoolResult : p5schools) {
            School s = schoolResult.getSchool();
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
            assertTrue("Found the same school on two pages", p1schools.indexOf(schoolResult) == -1);
            assertTrue("Found the same school on two pages", p2schools.indexOf(schoolResult) == -1);
            assertTrue("Found the same school on two pages", p4schools.indexOf(schoolResult) == -1);
        }

        // Check page #6 redirects
        request.setParameter("p", "6");
        mav = _controller.handleRequestInternal(request, getResponse());

        assertNotNull("ModelAndView should not be null", mav);
        assertTrue("ModelAndView should be a 301 redirect", mav.getView() instanceof RedirectView301);
        assertEquals("Redirect view should be canonical url", testPageUrl, ((RedirectView301) mav.getView()).getUrl());

        // Check page after the last exact multiple of pageSize redirects (GS-10794)
        request.setParameter("p", "3");
        request.setParameter("pageSize", "51");
        mav = _controller.handleRequestInternal(request, getResponse());

        assertNotNull("ModelAndView should not be null", mav);
        assertTrue("ModelAndView should be a 301 redirect", mav.getView() instanceof RedirectView301);
        assertEquals("Redirect view should be canonical url", testPageUrl, ((RedirectView301) mav.getView()).getUrl());

    }
    public void testEmptyResultSet() throws Exception {
        String testPageUrl = "/california/oakland/elementary-schools/";
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI(testPageUrl);
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        Map modelResults = (Map) model.get("results");

        assertEquals("Expected no results", 0, modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(25, modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals("Expected no results", 0, modelResults.get(SchoolsController.MODEL_TOTAL));
    }

    public void testFilteringLevelCode() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/alaska/anchorage/elementary-schools/");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        Map modelResults = (Map) model.get("results");

        assertEquals(new Integer(77), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(25), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(new Integer(77), modelResults.get(SchoolsController.MODEL_TOTAL));
        List<SchoolSearchResult> p1schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (SchoolSearchResult schoolResult : p1schools) {
            School s = schoolResult.getSchool();
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
            assertTrue("Wrong level included", s.getLevelCode().containsLevelCode(LevelCode.Level.ELEMENTARY_LEVEL));
        }

        // Check for other important stuff
        assertEquals("1", model.get(SchoolsController.MODEL_PAGE));
        assertEquals("Anchorage", model.get(SchoolsController.MODEL_CITY_NAME));
        assertEquals(LevelCode.createLevelCode("e"), model.get(SchoolsController.MODEL_LEVEL_CODE));
        assertEquals(null, model.get(SchoolsController.MODEL_SCHOOL_TYPE));

        // Look for all the middle schools
        request.setRequestURI("/alaska/anchorage/middle-schools/");
        fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);
        mav = _controller.handleRequestInternal(request, getResponse());

        model = mav.getModel();
        modelResults = (Map) model.get("results");

        assertEquals(new Integer(32), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(25), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(new Integer(32), modelResults.get(SchoolsController.MODEL_TOTAL));
        p1schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (SchoolSearchResult schoolResult : p1schools) {
            School s = schoolResult.getSchool();
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
            assertTrue("Wrong level included", s.getLevelCode().containsLevelCode(LevelCode.Level.MIDDLE_LEVEL));
        }

        // Check for other important stuff
        assertEquals("1", model.get(SchoolsController.MODEL_PAGE));
        assertEquals("Anchorage", model.get(SchoolsController.MODEL_CITY_NAME));
        assertEquals(LevelCode.createLevelCode("m"), model.get(SchoolsController.MODEL_LEVEL_CODE));
        assertEquals(null, model.get(SchoolsController.MODEL_SCHOOL_TYPE));


    }

    public void testFilteringSchoolType() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/alaska/anchorage/public/schools/");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        Map modelResults = (Map) model.get("results");

        assertEquals(new Integer(74), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(25), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(new Integer(74), modelResults.get(SchoolsController.MODEL_TOTAL));
        List<SchoolSearchResult> p1schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (SchoolSearchResult schoolResult : p1schools) {
            School s = schoolResult.getSchool();
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
            assertTrue("Wrong type", s.getType().equals(SchoolType.PUBLIC));
        }

        // Check for other important stuff
        assertEquals("1", model.get(SchoolsController.MODEL_PAGE));
        assertEquals("Anchorage", model.get(SchoolsController.MODEL_CITY_NAME));
        assertEquals("public", ((String[]) (model.get(SchoolsController.MODEL_SCHOOL_TYPE)))[0]);

        // Look for all the private schools
        request.setRequestURI("/alaska/anchorage/private/schools/");
        fields = new DirectoryStructureUrlFields(_request);
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);
        mav = _controller.handleRequestInternal(request, getResponse());

        model = mav.getModel();
        modelResults = (Map) model.get("results");

        assertEquals(new Integer(25), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(25), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(new Integer(25), modelResults.get(SchoolsController.MODEL_TOTAL));
        p1schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (SchoolSearchResult schoolResult : p1schools) {
            School s = schoolResult.getSchool();
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
            assertTrue("Wrong type", s.getType().equals(SchoolType.PRIVATE));
        }

        // Check for other important stuff
        assertEquals("1", model.get(SchoolsController.MODEL_PAGE));
        assertEquals("Anchorage", model.get(SchoolsController.MODEL_CITY_NAME));
        assertEquals("private", ((String[]) (model.get(SchoolsController.MODEL_SCHOOL_TYPE)))[0]);


    }

    public void testTitleCalcCode() {
        // These all have standard headers
        assertEquals("San Francisco Schools - San Francisco, CA | GreatSchools", SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, null, null));
        assertEquals("San Francisco Schools - San Francisco, CA | GreatSchools", SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.ELEMENTARY_MIDDLE, null));
        assertEquals("San Francisco Schools - San Francisco, CA | GreatSchools", SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.MIDDLE_HIGH, null));

        // These useful views get nice SEO friendly titles
        assertEquals("San Francisco Elementary Schools - San Francisco, CA | GreatSchools", SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.ELEMENTARY, null));
        assertEquals("San Francisco Middle Schools - San Francisco, CA | GreatSchools", SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.MIDDLE, null));
        assertEquals("San Francisco High Schools - San Francisco, CA | GreatSchools", SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.HIGH, null));
        assertEquals("San Francisco Preschools and Daycare Centers - San Francisco, CA | GreatSchools", SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.PRESCHOOL, null));

        assertEquals("San Francisco Public Schools - San Francisco, CA | GreatSchools", SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, null, new String[]{"public"}));
        assertEquals("San Francisco Private Schools - San Francisco, CA | GreatSchools", SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, null, new String[]{"private"}));
        assertEquals("San Francisco Public Charter Schools - San Francisco, CA | GreatSchools", SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, null, new String[]{"charter"}));

        assertEquals("San Francisco Public and Private Schools - San Francisco, CA | GreatSchools",
                     SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, null, new String[]{"public", "private"}));
        assertEquals("San Francisco Public and Public Charter Schools - San Francisco, CA | GreatSchools",
                     SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, null, new String[]{"public", "charter"}));
        assertEquals("San Francisco Private and Public Charter Schools - San Francisco, CA | GreatSchools", 
                     SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, null, new String[]{"private", "charter"}));

        assertEquals("San Francisco Public and Private Elementary Schools - San Francisco, CA | GreatSchools",
                     SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.ELEMENTARY, new String[]{"public", "private"}));
        assertEquals("San Francisco Public and Public Charter Middle Schools - San Francisco, CA | GreatSchools",
                     SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.MIDDLE, new String[]{"public", "charter"}));
        assertEquals("San Francisco Private and Public Charter High Schools - San Francisco, CA | GreatSchools",
                     SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.HIGH, new String[]{"private", "charter"}));
        assertEquals("San Francisco Private Preschools and Daycare Centers - San Francisco, CA | GreatSchools", 
                     SchoolsController.calcCitySchoolsTitle("San Francisco", State.CA, LevelCode.PRESCHOOL, new String[]{"private"}));
    }

    public void testMetaDescCalc() {
        assertEquals("View and map all San Francisco schools. Plus, compare or save schools.",
                SchoolsController.calcMetaDesc(null, "San Francisco", null, null));
        assertEquals("View and map all San Francisco middle schools. Plus, compare or save middle schools.",
                SchoolsController.calcMetaDesc(null, "San Francisco", LevelCode.MIDDLE, null));
        assertEquals("View and map all San Francisco public elementary schools. Plus, compare or save public elementary schools.",
                SchoolsController.calcMetaDesc(null, "San Francisco", LevelCode.ELEMENTARY, new String[]{"public"}));

        assertEquals("Find the best preschools in San Francisco, California (CA) - view preschool ratings, reviews and map locations.",
                SchoolsController.calcMetaDesc(null, "San Francisco", State.CA, LevelCode.PRESCHOOL, null));

        assertEquals("View and map all schools in the Oakland Unified School District. Plus, compare or save schools in this district.",
                SchoolsController.calcMetaDesc("Oakland Unified School District", "Oakland", null, null));
        assertEquals("View and map all middle schools in the Oakland Unified School District. Plus, compare or save middle schools in this district.",
                SchoolsController.calcMetaDesc("Oakland Unified School District", "Oakland", LevelCode.MIDDLE, null));
        assertEquals("View and map all public elementary schools in the Oakland Unified School District. Plus, compare or save public elementary schools in this district.",
                SchoolsController.calcMetaDesc("Oakland Unified School District", "Oakland", LevelCode.ELEMENTARY, new String[]{"public"}));
    }

}
