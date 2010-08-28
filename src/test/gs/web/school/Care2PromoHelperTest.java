package gs.web.school;

import gs.web.BaseTestCase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Aug 23, 2010
 * Time: 7:12:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class Care2PromoHelperTest extends BaseTestCase {
    final private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public void testIsInCare2DateRange() throws Exception {
        // TODO-10517 - uncomment me after Magnus/QA done testing
        //assertFalse(Care2PromoHelper.isInCare2DateRange(df.parse("2010-09-19")));
        assertTrue(Care2PromoHelper.isInCare2DateRange(df.parse("2010-09-20")));
        assertTrue(Care2PromoHelper.isInCare2DateRange(df.parse("2010-09-21")));
        assertTrue(Care2PromoHelper.isInCare2DateRange(df.parse("2010-09-26")));
        assertTrue(Care2PromoHelper.isInCare2DateRange(df.parse("2010-10-01")));
        assertTrue(Care2PromoHelper.isInCare2DateRange(df.parse("2010-10-11")));
        assertTrue(Care2PromoHelper.isInCare2DateRange(df.parse("2010-10-31")));
        assertTrue(Care2PromoHelper.isInCare2DateRange(df.parse("2010-11-05")));
        assertTrue(Care2PromoHelper.isInCare2DateRange(df.parse("2010-11-11")));
        assertTrue(Care2PromoHelper.isInCare2DateRange(df.parse("2010-11-12")));
        assertFalse(Care2PromoHelper.isInCare2DateRange(df.parse("2010-11-13")));
    }
}
