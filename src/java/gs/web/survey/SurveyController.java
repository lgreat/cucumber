package gs.web.survey;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.survey.Survey;
import gs.data.survey.ISurveyDao;
import gs.web.util.ReadWriteController;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SurveyController extends SimpleFormController implements ReadWriteController {

    public static final String BEAN_ID = "/survey/form.page";
    private ISurveyDao _surveyDao;
    private String _viewName;

    /**
     * This method creates a Survey object pre-loaded with a user's previous response if
     * she has alread taken this survey.
     *
      * @param request ServletRequest
     * @return a <code>Survey</code> type.
     * @throws Exception
     */
    protected void onBindOnNewForm(HttpServletRequest request, Object command, BindException errors)
            throws Exception {        //
        System.out.println("command before: " + command);
        Survey survey = getSurveyDao().getSurvey("test");
        command = survey;
        System.out.println("command after: " + command);        
    }

    /*
    protected ModelAndView showForm(HttpServletRequest request,
                                             HttpServletResponse response,
                                             BindException errors) {
        Map model = new HashMap();
        model.put("survey", getSurveyDao().getSurveys());
        return new ModelAndView(getFormView(), model);        
    }
    */

    protected ModelAndView onSubmit(Object command) {
        System.out.println ("command: " + command);
//        getSurveyDao().
//         (Survey)command;
        return new ModelAndView(getSuccessView());
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