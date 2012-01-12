package gs.web.school;

import gs.data.community.User;
import gs.data.school.EspMembership;
import gs.data.school.IEspMembershipDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.security.Role;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/school/esp/")
public class EspController {
    public static final String SIGN_IN_PAGE = "/school/esp/signIn.page";
    public static final String VIEW = "school/espLanding";

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @Autowired
    private ISchoolDao _schoolDao;

    @RequestMapping(value = "landing.page", method = RequestMethod.GET)
    public String showLandingPage(ModelMap modelMap, HttpServletRequest request) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();

        boolean userHasESPAccess = checkUserHasAccess(modelMap, user, request);

        if (userHasESPAccess) {
            return VIEW;
        }

        return "redirect:" + SIGN_IN_PAGE;
    }

    protected boolean checkUserHasAccess(ModelMap modelMap, User user, HttpServletRequest request) {

        if (user != null && user.hasRole(Role.ESP_MEMBER)) {

            List<EspMembership> espMemberships = getEspMembershipDao().findEspMembershipsByUserId(user.getId(), true);

            if (!espMemberships.isEmpty()) {

                //Take the user to the first active school.
                EspMembership espMembership = espMemberships.get(0);
                School school = getSchoolDao().getSchoolById(espMembership.getState(), espMembership.getSchoolId());
                if (school != null) {
                    espMembership.setSchool(school);
                    modelMap.put("espMembership", espMembership);
                }
                return true;
            }
        }
        return false;
    }


    public IEspMembershipDao getEspMembershipDao() {
        return _espMembershipDao;
    }

    public void setEspMembershipDao(IEspMembershipDao espMembershipDao) {
        _espMembershipDao = espMembershipDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }
}