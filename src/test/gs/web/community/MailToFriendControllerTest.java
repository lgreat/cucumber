package gs.web.community;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.util.email.MockJavaMailSender;
import gs.data.util.CmsUtil;
import gs.data.util.Address;
import gs.data.content.IArticleDao;
import gs.data.content.Article;
import gs.data.content.cms.ICmsFeatureDao;
import gs.data.content.cms.CmsFeature;
import gs.data.content.cms.ContentKey;
import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContext;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import static org.easymock.EasyMock.*;

import java.util.List;

/**
 * @author <a href="mailto:aroy@greatschools.org">Anthony Roy</a>
 * @author <a href="mailto:dlee@greatschools.org">David Lee</a>
 */
public class MailToFriendControllerTest extends BaseControllerTestCase {
    private MailToFriendController _controller;
    private ISchoolDao _schoolDao;
    private IArticleDao _articleDao;
    private MockJavaMailSender _sender;
    private School _school;
    private static final String SCHOOL_TEST_NAME = "TestNameSchool";
    private BindException _errors;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new MailToFriendController();

        _sender = new MockJavaMailSender();
        _sender.setHost("mail.greatschools.org");
        _controller.setMailSender(_sender);

        _school = new School();
        _school.setId(new Integer("1"));
        _school.setName(SCHOOL_TEST_NAME);
        _school.setActive(true);
        _school.setDatabaseState(State.CA);
        Address address = new Address("123 way", "CityName", State.CA, "12345");
        _school.setPhysicalAddress(address);

        _schoolDao = createStrictMock(ISchoolDao.class);
        _controller.setSchoolDao(_schoolDao);
        _articleDao = createStrictMock(IArticleDao.class);
        _controller.setArticleDao(_articleDao);

    }

    public void testGetEmailFromSessionOnBind() {
        MailToFriendCommand command = new MailToFriendCommand();
        BindException errors = new BindException(command, "");

        SessionContext sessionContext = new SessionContext();
        sessionContext.setEmail("dlee@greatschools.org");

        getRequest().setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        assertFalse(errors.hasErrors());
        assertEquals("dlee@greatschools.org", command.getUserEmail());

        //if user specifies his own email, that should override the one in the session
        command.setUserEmail("somethingelse@greatschools.org");
        _controller.onBindOnNewForm(getRequest(), command, errors);
        assertEquals("somethingelse@greatschools.org", command.getUserEmail());

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
        assertTrue(command.getMessage().indexOf("/california/cityname/1-TestNameSchool/") > -1);
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

    public void testFeatureId() {

        boolean cmsEnabled = CmsUtil.isCmsEnabled();

        CmsUtil.setCmsEnabled(true);

        MailToFriendCommand command = new MailToFriendCommand();
        BindException errors = new BindException(command, "");

        command.setFeatureId(1);
        _controller.setCmsFeatureDao(new ICmsFeatureDao() {
            public CmsFeature get(Long contentId) {

                assertEquals("Wrong content Id", contentId, new Long(1));

                CmsFeature feature = new CmsFeature();
                feature.setContentKey(new ContentKey("Article", 1L));
                feature.setFullUri("/full/uri");
                return feature;
            }

            public List<CmsFeature> getAll() {
                return null;
            }

            public CmsFeature get(Long contentId, String language) {
                return null;
            }
        });

        _controller.onBindOnNewForm(getRequest(), command, errors);

        assertTrue(command.getMessage().indexOf("helpful resource") > -1);
        assertTrue(command.getMessage().indexOf("/full/1-uri.gs") > -1);

        CmsUtil.setCmsEnabled(cmsEnabled);
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

        command.setUserEmail("dlee@greatschools.org");
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

        command.setFriendEmail("dlee@greatschools.org,dlee");
        errors = new BindException(command, "");
        _controller.onBind(getRequest(), command, errors);
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getAllErrors().size());

        command.setFriendEmail("dlee@greatschools.org,dlee@david.com");
        errors = new BindException(command, "");
        _controller.onBind(getRequest(), command, errors);
        assertFalse(errors.hasErrors());
    }

    public void testDoSubmitAction() throws Exception {
        MailToFriendCommand command = new MailToFriendCommand();
        _errors = new BindException(command, "");
        command.setUserEmail("dlee@greatschools.org");
        command.setFriendEmail("dlee@greatschools.org");

        command.setRefer("School Profile");

        _controller.setMailSender(_sender);
        ModelAndView mv = _controller.onSubmit(getRequest(), getResponse(), command, _errors);

        assertEquals(1, _sender.getSentMessages().size());
        assertEquals("School Profile", (String) mv.getModel().get("refer"));
    }

    public void testDoSubmitActionAuthorizer() throws Exception {
        MailToFriendCommand command = new MailToFriendCommand();
        _errors = new BindException(command, "");
        command.setUserEmail("dlee@greatschools.org");
        command.setFriendEmail("dlee@greatschools.org");

        command.setRefer("authorizer");

        ModelAndView mv = _controller.onSubmit(getRequest(), getResponse(), command, _errors);

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
        command.setFriendEmail("dlee@greatschools.org");

        String [] emails = command.getFriendEmails();

        assertEquals(1, emails.length);
        assertEquals("dlee@greatschools.org", emails[0]);

        //trim whitespace
        command.setFriendEmail(" dlee@greatschools.org, david@greatschools.org ");
        assertEquals("dlee@greatschools.org,david@greatschools.org", command.getFriendEmail());

        emails = command.getFriendEmails();
        assertEquals(2, emails.length);
        assertEquals("dlee@greatschools.org", emails[0]);
        assertEquals("david@greatschools.org", emails[1]);
    }

}
