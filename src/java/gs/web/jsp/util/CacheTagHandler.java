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
    public static final String DEFAULT_CACHE_NAME = "gs.web.cache.fragment.default";

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

            if (_key != null) {
                CacheManager manager = getCacheManager();
                if (manager != null) {
                    Cache cache = manager.getCache(_cacheName);
                    if (cache != null) {
                        Element cacheElement = cache.get(_key);
                        if (cacheElement != null && cacheElement.getObjectValue() != null) {
                            pageContext.getOut().print(cacheElement.getObjectValue().toString());
                            return SKIP_BODY;
                        }
                    } else {
                        _log.error("Cache not defined: '" + _cacheName + "'");
                    }
                } else {
                    _log.error("Unable to obtain cache manager.");
                }
            } else {
                _log.error("Cache key is null.");
            }

        } catch (Exception e) {
            _log.error("Error checking cache.", e);
            // Do nothing, just continue on and evaluate the body as if there was no cache
        }

        // In case of error, do not buffer the body
        return EVAL_BODY_BUFFERED; // calls doAfterBody, but SKIP_BODY there evals to no body
    }

    public int doAfterBody() throws JspException {
        if (bodyContent != null) {
            try {
                // Write the buffered body to the output stream
                bodyContent.writeOut(bodyContent.getEnclosingWriter());
            } catch (IOException ioe) {
                _log.error("Exception outputting body of tag.", ioe);
                throw new JspException("Error: " + ioe.getMessage(), ioe);
            }

            cacheBody();
        } else {
            _log.error("Body content null");
        }

        return SKIP_BODY;
    }

    protected void cacheBody() {
        try {
            if (_key != null) {
                CacheManager manager = getCacheManager();
                Cache cache = null;
                if (manager != null) {
                    cache = manager.getCache(_cacheName);
                }

                // If we have a valid cache, put the body into it
                if (cache != null) {
                    String body = bodyContent.getString();
                    //_log.warn("Attempting to cache " + body.length() + " chars as '" + _key + "'");
                    Element element = new Element(_key, body);
                    cache.put(element);
                }
            }
        } catch (Exception e) {
            // Ignore, act as if caching is disabled and proceed with rendering tag
            _log.error("Exception while caching tag body.", e);
        }
    }

    protected CacheManager getCacheManager() {
        return CacheManager.create();
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