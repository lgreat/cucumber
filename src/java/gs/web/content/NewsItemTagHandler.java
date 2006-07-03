/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: NewsItemTagHandler.java,v 1.1 2006/07/03 22:14:36 apeterson Exp $
 */

package gs.web.content;

import gs.data.content.INewsItemDao;
import gs.data.content.NewsItem;
import gs.data.util.SpringUtil;
import org.apache.commons.lang.StringUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.List;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class NewsItemTagHandler extends SimpleTagSupport {

    private String _category;

    public void doTag() throws JspException, IOException {
        INewsItemDao newsItemDao = (INewsItemDao) SpringUtil.getApplicationContext().getBean(INewsItemDao.BEAN_ID);
        List newsItems = newsItemDao.findNewsItems(_category);

        JspWriter out = this.getJspContext().getOut();
        if (newsItems.size() > 0) {
            NewsItem newsItem = (NewsItem) newsItems.get(0);
            if (StringUtils.isNotEmpty(newsItem.getLink())) {
                out.print("<a href=\""+newsItem.getLink()+"\">");
            }
            out.print(newsItem.getTitle());
            if (StringUtils.isNotEmpty(newsItem.getLink())) {
                out.print("</a>");
            }
            out.print(" ");
            out.print(newsItem.getText());
        }   else {
            out.print("News item not available");
        }

    }


    public void setCategory(String category) {
        _category = category;
    }

}
