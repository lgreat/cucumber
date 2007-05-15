/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.community.registration;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

import gs.data.community.User;
import gs.web.util.context.SessionContextUtil;

/**
 * Provides handling for the community landing page.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CommunityLandingController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String BEAN_ID = "/community/communityLanding.page";

    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = SessionContextUtil.getSessionContext(request).getUser();
        Map model = new HashMap();
        model.put("user", user);
        if (StringUtils.isNotEmpty(request.getParameter("message"))) {
            model.put("escapedMessage", StringEscapeUtils.escapeXml(request.getParameter("message")));
        }
        return new ModelAndView(_viewName, model);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
