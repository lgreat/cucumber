/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: FeedTagHandler.java,v 1.3 2008/08/28 20:22:11 thuss Exp $
 */

package gs.web.content;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * A generic rss/atom feed handler with time based cache expiry, uses ehcache
 *
 * @author thuss
 */
public class FeedTagHandler extends SimpleTagSupport {

    protected static int CACHE_SIZE = 500;
    protected static int CACHE_ENTRY_TTL_SECONDS = 3600; // 1 hour
    protected static int CACHE_ENTRY_IDLE_SECONDS = 3600; // 1 hour
    protected static String CACHE_NAME = "feedCache";

    protected static final Log _log = LogFactory.getLog(FeedTagHandler.class);
    private static Cache _cache;

    private String _feedUrl;
    private Integer _numberOfEntriesToShow;

    static {
        CacheManager manager = CacheManager.create();
        _cache = new Cache(CACHE_NAME, CACHE_SIZE, false, false, CACHE_ENTRY_TTL_SECONDS, CACHE_ENTRY_IDLE_SECONDS);
        manager.addCache(_cache); // You have to add a cache to a manager for it to work
    }

    public void doTag() throws JspException, IOException {
        super.doTag();
        StringBuffer out = new StringBuffer();
        List<SyndEntry> entries = getFeedEntries(_feedUrl);
        if (entries.size() > 0) {
            out.append("<ol>");
            for (int i = 0; i < _numberOfEntriesToShow && i < entries.size(); i++) {
                SyndEntry entry = entries.get(i);
                out.append("<li><a href=\"").append(entry.getLink()).append("\">").
                        append(entry.getTitle()).append("</a>").append("</li>");
            }
            out.append("</ol>");
            getJspContext().getOut().print(out);
        } else {
            if (getJspBody() != null) getJspBody().invoke(getJspContext().getOut());
        }
    }

    protected List<SyndEntry> getFeedEntries(String feedUrl) {
        List<SyndEntry> entries = null;
        try {
            Element cacheElement = _cache.get(feedUrl);
            if (cacheElement != null) {
                entries = (List<SyndEntry>) cacheElement.getObjectValue();
            } else {
                System.setProperty("sun.net.client.defaultConnectTimeout", "5000");
                System.setProperty("sun.net.client.defaultReadTimeout", "5000");
                entries = getFeedEntriesFromSource(feedUrl);
                if (_numberOfEntriesToShow == null) _numberOfEntriesToShow = entries.size();
                if (_numberOfEntriesToShow < entries.size())
                    entries = entries.subList(0, _numberOfEntriesToShow); // Shorten the list for caching
                _cache.put(new Element(feedUrl, entries));
            }
        } catch (Exception e) {
            _log.error("Unable to access feed at " + feedUrl, e);
        }
        return (entries == null) ? new ArrayList<SyndEntry>() : entries;
    }

    protected List<SyndEntry> getFeedEntriesFromSource(String feedUrl) throws Exception {
        return new SyndFeedInput().build(new XmlReader(new URL(feedUrl))).getEntries();
    }

    public void setFeedUrl(String atomUrl) {
        _feedUrl = atomUrl;
    }

    public void setNumberOfEntriesToShow(Integer numberOfEntriesToShow) {
        _numberOfEntriesToShow = numberOfEntriesToShow;
    }
}