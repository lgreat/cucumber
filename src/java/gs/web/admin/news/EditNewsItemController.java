/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: EditNewsItemController.java,v 1.11 2009/12/04 22:27:14 chriskimm Exp $
 */

package gs.web.admin.news;

import gs.data.content.INewsItemDao;
import gs.data.content.NewsItem;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.util.ReadWriteController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.HashSet;
import java.util.Calendar;
import java.util.Date;

/**
 * Provides create and edit capabilities for news item blurbs.
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 * @author <a href="mailto:aroy@greatschools.org">Anthony Roy</a>
 */
public class EditNewsItemController extends SimpleFormController implements ReadWriteController {

    protected final Log _log = LogFactory.getLog(getClass());
    public static final String NO_CHANGE = "noChange"; // leave stop date unchanged
    public static final String ONE_DAY = "oneDay";
    public static final String ONE_WEEK = "oneWeek";
    public static final String ONE_MONTH = "oneMonth";
    public static final String TWO_MONTHS = "twoMonths";
    public static final String THREE_MONTHS = "threeMonths";
    public static final String STATE_SELECT_NAME = "stateSelect";
    public static final String EXPIRATION_SELECT_NAME = "stopSelect";

    INewsItemDao _newsItemDao;
    StateManager _stateManager;

    protected void onBind(HttpServletRequest request, Object command, BindException errors) throws Exception {
        NewsItem newsItem = (NewsItem) command;
        newsItem.setStates(null); // clear out existing states

        String[] states = request.getParameterValues(STATE_SELECT_NAME);
        if (states != null && states.length > 0) {
            Set stateSet = new HashSet();
            for (int x=0; x < states.length; x++) {
                State state = _stateManager.getState(states[x]);
                if (state != null) { // avoid null case
                    stateSet.add(state);
                }
            }
            if (stateSet.size() > 0) { // avoid null case
                newsItem.setStates(stateSet);
            }
        }
        String stopSelect = request.getParameter(EXPIRATION_SELECT_NAME);
        final Date now = new Date();
        if (StringUtils.isEmpty(stopSelect)) {
            newsItem.setStop(null);
        } else if (!stopSelect.equals(NO_CHANGE)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(now);
            if (stopSelect.equals(ONE_DAY)) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
            } else if (stopSelect.equals(ONE_WEEK)) {
                cal.add(Calendar.WEEK_OF_YEAR, 1);
            } else if (stopSelect.equals(ONE_MONTH)) {
                cal.add(Calendar.MONTH, 1);
            } else if (stopSelect.equals(TWO_MONTHS)) {
                cal.add(Calendar.MONTH, 2);
            } else if (stopSelect.equals(THREE_MONTHS)) {
                cal.add(Calendar.MONTH, 3);
            }
            clearToMidnight(cal);
            newsItem.setStop(cal.getTime());
        }
        if (newsItem.getStart() == null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(now);
            clearToMidnight(cal);
            newsItem.setStart(cal.getTime());
        }
        super.onBind(request, newsItem, errors);
    }

    protected void clearToMidnight(Calendar cal) {
        cal.clear(Calendar.MILLISECOND);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.HOUR);
        cal.clear(Calendar.HOUR_OF_DAY);
        cal.clear(Calendar.AM_PM);
    }

    protected ModelAndView onSubmit(Object command) throws Exception {

        NewsItem newsItem = (NewsItem) command;

        if (StringUtils.isNotEmpty(newsItem.getText())) {
            newsItem.setText(newsItem.getText().replaceAll("<", "&lt;"));
            newsItem.setText(newsItem.getText().replaceAll(">", "&gt;"));
        }

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

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    protected Object formBackingObject(HttpServletRequest request) throws java.lang.Exception {
        if (request.getParameter("id") != null) {
            return _newsItemDao.findNewsItem(Integer.parseInt(request.getParameter("id")));
        }
        return super.formBackingObject(request);
    }
}
