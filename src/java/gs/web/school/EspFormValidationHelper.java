package gs.web.school;

import gs.data.school.School;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    /**
     * Performs basic server-side validation. Any errors should be returned in the map as keyName -> errorMsg.
     * WARNING: This should only validate data going into esp_response. Data going to external places MUST be
     * validated at save-time by their respective save methods!
     */
    public Map<String, String> performValidation(Map<String, String[]> requestParameterMap,
                                                Set<String> keysForPage, School school) {
        Map<String, String> errors = new HashMap<String, String>();

        if (keysForPage.contains("average_class_size")) {
            _log.debug("Validating average_class_size");
            String value = getSingleValue(requestParameterMap, "average_class_size");
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
    
    protected String getSingleValue(Map<String, String[]> requestParameterMap, String key) {
        String[] values = requestParameterMap.get(key);
        if (values != null && values.length == 1) {
            return values[0];
        }
        return null;
    }
}
