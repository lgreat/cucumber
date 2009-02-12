package gs.web.api.admin;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;

import gs.data.api.IApiAccountDao;
import gs.data.api.ApiAccount;
import gs.web.util.ReadWriteController;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

/**
 * @author chriskimm@greatschools.net
 */
public class AccountController extends SimpleFormController implements ReadWriteController {

    public static final String MESSAGES = "messages";

    private IApiAccountDao _apiAccountDao;
    private static final Logger _log = Logger.getLogger("gs.web.api.admin.AccountController");


    /**
     @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        String accountId = request.getParameter("id");
        try {
            Integer id = Integer.valueOf(accountId);
            return _apiAccountDao.getAccountById(id);
        } catch (NumberFormatException nfe) {
            _log.warning(nfe.toString());
            return null;
        }
    }
     */

    @Override
    protected ModelAndView onSubmit(Object o) throws Exception {
        ApiAccount command = (ApiAccount)o;
        _apiAccountDao.save(command);
        return new ModelAndView(getSuccessView());
    }

    public IApiAccountDao getApiAccountDao() {
        return _apiAccountDao;
    }

    public void setApiAccountDao(IApiAccountDao apiAccountDao) {
        _apiAccountDao = apiAccountDao;
    }
}
