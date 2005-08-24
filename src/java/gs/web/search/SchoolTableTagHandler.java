package gs.web.search;

import gs.data.school.School;
import gs.data.school.ISchoolDao;
import gs.data.school.census.ICensusValueDao;
import gs.data.school.census.SchoolCensusInfo;
import gs.data.state.StateManager;
import gs.data.state.State;
import gs.web.SessionContext;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolTableTagHandler extends SimpleTagSupport {

    public static final String BEAN_ID = "schoolTableTagHandler";

    private List _schools = null;
    private ISchoolDao _schoolDao;
    private ICensusValueDao _censusValueDao;
    private StateManager _stateManager;
    private static final Log _log = LogFactory.getLog(SchoolTableTagHandler.class);

    public SchoolTableTagHandler() {
        _stateManager = new StateManager();
    }

    private ISchoolDao getSchoolDao() {

        try {
            if (_schoolDao == null) {
                JspContext jspContext = getJspContext();

                if (jspContext != null) {
                    SessionContext sc = (SessionContext) jspContext.getAttribute(SessionContext.SESSION_ATTRIBUTE_NAME, PageContext.SESSION_SCOPE);
                    if (sc != null) {
                        _schoolDao = sc.getSchoolDao();
                        _censusValueDao = sc.getCensusValueDao();
                    }
                }
            }
        } catch (Exception e) {
            _log.debug("problemmo: ", e);
        }
        return _schoolDao;
    }

    public void setSchools(List sList) {
        _schools = sList;
    }

    public void doTag() throws IOException {
        if (_schools != null) {
            JspWriter out = getJspContext().getOut();

            out.println("<table id=\"schoolstable\">");
            out.println("<tr>");
            out.println("<th>School</th>");
            out.println("<th>Type</th>");
            out.println("<th>Level</th>");
            out.println("<th>Average Reading Score</th>");
            out.println("<th>Average Math Score</th>");
            out.println("<th>Class Size</th>");
            out.println("<th>Enrollment</th>");
            out.println("<th>Free Lunch</th>");

            out.println("</tr>");

            for (int i = 0; i < _schools.size(); i++) {

                SearchResult sr = (SearchResult) _schools.get(i);

                boolean odd = true;
                if (i%2 == 0) {
                    odd = false;
                }

                out.print("<tr");
                if (odd) {
                    out.print(" class=\"odd\"");
                }
                out.println(">");
                School school = getSchool(sr);

                out.println("<td>");

                if (school != null) {
                    SchoolCensusInfo sci = _censusValueDao.getSchoolCensusInfo(school);

                    out.print("<h3><a href=\"http://www.greatschools.net/modperl/browse_school/");
                    out.print(school.getState().getAbbreviationLowerCase());
                    out.print("/");
                    out.print(school.getId().toString());
                    out.println ("\">");
                    out.println(school.getName());
                    out.println("</a></h3>");
                    out.println("<br/>");
                    out.println(school.getPhysicalAddress().toString());
                    out.println("</td><td class=\"st\">");
                    out.println(school.getType().getSchoolTypeName());
                    out.println("</td>");
                    out.println("<td class=\"lc\">");
                    out.println(school.getLevelCodeAsString());
                    out.println("</td>");
                    out.println ("<td>N.I.Y.</td>");
                    out.println ("<td>N.I.Y.</td>");
                    out.print("<td class=\"cs\">");
                    out.print(sci.getStudentTeacherRatio());
                    out.println("</td>");
                    out.print("<td class=\"en\">");
                    out.print(sci.getEnrollment());
                    out.println("</td>");
                    out.print("<td>");
                    out.print ("N.I.Y.");
                } else {
                    out.println("school is null!");
                    out.println("</td>");
                    out.println("<td>");
                }
                out.println("</td>");
                out.println("</tr>");
            }
            out.println("</table>");
        }
    }

    private School getSchool(SearchResult sr) {
        ISchoolDao sd = getSchoolDao();
        School school = null;
        try {
            State state = _stateManager.getState(sr.getState());
            if (state != null) {
                school = sd.getSchoolById(state, Long.valueOf(sr.getId()));
            }
        } catch (Exception e) {
            _log.warn("error retrieving school: ", e);
        }
        return school;
    }
}
