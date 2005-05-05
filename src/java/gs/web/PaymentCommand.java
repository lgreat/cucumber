/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: PaymentCommand.java,v 1.2 2005/05/05 02:15:40 apeterson Exp $
 */
package gs.web;

import gs.data.community.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The purpose is ...
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class PaymentCommand {

    private Log _log = LogFactory.getLog(PaymentCommand.class);

    private String _creditCardType = "visa";
    private String _creditCardNumber;
    private String _expirationMonth;
    private String _expirationYear;
    private String _agree;
    private final User _user = new User();

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

}
