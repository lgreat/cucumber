package gs.web.community.registration;

import junit.framework.TestCase;
import gs.data.community.User;
import gs.data.community.UserProfile;

import java.util.List;
import java.util.ArrayList;

public class UserCommandSaTest extends TestCase {
    public void testBasics() {
        UserCommand command = new UserCommand();

        String confirmPassword = "confirm";
        String password = "pass";
        String confirmEmail = "email";
        User user = new User();
        UserProfile userProfile = new UserProfile();
        String redirectUrl = "redir";
        String referrer = "here";
        List cityList = new ArrayList();
        boolean recontact = false;
        boolean terms = false;
        boolean newsletter = false;
        boolean beta = true;

        command.setConfirmPassword(confirmPassword);
        command.setPassword(password);
        command.setConfirmEmail(confirmEmail);
        command.setUser(user);
        command.setUserProfile(userProfile);
        command.setRedirectUrl(redirectUrl);
        command.setReferrer(referrer);
        command.setCityList(cityList);
        command.setRecontact(recontact);
        command.setTerms(terms);
        command.setNewsletter(newsletter);
        command.setBeta(beta);

        assertSame(confirmPassword, command.getConfirmPassword());
        command.setPassword(password);
        assertSame(password, command.getPassword());
        command.setConfirmEmail(confirmEmail);
        assertSame(confirmEmail, command.getConfirmEmail());
        command.setUser(user);
        assertSame(user, command.getUser());
        command.setUserProfile(userProfile);
        assertSame(userProfile, command.getUserProfile());
        command.setRedirectUrl(redirectUrl);
        assertSame(redirectUrl, command.getRedirectUrl());
        command.setReferrer(referrer);
        assertSame(referrer, command.getReferrer());
        command.setCityList(cityList);
        assertSame(cityList, command.getCityList());
        command.setRecontact(recontact);
        assertSame(recontact, command.isRecontact());
        command.setTerms(terms);
        assertSame(terms, command.getTerms());
        command.setNewsletter(newsletter);
        assertSame(newsletter, command.getNewsletter());
        command.setBeta(beta);
        assertSame(beta, command.isBeta());
    }

    public void testNewsletterIsFalseByDefault() {
        UserCommand userCommand = new UserCommand();
        assertTrue("Newsletter should default to true",
                userCommand.getNewsletter());
    }

    public void testTermsIsTrueByDefault() {
        UserCommand command = new UserCommand();
        assertTrue("Terms should default to true", command.getTerms());
    }

    public void testBetaIsFalseByDefault() {
        UserCommand command = new UserCommand();
        assertFalse("Beta should default to false", command.isBeta());
    }
}
