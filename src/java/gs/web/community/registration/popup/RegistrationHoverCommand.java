package gs.web.community.registration.popup;

import gs.web.community.registration.UserCommand;
import gs.web.util.validator.EmailValidator;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationHoverCommand extends UserCommand implements EmailValidator.IEmail{
    private boolean _mslOnly;
    private String _how;

    public RegistrationHoverCommand() {
        super();
    }

    public boolean isMslOnly() {
        return _mslOnly;
    }

    public void setMslOnly(boolean mslOnly) {
        _mslOnly = mslOnly;
    }

    public String getHow() {
        return _how;
    }

    public void setHow(String how) {
        _how = how;
    }
}
