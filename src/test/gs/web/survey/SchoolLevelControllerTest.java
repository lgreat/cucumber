package gs.web.survey;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.school.SchoolPageInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class SchoolLevelControllerTest extends BaseControllerTestCase {
    private SchoolLevelController _controller = new SchoolLevelController();

    public void testControllerRedirectsWithNoLevelForSingleLevelSchools() throws Exception {
        School school = createSchool(345, State.AZ, LevelCode.ELEMENTARY);

        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);
        getRequest().setMethod("GET");

        _controller.handleRequest(getRequest(), getResponse());
        String expectedRedirectUrl = "/survey/form.page?id=345&state=AZ";
        assertEquals("Unexpected redirect URL", expectedRedirectUrl, getResponse().getRedirectedUrl());
    }

    public void testShouldReturnFormViewForMultiLevelSchools() throws Exception {
        School school = createSchool(345, State.AZ, LevelCode.ELEMENTARY_MIDDLE);

        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);
        getRequest().setMethod("GET");

        ModelAndView modelAndView = _controller.handleRequest(getRequest(), getResponse());
        assertEquals("Expected form view for multi-level school", _controller.getFormView(), modelAndView.getView());
    }

    private School createSchool(int schoolId, State state, LevelCode level) {
        School school = new School();
        school.setId(schoolId);
        school.setStateAbbreviation(state);
        school.setLevelCode(level);
        return school;
    }

}
