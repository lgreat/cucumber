package gs.web.school;

import gs.data.community.User;
import gs.data.school.EspMembership;
import gs.data.school.IEspMembershipDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.security.Role;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;

@Controller
@RequestMapping("/school/esp/dashboard.page")
public class EspDashboardController {
    public static final String VIEW = "school/espDashboard";

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @Autowired
    private ISchoolDao _schoolDao;

    @RequestMapping(method = RequestMethod.GET)
    public String showLandingPage(ModelMap modelMap, HttpServletRequest request) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();

        if (checkUserHasAccess(modelMap, user)) {
            return VIEW;
        }

        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_SIGN_IN);
        return "redirect:" + urlBuilder.asFullUrl(request);
    }

    protected boolean checkUserHasAccess(ModelMap modelMap, User user) {

        if (user != null && (user.hasRole(Role.ESP_MEMBER) || user.hasRole(Role.ESP_SUPERUSER))) {
            List<EspMembership> espMemberships = getEspMembershipDao().findEspMembershipsByUserId(user.getId(), true);

            if (!espMemberships.isEmpty()) {
                //Take the user to the first active school.
                EspMembership espMembership = espMemberships.get(0);
                School school = getSchoolDao().getSchoolById(espMembership.getState(), espMembership.getSchoolId());
                if (school != null && school.isActive()) {
                    espMembership.setSchool(school);
                    modelMap.put("espMembership", espMembership);
                    modelMap.put("espSuperuser", user.hasRole(Role.ESP_SUPERUSER));
                    //Get the information about who else has ESP access to this school
                    getOtherEspMembersForSchool(school,user, modelMap);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get the information about who else has ESP access to the same school as the user
     *
     * @param school
     * @param user
     * @param modelMap
     */
    protected void getOtherEspMembersForSchool(School school, User user, ModelMap modelMap) {
        List<EspMembership> espMemberships = getEspMembershipDao().findEspMembershipsBySchool(school, true);
        if (espMemberships != null && !espMemberships.isEmpty() && user != null && user.getId() != null) {
            Iterator<EspMembership> iter = espMemberships.iterator();
            //remove the current user from the list.
            while (iter.hasNext()) {
                if (iter.next().getUser().getId() == user.getId()) {
                    iter.remove();
                }
            }
            modelMap.put("otherEspMemberships", espMemberships);
        }
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