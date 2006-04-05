/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolsControllerTest.java,v 1.8 2006/04/05 17:24:21 apeterson Exp $
 */

package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.SessionContextUtil;
import gs.web.GsMockHttpServletRequest;
import gs.data.school.district.IDistrictDao;
import gs.data.school.School;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.search.Searcher;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.List;

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
        List list = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (int i = 0; i < list.size(); i++) {
            School s = (School) list.get(i);
            assertEquals("Wrong city for " + s, "Alameda", s.getPhysicalAddress().getCity());
        }

        // Check for other important stuff
        assertEquals("1", request.getAttribute(SchoolsController.MODEL_PAGE));
        assertEquals("Alameda", request.getAttribute(SchoolsController.MODEL_CITY_NAME));
        assertEquals(null, request.getAttribute(SchoolsController.MODEL_LEVEL_CODE));
        assertEquals(null, request.getAttribute(SchoolsController.MODEL_SCHOOL_TYPE));
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
        List list = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (int i = 0; i < list.size(); i++) {
            School s = (School) list.get(i);
            assertEquals("Wrong district for " + s, "Alameda", s.getPhysicalAddress().getCity());
        }

        // Check for other important stuff
        assertEquals("1", request.getAttribute(SchoolsController.MODEL_PAGE));
        assertEquals("Alameda City Unified", request.getAttribute(SchoolsController.MODEL_DISTNAME));
        assertEquals(null, request.getAttribute(SchoolsController.MODEL_LEVEL_CODE));
        assertEquals(null, request.getAttribute(SchoolsController.MODEL_SCHOOL_TYPE));

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
        List p1schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (int i = 0; i < p1schools.size(); i++) {
            School s = (School) p1schools.get(i);
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
        }

        // Check for other important stuff
        assertEquals("1", request.getAttribute(SchoolsController.MODEL_PAGE));
        assertEquals("Anchorage", request.getAttribute(SchoolsController.MODEL_CITY_NAME));
        assertEquals(null, request.getAttribute(SchoolsController.MODEL_LEVEL_CODE));
        assertEquals(null, request.getAttribute(SchoolsController.MODEL_SCHOOL_TYPE));

        // OK, now go to page #2
        request.setParameter("p", "2");
        mav = _controller.handleRequestInternal(request, getResponse());

        model = mav.getModel();
        modelResults = (Map) model.get("results");

        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(new Integer(102), modelResults.get(SchoolsController.MODEL_TOTAL));
        List p2schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (int i = 0; i < p2schools.size(); i++) {
            School s = (School) p2schools.get(i);
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
        List p10schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (int i = 0; i < p10schools .size(); i++) {
            School s = (School) p10schools .get(i);
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
        List p11schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        assertEquals(2, p11schools.size());
        for (int i = 0; i < p11schools .size(); i++) {
            School s = (School) p11schools .get(i);
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
        List p1schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (int i = 0; i < p1schools.size(); i++) {
            School s = (School) p1schools.get(i);
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
            assertTrue("Wrong level included", s.getLevelCode().containsLevelCode(LevelCode.Level.ELEMENTARY_LEVEL));
        }

        // Check for other important stuff
        assertEquals("1", request.getAttribute(SchoolsController.MODEL_PAGE));
        assertEquals("Anchorage", request.getAttribute(SchoolsController.MODEL_CITY_NAME));
        assertEquals(LevelCode.createLevelCode("e"), request.getAttribute(SchoolsController.MODEL_LEVEL_CODE));
        assertEquals(null, request.getAttribute(SchoolsController.MODEL_SCHOOL_TYPE));



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
        for (int i = 0; i < p1schools.size(); i++) {
            School s = (School) p1schools.get(i);
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
            assertTrue("Wrong level included", s.getLevelCode().containsLevelCode(LevelCode.Level.MIDDLE_LEVEL) ||
                    s.getLevelCode().containsLevelCode(LevelCode.Level.HIGH_LEVEL));
        }

        // Check for other important stuff
        assertEquals("1", request.getAttribute(SchoolsController.MODEL_PAGE));
        assertEquals("Anchorage", request.getAttribute(SchoolsController.MODEL_CITY_NAME));
        assertEquals(LevelCode.createLevelCode("m,h"), request.getAttribute(SchoolsController.MODEL_LEVEL_CODE));
        assertEquals(null, request.getAttribute(SchoolsController.MODEL_SCHOOL_TYPE));


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
        List p1schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (int i = 0; i < p1schools.size(); i++) {
            School s = (School) p1schools.get(i);
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
            assertTrue("Wrong type", s.getType().equals(SchoolType.PUBLIC));
        }

        // Check for other important stuff
        assertEquals("1", request.getAttribute(SchoolsController.MODEL_PAGE));
        assertEquals("Anchorage", request.getAttribute(SchoolsController.MODEL_CITY_NAME));
        assertEquals("public", ((String[])(request.getAttribute(SchoolsController.MODEL_SCHOOL_TYPE)))[0]);



        // Look for all the m/h schools
        request.setParameter("st", "private");
        mav = _controller.handleRequestInternal(request, getResponse());

        model = mav.getModel();
        modelResults = (Map) model.get("results");

        assertEquals(new Integer(25), modelResults.get(SchoolsController.MODEL_SCHOOLS_TOTAL));
        assertEquals(new Integer(10), modelResults.get(SchoolsController.MODEL_PAGE_SIZE));
        assertEquals(new Integer(25), modelResults.get(SchoolsController.MODEL_TOTAL));
        p1schools = (List) modelResults.get(SchoolsController.MODEL_SCHOOLS);
        for (int i = 0; i < p1schools.size(); i++) {
            School s = (School) p1schools.get(i);
            assertEquals("Wrong city for " + s, "Anchorage", s.getPhysicalAddress().getCity());
            assertTrue("Wrong type", s.getType().equals(SchoolType.PRIVATE));
        }

        // Check for other important stuff
        assertEquals("1", request.getAttribute(SchoolsController.MODEL_PAGE));
        assertEquals("Anchorage", request.getAttribute(SchoolsController.MODEL_CITY_NAME));
        assertEquals("private", ((String[]) (request.getAttribute(SchoolsController.MODEL_SCHOOL_TYPE)))[0]);


    }

}
