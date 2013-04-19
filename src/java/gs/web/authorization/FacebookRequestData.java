package gs.web.authorization;


import gs.data.community.User;

public class FacebookRequestData {
    private String _userId;
    private String _oAuthToken;

    public String getUserId() {
        return _userId;
    }

    public void setUserId(String userId) {
        _userId = userId;
    }

    public String getoAuthToken() {
        return _oAuthToken;
    }

    public void setoAuthToken(String oAuthToken) {
        _oAuthToken = oAuthToken;
    }

    public boolean isValid() {
        return _userId != null;
    }

    public boolean isOwnedBy(User user) {
        return (isValid() && _userId.equals(user.getFacebookId()));
    }
}
