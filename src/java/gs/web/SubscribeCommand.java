/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SubscribeCommand.java,v 1.1 2005/05/11 01:16:54 apeterson Exp $
 */
package gs.web;

import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.payment.CreditCardInfo;
import gs.data.state.State;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The purpose is ...
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class SubscribeCommand {

    private Log _log = LogFactory.getLog(SubscribeCommand.class);

    private String _creditCardType = "visa";
    private String _expirationMonth;
    private String _expirationYear;
    private String _agree;
    private final User _user = new User();
    private final CreditCardInfo _card = new CreditCardInfo();
    private SubscriptionProduct _subscriptionProduct;
    private Subscription _subscription;
    private State _state;

    private String _url;
    private String _urlLabel;

    public User getUser() {
        return _user;
    }

    public String getCreditCardType() {
        return _creditCardType;
    }

    public void setCreditCardType(String creditCardType) {
        _creditCardType = creditCardType;
    }

    public String getCreditCardNumber() {
        return _card.getNumber();
    }

    public void setCreditCardNumber(String creditCardNumber) {
        _card.setNumber(creditCardNumber);
    }

    public String getExpirationMonth() {
        return _expirationMonth;
    }

    public void setExpirationMonth(String expirationMonth) {
        _expirationMonth = expirationMonth;
        _card.setExpirationMonth(Integer.parseInt(expirationMonth));
    }

    public String getExpirationYear() {
        return _expirationYear;
    }

    public void setExpirationYear(String expirationYear) {
        _expirationYear = expirationYear;
        _card.setExpirationYear(Integer.parseInt(expirationYear));
    }

    public String getAgree() {
        return _agree;
    }

    public void setAgree(String agree) {
        _agree = agree;
    }

    public CreditCardInfo getCard() {
        return _card;
    }

    public void setSubscriptionProduct(SubscriptionProduct oneYearSub) {
        _subscriptionProduct = oneYearSub;
    }

    public SubscriptionProduct getSubscriptionProduct() {
        return _subscriptionProduct;
    }

    public Subscription getSubscription() {
        return _subscription;
    }

    public void setSubscription(Subscription subscription) {
        _subscription = subscription;
    }

    public String getUrlLabel() {
        return _urlLabel;
    }

    public void setUrlLabel(String urlLabel) {
        _urlLabel = urlLabel;
    }

    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        _url = url;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }
}
