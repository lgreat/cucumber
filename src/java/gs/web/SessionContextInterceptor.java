/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionContextInterceptor.java,v 1.12 2006/02/24 23:10:47 apeterson Exp $
 */
package gs.web;

import gs.web.util.PageHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The purpose to guarantee that certain values are stuck in the request.
 * It is responsible for:
 * <ul>
 * <li>guaranteeing that there is a ISessionFacade implementation available in
 * the request
 * <li>pulling standard cookie values and putting them into the ISessionFacade
 * <li>pulling param variables and putting them into the ISessionFacade
 * <li>puttting calculated values into the ISessionFacade
 * </ul>
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class SessionContextInterceptor
        implements HandlerInterceptor,
        ApplicationContextAware {


    private static final Log _log = LogFactory.getLog(SessionContextInterceptor.class);
    private ApplicationContext _applicationContext;

    private SessionContextUtil _sessionContextUtil;


    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object o) throws Exception {

        SessionContext context = _sessionContextUtil.prepareSessionContext(request, response);

        PageHelper pageHelper = new PageHelper(context);
        request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        return true; // go on
    }


    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        // nothing
    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        // nothing
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        _applicationContext = applicationContext;
    }


    public SessionContextUtil getSessionContextUtil() {
        return _sessionContextUtil;
    }

    public void setSessionContextUtil(SessionContextUtil sessionContextUtil) {
        _sessionContextUtil = sessionContextUtil;
    }
}

