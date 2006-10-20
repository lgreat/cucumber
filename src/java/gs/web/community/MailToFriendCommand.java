package gs.web.community;

import org.apache.commons.lang.StringUtils;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class MailToFriendCommand {
    private String _userEmail = "";
    private String _friendEmail = "";
    private String _subject = "";
    private String _message = "";
    private String [] _friendEmails;
    private String _refer = "";

    private int _schoolId;


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
}
