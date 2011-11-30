package gs.web.util;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: 11/28/11
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExactTargetUtil {
    // GS-12317
    public static final String EMAIL_SUB_WELCOME_TRIGGER_KEY = "email_sub_welcome";
    public static final String EMAIL_SUB_WELCOME_PARAM = "esw";

    private static final String EMAIL_SUB_WELCOME_VALUE_WEEKLY = "weekly";
    private static final String EMAIL_SUB_WELCOME_VALUE_DAILY = "daily";
    private static final String EMAIL_SUB_WELCOME_VALUE_MYSTATS = "mystats";
    private static final String EMAIL_SUB_WELCOME_VALUE_PARTNER = "partner";

    private static final Map<String,String> EMAIL_SUB_WELCOME_ABBREV_ET_VALUE_MAP = new HashMap<String,String>();
    private static final Map<String,String> EMAIL_SUB_WELCOME_ET_VALUE_ABBREV_MAP = new HashMap<String,String>();

    static {
        EMAIL_SUB_WELCOME_ABBREV_ET_VALUE_MAP.put("w",EMAIL_SUB_WELCOME_VALUE_WEEKLY);
        EMAIL_SUB_WELCOME_ABBREV_ET_VALUE_MAP.put("d",EMAIL_SUB_WELCOME_VALUE_DAILY);
        EMAIL_SUB_WELCOME_ABBREV_ET_VALUE_MAP.put("m",EMAIL_SUB_WELCOME_VALUE_MYSTATS);
        EMAIL_SUB_WELCOME_ABBREV_ET_VALUE_MAP.put("p",EMAIL_SUB_WELCOME_VALUE_PARTNER);

        for (String s : EMAIL_SUB_WELCOME_ABBREV_ET_VALUE_MAP.keySet()) {
            EMAIL_SUB_WELCOME_ET_VALUE_ABBREV_MAP.put(EMAIL_SUB_WELCOME_ABBREV_ET_VALUE_MAP.get(s), s);
        }
    }

    public static String getEmailSubWelcomeParamValue(boolean weekly, boolean daily, boolean mystats, boolean partner) {
        StringBuilder s = new StringBuilder();
        if (weekly) {
            s.append(EMAIL_SUB_WELCOME_ET_VALUE_ABBREV_MAP.get(EMAIL_SUB_WELCOME_VALUE_WEEKLY));
        }
        if (daily) {
            s.append(EMAIL_SUB_WELCOME_ET_VALUE_ABBREV_MAP.get(EMAIL_SUB_WELCOME_VALUE_DAILY));
        }
        if (mystats) {
            s.append(EMAIL_SUB_WELCOME_ET_VALUE_ABBREV_MAP.get(EMAIL_SUB_WELCOME_VALUE_MYSTATS));
        }
        if (partner) {
            s.append(EMAIL_SUB_WELCOME_ET_VALUE_ABBREV_MAP.get(EMAIL_SUB_WELCOME_VALUE_PARTNER));
        }
        return s.toString();
    }

    public static Map<String, String> getEmailSubWelcomeAttributes(String eswParamValue) {
        Map<String, String> attributes = new HashMap<String, String>();
        if (StringUtils.isNotBlank(eswParamValue)) {
            for (char character : eswParamValue.toCharArray()) {
                String val = EMAIL_SUB_WELCOME_ABBREV_ET_VALUE_MAP.get(String.valueOf(character));
                if (val != null) {
                    attributes.put(val, "1");
                }
            }
        }
        return attributes;
    }
}
