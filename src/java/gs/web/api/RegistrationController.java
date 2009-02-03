package gs.web.api;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

import gs.data.api.IApiAccountDao;
import gs.web.util.ReadWriteController;

/**
 * @author chriskimm@greatschools.net
 */
@Controller
@RequestMapping("/api/registration.page")
public class RegistrationController {

    // Maps to the Spring form view
    public static final String FORM_VIEW_NAME = "api/registration";

    @Autowired
    private IApiAccountDao _apiAccountDao;

    @RequestMapping(method = RequestMethod.GET)
    public String showForm() {
        System.out.println ("_apiAccountDao: " + _apiAccountDao);
        return "api/registration";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String processRegistration() {
        return "api/registrationSuccess";
    }
}
