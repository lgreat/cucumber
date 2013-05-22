package gs.web.school.usp;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import gs.data.community.User;
import gs.data.school.*;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.easymock.EasyMock;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.expect;

public class UspFormHelperTest extends BaseControllerTestCase {
    UspFormHelper _helper;
    private IEspResponseDao _espResponseDao;
    private static final String BOYS_SPORTS_OTHER_TEXT = "cricket";

    public void setUp() throws Exception {
        super.setUp();
        _helper = new UspFormHelper();
        _espResponseDao = EasyMock.createStrictMock(IEspResponseDao.class);

        _helper.setEspResponseDao(_espResponseDao);
    }

    private void replayAllMocks() {
        replayMocks(_espResponseDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_espResponseDao);
    }

    private void resetAllMocks() {
        resetMocks(_espResponseDao);
    }


    public void testGetSavedResponses() {
        resetAllMocks();

        State state = State.CA;
        Integer schoolId = 1;
        School school = getSampleSchool(state, schoolId);
        User user = new User();
        List<EspResponseSource> responseSources = new ArrayList<EspResponseSource>(){{
            add(EspResponseSource.usp);
        }};
        List<Object[]> keyValuePairs = getSampleKeyValuePairs();
        Multimap<String, String> keyValueMap = getSampleKeyValueMap(keyValuePairs);

        expect(_espResponseDao.getAllUniqueResponsesForSchoolBySourceAndByUser(school, state, responseSources, null)).
                andReturn(keyValuePairs);

        replayAllMocks();
        Multimap<String, String> responseKeyValues = _helper.getSavedResponses(user, school, state, false);
        verifyAllMocks();

        assertEquals(keyValueMap, responseKeyValues);

        resetAllMocks();

        responseSources = new ArrayList<EspResponseSource>(){{
            add(EspResponseSource.osp);
            add(EspResponseSource.datateam);
        }};
        expect(_espResponseDao.getAllUniqueResponsesForSchoolBySourceAndByUser(school, state, responseSources, user.getId())).
                andReturn(keyValuePairs);

        replayAllMocks();
        responseKeyValues = _helper.getSavedResponses(user, school, state, true);
        verifyAllMocks();

        assertEquals(keyValueMap, responseKeyValues);
    }

    /**
     * Test to check the order of the form section
     */
    public void testFormFieldsBuildHelper() throws Exception {
        resetAllMocks();

        ModelMap modelMap = new ModelMap();
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        State state = State.CA;
        Integer schoolId = 1;
        School school = getSampleSchool(state, schoolId);
        User user = new User();
        user.setId(1);
        boolean isOspUser = false;
        List<EspResponseSource> responseSources = new ArrayList<EspResponseSource>(){{
            add(EspResponseSource.usp);
        }};

        expect(_espResponseDao.getAllUniqueResponsesForSchoolBySourceAndByUser(school, state, responseSources, user.getId())).
                andReturn(getSampleKeyValuePairs());

        replayAllMocks();
        _helper.formFieldsBuilderHelper(modelMap, request, response, school, state, user, isOspUser);
        verifyAllMocks();

        List<UspFormResponseStruct> uspFormResponses = (List<UspFormResponseStruct>) modelMap.get("uspFormResponses");
        assertionsForFormSectionOrder(uspFormResponses);
        UspFormResponseStruct boysSportsResponses = uspFormResponses.get(7);
        assertionsForAdminFields(boysSportsResponses, false, false, false, false, false, null);
        UspFormResponseStruct foreignLanguageResponses = uspFormResponses.get(5);
        assertionsForAdminFields(foreignLanguageResponses, false, false, false, false, false, null);

        resetAllMocks();

        modelMap = new ModelMap();
        responseSources = new ArrayList<EspResponseSource>(){{
            add(EspResponseSource.osp);
            add(EspResponseSource.datateam);
        }};
        isOspUser = true;

        expect(_espResponseDao.getAllUniqueResponsesForSchoolBySourceAndByUser(school, state, responseSources, null)).
                andReturn(getSampleAdminKeyValuePairs());

        replayAllMocks();
        _helper.formFieldsBuilderHelper(modelMap, request, response, school, state, user, isOspUser);
        verifyAllMocks();

        uspFormResponses = (List<UspFormResponseStruct>) modelMap.get("uspFormResponses");
        assertionsForFormSectionOrder(uspFormResponses);
        boysSportsResponses = uspFormResponses.get(7);
        assertionsForAdminFields(boysSportsResponses, true, true, false, true, true, BOYS_SPORTS_OTHER_TEXT);
        foreignLanguageResponses = uspFormResponses.get(5);
        assertionsForAdminFields(foreignLanguageResponses, true, true, true, true, false, null);
    }

    public void testBuildSectionResponse() {
        resetAllMocks();

        /**
         * Test for sections with subsections
         */
        UspFormHelper.SectionResponseKeys sectionResponseKeys = UspFormHelper.SectionResponseKeys.arts;
        Multimap<String, String> savedResponseKeyValues = getSampleKeyValueMap(getSampleKeyValuePairs());
        boolean isOspUser= false;

        replayAllMocks();
        UspFormResponseStruct uspFormResponseStruct = _helper.buildSectionResponse(sectionResponseKeys, savedResponseKeyValues, isOspUser);
        verifyAllMocks();

        assertionsForAdminFields(uspFormResponseStruct, false, false, false, false, false, null);
        List<UspFormResponseStruct.SectionResponse> sectionResponses = uspFormResponseStruct.getSectionResponses();
        for(UspFormResponseStruct.SectionResponse sectionResponse : sectionResponses) {
            assertNotNull(sectionResponse.getTitle());
        }

        // sections that do not have subsections have no titles for section responses
        resetAllMocks();

        sectionResponseKeys = UspFormHelper.SectionResponseKeys.foreignLanguages;

        replayAllMocks();
        uspFormResponseStruct = _helper.buildSectionResponse(sectionResponseKeys, savedResponseKeyValues, isOspUser);
        verifyAllMocks();

        assertionsForAdminFields(uspFormResponseStruct, false, false, false, false, false, null);
        sectionResponses = uspFormResponseStruct.getSectionResponses();
        for(UspFormResponseStruct.SectionResponse sectionResponse : sectionResponses) {
            assertNull(sectionResponse.getTitle());
        }

        // osp user has admin fields
        savedResponseKeyValues = getSampleKeyValueMap(getSampleAdminKeyValuePairs());
        resetAllMocks();

        sectionResponseKeys = UspFormHelper.SectionResponseKeys.arts;
        isOspUser = true;

        replayAllMocks();
        uspFormResponseStruct = _helper.buildSectionResponse(sectionResponseKeys, savedResponseKeyValues, isOspUser);
        verifyAllMocks();

        assertionsForAdminFields(uspFormResponseStruct, true, true, false, false, false, null);
        sectionResponses = uspFormResponseStruct.getSectionResponses();
        for(UspFormResponseStruct.SectionResponse sectionResponse : sectionResponses) {
            assertNotNull(sectionResponse.getTitle());
        }

        resetAllMocks();

        sectionResponseKeys = UspFormHelper.SectionResponseKeys.foreignLanguages;

        replayAllMocks();
        uspFormResponseStruct = _helper.buildSectionResponse(sectionResponseKeys, savedResponseKeyValues, isOspUser);
        verifyAllMocks();

        assertionsForAdminFields(uspFormResponseStruct, true, true, true, true, false, null);

        resetAllMocks();

        sectionResponseKeys = UspFormHelper.SectionResponseKeys.boysSports;
        isOspUser = true;

        replayAllMocks();
        uspFormResponseStruct = _helper.buildSectionResponse(sectionResponseKeys, savedResponseKeyValues, isOspUser);
        verifyAllMocks();

        assertionsForAdminFields(uspFormResponseStruct, true, true, false, true, true, BOYS_SPORTS_OTHER_TEXT);
    }

    public void assertionsForFormSectionOrder(List<UspFormResponseStruct> uspFormResponses) {
        assertEquals(uspFormResponses.size(), 9);
        /**
         * Test the order in which the questions appear
         */
        assertEquals(uspFormResponses.get(0).getFieldName(), UspFormHelper.ARTS_MUSIC_PARAM);
        assertEquals(uspFormResponses.get(1).getFieldName(), UspFormHelper.EXTENDED_CARE_PARAM);
        assertEquals(uspFormResponses.get(2).getFieldName(), UspFormHelper.GIRLS_SPORTS_PARAM);
        assertEquals(uspFormResponses.get(3).getFieldName(), UspFormHelper.STAFF_PARAM);
        assertEquals(uspFormResponses.get(4).getFieldName(), UspFormHelper.FACILITIES_PARAM);
        assertEquals(uspFormResponses.get(5).getFieldName(), UspFormHelper.FOREIGN_LANGUAGES_PARAM);
        assertEquals(uspFormResponses.get(6).getFieldName(), UspFormHelper.TRANSPORTATION_PARAM);
        assertEquals(uspFormResponses.get(7).getFieldName(), UspFormHelper.BOYS_SPORTS_PARAM);
        assertEquals(uspFormResponses.get(8).getFieldName(), UspFormHelper.PARENT_INVOLVEMENT_PARAM);
    }

    public void assertionsForAdminFields(UspFormResponseStruct formResponseStruct,
                                         boolean isSchoolAdmin,
                                         boolean hasNoneField,
                                         boolean isNoneChecked,
                                         boolean hasOtherField,
                                         boolean isOtherChecked,
                                         String otherText) {
        assertEquals(formResponseStruct.isSchoolAdmin(), isSchoolAdmin);
        assertEquals(formResponseStruct.getHasNoneField(), hasNoneField);
        assertEquals(formResponseStruct.isNoneChecked(), isNoneChecked);
        assertEquals(formResponseStruct.getHasOtherField(), hasOtherField);
        assertEquals(formResponseStruct.isOtherChecked(), isOtherChecked);
        assertEquals(formResponseStruct.getOtherTextValue(), otherText);
    }

    public List<Object[]> getSampleKeyValuePairs() {
        return new ArrayList<Object[]>(){{
            add(new Object[]{UspFormHelper.BOYS_SPORTS_RESPONSE_KEY, UspFormHelper.SPORTS_SOCCER_RESPONSE_VALUE});
            add(new Object[]{UspFormHelper.BOYS_SPORTS_RESPONSE_KEY, UspFormHelper.BOYS_SPORTS_FOOTBALL_RESPONSE_VALUE});
            add(new Object[]{UspFormHelper.GIRLS_SPORTS_RESPONSE_KEY, UspFormHelper.SPORTS_BASKETBALL_RESPONSE_VALUE});
            add(new Object[]{UspFormHelper.GIRLS_SPORTS_RESPONSE_KEY, UspFormHelper.GIRLS_SPORTS_FIELD_HOCKEY_RESPONSE_VALUE});
            add(new Object[]{UspFormHelper.ARTS_VISUAL_RESPONSE_KEY, UspFormHelper.ARTS_VISUAL_PHOTO_RESPONSE_VALUE});
        }};
    }

    public List<Object[]> getSampleAdminKeyValuePairs() {
        List<Object[]> keyValuePairs = new ArrayList<Object[]>(){{
            add(new Object[]{UspFormHelper.BOYS_SPORTS_OTHER_RESPONSE_KEY, BOYS_SPORTS_OTHER_TEXT});
            add(new Object[]{UspFormHelper.FOREIGN_LANGUAGES_RESPONSE_KEY, UspFormHelper.NONE_RESPONSE_VALUE});
        }};
        keyValuePairs.addAll(getSampleKeyValuePairs());
        return keyValuePairs;
    }

    public Multimap<String, String> getSampleKeyValueMap(List<Object[]> keyValuePairs) {
        Multimap<String, String> keyValueMap = LinkedListMultimap.create();
        for(Object[] keyValue : keyValuePairs) {
            keyValueMap.put((String) keyValue[0], (String) keyValue[1]);
        }
        return keyValueMap;
    }

    public School getSampleSchool(State state, Integer schoolId) {
        School school = new School();
        school.setId(schoolId);
        school.setDatabaseState(state);
        return school;
    }
}