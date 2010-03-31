package gs.web.school;

import gs.data.school.School;
import gs.data.state.State;
import junit.framework.TestCase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Mar 31, 2010
 * Time: 1:26:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class Care2PromoHelperTest extends TestCase {
    public void testGetCare2PromoUrl() {
        School school = new School();

        // state fips codes
        // http://www.bls.gov/lau/lausfips.htm

        school.setStateAbbreviation(State.AK);
        school.setId(1);
        // universal id 0200001
        assertEquals("http://www.care2.com/schoolcontest/2000/01/", Care2PromoHelper.getCare2PromoUrl(school));

        school.setStateAbbreviation(State.WY);
        school.setId(12345);
        // universal id 5612345
        assertEquals("http://www.care2.com/schoolcontest/5612/345/", Care2PromoHelper.getCare2PromoUrl(school));
    }

    public void testIsDuringCare2PromoPeriod() throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat preciseDf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");

        Date date = df.parse("2010-04-12");
        assertTrue(Care2PromoHelper.isDuringCare2PromoPeriod(date));

        date = df.parse("2010-04-14");
        assertTrue(Care2PromoHelper.isDuringCare2PromoPeriod(date));

        date = df.parse("2010-05-20");
        assertTrue(Care2PromoHelper.isDuringCare2PromoPeriod(date));

        date = df.parse("2010-05-21");
        assertTrue(Care2PromoHelper.isDuringCare2PromoPeriod(date));

        date = df.parse("2010-05-22");
        assertFalse("Expected to return false for 2010-05-22", Care2PromoHelper.isDuringCare2PromoPeriod(date));

        date = preciseDf.parse("2010-05-21 11:59:59 PM");
        assertTrue(Care2PromoHelper.isDuringCare2PromoPeriod(date));

        date = preciseDf.parse("2010-05-22 12:00:01 AM");
        assertFalse(Care2PromoHelper.isDuringCare2PromoPeriod(date));

        date = preciseDf.parse("2010-05-22 12:00:00 AM");
        assertFalse(Care2PromoHelper.isDuringCare2PromoPeriod(date));
    }
}