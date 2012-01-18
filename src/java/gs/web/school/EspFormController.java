package gs.web.school;

import gs.data.community.User;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.*;
import gs.data.security.Role;
import gs.data.state.State;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author aroy@greatschools.org
 */
@Controller
@RequestMapping("/school/esp/")
public class EspFormController implements ReadWriteAnnotationController {
    private static final Log _log = LogFactory.getLog(EspFormController.class);
    public static final String VIEW = "school/espForm";
    public static final String PATH_TO_FORM = "/school/esp/form.page"; // used by UrlBuilder
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCHOOL_ID = "schoolId";
    
    @Autowired
    private IEspMembershipDao _espMembershipDao;
    @Autowired
    private IEspResponseDao _espResponseDao;
    @Autowired
    private ISchoolDao _schoolDao;

    // TODO: If user is valid but school/state is not, redirect to landing page
    @RequestMapping(value="form.page", method=RequestMethod.GET)
    public String showForm(ModelMap modelMap, HttpServletRequest request,
                           @RequestParam(value=PARAM_SCHOOL_ID, required=false) Integer schoolId, 
                           @RequestParam(value=PARAM_STATE, required=false) State state) {
        User user = getValidUser(request, state, schoolId);
        if (user == null) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_SIGN_IN);
            return "redirect:" + urlBuilder.asFullUrl(request);
        }
        
        School school = getSchool(state, schoolId);
        if (school == null) {
            // TODO: proper error handling
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_SIGN_IN);
            return "redirect:" + urlBuilder.asFullUrl(request);
        }
        int page = getPage(request);
        int maxPage = getMaxPageForSchool(school);
        if (page < 1 || page > maxPage) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_LANDING);
            return "redirect:" + urlBuilder.asFullUrl(request);
        }
        modelMap.put("school", school);
        modelMap.put("page", page);
        modelMap.put("maxPage", maxPage);
        Set<String> keysForPage = getKeysForPage(page);
        List<EspResponse> responses = _espResponseDao.getResponsesByKeys(school, keysForPage);
        for (EspResponse response: responses) {
            modelMap.put(response.getKey(), response.getValue());
        }
        return VIEW;
    }

    @RequestMapping(value="form.page", method=RequestMethod.POST)
    public void saveForm(HttpServletRequest request, HttpServletResponse response,
                         @RequestParam(value=PARAM_SCHOOL_ID, required=false) Integer schoolId,
                         @RequestParam(value=PARAM_STATE, required=false) State state) throws IOException, JSONException {
        response.setContentType("application/json");
        
        User user = getValidUser(request, state, schoolId);
        if (user == null) {
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", "noAccess");
            errorObj.write(response.getWriter());
            response.getWriter().flush();
            return; // early exit
        }

        School school = getSchool(state, schoolId);
        if (school == null) {
            // TODO: proper error handling
            _log.error("School is null or inactive: " + school);
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", "noSchool");
            errorObj.write(response.getWriter());
            response.getWriter().flush();
            return; // early exit
        }

        int page = getPage(request);

        // Deactivate page
        Set<String> keysForPage = getKeysForPage(page);
        _espResponseDao.deactivateResponsesByKeys(school, keysForPage);

        // Save page
        List<EspResponse> responseList = new ArrayList<EspResponse>();
        // this way saves null for anything not provided
        // and won't save any extra data that isn't in keysForPage
        // I'm not yet sure that's a good thing
        for (String key: keysForPage) {
            EspResponse espResponse = new EspResponse();
            espResponse.setKey(key);
            // TODO: what to do with null?
            espResponse.setValue(request.getParameter(key));
            espResponse.setSchool(school);
            espResponse.setMemberId(user.getId());
            responseList.add(espResponse);
        }
        
        _espResponseDao.saveResponses(school, responseList);
        JSONObject successObj = new JSONObject();
        successObj.put("success", "1");
        response.getWriter().flush();
    }

    protected boolean checkUserHasAccess(User user, State state, Integer schoolId) throws ObjectRetrievalFailureException {
        if (user != null && state != null && schoolId > 0) {
            if (user.hasRole(Role.ESP_MEMBER)) {
                return _espMembershipDao.findEspMembershipByStateSchoolIdUserId
                        (state, schoolId, user.getId(), true) != null;
            } else {
                _log.warn("User " + user + " does not have required role " + Role.ESP_MEMBER + " to access ESP form.");
            }
        } else {
            _log.warn("Invalid or null user/state/schoolId: " + user + "/" + state + "/" + schoolId);
        }
        return false;
    }

    protected User getValidUser(HttpServletRequest request, State state, Integer schoolId) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = null;
        if (sessionContext != null) {
            user = sessionContext.getUser();
        }
        if (checkUserHasAccess(user, state, schoolId)) {
            return user;
        }
        return null;
    }

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
        if (school == null || !school.isActive()) {
            _log.error("School is null or inactive: " + school);
            return null;
        }

        return school;
    }

    protected int getPage(HttpServletRequest request) {
        int page = -1;
        if (request.getParameter(PARAM_PAGE) != null) {
            try {
                page = Integer.parseInt(request.getParameter(PARAM_PAGE));
            } catch (NumberFormatException nfe) {
                // fall through
            }
        }
        return page;
    }
    
    protected int getMaxPageForSchool(School school) {
        return 2; // TODO: implement
    }

    protected Set<String> getKeysForPage(int page) {
        Set<String> keys = new HashSet<String>();
        if (page == 1) {
            keys.add("admissions_url");
        } else if (page == 2) {
            keys.add("facebook_url");
        } else {
            _log.error("Unknown page provided to getKeysForPage: " + page);
        }

        return keys;
    }
}
