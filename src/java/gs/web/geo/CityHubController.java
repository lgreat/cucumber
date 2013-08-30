package gs.web.geo;

import gs.data.hubs.HubConfig;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.school.review.ReviewFacade;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.AnchorListModel;
import gs.web.util.list.AnchorListModelFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

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
@RequestMapping("/cityHub/cityHub.page")
public class CityHubController  extends AbstractController implements IDirectoryStructureUrlController, IControllerFamilySpecifier {

    private static Logger _logger = Logger.getLogger(CityHubController.class);

    private  static int COUNT_OF_REVIEWS_TO_BE_DISPLAYED = 2 ;
    private  static int MAX_NO_OF_DAYS_BACK_REVIEWS_PUBLISHED = 90 ;
    public static int MAX_IMPORTANT_EVENTS_TO_DISPLAYED = 2;
    private  static final String PARAM_CITY = "city";
    public static final String IMPORTANT_EVENT_KEY_PREFIX = "importantEvent";

    private ControllerFamily _controllerFamily;

    @Autowired
    private AnchorListModelFactory _anchorListModelFactory;

    @Autowired
    private IReviewDao _reviewDao;

    @Autowired
    private CityHubHelper _cityHubHelper;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ModelAndView modelAndView = new ModelAndView("/cityHub/cityHub");


       SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
       State state = sessionContext.getState();
       String city = request.getParameter(PARAM_CITY);

        /**
         * Hard Coding for DC and Washington State .As controller Structure completes and hub configuration  works this hardcoding should go away.
         */

        city="Washington" ;
        modelAndView.addObject("city", city);
        state=State.DC;
        modelAndView.addObject("state", state);
        /**
         * Hard coding for state and city end .
         */

        /**
         *  School Review Link Functionality Start.
         */
          AnchorListModel schoolBreakdownAnchorList = _anchorListModelFactory.createSchoolSummaryModel(state, city, city, request);
          modelAndView.addObject("schoolBreakdown", schoolBreakdownAnchorList);
        /**
         *  School Review Link Functionality Start.
         */

        /**
         * Get the important events
         */
        List<HubConfig> configList = getCityHubHelper().getHubConfig(modelAndView, city, state);
        ModelMap importantEventsMap = getCityHubHelper().getFilteredConfigMap(configList, IMPORTANT_EVENT_KEY_PREFIX);
        List<String> configKeyPrefixesSortedByDate = getCityHubHelper().getConfigKeyPrefixesSortedByDate(importantEventsMap);
        importantEventsMap.put(getCityHubHelper().CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY, configKeyPrefixesSortedByDate);
        importantEventsMap.put("maxImportantEventsToDisplay", MAX_IMPORTANT_EVENTS_TO_DISPLAYED);
        modelAndView.addObject(IMPORTANT_EVENT_KEY_PREFIX, importantEventsMap);

        /**
         * Adding the Review Module Functionality to the Controller Start
         */
        List<ReviewFacade> reviews = getReviewFacades(state, city);
        modelAndView.addObject("reviews", reviews);
        /**
         * Adding the Review Module Functionality to the Controller End
         */

        return modelAndView;


}

    /**
     * Get review facade for the UX from
     * @param state   not Null State .All Nullability checks should be done at the start of the controller.
     * @param city    not Null City  .All Nullability checks should be done at the start of the controller.
     * @return
     */
    private List<ReviewFacade> getReviewFacades(final State state, final String city) {

        final  List<Integer> reviewIds  = _reviewDao.findRecentReviewsInCity(state, city, COUNT_OF_REVIEWS_TO_BE_DISPLAYED, MAX_NO_OF_DAYS_BACK_REVIEWS_PUBLISHED);
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

    // What does this do Revert Shomi
    public boolean shouldHandleRequest(DirectoryStructureUrlFields fields) {
        if (fields == null) {
            return false;
        }

        return fields.hasState() && fields.hasCityName() && !fields.hasDistrictName() && !fields.hasLevelCode() && !fields.hasSchoolName();

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
