/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: GeoController.java,v 1.4 2006/02/27 18:16:13 apeterson Exp $
 */

package gs.web.geo;

import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpCity;
import gs.data.geo.bestplaces.BpState;
import gs.data.geo.bestplaces.BpZip;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Fetches all sorts of information about a city (or zip code) and puts
 * it in the model for the view.  <code>PARAM_*</code> constants
 * specify the expected values, and <code>MODEL_</code> specify the
 * output model sent on to the view.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 * @todo separate out school information. This needs to be separate, more defined classes.
 */
public class GeoController implements Controller {
    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private String _viewName;

    protected final Log _log = LogFactory.getLog(getClass());

    private static final String PARAM_CITY = "city";
    private static final String PARAM_ZIP = "zip";

    private static final String MODEL_SCHOOLS = "schools"; // list of local schools
    private static final String MODEL_STATEWIDE = "statewide"; // BpCensus object for the state
    private static final String MODEL_US = "us"; // BpCensus object for the U.S.
    private static final String MODEL_ZIP = "zip"; // Zip BpCensus object
    private static final String MODEL_CITY = "city"; // City BpCensus object
    private static final String MODEL_LAT = "lat"; // Center lat
    private static final String MODEL_LON = "lon"; // City BpCensus object
    private static final String MODEL_SCALE = "scale"; // The scale of the map

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        State state = SessionContext.getInstance(request).getStateOrDefault();


        Map model = new HashMap();

        Float lat = null;
        Float lon = null;


        String zipCodeParam = request.getParameter(PARAM_ZIP);
        if (StringUtils.isNotEmpty(zipCodeParam)) {

            BpZip zip = _geoDao.findZip(zipCodeParam);

            if (zip != null) {
                model.put(MODEL_ZIP, zip);
                lat = zip.getLat();
                lon = zip.getLon();
            }
        }


        String cityNameParam = request.getParameter(PARAM_CITY);
        if (StringUtils.isNotEmpty(cityNameParam) && state != null) {

            BpCity city = _geoDao.findCity(state, cityNameParam);

            if (city != null) {
                model.put(MODEL_CITY, city);
                if (lat == null) {
                    lat = city.getLat();
                    lon = city.getLon();
                }
            }

            List schools = _schoolDao.findSchoolsInCity(state, cityNameParam, false);
            if (schools != null) {
                if (schools.size() > 50) {
                    schools = schools.subList(0, 50);
                }
                model.put(MODEL_SCHOOLS, schools);
                if (lat == null) {
                    for (Iterator iter = schools.iterator(); iter.hasNext() && lat == null;) {
                        School s = (School) iter.next();
                        if (s.getLat() != null) {
                            lat = new Float(s.getLat().floatValue());
                            lon = new Float(s.getLon().floatValue());
                        }
                    }
                }
            }
        }

        BpState bps = _geoDao.findState(state);
        model.put(MODEL_STATEWIDE, bps);

        List list = _geoDao.getAllBpNation();
        model.put(MODEL_US, list.get(0));

        model.put(MODEL_LAT, lat);
        model.put(MODEL_LON, lon);

        model.put(MODEL_SCALE, new Integer(7));

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

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }
}
