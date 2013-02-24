package gs.web.school;

import gs.data.school.*;
import gs.data.school.district.District;
import gs.data.state.State;
import gs.data.util.ListUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = {"classpath:gs/data/dao/hibernate/applicationContext-hibernate.xml", "classpath:gs/data/applicationContext-data.xml", "classpath:applicationContext.xml", "classpath:annotated-tests.xml", "classpath:pages-servlet.xml"})
public class SchoolProfileStatsControllerIntegrationTest {

    @Autowired
    SchoolProfileStatsController _controller;

    HttpServletRequest _request;

    public School getASchool() {
        School s = new School();
        s.setId(1000000);
        s.setDatabaseState(State.CA);
        s.setName("Test school");
        return s;
    }

    public School getASchool(State state, Integer id) {
        School s = new School();
        s.setId(id);
        s.setDatabaseState(state);
        s.setName("Test school for " + state.getAbbreviation() + " - " + id);
        return s;
    }

    @Before
    public void setUp() {
        _request = new MockHttpServletRequest();
    }

    /*@Test*/
    /*public void testSerialization() {
        School school = getASchool(State.CA, 1);
        school.setType(SchoolType.PUBLIC);
        school.setGradeLevels(Grades.createGrades("9,10,11,12"));
        school.setLevelCode(LevelCode.HIGH);
        school.setDistrictId(1);
        school.setStateAbbreviation(State.CA);

        _request.setAttribute("school", school);

        Map<String,Object> model = _controller.handle(_request);
        assertFalse(model.isEmpty());
    }*/

}
