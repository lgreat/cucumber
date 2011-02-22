package gs.web.search;

import junit.framework.TestCase;


public class SchoolSearchServiceSolrImplTest extends TestCase {
    SchoolSearchServiceSolrImpl _schoolSearchServiceSolr;

    public void setUp() {
        _schoolSearchServiceSolr = new SchoolSearchServiceSolrImpl();
    }
    public void testRequireNonOptionalWords() throws Exception {
        String qs = "Alameda High School";
        String expected = "+Alameda High School";

        String result = _schoolSearchServiceSolr.requireNonOptionalWords(qs);
        
        assertEquals("Wrong result", expected, result);
    }


}
