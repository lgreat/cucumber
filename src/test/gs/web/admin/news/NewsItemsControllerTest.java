/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: NewsItemsControllerTest.java,v 1.1 2006/05/30 21:20:26 apeterson Exp $
 */

package gs.web.admin.news;

import gs.data.content.INewsItemDao;
import gs.data.content.NewsItem;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.SessionContextUtil;
import org.springframework.web.servlet.ModelAndView;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Provides tests for NearbyCitiesController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class NewsItemsControllerTest extends BaseControllerTestCase {

    private SessionContextUtil _sessionContextUtil;
    private INewsItemDao _newsItemDao;

    protected void setUp() throws Exception {
        super.setUp();
        _sessionContextUtil = (SessionContextUtil) getApplicationContext().
                getBean(SessionContextUtil.BEAN_ID);
        _newsItemDao = (INewsItemDao) getApplicationContext().
                getBean(INewsItemDao.BEAN_ID);

    }

    public void testCreate() throws Exception {
        EditNewsItemController controller = new EditNewsItemController();
        controller.setApplicationContext(getApplicationContext());
        controller.setNewsItemDao(_newsItemDao);

        NewsItem command = new NewsItem();
        final Random random = new Random();
        final String category = "WEBTEST" + random.nextInt(888);
        command.setCategory(category);
        command.setText("Some text");
        command.setLink("Some link");

        try {
            controller.onSubmit(command);

            List list = _newsItemDao.findNewsItems(category);
            assertEquals(1, list.size());
            NewsItem item = (NewsItem) list.get(0);
            assertEquals("Some text", item.getText());
            assertEquals("Some link", item.getLink());

        } finally {
            List list = _newsItemDao.findNewsItems(category);

            for (Iterator iter = list.iterator(); iter.hasNext();) {
                NewsItem item = (NewsItem) iter.next();
                _newsItemDao.removeNewsItem(item);
            }
        }

    }

    public void testDelete() throws Exception {
        // Preconditions
        final Random random = new Random();
        final String category = "WEBTEST" + random.nextInt(888);

        assertEquals(0, _newsItemDao.findNewsItems(category).size());

        // Setup
        NewsItem item = new NewsItem();
        item.setCategory(category);
        item.setText("Some text");
        item.setLink("Some link");

        _newsItemDao.saveNewsItem(item);
        assertEquals(1, _newsItemDao.findNewsItems(category).size());

        // Call
        GsMockHttpServletRequest request = getRequest();
        request.setParameter("id", item.getId().toString());



        DeleteNewsItemController controller = new DeleteNewsItemController();
        controller.setNewsItemDao(_newsItemDao);

        ModelAndView mav = controller.handleRequest(request, getResponse());

        assertEquals(0, _newsItemDao.findNewsItems(category).size());


    }

}
