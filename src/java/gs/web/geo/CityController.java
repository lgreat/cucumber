/*
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: CityController.java,v 1.68 2010/12/15 01:53:02 yfan Exp $
 */

package gs.web.geo;

import gs.data.geo.City;
import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.SchoolType;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.test.rating.CityRating;
import gs.data.test.rating.ICityRatingDao;
import gs.data.url.DirectoryStructureUrlFactory;
import gs.data.community.local.ILocalBoardDao;
import gs.data.community.local.LocalBoard;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.AnchorListModel;
import gs.web.util.list.AnchorListModelFactory;
import gs.web.util.UrlBuilder;
import gs.web.util.RedirectView301;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.path.DirectoryStructureUrlFields;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Given a city and state in the URL, populates model properties needed
 * for the city view page. These are included in the MODEL_* constants.
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 */
public class CityController extends AbstractController  implements IDirectoryStructureUrlController {

    public static final String PARAM_CITY = "city";

    public static final String MODEL_SCHOOLS = "schools"; // list of local schools, either a sample or top-rated
    public static final String MODEL_SCHOOL_COUNT = "schoolCount"; // number of schools in the city
    public static final String MODEL_TOP_RATED_SCHOOLS = "topRatedSchools"; // List of ITopRatedSchool objects

    public static final String MODEL_CITY = "cityObject"; // City BpCensus object
    public static final String MODEL_CITY_NAME = "displayName"; // name of the city, correctly capitalized

    public static final String MODEL_DISTRICTS = "districts"; // AnchorListModel object
    public static final String MODEL_SCHOOL_BREAKDOWN = "schoolBreakdown"; // AnchorListModel object

    public static final String MODEL_DISCUSSION_BOARD_ID = "discussionBoardId"; // AnchorListModel object

    public static final String PARAM_CITY_CANONICAL_PATH = "canonicalCityPath";

    //public static final String MODEL_SCHOOLS_BY_LEVEL = "schoolsByLevel"; // map [e,m,h] of AnchorListModel object
    private static final Log _log = LogFactory.getLog(CityController.class);

    private static final Pattern UNDERLINE_PATTERN = Pattern.compile("_");

    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private IDistrictDao _districtDao;
    private ICityRatingDao _cityRatingDao;
    private StateManager _stateManager;
    private AnchorListModelFactory _anchorListModelFactory;
    private ILocalBoardDao _localBoardDao;
    private StateSpecificFooterHelper _stateSpecificFooterHelper;    

    public static final int MAX_SCHOOLS = 10;

    public CityController() {
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        // Figure out the inputs
        final SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        State state = sessionContext.getStateOrDefault();
        String cityNameParam = request.getParameter(PARAM_CITY);
        boolean redirectToNewStyleUrl = false;
        
        // Look in the URL if they aren't in parameters
        if (StringUtils.isEmpty(cityNameParam)) {
            String r = request.getRequestURI();
            if (r.startsWith("/gsweb/city/") || r.startsWith("/city/")) {
                //_log.error(r);
                r = r.replaceAll("/gs-web", "");
                r = r.replaceAll("/city/", "");
                // _log.error(r);
                String[] rs = StringUtils.split(r, "/");
                if (rs.length == 2) {
                    cityNameParam = UNDERLINE_PATTERN.matcher(rs[0]).replaceAll(" ");
                    state = _stateManager.getState(rs[1]);
                    redirectToNewStyleUrl = true;
                }
            } else {
                DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);
                if (fields != null) {
                    cityNameParam = fields.getCityName();
                }
            }
        }

        // Validate those inputs and give up if we can't build a reasonable page.
        if (state == null) {
            // no state name found on city page, so redirect to /
            View redirectView = new RedirectView("/");
            return new ModelAndView(redirectView);
        }

        if (StringUtils.isEmpty(cityNameParam)) {
            // no city name found, so redirect to /california or whichever state they did provide
            View redirectView = new RedirectView(DirectoryStructureUrlFactory.createNewStateBrowseURIRoot(state));
            return new ModelAndView(redirectView);
        }

        ICity city = _geoDao.findCity(state, cityNameParam);
        if (city == null) {
            // If we don't have census data on the city take the user to browse city
            _log.error("No city record found for '" + cityNameParam + ", " + state + "'. Redirecting to city browse page");
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, state, cityNameParam, new HashSet<SchoolType>(), null); 
            View redirectView = new RedirectView(urlBuilder.asSiteRelative(request));
            return new ModelAndView(redirectView);
        }

        if (redirectToNewStyleUrl || "/new-york/new-york/".equals(request.getRequestURI())) {
            // Redirect to the new city url if we got here via the old one
            UrlBuilder urlBuilder = new UrlBuilder(city, UrlBuilder.CITY_PAGE);
            View redirectView = new RedirectView301(urlBuilder.asSiteRelative(request));
            return new ModelAndView(redirectView);
        }

        if (!cityNameParam.equals(cityNameParam.toLowerCase())) {
            // Redirect to the lowercase name of the city if it isn't already lowercase
            UrlBuilder urlBuilder = new UrlBuilder(city, UrlBuilder.CITY_PAGE);
            View redirectView = new RedirectView301(urlBuilder.asSiteRelative(request));
            return new ModelAndView(redirectView);
        }

        Map model = new HashMap();

        if (!StringUtils.isEmpty(cityNameParam) && city != null && state != null) {
            model.put(PARAM_CITY_CANONICAL_PATH, "http://" + request.getServerName() + ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "") + DirectoryStructureUrlFactory.createNewCityBrowseURIRoot(state, city.getName()));
        }

        String cityDisplayName = ((City) city).getDisplayName();
        model.put(MODEL_CITY_NAME, cityDisplayName);
        model.put(MODEL_CITY, city);

        LocalBoard localBoard = _localBoardDao.findByCityId(((City)city).getId());
        if (localBoard != null) {
            model.put(MODEL_DISCUSSION_BOARD_ID, localBoard.getBoardId());
        }

        int schoolCount = _schoolDao.countSchools(state, null, null, city.getName());
        model.put(MODEL_SCHOOL_COUNT, new Integer(schoolCount));

        if (schoolCount > 0) {
            AnchorListModel schoolBreakdownAnchorList = _anchorListModelFactory.createSchoolSummaryModel(state, cityNameParam, cityDisplayName, request);
            model.put(MODEL_SCHOOL_BREAKDOWN, schoolBreakdownAnchorList);

            //Map schoolsByLevel = createSchoolsByLevelModel(state, city, request);
            //model.put(MODEL_SCHOOLS_BY_LEVEL, schoolsByLevel);
        }

        AnchorListModel districtAnchorList = _anchorListModelFactory.createDistrictList(state, cityNameParam, cityDisplayName,request);
        model.put(MODEL_DISTRICTS, districtAnchorList);

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
        }


        if (model.get(MODEL_TOP_RATED_SCHOOLS) == null) {
            List schools = _schoolDao.findSchoolsInCity(state, cityNameParam, false);
            if (schools.size() > 0) {
                if (schools.size() > MAX_SCHOOLS) {
                    schools = schools.subList(0, MAX_SCHOOLS);
                }
                model.put(MODEL_SCHOOLS, schools);
            }
        }

        Integer cityRatingValue = 0;
        String strCityRating = "no";
        try {
            CityRating cityRating = _cityRatingDao.getCityRatingByCity(state,city.getName());
            cityRatingValue = cityRating.getRating();
        } catch (ObjectRetrievalFailureException e) {
        }
        strCityRating = cityRatingValue > 0 ? cityRatingValue.toString() : strCityRating;
        String cityRatingAlt = cityRatingValue > 0 ? "out of 10" : "rating available";
        model.put("strCityRating",strCityRating);
        model.put("cityratingAlt",cityRatingAlt);

        model.put("levelCode",_schoolDao.getLevelCodeInCity(city.getName(),state));

        _stateSpecificFooterHelper.placePopularCitiesInModel(state, model);
        
        return new ModelAndView("geo/city", model);
    }

    // required to implement IDirectoryStructureUrlController
    public boolean shouldHandleRequest(DirectoryStructureUrlFields fields) {
        if (fields == null) {
            return false;
        }

        return fields.hasState() && fields.hasCityName() && !fields.hasDistrictName() && !fields.hasLevelCode() && !fields.hasSchoolName();
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

    public ICityRatingDao getCityRatingDao() {
        return _cityRatingDao;
    }

    public void setCityRatingDao(ICityRatingDao cityRatingDao) {
        _cityRatingDao = cityRatingDao;
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

    public ILocalBoardDao getLocalBoardDao() {
        return _localBoardDao;
    }

    public void setLocalBoardDao(ILocalBoardDao localBoardDao) {
        _localBoardDao = localBoardDao;
    }

    public StateSpecificFooterHelper getStateSpecificFooterHelper() {
        return _stateSpecificFooterHelper;
    }

    public void setStateSpecificFooterHelper(StateSpecificFooterHelper stateSpecificFooterHelper) {
        _stateSpecificFooterHelper = stateSpecificFooterHelper;
    }
}