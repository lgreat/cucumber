package gs.web.admin;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.*;
import gs.data.security.Role;
import gs.data.state.State;
import gs.web.util.ReadWriteAnnotationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: rramachandran
 * Date: 3/19/12
 * Time: 12:01 PM
 * To change this template use File | Settings | File Templates.
 */


@Controller
@RequestMapping("/admin/addMembership.page")
public class AddMembershipController implements ReadWriteAnnotationController {
    private static final String VIEW = "admin/addMembership";

    @Autowired
    private IUserDao _userDao;
    @Autowired
    protected IEspMembershipDao _espMembershipDao;
    @Autowired
    protected ISchoolDao _schoolDao;

    @RequestMapping(method = RequestMethod.GET)
    public String display(@ModelAttribute("addMembershipCommand") AddMembershipCommand command,
                          BindingResult bindingResult,
                          ModelMap modelMap,
                          HttpServletRequest request) {
        return VIEW;
    }

    @RequestMapping(method = RequestMethod.POST, params = "submit")
    public String onSubmit(@ModelAttribute("addMembershipCommand") AddMembershipCommand command,
                         BindingResult bindingResult,
                         ModelMap modelMap,
                         @RequestParam(value = "memberId", required = false) String memberId,
                         @RequestParam(value = "state", required = false) String stateAbbreviation,
                         @RequestParam(value = "schoolIds", required = false) String schoolIds){

        initialValidation(bindingResult, modelMap, memberId, stateAbbreviation, schoolIds);

        List<School> schools = new ArrayList<School>();
        modelMap.put("schools", schools);
        List<Integer> schoolIntIds = (List<Integer>) modelMap.get("schoolIntIds");
        int totalSchoolIds = schoolIntIds.size();

        if(!bindingResult.hasErrors()) {
            for(int i = 0; i < totalSchoolIds; i++) {
                validateSchoolState(bindingResult, modelMap, schoolIntIds.get(i));
            }
        }

        if(bindingResult.hasErrors()) {
            setFormFields(command, modelMap, memberId, schoolIds, stateAbbreviation, bindingResult);
            return VIEW;
        }

        EspMembership firstEspMembership = (EspMembership) modelMap.get("firstEspMembership");
        String jobTitle = firstEspMembership.getJobTitle();
        for(int i = 0; i < totalSchoolIds; i++) {
            EspMembership newEspMembership = new EspMembership();
            newEspMembership.setUser((User)modelMap.get("user"));
            newEspMembership.setState((State)modelMap.get("state"));
            newEspMembership.setSchool(schools.get(i));
            newEspMembership.setSchoolId(schoolIntIds.get(i));
            newEspMembership.setStatus(EspMembershipStatus.APPROVED);
            newEspMembership.setActive(true);
            newEspMembership.setJobTitle(jobTitle);
            _espMembershipDao.saveEspMembership(newEspMembership);
        }
        modelMap.put("onSubmitSuccess", true);
        return VIEW;
    }
    
    private void initialValidation(BindingResult bindingResult, ModelMap modelMap, String memberId,
                               String stateAbbreviation, String schoolIds) {
        int memberIntId = 0;
        State state = null;
        try {
            if(stateAbbreviation != null) {
                state = State.fromString(stateAbbreviation);
            }
            if(memberId != null) {
                memberIntId = Integer.parseInt(memberId);
            }
        }
        catch (NumberFormatException ex) {
            /* member id remains 0 */
            if(memberIntId == 0) {
                bindingResult.reject("invalid_memberId", "A valid member Id was not specified or no member found.");
            }
        }
        catch (IllegalArgumentException ex) {
            /* invalid state */
            if(state == null) {
                bindingResult.reject("invalid_state", "A valid state was not specified.");
            }
        }

        String[] schoolIdsArray = null;
        List<Integer> schoolIntIds = new ArrayList<Integer>();
        if(schoolIds != null) {
            schoolIdsArray = schoolIds.split(",");
        }
        if(schoolIdsArray == null) {
            bindingResult.reject("invalid_schoolId", "A valid school Id string was not specified.");
        }
        else {
            for(int i = 0; i < schoolIdsArray.length; i++) {
                String schoolId = schoolIdsArray[i].trim();
                int schoolIntId = 0;
                try {
                    if(schoolId != null) {
                        schoolIntId = Integer.parseInt(schoolId);
                        schoolIntIds.add(schoolIntId);
                    }
                }
                catch(NumberFormatException ex) {
                    bindingResult.reject("invalid_schoolId", schoolId + " is not a valid integer school Id.");
                }
            }
        }
        modelMap.put("state", state);
        modelMap.put("memberIntId", memberIntId);
        modelMap.put("schoolIntIds", schoolIntIds);

        if(!bindingResult.hasErrors()) {
            User user = null;
            List<EspMembership> activeEspMemberships = null;
            List<EspMembership> espMemberships = null;
            try {
                user = _userDao.findUserFromId(memberIntId);
            }
            catch(ObjectRetrievalFailureException ex) {
                bindingResult.reject("invalid_memberId", "A valid member Id was not specified or no member found.");
                return;
            }
            if(!user.getEmailVerified()) {
                bindingResult.reject("not_verified_member", "Member is not email validated.");
            }

            if(!user.hasRole(Role.ESP_MEMBER)){
                bindingResult.reject("no_espMember_role", "Member does not have ESP_MEMBER role.");
            }
            modelMap.put("user", user);
            modelMap.put("firstEspMembership", null);             
            modelMap.put("espMemberships", null);
            
            activeEspMemberships = _espMembershipDao.findEspMembershipsByUserId(user.getId(), true);
            if (activeEspMemberships.isEmpty()) {
                bindingResult.reject("esp_membership_not_approved", "Member does not have an approved ESP Membership.");
            }
            else {
                modelMap.put("firstEspMembership", activeEspMemberships.get(0));
            }
            
            espMemberships = _espMembershipDao.findEspMembershipsByUserId(user.getId(), false);
            if(!espMemberships.isEmpty()) {
                modelMap.put("espMemberships", espMemberships);
            }
        }
    }

    private void validateSchoolState(BindingResult bindingResult, ModelMap modelMap, int schoolIntId) {
        State state = (State) modelMap.get("state");
        School school = null;
        try {
            school = _schoolDao.getSchoolById(state, schoolIntId);
        }
        catch (ObjectRetrievalFailureException ex) {
            bindingResult.reject("invalid_schoolId_stateId", schoolIntId + " is an invalid school Id or no such school in state.");
            return;
        }
        if(!school.isActive()) {
            bindingResult.reject("inactive_school", schoolIntId + " belongs to an inactive school.");
        }
        if(school.isPreschoolOnly()) {
            bindingResult.reject("preschool_only", schoolIntId + " belongs to preschool level only school.");
        }
        List<School> schools = (List<School>) modelMap.get("schools");
        schools.add(school);
        modelMap.put("schools", schools);

        List<EspMembership> espMemberships = (List) modelMap.get("espMemberships");
        for(EspMembership espMembership : espMemberships) {
            if(espMembership.getSchoolId().equals(school.getId()) && espMembership.getState().equals(school.getStateAbbreviation())) {
                bindingResult.reject("has_esp_membership_to_school", "Member already has ESP Membership to the school with Id " + school.getId() + ".");
            }
        }
    }

    private void setFormFields(AddMembershipCommand command, ModelMap modelMap, String memberId, String schoolIds,
                               String stateAbbreviation, BindingResult bindingResult) {
        modelMap.put("hasErrors", true);
        command.setMemberId(memberId);
        command.setSchoolIds(schoolIds);
        command.setState(stateAbbreviation);
        Set<ObjectError> allErrors = new HashSet<ObjectError>(bindingResult.getAllErrors());
        command.setErrors(allErrors);
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public void setEspMembershipDao(IEspMembershipDao espMembershipDao) {
        _espMembershipDao = espMembershipDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }
}
