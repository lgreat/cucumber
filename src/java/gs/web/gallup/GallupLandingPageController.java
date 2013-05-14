package gs.web.gallup;

/**
 * Created with IntelliJ IDEA.
 * User: sarora  -
 * Date: 5/10/13
 * Time: 3:52 PM
 * To change this template use File | Settings | File Templates.
 */


import gs.data.gallup.*;
import java.util.ArrayList;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author sarora@greatschools.org Shomi Arora.
 */

@Controller
@RequestMapping("/gallup/gallupLanding.page")
public class GallupLandingPageController {

    @Autowired
    private GallupSchoolsDao _gallupDao;

    @RequestMapping(method= RequestMethod.GET)
    public String handleRequestInternal(HttpServletRequest request, Model model) throws Exception {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        final ArrayList<GallupSchools> gallupSchools= _gallupDao.findGallupSchoolsList();
        model.addAttribute("gallupSchools",gallupSchools);
        return "/gallup/gallupLanding";
    }
}
