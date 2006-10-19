package gs.web.community;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.util.MockJavaMailSender;
import gs.web.util.context.SessionContext;
import org.easymock.MockControl;
import org.springframework.validation.BindException;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class MailToFriendControllerTest extends BaseControllerTestCase {
    private MailToFriendController _controller;
    private static final String SCHOOL_TEST_NAME = "TestNameSchool";

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new MailToFriendController();

        MockJavaMailSender _sender = new MockJavaMailSender();
        _sender.setHost("mail.greatschools.net");
        _controller.setMailSender(_sender);

        School school = new School();
        school.setId(new Integer("1"));
        school.setName(SCHOOL_TEST_NAME);
        school.setActive(true);
        school.setDatabaseState(State.CA);

        MockControl schoolDaoMockControl = MockControl.createControl(ISchoolDao.class);
        ISchoolDao schoolDao = (ISchoolDao) schoolDaoMockControl.getMock();
        schoolDao.getSchoolById(State.CA, Integer.valueOf("1"));
        schoolDaoMockControl.setReturnValue(school);
        schoolDaoMockControl.replay();

        _controller.setSchoolDao(schoolDao);
    }

    public void testGetEmailFromSessionOnBind() {
        MailToFriendCommand command = new MailToFriendCommand();
        BindException errors = new BindException(command, "");

        SessionContext sessionContext = new SessionContext();
        sessionContext.setEmail("dlee@greatschools.net");

        getRequest().setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        assertFalse(errors.hasErrors());
        assertEquals("dlee@greatschools.net", command.getUserEmail());

        //if user specifies his own email, that should override the one in the session
        command.setUserEmail("somethingelse@greatschools.net");
        _controller.onBindOnNewForm(getRequest(), command, errors);
        assertEquals("somethingelse@greatschools.net", command.getUserEmail());

        assertEquals("", command.getFriendEmail());
        assertEquals("", command.getMessage());
        assertEquals(0, command.getSchoolId());
    }

    public void testPassInaSchoolIdAsParameter() {
        MailToFriendCommand command = new MailToFriendCommand();
        BindException errors = new BindException(command, "");

        getSessionContext().setState(State.CA);

        command.setSchoolId(1);
        _controller.onBindOnNewForm(getRequest(), command, errors);

        assertTrue(command.getMessage().indexOf(SCHOOL_TEST_NAME) > -1);
        //link to school profile page is part of the message
        assertTrue(command.getMessage().indexOf("browse_school") > -1);
        assertTrue(command.getSubject().indexOf(SCHOOL_TEST_NAME) > -1);
    }

    public void testFriendsStringtoFriendsArrayInCommand() {
        MailToFriendCommand command = new MailToFriendCommand();
        command.setFriendEmail("dlee@greatschools.net");

        String [] emails = command.getFriendEmails();

        assertEquals(1, emails.length);
        assertEquals("dlee@greatschools.net", emails[0]);

        //trim whitespace
        command.setFriendEmail(" dlee@greatschools.net, david@greatschools.net ");
        assertEquals("dlee@greatschools.net,david@greatschools.net", command.getFriendEmail());

        emails = command.getFriendEmails();
        assertEquals(2, emails.length);
        assertEquals("dlee@greatschools.net", emails[0]);
        assertEquals("david@greatschools.net", emails[1]);        
    }

    //test emails validate correctly
    public void testValidation() {
        MailToFriendCommand command = new MailToFriendCommand();
        BindException errors = new BindException(command, "");

        //don't set anything
        _controller.onBind(getRequest(), command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(4, errors.getAllErrors().size());

        //set bogus user email
        command.setUserEmail("dlee");
        errors = new BindException(command, "");
        _controller.onBind(getRequest(), command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(4, errors.getAllErrors().size());

        command.setUserEmail("dlee@greatschools.net");
        errors = new BindException(command, "");
        _controller.onBind(getRequest(), command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(3, errors.getAllErrors().size());

        command.setMessage("message");
        errors = new BindException(command, "");
        _controller.onBind(getRequest(), command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(2, errors.getAllErrors().size());

        command.setSubject("subject");
        errors = new BindException(command, "");
        _controller.onBind(getRequest(), command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getAllErrors().size());

        command.setFriendEmail("dlee@greatschools.net,dlee");
        errors = new BindException(command, "");
        _controller.onBind(getRequest(), command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getAllErrors().size());

        command.setFriendEmail("dlee@greatschools.net,dlee@david.com");
        errors = new BindException(command, "");
        _controller.onBind(getRequest(), command, errors);
        assertFalse(errors.hasErrors());
    }

    public void testDoSubmitAction() {
        MockJavaMailSender sender = new MockJavaMailSender();
        //must set a host to some value
        sender.setHost("hithere.com");
        MailToFriendCommand command = new MailToFriendCommand();
        command.setUserEmail("dlee@greatschools.net");
        command.setFriendEmail("dlee@greatschools.net");

        _controller.setMailSender(sender);
        _controller.doSubmitAction(command);

        assertEquals(1, sender.getSentMessages().size());

    }

}
