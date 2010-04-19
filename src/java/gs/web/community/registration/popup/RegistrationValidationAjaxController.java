package gs.web.community.registration.popup;

import gs.data.community.*;
import gs.data.json.JSONObject;
import gs.web.community.registration.UserCommand;
import gs.web.util.validator.EmailValidator;
import gs.web.util.validator.UserCommandValidator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Controller that handles field-by-field (onblur) and well as full form validation for the megaJoin hover.
 * @author ssprouse@greatschools.org
 * @author aroy@greatschools.org
 */
public class RegistrationValidationAjaxController extends AbstractCommandController {

    public static final String BEAN_ID = "/community/registrationValidationAjax.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;

    private UserCommandValidator _userCommandValidator = new UserCommandValidator();

    private boolean _requireEmailValidation = true;

    public static final String FIELD_PARAMETER = "field";
    public static final String FIRST_NAME = "firstName";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String CONFIRM_PASSWORD = "confirmPassword";

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        _userCommandValidator.setUserDao(_userDao);

        // if a particular field is specified, validate only that field
        if (StringUtils.isNotBlank(request.getParameter(FIELD_PARAMETER))) {
            handleOnblur(request, (UserCommand) command, errors);
        } else {
            handleFullValidation(request, (UserCommand) command, errors);
        }

        Map<Object, Object> mapErrors = new HashMap<Object, Object>();

        response.setContentType("application/json");

        List<FieldError> a = (List<FieldError>) errors.getFieldErrors();

        for (FieldError error : a) {
            mapErrors.put(error.getField(), error.getDefaultMessage());
        }

        String jsonString = new JSONObject(mapErrors).toString();
        _log.info("Writing JSON response -" + jsonString);
        response.getWriter().write(jsonString);
        response.getWriter().flush();
        return null;
    }

    protected void handleFullValidation(HttpServletRequest request,UserCommand userCommand,
                                        BindException errors) throws IOException {
        String joinHoverType = request.getParameter("joinHoverType");

        //start validation
        // validate email format
        EmailValidator emailValidator = new EmailValidator();
        emailValidator.validate(userCommand, errors);
        // validate email address
        User user = _userCommandValidator.validateEmail(userCommand,request,errors);

        _userCommandValidator.validateFirstName(userCommand, errors);
        _userCommandValidator.validateUsername(userCommand, user, errors);
        if (_userCommandValidator.validatePasswordFormat(userCommand.getPassword(), "password", errors)) {
            _userCommandValidator.validatePasswordEquivalence
                    (userCommand.getPassword(), userCommand.getConfirmPassword(), "confirmPassword", errors);
        }
        _userCommandValidator.validateTerms(userCommand, errors);

        // do not require city/state
//        if ("ChooserTipSheet".equals(joinHoverType)) {
//            userCommand.setChooserRegistration(true);
//            _userCommandValidator.validateStateCity(userCommand, errors);
//        }
    }

    protected void handleOnblur(HttpServletRequest request, UserCommand userCommand, BindException errors) {
        if (StringUtils.equals(FIRST_NAME, request.getParameter(FIELD_PARAMETER))) {
            _userCommandValidator.validateFirstName(userCommand, errors);
        } else if (StringUtils.equals(EMAIL, request.getParameter(FIELD_PARAMETER))) {
            // validate email format
            EmailValidator emailValidator = new EmailValidator();
            emailValidator.validate(userCommand, errors);
            _userCommandValidator.validateEmail(userCommand, request, errors);
        } else if (StringUtils.equals(USERNAME, request.getParameter(FIELD_PARAMETER))) {
            User user = null;
            // to properly validate username we need to find if the user already exists
            if (StringUtils.isNotBlank(userCommand.getEmail())) {
                _log.info("Fetching User from " + userCommand.getEmail());
                user = _userDao.findUserFromEmailIfExists(userCommand.getEmail());
            }
            _userCommandValidator.validateUsername(userCommand, user, errors);
        } else if (StringUtils.equals(PASSWORD, request.getParameter(FIELD_PARAMETER))) {
            _userCommandValidator.validatePasswordFormat(userCommand.getPassword(), "password", errors);
        } else if (StringUtils.equals(CONFIRM_PASSWORD, request.getParameter(FIELD_PARAMETER))) {
            _userCommandValidator.validatePasswordEquivalence
                    (userCommand.getPassword(), userCommand.getConfirmPassword(), "confirmPassword", errors);        }
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

    public UserCommandValidator getUserCommandValidator() {
        return _userCommandValidator;
    }

    public void setUserCommandValidator(UserCommandValidator userCommandValidator) {
        _userCommandValidator = userCommandValidator;
    }
}