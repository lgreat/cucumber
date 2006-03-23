/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolsControllerTest.java,v 1.4 2006/03/23 17:25:11 apeterson Exp $
 */

package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.SessionContextUtil;
import gs.web.MockHttpServletRequest;
import gs.web.search.ResultsPager;
import gs.data.school.district.IDistrictDao;
import gs.data.school.School;
import gs.data.search.Searcher;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.List;

/**
 * Tests SchoolsControllerTest.
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
        _controller.setResultsPager((ResultsPager) getApplicationContext().getBean(ResultsPager.BEAN_ID));
        _controller.setSearcher((Searcher) getApplicationContext().getBean(Searcher.BEAN_ID));

        _sessionContextUtil = (SessionContextUtil) getApplicationContext().getBean(SessionContextUtil.BEAN_ID);
    }

    public void testByCity() throws Exception {

        MockHttpServletRequest request = getRequest();
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
        assertEquals("1", request.getAttribute(SchoolsController.REQ_ATTR_PAGE));
        assertEquals("Alameda", request.getAttribute(SchoolsController.REQ_ATTR_CITY));
        assertEquals(null, request.getAttribute(SchoolsController.REQ_ATTR_LEVEL_CODE));
        assertEquals(null, request.getAttribute(SchoolsController.REQ_ATTR_SCHOOL_TYPE));
    }

    public void testByDistrict() throws Exception {
        MockHttpServletRequest request = getRequest();
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
            assertEquals("Wrong city for " + s, "Alameda", s.getPhysicalAddress().getCity());
        }

        // Check for other important stuff
        assertEquals("1", request.getAttribute(SchoolsController.REQ_ATTR_PAGE));
        assertEquals("Alameda City Unified", request.getAttribute(SchoolsController.REQ_ATTR_DISTNAME));
        assertEquals(null, request.getAttribute(SchoolsController.REQ_ATTR_LEVEL_CODE));
        assertEquals(null, request.getAttribute(SchoolsController.REQ_ATTR_SCHOOL_TYPE));

    }

}
