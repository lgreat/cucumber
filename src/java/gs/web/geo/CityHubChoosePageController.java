package gs.web.geo;

/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 8/30/13
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */

import gs.data.hubs.HubConfig;
import gs.data.state.State;
import gs.data.url.DirectoryStructureUrlFactory;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.school.review.ReviewFacade;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.AnchorListModel;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
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
import java.util.List;

/**
 * Controller for the City Hub Choose  Pages.
 * @author sarora@greatschools.org Shomi Arora.
 */

@Controller
public class CityHubChoosePageController {


    private  static final String PARAM_CITY = "city";
    @Autowired
    private CityHubHelper _cityHubHelper;

    @RequestMapping(method= RequestMethod.GET)
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        ModelAndView modelAndView = new ModelAndView("/cityHub/choosePage");
        State state= State.DC;
        String city= "washington";


        // Should be commented out once the Connical URL for Choose Page is in Place _Shomi Revert
//        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
//        final State state = sessionContext.getState();
//        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);
//        String city = StringUtils.defaultIfEmpty(request.getParameter(PARAM_CITY), fields.getCityName());
//        // Validate those inputs and give up if we can't build a reasonable page.
//        if (state == null) {
//            // no state name found on city page, so redirect to /
//            View redirectView = new RedirectView("/");
//            return new ModelAndView(redirectView);
//        }
//
//        if (StringUtils.isEmpty(city)) {
//            // no city name found, so redirect to /california or whichever state they did provide
//            View redirectView = new RedirectView(DirectoryStructureUrlFactory.createNewStateBrowseURIRoot(state));
//            return new ModelAndView(redirectView);
//        }

        modelAndView.addObject("city", WordUtils.capitalizeFully(city));
        modelAndView.addObject("state", state);
        modelAndView.addObject("hubId", getCityHubHelper().getHubID(city, state));

        /**
         * Get the important events
         */
        ModelMap importantEventsMap = getModelMap(state, city);
        modelAndView.addObject(CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX, importantEventsMap);


        return modelAndView;


    }

    private ModelMap getModelMap(final State state, final String city) {
        List<HubConfig> configList = getCityHubHelper().getHubConfig(city, state);
        ModelMap importantEventsMap = getCityHubHelper().getFilteredConfigMap(configList,  CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX);
        List<String> configKeyPrefixesSortedByDate = getCityHubHelper().getConfigKeyPrefixesSortedByDate(importantEventsMap);
        importantEventsMap.put(getCityHubHelper().CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY, configKeyPrefixesSortedByDate);
        importantEventsMap.put("maxImportantEventsToDisplay",  CityHubHelper.MAX_IMPORTANT_EVENTS_TO_DISPLAYED);
        return importantEventsMap;
    }

    public CityHubHelper getCityHubHelper() {
        return _cityHubHelper;
    }

    public void setCityHubHelper(final CityHubHelper _cityHubHelper) {
        this._cityHubHelper = _cityHubHelper;
    }
}
