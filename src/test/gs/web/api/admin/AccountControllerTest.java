package gs.web.api.admin;

import gs.web.BaseControllerTestCase;
import gs.data.api.ApiAccount;
import gs.data.api.IApiAccountDao;
import org.springframework.ui.ModelMap;
import static org.easymock.EasyMock.*;

/**
 * Created by chriskimm@greatschools.net
 */
public class AccountControllerTest extends BaseControllerTestCase {

    private AccountController _controller;
    private IApiAccountDao _apiAccountDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _controller = new AccountController();
        _apiAccountDao = createMock(IApiAccountDao.class);
        _controller.setApiAccountDao(_apiAccountDao);
    }

    public void testGetForm() throws Exception {
        getRequest().setMethod("GET");
        expect(_apiAccountDao.getAccountById(1)).andReturn(new ApiAccount());
        replay(_apiAccountDao);
        ModelMap mm = new ModelMap();
        String view = _controller.setupForm(1, mm);
        assertEquals(AccountController.MAIN_VIEW, view);
        verify(_apiAccountDao);
    }
}
