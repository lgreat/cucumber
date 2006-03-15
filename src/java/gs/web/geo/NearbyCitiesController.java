/*
* Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
* $Id: NearbyCitiesController.java,v 1.4 2006/03/15 02:24:20 apeterson Exp $
*/

package gs.web.geo;

import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpCity;
import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.SessionContextUtil;
import gs.web.SessionFacade;
import gs.web.util.Anchor;
import gs.web.util.ListModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides an UnorderList model of cities near the provided param "city" and "state".
 *
 * Parameters:
 * <li>state
 * <li>city
 * <li>count - maximum number of cities to show. (optional)
 *
 * Uses the styles "town", "city" and "bigCity" based on population.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class NearbyCitiesController extends AbstractController {
    private String _viewName;
    private IGeoDao _geoDao;

    private static final String PARAM_CITY = "city";
    private static final String PARAM_COUNT = "count";
    private static final int DEFAULT_MAX_CITIES = 15;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map model = new HashMap();

        ISessionFacade context = SessionFacade.getInstance(request);
        request.getParameter(SessionContextUtil.STATE_PARAM);

        State state = context.getStateOrDefault();

        String cityNameParam = request.getParameter(PARAM_CITY);
        if (StringUtils.isNotEmpty(cityNameParam) && state != null) {

            BpCity city = _geoDao.findCity(state, cityNameParam);

            if (city != null) {
                model.put(PARAM_CITY, city);

                int limit = DEFAULT_MAX_CITIES;
                if (request.getParameter(PARAM_COUNT) != null) {
                    limit = new Integer(request.getParameter(PARAM_COUNT)).intValue();
                }
                List nearbyCities = _geoDao.findNearbyCities(city, limit);

                model.put(ListModel.HEADING, "Cities Near " + city.getName());

                List items = new ArrayList(limit);
                for (int i = 0; i < limit; i++) {
                    BpCity nearbyCity = (BpCity) nearbyCities.get(i);
                    String styleClass = "town";
                    if (nearbyCity.getPopulation().intValue() > 50000) {
                        styleClass = (nearbyCity.getPopulation().longValue() > 200000) ? "bigCity" : PARAM_CITY;
                    }
                    Anchor anchor = new Anchor("/test/city.page?state=" +
                            nearbyCity.getState() +
                            "&amp;city=" +
                            nearbyCity.getName(),
                            nearbyCity.getName(),
                            styleClass);
                    items.add(anchor);
                }
                model.put(ListModel.RESULTS, items);
            }
        }


        ModelAndView modelAndView = new ModelAndView(_viewName, model);
        return modelAndView;
    }


    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }
}
