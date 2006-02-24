/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: GeoControllerTest.java,v 1.4 2006/02/24 23:10:47 apeterson Exp $
 */

package gs.web.geo;

import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.web.BaseControllerTestCase;
import gs.web.MockHttpServletRequest;
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

    public void testOakland() throws Exception {
        GeoController c = new GeoController();
        c.setGeoDao(_geoDao);
        c.setSchoolDao(_schoolDao);
        c.setViewName("/geo/cityViolence");

        MockHttpServletRequest request = getRequest();
        request.addParameter("city", "Oakland");
        request.addParameter("state", "CA");
        ModelAndView modelAndView = c.handleRequest(request, getResponse());

        Object city = modelAndView.getModel().get("city");
        assertNotNull(city);

        Object schools = modelAndView.getModel().get("schools");
        assertNull(schools);

        assertNotNull(modelAndView.getModel().get("lat"));
        assertNotNull(modelAndView.getModel().get("lon"));
    }

    public void testAlameda() throws Exception {
        GeoController c = new GeoController();

        c.setGeoDao(_geoDao);
        c.setSchoolDao(_schoolDao);
        c.setViewName("/geo/cityViolence");

        MockHttpServletRequest request = getRequest();
        request.addParameter("city", "Alameda");
        request.addParameter("state", "CA");
        ModelAndView modelAndView = c.handleRequest(request, getResponse());

        Object city = modelAndView.getModel().get("city");
        assertNotNull(city);

        Object schools = modelAndView.getModel().get("schools");
        assertNotNull(schools);
        assertTrue(schools instanceof Collection);
        assertTrue( ((Collection)schools).size() >= 10);

        assertNotNull(modelAndView.getModel().get("lat"));
        assertNotNull(modelAndView.getModel().get("lon"));
    }


    public void testSF() throws Exception {
        GeoController c = new GeoController();

        c.setGeoDao(_geoDao);
        c.setSchoolDao(_schoolDao);
        c.setViewName("/geo/cityViolence");

        MockHttpServletRequest request = getRequest();
        request.addParameter("city", "San Francisco");
        request.addParameter("state", "CA");
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

        MockHttpServletRequest request = getRequest();
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
