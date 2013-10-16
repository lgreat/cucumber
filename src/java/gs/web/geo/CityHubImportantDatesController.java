package gs.web.geo;

import gs.data.hubs.HubConfig;
import gs.data.state.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
@RequestMapping("/cityHub/importantDates.page")
public class CityHubImportantDatesController {
    private static final String IMPORTANT_EVENT_DATES_VIEW = "/cityHub/importantDates";

    @Autowired
    private CityHubHelper _cityHubHelper;

    @RequestMapping(method = RequestMethod.GET)
    public String showPage(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        State state= State.MI;
        String city= "detroit";

        modelMap.put("city", city);
        modelMap.put("state", state);

        Integer collectionId = getCityHubHelper().getCollectionId(city, state);
        modelMap.put("collectionId", collectionId);

        List<HubConfig> configList = getCityHubHelper().getHubConfig(city, state);
        ModelMap importantEventsMap = getCityHubHelper().getFilteredConfigMap(configList,  CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX);
        List<String> configKeyPrefixesSortedByDate = getCityHubHelper().getConfigKeyPrefixesSortedByDate(importantEventsMap);
        importantEventsMap.put(CityHubHelper.CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY, configKeyPrefixesSortedByDate);
        modelMap.put(CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX, importantEventsMap);
        return IMPORTANT_EVENT_DATES_VIEW;
    }

    public CityHubHelper getCityHubHelper() {
        return _cityHubHelper;
    }

    public void setCityHubHelper(CityHubHelper _cityHubHelper) {
        this._cityHubHelper = _cityHubHelper;
    }
}
