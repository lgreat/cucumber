package gs.web.search;

import gs.data.state.State;
import gs.data.search.highlight.TextHighlighter;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.List;

import org.apache.taglibs.standard.functions.Functions;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CityDistTableTagHandler extends ResultsTableTagHandler {

    private String _type;

    public String getConstraint() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }

    public void doTag() throws IOException {

        List results = getResults();
        if (results != null) {

            JspWriter out = getJspContext().getOut();

            out.println("<table class=\"columns\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
            out.println("<tr><td valign=\"top\">");
            out.println("<table class=\"school_results_only\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
            if (results.size() > 0) {
                out.print("<tr><th class=\"result_title\" width=\"1\">&nbsp;</th><th class=\"left\">");

                if (_type != null && _type.equals("city")) {
                    out.print("Browse Schools in City");
                } else {
                    out.print("Browse Schools in District");
                }
                out.println("</th>");
                out.println("<th class=\"result_title\">Number of Schools</th>");
                out.println("</tr>");

                for (int i = 0; i < results.size(); i++) {
                    SearchResult result = (SearchResult) results.get(i);

                    out.println("<tr class=\"result_row\"><td class=\"checkbox\" width=\"1\">&nbsp;</td>");
                    out.println("<td>");

                    if (_type != null && _type.equals("city")) {

                        out.print("<a href=\"/search/search.page?q=");
                        out.print(Functions.escapeXml(getQueryString()));
                        out.print("&c=school&amp;city=");
                        out.print(result.getCity());
                        out.print("&state=");
                        out.print(result.getState());
                        out.print("\">");

                        out.print(TextHighlighter.highlight(result.getCityAndState(), getQueryString(), "address"));
                        out.print("</a>");
                    } else {


                        out.print("<a href=\"/search/search.page?c=school&q=district:");
                        out.print(result.getId());       ///
                        out.print("&district=");     /// Todo: this is crap ugly
                        out.print(result.getId());
                        out.print("&state=");
                        out.print(result.getState());
                        out.print("\">");

                        out.print(TextHighlighter.highlight(result.getName(), getQueryString(), "name"));
                        out.print("</a>");
                        String ss = result.getCityAndState();
                        if (ss != null) {
                            out.print("<address>");
                            out.print(TextHighlighter.highlight(ss, getQueryString(), "address"));
                            out.print("</address>");
                        }
                    }

                    out.print("</td>");
                    out.print("<td align=\"center\">");
                    out.print(result.getSchools());
                    out.println("</td></tr>");
                }

                out.println("<tr class=\"last_row\"><td colspan=\"5\">");

                State s = getState();
                if (s != null) {
                    out.println("<ul><li class=\"viewall\">");
                    out.print("<a href=\"http://");
                    out.print(getHostname());
                    if (_type != null && _type.equals("city")) {
                        out.print("/modperl/citylist/");
                    } else {
                        out.print("/modperl/distlist/");
                    }
                    out.print(s.getAbbreviation());
                    out.print("\">Browse all ");
                    out.print(s.getLongName());

                    if (_type != null && _type.equals("city")) {
                        out.print(" cities");
                    } else {
                        out.print(" districts");
                    }
                    out.println("</a></li></ul>");
                }

                out.println("</td></tr>");

                out.println("</table><table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
                out.println("<td>&nbsp;</td>");
                out.println("<td class=\"results_pagenav\">");

                writePageNumbers(out);
            } else {
                String type = "districts";
                String path = "/distlist/";
                if (_type != null && _type.equals("city")) {
                    type = "cities";
                    path = "/citylist/";
                }

                out.print("<tr><th class=\"left result_title\">No ");
                out.print(type);
                out.println(" found</div></th></tr>");

                if (getState() != null) { // don't do this in "all" state
                    out.println("<tr class=\"last_row\"><td colspan=\"5\"><ul>");
                    out.print("<li class=\"viewall\"><a href=\"http://");
                    out.print(getHostname());
                    out.print("/modperl");
                    out.print(path);
                    out.print(getState().getAbbreviation());
                    out.print ("\">Browse all ");
                    out.print(getStateOrDefault().getLongName());
                    out.print(" ");
                    out.print(type);
                    out.println("</a></li>");
                    out.println ("</ul></td></tr>");
                }
                out.println("<tr><td valign=\"top\" height=\"100\">");
            }
            out.println("</td></tr></table>");
            out.println("</td></tr></table>");
        }
    }
}
