package gs.web.community;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.util.email.MockJavaMailSender;
import gs.data.content.IArticleDao;
import gs.data.content.Article;
import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContext;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import static org.easymock.EasyMock.*;

/**
 * @author <a href="mailto:aroy@greatschools.net">Anthony Roy</a>
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class MailToFriendControllerTest extends BaseControllerTestCase {
    private MailToFriendController _controller;
    private ISchoolDao _schoolDao;
    private IArticleDao _articleDao;
    private MockJavaMailSender _sender;
    private School _school;
    private static final String SCHOOL_TEST_NAME = "TestNameSchool";

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new MailToFriendController();

        _sender = new MockJavaMailSender();
        _sender.setHost("mail.greatschools.net");
        _controller.setMailSender(_sender);

        _school = new School();
        _school.setId(new Integer("1"));
        _school.setName(SCHOOL_TEST_NAME);
        _school.setActive(true);
        _school.setDatabaseState(State.CA);

        _schoolDao = createStrictMock(ISchoolDao.class);
        _controller.setSchoolDao(_schoolDao);
        _articleDao = createStrictMock(IArticleDao.class);
        _controller.setArticleDao(_articleDao);
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
        assertEquals(0, command.getArticleId());
    }

    public void testPassInaSchoolIdAsParameter() {
        MailToFriendCommand command = new MailToFriendCommand();
        BindException errors = new BindException(command, "");

        getSessionContext().setState(State.CA);

        command.setSchoolId(1);
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(_school);
        replay(_schoolDao);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        verify(_schoolDao);

        assertTrue(command.getMessage().indexOf(SCHOOL_TEST_NAME) > -1);
        //link to school profile page is part of the message
        assertTrue(command.getMessage().indexOf("browse_school") > -1);
        assertTrue(command.getSubject().indexOf(SCHOOL_TEST_NAME) > -1);
    }

    public void testArticleId() {
        MailToFriendCommand command = new MailToFriendCommand();
        BindException errors = new BindException(command, "");

        getSessionContext().setState(State.CA);

        command.setArticleId(1);
        Article article = new Article();
        article.setId(1);

        expect(_articleDao.getArticleFromId(1)).andReturn(article);
        replay(_articleDao, _schoolDao );
        _controller.onBindOnNewForm(getRequest(), command, errors);
        verify(_articleDao, _schoolDao);

        assertTrue(command.getMessage().indexOf("helpful resource") > -1);
        assertTrue(command.getMessage().indexOf("article") > -1);
        assertTrue(command.getMessage().indexOf("1") > -1);
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
        MailToFriendCommand command = new MailToFriendCommand();
        command.setUserEmail("dlee@greatschools.net");
        command.setFriendEmail("dlee@greatschools.net");

        command.setRefer("School Profile");

        _controller.setMailSender(_sender);
        ModelAndView mv = _controller.onSubmit(command);

        assertEquals(1, _sender.getSentMessages().size());
        assertEquals("School Profile", (String) mv.getModel().get("refer"));
    }

    public void testDoSubmitActionAuthorizer() {
        MailToFriendCommand command = new MailToFriendCommand();
        command.setUserEmail("dlee@greatschools.net");
        command.setFriendEmail("dlee@greatschools.net");

        command.setRefer("authorizer");

        ModelAndView mv = _controller.onSubmit(command);

        assertEquals(1, _sender.getSentMessages().size());
        assertEquals("authorizer", (String) mv.getModel().get("refer"));        
    }

    //command class tests
    public void testReferForCommand() {
        MailToFriendCommand command = new MailToFriendCommand();
        command.setRefer("overview");

        assertEquals("School Profile Overview", command.getRefer());

        command.setRefer("ratings");
        assertEquals("School Profile Rankings", command.getRefer());

        command.setRefer("School Profile Overview");
        assertEquals("School Profile Overview", command.getRefer());

        command.setRefer("article");
        assertEquals("article", command.getRefer());

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

}
