package gs.web.jsp.util;

import gs.web.jsp.MockPageContext;
import junit.framework.TestCase;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import java.io.*;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class CacheTagHandlerTest extends TestCase {

    public void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("www.greatschools.org");
    }

    public void testCaching() throws JspException, IOException {
        CacheManager cacheManager = CacheManager.create();
        String cacheKey = "testCachingKey";
        String body = "The body";
        MockPageContext pageContext;
        CacheTagHandler tag;

        tag = new CacheTagHandler();
        Cache cache = cacheManager.getCache(tag.getCacheName());
        assertNotNull("Cache should exist", cache);
        assertEquals("Should be nothing in cache", 0, cache.getStatistics().getObjectCount());
        assertEquals("Should be 0 hits", 0, cache.getStatistics().getCacheHits());
        assertEquals("Should be 0 misses", 0, cache.getStatistics().getCacheMisses());

        pageContext = new MockPageContext();
        BodyContent bodyContent = createStrictMock(BodyContent.class);

        tag.setPageContext(pageContext);
        tag.setKey(cacheKey);
        assertEquals("Key should be set", cacheKey, tag.getKey());
        tag.doStartTag();
        assertEquals("Should be nothing in cache", 0, cache.getStatistics().getObjectCount());
        assertEquals("Should be 0 hits", 0, cache.getStatistics().getCacheHits());
        assertEquals("Should be 1 miss", 1, cache.getStatistics().getCacheMisses());
        assertNull("Cached element should not exist", cache.get(cacheKey)); // This causes a miss

        tag.setBodyContent(bodyContent);
        expect(bodyContent.getEnclosingWriter()).andReturn(pageContext.getOut());
        bodyContent.writeOut(pageContext.getOut());
        expect(bodyContent.getString()).andReturn(body);
        replay(bodyContent);
        tag.doAfterBody();
        verify(bodyContent);
        assertEquals("Should be 1 object in cache", 1, cache.getStatistics().getObjectCount());
        assertEquals("Should be 0 hits", 0, cache.getStatistics().getCacheHits());
        assertEquals("Should be 2 misses (1 from tag, 1 from assertion)", 2, cache.getStatistics().getCacheMisses());
        assertEquals("Cached content should be tag body", body, cache.get(cacheKey).getObjectValue().toString()); // This causes a hit

        tag = new CacheTagHandler();
        assertNotNull("Cache should exist", cache);
        assertEquals("Should be 1 object in cache", 1, cache.getStatistics().getObjectCount());
        assertEquals("Should be 1 hits (1 from assertion)", 1, cache.getStatistics().getCacheHits());
        assertEquals("Should be 2 misses (1 from tag, 1 from assertion)", 2, cache.getStatistics().getCacheMisses());

        pageContext = new MockPageContext();
        tag.setPageContext(pageContext);
        tag.setKey(cacheKey);
        tag.doStartTag();
        assertEquals("Should be 1 object in cache", 1, cache.getStatistics().getObjectCount());
        assertEquals("Should be 2 hits (1 from tag, 1 from assertion)", 2, cache.getStatistics().getCacheHits());
        assertEquals("Should be 2 misses (1 from tag, 1 from assertion)", 2, cache.getStatistics().getCacheMisses());
        Element element = cache.get(cacheKey); // This causes another hit
        assertNotNull("Cached element should exist", element);
        assertEquals("Cached content should be tag body", body, element.getObjectValue().toString());
    }

    public void testNonexistentCache () throws JspException, IOException {
        CacheManager cacheManager = CacheManager.create();
        String cacheKey = "testCustomCacheKey";
        String body = "The body";
        MockPageContext pageContext;
        CacheTagHandler tag;

        tag = new CacheTagHandler();
        tag.setCacheName("testCustomCacheCache");
        Cache cache = cacheManager.getCache(tag.getCacheName());
        assertNull("Cache should not exist", cache);

        pageContext = new MockPageContext();
        BodyContent bodyContent = createStrictMock(BodyContent.class);

        tag.setPageContext(pageContext);
        tag.setKey(cacheKey);
        tag.doStartTag();

        tag.setBodyContent(bodyContent);
        expect(bodyContent.getEnclosingWriter()).andReturn(pageContext.getOut());
        bodyContent.writeOut(pageContext.getOut());
        replay(bodyContent);
        tag.doAfterBody();
        verify(bodyContent);
    }
}
