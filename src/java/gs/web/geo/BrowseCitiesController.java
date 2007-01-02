/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: BrowseCitiesController.java,v 1.5 2007/01/02 20:09:17 cpickslay Exp $
 */

package gs.web.geo;

import gs.data.state.State;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides redirection to perl page.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class BrowseCitiesController extends AbstractController {
    private SessionContextUtil _sessionContextUtil;

    public ModelAndView handleRequestInternal(HttpServletRequest request,
                                              HttpServletResponse response)
            throws Exception {

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);

        State state = sessionContext.getState();

        UrlUtil urlUtil = new UrlUtil();
        String url = urlUtil.buildUrl("/modperl/cities/$LCSTATE/", request);

        View redirectView = new RedirectView(url);
        return new ModelAndView(redirectView);
    }

    public SessionContextUtil getSessionContextUtil() {
        return _sessionContextUtil;
    }

    public void setSessionContextUtil(SessionContextUtil sessionContextUtil) {
        _sessionContextUtil = sessionContextUtil;
    }
}
