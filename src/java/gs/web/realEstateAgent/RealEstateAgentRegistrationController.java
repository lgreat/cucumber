package gs.web.realEstateAgent;

import gs.data.community.*;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.realEstateAgent.AgentAccount;
import gs.data.realEstateAgent.IAgentAccountDao;
import gs.data.state.State;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 2/6/13
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/real-estate/")
public class RealEstateAgentRegistrationController implements ReadWriteAnnotationController {
    private static Logger _logger = Logger.getLogger(RealEstateAgentRegistrationController.class);

    private static final String REGISTRATION_PAGE_VIEW = "/realEstateAgent/registrationHome";

    private static final String CREATE_REPORT_PAGE_VIEW = "/realEstateAgent/createReport";

    private final static String FIELD_NAME_REQ_PARAM_KEY = "fieldName";
    private final static String FIRST_NAME_REQ_PARAM_KEY = "firstName";
    private final static String LAST_NAME_REQ_PARAM_KEY = "lastName";
    private final static String EMAIL_REQ_PARAM_KEY = "email";
    private final static String PASSWORD_REQ_PARAM_KEY = "password";

    private final static String HAS_ERROR_VALIDATION_RESPONSE_KEY = "hasError";

    public static final String FIRST_NAME_ERROR_DETAIL_KEY = "firstNameErrorDetail";
    public static final int FIRST_NAME_MINIMUM_LENGTH = 2;
    public static final int FIRST_NAME_MAXIMUM_LENGTH = 24;
    public static final char[] FIRST_NAME_DISALLOWED_CHARACTERS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '<', '>', '&', '\\'
    };
    protected static final String ERROR_FIRST_NAME_LENGTH =
            "First name must be 2-24 characters long.";
    protected static final String ERROR_FIRST_NAME_BAD =
            "Please remove the numbers or symbols.";
    public static final String ERROR_INVALID_FIRST_NAME = "Please enter a valid first name.";

    public static final String LAST_NAME_ERROR_DETAIL_KEY = "lastNameErrorDetail";
    public static final int LAST_NAME_MINIMUM_LENGTH = 1;
    public static final int LAST_NAME_MAXIMUM_LENGTH = 24;
    protected static final String ERROR_LAST_NAME_LENGTH =
            "Last name must be 1-24 characters long.";
    protected static final String ERROR_LAST_NAME_INVALID_CHARACTERS =
            "Last name may contain only letters, numbers, spaces, and the following punctuation:, . - _ &";
    public static final String ERROR_INVALID_LAST_NAME = "Please enter a valid last name.";

    public static final String EMAIL_ERROR_DETAIL_KEY = "emailErrorDetail";
    public static final String ERROR_INVALID_EMAIL = "Please enter a valid email address.";
    protected static final int EMAIL_MAXIMUM_LENGTH = 127;
    protected static final String ERROR_EMAIL_MISSING =
            "Please enter your email address.";
    public static final String ERROR_EMAIL_LENGTH =
            "Your email must be less than 128 characters long.";
    protected static final String ERROR_EMAIL_TAKEN =
            "This email address is already registered.";
    protected static final String ERROR_EMAIL_HAS_AGENT_ACCOUNT =
            "This email address has registered real estate agent account.";

    public static final String PASSWORD_ERROR_DETAIL_KEY = "passwordErrorDetail";;
    protected static final int PASSWORD_MINIMUM_LENGTH = 6;
    protected static final int PASSWORD_MAXIMUM_LENGTH = 14;
    protected static final String ERROR_PASSWORD_LENGTH =
            "Password should be 6-14 characters.";
    protected static final String ERROR_INCORRECT_PASSWORD =
            "The password you entered is incorrect for the registered email.";

    @Autowired
    private IAgentAccountDao _agentAccountDao;

    @Autowired
    private IUserDao _userDao;

    @Autowired
    private ISubscriptionDao _subscriptionDao;

    @Autowired
    private RealEstateAgentHelper _realEstateAgentHelper;

    @RequestMapping(value = "school-guides.page", method = RequestMethod.GET)
    public String showRegistrationForm (HttpServletRequest request,
                            HttpServletResponse response) {
        //TODO: comment skip user validation
        if (!_realEstateAgentHelper.skipUserValidation(request) && _realEstateAgentHelper.hasAgentAccount(request)) {
            return "redirect:" + _realEstateAgentHelper.getRealEstateCreateGuideUrl(request);
        }
        return REGISTRATION_PAGE_VIEW;
    }

    @RequestMapping(value = "create-guide.page", method = RequestMethod.GET)
    public String showCreateReportForm (HttpServletRequest request,
                            HttpServletResponse response) {
        //TODO: comment skip user validation
        if(_realEstateAgentHelper.skipUserValidation(request)) {
            return CREATE_REPORT_PAGE_VIEW;
        }

        Integer userId = _realEstateAgentHelper.getUserId(request);

        if(userId != null) {
            return _realEstateAgentHelper.getViewForUser(request, userId, CREATE_REPORT_PAGE_VIEW);
        }

        return "redirect:" + _realEstateAgentHelper.getRealEstateSchoolGuidesUrl(request);
    }

    @RequestMapping(value = "savePersonalInfo.page", method = RequestMethod.POST)
    public void onPersonalInfoSubmit (HttpServletRequest request,
                                      HttpServletResponse response,
                                      @RequestParam(value = "firstName", required = true) String fName,
                                      @RequestParam(value = "lastName", required = true) String lName,
                                      @RequestParam(value = "email", required = true) String email,
                                      @RequestParam(value = "password", required = true) String password) {

        response.setContentType("application/json");
        JSONObject responseJson = new JSONObject();

        //TODO: comment skip user validation
        if(_realEstateAgentHelper.skipUserValidation(request)) {
            outputJson(response, responseJson, false);
            return;
        }

        try {
            doFullValidations(fName, lName, email, password, responseJson);

            if(responseJson.has(HAS_ERROR_VALIDATION_RESPONSE_KEY) && responseJson.getBoolean(HAS_ERROR_VALIDATION_RESPONSE_KEY)) {
                outputJson(response, responseJson, false);
                return;
            }
        }
        catch (JSONException ex) {
            _logger.warn("RealEstateAgentRegistrationController: Invalid value for response JSON object.");
        }
        catch (NoSuchAlgorithmException ex) {
            _logger.warn("RealEstateAgentRegistrationController: Error while trying to encode password.");
        }

        User user = _userDao.findUserFromEmailIfExists(email);

        if(user != null && user.getId() != null) {
            setUserFields(fName, lName, user);
        }
        else {
            user = new User();
            user.setEmail(email);
            user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
            setUserFields(fName, lName, user);
            _userDao.saveUser(user);
        }

        setUserPassword(password, user);
        updateUserProfile(user);
        _userDao.updateUser(user);

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setProduct(SubscriptionProduct.SCHOOL_GUIDE_RADAR);
        State state = SessionContextUtil.getSessionContext(request).getState();
        if(state != null) {
            subscription.setState(state);
        }
        List<Subscription> subscriptions = _subscriptionDao.getUserSubscriptions(user, SubscriptionProduct.SCHOOL_GUIDE_RADAR);
        if(subscriptions == null || subscriptions.isEmpty()) {
            _subscriptionDao.saveSubscription(subscription);
        }

        _realEstateAgentHelper.setUserCookie(user, request, response);

        outputJson(response, responseJson, true);
    }

    @RequestMapping(value = "saveBusinessInfo.page", method = RequestMethod.POST)
    public void onBusinessInfoSubmit (HttpServletRequest request,
                                      HttpServletResponse response,
                                      @RequestParam(value = "companyName", required = false) String companyName,
                                      @RequestParam(value = "workNumber", required = false) String workNumber,
                                      @RequestParam(value = "cellNumber", required = false) String cellNumber,
                                      @RequestParam(value = "address", required = false) String address,
                                      @RequestParam(value = "city", required = false) String city,
                                      @RequestParam(value = "state", required = false) String state,
                                      @RequestParam(value = "zip", required = false) String zip) {

        response.setContentType("application/json");
        JSONObject responseJson = new JSONObject();

        Integer userId = _realEstateAgentHelper.getUserId(request);

        if(userId == null) {
            outputJson(response, responseJson, false);
            return;
        }

        User user = _userDao.findUserFromId(userId);

        if(user == null || user.getId() == null) {
            outputJson(response, responseJson, false);
            return;
        }

        AgentAccount agentAccount = getAgentAccountDao().findAgentAccountByUserId(user.getId());
        if (agentAccount == null) {
            agentAccount = new AgentAccount(user);
            setAgentAccountFields(agentAccount, companyName, workNumber, cellNumber, address, city, state, zip);
            getAgentAccountDao().save(agentAccount);
        }
        else {
            setAgentAccountFields(agentAccount, companyName, workNumber, cellNumber, address, city, state, zip);
            getAgentAccountDao().updateAgentAccount(agentAccount);
        }

        outputJson(response, responseJson, true);
    }

    @RequestMapping(value = "personalInfoValidationAjax.page", method = RequestMethod.GET)
    public void handlePersonalInfoValidation(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> paramMap = request.getParameterMap();

        JSONObject responseJson = new JSONObject();

        try {
            if(paramMap.containsKey(FIELD_NAME_REQ_PARAM_KEY)) {
                if(paramMap.containsKey(FIRST_NAME_REQ_PARAM_KEY)) {
                    String firstName = request.getParameter(FIRST_NAME_REQ_PARAM_KEY);
                    validateFirstName(firstName, responseJson);
                }
                if(paramMap.containsKey(LAST_NAME_REQ_PARAM_KEY)) {
                    String lastName = request.getParameter(LAST_NAME_REQ_PARAM_KEY);
                    validateLastName(lastName, responseJson);
                }
                if(paramMap.containsKey(EMAIL_REQ_PARAM_KEY)) {
                    String email = request.getParameter(EMAIL_REQ_PARAM_KEY);
                    validateEmail(email, responseJson);
                }
                if(paramMap.containsKey(PASSWORD_REQ_PARAM_KEY)) {
                    String password = request.getParameter(PASSWORD_REQ_PARAM_KEY);
                    validatePassword(password, responseJson);
                }
            }
            responseJson.write(response.getWriter());
            response.getWriter().flush();
        }
        catch (JSONException ex) {}
        catch (IOException ex) {}
    }

    private void doFullValidations(String firstName, String lastName, String email, String password, JSONObject responseJson) throws JSONException, NoSuchAlgorithmException {
        validateFirstName(firstName, responseJson);
        validateLastName(lastName, responseJson);
        validateEmail(email, responseJson);
        validatePassword(password, responseJson);
        validateRegisteredUser(email, password, responseJson);
    }

    private void validateFirstName (String name, JSONObject responseJson) throws JSONException {
        if(StringUtils.isEmpty(name) || name.length() < FIRST_NAME_MINIMUM_LENGTH || name.length() > FIRST_NAME_MAXIMUM_LENGTH) {
            responseJson.put(FIRST_NAME_ERROR_DETAIL_KEY, ERROR_FIRST_NAME_LENGTH);
            responseJson.put(HAS_ERROR_VALIDATION_RESPONSE_KEY, true);
        }
        else if(!StringUtils.containsNone(name, FIRST_NAME_DISALLOWED_CHARACTERS)) {
            responseJson.put(FIRST_NAME_ERROR_DETAIL_KEY, ERROR_FIRST_NAME_BAD);
            responseJson.put(HAS_ERROR_VALIDATION_RESPONSE_KEY, true);
        }
        else if("First Name".equals(name)) {
            responseJson.put(FIRST_NAME_ERROR_DETAIL_KEY, ERROR_INVALID_FIRST_NAME);
            responseJson.put(HAS_ERROR_VALIDATION_RESPONSE_KEY, true);
        }
    }

    private void validateLastName (String name, JSONObject responseJson) throws JSONException {
        if(StringUtils.isEmpty(name) || name.length() < LAST_NAME_MINIMUM_LENGTH || name.length() > LAST_NAME_MAXIMUM_LENGTH) {
            responseJson.put(LAST_NAME_ERROR_DETAIL_KEY, ERROR_LAST_NAME_LENGTH);
            responseJson.put(HAS_ERROR_VALIDATION_RESPONSE_KEY, true);
        }
        else if(!name.matches("[0-9a-zA-Z\\-\\_\\.\\,\\&\\s]*")) {
            responseJson.put(LAST_NAME_ERROR_DETAIL_KEY, ERROR_LAST_NAME_INVALID_CHARACTERS);
            responseJson.put(HAS_ERROR_VALIDATION_RESPONSE_KEY, true);
        }
        else if("Last Name".equals(name)) {
            responseJson.put(LAST_NAME_ERROR_DETAIL_KEY, ERROR_INVALID_LAST_NAME);
            responseJson.put(HAS_ERROR_VALIDATION_RESPONSE_KEY, true);
        }
    }

    private void validateEmail(String email, JSONObject responseJson) throws JSONException{
        if (StringUtils.isEmpty(email) || "Email".equals(email)) {
            responseJson.put(EMAIL_ERROR_DETAIL_KEY, ERROR_EMAIL_MISSING);
            responseJson.put(HAS_ERROR_VALIDATION_RESPONSE_KEY, true);
            return;
        } else if (email.length() > EMAIL_MAXIMUM_LENGTH) {
            responseJson.put(EMAIL_ERROR_DETAIL_KEY, ERROR_EMAIL_LENGTH);
            responseJson.put(HAS_ERROR_VALIDATION_RESPONSE_KEY, true);
            return;
        }

        org.apache.commons.validator.routines.EmailValidator emailValidator = org.apache.commons.validator.routines.EmailValidator.getInstance();

        if(emailValidator.isValid(email)) {
            User user = _userDao.findUserFromEmailIfExists(email);
            if (user != null && user.getId() != null) {
                /*if (user.getUserProfile() != null && !user.getUserProfile().isActive()) {
                    String errmsg = "The account associated with that email address has been disabled. " +
                            "Please <a href=\"http://" +
                            SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                            "/report/email-moderator\">contact us</a> for more information.";
                    responseJson.accumulate(ERROR_DETAIL_VALIDATION_RESPONSE_KEY, errmsg);
                    responseJson.accumulate(HAS_ERROR_VALIDATION_RESPONSE_KEY, true);
                } else if (user.isEmailValidated()) {
                    UrlBuilder builder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null);
                    builder.addParameter("email",email);
                    String loginUrl = builder.asFullUrl(request);
                    String errmsg = ERROR_EMAIL_TAKEN + " <a class=\"launchSignInHover\" href=\"" + loginUrl + "\">&nbsp;Log in&nbsp;&gt;</a>";
                    responseJson.accumulate(ERROR_DETAIL_VALIDATION_RESPONSE_KEY, errmsg);
                    responseJson.accumulate(HAS_ERROR_VALIDATION_RESPONSE_KEY, true);
                } else if (user.isEmailProvisional()) {
                    // let them register, just overwrite previous values
                }*/

                AgentAccount agentAccount = getAgentAccountDao().findAgentAccountByUserId(user.getId());
                if(agentAccount != null) {
                    responseJson.put(EMAIL_ERROR_DETAIL_KEY, ERROR_EMAIL_HAS_AGENT_ACCOUNT);
                    responseJson.put(HAS_ERROR_VALIDATION_RESPONSE_KEY, true);
                }
            }
        }
        else {
            responseJson.put(EMAIL_ERROR_DETAIL_KEY, ERROR_INVALID_EMAIL);
            responseJson.put(HAS_ERROR_VALIDATION_RESPONSE_KEY, true);
        }
    }

    private void validatePassword(String password, JSONObject responseJson) throws JSONException{
        if (StringUtils.isEmpty(password) ||
                password.length() < PASSWORD_MINIMUM_LENGTH ||
                password.length() > PASSWORD_MAXIMUM_LENGTH) {
            responseJson.put(PASSWORD_ERROR_DETAIL_KEY, ERROR_PASSWORD_LENGTH);
            responseJson.put(HAS_ERROR_VALIDATION_RESPONSE_KEY, true);
        }
    }

    private void validateRegisteredUser(String email, String password, JSONObject responseJson) throws JSONException, NoSuchAlgorithmException {
        User user = getUserDao().findUserFromEmailIfExists(email);
        if(user != null && !user.matchesPassword(password)) {
            responseJson.put(PASSWORD_ERROR_DETAIL_KEY, ERROR_INCORRECT_PASSWORD);
            responseJson.put(HAS_ERROR_VALIDATION_RESPONSE_KEY, true);
        }
    }

    private void setUserFields(String fName, String lName, User user) {
        if(user != null) {
            if(StringUtils.isNotBlank(fName)) {
                user.setFirstName(fName.trim());
            }
            if(StringUtils.isNotBlank(lName)) {
                user.setLastName(lName.trim());
            }
        }
    }

    private void setUserPassword(String password, User user) {
        try {
            if (StringUtils.isNotEmpty(password) && !user.isEmailValidated()) {
                user.setPlaintextPassword(password);
            }
        }
        catch (Exception ex) {
            _logger.debug("RealEstateAgentSignUpController: Unable to set user password");
        }
    }

    private void updateUserProfile(User user) {
        UserProfile userProfile;

        if (user.getUserProfile() == null) {
            userProfile = new UserProfile();
            generateScreenName(userProfile, user.getId());
            userProfile.setUser(user);
            user.setUserProfile(userProfile);
        }
    }

    public void setAgentAccountFields(AgentAccount agentAccount, String companyName, String workNumber, String cellNumber,
                                      String address, String city, String state, String zip) {
        if(StringUtils.isNotBlank(workNumber)) {
            agentAccount.setWorkNumber(workNumber.trim());
        }
        if(StringUtils.isNotBlank(cellNumber)) {
            agentAccount.setCellNumber(cellNumber.trim());
        }
        if(StringUtils.isNotBlank(companyName)) {
            agentAccount.setCompanyName(companyName.trim());
        }
        if(StringUtils.isNotBlank(address)) {
            agentAccount.setAddress(address.trim());
        }
        if(StringUtils.isNotBlank(city)) {
            agentAccount.setCity(city.trim());
        }
        if(StringUtils.isNotBlank(state)) {
            agentAccount.setState(state.trim());
        }
        if(StringUtils.isNotBlank(zip)) {
            agentAccount.setZip(zip.trim());
        }
    }

    private void outputJson(HttpServletResponse response, JSONObject responseJson, boolean isSuccess) {
        try {
            responseJson.accumulate("success", isSuccess);
            responseJson.write(response.getWriter());
            response.getWriter().flush();
        }
        catch (JSONException ex) {}
        catch (IOException ex) {}
    }


    private void generateScreenName(UserProfile userProfile, Integer userId) {
        if (userProfile != null) {
            userProfile.setScreenName("user" + userId);
            userProfile.setUpdated(new Date());
        }
    }

    public IAgentAccountDao getAgentAccountDao() {
        return _agentAccountDao;
    }

    public void setAgentAccountDao(IAgentAccountDao _accountDao) {
        this._agentAccountDao = _accountDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao _userDao) {
        this._userDao = _userDao;
    }
}
