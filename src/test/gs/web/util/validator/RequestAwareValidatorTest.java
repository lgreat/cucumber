/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: RequestAwareValidatorTest.java,v 1.1 2006/07/11 21:14:04 dlee Exp $
 */
package gs.web.util.validator;

import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import java.util.HashSet;
import java.util.Set;

/**
 * Test request aware validators
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class RequestAwareValidatorTest extends BaseControllerTestCase {

    private static final Log _log = LogFactory.getLog(RequestAwareValidatorTest.class);

    public void testMaximumMssValidator() {

        IRequestAwareValidator v = new MaximumMssValidator();
        User user = new User();
        user.setEmail("whatever@whatevery.com");

        Set subscriptions = new HashSet();
        user.setSubscriptions(subscriptions);
        Errors errors = new BindException(user, "");

        v.validate(getRequest(), user, errors);
        assertFalse(errors.hasErrors());

        for (int i=0; i < SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER; i++ ) {
            Subscription sub = new Subscription();
            sub.setProduct(SubscriptionProduct.MYSTAT);
            sub.setSchoolId(i);
            sub.setState(State.CA);
            subscriptions.add(sub);
        }
        user.setSubscriptions(subscriptions);

        v.validate(getRequest(), user, errors);
        assertTrue(errors.hasErrors());
        _log.debug(errors.getGlobalError());
    }
}
