package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.util.DigestUtil;
import gs.data.geo.IGeoDao;
import gs.data.geo.ICity;
import gs.data.state.State;
import gs.web.util.ReadWriteController;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author <a href="mailto:aroy@urbanasoft.com">Anthony Roy</a>
 */
public class RegistrationController extends SimpleFormController implements ReadWriteController {
    public static final String BEAN_ID = "/community/registration.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private IGeoDao _geoDao;
    private JavaMailSender _mailSender;
    private boolean _requireEmailValidation = true;

    //set up defaults if none supplied
    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {

        UserCommand userCommand = (UserCommand) command;
        userCommand.setRedirectUrl(request.getParameter("redirect"));
        loadCityList(request, userCommand);

        if (StringUtils.isNotEmpty(userCommand.getEmail())) {
            User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());
            if (user != null && !user.isEmailValidated()) {
                // only allow setting the password on people with empty or provisional password
                // existing users have to authenticate and change account settings through other channels
                userCommand.setUser(user);
                // detach user from session so clearing the names has no effect
                getUserDao().evict(user);
                // clear first/last name for existing users
                userCommand.setFirstName(null);
                userCommand.setLastName(null);
                if (request.getParameter("reset") != null &&
                        request.getParameter("reset").equals("true") &&
                        user.isEmailProvisional()) {
                    // reset provisional status
                    user.setPasswordMd5(null);
                    _userDao.updateUser(user);
                }
            }
        }
    }

    public void onBind(HttpServletRequest request, Object command) {
        UserCommand userCommand = (UserCommand) command;
        userCommand.setRecontact(request.getParameter("recontact") != null);
        userCommand.setCity(request.getParameter("city"));
        loadCityList(request, userCommand);
    }

    protected void loadCityList(HttpServletRequest request, UserCommand userCommand) {
        State state = userCommand.getState();
        if (state == null) {
            state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        }
        List cities = _geoDao.findCitiesByState(state);
        userCommand.setCityList(cities);
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        UserCommand userCommand = (UserCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());

        boolean userExists = false;

        if (user != null) {
            userExists = true;
            // update the user's name if they specified a new one
            if (userCommand.getFirstName() != null && userCommand.getFirstName().length() > 0) {
                user.setFirstName(userCommand.getFirstName());
            }
            if (userCommand.getLastName() != null && userCommand.getLastName().length() > 0) {
                user.setLastName(userCommand.getLastName());
            }
            userCommand.setUser(user);
        } else {
            // only create the user if the user is new
            getUserDao().saveUser(userCommand.getUser());
        }

        try {
            userCommand.getUser().setPlaintextPassword(userCommand.getPassword());
            if (_requireEmailValidation) {
                // mark password as unauthenticated
                userCommand.getUser().setEmailProvisional();
            }
            getUserDao().updateUser(userCommand.getUser());
        } catch (Exception e) {
            _log.warn("Error setting password: " + e.getMessage());
            if (!userExists) {
                // for new users, cancel the account on error
                getUserDao().removeUser(userCommand.getUser().getId());
            }
            throw e;
        }

        if (_requireEmailValidation) {
            MimeMessage mm = RequestEmailValidationController.buildMultipartEmail
                    (_mailSender.createMimeMessage(), request, userCommand);
            try {
                _mailSender.send(mm);
            } catch (MailException me) {
                _log.error("Error sending email message.", me);
                if (userExists) {
                    // for existing users, set them back to no password
                    userCommand.getUser().setPasswordMd5(null);
                    getUserDao().updateUser(userCommand.getUser());
                } else {
                    // for new users, cancel the account on error
                    getUserDao().removeUser(userCommand.getUser().getId());
                }
                throw me;
            }
        }

        // gotten this far, now let's update their user profile
        UserProfile userProfile = userCommand.getUserProfile();

        userProfile.setUser(userCommand.getUser());
        userCommand.getUser().setUserProfile(userProfile);
        _userDao.updateUser(userCommand.getUser());

        ModelAndView mAndV = new ModelAndView();

        mAndV.setViewName(getSuccessView());
        FollowUpCommand fupCommand = new FollowUpCommand();
        fupCommand.setUser(userCommand.getUser());
        fupCommand.setUserProfile(userProfile);
        fupCommand.setRecontact(String.valueOf(userCommand.isRecontact()));

        // generate secure hash so if the followup profile page is submitted, we know who it is
        String hash = DigestUtil.hashStringInt(userCommand.getEmail(), userCommand.getUser().getId());
        mAndV.getModel().put("idString", hash);
        if (_requireEmailValidation) {
            mAndV.getModel().put("email", userCommand.getEmail());
        }
        mAndV.getModel().put("followUpCmd", fupCommand);
        return mAndV;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public JavaMailSender getMailSender() {
        return _mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        _mailSender = mailSender;
    }

    public boolean isRequireEmailValidation() {
        return _requireEmailValidation;
    }

    public void setRequireEmailValidation(boolean requireEmailValidation) {
        this._requireEmailValidation = requireEmailValidation;
    }
}
