package gs.web.util;

import junit.framework.TestCase;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CrawlerTest extends TestCase {

    public void testCrawler () throws Exception {
        Crawler crawler = new Crawler();
        TestVisitor tv = new TestVisitor();
        crawler.addPageVisitor(tv);
        crawler.crawl("http://www.greatschools.com", 10);
        assertEquals(10, tv.visitedCount);
    }
}

class TestVisitor implements IPageVisitor {

    int visitedCount = 0;
    public void visit(Page page) {
        visitedCount++;
        System.out.println ("visiting" + page.getUrl());
    }
}