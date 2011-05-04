package gs.web.school;

import gs.data.school.*;
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

    public void testShouldIndex() {
        School school = new School();
        school.setLevelCode(LevelCode.ELEMENTARY);
        school.setGradeLevels(Grades.createGrades(Grade.KINDERGARTEN, Grade.G_5));
        school.setType(SchoolType.PUBLIC);

        String noTeacherStudentData = "... Student data was not reported for this school. ... Teacher data was not reported for this school. ...";
        String noStudentData = "... Student data was not reported for this school. ...";
        String noTeacherData = "... Teacher data was not reported for this school. ...";
        String allData = "... lots of data here ...";
        String noFinanceData = "... Finance data was not reported for this school. ...";

        assertTrue("Should index non-preschool regardless of data", _controller.shouldIndex(school, noTeacherStudentData));

        school.setLevelCode(LevelCode.PRESCHOOL);
        assertFalse("Should not index preschool with no teacher or student data", _controller.shouldIndex(school, noTeacherStudentData));
        assertTrue("Should index preschool with no teacher data if it has no indication of absent student data", _controller.shouldIndex(school, noTeacherData));
        assertTrue("Should index preschool with no student data if it has no indication of absent teacher data", _controller.shouldIndex(school, noStudentData));
        assertTrue("Should index preschool with no indication of data missing", _controller.shouldIndex(school, allData));
        assertTrue("Should index preschool with unknown data missing", _controller.shouldIndex(school, noFinanceData));

        school.setLevelCode(LevelCode.ELEMENTARY);
        school.setGradeLevels(Grades.createGrades(Grade.KINDERGARTEN));
        school.setType(SchoolType.PUBLIC);
        assertTrue("Should index a public KG regardless of data", _controller.shouldIndex(school, noTeacherStudentData));

        school.setType(SchoolType.PRIVATE);
        assertFalse("Should not index a private KG with no teacher or student data", _controller.shouldIndex(school, noTeacherStudentData));
        assertTrue("Should index a private KG with no teacher data if it has no indication of absent student data", _controller.shouldIndex(school, noTeacherData));
        assertTrue("Should index a private KG with no student data if it has no indication of absent teacher data", _controller.shouldIndex(school, noStudentData));
        assertTrue("Should index a private KG with no indication of data missing", _controller.shouldIndex(school, allData));
        assertTrue("Should index  private KG with unknown data missing", _controller.shouldIndex(school, noFinanceData));

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
