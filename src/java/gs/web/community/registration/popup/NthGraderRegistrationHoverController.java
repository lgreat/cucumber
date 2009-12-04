package gs.web.community.registration.popup;

import gs.web.community.registration.RegistrationController;
import gs.web.community.registration.UserCommand;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteController;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;
import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.state.State;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.lang.StringUtils;


/**
 * @author greatschools.org>
 */

public class NthGraderRegistrationHoverController extends RegistrationController implements ReadWriteController {
    private boolean _requireEmailValidation = true;
    @Override
    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        UserCommand userCommand = (UserCommand) super.formBackingObject(httpServletRequest);
        // note: We have to create the right number of Grade Newsletters here so that the databinder will succeed
        // in binding the properties. Otherwise, it attempts to bind to e.g. userCommand.gradeNewsletters[0] and
        // gets an index out of bounds exception
        // (Since the page is configured to always submit 9 Newsletters, I have to add all 9 here)

        for(SubscriptionProduct myNth : SubscriptionProduct.MY_NTH_GRADER){
            UserCommand.NthGraderSubscription nthSubscription = new UserCommand.NthGraderSubscription(false, myNth);
            if (httpServletRequest.getParameter(myNth.getName()) != null) {
                nthSubscription.setChecked(true);
            }
            userCommand.getGradeNewsletters().add(nthSubscription);
        }

        return userCommand;
    }
   
     public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        if (isIPBlocked(request)) return new ModelAndView(getErrorView());

        UserCommand userCommand = (UserCommand) command;

        boolean userExists = updateCommandUser(userCommand);
        User user = userCommand.getUser();

        setUsersPassword(user, userCommand, userExists);

        if (_requireEmailValidation) {
            sendValidationEmail(user, userCommand, userExists, request);
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

        State state = userCommand.getUserProfile().getState();
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);

        if (null == state) {
        // use a default state for setting subscriptions
            userCommand.getUserProfile().setState(sessionContext.getStateOrDefault());
        } 

        ModelAndView mAndV = new ModelAndView();
        try {
            // complete registration

            processGradeNewsLetters(userCommand,user);

            if (userCommand.getPartnerNewsletter()) {
                Subscription subscription = new Subscription();
                subscription.setUser(user);
                subscription.setProduct(SubscriptionProduct.SPONSOR_OPT_IN);
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
            getUserDao().updateUser(user);
            // send to error page
            mAndV.setViewName(getErrorView());
            return mAndV;
        }
        if (!notifyCommunity(user, userCommand, mAndV, request)) {
            return mAndV; // early exit!
        }

        PageHelper.setMemberAuthorized(request, response, getUserDao().findUserFromEmailIfExists(userCommand.getEmail())); // auto-log in to community
        if(StringUtils.isNotBlank(getHoverView())) {
            userCommand.setRedirectUrl(getHoverView());
        }
        mAndV.setViewName("redirect:" + userCommand.getRedirectUrl());

        return mAndV;
    }


    protected void processGradeNewsLetters(UserCommand userCmd,User user){
        boolean alreadySubscribed = false;
        for(int i=0;i<userCmd.getGradeNewsletters().size();i++){
            if(userCmd.getGradeNewsletters().get(i).getChecked()){
                addMyNth(userCmd.getGradeNewsletters().get(i).getSubProduct(),user,user.getState());
                if(!alreadySubscribed){
                    Subscription subscription = new Subscription();
                    subscription.setUser(user);
                    subscription.setProduct(SubscriptionProduct.PARENT_ADVISOR);
                    subscription.setState(userCmd.getUserProfile().getState());
                    userCmd.addSubscription(subscription);
                    alreadySubscribed = true;
                }
            }
        }
    }

    public void addMyNth(SubscriptionProduct sp,User user, State state){
           Student student = new Student();
           student.setSchoolId(-1);
           student.setGrade(sp.getGrade());
           student.setState(state);
           user.addStudent(student);
           getUserDao().saveUser(user);
    }


    public void setRequireEmailValidation(boolean requireEmailValidation) {
        this._requireEmailValidation = requireEmailValidation;
    }

    public boolean isRequireEmailValidation() {
        return _requireEmailValidation;
    }
}
