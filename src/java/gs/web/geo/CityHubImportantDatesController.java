package gs.web.geo;

import gs.data.hubs.HubConfig;
import gs.data.state.State;
import gs.data.url.DirectoryStructureUrlFactory;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
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
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 9/13/13
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class CityHubImportantDatesController  implements IDirectoryStructureUrlController {
    private static final String IMPORTANT_EVENT_DATES_VIEW = "/cityHub/events";

    @Autowired
    private CityHubHelper _cityHubHelper;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) {

        ModelAndView modelAndView = new ModelAndView(IMPORTANT_EVENT_DATES_VIEW);
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);
        final String city =  fields !=  null ? fields.getCityName() : null;
        final State  state =  fields !=  null ? fields.getState() : null;
        // Validate those inputs and give up if we can't build a reasonable page.
        if (state == null) {
            // no state name found on city page, so redirect to /
            View redirectView = new RedirectView("/");
            return new ModelAndView(redirectView);
        }

        if (city == null) {
            // no city name found, so redirect to /california or whichever state they did provide
            View redirectView = new RedirectView(DirectoryStructureUrlFactory.createNewStateBrowseURIRoot(state));
            return new ModelAndView(redirectView);
        }

        modelAndView.addObject("city", city);
        modelAndView.addObject("state", state);

        Integer collectionId = getCityHubHelper().getCollectionId(city, state);
        modelAndView.addObject("collectionId", collectionId);

        List<HubConfig> configList = getCityHubHelper().getHubConfig(city, state);
        ModelMap importantEventsMap = getCityHubHelper().getFilteredConfigMap(configList,  CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX);
        List<String> configKeyPrefixesSortedByDate = getCityHubHelper().getConfigKeyPrefixesSortedByDate(importantEventsMap);
        importantEventsMap.put(CityHubHelper.CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY, configKeyPrefixesSortedByDate);
        modelAndView.addObject(CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX, importantEventsMap);
        return modelAndView;
    }

    public boolean shouldHandleRequest(final DirectoryStructureUrlFields fields) {
        return fields == null ? false : fields.hasState() && fields.hasCityName() && fields.hasEventsPage()  && !fields.hasDistrictName() && !fields.hasLevelCode() && !fields.hasSchoolName();

    }

    public CityHubHelper getCityHubHelper() {
        return _cityHubHelper;
    }

    public void setCityHubHelper(CityHubHelper _cityHubHelper) {
        this._cityHubHelper = _cityHubHelper;
    }
}
