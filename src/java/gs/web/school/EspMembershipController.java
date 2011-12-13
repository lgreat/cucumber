package gs.web.school;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.WelcomeMessageStatus;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.school.EspMembershipStatus;
import gs.data.school.IEspMembershipDao;
import gs.data.school.EspMembership;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;

@Controller
@RequestMapping("/school/esp/**")
public class EspMembershipController implements ReadWriteAnnotationController {
    private static final Log _log = LogFactory.getLog(EspMembershipController.class);

    public static final String FORM_VIEW = "school/espMembershipForm";
    public static final String SUCCESS_VIEW = "school/espMembershipSuccess";

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @Autowired
    private IUserDao _userDao;

    @RequestMapping(value = "/school/esp/form.page", method = RequestMethod.GET)
    public String showForm(ModelMap modelMap) {
        EspMembershipCommand command = new EspMembershipCommand();
        modelMap.addAttribute("schoolEspCommand", command);
        return FORM_VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String createEspMembership(@ModelAttribute("schoolEspCommand") EspMembershipCommand command,
                                   BindingResult result,
                                   HttpServletResponse response) {
        try {
            User user = getUserDao().findUserFromEmailIfExists(command.getEmail().trim());

            if (user == null) {
                //Todo - this code is in registration controller.Do not replicate here
                user = new User();
                user.setEmail(command.getEmail());
                user.setFirstName(command.getFirstName());
                user.setLastName(command.getLastName());
                UserProfile userProfile = new UserProfile();
                userProfile.setScreenName(command.getUserName());
                user.setUserProfile(userProfile);
                user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                user.setHow("esp");
                getUserDao().saveUser(user);
                ThreadLocalTransactionManager.commitOrRollback();
                user.setPlaintextPassword(command.getPassword());
                getUserDao().saveUser(user);
            }

            EspMembership esp = new EspMembership();
            esp.setIsActive(true);
            esp.setJobTitle(command.getJobTitle());
            esp.setState(command.getState());
            esp.setSchoolId(command.getSchoolId());
            esp.setStatus(EspMembershipStatus.PROCESSING);
            esp.setUser(user);
            esp.setWebUrl(command.getWebPageUrl());
            getEspMembershipDao().saveEspMembership(esp);

        } catch (NoSuchAlgorithmException algorithmExp) {
            _log.debug(algorithmExp);
            return FORM_VIEW;

        } catch (Exception exception) {
            _log.debug(exception);
            return FORM_VIEW;
        }

        return SUCCESS_VIEW;
    }

    public IEspMembershipDao getEspMembershipDao() {
        return _espMembershipDao;
    }

    public void setEspMembershipDao(IEspMembershipDao espMembershipDao) {
        _espMembershipDao = espMembershipDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}