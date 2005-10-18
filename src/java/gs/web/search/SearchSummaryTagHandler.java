package gs.web.search;

import gs.data.search.Searcher;
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
    private static String frag1;
    private static String frag2 = "</td></tr><tr><td class=\"col2\">";
    private static String frag3;

    static {
        StringBuffer buffer = new StringBuffer();

        buffer.append("<table class=\"columns\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr>");
        buffer.append("<td class=\"col1\" rowspan=\"2\">");
        buffer.append("<img src=\"/res/img/search/icon_resultoverview.gif\" /></td>");
        buffer.append("<td class=\"title\" colspan=\"2\">Results: ");
        frag1 = buffer.toString();

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
        _schoolsTotal = count;
    }

    public void setArticlesTotal(int articlesTotal) {
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

        out.println("<link rel=\"stylesheet\" href=\"/res/css/searchsummary.css\" type=\"text/css\" media=\"screen\"/>");

        int total = _schoolsTotal + _articlesTotal + _districtsTotal +
                _citiesTotal + _termsTotal;

        if (total > 0) {

            out.println(frag1);
            out.println(total);
            out.println(frag2);

            if (_constraint != null && _constraint.equals("school")) {
                out.print("<a class=\"active\">");
            } else {
                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=school\">");
            }
            out.print(SCHOOLS);
            out.print(_schoolsTotal);
            out.println("</a>");

            if (_constraint != null && _constraint.equals("article")) {
                out.print("<a class=\"active\">");
            } else {
                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=article\">");
            }
            out.print(ARTICLES);
            out.print(_articlesTotal);
            out.println("</a>");

            if (_constraint != null && _constraint.equals("term")) {
                out.print("<a class=\"active\">");
            } else {
                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=term\">");
            }
            out.print(TERMS);
            out.print(_termsTotal);
            out.println("</a>");

            out.println("</td><td class=\"col3\">");

            if (_constraint == null || _constraint.equals("") ||
                    _constraint.equals("all")) {
                out.print("<a class=\"active\">");
            } else {
                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=all\">");
            }
            out.print(VIEW_ALL);
            out.print(total);
            out.println("</a>");

            if (_constraint != null && _constraint.equals("city")) {
                out.print("<a class=\"active\">");
            } else {
                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=city\">");
            }
            out.print(CITIES);
            out.print(_citiesTotal);
            out.println("</a>");

            if (_constraint != null && _constraint.equals("district")) {
                out.print("<a class=\"active\">");
            } else {
                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=district\">");
            }
            out.print(DISTRICTS);
            out.print(_districtsTotal);
            out.println("</a>");

            out.println(frag3);
        } else {
            out.println("<table class=\"columns\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr>");
            out.println("<td class=\"col1\" rowspan=\"2\">");
            out.println("<img src=\"/res/img/search/icon_error1.gif\" /></td>");
            out.print("<td class=\"errormessage\">Your search for <b>\"");
            out.print(_query);
            out.println("\"</b> did not return any results.<br/>Please try again.");
            out.println("</td></tr></table>");
        }
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
