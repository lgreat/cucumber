package gs.web.school;

import gs.data.school.School;
import gs.data.state.State;
import gs.data.survey.Answer;
import gs.data.survey.ISurveyDao;
import gs.data.survey.Question;
import gs.data.survey.Survey;
import gs.web.BaseControllerTestCase;

import static org.easymock.EasyMock.*;

import java.util.*;

public class SchoolOverview2010ControllerTest extends BaseControllerTestCase {

    private SchoolOverview2010Controller _controller;

    private ISurveyDao _surveyDao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SchoolOverview2010Controller) getApplicationContext().getBean(SchoolOverview2010Controller.BEAN_ID);
        _surveyDao = createStrictMock(ISurveyDao.class);
        _controller.setSurveyDao(_surveyDao);
    }

    public void testGetOneResponseTokenForAnswer() throws Exception {
        School school = new School();
        school.setDatabaseState(State.CA);
        school.setId(999);

        String answerTitle = "Arts";

        Survey survey = new Survey();
        survey.setId(9);

        Map<Question, Answer> map = new HashMap<Question, Answer>();

        Question question = new Question();
        question.setId(2);
        Answer answer = new Answer();
        answer.setId(3);
        map.put(question, answer);

        expect(_surveyDao.findSurveyIdWithMostResultsForSchool(school)).andReturn(survey.getId());
        expect(_surveyDao.findSurveyById(9)).andReturn(survey);
        expect(_surveyDao.extractQuestionAnswerMapByAnswerTitle(survey, answerTitle)).andReturn(map);
        _surveyDao.findSurveyResultsBySchoolQuestionAnswer(isA(School.class), question.getId(), answer.getId(), survey.getId());

        replay(_surveyDao);

        _controller.getOneResponseTokenForAnswer(school, answerTitle);
    }
}
