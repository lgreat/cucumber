/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: AnchorListModelFactory.java,v 1.4 2007/05/31 19:12:44 droy Exp $
 */

package gs.web.util.list;

import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.geo.NearbyCitiesController;
import gs.web.search.SearchController;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.ObjectUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Generates AnchorListModel objects from all sorts of input. Created to reduce
 * the size of the controller classes and to easily allow sharing of model
 * generation code across controllers.
 * </p>
 * At some point, this could be divided into topic-specific factories.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class AnchorListModelFactory {

    public static final String BEAN_ID = "anchorListModelFactory";

    private ISchoolDao _schoolDao;
    private IDistrictDao _districtDao;
    private StateManager _stateManager;
    private final UrlUtil _urlUtil = new UrlUtil();


    /**
     * Provides a list of districts in a city. Currently it caps the list at 5. If there are
     * five or fewer, it sorts them alphabetically. If there are more than 5, it shows them
     * based on size.
     */
    public AnchorListModel createDistrictList(State state, String cityNameParam, HttpServletRequest request) {
        AnchorListModel districts = new AnchorListModel();

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
                districts.add(new Anchor(url, d.getName()));
            }

            if (needViewAll) {
                String url = "/modperl/districts/" + state.getAbbreviation() + "/";
                url = _urlUtil.buildUrl(url, request);
                districts.add(new Anchor(url, "View all " + state.getLongName() + " Districts", "viewall"));
            }
        }

        return districts;
    }

    /**
     * Summarizes the schools in a city. It creates an item for each of the following, if there are any:
     * <ul>
     * <li>elementary schools
     * <li>middle schools
     * <li>high schools
     * <li>public schools (includes charter)
     * <li>private schools
     * <li>all schools
     * </ul>
     *
     * @param cityName        the name as defined in the city table and associated with all schools
     * @param cityDisplayName name to display, usually the same as cityName. Two cases
     *                        where it is different "New York" is labeled "New York City", and "Washington" is
     *                        labelled "Washington, D.C."
     */
    public AnchorListModel createSchoolSummaryModel(State state, String cityName, String cityDisplayName, HttpServletRequest request) {
        // the summaries of schools in a city
        AnchorListModel schoolBreakdownAnchorList;
        schoolBreakdownAnchorList = new AnchorListModel();

        int sc;
        //Anchor a = new Anchor("/schools.page?state=" + state.getAbbreviation() + "&city=" + cityNameParam,
        //        "All " + cityNameParam + " schools (" + sc + ")");
        //schoolBreakdownAnchorList.addResult(a);

        UrlBuilder builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, state, cityName);

        sc = _schoolDao.countSchools(state, null, LevelCode.ELEMENTARY, cityName);
        if (sc > 0) {
            builder.setParameter("lc", "e");
            final Anchor anchor = builder.asAnchor(request, cityDisplayName + " Elementary Schools");
            anchor.setAfter(" (" + sc + ")");
            schoolBreakdownAnchorList.add(anchor);
        }

        sc = _schoolDao.countSchools(state, null, LevelCode.MIDDLE, cityName);
        if (sc > 0) {
            builder.setParameter("lc", "m");
            final Anchor anchor = builder.asAnchor(request, cityDisplayName + " Middle Schools");
            anchor.setAfter(" (" + sc + ")");
            schoolBreakdownAnchorList.add(anchor);
        }

        sc = _schoolDao.countSchools(state, null, LevelCode.HIGH, cityName);
        if (sc > 0) {
            builder.setParameter("lc", "h");
            final Anchor anchor = builder.asAnchor(request, cityDisplayName + " High Schools");
            anchor.setAfter(" (" + sc + ")");
            schoolBreakdownAnchorList.add(anchor);
        }
        builder.removeParameter("lc");

        sc = _schoolDao.countSchools(state, SchoolType.PUBLIC, null, cityName) +
                _schoolDao.countSchools(state, SchoolType.CHARTER, null, cityName);
        if (sc > 0) {
            builder.addParameter("st", "public");
            builder.addParameter("st", "charter");
            final Anchor anchor = builder.asAnchor(request, cityDisplayName + " Public Schools");
            anchor.setAfter(" (" + sc + ")");
            schoolBreakdownAnchorList.add(anchor);
            builder.removeParameter("st");
        }

        sc = _schoolDao.countSchools(state, SchoolType.PRIVATE, null, cityName);
        if (sc > 0) {
            builder.addParameter("st", "private");
            final Anchor anchor = builder.asAnchor(request, cityDisplayName + " Private Schools");
            anchor.setAfter(" (" + sc + ")");
            schoolBreakdownAnchorList.add(anchor);
        }

        // Add a "last" to the last item
        List results = schoolBreakdownAnchorList.getResults();
        Anchor a = (Anchor) results.get(results.size() - 1);
        if (a != null) {
            a.appendStyleClass("last");
        }

        // Add a "first" to the first item
        a = (Anchor) results.get(0);
        if (a != null) {
            a.appendStyleClass("first");
        }

        return schoolBreakdownAnchorList;
    }


    /**
     * Returns a map of level letter (e,m or h) -> AnchorListModel, where the list model has links to a list of schools and
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
//    public Map createSchoolsByLevelModel(State state, ICity city, HttpServletRequest request) {
//        // the summaries of schools in a city
//        Map map;
//
//        map = new HashMap();
//
//        UrlBuilder builder = new UrlBuilder(city, UrlBuilder.SCHOOLS_IN_CITY);
//
//        int sc;
//        sc = _schoolDao.countSchools(state, null, LevelCode.ELEMENTARY, city.getName());
//        if (sc > 0) {
//            AnchorListModel m = new AnchorListModel("Elementary Schools (" + sc + ")");
//            builder.setParameter(SchoolsController.PARAM_LEVEL_CODE, "e");
//            m.add(builder.asAnchor(request, "List"));
//            builder.setParameter(SchoolsController.PARAM_SHOW_MAP, "1");
//            m.add(builder.asAnchor(request, "Map & List"));
//            builder.removeParameter(SchoolsController.PARAM_SHOW_MAP);
//            map.put("e", m);
//        }
//
//        sc = _schoolDao.countSchools(state, null, LevelCode.MIDDLE, city.getName());
//        if (sc > 0) {
//            AnchorListModel m = new AnchorListModel("Middle Schools (" + sc + ")");
//            builder.setParameter(SchoolsController.PARAM_LEVEL_CODE, "m");
//            m.add(builder.asAnchor(request, "List"));
//            builder.setParameter(SchoolsController.PARAM_SHOW_MAP, "1");
//            m.add(builder.asAnchor(request, "Map & List"));
//            builder.removeParameter(SchoolsController.PARAM_SHOW_MAP);
//            map.put("m", m);
//        }
//
//        sc = _schoolDao.countSchools(state, null, LevelCode.HIGH, city.getName());
//        if (sc > 0) {
//            AnchorListModel m = new AnchorListModel("High Schools (" + sc + ")");
//            builder.setParameter(SchoolsController.PARAM_LEVEL_CODE, "h");
//            m.add(builder.asAnchor(request, "List"));
//            builder.setParameter(SchoolsController.PARAM_SHOW_MAP, "1");
//            m.add(builder.asAnchor(request, "Map & List"));
//            builder.removeParameter(SchoolsController.PARAM_SHOW_MAP);
//            map.put("h", m);
//        }
//
//        return map;
//    }

    /**
     * Generates a list of links to schools in the given Hits cities.
     *
     * @throws IOException
     */
    public AnchorListModel createCitiesListModel(HttpServletRequest request,
                                                 Hits cityHits,
                                                 SchoolType schoolType,
                                                 int maxCities,
                                                 boolean showMore) throws IOException {
        AnchorListModel anchorListModel = new AnchorListModel("" +
                (SchoolType.CHARTER.equals(schoolType) ? "Charter schools" : "Schools") +
                " in the city of: ");

        for (int i = 0; i < maxCities; i++) {
            if (cityHits != null && cityHits.length() > i) {
                Document cityDoc = cityHits.doc(i);
                String cityName = cityDoc.get("city");
                String s = cityDoc.get("state");
                State stateOfCity = _stateManager.getState(s);
                UrlBuilder builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY,
                        stateOfCity,
                        cityName);
                cityName += ", " + stateOfCity;
                anchorListModel.add(builder.asAnchor(request, cityName));
            }
        }

        // add a more button if necessary
        if (showMore) {
            UrlBuilder builder = new UrlBuilder(request, "/search/search.page");
            builder.addParametersFromRequest(request);
            builder.setParameter(SearchController.PARAM_MORE_CITIES, "true");
            anchorListModel.add(builder.asAnchor(request, "more cities..."));
        }

        if (anchorListModel.getResults().size() > 0) {
            Anchor a = (Anchor) anchorListModel.getResults().get(anchorListModel.getResults().size() - 1);
            a.setStyleClass("last");
            a = (Anchor) anchorListModel.getResults().get(0);
            a.appendStyleClass("first");
        }
        return anchorListModel;
    }

    /**
     * Creates list of links to the schools in the given districts.
     *
     * @throws IOException
     */
    public AnchorListModel createDistrictsListModel(HttpServletRequest request,
                                                    Hits districtHits,
                                                    State state,
                                                    SchoolType schoolType,
                                                    int maxDistricts,
                                                    boolean showMore) throws IOException {
        AnchorListModel anchorListModel = new AnchorListModel("" +
                (SchoolType.CHARTER.equals(schoolType) ? "Charter schools" : "Schools") +
                " in the district of:");

        for (int j = 0; j < maxDistricts; j++) {
            if (districtHits != null && districtHits.length() > j) {
                Document districtDoc = districtHits.doc(j);
                String id = districtDoc.get("id");
                UrlBuilder builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_DISTRICT, state, id);
                String districtName = districtDoc.get("name");
                String s = districtDoc.get("state");
                State stateOfCity = _stateManager.getState(s);
                if (!ObjectUtils.equals(state, stateOfCity)) {
                    districtName += " (" + stateOfCity.getAbbreviation() + ")";
                }
                anchorListModel.add(builder.asAnchor(request, districtName));
            }
        }

        // add a more button if necessary
        if (showMore) {
            UrlBuilder builder = new UrlBuilder(request, "/search/search.page");
            builder.addParametersFromRequest(request);
            builder.setParameter(SearchController.PARAM_MORE_DISTRICTS, "true");
            anchorListModel.add(builder.asAnchor(request, "more districts..."));
        }

        if (anchorListModel.getResults().size() > 0) {
            Anchor a = (Anchor) anchorListModel.getResults().get(anchorListModel.getResults().size() - 1);
            a.setStyleClass("last");
            a = (Anchor) anchorListModel.getResults().get(0);
            a.appendStyleClass("first");
        }
        return anchorListModel;
    }

    /**
     * Creates a list of city page links. Links are assigned a class of
     * "town", "city" or "bigCity" depending on their size.
     *
     * @param heading              of the list
     * @param city                 cities that this is near
     * @param nearbyCities         List of ICity objects
     * @param limit                maximum number of nearbyCities to show
     * @param alwaysIncludeState   if true, labels each city as City, ST; otherwise,
     *                             it only includes the state for cities in a different state than <code>city</code>.
     * @param includeMoreItem      includes a "More" link at the end that points to a
     *                             nearby city.
     * @param includeBrowseAllItem if true, includes "Browse all cities in ST" as the last
     *                             item. (Ignored for Washington, D.C.)
     */
    public AnchorListModel createNearbyCitiesAnchorListModel(final String heading, ICity city,
                                                             List nearbyCities,
                                                             int limit,
                                                             final boolean alwaysIncludeState,
                                                             final boolean includeMoreItem,
                                                             final boolean includeBrowseAllItem,
                                                             HttpServletRequest request) {
        AnchorListModel anchorListModel = new AnchorListModel(heading);

        for (int i = 0; i < limit && i < nearbyCities.size(); i++) {
            ICity nearbyCity = (ICity) nearbyCities.get(i);

            // name
            String name = nearbyCity.getName();
            if (alwaysIncludeState ||
                    !nearbyCity.getState().equals(city.getState())) {
                name += ", " + nearbyCity.getState().getAbbreviation();
            }

            // style class
            String styleClass = "town";
            long pop = 0;
            if (nearbyCity.getPopulation() != null) {
                pop = nearbyCity.getPopulation().intValue();
            }
            if (pop > 50000) {
                styleClass = (pop > 200000) ? "bigCity" : "city";
            }

            // anchor
            UrlBuilder builder = new UrlBuilder(nearbyCity, UrlBuilder.CITY_PAGE);
            Anchor anchor = builder.asAnchor(request, name, styleClass);
            anchorListModel.add(anchor);
        }

        if (includeMoreItem) {
            UrlBuilder builder = new UrlBuilder(city, UrlBuilder.CITIES_MORE_NEARBY);
            builder.setParameter(NearbyCitiesController.PARAM_ORDER, "alpha");
            builder.setParameter(NearbyCitiesController.PARAM_INCLUDE_STATE, "1");
            if (!city.getState().equals(State.DC)) {
                builder.setParameter(NearbyCitiesController.PARAM_ALL, "1");
            }
            Anchor anchor = builder.asAnchor(request, "More >", "more");
            anchorListModel.add(anchor);
        }
        if (includeBrowseAllItem && !city.getState().equals(State.DC)) {
            UrlBuilder builder = new UrlBuilder(UrlBuilder.CITIES, city.getState(), null);
            Anchor anchor = builder.asAnchor(request, "Browse all " + city.getState().getLongName() + " cities",
                    "more");
            anchorListModel.add(anchor);

        }
        return anchorListModel;
    }

    public void setGeoDao(IGeoDao geoDao) {
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}
