package gs.web.admin;

import gs.data.community.User;
import gs.data.school.*;
import gs.data.state.INoEditDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.school.EspFormExternalDataHelper;
import gs.web.school.EspFormValidationHelper;
import gs.web.school.EspSaveHelper;
import gs.web.school.usp.EspStatusManager;
import org.easymock.classextension.EasyMock;
import org.springframework.beans.factory.BeanFactory;

import java.util.*;

import static org.easymock.EasyMock.*;


public class AbstractEspModerationControllerTest extends BaseControllerTestCase {
    private EspSaveHelper _espSaveHelper;
    private IEspResponseDao _espResponseDao;
    private IEspMembershipDao _espMembershipDao;
    private AbstractEspModerationController _controller;
    private INoEditDao _noEditDao;
    private ISchoolDao _schoolDao;
    private EspFormExternalDataHelper _espFormExternalDataHelper;
    private EspFormValidationHelper _espFormValidationHelper;
    private BeanFactory _beanFactory;
    EspStatusManager _espStatusManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _espSaveHelper = new EspSaveHelper();
        _espFormExternalDataHelper = new EspFormExternalDataHelper();
        _espFormValidationHelper = new EspFormValidationHelper();
        _controller = new EspModerationDetailsController();

        _noEditDao = createMock(INoEditDao.class);
        _espResponseDao = createMock(IEspResponseDao.class);
        _schoolDao = createMock(ISchoolDao.class);
        _espMembershipDao = createMock(IEspMembershipDao.class);
        _beanFactory = EasyMock.createStrictMock(BeanFactory.class);
        _espStatusManager = EasyMock.createStrictMock(EspStatusManager.class);

        _controller.setEspMembershipDao(_espMembershipDao);
        _controller.setEspResponseDao(_espResponseDao);
        _controller.setEspSaveHelper(_espSaveHelper);
        _controller.setNoEditDao(_noEditDao);
        _controller.setSchoolDao(_schoolDao);

        _espSaveHelper.setEspFormExternalDataHelper(_espFormExternalDataHelper);
        _espSaveHelper.setEspFormValidationHelper(_espFormValidationHelper);
        _espSaveHelper.setNoEditDao(_noEditDao);
        _espSaveHelper.setEspResponseDao(_espResponseDao);
        _espSaveHelper.setBeanFactory(_beanFactory);
        _espFormExternalDataHelper.setSchoolDao(_schoolDao);
    }

    private void resetAllMocks() {
        resetMocks(_noEditDao, _schoolDao, _espResponseDao, _espMembershipDao, _beanFactory, _espStatusManager);
    }

    private void replayAllMocks() {
        replayMocks(_noEditDao, _schoolDao, _espResponseDao, _espMembershipDao, _beanFactory, _espStatusManager);
    }

    private void verifyAllMocks() {
        verifyMocks(_noEditDao, _schoolDao, _espResponseDao, _espMembershipDao, _beanFactory, _espStatusManager);
    }

    public void testPromoteProvisionalDataToActiveDataWithNullResponses() {
        User user = new User();
        user.setId(1);
        School school = new School();
        school.setDatabaseState(State.CA);

        //Test with null
        expect(_espResponseDao.getResponses(school, user.getId(), true)).andReturn(null);
        replayAllMocks();
        _controller.promoteProvisionalDataToActiveData(user, school, getRequest(), getResponse());
        verifyAllMocks();
    }

    public void testPromoteProvisionalDataToActiveData_NonOspPreferred() {
        User user = new User();
        user.setId(1);
        School school = new School();
        school.setDatabaseState(State.CA);

        //Test with grade_levels,address and transportation as provisional responses.
        EspResponse espResponse1 = buildEspResponse("grade_levels", "4", false);
        EspResponse espResponse2 = buildEspResponse("grade_levels", "5", false);
        EspResponse espResponse3 = buildEspResponse("grade_levels", "6", false);

        String addressStr = "some street 1, \n" + "street 2\n" + "san francisco, " + "california  " + "94101";
        EspResponse espResponse4 = buildEspResponse("address", addressStr, false);

        EspResponse espResponse5 = buildEspResponse("transportation", "none", false);

        EspResponse espResponse6 = buildEspResponse("_page_1_keys", "grade_levels,address,transportation", false);

        List<EspResponse> espResponses = new ArrayList<EspResponse>();
        espResponses.add(espResponse1);
        espResponses.add(espResponse2);
        espResponses.add(espResponse3);
        espResponses.add(espResponse4);
        espResponses.add(espResponse5);
        espResponses.add(espResponse6);

        Set<String> keysForPage = new HashSet<String>();
        keysForPage.add("grade_levels");
        keysForPage.add("address");
        keysForPage.add("transportation");

        Set<String> pageKeys = new HashSet<String>(Arrays.asList("_page_osp_gateway_keys"));

        Set<EspResponseSource> responseSourcesToDeactivate =
                new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.osp, EspResponseSource.datateam));

        Set<EspResponseSource> responseSourcesToDeactivateIfOspPreferred =
                new HashSet<EspResponseSource>();

        Set<Integer> memberIdsToExclude = new HashSet<Integer>();
        memberIdsToExclude.add(user.getId());

        expect(_espResponseDao.getResponses(school, user.getId(), true)).andReturn(espResponses);
        expect(_noEditDao.isStateLocked(school.getDatabaseState())).andReturn(false);
        expect(_beanFactory.getBean(eq(EspStatusManager.BEAN_NAME), isA(School.class))).andReturn(_espStatusManager);
        //All questions are answered, therefore not OSP preferred.
        expect(_espStatusManager.allOSPQuestionsAnswered(isA(Map.class))).andReturn(false);
        _espResponseDao.deactivateResponses(school, keysForPage, responseSourcesToDeactivate, responseSourcesToDeactivateIfOspPreferred);
        _espResponseDao.deleteResponses(school, user.getId(), pageKeys);
        _espResponseDao.saveResponses(isA(School.class), isA(ArrayList.class));
        _schoolDao.saveSchool(isA(State.class), isA(School.class), isA(String.class));
        _schoolDao.saveSchool(isA(State.class), isA(School.class), isA(String.class));
        expect(_espResponseDao.schoolHasNoUserCreatedRows(school, true, memberIdsToExclude)).andReturn(false);

        replayAllMocks();
        _controller.promoteProvisionalDataToActiveData(user, school, getRequest(), getResponse());
        verifyAllMocks();


    }

    public void testPromoteProvisionalDataToActiveData_OspPreferred() {
        User user = new User();
        user.setId(1);
        School school = new School();
        school.setDatabaseState(State.CA);

        //Test with grade_levels,address and transportation as provisional responses.
        EspResponse espResponse1 = buildEspResponse("grade_levels", "4", false);
        EspResponse espResponse2 = buildEspResponse("grade_levels", "5", false);
        EspResponse espResponse3 = buildEspResponse("grade_levels", "6", false);

        String addressStr = "some street 1, \n" + "street 2\n" + "san francisco, " + "california  " + "94101";
        EspResponse espResponse4 = buildEspResponse("address", addressStr, false);

        EspResponse espResponse5 = buildEspResponse("transportation", "none", false);

        EspResponse espResponse6 = buildEspResponse("_page_1_keys", "grade_levels,address,transportation", false);

        List<EspResponse> espResponses = new ArrayList<EspResponse>();
        espResponses.add(espResponse1);
        espResponses.add(espResponse2);
        espResponses.add(espResponse3);
        espResponses.add(espResponse4);
        espResponses.add(espResponse5);
        espResponses.add(espResponse6);

        Set<String> keysForPage = new HashSet<String>();
        keysForPage.add("grade_levels");
        keysForPage.add("address");
        keysForPage.add("transportation");

        Set<String> pageKeys = new HashSet<String>(Arrays.asList("_page_osp_gateway_keys"));

        Set<EspResponseSource> responseSourcesToDeactivate =
                new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.osp, EspResponseSource.datateam));

        Set<EspResponseSource> responseSourcesToDeactivateIfOspPreferred =
                new HashSet<EspResponseSource>(Arrays.asList(EspResponseSource.usp));

        Set<Integer> memberIdsToExclude = new HashSet<Integer>();
        memberIdsToExclude.add(user.getId());

        expect(_espResponseDao.getResponses(school, user.getId(), true)).andReturn(espResponses);
        expect(_noEditDao.isStateLocked(school.getDatabaseState())).andReturn(false);
        expect(_beanFactory.getBean(eq(EspStatusManager.BEAN_NAME), isA(School.class))).andReturn(_espStatusManager);
        //All questions are answered, therefore OSP preferred.
        expect(_espStatusManager.allOSPQuestionsAnswered(isA(Map.class))).andReturn(true);
        _espResponseDao.deactivateResponses(school, keysForPage, responseSourcesToDeactivate, responseSourcesToDeactivateIfOspPreferred);
        _espResponseDao.deleteResponses(school, user.getId(), pageKeys);
        _espResponseDao.saveResponses(isA(School.class), isA(ArrayList.class));
        _schoolDao.saveSchool(isA(State.class), isA(School.class), isA(String.class));
        _schoolDao.saveSchool(isA(State.class), isA(School.class), isA(String.class));
        expect(_espResponseDao.schoolHasNoUserCreatedRows(school, true, memberIdsToExclude)).andReturn(false);

        replayAllMocks();
        _controller.promoteProvisionalDataToActiveData(user, school, getRequest(), getResponse());
        verifyAllMocks();
    }

    public void testUpdateEspMembershipStateLocked() {
        User user = new User();
        user.setId(1);
        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);

        List<Integer> membershipIds = new ArrayList<Integer>();
        membershipIds.add(new Integer(1));
        EspModerationCommand command = new EspModerationCommand();
        command.setModeratorAction("approve");
        command.setEspMembershipIds(membershipIds);

        EspMembership membership = new EspMembership();
        membership.setId(1);
        membership.setStatus(EspMembershipStatus.PROVISIONAL);
        membership.setActive(false);
        membership.setState(State.CA);
        membership.setSchoolId(school.getId());
        membership.setUser(user);
        membership.setSchool(school);

        expect(_espMembershipDao.findEspMembershipById(1, false)).andReturn(membership);
        expect(_schoolDao.getSchoolById(membership.getState(), membership.getSchoolId())).andReturn(school);
        //state is locked
        expect(_noEditDao.isStateLocked(school.getDatabaseState())).andReturn(true);
        replayAllMocks();
        _controller.updateEspMembership(command, getRequest(), getResponse());
        verifyAllMocks();
    }

    public EspResponse buildEspResponse(String key, String value, boolean active) {
        EspResponse espResponse = new EspResponse();
        espResponse.setKey(key);
        espResponse.setValue(value);
        espResponse.setActive(active);
        return espResponse;
    }
}