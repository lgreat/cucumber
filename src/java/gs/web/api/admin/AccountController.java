package gs.web.api.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;

import gs.data.api.IApiAccountDao;
import gs.data.api.ApiAccount;
import gs.web.util.ReadWriteAnnotationController;

/**
 * @author chriskimm@greatschools.net
 */
@Controller
@RequestMapping("/api/admin/account.page")
public class AccountController implements ReadWriteAnnotationController {

    public static final String MAIN_VIEW = "api/admin/account";

    @SuppressWarnings({"SpringJavaAutowiringInspection"})
    @Autowired
    private IApiAccountDao _apiAccountDao;

    @RequestMapping(method = RequestMethod.GET)
    public String setupForm(@RequestParam("id") int id, ModelMap model) {
        ApiAccount account = getApiAccountDao().getAccountById(id);
        model.addAttribute("account", account);
        return MAIN_VIEW;
    }

    public IApiAccountDao getApiAccountDao() {
        return _apiAccountDao;
    }

    public void setApiAccountDao(IApiAccountDao apiAccountDao) {
        _apiAccountDao = apiAccountDao;
    }

}
