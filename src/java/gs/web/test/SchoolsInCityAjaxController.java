package gs.web.test;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.PrintWriter;

import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.LevelCode;
import gs.data.util.XMLUtil;

/**
 * This class provides is a service provider for ajax requests from /test/landing.page
 * for school lists.  The required request parameters are:
 *     state: a 2-letter abbreviation
 *     city: the cannonical city name
 * 
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public class SchoolsInCityAjaxController implements Controller {

    protected final Logger _log = Logger.getLogger(getClass());
    public static final String BEAN_ID = "/test/schoolsInCity.page";
    private ISchoolDao _schoolDao;
    private static final StateManager _stateManager = new StateManager();

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String levels = request.getParameter("levels");
        LevelCode filter = null;
        if (StringUtils.isNotBlank(levels)) {
            filter = LevelCode.createLevelCode(levels);
        }
        response.setContentType("text/xml");
        PrintWriter out = response.getWriter();
        try {
            outputSchoolSelect(request, out, filter);
        } catch (Exception e) {
            _log.warn("Error getting school list.", e);
            out.println("Error getting school list");
        }
        return null;
    }

    protected void outputSchoolSelect(HttpServletRequest request, PrintWriter out, LevelCode filter) {
        State state = _stateManager.getState(request.getParameter("state"));
        String onChange = request.getParameter("onchange");
        String city = request.getParameter("city");
        String includePrivateSchoolsStr = request.getParameter("includePrivateSchools");
        boolean includePrivateSchools = (StringUtils.isNotBlank(includePrivateSchoolsStr) ? Boolean.valueOf(includePrivateSchoolsStr) : false);
        String chooseSchoolLabel = request.getParameter("chooseSchoolLabel");
        if (StringUtils.isBlank(chooseSchoolLabel)) {
            chooseSchoolLabel = "2. Choose school";
        }
        List<School> schools = _schoolDao.findSchoolsInCity(state, city, false);
        out.println("<select id=\"schoolSelect\" name=\"sid\" class=\"selectSchool\""+(StringUtils.isNotBlank(onChange) ? " onchange=\"" + onChange + "\"" : "") +  ">");
        out.println("<option value=\"\">" + chooseSchoolLabel + "</option>");
        for (School school : schools) {
            if (includePrivateSchools || school.getType() != SchoolType.PRIVATE) {
                if (filter != null) {
                    if (!filter.containsSimilarLevelCode(school.getLevelCode())) {
                        continue;
                    } 
                }
                out.print("<option value=\"" + school.getId() + "\">");
                out.print(StringEscapeUtils.escapeHtml(school.getName()));
                out.println("</option>");
            }
        }
        out.print("</select>");
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }
}