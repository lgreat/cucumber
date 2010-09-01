package gs.web.school;

import gs.data.school.School;
import gs.data.school.SchoolHelper;
import gs.data.state.StateManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Aug 23, 2010
 * Time: 7:02:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class Care2PromoHelper {

    final private static SchoolHelper SCHOOL_HELPER = new SchoolHelper(new StateManager());    
    final private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    final private static String START_DATE = "2010-09-20";
    final private static String END_DATE = "2010-11-12";

    public static boolean isInCare2DateRange() {
        return Care2PromoHelper.isInCare2DateRange(new Date());
    }

    public static boolean isInCare2DateRange(Date dateToCheck) {
        if (dateToCheck == null) {
            return false;
        }
        String dateToCheckStr = df.format(dateToCheck);

        return (START_DATE.compareTo(dateToCheckStr) <= 0 &&
                dateToCheckStr.compareTo(END_DATE) <= 0);
    }

    public static String getCare2PromoUrl(School school) {
        if (school != null && school.getId() != null && school.getStateAbbreviation() != null) {
            String universalId = SCHOOL_HELPER.generateUniqueId(school.getStateAbbreviation(), school.getId());
            StringBuilder s = new StringBuilder("http://www.care2.com/schoolcontest/");

            // strip leading zeroes
            // http://forums.sun.com/thread.jspa?threadID=5391406
            universalId = universalId.replaceAll("^0+(?!$)", "");

            // insert a slash after 4 characters
            s.append(universalId.substring(0, 4));
            s.append("/");
            // remaining string must be 2 or 3 characters because universal ID is always 7 characters long
            s.append(universalId.substring(4));
            // insert a trailing slash
            s.append("/");

            return s.toString();
        }
        return null;
    }
}
