package gs.web.survey;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import javax.servlet.http.HttpServletRequest;

import gs.data.survey.*;
import gs.data.community.User;
import gs.web.util.ReadWriteController;
import gs.web.util.context.SessionContextUtil;

import java.util.*;

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

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        User user = SessionContextUtil.getSessionContext(request).getUser();
        Survey survey = getSurveyDao().getSurvey("test");
        return survey;
    }
     */

    /**
    protected void onBindOnNewForm(HttpServletRequest request, Object command, BindException errors)
            throws Exception {
        request.setAttribute(getCommandName(), command);
        request.setAttribute("command", command);
    }
     */

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        Map model = new HashMap();
        Survey survey = getSurveyDao().getSurvey("test");
        model.put("survey", survey);
        return model;
	}

    protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors)
            throws Exception {
        System.out.println("request: " + request);
        System.out.println("command: " + command);
    }

    protected ModelAndView onSubmit(Object command) {
//        writeSurvey((SurveyCommand)command); // debugging
        List<UserResponse> responses = ((SurveyCommand)command).getResponses();
        if (responses != null) {
            _surveyDao.saveSurveyResponses(responses);
        }
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

    void writeSurvey(Survey survey) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Survey Title: ");
        String title = survey.getTitle();
        buffer.append(title != null ? title : "null");
        buffer.append("\n");
        List<QuestionGroup> questionGroups = survey.getQuestionGroups();
        for (QuestionGroup group: questionGroups) {
            String groupTitle = group.getTitle();
            buffer.append("  Group: ");
            buffer.append(groupTitle != null ? groupTitle : "null");
            buffer.append("\n");
            List<Question> questions = group.getQuestions();
            for (Question question : questions) {
                String questionText = question.getText();
                buffer.append("    question: ");
                buffer.append(questionText != null ? questionText : "null");
                buffer.append("\n");
                List<Answer> answers = question.getAnswers();
                for (Answer answer : answers) {
                    buffer.append("    available answers: ");
                    for (String ans : answer.getAvailableAnswers()) {
                        buffer.append(ans);
                        buffer.append(":");
                    }
                    buffer.append("\n");
                    for (String ans : answer.getMyAnswers()) {
                        buffer.append("    user answers: ");
                        buffer.append(ans);
                        buffer.append(":");
                    }
                    buffer.append("\n");
                }
            }
        }
        System.out.println (buffer.toString());
    }

    SurveyCommand buildSurveyCommand(Survey survey) {
        SurveyCommand command = new SurveyCommand();

        

        return command;
    }
}
