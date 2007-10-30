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
import java.util.HashMap;

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
        _tag.setState(State.WV);
        _newsItemDao.findNewsItemForState(ResearchAndCompareNewsBlurbTagHandler.CATEGORY, State.WV);
        _newsItemControl.setReturnValue(null);
        _newsItemControl.replay();
        _schoolDao.countSchools(State.WV, null, null, null);
        _schoolControl.setReturnValue(2244);
        _schoolControl.replay();
        _tag.doTag();
        _newsItemControl.verify();
        _schoolControl.verify();

        MockJspWriter writer = (MockJspWriter)_context.getOut();

        StringBuffer output = writer.getOutputBuffer();
        assertNotNull(output);
        assertTrue(output.length() > 0);
        assertTrue(output.indexOf("http://data.greatschools.net/west_virginia/index.html") > -1);
        assertTrue(output.indexOf("2,200") > -1);
        assertTrue(output.indexOf("charter") < 0);
    }

    public void testDoTagDCNullItem() throws IOException {
        _tag.setState(State.DC);
        _newsItemDao.findNewsItemForState(ResearchAndCompareNewsBlurbTagHandler.CATEGORY, State.DC);
        _newsItemControl.setReturnValue(null);
        _newsItemControl.replay();
        _schoolDao.countSchools(State.DC, null, null, null);
        _schoolControl.setReturnValue(350);
        _schoolControl.replay();
        _tag.doTag();
        _newsItemControl.verify();
        _schoolControl.verify();

        MockJspWriter writer = (MockJspWriter)_context.getOut();

        StringBuffer output = writer.getOutputBuffer();
        System.out.println ("output:\n" + output);
        assertNotNull(output);
        assertTrue(output.length() > 0);
        assertTrue(output.indexOf("http://data.greatschools.net/district_of_columbia/index.html") > -1);
        assertTrue(output.indexOf("300") > -1);
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

    public void testRoundSchoolsToNearest() {
        assertEquals(150, _tag.roundToNearest(150, 50));
        assertEquals(150, _tag.roundToNearest(151, 50));
        assertEquals(150, _tag.roundToNearest(199, 50));
        assertEquals(200, _tag.roundToNearest(200, 50));

        assertEquals(200, _tag.roundToNearest(200, 100));
        assertEquals(200, _tag.roundToNearest(201, 100));
        assertEquals(200, _tag.roundToNearest(299, 100));
        assertEquals(195, _tag.roundToNearest(200, 13));

        assertEquals(0, _tag.roundToNearest(0, 50));
        assertEquals(217, _tag.roundToNearest(217, 0));
        assertEquals(217, _tag.roundToNearest(217, 1));
        assertEquals(216, _tag.roundToNearest(217, 2));

    }

    public void testPrintFooter() throws IOException {
        _tag.setStateOwners(new HashMap() {{
                put(State.AK, ResearchAndCompareNewsBlurbTagHandler.ELIZABETH_GARDNER);
                put(State.AL, ResearchAndCompareNewsBlurbTagHandler.ALASTAIR_BROWN);
        }});

        _tag.setState(State.AK);
        MockJspWriter writer = new MockJspWriter();
        _tag.printFooter(writer);

        StringBuffer output = writer.getOutputBuffer();
        assertTrue("Expected " + ResearchAndCompareNewsBlurbTagHandler.ELIZABETH_GARDNER + " for AK",
                output.toString().indexOf(ResearchAndCompareNewsBlurbTagHandler.ELIZABETH_GARDNER) != -1);
        assertTrue("Didn't expect " + ResearchAndCompareNewsBlurbTagHandler.ALASTAIR_BROWN + " for AK",
                output.toString().indexOf(ResearchAndCompareNewsBlurbTagHandler.ALASTAIR_BROWN) == -1);

        _tag.setState(State.AL);
        writer = new MockJspWriter();
        _tag.printFooter(writer);

        output = writer.getOutputBuffer();
        assertTrue("Didn't expect " + ResearchAndCompareNewsBlurbTagHandler.ELIZABETH_GARDNER + " for AL",
                output.toString().indexOf(ResearchAndCompareNewsBlurbTagHandler.ELIZABETH_GARDNER) == -1);
        assertTrue("Expected " + ResearchAndCompareNewsBlurbTagHandler.ALASTAIR_BROWN + " for AL",
                output.toString().indexOf(ResearchAndCompareNewsBlurbTagHandler.ALASTAIR_BROWN) != -1);

        _tag.setState(null);
        writer = new MockJspWriter();
        _tag.printFooter(writer);

        output = writer.getOutputBuffer();
        assertTrue("Expected " + ResearchAndCompareNewsBlurbTagHandler.ELIZABETH_GARDNER + " for null state",
                output.toString().indexOf(ResearchAndCompareNewsBlurbTagHandler.ELIZABETH_GARDNER) != -1);
        assertTrue("Expected " + ResearchAndCompareNewsBlurbTagHandler.ALASTAIR_BROWN + " for null state",
                output.toString().indexOf(ResearchAndCompareNewsBlurbTagHandler.ALASTAIR_BROWN) != -1);

    }
}
