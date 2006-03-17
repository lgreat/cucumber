/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: RequiresStateInterceptor.java,v 1.6 2006/03/17 22:33:24 apeterson Exp $
 */

package gs.web.state;

import gs.data.state.StateManager;
import gs.web.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;

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

                // Requires a state, so now generate a redirect to force the user to pick one.
                String url = "http://" +
                        httpServletRequest.getServerName() +
                        ((httpServletRequest.getServerPort() != 80) ? ":" + httpServletRequest.getServerPort() : "") +
                        httpServletRequest.getContextPath() +
                        "/selectAState.page?prompt=Please+select+a+state+to+continue.&url=" +
                        httpServletRequest.getRequestURI();

                // Add the parameters manually
                if (httpServletRequest.getParameterMap().size() != 0) {
                    url += "?";
                    for (Iterator iter = httpServletRequest.getParameterMap().keySet().iterator(); iter.hasNext();) {
                        String key = (String) iter.next();
                        if (!StringUtils.equals(key, "state")) {
                            String value = httpServletRequest.getParameter(key);
                            url += key + "=" + value;
                            if (iter.hasNext()) {
                                url += "%26";
                            }
                        }
                    }
                }

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
