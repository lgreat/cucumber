/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: GeoController.java,v 1.2 2006/01/10 18:26:07 apeterson Exp $
 */

package gs.web.geo;

import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpCity;
import gs.data.geo.bestplaces.BpState;
import gs.data.geo.bestplaces.BpZip;
import gs.data.state.State;
import gs.web.SessionContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class GeoController implements Controller {
    private IGeoDao _geoDao;
    private String _viewName;

    protected final Log _log = LogFactory.getLog(getClass());

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
        if (StringUtils.isNotEmpty(cityNameParam) && state != null) {

            BpCity city = _geoDao.findCity(state, cityNameParam);

            if (city != null) {
                model.put("city", city);
            }
        }

        BpState bps = _geoDao.findState(state);
        model.put("statewide", bps);

        List list = _geoDao.getAllBpNation();
        model.put("us", list.get(0));

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
