package gs.web.geo;

import gs.data.state.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 9/25/13
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/cityHub/educationCommunity.page")
public class CityHubEducationCommunity {
    public static final String EDUCATION_COMMUNITY_VIEW = "/cityHub/educationCommunity";

    @Autowired
    private CityHubHelper _cityHubHelper;

    @RequestMapping(method = RequestMethod.GET)
    public String showPage(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        String city = "Washington";
        State state = State.DC;

        modelMap.put("city", city);
        modelMap.put("state", state);

        Integer collectionId = getCityHubHelper().getHubID(city, state);
        modelMap.put("collectionId", collectionId);

        modelMap.put(CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX, getCityHubHelper().getImportantModuleMap(state, city));

        return EDUCATION_COMMUNITY_VIEW;
    }

    public CityHubHelper getCityHubHelper() {
        return _cityHubHelper;
    }

    public void setCityHubHelper(CityHubHelper _cityHubHelper) {
        this._cityHubHelper = _cityHubHelper;
    }
}
