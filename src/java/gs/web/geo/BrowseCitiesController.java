/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: BrowseCitiesController.java,v 1.2 2006/05/26 19:13:43 apeterson Exp $
 */

package gs.web.geo;

import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.SessionContextUtil;
import gs.web.SessionFacade;
import gs.web.util.UrlUtil;
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

        ISessionFacade sessionContext = SessionFacade.getInstance(request);

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
