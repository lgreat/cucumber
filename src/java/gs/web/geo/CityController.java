/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: CityController.java,v 1.32 2006/05/31 23:05:29 apeterson Exp $
 */

package gs.web.geo;

import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.ISessionFacade;
import gs.web.AnchorListModelFactory;
import gs.web.SessionContext;
import gs.web.util.AnchorListModel;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Given a city and state in the URL, populates model properties needed
 * for the city view page. These are included in the MODEL_* constants.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class CityController extends AbstractController {

    public static final String PARAM_CITY = "city";

    public static final String MODEL_SCHOOLS = "schools"; // list of local schools, either a sample or top-rated
    public static final String MODEL_SCHOOL_COUNT = "schoolCount"; // number of schools in the city
    public static final String MODEL_TOP_RATED_SCHOOLS = "topRatedSchools"; // List of ITopRatedSchool objects
    public static final String MODEL_LINK_TO_TOP_RATED_SCHOOLS = "linkToTopRatedSchools"; // Boolean or null (meaning false)

    public static final String MODEL_CITY = "cityObject"; // City BpCensus object
    public static final String MODEL_CITY_NAME = "displayName"; // name of the city, correctly capitalized

    public static final String MODEL_DISTRICTS = "districts"; // AnchorListModel object
    public static final String MODEL_SCHOOL_BREAKDOWN = "schoolBreakdown"; // AnchorListModel object

    //public static final String MODEL_SCHOOLS_BY_LEVEL = "schoolsByLevel"; // map [e,m,h] of AnchorListModel object
    private static final Log _log = LogFactory.getLog(CityController.class);


    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private IDistrictDao _districtDao;
    private StateManager _stateManager;
    private AnchorListModelFactory _anchorListModelFactory;

    public static final int MAX_SCHOOLS = 10;
    private static final Pattern UNDERLINE_PATTERN = Pattern.compile("_");

    public CityController() {
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        // TH: Set this controller to read-only to avoid hibernate updating school_rating
        ThreadLocalTransactionManager.setReadOnly();

        // Figure out the inputs
        final ISessionFacade sessionContext = SessionContext.getInstance(request);
        State state = sessionContext.getStateOrDefault();
        String cityNameParam = request.getParameter(PARAM_CITY);

        // Look in the URL if they aren't in parameters
        if (StringUtils.isEmpty(cityNameParam)) {
            String r = request.getRequestURI();
            //_log.error(r);
            r = r.replaceAll("/gs-web", "");
            r = r.replaceAll("/city/", "");
            // _log.error(r);
            String[] rs = StringUtils.split(r, "/");
            if (rs.length == 2) {
                cityNameParam = UNDERLINE_PATTERN.matcher(rs[0]).replaceAll(" ");
                state = _stateManager.getState(rs[1]);
                ((SessionContext) sessionContext).setState(state);
            }
        }

        // Validate those inputs and give up if we can't build a reasonable page.
        if (state == null) {
            _log.error("No state name found on city page. Redirecting to /");
            View redirectView = new RedirectView("/");
            return new ModelAndView(redirectView);
        }

        if (StringUtils.isEmpty(cityNameParam)) {
            _log.error("No city name found in " + state + ". Redirecting to /modperl/go");
            View redirectView = new RedirectView("/modperl/go/" + state.getAbbreviation());
            return new ModelAndView(redirectView);
        }

        ICity city = _geoDao.findCity(state, cityNameParam);
        if (city == null) {
            // If we don't have census data on the city take the user to browse city
            _log.error("No city record found for '" + cityNameParam + ", " + state + "'. Redirecting to schools.page");
            View redirectView = new RedirectView("/schools.page?state=" + state.getAbbreviation() +
                    "&city=" + URLEncoder.encode(cityNameParam, "UTF-8"));
            return new ModelAndView(redirectView);
        }


        Map model = new HashMap();

        // City name is what gets displayed, and can differ in a few cases from what we refer to
        // it as in the database and in URLs.
        String cityDisplayName;
        if (state.equals(State.DC)) {
            cityDisplayName = "Washington, D.C.";
        } else {
            cityDisplayName = city.getName();
        }
        model.put(MODEL_CITY_NAME, cityDisplayName);
        model.put(MODEL_CITY, city);

        int schoolCount = _schoolDao.countSchools(state, null, null, city.getName());
        model.put(MODEL_SCHOOL_COUNT, new Integer(schoolCount));

        if (schoolCount > 0) {
            AnchorListModel schoolBreakdownAnchorList = _anchorListModelFactory.createSchoolSummaryModel(state, cityNameParam, cityDisplayName, request);
            model.put(MODEL_SCHOOL_BREAKDOWN, schoolBreakdownAnchorList);

            //Map schoolsByLevel = createSchoolsByLevelModel(state, city, request);
            //model.put(MODEL_SCHOOLS_BY_LEVEL, schoolsByLevel);
        }

        AnchorListModel districtAnchorList = _anchorListModelFactory.createDistrictList(state, cityNameParam, request);
        model.put(MODEL_DISTRICTS, districtAnchorList);

        /*
        * If top rated schools are available for this city, then get them and
        * put them into the model.
        */
        if (state.isRatingsState()) {
            List topRatedSchools;
            topRatedSchools = _schoolDao.findTopRatedSchoolsInCity(city, 1, null, 5);
            if (topRatedSchools.size() > 0) {
                model.put(MODEL_TOP_RATED_SCHOOLS, topRatedSchools);

                List schools = new ArrayList(topRatedSchools.size());
                for (ListIterator iter = topRatedSchools.listIterator(); iter.hasNext();) {
                    ISchoolDao.ITopRatedSchool s = (ISchoolDao.ITopRatedSchool) iter.next();
                    schools.add(s.getSchool());
                }
                model.put(MODEL_SCHOOLS, schools);

                // If this is the first (therefore toppest rated), figure out
                // whether or not to display a link to top-rated schools. Otherwise no.
                if (topRatedSchools.size() >= 1) {
                    ISchoolDao.ITopRatedSchool s = (ISchoolDao.ITopRatedSchool) topRatedSchools.get(0);
                    model.put(MODEL_LINK_TO_TOP_RATED_SCHOOLS, Boolean.valueOf(s.getRating() >= 9));
                }

            }
        }

        if (model.get(MODEL_TOP_RATED_SCHOOLS) == null) {
            List schools = _schoolDao.findSchoolsInCity(state, cityNameParam, false);
            if (schools != null) {
                if (schools.size() > MAX_SCHOOLS) {
                    schools = schools.subList(0, MAX_SCHOOLS);
                }
                model.put(MODEL_SCHOOLS, schools);
            }
        }

        return new ModelAndView("geo/city", model);
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IDistrictDao getDistrictDao() {
        return _districtDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public AnchorListModelFactory getAnchorListModelFactory() {
        return _anchorListModelFactory;
    }

    public void setAnchorListModelFactory(AnchorListModelFactory anchorListModelFactory) {
        _anchorListModelFactory = anchorListModelFactory;
    }
}
