package gs.web.community;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.easymock.EasyMock.*;

public class MySchoolListAjaxControllerTest extends BaseControllerTestCase {
    MySchoolListAjaxController _controller;

    ISchoolDao _schoolDao;
    IUserDao _userDao;

    MySchoolListHelper _mySchoolListHelper;
    MySchoolListConfirmationEmail _mySchoolListConfirmationEmail;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new MySchoolListAjaxController();

        _userDao = createStrictMock(IUserDao.class);
        _schoolDao = createStrictMock(ISchoolDao.class);
        _mySchoolListHelper = org.easymock.classextension.EasyMock.createStrictMock(MySchoolListHelper.class);
        _mySchoolListConfirmationEmail = org.easymock.classextension.EasyMock.createStrictMock(MySchoolListConfirmationEmail.class);

        _controller.setSchoolDao(_schoolDao);
        _controller.setUserDao(_userDao);
        _controller.setMySchoolListHelper(_mySchoolListHelper);
        _controller.setMySchoolListConfirmationEmail(_mySchoolListConfirmationEmail);
    }

    public void testHandleAddWithExistingUser() throws Exception {
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        int testId = 1;
        State testState = State.CA;
        String testEmail = "test@greatschools.org";

        User user = new User();
        user.setEmail(testEmail);
        user.setId(testId);

        School school = new School();
        school.setId(testId);
        school.setDatabaseState(testState);

        MySchoolListCommand command = new MySchoolListCommand();
        command.setSchoolId(testId);
        command.setSchoolDatabaseState(testState.getAbbreviationLowerCase());

        getSessionContext().setUser(user);

        expect(_schoolDao.getSchoolById(testState, testId)).andReturn(school);
        _mySchoolListHelper.addToMSL(eq(user), eq(school));

        replay(_schoolDao);
        org.easymock.classextension.EasyMock.replay(_mySchoolListHelper);

        ModelAndView result = _controller.handleAdd(request, response, command);

        assertEquals("Should be json response", "application/json",response.getContentType());
        assertNull("should have received null", result);
        verify(_schoolDao);
        org.easymock.classextension.EasyMock.verify(_mySchoolListHelper);
    }

    public void testHandleAddWithExistingUserPlusEmailInCommand() throws Exception {
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        int testId = 1;
        State testState = State.CA;
        String testEmail = "test@greatschools.org";

        User user = new User();
        user.setEmail(testEmail);
        user.setId(testId);

        School school = new School();
        school.setId(testId);
        school.setDatabaseState(testState);

        MySchoolListCommand command = new MySchoolListCommand();
        command.setSchoolId(testId);
        command.setSchoolDatabaseState(testState.getAbbreviationLowerCase());
        command.setEmail(testEmail);

        getSessionContext().setUser(user);

        expect(_schoolDao.getSchoolById(testState, testId)).andReturn(school);
        _mySchoolListHelper.addToMSL(eq(user), eq(school));

        replay(_schoolDao);
        org.easymock.classextension.EasyMock.replay(_mySchoolListHelper);

        ModelAndView result = _controller.handleAdd(request, response, command);

        assertEquals("Should be json response", "application/json",response.getContentType());
        assertNull("should have received null", result);
        verify(_schoolDao);
        org.easymock.classextension.EasyMock.verify(_mySchoolListHelper);
    }

    public void testHandleAddWithEmailAndNoExistingUser() throws Exception {
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        int testId = 1;
        State testState = State.CA;
        String testEmail = "test@greatschools.org";

        User user = new User();
        user.setEmail(testEmail);
        user.setId(testId);

        School school = new School();
        school.setId(testId);
        school.setDatabaseState(testState);

        MySchoolListCommand command = new MySchoolListCommand();
        command.setSchoolId(testId);
        command.setSchoolDatabaseState(testState.getAbbreviationLowerCase());
        command.setEmail(testEmail);
        command.setRedirectUrl("www.greatschools.org");

        expect(_schoolDao.getSchoolById(testState, testId)).andReturn(school);
        expect(_userDao.findUserFromEmailIfExists(testEmail)).andReturn(null);
        expect(_mySchoolListHelper.createNewMSLUser(eq(testEmail))).andReturn(user);
        _mySchoolListConfirmationEmail.sendToUser(eq(user), eq(getRequest()));
        _mySchoolListHelper.addToMSL(eq(user), eq(school));

        replay(_schoolDao, _userDao);
        org.easymock.classextension.EasyMock.replay(_mySchoolListHelper);

        ModelAndView result = _controller.handleAdd(request, response, command);

        assertNotNull("should have received redirect", result);
        verify(_schoolDao, _userDao);
        org.easymock.classextension.EasyMock.verify(_mySchoolListHelper);
    }

    public void testHandleAddWithEmailAndExistingUser() throws Exception {
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        int testId = 1;
        State testState = State.CA;
        String testEmail = "test@greatschools.org";

        User user = new User();
        user.setEmail(testEmail);
        user.setId(testId);

        School school = new School();
        school.setId(testId);
        school.setDatabaseState(testState);

        MySchoolListCommand command = new MySchoolListCommand();
        command.setSchoolId(testId);
        command.setSchoolDatabaseState(testState.getAbbreviationLowerCase());
        command.setEmail(testEmail);
        command.setRedirectUrl("www.greatschools.org");

        expect(_schoolDao.getSchoolById(testState, testId)).andReturn(school);
        expect(_userDao.findUserFromEmailIfExists(testEmail)).andReturn(user);
        _mySchoolListHelper.addToMSL(eq(user), eq(school));

        replay(_schoolDao, _userDao);
        org.easymock.classextension.EasyMock.replay(_mySchoolListHelper);

        ModelAndView result = _controller.handleAdd(request, response, command);

        assertNotNull("should have received redirect", result);
        verify(_schoolDao, _userDao);
        org.easymock.classextension.EasyMock.verify(_mySchoolListHelper);
    }

}
