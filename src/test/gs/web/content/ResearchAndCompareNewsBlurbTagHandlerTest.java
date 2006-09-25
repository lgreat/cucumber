package gs.web.content;

import junit.framework.TestCase;
import gs.web.jsp.MockPageContext;
import gs.web.jsp.MockJspWriter;
import gs.data.content.INewsItemDao;
import gs.data.content.NewsItem;
import gs.data.school.ISchoolDao;
import gs.data.state.State;

import javax.servlet.jsp.PageContext;
import java.io.IOException;

import org.easymock.MockControl;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ResearchAndCompareNewsBlurbTagHandlerTest extends TestCase {
    private ResearchAndCompareNewsBlurbTagHandler _tag;
    private INewsItemDao _newsItemDao;
    private MockControl _newsItemControl;
    private ISchoolDao _schoolDao;
    private MockControl _schoolControl;
    private MockPageContext _context;

    protected void setUp() throws Exception {
        super.setUp();
        _tag = new ResearchAndCompareNewsBlurbTagHandler();
        _newsItemControl = MockControl.createControl(INewsItemDao.class);
        _newsItemDao = (INewsItemDao) _newsItemControl.getMock();
        _schoolControl = MockControl.createControl(ISchoolDao.class);
        _schoolDao = (ISchoolDao) _schoolControl.getMock();

        _tag.setNewsItemDao(_newsItemDao);
        _tag.setSchoolDao(_schoolDao);
        _context = new MockPageContext();
        _context.setAttribute(PageContext.PAGECONTEXT, _context);
        _tag.setJspContext(_context);
    }

    public void testDoTagNullAll() throws IOException {
        _newsItemDao.findNewsItemForState(ResearchAndCompareNewsBlurbTagHandler.CATEGORY, null);
        _newsItemControl.setReturnValue(null);
        _newsItemControl.replay();
        _schoolControl.replay();
        _tag.doTag();
        _newsItemControl.verify();
        _schoolControl.verify();

        MockJspWriter writer = (MockJspWriter)_context.getOut();

        StringBuffer output = writer.getOutputBuffer();
        assertNotNull(output);
        assertTrue(output.length() > 0);
    }

    public void testDoTagNullItem() throws IOException {
        _tag.setState(State.CA);
        _newsItemDao.findNewsItemForState(ResearchAndCompareNewsBlurbTagHandler.CATEGORY, State.CA);
        _newsItemControl.setReturnValue(null);
        _newsItemControl.replay();
        _schoolDao.countSchools(State.CA, null, null, null);
        _schoolControl.setReturnValue(13224);
        _schoolControl.replay();
        _tag.doTag();
        _newsItemControl.verify();
        _schoolControl.verify();

        MockJspWriter writer = (MockJspWriter)_context.getOut();

        StringBuffer output = writer.getOutputBuffer();
        assertNotNull(output);
        assertTrue(output.length() > 0);
        assertTrue(output.indexOf("132") > -1);
        assertTrue(output.indexOf("charter") > -1);
    }

    public void testDoTag() throws IOException {
        _tag.setState(State.CA);
        NewsItem newsItem = new NewsItem();
        newsItem.setLink("/here/there");
        newsItem.setTitle("Title");
        newsItem.setText("Text line 1\nText line 2");
        _newsItemDao.findNewsItemForState(ResearchAndCompareNewsBlurbTagHandler.CATEGORY, State.CA);
        _newsItemControl.setReturnValue(newsItem);
        _newsItemControl.replay();
        _schoolControl.replay();
        _tag.doTag();
        _newsItemControl.verify();
        _schoolControl.verify();

        MockJspWriter writer = (MockJspWriter)_context.getOut();

        StringBuffer output = writer.getOutputBuffer();
        assertNotNull(output);
        assertTrue(output.length() > 0);
        assertTrue(output.indexOf("/here/there") > -1);
        assertTrue(output.indexOf("Title") > -1);
        assertTrue(output.indexOf("Text line 1") > -1);
        assertTrue(output.indexOf("Text line 2") > -1);
    }
}
