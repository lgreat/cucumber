package gs.web.community;

import gs.web.util.context.SubCookie;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HoverHelper {

    //HttpServletRequest _request;
    //HttpServletResponse _response;
    SubCookie _cookie;

    private static String COOKIE_PROPERTY = "showHover";

    public enum Hover {
        //TODO: add comments describing each hover

        SCHOOL_REVIEW_POSTED_THANK_YOU("schoolReviewPostedThankYou"),
        SCHOOL_REVIEW_NOT_POSTED_THANK_YOU("schoolReviewNotPostedThankYou"),
        EMAIL_VALIDATED_SCHOOL_REVIEW_POSTED("emailValidatedSchoolReviewPosted"),
        EMAIL_VALIDATED_SCHOOL_REVIEW_QUEUED("emailValidatedSchoolReviewQueued"),
        EMAIL_VALIDATED("emailValidated");

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
}
