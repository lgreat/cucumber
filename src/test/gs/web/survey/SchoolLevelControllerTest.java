package gs.web.survey;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.school.SchoolPageInterceptor;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

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
        SchoolLevelCommand command = (SchoolLevelCommand) modelAndView.getModel().get("command");
        assertEquals("Expected level to be set in command", LevelCode.Level.MIDDLE_LEVEL, command.getLevel());
    }

    public void testMustSelectALevel() throws Exception {
        SchoolLevelCommand command = new SchoolLevelCommand();
        BindException errors = new BindException(command, "");
        _controller.onBindAndValidate(getRequest(), command, errors);
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
