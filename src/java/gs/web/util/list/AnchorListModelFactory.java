/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: AnchorListModelFactory.java,v 1.27 2012/10/10 23:02:52 yfan Exp $
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
import gs.data.test.rating.CityRating;
import gs.data.url.DirectoryStructureUrlFactory;
import gs.data.util.Address;
import gs.data.search.Indexer;
import gs.web.geo.NearbyCitiesController;
import gs.web.search.SearchController;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * Generates AnchorListModel objects from all sorts of input. Created to reduce
 * the size of the controller classes and to easily allow sharing of model
 * generation code across controllers.
 * </p>
 * At some point, this could be divided into topic-specific factories.
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 */
public class AnchorListModelFactory {

    public static final String BEAN_ID = "anchorListModelFactory";

    private ISchoolDao _schoolDao;
    protected IDistrictDao _districtDao;
    private StateManager _stateManager;
    private final UrlUtil _urlUtil = new UrlUtil();


    /**
     * Provides a list of districts in a city. Currently it caps the list at 5. If there are
     * five or fewer, it sorts them alphabetically. If there are more than 5, it shows them
     * based on size.
     */
    public AnchorListModel createDistrictList(State state, String cityNameParam, String cityDisplayName, HttpServletRequest request) {
        AnchorListModel districts = new AnchorListModel();

        List list = _districtDao.findDistrictsInCity(state, cityNameParam, true);
        if (list != null) {

            boolean needViewAll = false;

 //           cityNameParam = cityNameParam.equals("washington") && state.equals(State.DC) ? 
 //                   "Washington, DC" : cityNameParam;

            if (list.size() <= 5) {
                _districtDao.sortDistrictsByName(list);
                districts.setHeading(cityDisplayName + " School Districts");
            } else {
                // Too many districts to show... just show the largest
                _districtDao.sortDistrictsByNumberOfSchools(list, false);
                list = list.subList(0, 4);
                districts.setHeading("Biggest " + cityDisplayName + " Districts");
                needViewAll = true;
            }

            for (Iterator iter = list.iterator(); iter.hasNext();) {
                District d = (District) iter.next();
                String url = DirectoryStructureUrlFactory.createNewDistrictHomeURI(d.getDatabaseState(), d);
                districts.add(new Anchor(url, StringEscapeUtils.escapeXml(d.getName())));
            }

            if (needViewAll) {
                String url = "/schools/districts/" + state.getLongName() + "/" + state.getAbbreviation() + "/";
                url = _urlUtil.buildUrl(url, request);
                districts.add(new Anchor(url, "View all " + state.getLongName() + " Districts", "viewall"));
            }
        }

        return districts;
    }

    /**
     * Summarizes the schools in a city. It creates an item for each of the following, if there are any:
     * <ul>
     * <li>preschools
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
        Set<SchoolType> schoolTypes = new HashSet<SchoolType>();

        sc = _schoolDao.countSchools(state, null, LevelCode.PRESCHOOL, cityName);
        if (sc > 0) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, state, cityName, schoolTypes, LevelCode.PRESCHOOL);
            String href = urlBuilder.asSiteRelative(request);
            final Anchor anchor = new Anchor(href, "Preschools");
            anchor.setAfter(" (" + sc + ")");
            schoolBreakdownAnchorList.add(anchor);
        }

        sc = _schoolDao.countSchools(state, null, LevelCode.ELEMENTARY, cityName);
        if (sc > 0) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, state, cityName, schoolTypes, LevelCode.ELEMENTARY);
            String href = urlBuilder.asSiteRelative(request);
            final Anchor anchor = new Anchor(href, "Elementary Schools");
            anchor.setAfter(" (" + sc + ")");
            schoolBreakdownAnchorList.add(anchor);
        }

        sc = _schoolDao.countSchools(state, null, LevelCode.MIDDLE, cityName);
        if (sc > 0) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, state, cityName, schoolTypes, LevelCode.MIDDLE);
            String href = urlBuilder.asSiteRelative(request);
            final Anchor anchor = new Anchor(href, "Middle Schools");
            anchor.setAfter(" (" + sc + ")");
            schoolBreakdownAnchorList.add(anchor);
        }

        sc = _schoolDao.countSchools(state, null, LevelCode.HIGH, cityName);
        if (sc > 0) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, state, cityName, schoolTypes, LevelCode.HIGH);
            String href = urlBuilder.asSiteRelative(request);
            final Anchor anchor = new Anchor(href, "High Schools");
            anchor.setAfter(" (" + sc + ")");
            schoolBreakdownAnchorList.add(anchor);
        }

        sc = _schoolDao.countSchools(state, SchoolType.PUBLIC, null, cityName) +
             _schoolDao.countSchools(state, SchoolType.CHARTER, null, cityName);
        if (sc > 0) {
            schoolTypes.clear();
            schoolTypes.add(SchoolType.PUBLIC);
            schoolTypes.add(SchoolType.CHARTER);
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, state, cityName, schoolTypes, null);
            String href = urlBuilder.asSiteRelative(request);
            final Anchor anchor = new Anchor(href, "Public Schools");
            anchor.setAfter(" (" + sc + ")");
            schoolBreakdownAnchorList.add(anchor);
        }

        sc = _schoolDao.countSchools(state, SchoolType.PRIVATE, null, cityName);
        if (sc > 0) {
            schoolTypes.clear();
            schoolTypes.add(SchoolType.PRIVATE);
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, state, cityName, schoolTypes, null);
            String href = urlBuilder.asSiteRelative(request);
            final Anchor anchor = new Anchor(href, "Private Schools");
            anchor.setAfter(" (" + sc + ")");
            schoolBreakdownAnchorList.add(anchor);
        }

        sc = _schoolDao.countSchools(state, SchoolType.CHARTER, null, cityName);
        if (sc > 0) {
            schoolTypes.clear();
            schoolTypes.add(SchoolType.CHARTER);
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, state, cityName, schoolTypes, null);
            String href = urlBuilder.asSiteRelative(request);
            final Anchor anchor = new Anchor(href, "Charter Schools");
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
     * Generates a list of links to schools in the given Hits cities.
     *
     * @throws IOException
     */
    public AnchorListModel createCitiesListModel(HttpServletRequest request,
                                                 Hits cityHits,
                                                 SchoolType schoolType,
                                                 int maxCities,
                                                 boolean showMore) throws IOException {
        AnchorListModel anchorListModel = new AnchorListModel("Schools in the city of: ");

        for (int i = 0; i < maxCities; i++) {
            if (cityHits != null && cityHits.length() > i) {
                Document cityDoc = cityHits.doc(i);
                String cityName = cityDoc.get("city");
                String s = cityDoc.get("state");
                State stateOfCity = _stateManager.getState(s);
                if(isGoodCity(cityName,s)){
                    UrlBuilder builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY,
                            stateOfCity, cityName, new HashSet<SchoolType>(), null);
                    cityName += ", " + stateOfCity;
                    anchorListModel.add(builder.asAnchor(request, cityName));
                }
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

    public boolean isGoodCity(String cityName,String s){
        Map<String,String> badCities = new HashMap<String,String>();
        badCities.put("laurel","DC");
        if(badCities.get(cityName.toLowerCase()) !=null
                && badCities.get(cityName.toLowerCase()).equalsIgnoreCase(s)){
            return false;
        }
        return true;
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
        AnchorListModel anchorListModel = new AnchorListModel("Schools in the district of:");

        for (int j = 0; j < maxDistricts; j++) {
            if (districtHits != null && districtHits.length() > j) {
                Document districtDoc = districtHits.doc(j);
                String id = districtDoc.get("id");

                String districtName = districtDoc.get("name");

                District district = new District();
                district.setName(districtName);
                district.setDatabaseState(state);
                Address address = new Address();
                address.setCity(districtDoc.get(Indexer.CITY));
                district.setPhysicalAddress(address);

                UrlBuilder builder = new UrlBuilder(district, UrlBuilder.SCHOOLS_IN_DISTRICT);
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
            if(isGoodCity(name,nearbyCity.getState().getAbbreviation())){
                UrlBuilder builder = new UrlBuilder(nearbyCity, UrlBuilder.CITY_PAGE);
                Anchor anchor = builder.asAnchor(request, name, styleClass);
                anchorListModel.add(anchor);
            }
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
    public AnchorListModel createNearbyCitiesWithRatingsAnchorListModel(final String heading, ICity city,
                                                                        List<NearbyCitiesController.CityAndRating> nearbyCities,
                                                                        int limit,
                                                                        final boolean alwaysIncludeState,
                                                                        final boolean includeMoreItem,
                                                                        final boolean includeBrowseAllItem,
                                                                        HttpServletRequest request) {
        AnchorListModel anchorListModel = new AnchorListModel(heading);

        for (int i = 0; i < limit && i < nearbyCities.size(); i++) {
            NearbyCitiesController.CityAndRating cityAndRating = nearbyCities.get(i);
            ICity nearbyCity = cityAndRating.getCity();
            CityRating rating = cityAndRating.getRating();

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
            if(isGoodCity(name,nearbyCity.getState().getAbbreviation())){
                UrlBuilder builder = new UrlBuilder(nearbyCity, UrlBuilder.CITY_PAGE);
                Anchor anchor = builder.asAnchor(request, name, styleClass);
                if (rating == null) {
                    anchor.setAfter("0");
                } else {
                    anchor.setAfter(String.valueOf(rating.getRating()));
                }
                anchorListModel.add(anchor);
            }
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

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
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
