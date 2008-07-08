package gs.web.content;

import gs.web.community.ICaptchaCommand;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class Election2008EmailCommand implements ICaptchaCommand {
    private String _userEmail = "";
    private String _friendEmail = "";
    private String _subject = "";
    private String _message = "";
    private String _challenge = "";
    private String _response = "";
    private boolean _success = false;

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

    public boolean isSuccess() {
        return _success;
    }

    public void setSuccess(boolean success) {
        _success = success;
    }
}
