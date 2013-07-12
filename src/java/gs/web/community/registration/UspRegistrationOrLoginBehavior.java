package gs.web.community.registration;

public class UspRegistrationOrLoginBehavior extends RegistrationOrLoginBehavior {

    // whether the user needs to verify email, and hence have a provisional password.
    public boolean requireEmailVerification() {
        return true;
    }

    // whether to send email verification email to the user
    public boolean sendVerificationEmail() {
        return true;
    }

    // whether to send welcome email to the user
    public boolean sendConfirmationEmail() {
        return false;
    }
}
