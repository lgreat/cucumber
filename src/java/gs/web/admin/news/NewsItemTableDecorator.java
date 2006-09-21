package gs.web.admin.news;

import org.displaytag.decorator.TableDecorator;
import org.apache.commons.lang.StringUtils;
import gs.data.content.NewsItem;

import java.text.SimpleDateFormat;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class NewsItemTableDecorator extends TableDecorator {
    public static final String EXPIRES_DATE_FORMAT = "MMM d, yyyy 'at' hh:mm aaa";

    public String getTitle() {
        NewsItem newsItem = (NewsItem) getCurrentRowObject();
        if (StringUtils.isNotEmpty(newsItem.getLink())) {
            return "<a href=\"" + newsItem.getLink() + "\">" + newsItem.getTitle() + "</a>";
        }
        return newsItem.getTitle();
    }

    public String getStop() {
        NewsItem newsItem = (NewsItem) getCurrentRowObject();
        if (newsItem.getStop() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(EXPIRES_DATE_FORMAT);
            return sdf.format(newsItem.getStop());
        }
        return null;
    }
}
