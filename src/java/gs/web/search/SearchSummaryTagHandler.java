package gs.web.search;

import gs.data.state.State;
import gs.web.jsp.BaseTagHandler;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.functions.Functions;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SearchSummaryTagHandler extends BaseTagHandler {

    public static final String BEAN_ID = "searchSummaryTagHandler";
    private String _query;
    private int _schoolsTotal = 0;
    private int _articlesTotal = 0;
    private int _citiesTotal = 0;
    private int _districtsTotal = 0;
    private int _termsTotal = 0;
    private String _constraint = null;
    private static final Logger _log = Logger.getLogger(SearchSummaryTagHandler.class);

    // String constants:
    private static String VIEW_ALL = "All Results: ";
    private static String CITIES = "Cities: ";
    private static String SCHOOLS = "Schools: ";
    private static String ARTICLES = "Articles: ";
    private static String TERMS = "Glossary Terms: ";
    private static String DISTRICTS = "Districts: ";

    private static String aStart = "<a href=\"/search/search.page?q=";
    private static String frag2 = "</td></tr><tr><td class=\"col2\">";
    private static String frag3;

    static {
        StringBuffer buffer = new StringBuffer();
        buffer.delete(0, buffer.length());
        buffer.append("</td></tr></table>");
        frag3 = buffer.toString();
    }

    public void setQuery(String q) {
        _query = Functions.escapeXml(q);
    }

    /**
     * This is called setConstrain (with no ending t) instead of setConstraint
     * because constraint appears to be a reserved work in the jsp world.
     */
    public void setConstrain(String c) {
        _constraint = c;
    }

    public void setSchoolsTotal(int count) {
        System.out.println("schools: " + count);
        _schoolsTotal = count;
    }

    public void setArticlesTotal(int articlesTotal) {
        System.out.println("articles: " + articlesTotal);
        _articlesTotal = articlesTotal;
    }

    public void setCitiesTotal(int citiesTotal) {
        _citiesTotal = citiesTotal;
    }

    public void setDistrictsTotal(int districtsTotal) {
        _districtsTotal = districtsTotal;
    }

    public void setTermsTotal(int termsTotal) {
        _termsTotal = termsTotal;
    }

    public void doTag() throws IOException {

        JspWriter out = getJspContext().getOut();

        //out.println("<link rel=\"stylesheet\" href=\"/res/css/searchsummary.css\" type=\"text/css\" media=\"screen\"/>");

        int total = _schoolsTotal + _articlesTotal + _districtsTotal +
                _citiesTotal + _termsTotal;

        if (total > 0) {
            out.println("<table><tr><td class=\"resultheadline\">");
            out.print("Your search for &nbsp;&quot;<span class=\"searchfor\">");
            out.print(_query);
            out.print("</span>&quot;&nbsp; found ");
            out.print(total);
            out.print(" result");
            if (total > 1) {
                out.print("s");
            }
            out.println(".");

            out.println("</td><td class=\"searchbyaddress\">");
            writeSearchNearLink(out);
            out.println("</td></tr></table>");
            if (_constraint == null || _constraint.equals("") ||
                    _constraint.equals("all")) {
                out.println("<table><tr><td class=\"resultheadline\">");
                out.print("Results found in ");

                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=school\">");
                out.print("Schools</a> (");
                out.print(_schoolsTotal);
                out.print("), ");

                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=city\">");
                out.print("Cities</a> (");
                out.print(_citiesTotal);
                out.print("), ");

                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=district\">");
                out.print("Districts</a> (");
                out.print(_districtsTotal);
                out.print("), ");

                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=article\">Articles</a> (");
                out.print(_articlesTotal);
                out.print("), ");

                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=term\">");
                out.print("Glossary Terms</a> (");
                out.print(_termsTotal);
                out.println(")");
                out.println("</td></tr></table>");
            }
            out.println("<table><tr><td>Browse:</td><td>");
            out.print("<a class=\"rpad\" href=\"/content/allArticles.page?state=");
            out.print(getStateParam());
            out.println("\">All Articles</a></td>");

            String host = getSessionContext().getHostName();
            out.print("<td><a class=\"rpad\"  href=\"http://");
            out.print(host);
            out.print("/modperl/citylist/");
            out.print(getStateParam());
            out.print("\">All ");
            out.print(getStateOrDefault().getLongName());
            out.println(" Cities</a></td>");

            out.print("<td><a class=\"rpad\" href=\"http://");
            out.print(host);
            out.print("/modperl/distlist/");
            out.print(getStateParam());
            out.print("\">All ");
            out.print(getStateOrDefault().getLongName());
            out.println(" Districts</a></td>");

            out.println("</tr></table>");
        } else {
            out.println ("<table><tr><td>");
            out.print("<span class=\"resultheadline\">Your search for <b>\"");
            out.print(_query);
            out.println("\"</b> did not return any results.</span>");
            out.println ("</td><td class=\"searchbyaddress\">");
            writeSearchNearLink(out);
            out.println ("</td></tr><tr><td>");
            out.println("Please try again.");
            out.println ("</td></tr></table>");
        }
        _schoolsTotal = _articlesTotal = _districtsTotal = _citiesTotal = _termsTotal = 0;

    }

    private void writeSearchNearLink(JspWriter out) throws IOException {
        out.print("<a href=\"http://");
        out.print(getSessionContext().getHostName());
        out.print("/cgi-bin/template_plain/advanced/");
        out.print(getStateOrDefault());
        out.print("\">");
        out.println("Search near address</a>");
        /*
        out.println("<form action=\"/search/search.page\">");
        out.print("<input type=\"image\" name=\"searchnear\" value=\"submit\" ");
        out.println("src=\"/res/img/btn_searchbyaddress.gif\" alt=\"Search By Address\" >");
        out.print("<input type=\"hidden\" name=\"state\" value=\"");
        out.print(getStateOrDefault().getAbbreviationLowerCase());
        out.println("\">");
        out.println("</form>");
        */
    }

    private String getStateParam() {
        String param = "all";
        State s = getState();
        if (s != null) {
            param = s.getAbbreviationLowerCase();
        }
        return param;
    }
}
