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
public class ShutterflyCardsController extends AbstractController implements InitializingBean, ReadWriteController {

    public static final String BEAN_ID = "/promo/shutterfly/cards.page";


    protected static final String VIEW = "/promo/shutterfly/cards";
    protected static final String PARAM_EMAIL = "email";

    protected static final String IMAGE_LINK = "http://www.shutterfly.com/shop/product_c50285?cid=OMQ406GSCHL";

    protected static final String MODEL_IMAGE_ALT = "imageAlt";
    protected static final String MODEL_IMAGE_SRC = "imageSrc";
    protected static final String MODEL_PAGE_NAME = "pageName";    

    protected static final String ELIGIBLE_SRC = "/res/img/promo/shutterfly/eligible.jpg";
    protected static final String INELIGIBLE_SRC = "/res/img/promo/shutterfly/ineligible.jpg";
    protected static final String REDEEMED_SRC = "/res/img/promo/shutterfly/redeemed.jpg";

    protected static final String ELIGIBLE_ALT = "Thank you for celebrating the holidays with Shutterfly! We have emailed you a promotional code good for 12 free 4x8 Shutterfly Holiday Cards. Hurry, this offer expires December 8, 2006.";
    protected static final String INELIGIBLE_ALT = "Thank you for celebrating the holidays with Shutterfly! Unfortunately, this email address is ineligible for this offer, according to the terms of the offer outlined below.";
    protected static final String REDEEMED_ALT = "Thank you for celebrating the holidays with Shutterfly! The email address you entered has already redeemed this offer. Limit one offer per customer.  See other ways Shutterfly can help you wrap up your holiday shopping!";

    protected static final String ELIGIBLE_PAGE_NAME = "Success Page";
    protected static final String INELIGIBLE_PAGE_NAME = "Not Eligible";
    protected static final String REDEEMED_PAGE_NAME = "Already Redeemed";

    private static Promo PROMO;

    private Log _log = LogFactory.getLog(getClass());

    private IUserDao _userDao;
    private IPromoDao _promoDao;
    private JavaMailSender _mailSender;

    public void afterPropertiesSet() throws Exception {
        PROMO = _promoDao.findPromoById(1);
    }


    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        String email = request.getParameter(PARAM_EMAIL);
        Calendar calendar = Calendar.getInstance();
        calendar.set(2006, Calendar.NOVEMBER, 8);
        calendar.clear(Calendar.MILLISECOND);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.HOUR_OF_DAY);


        //Error checking
        if (StringUtils.isBlank(email)) {
            return processInelgibleRequest();
        }

        User user = null;
        try {
            user = _userDao.findUserFromEmail(email);
        } catch (ObjectRetrievalFailureException e) {
            _log.warn("Could not find user matching email:" + email);
        }

        if (user == null) {
            return processInelgibleRequest();
        }

        if (!user.getTimeAdded().before(calendar.getTime()) || user.getTimeAdded().equals(calendar.getTime())) {
            return processInelgibleRequest();
        }


        //start of promo processing
        PromoCode promoCode = _promoDao.findPromoCode(user, PROMO);

        if (null != promoCode) {
            return processRequest(REDEEMED_ALT, REDEEMED_SRC, REDEEMED_PAGE_NAME);
        } else {
            promoCode = _promoDao.assignPromoCode(user, PROMO);

            //no more promo codes left
            if (null == promoCode) {
                _log.error("No more promo codes left for promo: " + PROMO.getName() + " user: " + email);                
                return processRequest(REDEEMED_ALT, REDEEMED_SRC, REDEEMED_PAGE_NAME);
            } else {
                //send email
                try {
                    _mailSender.send(createMessage(email, promoCode.getCode()));
                } catch (MessagingException mess) {
                    _log.warn("User " + email + " did not receive promo code: " + promoCode.getCode() , mess);
                } catch (MailException me) {
                    _log.warn("User " + email + " did not receive promo code: " + promoCode.getCode() , me);
                }
                return processRequest(ELIGIBLE_ALT, ELIGIBLE_SRC, ELIGIBLE_PAGE_NAME);
            }
        }
    }

    private ModelAndView processInelgibleRequest() {
        return processRequest(INELIGIBLE_ALT, INELIGIBLE_SRC, INELIGIBLE_PAGE_NAME);
    }

    private ModelAndView processRequest(String imageAlt, String imageSrc, String pageName) {
        ModelAndView mv = new ModelAndView(VIEW);

        mv.getModel().put(MODEL_IMAGE_ALT, imageAlt);
        mv.getModel().put(MODEL_IMAGE_SRC, imageSrc);
        mv.getModel().put(MODEL_PAGE_NAME, pageName);

        return mv;
    }

    /**
     * This is a utility method to created the <code>MimeMessage</code object from a stub
     * MimeMessage.
     *
     * @param email A valid email String
     * @param promoCode A promo code to be included in the email.
     * @return a MimeMessage type
     * @throws javax.mail.MessagingException if there is a problem constructing the message.
     */
    MimeMessage createMessage(String email, String promoCode) throws MessagingException {

        MimeMessageHelper helper = new MimeMessageHelper(_mailSender.createMimeMessage(), false, "UTF-8");
        helper.setTo(email);
        try {
            helper.setFrom("shutterfly@greatschools.net", "GreatSchools");
        } catch (UnsupportedEncodingException uee) {
            helper.setFrom("shutterfly@greatschools.net");
        }
        helper.setSubject("Shutterfly Holiday Promotion Confirmation");
        helper.setSentDate(new Date());


        String emailText = "<p>Thank you for celebrating the holidays with Shutterfly! Your promotional code good for 12 free 4x8 Shutterfly Holiday Cards is:</p>\n" +
                "\n" +
                "<p>$PROMO_CODE</p>\n" +
                "\n" +
                "<p><a href=\"http://www.shutterfly.com/greatschools?cid=OMQ406GSCHL\">Visit Shutterfly today</a> to enter your unique code!<p>\n" +
                "\n" +
                "<p>We wish you the best the season has to offer,</p>\n" +
                "\n" +
                "<p>- The Shutterfly and GreatSchools teams</p>\n" +
                "\n" +
                "<small>\n" +
                "This special offer is only for GreatSchools.net users who subscribed to a GreatSchools newsletter before\n" +
                "November 8, 2006. To receive this offer, user must have or create a valid Shutterfly account at <a\n" +
                "    href=\"http://www.shutterfly.com/greatschools?cid=OMQ406GSCHL\">http://www.shutterfly.com/greatschools</a>. To\n" +
                "redeem, a unique promotional code must be inserted and used by December 8, 2006. Credit cannot be transferred to\n" +
                "other products or another account and cannot be combined with other offers, discounts or promotions. Users may\n" +
                "order additional holiday cards at their own expense. Shipping charges will apply to any card order. Limit one\n" +
                "per person. Shutterfly reserves the right to modify this offer should it be compromised in any manner including,\n" +
                "but not limited to, fraudulent activity.\n" +
                "</small>";

        emailText = emailText.replaceAll("\\$PROMO_CODE", promoCode);

        helper.setText(emailText, true);
        return helper.getMimeMessage();
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
