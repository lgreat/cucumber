package gs.web.api.admin;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;

import gs.data.api.IApiAccountDao;
import gs.data.api.ApiAccount;
import gs.web.util.ReadWriteController;

/**
 * @author chriskimm@greatschools.net
 */
public class AccountController extends SimpleFormController implements ReadWriteController {

    private IApiAccountDao _apiAccountDao;

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
