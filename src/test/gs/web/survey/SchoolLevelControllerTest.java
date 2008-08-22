package gs.web.survey;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.school.SchoolPageInterceptor;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

public class SchoolLevelControllerTest extends BaseControllerTestCase {
    private SchoolLevelController _controller = new SchoolLevelController();

    public void testControllerRedirectsWithNoLevelForSingleLevelSchools() throws Exception {
        School school = createSchool(345, State.AZ, LevelCode.ELEMENTARY);

        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);
        getRequest().setMethod("GET");

        ModelAndView modelAndView = _controller.handleRequest(getRequest(), getResponse());
        assertEquals("Expected survey view for single-level school", _controller.getSuccessView(), modelAndView.getView());
    }

    public void testShouldReturnFormViewForMultiLevelSchools() throws Exception {
        School school = createSchool(345, State.AZ, LevelCode.ELEMENTARY_MIDDLE);

        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);
        getRequest().setMethod("GET");

        ModelAndView modelAndView = _controller.handleRequest(getRequest(), getResponse());
        assertEquals("Expected form view for multi-level school", _controller.getFormView(), modelAndView.getView());
    }

    public void testShouldForwardToSuccessViewOnSubmitWithLevel() throws Exception {
        School school = createSchool(345, State.AZ, LevelCode.ELEMENTARY_MIDDLE);

        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);
        getRequest().setMethod("POST");
        getRequest().addParameter("level", "m");

        ModelAndView modelAndView = _controller.handleRequest(getRequest(), getResponse());
        assertEquals("Expected success view on submit", _controller.getSuccessView(), modelAndView.getView());
        assertEquals("Expected level to be set in model", "m", modelAndView.getModel().get("level"));
        assertEquals("Expected school id in model", 345, modelAndView.getModel().get("id"));
        assertEquals("Expected state in model", "AZ", modelAndView.getModel().get("state"));
    }

    public void testShouldReturnFormViewIfNoLevelSubmitted() throws Exception {
        School school = createSchool(345, State.AZ, LevelCode.ELEMENTARY_MIDDLE);

        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);
        getRequest().setMethod("POST");

        ModelAndView modelAndView = _controller.handleRequest(getRequest(), getResponse());
        assertEquals("Expected form view when no level is submitted", _controller.getFormView(), modelAndView.getView());
    }

    public void testMustSelectALevel() throws Exception {
        SchoolLevelCommand command = new SchoolLevelCommand();
        BindException errors = new BindException(command, "");
        _controller.onBind(getRequest(), command, errors);
        assertTrue("Expected level error message", errors.hasFieldErrors("level"));
    }

    private School createSchool(int schoolId, State state, LevelCode level) {
        School school = new School();
        school.setId(schoolId);
        school.setStateAbbreviation(state);
        school.setLevelCode(level);
        return school;
    }
}
