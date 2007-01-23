/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.community.registration;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.community.User;
import gs.web.util.context.SessionContextUtil;

import java.util.Map;
import java.util.HashMap;

/**
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class AccountInfoController extends AbstractController {
    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
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
