package gs.web.search;

import gs.data.school.School;
import gs.web.jsp.BaseTagHandler;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.Map;
import java.util.List;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class MixedResultsTagHandler extends BaseTagHandler {

    private static final int SCHOOLS_MAX   = 3;
    private static final int ARTICLES_MAX  = 3;
    private static final int TERMS_MAX     = 3;
    private static final int CITIES_MAX    = 3;
    private static final int DISTRICTS_MAX = 3;

    public static final String BEAN_ID = "MixedResultsTagHandler";
    private Map _results = null;
    private String _query = "";

    static String startHtml, endHtml;

    static {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<table class=\"columns\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
        buffer.append("<tr>"); //<td class=\"col1\" valign=\"top\">");
        startHtml = buffer.toString();
        endHtml = "</tr></table>";
    }

    public void setQuery(String q) {
        _query = q;
    }

    public void setResults(Map results) {
        _results = results;
    }

    public void doTag() throws IOException {

        JspWriter out = getJspContext().getOut();
        out.println(startHtml);

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
        out.println(endHtml);
    }

    private void writeSchools(JspWriter out) throws IOException {
        out.println("<div class=\"result_title\">Schools</div>");


        if (_results != null) {
            List schools = (List) _results.get("schools");
            if (schools != null) {
                out.println("<ul>");
                for (int i = 0; i < schools.size(); i++) {
                    out.print("<li>");
                    SearchResult school = (SearchResult) schools.get(i);
                    School school_ = getSchool(school);

                    out.print("<a href=\"http://www.greatschools.net/modperl/browse_school/");
                    out.print(school_.getState().getAbbreviationLowerCase());
                    out.print("/");
                    out.print(school_.getId().toString());
                    out.print("\">");
                    out.print(school_.getName());
                    out.println("</a><address>");
                    out.println(school_.getPhysicalAddress().toString());
                    out.println("</address></li>");
                }

                int schoolCount = ((Integer)_results.get("schoolsTotal")).intValue();
                if (schoolCount > SCHOOLS_MAX) {

                    out.print("<li class=\"viewall\"><a href=\"/search.page?q=");
                    out.print(_query);
                    out.print("&c=school\">View all ");
                    out.print(schoolCount);
                    out.println(" results</a><li>");
                }
                out.println("</ul>");
            }
        }

    }

    private void writeCities(JspWriter out) throws IOException {
        out.println("<div class=\"result_title\">Cities (# of schools)</div>");

        if (_results != null) {
            List cities = (List) _results.get("cities");
            if (cities != null) {
                out.println("<ul>");
                for (int i = 0; i < cities.size(); i++) {
                    SearchResult sr = (SearchResult)cities.get(i);
                    out.println("<li><a href=\"#\">");
                    out.println(sr.getCityName());
                    out.print(" (");
                    out.print(sr.getSchools());
                    out.print(")");
                    out.println("</a></li>");
                }
                int citiesCount = ((Integer)_results.get("citiesTotal")).intValue();
                if (citiesCount > CITIES_MAX) {
                    out.print("<li class=\"viewall\"><a href=\"/search.page?q=");
                    out.print(_query);
                    out.print("&c=city\">View all ");
                    out.print(citiesCount);
                    out.print(" results</a><li>");
                }
                out.println("</ul>");
            }
        }
    }

    private void writeDistricts(JspWriter out) throws IOException {
        out.println("<div class=\"result_title\">Districts (# of schools)</div>");

        if (_results != null) {
            List districts = (List) _results.get("districts");
            if (districts != null) {
                out.println("<ul>");
                for (int i = 0; i < districts.size(); i++) {
                    SearchResult sr = (SearchResult)districts.get(i);
                    out.print("<li>");
                    out.print("<a href=\"http://www.greatschools.net/modperl/browse_district/");
                    out.print(sr.getId());
                    out.print("/");
                    out.print(sr.getState());
                    out.print("\">");
                    out.print(sr.getName());
                    out.print(" (");
                    out.print(sr.getSchools());
                    out.print(")");
                    out.println("</a></li>");
                }
                int districtsCount = ((Integer)_results.get("districtsTotal")).intValue();
                if (districtsCount > DISTRICTS_MAX) {
                    out.print("<li class=\"viewall\"><a href=\"/search.page?q=");

                    out.print(_query);
                    out.print ("&c=district\">View all ");
                    out.print(districtsCount);
                    out.println(" results</a><li>");
                }
                out.println("</ul>");
            }
        }
    }

    private void writeArticles(JspWriter out) throws IOException {
        out.println("<div class=\"result_title\">Articles</div>");
        if (_results != null) {
            List articles = (List) _results.get("articles");
            if (articles != null) {
                out.println("<ul>");
                for (int i = 0; i < articles.size(); i++) {
                    SearchResult sr = (SearchResult) articles.get(i);
                    out.println("<li>");
                    out.print("<a href=\"http://www.greatschools.net/cgi-bin/showarticle/");
                    out.print(sr.getState());
                    out.print("/");
                    out.print(sr.getId());
                    out.print("\">");
                    out.println(sr.getTitle());
                    out.println("</a><br/>");
                    out.println(sr.getAbstract());
                    out.println("</li>");
                }
                int articlesCount = ((Integer)_results.get("articlesTotal")).intValue();
                if (articlesCount > ARTICLES_MAX) {
                    out.print("<li class=\"viewall\"><a href=\"/search.page?q=");
                    out.print(_query);
                    out.print("&c=article\">View all ");
                    out.print(articlesCount);
                    out.println(" results</a><li>");
                }
                out.println("</ul>");
            }
        }
    }

    private void writeGlossary(JspWriter out) throws IOException {
        out.println("<div class=\"result_title\">Glossary Terms</div>");

        if (_results != null) {
            List terms = (List) _results.get("terms");
            if (terms != null) {
                out.println("<ul>");
                for (int i = 0; i < terms.size(); i++) {
                    SearchResult sr = (SearchResult) terms.get(i);
                    out.println("<li>");
                    out.print("<a href=\"http://www.greatschools.net/cgi-bin/glossary_single/ca//?id=");
                    out.print(sr.getId());
                    out.print("\">");
                    out.println(sr.getTerm());
                    out.println("</a>");
                    out.println("</li>");
                }
                int termsCount = ((Integer)_results.get("termsTotal")).intValue();
                if (termsCount > TERMS_MAX) {
                    out.print("<li class=\"viewall\"><a href=\"/search.page?q=");
                    out.print(_query);
                    out.print("&c=term\">View all ");
                    out.println(termsCount);
                    out.println(" results</a><li>");
                }
                out.println("</ul>");
            }
        }
    }


}