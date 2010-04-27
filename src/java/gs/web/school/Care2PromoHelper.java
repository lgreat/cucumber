package gs.web.school;

import gs.data.school.Grade;
import gs.data.school.Grades;
import gs.data.school.School;
import gs.data.school.SchoolHelper;
import gs.data.state.StateManager;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author Young Fan
 */
public class Care2PromoHelper {
    final private static Grades K_TO_8_GRADES = Grades.createGrades(Grade.KINDERGARTEN, Grade.G_8);
    final private static SchoolHelper SCHOOL_HELPER = new SchoolHelper(new StateManager());
    public static void checkForCare2(HttpServletRequest request, HttpServletResponse response,
                                          School school, Map<String, Object> model) {
        // verify school
        if (school != null
                && school.getGradeLevels() != null
                && school.getGradeLevels().containsAny(K_TO_8_GRADES)
                && Care2PromoHelper.isDuringCare2PromoPeriod(new Date())) {
                model.put("isCare2", Boolean.TRUE);
                model.put("care2PromoUrl", Care2PromoHelper.getCare2PromoUrl(school));
        }
    }

    final private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
    private static Date CARE2_PROMO_END_DATE = null;
    static {
        try {
            // contest ends after 2010-05-21 11:59pm
            CARE2_PROMO_END_DATE = df.parse("2010-05-22 12:00:00 AM");
        } catch (ParseException e) {
            // nothing to do, but we aren't supposed to get here anyway
        }
    }

    public static boolean isDuringCare2PromoPeriod(Date dateForComparison) {
        // return true if dateForComparison is before Care2 Promo end date
        return dateForComparison.compareTo(CARE2_PROMO_END_DATE) < 0;
    }

    public static String getCare2PromoUrl(School school) {
        String universalId = SCHOOL_HELPER.generateUniqueId(school.getStateAbbreviation(), school.getId());
        StringBuilder s = new StringBuilder("http://www.care2.com/schoolcontest/");

        // strip leading zeroes
        // http://forums.sun.com/thread.jspa?threadID=5391406
        universalId = universalId.replaceAll("^0+(?!$)", "");

        // insert a slash after 4 characters
        s.append(universalId.substring(0,4));
        s.append("/");
        // remaining string must be 2 or 3 characters because universal ID is always 7 characters long
        s.append(universalId.substring(4));
        // insert a trailing slash
        s.append("/");

        return s.toString();
    }
}
