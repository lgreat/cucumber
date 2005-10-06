package gs.web.search;

import gs.data.school.School;
import gs.data.search.highlight.TextHighlighter;
import gs.data.state.State;
import gs.web.jsp.BaseTagHandler;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class MixedResultsTagHandler extends BaseTagHandler {

    /*
     * These are the max number of results displayed in the results lists.
     */
    private static final int SCHOOLS_MAX = 6;
    private static final int ARTICLES_MAX = 3;
    private static final int TERMS_MAX = 3;
    private static final int CITIES_MAX = 3;
    private static final int DISTRICTS_MAX = 3;

    public static final String BEAN_ID = "MixedResultsTagHandler";
    private Map _results = null;
    private String _query = "";

    static String startHtml, endHtml;

    static {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<table class=\"columns\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
        buffer.append("<tr>");
        startHtml = buffer.toString();
        endHtml = "</tr></table>";
    }

    public void setQuery(String q) {
        _query = q;
    }

    private String getDecoratedQuery() {
        String decoQuery = _query;
        State s = getState();
        if (s != null) {
            StringBuffer buff = new StringBuffer(_query);
            buff.append("&state=");
            buff.append(s.getAbbreviationLowerCase());
            decoQuery = buff.toString();
        }
        return decoQuery;
    }

    public void setResults(Map results) {
        _results = results;
    }

    public void doTag() throws IOException {

        JspWriter out = getJspContext().getOut();
        out.println(startHtml);
        if (hasResults(_results)) {
            out.println("<td class=\"col1\">");
            writeSchools(out);
            out.println("</td>");

            out.println("<td class=\"col2\">");
            writeCities(out);
            writeDistricts(out);
            out.println("</td>");

            out.println("<td class=\"col3\">");
            writeArticles(out);
            writeGlossary(out);
            out.println("</td>");
        } else {
            out.println("<td><table border=\"0\" class=\"school_results_only\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
            out.println("<tr><th class=\"left result_title\">No results found</div></th></tr>");
            out.println("<tr><td valign=\"top\" height=\"100\">");
            out.println("</td></table>");
        }
        out.println(endHtml);
    }

    private void writeSchools(JspWriter out) throws IOException {
        out.println("<div class=\"result_title\">Schools</div>");

        List schools = (List) _results.get("schools");
        if (schools != null) {
            int count = schools.size();
            if (count > SCHOOLS_MAX) {
                count = SCHOOLS_MAX;
            }
            out.println("<ul>");
            for (int i = 0; i < count; i++) {
                out.print("<li>");
                SearchResult school = (SearchResult) schools.get(i);
                School school_ = getSchool(school);

                out.print("<a href=\"http://");
                String host = getHostname();
                if (host != null) {
                    out.print(host);
                }
                out.print("/modperl/browse_school/");
                out.print(school_.getState().getAbbreviationLowerCase());
                out.print("/");
                out.print(school_.getId().toString());
                out.print("\">");
                out.print(TextHighlighter.highlight(school_.getName(), _query, "name"));
                out.println("</a><address>");
                out.println(TextHighlighter.highlight(school_.getPhysicalAddress().toString(), _query, "address"));
                out.println("</address></li>");
            }

            int schoolCount = ((Integer) _results.get("schoolsTotal")).intValue();
            if (schoolCount > SCHOOLS_MAX) {

                out.print("<li class=\"viewall\"><a href=\"/search.page?q=");
                out.print(getDecoratedQuery());
                out.print("&c=school\">View all ");
                out.print(schoolCount);
                out.println(" results</a></li>");
            }
            out.println("</ul>");
        }
    }

    private void writeCities(JspWriter out) throws IOException {
        out.println("<div class=\"result_title\">Cities (# of schools)</div>");

        List cities = (List) _results.get("cities");
        out.println("<ul>");
        if (cities != null && cities.size() > 0) {

            for (int i = 0; i < cities.size(); i++) {
                SearchResult sr = (SearchResult) cities.get(i);
                out.print("<li><a href=\"/search.page?c=school&q=");
                out.print(sr.getCity());
                out.print("\">");
                out.print(TextHighlighter.highlight(sr.getCity(), _query, "city"));
                out.print(" (");
                out.print(sr.getSchools());
                out.print(")</a>");
                out.println("</li>");
            }
            int citiesCount = ((Integer) _results.get("citiesTotal")).intValue();
            if (citiesCount > CITIES_MAX) {
                out.print("<li class=\"viewall\"><a href=\"/search.page?q=");
                out.print(getDecoratedQuery());
                out.print("&c=city\">View all ");
                out.print(citiesCount);
                out.print(" results</a></li>");
            }

        } else {
            State s = getState();
            if (s != null) {
                out.print("<li class=\"browseall\"><a href=\"http://");
                out.print(getHostname());
                out.print("/modperl/citylist/");
                out.print(s.getAbbreviation());
                out.println ("\">Browse all cities in ");
                out.print(s.getLongName());
                out.println("</a></li>");
            }
        }
        out.println("</ul>");
    }

    private void writeDistricts(JspWriter out) throws IOException {
        out.println("<div class=\"result_title\">Districts (# of schools)</div>");

        List districts = (List) _results.get("districts");
        if (districts != null) {
            int count = districts.size();
            if (count > DISTRICTS_MAX) {
                count = DISTRICTS_MAX;
            }

            out.println("<ul>");
            for (int i = 0; i < count; i++) {
                SearchResult sr = (SearchResult) districts.get(i);
                out.print("<li>");
                out.print("<a href=\"/modperl/browse_district/");
                out.print(sr.getId());
                out.print("/");
                String s = sr.getState();
                out.print(s);
                out.print("\">");
                out.print(TextHighlighter.highlight(sr.getName(), _query, "name"));
                out.print(", ");

                if (s != null) {
                    out.print(s.toUpperCase());
                }
                
                out.print(" (");
                out.print(sr.getSchools());
                out.print(")</a>");

                String ss = sr.getCityAndState();
                if (ss != null) {
                    out.print("<address>");
                    out.print(TextHighlighter.highlight(ss, _query, "address"));
                    out.println("</address>");
                }
                out.println("</li>");
            }
            int districtsCount = ((Integer) _results.get("districtsTotal")).intValue();
            if (districtsCount > DISTRICTS_MAX) {
                out.print("<li class=\"viewall\"><a href=\"/search.page?q=");
                out.print(getDecoratedQuery());
                out.print("&c=district\">View all ");
                out.print(districtsCount);
                out.println(" results</a></li>");
            }
            out.println("</ul>");
        }
    }

    private void writeArticles(JspWriter out) throws IOException {
        out.println("<div class=\"result_title\">Articles</div>");

        List articles = (List) _results.get("articles");
        if (articles != null) {
            out.println("<ul>");
            for (int i = 0; i < articles.size(); i++) {
                SearchResult sr = (SearchResult) articles.get(i);
                out.println("<li>");
                out.print("<a href=\"http://");
                out.print(getHostname());
                out.print("/cgi-bin/showarticle/");
                out.print(getStateOrDefault().getAbbreviationLowerCase());
                out.print("/");
                out.print(sr.getId());
                out.print("\">");
                out.println(escapleLongstate(sr.getTitle()));
                out.println("</a><br/>");
                out.println(sr.getAbstract());
                out.println("</li>");
            }
            int articlesCount = ((Integer) _results.get("articlesTotal")).intValue();
            if (articlesCount > ARTICLES_MAX) {
                out.print("<li class=\"viewall\"><a href=\"/search.page?q=");
                out.print(getDecoratedQuery());
                out.print("&c=article\">View all ");
                out.print(articlesCount);
                out.println(" results</a></li>");
            }
            out.println("</ul>");
        }
    }

    private void writeGlossary(JspWriter out) throws IOException {
        out.println("<div class=\"result_title\">Glossary Terms</div>");

        List terms = (List) _results.get("terms");
        out.println("<ul>");
        if (terms != null && terms.size() > 0) {
            for (int i = 0; i < terms.size(); i++) {
                SearchResult sr = (SearchResult) terms.get(i);
                out.println("<li>");
                out.print("<a href=\"http://");
                out.print(getHostname());
                out.print("/cgi-bin/glossary_single/");
                out.print(getStateOrDefault().getAbbreviationLowerCase());
                out.print("/?id=");
                out.print(sr.getId());
                out.print("\">");
                out.println(sr.getTerm());
                out.println("</a>");
                out.println("</li>");
            }
            int termsCount = ((Integer) _results.get("termsTotal")).intValue();
            if (termsCount > TERMS_MAX) {
                out.print("<li class=\"viewall\"><a href=\"/search.page?q=");
                out.print(getDecoratedQuery());
                out.print("&c=term\">View all ");
                out.println(termsCount);
                out.println(" results</a></li>");
            }

        } else {
            out.print("<li class=\"viewall\"><a href=\"/cgi-bin/glossary_home/");
            out.print(getStateOrDefault().getAbbreviation());
            out.println ("\">Browse all glossary terms</a></li>");
        }
        out.println("</ul>");
    }

    /**
     * Checks to see if there are any results by interating through all of the
     * <code>List</code>s in the <code>Map</code>.
     *
     * @param map
     * @return true if there are results
     */
    private static boolean hasResults
            (Map
                    map) {
        if (map != null) {
            Collection lists = map.values();
            Iterator iter = lists.iterator();
            while (iter.hasNext()) {
                Object o = iter.next();
                if (o instanceof List) {
                    List l = (List) o;
                    if (l != null) {
                        if (l.size() > 0) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}