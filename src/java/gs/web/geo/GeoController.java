/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: GeoController.java,v 1.1 2005/11/29 01:37:38 apeterson Exp $
 */

package gs.web.geo;

import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpCity;
import gs.data.geo.bestplaces.BpZip;
import gs.data.state.State;
import gs.web.SessionContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class GeoController implements Controller {
    private IGeoDao _geoDao;
    private String _viewName;


    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        State state = SessionContext.getInstance(request).getStateOrDefault();


        Map model = new HashMap();


        String zipCodeParam = request.getParameter("zip");
        if (StringUtils.isNotEmpty(zipCodeParam)) {

            BpZip zip = _geoDao.findZip(zipCodeParam);

            if (zip != null) {
                model.put("zip", zip);
            }
        }


        String cityNameParam = request.getParameter("city");
        if (StringUtils.isNotEmpty(zipCodeParam)) {

            BpCity city = _geoDao.findCity(state, cityNameParam);

            if (city != null) {
                model.put("city", city);
            }
        }


        ModelAndView modelAndView = new ModelAndView(_viewName, model);

        return modelAndView;
    }


    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
