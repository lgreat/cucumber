package gs.web.community.registration.popup;


import gs.data.community.*;
import gs.data.json.JSONObject;
import gs.data.util.table.ITableDao;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.school.Grade;
import gs.data.school.School;
import gs.data.school.ISchoolDao;
import gs.web.community.registration.EmailVerificationEmail;
import gs.web.community.registration.UserCommand;
import gs.web.util.*;
import gs.web.util.validator.UserCommandValidator;
import gs.web.util.context.SessionContextUtil;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.mvc.BaseCommandController;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: samson
 * Date: Mar 26, 2010
 * Time: 2:45:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegistrationValidationAjaxController extends AbstractCommandController {

    public static final String BEAN_ID = "/community/registrationValidationAjax.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private IGeoDao _geoDao;

    private StateManager _stateManager;

    private UserCommandValidator _userCommandValidator = new UserCommandValidator();

    private boolean _requireEmailValidation = true;

    public static final String CITY_PARAMETER = "city";

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {

        _userCommandValidator.setUserDao(_userDao);
        _userCommandValidator.validate(request, command, errors);

        UserCommand userCommand = (UserCommand) command;

        String joinHoverType = (String) request.getAttribute("joinHoverType");

        /*
        String gradeNewsletters = (String) request.getAttribute("gradeNewsletters");

        List<UserCommand.NthGraderSubscription> nthGraderSubscriptions = new ArrayList<UserCommand.NthGraderSubscription>();
        for (String grade : StringUtils.split(gradeNewsletters)) {
            nthGraderSubscriptions.add(new UserCommand.NthGraderSubscription(true,grade));
        }
        */

        //start validation
        User user = _userCommandValidator.validateEmail(userCommand,request,errors);
        if (user != null && errors.hasFieldErrors("email")) {
            return null; // other errors are irrelevant
        }

        _userCommandValidator.validateFirstName(userCommand, errors);
        _userCommandValidator.validateUsername(userCommand, user, errors);
        _userCommandValidator.validateTerms(userCommand, errors);
        _userCommandValidator.validatePassword(userCommand, errors);

        if ("ChooserTipSheet".equals(joinHoverType)) {
            _userCommandValidator.validateStateCity(userCommand, errors);
        }

        /*
        List<UserCommand.NthGraderSubscription> list = userCommand.getGradeNewsletters();

        for (UserCommand.NthGraderSubscription s : list) {
            System.out.println("subscription:" + s);
        }
        */

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

    protected boolean hasChildRows() {
        return true;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
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