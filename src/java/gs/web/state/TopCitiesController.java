/*
* Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
* $Id: TopCitiesController.java,v 1.27 2009/10/06 18:42:00 droy Exp $
*/

package gs.web.state;

import gs.data.state.State;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.Anchor;
import gs.web.util.list.AnchorListModel;
import gs.web.util.UrlBuilder;
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
 * Given a state (in the "state" param), creates a model of the largest
 * cities in the state (hand-tuned by our employees).
 * The number of cities returned is currently set by the State itself.
 * Uses the standard AnchorListModel.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 * @see AnchorListModel
 */
public class TopCitiesController extends AbstractController {

    public static final String PARAM_COUNT = "count"; // maximum number to show

    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        SessionContext context = SessionContextUtil.getSessionContext(request);
        request.getParameter(SessionContextUtil.STATE_PARAM);

        State state = context.getStateOrDefault();

        Map model = new HashMap();

        // There is only one city in DC, so rewrite a little bit.
        if (state.equals(State.DC)) {
            model.put(AnchorListModel.HEADING, state.getLongName() + " Schools");

            List items = new ArrayList(1);
            Anchor anchor = new Anchor("/washington-dc/washington/schools/",
                    "View all schools", "viewall");
            items.add(anchor);

            if (State.DC.equals(state)) {
                UrlBuilder dcCityPage = new UrlBuilder(UrlBuilder.CITY_PAGE, State.DC, "Washington");
                items.add(dcCityPage.asAnchor(request, "View city information", "viewall"));
            }

            model.put(AnchorListModel.RESULTS, items);

        } else {

            int cityCount = state.getTopCityCount();
            if (!StringUtils.isEmpty(request.getParameter(PARAM_COUNT))) {
                cityCount = Integer.parseInt(request.getParameter(PARAM_COUNT));
            }

            model.put(AnchorListModel.HEADING, state.getLongName() + " Cities");

            String[] cities = state.getTopCities();
            if (cities.length < cityCount) {
                cityCount = cities.length;
            }
            List items = new ArrayList(cityCount);
            for (int i = 0; i < cityCount; i++) {
                String city = cities[i];
                UrlBuilder builder = new UrlBuilder(UrlBuilder.CITY_PAGE, state, city);
                // special case for New York City...
                String label = ((city.equals(state.getLongName())) ? city + " City" : city) + " schools";
                Anchor anchor = builder.asAnchor(request, label);
                items.add(anchor);
            }
            items.add(new Anchor("/schools/cities/" + state.getLongName() + "/"+ state.getAbbreviation(),
                    "View all " + state.getLongName() + " cities",
                    "viewall"));
            model.put(AnchorListModel.RESULTS, items);
            model.put(AnchorListModel.COLUMNS, new Integer(items.size() > 5 ? 2 : 1));
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
