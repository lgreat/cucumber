package gs.web.school;

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
}
