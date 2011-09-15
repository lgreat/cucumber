/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: SessionContextInterceptor.java,v 1.7 2011/09/15 00:36:23 ssprouse Exp $
 */
package gs.web.util.context;

import gs.web.request.HostnameInfo;
import gs.web.util.PageHelper;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The purpose to guarantee that certain values are stuck in the request.
 * It is responsible for:
 * <ul>
 * <li>guaranteeing that there is a SessionContext implementation available in
 * the request
 * <li>pulling standard cookie values and putting them into the SessionContext
 * <li>pulling param variables and putting them into the SessionContext
 * <li>puttting calculated values into the SessionContext
 * </ul>
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.org>
 */
public class SessionContextInterceptor implements HandlerInterceptor {

    private SessionContextUtil _sessionContextUtil;

    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object o) throws Exception {
        SessionContext context = _sessionContextUtil.prepareSessionContext(request, response);
        PageHelper pageHelper = new PageHelper(context, request);
        HostnameInfo hostnameInfo = new HostnameInfo(request.getServerName());
        request.setAttribute(HostnameInfo.REQUEST_ATTRIBUTE_NAME, hostnameInfo);
        request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
        return true; // go on
    }


    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        // nothing
    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        // nothing
    }

    public void setSessionContextUtil(SessionContextUtil sessionContextUtil) {
        _sessionContextUtil = sessionContextUtil;
    }
}

