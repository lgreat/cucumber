/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: FeedTagHandler.java,v 1.11 2009/12/04 20:54:17 npatury Exp $
 */

package gs.web.content;

import com.sun.syndication.feed.synd.SyndEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.*;

import gs.web.jsp.Util;
import gs.data.util.feed.IFeedDao;
import gs.data.util.feed.CachedFeedDaoFactory;

/**
 * A generic rss/atom feed handler with time based cache expiry, uses ehcache
 * to cache the feed based on the URL.
 * <p/>
 * If numberOfEntries is not set it displays all entries in the feed.
 * <p/>
 * If numberOfCharactersPerEntry is not set it displays all characters in the
 * title entry, otherwise it truncates on the word boundry and adds ... to the end
 *
 * @author thuss
 */
public class FeedTagHandler extends SimpleTagSupport {

    protected static final Log _log = LogFactory.getLog(FeedTagHandler.class);

    private String _feedUrl;
    private Integer _numberOfEntriesToShow;
    private Integer _numberOfCharactersPerEntryToShow;
    private String _onClick;
    private boolean _showCommentCount;
    private IFeedDao _feedDao;

    protected void initializeFeedDao() {
        _feedDao = new CachedFeedDaoFactory().getFeedDao();
    }

    public void doTag() throws JspException, IOException {
        super.doTag();
        if (_feedDao == null) {
            initializeFeedDao();
        }
        StringBuffer out = new StringBuffer();
        _log.info(_feedUrl);
        List<SyndEntry> entries = _feedDao.getFeedEntries(_feedUrl, _numberOfEntriesToShow);
        if (entries.size() > 0) {
            out.append("<ol>");
            for (int i = 0; i < _numberOfEntriesToShow && i < entries.size(); i++) {
                SyndEntry entry = entries.get(i);

                String title = (_numberOfCharactersPerEntryToShow != null) ?
                        Util.abbreviateAtWhitespace(entry.getTitle(), _numberOfCharactersPerEntryToShow) :
                        entry.getTitle();

                // Write out the HTML
                out.append("<li><div><a ");
                if (_onClick != null) out.append("onclick=\"").append(_onClick).append("\" ");
                out.append("href=\"").append(entry.getLink()).append("\">");
                out.append(title);
                out.append("</a>");
                if (_showCommentCount) {
                    int commentCount = 0;
                    // TODO: actually implement comment count
                    out.append("<span>").append(commentCount).append(" answer");
                    if (commentCount != 1) {
                        out.append("s");
                    }
                    out.append("</span>");
                }
                out.append("</div></li>");
            }
            out.append("</ol>");
            getJspContext().getOut().print(out);
        } else {
            if (getJspBody() != null) getJspBody().invoke(getJspContext().getOut());
        }
    }

    public void setFeedUrl(String atomUrl) {
        _feedUrl = atomUrl;
    }

    public void setNumberOfEntriesToShow(Integer numberOfEntriesToShow) {
        _numberOfEntriesToShow = numberOfEntriesToShow;
    }

    public void setNumberOfCharactersPerEntryToShow(Integer numberOfCharactersPerEntryToShow) {
        _numberOfCharactersPerEntryToShow = numberOfCharactersPerEntryToShow;
    }

    public void setOnClick(String onClick) {
        _onClick = onClick;
    }

    public boolean isShowCommentCount() {
        return _showCommentCount;
    }

    public void setShowCommentCount(boolean showCommentCount) {
        _showCommentCount = showCommentCount;
    }

    public IFeedDao getFeedDao() {
        return _feedDao;
    }

    public void setFeedDao(IFeedDao feedDao) {
        _feedDao = feedDao;
    }
}