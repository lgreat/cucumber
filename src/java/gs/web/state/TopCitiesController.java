/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: TopCitiesController.java,v 1.6 2005/11/15 01:39:35 apeterson Exp $
 */

package gs.web.state;

import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.SessionContextUtil;
import gs.web.SessionFacade;
import gs.web.util.Anchor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class TopCitiesController extends AbstractController {
    private String _viewName;
    private IDistrictDao _districtDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ISessionFacade context = SessionFacade.getInstance(request);
        request.getParameter(SessionContextUtil.STATE_PARAM);

        State state = context.getStateOrDefault();

        Map model = new HashMap();

        // There is only one city in DC, so rewrite a little bit.
        if (state.equals(State.DC)) {
            model.put("header", state.getLongName() + " Schools");

            List items = new ArrayList(1);
            Anchor anchor = new Anchor("/cgi-bin/schoollist/DC",
                    "View all schools");
            items.add(anchor);
            model.put("results", items);

        } else {
            model.put("header", state.getLongName() + " Cities");

            String[] cities = state.getTopCities();
            int cityCount = state.getTopCityCount();
            if (cities.length < cityCount) {
                cityCount = cities.length;
            }
            List items = new ArrayList(cityCount);
            for (int i = 0; i < cityCount; i++) {
                String city = cities[i];
                String urlEncodedCity = URLEncoder.encode(city, "UTF-8");
                Anchor anchor = new Anchor("/modperl/bycity/" + state.getAbbreviationLowerCase() +
                        "/?city=" + urlEncodedCity + "", // removed &level=a 11/05 AJP
                        city + " schools");
                items.add(anchor);
            }
            items.add(new Anchor("/modperl/citylist/" + state.getAbbreviation() + "/",
                    "View all " + state.getLongName() + " cities",
                    "viewall"));
            model.put("results", items);

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

    public IDistrictDao getDistrictDao() {
        return _districtDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }
}
