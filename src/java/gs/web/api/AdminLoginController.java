package gs.web.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.apache.commons.lang.StringUtils;
import gs.web.community.registration.LoginCommand;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

/**
 * Created by chriskimm@greatschools.net
 */
@Controller
@RequestMapping("/api/admin_login.page")
public class AdminLoginController {

    public static final String API_ADMIN_COOKIE_NAME = "api_admin";
    public static final String MAIN_VIEW = "api/admin_login";

    @RequestMapping(method = RequestMethod.GET)
    public String getPage(@RequestParam(value="redirect", required=false) String redirect,
                          @RequestParam(value="action", required=false) String action,
                          HttpServletResponse response,
                          ModelMap model) {
        if (StringUtils.isNotBlank(action) && "logout".equals(action)) {
            logout(response);
        }
        LoginCommand command = new LoginCommand();
        command.setRedirect(redirect);
        model.addAttribute("command", command);
        return MAIN_VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String processSubmit(@ModelAttribute("command") LoginCommand command,
                                BindingResult result,
                                HttpServletResponse response) {
        System.out.println ("command: " + command);
        new AdminLoginValidator().validate(command, result);
        
        if (result.hasErrors()) {
            return MAIN_VIEW;
        } else {
            Cookie admin_auth_cookie = new Cookie(API_ADMIN_COOKIE_NAME, command.getEmail());
            admin_auth_cookie.setMaxAge(3600); // 1 hour
            response.addCookie(admin_auth_cookie);
            String redirect = "/api/admin/accounts.page";
            if (StringUtils.isNotBlank(command.getRedirect())) {
                redirect = command.getRedirect();
            }
            return "redirect:" + redirect;
        }
    }

    /**
     * Deletes the admin auth cookie.
     * @param response - HttpServletResponse
     */
    protected void logout(HttpServletResponse response) {
        Cookie admin_auth_cookie = new Cookie(API_ADMIN_COOKIE_NAME, "");
        admin_auth_cookie.setMaxAge(0);
        response.addCookie(admin_auth_cookie);
    }
}

class AdminLoginValidator implements Validator {
    public boolean supports(Class aClass) {
        return LoginCommand.class.isAssignableFrom(aClass);
    }

    public void validate(Object o, Errors errors) {
        LoginCommand command = (LoginCommand)o;

        String email = command.getEmail();
        if (StringUtils.isNotBlank(email)) {
            if (!"api-support@greatschools.net".equals(email)) {
                errors.rejectValue("email", "user.unknown", "unknown");
            }
        } else {
            errors.rejectValue("email", "field.required", "required");
        }

        String pass = command.getPassword();
        if (StringUtils.isNotBlank(pass)) {
            if (!"gsadmin".equals(pass)) {
                errors.rejectValue("password", "wrong.pass", "incorrect password");
            }
        } else {
            errors.rejectValue("password", "field.required", "required");
        }
    }
}