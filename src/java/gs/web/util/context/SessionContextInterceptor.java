/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionContextInterceptor.java,v 1.2 2006/07/26 22:29:21 thuss Exp $
 */
package gs.web.util.context;

import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The purpose to guarantee that certain values are stuck in the request.
 * It is responsible for:
 * <ul>
 * <li>guaranteeing that there is a ISessionContext implementation available in
 * the request
 * <li>pulling standard cookie values and putting them into the ISessionContext
 * <li>pulling param variables and putting them into the ISessionContext
 * <li>puttting calculated values into the ISessionContext
 * </ul>
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class SessionContextInterceptor implements HandlerInterceptor {

    private static final Log _log = LogFactory.getLog(SessionContextInterceptor.class);

    private SessionContextUtil _sessionContextUtil;

    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object o) throws Exception {
        SessionContext context = _sessionContextUtil.prepareSessionContext(request, response);
        PageHelper pageHelper = new PageHelper(context, request);
        request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
        return true; // go on
    }


    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        // nothing
    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        // nothing
    }

    public SessionContextUtil getSessionContextUtil() {
        return _sessionContextUtil;
    }

    public void setSessionContextUtil(SessionContextUtil sessionContextUtil) {
        _sessionContextUtil = sessionContextUtil;
    }
}

