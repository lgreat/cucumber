package gs.web.jsp.util;

import gs.web.jsp.MockJspWriter;
import gs.web.jsp.MockPageContext;
import junit.framework.TestCase;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import java.io.*;

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
        String output;

        tag = new CacheTagHandler();
        Cache cache = cacheManager.getCache(tag.getCacheName());
        assertNotNull("Cache should exist", cache);
        assertEquals("Should be nothing in cache", 0, cache.getStatistics().getObjectCount());
        assertEquals("Should be 0 hits", 0, cache.getStatistics().getCacheHits());
        assertEquals("Should be 0 misses", 0, cache.getStatistics().getCacheMisses());

        pageContext = new MockPageContext();
        BodyContent bodyContent = new BodyContentImpl(pageContext.getOut(), body);

        tag.setPageContext(pageContext);
        tag.setKey(cacheKey);
        tag.doStartTag();
        assertEquals("Should be nothing in cache", 0, cache.getStatistics().getObjectCount());
        assertEquals("Should be 0 hits", 0, cache.getStatistics().getCacheHits());
        assertEquals("Should be 1 miss", 1, cache.getStatistics().getCacheMisses());
        assertNull("Cached element should not exist", cache.get(cacheKey)); // This causes a miss

        tag.setBodyContent(bodyContent);
        tag.doAfterBody();
        assertEquals("Should be 1 object in cache", 1, cache.getStatistics().getObjectCount());
        assertEquals("Should be 0 hits", 0, cache.getStatistics().getCacheHits());
        assertEquals("Should be 2 misses (1 from tag, 1 from assertion)", 2, cache.getStatistics().getCacheMisses());
        assertEquals("Cached content should be tag body", body, cache.get(cacheKey).getObjectValue().toString()); // This causes a hit
        output = ((MockJspWriter) pageContext.getOut()).getOutputBuffer().toString();
        assertEquals("Tag output should match tag body", body, output);

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

        output = ((MockJspWriter) pageContext.getOut()).getOutputBuffer().toString();
        assertEquals("Tag output should match tag body", body, output);
    }

    public void testNonexistentCache () throws JspException {
        CacheManager cacheManager = CacheManager.create();
        String cacheKey = "testCustomCacheKey";
        String body = "The body";
        MockPageContext pageContext;
        CacheTagHandler tag;
        String output;

        tag = new CacheTagHandler();
        tag.setCacheName("testCustomCacheCache");
        Cache cache = cacheManager.getCache(tag.getCacheName());
        assertNull("Cache should not exist", cache);

        pageContext = new MockPageContext();
        BodyContent bodyContent = new BodyContentImpl(pageContext.getOut(), body);

        tag.setPageContext(pageContext);
        tag.setKey(cacheKey);
        tag.doStartTag();

        tag.setBodyContent(bodyContent);
        tag.doAfterBody();
        output = ((MockJspWriter) pageContext.getOut()).getOutputBuffer().toString();
        assertEquals("Tag output should match tag body", body, output);
    }

    /**
     * Bare bones implementation of BodyContent to test passing body content to a tag.  Many methods are overridden,
     * but not implemented.  DO NOT USE ELSEWHERE.
     */
    private class BodyContentImpl extends BodyContent {
        private final String _body;
        private final Reader _reader;

        public BodyContentImpl(javax.servlet.jsp.JspWriter e, String body) {
            super(e);
            _body = body;
            _reader = new StringReader(_body);
        }

        @Override
        public Reader getReader() {
            return _reader;
        }

        @Override
        public String getString() {
            return _body;
        }

        @Override
        public void writeOut(Writer writer) throws IOException {
            writer.write(_body);
        }

        @Override
        public void newLine() throws IOException {
        }

        @Override
        public void print(boolean b) throws IOException {
        }

        @Override
        public void print(char c) throws IOException {
        }

        @Override
        public void print(int i) throws IOException {
        }

        @Override
        public void print(long l) throws IOException {
        }

        @Override
        public void print(float v) throws IOException {
        }

        @Override
        public void print(double v) throws IOException {
        }

        @Override
        public void print(char[] chars) throws IOException {
        }

        @Override
        public void print(String s) throws IOException {
        }

        @Override
        public void print(Object o) throws IOException {
        }

        @Override
        public void println() throws IOException {
        }

        @Override
        public void println(boolean b) throws IOException {
        }

        @Override
        public void println(char c) throws IOException {
        }

        @Override
        public void println(int i) throws IOException {
        }

        @Override
        public void println(long l) throws IOException {
        }

        @Override
        public void println(float v) throws IOException {
        }

        @Override
        public void println(double v) throws IOException {
        }

        @Override
        public void println(char[] chars) throws IOException {
        }

        @Override
        public void println(String s) throws IOException {
        }

        @Override
        public void println(Object o) throws IOException {
        }

        @Override
        public void clear() throws IOException {
        }

        @Override
        public void clearBuffer() throws IOException {
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public int getRemaining() {
            return 0;
        }
    }

}
