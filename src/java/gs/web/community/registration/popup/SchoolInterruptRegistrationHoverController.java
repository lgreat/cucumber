package gs.web.community.registration.popup;

import gs.web.community.registration.RegistrationController;
import gs.web.community.registration.UserCommand;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteController;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.community.User;
import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.community.WelcomeMessageStatus;
import gs.data.school.School;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class SchoolInterruptRegistrationHoverController extends RegistrationController implements ReadWriteController {

    @Override
    protected boolean hasChildRows() {
        return false;
    }

    protected void loadMssSchoolName(UserCommand userCommand) {
        if (userCommand.getMystatSchoolId() > 0 && userCommand.getMystatSchoolState() != null) {
            School mssSchool =
                    getSchoolDao().getSchoolById(userCommand.getMystatSchoolState(), userCommand.getMystatSchoolId());
            userCommand.setMystatSchoolName(mssSchool.getName());
            userCommand.setState(mssSchool.getDatabaseState());
            userCommand.setCity(mssSchool.getCity());
        }
    }

    @Override
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        loadMssSchoolName((UserCommand) command);
        return super.referenceData(request, command, errors);
    }

    @Override
    public void onBind(HttpServletRequest request, Object command) {
        super.onBind(request, command);
        // make sure state/city are set correctly in userCommand
        loadMssSchoolName((UserCommand) command);        
    }

    @Override
    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        if (isIPBlocked(request)) return new ModelAndView(getErrorView());

        UserCommand userCommand = (UserCommand) command;

        boolean userExists = updateCommandUser(userCommand);
        User user = userCommand.getUser();

        setUsersPassword(user, userCommand, userExists);

        if (isRequireEmailValidation()) {
            sendValidationEmail(request, user, "/account/"); // TODO
        }

        OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
        updateUserProfile(user, userCommand, ot);

        if (user.isEmailProvisional()) {
            user.setEmailValidated();
        }

        user.setWelcomeMessageStatus(WelcomeMessageStatus.NEED_TO_SEND);        

        // save
        getUserDao().updateUser(user);
        // Because of hibernate caching, it's possible for a list_active record
        // (with list_member id) to be commited before the list_member record is
        // committed. Adding this commitOrRollback prevents this.
        ThreadLocalTransactionManager.commitOrRollback();

        // User object loses its session and this might fix that.
        user = getUserDao().findUserFromId(user.getId());
        userCommand.setUser(user);

        ModelAndView mAndV = new ModelAndView();
        try {
            if (userCommand.getPartnerNewsletter()) {
                Subscription subscription = new Subscription();
                subscription.setUser(user);
                subscription.setProduct(SubscriptionProduct.SPONSOR_OPT_IN);
                subscription.setState(userCommand.getUserProfile().getState());
                userCommand.addSubscription(subscription);
            }
            if (userCommand.isMystat()) {
                // subscribe to greatnews and mss for the school
                processNewsletterSubscriptions(userCommand);
            }

            saveSubscriptionsForUser(userCommand, ot);
        } catch (Exception e) {
            // if there is any sort of error prior to notifying community,
            // the user MUST BE ROLLED BACK to provisional status
            // otherwise our database is out of sync with community! Bad!
            _log.error("Unexpected error during registration", e);
            // undo registration
            user.setEmailProvisional(userCommand.getPassword());
            getUserDao().updateUser(user);
            // send to error page
            mAndV.setViewName(getErrorView());
            return mAndV;
        }

        PageHelper.setMemberAuthorized(request, response, user); // auto-log in to community
        mAndV.setViewName("redirect:" + getHoverView());

        return mAndV;
    }

    @Override
    protected void processNewsletterSubscriptions(UserCommand userCommand) {
        User user = userCommand.getUser();
        
        // greatnews
        Subscription communityNewsletterSubscription = new Subscription();
        communityNewsletterSubscription.setUser(user);
        communityNewsletterSubscription.setProduct(SubscriptionProduct.PARENT_ADVISOR);
        communityNewsletterSubscription.setState(userCommand.getState());
        userCommand.addSubscription(communityNewsletterSubscription);

        // mss
        if (userCommand.getMystatSchoolId() > 0 && userCommand.getMystatSchoolState() != null) {
            School school = getSchoolDao().getSchoolById(userCommand.getMystatSchoolState(),
                    userCommand.getMystatSchoolId());
            Subscription mss = new Subscription(user, SubscriptionProduct.MYSTAT, school);
            mss.setUpdatedNow();
            userCommand.addSubscription(mss);
        }
    }
}