package gs.web.util.google;

import gs.web.BaseTestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CachedGoogleSpreadsheetDaoTest extends BaseTestCase {
    private CachedGoogleSpreadsheetDao _dao;

    public void setUp() throws Exception {
        super.setUp();

        _dao = new CachedGoogleSpreadsheetDao();
    }

    public void testBasics() {
        assertNotNull(CachedGoogleSpreadsheetDao._rowCache);
        assertEquals("Expect HOUR to equal 1 hour in milliseconds",
                1, CachedGoogleSpreadsheetDao.HOUR / 60 / 60 / 1000);
        assertEquals("Expect DAY to equal 1 day in milliseconds",
                1, CachedGoogleSpreadsheetDao.DAY / 24 / 60 / 60 / 1000);

        _dao.clearCache();
        assertTrue(CachedGoogleSpreadsheetDao._rowCache.isEmpty());
        CachedGoogleSpreadsheetDao.GoogleSpreadsheetCachedItem item =
                new CachedGoogleSpreadsheetDao.GoogleSpreadsheetCachedItem();
        CachedGoogleSpreadsheetDao._rowCache.put("url:col:cell", item);
        assertFalse(CachedGoogleSpreadsheetDao._rowCache.isEmpty());
        _dao.clearCache();
        assertTrue(CachedGoogleSpreadsheetDao._rowCache.isEmpty());
        
    }

    public void testGetDataFromRow() {
        CachedGoogleSpreadsheetDao.GoogleSpreadsheetCachedItem item =
                new CachedGoogleSpreadsheetDao.GoogleSpreadsheetCachedItem();
        Map<String, String> dataMap = new HashMap<String, String>();
        dataMap.put("key", "value");
        item.setCacheDuration(60000);
        item.setCacheTime(System.currentTimeMillis() - 10000);
        item.setData(dataMap);
        CachedGoogleSpreadsheetDao._rowCache.put("url:col:cell", item);
        Map<String, String> returnedMap = _dao.getDataFromRow("url", "col", "cell",
                ICachedGoogleSpreadsheetDao.HOUR);

        assertNotNull("Expect cache to still contain item",
                CachedGoogleSpreadsheetDao._rowCache.get("url:col:cell"));
        assertFalse(returnedMap.isEmpty());
        assertSame("Expect item from cache to equal item put into cache", dataMap, returnedMap);

        item.setCacheTime(System.currentTimeMillis() - 60001);
        returnedMap = _dao.getDataFromRow("url", "col", "cell",
                ICachedGoogleSpreadsheetDao.HOUR);
        assertTrue("Expect invalid parameters to return empty map", returnedMap.isEmpty());
        assertNotSame("Expect returned map to not be from cache", dataMap, returnedMap);
        assertNull("Expect cache expiration to remove item from cache",
                CachedGoogleSpreadsheetDao._rowCache.get("url:col:cell"));
    }
}
