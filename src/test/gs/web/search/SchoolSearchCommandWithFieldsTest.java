package gs.web.search;

import gs.data.search.FieldSort;
import gs.web.path.DirectoryStructureUrlFields;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class SchoolSearchCommandWithFieldsTest {
    @Test
    public void testGetFieldSort() throws Exception {
        DirectoryStructureUrlFields fields;
        SchoolSearchCommand command;
        SchoolSearchCommandWithFields commandWithFields;

        fields = new DirectoryStructureUrlFields(new MockHttpServletRequest());
        command = new SchoolSearchCommand();
        command.setSortBy("DISTANCE");
        commandWithFields = new SchoolSearchCommandWithFields(command, fields);
        assertEquals("Expect to receive distance sort", FieldSort.DISTANCE, commandWithFields.getFieldSort());
        assertEquals("Expect sortBy to still be DISTANCE", "DISTANCE", command.getSortBy());

        fields = new DirectoryStructureUrlFields(new MockHttpServletRequest());
        command = new SchoolSearchCommand();
        command.setSortBy("JUNK");
        commandWithFields = new SchoolSearchCommandWithFields(command, fields);
        assertNull("Expect to receive null sort", commandWithFields.getFieldSort());
        assertNull("Expect sortBy to have been nulled-out by getFieldSort() method since sortBy wasnt valid enum", command.getSortBy());
    }
}
