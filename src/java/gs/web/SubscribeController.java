/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SubscribeController.java,v 1.9 2005/05/19 21:34:34 apeterson Exp $
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
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class SubscribeController extends org.springframework.web.servlet.mvc.SimpleFormController {
    private static Log _log = LogFactory.getLog(SubscribeController.class);

    private PurchaseManager _purchaseManager;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    private static final String HOST_PARAM = "host";
    private static final String EMAIL_PARAM = "email";
    private static final String STATE_PARAM = "state";
    private static final String URL_PARAM = "url";
    private static final String URL_LABEL_PARAM = "urlLabel";
    private static final String RENEW_PARAM = "renew"; // boolean set if the user is attempting renewal


    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {

        // Suck the information out of the URL, if available.
        User user;
        String paramEmail = httpServletRequest.getParameter(EMAIL_PARAM);
        if (!StringUtils.isEmpty(paramEmail)) {
            _log.debug("formBackingObject: found user in params: " + paramEmail);
            User existingUser = _userDao.getUserFromEmailIfExists(paramEmail);
            if (existingUser != null) {
                user = existingUser;
            } else {
                user = new User();
                user.setEmail(paramEmail);
            }
        } else {
            user = new User();
        }

        final SubscriptionProduct product = SubscriptionProduct.ONE_YEAR_SUB;
        final Price price = _purchaseManager.getSubscriptionPrice(user, product, new Date());


        SubscribeCommand command = new SubscribeCommand(user, product, price);
        _log.debug("formBackingObject: created command");


        if (!StringUtils.isEmpty(httpServletRequest.getParameter(RENEW_PARAM))) {
            command.setTryingToRenew(true);
        }


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

        SubscribeCommand command = (SubscribeCommand) o;
        if (command.isTryingToRenew() &&
                !_subscriptionDao.isUserSubscribed(command.getUser(), command.getSubscriptionProduct(), new Date())) {

            String message = "The membership for the email address " +
                    command.getUser().getEmail() +
                    " has already expired. Annual membership renewal is only " +
                    command.getPrice().asString();

            errors.reject("param0", new Object[]{message}, "Membership expired.");
        }
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

// getId() should always be null, but in case the code above changes, check first.
        final User existingUser = _userDao.getUserFromEmailIfExists(user.getEmail());
        if (existingUser != null) {
            updateUserInfo = true;
// We set this flag and only update if the transaction goes through.
// The thought was to prevent some bogus screwing around from updating what
// information we have.
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.getAddress().setStreet(user.getAddress().getStreet());
            existingUser.getAddress().setCity(user.getAddress().getCity());
            existingUser.getAddress().setState(user.getAddress().getState());
            existingUser.getAddress().setZip(user.getAddress().getZip());
            user = existingUser;
        }
        _userDao.saveUser(user);

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

        _log.info("User " + user.getEmail() + " whose name is " + user.getFirstName() + " " +
                user.getLastName() + ", and he/she bought a subscription for " + cardInfo.getTransactionAmount());
        command.setSubscription(subscription);

        try {
            _purchaseManager.sendSubscriptionThankYouEmail(subscription);
        } catch (TransformerException e) {
// ignore for now -- error was logged
        } catch (IOException e) {
// ignore for now -- error was logged
        }
    }

    protected ModelAndView onSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, BindException e) throws Exception {

        // Add the appropriate cookie to log the user in.
        CookieGenerator cookieGenerator = new CookieGenerator();
        cookieGenerator.setCookieDomain("greatschools.net");
        cookieGenerator.setCookieMaxAge(-1);
        cookieGenerator.setCookieName("MEMBER");
        cookieGenerator.setCookiePath("/");

        SubscribeCommand command = (SubscribeCommand) o;
        cookieGenerator.addCookie(httpServletResponse, command.getSubscription().getUser().getId().toString());


        return super.onSubmit(httpServletRequest, httpServletResponse, o, e);
    }

    protected ModelAndView onSubmit(Object o) throws Exception {
// Override the standard behavior so that the user sees a different URL
// in their browser, and if they hit "refresh", it doesn't resubmit their form.

        final RedirectView redirectView = new RedirectView("/thankyou.page");
        redirectView.setContextRelative(true);

// URL looks something like:
// ...thankyou.page?state=CA&email=ndp%40mac.com&price=%2416.95&longName=THISTHING&firstName=Andy&lastName=P.&host=gw.net&expires=Feb+1+2005&updated=Mar+3
        SimpleDateFormat df = new SimpleDateFormat("MMMMM d, yyyy");

        SubscribeCommand command = (SubscribeCommand) o;
        redirectView.addStaticAttribute("state", command.getState());
        redirectView.addStaticAttribute("email", command.getUser().getEmail());
        redirectView.addStaticAttribute("firstName", command.getUser().getFirstName());
        redirectView.addStaticAttribute("lastName", command.getUser().getLastName());
        redirectView.addStaticAttribute("price", command.getSubscriptionPrice());
        redirectView.addStaticAttribute("longName", command.getSubscriptionProduct().getLongName());
        redirectView.addStaticAttribute("expires", df.format(command.getSubscription().getExpires()));
        redirectView.addStaticAttribute("updated", df.format(command.getSubscription().getUpdated()));
        redirectView.addStaticAttribute("host", command.getHost());
        if (!State.PA.equals(command.getState())) {
            redirectView.addStaticAttribute("hasYellowFlags", "1");
        }
        redirectView.addStaticAttribute("memberId", command.getSubscription().getUser().getId().toString());

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

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }
}
