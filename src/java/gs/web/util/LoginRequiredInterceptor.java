/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.util;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class LoginRequiredInterceptor extends HandlerInterceptorAdapter {
    protected final Log _log = LogFactory.getLog(getClass());

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // you are authed only if you are logged in and your credentials match
        if (PageHelper.isMemberAuthorized(request)) {
            return true;
        } else {
            String url = request.getRequestURL().toString();
            UrlBuilder loginUrl = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, url);
            String targetView = loginUrl.asFullUrl(request);
            response.sendRedirect(targetView);
            return false;
        }
    }
}
