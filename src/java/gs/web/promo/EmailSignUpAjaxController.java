package gs.web.promo;

import gs.data.community.*;
import gs.data.promo.ILeadGenDao;
import gs.data.promo.LeadGen;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.EmailValidator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Young Fan <mailto:yfan@greatschools.org>
 */
@org.springframework.stereotype.Controller
public class EmailSignUpAjaxController implements ReadWriteAnnotationController {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String SUCCESS = "OK";
    public static final String FAILURE = "0";

    private ISubscriptionDao _subscriptionDao;
    private IUserDao _userDao;

    @RequestMapping(value = "/promo/emailSignUpAjax.page", method = RequestMethod.POST)
    public void generateLead(@ModelAttribute("command") EmailSignUpCommand command,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {
        _log.info(command.toString());

        String errors = validate(command);
        if (StringUtils.isBlank(errors)) {
            // log data
            handleEmailSignUp(command);

            response.getWriter().print(SUCCESS);
            return;
        }

        _log.warn("Failure generating email sign-up for: " + command.getEmail());
        response.getWriter().print(errors);
    }

    /**
     * Returns empty string if the command seems valid; otherwise, comma-separated list of fields with errors
     */
    protected String validate(EmailSignUpCommand command) {
        List<String> errorList = new ArrayList<String>();

        // validate not null email
        if (StringUtils.isBlank(command.getEmail())) {
            errorList.add("emailInvalid");
        } else {
            // validate format email
            EmailValidator emailValidator = EmailValidator.getInstance();
            if (!emailValidator.isValid(command.getEmail())) {
                _log.warn("Email Sign Up submitted with invalid email: " + command.getEmail());
                errorList.add("emailInvalid");
            } else {
                User user = _userDao.findUserFromEmailIfExists(command.getEmail());
                if (user != null &&
                    _subscriptionDao.isUserSubscribed(user, SubscriptionProduct.PARENT_ADVISOR, null)) {
                    errorList.add("emailAlreadySignedUp");
                }
            }
        }

        return StringUtils.join(errorList, ',');
    }

    private void handleEmailSignUp(EmailSignUpCommand command) {
        User user = _userDao.findUserFromEmailIfExists(command.getEmail());

        // If the user does not yet exist, add to list_member
        if (user == null) {
            user = new User();
            user.setEmail(command.getEmail());
            user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
            _userDao.saveUser(user);
        }

        SubscriptionProduct prod = SubscriptionProduct.PARENT_ADVISOR;
        if (!_subscriptionDao.isUserSubscribed(user, prod, null)) {
            // save new subscription
            Subscription sub = new Subscription();
            sub.setProduct(prod);
            sub.setUser(user);
            _subscriptionDao.saveSubscription(sub);
        } else {
            // user is already subscribed
        }
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}

