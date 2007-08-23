package gs.web.soap;

/**
 * Provides data encapsulation for the CreateOrUpdateUserRequest.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CreateOrUpdateUserRequestBean {
    private String _id;
    private String _screenName;
    private String _email;
    private String _password;

    public CreateOrUpdateUserRequestBean() {}

    public CreateOrUpdateUserRequestBean(String id, String screenName, String email, String password) {
        _id = id;
        _screenName = screenName;
        _email = email;
        _password = password;
    }

    public CreateOrUpdateUserRequestBean(int id, String screenName, String email, String password) {
        this(String.valueOf(id), screenName, email, password);
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    public String getScreenName() {
        return _screenName;
    }

    public void setScreenName(String screenName) {
        _screenName = screenName;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        _password = password;
    }
}
