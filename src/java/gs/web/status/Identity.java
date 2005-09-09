package gs.web.status;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class Identity {

    private String _username;
    private String _password;
    
    public String getUsername() {
        return _username;
    }

    public void setUsername(String username) {
        _username = username;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        _password = password;
    }
}
