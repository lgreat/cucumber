/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.community.registration;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

import gs.data.community.User;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlBuilder;

/**
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CommunityLandingController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String BEAN_ID = "/community/communityLanding.page";

    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String targetView = _viewName;
        User user = SessionContextUtil.getSessionContext(request).getUser();
        if (user == null) {
            UrlBuilder homeUrl = new UrlBuilder(UrlBuilder.COMMUNITY_LANDING, null, null);
            UrlBuilder loginUrl = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null,
                    homeUrl.asFullUrl(request));
            targetView = "redirect:" + loginUrl.asFullUrl(request);
            _log.info("Redirecting to " + targetView);
        }
        Map model = new HashMap();
        model.put("user", user);
        return new ModelAndView(targetView, model);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
