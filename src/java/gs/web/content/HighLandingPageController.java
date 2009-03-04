package gs.web.content;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class HighLandingPageController extends AbstractGradeLevelLandingPageController {
    protected void populateModel(Map<String, Object> model, HttpServletRequest request) {
        loadTableRowsIntoModel(model,"hs_boost_classroom");
        loadTableRowsIntoModel(model,"hs_boost_hallway");
    }
}
