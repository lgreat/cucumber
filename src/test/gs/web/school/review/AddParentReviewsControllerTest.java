package gs.web.school.review;

import gs.data.community.*;
import gs.data.school.School;
import gs.data.school.review.CategoryRating;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.data.util.email.EmailHelperFactory;
import gs.data.util.email.MockJavaMailSender;
import gs.web.BaseControllerTestCase;
import static org.easymock.EasyMock.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.BindException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class AddParentReviewsControllerTest extends BaseControllerTestCase {
    AddParentReviewsController _controller;
    IReviewDao _reviewDao;
    IUserDao _userDao;
    ISubscriptionDao _subscriptionDao;
    User _user;
    School _school;
    ReviewCommand _command;
    BindException _errors;
    MockJavaMailSender _sender;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new AddParentReviewsController();

        _reviewDao = createMock(IReviewDao.class);
        _userDao = createMock(IUserDao.class);
        _subscriptionDao = createMock(ISubscriptionDao.class);

        _school = new School();
        _school.setDatabaseState(State.CA);
        _school.setId(1);

        _request.setAttribute("school", _school);

        _user = new User();
        _user.setEmail("dlee@greatschools.net");
        _user.setId(1);

        _command = new ReviewCommand();
        _command.setEmail(_user.getEmail());
        _errors = new BindException(_command, "");

        _sender = new MockJavaMailSender();
        _sender.setHost("mail.greatschools.net");

        _controller.setEmailHelperFactory((EmailHelperFactory) getApplicationContext().getBean(EmailHelperFactory.BEAN_ID));
        _controller.getEmailHelperFactory().setMailSender(_sender);
    }

    public void testSubmitNewUser() throws Exception {
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(null);
        _userDao.saveUser((User) anyObject());
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(_user);
        replay(_userDao);

        _reviewDao.saveReview((Review) anyObject());
        replay(_reviewDao);

        //new user so we add an entry into list member that indicates where we got their email from
        Subscription sub = new Subscription(_user, SubscriptionProduct.RATING, _school.getDatabaseState());
        sub.setSchoolId(_school.getId());
        _subscriptionDao.saveSubscription(sub);
        replay(_subscriptionDao);

        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setSubscriptionDao(_subscriptionDao);       
        
        _controller.onSubmit(_request, _response, _command, _errors);
        verify(_userDao);
        verify(_reviewDao);
        verify(_subscriptionDao);
    }

    public void testSubmitExistingUserNoExistingReview() throws Exception {
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(_user);
        replay(_userDao);

        expect(_reviewDao.findReview(_user, _school)).andReturn(null);
        _reviewDao.saveReview((Review) anyObject());
        replay(_reviewDao);

        replay(_subscriptionDao);
        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.onSubmit(_request, _response, _command, _errors);
        verify(_userDao);
        verify(_reviewDao);
        verify(_subscriptionDao);
    }

    public void testEmptyReviewWithRatingMarkedActive() {
        _command.setComments("");
        _command.setOverallAsString("1");
        
        _controller.setReviewDao(_reviewDao);
        Review r = _controller.createOrUpdateReview(_user, _school, _command, false);
        assertEquals("a", r.getStatus());
    }

    public void testBadWords() throws Exception {
        String commentStart = "the word that best describes this school is ";
        String commentEnd = ". that pretty much sums it up.";

        String [] badwords = new String [] {
            "fuck",
            "poop ",
            "poop.",
            "poop,",
            "poopie",
            " ass ",
            " .ass ",
            " ,ass ",
            " ass ",
            " ass.",
            " ass,",
            "faggot",
            " gay ",
            " .gay ",
            " ,gay ",
            " gay ",
            " gay.",
            " gay,",
            "nigger",
            "shit",
            " prick ",
            " prick.",
            " prick,",
            "ass-kicker",
            "suck",
            "asshole",
            " dick ",
            " dick.",
            " dick,",
            "Satan",
            "dickhead",
            " piss ",
            " piss.",
            " piss,",            
        };

        for (int i=0; i<badwords.length; i++) {
            String text = commentStart + badwords[i] + commentEnd;
            assertTrue(text+ ": has a bad word in it", _controller.hasBadWord(text));

            text = commentStart + badwords[i];
            assertTrue(text+ ": has a bad word in it", _controller.hasBadWord(text));

            text = badwords[i] + commentEnd;
            assertTrue(text+ ": has a bad word in it", _controller.hasBadWord(text));
        }

        String [] goodWords = new String [] {
            "pissingly awesome",
            "gayingly awesome",
            "prickingly awesome",
            "gayingly awesome",
            "dickingly awesome",
        };

        for (int i=0; i<goodWords.length; i++) {
            String text = commentStart + badwords[i] + commentEnd;
            assertTrue(text+ ": does not have a bad word.", _controller.hasBadWord(text));

            text = commentStart + badwords[i];
            assertTrue(text+ ": does not have a bad word", _controller.hasBadWord(text));

            text = badwords[i] + commentEnd;
            assertTrue(text+ ": does not have a bad word", _controller.hasBadWord(text));
        }
    }

    public void testCreateReviewRejected() throws Exception {
        _command.setComments("fuck, this page is the shit");
        Review r = _controller.createOrUpdateReview(_user, _school, _command, true);

        assertEquals("r", r.getStatus());
    }

    public void testCreateReview() throws Exception {

        _command.setPrincipalAsString("1");
        _command.setTeacherAsString("2");
        _command.setActivitiesAsString("3");
        _command.setParentAsString("4");
        _command.setSafetyAsString("5");
        _command.setOverallAsString("3");

        _command.setComments("this school rocks");
        _command.setPosterAsString("parent");
        _command.setAllowContact(false);
        _command.setFirstName("dave");

        Review r = _controller.createOrUpdateReview(_user, _school, _command, true);
        assertEquals(CategoryRating.RATING_1, r.getPrincipal());
        assertEquals(CategoryRating.RATING_2,  r.getTeachers());
        assertEquals(CategoryRating.RATING_3, r.getActivities());
        assertEquals(CategoryRating.RATING_4,  r.getParents());
        assertEquals(CategoryRating.RATING_5, r.getSafety());
        assertEquals(CategoryRating.RATING_3, r.getQuality());

        assertEquals(_command.getComments(), r.getComments());
        assertEquals(_command.getComments(), r.getOriginal());

        assertEquals(_command.getPoster(), r.getPoster());
        assertEquals(_command.isAllowContact(), r.isAllowContact());
        assertTrue(r.isAllowName());
    }

    public void testErrorJson() throws Exception {
        _errors.reject("bad","this is a bad");
        _errors.reject("bad", "this is really bad");
        _controller.errorJSON(getResponse(), _errors);

        assertEquals("text/x-json", getResponse().getContentType());
        //{"status":true,"errors":["this is a bad","this is really bad"]}
        assertEquals("{\"status\":false,\"errors\":[\"this is a bad\",\"this is really bad\"]}", getResponse().getContentAsString());
    }

    public void testSuccessJson() throws Exception {
        _controller.successJSON(getResponse());
        assertEquals("text/x-json", getResponse().getContentType());
        //{"status":true,"errors":["this is a bad","this is really bad"]}
        assertEquals("{\"status\":true}", getResponse().getContentAsString());
    }

    public void xtestSendEmailReal() throws Exception {
        _user.setEmail("dlee@greatschools.net");
        String comments = "this school rocks!";

        _controller.getEmailHelperFactory().setMailSender((JavaMailSender)getApplicationContext().getBean("mailSender"));
        _controller.sendMessage(_user, comments, State.CA);
    }

    public void testMssSignUp() throws Exception {
        _command.setWantMssNL(true);

        expect(_userDao.findUserFromEmailIfExists("dlee@greatschools.net")).andReturn(_user);

        Subscription sub = new Subscription(_user, SubscriptionProduct.MYSTAT, _school.getDatabaseState());
        sub.setSchoolId(_school.getId());
        _subscriptionDao.addNewsletterSubscriptions(_user, Arrays.asList(sub));

        expect(_reviewDao.findReview(_user, _school)).andReturn(null);
        _reviewDao.saveReview((Review)anyObject());
        replay(_reviewDao);
        replay(_userDao);
        replay(_subscriptionDao);

        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.onSubmit(_request, _response, _command, _errors);

        verify(_reviewDao);
        verify(_userDao);
        verify(_subscriptionDao);
    }

    public void testMssSignUpWhenMssAlreadyMaxedOut() throws Exception {
        _command.setWantMssNL(true);

        expect(_userDao.findUserFromEmailIfExists("dlee@greatschools.net")).andReturn(_user);

        //max out a user's subscriptions
        Set<Subscription> subscriptions = new HashSet<Subscription>();
        for (int i=0; i < SubscriptionProduct.MAX_MSS_PRODUCT_FOR_ONE_USER; i++) {
            Subscription sub = new Subscription(_user, SubscriptionProduct.MYSTAT, State.CA);
            sub.setSchoolId(i+1);
            subscriptions.add(sub);
        }
        _user.setSubscriptions(subscriptions);

        Review r = new Review();
        r.setId(1234);
        r.setComments("these are my comments");

        //existing user did not leave a comment, so use the old comment
        _command.setComments("");

        expect(_reviewDao.findReview(_user, _school)).andReturn(r);
        _reviewDao.saveReview(r);

        replay(_reviewDao);
        replay(_userDao);
        replay(_subscriptionDao);

        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.onSubmit(_request, _response, _command, _errors);

        verify(_userDao);
        verify(_reviewDao);        
        //no calls to subscription dao to add a newsletter
        verify(_subscriptionDao);
    }

    public void testExistingUserEmptyReviewNonEmptyCategoryRating() throws Exception {
        Review r = new Review();
        r.setComments("this review has comments");

        _command.setComments("");
        _command.setOverall(CategoryRating.RATING_2);
        expect(_reviewDao.findReview(_user, _school)).andReturn(r);
        replay(_reviewDao);

        _controller.setReviewDao(_reviewDao);
        Review review2 = _controller.createOrUpdateReview(_user, _school, _command, false);
        assertEquals(r, review2);
        assertEquals(CategoryRating.RATING_2, review2.getQuality());

        verify(_reviewDao);
    }

    public void testExistingUserNonEmptyReviewAndEmptyOverallRating() throws Exception {
        Review r = new Review();
        r.setComments("old comment");
        r.setQuality(CategoryRating.RATING_4);
        r.setTeachers(CategoryRating.RATING_1);
        r.setParents(CategoryRating.RATING_2);
        r.setPrincipal(CategoryRating.RATING_3);
        r.setSafety(CategoryRating.RATING_5);
        r.setActivities(CategoryRating.RATING_3);

        _command.setComments("new comments");
        _command.setOverall(null);
        expect(_reviewDao.findReview(_user, _school)).andReturn(r);
        replay(_reviewDao);

        _controller.setReviewDao(_reviewDao);
        Review review2 = _controller.createOrUpdateReview(_user, _school, _command, false);
        assertEquals(_command.getComments(), review2.getComments());
        assertEquals(CategoryRating.RATING_4, review2.getQuality());
        assertEquals(CategoryRating.RATING_1, review2.getTeachers());
        assertEquals(CategoryRating.RATING_2, review2.getParents());
        assertEquals(CategoryRating.RATING_3, review2.getPrincipal());
        assertEquals(CategoryRating.RATING_5, review2.getSafety());
        assertEquals(CategoryRating.RATING_3, review2.getActivities());

        verify(_reviewDao);
    }
    

    public void testErrorOnJsonPageShortCircuits() throws Exception {
        _errors.reject("some error");
        _controller.setAjaxPage(true);

        assertNull(_controller.processFormSubmission(_request, _response, _command, _errors));
        assertEquals("text/x-json", _response.getContentType());
    }
}
