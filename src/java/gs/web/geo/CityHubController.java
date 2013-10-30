package gs.web.geo;

import gs.data.hubs.HubConfig;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.data.url.DirectoryStructureUrlFactory;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.school.review.ReviewFacade;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 8/16/13
 * Time: 9:59 AM
 * To change this template use File | Settings | File Templates.
 */
/**
 * Controller for the City Hub Pages.
 * @author sarora@greatschools.org Shomi Arora.
 */

@Controller
public class CityHubController   implements IDirectoryStructureUrlController, IControllerFamilySpecifier {
    private static Logger _logger = Logger.getLogger(CityHubController.class);
    private  static final String PARAM_CITY = "city";

    private ControllerFamily _controllerFamily;

    @Autowired
    private IReviewDao _reviewDao;

    @Autowired
    private CityHubHelper _cityHubHelper;

    @RequestMapping(method= RequestMethod.GET)
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("/cityHub/cityHub");
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        final State state = sessionContext.getState();
        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);
        String city = StringUtils.defaultIfEmpty(request.getParameter(PARAM_CITY), fields.getCityName());
        // Validate those inputs and give up if we can't build a reasonable page.
        if (state == null) {
            // no state name found on city page, so redirect to /
            View redirectView = new RedirectView("/");
            return new ModelAndView(redirectView);
        }

        if (StringUtils.isEmpty(city)) {
            // no city name found, so redirect to /california or whichever state they did provide
            View redirectView = new RedirectView(DirectoryStructureUrlFactory.createNewStateBrowseURIRoot(state));
            return new ModelAndView(redirectView);
        }

       PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        if (pageHelper != null) {
            pageHelper.setHideAds(true);
        }

        modelAndView.addObject("city", WordUtils.capitalizeFully(city));
        modelAndView.addObject("state", state);

        Integer collectionId = getCityHubHelper().getCollectionId(city, state);
        modelAndView.addObject("collectionId", collectionId);
        modelAndView.addObject("schoolBreakdown", getCityHubHelper().getCollectionBrowseLinks(request, collectionId, city, state));



        if (collectionId != null)  {


            /**
             * Get the important events
             */
        List<HubConfig> configList = getCityHubHelper().getHubConfig(city, state);
        ModelMap importantEventsMap = getCityHubHelper().getFilteredConfigMap(configList,  CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX);
        List<String> configKeyPrefixesSortedByDate = getCityHubHelper().getConfigKeyPrefixesSortedByDate(importantEventsMap);
        importantEventsMap.put(CityHubHelper.CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY, configKeyPrefixesSortedByDate);
        importantEventsMap.put("maxImportantEventsToDisplay", CityHubHelper.MAX_IMPORTANT_EVENTS_TO_DISPLAYED);
        modelAndView.addObject(CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX, importantEventsMap);

        /**
         * Adding the Review Module Functionality to the Controller Start
         */

        List<ReviewFacade> reviews = getReviewFacades(state, collectionId);
        modelAndView.addObject("reviews", reviews);

            ModelMap hubHomeModelMap = getCityHubHelper().getFilteredConfigMap(configList, CityHubHelper.HUB_HOME_KEY_PREFIX);

            modelAndView.addObject(CityHubHelper.HUB_HOME_CHOOSE_SCHOOL_MODEL_KEY,
                hubHomeModelMap.get(CityHubHelper.HUB_HOME_KEY_PREFIX+ "_" + CityHubHelper.HUB_HOME_CHOOSE_SCHOOL_MODEL_KEY));

            modelAndView.addObject(CityHubHelper.HUB_HOME_ANNOUNCEMENT_MODEL_KEY,
                hubHomeModelMap.get(CityHubHelper.HUB_HOME_KEY_PREFIX + "_" + CityHubHelper.HUB_HOME_ANNOUNCEMENT_MODEL_KEY));

            modelAndView.addObject(CityHubHelper.HUB_CITY_ARTICLE_MODEL_KEY,
                    hubHomeModelMap.get(CityHubHelper.HUB_HOME_KEY_PREFIX + "_" + CityHubHelper.HUB_CITY_ARTICLE_MODEL_KEY));

            modelAndView.addObject(CityHubHelper.HUB_PARTNER_CAROUSEL_MODEL_KEY,
                    hubHomeModelMap.get(CityHubHelper.HUB_HOME_KEY_PREFIX + "_" + CityHubHelper.HUB_PARTNER_CAROUSEL_MODEL_KEY));

            modelAndView.addObject(CityHubHelper.COLLECTION_NICKNAME_MODEL_KEY,
                getCityHubHelper().getCollectionNicknameFromConfigList(configList, collectionId));
        }
        return modelAndView;
}

    /**
     * Get review facade for the UX from
     * @param state   not Null State .All Nullability checks should be done at the start of the controller.
     * @param collectionId    not Null Collection id  .All Nullability checks should be done at the start of the controller.
     * @return
     */
    private List<ReviewFacade> getReviewFacades(final State state, final Integer collectionId) {

        final  List<Integer> reviewIds  = _reviewDao.findRecentReviewsInHub(state, collectionId, CityHubHelper.COUNT_OF_REVIEWS_TO_BE_DISPLAYED, CityHubHelper.MAX_NO_OF_DAYS_BACK_REVIEWS_PUBLISHED);
        List<ReviewFacade> reviews = new ArrayList<ReviewFacade>();
        for (Integer reviewId : reviewIds)
        {
            Review review = _reviewDao.getReview(reviewId);
            School school = review.getSchool();
            Integer totalReviews= _reviewDao.countTotalPublishedReviews(school);
            reviews.add(new ReviewFacade(school, review, totalReviews));
         }
        return reviews;
    }


    public boolean shouldHandleRequest(final DirectoryStructureUrlFields fields) {
        return fields == null ? false : fields.hasState() && fields.hasCityName() && !fields.hasChoosePage() && !fields.hasEnrollmentPage() && !fields.hasEducationCommunityPage() &&  !fields.hasEventsPage() && !fields.hasDistrictName() && !fields.hasLevelCode() && !fields.hasSchoolName();

    }

    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }


    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }

    public CityHubHelper getCityHubHelper() {
        return _cityHubHelper;
    }

    public void setCityHubHelper(CityHubHelper _cityHubHelper) {
        this._cityHubHelper = _cityHubHelper;
    }
}
