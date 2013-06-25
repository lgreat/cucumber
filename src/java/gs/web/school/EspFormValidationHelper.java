package gs.web.school;

import gs.data.community.User;
import gs.data.school.*;
import gs.data.security.Role;
import gs.data.state.State;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Separate basic validation from the main controller.
 *
 * @see OspFormController
 *
 * @author aroy@greatschools.org
 */
@Component("espFormValidationHelper")
public class EspFormValidationHelper {
    private static final Log _log = LogFactory.getLog(EspFormValidationHelper.class);

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @Autowired
    private ISchoolDao _schoolDao;

    /**
     * Checks if the user has access to the form for the school specified by the given state/schoolId.
     * Returns false any parameter is null, or if the user does not have an active esp membership
     * for the given state/schoolId and is not a superuser
     */
    public boolean checkUserHasAccess(User user, State state, School school) {
        if (user != null && state != null && school!= null && school.getId() > 0) {
            if (user.hasRole(Role.ESP_SUPERUSER)) {
                //If there is a provisional user for the school, then block out other users.GS-13363.
                if(getProvisionalMembershipForSchool(school,user) != null){
                 return false;
                }
                return true;
            } else if (user.hasRole(Role.ESP_MEMBER)) {
                EspMembership provisionalMembership = getProvisionalMembershipForSchool(school,user);
                EspMembership espMembership = _espMembershipDao.findEspMembershipByStateSchoolIdUserId
                        (state, school.getId(), user.getId(), true);
                //If there is a provisional user for the school, then block out other users.GS-13363.
                if( provisionalMembership == null && espMembership != null){
                    return true;
                }
                return  false;
            } else {
                // Check for provisional user (JIRA 13363)
                EspMembership membership = _espMembershipDao.findEspMembershipByStateSchoolIdUserId
                        (state, school.getId(), user.getId(), false);
                if( membership != null && membership.getStatus() == EspMembershipStatus.PROVISIONAL ) {
                    return true;
                }
                else {
                    _log.warn("User " + user + " does not have required role " + Role.ESP_MEMBER + " or " + Role.ESP_SUPERUSER + " to access ESP form.");
                }
            }
        } else {
            _log.warn("Invalid or null user/state/schoolId: " + user + "/" + state + "/" + school.getId());
        }
        return false;
    }

    public boolean checkUserHasAccess(User user, State state, Integer schoolId) {
      School school = _schoolDao.getSchoolById(state,schoolId);
      return checkUserHasAccess(user,state,school);
    }

    /**
     * Performs basic server-side validation. Any errors should be returned in the map as keyName -> errorMsg.
     * WARNING: This should only validate data going into esp_response. Data going to external places MUST be
     * validated at save-time by their respective save methods!
     */
    public Map<String, String> performValidation(Map<String, Object[]> requestParameterMap,
                                                Set<String> keysForPage, School school) {
        Map<String, String> errors = new HashMap<String, String>();
        String avgClassSizeKey = "average_class_size";
        if (keysForPage.contains(avgClassSizeKey)) {
            _log.debug("Validating average_class_size");
            String value = (String) getSingleValue(requestParameterMap, "average_class_size");
            if (value != null) {
                try {
                    int classSize = Integer.parseInt(value);
                    if (classSize < 0) {
                        errors.put(avgClassSizeKey, "Must be positive integer");
                    }
                } catch (NumberFormatException nfe) {
                    errors.put(avgClassSizeKey, "Must be positive integer");
                }
            } else {
                errors.put(avgClassSizeKey, "Must be positive integer");
            }
            if(errors.containsKey(avgClassSizeKey)){
                keysForPage.remove(avgClassSizeKey);
            }
        }
        return errors;
    }

    protected Object getSingleValue(Map<String, Object[]> requestParameterMap, String key) {
        Object[] values = requestParameterMap.get(key);
        if (values != null && values.length == 1) {
            return values[0];
        }
        return null;
    }

    /**
     * Check if a school has any provisional users.
     * @param espMemberships
     * @param userToExclude - User currently viewing the esp form.Therefore exclude them.
     * @return
     */
    protected EspMembership getProvisionalMembershipForSchool(List<EspMembership> espMemberships, User userToExclude) {
        for (EspMembership membership : espMemberships) {
            if (membership.getUser().getId() == userToExclude.getId()) {
                continue;
            }
            if (membership.getStatus().equals(EspMembershipStatus.PROVISIONAL)) {
                return membership;
            }
        }
        return null;
    }

    protected EspMembership getProvisionalMembershipForSchool(School school, User userToExclude) {
        List<EspMembership> espMemberships = _espMembershipDao.findEspMembershipsBySchool(school, false);
        return getProvisionalMembershipForSchool(espMemberships, userToExclude);
    }

    /**
     * Checks if the user is provisional
     */
    public boolean isUserProvisional(User user) {
        List<EspMembership> memberships = _espMembershipDao.findEspMembershipsByUserId(user.getId(), false);
        if (memberships != null && !memberships.isEmpty()) {
            return memberships.get(0).getStatus().equals(EspMembershipStatus.PROVISIONAL);
        }
        return false;
    }

    public void setEspMembershipDao(IEspMembershipDao espMembershipDao) {
        _espMembershipDao = espMembershipDao;
    }
}