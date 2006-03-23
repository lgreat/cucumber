/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: CityController.java,v 1.7 2006/03/23 18:21:38 apeterson Exp $
 */

package gs.web.state;

import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.data.geo.bestplaces.BpCity;
import gs.data.geo.bestplaces.BpState;
import gs.data.geo.IGeoDao;
import gs.web.SessionContext;
import gs.web.util.Anchor;
import gs.web.util.ListModel;
import gs.web.util.UrlUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class CityController extends AbstractController {

    public static final String BEAN_ID = "/city.page";

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
        String cityNameParam = request.getParameter(PARAM_CITY);


        Map model = new HashMap();

        ListModel schoolBreakdownList = createSchoolSummaryModel(state, cityNameParam);
        model.put(MODEL_SCHOOL_BREAKDOWN, schoolBreakdownList);

        ListModel districtList = createDistrictList(state, cityNameParam, request);
        model.put(MODEL_DISTRICTS, districtList);


        Float lat = null;
        Float lon = null;

        if (StringUtils.isNotEmpty(cityNameParam) && state != null) {

            String c = StringUtils.capitalize(cityNameParam);
            model.put(MODEL_CITY_NAME, c);

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

        //BpState bps = _geoDao.findState(state);
        //model.put(MODEL_STATEWIDE, bps);

        //List list = _geoDao.getAllBpNation();
        //model.put(MODEL_US, list.get(0));

        model.put(MODEL_MAP_LAT, lat);
        model.put(MODEL_MAP_LON, lon);

        model.put(MODEL_MAP_SCALE, new Integer(5)); // should be calculated better


        return new ModelAndView("test/city2", model);
    }

    private ListModel createDistrictList(State state, String cityNameParam, HttpServletRequest request) {
        ListModel districts = new ListModel();

        List list = _districtDao.findDistrictsInCity(state, cityNameParam, true);
        if (list != null) {
            districts.setHeading(cityNameParam + " Districts");
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                District d = (District) iter.next();
                String url = "/cgi-bin/" + state.getAbbreviationLowerCase() + "/district_profile/" + d.getId() + "/";
                url = _urlUtil.buildUrl(url, request);
                districts.addResult(new Anchor(url, d.getName()));
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
