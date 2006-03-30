/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: GeoControllerTest.java,v 1.9 2006/03/30 23:11:57 apeterson Exp $
 */

package gs.web.geo;

import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.SessionContextUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.context.ApplicationContext;

import java.util.Collection;

/**
 * Provides...
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
        request.addParameter("city", "Anchorage");
        request.addParameter("state", "AK");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        ModelAndView modelAndView = c.handleRequest(request, getResponse());

        Object city = modelAndView.getModel().get("city");
        assertNotNull(city);

        Object schools = modelAndView.getModel().get("schools");
        assertNotNull(schools);

        assertNotNull(modelAndView.getModel().get("lat"));
        assertNotNull(modelAndView.getModel().get("lon"));

        assertNotNull(modelAndView.getModel().get("scale"));
        assertEquals(new Integer(5), modelAndView.getModel().get("scale"));
    }

    public void testHopeAK() throws Exception {
        GeoController c = new GeoController();

        c.setGeoDao(_geoDao);
        c.setSchoolDao(_schoolDao);
        c.setViewName("/geo/cityViolence");

        GsMockHttpServletRequest request = getRequest();
        request.addParameter("city", "Hope");
        request.addParameter("state", "AK");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        ModelAndView modelAndView = c.handleRequest(request, getResponse());

        Object city = modelAndView.getModel().get("city");
        assertNotNull(city);

        Object schools = modelAndView.getModel().get("schools");
        assertNotNull(schools);
        assertTrue(schools instanceof Collection);
        assertTrue( ((Collection)schools).size() <= 2);

        assertNotNull(modelAndView.getModel().get("lat"));
        assertNotNull(modelAndView.getModel().get("lon"));
    }


    public void testSF() throws Exception {
        GeoController c = new GeoController();

        c.setGeoDao(_geoDao);
        c.setSchoolDao(_schoolDao);
        c.setViewName("/geo/cityViolence");

        GsMockHttpServletRequest request = getRequest();
        request.addParameter("city", "San Francisco");
        request.addParameter("state", "CA");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        ModelAndView modelAndView = c.handleRequest(request, getResponse());

        Object city = modelAndView.getModel().get("city");
        assertNotNull(city);

        assertNotNull(modelAndView.getModel().get("lat"));
        assertNotNull(modelAndView.getModel().get("lon"));
    }

    public void testBuffaloNY() throws Exception {
        GeoController c = new GeoController();

        c.setGeoDao(_geoDao);
        c.setSchoolDao(_schoolDao);
        c.setViewName("/geo/cityViolence");

        GsMockHttpServletRequest request = getRequest();
        request.addParameter("city", "Buffalo");
        request.addParameter("state", "NY");
        _sessionContextUtil.prepareSessionContext(getRequest(), getResponse());
        ModelAndView modelAndView = c.handleRequest(request, getResponse());

        Object city = modelAndView.getModel().get("city");
        assertNull(city);

        Object schools = modelAndView.getModel().get("schools");
        assertNotNull(schools);
        assertTrue(schools instanceof Collection);
        assertTrue(((Collection) schools).size() >= 3);

        assertNotNull(modelAndView.getModel().get("lat"));
        assertNotNull(modelAndView.getModel().get("lon"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        _applicationContext = getApplicationContext();
        _sessionContextUtil = (SessionContextUtil) _applicationContext.getBean(SessionContextUtil.BEAN_ID);
        _geoDao = (IGeoDao) _applicationContext.getBean(IGeoDao.BEAN_ID);
        _schoolDao = (ISchoolDao) _applicationContext.getBean(ISchoolDao.BEAN_ID);

    }
}
