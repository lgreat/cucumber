package gs.web.school;

import gs.data.school.EspResponse;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: rraker
 * Date: 3/7/13
 */
public class EspFormControllerTest extends BaseControllerTestCase {

    EspFormController _espFormController;

    public void setUp() throws Exception {
        super.setUp();
        _espFormController = new EspFormController();
    }

    public void testAfterSchoolIndicatorDistrict1() {
        ModelMap modelMap = new ModelMap();
        School school = new School();
        school.setId(1000000);
        school.setDatabaseState(State.CA);
        school.setStateId("CA");
        school.setName("Chabot");
        school.setCity("oakland");
        school.setDistrictId(14);
        school.setNewProfileSchool(2);

//        List<EspResponse> l = new ArrayList<EspResponse>();
//        l.add(createEspResponse("academic_award_1", "Award 1"));
//        Map<String, List<EspResponse>> espData = convertToEspData(l);
//        modelMap.put()
        _espFormController.putAfterSchoolIndicatorInModel(school, modelMap);

        String result = (String)modelMap.get("after_school_qualified");
        assertNotNull(result);
        assertEquals("yes", result);
    }

    public void testAfterSchoolIndicatorDistrict2() {
        ModelMap modelMap = new ModelMap();
        School school = new School();
        school.setId(1000000);
        school.setDatabaseState(State.CA);
        school.setStateId("CA");
        school.setName("Chabot");
        school.setCity("oakland");
        school.setDistrictId(99);
        school.setNewProfileSchool(2);

//        List<EspResponse> l = new ArrayList<EspResponse>();
//        l.add(createEspResponse("academic_award_1", "Award 1"));
//        Map<String, List<EspResponse>> espData = convertToEspData(l);
//        modelMap.put()
        _espFormController.putAfterSchoolIndicatorInModel(school, modelMap);

        String result = (String)modelMap.get("after_school_qualified");
        assertNull(result);
    }

    public void testAfterSchoolIndicatorCity1() {
        ModelMap modelMap = new ModelMap();
        School school = new School();
        school.setId(1000000);
        school.setDatabaseState(State.CA);
        school.setStateId("CA");
        school.setName("Chabot");
        school.setCity("oakland");
        school.setDistrictId(999);
        school.setNewProfileSchool(2);

        _espFormController.putAfterSchoolIndicatorInModel(school, modelMap);

        String result = (String)modelMap.get("after_school_qualified");
        assertNull(result);
    }

    public void testAfterSchoolIndicatorCity2() {
        ModelMap modelMap = new ModelMap();
        School school = new School();
        school.setId(1000000);
        school.setDatabaseState(State.CA);
        school.setStateId("CA");
        school.setName("Chabot");
        school.setCity("Oakland");
        school.setDistrictId(999);
        school.setNewProfileSchool(2);

        _espFormController.putAfterSchoolIndicatorInModel(school, modelMap);

        String result = (String)modelMap.get("after_school_qualified");
        assertNotNull(result);
        assertEquals("yes", result);
    }

    public void testRepeatingFormIndicator1() {
        ModelMap modelMap = new ModelMap();

        Map<String, EspFormResponseStruct> responseMap = new HashMap<String, EspFormResponseStruct>();
        EspFormResponseStruct r1 = new EspFormResponseStruct();
        r1.addValue("x");
        responseMap.put("after_school_name_1", r1);
        modelMap.put("responseMap", responseMap);

        String prefix = "after_school_";

        _espFormController.putRepeatingFormIndicatorInModel(modelMap, prefix, 5);

        Integer result = (Integer)modelMap.get(prefix);
        assertNotNull(result);
        assertEquals("ResponseMap includes only set 1", new Integer(1), result);
    }

    public void testRepeatingFormIndicator2() {
        ModelMap modelMap = new ModelMap();

        Map<String, EspFormResponseStruct> responseMap = new HashMap<String, EspFormResponseStruct>();
        EspFormResponseStruct r1 = new EspFormResponseStruct();
        modelMap.put("responseMap", responseMap);

        String prefix = "after_school_";

        _espFormController.putRepeatingFormIndicatorInModel(modelMap, prefix, 5);

        Integer result = (Integer)modelMap.get(prefix);
        assertNotNull(result);
        assertEquals("If there are no entries for this prefix should get 1 back", new Integer(1), result);
    }

    public void testRepeatingFormIndicator3() {
        ModelMap modelMap = new ModelMap();

        Map<String, EspFormResponseStruct> responseMap = new HashMap<String, EspFormResponseStruct>();
        EspFormResponseStruct r1 = new EspFormResponseStruct();
        r1.addValue("x");
        responseMap.put("after_school_name_4", r1);
        modelMap.put("responseMap", responseMap);

        String prefix = "after_school_";

        _espFormController.putRepeatingFormIndicatorInModel(modelMap, prefix, 5);

        Integer result = (Integer)modelMap.get(prefix);
        assertNotNull(result);
        assertEquals("ResponseMap includes only set 4", new Integer(4), result);
    }

    public void testRepeatingFormIndicator4() {
        ModelMap modelMap = new ModelMap();

        Map<String, EspFormResponseStruct> responseMap = new HashMap<String, EspFormResponseStruct>();
        EspFormResponseStruct r1 = new EspFormResponseStruct();
        r1.addValue("x");
        responseMap.put("after_school_name_6", r1);
        modelMap.put("responseMap", responseMap);

        String prefix = "after_school_";

        _espFormController.putRepeatingFormIndicatorInModel(modelMap, prefix, 5);

        Integer result = (Integer)modelMap.get(prefix);
        assertNotNull(result);
        assertEquals("Max datasets is 5, expect the default of 1 to be returned", new Integer(1), result);
    }

    // ========================== Utility methods ===============================
    private EspResponse createEspResponse( String key, String value ) {
        EspResponse response = new EspResponse();
        response.setActive( true );
        response.setKey( key );
        response.setValue( value );
        response.setPrettyValue( createPrettyValue(value) );
        return response;
    }

    private Map<String,List<EspResponse>> convertToEspData(List<EspResponse> l) {
        return EspResponse.rollup(l);
    }

    // Create a pretty value by capitalizing thr first character and removing underscores
    private String createPrettyValue( String value ) {
        StringBuilder sb = new StringBuilder();

        sb.append( Character.toUpperCase( value.charAt(0) ) );
        for( int i = 1; i < value.length(); i++ ) {
            char c = value.charAt(i);
            if( c == '_' ) {
                sb.append( ' ' );
            }
            else {
                sb.append( c );
            }
        }

        return sb.toString();
    }
}
