package gs.web.community;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.validator.EmailValidator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.community.*;
import gs.data.state.State;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteController;
import gs.web.util.NewSubscriberDetector;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MySchoolListLoginController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String BEAN_NAME = "/mySchoolListLogin.page";
    public IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private MySchoolListConfirmationEmail _mySchoolListConfirmationEmail;

    final public static String EMAIL_FIELD_CODE = "email";
    final public static String ERROR_EMPTY_EMAIL_ADDRESS = "Please enter your email address.";
    final public static String ERROR_INVALID_EMAIL_ADDRESS = "Please enter a valid email address.";

    protected void onBindAndValidate(HttpServletRequest request, java.lang.Object objCommand,
                                     BindException errors) {
        LoginCommand command = (LoginCommand)objCommand;

        EmailValidator emv = EmailValidator.getInstance();

        if (StringUtils.isEmpty(command.getEmail())) {
            errors.rejectValue(EMAIL_FIELD_CODE, null, ERROR_EMPTY_EMAIL_ADDRESS);
        } else if (!emv.isValid(command.getEmail())) {
            errors.rejectValue(EMAIL_FIELD_CODE, null, ERROR_INVALID_EMAIL_ADDRESS);
        }
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException e) throws Exception {
        LoginCommand command = (LoginCommand)o;
        String email = command.getEmail();
        User user = getUserDao().findUserFromEmailIfExists(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            getUserDao().saveUser(user);
            user = getUserDao().findUserFromEmail(email);
            sendConfirmationEmail(user, request);
        }

        if (request.getParameter("pa") != null) {
            NewSubscriberDetector.notifyOmnitureWhenNewNewsLetterSubscriber(user, request, response);
            _subscriptionDao.addNewsletterSubscriptions(user, createSubscriptionList(user, request));
        }

        PageHelper.setMemberCookie(request, response, user);

        Map<String,Boolean> model = new HashMap<String,Boolean>();
        model.put("showNewsletterHover", true);
        return new ModelAndView(getSuccessView(), model);
    }

    /**
     * Sends a confirmation email to the new user
     */
    protected void sendConfirmationEmail(User user, HttpServletRequest request) {
        try {
            _mySchoolListConfirmationEmail.sendToUser(user, request);
        } catch (Exception ex) {
            _log.error("Error sending msl confirmation email to " + user, ex);
        }
    }

    protected List<Subscription> createSubscriptionList(User user, HttpServletRequest request) {
        // create a list of subscriptions and add this one to it
        List<Subscription> subs = new ArrayList<Subscription>();

        // create a new subscription, set user on it, set product to parent_advisor,
        // set state to whatever is in sessioncontext
        State state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setProduct(SubscriptionProduct.PARENT_ADVISOR);
        subscription.setState(state);
        subs.add(subscription);

        return subs;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao _userDao) {
        this._userDao = _userDao;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao _subscriptionDao) {
        this._subscriptionDao = _subscriptionDao;
    }

    public MySchoolListConfirmationEmail getMySchoolListConfirmationEmail() {
        return _mySchoolListConfirmationEmail;
    }

    public void setMySchoolListConfirmationEmail(MySchoolListConfirmationEmail mySchoolListConfirmationEmail) {
        _mySchoolListConfirmationEmail = mySchoolListConfirmationEmail;
    }
}
