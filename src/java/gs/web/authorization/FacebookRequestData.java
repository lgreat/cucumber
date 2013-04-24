package gs.web.authorization;


import gs.data.community.User;

public class FacebookRequestData {
    private String _userId;
    private String _authorizationCode;

    public String getUserId() {
        return _userId;
    }

    public void setUserId(String userId) {
        _userId = userId;
    }

    public String getAuthorizationCode() {
        return _authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        _authorizationCode = authorizationCode;
    }

    public boolean isValid() {
        return _userId != null && _authorizationCode != null;
    }

    public boolean isOwnedBy(User user) {
        return (isValid() && _userId.equals(user.getFacebookId()));
    }
}
