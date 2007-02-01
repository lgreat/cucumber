package gs.web.community.registration;

import junit.framework.TestCase;

public class UserCommandSaTest extends TestCase {
    public void testNewsletterIsFalseByDefault() {
        UserCommand userCommand = new UserCommand();
        assertFalse("Newsletter should default to false so subscription is only saved when newsletterStr parameter is explicitly set to 'y'",
                userCommand.getNewsletter());
    }
}
