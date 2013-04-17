package gs.web.community;


import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/community/facebooktest.page")
public class FacebookTestController {

    @RequestMapping(method=RequestMethod.GET)
    public String handleGet(ModelMap model) {
        return "community/facebooktest";
    }
}
