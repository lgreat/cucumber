package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public class CompareSchoolControllerTest extends BaseControllerTestCase {

    private CompareSchoolController _controller;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new CompareSchoolController();
    }

    public void testSubmit() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        ModelAndView mAndV = _controller.handleRequestInternal(request, getResponse());
        assertEquals("/cgi-bin/cs_compare/null?compare_type=null&city=null&school_selected=null&level=&from=form",
                ((RedirectView)mAndV.getView()).getUrl());
    }
}
