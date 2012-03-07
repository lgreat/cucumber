package gs.web.admin;

import static org.apache.commons.lang.StringUtils.*;
import gs.data.school.EspMembership;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.UrlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/admin/espModerationSearch.page")
public class EspModerationSearchController extends AbstractEspModerationController {
    
    public static class EspModerationSearchCommand extends EspModerationCommand {
        private String _stateString;
        private String _schoolId;
        private String _userId;
        private String _email;
        
        public boolean isCommandEmpty() {
            return isEmpty(_stateString) && isEmpty(_schoolId) && isEmpty(_userId) && isEmpty(_email);
        }
        
        /**
         * @return the resolved State or null
         */
        public State getState() {
            if(isEmpty(_stateString)) return null;
            try {
                return State.fromString(_stateString);
            } catch(Exception e) {
                return null;
            }
        }
        
        public String getStateString() {
            return _stateString;
        }
        
        public void setStateString(String st) {
            this._stateString = st;
        }
        
        public String getSchoolId() {
            return _schoolId;
        }
        
        public void setSchoolId(String id) {
            this._schoolId = id;
        }
        
        public String getUserId() {
            return _userId;
        }
        
        public void setUserId(String userId) {
            this._userId = userId;
        }
        
        public String getEmail() {
            return _email;
        }
        
        public void setEmail(String email) {
            this._email = email;
        }

        @Override
        public String toString() {
            return String.format("EspModerationSearchCommand [_stateString=%s, _schoolId=%s, _userId=%s, _email=%s]", _stateString, _schoolId, _userId, _email);
        }

        public String toPrettyString() {
            StringBuilder sb = new StringBuilder();
            if(!isEmpty(_stateString)) {
                sb.append(", State: ");
                sb.append(_stateString);
            }
            if(!isEmpty(_schoolId)) {
                sb.append(", School ID: ");
                sb.append(_schoolId);
            }
            if(!isEmpty(_userId)) {
                sb.append(", User account ID: ");
                sb.append(_userId);
            }
            if(!isEmpty(_email)) {
                sb.append(", Email: ");
                sb.append(_email);
            }
            return sb.length() == 0 ? null : sb.substring(2);
        }
        
        public String getSearchQueryString() {
            Map<String, String> vmap = new HashMap<String, String>(4);
            vmap.put("stateString", _stateString);
            vmap.put("schoolId", _schoolId);
            vmap.put("userId", _userId);
            vmap.put("email", _email);
            return UrlUtil.getQueryStringFromMap(vmap);
        }
    }
    
    public static final String VIEW = "admin/espModerationSearch";
    
    @Override
    protected String getViewName() {
        return VIEW;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String display(ModelMap modelMap, HttpServletRequest request, @ModelAttribute("espModerationSearchCommand") EspModerationSearchCommand command) {
        List<EspMembership> memberships = null;
        
        if(command == null) {
            command = new EspModerationSearchCommand();
        }
        if(!command.isCommandEmpty()) {
            memberships = doSearch(command);
        }
        modelMap.addAttribute("espModerationSearchCommand", command);
        
        if(memberships != null) populateModelWithMemberships(memberships, modelMap);
        
        modelMap.put("currentSearchCriteria", command.toPrettyString());
        
        return getViewName();
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public String handlePost(@ModelAttribute("espModerationSearchCommand") EspModerationSearchCommand command,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        if(!"search".equals(request.getParameter("search"))) {
            // row data submission
            updateEspMembership(command, request, response);
        }

        String redirect = String.format("redirect:/%s.page", getViewName());
        String qs = command.getSearchQueryString();
        redirect = String.format("%s?%s", redirect, qs);
        return redirect;
    }
    
    protected int resolveSchoolId(EspModerationSearchCommand command) {
        try {
            return Integer.parseInt(command.getSchoolId()); 
        } catch(NumberFormatException e) {
            return -1;
        }
    }
    
    protected int resolveUserId(EspModerationSearchCommand command) {
        try {
            return Integer.parseInt(command.getUserId()); 
        } catch(NumberFormatException e) {
            return -1;
        }
    }
    
    protected List<EspMembership> doSearch(EspModerationSearchCommand command) {
        List<EspMembership> results = null;
        
        // by state, school and user?
        if(!isEmpty(command.getStateString()) && !isEmpty(command.getSchoolId()) && !isEmpty(command.getUserId())) {
            int schoolId = resolveSchoolId(command);
            int userId = resolveUserId(command);
            if(schoolId != -1 && userId != -1) {
                EspMembership em = _espMembershipDao.findEspMembershipByStateSchoolIdUserId(command.getState(), schoolId, userId, false);
                if(em != null) {
                    results = new ArrayList<EspMembership>(1);
                    results.add(em);
                }
            }
        }

        // by state and school or state, school and email?
        else if(!isEmpty(command.getStateString()) && !isEmpty(command.getSchoolId())) {
            int schoolId = resolveSchoolId(command);
            State state = command.getState();
            try {
                School school = _schoolDao.getSchoolById(state, schoolId);
                if(school != null) {
                    results = _espMembershipDao.findEspMembershipsBySchool(school, false);
                }
            } catch(ObjectRetrievalFailureException orfe) {
                // results remains null, do nothing
            }

            if(results != null && results.size() > 0) {
                // filter by email?
                if(!isEmpty(command.getEmail())) {
                    ArrayList<EspMembership> tmplist = new ArrayList<EspMembership>(results.size());
                    for(EspMembership m : results) {
                        if(m.getUser() != null && command.getEmail().equals(m.getUser().getEmail())) {
                            tmplist.add(m);
                        }
                    }
                    results = tmplist;
                }
            }
        }
        
        // by email only?
        else if(!isEmpty(command.getEmail())) {
            results = _espMembershipDao.findEspMembershipsByUserEmail(command.getEmail(), false);
        }
        
        // by user only?
        else if(!isEmpty(command.getUserId())) {
            int userId = resolveUserId(command);
            if(userId != -1) {
                results = _espMembershipDao.findEspMembershipsByUserId(userId, false);
            }
        }
        
        return results;
    }
}