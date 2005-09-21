package gs.web.search;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class TermTableTagHandler extends ResultsTableTagHandler {

    /*
    private List _terms;

    public void setTerms(List terms) {
        _terms = terms;
    }
    */

    public String getConstraint() {
        return "term";
    }

    public void doTag() throws IOException {

        List _terms = getResults();

        if (_terms != null) {
            JspWriter out = getJspContext().getOut();

            out.println("<table class=\"columns\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
            out.println("<tr><td valign=\"top\">");
            out.println("<table class=\"article_results_only\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
            if (_terms.size() > 0) {
                out.println("<tr>");
                out.println("<th class=\"result_title\" width=\"1\">&nbsp;</th>");
                out.println("<th class=\"left\">Glossary Term</th>");

                out.println("<th class=\"result_title\" width=\"1\">&nbsp;</th>");
                out.println("</tr>");

                for (int i = 0; i < _terms.size(); i++) {
                    SearchResult term = (SearchResult) _terms.get(i);
                    out.println("<tr class=\"result_row\">");
                    out.println("<td class=\"checkbox\" width=\"1\">&nbsp;</td>");
                    out.println("<td>");

                    //out.print("<a href=\"http://www.greatschools.net/cgi-bin/glossary_single/ca//?id=");
                    //out.print(term.getId());
                    //out.print("\">");

                    out.print("<b>");
                    out.print(term.getTerm());
                    out.print("</b>");
                    //out.println("</a>");
                    out.println("<br/>");
                    out.print(term.getDefinition());
                    out.print("</td>");
                    out.println("</tr>");
                }
                out.println("<tr class=\"last_row\"><td colspan=\"5\"><ul>");
                out.println("<li class=\"viewall\"><a href=\"#\">Browse all glossary terms</a></li></ul></td></tr>");
                out.println("</table>");
                out.println("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
                out.println("<td>&nbsp;</td>");
                out.println("<td class=\"results_pagenav\">");
                writePageNumbers(out);
            } else {
                out.println("<tr><th class=\"left result_title\">No results found</div></th></tr>");
                out.println("<tr><td valign=\"top\" height=\"100\">");
            }
            out.println("</td></tr></table>");
            out.println("</td></tr></table>");

        }
    }
}
