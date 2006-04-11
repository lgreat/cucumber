
/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: TopCitiesController.java,v 1.17 2006/04/11 20:13:48 apeterson Exp $
 */

package gs.web.state;

import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.SessionContextUtil;
import gs.web.SessionFacade;
import gs.web.util.Anchor;
import gs.web.util.ListModel;
import gs.web.util.UrlBuilder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Given a state (in the "state" param), creates a model of the largest
 * cities in the state (hand-tuned by our employees).
 * The number of cities returned is currently set by the State itself.
 * <p>
 * Uses the standard ListModel.
 *
 * @see ListModel
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class TopCitiesController extends AbstractController {

    public static final String PARAM_PATH = "path"; // path to the page
    public static final String PARAM_COUNT = "count"; // maximum number to show

    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ISessionFacade context = SessionFacade.getInstance(request);
        request.getParameter(SessionContextUtil.STATE_PARAM);

        State state = context.getStateOrDefault();

        Map model = new HashMap();

        // There is only one city in DC, so rewrite a little bit.
        if (state.equals(State.DC)) {
            model.put(ListModel.HEADING, state.getLongName() + " Schools");

            List items = new ArrayList(1);
            Anchor anchor = new Anchor("/schools.page?city=Washington&state=DC",
                    "View all schools");
            items.add(anchor);
            model.put(ListModel.RESULTS, items);

        } else {
            UrlBuilder builder = new UrlBuilder(request, "/schools.page");
            if (!StringUtils.isEmpty(request.getParameter(PARAM_PATH))) {
                builder.setPath(request.getParameter(PARAM_PATH));
            }
            builder.setParameter("state", state.getAbbreviation());

            int cityCount = state.getTopCityCount();
            if (!StringUtils.isEmpty(request.getParameter(PARAM_COUNT))) {
                cityCount = Integer.parseInt(request.getParameter(PARAM_COUNT));
            }

            model.put(ListModel.HEADING, state.getLongName() + " Cities");

            String[] cities = state.getTopCities();
            if (cities.length < cityCount) {
                cityCount = cities.length;
            }
            List items = new ArrayList(cityCount);
            for (int i = 0; i < cityCount; i++) {
                String city = cities[i];
                builder.setParameter("city", city);
                // special case for New York City...
                String label = ((city.equals(state.getLongName())) ? city + " City" : city) + " schools";
                Anchor anchor = builder.asAnchor(request, label);
                items.add(anchor);
            }
            items.add(new Anchor("/modperl/cities/" + state.getAbbreviation() + "/",
                    "View all " + state.getLongName() + " cities",
                    "viewall"));
            model.put(ListModel.RESULTS, items);
            model.put(ListModel.COLUMNS, new Integer(items.size() > 5 ? 2 : 1));
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

}
