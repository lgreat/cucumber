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
import org.apache.commons.lang.time.DateUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;

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

        _controller.setEmailHelperFactory((EmailHelperFactory) getApplicationContext().getBean(EmailHelperFactory.BEAN_ID));
        _controller.getEmailHelperFactory().setMailSender(_sender);
        _controller.setAlertWordDao(_alertWordDao);
        _controller.setReportContentService(_reportContentService);
        _controller.setReportedEntityDao(_reportedEntityDao);
        _controller.setExactTargetAPI(_exactTargetAPI);
        _controller.setHeldSchoolDao(_heldSchoolDao);

    }

    public void testSubmitExistingUserNoExistingReview() throws Exception {
        _command.setComments("safe safe safe safe safe safe safe safe safe safe safe safe.");
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(_user);
        replay(_userDao);

        expect(_reviewDao.findReview(_user, _school)).andReturn(null);
        _reviewDao.saveReview((Review) anyObject());
        _exactTargetAPI.sendTriggeredEmail(eq("review_posted_trigger"), isA(User.class), isA(Map.class));

        replay(_reviewDao);
        replay(_exactTargetAPI);

        replay(_subscriptionDao);
        expect(_heldSchoolDao.isSchoolOnHoldList(_school)).andReturn(false);
        replay(_heldSchoolDao);
        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.handle(_request, _response, _command, _errors);
        verify(_userDao);
        verify(_reviewDao);
        verify(_subscriptionDao);
        verify(_exactTargetAPI);
        verify(_heldSchoolDao);
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
        
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(_user);
        replay(_userDao);

        Review review = new Review();
        review.setStatus("r");
        review.setId(1);
        review.setComments("this school sucks");

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

    public void testCreateReview() throws Exception {

        _command.setPrincipalAsString("1");
        _command.setTeacherAsString("2");
        _command.setParentAsString("4");
        _command.setOverallAsString("3");

        _command.setComments("this school rocks");
        _command.setPosterAsString("parent");
        _command.setAllowContact(false);
        _command.setFirstName("dave");

        Review r = _controller.createOrUpdateReview(_user, _school, _command, true,"");

        assertEquals(CategoryRating.RATING_1, r.getPrincipal());
        assertEquals(CategoryRating.RATING_2, r.getTeachers());
        assertEquals(CategoryRating.RATING_4, r.getParents());
        assertEquals(CategoryRating.RATING_3, r.getQuality());

        assertEquals(_command.getComments(), r.getComments());
        assertEquals(_command.getComments(), r.getOriginal());

        assertEquals(_command.getPoster(), r.getPoster());
        assertEquals(_command.isAllowContact(), r.isAllowContact());
        assertTrue(r.isAllowName());
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
        expect(_reviewDao.findReview(_user, _school)).andReturn(r);
        replay(_reviewDao);
        _controller.setReviewDao(_reviewDao);
        Review review2 = _controller.createOrUpdateReview(_user, _school, _command, false,"");
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
        r.setSubmitter("dlee");
        r.setNote("note");

        _command.setComments("new comments");
        _command.setOverall(null);
        expect(_reviewDao.findReview(_user, _school)).andReturn(r);
        replay(_reviewDao);

        _controller.setReviewDao(_reviewDao);
        Review review2 = _controller.createOrUpdateReview(_user, _school, _command, false,"");
        assertEquals(_command.getComments(), review2.getComments());
        assertEquals(CategoryRating.RATING_4, review2.getQuality());
        assertEquals(CategoryRating.RATING_1, review2.getTeachers());
        assertEquals(CategoryRating.RATING_2, review2.getParents());
        assertEquals(CategoryRating.RATING_3, review2.getPrincipal());
        assertNull(review2.getProcessDate());
        assertNull(review2.getSubmitter());
        assertNull(review2.getNote());
        assertTrue(DateUtils.isSameDay(new Date(), review2.getPosted()));

        verify(_reviewDao);
    }
    
    public void testSubmitNewUser() throws Exception {
        _command.setComments("safe safe safe safe safe safe safe safe safe safe.");
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

        _controller.handle(_request, _response, _command, _errors);
        verify(_userDao);
        verify(_reviewDao);
        verify(_subscriptionDao);
    }

    public void testSetRatingsOnReviewPreschoolParent() throws Exception {
        LevelCode levelCode = LevelCode.PRESCHOOL;
        Review review = new Review();
        ReviewCommand reviewCommand = new ReviewCommand();

        reviewCommand.setTeacherAsString("1");
        reviewCommand.setParentAsString("1");
        reviewCommand.setPrincipalAsString("1");
        reviewCommand.setPFacilitiesAsString("1");
        reviewCommand.setOverallAsString("1");
        
        _controller.setRatingsOnReview(levelCode, reviewCommand, review, Poster.PARENT);

        assertEquals(CategoryRating.RATING_1, review.getPTeachers());
        assertEquals(CategoryRating.RATING_1, review.getPParents());
        assertEquals(CategoryRating.RATING_1, review.getPFacilities());
        assertEquals(CategoryRating.RATING_1, review.getPOverall());

        assertNull(review.getPrincipal());
        assertNull(review.getQuality());

    }

    public void testSetRatingsOnReviewParent() throws Exception {
        LevelCode levelCode = LevelCode.ELEMENTARY_HIGH;
        Review review = new Review();
        ReviewCommand reviewCommand = new ReviewCommand();

        reviewCommand.setTeacherAsString("1");
        reviewCommand.setParentAsString("1");
        reviewCommand.setPrincipalAsString("1");
        reviewCommand.setPFacilitiesAsString("1");
        reviewCommand.setOverallAsString("1");

        _controller.setRatingsOnReview(levelCode, reviewCommand, review, Poster.PARENT);

        assertEquals(CategoryRating.RATING_1, review.getTeachers());
        assertEquals(CategoryRating.RATING_1, review.getParents());
        assertEquals(CategoryRating.RATING_1, review.getPrincipal());
        assertEquals(CategoryRating.RATING_1, review.getQuality());

        assertNull(review.getPFacilities());
        assertNull(review.getPOverall());

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

        expect(_reviewDao.findReview(_user, _school)).andReturn(r);
        replay(_reviewDao);

        _controller.setReviewDao(_reviewDao);

        Review review2 = _controller.createOrUpdateReview(_user, _school, _command, false,"");

        assertEquals(_command.getComments(), review2.getComments());
        assertEquals(CategoryRating.RATING_1, review2.getQuality());
        assertEquals(CategoryRating.RATING_1, review2.getParents());
        assertEquals(CategoryRating.RATING_1, review2.getTeachers());
        assertEquals(CategoryRating.DECLINE_TO_STATE, review2.getPrincipal());

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

        expect(_reviewDao.findReview(_user, _school)).andReturn(r);
        replay(_reviewDao);

        _controller.setReviewDao(_reviewDao);

        Review review2 = _controller.createOrUpdateReview(_user, _school, _command, false,"");

        assertEquals(CategoryRating.RATING_1, review2.getTeachers());

        verify(_reviewDao);
    }

    public void testPFacilitiesSet() throws Exception {
        Review r = new Review();
        r.setPFacilities(CategoryRating.DECLINE_TO_STATE);
        r.setSubmitter("dlee");
        _school.setLevelCode(LevelCode.PRESCHOOL);

        _command.setPFacilitiesAsString(CategoryRating.RATING_1.getName());
        _command.setPoster(Poster.PARENT);

        expect(_reviewDao.findReview(_user, _school)).andReturn(r);
        replay(_reviewDao);

        _controller.setReviewDao(_reviewDao);

        Review review2 = _controller.createOrUpdateReview(_user, _school, _command, false,"");

        assertEquals(CategoryRating.RATING_1, review2.getPFacilities());

        verify(_reviewDao);
    }


    public void testRatingsOverwrittenWhenDeclined() throws Exception {
        Review r = new Review();
        r.setPrincipal(CategoryRating.RATING_4);

        _command.setPrincipalAsString("0");
        _command.setPoster(Poster.PARENT);

        expect(_reviewDao.findReview(_user, _school)).andReturn(r);
        replay(_reviewDao);

        _controller.setReviewDao(_reviewDao);

        Review review2 = _controller.createOrUpdateReview(_user, _school, _command, false,"");

        assertEquals(_command.getComments(), review2.getComments());
        assertEquals(CategoryRating.DECLINE_TO_STATE, review2.getPrincipal());

        verify(_reviewDao);
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

}