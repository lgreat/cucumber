package gs.web.util.google;

import gs.web.BaseTestCase;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SpreadsheetRowTest extends BaseTestCase {
    private SpreadsheetRow _row;

    public void setUp() throws Exception {
        super.setUp();

        _row = new SpreadsheetRow();
    }

    public void testBasics() {
        _row.addCell("col1", "val1");
        _row.addCell("col2", "val2");
        _row.addCell("col2", "val3");

        assertEquals("val1", _row.getCell("col1"));
        assertEquals("Expect val2 to have been overwritten", "val3", _row.getCell("col2"));

        for (String name: _row.getColumnNames()) {
            assertTrue(name.equals("col1") || name.equals("col2"));
        }
    }
}
