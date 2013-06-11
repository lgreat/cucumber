package gs.web.school.usp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gs.data.community.*;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.*;
import gs.data.state.State;
import gs.data.util.email.EmailUtils;
import gs.web.community.HoverHelper;
import gs.web.community.registration.*;
import gs.web.school.EspSaveBehaviour;
import gs.web.school.EspSaveHelper;
import gs.web.school.EspUserStateStruct;
import gs.web.util.HttpCacheInterceptor;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.SitePrefCookie;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
@RequestMapping("/school/QandA/")
public class UspFormController implements ReadWriteAnnotationController, BeanFactoryAware {
    public static final String FORM_VIEW = "/school/usp/uspForm";
    public static final String FORM_UNAVAILABLE_VIEW = "/school/usp/uspFormUnavailable";
    public static final String THANK_YOU_VIEW = "/school/usp/thankYou";

    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCHOOL_ID = "schoolId";
    public static final String PARAM_USP_SUBMISSION = "usp";

    HttpCacheInterceptor _cacheInterceptor = new HttpCacheInterceptor();

    private static Logger _logger = Logger.getLogger(UspFormController.class);

    private BeanFactory _beanFactory;

    @Autowired
    private UspFormHelper _uspFormHelper;
    @Autowired
    private IUserDao _userDao;
    @Autowired
    private EspSaveHelper _espSaveHelper;
    @Autowired
    private UserRegistrationOrLoginService _userRegistrationOrLoginService;
    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private IEspResponseDao _espResponseDao;
    @Autowired
    private ISubscriptionDao _subscriptionDao;
    @Autowired
    @Qualifier("exactTargetAPI")
    private ExactTargetAPI _exactTargetAPI;

    @RequestMapping(value = "/form.page", method = RequestMethod.GET)
    public String showUspUserForm(ModelMap modelMap,
                                  HttpServletRequest request,
                                  @RequestParam(value = UspFormHelper.PARAM_SCHOOL_ID, required = false) Integer schoolId,
                                  @RequestParam(value = UspFormHelper.PARAM_STATE, required = false) State state) {
        // First get the school
        School school = getSchool(state, schoolId);
        modelMap.put("school", school);
        if (school == null) {
            return "";
        }

        // Now, we need a user
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = null;
        if (sessionContext != null) {
            user = sessionContext.getUser();
        }

        modelMap.put("isSchoolAdmin", false);

        List<EspResponse> espResponses;

        // We need the List of EspResponses all for ourselves
        espResponses = _espResponseDao.getResponses(school);

        // Decorate the responses
        EspResponseData espResponseData = new EspResponseData(espResponses);

        // We could get EspStatus with static method on EspStatusManager, but that would make it a little harder to test
        EspStatusManager espStatusManager = (EspStatusManager) _beanFactory.getBean(
            "espStatusManager", school, espResponseData
        );

        // get the EspStatus for all EspResponses for this school
        EspStatus status = espStatusManager.getEspStatus();

        String view;
        switch (status) {
            case OSP_PREFERRED:
                view = FORM_UNAVAILABLE_VIEW;
                break;
            default:
                Multimap<String, String> responseKeyValues = ArrayListMultimap.create();

                // if a user is logged in, get key/val multimap for user USP responses
                if (user != null) {
                    espResponseData = (EspResponseData) espResponseData.getResponsesByUser(user.getId());
                    responseKeyValues = espResponseData.getUspResponses().getMultimap();
                }

                // Get a multimap from EspResponses, give it to form helper so it can generate form data
                List<UspFormResponseStruct> uspFormResponses = _uspFormHelper.formFieldsBuilderHelper(
                    responseKeyValues,
                    false
                );

                modelMap.put("uspFormResponses", uspFormResponses);

                view = FORM_VIEW;
                break;
        }

        return view;
    }

    @RequestMapping(value = "/form.page", method = RequestMethod.POST)
    public void onUspUserSubmitForm(HttpServletRequest request,
                                    HttpServletResponse response,
                                    UserRegistrationCommand userRegistrationCommand,
                                    UserLoginCommand userLoginCommand,
                                    BindingResult bindingResult,
                                    @RequestParam(value = UspFormHelper.PARAM_SCHOOL_ID, required = false) Integer schoolId,
                                    @RequestParam(value = UspFormHelper.PARAM_STATE, required = false) State state) {
        response.setContentType("application/json");
        JSONObject responseObject = new JSONObject();
        _cacheInterceptor.setNoCacheHeaders(response);

        School school = getSchool(state, schoolId);
        if (school == null) {
            writeIntoJsonObject(response, responseObject, "error", "noSchool");
            return; // early exit
        }

        UserStateStruct userStateStruct = getValidUser(request, response,
                userRegistrationCommand, userLoginCommand, bindingResult, school);

        if (userStateStruct == null || userStateStruct.getUser() == null) {
            writeIntoJsonObject(response, responseObject, "error", "noUser");
            return; // early exit
        }

        User user = userStateStruct.getUser();

        try {
            List<Subscription> userSubs = _subscriptionDao.getUserSubscriptions(user, SubscriptionProduct.USP);
            if(userSubs == null || userSubs.isEmpty()) {
                List subscriptions = new ArrayList();
                Subscription sub = new Subscription(user, SubscriptionProduct.USP, school);
                subscriptions.add(sub);
                getSubscriptionDao().saveSubscription(sub);
            }
            /**
             * MSS subscription will be set for only new members submitting response with the join hover and the checkbox
             * is checked, so not checking for any prev subscriptions to see if max limit has been reached.
             */
            if(userRegistrationCommand.isMss()) {
                List subscriptions = new ArrayList();
                Subscription sub = new Subscription(user, SubscriptionProduct.MYSTAT, school);
                subscriptions.add(sub);
                getSubscriptionDao().addNewsletterSubscriptions(user, subscriptions);
            }
        } catch (Exception e) {
            _logger.debug("Error while adding subscription: " +e);
        }

        //If the user is being logged in via the sign in hover and already has responses, then do not save the new responses.
        //Show the user his old responses.
        boolean doesUserAlreadyHaveResponses = checkIfUserHasExistingResponses(user, userStateStruct, school, false);

        if (doesUserAlreadyHaveResponses) {
            String redirectUrl = determineRedirects(user, userStateStruct, school, request, response, doesUserAlreadyHaveResponses);
            if (StringUtils.isNotBlank(redirectUrl)) {
                writeIntoJsonObject(response, responseObject, "redirect", redirectUrl);
            }
            return;
        }

        Map<String, Object[]> reqParamMap = request.getParameterMap();
        Set<String> formFieldNames = _uspFormHelper.FORM_FIELD_TITLES.keySet();

        _espSaveHelper.saveUspFormData(user, school, reqParamMap, formFieldNames, getSaveBehaviour(user));

        if(!user.isEmailProvisional()) {
            Map<String, String> emailAttributes = new HashMap<String, String>();
            emailAttributes.put("school_name", school.getName());
            UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
            emailAttributes.put("school_URL", urlBuilder.asFullUrl(request));
            getExactTargetAPI().sendTriggeredEmail("USP-thank-you", user, emailAttributes);
        }

        String redirectUrl = determineRedirects(user, userStateStruct, school, request, response, doesUserAlreadyHaveResponses);
        if (StringUtils.isNotBlank(redirectUrl)) {
            writeIntoJsonObject(response, responseObject, "redirect", redirectUrl);
        }

        return;
    }

    /**
     * Method to determine where the user should be redirected to after they have filled in the usp form.
     *
     * @param user
     * @param userStateStruct
     * @param school
     */

    public String determineRedirects(User user, UserStateStruct userStateStruct,
                                     School school, HttpServletRequest request, HttpServletResponse response,
                                     boolean doesUserAlreadyHaveResponses) {
        UrlBuilder urlBuilder = null;

        if(user == null || userStateStruct == null || request == null || school == null){
            return null;
        }

        if (user.isEmailValidated() && userStateStruct.isUserLoggedIn() && doesUserAlreadyHaveResponses) {
            //If the user is being logged in via the sign in hover and already has responses, then do not save the new responses.
            //Show the user his old responses.
            urlBuilder = new UrlBuilder(UrlBuilder.USP_FORM);
            urlBuilder.addParameter(PARAM_SCHOOL_ID, school.getId().toString());
            urlBuilder.addParameter(PARAM_STATE, school.getDatabaseState().toString());
            urlBuilder.addParameter("showExistingAnswersMsg", "true");
        } else if (user.isEmailValidated() && ((userStateStruct.isUserLoggedIn() && !doesUserAlreadyHaveResponses)
                || userStateStruct.isUserInSession())) {
            //If the user has been logged in but did not have any previous responses.
            //Or if the user is already in the session and filled in the usp form then show the thank you page.
            urlBuilder = new UrlBuilder(UrlBuilder.USP_FORM_THANKYOU);
            urlBuilder.addParameter(PARAM_SCHOOL_ID, school.getId().toString());
            urlBuilder.addParameter(PARAM_STATE, school.getDatabaseState().toString());
        } else if ((userStateStruct.isUserRegistered() || userStateStruct.isVerificationEmailSent())) {
            //If the user has registered via the register hover then show the profile page.
            //If the user was already existing but not email verified then sent an verification email and show the profile page.
            SitePrefCookie cookie = new SitePrefCookie(request, response);
            HoverHelper hoverHelper = new HoverHelper(cookie);
            hoverHelper.setHoverCookie(HoverHelper.Hover.USP_GO_VERIFY);

            urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        }
        if (urlBuilder != null) {
            return urlBuilder.asFullUrl(request);
        }
        return null;
    }


    /**
     * Method to check if the user is being logged in via the sign in hover and already has responses.
     *
     * @param user
     * @param userStateStruct
     * @param school
     * @return
     */
    public boolean checkIfUserHasExistingResponses(User user, UserStateStruct userStateStruct,
                                                   School school, boolean isOspUser) {
        if (user.isEmailValidated() && userStateStruct.isUserLoggedIn()) {
            Multimap<String, String> savedResponseKeyValues = _uspFormHelper.getSavedResponses(user, school, school.getDatabaseState(), isOspUser);

            return !savedResponseKeyValues.isEmpty();

        }
        return false;
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
    /**
     * Gets a user object from the session or by signing in the existing user or creating a new user.
     * The user object is and the state of the user object is encapsulated in the UserStateStruct.
     *
     * @param request
     * @param response
     * @param userRegistrationCommand
     * @param userLoginCommand
     * @param bindingResult
     * @return
     */

    public UserStateStruct getValidUser(HttpServletRequest request,
                                        HttpServletResponse response, UserRegistrationCommand userRegistrationCommand,
                                        UserLoginCommand userLoginCommand,
                                        BindingResult bindingResult,
                                        School school) {
        try {
            UspRegistrationBehavior registrationBehavior = new UspRegistrationBehavior();
            if (school != null) {
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.USP_FORM_THANKYOU);
                urlBuilder.addParameter(PARAM_SCHOOL_ID, school.getId().toString());
                urlBuilder.addParameter(PARAM_STATE, school.getDatabaseState().toString());
                urlBuilder.addParameter(PARAM_USP_SUBMISSION, "true");
                registrationBehavior.setRedirectUrl(urlBuilder.asFullUrl(request));
                registrationBehavior.setSchool(school);
            }
            //TODO set the below as a default in the  userRegistrationCommandand  registrationBehavior
            userRegistrationCommand.setHow("USP");
            //There is no additional confirm Password field. Hence set it to
            userRegistrationCommand.setConfirmPassword(userRegistrationCommand.getPassword());
            UserStateStruct userStateStruct =
                    _userRegistrationOrLoginService.getUserStateStruct(userRegistrationCommand, userLoginCommand, registrationBehavior, bindingResult, request, response);

            if (!bindingResult.hasErrors()) {
                return userStateStruct;
            }
        } catch (Exception ex) {
            //Do nothing. Ideally, this should not happen since we have command validations and client side validations.
        }
        return null;
    }


    @RequestMapping(value = "/thankYou.page", method = RequestMethod.GET)
    public String showThankYou(ModelMap modelMap, HttpServletRequest request,
                              HttpServletResponse response,
                              UserRegistrationCommand userRegistrationCommand,
                              UserLoginCommand userLoginCommand,
                              BindingResult bindingResult,
                              @RequestParam(value = UspFormHelper.PARAM_SCHOOL_ID, required = true) Integer schoolId,
                              @RequestParam(value = UspFormHelper.PARAM_STATE, required = true) State state) {
        School school = getSchool(state, schoolId);
        if(school != null) {
            UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
            modelMap.put("schoolUrl", urlBuilder.asFullUrl(request));
            modelMap.put("school", school);
        }

        UserStateStruct userStateStruct = getValidUser(request, response,
                userRegistrationCommand, userLoginCommand, bindingResult, school);

        return THANK_YOU_VIEW;
    }

    protected void writeIntoJsonObject(HttpServletResponse response,
                                       JSONObject responseObject, String key, String value) {
        try {
            responseObject.put(key, value);
            responseObject.write(response.getWriter());
            response.getWriter().flush();
        } catch (JSONException ex) {
            _logger.warn("UspFormHelper - exception while trying to write json object.", ex);
        } catch (IOException ex) {
            _logger.warn("UspFormHelper - exception while trying to get writer for response.", ex);
        }
    }

    /**
     * Parses the state and schoolId out of the request and fetches the school. Returns null if
     * it can't parse parameters, can't find school, or the school is inactive
     */
    protected School getSchool(State state, Integer schoolId) {
        if (state == null || schoolId == null) {
            return null;
        }
        School school = null;
        try {
            school = _schoolDao.getSchoolById(state, schoolId);
        } catch (Exception e) {
            // handled below
        }
        if (school == null || (!school.isActive() && !school.isDemoSchool())) {
            _logger.error("School is null or inactive: " + school);
            return null;
        }

        if (school.isPreschoolOnly()) {
            _logger.error("School is preschool only! " + school);
            return null;
        }

        return school;
    }

    public EspSaveBehaviour getSaveBehaviour(User user){
        boolean isUserEmailVerified = !(user.isEmailProvisional());
        return new EspSaveBehaviour(isUserEmailVerified, EspResponseSource.usp, false);
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        _beanFactory = beanFactory;
    }

    public EspStatusManager createEspStatusManager(School school) {
        return new EspStatusManager(school);
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public UspFormHelper getUspFormHelper() {
        return _uspFormHelper;
    }

    public void setUspFormHelper(UspFormHelper _uspFormHelper) {
        this._uspFormHelper = _uspFormHelper;
    }

    public void setUserRegistrationOrLoginService(UserRegistrationOrLoginService _userRegistrationOrLoginService) {
        this._userRegistrationOrLoginService = _userRegistrationOrLoginService;
    }

    public void setEspSaveHelper(EspSaveHelper _espSaveHelper) {
        this._espSaveHelper = _espSaveHelper;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI _exactTargetAPI) {
        this._exactTargetAPI = _exactTargetAPI;
    }
}
