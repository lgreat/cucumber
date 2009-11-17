package gs.web.community;

import org.apache.commons.lang.StringUtils;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class MailToFriendCommand implements ICaptchaCommand {
    private String _userEmail = "";
    private String _friendEmail = "";
    private String _subject = "";
    private String _message = "";
    private String [] _friendEmails;
    private String _refer = "";
    private String _challenge = "";
    private String _response = "";
    private int _schoolId;

    private int _articleId;

    private long _featureId;

    // Added for GS-8839: new email a friend controller
    private boolean _copy = false;
    private String _contentUrl;
    private String _contentTitle;
    private String _summary;
    private int _discussionId;

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

        if (StringUtils.isNotBlank(friendEmail)) {
            _friendEmail = friendEmail.replaceAll(" ","");
            _friendEmails = _friendEmail.split(",");
        }
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

    public String[] getFriendEmails() {
        return _friendEmails;
    }

    public int getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(int schoolId) {
        _schoolId = schoolId;
    }

    public int getArticleId() {
        return _articleId;
    }

    public void setArticleId(int articleId) {
        _articleId = articleId;
    }

    public long getFeatureId() {
        return _featureId;
    }

    public void setFeatureId(long featureId) {
        _featureId = featureId;
    }

    public String getRefer() {
        return _refer;
    }

    public void setRefer(String refer) {
        if (StringUtils.isNotEmpty(refer)) {
            if (refer.equals("overview")) {
                _refer = "School Profile Overview";
            } else if (refer.equals("ratings")) {
                _refer = "School Profile Rankings";
            } else if (refer.equals("authorizer")) {
                _refer = "authorizer";
            } else {
                _refer = refer;
            }
        }
    }

    public boolean isCopy() {
        return _copy;
    }

    public void setCopy(boolean copy) {
        _copy = copy;
    }

    public String getContentUrl() {
        return _contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        _contentUrl = contentUrl;
    }

    public String getContentTitle() {
        return _contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        _contentTitle = contentTitle;
    }

    public String getSummary() {
        return _summary;
    }

    public void setSummary(String summary) {
        _summary = summary;
    }

    public int getDiscussionId() {
        return _discussionId;
    }

    public void setDiscussionId(int discussionId) {
        _discussionId = discussionId;
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
}
