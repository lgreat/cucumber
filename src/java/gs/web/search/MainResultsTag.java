package gs.web.search;

import org.apache.commons.lang.StringUtils;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.util.List;

import gs.data.school.School;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class MainResultsTag extends ResultsTableTagHandler {

    private List _results;
    private String _constraint = null;

    public void setResults(List results) {
        _results = results;
    }

    public void setConstraint(String c) {
        _constraint = c;
    }

    public String getConstraint() {
        return _constraint;
    }

    public void doTag() throws IOException {

        JspWriter out = getJspContext().getOut();

        out.print("<table width=\"100%\">");
        out.println("<tr>");
        out.println("<td colspan=\"2\">");
        if (_results != null && _total > 0) {
            out.println("<table id=\"mainresults\">");
            for (int i = 0; i < _results.size(); i++) {
                SearchResult result = (SearchResult) _results.get(i);
                out.println("<tr class=\"result_row\">");

                out.print("<td class=\"headline\" colspan=\"3\">");
                switch (result.getType()) {
                    case SearchResult.SCHOOL:
                        out.print("<a href=\"http://");
                        out.print(getHostname());
                        out.print("/modperl/browse_school/");
                        out.print(result.getState());
                        out.print("/");
                        out.print(result.getId());
                        out.println("\">");
                        break;
                    case SearchResult.ARTICLE:
                        out.print("<a href=\"http://");
                        out.print(getHostname());
                        out.print("/cgi-bin/show");
                        if (result.isInsider()) {
                            out.print("part");
                        }
                        out.print("article/");
                        out.print(getSessionContext().getState().getAbbreviation());
                        out.print("/");
                        out.print(result.getId());
                        out.print("\">");
                        break;
                    case SearchResult.TERM:
                        out.print("<a href=\"http://");
                        out.print(getHostname());
                        out.print("/cgi-bin/glossary_single/");
                        out.print(getStateOrDefault().getAbbreviationLowerCase());
                        out.print("/?id=");
                        out.print(result.getId());
                        out.print("\">");
                        break;
                    default:
                }

                out.print(result.getHeadline());
                out.println("</a>");
                out.println("</td></tr>");

                out.println("<tr class=\"contextrow\">");
                if (result.getType() == SearchResult.SCHOOL) {
                    out.println("<td>");
                    School school = getSchool(result);
                    if (school != null) {
                        out.println(school.getPhysicalAddress().toString());
                        out.print("&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;");
                        out.println(school.getType().getSchoolTypeName());
                        String gl = school.getGradeLevels().getRangeString();
                        if (StringUtils.isNotEmpty(gl)) {
                            out.print("&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;");
                            out.println(gl);
                        }
                    }
                    out.println("</td>");
                } else {
                    String context = result.getContext();
                    if (context != null) {
                        out.println("<td colspan=\"3\">");
                        out.print("<span class=\"context\">");
                        out.print(context);
                        out.println("</span>");
                        out.println("</td>");
                    }
                }

                out.println("</tr>");
                if (_debug) {
                    out.print("<tr><td><pre class=\"explanation\">");
                    out.print(result.getExplanation());
                    out.println("</pre></td></tr>");
                }
            }
            out.println("</table>");
        } else {
            getJspBody().getJspContext().setAttribute("noresults", "true");
        }

        out.println("</td></tr><tr><td class=\"results_pagenav\">");
        writePageNumbers(out);
        out.println("</td></tr></table>");

        try {
            getJspBody().invoke(out);
        } catch (JspException e) {
            throw new IOException(e.getMessage());
        }
    }
}
