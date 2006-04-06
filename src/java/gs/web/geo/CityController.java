/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: CityController.java,v 1.3 2006/04/06 22:05:14 apeterson Exp $
 */

package gs.web.geo;

import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.web.SessionContext;
import gs.web.util.Anchor;
import gs.web.util.ListModel;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Given a city and state in the URL, populates model properties needed
 * for the city view page. These are included in the MODEL_* constants.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class CityController extends AbstractController {

    public static final String PARAM_CITY = "city";

    public static final String MODEL_SCHOOLS = "schools"; // list of local schools
    public static final String MODEL_STATEWIDE = "statewide"; // BpCensus object for the state
    public static final String MODEL_US = "us"; // BpCensus object for the U.S.
    public static final String MODEL_CITY = "city"; // City BpCensus object
    public static final String MODEL_CITY_NAME = "cityName"; // name of the city, correctly capitalized
    public static final String MODEL_MAP_LAT = "lat"; // Center lat
    public static final String MODEL_MAP_LON = "lon"; // Center lon
    public static final String MODEL_MAP_SCALE = "scale"; // The scale of the map


    public static final String MODEL_DISTRICTS = "districts"; // ListModel object
    public static final String MODEL_SCHOOL_BREAKDOWN = "schoolBreakdown"; // ListModel object
    public static final String MODEL_TOP_RATED_SCHOOLS = "topRatedSchools"; // List of schools


    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private IDistrictDao _districtDao;
    private final UrlUtil _urlUtil;

    public CityController() {
        _urlUtil = new UrlUtil();
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        State state = SessionContext.getInstance(request).getStateOrDefault();

        if (state == null) {
            View redirectView = new RedirectView("/");
            return new ModelAndView(redirectView);
        }

        String cityNameParam = request.getParameter(PARAM_CITY);
        if (StringUtils.isEmpty(cityNameParam)) {
            View redirectView = new RedirectView("/modperl/go/" + state.getAbbreviationLowerCase());
            return new ModelAndView(redirectView);
        }


        Map model = new HashMap();

        ListModel schoolBreakdownList = createSchoolSummaryModel(state, cityNameParam);
        model.put(MODEL_SCHOOL_BREAKDOWN, schoolBreakdownList);

        ListModel districtList = createDistrictList(state, cityNameParam, request);
        model.put(MODEL_DISTRICTS, districtList);


        String c = StringUtils.capitalize(cityNameParam);
        model.put(MODEL_CITY_NAME, c);

        ICity city = _geoDao.findCity(state, cityNameParam);

        Float lat = null;
        Float lon = null;
        if (city != null) {
            model.put(MODEL_CITY, city);
            lat = new Float(city.getLat());
            lon = new Float(city.getLon());
        }

        /*
         * If top rated schools are available for this city, then get them and
         * put them into the model.
         */
        if (state.isRatingsState() && city != null) {
            List topRatedSchools;
            topRatedSchools = _schoolDao.findTopRatedSchoolsInCity(city, 8, null, 5);
            //sampleSchools = _schoolDao.findSchoolsInCity(state, cityNameParam, 5);
            if (topRatedSchools.size() > 0) {
                model.put(MODEL_TOP_RATED_SCHOOLS, topRatedSchools);

                List schools = new ArrayList(topRatedSchools.size());
                for (Iterator iter = topRatedSchools.iterator(); iter.hasNext();) {
                    ISchoolDao.ITopRatedSchool s = (ISchoolDao.ITopRatedSchool) iter.next();
                    schools.add(s.getSchool());
                }
                model.put(MODEL_SCHOOLS, schools);
            }
        }

        if (model.get(MODEL_TOP_RATED_SCHOOLS) == null) {
            List schools = _schoolDao.findSchoolsInCity(state, cityNameParam, false);
            if (schools != null) {
                if (schools.size() > 20) {
                    schools = schools.subList(0, 20);
                }
                model.put(MODEL_SCHOOLS, schools);
            }
        }


        model.put(MODEL_MAP_LAT, lat);
        model.put(MODEL_MAP_LON, lon);

        model.put(MODEL_MAP_SCALE, new Integer(6)); // should be calculated better
        // 1 = house, 10 = the earth


        return new ModelAndView("geo/city", model);
    }

    private ListModel createDistrictList(State state, String cityNameParam, HttpServletRequest request) {
        ListModel districts = new ListModel();

        List list = _districtDao.findDistrictsInCity(state, cityNameParam, true);
        if (list != null) {

            boolean needViewAll = false;

            if (list.size() <= 6) {
                _districtDao.sortDistrictsByName(list);
                districts.setHeading(cityNameParam + " Districts");
            } else {
                // Too many districts to show... just show the largest
                _districtDao.sortDistrictsByNumberOfSchools(list, false);
                list = list.subList(0, 4);
                districts.setHeading("Biggest " + cityNameParam + " Districts");
                needViewAll = true;
            }

            for (Iterator iter = list.iterator(); iter.hasNext();) {
                District d = (District) iter.next();
                String url = "/cgi-bin/" + state.getAbbreviationLowerCase() + "/district_profile/" + d.getId() + "/";
                url = _urlUtil.buildUrl(url, request);
                districts.addResult(new Anchor(url, d.getName()));
            }

            if (needViewAll) {
                String url = "/modperl/distlist/" + state.getAbbreviation() + "/";
                url = _urlUtil.buildUrl(url, request);
                districts.addResult(new Anchor(url, "All " + state.getName() + " Districts"));
            }
        }

        return districts;
    }

    private ListModel createSchoolSummaryModel(State state, String cityNameParam) {
        // the summaries of schools in a city
        ListModel schoolBreakdownList;
        schoolBreakdownList = new ListModel();

        int sc = _schoolDao.countSchools(state, null, null, cityNameParam);
        if (sc > 0) {
            Anchor a = new Anchor("/schools.page?state=" + state.getAbbreviation() + "&city=" + cityNameParam,
                    "All " + cityNameParam + " schools (" + sc + ")");
            schoolBreakdownList.addResult(a);

            sc = _schoolDao.countSchools(state, null, LevelCode.ELEMENTARY, cityNameParam);
            if (sc > 0) {
                a = new Anchor("/schools.page?state=" + state.getAbbreviation() + "&city=" + cityNameParam + "&lc=e",
                        "All Elementary (" + sc + ")");
                schoolBreakdownList.addResult(a);
            }

            sc = _schoolDao.countSchools(state, null, LevelCode.MIDDLE, cityNameParam);
            if (sc > 0) {
                a = new Anchor("/schools.page?state=" + state.getAbbreviation() + "&city=" + cityNameParam + "&lc=m",
                        "All Middle (" + sc + ")");
                schoolBreakdownList.addResult(a);
            }

            sc = _schoolDao.countSchools(state, null, LevelCode.HIGH, cityNameParam);
            if (sc > 0) {
                a = new Anchor("/schools.page?c=school&state=" + state.getAbbreviation() + "&city=" + cityNameParam + "&lc=h",
                        "All High (" + sc + ")");
                schoolBreakdownList.addResult(a);
            }

        }
        return schoolBreakdownList;
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
}
