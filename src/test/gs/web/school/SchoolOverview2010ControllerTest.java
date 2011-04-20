package gs.web.school;

import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpCity;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.survey.*;
import gs.web.BaseControllerTestCase;

import static org.easymock.EasyMock.*;

import java.util.*;

public class SchoolOverview2010ControllerTest extends BaseControllerTestCase {

    private SchoolOverview2010Controller _controller;

    private ISurveyDao _surveyDao;
    private IGeoDao _geoDao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SchoolOverview2010Controller) getApplicationContext().getBean(SchoolOverview2010Controller.BEAN_ID);
        _surveyDao = createStrictMock(ISurveyDao.class);
        _geoDao = createStrictMock(IGeoDao.class);
        _controller.setSurveyDao(_surveyDao);
        _controller.setGeoDao(_geoDao);
    }

    public void testShouldIndex() {
        BpCity bpCity = new BpCity();

        School school = new School();
        school.setDatabaseState(State.CA);
        school.setId(999);
        school.setCity("San Francisco");
        school.setStateAbbreviation(State.CA);
        school.setLevelCode(LevelCode.ELEMENTARY);

        replay(_geoDao);
        assertTrue("Should index a non-preschool without reviews", _controller.shouldIndex(school, 0L));
        verify(_geoDao);
        reset(_geoDao);

        school.setLevelCode(LevelCode.HIGH);
        replay(_geoDao);
        assertTrue("Should index a non-preschool with reviews", _controller.shouldIndex(school, 1L));
        verify(_geoDao);
        reset(_geoDao);

        // Start Preschool tests
        school.setLevelCode(LevelCode.PRESCHOOL);

        replay(_geoDao);
        assertTrue("Should index a preschool with reviews", _controller.shouldIndex(school, 1L));
        verify(_geoDao);
        reset(_geoDao);

        bpCity.setPopulation(SchoolOverview2010Controller.PRESCHOOL_CITY_POPULATION_BOUNDARY);
        expect(_geoDao.findBpCity(school.getStateAbbreviation(), school.getCity())).andReturn(bpCity);
        replay(_geoDao);
        assertTrue("Should index a preschool with no reviews but large city", _controller.shouldIndex(school, 0L));
        verify(_geoDao);
        reset(_geoDao);

        bpCity.setPopulation(SchoolOverview2010Controller.PRESCHOOL_CITY_POPULATION_BOUNDARY - 1);
        expect(_geoDao.findBpCity(school.getStateAbbreviation(), school.getCity())).andReturn(bpCity);
        replay(_geoDao);
        assertFalse("Should not index a preschool with no reviews and small city", _controller.shouldIndex(school, 0L));
        verify(_geoDao);
        reset(_geoDao);

        replay(_geoDao);
        assertFalse("Should not index if school is null", _controller.shouldIndex(null, 0L));
        verify(_geoDao);
        reset(_geoDao);

        bpCity.setPopulation(SchoolOverview2010Controller.PRESCHOOL_CITY_POPULATION_BOUNDARY - 1);
        expect(_geoDao.findBpCity(school.getStateAbbreviation(), school.getCity())).andReturn(bpCity);
        replay(_geoDao);
        assertFalse("Should not index if preschool has null reviews and small city", _controller.shouldIndex(school, null));
        verify(_geoDao);
        reset(_geoDao);
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

        List<List<String>> userResponses = new ArrayList<List<String>>();
        ArrayList<String> response = new ArrayList<String>();
        response.add("o_n_e");
        response.add("two");
        response.add("three");
        userResponses.add(response);

        expect(_surveyDao.findSurveyIdWithMostResultsForSchool(school)).andReturn(survey.getId());
        expect(_surveyDao.findSurveyById(survey.getId())).andReturn(survey);
        expect(_surveyDao.extractQuestionAnswerMapByAnswerTitle(survey, answerTitle)).andReturn(map);
        expect(_surveyDao.findFriendlyResultsBySchoolQuestionAnswer(school, question.getId(), answer.getId(), survey.getId())).andReturn(userResponses);

        replay(_surveyDao);

        String value = _controller.getOneResponseTokenForAnswer(school, answerTitle);

        assertEquals(value, "o_n_e");

        verify(_surveyDao);
    }
}
