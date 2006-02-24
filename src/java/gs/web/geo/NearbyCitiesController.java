/*
* Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
* $Id: NearbyCitiesController.java,v 1.2 2006/02/24 23:41:42 apeterson Exp $
*/

package gs.web.geo;

import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpCity;
import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.SessionContextUtil;
import gs.web.SessionFacade;
import gs.web.util.Anchor;
import gs.web.util.UnorderedListModel;
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
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class NearbyCitiesController extends AbstractController {
    private String _viewName;
    private IGeoDao _geoDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map model = new HashMap();

        ISessionFacade context = SessionFacade.getInstance(request);
        request.getParameter(SessionContextUtil.STATE_PARAM);

        State state = context.getStateOrDefault();

        String cityNameParam = request.getParameter("city");
        if (StringUtils.isNotEmpty(cityNameParam) && state != null) {

            BpCity city = _geoDao.findCity(state, cityNameParam);

            if (city != null) {
                model.put("city", city);

                final int limit = 15;
                List nearbyCities = _geoDao.findNearbyCities(city, limit);

                model.put(UnorderedListModel.HEAD, "Cities Near " + city.getName());

                List items = new ArrayList(limit);
                for (int i = 0; i < limit; i++) {
                    BpCity nearbyCity = (BpCity) nearbyCities.get(i);
                    String styleClass = "town";
                    if (nearbyCity.getPopulation().intValue() > 50000) {
                        styleClass = (nearbyCity.getPopulation().intValue() > 500000) ? "bigCity" : "city";
                    }

                    Anchor anchor = new Anchor("/test/city.page?state=" +
                            nearbyCity.getState() +
                            "&amp;city=" +
                            nearbyCity.getName(),
                            nearbyCity.getName(),
                            styleClass);
                    items.add(anchor);
                }
                model.put(UnorderedListModel.RESULTS, items);
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
