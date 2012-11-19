package gs.web.community;

import gs.web.util.context.SubCookie;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HoverHelper {

    //HttpServletRequest _request;
    //HttpServletResponse _response;
    SubCookie _cookie;

    private static String COOKIE_PROPERTY = "showHover";

    public enum Hover {
        //TODO: add comments describing each hover

        SCHOOL_REVIEW_POSTED("schoolReviewPosted"),
        SCHOOL_REVIEW_QUEUED("schoolReviewNotPostedThankYou"),
        EMAIL_VERIFIED_SCHOOL_REVIEW_POSTED("schoolReviewPosted"),
        EMAIL_VERIFIED_SCHOOL_REVIEW_QUEUED("emailValidatedSchoolReviewQueued"),
        EMAIL_VERIFIED("emailValidated"),
        NEW_EMAIL_VERIFIED("editEmailValidated"),
        SUBSCRIPTION_EMAIL_VERIFIED("subscriptionEmailValidated"),
        ESP_ACCOUNT_VERIFIED("espAccountVerified"),
        ESP_ACCOUNT_PROVISIONAL("espAccountProvisional");

        private String _id;

        Hover(String id) {
            _id = id;
        }

        public String getId() {
            return _id;
        }

        public void setId(String id) {
            _id = id;
        }

        public String toString() {
            return _id;
        }
    }

    public HoverHelper(SubCookie cookie) {
        //_request = request;
        //_response = response;
        //_cookie = new SitePrefCookie(request, response);
        _cookie = cookie;
    }

    public void setHoverCookie(Hover hover) {
        _cookie.setProperty(COOKIE_PROPERTY, hover.getId());
    }

    public boolean isHoverCookieSet(Hover hover) {
        return StringUtils.equals(hover.getId(), _cookie.getProperty(COOKIE_PROPERTY));
    }
}
