package gs.web.school.usp;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.EspResponse;
import gs.data.school.IEspResponseDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.util.ListUtils;
import gs.web.BaseControllerTestCase;
import gs.web.community.registration.*;
import gs.web.school.EspSaveHelper;
import org.easymock.classextension.EasyMock;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static org.easymock.EasyMock.*;

public class UspFormControllerTest extends BaseControllerTestCase {
    UspFormController _controller;
    private IUserDao _userDao;
    private UspFormHelper _uspHelper;
    private ISchoolDao _schoolDao;
    private UserRegistrationOrLoginService _userRegistrationOrLoginService;
    private EspSaveHelper _espSaveHelper;
    private IEspResponseDao _espResponseDao;

    HttpServletRequest _request;
    HttpServletResponse _response;
    UserRegistrationCommand _userRegistrationCommand;
    UserLoginCommand _userLoginCommand;
    BindingResult _bindingResult;
    State _state;
    Integer _schoolId;
    BeanFactory _beanFactory;
    EspStatusManager _espStatusManager;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new UspFormController();

        _userDao = EasyMock.createStrictMock(IUserDao.class);
        _schoolDao = EasyMock.createStrictMock(ISchoolDao.class);
        _uspHelper = EasyMock.createStrictMock(UspFormHelper.class);
        _userRegistrationOrLoginService = EasyMock.createStrictMock(UserRegistrationOrLoginService.class);
        _espSaveHelper = EasyMock.createStrictMock(EspSaveHelper.class);
        _beanFactory = EasyMock.createStrictMock(BeanFactory.class);
        _espStatusManager = EasyMock.createStrictMock(EspStatusManager.class);
        _espResponseDao = EasyMock.createStrictMock(IEspResponseDao.class);

        _controller.setUserDao(_userDao);
        _controller.setUspFormHelper(_uspHelper);
        _controller.setSchoolDao(_schoolDao);
        _controller.setUserRegistrationOrLoginService(_userRegistrationOrLoginService);
        _controller.setEspSaveHelper(_espSaveHelper);
        _controller.setBeanFactory(_beanFactory);
        ReflectionTestUtils.setField(_controller, "_espResponseDao", _espResponseDao);
    }

    private void replayAllMocks() {
        replayMocks(_userDao, _schoolDao, _uspHelper, _userRegistrationOrLoginService, _espSaveHelper, _beanFactory, _espStatusManager, _espResponseDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_userDao, _schoolDao, _uspHelper, _userRegistrationOrLoginService, _espSaveHelper, _beanFactory, _espStatusManager, _espResponseDao);
    }

    private void resetAllMocks() {
        resetMocks(_userDao, _schoolDao, _uspHelper, _userRegistrationOrLoginService, _espSaveHelper, _beanFactory, _espStatusManager, _espResponseDao);
    }

    /**
     * Test with Nulls.
     */
    public void testCheckUserStateNulls() throws Exception {

        //Null email. Hence email should be invalid
        replayAllMocks();
        _controller.checkUserState(getRequest(),getResponse(),null,false,null);
        verifyAllMocks();

        String expectedJson = "{\"isCookieMatched\":true,\"isUserESPPreApproved\":false,\"isUserESPDisabled\":false,\"isUserApprovedESPMember\":false,\"isUserESPRejected\":false,\"isEmailValid\":false,\"isNewUser\":true,\"isUserCookieSet\":false,\"isUserAwaitingESPMembership\":false,\"isUserEmailValidated\":false}";
        assertEquals("application/json", getResponse().getContentType());
        assertEquals(expectedJson, getResponse().getContentAsString());
    }

    public void testCheckUserStateWithProvisionalUser() throws Exception {
        //provisional user
        User user = new User();
        user.setId(1);
        user.setPlaintextPassword("password");
        user.setEmailVerified(false);
        user.setEmailProvisional("password");

        //Email is valid. However isLogin is false
        expect(_userDao.findUserFromEmailIfExists("someone@somedomain.com")).andReturn(user);

        replayAllMocks();
        _controller.checkUserState(getRequest(),getResponse(),"someone@somedomain.com",false,null);
        verifyAllMocks();

        String expectedJson = new String("{\"isCookieMatched\":true,\"isUserESPPreApproved\":false,\"isUserESPDisabled\":false,\"isUserApprovedESPMember\":false,\"isUserESPRejected\":false,\"isEmailValid\":true,\"isNewUser\":false,\"isUserCookieSet\":false,\"isUserAwaitingESPMembership\":false,\"isUserEmailValidated\":false}");
        assertEquals("application/json", getResponse().getContentType());
        assertEquals("User is provisional.",expectedJson, getResponse().getContentAsString());
    }

    public void testCheckUserStateWithValidatedUserWrongPassword() throws Exception {

        //Email validated user.
        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setPlaintextPassword("password");

        //Email is valid. But password does not match.
        expect(_userDao.findUserFromEmailIfExists("someone@somedomain.com")).andReturn(user);

        replayAllMocks();
        _controller.checkUserState(getRequest(),getResponse(),"someone@somedomain.com",true,null);
        verifyAllMocks();

        String expectedJson = new String("{\"isCookieMatched\":false,\"isUserESPPreApproved\":false,\"isUserESPDisabled\":false,\"isUserApprovedESPMember\":false,\"isUserESPRejected\":false,\"isEmailValid\":true,\"isNewUser\":false,\"isUserCookieSet\":false,\"isUserAwaitingESPMembership\":false,\"isUserEmailValidated\":true}");
        assertEquals("application/json", getResponse().getContentType());
        assertEquals("User is email validated.But passwords dont match.",expectedJson, getResponse().getContentAsString());
    }

    public void testCheckUserStateWithValidatedUser() throws Exception {

        //Email validated user.
        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setPlaintextPassword("password");

        //Email is valid.
        expect(_userDao.findUserFromEmailIfExists("someone@somedomain.com")).andReturn(user);

        replayAllMocks();
        _controller.checkUserState(getRequest(),getResponse(),"someone@somedomain.com",true,"password");
        verifyAllMocks();

        String expectedJson = new String("{\"isCookieMatched\":true,\"isUserESPPreApproved\":false,\"isUserESPDisabled\":false,\"isUserApprovedESPMember\":false,\"isUserESPRejected\":false,\"isEmailValid\":true,\"isNewUser\":false,\"isUserCookieSet\":false,\"isUserAwaitingESPMembership\":false,\"isUserEmailValidated\":true}");
        assertEquals("application/json", getResponse().getContentType());
        assertEquals("User is email validated.",expectedJson, getResponse().getContentAsString());
    }

    public void testShowUserForm() {
        resetAllMocks();

        ModelMap modelMap = new ModelMap();

        replayAllMocks();
        String view = _controller.showUspUserForm(modelMap, getRequest(), null, null);
        verifyAllMocks();

        assertEquals("", view);

        resetAllMocks();

        User user = new User();
        user.setId(1);
        getSessionContext().setUser(user);
        State state = State.CA;
        Integer schoolId = 1;
        School school = getSchool(state, schoolId);

        expect(_schoolDao.getSchoolById(state,schoolId)).andReturn(school);

        expect(_espResponseDao.getResponsesByUserAndSchool(eq(school), isA(Integer.class), eq(false))).andReturn(
            Arrays.asList(
                EspResponse.with().school(school).key("abc").value("123").create(),
                EspResponse.with().school(school).key("abc").value("456").create()
            )
        );

        expect(_beanFactory.getBean(eq("espStatusManager"), eq(school), isA(EspResponseData.class))).andReturn(_espStatusManager);
        expect(_espStatusManager.getEspStatus()).andReturn(EspStatus.NO_DATA);
        expect(_uspHelper.formFieldsBuilderHelper(isA(Multimap.class), eq(false))).andReturn(null);

        replayAllMocks();
        view = _controller.showUspUserForm(modelMap, getRequest(), schoolId, state);
        verifyAllMocks();

        assertEquals(UspFormController.FORM_VIEW, view);
    }

    public void testUserFormUnavailable() {
        ModelMap modelMap = new ModelMap();

        resetAllMocks();

        User user = new User();
        user.setId(1);
        getSessionContext().setUser(user);
        State state = State.CA;
        Integer schoolId = 1;
        School school = getSchool(state, schoolId);
        expect(_schoolDao.getSchoolById(state, schoolId)).andReturn(school);

        expect(_espResponseDao.getResponsesByUserAndSchool(eq(school), isA(Integer.class), eq(false))).andReturn(
            Arrays.asList(
                EspResponse.with().school(school).create(),
                EspResponse.with().school(school).create()
            )
        );

        expect(_beanFactory.getBean(eq("espStatusManager"), eq(school), isA(EspResponseData.class))).andReturn(_espStatusManager);
        expect(_espStatusManager.getEspStatus()).andReturn(EspStatus.OSP_PREFERRED);

        replayAllMocks();
        String view = _controller.showUspUserForm(modelMap, getRequest(), schoolId, state);
        verifyAllMocks();

        assertEquals(UspFormController.FORM_UNAVAILABLE_VIEW, view);
    }

    public void testDetermineRedirectsWithNulls() {
        User user = new User();
        UserStateStruct userStateStruct = new UserStateStruct();
        School school = new School();
        HttpServletRequest request = getRequest();

        String url = _controller.determineRedirects(null, null, null,null, null, false);
        assertEquals("Url should be blank since all params were null", null, url);
    }

    public void testDetermineRedirectsWithUserLoggedIn() throws Exception {

        //Email validated user.
        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setPlaintextPassword("password");

        UserStateStruct userStateStruct = new UserStateStruct();
        userStateStruct.setUserLoggedIn(true);

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);

        //user already has previous answers
        String url = _controller.determineRedirects(user, userStateStruct, school, getRequest(), getResponse(), true);
        assertEquals("User is logged in.",
                "http://www.greatschools.org/school/usp/form.page?schoolId=1&showExistingAnswersMsg=true&state=CA", url);

    }

    public void testDetermineRedirectsWithUserInSession() throws Exception {

        //Email validated user.
        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setPlaintextPassword("password");

        UserStateStruct userStateStruct = new UserStateStruct();
        userStateStruct.setUserInSession(true);

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);

        String url = _controller.determineRedirects(user, userStateStruct, school, getRequest(), getResponse(), false);
        assertEquals("User is in the session in.",
                "http://www.greatschools.org/school/usp/thankYou.page?schoolId=1&state=CA", url);
    }

    public void testDetermineRedirectsWithUserRegistered() throws Exception {
        //Email validated user.
        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setPlaintextPassword("password");

        UserStateStruct userStateStruct = new UserStateStruct();
        userStateStruct.setUserRegistered(true);

        School school = new School();
        school.setId(1);
        school.setName("schoolName");
        school.setCity("city");
        school.setDatabaseState(State.CA);

        String url = _controller.determineRedirects(user, userStateStruct, school, getRequest(), getResponse(), false);
        assertEquals("User has been registered.",
                "http://www.greatschools.org/california/city/1-SchoolName/", url);
    }

    public void testFormSubmitNullSchoolId() throws UnsupportedEncodingException {
        setUpFormSubmitVariables();
        _controller.onUspUserSubmitForm(_request, _response, _userRegistrationCommand, _userLoginCommand, _bindingResult,
                null, _state);
        assertEquals(((MockHttpServletResponse) _response).getContentAsString(), "{\"error\":\"noSchool\"}");
    }

    public void testFormSubmitNonExistingSchool() throws UnsupportedEncodingException {
        setUpFormSubmitVariables();
        resetAllMocks();

        expect(_schoolDao.getSchoolById(_state, _schoolId)).andReturn(null);

        replayAllMocks();
        _controller.onUspUserSubmitForm(_request, _response, _userRegistrationCommand, _userLoginCommand, _bindingResult,
                _schoolId, _state);
        verifyAllMocks();

        assertEquals(((MockHttpServletResponse) _response).getContentAsString(), "{\"error\":\"noSchool\"}");
    }

    public void testFormSubmitWithNoPrevSavedResponses() throws Exception {
        setUpFormSubmitVariables();
        UserStateStruct userStateStruct = new UserStateStruct();
        User user = new User();
        user.setId(1);
        userStateStruct.setUser(user);
        School school = getSchool(_state, _schoolId);
        resetAllMocks();

        expect(_schoolDao.getSchoolById(_state, _schoolId)).andReturn(school);
        expect(_userRegistrationOrLoginService.getUserStateStruct(isA(UserRegistrationCommand.class), isA(UserLoginCommand.class),
                isA(UspRegistrationBehavior.class), isA(BindingResult.class), isA(MockHttpServletRequest.class),
                isA(MockHttpServletResponse.class))).andReturn(userStateStruct);
//        expect(_uspHelper.getSavedResponses(user, school, _state, false)).andReturn((Multimap) LinkedListMultimap.create());
        _espSaveHelper.saveUspFormData(user, school, _state, _request.getParameterMap(), UspFormHelper.FORM_FIELD_TITLES.keySet());
        expectLastCall();

        replayAllMocks();
        _controller.onUspUserSubmitForm(_request, _response, _userRegistrationCommand, _userLoginCommand, _bindingResult,
                _schoolId, _state);
        verifyAllMocks();

        assertEquals(((MockHttpServletResponse) _response).getContentAsString(), "");
    }

    public void testFormSubmitWithPrevSavedResponses() throws Exception {
        setUpFormSubmitVariables();
        UserStateStruct userStateStruct = new UserStateStruct();
        User user = new User();
        user.setId(1);
        user.setPlaintextPassword("qwerty");
        userStateStruct.setUser(user);
        userStateStruct.setUserLoggedIn(true);
        School school = getSchool(_state, _schoolId);
        resetAllMocks();

        expect(_schoolDao.getSchoolById(_state, _schoolId)).andReturn(school);
        expect(_userRegistrationOrLoginService.getUserStateStruct(isA(UserRegistrationCommand.class), isA(UserLoginCommand.class),
                isA(UspRegistrationBehavior.class), isA(BindingResult.class), isA(MockHttpServletRequest.class),
                isA(MockHttpServletResponse.class))).andReturn(userStateStruct);
        expect(_uspHelper.getSavedResponses(user, school, _state, false)).andReturn((Multimap) LinkedListMultimap.create());
        _espSaveHelper.saveUspFormData(user, school, _state, _request.getParameterMap(), UspFormHelper.FORM_FIELD_TITLES.keySet());
        expectLastCall();

        replayAllMocks();
        _controller.onUspUserSubmitForm(_request, _response, _userRegistrationCommand, _userLoginCommand, _bindingResult,
                _schoolId, _state);
        verifyAllMocks();

        assertEquals(((MockHttpServletResponse) _response).getContentAsString(), "{\"redirect\":\"http://www.greatschools.org/school/usp/thankYou.page?schoolId=1&state=CA\"}");
    }

    public School getSchool(State state, Integer schoolId) {
        School school = new School();
        school.setId(schoolId);
        school.setDatabaseState(state);
        school.setName("QWERT Elementary School");
        school.setActive(true);
        return school;
    }

    public void setUpFormSubmitVariables() {
        _request = getRequest();
        _response = getResponse();
        _userRegistrationCommand = new UserRegistrationCommand();
        _userLoginCommand = new UserLoginCommand();
        _bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "userRegistrationCommand");
        _state = State.CA;
        _schoolId = 1;
    }
}