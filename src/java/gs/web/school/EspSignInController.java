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
@RequestMapping("/official-school-profile/signin.page")
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
            EspUserStateStruct userState = new EspUserStateStruct();

            //Set the state of the user.
            User user = setUserState(command, userState);
            //validate the various states of the user.
            validateUserState(userState, result);

            //If there are no errors validate that the password entered is correct.
            //Else check if the user has ESP access and log them in.
            if (!result.hasErrors() && user != null && !user.matchesPassword(command.getPassword())) {
                result.rejectValue("password", null, "The password you entered is incorrect.");
            } else if (!result.hasErrors() && user != null && userState.isUserEmailValidated() && userState.isUserApprovedESPMember()) {
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

    //These conditions are complicated, refer to the flow charts attached to GS-12324 and  GS-12496.
    protected void validateUserState(EspUserStateStruct userState, BindingResult result) {
        if (userState.isNewUser() || (!userState.isUserRequestedESP())) {
            // new users or users who have never requested access
            result.rejectValue("email", null, "You do not have a School Official account. To request one, <a href='/official-school-profile/register.page'>register here.</a>");
        } else if (userState.isUserAwaitingESPMembership()) {
            // users who have requested access but are still being processed
            result.rejectValue("email", null, "You have already requested access to this school's Official School Profile. We are reviewing your request currently and will email you within a few days with a link to get started on the profile.");
        } else if (userState.isUserApprovedESPMember() && !userState.isUserEmailValidated()) {
            // users who have been approved but haven't followed through by clicking through the link in email
            result.rejectValue("email", null, "Please verify your email.<a href='#' class='js_espEmailNotVerifiedHover'>Verify email</a>");
        } else if (userState.isUserESPDisabled()) {
            result.rejectValue("email", null, "Our records indicate your school official's account is inactive. Please register again or contact us at gs_support@greatschools.org if you need further assistance.");
        }
    }

    protected User setUserState(EspRegistrationCommand command, EspUserStateStruct userState) {

        User user = getUserDao().findUserFromEmailIfExists(command.getEmail().trim());
        if (user != null) {
            userState.setNewUser(false);
            userState.setUserEmailValidated(user.isEmailValidated());

            //Check if the user has any esp memberships.
            List<EspMembership> espMemberships = getEspMembershipDao().findEspMembershipsByUserId(user.getId(), false);
            for (EspMembership membership : espMemberships) {
                userState.setUserRequestedESP(true);
                //User has at least one active membership.
                if (membership.getActive()) {
                    userState.setUserApprovedESPMember(true);
                    break;
                } else if (membership.getStatus().equals(EspMembershipStatus.PROCESSING)) {
                    //User is awaiting moderator decision.
                    userState.setUserAwaitingESPMembership(true);
                } else if (membership.getStatus().equals(EspMembershipStatus.DISABLED) && !membership.getActive()) {
                    userState.setUserESPDisabled(true);
                }
            }
            
            if (user.hasRole(Role.ESP_SUPERUSER)) {
                userState.setUserRequestedESP(true); // hack to bypass validation
                userState.setUserApprovedESPMember(true);
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
}
