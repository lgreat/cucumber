package gs.web.school.usp;

import gs.data.community.User;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.EspResponse;
import gs.data.school.IEspResponseDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.community.registration.UserRegistrationCommand;
import gs.web.community.registration.UserRegistrationOrLoginService;
import gs.web.community.registration.UspRegistrationBehavior;
import gs.web.school.EspSaveHelper;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
@RequestMapping("/school/usp/")
public class UspFormController implements ReadWriteAnnotationController {
    public static final String FORM_VIEW = "/school/usp/uspForm";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCHOOL_ID = "schoolId";
    public static final int MAX_RESPONSE_VALUE_LENGTH = 6000;

    @Autowired
    private UserRegistrationOrLoginService _userRegistrationOrLoginService;

    private static final Set<String> responseKeys = new HashSet<String>() {{
        add("arts_media");
        add("arts_music");
        add("arts_performing_written");
        add("arts_visual");
    }};

    private static Logger _logger = Logger.getLogger(UspFormController.class);

    @Autowired
    private IEspResponseDao _espResponseDao;
    @Autowired
    private EspSaveHelper _espSaveHelper;
    @Autowired
    private ISchoolDao _schoolDao;

    @RequestMapping(value = "form.page", method = RequestMethod.GET)
    public String showForm (HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestParam(value=PARAM_SCHOOL_ID, required=false) Integer schoolId,
                            @RequestParam(value=PARAM_STATE, required=false) State state) {
        School school = getSchool(state, schoolId);
        if (school == null) {
            return "";
        }

//        List<UspFormResponseStruct> uspFormResponses;
        List<EspResponse> responses = _espResponseDao.getResponses(school);
        final boolean isSchoolAdmin = false;

        for(EspResponse espResponse : responses) {


//            uspFormResponses = new LinkedList<UspFormResponseStruct>(){{
//                add(new UspFormResponseStruct("Arts & music"), isSchoolAdmin)
//            }};
        }

        return FORM_VIEW;
    }

    @RequestMapping(value = "form.page", method = RequestMethod.POST)
    public void onSubmitForm (HttpServletRequest request,
                              HttpServletResponse response,
                              UserRegistrationCommand userRegistrationCommand,
                              BindingResult bindingResult,
                              @RequestParam(value=PARAM_SCHOOL_ID, required=false) Integer schoolId,
                              @RequestParam(value=PARAM_STATE, required=false) State state) {
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

            _espSaveHelper.saveUspFormData(user, school, state, reqParamMap, responseKeys);

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

    /*protected void putInResponseMap(Map<String, EspFormResponseStruct> responseMap,EspResponse response){
        EspFormResponseStruct responseStruct = responseMap.get(response.getKey());
        if (responseStruct == null) {
            responseStruct = new EspFormResponseStruct();
            responseMap.put(response.getKey(), responseStruct);
        }
        responseStruct.addValue(response.getSafeValue());
    }*/

    protected User getValidUser(HttpServletRequest request,
                                HttpServletResponse response, UserRegistrationCommand userRegistrationCommand,
                                BindingResult bindingResult) {
        try {
            UspRegistrationBehavior registrationBehavior = new UspRegistrationBehavior();
            userRegistrationCommand.setHow("USP");
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
