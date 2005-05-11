/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SubscribeCommand.java,v 1.2 2005/05/11 22:48:37 apeterson Exp $
 */
package gs.web;

import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.state.State;
import gs.data.util.Price;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The purpose is ...
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class SubscribeCommand {

    private Log _log = LogFactory.getLog(SubscribeCommand.class);

    private final User _user;
    private final SubscriptionProduct _subscriptionProduct;
    private final Price _subscriptionPrice;

    private String _creditCardNumber;
    private String _creditCardType = "visa";
    private String _expirationMonth;
    private String _expirationYear;
    private String _agree;
    private Subscription _subscription;
    private State _state;
    private String _host;

    private String _url;
    private String _urlLabel;

    public SubscribeCommand(User user, SubscriptionProduct subscriptionProduct, Price subscriptionPrice) {
        _user = user;
        _subscriptionProduct = subscriptionProduct;
        _subscriptionPrice = subscriptionPrice;
    }

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
        return _creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        _creditCardNumber = creditCardNumber;
    }

    public String getExpirationMonth() {
        return _expirationMonth;
    }

    public void setExpirationMonth(String expirationMonth) {
        _expirationMonth = expirationMonth;
    }

    public String getExpirationYear() {
        return _expirationYear;
    }

    public void setExpirationYear(String expirationYear) {
        _expirationYear = expirationYear;
    }

    public String getAgree() {
        return _agree;
    }

    public void setAgree(String agree) {
        _agree = agree;
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

    public String getSubscriptionPrice() {
        return _subscriptionPrice.asString();
    }

    public Price getPrice() {
        return _subscriptionPrice;
    }

    public String getHost() {
        return _host;
    }

    public void setHost(String host) {
        _host = host;
    }
}
