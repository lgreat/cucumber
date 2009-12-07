package gs.web.util.google;

import gs.web.BaseTestCase;
import gs.data.util.table.ITableRow;
import gs.data.util.table.AbstractCachedTableDao;

import java.util.List;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.util.ServiceException;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class GoogleSpreadsheetDaoTest extends BaseTestCase {

    private GoogleSpreadsheetDao _dao;
    private static final String WORKSHEET_URL = "http://spreadsheets.google.com/feeds/worksheets/pmY-74KD4CbXrSKtrPdEnSg/public/values/od7";
    private static final String GOOGLE_KEY = "pmY-74KD4CbXrSKtrPdEnSg";
    private static final String VISIBILITY = "public";
    private static final String PROJECTION = "values";
    private static final String WORKSHEET_NAME = "od7";
    private static final GoogleSpreadsheetInfo SPREADSHEET_INFO = new GoogleSpreadsheetInfo(GOOGLE_KEY, VISIBILITY, PROJECTION, WORKSHEET_NAME);

    public void setUp() throws Exception {
        super.setUp();

        _dao = new GoogleSpreadsheetDao(new GoogleSpreadsheetInfo(GOOGLE_KEY, VISIBILITY, PROJECTION, WORKSHEET_NAME));
        _dao.clearCache();
    }

    public void testSetWorksheetUrl() {
        _dao = new GoogleSpreadsheetDao(new GoogleSpreadsheetInfo(null,null,null,null));

        boolean threwException = false;
        try {
            _dao.getWorksheetUrl();
        } catch (IllegalStateException e) {
            threwException = true;
        }

        assertTrue("Expected exception when spreadsheet url parameters not set", threwException);

        _dao.setSpreadsheetInfo(SPREADSHEET_INFO);
        assertEquals(WORKSHEET_URL, _dao.getWorksheetUrl());
    }

    public void testGetFirstRowByKey() {
        try {
            _dao.getFirstRowByKey("code", null);
            fail("IllegalArgumentException should be thrown when keyValue argument is null");
        } catch (Exception e) {
            assertTrue(true);
        }

        ITableRow row = _dao.getFirstRowByKey("code", "school/rating.page");

        assertEquals("Other than the education one receives - what makes a school \"great\"?",
                row.get("text"));
        assertEquals("http://community.greatschools.org/q-and-a/123301/Other-than-the-education-one-receives-what-makes-a-school-great",
                row.get("link"));
        assertEquals("Queserasera", row.get("username"));
        assertEquals("2093577", row.get("memberid"));

        row = _dao.getFirstRowByKey("code", "school/parentReviews.page");

        assertEquals("What's the difference?", row.get("text"));
        assertEquals("http://community.greatschools.org/q-and-a", row.get("link"));
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

    public void testGetListFeedExceptions() throws IOException, ServiceException, AbstractCachedTableDao.ExternalConnectionException {
        SpreadsheetService service = createMock(SpreadsheetService.class);
        ListFeed listFeed = null;
        _dao.setSpreadsheetService(service);

        expect(service.getEntry(isA(URL.class), eq(WorksheetEntry.class))).andThrow(new MalformedURLException("m1"));
        replay(service);

        try {
            listFeed = _dao.getListFeed();
        } catch (AbstractCachedTableDao.ExternalConnectionException e) {
            assertEquals("m1", e.getMessage());
        }

        verify(service);
        assertNull(listFeed);

        reset(service);
        expect(service.getEntry(isA(URL.class), eq(WorksheetEntry.class))).andThrow(new IOException("m2"));
        replay(service);

        try {
            listFeed = _dao.getListFeed();
        } catch (AbstractCachedTableDao.ExternalConnectionException e) {
            assertEquals("m2", e.getMessage());
        }

        verify(service);
        assertNull(listFeed);

        reset(service);
        expect(service.getEntry(isA(URL.class), eq(WorksheetEntry.class))).andThrow(new ServiceException("Testing ServiceException"));
        replay(service);

        try {
            listFeed = _dao.getListFeed();
        } catch (AbstractCachedTableDao.ExternalConnectionException e) {
            assertEquals("Testing ServiceException", e.getMessage());
        }

        verify(service);
        assertNull(listFeed);
    }

    public void testGetListFeedWithCredentials() throws IOException, ServiceException, AbstractCachedTableDao.ExternalConnectionException {
        _dao = new GoogleSpreadsheetDao(SPREADSHEET_INFO, "user", "pass");

        SpreadsheetService service = createMock(SpreadsheetService.class);
        WorksheetEntry dataWorksheet = createMock(WorksheetEntry.class);
        ListFeed listFeed = new ListFeed();

        _dao.setSpreadsheetService(service);

        service.setUserCredentials("user", "pass");
        expect(service.getEntry(isA(URL.class), eq(WorksheetEntry.class))).andReturn(dataWorksheet);

        URL url = new URL("http://dev.greatschools.org");
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
}
