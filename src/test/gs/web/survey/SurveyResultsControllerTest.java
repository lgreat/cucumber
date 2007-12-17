package gs.web.survey;

import gs.data.survey.SurveyResults;
import gs.data.school.School;
import gs.data.school.ISchoolDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.school.SchoolPageInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * @author chriskimm@greatschools.net
 */
public class SurveyResultsControllerTest extends BaseControllerTestCase {

    private SurveyResultsController _controller;
    private ISchoolDao _schoolDao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SurveyResultsController)getApplicationContext().
                getBean(SurveyResultsController.BEAN_ID);
        _schoolDao = (ISchoolDao)getApplicationContext().
                getBean(ISchoolDao.BEAN_ID);
    }

    public void testHandleRequest_SchoolWithResults() throws Exception {
        School school = _schoolDao.getSchoolById(State.CA, 1);
        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);
        getRequest().setParameter("level", "h");
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        Map model = mAndV.getModel();
        SurveyResults results = (SurveyResults)model.get("results");
        assertEquals(3, results.getTotalResponses());
    }

    public void testHandleRequest_SchoolWithNoResults() throws Exception {
        School school = _schoolDao.getSchoolById(State.CA, 2);
        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, school);
        getRequest().setParameter("level", "h");
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        Map model = mAndV.getModel();
        SurveyResults results = (SurveyResults)model.get("results");
        assertEquals(0, results.getTotalResponses());
    }
}
