package gs.web.school.usp;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.json.JSONArray;
import gs.data.json.JSONException;
import gs.data.json.JSONObject;
import gs.data.school.*;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.codehaus.jackson.map.ObjectMapper;
import org.easymock.EasyMock;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.HashMap;
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

    public void testJsonFormFieldsHelper() throws JSONException {
        User user = new User();
        user.setId(1);
        user.setEmail("asdfgh@gs.org");
        UserProfile userProfile = new UserProfile();
        userProfile.setScreenName("asdfgh");
        user.setUserProfile(userProfile);

        List<UspFormResponseStruct> uspFormResponses = getSampleFormResponses();

        JSONObject responseJson = _helper.jsonFormFieldsBuilderHelper(uspFormResponses, null, false);

        JSONArray formFields = responseJson.getJSONArray(UspFormHelper.FORM_FIELDS_JSON_RESPONSE_KEY);

        assertEquals(formFields.length(), 2); // we are passing in only 2 fields for test

        JSONObject artsMusicField = (JSONObject) formFields.get(0);
        assertEquals(artsMusicField.get(UspFormHelper.FIELD_NAME_JSON_RESPONSE_KEY), UspFormHelper.ARTS_MUSIC_PARAM);

        JSONArray artsMusicResponses = artsMusicField.getJSONArray(UspFormHelper.RESPONSES_JSON_RESPONSE_KEY);
        JSONObject response = (JSONObject) artsMusicResponses.get(0);
        JSONArray responseValues = response.getJSONArray(UspFormHelper.VALUES_JSON_RESPONSE_KEY);
        assertEquals(responseValues.get(0).toString(), "{\"label\":\"Computer animation\",\"isSelected\":true,\"responseValue\":\"animation\"}");
        assertEquals(responseValues.get(1).toString(), "{\"label\":\"Graphics\",\"isSelected\":false,\"responseValue\":\"graphics\"}");
        assertEquals(artsMusicResponses.get(1).toString(), "{\"key\":\"arts_music\",\"title\":\"Music\",\"values\":[{\"label\":\"Band\",\"isSelected\":true,\"responseValue\":\"band\"}," +
                "{\"label\":\"Bell / Handbell choir\",\"isSelected\":false,\"responseValue\":\"bells\"}]}");

        JSONObject extendedCareField = (JSONObject) formFields.get(1);
        assertEquals(extendedCareField.get(UspFormHelper.FIELD_NAME_JSON_RESPONSE_KEY), UspFormHelper.EXTENDED_CARE_PARAM);
        assertEquals(extendedCareField.toString(), "{\"responses\":[{\"key\":\"before\",\"values\":[{\"isSelected\":true,\"responseValue\":\"after\"}]" +
                "}],\"title\":\"Extended care\",\"ghostText\":\"What before/after school care does " +
                "this school offer?\",\"fieldName\":\"extCare\"}");

        responseJson = _helper.jsonFormFieldsBuilderHelper(uspFormResponses, user, false);
        JSONObject userDetails = responseJson.getJSONObject(UspFormHelper.USER_JSON_RESPONSE_KEY);
        assertEquals(userDetails.toString(), "{\"numberMSLItems\":0,\"screenName\":\"asdfgh\",\"email\":\"asdfgh@gs.org\",\"id\":1}");
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

    public List<UspFormResponseStruct> getSampleFormResponses() {
        List<UspFormResponseStruct> uspFormResponses = new ArrayList<UspFormResponseStruct>();

        String fieldName = UspFormHelper.ARTS_MUSIC_PARAM;
        UspFormResponseStruct uspFormResponseStruct = new UspFormResponseStruct(fieldName, UspFormHelper.ARTS_MUSIC_TITLE);
        uspFormResponseStruct.setGhostText(UspFormHelper.FORM_FIELD_GHOST_TEXT.get(fieldName));
        List<UspFormResponseStruct.SectionResponse> sectionResponses = uspFormResponseStruct.getSectionResponses();

        String responseKey = UspFormHelper.ARTS_MEDIA_RESPONSE_KEY;
        UspFormResponseStruct.SectionResponse sectionResponse = uspFormResponseStruct.new SectionResponse(responseKey);
        sectionResponse.setTitle(UspFormHelper.RESPONSE_KEY_SUB_SECTION_LABEL.get(responseKey));
        List<UspFormResponseStruct.SectionResponse.UspResponseValueStruct> uspResponseValues =
                sectionResponse.getResponses();

        String responseValue = UspFormHelper.ARTS_MEDIA_ANIMATION_RESPONSE_VALUE;
        UspFormResponseStruct.SectionResponse.UspResponseValueStruct uspResponseValue =
                sectionResponse.new UspResponseValueStruct(responseValue);
        uspResponseValue.setLabel(UspFormHelper.RESPONSE_VALUE_LABEL.get(responseKey + UspFormHelper.DOUBLE_UNDERSCORE_SEPARATOR
                + responseValue));
        uspResponseValue.setIsSelected(true);
        uspResponseValues.add(uspResponseValue);

        responseValue = UspFormHelper.ARTS_MEDIA_GRAPHICS_RESPONSE_VALUE;
        uspResponseValue = sectionResponse.new UspResponseValueStruct(responseValue);
        uspResponseValue.setLabel(UspFormHelper.RESPONSE_VALUE_LABEL.get(responseKey + UspFormHelper.DOUBLE_UNDERSCORE_SEPARATOR
                + responseValue));
        uspResponseValues.add(uspResponseValue);

//        sectionResponse.setResponses(uspResponseValues);
        sectionResponses.add(sectionResponse);

        responseKey = UspFormHelper.ARTS_MUSIC_RESPONSE_KEY;
        sectionResponse = uspFormResponseStruct.new SectionResponse(responseKey);
        sectionResponse.setTitle(UspFormHelper.RESPONSE_KEY_SUB_SECTION_LABEL.get(responseKey));
        uspResponseValues = sectionResponse.getResponses();

        responseValue = UspFormHelper.ARTS_MUSIC_BAND_RESPONSE_VALUE;
        uspResponseValue = sectionResponse.new UspResponseValueStruct(responseValue);
        uspResponseValue.setLabel(UspFormHelper.RESPONSE_VALUE_LABEL.get(responseKey + UspFormHelper.DOUBLE_UNDERSCORE_SEPARATOR
                + responseValue));
        uspResponseValue.setIsSelected(true);
        uspResponseValues.add(uspResponseValue);

        responseValue = UspFormHelper.ARTS_MUSIC_BELLS_RESPONSE_VALUE;
        uspResponseValue = sectionResponse.new UspResponseValueStruct(responseValue);
        uspResponseValue.setLabel(UspFormHelper.RESPONSE_VALUE_LABEL.get(responseKey + UspFormHelper.DOUBLE_UNDERSCORE_SEPARATOR
                + responseValue));
        uspResponseValues.add(uspResponseValue);

//        sectionResponse.setResponses(uspResponseValues);
        sectionResponses.add(sectionResponse);

//        uspFormResponseStruct.setSectionResponses(sectionResponses);
        uspFormResponses.add(uspFormResponseStruct);

        fieldName = UspFormHelper.EXTENDED_CARE_PARAM;
        uspFormResponseStruct = new UspFormResponseStruct(fieldName, UspFormHelper.EXTENDED_CARE_TITLE);
        uspFormResponseStruct.setGhostText(UspFormHelper.FORM_FIELD_GHOST_TEXT.get(fieldName));
        sectionResponses = uspFormResponseStruct.getSectionResponses();

        responseKey = UspFormHelper.EXTENDED_CARE_BEFORE_RESPONSE_VALUE;
        sectionResponse = uspFormResponseStruct.new SectionResponse(responseKey);
        sectionResponse.setTitle(UspFormHelper.RESPONSE_KEY_SUB_SECTION_LABEL.get(responseKey));
        uspResponseValues = sectionResponse.getResponses();

        responseValue = UspFormHelper.EXTENDED_CARE_AFTER_RESPONSE_VALUE;
        uspResponseValue = sectionResponse.new UspResponseValueStruct(responseValue);
        uspResponseValue.setLabel(UspFormHelper.RESPONSE_VALUE_LABEL.get(responseKey + UspFormHelper.DOUBLE_UNDERSCORE_SEPARATOR
                + responseValue));
        uspResponseValue.setIsSelected(true);
        uspResponseValues.add(uspResponseValue);

//        sectionResponse.setResponses(uspResponseValues);
        sectionResponses.add(sectionResponse);

//        uspFormResponseStruct.setSectionResponses(sectionResponses);
        uspFormResponses.add(uspFormResponseStruct);

        return uspFormResponses;
    }
}