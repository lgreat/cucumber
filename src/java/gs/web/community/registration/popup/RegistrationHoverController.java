package gs.web.community.registration.popup;

import gs.data.state.State;
import gs.web.util.NewSubscriberDetector;
import gs.web.util.UrlUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import gs.web.util.ReadWriteController;
import gs.web.util.validator.UserCommandHoverValidator;
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
 * // TODO: This class is deprecated and may (should?) no longer be in use
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class RegistrationHoverController extends RegistrationController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());

    private boolean _requireEmailValidation = true;

    private ISubscriptionDao _subscriptionDao;

    public static final String BEAN_ID = "/community/registration/popup/registrationHover.page";

    public void onBind(HttpServletRequest request, Object command, BindException errors) throws Exception {

        UserCommand userCommand = (UserCommand) command;
        String gradeNewsletters = (String) request.getAttribute("gradeNewsletters");

        List<UserCommand.NthGraderSubscription> nthGraderSubscriptions = new ArrayList<UserCommand.NthGraderSubscription>();

        for (String grade : StringUtils.split(gradeNewsletters)) {
            nthGraderSubscriptions.add(new UserCommand.NthGraderSubscription(true, SubscriptionProduct.getSubscriptionProduct(grade)));
        }

        userCommand.setGradeNewsletters(nthGraderSubscriptions);

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

        user.setHow(joinTypeToHow(userCommand.getHow()));

        // save
        getUserDao().updateUser(user);

        saveRegistrations(userCommand, user, ot);

        try {
            // GS-7649 Because of hibernate caching, it's possible for a list_active record
            // (with list_member id) to be commited before the list_member record is
            // committed. Adding this commitOrRollback prevents this.
            ThreadLocalTransactionManager.commitOrRollback();

            // subscribe to newsletters
            if (userCommand.getNewsletter()) {
                processNewsletterSubscriptions(userCommand);
            }
            saveSubscriptionsForUser(userCommand, ot);

            //TODO: figure out if we need evar
            //ot.addEvar(new OmnitureTracking.Evar(OmnitureTracking.EvarNumber.RegistrationSegment, "MSL Combo Reg"));
        } catch (Exception e) {
            _log.error("Error in RegistrationHoverController", e);
            mAndV.setViewName(getErrorView());
            return mAndV;
        }

        if (_requireEmailValidation) {
            // Determine redirect URL for validation email
            String emailRedirectUrl = "";

            if (UrlUtil.isDeveloperWorkstation(request.getServerName())) {
                emailRedirectUrl = "/index.page";
            } else {
                emailRedirectUrl = "/";
            }

            sendValidationEmail(request, user, emailRedirectUrl);
        }

        mAndV.setViewName("redirect:" + userCommand.getRedirectUrl());

        return mAndV;
    }

    private void saveRegistrations(RegistrationHoverCommand userCommand, User user, OmnitureTracking ot) {
        State state = userCommand.getState() == null ? userCommand.getState() : State.CA;

        List<Subscription> subscriptions = new ArrayList<Subscription>();

        List<UserCommand.NthGraderSubscription> nthGraderSubscriptions = userCommand.getGradeNewsletters();

        for (UserCommand.NthGraderSubscription sub : nthGraderSubscriptions) {
            subscriptions.add(new Subscription(user, sub.getSubProduct(), state));
        }

        if (userCommand.getNewsletter()) {
            subscriptions.add(new Subscription(user, SubscriptionProduct.getSubscriptionProduct("greatnews"), state));
        }
        if (userCommand.getPartnerNewsletter()) {
            subscriptions.add(new Subscription(user, SubscriptionProduct.getSubscriptionProduct("sponsor"), state));
        }
        if (userCommand.getLdNewsletter()) {
            subscriptions.add(new Subscription(user, SubscriptionProduct.getSubscriptionProduct("learning_dis"), state));
        }

        if (subscriptions.size() > 0) {
            _subscriptionDao.addNewsletterSubscriptions(user, subscriptions);
            NewSubscriberDetector.notifyOmnitureWhenNewNewsLetterSubscriber(user, ot);
        }
    }

    public String joinTypeToHow(String joinType) {
        //TODO: complete
        return null;
    }

    protected UserProfile updateUserProfile(User user, RegistrationHoverCommand userCommand, OmnitureTracking ot) {
        UserProfile userProfile;
        if (user.getUserProfile() != null && user.getUserProfile().getId() != null) {
            // hack to get provisional accounts working in least amount of development time
            userProfile = user.getUserProfile();
            userProfile.setCity(userCommand.getCity());
            userProfile.setScreenName(userCommand.getScreenName());
            userProfile.setState(userCommand.getState());
            userProfile.setHow(joinTypeToHow(""));
        } else {
            // gotten this far, now let's update their user profile

            userProfile = userCommand.getUserProfile();
            userProfile.setHow(joinTypeToHow(""));
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

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }
}