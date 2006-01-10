/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: GeoControllerTest.java,v 1.2 2006/01/10 18:54:17 apeterson Exp $
 */

package gs.web.geo;

import gs.data.geo.IGeoDao;
import gs.web.BaseControllerTestCase;
import gs.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class GeoControllerTest extends BaseControllerTestCase {

    public void xtestOakland() throws Exception {
        GeoController c = new GeoController();

        c.setGeoDao((IGeoDao) getApplicationContext().getBean(IGeoDao.BEAN_ID));
        c.setViewName("/geo/cityViolence");

        MockHttpServletRequest request = getRequest();
        request.addParameter("city", "Oakland");
        request.addParameter("state", "CA");
        ModelAndView modelAndView = c.handleRequest(request, getResponse());

        Object city = modelAndView.getModel().get("city");
        assertNotNull(city);


    }
    public void testSF() throws Exception {
        GeoController c = new GeoController();

        c.setGeoDao((IGeoDao) getApplicationContext().getBean(IGeoDao.BEAN_ID));
        c.setViewName("/geo/cityViolence");

        MockHttpServletRequest request = getRequest();
        request.addParameter("city", "San Francisco");
        request.addParameter("state", "CA");
        ModelAndView modelAndView = c.handleRequest(request, getResponse());

        Object city = modelAndView.getModel().get("city");
        assertNotNull(city);


    }
}
