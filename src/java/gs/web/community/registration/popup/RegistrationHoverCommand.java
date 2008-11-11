package gs.web.community.registration.popup;

import gs.web.community.registration.UserCommand;
import gs.web.util.validator.EmailValidator;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationHoverCommand extends UserCommand implements EmailValidator.IEmail{
    private boolean _mslOnly;

    public RegistrationHoverCommand() {
        super();
    }

    public boolean isMslOnly() {
        return _mslOnly;
    }

    public void setMslOnly(boolean mslOnly) {
        _mslOnly = mslOnly;
    }
}
