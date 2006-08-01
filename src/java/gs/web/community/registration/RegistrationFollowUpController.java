package gs.web.community.registration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import gs.data.community.IUserDao;
import gs.data.community.IUserProfileDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.util.DigestUtil;
import gs.web.util.ReadWriteController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationFollowUpController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/community/registrationSuccess.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private IUserProfileDao _userProfileDao;

    /**
     * this method is called after validation but before submit.
     */
    protected void onBindAndValidate(HttpServletRequest request,
                                     Object command,
                                     BindException errors) throws NoSuchAlgorithmException {
        if (errors.hasErrors()) {
            return;
        }
        FollowUpCommand fupCommand = (FollowUpCommand)command;
        Integer userId = fupCommand.getUser().getId();
        User user = _userDao.findUserFromId(userId.intValue());
        fupCommand.setUser(user);

        String hash = request.getParameter("marker");
        String realHash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        if (!realHash.equals(hash)) {
            _log.warn("Registration follow-up request with invalid hash: " + hash);
            errors.rejectValue("id", "bad_hash",
                    "We're sorry, we cannot validate your request at this time. Once " +
                            "your account is validated, please update your profile again.");
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        ModelAndView mAndV = new ModelAndView();

        FollowUpCommand fupCommand = (FollowUpCommand)command;
        User user = fupCommand.getUser();
        UserProfile profile = fupCommand.getUserProfile();
        // get existing profile
        UserProfile existingProfile = _userProfileDao.findUserProfileFromId(user.getId());
        // update existing profile with new information
        existingProfile.setAboutMe(profile.getAboutMe());
        existingProfile.setPrivate(profile.isPrivate());
        // save
        _userProfileDao.updateUserProfile(existingProfile);

        mAndV.setViewName(getSuccessView());
        mAndV.getModel().put("name", user.getFirstName());
        return mAndV;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public IUserProfileDao getUserProfileDao() {
        return _userProfileDao;
    }

    public void setUserProfileDao(IUserProfileDao userProfileDao) {
        _userProfileDao = userProfileDao;
    }

}
