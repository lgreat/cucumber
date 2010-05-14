package gs.web.school;

import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.community.User;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.security.Permission;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class PrincipalReviewController extends AbstractController {

    public static final String BEAN_ID = "/school/principalReview";

    private IReviewDao _reviewDao;
    private ISchoolDao _schoolDao;
    private String _viewName;
    private IReportedEntityDao _reportedEntityDao;


    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        School school = (School) request.getAttribute(AbstractSchoolController.SCHOOL_ATTRIBUTE);

        model.put("school", school);

        ModelAndView mAndV = new ModelAndView(new RedirectView(request.getContextPath() + "/cgi-bin/pq_start.cgi/" + school.getDatabaseState() + "/" + school.getId()));;

        if (validCookieExists(request,school)) {
            mAndV = new ModelAndView(getViewName(), model);
        }
        
        return mAndV;
    }


    public boolean validCookieExists(HttpServletRequest request, School school) {

        Map<String, String> credentials = _schoolDao.getPrincipalCredentials(school);
        String username = credentials.get("username");
        String password = credentials.get("password");

        MessageDigest md5 = null;
        
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            //can't recover
            return false;
        }
        
        md5.update(username.getBytes());
        md5.update(password.getBytes());
        md5.update("lounge".getBytes());

        String digest = new String(Hex.encodeHex(md5.digest()));
        Cookie[] cookies = request.getCookies();
        String pqLogin = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("PQLOGIN".equals(cookie.getName())) {
                    pqLogin = cookie.getValue();
                }
            }
        }

        boolean valid = digest.equals(pqLogin);

        return valid;
    }


    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }
}


