package gs.web.school;

import gs.data.school.School;
import gs.data.school.SchoolSubtype;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;

import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class KindercareLeadGenHelperTest extends BaseControllerTestCase {
    private Map<String, Object> _model;
    private School _school;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _model = new HashMap<String, Object>();

        _school = new School();
        _school.setId(1);
        _school.setDatabaseState(State.CA);
        _school.setPreschoolSubtype(SchoolSubtype.create("kindercare"));
    }

    private void resetModel() {
        _model.clear();
    }

    public void testHelper() {
        KindercareLeadGenHelper.checkForKindercare(getRequest(), getResponse(), _school, _model);
        assertEquals(2, _model.size());

        resetModel();

        Cookie cookie = new Cookie("kindercare", "");
        cookie.setValue("CA2$$:$$:1");
        getRequest().setCookies(new Cookie[] {cookie});
        KindercareLeadGenHelper.checkForKindercare(getRequest(), getResponse(), _school, _model);
        assertEquals(2, _model.size());

        resetModel();

        cookie.setValue("CA1$$:$$1");
        getRequest().setCookies(new Cookie[] {cookie});
        KindercareLeadGenHelper.checkForKindercare(getRequest(), getResponse(), _school, _model);
        assertEquals(0, _model.size());

        resetModel();

        cookie.setValue("CA2$$:$$:1$$/$$CA1$$:$$1");
        getRequest().setCookies(new Cookie[] {cookie});
        KindercareLeadGenHelper.checkForKindercare(getRequest(), getResponse(), _school, _model);
        assertEquals(0, _model.size());
    }
}
