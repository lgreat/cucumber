package gs.web.util;

import gs.web.BaseControllerTestCase;
import gs.data.util.CachedItem;
import gs.data.util.table.ITableRow;

import java.util.Map;
import java.util.List;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ClearTableDaoCacheControllerTest extends BaseControllerTestCase {
    private ClearTableDaoCacheController _controller;
    private Map<String, CachedItem<List<ITableRow>>> _cache;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new ClearTableDaoCacheController();
        ClearTableDaoCacheController.InformativeCachedDao dao =
                new ClearTableDaoCacheController.InformativeCachedDao();
        _cache = dao.getCache();
        _cache.clear();
    }

    public void testClear() throws Exception {
        CachedItem<List<ITableRow>> item = new CachedItem<List<ITableRow>>();
        _cache.put("key", item);
        assertEquals(1, _cache.size());

        _controller.handleRequest(getRequest(), getResponse());

        assertTrue("Expect cache to be cleared", _cache.isEmpty());
    }

    public void testClearDebug() throws Exception {
        getRequest().addParameter("debug", "1");
        CachedItem<List<ITableRow>> item = new CachedItem<List<ITableRow>>();
        _cache.put("key", item);
        assertEquals(1, _cache.size());

        _controller.handleRequest(getRequest(), getResponse());

        assertFalse("Expect cache not to have been cleared", _cache.isEmpty());
        assertEquals(1, _cache.size());
    }

    public void testClearDebugEmptyCache() throws Exception {
        // no crash
        getRequest().addParameter("debug", "1");
        assertTrue(_cache.isEmpty());

        _controller.handleRequest(getRequest(), getResponse());

        assertTrue(_cache.isEmpty());
    }
}
