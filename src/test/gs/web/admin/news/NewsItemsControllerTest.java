/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: NewsItemsControllerTest.java,v 1.4 2006/09/21 17:59:05 aroy Exp $
 */

package gs.web.admin.news;

import gs.data.content.INewsItemDao;
import gs.data.content.NewsItem;
import gs.data.state.StateManager;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import org.springframework.validation.BindException;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Date;

/**
 * Provides tests for Edit/DeleteNewsItemController.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 * @author <a href="mailto:aroy@greatschools.net">Anthony Roy</a>
 */
public class NewsItemsControllerTest extends BaseControllerTestCase {

    private INewsItemDao _newsItemDao;

    protected void setUp() throws Exception {
        super.setUp();
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
        command.setStatesAsString("CA^MD");

        try {
            controller.onSubmit(command);

            List list = _newsItemDao.findNewsItems(category);
            assertEquals(1, list.size());
            NewsItem item = (NewsItem) list.get(0);
            assertEquals("Some text", item.getText());
            assertEquals("Some link", item.getLink());
            assertEquals(2, item.getStates().size());

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

        controller.handleRequest(request, getResponse());

        assertEquals(0, _newsItemDao.findNewsItems(category).size());


    }

    public void testEdit() throws Exception {
        // Preconditions
        final Random random = new Random();
        final String category = "WEBTEST" + random.nextInt(888);

        assertEquals(0, _newsItemDao.findNewsItems(category).size());

        // Setup
        NewsItem item = new NewsItem();
        item.setCategory(category);
        item.setText("Some text");
        item.setLink("Some link");
        item.setStatesAsString("CA^MD");

        _newsItemDao.saveNewsItem(item);
        try {
            assertEquals(1, _newsItemDao.findNewsItems(category).size());

            // Calls
            EditNewsItemController controller = new EditNewsItemController();
            controller.setNewsItemDao(_newsItemDao);
            controller.setStateManager((StateManager) getApplicationContext().getBean(StateManager.BEAN_ID));

            getRequest().setParameter("id", String.valueOf(item.getId()));

            NewsItem editedItem = (NewsItem) controller.formBackingObject(getRequest());
            assertEquals(item, editedItem);

            editedItem.setText("Some new text");
            controller.onSubmit(editedItem);

            assertEquals(1, _newsItemDao.findNewsItems(category).size());

            NewsItem compareItem = _newsItemDao.findNewsItem(item.getId().intValue());
            assertEquals("Some new text", compareItem.getText());
            assertEquals("Some link", compareItem.getLink());
            assertEquals(2, compareItem.getStates().size());

        } finally {
            _newsItemDao.removeNewsItem(item);
        }
    }

    public void testBindStatesFromRequest() throws Exception {
        NewsItem item = new NewsItem();
        assertNull(item.getStates());

        getRequest().addParameter(EditNewsItemController.STATE_SELECT_NAME, "CA");
        getRequest().addParameter(EditNewsItemController.STATE_SELECT_NAME, "MD");

        EditNewsItemController controller = new EditNewsItemController();
        controller.setStateManager((StateManager) getApplicationContext().getBean(StateManager.BEAN_ID));

        BindException errors = new BindException(item, "");
        controller.onBind(getRequest(), item, errors);
        assertEquals(0, errors.getErrorCount());
        assertNotNull(item.getStates());
        assertEquals(2, item.getStates().size());

        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setServerName("www.greatschools.net");
        controller.onBind(request, item, errors);
        assertEquals(0, errors.getErrorCount());
        assertNull(item.getStates());
    }

    public void testBindStopDateFromRequest() throws Exception {
        NewsItem item = new NewsItem();
        assertNull(item.getStop());

        Date now = new Date();

        getRequest().setParameter(EditNewsItemController.EXPIRATION_SELECT_NAME,
                EditNewsItemController.ONE_DAY);

        EditNewsItemController controller = new EditNewsItemController();
        controller.setStateManager((StateManager) getApplicationContext().getBean(StateManager.BEAN_ID));

        BindException errors = new BindException(item, "");
        controller.onBind(getRequest(), item, errors);
        assertEquals(0, errors.getErrorCount());
        assertNotNull(item.getStop());
        assertTrue(now.before(item.getStop()));
        Date oneDay = item.getStop();

        getRequest().setParameter(EditNewsItemController.EXPIRATION_SELECT_NAME,
                EditNewsItemController.NO_CHANGE);
        controller.onBind(getRequest(), item, errors);
        assertEquals(0, errors.getErrorCount());
        assertNotNull(item.getStop());
        assertTrue(now.before(item.getStop()));
        assertEquals(oneDay, item.getStop());

        getRequest().setParameter(EditNewsItemController.EXPIRATION_SELECT_NAME,
                EditNewsItemController.ONE_WEEK);
        controller.onBind(getRequest(), item, errors);
        assertEquals(0, errors.getErrorCount());
        assertNotNull(item.getStop());
        assertTrue(now.before(item.getStop()));
        assertFalse(oneDay.equals(item.getStop()));
        assertTrue(oneDay.before(item.getStop()));

        getRequest().setParameter(EditNewsItemController.EXPIRATION_SELECT_NAME,
                "");
        controller.onBind(getRequest(), item, errors);
        assertEquals(0, errors.getErrorCount());
        assertNull(item.getStop());
    }
}
