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
import gs.data.school.SchoolType;

import java.io.IOException;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ResearchAndCompareNewsBlurbTagHandler extends SimpleTagSupport {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String CATEGORY = "RESEARCH AND COMPARE DATA UPDATE";
    private State _state;

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public void doTag() throws IOException {
        INewsItemDao newsItemDao = (INewsItemDao) SpringUtil.getApplicationContext().getBean(INewsItemDao.BEAN_ID);
        NewsItem newsItem = newsItemDao.findNewsItemForState(CATEGORY, _state);

        JspWriter out = getJspContext().getOut();
        if (newsItem != null) {
            String link = newsItem.getLink();
            out.println("<h2>" + newsItem.getTitle() + "</h2>");
            String text = newsItem.getText();
            if (StringUtils.isNotEmpty(text)) {
                String[] paragraphs = text.split("\n");
                int numParagraphs = paragraphs.length;
                for (int paragraphNum = 0; paragraphNum < numParagraphs; paragraphNum++) {
                    String curParagraph = paragraphs[paragraphNum];
                    if (StringUtils.isNotBlank(curParagraph)) {
                        out.print("<p>");
                        if (paragraphNum == (numParagraphs - 1) && StringUtils.isNotEmpty(link)) {
                            out.print("<a href=\"" + link + "\">");
                            out.print(curParagraph);
                            out.print("</a>");
                        } else {
                            out.print(curParagraph);
                        }
                        out.println("</p>");
                    }
                }
            }
        } else {
            // output default news item
            if (_state != null) {
                ISchoolDao schoolDao = (ISchoolDao) SpringUtil.getApplicationContext().getBean(ISchoolDao.BEAN_ID);
                int numSchools = schoolDao.countSchools(_state, null, null, null);
                int charterSchools = schoolDao.countSchools(_state, SchoolType.CHARTER, null, null);
                out.print("<p>");
                out.print("We have profiles for " + numSchools);
                if (charterSchools > 0) {
                    out.print(" public, private and charter ");
                } else {
                    out.print(" public and private ");
                }
                out.print("schools in " + _state.getLongName() + ". ");
                out.print("Search our site to find your school's test scores, ");
                out.print("teacher/student stats and more.");
                out.println("</p>");
                out.print("<p>");
                out.print("Get monthly email updates about your school's data with our free newsletter:");
                out.println("</p>");

                out.print("<p>");
                out.print("<a href=\"/cgi-bin/newsletters/" + _state.getAbbreviation() + "\">" +
                        "Sign up for My School Stats</a>");
                out.println("</p>");
            } else {
                out.print("<p>");
                out.print("We have profiles for over 120,000 public, private and charter " +
                        "schools across the country, " +
                        "including test scores, student-teacher ratios and demographic info.");
                out.println("</p>");
            }
        }
        out.print("<p>Amy Rickerson, ");
        if (_state != null) {
            out.print(_state.getLongName());
        } else {
            out.print("GreatSchools");
        }
        out.print(" Data Specialist");
        out.println("</p>");
    }
}
