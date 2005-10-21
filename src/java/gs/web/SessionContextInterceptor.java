/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionContextInterceptor.java,v 1.3 2005/10/21 00:16:01 apeterson Exp $
 */
package gs.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The purpose to guarantee that certain values are stuck in the request.
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class SessionContextInterceptor
        implements HandlerInterceptor,
        ApplicationContextAware {

    private static final Log _log = LogFactory.getLog(SessionContextInterceptor.class);
    private ApplicationContext _applicationContext;


    /**
     * @deprecated use the factory method {@link SessionFacade#getInstance(javax.servlet.http.HttpServletRequest)}
     */
    public static final String SESSION_ATTRIBUTE_NAME = "context";

    public boolean preHandle(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse,
                             Object o) throws Exception {


        HttpSession session = httpServletRequest.getSession();
        SessionContext sessionContext
                = (SessionContext) session.getAttribute(SESSION_ATTRIBUTE_NAME);

        if (sessionContext == null) {

            sessionContext =
                    (SessionContext) _applicationContext.getBean(SessionContext.BEAN_ID);

            session.setAttribute(SESSION_ATTRIBUTE_NAME, sessionContext);
        }

        httpServletRequest.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext);
        sessionContext.setRequest(httpServletRequest);

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

}
