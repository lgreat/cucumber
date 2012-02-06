package gs.web.admin;

import static org.apache.commons.lang.StringUtils.*;
import gs.data.school.EspMembership;
import gs.data.school.School;
import gs.data.state.State;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
    }
    
    public static final String VIEW = "admin/espModerationSearch";
    
    static final String searchStateKey, searchBindingKey;
    
    static {
        String cname = EspModerationSearchController.class.getName();
        searchStateKey = cname + ".command";
        searchBindingKey = cname + ".binding";
    }

    @Override
    protected String getViewName() {
        return VIEW;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String display(ModelMap modelMap, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session == null) throw new IllegalStateException();
        
        EspModerationSearchCommand command = (EspModerationSearchCommand) session.getAttribute(searchStateKey);
        BindingResult binding = (BindingResult) session.getAttribute(searchBindingKey);
        List<EspMembership> memberships = null;
        
        if(command == null) {
            command = new EspModerationSearchCommand();
        }
        else {
            assert binding != null;
            memberships = doSearch(command, binding);
        }
        modelMap.addAttribute("espModerationSearchCommand", command);
        modelMap.addAttribute("binding", binding); // TODO (jkirton) make use of BindingResult in espModerationSearch.jspx view
        
        if(memberships != null) populateModelWithMemberships(memberships, modelMap);
        
        modelMap.put("currentSearchCriteria", command.toPrettyString());
        
        return getViewName();
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public String handlePost(@ModelAttribute("espModerationSearchCommand") EspModerationSearchCommand command,
                                      BindingResult binding,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        if("search".equals(request.getParameter("search"))) {
            HttpSession session = request.getSession(false);
            if(session == null) throw new IllegalStateException();
            // retain search command and binding
            session.setAttribute(searchStateKey, command);
            session.setAttribute(searchBindingKey, binding);
        } else {
            // row data submission
            updateEspMembership(command, request, response);
        }
        return String.format("redirect:/%s.page", getViewName());
    }
    
    protected int resolveSchoolId(EspModerationSearchCommand command, BindingResult binding) {
        try {
            return Integer.parseInt(command.getSchoolId()); 
        } catch(NumberFormatException e) {
            binding.addError(new FieldError("espModerationSearchCommand", "schoolId", String.format("Invalid school ID: %s", command.getSchoolId())));
            return -1;
        }
    }
    
    protected int resolveUserId(EspModerationSearchCommand command, BindingResult binding) {
        try {
            return Integer.parseInt(command.getUserId()); 
        } catch(NumberFormatException e) {
            binding.addError(new FieldError("espModerationSearchCommand", "userId", String.format("Invalid user ID: %s", command.getUserId())));
            return -1;
        }
    }
    
    protected List<EspMembership> doSearch(EspModerationSearchCommand command, BindingResult binding) {
        List<EspMembership> results = null;
        
        // by state, school and user?
        if(!isEmpty(command.getStateString()) && !isEmpty(command.getSchoolId()) && !isEmpty(command.getUserId())) {
            int schoolId = resolveSchoolId(command, binding);
            int userId = resolveUserId(command, binding);
            if(schoolId != -1 && userId != -1) {
                EspMembership em = _espMembershipDao.findEspMembershipByStateSchoolIdUserId(command.getState(), schoolId, userId, false);
                if(em != null) {
                    results = new ArrayList<EspMembership>(1);
                    results.add(em);
                }
            }
        }

        // by state and school?
        else if(!isEmpty(command.getStateString()) && !isEmpty(command.getSchoolId())) {
            int schoolId = resolveSchoolId(command, binding);
            State state = command.getState();
            School school = _schoolDao.getSchoolById(state, schoolId);
            if(school == null) {
                binding.addError(new FieldError("espModerationSearchCommand", "schoolId", String.format("No school with id %s found in %s", command.getSchoolId(), command.getStateString())));
            } else {
                results = _espMembershipDao.findEspMembershipsBySchool(school, false);
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
                
                // filter by user?
                if(!isEmpty(command.getEmail())) {
                    int userId = resolveUserId(command, binding);
                    ArrayList<EspMembership> tmplist = new ArrayList<EspMembership>(results.size());
                    for(EspMembership m : results) {
                        int mUserId = m.getUser() == null ? -1 : m.getUser().getId().intValue();
                        if(userId == mUserId) {
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
            int userId = resolveUserId(command, binding);
            if(userId != -1) results = _espMembershipDao.findEspMembershipsByUserId(userId, false);
        }
        
        return results;
    }
}