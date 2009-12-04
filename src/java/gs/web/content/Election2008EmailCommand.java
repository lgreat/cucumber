package gs.web.content;

import gs.web.community.ICaptchaCommand;

/**
 * @author greatschools.org>
 */
public class Election2008EmailCommand implements ICaptchaCommand {
    final public static String SUFFIX_SIGN_UP_CONFIRM = "Sign Up Confirm";
    final public static String SUFFIX_TELL_A_FRIEND_CONFIRM = "Tell a Friend Confirm";
    final public static String SUFFIX_TELL_A_FRIEND_EMAIL_ERROR = "Tell a Friend Email Error";
    final public static String SUFFIX_TELL_A_FRIEND_VALIDATION = "Tell a Friend Validation";
    final public static String SUFFIX_CONFIRM_ENTRY = "Confirm Entry";

    private String _userEmail;
    private String _friendEmail;
    private String _subject = "Our schools are failing - Act now";
    private String _message =
            "Did you know that 24 countries outscore U.S. schools in math, and 20 outscore the U.S. in science?\n" +
                    " \n" +
                    "Our schools are failing. While the rest of the developed world is preparing their " +
                    "children for the new century, our system continues to lag behind.\n" +
                    " \n" +
                    "The countries with the best schools attract the best jobs. If jobs move to other " +
                    "countries, our children's opportunities dry up. And so does our economy.\n" +
                    " \n" +
                    "Join the national debate.  Act now and visit www.strongamericanschools.org " +
                    "to improve education before more American students lose out, hurting our " +
                    "economy and impacting every one of us.";
    private String _challenge;
    private String _response;
    private String _alert;
    private boolean _hideForm = false;
    private String _pageNameSuffix = SUFFIX_CONFIRM_ENTRY;

    public String getPageNameSuffix() {
        return _pageNameSuffix;
    }

    public void setPageNameSuffix(String pageNameSuffix) {
        _pageNameSuffix = pageNameSuffix;
    }

    public boolean isSignUpConfirm() {
        return SUFFIX_SIGN_UP_CONFIRM.equals(getPageNameSuffix());
    }

    public boolean isTellAFriendConfirm() {
        return SUFFIX_TELL_A_FRIEND_CONFIRM.equals(getPageNameSuffix());
    }

    public String getUserEmail() {
        return _userEmail;
    }

    public void setUserEmail(String userEmail) {
        _userEmail = userEmail;
    }

    public String getFriendEmail() {
        return _friendEmail;
    }

    public void setFriendEmail(String friendEmail) {
        _friendEmail = friendEmail;
    }

    public String getSubject() {
        return _subject;
    }

    public void setSubject(String subject) {
        _subject = subject;
    }

    public String getMessage() {
        return _message;
    }

    public void setMessage(String message) {
        _message = message;
    }

    public String getChallenge(){
        return _challenge;
    }

    public void setChallenge(String v){
        _challenge = v;
    }

    public String getResponse(){
        return _response;
    }

    public void setResponse(String v){
        _response = v;
    }

    public String getAlert() {
        return _alert;
    }

    public void setAlert(String alert) {
        _alert = alert;
    }

    public boolean isHideForm() {
        return _hideForm;
    }

    public void setHideForm(boolean hideForm) {
        this._hideForm = hideForm;
    }
}
