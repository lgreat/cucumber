package gs.web.school;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.EspMembershipStatus;
import gs.data.school.IEspMembershipDao;
import gs.data.school.EspMembership;
import gs.data.security.Role;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/school/esp/signIn.page")
public class EspSignInController implements ReadWriteAnnotationController {
    public static final String VIEW = "school/espSignIn";

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @Autowired
    private IUserDao _userDao;

    @RequestMapping(method = RequestMethod.GET)
    public String showForm(ModelMap modelMap, HttpServletRequest request) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();
        EspRegistrationCommand command = new EspRegistrationCommand();

        //member cookie is set and user has ESP role.Therefore take the user to the dashboard.
        if (user != null && user.getId() != null && (user.hasRole(Role.ESP_MEMBER) || user.hasRole(Role.ESP_SUPERUSER))) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
            return "redirect:" + urlBuilder.asFullUrl(request);
        }

        modelMap.addAttribute("espRegistrationCommand", command);
        return VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String signIn(@ModelAttribute("espRegistrationCommand") EspRegistrationCommand command,
                         BindingResult result,
                         HttpServletRequest request,
                         HttpServletResponse response) throws Exception {

        validateFormFields(command, result);
        if (!result.hasErrors()) {
            UserStateStruct userStateStruct = new UserStateStruct();

            //Set the state of the user.
            User user = setUserState(command, userStateStruct);
            //validate the various states of the user.
            validateUserState(userStateStruct, result);

            //If there are no errors validate that the password entered is correct.
            //Else check if the user has ESP access and log them in.
            if (!result.hasErrors() && user != null && !user.matchesPassword(command.getPassword())) {
                result.rejectValue("password", null, "The password you entered is incorrect.");
            } else if (!result.hasErrors() && user != null && userStateStruct.isUserEmailValidated() && userStateStruct.isUserApprovedESPMember()) {
                PageHelper.setMemberAuthorized(request, response, user, true);
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
                return "redirect:" + urlBuilder.asFullUrl(request);
            }
        }
        return VIEW;
    }

    protected void validateFormFields(EspRegistrationCommand command, BindingResult result) {
        String email = command.getEmail();
        String password = command.getPassword();

        if (StringUtils.isBlank(email)) {
            result.rejectValue("email", null, "Please enter a valid email address.");
        } else if (!validateEmail(email.trim())) {
            result.rejectValue("email", null, "Please enter a valid email address.");
        }

        if (StringUtils.isEmpty(password)) {
            result.rejectValue("password", null, "Please enter a password.");
        }

    }

    protected void validateUserState(UserStateStruct userStateStruct, BindingResult result) {
        if (userStateStruct.isNewUser() || (!userStateStruct.isUserRequestedESP())) {
            result.rejectValue("email", null, "You do not have a School Official account. To request one, <a href='/school/esp/register.page'>register here.</a>");
        } else if (userStateStruct.isUserAwaitingESPMembership()) {
            result.rejectValue("email", null, "You have already requested access to this school’s Official School Profile. We are reviewing your request currently and will email you within a few days with a link to get started on the profile.");
        } else if (userStateStruct.isUserApprovedESPMember() && !userStateStruct.isUserEmailValidated()) {
            result.rejectValue("email", null, "Please verify your email.<a href='#' class='js_espEmailNotVerifiedHover'>Verify email</a>");
        }
    }

    protected User setUserState(EspRegistrationCommand command, UserStateStruct userStateStruct) {

        User user = getUserDao().findUserFromEmailIfExists(command.getEmail().trim());
        if (user != null) {
            userStateStruct.setNewUser(false);
            userStateStruct.setUserEmailValidated(user.isEmailValidated());

            //Check if the user has any esp memberships.
            List<EspMembership> espMemberships = getEspMembershipDao().findEspMembershipsByUserId(user.getId(), false);
            for (EspMembership membership : espMemberships) {
                userStateStruct.setUserRequestedESP(true);
                //User has at least one active membership.
                if (membership.getActive()) {
                    userStateStruct.setUserApprovedESPMember(true);
                    break;
                } else if (membership.getStatus().equals(EspMembershipStatus.PROCESSING)) {
                    //User is awaiting moderator decision.
                    userStateStruct.setUserAwaitingESPMembership(true);
                }
            }
        }
        return user;
    }

    protected boolean validateEmail(String email) {
        org.apache.commons.validator.EmailValidator emv = org.apache.commons.validator.EmailValidator.getInstance();
        return emv.isValid(email);
    }

    public IEspMembershipDao getEspMembershipDao() {
        return _espMembershipDao;
    }

    public void setEspMembershipDao(IEspMembershipDao espMembershipDao) {
        _espMembershipDao = espMembershipDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    protected static class UserStateStruct {
        private boolean isNewUser = true;
        private boolean isUserEmailValidated = false;
        private boolean isUserRequestedESP = false;
        private boolean isUserAwaitingESPMembership = false;
        private boolean isUserApprovedESPMember = false;

        public boolean isNewUser() {
            return isNewUser;
        }

        public void setNewUser(boolean newUser) {
            isNewUser = newUser;
        }

        public boolean isUserEmailValidated() {
            return isUserEmailValidated;
        }

        public void setUserEmailValidated(boolean userEmailValidated) {
            isUserEmailValidated = userEmailValidated;
        }

        public boolean isUserRequestedESP() {
            return isUserRequestedESP;
        }

        public void setUserRequestedESP(boolean userRequestedESP) {
            isUserRequestedESP = userRequestedESP;
        }

        public boolean isUserAwaitingESPMembership() {
            return isUserAwaitingESPMembership;
        }

        public void setUserAwaitingESPMembership(boolean userAwaitingESPMembership) {
            isUserAwaitingESPMembership = userAwaitingESPMembership;
        }

        public boolean isUserApprovedESPMember() {
            return isUserApprovedESPMember;
        }

        public void setUserApprovedESPMember(boolean userApprovedESPMember) {
            isUserApprovedESPMember = userApprovedESPMember;
        }

    }
}