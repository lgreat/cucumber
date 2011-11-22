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
import gs.data.test.rating.DistrictRating;
import gs.data.test.rating.IDistrictRatingDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

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
            float lat = Float.valueOf(request.getParameter("lat"));
            float lon = Float.valueOf(request.getParameter("lon"));
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

    @RequestMapping(value="getSchoolBoundariesForDistrict.page", method=RequestMethod.GET)
    public void getSchoolBoundariesForDistrict(HttpServletRequest request,
                                               HttpServletResponse response) throws IOException, JSONException {
        long start = System.currentTimeMillis();
        response.setContentType("application/json");
        State state = State.fromString(request.getParameter("state"));
        Integer districtId = Integer.valueOf(request.getParameter("districtId"));
        LevelCode.Level schoolLevel = LevelCode.Level.getLevelCode(request.getParameter("level"));

        long dbStart = System.currentTimeMillis();
        List<School> schools = _schoolDao.getSchoolsInDistrict(state, districtId, true, schoolLevel);
        long dbEnd = System.currentTimeMillis();
        long sazStart = 0;
        long sazEnd = 0;
        if (schools != null && schools.size() > 0) {
            sazStart = System.currentTimeMillis();
            List<SchoolBoundary> schoolBoundaries = _schoolBoundaryDao.getSchoolBoundaries(schools, schoolLevel);
            sazEnd = System.currentTimeMillis();
            System.out.println("    (" + districtId + ") Of " + schools.size() + " schools in DB, " + schoolBoundaries.size() + " have SAZ info");
            JSONObject rval = new JSONObject();
            JSONArray array = new JSONArray();
            for (SchoolBoundary schoolBoundary: schoolBoundaries) {
                array.put(schoolBoundary.toJsonObject());
            }
            rval.put("schoolBoundaries", array);
            rval.write(response.getWriter());
        }
        System.out.println("    getSchoolBoundariesForDistrict(" + districtId + ") took " + (System.currentTimeMillis()-start) + " ms");
        System.out.println("      (" + districtId + ") DB took " + (dbEnd-dbStart) + " ms");
        System.out.println("      (" + districtId + ") SAZ took " + (sazEnd-sazStart) + " ms");
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
