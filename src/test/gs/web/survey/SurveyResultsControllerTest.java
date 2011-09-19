package gs.web.survey;

import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.state.State;
import gs.data.survey.ISurveyDao;
import gs.data.survey.SurveyResults;
import gs.web.BaseControllerTestCase;
import gs.web.school.SchoolPageInterceptor;
import static org.easymock.classextension.EasyMock.*;

import gs.web.school.SchoolProfileHeaderHelper;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

/**
 * @author chriskimm@greatschools.org
 */
public class SurveyResultsControllerTest extends BaseControllerTestCase {

    private SurveyResultsController _controller;
    private ISchoolDao _schoolDao;
    private ISurveyDao _surveyDao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SurveyResultsController)getApplicationContext().
                getBean(SurveyResultsController.BEAN_ID);
        _schoolDao = (ISchoolDao)getApplicationContext().
                getBean(ISchoolDao.BEAN_ID);
        _surveyDao = createMock(ISurveyDao.class);
        _controller.setSurveyDao(_surveyDao);
        SchoolProfileHeaderHelper _schoolProfileHeaderHelper = createStrictMock(SchoolProfileHeaderHelper.class);
        _controller.setSchoolProfileHeaderHelper(_schoolProfileHeaderHelper);
    }

    public void testHandleRequest_SchoolWithResults() throws Exception {
        School school = _schoolDao.getSchoolById(State.CA, 1);
        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);
        getRequest().setParameter("level", "h");

        SurveyResults expectedResults = new SurveyResults();
        expect(_surveyDao.getSurveyResultsForSchool("h", school)).andReturn(expectedResults);
        replay(_surveyDao);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        Map model = mAndV.getModel();
        SurveyResults results = (SurveyResults)model.get("results");
        assertSame("Unexpected results", expectedResults, results);

        verify(_surveyDao);
        reset(_surveyDao);
    }

    public void testHandleRequest_specificPages() throws Exception {
        // if no page is specified, page 1 should be returned.
    }

    public void testRedirectForPreschools() throws Exception {
        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);

        school.setLevelCode(LevelCode.PRESCHOOL);
        ModelAndView modelAndView = _controller.getPreschoolRedirectViewIfNeeded(getRequest(), school);
        assertEquals("http://pk.greatschools.org/survey/results.page?id=1&state=CA", ((RedirectView)modelAndView.getView()).getUrl());

        school.setLevelCode(LevelCode.PRESCHOOL_ELEMENTARY);
        school.setType(SchoolType.PUBLIC);
        modelAndView = _controller.getPreschoolRedirectViewIfNeeded(getRequest(), school);
        assertNull(modelAndView);
    }
}
