package gs.web.api;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;

import gs.data.api.IApiAccountDao;
import gs.web.util.ReadWriteController;

/**
 * @author chriskimm@greatschools.net
 */
public class RegistrationController extends SimpleFormController implements ReadWriteController {

    private IApiAccountDao _apiAccountDao;
    
    @Override
    protected ModelAndView onSubmit(Object o) throws Exception {
        RegistrationCommand command = (RegistrationCommand)o;
        return new ModelAndView(getSuccessView());
    }
}
