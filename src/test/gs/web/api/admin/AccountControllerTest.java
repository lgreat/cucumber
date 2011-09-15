package gs.web.api.admin;

import gs.data.api.ApiAccount;
import gs.data.api.IApiAccountDao;
import gs.data.util.email.MockJavaMailSender;
import gs.data.util.email.EmailHelperFactory;
import gs.web.GsMockHttpServletRequest;
import gs.web.api.ApiAccountCommandValidator;
import gs.web.request.HostnameInfo;
import org.springframework.ui.ModelMap;
import static org.easymock.EasyMock.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.List;

/**
 * Created by chriskimm@greatschools.org
 */
public class AccountControllerTest {

    private AccountController _controller;
    private IApiAccountDao _apiAccountDao;
    private MockJavaMailSender _javaMailSender;
    private GsMockHttpServletRequest _request;

    @Before
    public void setUp() throws Exception {
        //super.setUp();
        _controller = new AccountController();
        _apiAccountDao = createMock(IApiAccountDao.class);
        _controller.setApiAccountDao(_apiAccountDao);
        EmailHelperFactory _factory = new EmailHelperFactory();
        _javaMailSender = new MockJavaMailSender();
        _javaMailSender.setHost("greatschools.org");
        _factory.setMailSender(_javaMailSender);
        _controller.setEmailHelperFactory(_factory);
        _controller.setApiAccountValidator(new ApiAccountCommandValidator());

        _request = new GsMockHttpServletRequest();
        HostnameInfo hostnameInfo = new HostnameInfo("www.greatschools.org");
        _request.setAttribute(HostnameInfo.REQUEST_ATTRIBUTE_NAME, hostnameInfo);
        _request.setServerName("www.greatschools.org");

    }

    @Test
    public void getForm() throws Exception {
        expect(_apiAccountDao.getAccountById(1)).andReturn(new ApiAccount());
        replay(_apiAccountDao);
        ModelMap mm = new ModelMap();
        String view = _controller.viewEditAccount(1, mm);
        assertEquals(AccountController.MAIN_VIEW, view);
        verify(_apiAccountDao);
    }

    @Test
    public void addKey() throws Exception {
        ApiAccount account = new ApiAccount();
        account.setId(123);
        account.setName("foo");
        account.setEmail("foo@bar.com");
        expect(_apiAccountDao.getAccountById(123)).andReturn(account);
        _apiAccountDao.save(account);
        replay(_apiAccountDao);
        assertNull(account.getApiKey());
        String view = _controller.toggleActive(_request, 123, new ModelMap());
        assertEquals(AccountController.MAIN_VIEW, view);
        assertNotNull(account.getApiKey());
        
        List messages = _javaMailSender.getSentMessages();
        assertEquals(1, messages.size());
    }

    @Test
    public void removeKey() {
        ApiAccount account = new ApiAccount();
        account.setId(123);
        account.setName("foo");
        account.setEmail("foo@bar.com");
        account.setApiKey("blah");
        expect(_apiAccountDao.getAccountById(123)).andReturn(account);
        _apiAccountDao.save(account);
        replay(_apiAccountDao);
        assertNotNull(account.getApiKey());
        String view = _controller.toggleActive(_request, 123, new ModelMap());
        verify(_apiAccountDao);
        assertEquals(AccountController.MAIN_VIEW, view);
        assertNull(account.getApiKey());
    }

    // Uncomment following for live email send tests
//    private ApplicationContext getApplicationContext() {
//        String[] paths = {"applicationContext.xml",
//                          "modules-servlet.xml",
//                          "pages-servlet.xml"
//        };
//        return new ClassPathXmlApplicationContext(paths, gs.data.util.SpringUtil.getApplicationContext());
//    }
//
//
//    @Test
//    public void sendKeyEmail() throws IOException, MessagingException {
//        AccountController controller = new AccountController();
//        controller.setEmailHelperFactory((EmailHelperFactory) getApplicationContext().getBean(EmailHelperFactory.BEAN_ID));
//        ApiAccount account = new ApiAccount();
//        account.setId(123);
//        account.setEmail("aroy@greatschools.org");
//        account.setApiKey("123asldjf179");
//        controller.sendKeyEmail(_request, account);
//    }
}
