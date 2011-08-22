package gs.web.school.review;

import gs.data.community.*;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.school.IHeldSchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.review.CategoryRating;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Poster;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.data.util.email.EmailHelperFactory;
import gs.data.util.email.MockJavaMailSender;
import gs.web.BaseControllerTestCase;
import gs.web.community.IReportContentService;
import gs.web.community.registration.EmailVerificationReviewOnlyEmail;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author dlee
 * @author thuss
 */
public class SchoolReviewsAjaxControllerTest extends BaseControllerTestCase {
    SchoolReviewsAjaxController _controller;
    IReviewDao _reviewDao;
    IUserDao _userDao;
    IAlertWordDao _alertWordDao;
    ISubscriptionDao _subscriptionDao;
    private IReportContentService _reportContentService;
    User _user;
    School _school;
    ReviewCommand _command;
    BindException _errors;
    MockJavaMailSender _sender;
    private IReportedEntityDao _reportedEntityDao;
    private ExactTargetAPI _exactTargetAPI;
    private IHeldSchoolDao _heldSchoolDao;
    private EmailVerificationReviewOnlyEmail _emailVerificationEmail;
    ReviewHelper _reviewHelper;

    public void setUp() throws Exception {
        super.setUp();
        _exactTargetAPI = createStrictMock(ExactTargetAPI.class);
        
        _controller = new SchoolReviewsAjaxController();


        _reviewDao = createMock(IReviewDao.class);
        _userDao = createMock(IUserDao.class);
        
        _subscriptionDao = createMock(ISubscriptionDao.class);

        _reportContentService = createMock(IReportContentService.class);
        _reportedEntityDao = createStrictMock(IReportedEntityDao.class);
        _heldSchoolDao = createStrictMock(IHeldSchoolDao.class);

        _school = new School();
        _school.setDatabaseState(State.CA);
        _school.setId(6397);
        _school.setName("Lowell High School");
        _school.setCity("San Francisco");
        _school.setActive(true);

        _request.setAttribute("school", _school);

        _user = new User();
        _user.setEmail("ssprouse@greatschools.org");
        _user.setId(1);

        _alertWordDao = createMock(IAlertWordDao.class);

        _command = new ReviewCommand();
        _command.setEmail(_user.getEmail());
        _errors = new BindException(_command, "");

        _sender = new MockJavaMailSender();
        _sender.setHost("mail.greatschools.org");

        _emailVerificationEmail = createStrictMock(EmailVerificationReviewOnlyEmail.class);

        _controller.setEmailHelperFactory((EmailHelperFactory) getApplicationContext().getBean(EmailHelperFactory.BEAN_ID));
        _controller.getEmailHelperFactory().setMailSender(_sender);
        _controller.setAlertWordDao(_alertWordDao);
        _controller.setReportContentService(_reportContentService);
        _controller.setReportedEntityDao(_reportedEntityDao);
        _controller.setExactTargetAPI(_exactTargetAPI);
        _controller.setHeldSchoolDao(_heldSchoolDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setEmailVerificationReviewOnlyEmail(_emailVerificationEmail);

        _reviewHelper = new ReviewHelper();
        _controller.setReviewHelper(_reviewHelper);
    }

    public void testSubmitExistingUserNoPasswordNoExistingReview() throws Exception {
        User user = new User();
        user.setEmail("ssprouse@greatschools.org");
        user.setId(1);

        _command.setComments("safe safe safe safe safe safe safe safe safe safe safe safe.");
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(_user);
        replay(_userDao);

        //Since there is a user expect that subscriptionDao.findMssSubscriptionsByUser is called if the isMssSub is true.
        _command.setMssSub(true);
        List<Subscription> existingSubs = new ArrayList<Subscription>();
        expect(_subscriptionDao.findMssSubscriptionsByUser(user)).andReturn(existingSubs);
        
        //Test that the MYSTAT is added to the user's subscriptions.
        List<Subscription> newMyStatSubs = new ArrayList<Subscription>();
        Subscription newMyStatSub = new Subscription(user,SubscriptionProduct.MYSTAT,_school);
        newMyStatSubs.add(newMyStatSub);
        _subscriptionDao.addNewsletterSubscriptions(user,newMyStatSubs);

        expect(_reviewDao.findReview(_user, _school)).andReturn(null);
        _reviewDao.saveReview((Review) anyObject());

        _emailVerificationEmail.sendSchoolReviewVerificationEmail(eq(getRequest()), eq(user), isA(String.class));

        //should not send "review posted email" if user not logged in with password

        replay(_reviewDao);
        replay(_exactTargetAPI);

        replay(_subscriptionDao);
        expect(_heldSchoolDao.isSchoolOnHoldList(_school)).andReturn(false);
        replay(_heldSchoolDao);
        replay(_emailVerificationEmail);
        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.handle(_request, _response, _command, _errors);
        verify(_userDao);
        verify(_reviewDao);
        verify(_subscriptionDao);
        verify(_exactTargetAPI);
        verify(_heldSchoolDao);
        verify(_emailVerificationEmail);
    }

    public void testSubmitDuplicateKey() throws Exception {
        User user = new User();
        user.setEmail("ssprouse@greatschools.org");
        user.setId(1);

        _command.setComments("safe safe safe safe safe safe safe safe safe safe safe safe.");
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(_user);
        replay(_userDao);

        expect(_reviewDao.findReview(_user, _school)).andReturn(null);
        _reviewDao.saveReview((Review) anyObject());
        expectLastCall().andThrow(new DataIntegrityViolationException("testSubmitDuplicateKey"));

        //should not send "review posted email" on duplicate exception

        replay(_reviewDao);
        replay(_exactTargetAPI);

        replay(_subscriptionDao);
        expect(_heldSchoolDao.isSchoolOnHoldList(_school)).andReturn(false);
        replay(_heldSchoolDao);
        replay(_emailVerificationEmail);
        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        ModelAndView rval = _controller.handle(_request, _response, _command, _errors);
        verify(_userDao);
        verify(_reviewDao);
        verify(_subscriptionDao);
        verify(_exactTargetAPI);
        verify(_heldSchoolDao);
        verify(_emailVerificationEmail);
        assertNull("Expect empty response on exception", rval);
    }

    public void testSubmitExistingUserWithPasswordNoExistingReview() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("ssprouse@greatschools.org");
        user.setPlaintextPassword("abcdefg");

        _command.setComments("safe safe safe safe safe safe safe safe safe safe safe safe.");
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(user);
        replay(_userDao);

        //Since there is a user expect that subscriptionDao.findMssSubscriptionsByUser is called if the isMssSub is true.
        _command.setMssSub(true);
        List<Subscription> existingSubs = new ArrayList<Subscription>();
        expect(_subscriptionDao.findMssSubscriptionsByUser(user)).andReturn(existingSubs);

        List<Subscription> newMyStatSubs = new ArrayList<Subscription>();
        Subscription newMyStatSub = new Subscription(user,SubscriptionProduct.MYSTAT,_school);
        newMyStatSubs.add(newMyStatSub);
        //Test that the MYSTAT is added to the user's subscriptions.
        _subscriptionDao.addNewsletterSubscriptions(user,newMyStatSubs);

        expect(_reviewDao.findReview(user, _school)).andReturn(null);
        _reviewDao.saveReview((Review) anyObject());

        _exactTargetAPI.sendTriggeredEmail(eq("review_posted_trigger"), isA(User.class), isA(Map.class));

        replay(_reviewDao);
        replay(_exactTargetAPI);

        replay(_subscriptionDao);
        expect(_heldSchoolDao.isSchoolOnHoldList(_school)).andReturn(false);
        replay(_heldSchoolDao);
        replay(_emailVerificationEmail);
        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.handle(_request, _response, _command, _errors);
        verify(_userDao);
        verify(_reviewDao);
        verify(_subscriptionDao);
        verify(_exactTargetAPI);
        verify(_heldSchoolDao);
        verify(_emailVerificationEmail);
    }

    public void testSubmitExistingUserNoVerifiedEmailNoPasswordNoExistingReview() throws Exception {

        _command.setComments("safe safe safe safe safe safe safe safe safe safe safe safe.");
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(_user);
        replay(_userDao);

        //Even if the user has not verified, we check if the user has subscription to the MyStat for the school is isMssSub is true.
        _command.setMssSub(true);
        List<Subscription> existingSubs = new ArrayList<Subscription>();
        expect(_subscriptionDao.findMssSubscriptionsByUser(_user)).andReturn(existingSubs);

        List<Subscription> newMyStatSubs = new ArrayList<Subscription>();
        Subscription newMyStatSub = new Subscription(_user,SubscriptionProduct.MYSTAT,_school);
        newMyStatSubs.add(newMyStatSub);
        //Add the MYSTAT Sub even if the user is has not verified the email address.        
        _subscriptionDao.addNewsletterSubscriptions(_user,newMyStatSubs);

        expect(_reviewDao.findReview(_user, _school)).andReturn(null);
        _reviewDao.saveReview((Review) anyObject());

        //no longer send "review posted" email unless user is logged in with a password
        //_exactTargetAPI.sendTriggeredEmail(eq("review_posted_trigger"), isA(User.class), isA(Map.class));

        _emailVerificationEmail.sendSchoolReviewVerificationEmail(eq(getRequest()), eq(_user), isA(String.class));

        replay(_reviewDao);
        replay(_exactTargetAPI);

        replay(_subscriptionDao);
        expect(_heldSchoolDao.isSchoolOnHoldList(_school)).andReturn(false);
        replay(_heldSchoolDao);
        replay(_emailVerificationEmail);
        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.handle(_request, _response, _command, _errors);
        verify(_userDao);
        verify(_reviewDao);
        verify(_subscriptionDao);
        verify(_exactTargetAPI);
        verify(_heldSchoolDao);
        verify(_emailVerificationEmail);
    }

    private void checkHoldListGeneric(String initialStatus, String expectedStatus) {
        Review review = new Review();
        review.setStatus(initialStatus);
        expect(_heldSchoolDao.isSchoolOnHoldList(_school)).andReturn(true);
        replay(_heldSchoolDao);
        _controller.checkHoldList(_school, review);
        verify(_heldSchoolDao);
        assertEquals("Expecting review to be in held status", expectedStatus, review.getStatus());
    }

    public void testCheckHoldListPublish() {
        checkHoldListGeneric("p", "h");
    }

    public void testCheckHoldListDisable() {
        checkHoldListGeneric("d", "h");
    }

    public void testCheckHoldListUnprocessed() {
        checkHoldListGeneric("u", "h");
    }

    public void testCheckHoldListProvisionalPublish() {
        checkHoldListGeneric("pp", "ph");
    }

    public void testCheckHoldListProvisionalDisable() {
        checkHoldListGeneric("pd", "ph");
    }

    public void testCheckHoldListProvisionalUnprocessed() {
        checkHoldListGeneric("pu", "ph");
    }

    public void testCheckHoldListNoop() {
        Review review = new Review();
        review.setStatus("p");
        expect(_heldSchoolDao.isSchoolOnHoldList(_school)).andReturn(false);
        replay(_heldSchoolDao);
        _controller.checkHoldList(_school, review);
        verify(_heldSchoolDao);
        assertEquals("Expecting review to keep original status", "p", review.getStatus());
    }

    public void testSubmitExistingUserReviewRejected() throws Exception {
        User moderationUser = new User();
        moderationUser.setId(-1);
        moderationUser.setEmail("moderation@greatschools.org");
        moderationUser.setUserProfile(new UserProfile());
        moderationUser.getUserProfile().setScreenName("gs_alert_word_filter");

        //Set the Mss subscription to false.Therefore _subscriptionDao.findMssSubscriptionsByUser should not be called.
        //However we verify if the user has subscription to the school to unsubscribe the user.
        // In this case the user is not subscribed therefore no need to unsubscribe.
        _command.setMssSub(false);
        expect(_subscriptionDao.findMssSubscriptionByUserAndSchool(_user,_school)).andReturn(null);

        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(_user);
        replay(_userDao);

        Review review = new Review();
        review.setStatus("r");
        review.setId(1);
        review.setComments("this school sucks");

        _command.setComments("this school sucks"); //review comments can no longer be empty

        Map<IAlertWordDao.alertWordTypes, Set<String>> alertWordMap = new HashMap<IAlertWordDao.alertWordTypes, Set<String>>();
        Set<String> warningWords = new HashSet<String>();
        warningWords.add("sucks");
        alertWordMap.put(IAlertWordDao.alertWordTypes.WARNING, warningWords);


        expect(_reviewDao.findReview(_user, _school)).andReturn(review);
        _reviewDao.saveReview(review);

        expect(_alertWordDao.getAlertWords(review.getComments())).andReturn(alertWordMap);

        _reportedEntityDao.deleteReportsFor(ReportedEntity.ReportedEntityType.schoolReview, 1);

        expect(_reportContentService.getModerationEmail()).andReturn("moderation@greatschools.org");

        _reportContentService.reportContent(moderationUser, _user, getRequest(), review.getId(), ReportedEntity.ReportedEntityType.schoolReview, "Review contained warning words (sucks)");

        replay(_alertWordDao);
        replay(_reviewDao);
        replay(_reportContentService);
        replay(_subscriptionDao);
        replay(_reportedEntityDao);
        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.handle(_request, _response, _command, _errors);
        verify(_alertWordDao);
        verify(_userDao);
        verify(_reviewDao);
        verify(_subscriptionDao);
        verify(_reportContentService);
        verify(_reportedEntityDao);
    }

    public void testErrorJson() throws Exception {
        _errors.reject("bad", "this is a bad");
        _errors.reject("bad", "this is really bad");
        _controller.errorJSON(getResponse(), _errors);

        assertEquals("text/x-json", getResponse().getContentType());
        //{"status":true,"errors":["this is a bad","this is really bad"]}
        assertEquals("{\"status\":false,\"reviewPosted\":false,\"errors\":[\"this is a bad\",\"this is really bad\"]}", getResponse().getContentAsString());
    }

    public void testSuccessJson() throws Exception {
        _controller.successJSON(getResponse());
        assertEquals("text/x-json", getResponse().getContentType());
        //{"status":true,"errors":["this is a bad","this is really bad"]}
        assertEquals("{\"status\":true}", getResponse().getContentAsString());
    }

    public void testErrorRest() throws Exception {
        _errors.rejectValue("confirmEmail", "addPR_error_confirmation_email", "The confirmation email is not the same as your email.");
        _errors.rejectValue("comments", "addPR_error_comments", "Please enter a review or rating.");
        _controller.errorXML(getResponse(), _errors);

        assertEquals("application/xml", getResponse().getContentType());
        String lineSeparator = System.getProperty("line.separator");
        assertEquals("<errors>" + lineSeparator +
                "<error key=\"addPR_error_confirmation_email\">The confirmation email is not the same as your email.</error>" + lineSeparator +
                "<error key=\"addPR_error_comments\">Please enter a review or rating.</error>" + lineSeparator +
                "</errors>" + lineSeparator, getResponse().getContentAsString());
    }

    public void testSuccessRest() throws Exception {
        _controller.successXML(getResponse());
        assertEquals("application/xml", getResponse().getContentType());
        assertEquals("<success/>", getResponse().getContentAsString());
    }

    public void testExistingUserEmptyReviewNonEmptyCategoryRating() throws Exception {
        Review r = new Review();
        r.setComments("this review has comments");
        _command.setComments("");
        _command.setOverall(CategoryRating.RATING_2);
        replay(_reviewDao);
        _controller.setReviewDao(_reviewDao);
        _reviewHelper.updateReview(r, _school, _command);
        assertEquals(CategoryRating.RATING_2, r.getQuality());
        verify(_reviewDao);
    }

    public void testExistingUserNonEmptyReviewAndEmptyOverallRating() throws Exception {
        Review r = new Review();
        r.setComments("old comment");
        r.setQuality(CategoryRating.RATING_4);
        r.setTeachers(CategoryRating.RATING_1);
        r.setParents(CategoryRating.RATING_2);
        r.setPrincipal(CategoryRating.RATING_3);
        r.setSubmitter("dlee");
        r.setNote("note");

        _command.setComments("new comments");
        _command.setOverall(null);

        _controller.setReviewDao(_reviewDao);
        _reviewHelper.updateReview(r, _school, _command);
        assertEquals(_command.getComments(), r.getComments());
        assertEquals(CategoryRating.RATING_4, r.getQuality());
        assertEquals(CategoryRating.RATING_1, r.getTeachers());
        assertEquals(CategoryRating.RATING_2, r.getParents());
        assertEquals(CategoryRating.RATING_3, r.getPrincipal());
        assertNull(r.getProcessDate());
        assertNull(r.getSubmitter());
        assertNull(r.getNote());
        assertTrue(DateUtils.isSameDay(new Date(), r.getPosted()));
    }
    
    public void testSubmitNewUser() throws Exception {
        _command.setComments("safe safe safe safe safe safe safe safe safe safe.");
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(null);
        _userDao.saveUser((User) anyObject());
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(_user);
        replay(_userDao);

        //Since a new user is created expect that subscriptionDao.findMssSubscriptionsByUser is called if the isMssSub is true.
        _command.setMssSub(true);
        List<Subscription> existingSubs = new ArrayList<Subscription>();
        expect(_subscriptionDao.findMssSubscriptionsByUser(_user)).andReturn(existingSubs);

        List<Subscription> newMyStatSubs = new ArrayList<Subscription>();
        Subscription newMyStatSub = new Subscription(_user,SubscriptionProduct.MYSTAT,_school);
        newMyStatSubs.add(newMyStatSub);
        //Test that the MYSTAT is added to the user's subscriptions.
        _subscriptionDao.addNewsletterSubscriptions(_user,newMyStatSubs);
        
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

        _controller.handle(_request, _response, _command, _errors);
        verify(_userDao);
        verify(_reviewDao);
        verify(_subscriptionDao);
    }



    public void testExistingReviewRatingUpdated() throws Exception {
        Review r = new Review();
        r.setComments("old comment");
        r.setQuality(CategoryRating.RATING_4);
        r.setParents(CategoryRating.RATING_4);
        r.setPrincipal(CategoryRating.RATING_4);
        r.setSubmitter("dlee");

        _command.setComments("new comments");
        _command.setOverall(CategoryRating.RATING_1);
        _command.setTeacher(CategoryRating.RATING_1);
        _command.setParent(CategoryRating.RATING_1);
        _command.setPrincipal(CategoryRating.DECLINE_TO_STATE);
        _command.setPoster(Poster.PARENT);

        replay(_reviewDao);

        _controller.setReviewDao(_reviewDao);

        _reviewHelper.updateReview(r, _school, _command);

        assertEquals(_command.getComments(), r.getComments());
        assertEquals(CategoryRating.RATING_1, r.getQuality());
        assertEquals(CategoryRating.RATING_1, r.getParents());
        assertEquals(CategoryRating.RATING_1, r.getTeachers());
        assertEquals(CategoryRating.DECLINE_TO_STATE, r.getPrincipal());

        verify(_reviewDao);
    }

    public void testExistingReviewRatingUpdatedForStudents() throws Exception {
        Review r = new Review();
        r.setComments("old comment");
        r.setTeachers(CategoryRating.RATING_4);
        r.setSubmitter("dlee");

        _command.setComments("new comments");
        _command.setTeacher(CategoryRating.RATING_1);
        _command.setPoster(Poster.STUDENT);

        _controller.setReviewDao(_reviewDao);

        _reviewHelper.updateReview(r, _school, _command);

        assertEquals(CategoryRating.RATING_1, r.getTeachers());
    }

    public void testPFacilitiesSet() throws Exception {
        Review r = new Review();
        r.setPFacilities(CategoryRating.DECLINE_TO_STATE);
        r.setSubmitter("dlee");
        _school.setLevelCode(LevelCode.PRESCHOOL);

        _command.setPFacilitiesAsString(CategoryRating.RATING_1.getName());
        _command.setPoster(Poster.PARENT);

        _controller.setReviewDao(_reviewDao);

        _reviewHelper.updateReview(r, _school, _command);

        assertEquals(CategoryRating.RATING_1, r.getPFacilities());
    }


    public void testRatingsOverwrittenWhenDeclined() throws Exception {
        Review r = new Review();
        r.setPrincipal(CategoryRating.RATING_4);

        _command.setPrincipalAsString("0");
        _command.setPoster(Poster.PARENT);

        _reviewHelper.updateReview(r, _school, _command);

        assertEquals(_command.getComments(), r.getComments());
        assertEquals(CategoryRating.DECLINE_TO_STATE, r.getPrincipal());
    }

    public void testUserNotCreatedWithoutRequiredFields() throws Exception {
        _errors.rejectValue("email", "Please provide your email address");
        _command = new ReviewCommand();
        _command.setComments("spam spam spam spam spam spam spam spam spam spam spam spam spam spam spam spam spam spam.");

        replay(_userDao);
        replay(_reviewDao);

        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);

        _controller.handle(_request, _response, _command, _errors);
        verify(_userDao);
        verify(_reviewDao);
    }

    public void testCommandCommentFailsValidation() throws Exception {
        _command = new ReviewCommand();
        _command.setComments("spam spam spam spam spam spam");

        _controller = (SchoolReviewsAjaxController) getApplicationContext().getBean("postReview");

        Validator[] validators = _controller.getValidators();

        for (Validator validator : validators) {
            validator.validate(_command, _errors);
        }

        List errors = _errors.getFieldErrors();

        assertNotNull("Errors should contain email validation error", errors);
        assertTrue("Errors should contain several validation errors", errors.size() > 2);
    }


    public void testMssSubUserAlreadySubscribed() throws Exception {
        User moderationUser = new User();
        moderationUser.setId(-1);
        moderationUser.setEmail("moderation@greatschools.org");
        moderationUser.setUserProfile(new UserProfile());
        moderationUser.getUserProfile().setScreenName("gs_alert_word_filter");

        //Set the Mss subscription to false.Therefore _subscriptionDao.findMssSubscriptionsByUser should not be called.
        //However we verify if the user has subscription to the school to unsubscribe the user.
        //In this case the user is subscribed therefore unsubscribe.
        _command.setMssSub(false);
        Subscription sub = new Subscription();
        sub.setId(1);
        expect(_subscriptionDao.findMssSubscriptionByUserAndSchool(_user, _school)).andReturn(sub);
        _subscriptionDao.removeSubscription(sub.getId());

        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(_user);
        replay(_userDao);

        Review review = new Review();
        review.setStatus("r");
        review.setId(1);
        review.setComments("this school sucks");

        _command.setComments("this school sucks"); //review comments can no longer be empty

        Map<IAlertWordDao.alertWordTypes, Set<String>> alertWordMap = new HashMap<IAlertWordDao.alertWordTypes, Set<String>>();
        Set<String> warningWords = new HashSet<String>();
        warningWords.add("sucks");
        alertWordMap.put(IAlertWordDao.alertWordTypes.WARNING, warningWords);


        expect(_reviewDao.findReview(_user, _school)).andReturn(review);
        _reviewDao.saveReview(review);

        expect(_alertWordDao.getAlertWords(review.getComments())).andReturn(alertWordMap);

        _reportedEntityDao.deleteReportsFor(ReportedEntity.ReportedEntityType.schoolReview, 1);

        expect(_reportContentService.getModerationEmail()).andReturn("moderation@greatschools.org");

        _reportContentService.reportContent(moderationUser, _user, getRequest(), review.getId(), ReportedEntity.ReportedEntityType.schoolReview, "Review contained warning words (sucks)");

        replay(_alertWordDao);
        replay(_reviewDao);
        replay(_reportContentService);
        replay(_subscriptionDao);
        replay(_reportedEntityDao);
        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.handle(_request, _response, _command, _errors);
        verify(_alertWordDao);
        verify(_userDao);
        verify(_reviewDao);
        verify(_subscriptionDao);
        verify(_reportContentService);
        verify(_reportedEntityDao);
    }


    public void testNullMssSubUser() throws Exception {
        User moderationUser = new User();
        moderationUser.setId(-1);
        moderationUser.setEmail("moderation@greatschools.org");
        moderationUser.setUserProfile(new UserProfile());
        moderationUser.getUserProfile().setScreenName("gs_alert_word_filter");

        //Set command's mssSub to null, which means that the mss sub checkbox was not displayed on the view and therefore do nothing.
        _command.setMssSub(null);

        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(_user);
        replay(_userDao);

        Review review = new Review();
        review.setStatus("r");
        review.setId(1);
        review.setComments("this school sucks");

        _command.setComments("this school sucks"); //review comments can no longer be empty

        Map<IAlertWordDao.alertWordTypes, Set<String>> alertWordMap = new HashMap<IAlertWordDao.alertWordTypes, Set<String>>();
        Set<String> warningWords = new HashSet<String>();
        warningWords.add("sucks");
        alertWordMap.put(IAlertWordDao.alertWordTypes.WARNING, warningWords);


        expect(_reviewDao.findReview(_user, _school)).andReturn(review);
        _reviewDao.saveReview(review);

        expect(_alertWordDao.getAlertWords(review.getComments())).andReturn(alertWordMap);

        _reportedEntityDao.deleteReportsFor(ReportedEntity.ReportedEntityType.schoolReview, 1);

        expect(_reportContentService.getModerationEmail()).andReturn("moderation@greatschools.org");

        _reportContentService.reportContent(moderationUser, _user, getRequest(), review.getId(), ReportedEntity.ReportedEntityType.schoolReview, "Review contained warning words (sucks)");

        replay(_alertWordDao);
        replay(_reviewDao);
        replay(_reportContentService);
        replay(_subscriptionDao);
        replay(_reportedEntityDao);
        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.handle(_request, _response, _command, _errors);
        verify(_alertWordDao);
        verify(_userDao);
        verify(_reviewDao);
        verify(_subscriptionDao);
        verify(_reportContentService);
        verify(_reportedEntityDao);
    }

    public void testAddMssSubForSchoolUserHasNullMyStatSubs() throws Exception {
        User user = new User();
        user.setId(1);

        List<Subscription> subs = null;
        expect(_subscriptionDao.findMssSubscriptionsByUser(user)).andReturn(subs);

        List<Subscription> myStatSubs = new ArrayList<Subscription>();
        Subscription myStatSub = new Subscription(user,SubscriptionProduct.MYSTAT,_school);
        myStatSubs.add(myStatSub);
        _subscriptionDao.addNewsletterSubscriptions(user, myStatSubs);

        replay(_subscriptionDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.addMssSubForSchool(user, _school);
        verify(_subscriptionDao);

    }

    public void testAddMssSubForSchoolUserHasZeroMyStatSubs() throws Exception {
        User user = new User();
        user.setId(1);

        List<Subscription> subs = new ArrayList<Subscription>();
        expect(_subscriptionDao.findMssSubscriptionsByUser(user)).andReturn(subs);
        List<Subscription> myStatSubs = new ArrayList<Subscription>();
        Subscription myStatSub = new Subscription(user,SubscriptionProduct.MYSTAT,_school);
        myStatSubs.add(myStatSub);
        _subscriptionDao.addNewsletterSubscriptions(user, myStatSubs);

        replay(_subscriptionDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.addMssSubForSchool(user, _school);
        verify(_subscriptionDao);

    }

    public void testAddMssSubForSchoolUserAlreadyHas4OrMoreMyStatSubs() throws Exception {
        User user = new User();
        user.setId(1);

        List<Subscription> subs = new ArrayList<Subscription>();
        Subscription someOtherSub = new Subscription();
        subs.add(someOtherSub);
        someOtherSub = new Subscription();
        subs.add(someOtherSub);
        someOtherSub = new Subscription();
        subs.add(someOtherSub);
        someOtherSub = new Subscription();
        subs.add(someOtherSub);

        expect(_subscriptionDao.findMssSubscriptionsByUser(user)).andReturn(subs);

        replay(_subscriptionDao);

        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.addMssSubForSchool(user, _school);

        verify(_subscriptionDao);
    }

    public void testRemoveMssSubForSchoolUserHasSubscription() throws Exception {
        User user = new User();
        user.setId(1);

        Subscription sub = new Subscription();
        sub.setId(1);
        expect(_subscriptionDao.findMssSubscriptionByUserAndSchool(user, _school)).andReturn(sub);
        _subscriptionDao.removeSubscription(sub.getId());
        replay(_subscriptionDao);

        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.removeMssSubForSchool(user, _school);

        verify(_subscriptionDao);
    }

    public void testRemoveMssSubForSchoolUserHasNoSubscription() throws Exception {
        User user = new User();
        user.setId(1);

        expect(_subscriptionDao.findMssSubscriptionByUserAndSchool(user, _school)).andReturn(null);
        replay(_subscriptionDao);

        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.removeMssSubForSchool(user, _school);

        verify(_subscriptionDao);
    }

}