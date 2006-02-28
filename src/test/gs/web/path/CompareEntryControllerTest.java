package gs.web.path;

import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CompareEntryControllerTest extends BaseControllerTestCase {

    public void testNoParameters() throws Exception {
        CompareEntryController controller = new CompareEntryController();
        ModelAndView mAndV = controller.handleRequestInternal(getRequest(), getResponse());
        RedirectView view = (RedirectView)mAndV.getView();
        assertEquals("/cgi-bin/cs_where/CA?elementary=true#", view.getUrl());
    }

    public void testStateParameter() throws Exception {
        CompareEntryController controller = new CompareEntryController();
        getRequest().setParameter("state", "AK");
        getRequest().setParameter("level", "high");
        getRequest().setParameter("type", "address");
        ModelAndView mAndV = controller.handleRequestInternal(getRequest(), getResponse());
        RedirectView view = (RedirectView)mAndV.getView();
        assertEquals("/cgi-bin/cs_where/AK?high=true#address", view.getUrl());
    }
}
