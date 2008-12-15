package gs.web.widget;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.validation.BindException;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CustomizeSchoolSearchWidgetController extends SimpleFormController {
    private static final String BEAN_ID = "/widget/customizeSchoolSearchWidget.page";
    private static final Logger _log = Logger.getLogger(CustomizeSchoolSearchWidgetController.class);

    public static final int MINIMUM_WIDTH = 300;
    public static final int MINIMUM_HEIGHT = 412;

    protected void onBindAndValidate(HttpServletRequest request, Object commandObj,
                                     BindException errors) throws Exception {
        CustomizeSchoolSearchWidgetCommand command = (CustomizeSchoolSearchWidgetCommand) commandObj;

        // always reject for now
        errors.reject("Show results");
    }

}
