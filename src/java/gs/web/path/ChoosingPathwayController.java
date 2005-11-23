/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ChoosingPathwayController.java,v 1.1 2005/11/23 00:14:58 apeterson Exp $
 */

package gs.web.path;

import gs.web.ISessionFacade;
import gs.web.SessionContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.InternalResourceView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Choose the right choosing pathway landing page, based on whether the user is
 * logged in and what state they are looking at.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class ChoosingPathwayController implements Controller {
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ISessionFacade context = SessionContext.getInstance(request);

        String pageName;
        if (context.getStateOrDefault().isSubscriptionState() &&
                context.getUser() != null) {
            pageName = "chooseInsiderLoggedIn";
        } else if (context.getStateOrDefault().isSubscriptionState() &&
                context.getUser() == null &&
                !"azcentral".equals(context.getCobrand())) {
            pageName = "chooseInsiderLoggedOut";

        } else {
            pageName = "chooseOutsiderState";
        }

        String fileName = "/WEB-INF/page/path/" + pageName + ".jspx";
        View view = new InternalResourceView(fileName);
        ModelAndView modelAndView = new ModelAndView(view);
        return modelAndView;
    }
}
