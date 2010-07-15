package gs.web.api.admin;

import gs.data.api.ApiAccount;
import gs.data.api.IApiAccountDao;
import gs.web.util.ReadWriteController;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api/admin/deleteAccount.page")
public class DeleteAccountController extends AbstractController implements ReadWriteController {
    public final static String ACCOUNTS_URI = "/api/admin/accounts.page";
    public final static String ACCOUNT_URI = "/api/admin/account.page";

    @Autowired
    private IApiAccountDao _apiAccountDao;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        Integer id = null;
        try {
            String idParam = request.getParameter("id");
            if (StringUtils.isNotBlank(idParam)) {
                id = Integer.parseInt(idParam);
            }
        } catch (Exception e) {
            // do nothing
        }
        if (id == null) {
            return new ModelAndView(new RedirectView(ACCOUNTS_URI));
        }

        ApiAccount account = getApiAccountDao().getAccountById(id);

        // prevent accidental deletes by verifying email address
        if (account.getEmail() != null && account.getEmail().equals(request.getParameter("email"))) {
            String name = account.getName();
            String email = account.getEmail();

            getApiAccountDao().deleteAccountById(id);

            return new ModelAndView(new RedirectView(ACCOUNTS_URI +
                    "?deletedAccountName=" + UrlUtil.urlEncode(name) +
                    "&deletedAccountEmail=" + UrlUtil.urlEncode(email)));
        } else {
            return new ModelAndView(new RedirectView(ACCOUNT_URI + "?id=" + id));
        }
    }

    protected IApiAccountDao getApiAccountDao() {
        return _apiAccountDao;
    }

    protected void setApiAccountDao(IApiAccountDao apiAccountDao) {
        _apiAccountDao = apiAccountDao;
    }
}
