package gs.web.jsp.util;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;

/**
 * Cache the body of the tag.  By default it caches into a default cache that is
 * already defined in ehcache.xml.
 *
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class CacheTagHandler extends BodyTagSupport {
    /***
     * Default cache to store objects in.  Can be overridden.
     */
    public static final String DEFAULT_CACHE_NAME = "DefaultPageFragmentCache";

    /**
     * Log for cache information
     */
    private static final Log _log = LogFactory.getLog(CacheTagHandler.class);

    /**
     * The cache key to use to get/put the content
     */
    private String _key;

    /**
     * The name of the cache to get/put the content into
     */
    private String _cacheName = DEFAULT_CACHE_NAME;

    public int doStartTag() throws JspException {
        try {
            CacheManager manager = CacheManager.create();

            if (manager != null) {
                Cache cache = manager.getCache(_cacheName);
                if (cache != null) {
                    Element cacheElement = cache.get(_key);
                    if (cacheElement != null) {
                        pageContext.getOut().print(cacheElement.getObjectValue().toString());
                        return SKIP_BODY;
                    }
                } else {
                    _log.error("Cache not defined: '" + (_cacheName != null ? _cacheName : "null") + "'");
                }
            } else {
                _log.error("Unable to obtain cache manager");
            }

        } catch (Exception e) {
            _log.error("Error checking cache", e);
            // Do nothing, just continue on and evaluate the body as if there was no cache
        }
        return EVAL_BODY_BUFFERED;
    }

    public int doAfterBody() throws JspException {
        CacheManager manager = CacheManager.create();
        Cache cache = null;
        if (manager != null) {
            cache = manager.getCache(_cacheName);
        }
        
        if (bodyContent != null) {
            if (cache != null) {
//                _log.warn("Attempting to cache " + bodyContent.getString().length() + " chars as '" + _key + "'");
                Element element = new Element(_key, bodyContent.getString());
                cache.put(element);
            }
            try {
                JspWriter out = bodyContent.getEnclosingWriter();
                out.print(bodyContent.getString());
            } catch (IOException ioe) {
                _log.error("Exception outputting body of tag", ioe);
                throw new JspException("Error: " + ioe.getMessage());
            }
        } else {
            _log.error("Body content null");
        }

        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

    public String getKey() {
        return _key;
    }

    public void setKey(String key) {
        _key = key;
    }

    public String getCacheName() {
        return _cacheName;
    }

    public void setCacheName(String cacheName) {
        _cacheName = cacheName;
    }
}