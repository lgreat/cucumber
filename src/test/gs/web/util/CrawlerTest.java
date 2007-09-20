package gs.web.util;

import junit.framework.TestCase;
import static org.easymock.classextension.EasyMock.*;

import java.net.URL;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CrawlerTest extends TestCase {

    public void testPageReaderNotNullByDefault() {
        Crawler crawler = new Crawler();
        assertNotNull("Page reader should not be null", crawler.getPageReader());
    }
    
    public void testCrawlerRespectsPageLimit () throws Exception {
        Crawler.PageReader pageReader = createMock(Crawler.PageReader.class);
        String tenLinks = "<a href=\"/1\">1</a>" +
                "<a href=\"/2\">1</a>" +
                "<a href=\"/3\">1</a>" +
                "<a href=\"/4\">1</a>" +
                "<a href=\"/5\">1</a>" +
                "<a href=\"/6\">1</a>" +
                "<a href=\"/7\">1</a>" +
                "<a href=\"/8\">1</a>" +
                "<a href=\"/9\">1</a>" +
                "<a href=\"/10\">1</a>";
        expect(pageReader.getContentAsString((URL) anyObject())).andReturn(tenLinks).times(5);
        replay(pageReader);

        Crawler crawler = new Crawler();
        crawler.setPageReader(pageReader);
        
        TestVisitor tv = new TestVisitor();
        crawler.addPageVisitor(tv);
        crawler.crawl("http://www.greatschools.com", 5);
        assertEquals(5, tv.visitedCount);
    }
}

class TestVisitor implements IPageVisitor {

    int visitedCount = 0;
    public void visit(Page page) {
        visitedCount++;
        System.out.println ("visiting" + page.getUrl());
    }
}