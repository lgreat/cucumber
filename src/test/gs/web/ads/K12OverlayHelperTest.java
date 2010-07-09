package gs.web.ads;

import gs.web.BaseControllerTestCase;
import gs.web.util.PageHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Jul 8, 2010
 * Time: 10:17:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class K12OverlayHelperTest extends BaseControllerTestCase {

    final private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public void testIsInK12OverlayDateRange() throws Exception {
        assertTrue(K12OverlayHelper.isInK12OverlayDateRange(df.parse("2010-07-12")));
        assertTrue(K12OverlayHelper.isInK12OverlayDateRange(df.parse("2010-07-13")));
        assertTrue(K12OverlayHelper.isInK12OverlayDateRange(df.parse("2010-07-19")));
        assertTrue(K12OverlayHelper.isInK12OverlayDateRange(df.parse("2010-07-20")));
        assertTrue(K12OverlayHelper.isInK12OverlayDateRange(df.parse("2010-07-26")));
        assertTrue(K12OverlayHelper.isInK12OverlayDateRange(df.parse("2010-07-27")));
        assertTrue(K12OverlayHelper.isInK12OverlayDateRange(df.parse("2010-08-02")));
        assertTrue(K12OverlayHelper.isInK12OverlayDateRange(df.parse("2010-08-09")));
        assertTrue(K12OverlayHelper.isInK12OverlayDateRange(df.parse("2010-08-16")));
        assertTrue(K12OverlayHelper.isInK12OverlayDateRange(df.parse("2010-08-23")));
        assertTrue(K12OverlayHelper.isInK12OverlayDateRange(df.parse("2010-08-30")));

        assertFalse(K12OverlayHelper.isInK12OverlayDateRange(df.parse("2010-07-11")));
        assertFalse(K12OverlayHelper.isInK12OverlayDateRange(df.parse("2010-07-14")));
        assertFalse(K12OverlayHelper.isInK12OverlayDateRange(df.parse("2010-07-15")));
    }

    public void testIsExcludeFromK12Overlay() throws Exception {
        PageHelper pageHelper = new PageHelper(getSessionContext(), getRequest());
        pageHelper.addAdKeywordMulti("editorial","Preschool");
        assertTrue(K12OverlayHelper.isExcludeFromK12Overlay(pageHelper));

        pageHelper = new PageHelper(getSessionContext(), getRequest());
        pageHelper.addAdKeywordMulti("editorial","AcademicsA");
        assertTrue(K12OverlayHelper.isExcludeFromK12Overlay(pageHelper));

        pageHelper = new PageHelper(getSessionContext(), getRequest());
        pageHelper.addAdKeywordMulti("editorial","Findaschoo");
        assertTrue(K12OverlayHelper.isExcludeFromK12Overlay(pageHelper));

        pageHelper = new PageHelper(getSessionContext(), getRequest());
        pageHelper.addAdKeywordMulti("article_id","109");
        assertTrue(K12OverlayHelper.isExcludeFromK12Overlay(pageHelper));

        pageHelper = new PageHelper(getSessionContext(), getRequest());
        pageHelper.addAdKeywordMulti("article_id","1082");
        assertTrue(K12OverlayHelper.isExcludeFromK12Overlay(pageHelper));


        pageHelper = new PageHelper(getSessionContext(), getRequest());
        pageHelper.addAdKeywordMulti("article_id","1");
        assertFalse(K12OverlayHelper.isExcludeFromK12Overlay(pageHelper));

        pageHelper = new PageHelper(getSessionContext(), getRequest());
        pageHelper.addAdKeywordMulti("editorial","other");
        assertFalse(K12OverlayHelper.isExcludeFromK12Overlay(pageHelper));
    }
}
