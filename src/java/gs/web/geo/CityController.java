/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: CityController.java,v 1.6 2006/04/07 03:38:48 apeterson Exp $
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
import gs.web.school.SchoolsController;
import gs.web.util.Anchor;
import gs.web.util.ListModel;
import gs.web.util.UrlUtil;
import gs.web.util.UrlBuilder;
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

    public static final String MODEL_SCHOOLS = "schools"; // list of local schools, either a sample or top-rated
    public static final String MODEL_TOP_RATED_SCHOOLS = "topRatedSchools"; // List of ITopRatedSchool objects

    public static final String MODEL_CITY = "cityObject"; // City BpCensus object
    public static final String MODEL_CITY_NAME = "cityName"; // name of the city, correctly capitalized

    public static final String MODEL_DISTRICTS = "districts"; // ListModel object
    public static final String MODEL_SCHOOL_BREAKDOWN = "schoolBreakdown"; // ListModel object

    public static final String MODEL_SCHOOLS_BY_LEVEL = "schoolsByLevel"; // map [e,m,h] of ListModel object


    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private IDistrictDao _districtDao;
    private final UrlUtil _urlUtil;
    public static final int MAX_SCHOOLS = 10;

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

        ICity city = _geoDao.findCity(state, cityNameParam);
        if (city == null) {
            View redirectView = new RedirectView("/modperl/go/" + state.getAbbreviationLowerCase());
            return new ModelAndView(redirectView);
        }


        Map model = new HashMap();

        model.put(MODEL_CITY_NAME, city.getName());
        model.put(MODEL_CITY, city);

        ListModel schoolBreakdownList = createSchoolSummaryModel(state, cityNameParam);
        model.put(MODEL_SCHOOL_BREAKDOWN, schoolBreakdownList);

        Map schoolsByLevel = createSchoolsByLevelModel(state, city, request);
        model.put(MODEL_SCHOOLS_BY_LEVEL, schoolsByLevel);

        ListModel districtList = createDistrictList(state, cityNameParam, request);
        model.put(MODEL_DISTRICTS, districtList);


        /*
         * If top rated schools are available for this city, then get them and
         * put them into the model.
         */
        if (state.isRatingsState()) {
            List topRatedSchools;
            topRatedSchools = _schoolDao.findTopRatedSchoolsInCity(city, 9, null, 5);
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
                if (schools.size() > MAX_SCHOOLS) {
                    schools = schools.subList(0, MAX_SCHOOLS);
                }
                model.put(MODEL_SCHOOLS, schools);
            }
        }

        return new ModelAndView("geo/city", model);
    }

    private ListModel createDistrictList(State state, String cityNameParam, HttpServletRequest request) {
        ListModel districts = new ListModel();

        List list = _districtDao.findDistrictsInCity(state, cityNameParam, true);
        if (list != null) {

            boolean needViewAll = false;

            if (list.size() <= 6) {
                _districtDao.sortDistrictsByName(list);
                districts.setHeading(cityNameParam + " School Districts");
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

    private Map createSchoolsByLevelModel(State state, ICity city, HttpServletRequest request) {
        // the summaries of schools in a city
        Map map = null;

        int sc = _schoolDao.countSchools(state, null, null, city.getName());
        if (sc > 0) {
            map = new HashMap();

            UrlBuilder builder = new UrlBuilder(city, UrlBuilder.SCHOOLS_IN_CITY);

            sc = _schoolDao.countSchools(state, null, LevelCode.ELEMENTARY, city.getName());
            if (sc > 0) {
                ListModel m = new ListModel("Elementary Schools (" + sc + ")");
                builder.setParameter(SchoolsController.PARAM_LEVEL_CODE, "e");
                m.addResult( builder.asAnchor(request, "List"));
                builder.setParameter(SchoolsController.PARAM_SHOW_MAP, "1");
                m.addResult(builder.asAnchor(request, "Map & List"));
                builder.removeParameter(SchoolsController.PARAM_SHOW_MAP);
                map.put("e", m);
            }

            sc = _schoolDao.countSchools(state, null, LevelCode.MIDDLE, city.getName());
            if (sc > 0) {
                ListModel m = new ListModel("Middle Schools (" + sc + ")");
                builder.setParameter(SchoolsController.PARAM_LEVEL_CODE, "m");
                m.addResult(builder.asAnchor(request, "List"));
                builder.setParameter(SchoolsController.PARAM_SHOW_MAP, "1");
                m.addResult(builder.asAnchor(request, "Map & List"));
                builder.removeParameter(SchoolsController.PARAM_SHOW_MAP);
                map.put("m", m);
            }

            sc = _schoolDao.countSchools(state, null, LevelCode.HIGH, city.getName());
            if (sc > 0) {
                ListModel m = new ListModel("High Schools (" + sc + ")");
                builder.setParameter(SchoolsController.PARAM_LEVEL_CODE, "h");
                m.addResult(builder.asAnchor(request, "List"));
                builder.setParameter(SchoolsController.PARAM_SHOW_MAP, "1");
                m.addResult(builder.asAnchor(request, "Map & List"));
                builder.removeParameter(SchoolsController.PARAM_SHOW_MAP);
                map.put("h", m);
            }

        }
        return map;
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
