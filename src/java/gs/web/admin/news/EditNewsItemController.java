/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: EditNewsItemController.java,v 1.1 2006/05/30 21:20:26 apeterson Exp $
 */

package gs.web.admin.news;

import gs.data.content.INewsItemDao;
import gs.data.content.NewsItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class EditNewsItemController extends SimpleFormController {

    private static final Log _log = LogFactory.getLog(EditNewsItemController.class);

    INewsItemDao _newsItemDao;

    protected ModelAndView onSubmit(Object command) throws Exception {

        NewsItem newsItem = (NewsItem) command;

        _newsItemDao.saveNewsItem(newsItem);

        return super.onSubmit(command);
    }

    protected boolean isFormSubmission(HttpServletRequest request) {
        return request.getParameter("submit") != null;

    }

    public INewsItemDao getNewsItemDao() {
        return _newsItemDao;
    }

    public void setNewsItemDao(INewsItemDao newsItemDao) {
        _newsItemDao = newsItemDao;
    }


}
