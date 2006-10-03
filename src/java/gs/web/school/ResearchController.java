package gs.web.school;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import gs.web.util.context.ISessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlBuilder;
import gs.data.state.State;

/**
 * Controls the research and compare page.  Currently we're letting the Perl pages
 * handle all of the error handling so this page does not use a command object or
 * Spring error handling (binding) system.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class ResearchController extends AbstractController {

    /** Used to identify which form on the page was submitted */
    private final static String FORM_PARAM = "form";

    /** Used by the "district" form */
    private final static String DISTRICT_PARAM = "district";

    /** Used by the state pull-downs */
    private final static String STATE_PARAM = "state";

    /** Used for the school level codes */
    private final static String LEVEL_PARAM = "level";

    /** The # of cities to display in the top cities list */
    private final static int CITY_LIST_SIZE = 5;

    /** The form view - set in pages-servlet.xml */
    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String form = request.getParameter(FORM_PARAM);
        if (StringUtils.isNotBlank (form)) {
            StringBuffer buf = new StringBuffer ();
            String stateParam = request.getParameter(STATE_PARAM);
            if ("district".equals(form)) {
                String district = request.getParameter(DISTRICT_PARAM);
                String level = request.getParameter(LEVEL_PARAM);
                buf.append("/cgi-bin/cs_distlist/");
                buf.append(stateParam.toLowerCase());
                buf.append("/?area=d&district=").append(district);
                buf.append("&level=").append(level);
            } else if ("cities".equals(form)) {
                buf.append("/modperl/cities/");
                buf.append(stateParam).append("/");
            }
            return new ModelAndView (new RedirectView(buf.toString()));
        }

        ISessionContext context = SessionContextUtil.getSessionContext(request);
        State state = context.getState();
        Map model = new HashMap ();
        model.put("cities", getCitiesForState (state));
        return new ModelAndView (getViewName(), model);
    }

    /**
     * Populate a <code>List</code> with the top cities in the state.  If the
     * supplied State arguement is null, then return a List of the top
     * national cities:
     *       New York
     *       Los Angeles
     *       Chicago
     *       Houston
     *       Miami
     * @param state
     * @return
     */
    List getCitiesForState (State state) {
        List cities = new ArrayList();
        if (state != null) {
            String[] cityNames = state.getTopCities();
            for (int i = 0; i < CITY_LIST_SIZE; i++) {
                // If there is a city for this index, then use it.
                Map data = new HashMap ();
                if (!ArrayUtils.isEmpty(cityNames) && (i < cityNames.length)) {
                    data.put("name", cityNames[i]);
                    UrlBuilder cityPageUrl = new UrlBuilder(UrlBuilder.CITY_PAGE, state, cityNames[i]);
                    data.put("link", cityPageUrl.toString());
                } else {
                    // Otherwise, fill the list with filler data
                    data.put("name", "");
                    data.put("link", "");
                }
                cities.add (data);
            }
        } else {
            Map data = new HashMap ();
            data.put("name", "New York City");
            data.put("link", new UrlBuilder(UrlBuilder.CITY_PAGE, State.NY, "New York"));
            cities.add(data);
            data = new HashMap ();
            data.put("name", "Los Angeles");
            data.put("link", new UrlBuilder(UrlBuilder.CITY_PAGE, State.CA, "Los Angeles"));
            cities.add(data);
            data = new HashMap ();
            data.put("name", "Chicago");
            data.put("link", new UrlBuilder(UrlBuilder.CITY_PAGE, State.IL, "Chicago"));
            cities.add(data);
            data = new HashMap ();
            data.put("name", "Houston");
            data.put("link", new UrlBuilder(UrlBuilder.CITY_PAGE, State.TX, "Houston"));
            cities.add(data);
            data = new HashMap ();
            data.put("name", "Miami");
            data.put("link", new UrlBuilder(UrlBuilder.CITY_PAGE, State.FL, "Miami"));
            cities.add(data);
        }
        return cities;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
