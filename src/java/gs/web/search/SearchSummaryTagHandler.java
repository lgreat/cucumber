package gs.web.search;

import gs.data.search.Searcher;
import gs.data.state.State;
import gs.web.SessionContext;
import gs.web.jsp.BaseTagHandler;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.apache.log4j.Logger;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SearchSummaryTagHandler extends BaseTagHandler {

    public static final String BEAN_ID = "searchSummaryTagHandler";
    private Searcher _searcher;
    private String _query;
    private int _schoolsTotal = 0;
    private String _constraint = null;
    private GroupingHitCollector _groupingHitCollector;
    private static final Logger _log = Logger.getLogger(SearchSummaryTagHandler.class);

    // String constants:
    private static String VIEW_ALL = "View All Results";
    private static String CITIES = "Cities: ";
    private static String SCHOOLS = "Schools: ";
    private static String ARTICLES = "Articles: ";
    private static String TERMS = "Glossary Terms: ";
    private static String DISTRICTS = "Districts: ";


    private static String aStart = "<a href=\"/search.page?q=";
    private static String frag1;
    private static String frag2 = "</td></tr><tr><td class=\"col2\">";
    private static String frag3;

    static {
        StringBuffer buffer = new StringBuffer();

        buffer.append("<table class=\"columns\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr>");
        buffer.append("<td class=\"col1\" rowspan=\"2\">");
        buffer.append("<img src=\"res/img/search/icon_resultoverview.gif\" /></td>");
        buffer.append("<td class=\"title\" colspan=\"2\">Results: ");
        frag1 = buffer.toString();

        buffer.delete(0, buffer.length());
        buffer.append("</td></tr></table>");
        frag3 = buffer.toString();
    }

    public void setQuery(String q) {
        _query = q;
        _groupingHitCollector = new GroupingHitCollector();
    }

    /**
     * This is called setConstrain (with no ending t) instead of setConstraint
     * because constraint appears to be a reserved work in the jsp world.
     *
     * @param c
     */
    public void setConstrain(String c) {
        _constraint = c;
    }

    public void setSchoolsTotal(int count) {
        _schoolsTotal = count;
    }

    private Searcher getSearcher() {
        if (_searcher == null) {
            try {
                JspContext jspContext = getJspContext();
                if (jspContext != null) {
                    SessionContext sc = (SessionContext) jspContext.getAttribute(SessionContext.SESSION_ATTRIBUTE_NAME, PageContext.SESSION_SCOPE);
                    if (sc != null) {
                        ApplicationContext ac = sc.getApplicationContext();
                        _searcher = (Searcher) ac.getBean(Searcher.BEAN_ID);
                    }
                }
            } catch (Exception e) {
                _log.warn("problem getting ISchoolDao: ", e);
            }
        }
        return _searcher;
    }

    public void doTag() throws IOException {

        _groupingHitCollector.reset();

        String queryIncludingState = _query;
        State s = getState();
        if (s != null) {
            if (_query.indexOf("state:") == -1) {
                StringBuffer buffer = new StringBuffer(_query);
                buffer.append(" AND state:");
                buffer.append(s.getAbbreviation());
                queryIncludingState = buffer.toString();
            }
        }

        getSearcher().search(queryIncludingState, null, _groupingHitCollector, null);

        JspWriter out = getJspContext().getOut();

        out.println("<link rel=\"stylesheet\" href=\"res/css/searchsummary.css\" type=\"text/css\" media=\"screen\"/>");

        int total = _schoolsTotal +
                _groupingHitCollector.getArticles() +
                _groupingHitCollector.getCities() +
                _groupingHitCollector.getDistricts();

        if (total > 0) {

            out.println(frag1);
            out.println(total);
            out.println(frag2);


            if (_constraint != null && _constraint.equals("school")) {
                out.print("<a class=\"active\">");
                out.print(SCHOOLS);
                out.print(_schoolsTotal);
                out.println("</a>");
            } else {
                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=school\">");
                out.print(SCHOOLS);
                out.print(_schoolsTotal);
                out.println("</a>");
            }

            if (_constraint != null && _constraint.equals("article")) {
                out.print("<a class=\"active\">");
                out.print(ARTICLES);
                out.print(_groupingHitCollector.getArticles());
                out.println("</a>");
            } else {
                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=article\">");
                out.print(ARTICLES);
                out.print(_groupingHitCollector.getArticles());
                out.println("</a>");
            }

            if (_constraint != null && _constraint.equals("term")) {
                out.print("<a class=\"active\">");
                out.print(TERMS);
                out.print(_groupingHitCollector.getTerms());
                out.println("</a>");
            } else {
                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=term\">");
                out.print(TERMS);
                out.print(_groupingHitCollector.getTerms());
                out.println("</a>");
            }

            out.println("</td><td class=\"col3\">");

            if (_constraint == null || _constraint.equals("") ||
                _constraint.equals("all")) {
                out.print("<a class=\"active\">");
                out.print(VIEW_ALL);
                out.println("</a>");
            } else {
                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=all\">");
                out.print(VIEW_ALL);
                out.println("</a>");
            }

            if (_constraint != null && _constraint.equals("city")) {
                out.print("<a class=\"active\">");
                out.print(CITIES);
                out.print(_groupingHitCollector.getCities());
                out.println("</a>");
            } else {
                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=city\">");
                out.print(CITIES);
                out.print(_groupingHitCollector.getCities());
                out.println("</a>");
            }

            if (_constraint != null && _constraint.equals("district")) {
                out.print("<a class=\"active\">");
                out.print(DISTRICTS);
                out.print(_groupingHitCollector.getDistricts());
                out.println("</a>");
            } else {
                out.print(aStart);
                out.print(_query);
                out.print("&state=");
                out.print(getStateParam());
                out.print("&c=district\">");
                out.print(DISTRICTS);
                out.print(_groupingHitCollector.getDistricts());
                out.println("</a>");
            }

            out.println(frag3);
        } else {
            out.println("<table class=\"columns\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr>");
            out.println("<td class=\"col1\" rowspan=\"2\">");
            out.println("<img src=\"res/img/search/icon_error1.gif\" /></td>");
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

    static class GroupingHitCollector extends org.apache.lucene.search.HitCollector {

        int total = 0;
        int pubSchools = 0;
        int priSchools = 0;
        int chaSchools = 0;
        int elementarySchools = 0;
        int middleSchools = 0;
        int highSchools = 0;
        int districts = 0;
        int articles = 0;
        int cities = 0;
        int terms = 0;

        public void collect(int id, float score) {
            total++;
            if (Searcher.publicSchoolBits.get(id)) {
                pubSchools++;
            } else if (Searcher.privateSchoolBits.get(id)) {
                priSchools++;
            } else if (Searcher.charterSchoolBits.get(id)) {
                chaSchools++;
            }

            if (Searcher.elementarySchoolBits.get(id)) {
                elementarySchools++;
            }

            if (Searcher.middleSchoolBits.get(id)) {
                middleSchools++;
            }

            if (Searcher.highSchoolBits.get(id)) {
                highSchools++;
            }

            if (Searcher.districtBits.get(id)) {
                districts++;
            }

            if (Searcher.articleBits.get(id)) {
                articles++;
            }

            if (Searcher.cityBits.get(id)) {
                cities++;
            }

            if (Searcher.glossaryTermBits.get(id)) {
                terms++;
            }
        }

        public void reset() {
            total = 0;
            pubSchools = 0;
            priSchools = 0;
            chaSchools = 0;
            elementarySchools = 0;
            middleSchools = 0;
            highSchools = 0;
            districts = 0;
            cities = 0;
            articles = 0;
            terms = 0;
        }

        public int getElementarySchools() {
            return elementarySchools;
        }

        public int getMiddleSchools() {
            return middleSchools;
        }

        public int getHighSchools() {
            return highSchools;
        }

        public int getPublicSchools() {
            return pubSchools;
        }

        public int getPrivateSchools() {
            return priSchools;
        }

        public int getCharterSchools() {
            return chaSchools;
        }

        public int getDistricts() {
            return districts;
        }

        public int getArticles() {
            return articles;
        }

        public int getCities() {
            return cities;
        }

        public int getTerms() {
            return terms;
        }
    }
}
