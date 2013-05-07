package gs.web.school.usp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.EspResponseSource;
import gs.data.school.IEspResponseDao;
import gs.data.school.ISchoolDao;
import gs.data.school.*;
import gs.data.security.Role;
import gs.data.state.State;
import gs.data.util.email.EmailUtils;
import gs.web.community.registration.UserRegistrationCommand;
import gs.web.community.registration.UserRegistrationOrLoginService;
import gs.web.community.registration.UspRegistrationBehavior;
import gs.web.school.EspSaveHelper;
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
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 4/25/13
 * Time: 4:22 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/school/")
public class UspFormController implements ReadWriteAnnotationController {
    public static final String FORM_VIEW = "/school/usp/uspForm";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCHOOL_ID = "schoolId";
    public static final int MAX_RESPONSE_VALUE_LENGTH = 6000;

    HttpCacheInterceptor _cacheInterceptor = new HttpCacheInterceptor();

    @Autowired
    private UserRegistrationOrLoginService _userRegistrationOrLoginService;

    private static Logger _logger = Logger.getLogger(UspFormController.class);

    @Autowired
    private IEspResponseDao _espResponseDao;
    @Autowired
    private EspSaveHelper _espSaveHelper;
    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private IEspMembershipDao _espMembershipDao;
    @Autowired
    private IUserDao _userDao;

    @RequestMapping(value = "/usp/form.page", method = RequestMethod.GET)
    public String showUspUserForm (ModelMap modelMap,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   @RequestParam(value=PARAM_SCHOOL_ID, required=false) Integer schoolId,
                                   @RequestParam(value=PARAM_STATE, required=false) State state) {
        School school = getSchool(state, schoolId);
        if (school == null) {
            return "";
        }

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = null;
        if (sessionContext != null) {
            user = sessionContext.getUser();
        }

        formBuilderHelper(modelMap, request, response, school, state, user, false);
        return FORM_VIEW;
    }

    @RequestMapping(value = "/usp/form.page", method = RequestMethod.POST)
    public void onUspUserSubmitForm (HttpServletRequest request,
                                     HttpServletResponse response,
                                     UserRegistrationCommand userRegistrationCommand,
                                     BindingResult bindingResult,
                                     @RequestParam(value=PARAM_SCHOOL_ID, required=false) Integer schoolId,
                                     @RequestParam(value=PARAM_STATE, required=false) State state) {
        formSubmitHelper(request, response, userRegistrationCommand, bindingResult, schoolId, state);
    }

    public void formBuilderHelper (ModelMap modelMap,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   School school,
                                   State state,
                                   User user,
                                   boolean isOspUser) {

        modelMap.put("school", school);
        Multimap<String, String> responseKeyValues = ArrayListMultimap.create();

        if(user != null) {
            List<Object[]> keyValuePairs = _espResponseDao.getAllUniqueResponsesForSchoolBySourceAndByUser(school, state,
                    EspResponseSource.usp, user.getId());
            for(Object[] keyValue : keyValuePairs) {
                responseKeyValues.put((String)keyValue[0], (String) keyValue[1]);
            }
        }
        List<UspFormResponseStruct> uspFormResponses = new LinkedList<UspFormResponseStruct>();

        /**
         * For each enum value (form fields), construct the usp response object. Each section has one (no subsection) or
         * more section responses.
         * For each response key that the form field has, get all the response values from the multimap and construct
         * section response. Each section response has a list of response values objects.
         */
        for(UspHelper.SectionResponseKeys sectionResponseKeys : UspHelper.SectionResponseKeys.values()) {
            String fieldName = sectionResponseKeys.getSectionFieldName();
            String sectionTitle = UspHelper.FORM_FIELD_TITLES.get(fieldName);
            UspFormResponseStruct uspFormResponse = new UspFormResponseStruct(fieldName, sectionTitle);
            List<UspFormResponseStruct.SectionResponse> sectionResponses = uspFormResponse.getSectionResponses();

            String[] responseKeys = sectionResponseKeys.getResponseKeys();
            for(String responseKey : responseKeys) {
                Collection<String> responseValues = UspHelper.SECTION_RESPONSE_KEY_VALUE_MAP.get(responseKey);
                UspFormResponseStruct.SectionResponse sectionResponse = uspFormResponse.new SectionResponse(responseKey);
                sectionResponse.setTitle(UspHelper.RESPONSE_KEY_SUB_SECTION_LABEL.get(responseKey));

                Collection<String> savedResponses = responseKeyValues.get(responseKey);

                List<UspFormResponseStruct.SectionResponse.UspResponseValueStruct> uspResponseValues = sectionResponse.getResponses();

                Iterator<String> responseValueIter = responseValues.iterator();
                while(responseValueIter.hasNext()) {
                    String responseValue = responseValueIter.next();
                    UspFormResponseStruct.SectionResponse.UspResponseValueStruct uspResponseValue =
                            sectionResponse.new UspResponseValueStruct(responseValue);
                    uspResponseValue.setLabel(UspHelper.RESPONSE_VALUE_LABEL.get(responseValue));

                    if(savedResponses.contains(responseValue)) {
                        uspResponseValue.setIsSelected(true);
                    }

                    uspResponseValues.add(uspResponseValue);
                }

                sectionResponse.setResponses(uspResponseValues);
                sectionResponses.add(sectionResponse);
            }

            uspFormResponse.setSectionResponses(sectionResponses);
            uspFormResponses.add(uspFormResponse);
        }

        modelMap.put("uspFormResponses", uspFormResponses);
    }

    public void formSubmitHelper(HttpServletRequest request,
                                 HttpServletResponse response,
                                 UserRegistrationCommand userRegistrationCommand,
                                 BindingResult bindingResult,
                                 Integer schoolId,
                                 State state) {
        response.setContentType("application/json");
        JSONObject responseObject = new JSONObject();

        try {
            School school = getSchool(state, schoolId);
            if (school == null) {
                outputJsonError("noSchool", response);
                return; // early exit
            }

            User user = getValidUser(request,response,userRegistrationCommand,bindingResult);
            if (user == null) {
                outputJsonError("noUser", response);
                return; // early exit
            }

            Map<String, Object[]> reqParamMap = request.getParameterMap();

            Set<String> formFieldNames = UspHelper.FORM_FIELD_TITLES.keySet();

            _espSaveHelper.saveUspFormData(user, school, state, reqParamMap, formFieldNames);

            responseObject.put("success", true);
            responseObject.write(response.getWriter());
            response.getWriter().flush();
        }
        catch (JSONException ex) {
            _logger.warn("UspFormController - exception while trying to write json object.", ex);
        }
        catch (IOException ex) {
            _logger.warn("UspFormController - exception while trying to get writer for response.", ex);
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

    @RequestMapping(value = "/usp/checkUserState.page", method = RequestMethod.GET)
    protected void validateEmail(HttpServletRequest request,
                                 HttpServletResponse response,
                                 @RequestParam(value = "email", required = true) String email) {
        response.setContentType("application/json");

        EspUserStateStruct userState = new EspUserStateStruct();

        boolean isValid = EmailUtils.isValidEmail(email);
        if (isValid) {
            userState.setEmailValid(true);
            User user = _userDao.findUserFromEmailIfExists(email);
            if (user != null) {
                userState.setNewUser(false);
                userState.setUserEmailValidated(user.isEmailValidated());
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

    protected User getValidUser(HttpServletRequest request,
                                HttpServletResponse response, UserRegistrationCommand userRegistrationCommand,
                                BindingResult bindingResult) {
        try {
            UspRegistrationBehavior registrationBehavior = new UspRegistrationBehavior();
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ABOUT_US);
            registrationBehavior.setRedirectUrl(urlBuilder.asFullUrl(request));
            userRegistrationCommand.setHow("USP");
            userRegistrationCommand.setConfirmPassword(userRegistrationCommand.getPassword());
            User user = _userRegistrationOrLoginService.registerOrLoginUser(userRegistrationCommand, registrationBehavior, bindingResult, request, response);
            if (!bindingResult.hasErrors()) {
                return user;
            }
        } catch (Exception ex) {
            //Do nothing. Ideally, this should not happen since we have client side validations.
        }
        return null;
    }

    protected void outputJsonError(String msg, HttpServletResponse response) throws JSONException, IOException {
        JSONObject errorObj = new JSONObject();
        errorObj.put("error", msg);
        errorObj.write(response.getWriter());
        response.getWriter().flush();
    }
}
