package gs.web.api.admin;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.api.IApiAccountDao;

/**
 * Created by chriskimm@greatschools.net
 */
public class AccountsController extends AbstractController {

    /** Spring bean id */
    public static final String BEAN_NAME = "/api/admin/accounts.page";

    private IApiAccountDao _apiAccountDao;
    private final static String ACCOUNTS_VIEW_NAME = "/api/admin/accounts";

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        ModelAndView mAndV = new ModelAndView();
        mAndV.setViewName(ACCOUNTS_VIEW_NAME);
        mAndV.getModelMap().addAttribute("accounts", _apiAccountDao.getAllAccounts());
        return mAndV;
    }

    public IApiAccountDao getApiAccountDao() {
        return _apiAccountDao;
    }

    public void setApiAccountDao(IApiAccountDao apiAccountDao) {
        _apiAccountDao = apiAccountDao;
    }
}
