package gs.web.geo;

import gs.data.geo.DistrictBoundary;
import gs.data.geo.IDistrictBoundaryDao;
import gs.data.geo.ISchoolBoundaryDao;
import gs.data.geo.SchoolBoundary;
import gs.data.json.JSONArray;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;
import gs.data.search.beans.IDistrictSearchResult;
import gs.data.search.services.DistrictSearchService;
import gs.data.state.State;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.DistrictRating;
import gs.data.test.rating.IDistrictRatingDao;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.util.UrlBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
@Controller
@RequestMapping("/geo/boundary/ajax/")
public class BoundaryAjaxController {
    protected static final Log _log = LogFactory.getLog(BoundaryAjaxController.class);

    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private IRatingsConfigDao _ratingsConfigDao;
    @Autowired
    private TestManager _testManager;
    @Autowired
    private IDistrictDao _districtDao;
    @Autowired
    private IDistrictRatingDao _districtRatingDao;
    @Autowired
    private IDistrictBoundaryDao _districtBoundaryDao;
    @Autowired
    private ISchoolBoundaryDao _schoolBoundaryDao;
    @Resource(name="solrDistrictSearchService")
    private DistrictSearchService _districtSearchService;

    @RequestMapping(value="getDistrictBoundaryById.page", method=RequestMethod.GET)
    public void getDistrictBoundaryById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        State state = State.fromString(request.getParameter("state"));
        Integer id = Integer.valueOf(request.getParameter("id"));
        DistrictBoundary districtBoundary = _districtBoundaryDao.getDistrictBoundaryByGSId(state, id);
        if (districtBoundary != null) {
            District district = _districtDao.findDistrictById(state, id);
            DistrictRating rating = _districtRatingDao.getDistrictRatingByDistrict(district);
            int ratingInt = 0;
            if (rating != null && rating.getActive() == 1) {
                ratingInt = rating.getRating();
            }
            districtBoundary.setDistrict(district);
            try {
                response.setContentType("application/json");
                MapPolygon polygon = getPolygonFromDistrict(districtBoundary, district, ratingInt);
                JSONObject rval = new JSONObject();
                JSONArray features = new JSONArray();
                rval.put("features", features);
                features.put(polygon.toJsonObject());
                rval.write(response.getWriter());
            } catch (Exception e) {
                _log.error("Error parsing geometry:" + e, e);
                response.setStatus(500);
            }
        } else {
            response.setStatus(404);
        }
    }

    @RequestMapping(value="getSchoolBoundaryById.page", method=RequestMethod.GET)
    public void getSchoolBoundaryById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        State state = State.fromString(request.getParameter("state"));
        Integer id = Integer.valueOf(request.getParameter("id"));
        LevelCode.Level schoolLevel = LevelCode.Level.getLevelCode(request.getParameter("level"));
        SchoolBoundary schoolBoundary = _schoolBoundaryDao.getSchoolBoundaryByGSId(state, id, schoolLevel);
        if (schoolBoundary != null) {
            School school = _schoolDao.getSchoolById(state, id);
            schoolBoundary.setSchool(school);
            try {
                response.setContentType("application/json");
                MapPolygon polygon = getPolygonFromSchool(schoolBoundary, school, getGSRating(school));
                JSONObject rval = new JSONObject();
                JSONArray features = new JSONArray();
                rval.put("features", features);
                features.put(polygon.toJsonObject());
                rval.write(response.getWriter());
            } catch (Exception e) {
                _log.error("Error parsing geometry:" + e, e);
                response.setStatus(500);
            }
        } else {
            response.setStatus(404);
        }
    }

    protected MapPolygon getPolygonFromSchool(SchoolBoundary boundary, School school, int ratingInt) throws JSONException {
        MapPolygon polygon = new MapPolygon(boundary.getGeometry());
        polygon.getData().put("state", school.getDatabaseState());
        polygon.getData().put("id", school.getId());
        polygon.getData().put("name", school.getName());
        polygon.getData().put("rating", ratingInt);
        polygon.getData().put("type", "school");
        return polygon;
    }

    @RequestMapping(value="getDistrictsForLocation.page", method=RequestMethod.GET)
    public void getDistrictsForLocation(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {
        response.setContentType("application/json");
        double lat = Double.valueOf(request.getParameter("lat"));
        double lon = Double.valueOf(request.getParameter("lon"));
        LevelCode.Level level = LevelCode.Level.getLevelCode(request.getParameter("level"));

        JSONObject output = new JSONObject();
        output.put("features", getDistrictsForLocation(lat, lon, level, request));
        output.write(response.getWriter());
    }

    protected JSONArray getDistrictsForLocation(double lat, double lon, LevelCode.Level level, HttpServletRequest request) throws JSONException {
        List<DistrictBoundary> districtBoundaries = _districtBoundaryDao.getDistrictBoundariesContainingPoint(lat, lon, level);
        JSONArray features = new JSONArray();
        for (DistrictBoundary boundary: districtBoundaries) {
            try {
                District district = _districtDao.findDistrictById(boundary.getState(), boundary.getDistrictId());
                DistrictRating rating = _districtRatingDao.getDistrictRatingByDistrict(district);
                boundary.setDistrict(district);

                int ratingInt = 0;
                if (rating != null && rating.getActive() == 1) {
                    ratingInt = rating.getRating();
                }
                MapMarker marker = getMarkerFromDistrict(district, ratingInt, request);

                MapPolygon polygon = getPolygonFromDistrict(boundary, district, ratingInt);
                marker.addDependent(polygon);

                features.put(marker.toJsonObject());
            } catch (ObjectRetrievalFailureException orfe) {
                _log.error("Can't find district " + boundary.getState() +"," + boundary.getDistrictId());
            }
        }
        return features;
    }

    protected MapPolygon getPolygonFromDistrict(DistrictBoundary boundary, District district, int ratingInt) throws JSONException {
        MapPolygon polygon = new MapPolygon(boundary.getGeometry());
        polygon.getData().put("state", district.getDatabaseState());
        polygon.getData().put("id", district.getId());
        polygon.getData().put("name", district.getName());
        polygon.getData().put("rating", ratingInt);
        polygon.getData().put("type", "district");
        return polygon;
    }

    protected MapMarker getMarkerFromDistrict(District district, int ratingInt, HttpServletRequest request) throws JSONException {
        return getMarkerForDistrict(district.getLat(), district.getLon(), district.getName(),
                district.getDatabaseState(), district.getId(), district.getPhysicalAddress().getCity(),
                district.getPhysicalAddress().getStreet(), district.getPhysicalAddress().getStreetLine2(),
                district.getPhysicalAddress().getCityStateZip(), ratingInt, request);
    }

    protected MapMarker getMarkerForDistrict(double lat, double lon, String name, State state, Integer id, String city, String street1, String street2, String cityStateZip, int ratingInt, HttpServletRequest request) throws JSONException {
        String icon = "/res/img/map/pushpin_na.png";
        if (ratingInt > 0) {
            icon = "/res/img/map/pushpin_" + ratingInt + ".png";
        }
        MapMarker marker = new MapMarker(lat, lon,
                icon, 40, 40);
        marker.setTooltip(name);
        marker.setOrigin(0, 0);
        marker.setAnchor(11, 34);
        marker.setShape(MapMarker.MarkerShapeType.poly, new int[] {0, 0, 30, 0, 30, 37, 0, 37});

        populateDistrictData(marker.getData(), state, id, name, city, street1, street2, cityStateZip, ratingInt, request);
        return marker;
    }

    protected void populateDistrictData(JSONObject data, State state, Integer id, String name, String city,
                                        String street1, String street2, String cityStateZip, int ratingInt,
                                        HttpServletRequest request) throws JSONException {
        data.put("state", state);
        data.put("id", id);
        data.put("name", name);
        data.put("rating", ratingInt);
        data.put("type", "district");
        UrlBuilder urlBuilder = new UrlBuilder(state, id, name, city, UrlBuilder.DISTRICT_HOME);
        data.put("url", urlBuilder.asSiteRelative(request));
        data.put("street1", street1);
        data.put("street2", street2);
        data.put("cityStateZip", cityStateZip);
    }

    protected MapMarker getMarkerFromDistrictSearch(IDistrictSearchResult district, int ratingInt, HttpServletRequest request) throws JSONException {
        return getMarkerForDistrict(district.getLatitude(), district.getLongitude(), district.getName(),
                district.getState(), district.getId(), district.getCity(), district.getAddress().getStreet(), district.getAddress().getStreetLine2(),
                district.getAddress().getCityStateZip(), ratingInt, request);
    }

    @RequestMapping(value="getSchoolsForLocation.page", method=RequestMethod.GET)
    public void getSchoolsForLocation(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {
        response.setContentType("application/json");
        double lat = Double.valueOf(request.getParameter("lat"));
        double lon = Double.valueOf(request.getParameter("lon"));
        LevelCode.Level level = LevelCode.Level.getLevelCode(request.getParameter("level"));

        List<SchoolBoundary> schoolBoundaries = _schoolBoundaryDao.getSchoolBoundariesContainingPoint(lat, lon, level);
        JSONObject output = new JSONObject();
        JSONArray features = new JSONArray();
        long start = System.currentTimeMillis();
        for (SchoolBoundary boundary: schoolBoundaries) {
            try {
                if (boundary.getSchoolId() == null) {
                    continue;
                }
                School school = _schoolDao.getSchoolById(boundary.getState(), boundary.getSchoolId());
                if (school == null) {
                    continue;
                }
                int rating = getGSRating(school);
                MapMarker marker = getMarkerFromSchool(school, null, rating, request);
                MapPolygon polygon = getPolygonFromSchool(boundary, school, rating);
                marker.addDependent(polygon);
                features.put(marker.toJsonObject());
            } catch (ObjectRetrievalFailureException orfe) {
                _log.error("Can't find school " + boundary.getState() +"," + boundary.getSchoolId());
            }
        }
        System.out.println("  getSchoolsForLocation fetchSchool,Rating,toJSON took " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();
        JSONArray districts = getDistrictsForLocation(lat, lon, level, request);
        if (districts != null) {
            for (int x=0; x < districts.length(); x++) {
                features.put(districts.get(x));
            }
        }
        System.out.println("  getSchoolsForLocation addingDistrict took " + (System.currentTimeMillis() - start) + " ms");
        output.put("features", features);
        output.write(response.getWriter());
    }

    @RequestMapping(value="getDistrictsNearPoint.page", method=RequestMethod.GET)
    public void getDistrictsNearPoint(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        try {
            float lat = Float.valueOf(request.getParameter("lat"));
            float lon = Float.valueOf(request.getParameter("lon"));
            LevelCode.Level districtLevel = null;
            if (request.getParameter("level") != null) {
                districtLevel = LevelCode.Level.getLevelCode(request.getParameter("level"));
            }
            SearchResultsPage<IDistrictSearchResult> resultsPage =
                    _districtSearchService.getDistrictsNear(lat, lon, 50, null, districtLevel, 0, 30);
            JSONObject output = new JSONObject();
            JSONArray mapObjects = new JSONArray();
            for (IDistrictSearchResult searchResult: resultsPage.getSearchResults()) {
                District districtFacade = new District();
                districtFacade.setId(searchResult.getId());
                districtFacade.setDatabaseState(searchResult.getState());
                DistrictRating rating = _districtRatingDao.getDistrictRatingByDistrict(districtFacade);
                int ratingInt = 0;
                if (rating != null && rating.getActive() == 1) {
                    ratingInt = rating.getRating();
                }
                MapMarker marker = getMarkerFromDistrictSearch(searchResult, ratingInt, request);
                mapObjects.put(marker.toJsonObject());
            }
            output.put("features", mapObjects);
            output.write(response.getWriter());
            return;
        } catch (NumberFormatException nfe) {
            _log.error("Error parsing params: " + nfe, nfe);
        } catch (SearchException se) {
            _log.error("Error searching: " + se, se);
        } catch (Exception e) {
            _log.error("Unexpected error: " + e, e);
        }
        response.setStatus(500);
    }

    @RequestMapping(value="getSchoolsForDistrict.page", method=RequestMethod.GET)
    public void getSchoolsForDistrict(HttpServletRequest request,
                                      HttpServletResponse response) throws IOException, JSONException {
        response.setContentType("application/json");
        State state = State.fromString(request.getParameter("state"));
        Integer districtId = Integer.valueOf(request.getParameter("districtId"));
        LevelCode.Level schoolLevel = LevelCode.Level.getLevelCode(request.getParameter("level"));

        JSONObject rval = new JSONObject();
        JSONArray features = new JSONArray();

        long start = System.currentTimeMillis();
        District district = _districtDao.findDistrictById(state, districtId);
        if (district != null) {
            List<School> schools = _schoolDao.getSchoolsInDistrict(state, districtId, true, schoolLevel);
            System.out.println(" getSchoolsForDistrict DB took " + (System.currentTimeMillis() - start) + " ms");

            if (schools != null && schools.size() > 0) {
                start = System.currentTimeMillis();
                for (School s: schools) {
                    int ratingInt = getGSRating(s);
                    MapMarker marker = getMarkerFromSchool(s, district, ratingInt, request);
                    features.put(marker.toJsonObject());
                }
                System.out.println(" getSchoolsForDistrict ratings took " + (System.currentTimeMillis() - start) + " ms");
            }
        }
        rval.put("features", features);
        rval.write(response.getWriter());
    }

    protected MapMarker getMarkerFromSchool(School s, District district, int ratingInt,
                                            HttpServletRequest request) throws JSONException {
        String icon = "/res/img/map/GS_gsr_na_forground.png";
        if (ratingInt > 0) {
            icon = "/res/img/map/GS_gsr_" + ratingInt + "_forground.png";
        }
        MapMarker marker = new MapMarker(s.getLat(), s.getLon(),
                icon, 40, 40);
        marker.setTooltip(s.getName());
        marker.setOrigin(0, 0);
        marker.setAnchor(11, 34);
        marker.setShape(MapMarker.MarkerShapeType.poly, new int[] {0, 0, 30, 0, 30, 37, 0, 37});

        populateSchoolData(marker.getData(), s, ratingInt, district, request);
        return marker;
    }

    protected void populateSchoolData(JSONObject data, School school, int rating, District district,
                                      HttpServletRequest request) throws JSONException {
        data.put("state", school.getDatabaseState());
        data.put("id", school.getId());
        data.put("name", school.getName());
        data.put("rating", rating);
        data.put("type", "school");
        UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        data.put("url", urlBuilder.asSiteRelative(request));
        JSONObject address = new JSONObject();
        address.put("street1", school.getPhysicalAddress().getStreet());
        address.put("street2", school.getPhysicalAddress().getStreetLine2());
        address.put("cityStateZip", school.getPhysicalAddress().getCityStateZip());
        data.put("address", address);
        if (district != null) {
            data.put("districtName", district.getName());
            data.put("districtId", district.getId());
        } else {
            data.put("districtId", school.getDistrictId());
        }
    }

    @RequestMapping(value="getSchoolBoundariesForDistrict.page", method=RequestMethod.GET)
    public void getSchoolBoundariesForDistrict(HttpServletRequest request,
                                               HttpServletResponse response) throws IOException, JSONException {
        long start = System.currentTimeMillis();
        response.setContentType("application/json");
        State state = State.fromString(request.getParameter("state"));
        Integer districtId = Integer.valueOf(request.getParameter("districtId"));
        LevelCode.Level schoolLevel = LevelCode.Level.getLevelCode(request.getParameter("level"));

        District district = _districtDao.findDistrictById(state, districtId);
        if (district != null) {
            JSONObject rval = new JSONObject();
            JSONArray features = new JSONArray();

            long dbStart = System.currentTimeMillis();
            List<School> schools = _schoolDao.getSchoolsInDistrict(state, districtId, true, schoolLevel);
            long dbEnd = System.currentTimeMillis();
            long sazStart = 0;
            long sazEnd = 0;
            if (schools.size() > 0) {
                Map<Integer, SchoolBoundary> idSchoolBoundaryMap = new HashMap<Integer, SchoolBoundary>(schools.size());
                sazStart = System.currentTimeMillis();
                List<SchoolBoundary> schoolBoundaries = _schoolBoundaryDao.getSchoolBoundaries(schools, schoolLevel);
                sazEnd = System.currentTimeMillis();
                System.out.println("    (" + districtId + ") Of " + schools.size() + " schools in DB, " + schoolBoundaries.size() + " have SAZ info");
                for (SchoolBoundary boundary: schoolBoundaries) {
                    idSchoolBoundaryMap.put(boundary.getSchoolId(), boundary);
                }
                for (School s: schools) {
                    SchoolBoundary boundary = idSchoolBoundaryMap.get(s.getId());
                    int rating = getGSRating(s);
                    MapMarker marker = getMarkerFromSchool(s, district, rating, request);
                    if (boundary != null) {
                        MapPolygon polygon = getPolygonFromSchool(boundary, s, rating);
                        marker.addDependent(polygon);
                    }
                    features.put(marker.toJsonObject());
                }
            }
            rval.put("features", features);
            rval.write(response.getWriter());
            System.out.println("    (" + districtId + ") getSchoolBoundariesForDistrict took " + (System.currentTimeMillis()-start) + " ms");
            System.out.println("      (" + districtId + ") DB took " + (dbEnd-dbStart) + " ms");
            System.out.println("      (" + districtId + ") SAZ took " + (sazEnd-sazStart) + " ms");
        }
    }

    protected int getGSRating(School school) {
        try {
            IRatingsConfig ratingsConfig = _ratingsConfigDao.restoreRatingsConfig(school.getDatabaseState(), true);

            if (null != ratingsConfig) {
                SchoolTestValue schoolTestValue =
                        _testManager.getOverallRating(school, ratingsConfig.getYear());

                if (null != schoolTestValue && null != schoolTestValue.getValueInteger()) {
                    return schoolTestValue.getValueInteger();
                }
            }
        } catch (IOException ioe) {
            _log.error("Error determining GS rating for " + school + ": " + ioe, ioe);
            // fall through
        }
        return 0;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IRatingsConfigDao getRatingsConfigDao() {
        return _ratingsConfigDao;
    }

    public void setRatingsConfigDao(IRatingsConfigDao ratingsConfigDao) {
        _ratingsConfigDao = ratingsConfigDao;
    }

    public TestManager getTestManager() {
        return _testManager;
    }

    public void setTestManager(TestManager testManager) {
        _testManager = testManager;
    }

    public IDistrictDao getDistrictDao() {
        return _districtDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }

    public IDistrictRatingDao getDistrictRatingDao() {
        return _districtRatingDao;
    }

    public void setDistrictRatingDao(IDistrictRatingDao districtRatingDao) {
        _districtRatingDao = districtRatingDao;
    }

    public DistrictSearchService getDistrictSearchService() {
        return _districtSearchService;
    }

    public void setDistrictSearchService(DistrictSearchService districtSearchService) {
        _districtSearchService = districtSearchService;
    }

    public IDistrictBoundaryDao getDistrictBoundaryDao() {
        return _districtBoundaryDao;
    }

    public void setDistrictBoundaryDao(IDistrictBoundaryDao districtBoundaryDao) {
        _districtBoundaryDao = districtBoundaryDao;
    }

    public ISchoolBoundaryDao getSchoolBoundaryDao() {
        return _schoolBoundaryDao;
    }

    public void setSchoolBoundaryDao(ISchoolBoundaryDao schoolBoundaryDao) {
        _schoolBoundaryDao = schoolBoundaryDao;
    }
}
