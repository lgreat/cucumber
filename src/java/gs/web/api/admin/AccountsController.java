package gs.web.api.admin;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.api.IApiAccountDao;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by chriskimm@greatschools.net
 */
public class AccountsController extends AbstractController {

    /** Spring bean id */
    public static final String BEAN_NAME = "/api/admin/accounts.page";

    private IApiAccountDao _apiAccountDao;
    private final static String ACCOUNTS_VIEW_NAME = "/api/admin/accounts";

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("accounts", _apiAccountDao.getAllAccounts());
        return new ModelAndView(ACCOUNTS_VIEW_NAME, model);
    }

    public IApiAccountDao getApiAccountDao() {
        return _apiAccountDao;
    }

    public void setApiAccountDao(IApiAccountDao apiAccountDao) {
        _apiAccountDao = apiAccountDao;
    }
}
