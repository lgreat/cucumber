package gs.web.school;

import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class SchoolInModelTest extends BaseControllerTestCase {
    private SchoolInModelController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new SchoolInModelController();
    }

    public void testModelContainsSchool() throws Exception {
        School s = new School();
        s.setId(12345);
        s.setDatabaseState(State.CA);

        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, s);
        _controller.setViewName("myview");

        ModelAndView modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());

        assertEquals(s.getId(), ((School) modelAndView.getModel().get(SchoolInModelController.MODEL_SCHOOL)).getId());
        assertEquals("myview", modelAndView.getViewName());
    }
}
