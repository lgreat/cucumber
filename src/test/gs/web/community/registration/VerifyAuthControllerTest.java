package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import org.easymock.MockControl;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.apache.commons.lang.StringUtils;

import java.security.NoSuchAlgorithmException;
import java.io.IOException;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class VerifyAuthControllerTest extends BaseControllerTestCase {
    private VerifyAuthController _controller;
    private MockControl _userControl;
    private IUserDao _mockUserDao;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _userControl = MockControl.createControl(IUserDao.class);
        _controller = (VerifyAuthController) appContext.getBean(VerifyAuthController.BEAN_ID);
        _mockUserDao = (IUserDao)_userControl.getMock();
        _controller.setUserDao(_mockUserDao);
    }

    public void testVerifyAuth() throws NoSuchAlgorithmException, IOException {
        User user = new User();
        user.setId(new Integer(99));
        user.setEmail("testVerifyAuth@greatschools.net");

        _mockUserDao.findUserFromId(user.getId().intValue());
        _userControl.setReturnValue(user);
        _userControl.replay();

        AuthenticationManager.AuthInfo authInfo = _controller.getAuthenticationManager().generateAuthInfo(user);

        getRequest().addParameter(_controller.getAuthenticationManager().getParameterName(),
                _controller.getAuthenticationManager().getParameterValue(authInfo));

        _controller.handleRequest(getRequest(), getResponse());

        _userControl.verify();

        assertTrue(getResponse().getStatus() == 200);
        assertTrue(getResponse().getContentAsString().equals(String.valueOf(user.getId())));
    }

    public void testVerifyFail() throws NoSuchAlgorithmException, IOException {
        User user = new User();
        user.setId(new Integer(99));
        user.setEmail("testVerifyAuth2@greatschools.net");

        User user2 = new User();
        user2.setId(new Integer(98));
        user2.setEmail("testVerifyAuth3@greatschools.net");

        _mockUserDao.findUserFromId(98);
        _userControl.setReturnValue(user2);
        _userControl.replay();

        AuthenticationManager.AuthInfo authInfo = _controller.getAuthenticationManager().generateAuthInfo(user);

        String paramValue = _controller.getAuthenticationManager().getParameterValue(authInfo);
        // spoof another user id
        paramValue = paramValue.substring(0, DigestUtil.MD5_HASH_LENGTH) +
                StringUtils.leftPad("98", _controller.getAuthenticationManager().getUserIdLength(), '0') +
                paramValue.substring(DigestUtil.MD5_HASH_LENGTH+10);
        getRequest().addParameter(_controller.getAuthenticationManager().getParameterName(), paramValue);

        _controller.handleRequest(getRequest(), getResponse());

        _userControl.verify();

        assertTrue(getResponse().getStatus() == 403);
    }

    public void testFindUserError() throws NoSuchAlgorithmException, IOException {
        User user = new User();
        user.setId(new Integer(99));
        user.setEmail("testVerifyAuth4@greatschools.net");

        _mockUserDao.findUserFromId(98);
        _userControl.setThrowable(new ObjectRetrievalFailureException("Can't find user", null));
        _userControl.replay();

        AuthenticationManager.AuthInfo authInfo = _controller.getAuthenticationManager().generateAuthInfo(user);

        String paramValue = _controller.getAuthenticationManager().getParameterValue(authInfo);
        // spoof another user id
        paramValue = paramValue.substring(0, DigestUtil.MD5_HASH_LENGTH) +
                StringUtils.leftPad("98", _controller.getAuthenticationManager().getUserIdLength(), '0') +
                paramValue.substring(DigestUtil.MD5_HASH_LENGTH+10);
        getRequest().addParameter(_controller.getAuthenticationManager().getParameterName(), paramValue);

        _controller.handleRequest(getRequest(), getResponse());

        _userControl.verify();

        assertTrue(getResponse().getStatus() == 403);
    }

    public void testMissingParameter() throws NoSuchAlgorithmException, IOException {
        _controller.handleRequest(getRequest(), getResponse());

        assertTrue(getResponse().getStatus() == 403);
    }


    public void testMangledParameter() throws NoSuchAlgorithmException, IOException {
        getRequest().addParameter(_controller.getAuthenticationManager().getParameterName(), "blahblah");
        _controller.handleRequest(getRequest(), getResponse());

        assertTrue(getResponse().getStatus() == 403);
    }
}
