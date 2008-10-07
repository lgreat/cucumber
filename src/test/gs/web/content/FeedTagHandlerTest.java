package gs.web.content;

import gs.web.jsp.MockJspWriter;
import gs.web.jsp.MockPageContext;
import gs.data.util.feed.IFeedDao;
import junit.framework.TestCase;

import javax.servlet.jsp.JspContext;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;

import java.util.List;
import java.util.ArrayList;

import static org.easymock.EasyMock.*;

/**
 * @author thuss
 */
public class FeedTagHandlerTest extends TestCase {
    private FeedTagHandlerTestCase _tag;
    private MockPageContext _jspContext;
    private IFeedDao _feedDao;

    public void setUp() {
        _tag = new FeedTagHandlerTestCase();
        resetJspContext();
        _feedDao = createStrictMock(IFeedDao.class);
        _tag.setFeedDao(_feedDao);
    }

    public void testGettingFeedEntries() throws Exception {
        _tag.setFeedUrl("http://testEntriesToShowAndCaching"); // cache key
        _tag.setNumberOfEntriesToShow(2);
        expect(_feedDao.getFeedEntries("http://testEntriesToShowAndCaching", 2)).andReturn(generateFeedEntries(5));
        replay(_feedDao);
        _tag.doTag();
        verify(_feedDao);
        String output = getJspContextOutput();
        assertTrue(output.indexOf("http://post2") > -1);
        assertFalse(output.indexOf("http://post3") > -1);
    }

    public void testGettingNoFeedEntries() throws Exception {
        _tag.setFeedUrl("http://testNoEntriesToShowAndCaching"); // cache key
        _tag.setNumberOfEntriesToShow(2);
        expect(_feedDao.getFeedEntries("http://testNoEntriesToShowAndCaching", 2)).andReturn(generateFeedEntries(0));
        replay(_feedDao);
        _tag.doTag();
        verify(_feedDao);
        String output = getJspContextOutput();
        assertEquals("", output);
    }

    public void testOnClick() throws Exception {
        _tag.setFeedUrl("http://testOnClick"); // cache key
        _tag.setNumberOfEntriesToShow(1);
        expect(_feedDao.getFeedEntries("http://testOnClick", 1)).andReturn(generateFeedEntries(1));
        replay(_feedDao);
        _tag.setOnClick("foo(); return true;");
        _tag.doTag();
        verify(_feedDao);
        String output = getJspContextOutput();
        assertEquals("<ol><li><div><a onclick=\"foo(); return true;\" href=\"http://post1\">Post 1</a></div></li></ol>", output);
    }

    public void testAbbreviatingLongFeedEntries() throws Exception {
        // First test for no abbreviation
        _tag.setFeedUrl("http://testEntryAbbreviation"); // cache key
        _tag.setNumberOfEntriesToShow(1);
        expect(_feedDao.getFeedEntries("http://testEntryAbbreviation", 1))
                .andReturn(new ArrayList<SyndEntry>() { {
                        add(new SyndEntryImpl() { {
                                setTitle("Very Long Title That Should Be Abbreviated");
                                setLink("http://post");
                            }
                        });
                    }
                });
        replay(_feedDao);
        _tag.doTag();
        verify(_feedDao);
        String output = getJspContextOutput();
        assertEquals("No Abbreviation Expected",
                "<ol><li><div><a href=\"http://post\">Very Long Title That Should Be Abbreviated</a></div></li></ol>",
                output);

        reset(_feedDao);
        expect(_feedDao.getFeedEntries("http://testEntryAbbreviation", 1))
                .andReturn(new ArrayList<SyndEntry>() { {
                        add(new SyndEntryImpl() { {
                                setTitle("Very Long Title That Should Be Abbreviated");
                                setLink("http://post");
                            }
                        });
                    }
                });
        replay(_feedDao);
        // Now abbreviate it
        resetJspContext();
        _tag.setNumberOfCharactersPerEntryToShow(12);
        _tag.doTag();
        output = getJspContextOutput();
        assertEquals("Abbreviation Expected",
                "<ol><li><div><a href=\"http://post\">Very Long...</a></div></li></ol>",
                output);
    }

    /**
     * A more easily testable version of the tag handler
     */
    public class FeedTagHandlerTestCase extends FeedTagHandler {

        private JspContext _jspContext;

        public JspContext getJspContext() {
            return _jspContext;
        }

        public void setJspContext(JspContext jspContext) {
            _jspContext = jspContext;
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