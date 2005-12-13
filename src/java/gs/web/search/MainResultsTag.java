package gs.web.search;

import org.apache.taglibs.standard.functions.Functions;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.List;

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
        out.println ("<tr>");
        out.println("<td colspan=\"2\">");
        if (_results != null && _total > 0) {
            out.println("<table id=\"mainresults\">");
            for (int i = 0; i < _results.size(); i++) {
                SearchResult result = (SearchResult) _results.get(i);
                out.println("<tr class=\"result_row\">");
                out.print("<td name=\"headline\">");
                StringBuffer linkBuffer = new StringBuffer();
                switch(result.getType()) {
                    case SearchResult.DISTRICT:
                        out.print("Schools in the district of ");
                        linkBuffer.append("<a href=\"/search/search.page?c=school&state=");
                        linkBuffer.append(getSessionContext().getState().getAbbreviation());
                        linkBuffer.append("&district=");
                        linkBuffer.append(result.getId());
                        out.print(linkBuffer.toString());
                        out.print("\">");
                        break;
                    case SearchResult.CITY:
                        out.print("Schools in the city of ");
                        linkBuffer.append("<a href=\"/search/search.page?c=school&state=");
                        linkBuffer.append(result.getState());
                        linkBuffer.append("&amp;q=");
                        linkBuffer.append(Functions.escapeXml(_queryString));
                        linkBuffer.append("&amp;city=");
                        linkBuffer.append(result.getCity());

                        out.print(linkBuffer.toString());
                        out.print("\">");
                        break;
                    case SearchResult.SCHOOL:
                        out.print("<a href=\"http://");
                        out.print(getSessionContext().getHostName());
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
                        if (result.isInsider()){
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
                String context = result.getContext();
                if (context != null) {
                    out.print("<span class=\"context\">");
                    out.print(context);
                    out.println("</span>");
                }
                out.println("</td></tr>");
                if (_debug) {
                    out.print("<tr><td><pre class=\"explanation\">");
                    out.print(result.getExplanation());
                    out.println("</pre></td></tr>");
                }
            }
            out.println("</table>");
        } else {
            out.println("<span class=\"noresults\">No Results Found</span>");
        }
        out.println ("</td></tr><tr><td class=\"results_pagenav\">");
        writePageNumbers(out);
        out.println("</td></tr></table>");

    }
}
