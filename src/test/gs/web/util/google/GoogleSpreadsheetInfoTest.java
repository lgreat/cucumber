package gs.web.util.google;

import gs.web.BaseTestCase;

/**
 * Encapsulates Google Spreadsheet info information
 * @author Young Fan
 */
public class GoogleSpreadsheetInfoTest extends BaseTestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testBasics() {
        GoogleSpreadsheetInfo info = new GoogleSpreadsheetInfo(null, null, null, null);
        assertNull(info.getGoogleKey());
        assertNull(info.getVisibility());
        assertNull(info.getProjection());
        assertNull(info.getWorksheetName());

        info.setGoogleKey("a");
        info.setVisibility("b");
        info.setProjection("c");
        info.setWorksheetName("d");

        assertEquals("a", info.getGoogleKey());
        assertEquals("b", info.getVisibility());
        assertEquals("c", info.getProjection());
        assertEquals("d", info.getWorksheetName());
    }

    public void testGetWorksheetinfo() {
        GoogleSpreadsheetInfo info = new GoogleSpreadsheetInfo("a", "b", "c", "d");
        assertEquals(GoogleSpreadsheetInfo.SPREADSHEET_PREFIX + "a/b/c/d", info.getWorksheetUrl());

        info = new GoogleSpreadsheetInfo("a", "b", "c", null);
        boolean threwException = false;
        try {
            info.getWorksheetUrl();
        } catch (IllegalStateException e) {
            threwException = true;
        }
        assertTrue(threwException);

        info = new GoogleSpreadsheetInfo("a", "b", null, "d");
         threwException = false;
        try {
            info.getWorksheetUrl();
        } catch (IllegalStateException e) {
            threwException = true;
        }
        assertTrue(threwException);

        info = new GoogleSpreadsheetInfo("a", null, "c", "d");
         threwException = false;
        try {
            info.getWorksheetUrl();
        } catch (IllegalStateException e) {
            threwException = true;
        }
        assertTrue(threwException);

        info = new GoogleSpreadsheetInfo(null, "b", "c", "d");
         threwException = false;
        try {
            info.getWorksheetUrl();
        } catch (IllegalStateException e) {
            threwException = true;
        }
        assertTrue(threwException);
    }
}
