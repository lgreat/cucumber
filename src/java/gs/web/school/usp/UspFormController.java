package gs.web.school.usp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 4/25/13
 * Time: 4:22 PM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/school/usp/")
public class UspFormController {
    public static final String FORM_VIEW = "/school/usp/uspForm";

    @RequestMapping(value = "form.page", method = RequestMethod.GET)
    public String showRegistrationForm (HttpServletRequest request,
                                        HttpServletResponse response) {
        return FORM_VIEW;
    }
}
