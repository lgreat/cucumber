package gs.web.path;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.data.school.SchoolType;
import gs.data.school.LevelCode;

import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Young Fan
 */
public class DirectoryStructureUrlFieldsTest extends BaseControllerTestCase {

    public void testConstructor() throws Exception {
        GsMockHttpServletRequest request = getRequest();

        request.setRequestURI(null);

        Set<SchoolType> schoolTypeSet = new HashSet<SchoolType>();

        request.setRequestURI("/california/san-francisco/schools/");
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(request);
        assertEquals("Expected city name 'san francisco'", "san francisco", fields.getCityName());
        assertNull("Expected school type null", fields.getSchoolTypesParams());
        schoolTypeSet.clear();
        assertEquals("Expected school type set to be empty", schoolTypeSet, fields.getSchoolTypes());
        assertNull("Expected level code null", fields.getLevelCode());

        request.setRequestURI("/california/cardiff_by_the_sea/schools/");
        fields = new DirectoryStructureUrlFields(request);
        assertEquals("Expected city name 'cardiff-by-the-sea'", "cardiff-by-the-sea", fields.getCityName());
        assertNull("Expected school type null", fields.getSchoolTypesParams());
        schoolTypeSet.clear();
        assertEquals("Expected school type set to be empty", schoolTypeSet, fields.getSchoolTypes());
        assertNull("Expected level code null", fields.getLevelCode());

        request.setRequestURI("/california/san-francisco/public/schools/");
        fields = new DirectoryStructureUrlFields(request);
        assertEquals("Expected city name 'san francisco'", "san francisco", fields.getCityName());
        assertTrue("Expected school type array to contain public",
                Arrays.deepEquals(new String[]{SchoolType.PUBLIC.getSchoolTypeName()},
                        fields.getSchoolTypesParams()));
        schoolTypeSet.clear();
        schoolTypeSet.add(SchoolType.PUBLIC);
        assertEquals("Expected school type set to contain public", schoolTypeSet, fields.getSchoolTypes());
        assertNull("Expected level code null", fields.getLevelCode());

        request.setRequestURI("/california/san-francisco/public-private/schools/");
        fields = new DirectoryStructureUrlFields(request);
        assertEquals("Expected city name 'san francisco'", "san francisco", fields.getCityName());
        assertTrue("Expected school type array to contain public and private",
                Arrays.deepEquals(new String[]{SchoolType.PUBLIC.getSchoolTypeName(), SchoolType.PRIVATE.getSchoolTypeName()},
                        fields.getSchoolTypesParams()));
        schoolTypeSet.clear();
        schoolTypeSet.add(SchoolType.PUBLIC);
        schoolTypeSet.add(SchoolType.PRIVATE);
        assertEquals("Expected school type set to contain public and private", schoolTypeSet, fields.getSchoolTypes());
        assertNull("Expected level code null", fields.getLevelCode());

        request.setRequestURI("/california/san-francisco/private-charter/elementary-schools/");
        fields = new DirectoryStructureUrlFields(request);
        assertEquals("Expected city name 'san francisco'", "san francisco", fields.getCityName());
        assertTrue("Expected school type array to contain private and charter",
                Arrays.deepEquals(new String[]{SchoolType.PRIVATE.getSchoolTypeName(), SchoolType.CHARTER.getSchoolTypeName()},
                        fields.getSchoolTypesParams()));
        schoolTypeSet.clear();
        schoolTypeSet.add(SchoolType.PRIVATE);
        schoolTypeSet.add(SchoolType.CHARTER);
        assertEquals("Expected school type set to contain private and charter", schoolTypeSet, fields.getSchoolTypes());
        assertEquals("Expected level code elementary", LevelCode.ELEMENTARY, fields.getLevelCode());

        request.setRequestURI("/california/san-francisco/preschools/");
        fields = new DirectoryStructureUrlFields(request);
        assertEquals("Expected city name 'san francisco'", "san francisco", fields.getCityName());
        assertNull("Expected school type null", fields.getSchoolTypesParams());
        schoolTypeSet.clear();
        assertEquals("Expected school type set to be empty", schoolTypeSet, fields.getSchoolTypes());
        assertEquals("Expected level code preschools", LevelCode.PRESCHOOL, fields.getLevelCode());
    }
}
