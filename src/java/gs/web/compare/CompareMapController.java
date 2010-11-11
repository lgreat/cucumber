package gs.web.compare;

import gs.data.geo.LatLonRect;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CompareMapController extends AbstractCompareSchoolController {
    private String _successView;
    private IReviewDao _reviewDao;
    public static final String MODEL_MAP_CENTER = "mapCenter";
    public static final String PARAM_SELECTED_SCHOOL = "selectedSchool";

    @Override
    protected void handleCompareRequest(HttpServletRequest request, HttpServletResponse response,
                                        List<ComparedSchoolBaseStruct> schools, Map<String, Object> model) throws
                                                                                                           IOException {
        model.put(MODEL_TAB, "map");
        handleGSRating(request, schools);
        determineCenterOfMap(schools, model);

        handleCommunityRating(schools);
        
        if (request.getParameter(PARAM_SELECTED_SCHOOL) != null) {
            for (ComparedSchoolBaseStruct struct: schools) {
                if (StringUtils.equals(request.getParameter(PARAM_SELECTED_SCHOOL), struct.getUniqueIdentifier())) {
                    ((ComparedSchoolMapStruct) struct).setSelected(true);
                }
            }
        }
    }

    protected void handleCommunityRating(List<ComparedSchoolBaseStruct> structs) {

        for (ComparedSchoolBaseStruct struct: structs) {
            School school = struct.getSchool();
            Ratings ratings = _reviewDao.findRatingsBySchool(school);
            if (ratings == null || ratings.getOverall() == null) {
                struct.setCommunityRating(0);
            } else {
                struct.setCommunityRating(ratings.getOverall());
            }
        }

    }
    protected void determineCenterOfMap(List<ComparedSchoolBaseStruct> schools, Map<String, Object> model) {
        LatLonRect latLonRect = new LatLonRect(schools);
        model.put(MODEL_MAP_CENTER, latLonRect.getCenter());
    }

    @Override
    protected ComparedSchoolBaseStruct getStruct() {
        return new ComparedSchoolMapStruct();
    }

    @Override
    public String getSuccessView() {
        return _successView;
    }

    public void setSuccessView(String successView) {
        _successView = successView;
    }

    @Override
    public int getPageSize() {
        return 8;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }
}
