/*
* Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
* $Id: NearbyCitiesController.java,v 1.11 2006/03/31 19:28:32 apeterson Exp $
*/

package gs.web.geo;

import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;
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
import java.util.*;

/**
 * Provides an ListModel of cities near the provided param "city" and "state".
 * Parameters:
 * <li>state
 * <li>city
 * <li>count - maximum number of cities to show. (optional)
 * Uses the styles "town", "city" and "bigCity" based on population.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class NearbyCitiesController extends AbstractController {
    private String _viewName;
    private IGeoDao _geoDao;

    private static final String PARAM_CITY = "city";
    private static final String PARAM_COUNT = "count";
    /**
     * Set if you want a "see more nearby cities..." link.
     */
    private static final String PARAM_MORE = "more";
    /**
     * How the cities are sorted. Default is by proximity, but also can use "alpha" for
     * alphabetical.
     */
    private static final String PARAM_ORDER = "order";
    private static final int DEFAULT_MAX_CITIES = 20;

    private static final String MODEL_CITY = "cityObject";
    public static final String MODEL_CITIES = "cities"; // List of nearby cities

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map model = new HashMap();

        ISessionFacade context = SessionFacade.getInstance(request);
        request.getParameter(SessionContextUtil.STATE_PARAM);

        State state = context.getStateOrDefault();

        String cityNameParam = request.getParameter(PARAM_CITY);
        if (StringUtils.isNotEmpty(cityNameParam) && state != null) {

            ICity city = _geoDao.findCity(state, cityNameParam);

            if (city != null) {
                model.put(MODEL_CITY, city);

                int limit = DEFAULT_MAX_CITIES;
                if (request.getParameter(PARAM_COUNT) != null) {
                    limit = new Integer(request.getParameter(PARAM_COUNT)).intValue();
                }
                List nearbyCities = _geoDao.findNearbyCities(city, limit);

                if (StringUtils.equals("alpha", request.getParameter(PARAM_ORDER))) {
                    Collections.sort(nearbyCities, new Comparator() {
                        public int compare(Object o, Object o1) {
                            return ((ICity) o).getName().compareToIgnoreCase(((ICity) o1).getName());
                        }
                    });
                }

                model.put(MODEL_CITIES, nearbyCities);

                model.put(ListModel.HEADING, "Nearby cities");

                List items = new ArrayList(limit);
                for (int i = 0; i < limit && i < nearbyCities.size(); i++) {
                    ICity nearbyCity = (ICity) nearbyCities.get(i);
                    String styleClass = "town";
                    long pop = 0;
                    if (nearbyCity.getPopulation() != null) {
                        pop = nearbyCity.getPopulation().intValue();
                    }
                    if (pop > 50000) {
                        styleClass = (pop > 200000) ? "bigCity" : "city";
                    }
                    Anchor anchor = new Anchor("/city.page?state=" +
                            nearbyCity.getState() +
                            "&amp;city=" +
                            nearbyCity.getName(),
                            nearbyCity.getName(),
                            styleClass);
                    items.add(anchor);
                }

                if (request.getParameter(PARAM_MORE) != null) {
                    Anchor anchor = new Anchor("/cities.page?state=" +
                            state +
                            "&amp;city=" +
                            cityNameParam +
                            "&amp;order=alpha",
                            "More...",
                            "more");
                    items.add(anchor);
                } else {
                    Anchor anchor = new Anchor("/modperl/citylist/" +
                            state +
                            "/",
                            "Browse all " + state.getLongName() + " cities...",
                            "more");
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
