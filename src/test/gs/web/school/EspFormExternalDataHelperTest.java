package gs.web.school;

import gs.data.community.User;
import gs.data.school.Grade;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.BaseControllerTestCase;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class EspFormExternalDataHelperTest extends BaseControllerTestCase {
     private EspFormExternalDataHelper _helper;

    @Override
    public void setUp() throws Exception {

        super.setUp();
        _helper = new EspFormExternalDataHelper();

    }

    private void replayAllMocks() {
        replayMocks();
    }

    private void verifyAllMocks() {
        verifyMocks();
    }

    public void testSaveExternalValueForProvisionalData(){

         Object[] values = new Object[1];
         School school = new School();
         User user = new User();
         Date date = new Date();
         boolean isProvisionalData = true;

        String key = "student_enrollment";
        values[0] = "3";
        String err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals(null,err);

        //Test that an error message is returned when enrollment is not a number.
        key = "student_enrollment";
        values[0] = "err";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals("Must be an integer.",err);


        key = "administrator_name";
        values[0] = "Mr.XYZ";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals(null,err);

        key = "administrator_email";
        values[0] = "abc@abc.com";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals(null,err);

        key = "school_url";
        values[0] = "www.abc.com";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals(null,err);

        key = "grade_levels";
        values = new String[3];
        values[0] = "6";
        values[1] = "7";
        values[2] = "8";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals(null,err);

        //Test that error message is returned when only PK grade is selected.
        key = "grade_levels";
        values = new String[1];
        values[0] = "PK";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals("You can not set preschool as your only grade.",err);

        //Test that error message is returned when no grade is selected.
        key = "grade_levels";
        values = new String[1];
        values[0] = "";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals("You must select a grade level.",err);

        key = "school_type";
        values[0] = "private";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals(null,err);

        //Test that error message is returned when invalid school type is selected.
        key = "school_type";
        values[0] = "";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals("Must select a valid school type.",err);

        key = "school_type_affiliation";
        values[0] = "religious";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals(null,err);

        key = "school_type_affiliation_other";
        values[0] = "other";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals(null,err);

        key = "coed";
        values[0] = "all_boys";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals(null,err);

        key = "address";
        Address address = new Address();
        address.setStreet("some street");
        address.setCity("some city");
        values = new Object[1];
        values[0] = address;
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals(null,err);

        key = "school_phone";
        values[0] = "1231231234";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals(null,err);

        //Test that error message is returned when phone number has invalid characters.
        key = "school_phone";
        values[0] = "<1231231234";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals("Contains invalid characters.",err);

        key = "school_fax";
        values[0] = "1231231234";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals(null,err);

        //Test that error message is returned when fax number has invalid characters.
        key = "school_fax";
        values[0] = "<1231231234";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals("Contains invalid characters.",err);

        school.setDatabaseState(State.WI);
        key = "census_ethnicity";
        Map<Integer, Integer> breakdownIdToValueMap = new HashMap<Integer, Integer>();
//        breakdownIdToValueMap.put(22,44);
        breakdownIdToValueMap.put(6,100);
        values[0] = breakdownIdToValueMap;
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals(null,err);

        //Test that error message is returned when ethnicity does not add up to 100%.
        key = "census_ethnicity";
        breakdownIdToValueMap = new HashMap<Integer, Integer>();
        breakdownIdToValueMap.put(6,4);
        values[0] = breakdownIdToValueMap;
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals("Your ethnicity percents must add up to 100%.",err);

        //Test that error message is returned when ethnicity is not a positive number.
        key = "census_ethnicity";
        breakdownIdToValueMap = new HashMap<Integer, Integer>();
        breakdownIdToValueMap.put(6,-4);
        values[0] = breakdownIdToValueMap;
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals("Please specify a positive number.",err);

        key = "school_video";
        values[0] = "http://youtube.com/";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals(null,err);

        key = "facebook_url";
        values[0] = "http://facebook.com/";
        err = _helper.saveExternalValue(key,values,school,user,date,isProvisionalData);
        assertEquals(null,err);
    }


    public void testInsertEspFormResponseStructForProvisionalAddress(){
        Map<String, EspFormResponseStruct> responseMap = new HashMap<String, EspFormResponseStruct>();

        String addressStr = null;
        _helper.insertEspFormResponseStructForProvisionalAddress(responseMap,addressStr);
        assertEquals("Empty provisional address string, hence nothing should be put in the map.",0,responseMap.size());
        assertNull("Empty provisional address string, hence nothing should be put in the map.",
                responseMap.get("physical_address_street"));

        addressStr = "something";
        _helper.insertEspFormResponseStructForProvisionalAddress(responseMap,addressStr);
        assertEquals("Invalid provisional address format, hence nothing should be put in the map.",0,responseMap.size());
        assertNull("Invalid provisional address format, hence nothing should be put in the map.",
                responseMap.get("physical_address_street"));

        addressStr = "some street 1, \n" + "san francisco, "+"california  ";
        _helper.insertEspFormResponseStructForProvisionalAddress(responseMap,addressStr);
        assertEquals("Zip code missing, hence nothing should be put in the map.",0,responseMap.size());
        assertNull("Zip code missing, hence nothing should be put in the map.",
                responseMap.get("physical_address_street"));

        addressStr = "some street 1, \n" + "san francisco, "+"94101";
        _helper.insertEspFormResponseStructForProvisionalAddress(responseMap,addressStr);
        assertEquals("State missing, hence nothing should be put in the map.",0,responseMap.size());
        assertNull("State missing, hence nothing should be put in the map.",
                responseMap.get("physical_address_street"));

        addressStr = "some street 1, \n" + "california  "+"94101";
        _helper.insertEspFormResponseStructForProvisionalAddress(responseMap,addressStr);
        assertEquals("City missing, hence nothing should be put in the map.",0,responseMap.size());
        assertNull("City missing, hence nothing should be put in the map.",
                responseMap.get("physical_address_street"));

        addressStr = "some street 1, \n" + "san francisco, "+"california  "+"94101";
        _helper.insertEspFormResponseStructForProvisionalAddress(responseMap,addressStr);
        assertEquals("Valid provisional address format.",4,responseMap.size());
        assertEquals("Valid provisional address format.","some street 1",
                responseMap.get("physical_address_street").toString());
        assertEquals("Valid provisional address format.","san francisco",
                responseMap.get("physical_address_city").toString());
        assertEquals("Valid provisional address format.",State.CA.getAbbreviation().toString(),
                responseMap.get("physical_address_state").toString());
        assertEquals("Valid provisional address format.","94101",
                responseMap.get("physical_address_zip").toString());

    }


    public void testInsertEspFormResponseStructForProvisionalPhone(){
        Map<String, EspFormResponseStruct> responseMap = new HashMap<String, EspFormResponseStruct>();

        String phoneStr = "";
        _helper.insertEspFormResponseStructForProvisionalPhone(responseMap,phoneStr);
        assertEquals("Empty provisional phone string, hence nothing should be put in the map.",0,responseMap.size());
        assertNull("Empty provisional phone string, hence nothing should be put in the map.",
                responseMap.get("school_phone_area_code"));

        phoneStr = null;
        _helper.insertEspFormResponseStructForProvisionalPhone(responseMap,phoneStr);
        assertEquals("Empty provisional phone string, hence nothing should be put in the map.",0,responseMap.size());
        assertNull("Empty provisional phone string, hence nothing should be put in the map.",
                responseMap.get("school_phone_area_code"));

        phoneStr = "1231231234";
        _helper.insertEspFormResponseStructForProvisionalPhone(responseMap,phoneStr);
        assertEquals("Invalid provisional phone format, hence nothing should be put in the map.",0,responseMap.size());
        assertNull("Invalid provisional phone format, hence nothing should be put in the map.",
                responseMap.get("school_phone_area_code"));

        phoneStr = ")1(231231234-";
        _helper.insertEspFormResponseStructForProvisionalPhone(responseMap,phoneStr);
        assertEquals("Invalid provisional phone format, hence nothing should be put in the map.",0,responseMap.size());
        assertNull("Invalid provisional phone format, hence nothing should be put in the map.",
                responseMap.get("school_phone_area_code"));

        phoneStr = "(123) 123-1234";
        _helper.insertEspFormResponseStructForProvisionalPhone(responseMap,phoneStr);
        assertEquals("Valid provisional phone format.",3,responseMap.size());
        assertEquals("Valid provisional phone format.","123",
                responseMap.get("school_phone_area_code").getValue());
        assertEquals("Valid provisional phone format.","123",
                responseMap.get("school_phone_office_code").getValue());
        assertEquals("Valid provisional phone format.","1234",
                responseMap.get("school_phone_last_four").getValue());

    }

    public void testParsePhoneAndFaxNumber(){
        String phoneStr = "";
        Map<String, String> rval = _helper.parsePhoneAndFaxNumbers(phoneStr);
        assertEquals("Empty provisional phone string, hence nothing should be put in the map.",0,rval.size());

        phoneStr = null;
        rval = _helper.parsePhoneAndFaxNumbers(phoneStr);
        assertEquals("Null provisional phone string, hence nothing should be put in the map.",0,rval.size());

        phoneStr = ")-122(31213 ";
        rval = _helper.parsePhoneAndFaxNumbers(phoneStr);
        assertEquals("Invalid provisional phone string, hence nothing should be put in the map.",0,rval.size());

        phoneStr = ")122(31213-";
        rval = _helper.parsePhoneAndFaxNumbers(phoneStr);
        assertEquals("Invalid provisional phone string, hence nothing should be put in the map.",0,rval.size());

        phoneStr = "(1234) 4123-12346";
        rval = _helper.parsePhoneAndFaxNumbers(phoneStr);
        assertEquals("Invalid provisional phone string, hence nothing should be put in the map.",0,rval.size());

        phoneStr = "(123) 123-1234";
        rval = _helper.parsePhoneAndFaxNumbers(phoneStr);
        assertEquals("Valid provisional phone string.",3,rval.size());
        assertEquals("Valid provisional phone string.","123",rval.get("areaCode"));
        assertEquals("Valid provisional phone string.","123",rval.get("officeCode"));
        assertEquals("Valid provisional phone string.","1234",rval.get("lastFour"));
    }


}