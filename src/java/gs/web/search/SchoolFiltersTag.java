package gs.web.search;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

/**
 * This class writes the table grid of checkboxes used in searchControl.tagx
 * to filter school results.
 *
 * @author Chris Kimm <mailto:chris@seeqa.com>
 */
public class SchoolFiltersTag extends BaseQueryTagHandler {//SimpleTagSupport {

    private Set _schoolTypeSet;
    private Set _gradeLevelSet;
    private static Set _defaultSchoolTypeSet = new HashSet();
    private static Set _defaultGradeLevelSet = new HashSet();

    static {
        _defaultGradeLevelSet.add("elementary");
        _defaultGradeLevelSet.add("middle");
        _defaultGradeLevelSet.add("high");

        _defaultSchoolTypeSet.add("public");
        _defaultSchoolTypeSet.add("private");
        _defaultSchoolTypeSet.add("charter");
    }
    /**
     * This is collection of schooltype checks: public; private; charter
     * If null is passed as an argument, defaults to all three types.
     * @param o
     */
    public void setSchoolTypes(Object o) {
        String[] params = (String[])o;
        if (params != null) {
            _schoolTypeSet = new HashSet();
            for (int i = 0; i < params.length; i++) {
                _schoolTypeSet.add(params[i]);
            }
        } else {
            _schoolTypeSet = _defaultSchoolTypeSet;
        }
    }

    /**
     * This is the collection of gradeLevel checks: elementary; middle; high
     * If null is passed as the argument, defaults to all three types.
     * @param o
     */
    public void setGradeLevels(Object o) {
        String[] params = (String[])o;
        if (params != null) {
            _gradeLevelSet = new HashSet();
            for (int i = 0; i < params.length; i++) {
                _gradeLevelSet.add(params[i]);
            }
        } else {
            _gradeLevelSet = _defaultGradeLevelSet;
        }
    }

    /**
     * Writes a table with 3 schooltype checkboxes and 3 gradelevel checkboxes.
     * @throws IOException
     */
    public void doTag() throws IOException {

        JspWriter out = getJspContext().getOut();

        out.print("<form action=\"/search/search.page\">");
        out.print("<input type=\"hidden\" name=\"c\" value=\"school\">");
        out.print("<input type=\"hidden\" name=\"state\" value=\"");
        out.print(getStateOrDefault().getAbbreviationLowerCase());
        out.print("\">");
        out.print("<input type=\"hidden\" name=\"q\" value=\"");
        out.print(_query);
        out.print("\">");
        out.println("<table><tr><td>");
        //out.println("<td>");

        out.println("<div class=\"checkbox\">");
        out.print("<input id=\"stpub\" type=\"checkbox\" name=\"st\" ");
        if (_schoolTypeSet != null && _schoolTypeSet.contains("public")) {
            out.print("checked ");
        }
        out.print("value=\"public\" onClick=\"checkSchoolTypes('stpub')\">");
        out.print(" Public");
        out.println("</input>");
        out.println("</div>");

        out.println("<div class=\"checkbox\">");
        out.print("<input id=\"stpri\" type=\"checkbox\" name=\"st\" ");
        if (_schoolTypeSet != null && _schoolTypeSet.contains("private")) {
            out.print("checked ");
        }
        out.print("value=\"private\" onClick=\"checkSchoolTypes('stpri')\">");
        out.print(" Private");
        out.println("</input>");
        out.println("</div>");

        out.println("<div class=\"checkbox\">");
        out.print("<input id=\"stcha\" type=\"checkbox\" name=\"st\" ");
        if (_schoolTypeSet != null && _schoolTypeSet.contains("charter")) {
            out.print("checked ");
        }
        out.print("value=\"charter\" onClick=\"checkSchoolTypes('stcha')\">");
        out.print(" Charter");
        out.println("</input>");
        out.println("</div>");

        out.println("</td><td style=\"padding-left:12mm\">");

        out.println("<div class=\"checkbox\">");
        out.print("<input id=\"gle\" type=\"checkbox\" name=\"gl\" ");
        if (_gradeLevelSet != null && _gradeLevelSet.contains("elementary")) {
            out.print("checked ");
        }
        out.print("value=\"elementary\" onClick=\"checkGradeLevels('gle')\">");
        out.print(" Elementary");
        out.println("</input>");
        out.println("</div>");

        out.println("<div class=\"checkbox\">");
        out.print("<input id=\"glm\" type=\"checkbox\" name=\"gl\" ");
        if (_gradeLevelSet != null && _gradeLevelSet.contains("middle")) {
            out.print("checked ");
        }
        out.print("value=\"middle\" onClick=\"checkGradeLevels('glm')\">");
        out.print(" Middle");
        out.println("</input>");
        out.println("</div>");

        out.println("<div class=\"checkbox\">");
        out.print("<input id=\"glh\" type=\"checkbox\" name=\"gl\" ");
        if (_gradeLevelSet != null && _gradeLevelSet.contains("high")) {
            out.print("checked ");
        }
        out.print("value=\"high\" onClick=\"checkGradeLevels('glh')\">");
        out.print(" High");
        out.println("</input>");
        out.println("</div>");

        out.println("</td>");
        out.println ("<td><input type=\"submit\" value=\"Filter Schools\" /></td>");
        out.println("</tr></table");
        out.println("</form>");
    }
}
