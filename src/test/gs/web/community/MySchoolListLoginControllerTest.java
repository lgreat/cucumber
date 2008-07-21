package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.IUserDao;
import gs.data.community.User;
import org.springframework.web.servlet.ModelAndView;
import static org.easymock.EasyMock.*;

public class MySchoolListLoginControllerTest extends BaseControllerTestCase {

    MySchoolListLoginController _controller;
    IUserDao _mockUserDao;
    User _testUser;

    public void setUp() throws Exception {
        super.setUp();

        _testUser = new User();
        _testUser.setEmail("eford@greatschools.net");
        _testUser.setId(1);

        _controller = new MySchoolListLoginController();
        _controller.setCommandClass(gs.web.community.LoginCommand.class);
        _controller.setFormView("/community/mySchoolListLogin");
        _controller.setSuccessView("/community/mySchoolList");
        _mockUserDao = createStrictMock(IUserDao.class);
        _controller.setUserDao(_mockUserDao);
    }

    public void testRequestWithKnownUser() throws Exception {
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        sc.setMemberId(1);
        getRequest().setMethod("GET");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        // for now, known users will not be redirected to the msl - instead, they will be
        // allowed to change identity on the login page.
        assertEquals("Expected msl login form view", "/community/mySchoolListLogin", mAndV.getViewName());
//        assertEquals("Should redirect to msl page",
//                MySchoolListController.LIST_VIEW_NAME, mAndV.getViewName());
    }

    public void testRequestWithUnknownUser() throws Exception {
        getRequest().setMethod("GET");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        assertEquals("Expected msl login form view", "/community/mySchoolListLogin", mAndV.getViewName());
    }

    public void testSubmitWithKnownEmail() throws Exception {
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        assertNull("Member id should not be set", sc.getUser());
        getRequest().setParameter("email", _testUser.getEmail());
        getRequest().setMethod("POST");
        expect(_mockUserDao.findUserFromEmailIfExists(_testUser.getEmail())).andReturn(_testUser);
        replay(_mockUserDao);
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        verify(_mockUserDao);
        assertEquals("Expected the MSL page", _controller.getSuccessView(), mAndV.getViewName());
        assertEquals("Member cookie should now be in response", "1", getResponse().getCookie("MEMID").getValue());
    }

    public void testSubmitWithUnkownEmail() throws Exception {
        assertNull("Member cookie should not be set", getResponse().getCookie("MEMID"));
        _testUser.setEmail("foo@flimflam.com");
        _testUser.setId(1234);
        getRequest().setParameter("email", _testUser.getEmail());
        getRequest().setMethod("POST");
        expect(_mockUserDao.findUserFromEmailIfExists(_testUser.getEmail())).andReturn(null);
        _mockUserDao.saveUser(_testUser);
        expect(_mockUserDao.findUserFromEmail(_testUser.getEmail())).andReturn(_testUser);
        replay(_mockUserDao);
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        verify(_mockUserDao);
        assertEquals("Expected the MSL page", _controller.getSuccessView(), mAndV.getViewName());
        assertEquals("Member cookie should be set set to user id", "1234", getResponse().getCookie("MEMID").getValue());
    }
}
