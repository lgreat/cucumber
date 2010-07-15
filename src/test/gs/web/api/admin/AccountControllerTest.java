package gs.web.api.admin;

import gs.data.api.ApiAccount;
import gs.data.api.IApiAccountDao;
import gs.data.util.email.MockJavaMailSender;
import gs.data.util.email.EmailHelperFactory;
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
        String view = _controller.toggleActive(123, new ModelMap());
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
        String view = _controller.toggleActive(123, new ModelMap());
        verify(_apiAccountDao);
        assertEquals(AccountController.MAIN_VIEW, view);
        assertNull(account.getApiKey());
    }

    @Test
    public void updatePremiumOptions() {
        ApiAccount account = new ApiAccount();
        account.setId(123);
        account.setName("foo");
        account.setEmail("foo@bar.com");
        account.setApiKey("blah");
        expect(_apiAccountDao.getAccountById(123)).andReturn(account);
        _apiAccountDao.save(account);
        replay(_apiAccountDao);
        _controller.update(account, 123, new ModelMap());
        verify(_apiAccountDao);
    }
}
