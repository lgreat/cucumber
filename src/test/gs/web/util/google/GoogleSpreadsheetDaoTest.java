package gs.web.util.google;

import gs.web.BaseTestCase;

import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class GoogleSpreadsheetDaoTest extends BaseTestCase {

    private GoogleSpreadsheetDao _dao;
    private String _worksheetUrl;

    public void setUp() throws Exception {
        super.setUp();

        _dao = new GoogleSpreadsheetDao();

        _worksheetUrl = "http://spreadsheets.google.com/feeds/worksheets/pmY-74KD4CbXrSKtrPdEnSg/public/values/od7";
    }

    public void testGetDataFromRow() {
        Map<String, String> map = _dao.getDataFromRow(_worksheetUrl, "code", "school/rating.page");

        assertEquals("Other than the education one receives - what makes a school \"great\"?",
                map.get("text"));
        assertEquals("http://community.greatschools.net/q-and-a/123301/Other-than-the-education-one-receives-what-makes-a-school-great",
                map.get("link"));
        assertEquals("Queserasera",
                map.get("username"));
        assertEquals("2093577",
                map.get("memberid"));

        map = _dao.getDataFromRow(_worksheetUrl, "code", "school/parentReviews.page");

        assertEquals("What's the difference?",
                map.get("text"));
        assertEquals("http://community.greatschools.net/q-and-a",
                map.get("link"));
        assertEquals("Anthony",
                map.get("username"));
        assertEquals("1",
                map.get("memberid"));

    }

    public void testGetDataFromWorksheet() {
        Map<String, Map<String, String>> worksheetMap = _dao.getDataFromWorksheet(_worksheetUrl, "code");
        assertNotNull("Expected a public worksheet for unit tests at " + _worksheetUrl, worksheetMap);
        Map<String, String> rowMap;

        rowMap = worksheetMap.get("school/rating.page");
        assertNotNull("Expected a row with value \"school/rating.page\" in the \"code\" column at " + _worksheetUrl,
                rowMap);
        assertEquals("Other than the education one receives - what makes a school \"great\"?",
                rowMap.get("text"));
        assertEquals("http://community.greatschools.net/q-and-a/123301/Other-than-the-education-one-receives-what-makes-a-school-great",
                rowMap.get("link"));
        assertEquals("Queserasera",
                rowMap.get("username"));
        assertEquals("2093577",
                rowMap.get("memberid"));

        rowMap = worksheetMap.get("school/parentReviews.page");
        assertNotNull("Expected a row with value \"school/parentReviews.page\" in the \"code\" column at " + _worksheetUrl,
                rowMap);
        assertEquals("What's the difference?",
                rowMap.get("text"));
        assertEquals("http://community.greatschools.net/q-and-a",
                rowMap.get("link"));
        assertEquals("Anthony",
                rowMap.get("username"));
        assertEquals("1",
                rowMap.get("memberid"));

        rowMap = worksheetMap.get("does_not_exist");
        assertNull(rowMap);
    }
}
