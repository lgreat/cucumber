package gs.web.school;

import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class TeachersStudentsControllerTest extends BaseControllerTestCase {
    private TeachersStudentsController _controller;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new TeachersStudentsController();

        _controller.setPrivateSchoolContentPath("/$STATE/private/$ID");
        _controller.setPublicSchoolContentPath("/$STATE/public/$ID");
    }

    public void testGetAbsoluteHrefPrivate() throws Exception {
        getRequest().setServerName("dev.greatschools.org");
        School school = new School();
        school.setType(SchoolType.PRIVATE);
        school.setId(1);
        school.setDatabaseState(State.CA);
        String href = _controller.getAbsoluteHref(school, getRequest());
        assertEquals("http://dev.greatschools.org/ca/private/1", href);
    }
    
    public void testGetAbsoluteHrefPublic() throws Exception {
        getRequest().setServerName("dev.greatschools.org");
        School school = new School();
        school.setType(SchoolType.PUBLIC);
        school.setId(1);
        school.setDatabaseState(State.CA);
        String href = _controller.getAbsoluteHref(school, getRequest());
        assertEquals("http://dev.greatschools.org/ca/public/1", href);
    }
}
