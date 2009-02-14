package gs.web.api.admin;

import static junit.framework.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import gs.data.api.IApiAccountDao;
import gs.data.api.ApiAccount;
import static org.easymock.EasyMock.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by chriskimm@greatschools.net
 */
public class AccountsControllerTest {

    private AccountsController _controller;
    private IApiAccountDao _mockApiAccountDao;

    @Before
    public void setup() {
        _controller = new AccountsController();
        _mockApiAccountDao = createMock(IApiAccountDao.class);
        _controller.setApiAccountDao(_mockApiAccountDao);
    }

    @Test
    public void testGetPage() {
        ModelMap model = new ModelMap();

        List<ApiAccount> accounts = new ArrayList<ApiAccount>();
        accounts.add(new ApiAccount());
        expect(_mockApiAccountDao.getAllAccounts()).andReturn(accounts);
        replay(_mockApiAccountDao);
        String s = _controller.getPage(model);
        assertEquals(AccountsController.ACCOUNTS_VIEW_NAME, s);
        verify(_mockApiAccountDao);
    }
}
