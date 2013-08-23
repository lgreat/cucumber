package gs.web.geo;

import gs.data.hubs.HubConfig;
import gs.data.hubs.IHubCityMappingDao;
import gs.data.hubs.IHubConfigDao;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private  static final String PARAM_CITY = "city";
    private static final String IMPORTANT_EVENT = "importantEvent";

    private ControllerFamily _controllerFamily;

    @Autowired
    private AnchorListModelFactory _anchorListModelFactory;

    @Autowired
    private IReviewDao _reviewDao;
    @Autowired
    private IHubCityMappingDao _hubCityMappingDao;
    @Autowired
    private IHubConfigDao _hubConfigDao;

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
        List<HubConfig> configList = getHubConfig(modelAndView, city, state);
        ModelMap importantEventsMap = getFromConfigList(configList, IMPORTANT_EVENT);
        modelAndView.addObject(IMPORTANT_EVENT, importantEventsMap);

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

    public List<HubConfig> getHubConfig(ModelAndView modelAndView, String city, State state) {
        Integer hubId = _hubCityMappingDao.getHubIdFromCityAndState(city, state);

        if(hubId == null) {
            return new ArrayList<HubConfig>();
        }

        modelAndView.addObject("hubId", hubId);
        List<HubConfig> configList = _hubConfigDao.getAllConfigFromHubId(hubId);

        return configList != null ? configList : new ArrayList<HubConfig>();
    }

    public ModelMap getFromConfigList(List<HubConfig> configList, String keyPrefix) {
        ModelMap filteredConfig = new ModelMap();
        if(configList == null || keyPrefix == null) {
            return filteredConfig;
        }

        int numEvents = 0;
        for(HubConfig hubConfig : configList) {
            String key = hubConfig.getQuay();
            if(hubConfig != null && key.startsWith(keyPrefix)) {
                /**
                 * The key should always be in this format - [type_of_key]_[index]_[type_of_value]
                 * an example for the type of key is "importantEvent"
                 * [index] is a number. This should be sequential for each key type - for example there shouldn't be
                 * "importantEvent_2" without "importantEvent_1"
                 * type_of_value identifies what the value is, for importantEvent this could be description, url, date.
                 */
                String eventNum = key.substring(key.indexOf("_") + 1, key.lastIndexOf("_"));
                try {
                    if(key.endsWith("_date")) {
                        Calendar calendar = getDateFromString(hubConfig.getValue());
                        filteredConfig.put(key + "_year", calendar.get(Calendar.YEAR));
                        filteredConfig.put(key + "_dayOfMonth", calendar.get(Calendar.DAY_OF_MONTH));
                        filteredConfig.put(key + "_month", calendar.get(Calendar.MONTH) + 1);
                    }
                    else {
                        filteredConfig.put(key, hubConfig.getValue());
                    }
                    Integer count = Integer.parseInt(eventNum);
                    numEvents = (count > numEvents) ? count : numEvents;
                }
                catch (ParseException ex) {
                    _logger.error("CityHubController - unable to convert string to java date", ex.getCause());
                }
                catch (NumberFormatException ex) {
                    _logger.error("CityHubController - unable to get index value from config key.", ex.getCause());
                }
            }
        }

        filteredConfig.put("count", numEvents);
        return filteredConfig;
    }

    public Calendar getDateFromString(String date) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new SimpleDateFormat("MM-dd-yyyy").parse(date));
        return calendar;
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

    public void setHubCityMappingDao(IHubCityMappingDao _hubCityMappingDao) {
        this._hubCityMappingDao = _hubCityMappingDao;
    }

    public void setHubConfigDao(IHubConfigDao _hubConfigDao) {
        this._hubConfigDao = _hubConfigDao;
    }
}
