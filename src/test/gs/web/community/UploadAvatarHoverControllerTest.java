package gs.web.community;

import gs.web.BaseControllerTestCase;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class UploadAvatarHoverControllerTest extends BaseControllerTestCase {
    private UploadAvatarHoverController _controller;
    private UploadAvatarCommand _command;
    private BindException _errors;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _controller = new UploadAvatarHoverController();

        _controller.setFormView("formView");

        _command = new UploadAvatarCommand();

        _errors = new BindException(_command, "");
    }

    public void testValidation() throws Exception {
        assertFalse(_errors.hasErrors());
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        assertTrue("Expect errors on empty command", _errors.hasErrors());

        _errors = new BindException(_command, "");
        _command.setStockPhoto("1");
        assertFalse(_errors.hasErrors());
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        assertFalse("Expect no errors if stock photo is chosen", _errors.hasErrors());

        _errors = new BindException(_command, "");
        _command.setStockPhoto(null);
        MultipartFile file = new MockMultipartFile("avatar", "mypic.jpg", "image/jpeg", new byte[0]);
        _command.setAvatar(file);
        assertFalse(_errors.hasErrors());
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        assertTrue("Expect errors if empty avatar file is uploaded", _errors.hasErrors());

        _errors = new BindException(_command, "");
        file = new MockMultipartFile("avatar", "mypic.png", "image/png", new byte[1]);
        _command.setAvatar(file);
        assertFalse(_errors.hasErrors());
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        assertTrue("Expect errors if png avatar file is uploaded", _errors.hasErrors());

        _errors = new BindException(_command, "");
        file = new MockMultipartFile("avatar", "mypic.jpg", "image/jpeg", new byte[1]);
        _command.setAvatar(file);
        assertFalse(_errors.hasErrors());
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        assertTrue("Expect errors if invalid avatar file is uploaded", _errors.hasErrors());
    }
}
