/**
 * Copyright (c) 2006 GreatSchools.org. All Rights Reserved.
 */
package gs.web.widget;

import gs.web.BaseControllerTestCase;
import gs.data.util.email.MockJavaMailSender;
import gs.data.util.email.EmailHelperFactory;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.state.State;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;

import javax.mail.MessagingException;
import javax.mail.Message;
import java.io.IOException;
import java.util.List;

import static org.easymock.EasyMock.*;

/**
 * Provides testing for the SchoolFinderWidgetEmail bean.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class SchoolFinderWidgetEmailTest extends BaseControllerTestCase {
    private static final String FROM_EMAIL = "aroy@greatschools.org";
    private static final String FROM_NAME = "Anthony";
    private static final String SUBJECT = "Testing";
    private SchoolFinderWidgetEmail _email;
    private MockJavaMailSender _mailSender;

    public void setUp() throws Exception {
        super.setUp();
        _email = new SchoolFinderWidgetEmail();
        _email.setFromEmail(FROM_EMAIL);
        _email.setFromName(FROM_NAME);
        _email.setSubject(SUBJECT);
        _mailSender = new MockJavaMailSender();
        _mailSender.setHost("greatschools.org");
        EmailHelperFactory _factory = new EmailHelperFactory();
        _factory.setMailSender(_mailSender);
        _email.setEmailHelperFactory(_factory);
    }

    public void testSend() throws MessagingException, IOException {
        // verify init
        assertEquals(FROM_EMAIL, _email.getFromEmail());
        assertEquals(SUBJECT, _email.getSubject());
        assertEquals(FROM_NAME, _email.getFromName());
        // setup
        User user = new User();
        user.setEmail("aroy+1@greatschools.org");
        _email.sendToUser(user, "<widgetCode>", getRequest());
        // verify
        List msgs = _mailSender.getSentMessages();
        assertNotNull(msgs);
        assertEquals(1, msgs.size());
        Message msg = (Message) msgs.get(0);
        assertEquals(SUBJECT, msg.getSubject());
        assertTrue(msg.getFrom()[0].toString().indexOf(FROM_NAME) > -1);
        assertTrue(msg.getFrom()[0].toString().indexOf(FROM_EMAIL) > -1);
        assertNotNull(msg.getContent());
    }

    public void xtestRealSend() throws Exception {
        SchoolFinderWidgetEmail email = (SchoolFinderWidgetEmail)
                getApplicationContext().getBean(SchoolFinderWidgetEmail.BEAN_ID);
        User user = new User();
        user.setEmail("aroy@greatschools.org");

        CustomizeSchoolSearchWidgetController controller = new CustomizeSchoolSearchWidgetController();
        CustomizeSchoolSearchWidgetCommand command = new CustomizeSchoolSearchWidgetCommand();
        command.setBackgroundColor("123456");
        getRequest().setServerName("localhost");
        String widgetCode = controller.getWidgetCode(command, getRequest());

        email.sendToUser(user, widgetCode, getRequest());
    }
}
