package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.util.table.ITableDao;
import gs.web.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;

import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.validation.Valid;

@Controller
@RequestMapping("/community/registration")
public class SocialRegistrationAndLoginController implements ReadWriteAnnotationController {
    protected final Log _log = LogFactory.getLog(getClass());

    private ITableDao _tableDao;

    @Autowired
    private IUserDao _userDao;

    private ExactTargetAPI _exactTargetAPI;

    @Autowired
    private LocalValidatorFactoryBean _validatorFactory;

    @Autowired
    private UserRegistrationOrLoginService _userRegistrationOrLoginService;

    public String EMAIL_MODEL_KEY = "email";
    public String SCREEN_NAME_MODEL_KEY = "screenName";
    public String USER_ID_MODEL_KEY = "userId";
    public String NUMBER_MSL_ITEMS_MODEL_KEY = "numberMSLItems";
    public String MODEL_ACCOUNT_CREATED_KEY = "GSAccountCreated";
    public String MODEL_SUCCESS_KEY = "success";
    public String MODEL_FIRST_NAME_KEY = "firstName";

    public static final String SPREADSHEET_ID_FIELD = "ip";

    @RequestMapping(value = "/socialRegistrationAndLogin.json", method = RequestMethod.POST)
    public View handleJoin(ModelMap modelMap,
                           @Valid UserRegistrationCommand userRegistrationCommand,
                           @Valid UserLoginCommand userLoginCommand,
                           BindingResult bindingResult,
                           UserSubscriptionCommand userSubscriptionCommand,
                           RegistrationOrLoginBehavior registrationBehavior,
                           HttpServletRequest request,
                           HttpServletResponse response

    ) throws Exception {
        View view = new MappingJacksonJsonView();

        _validatorFactory.validate(userRegistrationCommand, bindingResult);

        if (bindingResult.hasErrors()) {
            modelMap.put("errors", bindingResult.getAllErrors());
            return new MappingJacksonJsonView();
        }

        UserRegistrationOrLoginService.Summary summary = _userRegistrationOrLoginService.loginOrRegister(
            userRegistrationCommand, userLoginCommand, registrationBehavior, bindingResult, request, response
        );

        User user = summary.getUser();

        modelMap.put(MODEL_ACCOUNT_CREATED_KEY, String.valueOf(summary.wasUserRegistered()));
        modelMap.put(USER_ID_MODEL_KEY, user.getId());

        if (user.getUserProfile() != null) {
            modelMap.put(SCREEN_NAME_MODEL_KEY, user.getUserProfile().getScreenName());
        }
        modelMap.put(EMAIL_MODEL_KEY, user.getEmail());
        modelMap.put(
            NUMBER_MSL_ITEMS_MODEL_KEY,
            user.getFavoriteSchools() != null ? user.getFavoriteSchools().size() : 0
        );
        modelMap.put(MODEL_SUCCESS_KEY, "true");
        modelMap.put(MODEL_FIRST_NAME_KEY, user.getFirstName());
        modelMap.remove("userRegistrationCommand");
        modelMap.remove("userSubscriptionCommand");
        modelMap.remove("registrationBehavior");

        if (summary.wasUserRegistered()) {
            PageHelper.setMemberAuthorized(request, response, summary.getUser());
        }

        ThreadLocalTransactionManager.commitOrRollback();

        return view;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public ITableDao getTableDao() {
        return _tableDao;
    }

    public void setTableDao(ITableDao tableDao) {
        _tableDao = tableDao;
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI exactTargetAPI) {
        _exactTargetAPI = exactTargetAPI;
    }

    public void setUserRegistrationOrLoginService(UserRegistrationOrLoginService userRegistrationOrLoginService) {
        _userRegistrationOrLoginService = userRegistrationOrLoginService;
    }
}
