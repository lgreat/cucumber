package gs.web.about;

import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Dec 29, 2008
 * Time: 1:14:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinkToUsController extends SimpleFormController {
    private boolean _hideSchoolFinderWidgetPreschools;

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        LinkToUsCommand command = new LinkToUsCommand();
        command.setHideSchoolFinderWidgetPreschools(_hideSchoolFinderWidgetPreschools);
        return command;
    }

    public boolean isHideSchoolFinderWidgetPreschools() {
        return _hideSchoolFinderWidgetPreschools;
    }

    public void setHideSchoolFinderWidgetPreschools(boolean hideSchoolFinderWidgetPreschools) {
        _hideSchoolFinderWidgetPreschools = hideSchoolFinderWidgetPreschools;
    }
}
