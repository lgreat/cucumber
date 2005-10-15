package gs.web.search;

import gs.data.search.highlight.TextHighlighter;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class TermTableTagHandler extends ResultsTableTagHandler {

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
                    out.print("<a href=\"http://");
                    out.print(getHostname());
                    out.print("/cgi-bin/glossary_single/");
                    out.print(getStateOrDefault().getAbbreviationLowerCase());
                    out.print("/?id=");
                    out.print(term.getId());
                    out.print("\">");
                    out.print(TextHighlighter.highlight(term.getTerm(), getQueryString(), "term"));
                    out.println("</a>");
                    out.println("<br/>");
                    out.print(TextHighlighter.highlight(term.getDefinition(), getQueryString(), "term"));
                    out.print("</td>");
                    out.println("</tr>");
                }
                writeBrowseAllRow(out);
                out.println("</table>");
                out.println("<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
                out.println("<td>&nbsp;</td>");
                out.println("<td class=\"results_pagenav\">");
                writePageNumbers(out);
            } else {
                out.println("<tr><th class=\"left result_title\">No glossary terms found</div></th></tr>");
                writeBrowseAllRow(out);
                out.println("<tr><td valign=\"top\" height=\"100\">");
            }
            out.println("</td></tr></table>");
            out.println("</td></tr></table>");
        }
    }

    private void writeBrowseAllRow(JspWriter out) throws IOException {
        out.println("<tr class=\"last_row\"><td colspan=\"5\"><ul>");
        out.print("<li class=\"viewall\"><a href=\"http://");
        out.print(getHostname());
        out.print("/cgi-bin/glossary_home/");
        out.print(getStateOrDefault().getAbbreviation());
        out.println ("\">Browse all glossary terms</a></li></ul></td></tr>");
    }
}
