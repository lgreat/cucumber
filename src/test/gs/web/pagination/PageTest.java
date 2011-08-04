package gs.web.pagination;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

public class PageTest {

    @Test
    public void testPageAhead() {
        Pager pager = createStrictMock(Pager.class);

        expect(pager.getPageNumber(eq(0))).andReturn(1); //index 0 is the first item on page 1
        expect(pager.getPageSize()).andReturn(4);
        expect(pager.getLastOffset()).andReturn(5); // 6 results, 2 pages

        //when the method constructs a new Page:
        expect(pager.getPageNumber(eq(4))).andReturn(2);

        replay(pager);

        Page page = new Page(pager, 0); //get a page with results 3-5
        Page nextPage = page.pageAhead(1);

        verify(pager);
        assertEquals(4,nextPage.getOffset());
        assertEquals(2, nextPage.getPageNumber());
    }

    @Test
    public void testPageAheadWhenNextPageIsPartialPage() {
        Pager pager = createStrictMock(Pager.class);

        expect(pager.getPageNumber(eq(3))).andReturn(1); //index 3 is the last item on page 1
        expect(pager.getPageSize()).andReturn(4);
        expect(pager.getLastOffset()).andReturn(5); // 6 results, 2 pages
        expect(pager.getLastPage()).andReturn(null);

        replay(pager);

        Page page = new Page(pager, 3); //get a page with results 3-5
        Page nextPage = page.pageAhead(1);

        verify(pager);
        assertNull(nextPage);
    }
}
