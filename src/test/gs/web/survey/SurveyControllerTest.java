package gs.web.survey;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.request.RequestInfo;
import gs.web.school.SchoolPageInterceptor;
import gs.web.util.UrlBuilder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public class SurveyControllerTest extends BaseControllerTestCase {

    private SurveyController _controller;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new SurveyController();
    }

    public void testHandleRequest_RedirectForNewSchoolProfile() throws Exception {
        School school = new School();
        school.setId(1);
        school.setNewProfileSchool(School.ProfileAndRatingFlag.NEW_PROFILE_OLD_RATING.value);
        school.setDatabaseState(State.CA);
        school.setLevelCode(LevelCode.HIGH);
        school.setCity("Alameda");
        school.setName("Alameda High School");
        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);

        ModelAndView modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());

        assertTrue(modelAndView.getView() instanceof RedirectView);
        assertTrue("Expect redirect to overview page",
                ((RedirectView) modelAndView.getView()).getUrl().contains("1-Alameda-High-School"));
        assertFalse("Expect this URL to be a k-12 style",
                ((RedirectView) modelAndView.getView()).getUrl().contains("preschools"));
    }

    public void testRedirectAllRequestsToOverviewPage() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        RequestInfo requestInfo = new RequestInfo(request);

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);
        school.setCity("Oakland");
        school.setLevelCode(LevelCode.PRESCHOOL);
        school.setName("Roy's School for Tots");
        request.setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);

        request.setAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME, requestInfo);

        ModelAndView modelAndView = _controller.handleRequestInternal(request, response);

        assertTrue(modelAndView.getView() instanceof RedirectView);
        UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        assertEquals("Expect redirect to overview page",
                urlBuilder.asSiteRelative(request), ((RedirectView) modelAndView.getView()).getUrl());
    }

}
