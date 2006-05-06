/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: CityControllerTest.java,v 1.9 2006/05/06 05:19:34 apeterson Exp $
 */

package gs.web.geo;

import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.data.state.StateManager;
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
        _controller.setStateManager((StateManager) getApplicationContext().getBean(StateManager.BEAN_ID));
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
        assertEquals(5, list.size());

        assertEquals("/schools.page?city=Anchorage&lc=e&state=AK", ((Anchor) list.get(0)).getHref());
        assertEquals("Anchorage Elementary Schools", ((Anchor) list.get(0)).getContents());
        assertEquals(" (77)", ((Anchor) list.get(0)).getAfter());

        assertEquals("/schools.page?city=Anchorage&lc=m&state=AK", ((Anchor) list.get(1)).getHref());

        assertEquals("Anchorage High Schools", ((Anchor) list.get(2)).getContents());
        assertEquals(" (30)", ((Anchor) list.get(2)).getAfter());

        assertEquals("/schools.page?city=Anchorage&st=public&st=charter&state=AK", ((Anchor) list.get(3)).getHref());
        assertEquals("/schools.page?city=Anchorage&st=private&state=AK", ((Anchor) list.get(4)).getHref());
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

    public void testAlamedaHasSchools() throws Exception {

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


    public void testSFIsAvalidCity() throws Exception {

        GsMockHttpServletRequest request = getRequest();
        request.addParameter("city", "San Francisco");
        request.addParameter("state", "CA");
        ModelAndView modelAndView = _controller.handleRequestInternal(request, getResponse());

        Object city = modelAndView.getModel().get(CityController.MODEL_CITY_NAME);
        assertNotNull(city);
    }


    public void testSearchEngineFriendlyUrlsWork() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/city/CA/San_Francisco.page");
        ModelAndView modelAndView = _controller.handleRequestInternal(request, getResponse());

        ICity city = (ICity) modelAndView.getModel().get(CityController.MODEL_CITY);
        assertNotNull(city);
        assertEquals("San Francisco", city.getName());
        assertEquals(State.CA, city.getState());

        request.setRequestURI("/CA/city/San_Francisco.page");
         modelAndView = _controller.handleRequestInternal(request, getResponse());

         city = (ICity) modelAndView.getModel().get(CityController.MODEL_CITY);
        assertNotNull(city);
        assertEquals("San Francisco", city.getName());
        assertEquals(State.CA, city.getState());

        request.setRequestURI("/CA/San_Francisco/city.page");
         modelAndView = _controller.handleRequestInternal(request, getResponse());

         city = (ICity) modelAndView.getModel().get(CityController.MODEL_CITY);
        assertNotNull(city);
        assertEquals("San Francisco", city.getName());
        assertEquals(State.CA, city.getState());

        request.setRequestURI("/gs-web/CA/San_Francisco/city.page");
         modelAndView = _controller.handleRequestInternal(request, getResponse());

         city = (ICity) modelAndView.getModel().get(CityController.MODEL_CITY);
        assertNotNull(city);
        assertEquals("San Francisco", city.getName());
        assertEquals(State.CA, city.getState());


        request.setRequestURI("/CA/Alameda/city.page");
         modelAndView = _controller.handleRequestInternal(request, getResponse());

         city = (ICity) modelAndView.getModel().get(CityController.MODEL_CITY);
        assertNotNull(city);
        assertEquals("Alameda", city.getName());
        assertEquals(State.CA, city.getState());

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
