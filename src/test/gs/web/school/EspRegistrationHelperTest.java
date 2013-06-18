package gs.web.school;

import gs.data.school.*;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.easymock.classextension.EasyMock;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.easymock.classextension.EasyMock.*;

public class EspRegistrationHelperTest extends BaseControllerTestCase {
    private EspRegistrationHelper _helper;
    private IEspResponseDao _espResponseDao;
    private IEspMembershipDao _espMembershipDao;
    private ISchoolDao _schoolDao;

    public void setUp() throws Exception {
        super.setUp();
        _helper = new EspRegistrationHelper();

        _espResponseDao = EasyMock.createStrictMock(IEspResponseDao.class);
        _espMembershipDao = EasyMock.createStrictMock(IEspMembershipDao.class);
        _schoolDao = EasyMock.createStrictMock(ISchoolDao.class);

        _helper.setEspResponseDao(_espResponseDao);
        _helper.setEspMembershipDao(_espMembershipDao);
        _helper.setSchoolDao(_schoolDao);
    }

    private void resetAllMocks() {
        resetMocks(_espResponseDao,_espMembershipDao, _schoolDao);
    }

    private void replayAllMocks() {
        replayMocks(_espResponseDao,_espMembershipDao, _schoolDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_espResponseDao,_espMembershipDao, _schoolDao);
    }

    public void testIsMembershipEligibleForPromotionToProvisional() {
        School school = new School();
        resetAllMocks();
        boolean rval;
        EspMembership membership = new EspMembership();
        membership.setStatus(EspMembershipStatus.PROCESSING);
        membership.setSchoolId(1);
        membership.setState(State.CA);

        expect(_schoolDao.getSchoolById(State.CA, 1)).andThrow(new RuntimeException("testIsMembershipEligibleForPromotionToProvisional"));
        replayAllMocks();
        rval = _helper.isMembershipEligibleForProvisionalStatus(membership.getSchoolId(),membership.getState());
        assertFalse("Expect failure to retrieve school to disallow provisional osp", rval);
        verifyAllMocks();

        resetAllMocks();

        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(null);
        replayAllMocks();
        rval = _helper.isMembershipEligibleForProvisionalStatus(membership.getSchoolId(),membership.getState());
        assertFalse("Expect failure to retrieve school to disallow provisional osp", rval);
        verifyAllMocks();

        resetAllMocks();

        List<EspMembership> memberships = new ArrayList<EspMembership>();
        memberships.add(membership);
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);
        expect(_espMembershipDao.findEspMembershipsBySchool(school, false)).andReturn(memberships);
        expect(_espResponseDao.getMaxCreatedForSchool(school, false, EspResponseSource.osp)).andReturn(null);
        replayAllMocks();
        rval = _helper.isMembershipEligibleForProvisionalStatus(membership.getSchoolId(),membership.getState());
        assertTrue("Expect no pre-existing responses to allow provisional osp", rval);
        verifyAllMocks();

        resetAllMocks();

        Date lastUpdated;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        lastUpdated = cal.getTime();
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);
        expect(_espMembershipDao.findEspMembershipsBySchool(school, false)).andReturn(memberships);
        expect(_espResponseDao.getMaxCreatedForSchool(school, false, EspResponseSource.osp)).andReturn(lastUpdated);
        replayAllMocks();
        rval = _helper.isMembershipEligibleForProvisionalStatus(membership.getSchoolId(),membership.getState());
        assertTrue("Expect year old response to allow provisional osp", rval);
        verifyAllMocks();

        resetAllMocks();

        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -8);
        lastUpdated = cal.getTime();
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);
        expect(_espMembershipDao.findEspMembershipsBySchool(school, false)).andReturn(memberships);
        expect(_espResponseDao.getMaxCreatedForSchool(school, false, EspResponseSource.osp)).andReturn(lastUpdated);
        replayAllMocks();
        rval = _helper.isMembershipEligibleForProvisionalStatus(membership.getSchoolId(),membership.getState());
        assertTrue("Expect 8 day old response to allow provisional osp", rval);
        verifyAllMocks();

        resetAllMocks();

        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);
        lastUpdated = cal.getTime();
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);
        expect(_espMembershipDao.findEspMembershipsBySchool(school, false)).andReturn(memberships);
        expect(_espResponseDao.getMaxCreatedForSchool(school, false, EspResponseSource.osp)).andReturn(lastUpdated);
        replayAllMocks();
        rval = _helper.isMembershipEligibleForProvisionalStatus(membership.getSchoolId(),membership.getState());
        assertFalse("Expect a response 6 days ago to prevent provisional osp", rval);
        verifyAllMocks();

        resetAllMocks();

        EspMembership provisionalMembership = new EspMembership();
        provisionalMembership.setStatus(EspMembershipStatus.PROVISIONAL);
        memberships.add(provisionalMembership);
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);
        expect(_espMembershipDao.findEspMembershipsBySchool(school, false)).andReturn(memberships);
        replayAllMocks();
        rval = _helper.isMembershipEligibleForProvisionalStatus(membership.getSchoolId(),membership.getState());
        assertFalse("Expect existing provisional membership to prevent new one", rval);
        verifyAllMocks();
    }

}