package gs.web.community.registration;

import gs.data.community.ISubscriptionDao;
import gs.data.community.IUserDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by IntelliJ IDEA.
 * User: UrbanaSoft
 * Date: Jun 15, 2006
 * Time: 9:21:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class RegistrationController extends SimpleFormController {
    public static final String BEAN_ID = "/community/registration.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        UserCommand userCommand = (UserCommand) command;

        getUserDao().saveUser(userCommand.getUser());

        try {
            userCommand.getUser().setPlaintextPassword(userCommand.getPassword());
            getUserDao().updateUser(userCommand.getUser());
        } catch (Exception e) {
            _log.warn("Error setting password: " + e.getMessage());
            getUserDao().removeUser(userCommand.getUser().getId());
            throw e;
        }

        ModelAndView mAndV = new ModelAndView();

        mAndV.setViewName(getSuccessView());
        return mAndV;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }


}
