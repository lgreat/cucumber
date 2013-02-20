package gs.web.util;

import gs.data.school.School;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.BaseTestCase;

/**
 * @author aroy@greatschools.org
 */
public class AdUtilTest extends BaseTestCase {

    public void testK12AffiliateLink() {
        assertNull("Expect null-safety", AdUtil.getK12AffiliateLinkForSchool(null, null));
        assertNull("Expect null-safety", AdUtil.getK12AffiliateLinkForSchool(null, "te"));
        School school1 = new School();
        assertNull("Expect null state/id safety", AdUtil.getK12AffiliateLinkForSchool(school1, "te"));
        school1.setPhysicalAddress(new Address());
        school1.getPhysicalAddress().setState(State.CA);
        assertNull("Expect null id safety", AdUtil.getK12AffiliateLinkForSchool(school1, "te"));
        school1.setId(1);
        assertNull("Expect CA-1 to NOT be a k12 affiliate", AdUtil.getK12AffiliateLinkForSchool(school1, "te"));
        assertNull("Expect null-safety on traffic driver code", AdUtil.getK12AffiliateLinkForSchool(school1, null));

        school1.setId(12222);
        assertNotNull("Expect CA-12222 to be a k12 affiliate", AdUtil.getK12AffiliateLinkForSchool(school1, "te"));
        assertTrue(AdUtil.getK12AffiliateLinkForSchool(school1, "zz").contains("zz"));
    }
}
