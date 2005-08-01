package gs.web.search;

import gs.web.BaseTestCase;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Chris Kimm <chriskimm@greatschools.net>
 */
public class SearchControllerTest extends BaseTestCase {

    protected void setUp () throws Exception {
        super.setUp ();
    }

    public void testOnSubmit () throws Exception {
        SearchController sc =
                (SearchController)_sApplicationContext.getBean(SearchController.BEAN_ID);
        assertNotNull (sc);
        //todo add tests - possibly using mock HttpServlet.. objects: Chris Kimm
    }
}
