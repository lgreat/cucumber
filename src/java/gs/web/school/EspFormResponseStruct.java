package gs.web.school;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple data structure that presents data to the view in two ways. ${struct.value} fetches a single
 * string value and should be used for keys with 1-to-1 mappings to values. ${struct.valueMap} returns a
 * map of answer_key to (irrelevant) which should be used for 1-to-many mappings of key to value. It would
 * be checked like ${not empty struct.valueMap['K']}.
 *
 * Behind the scenes, addValue both sets value and creates a key in valueMap. At this point in development
 * there is no way of knowing whether any given key is 1-to-1 or 1-to-many, so this structure basically
 * separates out needing to know that.
 *
 * @author Anthony Roy (aroy@greatschools.org)
 */
public class EspFormResponseStruct {
    private String _value;
    private Map<String, Boolean> _valueMap = new HashMap<String, Boolean>();

    public String getValue() {
        return _value;
    }

    public Map<String, Boolean> getValueMap() {
        return _valueMap;
    }

    public void addValue(String value) {
        _value = value;
        _valueMap.put(value, true);
    }
    
    public String toString() {
        return getValue();
    }
}
