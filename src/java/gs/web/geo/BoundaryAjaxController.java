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

    @RequestMapping(value="getDistrictBoundary.page", method=RequestMethod.GET)
    public void getDistrictBoundary(HttpServletRequest request, HttpServletResponse response) throws IOException {
        State state = State.fromString(request.getParameter("state"));
        Integer id = Integer.valueOf(request.getParameter("id"));
        DistrictBoundary districtBoundary = _districtBoundaryDao.getDistrictBoundaryByGSId(state, id);
        if (districtBoundary != null) {
            District district = _districtDao.findDistrictById(state, id);
            districtBoundary.setDistrict(district);
            try {
                response.setContentType("application/json");
                response.getWriter().write(districtBoundary.toJson());
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
                JSONObject output = schoolBoundary.toJsonObject();
                output.put("rating", getGSRating(school));
                output.write(response.getWriter());
            } catch (Exception e) {
                _log.error("Error parsing geometry:" + e, e);
                response.setStatus(500);
            }
        } else {
            response.setStatus(404);
        }
    }

    @RequestMapping(value="getDistrictsForLocation.page", method=RequestMethod.GET)
    public void getDistrictsForLocation(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {
        response.setContentType("application/json");
        double lat = Double.valueOf(request.getParameter("lat"));
        double lon = Double.valueOf(request.getParameter("lon"));
        LevelCode.Level districtLevel = null;
        if (request.getParameter("level") != null) {
            districtLevel = LevelCode.Level.getLevelCode(request.getParameter("level"));
        }

        List<DistrictBoundary> districtBoundaries = _districtBoundaryDao.getDistrictBoundariesContainingPoint(lat, lon, districtLevel);
        JSONObject output = new JSONObject();
        JSONArray districtsJson = new JSONArray();
        for (DistrictBoundary boundary: districtBoundaries) {
            try {
                District district = _districtDao.findDistrictById(boundary.getState(), boundary.getDistrictId());
                DistrictRating rating = _districtRatingDao.getDistrictRatingByDistrict(district);
                boundary.setDistrict(district);
                JSONObject districtJson = boundary.toJsonObject();
                districtJson.put("lat", district.getLat());
                districtJson.put("lon", district.getLon());
                if (rating != null && rating.getActive() == 1) {
                    districtJson.put("rating", rating.getRating());
                } else {
                    districtJson.put("rating", 0);
                }
                districtsJson.put(districtJson);
            } catch (ObjectRetrievalFailureException orfe) {
                _log.error("Can't find district " + boundary.getState() +"," + boundary.getDistrictId());
            }
        }
        output.put("districts", districtsJson);
        output.write(response.getWriter());
    }

    @RequestMapping(value="getSchoolsForLocation.page", method=RequestMethod.GET)
    public void getSchoolsForLocation(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {
        response.setContentType("application/json");
        double lat = Double.valueOf(request.getParameter("lat"));
        double lon = Double.valueOf(request.getParameter("lon"));
        LevelCode.Level schoolLevel = null;
        if (request.getParameter("level") != null) {
            schoolLevel = LevelCode.Level.getLevelCode(request.getParameter("level"));
        }

        List<SchoolBoundary> schoolBoundaries = _schoolBoundaryDao.getSchoolBoundariesContainingPoint(lat, lon, schoolLevel);
        JSONObject output = new JSONObject();
        JSONArray schoolsJson = new JSONArray();
        for (SchoolBoundary boundary: schoolBoundaries) {
            try {
                if (boundary.getSchoolId() == null) {
                    continue;
                }
                School school = _schoolDao.getSchoolById(boundary.getState(), boundary.getSchoolId());
                if (school == null) {
                    continue;
                }
                boundary.setSchool(school);
                JSONObject schoolJson = boundary.toJsonObject();
                schoolJson.put("lat", school.getLat());
                schoolJson.put("lon", school.getLon());
                schoolJson.put("rating", getGSRating(school));
                schoolJson.put("districtId", school.getDistrictId());
                schoolsJson.put(schoolJson);
            } catch (ObjectRetrievalFailureException orfe) {
                _log.error("Can't find school " + boundary.getState() +"," + boundary.getSchoolId());
            }
        }
        output.put("schools", schoolsJson);
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
            JSONArray districtsJson = new JSONArray();
            for (IDistrictSearchResult searchResult: resultsPage.getSearchResults()) {
                JSONObject districtJson = new JSONObject();
                districtJson.put("name", searchResult.getName());
                districtJson.put("lat", searchResult.getLatitude());
                districtJson.put("lon", searchResult.getLongitude());
                districtJson.put("id", searchResult.getId());
                districtJson.put("state", searchResult.getState());
                districtsJson.put(districtJson);
                District districtFacade = new District();
                districtFacade.setId(searchResult.getId());
                districtFacade.setDatabaseState(searchResult.getState());
                DistrictRating rating = _districtRatingDao.getDistrictRatingByDistrict(districtFacade);
                if (rating != null && rating.getActive() == 1) {
                    districtJson.put("rating", rating.getRating());
                } else {
                    districtJson.put("rating", 0);
                }
            }
            output.put("districts", districtsJson);
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

    @RequestMapping(value="getDistrictContainingPoint.page", method=RequestMethod.GET)
    public void getDistrictContainingPoint(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        try {
            double lat = Double.valueOf(request.getParameter("lat"));
            double lon = Double.valueOf(request.getParameter("lon"));
            LevelCode.Level districtLevel = LevelCode.Level.getLevelCode(request.getParameter("level"));
            DistrictBoundary districtBoundary = _districtBoundaryDao.getDistrictBoundaryContainingPoint(lat, lon, districtLevel);
            if (districtBoundary == null) {
                response.setStatus(404);
            } else {
                try {
                    response.setContentType("application/json");
                    response.getWriter().write(districtBoundary.toJson());
                } catch (Exception e) {
                    _log.error("Error parsing geometry:" + e, e);
                    response.setStatus(500);
                }
            }
        } catch (NumberFormatException nfe) {
            _log.error("Error parsing params: " + nfe, nfe);
            response.setStatus(500);
        } catch (Exception e) {
            _log.error("Unexpected error: " + e, e);
            response.setStatus(500);
        }
    }

    @RequestMapping(value="getSchoolsForDistrict.page", method=RequestMethod.GET)
    public void getSchoolsForDistrict(HttpServletRequest request,
                                      HttpServletResponse response) throws IOException, JSONException {
        response.setContentType("application/json");
        State state = State.fromString(request.getParameter("state"));
        Integer districtId = Integer.valueOf(request.getParameter("districtId"));
        LevelCode.Level schoolLevel = LevelCode.Level.getLevelCode(request.getParameter("level"));

        JSONObject rval = new JSONObject();

        District district = _districtDao.findDistrictById(state, districtId);
        if (district != null) {
            rval.put("districtName", district.getName());
            rval.put("districtId", districtId);
            rval.put("state", state);

            List<School> schools = _schoolDao.getSchoolsInDistrict(state, districtId, true, schoolLevel);

            JSONArray array = new JSONArray();
            if (schools != null && schools.size() > 0) {
                for (School s: schools) {
                    JSONObject schoolInfo = new JSONObject();
                    schoolInfo.put("name", s.getName());
                    schoolInfo.put("id", s.getId());
                    schoolInfo.put("lat", s.getLat());
                    schoolInfo.put("lon", s.getLon());
                    schoolInfo.put("state", s.getDatabaseState());
                    schoolInfo.put("rating", getGSRating(s));
                    array.put(schoolInfo);
                }
            }
            rval.put("schools", array);
        }
        rval.write(response.getWriter());
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
            rval.put("districtName", district.getName());
            rval.put("districtId", districtId);
            rval.put("state", state);

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
                JSONArray array = new JSONArray();
                for (School s: schools) {
                    SchoolBoundary boundary = idSchoolBoundaryMap.get(s.getId());
                    JSONObject schoolInfo;
                    if (boundary != null) {
                        schoolInfo = boundary.toJsonObject();
                    } else {
                        schoolInfo = new JSONObject();
                        schoolInfo.put("coordinates", new JSONObject());
                    }
                    schoolInfo.put("name", s.getName());
                    schoolInfo.put("id", s.getId());
                    schoolInfo.put("lat", s.getLat());
                    schoolInfo.put("lon", s.getLon());
                    schoolInfo.put("state", s.getDatabaseState());
                    array.put(schoolInfo);
                }
                rval.put("schoolBoundaries", array);
            }
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
