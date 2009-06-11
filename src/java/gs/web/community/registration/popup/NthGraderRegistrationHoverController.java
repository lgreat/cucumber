package gs.web.community.registration.popup;

import gs.web.community.registration.RegistrationController;
import gs.web.community.registration.UserCommand;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.util.PageHelper;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.geo.IGeoDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.ArrayList;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.lang.StringUtils;


/**
 * @author Nanditha Patury <mailto:npatury@greatschools.net>
 */

public class NthGraderRegistrationHoverController extends RegistrationController {
    private IUserDao _userDao;
    private boolean _requireEmailValidation = true;

    @Override
    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        UserCommand userCommand = (UserCommand) super.formBackingObject(httpServletRequest);
        // note: We have to create the right number of Grade Newsletters here so that the databinder will succeed
        // in binding the properties. Otherwise, it attempts to bind to e.g. userCommand.gradeNewsletters[0] and
        // gets an index out of bounds exception
        // (Since the page is configured to always submit 9 Newsletters, I have to add all 9 here)

        List<Boolean> al = new ArrayList(9);
        for(int x=0;x < 9;x++){
            al.add(new Boolean(false));
        }
        userCommand.setGradeNewsletters(al);
        return userCommand;
    }

     public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        if (isIPBlocked(request)) return new ModelAndView(getErrorView());

        UserCommand userCommand = (UserCommand) command;
System.out.println("--------------------------------------------"+userCommand.getGradeNewsletters().get(0));
        boolean userExists = updateCommandUser(userCommand);
        User user = userCommand.getUser();

        setUsersPassword(user, userCommand, userExists);

        if (_requireEmailValidation) {
            sendValidationEmail(user, userCommand, userExists, request);
        }

        OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
        updateUserProfile(user, userCommand, ot);

        if (hasChildRows()) {
            persistChildren(userCommand);
        }

        if (user.isEmailProvisional()) {
            user.setEmailValidated();
        }

        // save
        _userDao.updateUser(user);
        // Because of hibernate caching, it's possible for a list_active record
        // (with list_member id) to be commited before the list_member record is
        // committed. Adding this commitOrRollback prevents this.
        ThreadLocalTransactionManager.commitOrRollback();

        ModelAndView mAndV = new ModelAndView();
        try {
            // if a user registers for the community through the hover and selects the Parent advisor newsletter subscription
            // and even if this is their first subscription no do send the NL welcome email. -Jira -7968
            if(isChooserRegistration() && (userCommand.getNewsletter())){
                user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                _userDao.updateUser(user);
            }
            // complete registration
            if (userCommand.getNewsletter()) {
                processNewsletterSubscriptions(userCommand);
            }

            if (userCommand.getPartnerNewsletter()) {
                Subscription subscription = new Subscription();
                subscription.setUser(user);
                subscription.setProduct(SubscriptionProduct.SPONSOR_OPT_IN);
                subscription.setState(userCommand.getUserProfile().getState());
                userCommand.addSubscription(subscription);
            }

            if (userCommand.getLdNewsletter()) {
                Subscription subscription = new Subscription();
                subscription.setUser(user);
                subscription.setProduct(SubscriptionProduct.LEARNING_DIFFERENCES);
                subscription.setState(userCommand.getUserProfile().getState());
                userCommand.addSubscription(subscription);
            }

            saveSubscriptionsForUser(userCommand, ot);
        } catch (Exception e) {
            // if there is any sort of error prior to notifying community,
            // the user MUST BE ROLLED BACK to provisional status
            // otherwise our database is out of sync with community! Bad!
            _log.error("Unexpected error during registration", e);
            // undo registration
            user.setEmailProvisional(userCommand.getPassword());
            _userDao.updateUser(user);
            // send to error page
            mAndV.setViewName(getErrorView());
            return mAndV;
        }
        if (!notifyCommunity(user, userCommand, mAndV, request)) {
            return mAndV; // early exit!
        }
        if (!user.isEmailProvisional()) {
            if (!isChooserRegistration()) {
                sendConfirmationEmail(user, userCommand, request);
            }
        }

        PageHelper.setMemberAuthorized(request, response, _userDao.findUserFromEmailIfExists(userCommand.getEmail())); // auto-log in to community
        if(StringUtils.isNotBlank(getHoverView())) {
            userCommand.setRedirectUrl(getHoverView());
        } else if (!isChooserRegistration() && (StringUtils.isEmpty(userCommand.getRedirectUrl()) ||
                !UrlUtil.isCommunityContentLink(userCommand.getRedirectUrl()))) {
            String redirectUrl = "http://" +
                    SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                    "/members/" + user.getUserProfile().getScreenName() + "/profile/interests?registration=1";
            userCommand.setRedirectUrl(redirectUrl);
        }
        mAndV.setViewName("redirect:" + userCommand.getRedirectUrl());

        return mAndV;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public void setRequireEmailValidation(boolean requireEmailValidation) {
        this._requireEmailValidation = requireEmailValidation;
    }

    public boolean isRequireEmailValidation() {
        return _requireEmailValidation;
    }
}
