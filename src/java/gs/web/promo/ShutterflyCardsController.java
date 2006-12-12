package gs.web.promo;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.promo.IPromoDao;
import gs.data.community.promo.Promo;
import gs.data.community.promo.PromoCode;
import gs.web.util.ReadWriteController;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.orm.ObjectRetrievalFailureException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 *         <p/>
 *         This controller delivers three different views based on the following logic:
 *         <p/>
 *         <ol>
 *         <li>Valid (members before 11/8/06) members who have not yet been sent a promo code
 *         are shown a success view and are sent an email with the promo code.</li>
 *         <li>Members who have already received a promo code are shown an "already been
 *         redeemed" view</li>
 *         <li>All other users are shown an "ineligible" view</li>
 *         </ol>
 */
public class ShutterflyCardsController extends AbstractController implements ReadWriteController {

    public static final String BEAN_ID = "/promo/shutterfly/cards.page";


    protected static final String VIEW = "/promo/shutterfly/cards";
    protected static final String PARAM_EMAIL = "email";

    protected static final String IMAGE_LINK = "http://www.shutterfly.com/shop/product_c50285?cid=OMQ406GSCHL";

    protected static final String MODEL_IMAGE_ALT = "imageAlt";
    protected static final String MODEL_IMAGE_SRC = "imageSrc";
    protected static final String MODEL_PAGE_NAME = "pageName";
    protected static final String MODEL_IMAGE_LINK = "imageLink";

    protected static final String ELIGIBLE_SRC = "/res/img/promo/shutterfly/eligible.jpg";
    protected static final String INELIGIBLE_SRC = "/res/img/promo/shutterfly/ineligible.jpg";
    protected static final String REDEEMED_SRC = "/res/img/promo/shutterfly/redeemed.jpg";

    protected static final String ELIGIBLE_ALT = "Thank you for celebrating the holidays with Shutterfly! We have emailed you a promotional code good for 12 free 4x8 Shutterfly Holiday Cards. Hurry, this offer expires December 8, 2006.";
    protected static final String INELIGIBLE_ALT = "Thank you for celebrating the holidays with Shutterfly! Unfortunately, this email address is ineligible for this offer, according to the terms of the offer outlined below.";
    protected static final String REDEEMED_ALT = "Thank you for celebrating the holidays with Shutterfly! The email address you entered has already redeemed this offer. Limit one offer per customer.  See other ways Shutterfly can help you wrap up your holiday shopping!";

    protected static final String ELIGIBLE_PAGE_NAME = "Success Page";
    protected static final String INELIGIBLE_PAGE_NAME = "Not Eligible";
    protected static final String REDEEMED_PAGE_NAME = "Already Redeemed";

    private IUserDao _userDao;
    private IPromoDao _promoDao;
    private JavaMailSender _mailSender;

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        return processInelgibleRequest();
    }

    private static ModelAndView processInelgibleRequest() {
        return processRequest(INELIGIBLE_ALT, INELIGIBLE_SRC, INELIGIBLE_PAGE_NAME);
    }

    private static ModelAndView processRequest(String imageAlt, String imageSrc, String pageName) {
        ModelAndView mv = new ModelAndView(VIEW);

        mv.getModel().put(MODEL_IMAGE_ALT, imageAlt);
        mv.getModel().put(MODEL_IMAGE_SRC, imageSrc);
        mv.getModel().put(MODEL_PAGE_NAME, pageName);
        mv.getModel().put(MODEL_IMAGE_LINK, IMAGE_LINK);
        return mv;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public IPromoDao getPromoDao() {
        return _promoDao;
    }

    public void setPromoDao(IPromoDao promoDao) {
        _promoDao = promoDao;
    }

    public JavaMailSender getMailSender() {
        return _mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        _mailSender = mailSender;
    }
}
