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

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class NewsItemByStateTagHandler extends SimpleTagSupport {
    protected final Log _log = LogFactory.getLog(getClass());

    private String _category;
    private State _state;

    public String getCategory() {
        return _category;
    }

    public void setCategory(String category) {
        _category = category;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public void doTag() throws IOException {
        INewsItemDao newsItemDao = (INewsItemDao) SpringUtil.getApplicationContext().getBean(INewsItemDao.BEAN_ID);
        NewsItem newsItem = newsItemDao.findNewsItemForState(_category, _state);

        JspWriter out = getJspContext().getOut();
        if (newsItem != null) {
            String link = newsItem.getLink();
            if (StringUtils.isNotEmpty(link)) {
                out.print("<a href=\"" + link + "\">");
            }
            out.print(newsItem.getTitle());
            if (StringUtils.isNotEmpty(link)) {
                out.print("</a>");
            }
            out.print(" ");
            out.print(newsItem.getText());
        } else {
            // output default news item
            if (_state != null) {
                ISchoolDao schoolDao = (ISchoolDao) SpringUtil.getApplicationContext().getBean(ISchoolDao.BEAN_ID);
                int numSchools = schoolDao.countSchools(_state, null, null, null);
                out.print("We have profiles for " + numSchools + " schools in ");
                out.print(_state.getLongName() + ", ");
                out.print("including test scores, student-teacher ratios and demographic info.");
            } else {
                out.print("We have profiles for over 120,000 schools across the country, " +
                        "including test scores, student-teacher ratios and demographic info..");
            }
        }
    }
}
