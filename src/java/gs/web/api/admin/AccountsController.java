package gs.web.api.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;

import gs.data.api.IApiAccountDao;

/**
 * Created by chriskimm@greatschools.net
 */
@Controller
@RequestMapping("/api/admin/accounts.page")
public class AccountsController {

    @Autowired
    private IApiAccountDao _apiAccountDao;

    public final static String ACCOUNTS_VIEW_NAME = "/api/admin/accounts";

    @RequestMapping(method = RequestMethod.GET)
    public String getPage(ModelMap model) {
        model.addAttribute("accounts", getApiAccountDao().getAllAccounts());
        return ACCOUNTS_VIEW_NAME;
    }

    IApiAccountDao getApiAccountDao() {
        return _apiAccountDao;
    }

    void setApiAccountDao(IApiAccountDao apiAccountDao) {
        _apiAccountDao = apiAccountDao;
    }
}
