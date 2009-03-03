package gs.web.content;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.data.util.NameValuePair;
import gs.data.state.State;
import gs.web.util.google.GoogleSpreadsheetDao;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

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
