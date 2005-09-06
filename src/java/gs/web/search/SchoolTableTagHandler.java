package gs.web.search;

import gs.data.school.School;
import gs.data.school.ISchoolDao;
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
    private String _queryString = null;
    private String _sortColumn = null;
    private boolean _reverse = false;
    private ISchoolDao _schoolDao;
    private StateManager _stateManager;

    private static final Log _log = LogFactory.getLog(SchoolTableTagHandler.class);

    public SchoolTableTagHandler() {
        _stateManager = new StateManager();
    }

    private ISchoolDao getSchoolDao() {
        if (_schoolDao == null) {
            try {
                JspContext jspContext = getJspContext();

                if (jspContext != null) {
                    SessionContext sc = (SessionContext) jspContext.getAttribute(SessionContext.SESSION_ATTRIBUTE_NAME, PageContext.SESSION_SCOPE);
                    if (sc != null) {
                        _schoolDao = sc.getSchoolDao();
                    }
                }

            } catch (Exception e) {
                _log.warn("problem getting ISchoolDao: ", e);
            }
        }
        return _schoolDao;
    }

    public void setSchools(List sList) {
        _schools = sList;
    }

    public void setQuery(String query) {
        _queryString = query;
    }

    public void setSortColumn(String sort) {
        if (sort != null) {
            _sortColumn = sort;
        }
    }

    public void setReverse(String reverse) {
        if (reverse != null && reverse.equals("t")) {
            _reverse = true;
        }
    }

    public void doTag() throws IOException {
        long start = System.currentTimeMillis();

        if (_schools != null) {

            JspWriter out = getJspContext().getOut();


            StringBuffer buffer = new StringBuffer();
            buffer.append("<th><a href=\"/search.page?c=school");

            if (_queryString != null) {
                buffer.append("&amp;q=");
                buffer.append(_queryString);
            }

            String href = buffer.toString();

            out.print("<form action=\"compareSchools.page\">");
            out.println("<table id=\"schools\" cellpadding=\"0\" cellspacing=\"0\">");
            out.println("<tr>");
            out.println("<th></th>");
            out.print(href);
            out.print("&amp;sort=name");

            if (_sortColumn != null && _sortColumn.equals("name")) {
                if (_reverse) {
                    out.print("&amp;r=f");
                } else {
                    out.print("&amp;r=t");
                }
            }
            out.println("\">");
            out.println("School</a></th>");

            out.print(href);
            out.println("&amp;sort=schooltype");
            if (_sortColumn != null && _sortColumn.equals("schooltype")) {
                if (_reverse) {
                    out.print("&amp;r=f");
                } else {
                    out.print("&amp;r=t");
                }
            }
            out.println("\">");

            out.print("Type</a></th>");

            out.println("<th><a href=\"\">Grade Range</a></th>");
            out.println("<th>Class Size</th>");
            out.println("<th>Enrollment</th>");
            out.println("</tr>");

            for (int i = 0; i < _schools.size(); i++) {

                SearchResult sr = (SearchResult) _schools.get(i);

                boolean odd = true;
                if (i % 2 == 0) {
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
                    out.print("<input name=\"sc\" type=\"checkbox\"  value=\"");
                    out.print(school.getState().getAbbreviationLowerCase());
                    out.print(school.getId());
                    out.print("\" />");
                    out.println("</td>");
                    out.print("<td>");
                    out.print("<h3><a href=\"http://www.greatschools.net/modperl/browse_school/");
                    out.print(school.getState().getAbbreviationLowerCase());
                    out.print("/");
                    out.print(school.getId().toString());
                    out.println("\">");
                    out.println(school.getName());
                    out.println("</a></h3>");
                    out.print("<address>");
                    out.print(school.getPhysicalAddress().toString());
                    out.println("</address>");
                    out.println("</td><td class=\"st\">");
                    out.println(school.getType().getSchoolTypeName());
                    out.println("</td>");
                    out.println("<td class=\"lc\">");
                    out.println(school.getGradeLevels().getRangeString());
                    out.println("</td>");
                    out.print("<td class=\"cs\">");
                    out.print(school.getClassSize());
                    out.println("</td>");
                    out.print("<td class=\"en\">");
                    out.print(school.getEnrollment());
                } else {
                    out.println("school is null!");
                    out.println("</td>");
                    out.println("<td>");
                }
                out.println("</td>");
                out.println("</tr>");
            }
            // footer
            out.print("<tr id=\"schoolfooter\">");
            out.print("<td></td>");
            out.print("<td colspan=\"7\">");
            out.print("<input type=\"submit\" value=\" Add checked schools to My Schools List \"/>");
            out.print("<input type=\"submit\" value=\" Compare checked schools \"/>");
            out.println("</td></tr>");

            out.println("</table>");
            out.print("</form>");
        }
        long end = System.currentTimeMillis();
        _log.debug("SchoolTableTagHandler.doTag takes: " + (end-start) + " milliseconds");
    }

    private School getSchool(SearchResult sr) {
        long start = System.currentTimeMillis();
        School school = null;
        try {
            State state = _stateManager.getState(sr.getState());
            if (state != null) {
                school = getSchoolDao().getSchoolById(state, Integer.valueOf(sr.getId()));
            }
        } catch (Exception e) {
            _log.warn("error retrieving school: ", e);
        }
        long end = System.currentTimeMillis();
        _log.debug("SchoolTableTagHandler.getSchool takes: " + (end-start) + " milliseconds");
        return school;
    }
}
