package gs.web.school.review;

import gs.data.community.*;
import gs.data.school.School;
import gs.data.school.LevelCode;
import gs.data.school.ISchoolDao;
import gs.data.school.SchoolType;
import gs.data.school.review.CategoryRating;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.security.Permission;
import gs.data.util.email.EmailHelper;
import gs.data.util.email.EmailHelperFactory;
import gs.data.util.email.EmailContentHelper;
import static gs.data.util.XMLUtil.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.state.State;
import gs.web.school.SchoolPageInterceptor;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteController;
import gs.web.util.NewSubscriberDetector;
import gs.web.util.UrlBuilder;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author <a href="mailto:dlee@greatschools.org">David Lee</a>
 *         <p/>
 *         http://wiki.greatschools.org/bin/view/Greatschools/ParentReviewWebService
 */
public class AddParentReviewsController extends SimpleFormController implements ReadWriteController {

    protected final static Log _log = LogFactory.getLog(AddParentReviewsController.class);

    private IUserDao _userDao;
    private IReviewDao _reviewDao;
    private ISubscriptionDao _subscriptionDao;
    private EmailContentHelper _emailContentHelper;
    private String _viewName;

    private ISchoolDao _schoolDao;

    private EmailHelperFactory _emailHelperFactory;

    private Boolean _jsonPage = Boolean.FALSE;

    private Boolean _xmlPage = Boolean.FALSE;

    private String _how;

    public static Pattern BAD_WORDS = Pattern.compile(".*(fuck|poop[\\s\\.,]|poopie|[\\s\\.,]ass[\\s\\.,]|faggot|[\\s\\.,]gay[\\s\\.,]|nigger|shit|prick[\\s\\.,]|ass-kicker|suck|asshole|dick[\\s\\.,]|Satan|dickhead|piss[\\s\\.,]).*");

    protected ModelAndView processFormSubmission(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 Object command,
                                                 BindException errors) throws Exception {
        if (errors.hasErrors()) {
            ReviewCommand rc = (ReviewCommand) command;
            if ("xml".equals(rc.getOutput())) return errorXML(response, errors);
            else if ("json".equals(rc.getOutput())) return errorXML(response, errors);
            else if ("html".equals(rc.getOutput()))
                return super.processFormSubmission(request, response, command, errors);
            else if (isXmlPage()) return errorXML(response, errors);
            else if (isJsonPage()) return errorJSON(response, errors);
        }
        return super.processFormSubmission(request, response, command, errors);
    }

    public Map<String,Object> referenceData(HttpServletRequest request) throws Exception {
        Map<String,Object> model = new HashMap<String,Object>();
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user;
        if(PageHelper.isMemberAuthorized(request)){
            user = sessionContext.getUser();
            if (user != null) {
                model.put("validUser", user);
            }
        }

        return model;
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {

        OmnitureTracking omnitureTracking = new CookieBasedOmnitureTracking(request, response);

        ReviewCommand rc = (ReviewCommand) command;


        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);
        String check = request.getParameter("isAddNewParentReview");
        if(!StringUtils.isBlank(check)){
           if (request.getParameter("schoolId") != null && request.getParameter("schoolState") != null) {
            school = _schoolDao.getSchoolById(State.fromString(request.getParameter("schoolState")), Integer.parseInt(request.getParameter("schoolId")));
            }
        }

        User user = getUserDao().findUserFromEmailIfExists(rc.getEmail());
        boolean isNewUser = false;

        //new user - create user, add entry to list_active table,
        if (user == null) {
            user = new User();

            //GS-9756: How field added to this controller so that users created when submitting a review on the Morgan Stanley review landing page get tracked
            if (!StringUtils.isEmpty(_how)) {
                user.setHow(_how);
            }

            user.setEmail(rc.getEmail());
            getUserDao().saveUser(user);
            // Because of hibernate caching, it's possible for a list_active record
            // (with list_member id) to be commited before the list_member record is
            // committed. Adding this commitOrRollback prevents this.
            ThreadLocalTransactionManager.commitOrRollback();
            //this is for unit test purposes.  so I can return any user.
            user = getUserDao().findUserFromEmailIfExists(rc.getEmail());

            Subscription sub = new Subscription(user, SubscriptionProduct.RATING, school.getDatabaseState());
            sub.setSchoolId(school.getId());
            getSubscriptionDao().saveSubscription(sub);
            isNewUser = true;
        }

        //does user want mss? if user maxed out MSS subs, just don't add.  do not throw error message.
        if (rc.isWantMssNL() && !user.hasReachedMaximumMssSubscriptions() && StringUtils.isBlank(check)) {
            Subscription sub = new Subscription(user, SubscriptionProduct.MYSTAT, school.getDatabaseState());
            sub.setSchoolId(school.getId());
            NewSubscriberDetector.notifyOmnitureWhenNewNewsLetterSubscriber(user, omnitureTracking);
            getSubscriptionDao().addNewsletterSubscriptions(user, Arrays.asList(sub));
        }else if(!StringUtils.isBlank(check) && rc.isWantMssNL() && school.getType().equals(SchoolType.PUBLIC)){
            Subscription sub = new Subscription(user, SubscriptionProduct.MYSTAT, school.getDatabaseState());
            sub.setSchoolId(school.getId());
            NewSubscriberDetector.notifyOmnitureWhenNewNewsLetterSubscriber(user, omnitureTracking);
            getSubscriptionDao().addNewsletterSubscriptions(user, Arrays.asList(sub));
        }

         if(!StringUtils.isBlank(check) && rc.isWantMssNL()){
            Subscription sub = new Subscription(user, SubscriptionProduct.PARENT_ADVISOR, school.getDatabaseState());
            sub.setSchoolId(school.getId());
            NewSubscriberDetector.notifyOmnitureWhenNewNewsLetterSubscriber(user, omnitureTracking);
            getSubscriptionDao().addNewsletterSubscriptions(user, Arrays.asList(sub));
         }

        //has user's name changed?
        boolean nameChange = false;
        if (StringUtils.isNotBlank(rc.getFirstName())) {
            user.setFirstName(rc.getFirstName());
            nameChange = true;
        }

        if (StringUtils.isNotBlank(rc.getLastName())) {
            user.setLastName(rc.getLastName());
            nameChange = true;
        }

        if (nameChange) {
            getUserDao().saveUser(user);
        }

        Review r = createOrUpdateReview(user, school, rc, isNewUser,check);
        //save the review
        getReviewDao().saveReview(r);

        //only send them an email if they submitted a message that is not blank
        if ("a".equals(r.getStatus()) && StringUtils.isNotBlank(r.getComments())) {
            sendRejectMessage(user, r.getComments(), school, "rejectEmail.txt");
        } else if ("u".equals(r.getStatus()) && StringUtils.isNotBlank(r.getComments())) {
            sendMessage(user, r.getComments(), school, "communityEmail.txt");
        }


        //trigger the success events
        if (userRatedOneOrMoreCategories(rc)) {
            omnitureTracking.addSuccessEvent(OmnitureTracking.SuccessEvent.ParentRating);
        }

        if (StringUtils.isNotBlank(rc.getComments())) {
            omnitureTracking.addSuccessEvent(OmnitureTracking.SuccessEvent.ParentReview);
        }

        if ((!StringUtils.isBlank(check))) {
            UrlBuilder builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PARENT_REVIEWS_WITH_HOVER);
            return new ModelAndView(new RedirectView(builder.asFullUrl(request)));

        } else {
            if ("xml".equals(rc.getOutput()) || isXmlPage()) return successXML(response);
            else if ("json".equals(rc.getOutput()) || isJsonPage()) return successJSON(response);
            else return successHTML();
        }
    }

    protected static boolean userRatedOneOrMoreCategories(ReviewCommand rc) {
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getActivities()) && null != rc.getActivities()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getOverall()) && null != rc.getOverall()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getParent()) && null != rc.getParent()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getPrincipal()) && null != rc.getPrincipal()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getSafety()) && null != rc.getSafety()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getTeacher()) && null != rc.getTeacher()) return true;

        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getPProgram()) && null != rc.getPProgram()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getPOverall()) && null != rc.getPOverall()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getPParents()) && null != rc.getPParents()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getPTeachers()) && null != rc.getPTeachers()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getPSafety()) && null != rc.getPSafety()) return true;
        if (!CategoryRating.DECLINE_TO_STATE.equals(rc.getPFacilities()) && null != rc.getPFacilities()) return true;
        return false;
    }

    protected Review createOrUpdateReview(final User user, final School school,
                                          final ReviewCommand command, final boolean isNewUser,String check) {

        Review review = null;
        //existing user, check if they have previously left a review for this school
        if (!isNewUser) {
            review = getReviewDao().findReview(user, school);

            if (review != null) {
                //old review is not blank and current review is blank
                if (StringUtils.isNotBlank(review.getComments()) && StringUtils.isBlank(command.getComments())) {
                    //only update the overall quality rating and who they are
                    if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                        review.setPOverall(command.getOverall());
                    } else {
                        review.setQuality(command.getOverall());
                    }
                    review.setPoster(command.getPoster());
                    return review;
                } else {
                    //only set the category rating if one was given
                    if (!CategoryRating.DECLINE_TO_STATE.equals(command.getOverall()) && command.getOverall() != null) {
                        if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                            review.setPOverall(command.getOverall());
                        } else {
                            review.setQuality(command.getOverall());
                        }
                    }

                    review.setPosted(new Date());
                    review.setProcessDate(null);
                    //new review submitted, so set the processor to null
                    review.setSubmitter(null);
                    review.setNote(null);
                    _log.warn("updating a non empty review with a new comment.\nOld Comment: "
                            + review.getComments() +
                            "\nNew Comment: " + command.getComments() + "\nOld processor: " + review.getSubmitter()
                            + "\nOld Note: " + review.getNote());
                }

                if(StringUtils.isNotBlank(check)){
                    if(!LevelCode.PRESCHOOL.equals(school.getLevelCode())){
                        if(!CategoryRating.DECLINE_TO_STATE.equals(command.getTeacher()) && command.getTeacher() != null){
                        review.setTeachers(command.getTeacher());
                        }
                        if(!CategoryRating.DECLINE_TO_STATE.equals(command.getActivities()) && command.getActivities() != null){
                            review.setActivities(command.getActivities());
                        }
                        if(!CategoryRating.DECLINE_TO_STATE.equals(command.getParent()) && command.getParent() != null){
                            review.setParents(command.getParent());
                        }
                        if(!CategoryRating.DECLINE_TO_STATE.equals(command.getSafety()) && command.getSafety() != null){
                            review.setSafety(command.getSafety());
                        }
                        if(!CategoryRating.DECLINE_TO_STATE.equals(command.getPrincipal()) && command.getPrincipal() != null){
                            review.setPrincipal(command.getPrincipal());
                        }
                    }else{
                        if(!CategoryRating.DECLINE_TO_STATE.equals(command.getPFacilities()) && command.getPFacilities() != null){
                            review.setPFacilities(command.getPFacilities());
                        }
                        if(!CategoryRating.DECLINE_TO_STATE.equals(command.getPProgram()) && command.getPProgram() != null){
                            review.setPProgram(command.getPProgram());
                        }
                        if(!CategoryRating.DECLINE_TO_STATE.equals(command.getPSafetyPreschool()) && command.getPSafetyPreschool() != null){
                            review.setPSafety(command.getPSafetyPreschool());
                        }
                        if(!CategoryRating.DECLINE_TO_STATE.equals(command.getPTeachersPreschool()) && command.getPTeachersPreschool() != null){
                            review.setPTeachers(command.getPTeachersPreschool());
                        }
                        if(!CategoryRating.DECLINE_TO_STATE.equals(command.getPParentsPreschool()) && command.getPParentsPreschool() != null){
                            review.setPParents(command.getPParentsPreschool());
                        }
                    }
                }
            }
        }

        if (null == review) {
            review = new Review();

            review.setUser(user);
            review.setSchool(school);

            if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
                review.setPFacilities(command.getPFacilities());
                review.setPParents(command.getPParentsPreschool());
                review.setPProgram(command.getPProgram());
                review.setPSafety(command.getPSafetyPreschool());
                review.setPTeachers(command.getPTeachersPreschool());
                review.setPOverall(command.getOverall());
            } else {
                review.setPrincipal(command.getPrincipal());
                review.setTeachers(command.getTeacher());
                review.setActivities(command.getActivities());
                review.setParents(command.getParent());
                review.setSafety(command.getSafety());
                review.setQuality(command.getOverall());
            }
        }
        review.setHow(command.getClient());
        review.setPoster(command.getPoster());
        review.setComments(command.getComments());
        review.setOriginal(command.getComments());
        review.setAllowContact(command.isAllowContact());

        if (StringUtils.isNotEmpty(command.getFirstName()) || StringUtils.isNotEmpty(command.getLastName())) {
            review.setAllowName(true);
        }

        review.setStatus("u");

        if (StringUtils.isNotBlank(review.getComments())) {
            if (review.getComments().length() < 25) {
                review.setStatus("a");
            }

            if (hasBadWord(review.getComments())) {
                review.setStatus("r");
            }
        } else {
            review.setStatus("a");
        }
        return review;
    }

    protected boolean hasBadWord(final String comments) {
        Matcher m = BAD_WORDS.matcher(comments);
        return m.matches();
    }

    protected void sendMessage(final User user, final String comments, final School school, String emailTemplate) throws MessagingException, IOException {

        if (user.getUndeliverable()) {
            _log.warn("Not sending to user marked undeliverable: " + user);
            return;
        }

        EmailHelper emailHelper = getEmailHelperFactory().getEmailHelper();
        emailHelper.setSubject("Thanks for your feedback");
        emailHelper.setFromEmail("editorial@greatschools.org");
        emailHelper.setFromName("GreatSchools");

        emailHelper.setToEmail(user.getEmail());
        emailHelper.readHtmlFromResource("gs/web/school/review/" + emailTemplate);

        emailHelper.setSentToCustomMessage(emailHelper.getHTML_THIS_EMAIL_SENT_TO_EMAIL_MSG());
        emailHelper.addInlineReplacement("EMAIL", user.getEmail());
        emailHelper.addInlineReplacement("STATE", school.getDatabaseState().getAbbreviation());
        emailHelper.addInlineReplacement("USER_COMMENTS", comments);
        emailHelper.addInlineReplacement("SCHOOL_NAME", school.getName());
        emailHelper.addInlineReplacement("SCHOOL_ID", school.getId().toString());
        _emailContentHelper.setCityAndLocalQuestions(school, emailHelper.getInlineReplacements(), "ParentReviewEmail");

        emailHelper.send();
    }

    protected void sendRejectMessage(final User user, final String comments, final School school, String emailTemplate) throws MessagingException, IOException {

        if (user.getUndeliverable()) {
            _log.warn("Not sending to user marked undeliverable: " + user);
            return;
        }

        EmailHelper emailHelper = getEmailHelperFactory().getEmailHelper();
        emailHelper.setSubject("Thanks for your feedback");
        emailHelper.setFromEmail("editorial@greatschools.org");
        emailHelper.setFromName("GreatSchools");

        emailHelper.setToEmail(user.getEmail());
        emailHelper.readPlainTextFromResource("gs/web/school/review/" + emailTemplate);

        emailHelper.setSentToCustomMessage(emailHelper.getHTML_THIS_EMAIL_SENT_TO_EMAIL_MSG());
        emailHelper.addInlineReplacement("EMAIL", user.getEmail());
        emailHelper.addInlineReplacement("STATE", school.getDatabaseState().getAbbreviation());
        emailHelper.addInlineReplacement("USER_COMMENTS", comments);
        emailHelper.addInlineReplacement("SCHOOLNAME", school.getName());
        emailHelper.addInlineReplacement("SCHOOLID", school.getId().toString());

        emailHelper.send();
    }


    protected ModelAndView errorJSON(HttpServletResponse response, BindException errors) throws IOException {
        StringBuffer buff = new StringBuffer(400);
        buff.append("{\"status\":false,\"errors\":");
        buff.append("[");
        List messages = errors.getAllErrors();

        for (Iterator iter = messages.iterator(); iter.hasNext();) {
            ObjectError error = (ObjectError) iter.next();
            buff.append("\"").append(error.getDefaultMessage()).append("\"");
            if (iter.hasNext()) {
                buff.append(",");
            }
        }
        buff.append("]}");
        response.setContentType("text/x-json");
        response.getWriter().print(buff.toString());
        response.getWriter().flush();
        return null;
    }

    protected ModelAndView errorXML(HttpServletResponse response, BindException errors) throws IOException, ParserConfigurationException, TransformerException {
        Document doc = getDocument("errors");
        for (Object e : errors.getAllErrors()) {
            ObjectError error = (ObjectError) e;
            Element errorElem = appendElement(doc, "error", error.getDefaultMessage());
            errorElem.setAttribute("key", error.getCode());
        }
        response.setContentType("application/xml");
        serializeDocument(response.getWriter(), doc);
        response.getWriter().flush();
        return null;
    }

    protected ModelAndView successJSON(HttpServletResponse response) throws IOException {
        response.setContentType("text/x-json");
        response.getWriter().print("{\"status\":true}");
        response.getWriter().flush();
        return null;
    }

    protected ModelAndView successXML(HttpServletResponse response) throws IOException {
        response.setContentType("application/xml");
        response.getWriter().print("<success/>");
        response.getWriter().flush();
        return null;
    }

    protected ModelAndView successHTML() {
        ModelAndView mAndV = new ModelAndView();
        mAndV.setViewName(getSuccessView());
        return mAndV;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public void setEmailContentHelper(EmailContentHelper emailContentHelper) {
        _emailContentHelper = emailContentHelper;
    }

    public Boolean isJsonPage() {
        return _jsonPage;
    }

    public void setJsonPage(Boolean jsonPage) {
        _jsonPage = jsonPage;
    }

    public Boolean isXmlPage() {
        return _xmlPage;
    }

    public void setXmlPage(Boolean xmlPage) {
        _xmlPage = xmlPage;
    }

    public EmailHelperFactory getEmailHelperFactory() {
        return _emailHelperFactory;
    }

    public void setEmailHelperFactory(EmailHelperFactory emailHelperFactory) {
        _emailHelperFactory = emailHelperFactory;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public String getHow() {
        return _how;
    }

    public void setHow(String how) {
        _how = how;
    }
}
