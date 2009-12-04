package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.data.community.*;
import gs.data.security.Role;
import gs.data.security.Permission;

import static org.easymock.EasyMock.*;
import org.springframework.validation.BindException;

import java.util.Set;
import java.util.HashSet;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class DeactivateContentAjaxControllerTest extends BaseControllerTestCase {
    private DeactivateContentAjaxController _controller;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private DeactivateContentCommand _command;
    private BindException _errors;
    private User _user;
    private Role _moderator;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _controller = new DeactivateContentAjaxController();

        _discussionDao = createStrictMock(IDiscussionDao.class);
        _discussionReplyDao = createStrictMock(IDiscussionReplyDao.class);

        _controller.setDiscussionDao(_discussionDao);
        _controller.setDiscussionReplyDao(_discussionReplyDao);

        _command = new DeactivateContentCommand();

        _errors = new BindException(_command, "DeactivateContentCommand");

        _user = new User();
        _user.setUserProfile(new UserProfile());

        _moderator = new Role();
        _moderator.setKey(Role.CMS_MODERATOR);
        Set<Permission> modPerms = new HashSet<Permission>(1);
        Permission canView = new Permission();
        canView.setKey(Permission.COMMUNITY_VIEW_REPORTED_POSTS);
        modPerms.add(canView);
        _moderator.setPermissions(modPerms);
    }

    public void replayAllMocks() {
        replayMocks(_discussionDao, _discussionReplyDao);
    }

    public void verifyAllMocks() {
        verifyMocks(_discussionDao, _discussionReplyDao);
    }

    public void testBasics() {
        assertSame(_discussionDao, _controller.getDiscussionDao());
        assertSame(_discussionReplyDao, _controller.getDiscussionReplyDao());
    }

    public void testOnSubmitNothing() throws Exception {
        replayAllMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyAllMocks();
    }

    public void testOnSubmitReplyDeactivate() throws Exception {
        _sessionContext.setUser(_user);
        _user.addRole(_moderator);

        _command.setContentId(1);
        _command.setContentType(DeactivateContentCommand.ContentType.reply);
        _command.setReactivate(false);
        
        DiscussionReply reply = new DiscussionReply();
        reply.setId(1);

        expect(_discussionReplyDao.findById(1)).andReturn(reply);

        _discussionReplyDao.save(reply);
        replayAllMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyAllMocks();

        assertFalse(reply.isActive());
    }

    public void testOnSubmitReplyReactivate() throws Exception {
        _sessionContext.setUser(_user);
        _user.addRole(_moderator);

        _command.setContentId(1);
        _command.setContentType(DeactivateContentCommand.ContentType.reply);
        _command.setReactivate(true);

        DiscussionReply reply = new DiscussionReply();
        reply.setId(1);
        reply.setActive(false);

        expect(_discussionReplyDao.findById(1)).andReturn(reply);

        _discussionReplyDao.save(reply);
        replayAllMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyAllMocks();

        assertTrue(reply.isActive());
    }

    public void testOnSubmitReplyNoop() throws Exception {
        _sessionContext.setUser(_user);
        _user.addRole(_moderator);

        _command.setContentId(1);
        _command.setContentType(DeactivateContentCommand.ContentType.reply);
        _command.setReactivate(false);

        DiscussionReply reply = new DiscussionReply();
        reply.setId(1);
        reply.setActive(false);

        expect(_discussionReplyDao.findById(1)).andReturn(reply);
        replayAllMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyAllMocks();

        assertFalse(reply.isActive());        
    }

    public void testOnSubmitDiscussionDeactivate() throws Exception {
        _sessionContext.setUser(_user);
        _user.addRole(_moderator);

        _command.setContentId(1);
        _command.setContentType(DeactivateContentCommand.ContentType.discussion);
        _command.setReactivate(false);

        Discussion discussion= new Discussion();
        discussion.setId(1);

        expect(_discussionDao.findById(1)).andReturn(discussion);

        _discussionDao.save(discussion);
        replayAllMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyAllMocks();

        assertFalse(discussion.isActive());
    }

    public void testOnSubmitDiscussionReactivate() throws Exception {
        _sessionContext.setUser(_user);
        _user.addRole(_moderator);

        _command.setContentId(1);
        _command.setContentType(DeactivateContentCommand.ContentType.discussion);
        _command.setReactivate(true);

        Discussion discussion = new Discussion();
        discussion.setId(1);
        discussion.setActive(false);

        expect(_discussionDao.findById(1)).andReturn(discussion);

        _discussionDao.save(discussion);
        replayAllMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyAllMocks();

        assertTrue(discussion.isActive());
    }

    public void testOnSubmitDiscussionNoop() throws Exception {
        _sessionContext.setUser(_user);
        _user.addRole(_moderator);

        _command.setContentId(1);
        _command.setContentType(DeactivateContentCommand.ContentType.discussion);
        _command.setReactivate(false);

        Discussion discussion = new Discussion();
        discussion.setId(1);
        discussion.setActive(false);

        expect(_discussionDao.findById(1)).andReturn(discussion);
        replayAllMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyAllMocks();

        assertFalse(discussion.isActive());
    }

    public void testOnSubmitDiscussionNoPermission() throws Exception {
        _sessionContext.setUser(_user);

        _command.setContentId(1);
        _command.setContentType(DeactivateContentCommand.ContentType.discussion);
        _command.setReactivate(true);

        replayAllMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyAllMocks();
    }

    public void testOnSubmitDiscussionReplyNoPermission() throws Exception {
        _sessionContext.setUser(_user);

        _command.setContentId(1);
        _command.setContentType(DeactivateContentCommand.ContentType.reply);
        _command.setReactivate(true);

        replayAllMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verifyAllMocks();
    }
}
