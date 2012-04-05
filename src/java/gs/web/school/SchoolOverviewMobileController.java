package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.web.mobile.IDeviceSpecificControllerPartOfPair;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author aroy@greatschools.org
 */
public class SchoolOverviewMobileController implements Controller, IDirectoryStructureUrlController, IDeviceSpecificControllerPartOfPair {
    protected static final Log _log = LogFactory.getLog(SchoolOverviewMobileController.class.getName());

    public static final int MAX_SCHOOL_REVIEWS = 3;

    private boolean _controllerHandlesMobileRequests;
    private boolean _controllerHandlesDesktopRequests;
    private ISchoolDao _schoolDao;
    private RatingHelper _ratingHelper;
    private IReviewDao _reviewDao;

    protected School getSchool(HttpServletRequest request) {
        School school = null;
        State state = SessionContextUtil.getSessionContext(request).getState();
        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);
        if (fields != null) {
            try {
                Integer id = new Integer(fields.getSchoolID());
                School s = _schoolDao.getSchoolById(state, id);
                if (s.isActive()) {
                    school = s;
                } else {
                    _log.error("School " + s + " is inactive");
                }
            } catch (Exception e) {
                _log.error("Could not get a valid or active school: " + fields.getSchoolID() + " in state: " + state, e);
            }
        } else {
            _log.error("Fields is null");
        }
        return school;
    }

    protected RedirectView getRedirectViewIfNecessary(School school, HttpServletRequest request) {
        String currentUrl = request.getRequestURI();
        UrlBuilder canonicalUrlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        String canonicalUrl = canonicalUrlBuilder.asSiteRelative(request);
        if (!currentUrl.equals(canonicalUrl)) {
            return new RedirectView301(canonicalUrlBuilder.asFullUrl(request));
        }
        return null;
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        School school = getSchool(request);
        if (school == null) {
            return new ModelAndView(new RedirectView301(new UrlBuilder(UrlBuilder.HOME).asSiteRelative(request)));
        }
        RedirectView seoRedirectView = getRedirectViewIfNecessary(school, request);
        if (seoRedirectView != null) {
            return new ModelAndView(seoRedirectView);
        }
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("school", school);

        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        boolean useCache = (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer());
        Integer gsRating = getRatingHelper().getGreatSchoolsOverallRating(school, useCache);
        model.put("gs_rating", gsRating);

        List<Review> reviews = _reviewDao.findPublishedNonPrincipalReviewsBySchool(school, MAX_SCHOOL_REVIEWS);
        Long numberOfReviews = _reviewDao.countPublishedNonPrincipalReviewsBySchool(school);
        model.put("reviews", reviews);
        model.put("numberOfReviews", numberOfReviews);
        Ratings ratings = _reviewDao.findRatingsBySchool(school);
        model.put("ratings", ratings);

        return new ModelAndView("school/overview-mobile", model);
    }

    public boolean shouldHandleRequest(DirectoryStructureUrlFields fields) {
        if (fields == null) {
            return false;
        }
        return fields.hasState() && fields.hasCityName() &&
                fields.hasSchoolTypes() && fields.getSchoolTypes().isEmpty() &&
                // Check for the PK version or the public/private version
                ((fields.hasLevelCode() && fields.getLevelCode().equals(LevelCode.PRESCHOOL) &&
                        fields.hasSchoolName() && fields.hasSchoolID()) ||
                        (!fields.hasLevelCode() && fields.hasSchoolName() && fields.hasSchoolID()));
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

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    public boolean controllerHandlesMobileRequests() {
        return _controllerHandlesMobileRequests;
    }

    public void setControllerHandlesMobileRequests(boolean controllerHandlesMobileRequests) {
        _controllerHandlesMobileRequests = controllerHandlesMobileRequests;
    }

    public boolean controllerHandlesDesktopRequests() {
        return _controllerHandlesDesktopRequests;
    }

    public void setControllerHandlesDesktopRequests(boolean controllerHandlesDesktopRequests) {
        _controllerHandlesDesktopRequests = controllerHandlesDesktopRequests;
    }
}
