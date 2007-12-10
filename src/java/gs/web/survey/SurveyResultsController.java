package gs.web.survey;

import gs.data.survey.ISurveyDao;
import gs.data.survey.SurveyResults;
import gs.data.school.School;
import gs.data.state.StateManager;
import gs.data.state.State;
import gs.web.school.SchoolPageInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author chriskimm@greatschools.net
 */
public class SurveyResultsController extends AbstractController {

    public static final String BEAN_ID = "surveyResultsController";

    private ISurveyDao _surveyDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // This controller is configured to be school-aware in pages-servlet.xml
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);

        // Don't the get level from the school - in case it's a multi-level school
        String level = request.getParameter("level");

        SurveyResults results = getSurveyDao().getSurveyResultsForSchool(level, school);
        ModelAndView mAndV = new ModelAndView("survey/results");
        mAndV.getModel().put("results", results);
        mAndV.getModel().put("schooler", school);  // todo: why isn't the school available in the request?
        return mAndV;
    }

    public ISurveyDao getSurveyDao() {
        return _surveyDao;
    }

    public void setSurveyDao(ISurveyDao surveyDao) {
        _surveyDao = surveyDao;
    }
}
