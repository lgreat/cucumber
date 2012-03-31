package gs.web.search;


import gs.data.school.district.District;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.GsMockHttpServletRequest;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;

public class DistrictBrowseHelperTest {


    @Test
    public void testGetRelCanonicalForDistrictBrowse() {
        HttpServletRequest request = new GsMockHttpServletRequest();

        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(request);
        request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);
        SchoolSearchCommand schoolSearchCommand = new SchoolSearchCommand();
        SchoolSearchCommandWithFields cmd = new SchoolSearchCommandWithFields(schoolSearchCommand, fields);

        Address address = new Address();
        address.setCity("Alameda");

        District district = new District();
        district.setId(0);
        district.setStateId(State.CA.getAbbreviationLowerCase());
        district.setDatabaseState(State.CA);
        district.setName("Alameda City Unified School District");
        district.setPhysicalAddress(address);

        cmd.setDistrict(district);

        String result = new DistrictBrowseHelper(cmd).getRelCanonical(request);


        assertEquals(result, "http://localhost/california/alameda/Alameda-City-Unified-School-District/schools/");
    }
}
