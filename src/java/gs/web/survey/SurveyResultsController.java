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
        String level = request.getParameter("level");
        String id = request.getParameter("id");
        String state = request.getParameter("state");
        School s = new School();
        s.setId(Integer.parseInt(id));
        StateManager sm = new StateManager();
        s.setDatabaseState(sm.getState(state));
        SurveyResults results = getSurveyDao().getSurveyResultsForSchool(level, s);
        ModelAndView mAndV = new ModelAndView("survey/results");
        mAndV.getModel().put("results", results);
        return mAndV;
    }

    public ISurveyDao getSurveyDao() {
        return _surveyDao;
    }

    public void setSurveyDao(ISurveyDao surveyDao) {
        _surveyDao = surveyDao;
    }
}
