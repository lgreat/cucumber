package gs.web.pagination;

import gs.data.pagination.PaginationConfig;
import gs.data.util.ListUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class PagerTest {
    PaginationConfig paginationConfig = new PaginationConfig("pageSize", "page", "start", 25, 25, true, false);
    Pager _pager;

    @Before
    public void setUp() {

    }

    @Test
    public void testGetPageSequence() throws Exception {
        _pager = new Pager(100, 25, paginationConfig);
        assertTrue(sequenceMatches(new int[]{1,2,3}, _pager.getPageSequence(1)));
        assertTrue(sequenceMatches(new int[]{1,2,3}, _pager.getPageSequence(2)));
        assertTrue(sequenceMatches(new int[]{2,3,4}, _pager.getPageSequence(3)));
        assertTrue(sequenceMatches(new int[]{2,3,4}, _pager.getPageSequence(4)));

        _pager = new Pager(125, 25, paginationConfig);
        assertTrue(sequenceMatches(new int[]{1,2,3}, _pager.getPageSequence(1)));
        assertTrue(sequenceMatches(new int[]{1,2,3}, _pager.getPageSequence(2)));
        assertTrue(sequenceMatches(new int[]{2,3,4}, _pager.getPageSequence(3)));
        assertTrue(sequenceMatches(new int[]{3,4,5}, _pager.getPageSequence(4)));
        assertTrue(sequenceMatches(new int[]{3,4,5}, _pager.getPageSequence(5)));

    }

    public boolean sequenceMatches(int[] pageNumbers, List<Page> pages) {
        int pos = 0;
        for (Page page : pages) {
            if (page.getPageNumber() != pageNumbers[pos++]) {
                return false;
            }
        }
        return true;
    }
}
