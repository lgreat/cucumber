package gs.web.school;

import junit.framework.TestCase;

import java.util.*;

/**
 * Tester for the SchoolmodelDisplayBean
 * User: rraker
 * Date: 6/22/12
 * Time: 10:24 AM
 *
 */
public class SchoolProfileDisplayBeanTester extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testBeanCreation() {
        String tabAbbrev = "tab";
        String sectionAbbrev = "section";
        String sectionTitle = "section title";
        String rowTitle = "row title";
        SchoolProfileDisplayBean bean = new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, rowTitle );

        assertEquals( "testBeanCreation: tab abbrev not returned correctly", tabAbbrev, bean.getTabAbbreviation() );
        assertEquals( "testBeanCreation: section abbrev not returned correctly", sectionAbbrev, bean.getSectionAbbreviation() );
        assertEquals( "testBeanCreation: section title not returned correctly", sectionTitle, bean.getSectionTitle() );
        assertEquals("testBeanCreation: Row title not returned correctly", rowTitle, bean.getRowTitle());
    }

    public void testModelKeyCreation() {
        SchoolProfileDisplayBean bean = new SchoolProfileDisplayBean( "tab", "section", "section title", "row title", "test_key" );

        String expectedKey = "tab/section/test_key";

        assertEquals( "testModelKeyCreation: ModelKey not calculated correctly", expectedKey, bean.getModelKey() );

        SchoolProfileDisplayBean.DisplayFormat df = bean.getDisplayFormat();
        assertTrue( "testModelKeyCreation: displayformat is incorrect", SchoolProfileDisplayBean.DisplayFormat.BASIC.equals(( df )));

        bean.setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        df = bean.getDisplayFormat();
        assertTrue("testModelKeyCreation: displayformat not correctly changed", SchoolProfileDisplayBean.DisplayFormat.TWO_COL.equals((df)));
    }

    public void testEspKeyAddition() {
        SchoolProfileDisplayBean bean = new SchoolProfileDisplayBean( "tab", "section", "section title", "row title", "test_key" );
        bean.addKey( "test_key2" );

        String [] expectedKeys = new String[] { "test_key", "test_key2" };
        Set<String> actualKeys = bean.getEspResponseKeys();

        assertEquals("testEspKeyAddition: axctual and expected lists are not the same size", expectedKeys.length, actualKeys.size() );

        for( String expectedKey : expectedKeys ) {
            assertTrue( "testEspKeyAddition: expected espResponseKey not found: " + expectedKey, actualKeys.contains( expectedKey ) );
        }

        assertEquals("testEspKeyAddition: ModelKey not correct", "tab/section/test_key", bean.getModelKey());

    }

    public void testAddUrl() {
        SchoolProfileDisplayBean bean = new SchoolProfileDisplayBean( "tab", "section", "section title", "row title", "test_key" );
        bean.addUrl( "test_url_desc_key", "test_url_key" );

        assertEquals( "testAddUrl: Url description is not correct", "tab/section/test_url_desc_key", bean.getUrlDescModelKeys()[0] );
        assertEquals( "testAddUrl: Url is not correct", "tab/section/test_url_key", bean.getUrlValueModelKeys()[0]);

        // also make sure this information got added to the AdditionalData section
        List<SchoolProfileDisplayBean.AdditionalData> addData = bean.getAdditionalData();
        // addData should contain one entry for the desc and one for the url
        assertEquals( "testAddUrl: SchoolProfileDisplayBean.AdditionalData does not contain the expected number of elements", 2, addData.size() );
        assertEquals( "testAddUrl: SchoolProfileDisplayBean.AdditionalData does not contain the expected desc modelKey", "tab/section/test_url_desc_key", addData.get(0).getModelKey() );
        assertEquals( "testAddUrl: SchoolProfileDisplayBean.AdditionalData does not contain the expected url modelKey", "tab/section/test_url_key", addData.get(1).getModelKey() );
    }

    public void testAddTitleSubstitution() {
        SchoolProfileDisplayBean bean = new SchoolProfileDisplayBean( "tab", "section", "section title", "row title YEAR-YEAR and XYZ.", "test_key" );
        bean.addRowTitleSubstitution( "XYZ", "results_xyz");
        bean.addRowTitleSubstitution( "YEAR-YEAR", "results_year");

        Map<String, List<String>> model = new HashMap<String, List<String>>();
        model.put( "tab/section/results_year", Arrays.asList(new String[] {"2010-2011"}) );
        model.put( "tab/section/results_xyz", Arrays.asList(new String[] {"x and y and z"}) );

        assertEquals( "testAddTitleSubstitution: match not correct", "YEAR-YEAR", bean.getTitleMatch(1));
        assertEquals( "testAddTitleSubstitution: modelKey not correct", "tab/section/results_year", bean.getTitleModelKey(1));
        String title = SchoolProfileDisplayBean.buildRowTitle( bean, model );
        assertEquals( "testAddTitleSubstitution: match not correct", "row title 2010-2011 and x and y and z.", title );

    }

}
