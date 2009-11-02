package gs.web.community;

import gs.web.BaseControllerTestCase;
import org.springframework.validation.BindException;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
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
        _command.setAvatar(new byte[0]);
        assertFalse(_errors.hasErrors());
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        assertTrue("Expect errors if empty avatar file is uploaded", _errors.hasErrors());

        _errors = new BindException(_command, "");
        _command.setAvatar(new byte[1]);
        assertFalse(_errors.hasErrors());
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        assertFalse("Expect no errors if non-empty avatar file is uploaded", _errors.hasErrors());
    }
}
