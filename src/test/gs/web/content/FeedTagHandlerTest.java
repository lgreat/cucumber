package gs.web.content;

import gs.web.jsp.MockJspWriter;
import gs.web.jsp.MockPageContext;
import junit.framework.TestCase;

import javax.servlet.jsp.JspContext;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;

import java.util.List;
import java.util.ArrayList;

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

    public void testGettingFeedEntriesAndCaching() throws Exception {
        _tag.setFeedUrl("http://testEntriesToShowAndCaching"); // cache key
        _tag.setNumberOfEntriesToShow(2);
        _tag.setFeedEntriesFromSource(generateFeedEntries(5));
        _tag.doTag();
        String output = getJspContextOutput();
        assertTrue(output.indexOf("http://post2") > -1);
        assertFalse(output.indexOf("http://post3") > -1);

        // Now do it again and verify a cache hit
        resetJspContext();
        _tag.setFeedEntriesFromSource(null); // Only a cache hit would return entries now
        _tag.doTag();
        output = getJspContextOutput();
        assertTrue(output.indexOf("http://post2") > -1);
        assertFalse(output.indexOf("http://post3") > -1);
    }

    public void testGettingNoFeedEntriesAndCaching() throws Exception {
        _tag.setFeedUrl("http://testNoEntriesToShowAndCaching"); // cache key
        _tag.setNumberOfEntriesToShow(2);
        _tag.setFeedEntriesFromSource(generateFeedEntries(0));
        _tag.doTag();
        String output = getJspContextOutput();
        assertEquals(output, "");

        // Now do it again and verify a cache hit
        resetJspContext();
        _tag.setFeedEntriesFromSource(generateFeedEntries(5)); // Cached version should still be empty
        _tag.doTag();
        output = getJspContextOutput();
        assertEquals("", output);
    }

    public void testAbbreviatingLongFeedEntries() throws Exception {
        // First test for no abbreviation
        _tag.setFeedUrl("http://testEntryAbbreviation"); // cache key
        _tag.setNumberOfEntriesToShow(1);
        _tag.setFeedEntriesFromSource(new ArrayList<SyndEntry>() {
            {
                add(new SyndEntryImpl() {{
                    setTitle("Very Long Title That Should Be Abbreviated");
                    setLink("http://post");
                }});
            }});
        _tag.doTag();
        String output = getJspContextOutput();
        assertEquals("No Abbreviation Expected",
                "<ol><li><a href=\"http://post\">Very Long Title That Should Be Abbreviated</a></li></ol>",
                output);

        // Now abbreviate it
        resetJspContext();
        _tag.setNumberOfCharactersPerEntryToShow(12);
        _tag.doTag();
        output = getJspContextOutput();
        assertEquals("Abbreviation Expected",
                "<ol><li><a href=\"http://post\">Very Long...</a></li></ol>",
                output);
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