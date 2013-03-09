package gs.web.school;

import gs.data.util.ListUtils;
import gs.web.school.census.SchoolProfileStatsDisplayRowFluentInterface;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class GroupOfStudentTeacherViewRowsTest {
    @Before
    public void setUp() {
    }

    @Test
    public void testGetSchoolValueMap() throws Exception {
        GroupOfStudentTeacherViewRows rows = new GroupOfStudentTeacherViewRows(CensusGroup.Student_Ethnicity,
            ListUtils.newArrayList(
                basicDisplayRow().text("one").schoolValue("1").create(),
                basicDisplayRow().text("two").schoolValue("2").create(),
                basicDisplayRow().text("three").schoolValue("").create(),
                basicDisplayRow().text("four").schoolValue("4").create()
            )
        );

        Map<String,String> schoolValueMap = rows.getSchoolValueMap();

        assertEquals("Expect map to not include empty string value", 3, schoolValueMap.size());
        assertFalse("Expect map to not include empty string value", schoolValueMap.containsKey("three"));
        assertTrue("Expect map to include a valid item", schoolValueMap.containsKey("four"));
    }

    public static SchoolProfileStatsDisplayRowFluentInterface basicDisplayRow() {
        return SchoolProfileStatsDisplayRow.with()
            .censusDataSetId(1)
            .groupId(1l)
            .dataTypeId(1);
    }
}
