package gs.web.school;


import gs.data.school.School;
import gs.data.school.SchoolDaoHibernate;
import gs.data.state.State;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public abstract class AbstractSchoolProfileController {

    @Autowired
    private SchoolDaoHibernate _schoolDaoHibernate;

    public School getSchool(HttpServletRequest request, State state, Integer schoolId) {

        School school = (School) request.getAttribute("school");

        if (school == null) {
            school = _schoolDaoHibernate.getSchoolById(state, schoolId);
        }

        return school;
    }
}
