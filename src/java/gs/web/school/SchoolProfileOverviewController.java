package gs.web.school;

import gs.data.json.JSONException;
import gs.data.school.*;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.web.util.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/school/profileOverview.page")
public class SchoolProfileOverviewController extends AbstractSchoolProfileController {
    protected static final Log _log = LogFactory.getLog(SchoolProfileOverviewController.class.getName());

    String _viewName;

    @Autowired
    private RatingHelper _ratingHelper;
    @Autowired
    private IEspResponseDao _espResponseDao;
    @Autowired
    private IReviewDao _reviewDao;
    @Autowired
    private SchoolHelper _schoolHelper;
    @Autowired
    private SchoolMediaHelper _schoolMediaHelper;


    @RequestMapping(method= RequestMethod.GET)
    public Map<String,Object> handle(HttpServletRequest request,
        HttpServletResponse response,
        @RequestParam(value = "schoolId", required = false) Integer schoolId,
        @RequestParam(value = "state", required = false) State state
    ) {

        Map<String,Object> model = new HashMap<String,Object>();
        School school = getSchool(request, state, schoolId);


        // school's quote
        model.put("bestKnownFor", getBestKnownForQuote(school));



        // GreatSchools rating
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        boolean useCache = (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer());
        Integer gsRating = _ratingHelper.getGreatSchoolsOverallRating(school, useCache);
        if (gsRating != null && gsRating > 0 && gsRating < 11) {
            pageHelper.addAdKeyword("gs_rating", String.valueOf(gsRating));
        }
        model.put("gs_rating", gsRating);



        // photos
        _schoolMediaHelper.addSchoolPhotosToModel(school, model);


        // videos



        // Community ratings
        Ratings ratings = _reviewDao.findRatingsBySchool(school);
        model.put("ratings", ratings);
        model.put("noIndexFlag", school != null);



        // User reviews
        /*List<Review> reviews = _reviewDao.findPublishedNonPrincipalReviewsBySchool(school, MAX_SCHOOL_REVIEWS);
        Long numberOfReviews = _reviewDao.countPublishedNonPrincipalReviewsBySchool(school);
        model.put("reviews", reviews);
        model.put("numberOfReviews", numberOfReviews);
        // find and expose last modified date
        model.put("lastModifiedDate", _schoolHelper.getLastModifiedDate(school, reviews, numberOfReviews));
        */



        // Student ethnicity



        // Special education
        Set<String> ospKeys = new HashSet<String>(1);
        ospKeys.add("special_ed_programs");
        ospKeys.add("before_after_care");
        ospKeys.add("before_after_care_start");
        ospKeys.add("before_after_care_end");

        List<EspResponse> espResponses = _espResponseDao.getResponsesByKeys(school, ospKeys);
        List<String> specialEducationItems = new ArrayList<String>();
        if (espResponses != null && espResponses.size() > 0) {
            for (EspResponse r : espResponses) {
                if (r.getKey().equals("special_ed_programs")) {
                    specialEducationItems.add(r.getSafeValue());
                }
            }
        }
        model.put("specialEducationItems", specialEducationItems);




        // Transportation
        if (espResponses != null && espResponses.size() > 0) {
            for (EspResponse r : espResponses) {
                if (r.getKey().contains("_care")) {
                    if (r.getKey().equals("before_after_care")) {
                        if (r.getSafeValue().equals("before")) {
                            model.put("before_care", true);
                        } else if (r.getSafeValue().equals("after")) {
                            model.put("after_care", true);
                        }
                    } else {
                        model.put(r.getKey(), r.getSafeValue());
                    }
                }
            }
        }




        // Programs




        // Public schools




        // District information



        // OSP data (sports, arts, music)









        return model;
    }

    public String getBestKnownForQuote(School school) {
        // get PQ data to find quote if it exists
        Set<String> pqKeys = new HashSet<String>(1);
        pqKeys.add("best_known_for");
        String bestKnownFor = null;
        List<EspResponse> espResponses = _espResponseDao.getResponsesByKeys(school, pqKeys);
        if (espResponses != null && espResponses.size() > 0) {
            bestKnownFor = espResponses.get(0).getSafeValue();
            if (StringUtils.isNotBlank(bestKnownFor)) {
                if (!StringUtils.endsWith(bestKnownFor, ".")) {
                    bestKnownFor += ".";
                }
            }
        }
        return bestKnownFor;
    }





    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}