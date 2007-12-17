/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SubscriptionSummaryTest.java,v 1.25 2007/12/17 18:32:17 aroy Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.IUserDao;
import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import static gs.data.community.SubscriptionProduct.*;
import gs.data.community.User;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import static gs.web.community.newsletters.popup.SubscriptionSummaryController.*;
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

        Set myNthMsHs = (Set) model.get(SubscriptionSummaryController.MODEL_SET_NTH_MS_HS);
        assertNotNull(myNthMsHs);

        assertTrue("Expect exactly 8 nth grader newsletters (found " + myNthMsHs.size() + "). " +
                "If this fails, either an unknown newsletter is being added to this category or " +
                "you should update this assert to expect the new # of nth grader newsletters",
                8 == myNthMsHs.size());

        assertNotNull(model.get(SubscriptionSummaryController.MODEL_PARENT_ADVISOR));
        assertNotNull(model.get(SubscriptionSummaryController.MODEL_SCHOOL_NAME));
        assertEquals("My school", model.get(SubscriptionSummaryController.MODEL_SCHOOL_NAME).toString());
        assertEquals(command.getEmail(), model.get(SubscriptionSummaryController.MODEL_EMAIL).toString());
        assertNotNull(model.get(SubscriptionSummaryController.MODEL_SPONSOR));
        assertEquals("Expect subscription product long name to be in model",
                SubscriptionProduct.SPONSOR_OPT_IN.getLongName(),
                model.get(SubscriptionSummaryController.MODEL_SPONSOR));
    }

    public void testSortNewsletterSubs() {
        List<Subscription> newsletterSubs = new ArrayList<Subscription>();
        newsletterSubs.add(createSubscription(MY_HS));
        newsletterSubs.add(createSubscription(MY_FIFTH_GRADER));
        newsletterSubs.add(createSubscription(PARENT_ADVISOR));
        newsletterSubs.add(createSubscription(MY_FOURTH_GRADER));
        newsletterSubs.add(createSubscription(MY_SECOND_GRADER));
        newsletterSubs.add(createSubscription(SPONSOR_OPT_IN));
        newsletterSubs.add(createSubscription(MY_KINDERGARTNER));
        newsletterSubs.add(createSubscription(MYSTAT));
        newsletterSubs.add(createSubscription(COMMUNITY));
        newsletterSubs.add(createSubscription(MY_THIRD_GRADER));
        newsletterSubs.add(createSubscription(MY_FIRST_GRADER));
        newsletterSubs.add(createSubscription(MY_MS));
        assertEquals(MY_MS, newsletterSubs.get(11).getProduct());
        _controller.sortNewsletterSubs(newsletterSubs);
        assertEquals(MYSTAT, newsletterSubs.get(0).getProduct());
        assertEquals(PARENT_ADVISOR, newsletterSubs.get(1).getProduct());
        assertEquals(COMMUNITY, newsletterSubs.get(2).getProduct());
        assertEquals(SPONSOR_OPT_IN, newsletterSubs.get(3).getProduct());
        assertEquals(MY_KINDERGARTNER, newsletterSubs.get(4).getProduct());
        assertEquals(MY_FIRST_GRADER, newsletterSubs.get(5).getProduct());
        assertEquals(MY_SECOND_GRADER, newsletterSubs.get(6).getProduct());
        assertEquals(MY_THIRD_GRADER, newsletterSubs.get(7).getProduct());
        assertEquals(MY_FOURTH_GRADER, newsletterSubs.get(8).getProduct());
        assertEquals(MY_FIFTH_GRADER, newsletterSubs.get(9).getProduct());
        assertEquals(MY_MS, newsletterSubs.get(10).getProduct());
        assertEquals(MY_HS, newsletterSubs.get(11).getProduct());
    }

    public void testSplitList() {
        Map<String, Object> model = new HashMap<String, Object>();
        Set<String> newsletterSubs = _controller.getOrderedNewsletterSet();

        _controller.splitSet(null, model);
        assertNull(model.get(MODEL_FIRST_LIST));
        assertNull(model.get(MODEL_SECOND_LIST));
        _controller.splitSet(newsletterSubs, model);
        assertNull(model.get(MODEL_FIRST_LIST));
        assertNull(model.get(MODEL_SECOND_LIST));

        newsletterSubs.add(MY_MS.getLongName());
        _controller.splitSet(newsletterSubs, model);
        assertNotNull(model.get(MODEL_FIRST_LIST));
        assertEquals(1, model.get(MODEL_FIRST_LIST_SIZE));
        assertNotNull(model.get(MODEL_SECOND_LIST));
        assertEquals(0, model.get(MODEL_SECOND_LIST_SIZE));

        newsletterSubs.add(MY_FIRST_GRADER.getLongName());
        _controller.splitSet(newsletterSubs, model);
        assertNotNull(model.get(MODEL_FIRST_LIST));
        assertEquals(1, model.get(MODEL_FIRST_LIST_SIZE));
        assertEquals(MY_FIRST_GRADER.getLongName(), getNameFromList(model.get(MODEL_FIRST_LIST), 0));
        assertNotNull(model.get(MODEL_SECOND_LIST));
        assertEquals(1, model.get(MODEL_SECOND_LIST_SIZE));
        assertEquals(MY_MS.getLongName(), getNameFromList(model.get(MODEL_SECOND_LIST), 0));

        newsletterSubs.add(MY_HS.getLongName());
        _controller.splitSet(newsletterSubs, model);
        assertNotNull(model.get(MODEL_FIRST_LIST));
        assertEquals(2, model.get(MODEL_FIRST_LIST_SIZE));
        assertEquals(MY_FIRST_GRADER.getLongName(), getNameFromList(model.get(MODEL_FIRST_LIST), 0));
        assertEquals(MY_MS.getLongName(), getNameFromList(model.get(MODEL_FIRST_LIST), 1));
        assertNotNull(model.get(MODEL_SECOND_LIST));
        assertEquals(1, model.get(MODEL_SECOND_LIST_SIZE));
        assertEquals(MY_HS.getLongName(), getNameFromList(model.get(MODEL_SECOND_LIST), 0));

        newsletterSubs.add(MY_SECOND_GRADER.getLongName());
        _controller.splitSet(newsletterSubs, model);
        assertNotNull(model.get(MODEL_FIRST_LIST));
        assertEquals(2, model.get(MODEL_FIRST_LIST_SIZE));
        assertEquals(MY_FIRST_GRADER.getLongName(), getNameFromList(model.get(MODEL_FIRST_LIST), 0));
        assertEquals(MY_SECOND_GRADER.getLongName(), getNameFromList(model.get(MODEL_FIRST_LIST), 1));
        assertNotNull(model.get(MODEL_SECOND_LIST));
        assertEquals(2, model.get(MODEL_SECOND_LIST_SIZE));
        assertEquals(MY_MS.getLongName(), getNameFromList(model.get(MODEL_SECOND_LIST), 0));
        assertEquals(MY_HS.getLongName(), getNameFromList(model.get(MODEL_SECOND_LIST), 1));
    }

    private String getNameFromList(Object oList, int index) {
        List list = (List) oList;
        return String.valueOf(list.get(index));
    }

    private Subscription createSubscription(SubscriptionProduct product) {
        Subscription sub = new Subscription();
        sub.setProduct(product);
        sub.setId(10);
        sub.setState(State.CA);
        sub.setSchoolId(1);
        return sub;
    }
}
