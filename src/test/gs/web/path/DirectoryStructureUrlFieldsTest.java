package gs.web.path;

import gs.web.BaseControllerTestCase;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Sep 25, 2008
 * Time: 4:15:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryStructureUrlFieldsTest extends BaseControllerTestCase {
    public void setUp() {

    }

    public void testConstructor() {
        
    }

    /*
     // TODO-7171 - convert this to test the new code
     public void testGetFieldsFromNewStyleCityBrowseRequest() throws Exception {
         GsMockHttpServletRequest request = getRequest();

         request.setRequestURI(null);
         boolean foundException = false;
         try {
             SchoolsController.getFieldsFromNewStyleCityBrowseRequest(request);
         } catch (IllegalArgumentException e) {
             foundException = true;
         }
         assertTrue("Expected IllegalArgumentException when request URI invalid", foundException);

         Set<SchoolType> schoolTypeSet = new HashSet<SchoolType>();

         request.setRequestURI("/california/san-francisco/schools/");
         SchoolsController.CityBrowseFields fields = SchoolsController.getFieldsFromNewStyleCityBrowseRequest(request);
         assertEquals("Expected city name 'san francisco'", "san francisco", fields.getCityName());
         assertNull("Expected school type null", fields.getSchoolType());
         schoolTypeSet.clear();
         assertEquals("Expected school type set to be empty", schoolTypeSet, fields.getSchoolTypeSet());
         assertNull("Expected level code null", fields.getLevelCode());

         request.setRequestURI("/california/cardiff_by_the_sea/schools/");
         fields = SchoolsController.getFieldsFromNewStyleCityBrowseRequest(request);
         assertEquals("Expected city name 'cardiff-by-the-sea'", "cardiff-by-the-sea", fields.getCityName());
         assertNull("Expected school type null", fields.getSchoolType());
         schoolTypeSet.clear();
         assertEquals("Expected school type set to be empty", schoolTypeSet, fields.getSchoolTypeSet());
         assertNull("Expected level code null", fields.getLevelCode());

         request.setRequestURI("/california/san-francisco/public/schools/");
         fields = SchoolsController.getFieldsFromNewStyleCityBrowseRequest(request);
         assertEquals("Expected city name 'san francisco'", "san francisco", fields.getCityName());
         assertTrue("Expected school type array to contain public",
                 Arrays.deepEquals(new String[]{SchoolType.PUBLIC.getSchoolTypeName()},
                         fields.getSchoolType()));
         schoolTypeSet.clear();
         schoolTypeSet.add(SchoolType.PUBLIC);
         assertEquals("Expected school type set to contain public", schoolTypeSet, fields.getSchoolTypeSet());
         assertNull("Expected level code null", fields.getLevelCode());

         request.setRequestURI("/california/san-francisco/public-private/schools/");
         fields = SchoolsController.getFieldsFromNewStyleCityBrowseRequest(request);
         assertEquals("Expected city name 'san francisco'", "san francisco", fields.getCityName());
         assertTrue("Expected school type array to contain public and private",
                 Arrays.deepEquals(new String[]{SchoolType.PUBLIC.getSchoolTypeName(), SchoolType.PRIVATE.getSchoolTypeName()},
                         fields.getSchoolType()));
         schoolTypeSet.clear();
         schoolTypeSet.add(SchoolType.PUBLIC);
         schoolTypeSet.add(SchoolType.PRIVATE);
         assertEquals("Expected school type set to contain public and private", schoolTypeSet, fields.getSchoolTypeSet());
         assertNull("Expected level code null", fields.getLevelCode());

         request.setRequestURI("/california/san-francisco/private-charter/elementary-schools/");
         fields = SchoolsController.getFieldsFromNewStyleCityBrowseRequest(request);
         assertEquals("Expected city name 'san francisco'", "san francisco", fields.getCityName());
         assertTrue("Expected school type array to contain private and charter",
                 Arrays.deepEquals(new String[]{SchoolType.PRIVATE.getSchoolTypeName(), SchoolType.CHARTER.getSchoolTypeName()},
                         fields.getSchoolType()));
         schoolTypeSet.clear();
         schoolTypeSet.add(SchoolType.PRIVATE);
         schoolTypeSet.add(SchoolType.CHARTER);
         assertEquals("Expected school type set to contain private and charter", schoolTypeSet, fields.getSchoolTypeSet());
         assertEquals("Expected level code elementary", LevelCode.ELEMENTARY, fields.getLevelCode());

         request.setRequestURI("/california/san-francisco/preschools/");
         fields = SchoolsController.getFieldsFromNewStyleCityBrowseRequest(request);
         assertEquals("Expected city name 'san francisco'", "san francisco", fields.getCityName());
         assertNull("Expected school type null", fields.getSchoolType());
         schoolTypeSet.clear();
         assertEquals("Expected school type set to be empty", schoolTypeSet, fields.getSchoolTypeSet());
         assertEquals("Expected level code preschools", LevelCode.PRESCHOOL, fields.getLevelCode());
     }
     */    
}
