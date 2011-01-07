package gs.web.school.review;

import gs.data.json.JSONObject;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.school.RatingHelper;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Parent review page
 *
 * @author <a href="mailto:npatury@greatschools.org">Nanditha Patury</a>
 */

public class ParentReviewAjaxControllerForSchoolInfo implements Controller {

    ISchoolDao _schoolDao;
    IReviewDao _reviewDao;
    private RatingHelper _ratingHelper;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);

        Map<Object,Object> model = new HashMap<Object,Object>();

        StringBuffer str = new StringBuffer();
        if (request.getParameter("schoolId") != null && request.getParameter("state") != null) {
            String state = request.getParameter("state");
            School school = _schoolDao.getSchoolById(State.fromString(state), Integer.parseInt(request.getParameter("schoolId")));

            if (school != null) {

                Integer id = school.getId();
                String name = school.getName();
                Address address = school.getPhysicalAddress();
                String street1 = address.getStreet();
                String street2 = address.getStreetLine2();
                String cityStateZip = address.getCityStateZip();
                String county = school.getCounty();
                LevelCode levelCode = school.getLevelCode();

                boolean useCache = (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer());

                Integer gsRating = getRatingHelper().getGreatSchoolsOverallRating(school, useCache);

                UrlBuilder builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_ESP_LOGIN);

                Ratings ratings = _reviewDao.findRatingsBySchool(school);

                Integer communityRating;
                if (LevelCode.PRESCHOOL.equals(levelCode)) {
                    communityRating = ratings.getAvgP_Overall();
                } else {
                    communityRating = ratings.getOverall();
                }

                model.put("id", id);
                model.put("state",state);
                model.put("name", name);
                model.put("street1", street1);
                model.put("street2", street2);
                model.put("cityStateZip", cityStateZip);
                model.put("county", county);
                model.put("greatSchoolsRating", gsRating);
                model.put("communityRating", communityRating);
                model.put("levelCode", school.getLevelCode().getCommaSeparatedString());
                model.put("numberOfCommunityRatings", ratings.getCount());
                model.put("espLoginUrl", builder.asFullUrl(request));
            }
        }

        jsonResponse(response, model);

        return null;
    }

    public void jsonResponse(HttpServletResponse response, Map<Object,Object> data) {
        try {
            response.setContentType("application/json");
            JSONObject rval = new JSONObject(data);
            response.getWriter().print(rval.toString());
            response.getWriter().flush();
        } catch (IOException e) {
            //_log.info("Failed to get response writer");
            //give up
        }
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public RatingHelper getRatingHelper() {
        return _ratingHelper;
    }

    public void setRatingHelper(RatingHelper ratingHelper) {
        _ratingHelper = ratingHelper;
    }
}
