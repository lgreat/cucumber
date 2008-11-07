package gs.web.community.registration.popup;

import gs.web.community.registration.LoginCommand;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class LoginHoverCommand extends LoginCommand {
    private boolean _joinError = false;

    public boolean isJoinError() {
        return _joinError;
    }

    public void setJoinError(boolean joinError) {
        _joinError = joinError;
    }
}
