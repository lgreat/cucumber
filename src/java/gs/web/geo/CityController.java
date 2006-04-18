/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: CityController.java,v 1.15 2006/04/18 15:15:19 thuss Exp $
 */

package gs.web.geo;

import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.web.SessionContext;
import gs.web.school.SchoolsController;
import gs.web.util.Anchor;
import gs.web.util.ListModel;
import gs.web.util.UrlBuilder;
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

    public static final String MODEL_SCHOOLS = "schools"; // list of local schools, either a sample or top-rated
    public static final String MODEL_SCHOOL_COUNT = "schoolCount"; // number of schools in the city
    public static final String MODEL_TOP_RATED_SCHOOLS = "topRatedSchools"; // List of ITopRatedSchool objects

    public static final String MODEL_CITY = "cityObject"; // City BpCensus object
    public static final String MODEL_CITY_NAME = "displayName"; // name of the city, correctly capitalized

    public static final String MODEL_DISTRICTS = "districts"; // ListModel object
    public static final String MODEL_SCHOOL_BREAKDOWN = "schoolBreakdown"; // ListModel object

    //public static final String MODEL_SCHOOLS_BY_LEVEL = "schoolsByLevel"; // map [e,m,h] of ListModel object


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

        // TH: Set this controller to read-only to avoid hibernate updating school_rating
        ThreadLocalTransactionManager.setReadOnly();


        final State state = SessionContext.getInstance(request).getStateOrDefault();

        if (state == null) {
            View redirectView = new RedirectView("/");
            return new ModelAndView(redirectView);
        }

        final String cityNameParam = request.getParameter(PARAM_CITY);
        if (StringUtils.isEmpty(cityNameParam)) {
            View redirectView = new RedirectView("/modperl/go/" + state.getAbbreviationLowerCase());
            return new ModelAndView(redirectView);
        }

        ICity city = _geoDao.findCity(state, cityNameParam);
        if (city == null) {
            View redirectView = new RedirectView("/modperl/go/" + state.getAbbreviationLowerCase() +
                    "?error=Nothing+known+about+" + cityNameParam + ".");
            return new ModelAndView(redirectView);
        }


        Map model = new HashMap();

        if (state.equals(State.DC)) {
            model.put(MODEL_CITY_NAME, "Washington, D.C.");
        } else {
            model.put(MODEL_CITY_NAME, city.getName());
        }
        model.put(MODEL_CITY, city);

        int schoolCount = calcSchoolCount(state, city.getName());
        model.put(MODEL_SCHOOL_COUNT, new Integer(schoolCount));

        if (schoolCount > 0) {
            ListModel schoolBreakdownList = createSchoolSummaryModel(state, cityNameParam, city.getName());
            model.put(MODEL_SCHOOL_BREAKDOWN, schoolBreakdownList);

            //Map schoolsByLevel = createSchoolsByLevelModel(state, city, request);
            //model.put(MODEL_SCHOOLS_BY_LEVEL, schoolsByLevel);
        }

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

            if (list.size() <= 5) {
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
                String url = "/modperl/districts/" + state.getAbbreviation() + "/";
                url = _urlUtil.buildUrl(url, request);
                districts.addResult(new Anchor(url, "View all " + state.getLongName() + " Districts", "viewall"));
            }
        }

        return districts;
    }

    private ListModel createSchoolSummaryModel(State state, String cityNameParam, String cityName) {
        // the summaries of schools in a city
        ListModel schoolBreakdownList;
        schoolBreakdownList = new ListModel();

        int sc;
        //Anchor a = new Anchor("/schools.page?state=" + state.getAbbreviation() + "&city=" + cityNameParam,
        //        "All " + cityNameParam + " schools (" + sc + ")");
        //schoolBreakdownList.addResult(a);
        Anchor a = null;

        sc = _schoolDao.countSchools(state, null, LevelCode.ELEMENTARY, cityNameParam);
        if (sc > 0) {
            a = new Anchor("/schools.page?state=" + state.getAbbreviation() + "&city=" + cityNameParam + "&lc=e",
                    cityName + " Elementary Schools (" + sc + ")");
            schoolBreakdownList.addResult(a);
        }

        sc = _schoolDao.countSchools(state, null, LevelCode.MIDDLE, cityNameParam);
        if (sc > 0) {
            a = new Anchor("/schools.page?state=" + state.getAbbreviation() + "&city=" + cityNameParam + "&lc=m",
                    cityName + " Middle Schools (" + sc + ")");
            schoolBreakdownList.addResult(a);
        }

        sc = _schoolDao.countSchools(state, null, LevelCode.HIGH, cityNameParam);
        if (sc > 0) {
            a = new Anchor("/schools.page?state=" + state.getAbbreviation() + "&city=" + cityNameParam + "&lc=h",
                    cityName + " High Schools (" + sc + ")");
            schoolBreakdownList.addResult(a);
        }

        sc = _schoolDao.countSchools(state, SchoolType.PUBLIC, null, cityNameParam) +
                _schoolDao.countSchools(state, SchoolType.CHARTER, null, cityNameParam);
        if (sc > 0) {
            a = new Anchor("/schools.page?state=" + state.getAbbreviation() + "&city=" + cityNameParam + "&st=public&st=charter",
                    cityName + " Public Schools (" + sc + ")");
            schoolBreakdownList.addResult(a);
        }

        sc = _schoolDao.countSchools(state, SchoolType.PRIVATE, null, cityNameParam);
        if (sc > 0) {
            a = new Anchor("/schools.page?state=" + state.getAbbreviation() + "&city=" + cityNameParam + "&st=private",
                    cityName + " Private Schools (" + sc + ")");
            schoolBreakdownList.addResult(a);
        }

        // Add a "last" to the last item
        // It's already set!
        // List results = schoolBreakdownList.getResults();
        //a = (Anchor) results.get(results.size() - 1);
        if (a != null) {
            a.setStyleClass(a.getStyleClass() + " last");
        }

        return schoolBreakdownList;
    }

    private int calcSchoolCount(State state, String cityNameParam) {
        return _schoolDao.countSchools(state, null, null, cityNameParam);
    }

    private Map createSchoolsByLevelModel(State state, ICity city, HttpServletRequest request) {
        // the summaries of schools in a city
        Map map;

        map = new HashMap();

        UrlBuilder builder = new UrlBuilder(city, UrlBuilder.SCHOOLS_IN_CITY);

        int sc;
        sc = _schoolDao.countSchools(state, null, LevelCode.ELEMENTARY, city.getName());
        if (sc > 0) {
            ListModel m = new ListModel("Elementary Schools (" + sc + ")");
            builder.setParameter(SchoolsController.PARAM_LEVEL_CODE, "e");
            m.addResult(builder.asAnchor(request, "List"));
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
