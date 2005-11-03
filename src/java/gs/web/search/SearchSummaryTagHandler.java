package gs.web.search;

import gs.data.state.State;
import gs.data.school.district.District;
import gs.web.jsp.BaseTagHandler;
import org.apache.taglibs.standard.functions.Functions;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspContext;
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
    //private static final Logger _log = Logger.getLogger(SearchSummaryTagHandler.class);

    private static final int ALL       = 0;
    private static final int CITIES    = 1;
    private static final int SCHOOLS   = 2;
    private static final int ARTICLES  = 3;
    private static final int TERMS     = 4;
    private static final int DISTRICTS = 5;

    private static String aStart = "<a href=\"/search/search.page?q=";

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

    private int getConstraint() {
        int constraint = SCHOOLS;
        if (_constraint == null || _constraint.equals("") ||
                _constraint.equals("all")) {
            return ALL;
        } else if ("term".equals(_constraint)) {
            constraint = TERMS;
        } else if ("article".equals(_constraint)) {
            constraint = ARTICLES;
        } else if ("city".equals(_constraint)) {
            constraint = CITIES;
        } else if ("district".equals(_constraint)) {
            constraint = DISTRICTS;
        }
        return constraint;
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

        int total = _schoolsTotal + _articlesTotal + _districtsTotal +
                _citiesTotal + _termsTotal;

        JspContext jspContext = getJspContext();
        String city = null;
        String dist = null;

        if (jspContext != null) {
            city = (String)jspContext.findAttribute("city");
            if (city == null) {
                String distID = (String)jspContext.findAttribute("district");
                if (distID != null) {
                    District district =
                            getDistrictDao().findDistrictById(getState(),
                                    Integer.valueOf(distID));
                    dist = district.getName();
                }
            }
        }

        if (total > 0) {
            out.println("<table><tr><td class=\"resultheadline\">");

            switch(getConstraint()) {
                case SCHOOLS:
                    out.print("<b>Schools</b>");
                    if (city != null) {
                        out.print(" in the city of &quot;<span class=\"searchfor\">");
                        out.print(city);
                        out.print("</span>&quot;&nbsp;(");
                    } else if (dist != null) {
                        out.print(" in the district&nbsp;&quot;<span class=\"searchfor\">");
                        out.print(dist);
                        out.print("</span>&quot;&nbsp;(");
                    } else {
                        out.print(" with &nbsp;&quot;<span class=\"searchfor\">");
                        out.print(_query);
                        out.print("</span>&quot;&nbsp; in name&nbsp;(");
                    }
                    out.print(_schoolsTotal);
                    out.println("&nbsp;Results)");
                    break;
                case ARTICLES:
                    out.print("<b>Articles</b> matching &nbsp;&quot;<span class=\"searchfor\">");
                    out.print(_query);
                    out.print("</span>&quot;&nbsp; (");
                    out.print(_articlesTotal);
                    out.println("&nbsp;Results)");
                    break;
                case TERMS:
                    out.print("<b>Glossary Terms</b> matching &nbsp;&quot;<span class=\"searchfor\">");
                    out.print(_query);
                    out.print("</span>&quot;&nbsp; (");
                    out.print(_termsTotal);
                    out.println("&nbsp;Results)");
                    break;
                case CITIES:
                    out.print("<b>Cities</b> matching &nbsp;&quot;<span class=\"searchfor\">");
                    out.print(_query);
                    out.print("</span>&quot;&nbsp; (");
                    out.print(_citiesTotal);
                    out.println("&nbsp;Results)");
                    break;
                case DISTRICTS:
                    out.print("<b>Districts</b> matching &nbsp;&quot;<span class=\"searchfor\">");
                    out.print(_query);
                    out.print("</span>&quot;&nbsp; (");
                    out.print(_districtsTotal);
                    out.println("&nbsp;Results)");
                    break;
                default: // all
                    out.print("Your search for &nbsp;&quot;<span class=\"searchfor\">");
                    out.print(_query);
                    out.print("</span>&quot;&nbsp; found ");
                    out.print(total);
                    out.print(" result");
                    if (total > 1) {
                        out.print("s");
                    }
                    out.println(".");
                    break;
            }

            out.println("</td><td class=\"searchbyaddress\">");
            writeSearchNearLink(out);
            out.println("</td></tr></table>");
            if (getConstraint() == ALL) {
                out.println("<table><tr><td class=\"resultheadline\">");
                out.print("Results found in ");

                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=school\">Schools</a> (");
                out.print(_schoolsTotal);
                out.print("), ");

                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=city\">Cities</a> (");
                out.print(_citiesTotal);
                out.print("), ");

                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=district\">Districts</a> (");
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
                out.print("&c=term\">Glossary Terms</a> (");
                out.print(_termsTotal);
                out.println(")");
                out.println("</td></tr></table>");
            }
            writeBrowseLinks(out);

        } else {
            out.println ("<table><tr><td>");
            out.print("<span class=\"resultheadline\">Your search for <b>\"");
            if (city != null) {
                out.print(city);
            } else if (dist != null) {
                out.print(dist);
            } else {
                out.print(_query);
            }
            out.println("\"</b> did not return any results.</span>");
            out.println ("</td><td class=\"searchbyaddress\">");
            writeSearchNearLink(out);
            out.println ("</td></tr><tr><td>");
            writeBrowseLinks(out);
            //Please try again.
            out.println("</td></tr></table>");
        }
        _schoolsTotal = _articlesTotal = _districtsTotal = _citiesTotal = _termsTotal = 0;

    }

    private void writeSearchNearLink(JspWriter out) throws IOException {
        out.print("<a href=\"http://");
        out.print(getSessionContext().getHostName());
        out.print("/cgi-bin/template_plain/advanced/");
        out.print(getStateOrDefault());
        out.print("/#address\">");
        out.println("Search near Address</a>");
    }

    private void writeBrowseLinks(JspWriter out) throws IOException {

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
        out.println(" Districts</a></td></tr></table>");
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
