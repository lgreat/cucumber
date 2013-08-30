package gs.web.geo;

import gs.data.hubs.HubConfig;
import gs.data.hubs.IHubCityMappingDao;
import gs.data.hubs.IHubConfigDao;
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
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.AnchorListModel;
import gs.web.util.list.AnchorListModelFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

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
public class CityHubController   implements IDirectoryStructureUrlController, IControllerFamilySpecifier {

    private static Logger _logger = Logger.getLogger(CityHubController.class);

    private  static int COUNT_OF_REVIEWS_TO_BE_DISPLAYED = 2 ;
    private  static int MAX_NO_OF_DAYS_BACK_REVIEWS_PUBLISHED = 90 ;
    public   static int MAX_IMPORTANT_EVENTS_TO_DISPLAYED = 2;
    private  static final String PARAM_CITY = "city";
    public   static final String IMPORTANT_EVENT_KEY_PREFIX = "importantEvent";
    public   static final String DATE_FORMAT = "MM-dd-yyyy";

    private ControllerFamily _controllerFamily;

    @Autowired
    private AnchorListModelFactory _anchorListModelFactory;

    @Autowired
    private IReviewDao _reviewDao;
    @Autowired
    private IHubCityMappingDao _hubCityMappingDao;
    @Autowired
    private IHubConfigDao _hubConfigDao;

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

        modelAndView.addObject("city", city);
        modelAndView.addObject("state", state);


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
        ModelMap importantEventsMap = getFilteredConfigMap(configList, IMPORTANT_EVENT_KEY_PREFIX);
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

    public List<HubConfig> getHubConfig(ModelAndView modelAndView,String city,State state) {
        Integer hubId = _hubCityMappingDao.getHubIdFromCityAndState(city, state);

        if(hubId == null) {
            return new ArrayList<HubConfig>();
        }

        modelAndView.addObject("hubId", hubId);
        List<HubConfig> configList = _hubConfigDao.getAllConfigFromHubId(hubId);

        return configList != null ? configList : new ArrayList<HubConfig>();
    }

    public ModelMap getFilteredConfigMap(List<HubConfig> configList, String keyPrefix) {
        ModelMap filteredConfig = new ModelMap();
        List<String> configKeyPrefixListWithIndex = new ArrayList<String>();

        if(configList == null || keyPrefix == null) {
            return filteredConfig;
        }

        boolean isImportantEventsModule = (IMPORTANT_EVENT_KEY_PREFIX.equals(keyPrefix)) ? true : false;

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
                String keyPrefixWithIndex = key.substring(0, key.lastIndexOf("_"));
                /**
                 * If the key is for date, convert the date in string to date object. This is done to sort the events
                 * by date and also to get the day, month and year to apply the appropriate styles for the module.
                 */
                if(key.endsWith("_date")) {
                    try {
                        Calendar calendar = Calendar.getInstance();
                        Date date = new SimpleDateFormat(DATE_FORMAT).parse(hubConfig.getValue());
                        calendar.setTime(date);
                        filteredConfig.put(key + "_year", calendar.get(Calendar.YEAR));
                        filteredConfig.put(key + "_dayOfMonth", calendar.get(Calendar.DAY_OF_MONTH));
                        filteredConfig.put(key + "_month", calendar.get(Calendar.MONTH) + 1);
                        filteredConfig.put(key, date);

                        if (isImportantEventsModule) {
                            /**
                             * Key prefix (with index) for an event must have a key for the date. Do not add a key that doesn't
                             * have a date.
                             */
                            configKeyPrefixListWithIndex.add(keyPrefixWithIndex);
                        }
                    }
                    catch (ParseException ex) {
                        _logger.error("CityHubController - unable to convert string to java date", ex.getCause());
                    }
                }
                else if (key.endsWith("_url")) {
                    filteredConfig.put(key, UrlUtil.formatUrl(hubConfig.getValue()));
                }
                else {
                    filteredConfig.put(key, hubConfig.getValue());
                }
            }
        }

        /**
         * For the important events module, only the first two upcoming events are displayed. So the event key prefixes
         * are sorted by date.
         */
        if(isImportantEventsModule) {
            getConfigKeyPrefixesSortedByDate(configKeyPrefixListWithIndex, filteredConfig);
        }
        filteredConfig.put("configKeyPrefixListWithIndex", configKeyPrefixListWithIndex);

        return filteredConfig;
    }

    public void getConfigKeyPrefixesSortedByDate(List<String> configKeyPrefixListWithIndex, final ModelMap filteredConfigMap) {
        if(configKeyPrefixListWithIndex == null || filteredConfigMap == null) {
            return;
        }

        Collections.sort(configKeyPrefixListWithIndex, new Comparator<String>() {
            public int compare(String keyPrefix1, String keyPrefix2) {
                String dateKeyPrefix1 = keyPrefix1 + "_date";
                String dateKeyPrefix2 = keyPrefix2 + "_date";

                Object date1 = filteredConfigMap.get(dateKeyPrefix1);
                Object date2 = filteredConfigMap.get(dateKeyPrefix2);
                if(date1 instanceof Date && date2 instanceof Date) {
                    return (((Date) date1).after((Date) date2) ? 1 : -1);
                }
                return 0;
            }
        });
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
