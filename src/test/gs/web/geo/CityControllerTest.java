/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: CityControllerTest.java,v 1.5 2006/04/07 00:32:02 apeterson Exp $
 */

package gs.web.geo;

import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.district.IDistrictDao;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.SessionContextUtil;
import gs.web.util.Anchor;
import gs.web.util.ListModel;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Tests CityController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class CityControllerTest extends BaseControllerTestCase {

    private CityController _controller;
    private SessionContextUtil _sessionContextUtil;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new CityController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setSchoolDao((ISchoolDao) getApplicationContext().getBean(ISchoolDao.BEAN_ID));
        _controller.setDistrictDao((IDistrictDao) getApplicationContext().getBean(IDistrictDao.BEAN_ID));
        _controller.setGeoDao((IGeoDao) getApplicationContext().getBean(IGeoDao.BEAN_ID));
        _sessionContextUtil = (SessionContextUtil) getApplicationContext().
                getBean(SessionContextUtil.BEAN_ID);
    }

    public void testSchoolBreakdown() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "AK");
        request.setParameter("city", "Anchorage");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();

        ListModel listModel = (ListModel) model.get(CityController.MODEL_SCHOOL_BREAKDOWN);

        List list = listModel.getResults();
        assertEquals(4, list.size());

        assertEquals("/schools.page?state=AK&city=Anchorage", ((Anchor) list.get(0)).getHref());
        assertEquals("/schools.page?state=AK&city=Anchorage&lc=m", ((Anchor) list.get(2)).getHref());
        assertEquals("All Middle (32)", ((Anchor) list.get(2)).getContents());

    }

    public void testFindDistricts() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "NY");
        request.setParameter("city", "Dolgeville");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();

        ListModel listModel = (ListModel) model.get(CityController.MODEL_DISTRICTS);

        List list = listModel.getResults();
        assertTrue(list.size() > 0);
        assertTrue(list.size() <= 2);
        assertEquals("/cgi-bin/ny/district_profile/1/", ((Anchor) list.get(0)).getHref());
    }

    public void testBadCity() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "WY");
        request.setParameter("city", "XXXWorland");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        View view = mav.getView();
        assertTrue(view instanceof RedirectView);
    }

    public void testCapitalization() throws Exception {
        // Alison complained that "city=oakland" fails to capitalize as "Oakland".
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "CA");
        request.setParameter("city", "alameda");
        _sessionContextUtil.prepareSessionContext(request, getResponse());

        ModelAndView mav = _controller.handleRequestInternal(request, getResponse());

        Map model = mav.getModel();

        assertEquals("Alameda", model.get(CityController.MODEL_CITY_NAME));
    }


    public void testOakland() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.addParameter("city", "Oakland");
        request.addParameter("state", "CA");
        ModelAndView modelAndView = _controller.handleRequestInternal(request, getResponse());

        Object city = modelAndView.getModel().get(CityController.MODEL_CITY_NAME);
        assertNotNull(city);

        Object schools = modelAndView.getModel().get("schools");
        assertNull(schools);
    }

    public void testAlameda() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.addParameter("city", "Alameda");
        request.addParameter("state", "CA");
        ModelAndView modelAndView = _controller.handleRequestInternal(request, getResponse());

        Object city = modelAndView.getModel().get(CityController.MODEL_CITY_NAME);
        assertNotNull(city);

        Object schools = modelAndView.getModel().get("schools");
        assertNotNull(schools);
        assertTrue(schools instanceof Collection);
        assertTrue(((Collection) schools).size() >= 3);
        assertTrue(((Collection) schools).size() <= 10);

    }


    public void testSF() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.addParameter("city", "San Francisco");
        request.addParameter("state", "CA");
        ModelAndView modelAndView = _controller.handleRequestInternal(request, getResponse());

        Object city = modelAndView.getModel().get(CityController.MODEL_CITY_NAME);
        assertNotNull(city);
    }

    public void xtestBuffaloNY() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.addParameter("city", "Buffalo");
        request.addParameter("state", "NY");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        ModelAndView modelAndView = _controller.handleRequestInternal(request, getResponse());

        Object city = modelAndView.getModel().get(CityController.MODEL_CITY_NAME);
        assertNull(city);

        Object schools = modelAndView.getModel().get(CityController.MODEL_SCHOOLS);
        assertNotNull(schools);
        assertTrue(schools instanceof Collection);
        assertTrue(((Collection) schools).size() >= 3);

    }

}
