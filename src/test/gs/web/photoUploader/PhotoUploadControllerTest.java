package gs.web.photoUploader;

import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.school.ISchoolMediaDao;
import gs.data.school.SchoolMedia;
import gs.data.school.SchoolMediaDaoHibernate;
import gs.data.security.Role;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.school.EspFormValidationHelper;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.fileupload.FileItemStream;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.*;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class PhotoUploadControllerTest extends BaseControllerTestCase {
    
    FileItemStream fileItemStream;
    ISchoolMediaDao _schoolMediaDao;
    
    PhotoUploadController _controller;
    PhotoUploadController _mockedController;
    EspFormValidationHelper _espFormValidationHelper;

    SchoolPhotoProcessor _processor;
    
    private User _user;

    public void setUp() throws Exception {
        super.setUp();
        fileItemStream = createStrictMock(FileItemStream.class);
        _schoolMediaDao = createStrictMock(ISchoolMediaDao.class);
        _controller = new PhotoUploadController();
        _mockedController = new MockedUploadTestController();
        _processor = org.easymock.classextension.EasyMock.createStrictMock(SchoolPhotoProcessor.class);
        _espFormValidationHelper = org.easymock.classextension.EasyMock.createStrictMock(EspFormValidationHelper.class);
        _controller.setEspFormValidationHelper(_espFormValidationHelper);
        Role superuser = new Role();
        superuser.setKey(Role.ESP_SUPERUSER);
        _user = new User();

        _controller.setSchoolMediaDao(_schoolMediaDao);
    }

    void authorizeUser() {
        _user.setId(1);
        _user.setEmail("ssprouse@greatschools.org");
        try {
            _user.setPlaintextPassword("foobar");
            _user.setUserProfile(new UserProfile());
            PageHelper.setMemberAuthorized(getRequest(), getResponse(), _user);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        getRequest().setCookies(getResponse().getCookies());

        SessionContextUtil.getSessionContext(getRequest()).setUser(_user);

    }

    public void testHandleDelete_userHasAccess() throws Exception {
        authorizeUser();
        int mediaId = 1;
        int schoolId = 1;
        State schoolDatabaseState = State.CA;
        
        SchoolMedia schoolMedia = new SchoolMedia();
        schoolMedia.setId(mediaId);
        schoolMedia.setSchoolId(schoolId);
        schoolMedia.setSchoolState(schoolDatabaseState);

        expect(_schoolMediaDao.getById(eq(mediaId))).andReturn(schoolMedia);
        expect(_espFormValidationHelper.checkUserHasAccess(eq(_user), eq(State.CA), eq(new Integer(schoolId)))).andReturn(true);
        schoolMedia.setStatus(SchoolMediaDaoHibernate.Status.DELETED.value);
        _schoolMediaDao.save(eq(schoolMedia));
        org.easymock.classextension.EasyMock.replay(_espFormValidationHelper);
        replay(_schoolMediaDao);
        
        _controller.handleDelete(mediaId, schoolId, schoolDatabaseState, getRequest(), getResponse());

        org.easymock.classextension.EasyMock.verify(_espFormValidationHelper);
        verify(_schoolMediaDao);
        
        assertFalse(getResponse().getContentAsString().contains("error"));
    }

    public void testHandleDelete_userDoesntHaveAccess() throws Exception {
        authorizeUser();
        int mediaId = 1;
        int schoolId = 1;
        State schoolDatabaseState = State.CA;

        SchoolMedia schoolMedia = new SchoolMedia();
        schoolMedia.setId(mediaId);
        schoolMedia.setSchoolId(schoolId);
        schoolMedia.setSchoolState(schoolDatabaseState);

        expect(_schoolMediaDao.getById(eq(mediaId))).andReturn(schoolMedia);
        expect(_espFormValidationHelper.checkUserHasAccess(eq(_user), eq(State.CA), eq(new Integer(schoolId)))).andReturn(false);
        org.easymock.classextension.EasyMock.replay(_espFormValidationHelper);
        replay(_schoolMediaDao);

        _controller.handleDelete(mediaId, schoolId, schoolDatabaseState, getRequest(), getResponse());

        org.easymock.classextension.EasyMock.verify(_espFormValidationHelper);
        verify(_schoolMediaDao);

        assertTrue(getResponse().getContentAsString().contains("error"));
    }

    public void testHandleDeleteFailure() throws Exception {
        int mediaId = new Integer(1);
        int schoolId = new Integer(1);
        State schoolDatabaseState = State.CA;

        expect(_schoolMediaDao.getById(eq(mediaId))).andReturn(null);
        replay(_schoolMediaDao);

        _controller.handleDelete(mediaId, schoolId, schoolDatabaseState, getRequest(), getResponse());

        verify(_schoolMediaDao);

        assertTrue(getResponse().getContentAsString().contains("error"));
    }

    public void xtestHandlePost() throws Exception {
        getRequest().setContentType("multipart/form-data; boundary=\"blah\"");
        getRequest().setMethod("post");
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] bytes = decoder.decodeBuffer("YmxhaA==/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEABALDA4MChAODQ4SERATGCgaGBYWGDEjJR0oOjM9PDkzODdASFxOQERXRTc4UG1RV19iZ2hnPk1xeXBkeFxlZ2MBERISGBUYLxoaL2NCOEJjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY//AABEIADIAMgMBEQACEQEDEQH/xAGiAAABBQEBAQEBAQAAAAAAAAAAAQIDBAUGBwgJCgsQAAIBAwMCBAMFBQQEAAABfQECAwAEEQUSITFBBhNRYQcicRQygZGhCCNCscEVUtHwJDNicoIJChYXGBkaJSYnKCkqNDU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6g4SFhoeIiYqSk5SVlpeYmZqio6Slpqeoqaqys7S1tre4ubrCw8TFxsfIycrS09TV1tfY2drh4uPk5ebn6Onq8fLz9PX29/j5+gEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoLEQACAQIEBAMEBwUEBAABAncAAQIDEQQFITEGEkFRB2FxEyIygQgUQpGhscEJIzNS8BVictEKFiQ04SXxFxgZGiYnKCkqNTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqCg4SFhoeIiYqSk5SVlpeYmZqio6Slpqeoqaqys7S1tre4ubrCw8TFxsfIycrS09TV1tfY2dri4+Tl5ufo6ery8/T19vf4+fr/2gAMAwEAAhEDEQA/AJ9Z1G8i1e5SO6mRFbAVXIA4oAqDVL//AJ/J/wDvs0AI2qX/APz+z/8Afw0ARNq2of8AP7cf9/DQBE2sakOl/cf9/DQBC2s6mP8AmIXP/f00Aen27FreMkkkqCfyoA4HXjjW7v8A3/6CgBdK0q41QS/Z3jXy8Z3kjrn0B9KALsnhS/WNmMtudoJwGbP/AKDQBzrGgCFzQBA5oA9etv8Aj1h/3B/KgDz7xA2Ndu/9/wDoKANrwSS0V+BycJ/7NQBkTaNq0MLyyW7qiKWY7xwB170ATW+j219oEt5aySm6hB3xkgjjk44z06UAQXuk29joEN3cySi7n5jiBAAHXJ4z0/mKAOdc0wPYbX/j1h/3F/lSA858RtjX7z/f/oKANvwM37nUSOoCfyagDAl1vUZY2jkvJWRgQyluCD2oA2PCG+zgvdTncpaIm0j++Rzx9On40AJ43geZbXU4XMlq6BR6LnkH8f6UAca7UwPZbX/j1h/3F/lSA8z8TNjxDej/AG/6CmBBYaveaasq2k3liXAf5Qc4zjqPc0AUi9AFqXV7yTTlsGmH2VMEIFUe/JAyaAE/tu/XTTp/ng2pBGxkU989SM9aQGU70Ae12v8Ax6w/7i/yoA8w8UK//CR3uEYjf2HsKYGXtk/55v8AkaAArJ/zzb8jQAwrJ/zzf8jQBGySn/lm/wD3yaQDfJlP/LN/++TQB7Zag/ZYeP4F/lQA9o0LElFJ9xQAvlR/880/75FAB5Uf/PNP++RQAeVH/wA80/75FAB5Uf8AzzT/AL5FMA8qP/nmn/fIoAcBxSA//9k=YmxhaC0t");
        getRequest().setContent( bytes);

        _processor.handleScaledPhoto(isA(User.class), eq(PhotoUploadController.FULL_SIZE_IMAGE_MAX_DIMENSION));
        _mockedController.handlePost(getRequest(), getResponse());

        assertFalse(getResponse().getContentAsString().contains("error"));
    }

    class MockedUploadTestController extends PhotoUploadController {
        @Override
        public SchoolPhotoProcessor createSchoolPhotoProcessor(FileItemStream fileItemStream) throws IOException {
            return _processor;
        }
    }
}
