/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: InsiderOnlyInterceptor.java,v 1.2 2005/10/14 23:21:26 apeterson Exp $
 */
package gs.web.community;

import gs.data.community.ISubscriptionDao;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.web.ISessionFacade;
import gs.web.SessionFacade;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * The purpose to guarantee that the user is logged into insider in order to
 * get insider content.
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class InsiderOnlyInterceptor
        implements HandlerInterceptor,
        ApplicationContextAware {

    private static final Log _log = LogFactory.getLog(InsiderOnlyInterceptor.class);
    private ApplicationContext _applicationContext;
    private ISubscriptionDao _subscriptionDao;


    public boolean preHandle(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse,
                             Object o) throws Exception {

        ISessionFacade sessionFacade = SessionFacade.getInstance(httpServletRequest);

        if (httpServletRequest.getRequestURI().indexOf("insider.page") >= 0) {
            final User user = sessionFacade.getUser();
            if (user == null ||
                    !_subscriptionDao.isUserSubscribed(user, SubscriptionProduct.ONE_YEAR_SUB, new Date())) {

                String url = "http://" +
                        sessionFacade.getHostName() +
                        "/cgi-bin/site/signin.cgi/" +
                        sessionFacade.getState().getAbbreviation();
                final String redirect = httpServletResponse.encodeRedirectURL(url);
                httpServletResponse.sendRedirect(redirect);
                return false;
            }
        }

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

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

}
