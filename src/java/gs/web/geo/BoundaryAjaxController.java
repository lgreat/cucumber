package gs.web.geo;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import gs.data.geo.*;
import gs.data.json.JSONArray;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.*;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;
import gs.data.search.beans.IDistrictSearchResult;
import gs.data.search.beans.ISchoolSearchResult;
import gs.data.search.beans.SolrSchoolSearchResult;
import gs.data.search.services.DistrictSearchService;
import gs.data.search.services.SchoolSearchService;
import gs.data.state.State;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.DistrictRating;
import gs.data.test.rating.IDistrictRatingDao;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
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
    @Resource(name="solrSchoolSearchService")
    private SchoolSearchService _schoolSearchService;

    /**
     *
     * @param lat
     * @param lon
     * @param level
     * @return
     * @throws SearchException
     * @throws JSONException
     */
    /**
     * Return a list of districts near a location.
     */
    @RequestMapping(value="getDistrictsNearLocation.json", method=RequestMethod.GET, headers={})
    public ModelAndView getDistrictsNearPoint(
            @RequestParam("lat") Float lat,
            @RequestParam("lon") Float lon,
            @RequestParam(value="level", required = false) String level,
            Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            LevelCode.Level districtLevel = null;
            if (StringUtils.isBlank(level)){
                level = "e";
            }
            districtLevel = LevelCode.Level.getLevelCode(level);

            SearchResultsPage<IDistrictSearchResult> resultsPage =
                    _districtSearchService.getNonCharterDistrictsNear(lat, lon, 50, null, districtLevel, 0, 30);

            List<Map> mapObjects = new ArrayList();
            for (IDistrictSearchResult searchResult: resultsPage.getSearchResults()) {
                District districtFacade = new District();
                districtFacade.setId(searchResult.getId());
                districtFacade.setDatabaseState(searchResult.getState());
                DistrictRating rating = _districtRatingDao.getDistrictRatingByDistrict(districtFacade);
                int ratingInt = 0;
                if (rating != null && rating.getActive() == 1) {
                    ratingInt = rating.getRating();
                }
                mapObjects.add(map(searchResult, ratingInt, request));
            }
            model.addAttribute("districts", mapObjects);
            return prepareView(model, response);

        } catch (NumberFormatException nfe) {
            _log.error("Error parsing params: " + nfe, nfe);
        } catch (SearchException se) {
            _log.error("Error searching: " + se, se);
        } catch (Exception e) {
            _log.error("Unexpected error: " + e, e);
        }
        response.setStatus(500);
        return prepareView(model, response);
    }

    /**
     * Return a list of district boundaries intersecting a location.
     */
    @RequestMapping(value="getDistrictsForLocation.json", method=RequestMethod.GET)
    public ModelAndView getDistrictsForLocation(
            @RequestParam("lat") Double lat, @RequestParam("lon") Double lon, @RequestParam("level") String levelParam,
            Model model, HttpServletRequest request, HttpServletResponse response) {

        response.setHeader("Cache-Control", "public, max-age=3600");

        LevelCode.Level level = LevelCode.Level.getLevelCode(levelParam);
        List<DistrictBoundary> districtBoundaries = _districtBoundaryDao.getDistrictBoundariesContainingPoint(lat, lon, level);

        List<Map> districts = new ArrayList();

        for (DistrictBoundary boundary: districtBoundaries) {
            try {
                District district = _districtDao.findDistrictById(boundary.getState(), boundary.getDistrictId());
                if (district.isCharterOnly()) {
                    _log.warn("Skipping charter only district: " + district.getName());
                    continue;
                }
                DistrictRating rating = _districtRatingDao.getDistrictRatingByDistrict(district);
                boundary.setDistrict(district);

                int ratingInt = 0;
                if (rating != null && rating.getActive() == 1) {
                    ratingInt = rating.getRating();
                }
                Map map = map(district, ratingInt, request);
                map.put("coordinates", map(boundary.getGeometry()));
                districts.add(map);
            } catch (ObjectRetrievalFailureException orfe) {
                _log.error("Can't find district " + boundary.getState() +"," + boundary.getDistrictId());
            }
        }
        model.addAttribute("districts", districts);
        return prepareView(model, response);
    }

    @RequestMapping( value = "getDistrictBoundaryById.json")
    public ModelAndView getDistrictBoundaryById(
            @RequestParam("state") String stateParam,
            @RequestParam("id") Integer idParam,
            Model model, HttpServletRequest request, HttpServletResponse response ){

        response.setHeader("Cache-Control", "public, max-age=3600");

        State state = State.fromString(stateParam);
        Integer id = Integer.valueOf(idParam);
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

                model.addAttribute("boundary", map(districtBoundary.getGeometry()));
                return prepareView(model, response);
            } catch (Exception e) {
                _log.error("Error parsing geometry:" + e, e);
                response.setStatus(500);
            }
        } else {
            response.setStatus(404);
        }
        return prepareView(model, response);
    }

    @RequestMapping( value = "getDistrictById.json")
    public ModelAndView getDistrictById(
            @RequestParam("state") String stateParam,
            @RequestParam("id") Integer idParam,
            Model model, HttpServletRequest request, HttpServletResponse response ){

        response.setHeader("Cache-Control", "public, max-age=3600");

        List<Map> districts = new ArrayList();

        State state = State.fromString(stateParam);
        Integer id = Integer.valueOf(idParam);
        District district = _districtDao.findDistrictById(state, id);
        DistrictRating rating = _districtRatingDao.getDistrictRatingByDistrict(district);
        int ratingInt = 0;
        if (rating != null && rating.getActive() == 1) {
            ratingInt = rating.getRating();
        }

        List<School> results = _schoolDao.getSchoolsInDistrict(state, id, true);

        Map districtMap = map(district, ratingInt, request);
        try {
            DistrictBoundary boundary = _districtBoundaryDao.getDistrictBoundaryByGSId(state, id);
            districtMap.put("coordinates", map(boundary.getGeometry()));
        } catch (ObjectRetrievalFailureException e ) {
            _log.error("Error getting district boundary");
        }
        districts.add(districtMap);
        model.addAttribute("districts", districts);
        return prepareView(model,response);
    }

    @RequestMapping("getSchoolsByDistrictId.json")
    public ModelAndView getSchoolsByDistrictId(
            @RequestParam("state") String stateParam,
            @RequestParam("id") Integer id,
            @RequestParam("level") String levelParam,
            Model model, HttpServletRequest request, HttpServletResponse response ) {

        response.setHeader("Cache-Control", "public, max-age=3600");

        State state = State.fromString(stateParam);
        LevelCode.Level level = LevelCode.Level.getLevelCode(levelParam);
        List<Map> schools = new ArrayList();

        District district = _districtDao.findDistrictById(state, id);
        if (district != null) {
            List<School> results = _schoolDao.getSchoolsInDistrict(state, id, true, level);

            List<SchoolWithRatings> schoolsWithRatings = _schoolDao.populateSchoolsWithRatingsNewGSRating
                    (district.getDatabaseState(), results);

            if (schoolsWithRatings != null && schoolsWithRatings.size() > 0) {
                for (SchoolWithRatings s: schoolsWithRatings) {
                    schools.add(map(s.getSchool(), district, s.getRating().intValue(), request));
                }
            }
        }

        model.addAttribute("schools", schools);
        return prepareView(model,response);
    }

    @RequestMapping("getSchoolById.json")
    public ModelAndView getSchoolsForLocation(
            @RequestParam("state") String stateParam,
            @RequestParam("id") Integer id,
            @RequestParam("level") String levelParam,
            Model model, HttpServletRequest request, HttpServletResponse response ){

        response.setHeader("Cache-Control", "public, max-age=3600");

        State state = State.fromString(stateParam);
        LevelCode.Level level = LevelCode.Level.getLevelCode(levelParam);
        List<Map> features = new ArrayList<Map>();
        List<School> schools = new ArrayList();

        try {
            schools.add(_schoolDao.getSchoolById(state, id));
            List<SchoolWithRatings> schoolsWithRatings = _schoolDao.populateSchoolsWithRatingsNewGSRating(state, schools);
            for (SchoolWithRatings s : schoolsWithRatings){
                Map schoolMap = map(s.getSchool(), null, s.getRating(), request);
                SchoolBoundary schoolBoundary = _schoolBoundaryDao.getSchoolBoundaryByGSId(state, id, level);
                if (schoolBoundary!=null){
                    schoolMap.put("coordinates", map(schoolBoundary.getGeometry()));
                }
                features.add(schoolMap);
            }
        } catch (ObjectRetrievalFailureException e) {
            _log.error("Can't find school " + state + "," + id);
        }
        model.addAttribute("schools", features);
        return prepareView(model,response);
    }

    @RequestMapping("getSchoolBoundaryById.json")
    public ModelAndView getSchoolBoundaryById(
            @RequestParam("state") String stateParam,
            @RequestParam("id") Integer id,
            @RequestParam("level") String levelParam,
            Model model, HttpServletRequest request, HttpServletResponse response ){

        response.setHeader("Cache-Control", "public, max-age=3600");

        State state = State.fromString(stateParam);
        LevelCode.Level schoolLevel = LevelCode.Level.getLevelCode(levelParam);
        SchoolBoundary schoolBoundary = _schoolBoundaryDao.getSchoolBoundaryByGSId(state, id, schoolLevel);
        if (schoolBoundary != null) {
            model.addAttribute("boundary", map(schoolBoundary.getGeometry()));
        } else {
            response.setStatus(404);
        }
        return prepareView(model,response);
    }

    @RequestMapping("getSchoolByLocation.json")
    public ModelAndView getSchoolForLocation(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            @RequestParam("level") String levelParam,
            Model model, HttpServletRequest request, HttpServletResponse response ){
        response.setHeader("Cache-Control", "public, max-age=3600");
        try {
            getSchoolsForLocation(lat, lon, levelParam,  model, request, response);
        } catch (TopologyException e) {
            _log.warn("Topology Exception for lat " + lat + " and lon " + lon + " and level " + levelParam);
        }
        List<Map> schools = (List<Map>) model.asMap().get("schools");
        List<Map> result = new ArrayList();
        Map school = new HashMap();
        if (schools!=null && schools.size()>0){
            school = schools.get(0);
            boolean duplicate = false;
            for (int i=1; i<schools.size(); i++){
                Double area = (Double)((Map)school.get("coordinates")).get("area");
                Double compare = (Double)((Map)schools.get(i).get("coordinates")).get("area");
                if (area > compare) {
                    school = schools.get(i);
                    duplicate = false;
                } else if (area.equals(compare)) {
                    duplicate = true;
                }
            }
            if (!duplicate){
                result.add(school);
            }
        }
        model.asMap().remove("schools");
        model.addAttribute("schools", result);

        return prepareView(model,response);
    }

    @RequestMapping("getSchoolsByLocation.json")
    public ModelAndView getSchoolsForLocation(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            @RequestParam("level") String levelParam,
            Model model, HttpServletRequest request, HttpServletResponse response ){
        response.setHeader("Cache-Control", "public, max-age=3600");

        LevelCode.Level level = LevelCode.Level.getLevelCode(levelParam);
        List<SchoolBoundary> schoolBoundaries = _schoolBoundaryDao.getSchoolBoundariesContainingPoint(lat, lon, level);
        List<Map> features = new ArrayList();
        Map<Integer, SchoolBoundary> schoolIdToBoundaryMap = new HashMap<Integer, SchoolBoundary>(schoolBoundaries.size());
        List<School> schools = new ArrayList<School>(schoolBoundaries.size());
        State state = null;
        for (SchoolBoundary boundary: schoolBoundaries) {
            try {
                if (boundary.getSchoolId() == null) {
                    continue;
                }
                School school = _schoolDao.getSchoolById(boundary.getState(), boundary.getSchoolId());
                if (school == null) {
                    continue;
                }
                schools.add(school);
                schoolIdToBoundaryMap.put(boundary.getSchoolId(), boundary);
                state = school.getDatabaseState();
            } catch (ObjectRetrievalFailureException orfe) {
                _log.error("Can't find school " + boundary.getState() + "," + boundary.getSchoolId());
            }
        }
        if (schools.size() > 0) {
            List<SchoolWithRatings> schoolsWithRatings = _schoolDao.populateSchoolsWithRatingsNewGSRating(state, schools);
            for (SchoolWithRatings s: schoolsWithRatings) {
                Map mapping = map(s.getSchool(), null, s.getRating(), request);
                mapping.put("coordinates", map(schoolIdToBoundaryMap.get(s.getSchool().getId()).getGeometry()));
                features.add(mapping);
            }
        }
        model.addAttribute("schools", features);
        return prepareView(model,response);
    }

    /**
     * Return a list of private schools near the specified location that do not belong to a district.
     */
    @RequestMapping(value="getPrivateSchoolsNearLocation.json", method=RequestMethod.GET)
    public ModelAndView getPrivateSchoolsNearLocation(
            @RequestParam("lat") float lat,
            @RequestParam("lon") float lon,
            @RequestParam("level") String levelParam,
            @RequestParam(value="limit", required = false, defaultValue = "50") int limit,
            @RequestParam(value="radius", required = false, defaultValue = "10") int radius,
            Model model, HttpServletRequest request, HttpServletResponse response ) {

        try {
            getNonDistrictSchoolsNearLocation(SchoolType.PRIVATE, lat, lon, levelParam, limit, radius, model, request, response);
        } catch (SearchException e) {
            _log.error("Error searching ", e);
        }
        return prepareView(model,response);
    }

    @RequestMapping(value="getCharterSchoolsNearLocation.json", method=RequestMethod.GET)
    public ModelAndView getCharterSchoolsNearLocation(
            @RequestParam("lat") float lat,
            @RequestParam("lon") float lon,
            @RequestParam("level") String levelParam,
            @RequestParam(value="limit", required = false, defaultValue = "50") int limit,
            @RequestParam(value="radius", required = false, defaultValue = "10") int radius,
            Model model, HttpServletRequest request, HttpServletResponse response ) {

        try {
            getNonDistrictSchoolsNearLocation(SchoolType.CHARTER, lat, lon, levelParam, limit, radius, model, request, response);
        } catch (SearchException e) {
            _log.error("Error searching", e);
        }
        return prepareView(model,response);
    }

    protected ModelAndView prepareView(Model model, HttpServletResponse response){
        MappingJacksonJsonView jsonView = new MappingJacksonJsonView();
        jsonView.setDisableCaching(false);
        ModelAndView modelAndView = new ModelAndView(jsonView);
        modelAndView.addAllObjects(model.asMap());
        response.setHeader("Cache-Control", "public, max-age=3600");
        return modelAndView;
    }

    protected Map<String, Integer> getSchoolCounts(State state, Integer id) {
        Map<String, Integer> result = new HashMap();
        result.put(LevelCode.Level.ELEMENTARY_LEVEL.getName(), new Integer(0));
        result.put(LevelCode.Level.MIDDLE_LEVEL.getName(), new Integer(0));
        result.put(LevelCode.Level.HIGH_LEVEL.getName(), new Integer(0));

        List<School> schools = _schoolDao.getSchoolsInDistrict(state, id, true);
        for (int i=0; i<schools.size(); i++) {
            School school = schools.get(i);
            if (school.getLevelCode().containsLevelCode(LevelCode.Level.ELEMENTARY_LEVEL)){
                String code = LevelCode.Level.ELEMENTARY_LEVEL.getName();
                result.put(code, result.get(code)+1);
            }
            if (school.getLevelCode().containsLevelCode(LevelCode.Level.MIDDLE_LEVEL)){
                String code = LevelCode.Level.MIDDLE_LEVEL.getName();
                result.put(code, result.get(code)+1);
            }
            if (school.getLevelCode().containsLevelCode(LevelCode.Level.HIGH_LEVEL)){
                String code = LevelCode.Level.HIGH_LEVEL.getName();
                result.put(code, result.get(code)+1);
            }
        }
        return result;
    }

    protected void getNonDistrictSchoolsNearLocation( SchoolType schoolType,
            float lat, float lon, String levelParam, int limit, int radius,
            Model model, HttpServletRequest request, HttpServletResponse response) throws SearchException {

        LevelCode.Level schoolLevel = LevelCode.Level.getLevelCode(levelParam);
        if ( limit > 1000 ) {
            limit = 1000;
        }

        if (radius > 100) {
            radius = 100;
        }

        SearchResultsPage<SolrSchoolSearchResult> resultsPage =
                _schoolSearchService.getNonDistrictSchoolsNear(lat, lon, radius, schoolLevel, schoolType, 0, limit);

        List<Map> features = new ArrayList();
        for (SolrSchoolSearchResult searchResult: resultsPage.getSearchResults()) {
            int rating = 0;
            if (searchResult.getGreatSchoolsRating() != null) {
                rating = searchResult.getGreatSchoolsRating();
            }
            features.add(map(searchResult, null, rating, request));
        }
        model.addAttribute("schools", features);
    }

    private Map map(School school, District district, int rating, HttpServletRequest request){
        Map map = new HashMap();
        map.put("state", school.getDatabaseState().toString());
        map.put("id", school.getId());
        map.put("name", school.getName());
        map.put("rating", rating);
        map.put("lat", school.getLat());
        map.put("lon", school.getLon());
        map.put("type", "school");
        map.put("schoolType", school.getType().getSchoolTypeName());
        UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        map.put("url", urlBuilder.asSiteRelative(request));
        Map address = new HashMap();
        address.put("street1", school.getPhysicalAddress().getStreet());
        address.put("street2", school.getPhysicalAddress().getStreetLine2());
        address.put("cityStateZip", school.getPhysicalAddress().getCityStateZip());
        address.put("zip", school.getPhysicalAddress().getZip());
        map.put("address", address);
        if (district != null) {
            map.put("districtName", district.getName());
            map.put("districtId", district.getId());
        } else {
            map.put("districtId", school.getDistrictId());
        }
        return map;
    }

    private Map map ( ISchoolSearchResult school, District district, int rating, HttpServletRequest request) {
        Map data = new HashMap();
        data.put("state", school.getDatabaseState().toString());
        data.put("id", school.getId());
        data.put("lat", school.getLatLon().getLat());
        data.put("lon", school.getLatLon().getLon());
        data.put("name", school.getName());
        data.put("rating", rating);
        data.put("type", "school");
        data.put("schoolType", school.getSchoolType());
        School schoolFacade = new School();
        schoolFacade.setId(school.getId());
        schoolFacade.setDatabaseState(school.getDatabaseState());
        schoolFacade.setName(school.getName());
        schoolFacade.setPhysicalAddress(school.getAddress());
        schoolFacade.setLevelCode(LevelCode.createLevelCode(school.getLevelCode()));
        UrlBuilder urlBuilder = new UrlBuilder(schoolFacade, UrlBuilder.SCHOOL_PROFILE);
        data.put("url", urlBuilder.asSiteRelative(request));
        Map address = new HashMap();
        address.put("street1", school.getAddress().getStreet());
        address.put("street2", school.getAddress().getStreetLine2());
        address.put("cityStateZip", school.getAddress().getCityStateZip());
        address.put("zip", school.getAddress().getZip());
        data.put("address", address);
        if (district != null) {
            data.put("districtName", district.getName());
            data.put("districtId", district.getId());
        } else {
            data.put("districtId", school.getDistrictId());
        }
        return data;
    }


    private Map map(Geometry geometry){
        Map map = new HashMap();
        map.put("coordinates", BoundaryUtil.geometryToList(geometry));
        Map centroid = new HashMap();
        centroid.put("lon", geometry.getCentroid().getX());
        centroid.put("lat", geometry.getCentroid().getY());
        map.put("area", geometry.getArea());
        map.put("centroid", centroid);
        return map;
    }

    protected Map map(District district, int ratingInt, HttpServletRequest request) {
        Map map = new HashMap();
        Map address = new HashMap();
        address.put("street1", district.getPhysicalAddress().getStreet());
        address.put("street2", district.getPhysicalAddress().getStreetLine2());
        address.put("cityStateZip", district.getPhysicalAddress().getCityStateZip());
        address.put("zip", district.getPhysicalAddress().getZip());
        map.put("address", address);

        map.put("type", "district");
        map.put("lat", district.getLat());
        map.put("lon", district.getLon());
        map.put("name", district.getName());
        map.put("state", district.getDatabaseState().toString());
        map.put("id", district.getId());
        map.put("rating", ratingInt);
        UrlBuilder urlBuilder = new UrlBuilder(district.getDatabaseState(), district.getId(), district.getName(), district.getPhysicalAddress().getCity(), UrlBuilder.DISTRICT_HOME);
        map.put("url", urlBuilder.asSiteRelative(request));
        map.put("elementary", _schoolDao.getSchoolCountInDistrict(district.getDatabaseState(), district.getId(), LevelCode.Level.ELEMENTARY_LEVEL));
        map.put("middle", _schoolDao.getSchoolCountInDistrict(district.getDatabaseState(), district.getId(), LevelCode.Level.MIDDLE_LEVEL));
        map.put("high", _schoolDao.getSchoolCountInDistrict(district.getDatabaseState(), district.getId(), LevelCode.Level.HIGH_LEVEL));

        return map;
    }


    protected Map map(IDistrictSearchResult district, int ratingInt, HttpServletRequest request) {
        Map map = new HashMap();

        Map address = new HashMap();
        address.put("street1", district.getAddress().getStreet());
        address.put("street2", district.getAddress().getStreetLine2());
        address.put("cityStateZip", district.getAddress().getCityStateZip());
        address.put("zip", district.getAddress().getZip());
        map.put("address", address);

        map.put("type", "district");
        map.put("lat", district.getLatitude());
        map.put("lon", district.getLongitude());
        map.put("name", district.getName());
        map.put("state", district.getState().toString());
        map.put("id", district.getId());
        map.put("rating", ratingInt);
        UrlBuilder urlBuilder = new UrlBuilder(district.getState(), district.getId(), district.getName(), district.getCity(), UrlBuilder.DISTRICT_HOME);
        map.put("url", urlBuilder.asSiteRelative(request));

        map.put("elementary", district.getNumberOfElementarySchools());
        map.put("middle", district.getNumberOfMiddleSchools());
        map.put("high", district.getNumberOfHighSchools());

        return map;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
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

    public SchoolSearchService getSchoolSearchService() {
        return _schoolSearchService;
    }

    public void setSchoolSearchService(SchoolSearchService schoolSearchService) {
        _schoolSearchService = schoolSearchService;
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