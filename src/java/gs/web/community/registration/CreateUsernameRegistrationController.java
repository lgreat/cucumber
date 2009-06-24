package gs.web.community.registration;

import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.validator.UserCommandValidator;
import gs.web.util.context.SessionContextUtil;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.data.community.User;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CreateUsernameRegistrationController extends RegistrationController implements ReadWriteController {
    @Override
    protected boolean hasChildRows() {
        return false;
    }

    @Override
    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response,
                                    BindException be)
            throws Exception {
        UserCommand command =  (UserCommand) be.getTarget();
        if(StringUtils.isBlank(command.getEmail())){
            return new ModelAndView("redirect:/community/loginOrRegister.page");
        }else{
            User user = getUserDao().findUserFromEmailIfExists(command.getEmail());
            if (user == null || user.getUserProfile() != null) {
                return new ModelAndView("redirect:/community/loginOrRegister.page");
            }
            return super.showForm(request, response, be);
        }
    }

    @Override
    public void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) throws Exception {
        UserCommand userCommand = (UserCommand) command;
        UserCommandValidator validator = new UserCommandValidator();
        validator.setUserDao(getUserDao());
        validator.validatePassword(userCommand, errors);
        validator.validateUsername(userCommand, null, errors);
    }

    @Override
    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        if (isIPBlocked(request)) return new ModelAndView(getErrorView());

        UserCommand userCommand = (UserCommand) command;

        if (!updateCommandUser(userCommand)) {
            // somehow a new user got to here
            return new ModelAndView("redirect:/community/registration.page?email=" +
                    userCommand.getEmail());
        }
        User user = userCommand.getUser();

        setUsersPassword(user, userCommand, true);

        if (isRequireEmailValidation()) {
            sendValidationEmail(user, userCommand, true, request);
        }

        OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
        updateUserProfile(user, userCommand, ot);

        if (user.isEmailProvisional()) {
            user.setEmailValidated();
        }

        // save
        getUserDao().updateUser(user);

        ModelAndView mAndV = new ModelAndView();
        if (!notifyCommunity(user, userCommand, mAndV, request)) {
            return mAndV; // early exit!
        }

        PageHelper.setMemberAuthorized(request, response, user); // auto-log in to community

        if (StringUtils.isEmpty(userCommand.getRedirectUrl())) {
            String redirectUrl = "http://" +
                    SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                    "/members/" + user.getUserProfile().getScreenName() + "/profile/interests?registration=1";
            userCommand.setRedirectUrl(redirectUrl);
        }
        mAndV.setViewName("redirect:" + userCommand.getRedirectUrl());

        return mAndV;
    }

    @Override
    protected boolean updateCommandUser(UserCommand userCommand) {
        User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());
        if (user == null) {
            return false;
        }

        // update the user's name if they specified a new one
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
        userCommand.setUser(user);

        if (StringUtils.isBlank(user.getGender())) {
            // Existing and new users that didn't get a gender set should get the default
            user.setGender("u");
        }

        return true;
    }
}
