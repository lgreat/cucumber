package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.community.IUserDao;
import gs.data.community.User;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class MySchoolListCompareController extends AbstractController {

    public static final String BEAN_ID = "/community/mySchoolListCompare.page";
    public static final Logger _log = Logger.getLogger(MySchoolListCompareController.class);

    private IUserDao _userDao;

    /** Query Parameter - all required */
    public static final String PARAM_USERID = "user"; // user db id
    public static final String PARAM_LEVEL = "level"; // e, m, h
    public static final String PARAM_STATE = "state"; // 2-letter abbreviation

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        String idParam = request.getParameter(PARAM_USERID);
        if (StringUtils.isNotBlank(idParam)) {
            Integer uid = Integer.decode(idParam.trim());
            try {
                User u = getUserDao().findUserFromId(uid);
            } catch (ObjectRetrievalFailureException e) {
                _log.warn(e);
            }
        }
        StringBuilder sb = new StringBuilder();

        return new ModelAndView(new RedirectView(sb.toString()));
    }


    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
