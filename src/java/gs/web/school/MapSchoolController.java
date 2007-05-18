package gs.web.school;

import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.school.School;
import gs.data.school.NearbySchool;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;

import java.util.List;
import java.util.ArrayList;

/**
 * Provides the reference data for the page that contains a map of a given school, plus the five
 * nearest other schools. Each school needs the GS rating as well as the parent rating, which is
 * displayed in an info bubble in the map.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class MapSchoolController extends AbstractSchoolController {
    public static final String BEAN_ID = "/school/mapSchool.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private String _viewName;
    private IReviewDao _reviewDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        // the school is obtained from the request by our super class: AbstractSchoolController
        School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);

        // five nearest schools
        List<NearbySchool> nearbySchools = getSchoolDao().findNearbySchools(school, 5);
        // nearby schools go in request for the nearby box
        request.setAttribute("nearbySchools", nearbySchools);
        // convenience variable for page logic
        request.setAttribute("hasNearby", (nearbySchools.size() > 0));
        // convenience variable for page logic
        request.setAttribute("levelLongName", school.getLevelCode().getLowestLevel().getLongName());

        // if there are nearby schools, obtain parent rating information on them for the map
        if (nearbySchools.size() > 0) {
            loadRatings(request, nearbySchools);
        }

        return new ModelAndView(_viewName);
    }

    /**
     * Obtain parent ratings for a list of schools (kept in NearbySchool's)
     * @param request page request
     * @param schools list of NearbySchool objects
     */
    protected void loadRatings(HttpServletRequest request, List<NearbySchool> schools) {
        // this is our data structure -- contains basically a school, a GS rating, and a parent rating
        List<MapSchool> mapSchools = new ArrayList<MapSchool>();
        // for each school
        for (NearbySchool nearbySchool: schools) {
            School school = nearbySchool.getNeighbor();
            // MapSchool is a subclass of NearbySchool
            MapSchool mapSchool = new MapSchool();
            // now we copy over the fields we want: school and gs rating
            // School. I don't like that it is called neighbor, but that's from the superclass NearbySchool
            mapSchool.setNeighbor(school);
            // GS rating
            mapSchool.setRating(nearbySchool.getRating());

            // Retrieve parent ratings
            Ratings ratings = _reviewDao.findRatingsBySchool(school);
            // Parent ratings
            mapSchool.setParentRatings(ratings);

            // Add data structure to list
            mapSchools.add(mapSchool);
        }
        // MapSchools for populating the map info bubbles
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

    /**
     * Extends NearbySchool as a convenience (but it really buys me nothing). The only used fields are:
     * neighbor (from super, for the school),
     * rating (from super, for the gs rating),
     * and parentRating (defined here, for the parent rating)
     */
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
