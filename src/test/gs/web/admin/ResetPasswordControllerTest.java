package gs.web.admin;

import gs.web.BaseControllerTestCase;
import gs.data.community.IUserDao;
import gs.data.community.User;

import javax.servlet.http.HttpServletRequest;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Created by chriskimm@greatschools.net
 */
public class ResetPasswordControllerTest extends BaseControllerTestCase {

    private ResetPasswordController _controller;
    private IUserDao _userDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _controller = new ResetPasswordController();
        _userDao = createStrictMock(IUserDao.class);
        _controller.setUserDao(_userDao);
    }

    public void testHandleRequest() throws Exception {
        MockHttpServletResponse res = getResponse();
        String email = "tester@greatschools.net";
        getRequest().setParameter("email", email);

        User u = new User();
        u.setId(1);
        u.setEmail(email);
        expect(_userDao.findUserFromEmailIfExists(email)).andReturn(u);
        _userDao.saveUser(u);
        replay(_userDao);
        _controller.handleRequest(getRequest(), res);
        verify(_userDao);
        assertEquals("content type should be \"text/plain\"", "text/plain",
                res.getContentType());

        assertEquals("password changed for " + email, res.getContentAsString());
    }
}
