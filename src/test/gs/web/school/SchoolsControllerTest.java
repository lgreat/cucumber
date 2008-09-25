/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolsControllerTest.java,v 1.41 2008/09/25 00:33:37 yfan Exp $
 */

package gs.web.school;

import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.search.Searcher;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.search.SchoolSearchResult;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.DirectoryStructureUrlFactory;
import static org.easymock.EasyMock.*;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;

/**
 * Tests SchoolsController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
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

        _sessionContextUtil = (SessionContextUtil) getApplicationContext().getBean(SessionContextUtil.BEAN_ID);
    }

    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================


    public void testHandleRequestInternalRedirects() throws Exception {
        GsMockHttpServletRequest request = getRequest();

        // old-style to new-style city browse urls
        request.setRequestURI("/schools.page");
        request.setParameter(SchoolsController.PARAM_CITY, "Anchorage");
        request.setParameter("state", "AK");
        request.setQueryString("city=Anchorage&state=AK");
        _sessionContextUtil.updateStateFromParam(getSessionContext(), request, getResponse());
        ModelAndView mAndV = _controller.handleRequestInternal(request, getResponse());
        assertTrue(mAndV.getView() instanceof RedirectView);
        assertEquals("/alaska/anchorage/schools/", ((RedirectView) mAndV.getView()).getUrl());

        // adding trailing schools label
        request.removeAllParameters();
        request.setQueryString(null);
        request.setRequestURI("/alaska/anchorage/");
        mAndV = _controller.handleRequestInternal(request, getResponse());
        assertTrue(mAndV.getView() instanceof RedirectView);
        assertEquals("/alaska/anchorage/schools/", ((RedirectView) mAndV.getView()).getUrl());
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

    public void testIsRequestURIWithTrailingSchoolsLabel() throws Exception {
        GsMockHttpServletRequest request = getRequest();

        request.setRequestURI(null);
        boolean foundException = false;
        try {
            SchoolsController.isRequestURIWithTrailingSchoolsLabel(request);
        } catch (IllegalArgumentException e) {
            foundException = true;
        }
        assertTrue("Expected IllegalArgumentException when request URI null", foundException);

        request.setRequestURI("/california/san-francisco/");
        assertFalse("Expected false return value", SchoolsController.isRequestURIWithTrailingSchoolsLabel(request));

        request.setRequestURI("/california/san-francisco/private/");
        assertFalse("Expected false return value", SchoolsController.isRequestURIWithTrailingSchoolsLabel(request));

        request.setRequestURI("/california/san-francisco/public/preschools/");
        assertTrue("Expected true return value", SchoolsController.isRequestURIWithTrailingSchoolsLabel(request));

        request.setRequestURI("/california/san-francisco/elementary-schools/");
        assertTrue("Expected true return value", SchoolsController.isRequestURIWithTrailingSchoolsLabel(request));
    }

    public void testCreateRequestURIWithTrailingSchoolsLabel() throws Exception {
        GsMockHttpServletRequest request = getRequest();

        request.setRequestURI(null);
        boolean foundException = false;
        try {
            SchoolsController.isRequestURIWithTrailingSchoolsLabel(request);
        } catch (IllegalArgumentException e) {
            foundException = true;
        }
        assertTrue("Expected IllegalArgumentException when request URI null", foundException);

        request.setRequestURI("/california/san-francisco/");
        assertEquals("Expected appended schools label", "/california/san-francisco/schools/", SchoolsController.createURIWithTrailingSchoolsLabel(request));

        request.setRequestURI("/california/san-francisco/private/");
        assertEquals("Expected appended schools label", "/california/san-francisco/private/schools/", SchoolsController.createURIWithTrailingSchoolsLabel(request));

        request.setRequestURI("/california/san-francisco");
        assertEquals("Expected appended schools label", "/california/san-francisco/schools/", SchoolsController.createURIWithTrailingSchoolsLabel(request));

        request.setRequestURI("/california/san-francisco/private");
        assertEquals("Expected appended schools label", "/california/san-francisco/private/schools/", SchoolsController.createURIWithTrailingSchoolsLabel(request));

        request.setRequestURI("/california/san-francisco/elementary-schools/");
        assertEquals("Expected unmodified uri", "/california/san-francisco/elementary-schools/", SchoolsController.createURIWithTrailingSchoolsLabel(request));

        request.setRequestURI("/california/san-francisco/public/preschools/");
        assertEquals("Expected unmodified uri", "/california/san-francisco/public/preschools/", SchoolsController.createURIWithTrailingSchoolsLabel(request));
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

    public void testIsValidNewStyleCityBrowseRequest() throws Exception {
        GsMockHttpServletRequest request = getRequest();

        request.setRequestURI(null);
        boolean foundException = false;
        try {
            SchoolsController.isValidNewStyleCityBrowseRequest(request);
        } catch (IllegalArgumentException e) {
            foundException = true;
        }
        assertTrue("Expected IllegalArgumentException when request URI null", foundException);

        request.setRequestURI("/schools.page");
        assertFalse("Expected request considered invalid", SchoolsController.isValidNewStyleCityBrowseRequest(request));

        request.setRequestURI("/california/san-francisco/");
        assertFalse("Expected request considered invalid", SchoolsController.isValidNewStyleCityBrowseRequest(request));

        request.setRequestURI("/california/san-francisco/schools");
        assertFalse("Expected request considered invalid", SchoolsController.isValidNewStyleCityBrowseRequest(request));

        request.setRequestURI("/california/san-francisco/schools/");
        assertTrue("Expected request considered valid", SchoolsController.isValidNewStyleCityBrowseRequest(request));

        request.setRequestURI("/california/san-francisco/public/schools/");
        assertTrue("Expected request considered valid", SchoolsController.isValidNewStyleCityBrowseRequest(request));

        request.setRequestURI("/california/san-francisco/public-private/schools/");
        assertTrue("Expected request considered valid", SchoolsController.isValidNewStyleCityBrowseRequest(request));

        request.setRequestURI("/california/san-francisco/private-charter/elementary-schools/");
        assertTrue("Expected request considered valid", SchoolsController.isValidNewStyleCityBrowseRequest(request));

        request.setRequestURI("/california/san-francisco/private-charter/elementary-schools");
        assertFalse("Expected request considered invalid", SchoolsController.isValidNewStyleCityBrowseRequest(request));

        request.setRequestURI("/california/san-francisco/preschools/");
        assertTrue("Expected request considered valid", SchoolsController.isValidNewStyleCityBrowseRequest(request));

        request.setRequestURI("//san-francisco/preschools/");
        assertFalse("Expected request considered invalid", SchoolsController.isValidNewStyleCityBrowseRequest(request));

        request.setRequestURI("/california//preschools/");
        assertFalse("Expected request considered invalid", SchoolsController.isValidNewStyleCityBrowseRequest(request));

        request.setRequestURI("/california/san-francisco//");
        assertFalse("Expected request considered invalid", SchoolsController.isValidNewStyleCityBrowseRequest(request));
    }

    public void testGetFieldsFromNewStyleCityBrowseRequest() throws Exception {
        GsMockHttpServletRequest request = getRequest();

        request.setRequestURI(null);
        boolean foundException = false;
        try {
            SchoolsController.getFieldsFromNewStyleCityBrowseRequest(request);
        } catch (IllegalArgumentException e) {
            foundException = true;
        }
        assertTrue("Expected IllegalArgumentException when request URI invalid", foundException);

        Set<SchoolType> schoolTypeSet = new HashSet<SchoolType>();

        request.setRequestURI("/california/san-francisco/schools/");
        SchoolsController.CityBrowseFields fields = SchoolsController.getFieldsFromNewStyleCityBrowseRequest(request);
        assertEquals("Expected city name 'san francisco'", "san francisco", fields.getCityName());
        assertNull("Expected school type null", fields.getSchoolType());
        schoolTypeSet.clear();
        assertEquals("Expected school type set to be empty", schoolTypeSet, fields.getSchoolTypeSet());
        assertNull("Expected level code null", fields.getLevelCode());

        request.setRequestURI("/california/cardiff_by_the_sea/schools/");
        fields = SchoolsController.getFieldsFromNewStyleCityBrowseRequest(request);
        assertEquals("Expected city name 'cardiff-by-the-sea'", "cardiff-by-the-sea", fields.getCityName());
        assertNull("Expected school type null", fields.getSchoolType());
        schoolTypeSet.clear();
        assertEquals("Expected school type set to be empty", schoolTypeSet, fields.getSchoolTypeSet());
        assertNull("Expected level code null", fields.getLevelCode());

        request.setRequestURI("/california/san-francisco/public/schools/");
        fields = SchoolsController.getFieldsFromNewStyleCityBrowseRequest(request);
        assertEquals("Expected city name 'san francisco'", "san francisco", fields.getCityName());
        assertTrue("Expected school type array to contain public",
                Arrays.deepEquals(new String[]{SchoolType.PUBLIC.getSchoolTypeName()},
                        fields.getSchoolType()));
        schoolTypeSet.clear();
        schoolTypeSet.add(SchoolType.PUBLIC);
        assertEquals("Expected school type set to contain public", schoolTypeSet, fields.getSchoolTypeSet());
        assertNull("Expected level code null", fields.getLevelCode());

        request.setRequestURI("/california/san-francisco/public-private/schools/");
        fields = SchoolsController.getFieldsFromNewStyleCityBrowseRequest(request);
        assertEquals("Expected city name 'san francisco'", "san francisco", fields.getCityName());
        assertTrue("Expected school type array to contain public and private",
                Arrays.deepEquals(new String[]{SchoolType.PUBLIC.getSchoolTypeName(), SchoolType.PRIVATE.getSchoolTypeName()},
                        fields.getSchoolType()));
        schoolTypeSet.clear();
        schoolTypeSet.add(SchoolType.PUBLIC);
        schoolTypeSet.add(SchoolType.PRIVATE);
        assertEquals("Expected school type set to contain public and private", schoolTypeSet, fields.getSchoolTypeSet());
        assertNull("Expected level code null", fields.getLevelCode());

        request.setRequestURI("/california/san-francisco/private-charter/elementary-schools/");
        fields = SchoolsController.getFieldsFromNewStyleCityBrowseRequest(request);
        assertEquals("Expected city name 'san francisco'", "san francisco", fields.getCityName());
        assertTrue("Expected school type array to contain private and charter",
                Arrays.deepEquals(new String[]{SchoolType.PRIVATE.getSchoolTypeName(), SchoolType.CHARTER.getSchoolTypeName()},
                        fields.getSchoolType()));
        schoolTypeSet.clear();
        schoolTypeSet.add(SchoolType.PRIVATE);
        schoolTypeSet.add(SchoolType.CHARTER);
        assertEquals("Expected school type set to contain private and charter", schoolTypeSet, fields.getSchoolTypeSet());
        assertEquals("Expected level code elementary", LevelCode.ELEMENTARY, fields.getLevelCode());

        request.setRequestURI("/california/san-francisco/preschools/");
        fields = SchoolsController.getFieldsFromNewStyleCityBrowseRequest(request);
        assertEquals("Expected city name 'san francisco'", "san francisco", fields.getCityName());
        assertNull("Expected school type null", fields.getSchoolType());
        schoolTypeSet.clear();
        assertEquals("Expected school type set to be empty", schoolTypeSet, fields.getSchoolTypeSet());
        assertEquals("Expected level code preschools", LevelCode.PRESCHOOL, fields.getLevelCode());
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
        expectedRedirectURI = "/california/san-francisco/public/schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE, SchoolType.PRIVATE.getSchoolTypeName());
        expectedRedirectURI = "/california/san-francisco/private/schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE, SchoolType.CHARTER.getSchoolTypeName());
        expectedRedirectURI = "/california/san-francisco/charter/schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE,
                new String[]{SchoolType.PUBLIC.getSchoolTypeName(), SchoolType.CHARTER.getSchoolTypeName()});
        expectedRedirectURI = "/california/san-francisco/public-charter/schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE,
                new String[]{SchoolType.PRIVATE.getSchoolTypeName(), SchoolType.CHARTER.getSchoolTypeName()});
        expectedRedirectURI = "/california/san-francisco/private-charter/schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE,
                new String[]{SchoolType.PUBLIC.getSchoolTypeName(), SchoolType.PRIVATE.getSchoolTypeName()});
        expectedRedirectURI = "/california/san-francisco/public-private/schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE,
                new String[]{SchoolType.PUBLIC.getSchoolTypeName(), SchoolType.PRIVATE.getSchoolTypeName(), SchoolType.CHARTER.getSchoolTypeName()});
        expectedRedirectURI = "/california/san-francisco/schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        // level filters

        request.removeParameter(SchoolsController.PARAM_SCHOOL_TYPE);

        request.setParameter(SchoolsController.PARAM_LEVEL_CODE, LevelCode.PRESCHOOL.getLowestLevel().getName());
        expectedRedirectURI = "/california/san-francisco/preschools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_LEVEL_CODE, LevelCode.ELEMENTARY.getLowestLevel().getName());
        expectedRedirectURI = "/california/san-francisco/elementary-schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_LEVEL_CODE, LevelCode.MIDDLE.getLowestLevel().getName());
        expectedRedirectURI = "/california/san-francisco/middle-schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.setParameter(SchoolsController.PARAM_LEVEL_CODE, LevelCode.HIGH.getLowestLevel().getName());
        expectedRedirectURI = "/california/san-francisco/high-schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        // multiple level codes is interpreted as no level filter
        request.setParameter(SchoolsController.PARAM_LEVEL_CODE,
                new String[]{LevelCode.MIDDLE.getLowestLevel().getName(), LevelCode.HIGH.getLowestLevel().getName()});
        expectedRedirectURI = "/california/san-francisco/schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        // combined filters

        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE,
                new String[]{SchoolType.PUBLIC.getSchoolTypeName(), SchoolType.CHARTER.getSchoolTypeName()});
        request.removeParameter(SchoolsController.PARAM_LEVEL_CODE);
        request.setParameter(SchoolsController.PARAM_LEVEL_CODE, LevelCode.ELEMENTARY.getLowestLevel().getName());
        expectedRedirectURI = "/california/san-francisco/public-charter/elementary-schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        request.removeParameter(SchoolsController.PARAM_SCHOOL_TYPE);
        request.setParameter(SchoolsController.PARAM_SCHOOL_TYPE, SchoolType.CHARTER.getSchoolTypeName());
        request.removeParameter(SchoolsController.PARAM_LEVEL_CODE);
        request.setParameter(SchoolsController.PARAM_LEVEL_CODE, LevelCode.MIDDLE.getLowestLevel().getName());
        expectedRedirectURI = "/california/san-francisco/charter/middle-schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));

        // hyphenated city
        request.removeParameter(SchoolsController.PARAM_SCHOOL_TYPE);
        request.removeParameter(SchoolsController.PARAM_LEVEL_CODE);
        request.setParameter(SchoolsController.PARAM_CITY, "Cardiff-By-The-Sea");
        expectedRedirectURI = "/california/cardiff_by_the_sea/schools/";
        assertEquals(expectedRedirectURI, SchoolsController.createNewCityBrowseURI(request));
    }

    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    // ===========================================================================================================
    

    public void testCityBrowseModel() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/alaska/anchorage/public/schools/");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        assertNotNull("Expected uri root", model.get(SchoolsController.MODEL_CITY_BROWSE_URI_ROOT));
        assertNotNull("Expected uri level label", model.get(SchoolsController.MODEL_CITY_BROWSE_URI_LEVEL_LABEL));
        assertNotNull("Expected uri", model.get(SchoolsController.MODEL_CITY_BROWSE_URI));
        assertEquals("Expected city id", "133917", model.get(SchoolsController.MODEL_CITY_ID).toString());
    }

    public void testDistrictBrowseModel() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/schools.page");
        request.setParameter(SchoolsController.PARAM_DISTRICT, "10");
        request.setParameter("state", "ak");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());
        Map model = mav.getModel();
        assertEquals("Expected city id", "133981", model.get(SchoolsController.MODEL_CITY_ID).toString());
        assertEquals("Expected city display name", "Tok", model.get(SchoolsController.MODEL_CITY_DISPLAY_NAME).toString());
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
        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());
        Map modelResults = (Map) mav.getModel().get("results");
        assertEquals(102, ((List) modelResults.get(SchoolsController.MODEL_SCHOOLS)).size());
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_TOTAL));

        // Switch it back to paging
        request.setParameter(SchoolsController.PARAM_SHOW_ALL, (String) null);
        mav = _controller.handleRequestInternal(request, getResponse());
        modelResults = (Map) mav.getModel().get("results");
        assertEquals(10, ((List) modelResults.get(SchoolsController.MODEL_SCHOOLS)).size());
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_TOTAL));
    }

    public void testByCity() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/california/alameda/schools/");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        Map modelResults = (Map) model.get("results");

        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
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
        request.setRequestURI("/schools.page");
        request.setParameter("state", "CA");
        request.setParameter("district", "1");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        Map modelResults = (Map) model.get("results");

        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
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
        district.setName("Some District Name");
        IDistrictDao districtDao = createMock(IDistrictDao.class);
        expect(districtDao.findDistrictById(State.CA, new Integer(1))).andReturn(district);
        replay(districtDao);
        _controller.setDistrictDao(districtDao);

        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/schools.page");
        request.setParameter("state", "CA");
        request.setParameter("district", "1");

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        assertEquals("Expected district object", district, model.get(SchoolsController.MODEL_DISTRICT_OBJECT));
    }

    public void testPaging() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/alaska/anchorage/schools/");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        Map modelResults = (Map) model.get("results");

        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_TOTAL));
        List<SchoolSearchResult> p1schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
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

        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_TOTAL));
        List<SchoolSearchResult> p2schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (SchoolSearchResult schoolResult : p2schools) {
            School s = schoolResult.getSchool();
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
            assertTrue("Found the same school on two pages", p1schools.indexOf(s) == -1);
        }

        // OK, jump to page #10
        request.setParameter("p", "10");
        mav = _controller.handleRequestInternal(request, getResponse());

        model = mav.getModel();
        modelResults = (Map) model.get("results");

        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_TOTAL));
        List<SchoolSearchResult> p10schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (SchoolSearchResult schoolResult : p10schools) {
            School s = schoolResult.getSchool();
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
            assertTrue("Found the same school on two pages", p1schools.indexOf(s) == -1);
            assertTrue("Found the same school on two pages", p2schools.indexOf(s) == -1);
        }

        // Check page #11
        request.setParameter("p", "11");
        mav = _controller.handleRequestInternal(request, getResponse());

        model = mav.getModel();
        modelResults = (Map) model.get("results");

        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_TOTAL));
        List<SchoolSearchResult> p11schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        assertEquals(2, p11schools.size());
        for (SchoolSearchResult schoolResult : p11schools) {
            School s = schoolResult.getSchool();
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
            assertTrue("Found the same school on two pages", p1schools.indexOf(s) == -1);
            assertTrue("Found the same school on two pages", p2schools.indexOf(s) == -1);
            assertTrue("Found the same school on two pages", p10schools.indexOf(s) == -1);
        }


    }

    public void testFilteringLevelCode() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/alaska/anchorage/elementary-schools/");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        Map modelResults = (Map) model.get("results");

        assertEquals(new Integer(77), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
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
        mav = _controller.handleRequestInternal(request, getResponse());

        model = mav.getModel();
        modelResults = (Map) model.get("results");

        assertEquals(new Integer(32), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
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

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        Map modelResults = (Map) model.get("results");

        assertEquals(new Integer(74), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
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
        mav = _controller.handleRequestInternal(request, getResponse());

        model = mav.getModel();
        modelResults = (Map) model.get("results");

        assertEquals(new Integer(25), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
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
        assertEquals("San Francisco Schools", SchoolsController.calcCitySchoolsTitle("San Francisco", null, null));
        assertEquals("San Francisco Schools", SchoolsController.calcCitySchoolsTitle("San Francisco", LevelCode.ELEMENTARY_MIDDLE, null));
        assertEquals("San Francisco Schools", SchoolsController.calcCitySchoolsTitle("San Francisco", LevelCode.MIDDLE_HIGH, null));
        assertEquals("San Francisco Schools", SchoolsController.calcCitySchoolsTitle("San Francisco", null, new String[]{"public", "private"}));
        assertEquals("San Francisco Schools", SchoolsController.calcCitySchoolsTitle("San Francisco", null, new String[]{"public", "charter"}));
        assertEquals("San Francisco Schools", SchoolsController.calcCitySchoolsTitle("San Francisco", null, new String[]{"private", "charter"}));

        // These useful views get nice SEO friendly titles
        assertEquals("San Francisco Elementary Schools", SchoolsController.calcCitySchoolsTitle("San Francisco", LevelCode.ELEMENTARY, null));
        assertEquals("San Francisco Middle Schools", SchoolsController.calcCitySchoolsTitle("San Francisco", LevelCode.MIDDLE, null));
        assertEquals("San Francisco High Schools", SchoolsController.calcCitySchoolsTitle("San Francisco", LevelCode.HIGH, null));
        assertEquals("San Francisco Preschools", SchoolsController.calcCitySchoolsTitle("San Francisco", LevelCode.PRESCHOOL, null));

        assertEquals("San Francisco Public Schools", SchoolsController.calcCitySchoolsTitle("San Francisco", null, new String[]{"public"}));
        assertEquals("San Francisco Private Schools", SchoolsController.calcCitySchoolsTitle("San Francisco", null, new String[]{"private"}));
        assertEquals("San Francisco Charter Schools", SchoolsController.calcCitySchoolsTitle("San Francisco", null, new String[]{"charter"}));
    }

    public void testMetaDescCalc() {
        assertEquals("View and map all San Francisco schools. Plus, compare or save schools.",
                SchoolsController.calcMetaDesc(null, "San Francisco", null, null));
        assertEquals("View and map all San Francisco middle schools. Plus, compare or save middle schools.",
                SchoolsController.calcMetaDesc(null, "San Francisco", LevelCode.MIDDLE, null));
        assertEquals("View and map all San Francisco public elementary schools. Plus, compare or save public elementary schools.",
                SchoolsController.calcMetaDesc(null, "San Francisco", LevelCode.ELEMENTARY, new String[]{"public"}));

        assertEquals("View and map all San Francisco public preschool schools. Plus, compare or save public preschool schools.",
                SchoolsController.calcMetaDesc(null, "San Francisco", LevelCode.PRESCHOOL, new String[]{"public"}));

        assertEquals("View and map all schools in the Oakland Unified School District. Plus, compare or save schools in this district.",
                SchoolsController.calcMetaDesc("Oakland Unified School District", "Oakland", null, null));
        assertEquals("View and map all middle schools in the Oakland Unified School District. Plus, compare or save middle schools in this district.",
                SchoolsController.calcMetaDesc("Oakland Unified School District", "Oakland", LevelCode.MIDDLE, null));
        assertEquals("View and map all public elementary schools in the Oakland Unified School District. Plus, compare or save public elementary schools in this district.",
                SchoolsController.calcMetaDesc("Oakland Unified School District", "Oakland", LevelCode.ELEMENTARY, new String[]{"public"}));
    }

}
