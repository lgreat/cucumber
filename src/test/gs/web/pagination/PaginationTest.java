package gs.web.pagination;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PaginationTest {
    @Test
    public void testGetNumberOfPages() throws Exception {

        assertEquals(2, Pagination.getNumberOfPages(10, 15));
        assertEquals(1, Pagination.getNumberOfPages(10, 10));
        assertEquals(2, Pagination.getNumberOfPages(10, 11));
        assertEquals(1, Pagination.getNumberOfPages(10, 9));
        assertEquals(1, Pagination.getNumberOfPages(10, 1));
        assertEquals(1, Pagination.getNumberOfPages(1, 1));

        assertEquals(0, Pagination.getNumberOfPages(10, 0));
        assertEquals(0, Pagination.getNumberOfPages(0, 1));
        /*
        try {
            Pagination.getNumberOfPages(0, 1);
            fail();
        } catch (Exception e) {
            //ok
        }*/
    }

    @Test
    public void testGetPageNumber() throws Exception {
        assertEquals(0, Pagination.getPageNumber(10, 3, true, true));
        assertEquals(1, Pagination.getPageNumber(10, 3, true, false));
        assertEquals(0, Pagination.getPageNumber(10, 3, false, true));
        assertEquals(1, Pagination.getPageNumber(10, 3, false, false));

        assertEquals(1, Pagination.getPageNumber(10, 10, true, true));
        assertEquals(2, Pagination.getPageNumber(10, 10, true, false));
        assertEquals(0, Pagination.getPageNumber(10, 10, false, true));
        assertEquals(1, Pagination.getPageNumber(10, 10, false, false));
        assertEquals(1, Pagination.getPageNumber(10, 11, false, true));
        assertEquals(2, Pagination.getPageNumber(10, 11, false, false));

        assertEquals(0, Pagination.getPageNumber(10, 9, true, true));
        assertEquals(1, Pagination.getPageNumber(10, 9, true, false));
        assertEquals(0, Pagination.getPageNumber(10, 9, false, true));
        assertEquals(1, Pagination.getPageNumber(10, 9, false, false));
    }

    @Test
    public void testGetOffset() throws Exception {
        assertEquals(30, Pagination.getOffset(10, 3, true, true));
        assertEquals(20, Pagination.getOffset(10, 3, true, false));
        assertEquals(31, Pagination.getOffset(10, 3, false, true));
        assertEquals(21, Pagination.getOffset(10, 3, false, false));

        assertEquals(10, Pagination.getOffset(10, 1, true, true));
        assertEquals(0, Pagination.getOffset(10, 1, true, false));
        assertEquals(11, Pagination.getOffset(10, 1, false, true));
        assertEquals(1, Pagination.getOffset(10, 1, false, false));
    }

}
