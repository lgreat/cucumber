/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: ListModelFactory.java,v 1.1 2006/05/24 19:26:26 apeterson Exp $
 */

package gs.web;

import gs.data.geo.IGeoDao;
import gs.data.geo.ICity;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.school.district.IDistrictDao;
import gs.data.school.district.District;
import gs.data.state.StateManager;
import gs.data.state.State;
import gs.web.util.ListModel;
import gs.web.util.Anchor;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import gs.web.school.SchoolsController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class ListModelFactory {

    public static final String BEAN_ID = "listModelFactory";

    private IGeoDao _geoDao;
    private ISchoolDao _schoolDao;
    private IDistrictDao _districtDao;
    private StateManager _stateManager;
    private final UrlUtil _urlUtil = new UrlUtil();



    public ListModel createDistrictList(State state, String cityNameParam, HttpServletRequest request) {
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

    public ListModel createSchoolSummaryModel(State state, String cityName, String cityDisplayName, HttpServletRequest request) {
        // the summaries of schools in a city
        ListModel schoolBreakdownList;
        schoolBreakdownList = new ListModel();

        int sc;
        //Anchor a = new Anchor("/schools.page?state=" + state.getAbbreviation() + "&city=" + cityNameParam,
        //        "All " + cityNameParam + " schools (" + sc + ")");
        //schoolBreakdownList.addResult(a);

        UrlBuilder builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, state, cityName);

        sc = _schoolDao.countSchools(state, null, LevelCode.ELEMENTARY, cityName);
        if (sc > 0) {
            builder.setParameter("lc", "e");
            final Anchor anchor = builder.asAnchor(request, cityDisplayName + " Elementary Schools");
            anchor.setAfter(" (" + sc + ")");
            schoolBreakdownList.addResult(anchor);
        }

        sc = _schoolDao.countSchools(state, null, LevelCode.MIDDLE, cityName);
        if (sc > 0) {
            builder.setParameter("lc", "m");
            final Anchor anchor = builder.asAnchor(request, cityDisplayName + " Middle Schools");
            anchor.setAfter(" (" + sc + ")");
            schoolBreakdownList.addResult(anchor);
        }

        sc = _schoolDao.countSchools(state, null, LevelCode.HIGH, cityName);
        if (sc > 0) {
            builder.setParameter("lc", "h");
            final Anchor anchor = builder.asAnchor(request, cityDisplayName + " High Schools");
            anchor.setAfter(" (" + sc + ")");
            schoolBreakdownList.addResult(anchor);
        }
        builder.removeParameter("lc");

        sc = _schoolDao.countSchools(state, SchoolType.PUBLIC, null, cityName) +
                _schoolDao.countSchools(state, SchoolType.CHARTER, null, cityName);
        if (sc > 0) {
            builder.addParameter("st", "public");
            builder.addParameter("st", "charter");
            final Anchor anchor = builder.asAnchor(request, cityDisplayName + " Public Schools");
            anchor.setAfter(" (" + sc + ")");
            schoolBreakdownList.addResult(anchor);
            builder.removeParameter("st");
        }

        sc = _schoolDao.countSchools(state, SchoolType.PRIVATE, null, cityName);
        if (sc > 0) {
            builder.addParameter("st", "private");
            final Anchor anchor = builder.asAnchor(request, cityDisplayName + " Private Schools");
            anchor.setAfter(" (" + sc + ")");
            schoolBreakdownList.addResult(anchor);
        }

        // Add a "last" to the last item
        List results = schoolBreakdownList.getResults();
        Anchor a = (Anchor) results.get(results.size() - 1);
        if (a != null) {
            a.setStyleClass(a.getStyleClass() + " last");
        }

        return schoolBreakdownList;
    }


    /**
     * Returns a map of level letter (e,m or h) -> ListModel, where the list model has links to a list of schools and
     * a Map & List link, both taking you to the schools page.
     * For example, the elementary school list model would be:
     * <code>
     * <h1>Elementary Schools (54)</h1>
     * <ul>
     * <li><a href="...">List</a></li>
     * <li><a href="...">List & Map</a></li>
     * </ul>
     * </code>
     *
     * @param state   required state
     * @param city    required
     * @param request required
     * @return non-null, but possibly empty map
     */
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


    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
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

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}
