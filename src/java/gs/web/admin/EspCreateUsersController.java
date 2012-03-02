package gs.web.admin;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.WelcomeMessageStatus;
import gs.data.school.*;
import gs.data.state.State;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Controller
@RequestMapping("/admin/createEspUsers.page")
public class EspCreateUsersController implements ReadWriteAnnotationController {

    public static final String VIEW = "admin/espCreateUsers";

    @Autowired
    private IUserDao _userDao;

    @Autowired
    private ISchoolDao _schoolDao;

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @RequestMapping(method = RequestMethod.GET)
    public String display(ModelMap modelMap, HttpServletRequest request) {
        return VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public void createUser(HttpServletRequest request, HttpServletResponse response) {
        String email = request.getParameter("email");
        String stateStr = request.getParameter("state");
        String schoolIdStr = request.getParameter("schoolId");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String jobTitle = request.getParameter("jobTitle");

        if (StringUtils.isNotBlank(email) && StringUtils.isNotBlank(stateStr) && StringUtils.isNotBlank(schoolIdStr)) {
            email = email.trim();
            stateStr = stateStr.trim();
            schoolIdStr = schoolIdStr.trim();
            State state = State.fromString(stateStr);
            if (state != null) {
                School school = _schoolDao.getSchoolById(state, new Integer(schoolIdStr));
                if (school != null) {
                    User user = _userDao.findUserFromEmailIfExists(email);
                    if (user == null) {
                        user = new User();
                        user.setEmail(email);
                        user.setFirstName(StringUtils.isNotBlank(firstName) ? firstName : null);
                        user.setLastName(StringUtils.isNotBlank(lastName) ? lastName : null);
                        user.setHow("esp_pre_approved");
                        user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                        _userDao.saveUser(user);
                    }
                    saveEspMembership(user, state, school, jobTitle);
                }

            }
        }
    }

    protected void saveEspMembership(User user, State state, School school, String jobTitle) {
        if (state != null && school != null && school.getId() != null && school.getId() > 0 && user != null
                && user.getId() != null) {

            EspMembership espMembership = _espMembershipDao.findEspMembershipByStateSchoolIdUserId(state, school.getId(), user.getId(), false);

            if (espMembership == null) {
                EspMembership esp = new EspMembership();
                esp.setActive(false);
                esp.setJobTitle(StringUtils.isNotBlank(jobTitle) ? jobTitle : null);
                esp.setState(state);
                esp.setSchoolId(school.getId());
                esp.setStatus(EspMembershipStatus.PRE_APPROVED);
                esp.setUser(user);
                _espMembershipDao.saveEspMembership(esp);
            } else if ((espMembership.getStatus().equals(EspMembershipStatus.DISABLED) ||
                    espMembership.getStatus().equals(EspMembershipStatus.REJECTED) ||
                    espMembership.getStatus().equals(EspMembershipStatus.PROCESSING)) && !espMembership.getActive()) {
                espMembership.setStatus(EspMembershipStatus.PRE_APPROVED);
                espMembership.setUpdated(new Date());
                _espMembershipDao.saveEspMembership(espMembership);
            }
        }
    }

}