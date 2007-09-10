/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SubscriptionSummaryTest.java,v 1.21 2007/09/10 17:43:02 dlee Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.IUserDao;
import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import static org.easymock.EasyMock.*;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;

import java.util.*;

/**
 * Tests SubscriptionSummaryController.
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class SubscriptionSummaryTest extends BaseControllerTestCase {
    private SubscriptionSummaryController _controller;
    private ISchoolDao _schoolDao;
    private IUserDao _userDao;
    private Validator _emailValidator;
    private Validator _stateValidator;
    private Validator _schoolIdValidator;
    private static final String EMAIL = "someemail@greatschools.net";

    protected void setUp() throws Exception {
        super.setUp();

        _schoolDao = createMock(ISchoolDao.class);
        _userDao = createMock(IUserDao.class);
        _emailValidator = createMock(Validator.class);
        _stateValidator = createMock(Validator.class);
        _schoolIdValidator = createMock(Validator.class);

        _controller = new SubscriptionSummaryController();
        _controller.setCommandClass(NewsletterCommand.class);
        _controller.setCommandName("newsCmd");
        _controller.setFormView("/community/newsletters/popup/mss/page3");
        _controller.setSuccessView("/community/newsletters/popup/mss/page3");
        _controller.setSchoolDao(_schoolDao);
        _controller.setUserDao(_userDao);

        List<Validator> onLoadValidators = new ArrayList<Validator>();
        onLoadValidators.add(_emailValidator);
        onLoadValidators.add(_stateValidator);
        onLoadValidators.add(_schoolIdValidator);
        _controller.setOnLoadValidators(onLoadValidators);
    }

    public void testValidation() {
        NewsletterCommand command = new NewsletterCommand();
        BindException errors = new BindException(command, "");
        replay(_userDao);
        replay(_schoolDao);
        expect(_emailValidator.supports(NewsletterCommand.class)).andReturn(true);
        _emailValidator.validate(command, errors);
        expect(_stateValidator.supports(NewsletterCommand.class)).andReturn(true);
        _stateValidator.validate(command, errors);
        expect(_schoolIdValidator.supports(NewsletterCommand.class)).andReturn(true);
        _schoolIdValidator.validate(command, errors);
        replay(_emailValidator);
        replay(_stateValidator);
        replay(_schoolIdValidator);
        _controller.onBindOnNewForm(getRequest(), command, errors);

        verify(_userDao);
        verify(_schoolDao);
        verify(_emailValidator);
        verify(_stateValidator);
        verify(_schoolIdValidator);
    }

    public void testReferenceData() {
        NewsletterCommand command = new NewsletterCommand();
        command.setEmail(EMAIL);
        command.setState(State.CA);
        command.setSchoolId(1);

        BindException errors = new BindException(command, "");
        User user = new User();
        user.setId(1);
        user.setEmail(EMAIL);
        Set<Subscription> subscriptions = new HashSet<Subscription>();
        for (SubscriptionProduct product : SubscriptionProduct.getNewsletterProducts()) {
            Subscription sub = new Subscription();

            sub.setProduct(product);
            if (product == SubscriptionProduct.MYSTAT) {
                sub.setSchoolId(1);
            }
            sub.setState(State.CA);
            sub.setUser(user);
            subscriptions.add(sub);
        }
        user.setSubscriptions(subscriptions);
        expect(_userDao.findUserFromEmailIfExists(EMAIL)).andReturn(user);
        replay(_userDao);
        School school = new School();
        school.setId(1);
        school.setName("My school");
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school); // for mystats
        replay(_schoolDao);
        Map model = _controller.referenceData(getRequest(), command, errors);
        verify(_userDao);
        verify(_schoolDao);
        assertFalse(errors.hasErrors());

        Set myMsHs = (Set) model.get(SubscriptionSummaryController.MODEL_SET_MS_HS);
        assertNotNull(myMsHs);
        assertEquals(2, myMsHs.size());

        Set myNthGrader = (Set) model.get(SubscriptionSummaryController.MODEL_SET_NTH_GRADER);
        assertNotNull(myNthGrader);
        _log.debug(myNthGrader);
        assertTrue(5 <= myNthGrader.size());

        assertNotNull(model.get(SubscriptionSummaryController.MODEL_PARENT_ADVISOR));
        assertNotNull(model.get(SubscriptionSummaryController.MODEL_SCHOOL_NAME));
        assertEquals("My school", model.get(SubscriptionSummaryController.MODEL_SCHOOL_NAME).toString());
        assertEquals(command.getEmail(), model.get(SubscriptionSummaryController.MODEL_EMAIL).toString());
        assertNotNull(model.get(SubscriptionSummaryController.MODEL_SPONSOR));
        assertEquals("Expect subscription product long name to be in model",
                SubscriptionProduct.SPONSOR_OPT_IN.getLongName(),
                model.get(SubscriptionSummaryController.MODEL_SPONSOR));
    }
}
