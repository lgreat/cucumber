/*
* Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
* $Id: NearbyCitiesController.java,v 1.19 2006/05/31 21:44:29 apeterson Exp $
*/

package gs.web.geo;

import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;
import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.SessionContextUtil;
import gs.web.SessionFacade;
import gs.web.util.Anchor;
import gs.web.util.AnchorListModel;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Provides an AnchorListModel of cities near the provided param "city" and "state".
 * Parameters:
 * <li>state
 * <li>city
 * <li>count - maximum number of cities to show. (optional)
 * Uses the styles "town", "city" and "bigCity" based on population.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class NearbyCitiesController extends AbstractController {
    protected static final String PARAM_CITY = "city";
    protected static final String PARAM_COUNT = "count";
    /**
     * Set if you want a "see more nearby cities..." link.
     */
    protected static final String PARAM_MORE = "more";
    /**
     * Set if you want a "see Browse all cities in CA..." link.
     */
    protected static final String PARAM_ALL = "all";
    /**
     * Set if you want to include the state in all names. Otherwise, only
     * those in cities of other states are indicated.
     */
    protected static final String PARAM_INCLUDE_STATE = "includeState";
    /**
     * How the cities are sorted. Default is by proximity, but also can use "alpha" for
     * alphabetical.
     */
    protected static final String PARAM_ORDER = "order";

    protected static final String PARAM_HEADING = "heading";


    // AnchorListModel.RESULTS has a list of Anchor objects
    protected static final String MODEL_CITY = "cityObject"; // Base city, ICity
    public static final String MODEL_CITIES = "cities"; // List of nearby cities

    private static final int DEFAULT_MAX_CITIES = 20;

    private String _viewName;
    private IGeoDao _geoDao;


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

                String heading = "Cities Near " + city.getName();
                if (request.getParameter(PARAM_HEADING) != null) {
                    heading = request.getParameter(PARAM_HEADING);
                }
                model.put(AnchorListModel.HEADING, heading);

                List items = new ArrayList(limit);
                for (int i = 0; i < limit && i < nearbyCities.size(); i++) {
                    ICity nearbyCity = (ICity) nearbyCities.get(i);

                    // name
                    String name = nearbyCity.getName();
                    if (request.getParameter(PARAM_INCLUDE_STATE) != null ||
                            !nearbyCity.getState().equals(state)) {
                        name += ", " + nearbyCity.getState().getAbbreviation();
                    }

                    // style class
                    String styleClass = "town";
                    long pop = 0;
                    if (nearbyCity.getPopulation() != null) {
                        pop = nearbyCity.getPopulation().intValue();
                    }
                    if (pop > 50000) {
                        styleClass = (pop > 200000) ? "bigCity" : "city";
                    }

                    // anchor
                    UrlBuilder builder = new UrlBuilder(nearbyCity, UrlBuilder.CITY_PAGE);
                    Anchor anchor = builder.asAnchor(request, name, styleClass);
                    items.add(anchor);
                }

                if (request.getParameter(PARAM_MORE) != null) {
                    UrlBuilder builder = new UrlBuilder(city, UrlBuilder.CITIES_MORE_NEARBY);
                    builder.setParameter(PARAM_ORDER, "alpha");
                    builder.setParameter(PARAM_INCLUDE_STATE, "1");
                    if (!state.equals(State.DC)) {
                        builder.setParameter(PARAM_ALL, "1");
                    }
                    Anchor anchor = builder.asAnchor(request, "More", "more");
                    items.add(anchor);
                }
                if (request.getParameter(PARAM_ALL) != null) {
                    UrlBuilder builder = new UrlBuilder(UrlBuilder.CITIES, state, null);
                    Anchor anchor = builder.asAnchor(request, "Browse all " + state.getLongName() + " cities",
                            "more");
                    items.add(anchor);

                }
                model.put(AnchorListModel.RESULTS, items);
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
