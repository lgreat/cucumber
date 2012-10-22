package gs.web.community;

import gs.data.content.Article;
import gs.data.content.IArticleDao;
import gs.data.content.cms.CmsFeature;
import gs.data.content.cms.ICmsFeatureDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.util.CmsUtil;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:dlee@greatschools.org">David Lee</a>
 */
public class MailToFriendController extends SimpleFormController {

    public static final String BEAN_ID = "/community/mailToFriend.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private JavaMailSender _mailSender;
    private ISchoolDao _schoolDao;
    private IArticleDao _articleDao;
    private ICmsFeatureDao _featureDao;

    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {
        MailToFriendCommand mtc = (MailToFriendCommand) command;

        SessionContext session = SessionContextUtil.getSessionContext(request);

        if (StringUtils.isEmpty(mtc.getUserEmail())) {
            String email = session.getEmail();

            if (StringUtils.isNotEmpty(email) && StringUtils.isEmpty(mtc.getUserEmail())) {
                mtc.setUserEmail(email);
            }
        }

        if (0 != mtc.getSchoolId()) {
            State state = session.getState();
            if (state != null) {
                School school = getSchoolDao().getSchoolById(state, mtc.getSchoolId());

                if (null != school && school.isActive()) {
                    mtc.setSubject("Check out this info about " + school.getName());
                    StringBuffer msgBuffer = new StringBuffer();

                    msgBuffer.append("Check out what I found about my school -");

                    if (!school.getType().getSchoolTypeName().equals("private")) {
                        msgBuffer.append("test scores, ");
                    }
                    msgBuffer.append("demographics, parent reviews and more.")
                            .append("\n\n")
                            .append("I think you'll find this helpful too! Click on this link to ")
                            .append(school.getName())
                            .append(".\n\n");

                    UrlBuilder urlBuilder;
                    if (mtc.getRefer().equals("authorizer")) {
                        urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_AUTHORIZER);
                    } else {
                        urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
                    }
                    msgBuffer.append(urlBuilder.asFullUrl(request));
                    mtc.setMessage(msgBuffer.toString());
                }
            }
        } else if (0 != mtc.getArticleId()) {
            Article article = getArticleDao().getArticleFromId(mtc.getArticleId());
            if (null != article) {
                UrlBuilder urlBuilder = new UrlBuilder(article.getId(), false);
                createMessage(request, mtc, urlBuilder.asFullUrl(request));
            }
        } else if (CmsUtil.isCmsEnabled() && mtc.getFeatureId() > 0) {

            CmsFeature feature = _featureDao.get(mtc.getFeatureId());
            if (null != feature) {
                UrlBuilder urlBuilder = new UrlBuilder(feature.getContentKey(), feature.getFullUri());
                createMessage(request, mtc, urlBuilder.asFullUrl(request));
            }
        }
    }

    private void createMessage(HttpServletRequest request, MailToFriendCommand mtc, String url) {
        mtc.setSubject("I think you'll like GreatSchools");

        StringBuffer msgBuffer = new StringBuffer();
        msgBuffer.append("Check out this helpful resource I found").append
                (" on GreatSchools.org to support my child!").append
                ("\n\n").append
                ("Click on this link to learn more:").append
                ("\n\n").append
                (url);
        mtc.setMessage(msgBuffer.toString());
    }

    /**
     * Validate the following:
     * user email, friend's email, subject, message
     */
    protected void onBind(HttpServletRequest request, Object command, BindException errors) {
        MailToFriendCommand mtc = (MailToFriendCommand) command;
        org.apache.commons.validator.EmailValidator emv = org.apache.commons.validator.EmailValidator.getInstance();

        if (!emv.isValid(mtc.getUserEmail())) {
            errors.rejectValue("userEmail", null, "Please enter a valid email address.");
        }

        if (null == mtc.getFriendEmails()) {
            errors.rejectValue("friendEmail", null, "Please enter your friend's email address.");
        } else {
            String[] friendEmails = mtc.getFriendEmails();
            int numFriends = friendEmails.length;

            for (int i = 0; i < numFriends; i++) {
                if (!emv.isValid(friendEmails[i])) {
                    errors.rejectValue("friendEmail", null, "Your friend's email is not valid: " + friendEmails[i] + ".");
                    break;
                }
            }
        }

        if (StringUtils.isEmpty(mtc.getSubject())) {
            errors.rejectValue("subject", null, "Please enter a subject.");
        }

        if (StringUtils.isEmpty(mtc.getMessage())) {
            errors.rejectValue("message", null, "Sorry, the message cannot be empty.");
        }
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        MailToFriendCommand mtc = (MailToFriendCommand) command;
        OmnitureTracking omnitureTracking = new CookieBasedOmnitureTracking(request, response);

        SimpleMailMessage smm = new SimpleMailMessage();
        smm.setText(mtc.getMessage());
        smm.setTo(mtc.getFriendEmails());
        smm.setSubject(mtc.getSubject());
        smm.setFrom(mtc.getUserEmail());

        try {
            _mailSender.send(smm);
            omnitureTracking.addSuccessEvent(OmnitureTracking.SuccessEvent.TellAFriend);
        } catch (MailException ex) {
            _log.info(ex.getMessage());
        }

        ModelAndView mv = new ModelAndView(getSuccessView());
        mv.getModel().put("refer", mtc.getRefer());

        return mv;
    }

    public JavaMailSender getMailSender() {
        return _mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        _mailSender = mailSender;
    }


    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IArticleDao getArticleDao() {
        return _articleDao;
    }

    public void setArticleDao(IArticleDao articleDao) {
        _articleDao = articleDao;
    }

    public ICmsFeatureDao getCmsFeatureDao() {
        return _featureDao;
    }

    public void setCmsFeatureDao(ICmsFeatureDao cmsFeatureDao) {
        _featureDao = cmsFeatureDao;
    }
}
