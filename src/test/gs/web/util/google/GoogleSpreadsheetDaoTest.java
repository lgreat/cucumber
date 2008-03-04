package gs.web.util.google;

import gs.web.BaseTestCase;
import gs.data.util.CachedItem;
import gs.data.util.table.ITableRow;

import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.util.ServiceException;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class GoogleSpreadsheetDaoTest extends BaseTestCase {

    private GoogleSpreadsheetDao _dao;

    public void setUp() throws Exception {
        super.setUp();

        String worksheetUrl =
                "http://spreadsheets.google.com/feeds/worksheets/pmY-74KD4CbXrSKtrPdEnSg/public/values/od7";
        _dao = new GoogleSpreadsheetDao(worksheetUrl);
        _dao.clearCache();
    }

    public void testGetFirstRowByKey() {
        ITableRow row = _dao.getFirstRowByKey("code", "school/rating.page");

        assertEquals("Other than the education one receives - what makes a school \"great\"?",
                row.get("text"));
        assertEquals("http://community.greatschools.net/q-and-a/123301/Other-than-the-education-one-receives-what-makes-a-school-great",
                row.get("link"));
        assertEquals("Queserasera", row.get("username"));
        assertEquals("2093577", row.get("memberid"));

        row = _dao.getFirstRowByKey("code", "school/parentReviews.page");

        assertEquals("What's the difference?", row.get("text"));
        assertEquals("http://community.greatschools.net/q-and-a", row.get("link"));
        assertEquals("Anthony", row.get("username"));
        assertEquals("1", row.get("memberid"));
    }

    public void testGetRowsByKey() {
        List<ITableRow> rows = _dao.getRowsByKey("code", "test/rowsByKey");

        assertEquals(2, rows.size());

        ITableRow row = rows.get(0);
        assertEquals("2", row.get("memberid"));
        row = rows.get(1);
        assertEquals("3", row.get("memberid"));

        rows = _dao.getRowsByKey("code", "school/rating.page");

        assertEquals(1, rows.size());

        row = rows.get(0);
        assertEquals("2093577", row.get("memberid"));
    }

    public void testGetFirstRowBadValue() {
        ITableRow row = _dao.getFirstRowByKey("code", "lala");

        assertNull(row);
    }

    public void testGetAllRows() {
        List<ITableRow> rows = _dao.getAllRows();

        assertEquals(4, rows.size());
    }

    public void testGetListFeedExceptions() throws IOException, ServiceException {
        SpreadsheetService service = createMock(SpreadsheetService.class);
        ListFeed listFeed;
        _dao.setSpreadsheetService(service);

        expect(service.getEntry(isA(URL.class), eq(WorksheetEntry.class))).andThrow(new MalformedURLException());
        replay(service);

        listFeed = _dao.getListFeed();

        verify(service);
        assertNull(listFeed);

        reset(service);
        expect(service.getEntry(isA(URL.class), eq(WorksheetEntry.class))).andThrow(new IOException());
        replay(service);

        listFeed = _dao.getListFeed();

        verify(service);
        assertNull(listFeed);

        reset(service);
        expect(service.getEntry(isA(URL.class), eq(WorksheetEntry.class))).andThrow(new ServiceException("Testing ServiceException"));
        replay(service);

        listFeed = _dao.getListFeed();

        verify(service);
        assertNull(listFeed);
    }

    public void testGetListFeedWithCredentials()  throws IOException, ServiceException {
        String worksheetUrl =
                "http://spreadsheets.google.com/feeds/worksheets/pmY-74KD4CbXrSKtrPdEnSg/public/values/od7";
        _dao = new GoogleSpreadsheetDao(worksheetUrl, "user", "pass");

        SpreadsheetService service = createMock(SpreadsheetService.class);
        WorksheetEntry dataWorksheet = createMock(WorksheetEntry.class);
        ListFeed listFeed = new ListFeed();

        _dao.setSpreadsheetService(service);

        service.setUserCredentials("user", "pass");
        expect(service.getEntry(isA(URL.class), eq(WorksheetEntry.class))).andReturn(dataWorksheet);

        URL url = new URL("http://dev.greatschools.net");
        expect(dataWorksheet.getListFeedUrl()).andReturn(url);
        replay(dataWorksheet);

        expect(service.getFeed(url, ListFeed.class)).andReturn(listFeed);
        replay(service);

        ListFeed rval = _dao.getListFeed();

        verify(service);
        verify(dataWorksheet);
        assertNotNull(rval);
        assertSame(listFeed, rval);
    }

//    public void testGetFromCache() {
//        assertTrue(_cache.isEmpty());
//
//        assertNull("Expect no results when cache is empty", _dao.getFromCache("key"));
//
//        long currentTime = System.currentTimeMillis();
//        long fiveMinsAgo = currentTime - 300000;
//        long tenMins = 600000;
//        long twoMins = 120000;
//
//        CachedItem<List<ITableRow>> item = new CachedItem<List<ITableRow>>();
//        item.setCachedAt(fiveMinsAgo);
//        item.setCacheDuration(tenMins);
//        List<ITableRow> rows = new ArrayList<ITableRow>();
//        item.setData(rows);
//
//        _cache.put("key", item);
//
//        List<ITableRow> rval = _dao.getFromCache("key");
//
//        assertNotNull(rval);
//        assertSame(rows, rval);
//
//        item.setCacheDuration(twoMins);
//
//        rval = _dao.getFromCache("key");
//
//        assertNull("Expect null because cache expired", rval);
//    }
//
//    public void testPutIntoCache() {
//        assertTrue(_cache.isEmpty());
//
//        List<ITableRow> rows = new ArrayList<ITableRow>();
//        long twoMins = 120000;
//
//        _dao.putIntoCache("key", rows, twoMins);
//
//
//        assertFalse(_cache.isEmpty());
//        assertEquals(1, _cache.size());
//        CachedItem<List<ITableRow>> item = _cache.get("key");
//
//        assertEquals(twoMins, item.getCacheDuration());
//        assertSame(rows, item.getData());
//        assertNotNull(item.getDataIfNotExpired());
//    }
//
//    public void testClearCacheKey() {
//        assertTrue(_cache.isEmpty());
//
//        long currentTime = System.currentTimeMillis();
//        long fiveMinsAgo = currentTime - 300000;
//        long tenMins = 600000;
//
//        CachedItem<List<ITableRow>> item1 = new CachedItem<List<ITableRow>>();
//        item1.setCachedAt(fiveMinsAgo);
//        item1.setCacheDuration(tenMins);
//        List<ITableRow> rows1 = new ArrayList<ITableRow>();
//        item1.setData(rows1);
//
//        CachedItem<List<ITableRow>> item2 = new CachedItem<List<ITableRow>>();
//        item2.setCachedAt(fiveMinsAgo);
//        item2.setCacheDuration(tenMins);
//        List<ITableRow> rows2 = new ArrayList<ITableRow>();
//        item2.setData(rows2);
//
//        _cache.put("key1", item1);
//        _cache.put("key2", item2);
//
//        assertNotNull(_cache.get("key1"));
//        assertNotNull(_cache.get("key2"));
//
//        _dao.clearCacheKey("key2");
//        assertNotNull(_cache.get("key1"));
//        assertNull(_cache.get("key2"));
//
//        _dao.clearCacheKey("key1");
//        assertNull(_cache.get("key1"));
//        assertNull(_cache.get("key2"));
//
//        assertTrue(_cache.isEmpty());
//    }
//
//    public void testClearCache() {
//        assertTrue(_cache.isEmpty());
//
//        long currentTime = System.currentTimeMillis();
//        long fiveMinsAgo = currentTime - 300000;
//        long tenMins = 600000;
//
//        CachedItem<List<ITableRow>> item1 = new CachedItem<List<ITableRow>>();
//        item1.setCachedAt(fiveMinsAgo);
//        item1.setCacheDuration(tenMins);
//        List<ITableRow> rows1 = new ArrayList<ITableRow>();
//        item1.setData(rows1);
//
//        CachedItem<List<ITableRow>> item2 = new CachedItem<List<ITableRow>>();
//        item2.setCachedAt(fiveMinsAgo);
//        item2.setCacheDuration(tenMins);
//        List<ITableRow> rows2 = new ArrayList<ITableRow>();
//        item2.setData(rows2);
//
//        _cache.put("key1", item1);
//        _cache.put("key2", item2);
//
//        assertNotNull(_cache.get("key1"));
//        assertNotNull(_cache.get("key2"));
//
//        _dao.clearCache();
//        assertNull(_cache.get("key1"));
//        assertNull(_cache.get("key2"));
//
//        assertTrue(_cache.isEmpty());
//    }
}
