package gs.web.school;

import gs.data.community.User;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.school.*;
import gs.data.security.Role;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.school.usp.EspStatus;
import gs.web.school.usp.EspStatusManager;
import gs.web.school.usp.UspFormController;
import org.easymock.classextension.EasyMock;
import org.springframework.beans.factory.BeanFactory;

import java.util.*;

import static org.easymock.classextension.EasyMock.*;

public class EspRegistrationConfirmationServiceTest extends BaseControllerTestCase {
    private EspRegistrationConfirmationService _service;
    private IEspResponseDao _espResponseDao;
    private IEspMembershipDao _espMembershipDao;
    private BeanFactory _beanFactory;
    private ISchoolDao _schoolDao;
    EspStatusManager _espStatusManager;
    private EspRegistrationHelper _espRegistrationHelper;
    private ExactTargetAPI _exactTargetAPI;

    public void setUp() throws Exception {
        super.setUp();
        _service = new EspRegistrationConfirmationService();

        _espResponseDao = EasyMock.createStrictMock(IEspResponseDao.class);
        _espMembershipDao = EasyMock.createStrictMock(IEspMembershipDao.class);
        _schoolDao = EasyMock.createStrictMock(ISchoolDao.class);
        _beanFactory = EasyMock.createStrictMock(BeanFactory.class);
        _espStatusManager = EasyMock.createStrictMock(EspStatusManager.class);
        _espRegistrationHelper = EasyMock.createStrictMock(EspRegistrationHelper.class);
        _exactTargetAPI = createStrictMock(ExactTargetAPI.class);

        _service.setEspResponseDao(_espResponseDao);
        _service.setEspMembershipDao(_espMembershipDao);
        _service.setBeanFactory(_beanFactory);
        _service.setSchoolDao(_schoolDao);
        _service.setEspRegistrationHelper(_espRegistrationHelper);
        _service.setExactTargetAPI(_exactTargetAPI);
    }

    private void resetAllMocks() {
        resetMocks(_espResponseDao,_espMembershipDao,_schoolDao, _beanFactory, _espStatusManager, _espRegistrationHelper, _exactTargetAPI);
    }

    private void replayAllMocks() {
        replayMocks(_espResponseDao,_espMembershipDao,_schoolDao, _beanFactory, _espStatusManager, _espRegistrationHelper, _exactTargetAPI);
    }

    private void verifyAllMocks() {
        verifyMocks(_espResponseDao,_espMembershipDao,_schoolDao, _beanFactory,_espStatusManager, _espRegistrationHelper, _exactTargetAPI);
    }

    public void testGetProcessingMembershipForUser() {
        resetAllMocks();
        User user = new User();
        user.setId(1);
        expect(_espMembershipDao.findEspMembershipsByUserId(1, false)).andReturn(null);
        replayAllMocks();
        assertNull(_service.getProcessingMembershipForUser(user));
        verifyAllMocks();

        resetAllMocks();

        List<EspMembership> memberships = new ArrayList<EspMembership>();
        expect(_espMembershipDao.findEspMembershipsByUserId(1, false)).andReturn(memberships);
        replayAllMocks();
        assertNull(_service.getProcessingMembershipForUser(user));
        verifyAllMocks();

        resetAllMocks();

        EspMembership memRejected = new EspMembership();
        memRejected.setStatus(EspMembershipStatus.REJECTED);
        memberships.add(memRejected);
        expect(_espMembershipDao.findEspMembershipsByUserId(1, false)).andReturn(memberships);
        replayAllMocks();
        assertNull(_service.getProcessingMembershipForUser(user));
        verifyAllMocks();

        resetAllMocks();

        EspMembership memProcessing = new EspMembership();
        memProcessing.setStatus(EspMembershipStatus.PROCESSING);
        memberships.add(memProcessing);
        expect(_espMembershipDao.findEspMembershipsByUserId(1, false)).andReturn(memberships);
        replayAllMocks();
        EspMembership rval = _service.getProcessingMembershipForUser(user);
        assertNotNull(rval);
        assertSame(memProcessing, rval);
        verifyAllMocks();
    }

    public void testHandleEspSubmissions_USP() {

        GsMockHttpServletRequest request = getRequest();
        request.setParameter("state", "CA");
        request.setParameter("schoolId", "1");
        request.setParameter(UspFormController.PARAM_USP_SUBMISSION, "true");

        User user = new User();
        user.setId(1);

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);
        school.setPhysicalAddress(new Address("ASD St", "Some city", State.CA, "12345"));
        school.setName("QWERTY Elementary");
        school.setLevelCode(LevelCode.ELEMENTARY);

        List<EspResponse> responses = new ArrayList<EspResponse>(Arrays.asList(new EspResponse()));

        //School is inactive.
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);

        replayAllMocks();
        EspRegistrationConfirmationService.EspSubmissionStatus status = _service.handleEspSubmissions(request, user);
        verifyAllMocks();
        assertEquals("Even though the school is inactive, " +
                "do not let the user know that.Therefore mark it as USP successfully submitted.",
                EspRegistrationConfirmationService.EspSubmissionStatus.USP_SUBMITTED, status);

        resetAllMocks();

        //Set school to active.
        school.setActive(true);

        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);
        expect(_beanFactory.getBean(eq(EspStatusManager.BEAN_NAME), isA(School.class))).andReturn(_espStatusManager);
        expect(_espStatusManager.getEspStatus()).andReturn(EspStatus.OSP_OUTDATED);
        expect(_espResponseDao.getResponses(school, user.getId(), true)).andReturn(responses);
        _espResponseDao.activateResponses(school, user.getId(), EspResponseSource.usp);
        _exactTargetAPI.sendTriggeredEmail(isA(String.class), isA(User.class), isA(Map.class));
        expectLastCall();

        replayAllMocks();
        status = _service.handleEspSubmissions(request, user);
        verifyAllMocks();
        assertEquals("School is not in OSP preferred status.USP was successfully submitted.",
                EspRegistrationConfirmationService.EspSubmissionStatus.USP_SUBMITTED, status);

        resetAllMocks();

        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);
        expect(_beanFactory.getBean(eq(EspStatusManager.BEAN_NAME), isA(School.class))).andReturn(_espStatusManager);
        expect(_espStatusManager.getEspStatus()).andReturn(EspStatus.OSP_PREFERRED);

        replayAllMocks();
        status = _service.handleEspSubmissions(request, user);
        verifyAllMocks();
        assertEquals("School is in OSP preferred status.But no need to let the user know. Therefore mark it as USP " +
                "successfully submitted.",
                EspRegistrationConfirmationService.EspSubmissionStatus.USP_SUBMITTED, status);

    }

    public void testHandleEspSubmissions_OSPVerified() {
        GsMockHttpServletRequest request = getRequest();
        User user = new User();
        user.setId(1);
        Role role = new Role();
        role.setKey(Role.ESP_MEMBER);
        user.addRole(role);

        replayAllMocks();
        EspRegistrationConfirmationService.EspSubmissionStatus status = _service.handleEspSubmissions(request, user);
        verifyAllMocks();
        assertEquals("User is an ESP Member",
                EspRegistrationConfirmationService.EspSubmissionStatus.OSP_VERIFIED, status);

    }

    public void testHandleEspSubmissions_OSPProvisional() {
        GsMockHttpServletRequest request = getRequest();
        User user = new User();
        user.setId(1);

        EspMembership membership = new EspMembership();
        membership.setStatus(EspMembershipStatus.PROCESSING);
        membership.setSchoolId(1);
        membership.setState(State.CA);
        List<EspMembership> memberships = new ArrayList<EspMembership>(Arrays.asList(membership));

        expect(_espMembershipDao.findEspMembershipsByUserId(user.getId(), false)).andReturn(memberships);
        expect(_espRegistrationHelper.isMembershipEligibleForProvisionalStatus(membership.getSchoolId(), membership.getState())).andReturn(true);
        _espMembershipDao.updateEspMembership(membership);

        replayAllMocks();
        EspRegistrationConfirmationService.EspSubmissionStatus status = _service.handleEspSubmissions(request, user);
        verifyAllMocks();
        assertEquals("User is eligible for upgrading to provisional.",
                EspRegistrationConfirmationService.EspSubmissionStatus.OSP_PROVISIONAL_UPGRADED, status);

        resetAllMocks();

        membership = new EspMembership();
        membership.setStatus(EspMembershipStatus.PROCESSING);
        membership.setSchoolId(1);
        membership.setState(State.CA);
        memberships = new ArrayList<EspMembership>(Arrays.asList(membership));

        expect(_espMembershipDao.findEspMembershipsByUserId(user.getId(), false)).andReturn(memberships);
        expect(_espRegistrationHelper.isMembershipEligibleForProvisionalStatus(membership.getSchoolId(), membership.getState())).andReturn(false);

        replayAllMocks();
        status = _service.handleEspSubmissions(request, user);
        verifyAllMocks();
        assertEquals("User is not eligible for upgrading to provisional.",
                EspRegistrationConfirmationService.EspSubmissionStatus.OSP_PROVISIONAL_NOT_UPGRADED, status);

        resetAllMocks();

        expect(_espMembershipDao.findEspMembershipsByUserId(user.getId(), false)).andReturn(null);

        replayAllMocks();
        status = _service.handleEspSubmissions(request, user);
        verifyAllMocks();
        assertEquals("No usp,no osp and no provisional usp.",
                EspRegistrationConfirmationService.EspSubmissionStatus.NO_ESP_SUBMISSION, status);

    }

}