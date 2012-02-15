package gs.web.compare;

import gs.data.school.*;
import gs.data.state.State;
import gs.data.survey.*;
import gs.web.BaseControllerTestCase;
import org.apache.commons.lang.WordUtils;

import java.util.*;

import static gs.web.compare.CompareProgramsExtracurricularsController.*;
import static org.easymock.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CompareProgramsExtracurricularsControllerTest extends BaseControllerTestCase {
    private CompareProgramsExtracurricularsController _controller;
    private ISurveyDao _surveyDao;
    private IEspResponseDao _espResponseDao;

    private ComparedSchoolProgramsExtracurricularsStruct _struct;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CompareProgramsExtracurricularsController();

        _surveyDao = createStrictMock(ISurveyDao.class);
        _espResponseDao = createStrictMock(IEspResponseDao.class);

        _controller.setSurveyDao(_surveyDao);
        _controller.setEspResponseDao(_espResponseDao);
        _controller.setSuccessView("success");

        _struct = new ComparedSchoolProgramsExtracurricularsStruct();
    }
    
    public void replayAllMocks() {
        replayMocks(_surveyDao, _espResponseDao);
    }

    public void verifyAllMocks() {
        verifyMocks(_surveyDao, _espResponseDao);
    }

    public void testBasics() {
        assertSame(_surveyDao, _controller.getSurveyDao());
        assertSame(_espResponseDao, _controller.getEspResponseDao());
        assertEquals("success", _controller.getSuccessView());
        assertEquals(ComparedSchoolProgramsExtracurricularsStruct.class, _controller.getStruct().getClass());
    }
    
    private EspResponse getEspResponse(String value) {
        return getEspResponse(null, value);
    }
    
    private EspResponse getEspResponse(String key, String value) {
        EspResponse response = new EspResponse();
        response.setKey(key);
        response.setValue(value);
        response.setPrettyValue(WordUtils.capitalizeFully(value.replace('_', ' ')));
        return response;
    }
    
    private List<EspResponse> getEspResponseList(String... values) {
        List<EspResponse> responses = new ArrayList<EspResponse>();
        for (String value: values) {
            responses.add(getEspResponse(value));
        }
        return responses;
    }
    
    private List<EspResponse> getEspResponseListWithKey(String key, String... values) {
        List<EspResponse> responses = new ArrayList<EspResponse>();
        for (String value: values) {
            responses.add(getEspResponse(key, value));
        }
        return responses;
    }
    
    public void testParseESPValues() {
        replayAllMocks();
        Set<String> category = new TreeSet<String>();

        _controller.parseESPValues(category, getEspResponseList("alpha", "bravo"), getEspResponseList("value, other"));
        assertEquals(4, category.size());
        assertEquals("Alpha", category.toArray()[0]);
        assertEquals("Bravo", category.toArray()[1]);
        assertEquals("Other", category.toArray()[2]);
        assertEquals("Value", category.toArray()[3]);

        _controller.parseESPValues(category, getEspResponseList("charlie"));
        assertEquals(5, category.size());
        assertEquals("Alpha", category.toArray()[0]);
        assertEquals("Bravo", category.toArray()[1]);
        assertEquals("Charlie", category.toArray()[2]);
        assertEquals("Other", category.toArray()[3]);
        assertEquals("Value", category.toArray()[4]);

        Set<String> category2 = new TreeSet<String>();
        _controller.parseESPValues(category2, null, getEspResponseList("This , category, is, awesome!"));
        assertEquals(4, category2.size());
        assertEquals("Awesome!", category2.toArray()[0]);
        assertEquals("Category", category2.toArray()[1]);
        assertEquals("Is", category2.toArray()[2]);
        assertEquals("This", category2.toArray()[3]);

        verifyAllMocks();
    }

    public void testProcessESPResults() {
        replayAllMocks();
        Map<String, List<EspResponse>> responseMap = new HashMap<String, List<EspResponse>>();

        _struct.setCategoryResponses(new HashMap<String, Set<String>>());

        _controller.processESPResults(_struct, responseMap);
        assertNotNull(_struct.getCategoryResponses());
        assertEquals(0, _struct.getCategoryResponses().size());

        _struct.getCategoryResponses().put(ROW_LABEL_ARTS, new TreeSet<String>());
        responseMap.put("arts_media", getEspResponseList("band", "chorus", "drawing/painting"));
        responseMap.put("arts_music", getEspResponseList("origami", "chess"));

        _controller.processESPResults(_struct, responseMap);
        assertNotNull(_struct.getCategoryResponses());
        assertEquals(1, _struct.getCategoryResponses().size());
        Set<String> responses = _struct.getCategoryResponses().get(ROW_LABEL_ARTS);
        assertNotNull(responses);
        assertEquals(5, responses.size());
        assertEquals("Band", responses.toArray()[0]);
        assertEquals("Chess", responses.toArray()[1]);
        assertEquals("Chorus", responses.toArray()[2]);
        assertEquals("Drawing/painting", responses.toArray()[3]);
        assertEquals("Origami", responses.toArray()[4]);

        _struct.getCategoryResponses().put(ROW_LABEL_BEFORE_AFTER_SCHOOL, new TreeSet<String>());
        _controller.processESPResults(_struct, responseMap);
        assertNotNull(_struct.getCategoryResponses());
        assertEquals(2, _struct.getCategoryResponses().size());
        responses = _struct.getCategoryResponses().get(ROW_LABEL_BEFORE_AFTER_SCHOOL);
        assertNotNull(responses);
        assertEquals(0, responses.size());

        responseMap.put("before_after_care", getEspResponseList("before", "after"));
        _controller.processESPResults(_struct, responseMap);
        assertNotNull(_struct.getCategoryResponses());
        assertEquals(2, _struct.getCategoryResponses().size());
        responses = _struct.getCategoryResponses().get(ROW_LABEL_BEFORE_AFTER_SCHOOL);
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("After-school care", responses.toArray()[0]);
        assertEquals("Before-school care", responses.toArray()[1]);

        verifyAllMocks();
    }

    public void testHandleCompareRequestNone() {
        List<ComparedSchoolBaseStruct> structs =
                new ArrayList<ComparedSchoolBaseStruct>();
        ComparedSchoolProgramsExtracurricularsStruct struct1 = new ComparedSchoolProgramsExtracurricularsStruct();
        School school1 = new School();
        school1.setDatabaseState(State.CA);
        school1.setId(1);
        school1.setName("Test School 1");
        school1.setLevelCode(LevelCode.ELEMENTARY);
        struct1.setSchool(school1);
        ComparedSchoolProgramsExtracurricularsStruct struct2 = new ComparedSchoolProgramsExtracurricularsStruct();
        School school2 = new School();
        school2.setDatabaseState(State.CA);
        school2.setId(2);
        school2.setName("Test School 2");
        school2.setLevelCode(LevelCode.ELEMENTARY_MIDDLE);
        struct2.setSchool(school2);
        structs.add(struct1);
        structs.add(struct2);

        Map<String, Object> model = new HashMap<String, Object>();

        expect(_espResponseDao.getResponses(school1)).andReturn(null);
        expect(_surveyDao.getSurveyResultsForSchool("e", school1)).andReturn(null);
        expect(_espResponseDao.getResponses(school2)).andReturn(null);
        expect(_surveyDao.getSurveyResultsForSchool("e", school2)).andReturn(null);
        expect(_surveyDao.getSurveyResultsForSchool("m", school2)).andReturn(null);
        replayAllMocks();
        _controller.handleCompareRequest(getRequest(), getResponse(), structs, model);
        verifyAllMocks();
        assertNotNull(model.get("categories"));
        assertEquals(0, ((List)model.get("categories")).size());
    }

    public void testHandleCompareRequestESPOverride() {
        // test that ESP overrides parent survey
        List<ComparedSchoolBaseStruct> structs =
                new ArrayList<ComparedSchoolBaseStruct>();
        ComparedSchoolProgramsExtracurricularsStruct struct1 = new ComparedSchoolProgramsExtracurricularsStruct();
        School school1 = new School();
        school1.setDatabaseState(State.CA);
        school1.setId(1);
        school1.setName("Test School 1");
        school1.setLevelCode(LevelCode.ELEMENTARY);
        struct1.setSchool(school1);
        ComparedSchoolProgramsExtracurricularsStruct struct2 = new ComparedSchoolProgramsExtracurricularsStruct();
        School school2 = new School();
        school2.setDatabaseState(State.CA);
        school2.setId(2);
        school2.setName("Test School 2");
        school2.setLevelCode(LevelCode.ELEMENTARY_MIDDLE);
        struct2.setSchool(school2);
        structs.add(struct1);
        structs.add(struct2);

        Map<String, Object> model = new HashMap<String, Object>();

        expect(_espResponseDao.getResponses(school1)).andReturn(getEspResponseListWithKey("arts_media", "band", "chorus", "drawing/painting"));
        // surveyDao is never checked for school1 since there is a valid pq
        expect(_espResponseDao.getResponses(school2)).andReturn(null);
        expect(_surveyDao.getSurveyResultsForSchool("e", school2)).andReturn(null);
        expect(_surveyDao.getSurveyResultsForSchool("m", school2)).andReturn(null);
        replayAllMocks();
        _controller.handleCompareRequest(getRequest(), getResponse(), structs, model);
        verifyAllMocks();
        assertNotNull(model.get("categories"));
        assertEquals(1, ((List)model.get("categories")).size());
        assertEquals(ROW_LABEL_ARTS, ((List)model.get("categories")).get(0));
        Set<String> responses = struct1.getCategoryResponses().get(ROW_LABEL_ARTS);
        assertNotNull(responses);
        assertEquals(3, responses.size());
        assertEquals("Band", responses.toArray()[0]);
        assertEquals("Chorus", responses.toArray()[1]);
        assertEquals("Drawing/painting", responses.toArray()[2]);
    }

    public void testProcessSurveyResults() {
        _struct.setCategoryResponses(new HashMap<String, Set<String>>());
        _struct.getCategoryResponses().put(ROW_LABEL_ARTS, new TreeSet<String>());
        
        List<SurveyResults> surveyResultsList = new ArrayList<SurveyResults>();
        SurveyResults surveyResults = new SurveyResults();
        surveyResultsList.add(surveyResults);
        surveyResults.setTotalResponses(1);
        List<SurveyResultPage> pages = new ArrayList<SurveyResultPage>();
        surveyResults.setPages(pages);
        SurveyResultPage page1 = new SurveyResultPage();
        pages.add(page1);
        List<SurveyResultGroup> groups1 = new ArrayList<SurveyResultGroup>();
        page1.setGroups(groups1);
        SurveyResultGroup group1 = new SurveyResultGroup();
        groups1.add(group1);
        List<SurveyResultQuestion> questions1 = new ArrayList<SurveyResultQuestion>();
        group1.setQuestions(questions1);
        SurveyResultQuestion question1 = new SurveyResultComplexQuestion();
        questions1.add(question1);
        question1.setDisplayType("COMPLEX");
        Question q1 = new Question();
        q1.setId(1);
        question1.setQuestion(q1);
        List<Answer> answers1 = new ArrayList<Answer>();
        q1.setAnswers(answers1);
        Answer a1 = new Answer();
        a1.setId(1); // Arts
        answers1.add(a1);
        List<AnswerValue> answerValues1 = new ArrayList<AnswerValue>();
        a1.setAnswerValues(answerValues1);
        AnswerValue answerValue1 = new AnswerValue();
        answerValues1.add(answerValue1);
        answerValue1.setSymbol("band");
        answerValue1.setDisplay("Band");

        List<UserResponse> userResponses = new ArrayList<UserResponse>();
        question1.setResponses(userResponses);
        UserResponse response1 = new UserResponse();
        response1.setQuestionId(1);
        response1.setAnswerId(1);
        response1.setResponseValue("band");
        userResponses.add(response1);

        replayAllMocks();
        _controller.processSurveyResults(_struct, surveyResultsList);
        verifyAllMocks();

        Set<String> responses = _struct.getCategoryResponses().get(ROW_LABEL_ARTS);
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Band", responses.toArray()[0]);

    }
}
