package gs.web.promo;

import gs.web.BaseControllerTestCase;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.community.User;

/**
 * Created by chriskimm@greatschools.net
 */
public class SchoolChoicePackPromoControllerTest extends BaseControllerTestCase {

    private SchoolChoicePackPromoController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SchoolChoicePackPromoController)getApplicationContext().getBean(SchoolChoicePackPromoController.BEAN_ID);
    }

    public void testAjaxRequest() throws Exception {
        assertTrue (true);
    }

    public void testExactTargetTrigger() throws Exception {
        User u = new User();
        u.setEmail("chriskimm@greatschools.net");
        _controller.triggerEmail(u, new String[] {"preschool", "elementary"});
    }
}
