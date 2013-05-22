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
import java.util.Collections;
import java.util.Comparator;
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
        sortResult(gallupSchools);
        model.addAttribute("gallupSchools",gallupSchools);
        return "/gallup/gallupLanding";
    }

    private void sortResult(ArrayList<GallupSchools> gallupSchools) {
        Collections.sort(gallupSchools, new Comparator<GallupSchools>() {
            public int compare(GallupSchools s1, GallupSchools s2) {
                 String state1 = s1.getState();
                String state2 = s2.getState();
                String schoolName1 = s1.getSchoolName();
                String schoolName2 = s2.getSchoolName();
                int stateCompare = state1.compareToIgnoreCase(state2);
                int valueToReturn;
                if (stateCompare != 0) {
                    valueToReturn = stateCompare;
                } else {
                    valueToReturn = schoolName1.compareToIgnoreCase(schoolName2);

                }
                return valueToReturn;
            }

        });
    }
}
