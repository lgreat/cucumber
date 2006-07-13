/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: RequiresStateInterceptor.java,v 1.14 2006/07/13 07:53:59 apeterson Exp $
 */

package gs.web.state;

import gs.data.state.StateManager;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The purpose to guarantee that the user that is going to a
 * specific URL has chosen a state first. We look in the URL for
 * the state, and if it's not there, redirect to the "selectAState" page.
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class RequiresStateInterceptor
        implements HandlerInterceptor {

    private static final Log _log = LogFactory.getLog(RequiresStateInterceptor.class);
    private StateManager _stateManager;

    public boolean preHandle(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse,
                             Object o) throws Exception {

        // If this page requires a state...
        if (httpServletRequest.getRequestURI().indexOf("selectAState.page") == -1 && // prevent recursion
                httpServletRequest.getRequestURI().indexOf("status") == -1) {
            String state = httpServletRequest.getParameter(SessionContextUtil.STATE_PARAM);
            if (StringUtils.isEmpty(state) ||
                    state.length() < 2 ||
                    _stateManager.getState(state) == null) {

                UrlBuilder finalPage = new UrlBuilder(httpServletRequest, null);
                finalPage.addParametersFromRequest(httpServletRequest);
                finalPage.removeParameter("state");
                String finalPageParam = finalPage.asSiteRelativeXml(httpServletRequest);

                UrlBuilder redirectPage = new UrlBuilder(httpServletRequest, "/selectAState.page");
                redirectPage.setParameter("prompt", "Please select a state to continue.");
                redirectPage.setParameter("url", finalPageParam);
                String url = redirectPage.asFullUrl(httpServletRequest);

                // Execute the redirect...
                final String redirect = httpServletResponse.encodeRedirectURL(url);
                _log.info("Redirecting to " + url);
                httpServletResponse.sendRedirect(redirect);
                return false;
            }
        }

        return true;
    }

    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        // nothing
    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        // nothing
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}
