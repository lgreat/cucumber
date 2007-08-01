package gs.web.survey;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.survey.Survey;
import gs.data.survey.ISurveyDao;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SurveyAdminController extends AbstractController {

    public static final String BEAN_ID = "/survey/admin.page";
    private ISurveyDao _surveyDao;
    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        Map model = new HashMap();
        model.put("surveys", getSurveyDao().getSurveys());
        return new ModelAndView("survey/admin", model);
    }

    public ISurveyDao getSurveyDao() {
        return _surveyDao;
    }

    public void setSurveyDao(ISurveyDao surveyDao) {
        _surveyDao = surveyDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
