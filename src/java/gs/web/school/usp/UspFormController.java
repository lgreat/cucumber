package gs.web.school.usp;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.json.JSONObject;
import gs.data.school.*;
import gs.data.state.State;
import gs.data.util.email.EmailUtils;
import gs.web.community.registration.UserLoginCommand;
import gs.web.community.registration.UserRegistrationCommand;
import gs.web.school.EspUserStateStruct;
import gs.web.util.HttpCacheInterceptor;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 4/25/13
 * Time: 4:22 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/school/usp/")
public class UspFormController implements ReadWriteAnnotationController {
    public static final String FORM_VIEW = "/school/usp/uspForm";
    public static final String THANK_YOU_VIEW = "/school/usp/thankYou";

    HttpCacheInterceptor _cacheInterceptor = new HttpCacheInterceptor();

    private static Logger _logger = Logger.getLogger(UspFormController.class);

    @Autowired
    private UspFormHelper _uspFormHelper;
    @Autowired
    private IUserDao _userDao;

    @RequestMapping(value = "/form.page", method = RequestMethod.GET)
    public String showUspUserForm(ModelMap modelMap,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestParam(value = UspFormHelper.PARAM_SCHOOL_ID, required = false) Integer schoolId,
                                  @RequestParam(value = UspFormHelper.PARAM_STATE, required = false) State state) {
        School school = _uspFormHelper.getSchool(state, schoolId);
        if (school == null) {
            return "";
        }

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = null;
        if (sessionContext != null) {
            user = sessionContext.getUser();
        }

        _uspFormHelper.formFieldsBuilderHelper(modelMap, request, response, school, state, user, false);
        return FORM_VIEW;
    }

    @RequestMapping(value = "/form.page", method = RequestMethod.POST)
    public void onUspUserSubmitForm(HttpServletRequest request,
                                    HttpServletResponse response,
                                    UserRegistrationCommand userRegistrationCommand,
                                    UserLoginCommand userLoginCommand,
                                    BindingResult bindingResult,
                                    @RequestParam(value = UspFormHelper.PARAM_SCHOOL_ID, required = false) Integer schoolId,
                                    @RequestParam(value = UspFormHelper.PARAM_STATE, required = false) State state) {
        _uspFormHelper.formSubmitHelper(request, response, userRegistrationCommand, userLoginCommand, bindingResult,
                schoolId, state, false);
    }

    /**
     * Checks the various states a User can be in.
     * @param request
     * @param response
     * @param email
     * @param isLogin
     * @param password
     */

    @RequestMapping(value = "/checkUserState.page", method = RequestMethod.GET)
    protected void checkUserState(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestParam(value = "email", required = true) String email,
                                  @RequestParam(value = "isLogin", required = true) boolean isLogin,
                                  @RequestParam(value = "password", required = false) String password) {
        response.setContentType("application/json");

        EspUserStateStruct userState = new EspUserStateStruct();

        boolean isValid = EmailUtils.isValidEmail(email);
        if (isValid) {
            userState.setEmailValid(true);
            User user = _userDao.findUserFromEmailIfExists(email);
            if (user != null) {
                userState.setNewUser(false);
                userState.setUserEmailValidated(user.isEmailValidated());
                if (user.isEmailValidated() && isLogin) {
                    try {
                        boolean isValidLoginCredentials = user.matchesPassword(password);
                        userState.setCookieMatched(isValidLoginCredentials);
                    } catch (NoSuchAlgorithmException ex) {
                        userState.setCookieMatched(false);
                    }
                }
            } else {
                userState.setNewUser(true);
            }
        } else {
            userState.setEmailValid(false);
        }
        try {
            Map data = userState.getUserState();
            JSONObject responseObject = new JSONObject(data);
            _cacheInterceptor.setNoCacheHeaders(response);
            response.setContentType("application/json");
            response.getWriter().print(responseObject.toString());
            response.getWriter().flush();
        } catch (Exception exp) {
            _logger.error("Error " + exp, exp);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/thankYou.page", method = RequestMethod.GET)
    public String getThankYou(ModelMap modelMap, HttpServletRequest request,
                              HttpServletResponse response,
                              @RequestParam(value = UspFormHelper.PARAM_SCHOOL_ID, required = true) Integer schoolId,
                              @RequestParam(value = UspFormHelper.PARAM_STATE, required = true) State state) {
        School school = _uspFormHelper.getSchool(state, schoolId);
        if(school != null) {
            UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
            modelMap.put("schoolUrl", urlBuilder.asFullUrl(request));
        }
        return THANK_YOU_VIEW;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
