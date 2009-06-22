package gs.web.content.cms;

import junit.framework.TestCase;
import gs.data.content.cms.CmsFeature;
import gs.web.BaseControllerTestCase;

public class CmsContentUtilsTest extends BaseControllerTestCase {
    public void testReplaceGreatSchoolsUrls() {
        String toReplace = "Hello! Visit us " +
                "<a href=\"gs://home?foo=bar&taz=whatcomesnext\">here</a>." +
                " Also, you may want to go <a href=\"gs://home?foo=bar&taz=whatcomesnext\">here</a>.";

        toReplace = CmsContentUtils.replaceGreatSchoolsUrlInString(toReplace);

        assertEquals("Hello! Visit us <a href=\"/\">here</a>." +
                " Also, you may want to go <a href=\"/\">here</a>.", toReplace);
    }
}
