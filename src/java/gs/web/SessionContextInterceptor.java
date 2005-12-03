/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionContextInterceptor.java,v 1.10 2005/12/03 00:35:59 apeterson Exp $
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
        /*
         * Set pages to not be cached since almost all pages include the member bar now or some
         * other dynamic content. This should be in the decorator as gsml:nocache but since that
         * tag doesn't work due to a sitemesh bug (see gsml:nocache tag for more info) it's here
         * for the time being.
         */
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        /*
         Create a new session context every time.
         Then, pull information out of cookies and parameters.
         We also pull things out the request because this is a convenient way
         to pass things in from include tags.
         */
        SessionContext context =
                (SessionContext) _applicationContext.getBean(SessionContext.BEAN_ID);

        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, context);

        _sessionContextUtil.readCookies(request, context);
        _sessionContextUtil.updateFromRequestAttributes(request, context);
        _sessionContextUtil.updateFromParams(request, response, context);


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

