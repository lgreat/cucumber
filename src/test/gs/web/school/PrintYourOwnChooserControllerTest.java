package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.easymock.EasyMock.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = {"classpath:gs/data/dao/hibernate/applicationContext-hibernate.xml", "classpath:gs/data/applicationContext-data.xml", "classpath:applicationContext.xml", "classpath:annotated-tests.xml"})
public class PrintYourOwnChooserControllerTest {

    @Autowired
    PrintYourOwnChooserController _pdfController;

    ISchoolDao _schoolDaoHibernate;

    @Before
    public void setUp() {

        _schoolDaoHibernate = createStrictMock(ISchoolDao.class);

        ReflectionTestUtils.setField(_pdfController, "_schoolDaoHibernate", _schoolDaoHibernate);
    }


    @Test
    public void testGetSchoolsFromParams() throws Exception {

        String states[] = {"CA", "DC", "AK"};
        Integer[] ids = {1,2,3};

        expect(_schoolDaoHibernate.getSchoolById(eq(State.CA), eq(1))).andReturn(new School());
        expect(_schoolDaoHibernate.getSchoolById(eq(State.DC), eq(2))).andReturn(new School());
        expect(_schoolDaoHibernate.getSchoolById(eq(State.AK), eq(3))).andReturn(new School());

        replay(_schoolDaoHibernate);

        List<School> schools = _pdfController.getSchoolsFromParams(states, ids);
        assertEquals("Expect number of schools returned to equal to number of IDs provided", 3, schools.size());

        verify(_schoolDaoHibernate);
    }

    @Test
    public void testGetSchoolsFromParams_onlyOneState() throws Exception {

        String states[] = {"CA"};
        Integer[] ids = {1,2,3};

        expect(_schoolDaoHibernate.getSchoolById(eq(State.CA), eq(1))).andReturn(new School());
        expect(_schoolDaoHibernate.getSchoolById(eq(State.CA), eq(2))).andReturn(new School());
        expect(_schoolDaoHibernate.getSchoolById(eq(State.CA), eq(3))).andReturn(new School());

        replay(_schoolDaoHibernate);

        List<School> schools = _pdfController.getSchoolsFromParams(states, ids);
        assertEquals("Expect number of schools returned to equal to number of IDs provided", 3, schools.size());

        verify(_schoolDaoHibernate);
    }

    @Test
    public void testException_nullInputs() throws Exception {

        String states[] = null;
        Integer[] ids = {1,2,3};

        try {
            List<School> schools = _pdfController.getSchoolsFromParams(states, ids);
            fail("Expect exception when multiple states were provided, but number of states != number of IDs");
        } catch (IllegalArgumentException e) {
            // pass
        }
    }

    @Test
    public void testException_emptyInputs() throws Exception {

        String states[] = {};
        Integer[] ids = {};

        try {
            List<School> schools = _pdfController.getSchoolsFromParams(states, ids);
            fail("Expect exception when multiple states were provided, but number of states != number of IDs");
        } catch (IllegalArgumentException e) {
            // pass
        }
    }

    @Test
    public void testException_stateAndIDsInequal_moreThanOneState() throws Exception {

        String states[] = {"CA", "AK"};
        Integer[] ids = {1,2,3};

        try {
            List<School> schools = _pdfController.getSchoolsFromParams(states, ids);
            fail("Expect exception when multiple states were provided, but number of states != number of IDs");
        } catch (IllegalArgumentException e) {
            // pass
        }
    }
}
