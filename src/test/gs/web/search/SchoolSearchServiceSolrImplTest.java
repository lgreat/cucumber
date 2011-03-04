package gs.web.search;

import gs.web.BaseTestCase;
import gs.web.community.registration.LoginController;
import org.springframework.context.ApplicationContext;


public class SchoolSearchServiceSolrImplTest extends BaseTestCase {
    SchoolSearchServiceSolrImpl _schoolSearchServiceSolr;

    public void setUp() {
        ApplicationContext appContext = getApplicationContext();
        _schoolSearchServiceSolr = (SchoolSearchServiceSolrImpl) appContext.getBean(SchoolSearchServiceSolrImpl.BEAN_ID);

    }
    public void testRequireNonOptionalWords() throws Exception {
        String qs = "Alameda High School";
        String expected = "+Alameda High School";

        String result = _schoolSearchServiceSolr.requireNonOptionalWords(qs);
        
        assertEquals("Wrong result", expected, result);
    }


}
