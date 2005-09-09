package gs.web.search;

import gs.data.search.Searcher;
import gs.web.SessionContext;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.apache.log4j.Logger;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SearchSummaryTagHandler extends SimpleTagSupport {

    public static final String BEAN_ID = "searchSummaryTagHandler";
    private Searcher _searcher;
    private String _query;
    private int _schoolsTotal = 0;
    private GroupingHitCollector _groupingHitCollector;
    private static final Logger _log = Logger.getLogger(SearchSummaryTagHandler.class);

    private static String aStart = "<a href=\"/search.page?q=";
    private static String frag1;
    private static String frag2 = "</td></tr><tr><td class=\"col2\">";
    private static String frag3;

    static {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<table id=\"resultoverview\" class=\"bottomalign\" border=0 cellspacing=0 cellpadding=0 width=100%\">");
        buffer.append("<tr><td class=\"TL\"></td>");
        buffer.append("<td class=\"T\"></td>");
        buffer.append("<td class=\"TR\"></td>");
        buffer.append("</tr><tr><td class=\"L\"></td>");
        buffer.append("<td class=\"C\">");
        buffer.append("<table class=\"columns\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr>");
        buffer.append("<td class=\"col1\" rowspan=\"2\">");
        buffer.append("<img src=\"res/img/search/icon_resultoverview.gif\" /></td>");
        buffer.append("<td class=\"title\" colspan=\"2\">Results: ");
        frag1 = buffer.toString();

        buffer.delete(0, buffer.length());
        buffer.append("</td></tr></table></td>");
        buffer.append("<td class=\"R\"></td></tr><tr>");
        buffer.append("<td class=\"BL\"></td><td class=\"B\"></td>");
        buffer.append("<td class=\"BR\"></td></tr></table>");
        frag3 = buffer.toString();
    }

    public void setQuery(String q) {
        _query = q;
        _groupingHitCollector = new GroupingHitCollector();
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
        getSearcher().basicSearch(_query, null, _groupingHitCollector);

        JspWriter out = getJspContext().getOut();


        out.println("<link rel=\"stylesheet\" href=\"res/css/search.css\" type=\"text/css\" media=\"screen\"/>");

        out.println(frag1);
        out.println(_schoolsTotal +
                _groupingHitCollector.getArticles() +
                _groupingHitCollector.getCities() +
                _groupingHitCollector.getDistricts());
        out.println(frag2);

        out.print(aStart);
        out.print(_query);
        out.print("&c=school\">Schools: ");
        out.print(_schoolsTotal);
        out.println("</a>");

        out.print(aStart);
        out.print(_query);
        out.print("&c=article\">Topics: ");
        out.print(_groupingHitCollector.getArticles());
        out.println("</a>");

        out.println("</td><td class=\"col3\">");

        out.print(aStart);
        out.print(_query);
        out.print("&c=city\">Cities: ");
        out.print(_groupingHitCollector.getCities());
        out.println("</a>");

        out.print(aStart);
        out.print(_query);
        out.print("&c=district\">Districts: ");
        out.print(_groupingHitCollector.getDistricts());
        out.println("</a>");

        out.println (frag3);

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
             if (getSearcher().getPublicSchoolBits().get(id)) {
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