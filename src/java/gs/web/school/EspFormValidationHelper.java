package gs.web.school;

import gs.data.community.User;
import gs.data.school.IEspMembershipDao;
import gs.data.school.School;
import gs.data.security.Role;
import gs.data.state.State;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Separate basic validation from the main controller.
 *
 * @see EspFormController
 *
 * @author aroy@greatschools.org
 */
@Component("espFormValidationHelper")
public class EspFormValidationHelper {
    private static final Log _log = LogFactory.getLog(EspFormValidationHelper.class);

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    /**
     * Checks if the user has access to the form for the school specified by the given state/schoolId.
     * Returns false any parameter is null, or if the user does not have an active esp membership
     * for the given state/schoolId and is not a superuser
     */
    public boolean checkUserHasAccess(User user, State state, Integer schoolId) {
        if (user != null && state != null && schoolId > 0) {
            if (user.hasRole(Role.ESP_SUPERUSER)) {
                return true;
            } else if (user.hasRole(Role.ESP_MEMBER)) {
                return _espMembershipDao.findEspMembershipByStateSchoolIdUserId
                        (state, schoolId, user.getId(), true) != null;
            } else {
                _log.warn("User " + user + " does not have required role " + Role.ESP_MEMBER + " or " + Role.ESP_SUPERUSER + " to access ESP form.");
            }
        } else {
            _log.warn("Invalid or null user/state/schoolId: " + user + "/" + state + "/" + schoolId);
        }
        return false;
    }

    /**
     * Performs basic server-side validation. Any errors should be returned in the map as keyName -> errorMsg.
     * WARNING: This should only validate data going into esp_response. Data going to external places MUST be
     * validated at save-time by their respective save methods!
     */
    public Map<String, String> performValidation(Map<String, Object[]> requestParameterMap,
                                                Set<String> keysForPage, School school) {
        Map<String, String> errors = new HashMap<String, String>();

        if (keysForPage.contains("average_class_size")) {
            _log.debug("Validating average_class_size");
            String value = (String) getSingleValue(requestParameterMap, "average_class_size");
            if (value != null) {
                try {
                    int classSize = Integer.parseInt(value);
                    if (classSize < 0) {
                        errors.put("Average class size", "Must be positive integer");
                    }
                } catch (NumberFormatException nfe) {
                    errors.put("Average class size", "Must be positive integer");
                }
            } else {
                errors.put("Average class size", "Must be positive integer");
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
}