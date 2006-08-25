/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SubscriptionSummaryTest.java,v 1.11 2006/08/25 23:32:43 aroy Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.data.geo.ICity;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.BaseControllerTestCase;
import gs.web.util.validator.EmailValidator;
import gs.web.util.validator.SchoolIdValidator;
import gs.web.util.validator.StateValidator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.validation.BindException;

import java.util.*;

/**
 * Tests SubscriptionSummaryController.
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class SubscriptionSummaryTest extends BaseControllerTestCase {
    private SubscriptionSummaryController _controller;
    private static final String EMAIL = "someemail@greatschools.net";

    protected void setUp() throws Exception {
        super.setUp();

        _controller = new SubscriptionSummaryController();
        _controller.setApplicationContext(getApplicationContext());
        _controller.setCommandClass(NewsletterCommand.class);
        _controller.setCommandName("newsCmd");
        _controller.setFormView("/community/newsletters/popup/mss/page3");
        _controller.setSuccessView("/community/newsletters/popup/mss/page3");
        _controller.setSchoolDao(new MockSchoolDao());
        _controller.setUserDao(new MockUserDao());

        List onLoadValidators = new ArrayList();
        onLoadValidators.add(new EmailValidator());
        onLoadValidators.add(new StateValidator());
        onLoadValidators.add(new SchoolIdValidator());
        _controller.setOnLoadValidators(onLoadValidators);
    }

    protected void tearDown() throws Exception {
        super.tearDown();


    }

    public void testNoInputOnBindOnNewForm() {
        NewsletterCommand command = new NewsletterCommand();
        BindException errors = new BindException(command, "");
        _controller.onBindOnNewForm(getRequest(), command, errors);

        //not passing in request parameters..should get errors
        assertTrue(errors.hasErrors());

    }

    public void testGoodInputOnBindOnNewForm() {
        NewsletterCommand command = new NewsletterCommand();
        BindException errors = new BindException(command, "");

        command.setSchoolId(1);
        command.setState(State.CA);
        command.setEmail(EMAIL);

        _controller.onBindOnNewForm(getRequest(), command, errors);
        assertFalse(errors.hasErrors());
    }

    public void testReferenceData() {
        NewsletterCommand command = new NewsletterCommand();
        command.setEmail(EMAIL);
        command.setState(State.CA);
        command.setSchoolId(1);

        BindException errors = new BindException(command, "");
        Map model = _controller.referenceData(getRequest(), command, errors);
        assertFalse(errors.hasErrors());

        Set myMsHs = (Set) model.get(SubscriptionSummaryController.MODEL_SET_MS_HS);
        assertNotNull(myMsHs);
        assertEquals(2, myMsHs.size());

        Set myNthGrader = (Set) model.get(SubscriptionSummaryController.MODEL_SET_NTH_GRADER);
        assertNotNull(myNthGrader);
        _log.debug(myNthGrader);
        assertTrue(5 <= myNthGrader.size());

        assertNotNull(model.get(SubscriptionSummaryController.MODEL_PARENT_ADVISOR));
        assertNotNull(model.get(SubscriptionSummaryController.MODEL_SCHOOL_NAME));
        assertEquals(MockSchoolDao.SCHOOL_NAME, model.get(SubscriptionSummaryController.MODEL_SCHOOL_NAME).toString());
        assertEquals(command.getEmail(), model.get(SubscriptionSummaryController.MODEL_EMAIL).toString());
    }

    private static class MockUserDao implements IUserDao {

        public void evict(User user) {
            
        }

        public User findUserFromEmail(String email) throws ObjectRetrievalFailureException {
            Set subscriptions = new HashSet();

            for (Iterator iter = SubscriptionProduct.getNewsletterProducts().iterator(); iter.hasNext();) {
                Subscription sub = new Subscription();

                SubscriptionProduct product = (SubscriptionProduct) iter.next();
                sub.setProduct(product);
                if (product == SubscriptionProduct.MYSTAT) {
                    sub.setSchoolId(MockSchoolDao.SCHOOL_ID.intValue());
                }
                sub.setState(State.CA);
                subscriptions.add(sub);
            }

            User user = new User();
            user.setSubscriptions(subscriptions);
            user.setEmail(email);
            return user;
        }

        public User findUserFromEmailIfExists(String email) {
            return findUserFromEmail(email);
        }

        public User findUserFromId(int i) throws ObjectRetrievalFailureException {
            return null;
        }

        public void saveUser(User user) throws DataIntegrityViolationException {

        }

        public void updateUser(User user) throws DataIntegrityViolationException {

        }

        public void removeUser(Integer integer) {

        }

        public List findUsersModifiedSince(Date date) {
            return null;
        }

        public List findUsersModifiedBetween(Date begin, Date end) {
            return null;
        }

        public User findUserFromScreenNameIfExists(String screenName) {
            return null;
        }
    }

    private static class MockSchoolDao implements ISchoolDao {
        public static final String SCHOOL_NAME = "School's Name";
        public static final Integer SCHOOL_ID = Integer.valueOf("1");

        public List getPublishedSchools(State state) {
            return null;
        }

        public List getActiveSchools(State state) {
            return null;
        }

        public List getSchoolsInDistrict(State state, Integer integer, boolean b) {
            return null;
        }

        public School getSchoolById(State state, Integer schoolId) {
            School s = new School();
            s.setName(SCHOOL_NAME);
            return s;
        }

        public School getSchoolByStateId(State state, String string) {
            return null;
        }

        public School findSchool(State state, String string, String string1, District district, boolean b, String string2, Address address) throws ObjectRetrievalFailureException {
            return null;
        }

        public void saveSchool(State state, School school, String string) {

        }

        public void removeSchoolById(State state, Integer integer) {

        }

        public Map getStateIdMap(State state) {
            return null;
        }

        public Map getNcesIdMap(State state) {
            return null;
        }

        public Set getSchoolUniqueIds(State state, boolean b) {
            return null;
        }

        public Set getSchoolIds(State state, boolean b) {
            return null;
        }

        public List findSchoolsInCity(State state, String string, boolean b) {
            return null;
        }

        public List findSchoolsInCity(State state, String string, int i) {
            return null;
        }

        public List findSchoolsInCounty(State state, String string, boolean b) {
            return null;
        }

        public School getSampleSchool(State state) {
            return null;
        }

        public int countSchools(State state, SchoolType schoolType, LevelCode levelCode, String string) {
            return 0;
        }

        public int countSchoolsInDistrict(State state, SchoolType schoolType, LevelCode levelCode, String string) {
            return 0;
        }

        public List findTopRatedSchoolsInCity(ICity iCity, int i, LevelCode.Level level, int i1) {
            return null;
        }

        public List findRecentNameChanges(State state, Date since, Date before) {
            return null;
        }

        public void setModifiedInfo(School school, Date modified, String modifiedBy) {
            // do nothing
        }

        public List findSchoolsInDataLimbo(State state, boolean activeOnly) {
            return null;
        }
    }
}
