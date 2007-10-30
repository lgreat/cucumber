package gs.web.content;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspWriter;

import gs.data.state.State;
import gs.data.content.INewsItemDao;
import gs.data.content.NewsItem;
import gs.data.util.SpringUtil;
import gs.data.school.ISchoolDao;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.text.NumberFormat;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ResearchAndCompareNewsBlurbTagHandler extends SimpleTagSupport {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String CATEGORY = "RESEARCH AND COMPARE DATA UPDATE";
    private State _state;
    private INewsItemDao _newsItemDao; // use getter
    private ISchoolDao _schoolDao; // use getter
    private String _paragraphTag = "div"; // default
    private String _textClass = "updatesText"; // default
    public static final int ROUND_DOWN_TO_NEAREST = 100;
    public static final String ALASTAIR_BROWN = "Alastair Brown";
    public static final String ELIZABETH_GARDNER = "Elizabeth Gardner";
    private static Map _stateOwners = new HashMap() {{
        put(State.AK, ELIZABETH_GARDNER);
        put(State.AL, ALASTAIR_BROWN);
        put(State.AR, ELIZABETH_GARDNER);
        put(State.AZ, ELIZABETH_GARDNER);
        put(State.CA, ALASTAIR_BROWN);
        put(State.CO, ALASTAIR_BROWN);
        put(State.CT, ALASTAIR_BROWN);
        put(State.DC, ALASTAIR_BROWN);
        put(State.DE, ALASTAIR_BROWN);
        put(State.FL, ALASTAIR_BROWN);
        put(State.GA, ALASTAIR_BROWN);
        put(State.HI, ELIZABETH_GARDNER);
        put(State.IA, ALASTAIR_BROWN);
        put(State.ID, ALASTAIR_BROWN);
        put(State.IL, ELIZABETH_GARDNER);
        put(State.IN, ELIZABETH_GARDNER);
        put(State.KS, ALASTAIR_BROWN);
        put(State.KY, ELIZABETH_GARDNER);
        put(State.LA, ELIZABETH_GARDNER);
        put(State.MA, ELIZABETH_GARDNER);
        put(State.MD, ALASTAIR_BROWN);
        put(State.ME, ELIZABETH_GARDNER);
        put(State.MI, ELIZABETH_GARDNER);
        put(State.MN, ELIZABETH_GARDNER);
        put(State.MO, ALASTAIR_BROWN);
        put(State.MS, ALASTAIR_BROWN);
        put(State.MT, ELIZABETH_GARDNER);
        put(State.NC, ALASTAIR_BROWN);
        put(State.ND, ELIZABETH_GARDNER);
        put(State.NE, ALASTAIR_BROWN);
        put(State.NH, ELIZABETH_GARDNER);
        put(State.NJ, ALASTAIR_BROWN);
        put(State.NM, ELIZABETH_GARDNER);
        put(State.NV, ELIZABETH_GARDNER);
        put(State.NY, ELIZABETH_GARDNER);
        put(State.OH, ELIZABETH_GARDNER);
        put(State.OK, ELIZABETH_GARDNER);
        put(State.OR, ALASTAIR_BROWN);
        put(State.PA, ELIZABETH_GARDNER);
        put(State.RI, ELIZABETH_GARDNER);
        put(State.SC, ALASTAIR_BROWN);
        put(State.SD, ALASTAIR_BROWN);
        put(State.TN, ALASTAIR_BROWN);
        put(State.TX, ELIZABETH_GARDNER);
        put(State.UT, ALASTAIR_BROWN);
        put(State.VA, ALASTAIR_BROWN);
        put(State.VT, ELIZABETH_GARDNER);
        put(State.WA, ELIZABETH_GARDNER);
        put(State.WI, ELIZABETH_GARDNER);
        put(State.WV, ALASTAIR_BROWN);
        put(State.WY, ALASTAIR_BROWN);
    }};


    public static void setStateOwners(Map stateOwners) {
        ResearchAndCompareNewsBlurbTagHandler._stateOwners = stateOwners;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public INewsItemDao getNewsItemDao() {
        if (_newsItemDao == null) {
            _newsItemDao = (INewsItemDao) SpringUtil.getApplicationContext().getBean(INewsItemDao.BEAN_ID);
        }
        return _newsItemDao;
    }

    public void setNewsItemDao(INewsItemDao newsItemDao) {
        _newsItemDao = newsItemDao;
    }

    public ISchoolDao getSchoolDao() {
        if (_schoolDao == null) {
            _schoolDao = (ISchoolDao) SpringUtil.getApplicationContext().getBean(ISchoolDao.BEAN_ID);
        }
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public String getParagraphTag() {
        return _paragraphTag;
    }

    public void setParagraphTag(String paragraphTag) {
        _paragraphTag = paragraphTag;
    }

    public String getTextClass() {
        return _textClass;
    }

    public void setTextClass(String textClass) {
        _textClass = textClass;
    }

    protected String openParagraph() {
        return "<" + getParagraphTag() + " class=\"" + getTextClass() + "\">";
    }

    protected String closeParagraph() {
        return "</" + getParagraphTag() + ">";
    }

    protected String openTitle() {
        return "<h2 class=\"updatesTitle\">";
    }

    protected String closeTitle() {
        return "</h2>";
    }

    protected int roundToNearest(int numSchools, int roundToNearest) {
        if (numSchools <= 0) {
            return 0;
        } else if (roundToNearest <= 1) {
            return numSchools;
        }
        return (numSchools - (numSchools % roundToNearest));
    }

    public void doTag() throws IOException {
        NewsItem newsItem = getNewsItemDao().findNewsItemForState(CATEGORY, _state);

        JspWriter out = getJspContext().getOut();
        if (newsItem != null) {
            String link = newsItem.getLink();
            out.print(openTitle());
            out.print(newsItem.getTitle().toUpperCase());
            out.println(closeTitle());
            String text = newsItem.getText();
            if (StringUtils.isNotEmpty(text)) {
                String[] paragraphs = text.split("\n");
                int numParagraphs = paragraphs.length;
                for (int paragraphNum = 0; paragraphNum < numParagraphs; paragraphNum++) {
                    String curParagraph = paragraphs[paragraphNum];
                    if (StringUtils.isNotBlank(curParagraph)) {
                        out.print(openParagraph());
                        if (paragraphNum == (numParagraphs - 1) && StringUtils.isNotEmpty(link)) {
                            out.print("<a href=\"" + link + "\">");
                            out.print(curParagraph);
                            out.print("</a>");
                        } else {
                            out.print(curParagraph);
                        }
                        out.println(closeParagraph());
                    }
                }
            }
        } else {
            // output default news item
            if (_state != null) {
                int numSchools = getSchoolDao().countSchools(_state, null, null, null);
                int roundedNumSchools = roundToNearest(numSchools, ROUND_DOWN_TO_NEAREST);
                boolean hasCharterSchools = _state.isCharterSchoolState();
                out.print(openTitle());
                String title = "About " + _state.getLongName() + " data";
                out.print(title.toUpperCase());
                out.println(closeTitle());
                out.print(openParagraph());
                out.print("We have profiles for more than " +
                        NumberFormat.getInstance().format(roundedNumSchools));
                if (hasCharterSchools) {
                    out.print(" public, private and charter ");
                } else {
                    out.print(" public and private ");
                }
                out.print("schools in " + _state.getLongName() + ". ");
                out.print("Search our site to find your school's test scores, ");
                out.print("teacher/student stats and more.");
                out.println(closeParagraph());

                out.print(openParagraph());

                if (State.DC.equals(_state)) {
                    out.print("<a href=\"http://data.greatschools.net/district_of_columbia/index.html\">" +
                            "Read more about data updates in Washington DC</a>");                    
                } else {
                    out.print("<a href=\"http://data.greatschools.net/" + _state.getLongName().toLowerCase().replaceAll(" ", "_") + "/index.html\">" +
                            "Read more about data updates in "+ _state.getLongName() +"</a>");
                }

                out.println(closeParagraph());
            } else {
                out.print(openTitle());
                out.print("About GreatSchools data".toUpperCase());
                out.println(closeTitle());
                out.print(openParagraph());
                out.print("We have profiles for more than 120,000 public, private and charter " +
                        "schools nationwide. ");
                out.print("Search our site to find your school's test scores, teacher/student " +
                        "stats and more.");
                out.println(closeParagraph());
            }
        }
        printFooter(out);
    }

    protected void printFooter(JspWriter out) throws IOException {
        out.print(openParagraph());
        String owner = (String) _stateOwners.get(_state);
        if (StringUtils.isNotBlank(owner)) {
            out.print("<span class=\"printName\">" + owner + "</span>");
            out.print("<span class=\"italicTitle\">");
            out.print(_state.getLongName());
            out.print(" Data Specialist</span>");
        } else {
            out.print("<span class=\"printName\">Alastair Brown &amp; Elizabeth Gardner</span>");
            out.print("<span class=\"italicTitle\">");
            out.print("Education Data Specialists</span>");
        }
        out.println(closeParagraph());
    }
}
