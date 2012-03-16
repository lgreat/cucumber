package gs.web.admin;

import gs.data.community.User;
import gs.data.school.EspMembership;
import gs.data.school.School;
import gs.data.state.State;
import org.apache.commons.lang.StringUtils;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: rramachandran
 * Date: 2/29/12
 * Time: 4:35 PM
 * To change this template use File | Settings | File Templates.
 */

@Controller
@RequestMapping("/admin/espModerationDetails.page")
public class EspModerationDetailsController extends AbstractEspModerationController {
    
    private static final String ESP_MODERATION_DETAILS_VIEW = "admin/espModerationDetails";
    public static final String ESP_MODERATION_VIEW = "redirect:/admin/espModerationForm.page";
    
    @RequestMapping(method = RequestMethod.GET)
    public String showForm(@ModelAttribute("espModerationDetailsCmd") EspModerationDetailsCommand command,
                           BindingResult bindingResult,
                           ModelMap modelMap,
                           HttpServletRequest request,
                           @RequestParam(value="id", required=false) String id) {
                
        int membershipId = getIntMembershipId(id);
        EspMembership espMembership = getEspMembershipDao().findEspMembershipById(membershipId, false);

        School school = null;
        try {
            school = getSchoolDao().getSchoolById(espMembership.getState(), espMembership.getSchoolId());
        }
        catch (Exception ex) {
            /* any other exception - such as nullpointer when non existing id argument is passed */
            return ESP_MODERATION_VIEW;
        }

        espMembership.setSchool(school);
        command.setEspMembership(espMembership);

        modelMap.put("espModerationDetailsCmd", command);

        return getViewName();
    }

    /* perform any of these actions - approve, reject, deactivate */
    @RequestMapping(method = RequestMethod.POST, params = "moderatorAction")
    public String onModeratorAction(@ModelAttribute("espModerationDetailsCmd") EspModerationDetailsCommand command,
                            BindingResult bindingResult,
                            ModelMap modelMap,
                            HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestParam(value="moderatorAction", required=true) String moderatorAction,
                            @RequestParam(value="id", required=false) String id) {
        int membershipId = getIntMembershipId(id);

        EspModerationCommand moderationCommand = new EspModerationCommand();
        moderationCommand.setModeratorAction(moderatorAction);
        List<Integer> espMembershipId = new ArrayList<Integer>();
        espMembershipId.add(membershipId);
        moderationCommand.setEspMembershipIds(espMembershipId);

        Map<Integer, String> espMembershipNote = new HashMap<Integer, String>();
        espMembershipNote.put(membershipId, command.getEspMembership().getNote());
        moderationCommand.setNotes(espMembershipNote);

        /* call base class method and update status and note entries for that membership in the db table */
        updateEspMembership(moderationCommand, request, response);
        return ESP_MODERATION_VIEW;
    }

    @RequestMapping(method = RequestMethod.POST, params = "save")
    public String onSave(@ModelAttribute("espModerationDetailsCmd") EspModerationDetailsCommand command,
                         BindingResult bindingResult,
                         ModelMap modelMap,
                         HttpServletRequest request,
                         @RequestParam(value="id", required=false) String id){
        int membershipId = getIntMembershipId(id);
        String newStateAbbreviation = request.getParameter("stateName");
        
        EspMembership _espMembership = getEspMembershipDao().findEspMembershipById(membershipId, false);

        String newEmail = command.getEspMembership().getUser().getEmail();
        Integer newSchoolId = command.getEspMembership().getSchoolId();
        String newJobTitle = command.getEspMembership().getJobTitle();
        String newFirstName = command.getEspMembership().getUser().getFirstName();
        String newLastName = command.getEspMembership().getUser().getLastName();
        String newNote = command.getEspMembership().getNote();
        State newState = null;
        if(newStateAbbreviation != null) {
            newState = State.fromString(newStateAbbreviation);
            command.getEspMembership().setState(newState);
        }
        
        validate(command, bindingResult, _espMembership, modelMap);

        if(bindingResult.hasErrors()) {
            return showForm(command, bindingResult, modelMap, request, id);
        }

        User espUser = _espMembership.getUser();

        espUser.setEmail(newEmail);
        espUser.setLastName(newLastName);
        espUser.setFirstName(newFirstName);

        if(StringUtils.isNotBlank(newJobTitle)) {
            _espMembership.setJobTitle(newJobTitle);
        }

        if(modelMap.get("newSchool") != null) {
            School newSchool = (School) modelMap.get("newSchool");
            if(newSchool != null) {
                _espMembership.setSchool(newSchool);
                _espMembership.setSchoolId(newSchoolId);
                _espMembership.setState(newState);
            }
        }

        _espMembership.setNote(newNote);

        Date updateDate = new Date();
        espUser.setUpdated(updateDate);
        getUserDao().updateUser(espUser);
        _espMembership.setUpdated(updateDate);
        getEspMembershipDao().updateEspMembership(_espMembership);

        command.setEspMembership(_espMembership);
        modelMap.put("espModerationDetailsCmd", command);

        return getViewName();
    }
    
    private int getIntMembershipId(String id){
        int membershipId = 0;
        if(id != null) {
            try {
                membershipId = Integer.parseInt(id);
            }
            catch (NumberFormatException ex) {
                /* in case of an exception, the membershipId remains 0 */
            }
        }
        return membershipId;
    }

    private void validate(EspModerationDetailsCommand command,
                          BindingResult bindingResult,
                          EspMembership espMembership,
                          ModelMap modelMap) {
        String newEmail = command.getEspMembership().getUser().getEmail();
        if(!newEmail.equals(espMembership.getUser().getEmail())) {
            if(StringUtils.isNotBlank(newEmail)) {
                User userForNewEmail = getUserDao().findUserFromEmailIfExists(newEmail);
                if(userForNewEmail != null && !userForNewEmail.equals(espMembership.getUser())) {
                    bindingResult.rejectValue("espMembership.user.email", "email_exists", "choose a different email address");
                    modelMap.put("emailError", true);
                }
            }
            else {
                bindingResult.rejectValue("espMembership.user.email", "invalid_email", "please enter an email address");
                modelMap.put("emailError", true);
            }
        }

        String newFirstName = command.getEspMembership().getUser().getFirstName();
        if(StringUtils.isBlank(newFirstName) || newFirstName.length() < 2 || newFirstName.length() > 24) {
            bindingResult.rejectValue("espMembership.user.firstName", "invalid_firstname", "please enter a first name");
            modelMap.put("firstNameError", true);
        }

        String newLastName = command.getEspMembership().getUser().getLastName();
        if(StringUtils.isBlank(newLastName) || newLastName.length() > 24) {
            bindingResult.rejectValue("espMembership.user.lastName", "invalid_lastname", "please enter a last name");
            modelMap.put("lastNameError", true);
        }

        State newState = command.getEspMembership().getState();
        Integer newSchoolId = command.getEspMembership().getSchoolId();
        School school = null;
        if(newSchoolId != null && newState != null) {
            try {
                school = getSchoolDao().getSchoolById(newState, newSchoolId);
                modelMap.put("newSchool", school);
            }
            catch (ObjectRetrievalFailureException ex) {
                bindingResult.rejectValue("espMembership.state", "invalid_schoolId_state", "Please enter a valid school id for the state");
                bindingResult.rejectValue("espMembership.schoolId", "invalid_schoolId_state", "Please enter a valid school id for the state");
                modelMap.put("schoolIdStateError", true);
            }
            if(school != null && (!school.isActive() || school.getLevelCode().equals("p"))) {
                bindingResult.rejectValue("espMembership.schoolId", "inactive_or_pkonly_school", "Please enter a valid school Id");
                modelMap.put("schoolIdError", true);
            }
        }
    }

    @Override
    protected  String getViewName() {
        return ESP_MODERATION_DETAILS_VIEW;
    }
}
