package gs.web.community.registration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;


public class UserLoginCommand  {
    protected final Log _log = LogFactory.getLog(getClass());

    @Email(message="Please enter a valid email address.")
    @NotNull(message="Please enter a valid email address.")
    private String email;

    @Size(min=2, max=14, message="Password should be 6-14 characters.")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}