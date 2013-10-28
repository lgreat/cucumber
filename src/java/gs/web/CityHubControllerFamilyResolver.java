package gs.web;

/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 8/16/13
 * Time: 3:29 PM
 * To change this template use File | Settings | File Templates.
 */


import gs.data.hubs.IHubCityMappingDao;
import gs.data.hubs.IHubConfigDao;
import gs.web.geo.CityHubHelper;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.request.RequestAttributeHelper;
import gs.data.state.State;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * City Hub Controller Family Resolver.
 * @author Shomi Arora sarora@greatschools.org
 */
public class CityHubControllerFamilyResolver  implements IControllerFamilyResolver{

    @Autowired
    private RequestAttributeHelper _requestAttributeHelper;


    @Autowired
    private CityHubHelper _cityHubHelper;

    private static final String WASHINGTON_HUB_CITY= "washington";

    private static final State  WASHINGTON_HUB_STATE= State.DC;

    private static final String MILWAUKEE_HUB_CITY= "milwaukee";

    private static final State  MILWAUKEE_HUB_STATE = State.WI;

    private static final String DETROIT_HUB_CITY= "detroit";

    private static final State  DETROIT_HUB_STATE = State.MI;



    public ControllerFamily resolveControllerFamily() {
        /**
         * By default the controller should be City Default Controller.
         */
        ControllerFamily cityController= ControllerFamily.CITY_DEFAULT;
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
         /**
         * This Code should change to add the logic for city pages .Possibly string match on URL. -To do Shomi Revert
         */
        if (request == null) {
            throw new IllegalStateException("Request cannot be null.");
        }

        DirectoryStructureUrlFields fields = _requestAttributeHelper.getDirectoryStructureUrlFields(request);
        final String cityName = fields.getCityName();
        final State  state    = fields.getState();

        if (shoulCityLocalControllerBeUsed(cityName, state)) {
            cityController= ControllerFamily.CITY_LOCAL;
        }

        return cityController;
    }


    private boolean shoulCityLocalControllerBeUsed(final String cityName, final State state)
    {
          return  true ? _cityHubHelper.getCollectionId(cityName, state) != null : false;
    }
}
