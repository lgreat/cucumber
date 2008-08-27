package gs.web.content;

import gs.web.jsp.MockJspWriter;
import gs.web.jsp.MockPageContext;
import junit.framework.TestCase;

import javax.servlet.jsp.JspContext;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author thuss
 */
public class FeedTagHandlerTest extends TestCase {
    private FeedTagHandlerTestCase _tag;
    private MockPageContext _jspContext;

    public void setUp() {
        _tag = new FeedTagHandlerTestCase();
        resetJspContext();
    }

    public void testNumberOfEntriesToShowAndCaching() throws Exception {
        _tag.setFeedUrl("http://testNumberOfEntriesToShowAndCaching"); // cache key
        _tag.setNumberOfEntriesToShow(2);
        _tag.setFeedEntriesFromSource(generateFeedEntries(5));
        _tag.doTag();
        String output = getJspContextOutput();
        assertTrue(output.indexOf("http://post2") > -1);
        assertFalse(output.indexOf("http://post3") > -1);

        // Now do it again and verify a cache hit
        resetJspContext();
        _tag.setFeedEntriesFromSource(null); // This will tell us if it gets it from cache
        _tag.doTag();
        output = getJspContextOutput();
        assertTrue(output.indexOf("http://post2") > -1);
        assertFalse(output.indexOf("http://post3") > -1);

    }

    /**
     * A more easily testable version of the tag handler
     */
    public class FeedTagHandlerTestCase extends FeedTagHandler {

        private JspContext _jspContext;
        private List<SyndEntry> _entries;

        public JspContext getJspContext() {
            return _jspContext;
        }

        public void setJspContext(JspContext jspContext) {
            _jspContext = jspContext;
        }

        protected List<SyndEntry> getFeedEntriesFromSource(String feedUrl) throws Exception {
            return _entries;
        }

        public void setFeedEntriesFromSource(List<SyndEntry> entries) {
            _entries = entries;
        }
    }

    private void resetJspContext() {
        _jspContext = new MockPageContext();
        _tag.setJspContext(_jspContext);
    }

    private List<SyndEntry> generateFeedEntries(final int size) {
        return new ArrayList<SyndEntry>() {
            {
                for (int i = 0; i < size; i++) {
                    final int num = i + 1;
                    add(new SyndEntryImpl() {
                        {
                            setTitle("Post " + num);
                            setLink("http://post" + num);
                        }
                    });
                }
            }
        };
    }

    private String getJspContextOutput() {
        return ((MockJspWriter) _jspContext.getOut()).getOutputBuffer().toString();
    }
}