/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ChoosingPathwayController.java,v 1.3 2005/11/29 23:38:25 apeterson Exp $
 */

package gs.web.path;

import gs.data.community.ISubscriptionDao;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.state.State;
import gs.web.ISessionFacade;
import gs.web.SessionContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.InternalResourceView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Choose the right choosing pathway landing page, based on whether the user is
 * logged in and what state they are looking at.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class ChoosingPathwayController implements Controller {

    ISubscriptionDao _subscriptionDao;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ISessionFacade context = SessionContext.getInstance(request);

        String pageName;
        final User user = context.getUser();
        final State state = context.getStateOrDefault();
        if (state.isSubscriptionState() &&
                user != null &&
                _subscriptionDao.isUserSubscribed(user, SubscriptionProduct.ONE_YEAR_SUB, new Date())) {
            pageName = "chooseInsiderLoggedIn";
        } else if (state.isSubscriptionState() &&
                user == null &&
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


    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }
}
