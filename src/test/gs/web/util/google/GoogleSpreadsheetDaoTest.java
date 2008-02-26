package gs.web.util.google;

import gs.web.BaseTestCase;
import gs.web.util.CachedItem;

import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.util.ServiceException;

import static gs.web.util.google.GoogleSpreadsheetDao._cache;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class GoogleSpreadsheetDaoTest extends BaseTestCase {

    private GoogleSpreadsheetDao _dao;

    public void setUp() throws Exception {
        super.setUp();

        _cache.clear();
        String worksheetUrl =
                "http://spreadsheets.google.com/feeds/worksheets/pmY-74KD4CbXrSKtrPdEnSg/public/values/od7";
        _dao = new GoogleSpreadsheetDao(worksheetUrl);
    }

    public void testGetFirstRowByKey() {
        assertTrue(_cache.isEmpty());
        SpreadsheetRow row = _dao.getFirstRowByKey("code", "school/rating.page");

        assertEquals("Other than the education one receives - what makes a school \"great\"?",
                row.getCell("text"));
        assertEquals("http://community.greatschools.net/q-and-a/123301/Other-than-the-education-one-receives-what-makes-a-school-great",
                row.getCell("link"));
        assertEquals("Queserasera", row.getCell("username"));
        assertEquals("2093577", row.getCell("memberid"));

        assertEquals(1, _cache.size());

        row = _dao.getFirstRowByKey("code", "school/parentReviews.page");

        assertEquals("What's the difference?", row.getCell("text"));
        assertEquals("http://community.greatschools.net/q-and-a", row.getCell("link"));
        assertEquals("Anthony", row.getCell("username"));
        assertEquals("1", row.getCell("memberid"));

        assertEquals(2, _cache.size());
    }

    public void testGetRowsByKey() {
        assertTrue(_cache.isEmpty());
        List<SpreadsheetRow> rows = _dao.getRowsByKey("code", "test/rowsByKey");

        assertEquals(2, rows.size());
        assertEquals(1, _cache.size());

        SpreadsheetRow row = rows.get(0);
        assertEquals("2", row.getCell("memberid"));
        row = rows.get(1);
        assertEquals("3", row.getCell("memberid"));

        rows = _dao.getRowsByKey("code", "school/rating.page");

        assertEquals(1, rows.size());
        assertEquals(2, _cache.size());

        row = rows.get(0);
        assertEquals("2093577", row.getCell("memberid"));
    }

    public void testGetFirstRowBadValue() {
        assertTrue(_cache.isEmpty());
        SpreadsheetRow row = _dao.getFirstRowByKey("code", "lala");

        assertNull(row);
        assertTrue(_cache.isEmpty());
    }

    public void testGetAllRows() {
        assertTrue(_cache.isEmpty());
        List<SpreadsheetRow> rows = _dao.getAllRows();

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

    public void testGetFromCache() {
        assertTrue(_cache.isEmpty());

        assertNull("Expect no results when cache is empty", _dao.getFromCache("key"));

        long currentTime = System.currentTimeMillis();
        long fiveMinsAgo = currentTime - 300000;
        long tenMins = 600000;
        long twoMins = 120000;

        CachedItem<List<SpreadsheetRow>> item = new CachedItem<List<SpreadsheetRow>>();
        item.setCachedAt(fiveMinsAgo);
        item.setCacheDuration(tenMins);
        List<SpreadsheetRow> rows = new ArrayList<SpreadsheetRow>();
        item.setData(rows);

        _cache.put("key", item);

        List<SpreadsheetRow> rval = _dao.getFromCache("key");

        assertNotNull(rval);
        assertSame(rows, rval);

        item.setCacheDuration(twoMins);

        rval = _dao.getFromCache("key");

        assertNull("Expect null because cache expired", rval);
        assertTrue("Expect cache to have been cleared", _cache.isEmpty());
    }

    public void testPutIntoCache() {
        assertTrue(_cache.isEmpty());

        List<SpreadsheetRow> rows = new ArrayList<SpreadsheetRow>();
        long twoMins = 120000;

        _dao.putIntoCache("key", rows, twoMins);


        assertFalse(_cache.isEmpty());
        assertEquals(1, _cache.size());
        CachedItem<List<SpreadsheetRow>> item = _cache.get("key");

        assertEquals(twoMins, item.getCacheDuration());
        assertSame(rows, item.getData());
        assertNotNull(item.getDataIfNotExpired());
    }

    public void testClearCacheKey() {
        assertTrue(_cache.isEmpty());

        long currentTime = System.currentTimeMillis();
        long fiveMinsAgo = currentTime - 300000;
        long tenMins = 600000;

        CachedItem<List<SpreadsheetRow>> item1 = new CachedItem<List<SpreadsheetRow>>();
        item1.setCachedAt(fiveMinsAgo);
        item1.setCacheDuration(tenMins);
        List<SpreadsheetRow> rows1 = new ArrayList<SpreadsheetRow>();
        item1.setData(rows1);

        CachedItem<List<SpreadsheetRow>> item2 = new CachedItem<List<SpreadsheetRow>>();
        item2.setCachedAt(fiveMinsAgo);
        item2.setCacheDuration(tenMins);
        List<SpreadsheetRow> rows2 = new ArrayList<SpreadsheetRow>();
        item2.setData(rows2);
        
        _cache.put("key1", item1);
        _cache.put("key2", item2);

        assertNotNull(_cache.get("key1"));
        assertNotNull(_cache.get("key2"));

        _dao.clearCacheKey("key2");
        assertNotNull(_cache.get("key1"));
        assertNull(_cache.get("key2"));

        _dao.clearCacheKey("key1");
        assertNull(_cache.get("key1"));
        assertNull(_cache.get("key2"));

        assertTrue(_cache.isEmpty());
    }

    public void testClearCache() {
        assertTrue(_cache.isEmpty());

        long currentTime = System.currentTimeMillis();
        long fiveMinsAgo = currentTime - 300000;
        long tenMins = 600000;

        CachedItem<List<SpreadsheetRow>> item1 = new CachedItem<List<SpreadsheetRow>>();
        item1.setCachedAt(fiveMinsAgo);
        item1.setCacheDuration(tenMins);
        List<SpreadsheetRow> rows1 = new ArrayList<SpreadsheetRow>();
        item1.setData(rows1);

        CachedItem<List<SpreadsheetRow>> item2 = new CachedItem<List<SpreadsheetRow>>();
        item2.setCachedAt(fiveMinsAgo);
        item2.setCacheDuration(tenMins);
        List<SpreadsheetRow> rows2 = new ArrayList<SpreadsheetRow>();
        item2.setData(rows2);

        _cache.put("key1", item1);
        _cache.put("key2", item2);

        assertNotNull(_cache.get("key1"));
        assertNotNull(_cache.get("key2"));

        _dao.clearCache();
        assertNull(_cache.get("key1"));
        assertNull(_cache.get("key2"));

        assertTrue(_cache.isEmpty());
    }
}
