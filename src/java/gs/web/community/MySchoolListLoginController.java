package gs.web.community;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.PageHelper;
import gs.web.util.ReadWriteController;

public class MySchoolListLoginController extends SimpleFormController implements ReadWriteController {

    public static final String BEAN_NAME = "/community/mySchoolListLogin.page";
    public IUserDao _userDao;

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
        }
        PageHelper.setMemberCookie(request, response, user);
        return new ModelAndView(getSuccessView());
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao _userDao) {
        this._userDao = _userDao;
    }
}
