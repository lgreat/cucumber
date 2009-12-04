/*
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: GeoControllerTest.java,v 1.13 2009/12/04 20:54:12 npatury Exp $
 */

package gs.web.geo;

import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpCensus;
import gs.data.geo.bestplaces.BpState;
import gs.data.school.ISchoolDao;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContextUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tests GeoController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class GeoControllerTest extends BaseControllerTestCase {
    private SessionContextUtil _sessionContextUtil;
    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private ApplicationContext _applicationContext;


    public void testAnchorage() throws Exception {
        GeoController c = new GeoController();
        c.setGeoDao(_geoDao);
        c.setSchoolDao(_schoolDao);
        c.setViewName("/geo/cityViolence");

        GsMockHttpServletRequest request = getRequest();
        request.addParameter(GeoController.PARAM_CITY, "Anchorage");
        request.addParameter("state", "AK");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        ModelAndView modelAndView = c.handleRequest(request, getResponse());

        BpCensus city = (BpCensus) modelAndView.getModel().get(GeoController.MODEL_LOCAL_CENSUS);
        assertNotNull(city);
        assertEquals("Anchorage", city.getName());

        BpState stateWide = (BpState) modelAndView.getModel().get(GeoController.MODEL_STATE_CENSUS);
        assertNotNull(stateWide);
        assertEquals("Alaska", stateWide.getName());

        BpCensus us = (BpCensus) modelAndView.getModel().get(GeoController.MODEL_US_CENSUS);
        assertNotNull(us);
        assertEquals("United States", us.getName());
    }

    public void testSF() throws Exception {
        GeoController c = new GeoController();

        c.setGeoDao(_geoDao);
        c.setSchoolDao(_schoolDao);
        c.setViewName("/geo/cityViolence");

        GsMockHttpServletRequest request = getRequest();
        request.addParameter(GeoController.PARAM_CITY, "San Francisco");
        request.addParameter("state", "CA");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        ModelAndView modelAndView = c.handleRequest(request, getResponse());

        Object city = modelAndView.getModel().get(GeoController.MODEL_LOCAL_CENSUS);
        assertNotNull(city);
    }


    public void testNameOverrides() throws Exception {
        GeoController c = new GeoController();
        c.setGeoDao(_geoDao);
        c.setSchoolDao(_schoolDao);
        GsMockHttpServletRequest request = getRequest();
        
        request.addParameter(GeoController.PARAM_CITY, "New York");
        request.addParameter("state", "NY");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        ModelAndView modelAndView = c.handleRequest(request, getResponse());
        assertEquals("New York City", modelAndView.getModel().get(GeoController.MODEL_DISPLAY_NAME));

        request.setParameter(GeoController.PARAM_CITY, "Washington");
        request.setParameter("state", "DC");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        modelAndView = c.handleRequest(request, getResponse());
        assertEquals("Washington, DC", modelAndView.getModel().get(GeoController.MODEL_DISPLAY_NAME));
    }


    protected void setUp() throws Exception {
        super.setUp();
        _applicationContext = getApplicationContext();
        _sessionContextUtil = (SessionContextUtil) _applicationContext.getBean(SessionContextUtil.BEAN_ID);
        _geoDao = (IGeoDao) _applicationContext.getBean(IGeoDao.BEAN_ID);
        _schoolDao = (ISchoolDao) _applicationContext.getBean(ISchoolDao.BEAN_ID);

    }
}
