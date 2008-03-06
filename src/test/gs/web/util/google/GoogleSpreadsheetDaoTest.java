package gs.web.util.google;

import gs.web.BaseTestCase;
import gs.data.util.table.ITableRow;

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
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class GoogleSpreadsheetDaoTest extends BaseTestCase {

    private GoogleSpreadsheetDao _dao;
    private static final String WORKSHEET_URL = "http://spreadsheets.google.com/feeds/worksheets/pmY-74KD4CbXrSKtrPdEnSg/public/values/od7";

    public void setUp() throws Exception {
        super.setUp();

        String worksheetUrl =
                WORKSHEET_URL;
        _dao = new GoogleSpreadsheetDao(worksheetUrl);
        _dao.clearCache();
    }

    public void testSetWorksheetUrl() {
        _dao = new GoogleSpreadsheetDao(null);

        assertNull(_dao.getWorksheetUrl());

        _dao.setWorksheetUrl(WORKSHEET_URL);
        assertEquals(WORKSHEET_URL, _dao.getWorksheetUrl());
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
        _dao = new GoogleSpreadsheetDao(WORKSHEET_URL, "user", "pass");

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
}
