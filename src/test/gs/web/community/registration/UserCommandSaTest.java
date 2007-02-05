package gs.web.community.registration;

import junit.framework.TestCase;

public class UserCommandSaTest extends TestCase {
    public void testNewsletterIsFalseByDefault() {
        UserCommand userCommand = new UserCommand();
        assertTrue("Newsletter should default to true",
                userCommand.getNewsletter());
    }
}
