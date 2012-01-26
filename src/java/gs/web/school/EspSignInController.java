package gs.web.school;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.IEspMembershipDao;
import gs.data.school.EspMembership;
import gs.data.security.Role;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private static final Log _log = LogFactory.getLog(EspRegistrationController.class);

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

        //member cookie is set and user has ESP role.
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


        String email = command.getEmail();
        String password = command.getPassword();
        User user = null;
        List<EspMembership> espMemberships = null;

        if (StringUtils.isNotBlank(email) && StringUtils.isNotEmpty(password)) {
            email = email.trim();
            boolean isEmailValid = validateEmail(email);
            if (isEmailValid) {
                user = getUserDao().findUserFromEmailIfExists(email);
                boolean foundActiveEspMembership = false;
                boolean isUserEmailValidated = false;

                if (user != null) {

                    isUserEmailValidated = user.isEmailValidated();

                    //Check if the user has any esp memberships.Active or Inactive.
                    espMemberships = getEspMembershipDao().findEspMembershipsByUserId(user.getId(), false);
                    for (EspMembership membership : espMemberships) {

                        if (membership.getActive() && isUserEmailValidated) {
                            foundActiveEspMembership = true;
                            boolean matchesPassword = user.matchesPassword(password);

                            if (matchesPassword) {
                                PageHelper.setMemberAuthorized(request, response, user, true);
                                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_DASHBOARD);
                                return "redirect:" + urlBuilder.asFullUrl(request);
                            } else {
                                result.rejectValue("password", null, "Incorrect password.");
                            }
                        }
                    }
                }

                if (user == null || espMemberships == null || espMemberships.isEmpty()) {
                    result.rejectValue("email", null, "There is no account associated with that email address.");
                } else if (!isUserEmailValidated) {
                    result.rejectValue("email", null, "Please verify ur email.");
                } else if (!foundActiveEspMembership) {
                    //TODO should there be a different message for rejected users?
                    result.rejectValue("email", null, "Your ESP request is still under consideration. Please be patient.");
                }

            } else {
                result.rejectValue("email", null, "Invalid email address.");
            }
        } else {
            if (StringUtils.isBlank(email)) {
                result.rejectValue("email", null, "Please enter an email address.");
            }
            if (StringUtils.isEmpty(password)) {
                result.rejectValue("password", null, "Please enter a password.");
            }
        }
        return VIEW;
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