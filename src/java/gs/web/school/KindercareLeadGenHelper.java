package gs.web.school;

import gs.data.school.School;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class KindercareLeadGenHelper {
    public static void checkForKindercare(HttpServletRequest request, HttpServletResponse response,
                                          School school, Map<String, Object> model) {
        // verify school
        if (school != null
                && school.getPreschoolSubtype() != null
                && school.getPreschoolSubtype().contains("kindercare")) {
            // check for cookie
            String cookieKey = school.getDatabaseState().toString() + school.getId();

            KindercareLeadGenCookie cookie = new KindercareLeadGenCookie(request, response);
            if (StringUtils.isEmpty(cookie.getProperty(cookieKey))) {
                model.put("isKindercare", Boolean.TRUE);                
                model.put("leadGenModule", "Kindercare");                
            }
        }
    }
}
