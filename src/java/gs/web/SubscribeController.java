/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SubscribeController.java,v 1.1 2005/05/11 01:16:54 apeterson Exp $
 */
package gs.web;

import gs.data.community.*;
import gs.data.state.State;
import gs.data.state.StateUtil;
import gs.data.util.Price;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.*;

/**
 * The purpose is ...
 * TODO
 * authorize transaction
 * remember "nextUrl" attribute send in, and send the user there on completion.
 * accept "email" passed in.
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class SubscribeController extends org.springframework.web.servlet.mvc.SimpleFormController {
    private static Log _log = LogFactory.getLog(SubscribeController.class);

    private Purchaser _purchaser;
    private IUserDao _userDao;

    private static final String EMAIL_PARAM = "email";
    private static final String STATE_PARAM = "state";
    private static final String URL_PARAM = "url";
    private static final String URL_LABEL_PARAM = "urlLabel";


    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {

        SubscribeCommand command = new SubscribeCommand();
        _log.debug("formBackingObject: created command");

        // Suck the information out of the URL, if available.
        String paramEmail = httpServletRequest.getParameter(EMAIL_PARAM);
        if (!StringUtils.isEmpty(paramEmail)) {
            User user = command.getUser();
            user.setEmail(paramEmail);
            _log.debug("formBackingObject: found user in params: " + paramEmail);
        }

        String paramStateStr = httpServletRequest.getParameter(STATE_PARAM);
        if (!StringUtils.isEmpty(paramStateStr)) {
            Subscription subscription = command.getSubscription();
            State s = StateUtil.getState(paramStateStr);
            command.setState(s);
            _log.debug("formBackingObject: found state in params: " + paramEmail);
        }

        String paramUrl = httpServletRequest.getParameter(URL_PARAM);
        if (!StringUtils.isEmpty(paramUrl)) {
            command.setUrl(paramUrl);
        }

        String paramUrlLabel = httpServletRequest.getParameter(URL_LABEL_PARAM);
        if (!StringUtils.isEmpty(paramUrlLabel)) {
            command.setUrl(paramUrlLabel);
        }

        // Set of the product and price
        final SubscriptionProduct product = SubscriptionProduct.ONE_YEAR_SUB;
        command.setSubscriptionProduct(product);

        // Purchaser can accurately determine the price based on the user
        Price p = _purchaser.getSubscriptionPrice(command.getUser(), product);
        command.getCard().setTransactionAmount(p);

        return command;

    }

    protected Map referenceData(HttpServletRequest httpServletRequest) throws Exception {
        List subStates = StateUtil.getSubscriptionStates();
        StringBuffer sb = new StringBuffer(subStates.size() * 4);
        for (Iterator iter = subStates.iterator(); iter.hasNext();) {
            State s = (State) iter.next();
            sb.append(s.getAbbreviation());
            if (iter.hasNext()) {
                sb.append(", ");
            }
        }

        Map map = new HashMap();
        map.put("subscriptionStates", sb.toString());

        final Price price = SubscriptionProduct.ONE_YEAR_SUB.getPrice(new Date());
        map.put("subscriptionPrice", price.asString());

        return map;
    }


    protected ModelAndView onSubmit(Object o, BindException errors) throws Exception {
        SubscribeCommand command = (SubscribeCommand) o;

        final User user = command.getUser();

        _userDao.saveUser(user);

        Subscription subscription = _purchaser.purchaseSubscription(user,
                command.getSubscriptionProduct(),
                command.getState(),
                command.getCard());

        command.setSubscription(subscription);

        try {
            _purchaser.sendSubscriptionThankYouEmail(subscription);
        } catch (TransformerException e) {
            // ignore for now -- error is logged
        } catch (IOException e) {
            // ignore for now -- error is logged
        }


        //ModelAndView mv = onSubmit(command);


        // default behavior: render success view
        if (getSuccessView() == null) {
            throw new ServletException("successView isn't set");
        }
        return new ModelAndView(getSuccessView(), errors.getModel());

    }

    public Purchaser getPurchaser() {
        return _purchaser;
    }

    public void setPurchaser(Purchaser purchaser) {
        _purchaser = purchaser;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

}
