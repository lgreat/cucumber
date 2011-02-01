package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.security.NoSuchAlgorithmException;

public class MySchoolListLoginControllerTest extends BaseControllerTestCase {

    MySchoolListLoginController _controller;
    User _testUser;

    public void setUp() throws Exception {
        super.setUp();

        _testUser = new User();
        _testUser.setEmail("eford@greatschools.org");
        _testUser.setId(1);

        _controller = new MySchoolListLoginController();
        _controller.setCommandClass(gs.web.community.LoginCommand.class);
        _controller.setFormView("/community/mySchoolListLogin");
        _controller.setSuccessView("/community/mySchoolList");
    }

    void authorizeUser() {
        try {
            _testUser.setPlaintextPassword("foobar");
            _testUser.setUserProfile(new UserProfile());
            PageHelper.setMemberAuthorized(getRequest(), getResponse(), _testUser);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        getRequest().setCookies(getResponse().getCookies());

        SessionContextUtil.getSessionContext(getRequest()).setUser(_testUser);
    }

    public void testRequestWithKnownUser() throws Exception {
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        sc.setMemberId(1);
        authorizeUser();
        getRequest().setMethod("GET");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        // for now, known users will not be redirected to the msl - instead, they will be
        // allowed to change identity on the login page.
        RedirectView view = (RedirectView) mAndV.getView();
        assertEquals("Expected msl view", "/mySchoolList.page", view.getUrl());
    }

    public void testRequestWithUnknownUser() throws Exception {
        getRequest().setMethod("GET");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        RedirectView view = (RedirectView) mAndV.getView();
        assertEquals("Expected login page", "/community/loginOrRegister.page?redirect=%2FmySchoolList.page", view.getUrl());
    }

    public void testSubmitWithCommandParameters() throws Exception {
        // Regression test for GS-7623
        SessionContext sc = SessionContextUtil.getSessionContext(getRequest());
        assertNull("Member id should not be set", sc.getUser());
        authorizeUser();
        getRequest().setMethod("POST");
        getRequest().setParameter("command", "add");
        getRequest().setParameter("ids", "8485");
        getRequest().setParameter("state", "CA");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        RedirectView view = (RedirectView) mAndV.getView();
        assertEquals("Expected msl view", "/mySchoolList.page?command=add&ids=8485&state=CA", view.getUrl());
    }
}
