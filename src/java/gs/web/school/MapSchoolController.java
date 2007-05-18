package gs.web.school;

import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.NearbySchool;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.test.rating.IRatingsConfigDao;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.TestManager;
import gs.data.test.SchoolTestValue;
import gs.web.util.PageHelper;

import java.util.List;
import java.util.ArrayList;

/**
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class MapSchoolController extends AbstractSchoolController {
    public static final String BEAN_ID = "/school/mapSchool.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private String _viewName;
    private IReviewDao _reviewDao;
    private IRatingsConfigDao _ratingsConfigDao;
    private TestManager _testManager;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);

        List<NearbySchool> nearbySchools = getSchoolDao().findNearbySchools(school, 5);
//        List<School> mapSchools = new ArrayList<School>();
//        for (NearbySchool nearbySchool : nearbySchools) {
//            mapSchools.add(nearbySchool.getNeighbor());
//        }
//        request.setAttribute("mapSchools", mapSchools);
        request.setAttribute("nearbySchools", nearbySchools);
        request.setAttribute("hasNearby", (nearbySchools.size() > 0));
        request.setAttribute("levelLongName", school.getLevelCode().getLowestLevel().getLongName());

        if (nearbySchools.size() > 0) {
            loadRatings(request, nearbySchools);
        }

        return new ModelAndView(_viewName);
    }

    protected void loadRatings(HttpServletRequest request, List<NearbySchool> schools) {
        List<MapSchool> mapSchools = new ArrayList<MapSchool>();
        for (NearbySchool nearbySchool: schools) {
            School school = nearbySchool.getNeighbor();
            MapSchool mapSchool = new MapSchool();
            mapSchool.setNeighbor(school);
            mapSchool.setRating(nearbySchool.getRating());

            // if parent ratings have not been supplied, retrieve them here
            try {
                Ratings ratings = _reviewDao.findRatingsBySchool(school);
                mapSchool.setParentRatings(ratings);
            } catch (Exception ex) {
                _log.error("Error getting parent ratings for school " + school.getId() +
                        " in " + school.getDatabaseState());
                _log.error(ex);
            }

            mapSchools.add(mapSchool);
        }
        request.setAttribute("mapSchools", mapSchools);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
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

    public class MapSchool extends NearbySchool {
        private Ratings _parentRatings;

        public Ratings getParentRatings() {
            return _parentRatings;
        }

        public void setParentRatings(Ratings parentRatings) {
            _parentRatings = parentRatings;
        }
    }
}
