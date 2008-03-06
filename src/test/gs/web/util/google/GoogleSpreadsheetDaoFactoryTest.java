package gs.web.util.google;

import gs.web.BaseTestCase;
import gs.data.util.table.ITableDao;

import static gs.web.util.google.GoogleSpreadsheetDaoFactory.*;
/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class GoogleSpreadsheetDaoFactoryTest extends BaseTestCase {
    GoogleSpreadsheetDaoFactory _factory;

    public void setUp() throws Exception {
        super.setUp();

        _factory = new GoogleSpreadsheetDaoFactory();
    }

    public void testBasics() {
        _factory.setGoogleKey("googleKey");
        _factory.setPassword("password");
        _factory.setProjection("projection");
        _factory.setUsername("username");
        _factory.setVisibility("visibility");
        _factory.setWorksheetName("worksheetName");

        assertEquals("googleKey", _factory.getGoogleKey());
        assertEquals("password", _factory.getPassword());
        assertEquals("projection", _factory.getProjection());
        assertEquals("username", _factory.getUsername());
        assertEquals("visibility", _factory.getVisibility());
        assertEquals("worksheetName", _factory.getWorksheetName());
    }

    public void testNullGoogleKey() {
        _factory.setGoogleKey(null);
        try {
            _factory.getTableDao();
            fail("Expected illegal state exception because of null google key");
        } catch (IllegalStateException ise) {
            // ok
        }
    }

    public void testGetTableDaoBasic() {
        _factory.setGoogleKey("1234");

        ITableDao rval = _factory.getTableDao();

        assertEquals(GoogleSpreadsheetDao.class, rval.getClass());
        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) rval;
        assertEquals(SPREADSHEET_PREFIX + "1234/" +
                DEFAULT_VISIBILITY + "/" + DEFAULT_PROJECTION + "/",
                dao.getWorksheetUrl());

        assertNull(dao.getUsername());
        assertNull(dao.getPassword());
    }

    public void testGetTableDaoPrivate() {
        _factory.setGoogleKey("12345");
        _factory.setVisibility("vis");
        _factory.setProjection("proj");
        _factory.setUsername("user");
        _factory.setPassword("pass");

        ITableDao rval = _factory.getTableDao();

        assertEquals(GoogleSpreadsheetDao.class, rval.getClass());
        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) rval;
        assertEquals(SPREADSHEET_PREFIX + "12345/vis/proj/",
                dao.getWorksheetUrl());

        assertEquals("user", dao.getUsername());
        assertEquals("pass", dao.getPassword());
    }

    public void testGetTableDaoWithWorksheetName() {
        _factory.setGoogleKey("12345");
        _factory.setVisibility("vis");
        _factory.setProjection("proj");
        _factory.setUsername("user");
        _factory.setPassword("pass");
        _factory.setWorksheetName("name");

        ITableDao rval = _factory.getTableDao();

        assertEquals(GoogleSpreadsheetDao.class, rval.getClass());
        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) rval;
        assertEquals(SPREADSHEET_PREFIX + "12345/vis/proj/name",
                dao.getWorksheetUrl());

        assertEquals("user", dao.getUsername());
        assertEquals("pass", dao.getPassword());
    }
}
