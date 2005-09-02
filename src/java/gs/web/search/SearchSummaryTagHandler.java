package gs.web.search;

import gs.data.search.Searcher;
import gs.web.SessionContext;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.BitSet;

import org.springframework.context.ApplicationContext;
import org.apache.log4j.Logger;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SearchSummaryTagHandler extends SimpleTagSupport {

    public static final String BEAN_ID = "searchSummaryTagHandler";
    private Searcher _searcher;
    private String _query;
    private GroupingHitCollector _groupingHitCollector;
    private static final Logger _log = Logger.getLogger(SearchSummaryTagHandler.class);

    public void setQuery(String q) {
        _query = q;
        _groupingHitCollector = new GroupingHitCollector();
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
        getSearcher().basicSearch(_query, null, _groupingHitCollector);

        JspWriter out = getJspContext().getOut();

        /*
        out.println("<table id=\"results_overview\" cellpadding=\"0\" cellspacing=\"0\">");
        out.println("<tr><td class=\"TL\"></td><td class=\"T\"></td><td class=\"TR\"></td>");
        out.println("</tr><tr><td class=\"L\"></td><td class=\"C\">");
        */

        out.print("<img style=\"position:absolute;left:10;top:20;\"");
        out.println("src=\"images/icon_resultoverview_60x60.gif\" />");
        out.println("<div class=\"results_overview\">Results: ");
        out.println(_groupingHitCollector.total);
        out.println("</div>");
        out.println("<div style=\"margin-left:70px;\">");
        out.println("<div style=\"float:right;\">");
        out.println("<a href=\"#\" class=\"block results_overview_link\">");

        out.print("Districts: ");
        out.print(_groupingHitCollector.getDistricts());
        out.println("</a>");

        out.println("<a href=\"#\" class=\"block results_overview_link\">");

        out.print("Cities: ");
        out.println(_groupingHitCollector.getCities());
        out.println("</a>");

        out.print("<a href=\"#\" class=\"block results_overview_link\">");

        out.println("Topics: ");
        out.println(_groupingHitCollector.getArticles());
        out.println("</a>");
        out.println("</div>");

        out.println("<a href=\"#\" class=\"block results_overview_link\">");
        out.println("Elementary Schools: ");
        out.println(_groupingHitCollector.getElementarySchools());
        out.println("</a>");
        out.println("<a href=\"#\" class=\"block results_overview_link\">");
        out.println("Middle Schools: ");
        out.println(_groupingHitCollector.getMiddleSchools());
        out.println("</a><a href=\"#\" class=\"block results_overview_link\">");
        out.println("High Schools: ");
        out.println(_groupingHitCollector.getHighSchools());

        out.println("</a>");
        out.println("</div>");

        /*
        out.println("</td><td class=\"R\"></td></tr><tr>");
        out.println("<td class=\"BL\"></td><td class=\"B\"></td>");
        out.println("<td class=\"BR\"></td>");
        out.println("</tr></table>");
          */
    }


    class GroupingHitCollector extends org.apache.lucene.search.HitCollector {

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

        public void collect(int id, float score) {
            total++;
            _log.debug("debug 1................");
            if (getSearcher().getPublicSchoolBits().get(id)) {
                _log.debug("incrementing public school................");
                pubSchools++;
            } else if (getSearcher().getPrivateSchoolBits().get(id)) {
                priSchools++;
            } else if (getSearcher().getCharterSchoolBits().get(id)) {
                chaSchools++;
            }

            if (getSearcher().getElementarySchoolBits().get(id)) {
                elementarySchools++;
            }

            if (getSearcher().getMiddleSchoolBits().get(id)) {
                middleSchools++;
            }

            if (getSearcher().getHighSchoolBits().get(id)) {
                highSchools++;
            }

            if (getSearcher().getDistrictBits().get(id)) {
                districts++;
            }

            if (getSearcher().getArticleBits().get(id)) {
                articles++;
            }

            if (getSearcher().getCityBits().get(id)) {
                cities++;
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
    }
}