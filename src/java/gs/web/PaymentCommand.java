/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: PaymentCommand.java,v 1.1 2005/05/03 01:38:30 apeterson Exp $
 */
package gs.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The purpose is ...
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class PaymentCommand {

    private Log _log = LogFactory.getLog(PaymentCommand.class);
    private String _email;
    private String _firstName;
    private String _lastName;
    private String _address;
    private String _city;
    private String _state;
    private String _zip;
    private String _creditCardType;
    private String _creditCardNumber;
    private int _expirationMonth;
    private int _expirationYear;
    private String _agree;


    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public String getFirstName() {
        return _firstName;
    }

    public void setFirstName(String firstName) {
        _firstName = firstName;
    }

    public String getLastName() {
        return _lastName;
    }

    public void setLastName(String lastName) {
        _lastName = lastName;
    }

    public String getAddress() {
        return _address;
    }

    public void setAddress(String address) {
        _address = address;
    }

    public String getCity() {
        return _city;
    }

    public void setCity(String city) {
        _city = city;
    }

    public String getState() {
        return _state;
    }

    public void setState(String state) {
        _state = state;
    }

    public String getZip() {
        return _zip;
    }

    public void setZip(String zip) {
        _zip = zip;
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

    public int getExpirationMonth() {
        return _expirationMonth;
    }

    public void setExpirationMonth(int expirationMonth) {
        _expirationMonth = expirationMonth;
    }

    public int getExpirationYear() {
        return _expirationYear;
    }

    public void setExpirationYear(int expirationYear) {
        _expirationYear = expirationYear;
    }

    public String getAgree() {
        return _agree;
    }

    public void setAgree(String agree) {
        _agree = agree;
    }

}
