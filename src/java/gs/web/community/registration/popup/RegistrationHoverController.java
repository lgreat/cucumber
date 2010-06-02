package gs.web.community.registration.popup;

import gs.data.state.State;
import gs.web.util.NewSubscriberDetector;
import gs.web.util.SitePrefCookie;
import gs.web.util.UrlUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import gs.web.util.ReadWriteController;
import gs.web.community.registration.RegistrationController;
import gs.web.community.registration.UserCommand;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class RegistrationHoverController extends RegistrationController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());

    private boolean _requireEmailValidation = true;

    public static final String BEAN_ID = "/community/registration/popup/registrationHover.page";

    public void onBind(HttpServletRequest request, Object command, BindException errors) throws Exception {

        RegistrationHoverCommand userCommand = (RegistrationHoverCommand) command;
        String[] gradeNewsletters = request.getParameterValues("grades");
        _log.info("gradeNewsletters=" + gradeNewsletters);
        if (gradeNewsletters != null) {
            List<UserCommand.NthGraderSubscription> nthGraderSubscriptions = new ArrayList<UserCommand.NthGraderSubscription>();

            for (String grade : gradeNewsletters) {
                _log.info("Adding " + grade + " to nthGraderSubscriptions");
                nthGraderSubscriptions.add(new UserCommand.NthGraderSubscription(true, SubscriptionProduct.getSubscriptionProduct(grade)));
            }

            userCommand.setGradeNewsletters(nthGraderSubscriptions);
        }

        if (userCommand.isBtsTip()) {
            // if tip version is not valid e/m/h, use e
            if (!"e".equals(userCommand.getBtsTipVersion()) &&
                !"m".equals(userCommand.getBtsTipVersion()) &&
                !"h".equals(userCommand.getBtsTipVersion())) {
                userCommand.setBtsTipVersion("e");
            }
        }

        if (StringUtils.equals("Loading...", userCommand.getCity())) {
            userCommand.setCity(null);
        }

        if (RegistrationHoverCommand.JoinHoverType.ChooserTipSheet == userCommand.getJoinHoverType()) {
            userCommand.setChooserRegistration(true);
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        if (isIPBlocked(request)) return new ModelAndView(getErrorView());

        RegistrationHoverCommand userCommand = (RegistrationHoverCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());
        ModelAndView mAndV = new ModelAndView();
        OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
        boolean userExists = false;

        if (user != null) {
            userExists = true;
            setFieldsOnUserUsingCommand(userCommand, user);
            userCommand.setUser(user);
        } else {
            // only create the user if the user is new
            getUserDao().saveUser(userCommand.getUser());
            user = userCommand.getUser();
        }

        setUsersPassword(user, userCommand, userExists);
        updateUserProfile(user, userCommand, ot);
        // set up defaults for data not collected in hover registration
        if (StringUtils.isEmpty(user.getGender())) {
            user.setGender("u");
        }

        user.setHow(joinTypeToHow(userCommand.getJoinHoverType()));
        
        // save
        getUserDao().updateUser(user);


        try {
            // GS-7649 Because of hibernate caching, it's possible for a list_active record
            // (with list_member id) to be commited before the list_member record is
            // committed. Adding this commitOrRollback prevents this.
            ThreadLocalTransactionManager.commitOrRollback();

            user = getUserDao().findUserFromId(user.getId()); // refresh session
            saveRegistrations(userCommand, user, ot);
        } catch (Exception e) {
            _log.error("Error in RegistrationHoverController", e);
            mAndV.setViewName(getErrorView());
            return mAndV;
        }

        if (_requireEmailValidation) {
            // Determine redirect URL for validation email
            String emailRedirectUrl;

            if (UrlUtil.isDeveloperWorkstation(request.getServerName())) {
                emailRedirectUrl = "/index.page";
            } else {
                emailRedirectUrl = "/";
            }

            sendValidationEmail(request, user, emailRedirectUrl);
        }
        String redirect = userCommand.getRedirectUrl();
       
        SitePrefCookie cookie = new SitePrefCookie(request, response);
       
        if (RegistrationHoverCommand.JoinHoverType.SchoolReview.equals(userCommand.getJoinHoverType())) {
            cookie.setProperty("showHover", "validateEmailSchoolReview");
        } else {
            cookie.setProperty("showHover", "validateEmail");
        }

        mAndV.setViewName("redirect:" + redirect);

        return mAndV;
    }

    protected void setFieldsOnUserUsingCommand(RegistrationHoverCommand userCommand, User user) {
        if (StringUtils.isNotEmpty(userCommand.getFirstName())) {
            user.setFirstName(userCommand.getFirstName());
        }
        if (StringUtils.isNotEmpty(userCommand.getLastName())) {
            user.setLastName(userCommand.getLastName());
        }
        String gender = userCommand.getGender();
        if (StringUtils.isNotEmpty(gender)) {
            user.setGender(userCommand.getGender());
        }
    }

    private void saveRegistrations(RegistrationHoverCommand userCommand, User user, OmnitureTracking ot) {
        State state = userCommand.getState() == null ? userCommand.getState() : State.CA;


        List<Subscription> subscriptions = new ArrayList<Subscription>();

        List<UserCommand.NthGraderSubscription> nthGraderSubscriptions = userCommand.getGradeNewsletters();

        _log.info("nthGraderSubscriptions.size()=" + nthGraderSubscriptions.size());
        for (UserCommand.NthGraderSubscription sub : nthGraderSubscriptions) {
            Student student = new Student();
            student.setSchoolId(-1);
            student.setGrade(sub.getSubProduct().getGrade());
            student.setState(state);
            user.addStudent(student);

        }

        getUserDao().updateUser(user);

        if (userCommand.getNewsletter()) {
            subscriptions.add(new Subscription(user, SubscriptionProduct.getSubscriptionProduct("greatnews"), state));
            if (userCommand.getJoinHoverType() == RegistrationHoverCommand.JoinHoverType.Auto) {
                Subscription sub = new Subscription();
                sub.setUser(user);
                sub.setProduct(SubscriptionProduct.MYSTAT);
                sub.setState(userCommand.getMystatSchoolState());
                sub.setSchoolId(userCommand.getMystatSchoolId());
                subscriptions.add(sub);
            }
        }
        if (userCommand.getPartnerNewsletter()) {
            subscriptions.add(new Subscription(user, SubscriptionProduct.getSubscriptionProduct("sponsor"), state));
        }
        if (userCommand.getLdNewsletter()) {
            subscriptions.add(new Subscription(user, SubscriptionProduct.getSubscriptionProduct("learning_dis"), state));
        }
        if (userCommand.isBtsTip()) {
            subscriptions.add(new Subscription(user, SubscriptionProduct.getSubscriptionProduct("btstip_" + userCommand.getBtsTipVersion()), state));
        }

        if (subscriptions.size() > 0) {
            getSubscriptionDao().addNewsletterSubscriptions(user, subscriptions);
            NewSubscriberDetector.notifyOmnitureWhenNewNewsLetterSubscriber(user, ot);
        }
    }

    public String joinTypeToHow(RegistrationHoverCommand.JoinHoverType joinType) {
        switch (joinType) {
            case Auto:
                return "hover_mss";
            case ChooserTipSheet:
                return "acq_chooserpack";
            case LearningDifficultiesNewsletter:
                return "hover_ld";
            case PostComment:
                return "hover_community";
            case TrackGrade:
                return "hover_greatnews";
            case GlobalHeader:
                return "hover_headerjoin";
            case FooterNewsletter:
                return "hover_footernewsletter";
            case SchoolReview:
                return "hover_review";
            case BTSTip:
                return "hover_btstip";
        }
        return null;
    }

    protected UserProfile updateUserProfile(User user, RegistrationHoverCommand userCommand, OmnitureTracking ot) {
        UserProfile userProfile;
        if (user.getUserProfile() != null && user.getUserProfile().getId() != null) {
            // hack to get provisional accounts working in least amount of development time
            userProfile = user.getUserProfile();
            if (StringUtils.isBlank(userCommand.getCity())) {
                userProfile.setCity(null);
            } else {
                userProfile.setCity(userCommand.getCity());
            }
            userProfile.setScreenName(userCommand.getScreenName());
            userProfile.setState(userCommand.getState());
            userProfile.setHow(joinTypeToHow(userCommand.getJoinHoverType()));
        } else {
            // gotten this far, now let's update their user profile

            userProfile = userCommand.getUserProfile();
            userProfile.setHow(joinTypeToHow(userCommand.getJoinHoverType()));
            userProfile.setUser(user);
            user.setUserProfile(userProfile);

            ot.addSuccessEvent(OmnitureTracking.SuccessEvent.CommunityRegistration);
        }
        if (!StringUtils.isBlank(userCommand.getHow())) {
            user.getUserProfile().setHow(userCommand.getHow());
        }
        user.getUserProfile().setUpdated(new Date());
        user.getUserProfile().setNumSchoolChildren(0);
        return userProfile;
    }

    public boolean isRequireEmailValidation() {
        return _requireEmailValidation;
    }

    public void setRequireEmailValidation(boolean requireEmailValidation) {
        _requireEmailValidation = requireEmailValidation;
    }
}