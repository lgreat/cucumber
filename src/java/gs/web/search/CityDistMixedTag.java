package gs.web.search;

import gs.data.search.highlight.TextHighlighter;
import gs.data.state.State;
import org.apache.taglibs.standard.functions.Functions;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CityDistMixedTag extends BaseQueryTagHandler {

    public static final String BEAN_ID = "cityDistMixedTag";
    private String _query;
    private Map _results;
    private static final int CITIES_MAX = 3;
    private static final int DISTRICTS_MAX = 3;

    public void setQuery(String q) {
        _query = Functions.escapeXml(q);
    }

    public void setResults(Map results) {
        _results = results;
    }

    public void doTag() throws IOException {
        JspWriter out = getJspContext().getOut();
        out.println("<table class=\"columns\"><tr>");
        out.println("<td class=\"col1\">");
        writeCities(out);
        out.println("</td><td class=\"col3\">");
        writeDistricts(out);
        out.println("</td>");
        out.println("</tr></table>");
    }

    private void writeCities(JspWriter out) throws IOException {
        out.println("<div class=\"result_title\">Browse Schools in City</div>");

        List cities = (List) _results.get("cities");

        out.println("<ul style=\"padding-bottom:0px\">");
        if (cities != null && cities.size() > 0) {

            for (int i = 0; i < cities.size(); i++) {
                SearchResult sr = (SearchResult) cities.get(i);
                out.print("<li>");
                out.print("<a href=\"/search/search.page?q=");
                out.print(Functions.escapeXml(_query));
                out.print("&c=school&amp;city=");
                out.print(sr.getCity());
                out.print("&state=");
                out.print(sr.getState());
                out.print("\">");
                out.print(TextHighlighter.highlight(sr.getCityAndState(), _query, "city"));
                out.print(" (");
                out.print(sr.getSchools());
                out.print(" schools)</a>");
                out.println("</li>");
            }
            int citiesCount = ((Integer) _results.get("citiesTotal")).intValue();
            if (citiesCount > CITIES_MAX) {
                out.print("<li class=\"viewall\" style=\"padding-bottom:0px\"><a href=\"/search/search.page?q=");
                out.print(getDecoratedQuery());
                out.print("&c=city\">View all ");
                out.print(citiesCount);
                out.print(" results</a></li>");
            }
        }
        State s = getState();
        if (s != null) {

            out.print("<li class=\"browseall\"><a href=\"http://");
            out.print(getHostname());
            out.print("/modperl/citylist/");
            out.print(s.getAbbreviation());
            out.print ("\">Browse all ");
            out.print(s.getLongName());
            out.print(" cities");
            out.println("</a></li>");

        }
        out.println("</ul>");
    }

    private void writeDistricts(JspWriter out) throws IOException {
        out.println("<div class=\"result_title\">Browse Schools in District</div>");

        List districts = (List) _results.get("districts");
        out.println("<ul>");
        if (districts != null && districts.size() > 0) {
            int count = districts.size();
            if (count > DISTRICTS_MAX) {
                count = DISTRICTS_MAX;
            }

            for (int i = 0; i < count; i++) {
                SearchResult sr = (SearchResult) districts.get(i);
                String s = sr.getState();
                out.print("<li>");
                out.print("<a href=\"/search/search.page?c=school&q=district:");
                out.print(sr.getId());
                out.print("&state=");
                out.print(s);
                out.print("\">");
                out.print(TextHighlighter.highlight(sr.getName(), _query, "name"));
                out.print(" (");
                out.print(sr.getSchools());
                out.print(" schools)</a>");

                String ss = sr.getCityAndState();
                if (ss != null) {
                    out.print("<address style=\"display:inline;padding-left:8px\">");
                    out.print(TextHighlighter.highlight(ss, _query, "address"));
                    out.println("</address>");
                }
                out.println("</li>");
            }
            int districtsCount = ((Integer) _results.get("districtsTotal")).intValue();
            if (districtsCount > DISTRICTS_MAX) {
                out.print("<li class=\"viewall\" style=\"padding-bottom:0px\"><a href=\"/search/search.page?q=");
                out.print(getDecoratedQuery());
                out.print("&c=district\">View all ");
                out.print(districtsCount);
                out.println(" results</a></li>");
            }
        }

        State s = getState();
        if (s != null) {
            out.print("<li class=\"browseall\"><a href=\"http://");
            out.print(getHostname());
            out.print("/modperl/distlist/");
            out.print(s.getAbbreviation());
            out.print ("\">Browse all ");
            out.print(s.getLongName());
            out.print(" districts");
            out.println("</a></li>");
        }
        out.println("</ul>");
    }

}
