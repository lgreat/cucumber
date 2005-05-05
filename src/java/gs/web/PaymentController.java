/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: PaymentController.java,v 1.2 2005/05/05 02:15:40 apeterson Exp $
 */
package gs.web;

import gs.data.community.ISubscriptionDao;
import gs.data.community.IUserDao;
import gs.data.payment.ITransactionAuthorizer;
import gs.data.state.State;
import gs.data.state.StateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The purpose is ...
 * TODO
 * authorize transaction
 * remember "nextUrl" attribute send in, and send the user there on completion.
 * accept "email" passed in.
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class PaymentController extends org.springframework.web.servlet.mvc.SimpleFormController {
    private Log _log = LogFactory.getLog(PaymentController.class);

    private ITransactionAuthorizer _transactionAuthorizer;

    private IUserDao _userDao;

    private ISubscriptionDao _subscriptionDao;


    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        return super.formBackingObject(httpServletRequest);

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
        httpServletRequest.setAttribute("subscriptionStates", sb.toString());
        // TODO return new HashMap("subscriptionStates", sb.toString() );

        return super.referenceData(httpServletRequest);
    }

    protected void onBindAndValidate(HttpServletRequest httpServletRequest, Object o, BindException e) throws Exception {

        // Authorize through verisign

    }

    public ITransactionAuthorizer getTransactionAuthorizer() {
        return _transactionAuthorizer;
    }

    public void setTransactionAuthorizer(ITransactionAuthorizer transactionAuthorizer) {
        _transactionAuthorizer = transactionAuthorizer;
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
