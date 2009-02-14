package gs.web.api.admin;

import gs.data.api.ApiAccount;
import gs.data.api.IApiAccountDao;
import org.springframework.ui.ModelMap;
import static org.easymock.EasyMock.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by chriskimm@greatschools.net
 */
public class AccountControllerTest {

    private AccountController _controller;
    private IApiAccountDao _apiAccountDao;

    @Before
    public void setUp() throws Exception {
        //super.setUp();
        _controller = new AccountController();
        _apiAccountDao = createMock(IApiAccountDao.class);
        _controller.setApiAccountDao(_apiAccountDao);
    }

    @Test
    public void getForm() throws Exception {
        expect(_apiAccountDao.getAccountById(1)).andReturn(new ApiAccount());
        replay(_apiAccountDao);
        ModelMap mm = new ModelMap();
        String view = _controller.showPage(1, mm);
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
        assertEquals(AccountController.MAIN_VIEW, view);
        assertNull(account.getApiKey());
    }
}
