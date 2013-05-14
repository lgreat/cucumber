package gs.web.community.registration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;


public class UserLoginCommand {
    protected final Log _log = LogFactory.getLog(getClass());

    // Sometime we want to validate only email.For example when we send a re-verification email.
    //Therefore we have to use validation groups.
    //http://blog.codeleak.pl/2011/03/how-to-jsr303-validation-groups-in.html
    public interface ValidateJustEmail {
    }

    public interface ValidateLoginCredentials {
    }

    @Email(message = "Please enter a valid email address.", groups = {ValidateJustEmail.class})
    @NotNull(message = "Please enter a valid email address.", groups = {ValidateJustEmail.class})
    private String email;

    @NotNull(message = "Password should be 6-14 characters.", groups = {ValidateLoginCredentials.class})
    @Size(min = 2, max = 14, message = "Password should be 6-14 characters.", groups = {ValidateLoginCredentials.class})
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