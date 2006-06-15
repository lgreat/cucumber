/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: GeoController.java,v 1.15 2006/06/15 17:17:20 thuss Exp $
 */

package gs.web.geo;

import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpCensus;
import gs.data.geo.bestplaces.BpState;
import gs.data.school.ISchoolDao;
import gs.data.state.State;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
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

    public static final String PARAM_CITY = "city";

    public static final String MODEL_STATE_CENSUS = "statewide"; // BpCensus object for the state
    public static final String MODEL_US_CENSUS = "us"; // BpCensus object for the U.S.
    public static final String MODEL_LOCAL_CENSUS = "bpCensus"; // City BpCensus object
    public static final String MODEL_DISPLAY_NAME = "displayName"; // String

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ThreadLocalTransactionManager.setReadOnly();

        State state = SessionContext.getInstance(request).getStateOrDefault();


        Map model = new HashMap();


        String cityNameParam = request.getParameter(PARAM_CITY);
        if (StringUtils.isNotEmpty(cityNameParam) && state != null) {

            _log.debug("State: " + state.getAbbreviation() + " city: " + cityNameParam);
            BpCensus bpCensus = _geoDao.findBpCity(state, cityNameParam);

            if (bpCensus == null) {
                bpCensus = _geoDao.findZip(state, cityNameParam);
            }

            if (bpCensus != null) {
                model.put(MODEL_LOCAL_CENSUS, bpCensus);
            }

            // displayName needs minor tweaks
            String displayName;
            if (State.DC.equals(state)) {
                displayName = "Washington, DC";
            } else if (State.NY.equals(state) && "New York".equals(cityNameParam)) {
                displayName = "New York City";
            } else if (bpCensus != null) {
                displayName = bpCensus.getName();
            } else {
                displayName = cityNameParam;
            }
            model.put(MODEL_DISPLAY_NAME, displayName);
        }

        BpState bps = _geoDao.findState(state);
        model.put(MODEL_STATE_CENSUS, bps);
        _log.debug("Setting state to " + state + " and bps to " + bps);

        List list = _geoDao.getAllBpNation();
        model.put(MODEL_US_CENSUS, list.get(0));


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
