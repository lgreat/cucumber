package gs.web.jsp;

import org.springframework.context.ApplicationContext;
import gs.web.util.context.SessionContext;
import gs.web.util.SpringUtil;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;

/**
 * This abstract class provides access to the application context which is
 * extracted from the SessionContext.
 *
 * Created by chriskimm@greatschools.net
 */
public abstract class SpringTagHandler extends SimpleTagSupport {

    private ApplicationContext _applicationContext;

    protected ApplicationContext getApplicationContext() {
        if (_applicationContext == null) {
            SessionContext sc = getSessionContext();
            if (sc != null) {
                _applicationContext = sc.getApplicationContext();
            } else {
                _applicationContext = SpringUtil.getApplicationContext();
            }
        }
        return _applicationContext;
    }

    /**
     * Used for unit testing.
     * @param context Spring ApplicationContext
     */
    public void setApplicationContext(ApplicationContext context) {
        _applicationContext = context;
    }

    protected SessionContext getSessionContext() {
        JspContext jspContext = getJspContext();
        SessionContext sc = null;
        if (jspContext != null) {
            sc = (SessionContext) jspContext.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, PageContext.REQUEST_SCOPE);
        }
        return sc;
    }
}
