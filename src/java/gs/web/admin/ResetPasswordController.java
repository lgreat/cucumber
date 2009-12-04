package gs.web.admin;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import gs.data.util.email.EmailUtils;
import gs.data.community.IUserDao;
import gs.data.community.User;
import org.apache.commons.lang.StringUtils;

/**
 * This controller provides a simple web interface to set a user's password to "password".
 *
 * usage: http://www.greatschools.net/admin/resetPassword.page?email=tester@greatschools.net 
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public class ResetPasswordController implements Controller {

    /** Spring BEAN id */
    public static final String BEAN_ID = "resetPasswordController";

    protected static final String DEFAULT_PASSWORD = "password";

    private IUserDao _userDao;

    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        String email = request.getParameter("email");
        if (StringUtils.isNotBlank(email)) {
            User user = getUserDao().findUserFromEmailIfExists(email);
            if (user != null) {
                user.setPlaintextPassword(DEFAULT_PASSWORD);
                _userDao.saveUser(user);
                out.print("password changed for ");
                out.print(email);
            } else {
                out.print("unknown user");
            }
        } else{
            out.print("invalid email");
        }
        out.flush();
        return null;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
