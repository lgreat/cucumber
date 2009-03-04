package gs.web.content;

import javax.servlet.http.HttpServletRequest;
import gs.data.state.State;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;

import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ElementaryLandingPageController extends AbstractGradeLevelLandingPageController {
     protected void populateModel(Map<String, Object> model,HttpServletRequest request) {
        loadTableRowsIntoModel(model,"k");
        loadTableRowsIntoModel(model,"1");
        loadTableRowsIntoModel(model,"2");
        loadTableRowsIntoModel(model,"3");
        loadTableRowsIntoModel(model,"4");
        loadTableRowsIntoModel(model,"5");

        SessionContext context = SessionContextUtil.getSessionContext(request);
        String userCityName = "Los Angeles";
        State userState = context.getStateOrDefault();
        if (context.getCity() != null) {
            userCityName = context.getCity().getName();
        }
        model.put("userCity", userCityName);
        model.put("userState", userState);
    }
}
