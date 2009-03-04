package gs.web.content;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
/**
 *  @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class MiddleLandingPageController extends AbstractGradeLevelLandingPageController {

    protected void populateModel(Map<String, Object> model, HttpServletRequest request) {
        loadTableRowsIntoModel(model,"ms_support_classroom");
        loadTableRowsIntoModel(model,"ms_support_hallway");
        loadTableRowsIntoModel(model,"ms_college");
    }
}
