/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: DeleteNewsItemController.java,v 1.2 2006/07/26 22:29:20 thuss Exp $
 */

package gs.web.admin.news;

import gs.data.content.INewsItemDao;
import gs.data.content.NewsItem;
import gs.data.state.State;
import gs.web.util.UrlBuilder;
import gs.web.util.ReadWriteController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class DeleteNewsItemController implements ReadWriteController {

    private INewsItemDao newsItemDao;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        NewsItem item = newsItemDao.findNewsItem(Integer.parseInt(request.getParameter("id")));

        UrlBuilder builder = new UrlBuilder(UrlBuilder.ADMIN_NEWS_ITEMS, State.CA);
        builder.setParameter("message",
                "Deleted news item. " +
                        "<br /><br />Category: " + item.getCategory() + "<br />" + item.getText() + "");

        newsItemDao.removeNewsItem(item);

        View view = new RedirectView(builder.asSiteRelative(request));

        return new ModelAndView(view);
    }

    public INewsItemDao getNewsItemDao() {
        return newsItemDao;
    }

    public void setNewsItemDao(INewsItemDao newsItemDao) {
        this.newsItemDao = newsItemDao;
    }
}
