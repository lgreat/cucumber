package gs.web.survey;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.School;
import gs.data.survey.*;
import gs.web.school.SchoolPageInterceptor;
import gs.web.util.ReadWriteController;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SurveyController extends SimpleFormController implements ReadWriteController {

    protected final static Log _log = LogFactory.getLog(SurveyController.class);

    public static final String BEAN_ID = "surveyController";
    private ISurveyDao _surveyDao;
    private String _viewName;
    private IUserDao _userDao;

    protected final static Pattern QUESTION_ANSWER_IDS = Pattern.compile("^responseMap\\[q(\\d+)a(\\d+)\\]\\.values*$");

    protected void initBinder(HttpServletRequest request,
                          ServletRequestDataBinder binder) {
        binder.setDisallowedFields(new String [] {"responseMap*"});
    }

    protected Object formBackingObject(HttpServletRequest request) {
        School school = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);
        Survey survey = getSurveyDao().getSurvey("test");
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();

        UserResponseCommand urc = new UserResponseCommand();
        urc.setSurvey(survey);
        urc.setSchool(school);
        urc.setUser(user);

        return urc;
    }

    protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors)
            throws Exception {
        UserResponseCommand urc = (UserResponseCommand) command;
        Enumeration<String> params = request.getParameterNames();

        if (null == urc.getUser()) {
            User user = getUserDao().findUserFromEmailIfExists(urc.getEmail());
            if (null == user) {
                user = new User();
                user.setEmail(urc.getEmail());
                getUserDao().saveUser(user);
            }
            urc.setUser(user);
        }

        populateUserResponses(request, urc);
    }

    protected void populateUserResponses(HttpServletRequest request, UserResponseCommand urc) {
        Enumeration<String> params = request.getParameterNames();
        Matcher m;
        String paramName;
        while (params.hasMoreElements()) {
            paramName = params.nextElement();
            m = QUESTION_ANSWER_IDS.matcher(paramName);

            if (m.matches()) {
                Integer qId = Integer.valueOf(m.replaceAll("$1"));
                Integer aId = Integer.valueOf(m.replaceAll("$2"));
                String [] paramValues = request.getParameterValues(paramName);

                if (null != paramValues) {
                    UserResponse response = new UserResponse();
                    response.setResponseValue(StringUtils.join(paramValues, ","));
                    response.setAnswerId(aId);
                    response.setQuestionId(qId);
                    response.setSchoolId(urc.getSchool().getId());
                    response.setState(urc.getSchool().getDatabaseState());
                    response.setSurveyId(urc.getSurvey().getId());
                    response.setUserId(urc.getUser().getId());
                    //TODO: dlee get year from form
                    response.setYear(2005);
                    urc.addToResponseMap(response);
                }
            }
        }
    }

    protected ModelAndView onSubmit(Object command) {
//        writeSurvey((UserResponseCommand)command); // debugging
        UserResponseCommand urc = (UserResponseCommand) command;
        List<UserResponse> responses = urc.getResponses();

        _surveyDao.removeAllUserResponses(urc.getSurvey(), urc.getSchool(), urc.getUser());
        _surveyDao.saveSurveyResponses(responses);
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

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
