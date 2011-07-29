package gs.web.pagination;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class RequestedPageTest {
    @Test
    public void testGetValidatedOffset() throws Exception {
        String pageSizeParam = "pageSize";
        String pageNumberParam = "p";
        String offsetParam = "offset";
        int defaultPageSize = 20;
        int maxPageSize = 100;
        boolean zeroBasedOffset = true;
        boolean zeroBasedPages = false;

        PaginationConfig paginationConfig = new PaginationConfig(pageSizeParam, pageNumberParam, offsetParam, defaultPageSize, maxPageSize, zeroBasedOffset, zeroBasedPages) {};

        RequestedPage page = new RequestedPage(9999, 1, 20);
        assertEquals(0, page.getValidatedOffset(paginationConfig, 3));

        page = new RequestedPage(3, 1, 20);
        assertEquals(0, page.getValidatedOffset(paginationConfig, 3));

        page = new RequestedPage(100, 1, 20);
        assertEquals(80, page.getValidatedOffset(paginationConfig, 100));

        page = new RequestedPage(101, 1, 20);
        assertEquals(80, page.getValidatedOffset(paginationConfig, 100));

        page = new RequestedPage(4, 1, 20);
        assertEquals(0, page.getValidatedOffset(paginationConfig, 3));
        
        page = new RequestedPage(2, 1, 20);
        assertEquals(2, page.getValidatedOffset(paginationConfig, 3));

        page = new RequestedPage(99, 1, 20);
        assertEquals(99, page.getValidatedOffset(paginationConfig, 100));

        page = new RequestedPage(9999, 1, 20);
        assertEquals(100, page.getValidatedOffset(paginationConfig, 110));

        page = new RequestedPage(2, 1, 20);
        assertEquals(0, page.getValidatedOffset(paginationConfig, 1));

        zeroBasedOffset = false;
        paginationConfig = new PaginationConfig(pageSizeParam, pageNumberParam, offsetParam, defaultPageSize, maxPageSize, zeroBasedOffset, zeroBasedPages) {};

        page = new RequestedPage(9999, 1, 20);
        assertEquals(1, page.getValidatedOffset(paginationConfig, 3));

        page = new RequestedPage(100, 1, 20);
        assertEquals(100, page.getValidatedOffset(paginationConfig, 100));

        page = new RequestedPage(101, 1, 20);
        assertEquals(81, page.getValidatedOffset(paginationConfig, 100));

        page = new RequestedPage(3, 1, 20);
        assertEquals(3, page.getValidatedOffset(paginationConfig, 3));

        page = new RequestedPage(4, 1, 20);
        assertEquals(1, page.getValidatedOffset(paginationConfig, 3));

        page = new RequestedPage(2, 1, 20);
        assertEquals(2, page.getValidatedOffset(paginationConfig, 3));

        page = new RequestedPage(2, 1, 20);
        assertEquals(1, page.getValidatedOffset(paginationConfig, 1));

    }
}
