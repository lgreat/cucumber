package gs.web.mobile;

import gs.web.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Trivial controller that will choose either a standard (desktop) view or a mobile view, depending
 * on what <code>RequestInfo</code> says we should render. Use this controller if you have two views (a standard
 * view and mobile view) that can be rendered by the same page/URI/spring bean
 *
 * If you're configuring a mobile-only controller in spring, just use ParameterizableViewController as usual
 * and configure it with your mobile view name
 */
public class MobileCapableParameterizableViewController extends ParameterizableViewController {
    
    private String _mobileViewName;

    @Autowired
    private RequestInfo _requestInfo;

    public String resolveViewName() {
        if (_requestInfo.shouldRenderMobileView()) {
            return getMobileViewName();
        } else {
            return getViewName();
        }
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        return new ModelAndView(resolveViewName());
    }

    public String getMobileViewName() {
        return _mobileViewName;
    }

    public void setMobileViewName(String mobileViewName) {
        _mobileViewName = mobileViewName;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        _requestInfo = requestInfo;
    }
}
