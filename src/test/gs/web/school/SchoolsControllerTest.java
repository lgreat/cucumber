/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolsControllerTest.java,v 1.21 2007/10/03 21:20:43 dlee Exp $
 */

package gs.web.school;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.district.IDistrictDao;
import gs.data.search.Searcher;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.search.SchoolSearchResult;
import gs.web.util.context.SessionContextUtil;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

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
        _controller.setSearcher((Searcher) getApplicationContext().getBean(Searcher.BEAN_ID));

        _sessionContextUtil = (SessionContextUtil) getApplicationContext().getBean(SessionContextUtil.BEAN_ID);
    }

    public void testShowAllAndCrawlers() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "AK");
        request.setParameter("city", "Anchorage");
        request.setParameter(SchoolsController.PARAM_SHOW_ALL, "true");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());
        Map modelResults = (Map) mav.getModel().get("results");
        assertEquals(102, ((List) modelResults.get(SchoolsController.MODEL_SCHOOLS)).size());
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_TOTAL));

        // Switch it back to paging
        request.setParameter(SchoolsController.PARAM_SHOW_ALL, (String)null);
        mav = _controller.handleRequestInternal(request, getResponse());
        modelResults = (Map) mav.getModel().get("results");
        assertEquals(10, ((List) modelResults.get(SchoolsController.MODEL_SCHOOLS)).size());
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_TOTAL));

        // Switch it to a crawler where it should disable paging
        request.addHeader("User-Agent", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        mav = _controller.handleRequestInternal(request, getResponse());
        modelResults = (Map) mav.getModel().get("results");
        assertEquals(100, ((List) modelResults.get(SchoolsController.MODEL_SCHOOLS)).size());
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_TOTAL));
    }

    public void testByCity() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "CA");
        request.setParameter("city", "Alameda");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        Map modelResults = (Map) model.get("results");

        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_TOTAL));
        List<SchoolSearchResult> list = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (SchoolSearchResult schoolResult : list) {
            School s = schoolResult.getSchool();
            assertEquals("Wrong city for " + s, "Alameda", s.getPhysicalAddress().getCity());
        }

        // Check for other important stuff
        assertEquals("1", model.get(SchoolsController.MODEL_PAGE));
        assertEquals("Alameda", model.get(SchoolsController.MODEL_CITY_NAME));
        assertEquals(null, model.get(SchoolsController.MODEL_LEVEL_CODE));
        assertEquals(null, model.get(SchoolsController.MODEL_SCHOOL_TYPE));
    }

    public void testBadDistrict() throws Exception {
        GsMockHttpServletRequest request = getRequest();
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
        request.setParameter("state", "CA");
        request.setParameter("district", "1");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();
        Map modelResults = (Map) model.get("results");

        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_TOTAL));
        List<SchoolSearchResult> list = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (SchoolSearchResult schoolResult : list) {
            School s = schoolResult.getSchool();
            assertEquals("Wrong district for " + s, "Alameda", s.getPhysicalAddress().getCity());
        }

        // Check for other important stuff
        assertEquals("1", model.get(SchoolsController.MODEL_PAGE));
        assertEquals("Alameda City Unified", model.get(SchoolsController.MODEL_DISTNAME));
        assertEquals(null, model.get(SchoolsController.MODEL_LEVEL_CODE));
        assertEquals(null, model.get(SchoolsController.MODEL_SCHOOL_TYPE));

    }

    public void testPaging() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "AK");
        request.setParameter("city", "Anchorage");
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
        request.setParameter("state", "AK");
        request.setParameter("city", "Anchorage");
        request.setParameter("lc", "e");
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

        // Look for all the m/h schools
        request.setParameter("lc", "m");
        request.addParameter("lc", "h");
        mav = _controller.handleRequestInternal(request, getResponse());

        model = mav.getModel();
        modelResults = (Map) model.get("results");

        assertEquals(new Integer(41), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(new Integer(41), modelResults.get(SchoolsController.MODEL_TOTAL));
        p1schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (SchoolSearchResult schoolResult : p1schools) {
            School s = schoolResult.getSchool();
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
            assertTrue("Wrong level included", s.getLevelCode().containsLevelCode(LevelCode.Level.MIDDLE_LEVEL) ||
                    s.getLevelCode().containsLevelCode(LevelCode.Level.HIGH_LEVEL));
        }

        // Check for other important stuff
        assertEquals("1", model.get(SchoolsController.MODEL_PAGE));
        assertEquals("Anchorage", model.get(SchoolsController.MODEL_CITY_NAME));
        assertEquals(LevelCode.createLevelCode("m,h"), model.get(SchoolsController.MODEL_LEVEL_CODE));
        assertEquals(null, model.get(SchoolsController.MODEL_SCHOOL_TYPE));


    }

    public void testFilteringSchoolType() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "AK");
        request.setParameter("city", "Anchorage");
        request.setParameter("st", "public");
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

        // Look for all the m/h schools
        request.setParameter("st", "private");
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
        assertEquals("San Francisco Schools", _controller.calcCitySchoolsTitle("San Francisco", null, null));
        assertEquals("San Francisco Schools", _controller.calcCitySchoolsTitle("San Francisco", LevelCode.ELEMENTARY_MIDDLE, null));
        assertEquals("San Francisco Schools", _controller.calcCitySchoolsTitle("San Francisco", LevelCode.MIDDLE_HIGH, null));
        assertEquals("San Francisco Schools", _controller.calcCitySchoolsTitle("San Francisco", null, new String[]{"public", "private"}));
        assertEquals("San Francisco Schools", _controller.calcCitySchoolsTitle("San Francisco", null, new String[]{"public", "charter"}));
        assertEquals("San Francisco Schools", _controller.calcCitySchoolsTitle("San Francisco", null, new String[]{"private", "charter"}));

        // These useful views get nice SEO friendly titles
        assertEquals("San Francisco Elementary Schools", _controller.calcCitySchoolsTitle("San Francisco", LevelCode.ELEMENTARY, null));
        assertEquals("San Francisco Middle Schools", _controller.calcCitySchoolsTitle("San Francisco", LevelCode.MIDDLE, null));
        assertEquals("San Francisco High Schools", _controller.calcCitySchoolsTitle("San Francisco", LevelCode.HIGH, null));
        assertEquals("San Francisco Preschool Schools", _controller.calcCitySchoolsTitle("San Francisco", LevelCode.PRESCHOOL, null));

        assertEquals("San Francisco Public Schools", _controller.calcCitySchoolsTitle("San Francisco", null, new String[]{"public"}));
        assertEquals("San Francisco Private Schools", _controller.calcCitySchoolsTitle("San Francisco", null, new String[]{"private"}));
        assertEquals("San Francisco Charter Schools", _controller.calcCitySchoolsTitle("San Francisco", null, new String[]{"charter"}));
    }

    public void testMetaDescCalc() {
        assertEquals("View and map all San Francisco schools. Plus, compare or save schools.",
                _controller.calcMetaDesc(null, "San Francisco", null, null));
        assertEquals("View and map all San Francisco middle schools. Plus, compare or save middle schools.",
                _controller.calcMetaDesc(null, "San Francisco", LevelCode.MIDDLE, null));
        assertEquals("View and map all San Francisco public elementary schools. Plus, compare or save public elementary schools.",
                _controller.calcMetaDesc(null, "San Francisco", LevelCode.ELEMENTARY, new String[]{"public"}));

        assertEquals("View and map all San Francisco public preschool schools. Plus, compare or save public preschool schools.",
                _controller.calcMetaDesc(null, "San Francisco", LevelCode.PRESCHOOL, new String[]{"public"}));

        assertEquals("View and map all schools in the Oakland Unified School District. Plus, compare or save schools in this district.",
                _controller.calcMetaDesc("Oakland Unified School District", "Oakland", null, null));
        assertEquals("View and map all middle schools in the Oakland Unified School District. Plus, compare or save middle schools in this district.",
                _controller.calcMetaDesc("Oakland Unified School District", "Oakland", LevelCode.MIDDLE, null));
        assertEquals("View and map all public elementary schools in the Oakland Unified School District. Plus, compare or save public elementary schools in this district.",
                _controller.calcMetaDesc("Oakland Unified School District", "Oakland", LevelCode.ELEMENTARY, new String[]{"public"}));
    }

}
