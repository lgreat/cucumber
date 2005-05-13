/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SubscribeController.java,v 1.4 2005/05/13 17:40:06 apeterson Exp $
 */
package gs.web;

import gs.data.community.*;
import gs.data.payment.CreditCardInfo;
import gs.data.state.State;
import gs.data.state.StateUtil;
import gs.data.util.Price;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class SubscribeController extends org.springframework.web.servlet.mvc.SimpleFormController {
    private static Log _log = LogFactory.getLog(SubscribeController.class);

    private PurchaseManager _purchaseManager;
    private IUserDao _userDao;

    private static final String HOST_PARAM = "host";
    private static final String EMAIL_PARAM = "email";
    private static final String STATE_PARAM = "state";
    private static final String URL_PARAM = "url";
    private static final String URL_LABEL_PARAM = "urlLabel";


    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {

        // Suck the information out of the URL, if available.
        User user = new User();
        String paramEmail = httpServletRequest.getParameter(EMAIL_PARAM);
        if (!StringUtils.isEmpty(paramEmail)) {
            user.setEmail(paramEmail);
            _log.debug("formBackingObject: found user in params: " + paramEmail);
        }


        final SubscriptionProduct product = SubscriptionProduct.ONE_YEAR_SUB;
        final Price price = _purchaseManager.getSubscriptionPrice(user, product);


        SubscribeCommand command = new SubscribeCommand(user, product, price);
        _log.debug("formBackingObject: created command");


        String paramStateStr = httpServletRequest.getParameter(STATE_PARAM);
        if (!StringUtils.isEmpty(paramStateStr)) {
            State s = StateUtil.getState(paramStateStr);
            command.setState(s);
            _log.debug("formBackingObject: found state in params: " + paramEmail);
        }

        String paramUrl = httpServletRequest.getParameter(URL_PARAM);
        if (!StringUtils.isEmpty(paramUrl)) {
            command.setUrl(paramUrl);
        }

        String paramHost = httpServletRequest.getParameter(HOST_PARAM);
        if (!StringUtils.isEmpty(paramHost)) {
            command.setHost(paramHost);
        } else {
            command.setHost("greatschools.net");
        }

        String paramUrlLabel = httpServletRequest.getParameter(URL_LABEL_PARAM);
        if (!StringUtils.isEmpty(paramUrlLabel)) {
            command.setUrl(paramUrlLabel);
        }

        return command;
    }

    protected Map referenceData(HttpServletRequest httpServletRequest, Object o, Errors errors) throws Exception {
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

        return map;
    }


    protected void onBindAndValidate(HttpServletRequest httpServletRequest, Object o, BindException be)
            throws Exception {
        // This method gets called whether
        // there are errors or not. Don't bother proceeding if there are errors.
        if (be.hasErrors()) {
            return;
        }

        SubscribeCommand command = (SubscribeCommand) o;

        User user = command.getUser();
        boolean updateUserInfo = false; // Do we need to update the user's information?

        // If there's already a user in the datbase, use it.
        if (user.getId() == null) {
            // getId() should always be null, but in case the code above changes, check first.
            final User existingUser = _userDao.getUserFromEmailIfExists(user.getEmail());
            if (existingUser != null) {
                updateUserInfo = true;
                // We set this flag and only update if the transaction goes through.
                // The thought was to prevent some bogus screwing around from updating what
                // information we have.
                user = existingUser;
            } else {
                _userDao.saveUser(user);
            }
        }

        // If for some reason we didn't get a state when the user entered the page,
        // we default to their home, credit card state.
        if (command.getState() == null) {
            command.setState(user.getState());
        }


        // Transfer the credit card expiration date into the CreditCardInfo object.
        // We don't expose this in the command to prevent hacking. Alternatively,
        // you could specify read-only parameters via config.
        CreditCardInfo cardInfo = new CreditCardInfo();
        cardInfo.setTransactionAmount(command.getPrice());
        cardInfo.setNumber(command.getCreditCardNumber());
        cardInfo.setExpirationMonth(Integer.parseInt(command.getExpirationMonth()));
        cardInfo.setExpirationYear(Integer.parseInt(command.getExpirationYear()));
        cardInfo.setStreet(user.getAddress().getStreet());
        cardInfo.setZip(user.getAddress().getZip());
        cardInfo.setUserEmail(user.getEmail());

        // Make the purchase. Throws an exception if the purchase doesn't go through.
        Subscription subscription = null;
        try {
            subscription = _purchaseManager.purchaseSubscription(user,
                    command.getSubscriptionProduct(),
                    command.getState(),
                    cardInfo);
        } catch (PurchaseManager.AuthorizationException e) {
            be.reject("param0", new Object[]{e.getMessage()}, "Credit card authorization failed.");
            return; // PREMATURE EXIT
        }

        _log.info("User " + user.getEmail() + " bought a subscription for " + cardInfo.getTransactionAmount());
        command.setSubscription(subscription);

        // If the transaction goes through, update the user's information in the DB.
        if (updateUserInfo) {
            User enteredUser = command.getUser();
            final User existingUser = _userDao.getUserFromEmailIfExists(enteredUser.getEmail());
            existingUser.setFirstName(enteredUser.getFirstName());
            existingUser.setLastName(enteredUser.getLastName());
            existingUser.getAddress().setStreet(enteredUser.getAddress().getStreet());
            existingUser.getAddress().setCity(enteredUser.getAddress().getCity());
            existingUser.getAddress().setState(enteredUser.getAddress().getState());
            existingUser.getAddress().setZip(enteredUser.getAddress().getZip());
            _userDao.saveUser(existingUser);
        }

        try {
            _purchaseManager.sendSubscriptionThankYouEmail(subscription);
        } catch (TransformerException e) {
            // ignore for now -- error was logged
        } catch (IOException e) {
            // ignore for now -- error was logged
        }
    }

    protected ModelAndView onSubmit(Object o) throws Exception {
        // Override the standard behavior so that the user sees a different URL
        // in their browser, and if they hit "refresh", it doesn't resubmit their form.

        final RedirectView redirectView = new RedirectView("/thankyou.page");
        redirectView.setContextRelative(true);

        // URL looks something like:
        // ...thankyou.page?state=CA&email=ndp%40mac.com&price=%2416.95&longName=THISTHING&firstName=Andy&lastName=P.&host=gw.net&expires=Feb+1+2005&updated=Mar+3

        SubscribeCommand command = (SubscribeCommand) o;
        redirectView.addStaticAttribute("state", command.getState());
        redirectView.addStaticAttribute("email", command.getUser().getEmail());
        redirectView.addStaticAttribute("firstName", command.getUser().getFirstName());
        redirectView.addStaticAttribute("lastName", command.getUser().getLastName());
        redirectView.addStaticAttribute("price", command.getSubscriptionPrice());
        redirectView.addStaticAttribute("longName", command.getSubscriptionProduct().getLongName());
        redirectView.addStaticAttribute("expires", command.getSubscription().getExpires());
        redirectView.addStaticAttribute("updated", command.getSubscription().getUpdated());
        redirectView.addStaticAttribute("host", command.getHost());

        return new ModelAndView(redirectView);
    }

    public PurchaseManager getPurchaseManager() {
        return _purchaseManager;
    }

    public void setPurchaseManager(PurchaseManager purchaseManager) {
        _purchaseManager = purchaseManager;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

}
