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
    
    @Autowired
    private IEspMembershipDao _espMembershipDao;
    @Autowired
    private IEspResponseDao _espResponseDao;
    @Autowired
    private ISchoolDao _schoolDao;

    // TODO: If user is valid but school/state is not, redirect to landing page
    @RequestMapping(value="form.page", method=RequestMethod.GET)
    public String showForm(ModelMap modelMap, HttpServletRequest request,
                           @RequestParam(value="schoolId", required=false) Integer schoolId, 
                           @RequestParam(value="state", required=false) State state, 
                           @RequestParam(value="page") int page) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = null;
        if (sessionContext != null) {
            user = sessionContext.getUser();
        }
        if (!checkUserHasAccess(user, state, schoolId)) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_SIGN_IN);
            return "redirect:" + urlBuilder.asFullUrl(request);
        }
        
        School school = null;
        try {
            school = _schoolDao.getSchoolById(state, schoolId);
        } catch (Exception e) {
            // handled below
        }
        if (school == null || !school.isActive()) {
            // TODO: proper error handling
            _log.error("School is null or inactive: " + school);
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_SIGN_IN);
            return "redirect:" + urlBuilder.asFullUrl(request);
        }
        modelMap.put("school", school);
        modelMap.put("page", page);
        Set<String> keysForPage = new HashSet<String>();
        keysForPage.add("admissions_url");
        keysForPage.add("facebook_url");
        List<EspResponse> responses = _espResponseDao.getResponsesByKeys(school, keysForPage);
        for (EspResponse response: responses) {
            modelMap.put(response.getKey(), response.getValue());
        }
        return VIEW;
    }

    @RequestMapping(value="form.page", method=RequestMethod.POST)
    public void saveForm(HttpServletRequest request, HttpServletResponse response,
                         @RequestParam(value="schoolId", required=false) Integer schoolId,
                         @RequestParam(value="state", required=false) State state,
                         @RequestParam(value="page") int page) throws IOException, JSONException {
        response.setContentType("application/json");
        
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = null;
        if (sessionContext != null) {
            user = sessionContext.getUser();
        }
        if (!checkUserHasAccess(user, state, schoolId)) {
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", "noAccess");
            errorObj.write(response.getWriter());
            response.getWriter().flush();
            return; // early exit
        }

        School school = null;
        try {
            school = _schoolDao.getSchoolById(state, schoolId);
        } catch (Exception e) {
            // handled below
        }
        if (school == null || !school.isActive()) {
            // TODO: proper error handling
            _log.error("School is null or inactive: " + school);
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", "noSchool");
            errorObj.write(response.getWriter());
            response.getWriter().flush();
            return; // early exit
        }

        // Deactivate page
        Set<String> keysForPage = new HashSet<String>();
        if (page == 1) {
            keysForPage.add("admissions_url");
        } else {
            keysForPage.add("facebook_url");
        }
        _espResponseDao.deactivateResponsesByKeys(school, keysForPage);

        // Save page
        List<EspResponse> responseList = new ArrayList<EspResponse>();
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
        if (user != null && state != null && schoolId > 0 && user.hasRole(Role.ESP_MEMBER)) {
            return _espMembershipDao.findEspMembershipByStateSchoolIdUserId
                    (state, schoolId, user.getId(), true) != null;
        }
        return false;
    }
}
