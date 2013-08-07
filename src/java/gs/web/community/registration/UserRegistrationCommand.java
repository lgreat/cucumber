package gs.web.community.registration;

import gs.data.state.State;

import gs.data.validation.FieldMatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

@FieldMatch.List({
    @FieldMatch(first = "password", second = "confirmPassword", message = "Passwords do not match.")
})
public class UserRegistrationCommand  {
    protected final Log _log = LogFactory.getLog(getClass());

    @Email(message="Please enter a valid email address.")
    @NotNull(message="Please enter a valid email address.")
    private String email;

    @Size(min=6, max=14, message="Password should be 6-14 characters.")
    private String password;

    @Size(min=6, max=14, message="Password should be 6-14 characters.")
    private String confirmPassword;

    private State state;

    @Size(min=2, max=24, message="First name must be 2-24 characters long.")
    private String firstName;

    private String lastName;
    private String city;

    @Size(min=6, max=14, message="Username must be 6-14 characters.")
    private String screenName;

    private String gender;
    private String facebookId;

    private boolean mss;

    @AssertTrue(message="You must accept the terms of use.")
    private boolean terms;

    @AssertTrue(message="Password should be 6-14 characters.")
    public boolean isPasswordOrFacebookId() {
        // either password or facebook ID needs to be provided
        boolean valid = (password != null || facebookId != null);
        return valid;
    }

    @NotNull(message="How field must be provided.")
    private String how;

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getScreenName() {
        return this.screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public boolean isTerms() {
        return terms;
    }

    public void setTerms(boolean terms) {
        this.terms = terms;
    }

    public String getHow() {
        return how;
    }

    public void setHow(String how) {
        this.how = how;
    }

    public boolean isMss() {
        return mss;
    }

    public void setMss(boolean mss) {
        this.mss = mss;
    }

    // fluent interface methods generated with IntelliJ plugin

    public UserRegistrationCommand email(final String email) {
        this.email = email;
        return this;
    }

    public UserRegistrationCommand password(final String password) {
        this.password = password;
        return this;
    }

    public UserRegistrationCommand confirmPassword(final String confirmPassword) {
        this.confirmPassword = confirmPassword;
        return this;
    }

    public UserRegistrationCommand state(final State state) {
        this.state = state;
        return this;
    }

    public UserRegistrationCommand firstName(final String firstName) {
        this.firstName = firstName;
        return this;
    }

    public UserRegistrationCommand lastName(final String lastName) {
        this.lastName = lastName;
        return this;
    }

    public UserRegistrationCommand city(final String city) {
        this.city = city;
        return this;
    }

    public UserRegistrationCommand screenName(final String screenName) {
        this.screenName = screenName;
        return this;
    }

    public UserRegistrationCommand gender(final String gender) {
        this.gender = gender;
        return this;
    }

    public UserRegistrationCommand facebookId(final String facebookId) {
        this.facebookId = facebookId;
        return this;
    }

    public UserRegistrationCommand terms(final boolean terms) {
        this.terms = terms;
        return this;
    }

    public UserRegistrationCommand how(final String how) {
        this.how = how;
        return this;
    }

    public UserRegistrationCommand mss(final boolean mss) {
        this.mss = mss;
        return this;
    }

}
