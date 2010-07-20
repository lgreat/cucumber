package gs.web.school;

import com.google.gdata.data.extensions.Rating;
import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Poster;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.security.Permission;
import gs.data.state.State;
import gs.web.school.review.ReviewCommand;
import gs.web.util.*;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class PrincipalReviewController extends SimpleFormController implements ReadWriteController {
    private static final Logger _log = Logger.getLogger(PrincipalReviewController.class);
    public static final String BEAN_ID = "/school/principalReview";

    private IReviewDao _reviewDao;
    private ISchoolDao _schoolDao;
    private String _viewName;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    @Override
    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        School school = (School) request.getAttribute(AbstractSchoolController.SCHOOL_ATTRIBUTE);
        model.put("school", school);

        ModelAndView mAndV = new ModelAndView(new RedirectView(request.getContextPath() + "/cgi-bin/pq_start.cgi/" + school.getDatabaseState() + "/" + school.getId()));

        if (validCookieExists(request,school)) {
            List<Review> principalReview = _reviewDao.findPublishedPrincipalReviewsBySchool(school);
            //There should never be more than one published principal review. But if there is, use only the most recent one
            if (principalReview != null && principalReview.size() > 0) {
                model.put("review", principalReview.get(0));
            }
            return super.showForm(request, response, errors);
        }
        
        return mAndV;
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        Map<String,Object> model = new HashMap();

        ReviewCommand reviewCommand = (ReviewCommand) command;

        State state = State.fromString(request.getParameter("state"));
        Integer schoolId = new Integer(request.getParameter("id"));
        School school  = _schoolDao.getSchoolById(state, schoolId);

        User user = _userDao.findUserFromEmailIfExists(reviewCommand.getEmail());
        boolean newUser = (user == null);

        if (newUser) {
            user = new User();
            user.setEmail(reviewCommand.getEmail());
            user.setFirstName(reviewCommand.getFirstName());
            user.setLastName(reviewCommand.getLastName());
            user.setUpdated(new Date());
            user.setHow("principal");
            _userDao.saveUser(user);
            // GS-7649 Because of hibernate caching, it's possible for a list_active record
            // (with list_member id) to be commited before the list_member record is
            // committed. Adding this commitOrRollback prevents this.
            ThreadLocalTransactionManager.commitOrRollback();
            user = _userDao.findUserFromEmail(user.getEmail());
        } else {
            user.setFirstName(reviewCommand.getFirstName());
            user.setLastName(reviewCommand.getLastName());
            user.setUpdated(new Date());
            Set<Subscription> subscriptions = user.getSubscriptions();

            //if this principal already has a one free school subscription, remove it
            if (subscriptions != null) {
                HashSet<Subscription> subscriptionsToRemove = new HashSet<Subscription>();
                for (Subscription s : subscriptions) {
                    if (SubscriptionProduct.ONE_FREE_SCHOOL.equals(s.getProduct())) {
                        subscriptionsToRemove.add(s);
                    }
                }
                
                subscriptions.removeAll(subscriptionsToRemove);

                for (Subscription s : subscriptionsToRemove){
                    _subscriptionDao.removeSubscription(s.getId());
                }
            }
            _userDao.updateUser(user);
            ThreadLocalTransactionManager.commitOrRollback();
        }

        Set<Subscription> subscriptions = user.getSubscriptions();
        if (subscriptions == null) {
            subscriptions = new HashSet<Subscription>();
        }

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setProduct(SubscriptionProduct.RATING);
        subscription.setState((state));
        subscription.setSchoolId((schoolId));
        if (subscriptions.add(subscription)) {
            _subscriptionDao.saveSubscription(subscription);
        };

        //add a one free school subscription for this specific school
        subscription = new Subscription();
        subscription.setUser(user);
        subscription.setProduct(SubscriptionProduct.ONE_FREE_SCHOOL);
        subscription.setState((state));
        subscription.setSchoolId((schoolId));
        if (subscriptions.add(subscription)) {
            _subscriptionDao.saveSubscription(subscription);
        };

        user.setSubscriptions(subscriptions);

        _userDao.updateUser(user);

        Review review = new Review();
        review.setUser(user);
        review.setPosted(new Date());
        review.setStatus("u");
        review.setPoster(Poster.PRINCIPAL);
        review.setSchool(school);
        review.setComments(reviewCommand.getComments());
        review.setJobTitle(reviewCommand.getJobTitle());
        _reviewDao.saveReview(review);

        SitePrefCookie cookie = new SitePrefCookie(request, response);
                cookie.setProperty("showHover", "principalReviewSubmitted");

        return new ModelAndView(getSuccessView()
                + "?id="+schoolId
                + "&state="+state,model);
    }

    public boolean validCookieExists(HttpServletRequest request, School school) {

        Map<String, String> credentials = _schoolDao.getPrincipalCredentials(school);

        if (credentials == null) {
            return false; //early exit
        }

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


