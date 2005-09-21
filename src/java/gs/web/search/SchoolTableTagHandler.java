package gs.web.search;

import gs.data.school.School;
import gs.data.state.State;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This tag handler generates a table of schools.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolTableTagHandler extends ResultsTableTagHandler {

    public static final String BEAN_ID = "schoolTableTagHandler";

    private List _schools = null;

    private static final Log _log = LogFactory.getLog(SchoolTableTagHandler.class);


    public void setSchools(List sList) {
        _schools = sList;
    }

    public String getConstraint() {
        return "school";
    }

    public void doTag() throws IOException {
        long start = System.currentTimeMillis();

        if (_schools != null) {

            JspWriter out = getJspContext().getOut();


            out.println("<form action=\"compareSchools.page\">");
            out.println("<table class=\"columns\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
            out.println("<tr><td valign=\"top\">");
            out.println("<table class=\"school_results_only\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");

            if (_schools.size() > 0) {
                out.println("<tr><th class=\"result_title\" width=\"1\">&nbsp;</th>");

                StringBuffer buffer = new StringBuffer();
                buffer.append("<th class=\"left\"><a href=\"/search.page?c=school");
                if (getQueryString() != null) {
                    buffer.append("&amp;q=");
                    buffer.append(getQueryString());
                }
                String href = buffer.toString();
                out.print(href);
                out.print("&amp;sort=name");

                if (getSortColumn() != null && getSortColumn().equals("name")) {
                    if (sortReverse()) {
                        out.print("&amp;r=f");
                    } else {
                        out.print("&amp;r=t");
                    }
                }
                out.println("\">");
                out.print("Schools</a></th>");

                out.println("<th class=\"result_title\">Type</th>");
                out.println("<th class=\"result_title\">Grade</th>");
                out.println("<th class=\"result_title\">Enrollment</th>");

                out.println("</tr>");

                for (int i = 0; i < _schools.size(); i++) {

                    SearchResult sResult = (SearchResult) _schools.get(i);
                    School school = getSchool(sResult);

                    out.println("<tr class=\"result_row\">");
                    out.println("<td class=\"checkbox\" width=\"1\">");
                    out.print("<input name=\"sc\" type=\"checkbox\"  value=\"");
                    out.print(school.getState().getAbbreviationLowerCase());
                    out.print(school.getId());
                    out.print("\" /></td>");

                    out.println("<td>");

                    out.print("<a href=\"http://www.greatschools.net/modperl/browse_school/");
                    out.print(school.getState().getAbbreviationLowerCase());
                    out.print("/");
                    out.print(school.getId().toString());
                    out.println("\">");

                    out.println(school.getName());
                    out.println("</b></a><br/>");
                    out.print(school.getPhysicalAddress().toString());
                    out.println("<br/>");

                    //out.print(school.getDistrict().getName());
                    out.print("District not implemented yet");
                    out.println("</td>");
                    out.println("<td align=\"center\">");
                    out.println(school.getType().getSchoolTypeName());
                    out.println("</td><td align=\"center\">");
                    out.println(school.getGradeLevels().getRangeString());
                    out.println("</td><td align=\"center\">");
                    out.print(school.getEnrollment());
                    out.println("</td>");
                    out.println("</tr>");
                }
                out.print("<tr class=\"last_row\"><td colspan=\"5\">");

                State s = getState();
                if (s != null) {
                    out.print("<ul><li class=\"viewall\">");
                    out.print("<a href=\"http://www.greatschools.net/cgi-bin/template_plain/advanced/");
                    out.print(s.getAbbreviation());
                    out.println("\">");
                    out.print("Browse all ");
                    out.print(s.getLongName());
                    out.println(" schools</a></li></ul>");
                }

                out.println("</td></tr></table>");
                out.println("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
                out.println("<td>");

                out.println("<input style=\"display:block\" type=\"image\" src=\"res/img/search/btn_old_comparechecked_167x21.gif\" alt=\"Submit Form\">");
                out.println("<input style=\"display:block\" type=\"image\" src=\"res/img/search/btn_old_savechecked2msl_193x21.gif\" alt=\"Submit Form\">");

                out.println("</td><td class=\"results_pagenav\">");

                writePageNumbers(out);

            } else {
                out.println("<tr><th class=\"left result_title\">No results found</div></th></tr>");
                out.println("<tr><td valign=\"top\" height=\"100\">");
            }
            out.println("</td></tr></table>");
            out.println("</td></tr></table>");
            out.println("</form>");
        }
        long end = System.currentTimeMillis();
        _log.debug("SchoolTableTagHandler.doTag takes: " + (end - start) + " milliseconds");
    }
}
