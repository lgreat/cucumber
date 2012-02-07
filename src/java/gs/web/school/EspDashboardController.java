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

/**
 * The request mapping does not have a trailing slash.This handles the url with and without a trailing slash.
 */
@Controller
@RequestMapping("/official-school-profile/dashboard")
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

        if (user != null) {
            EspMembership espMembership = getEspMembershipForUser(user);
            if (espMembership != null) {
                School school = getSchool(espMembership);
                if (school != null) {

                    //set the school on the espMembership
                    espMembership.setSchool(school);
                    modelMap.put("espMembership", espMembership);
                    modelMap.put("espSuperuser", user.hasRole(Role.ESP_SUPERUSER));

                    //Get the information about who else has ESP access to this school
                    List<EspMembership> otherEspMemberships = getOtherEspMembersForSchool(school, user);
                    if (otherEspMemberships != null && !otherEspMemberships.isEmpty()) {
                        modelMap.put("otherEspMemberships", otherEspMemberships);
                    }

                    return VIEW;
                }
            } else if (user.hasRole(Role.ESP_SUPERUSER)) {
                modelMap.put("espSuperuser", true);
                return VIEW;
            }
        }

        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_SIGN_IN);
        return "redirect:" + urlBuilder.asFullUrl(request);
    }

    /**
     * Get the esp membership for the user.
     *
     * @param user
     */

    protected EspMembership getEspMembershipForUser(User user) {
        if (user != null && (user.hasRole(Role.ESP_MEMBER) || user.hasRole(Role.ESP_SUPERUSER))) {
            List<EspMembership> espMemberships = getEspMembershipDao().findEspMembershipsByUserId(user.getId(), true);
            if (!espMemberships.isEmpty()) {
                //Take the user to the first active school.
                return espMemberships.get(0);
            }
        }
        return null;
    }

    /**
     * Get the school for the esp membership for the user.
     *
     * @param espMembership
     */
    protected School getSchool(EspMembership espMembership) {
        School school = getSchoolDao().getSchoolById(espMembership.getState(), espMembership.getSchoolId());
        if (school != null && school.isActive()) {
            return school;
        }
        return null;
    }

    /**
     * Get the information about who else has ESP access to the same school as the user
     *
     * @param school
     * @param user
     */
    protected List<EspMembership> getOtherEspMembersForSchool(School school, User user) {
        List<EspMembership> espMemberships = getEspMembershipDao().findEspMembershipsBySchool(school, true);
        if (espMemberships != null && !espMemberships.isEmpty() && user != null && user.getId() != null) {
            Iterator<EspMembership> iter = espMemberships.iterator();
            //remove the current user from the list.
            while (iter.hasNext()) {
                if (iter.next().getUser().getId() == user.getId()) {
                    iter.remove();
                }
            }
        }
        return espMemberships;
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