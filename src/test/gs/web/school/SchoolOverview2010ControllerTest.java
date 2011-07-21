package gs.web.school;

import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpCity;
import gs.data.school.ISchoolMediaDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolMedia;
import gs.data.state.State;
import gs.data.survey.Answer;
import gs.data.survey.ISurveyDao;
import gs.data.survey.Question;
import gs.data.survey.Survey;
import gs.web.BaseControllerTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;

public class SchoolOverview2010ControllerTest extends BaseControllerTestCase {

    private SchoolOverview2010Controller _controller;

    private ISurveyDao _surveyDao;
    private IGeoDao _geoDao;

    private ISchoolMediaDao _schoolMediaDao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SchoolOverview2010Controller) getApplicationContext().getBean(SchoolOverview2010Controller.BEAN_ID);
        _surveyDao = createStrictMock(ISurveyDao.class);
        _geoDao = createStrictMock(IGeoDao.class);
        _schoolMediaDao = createStrictMock(ISchoolMediaDao.class);
        _controller.setSurveyDao(_surveyDao);
        _controller.setGeoDao(_geoDao);
        _controller.setSchoolMediaDao(_schoolMediaDao);
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

    public void testGetSchoolPhotos() throws Exception {
        School school = new School();
        List<SchoolMedia> schoolMedias = new ArrayList<SchoolMedia>();

        expect(_schoolMediaDao.getActiveBySchool(eq(school), eq(SchoolOverview2010Controller.MAX_SCHOOL_PHOTOS_IN_GALLERY))).andReturn(schoolMedias);
        replay(_schoolMediaDao);

        List<SchoolMedia> result = _controller.getSchoolPhotos(school);
        assertEquals(schoolMedias, result);

        verify(_schoolMediaDao);
    }
}
